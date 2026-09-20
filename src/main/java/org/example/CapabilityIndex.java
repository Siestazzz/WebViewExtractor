package org.example;

import java.nio.file.Path;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.iface.instruction.*;

/** One DEX pass for structure and call references. Bodies are decoded on demand. */
final class CapabilityIndex {
    final Map<String,ClassDef> classes=new LinkedHashMap<>();
    final Map<String,Method> methods=new LinkedHashMap<>();
    final Map<String,Set<String>> callers=new HashMap<>(), calls=new HashMap<>();
    final Map<String,List<Method>> byClass=new HashMap<>();
    final Map<String,List<Method>> byShape=new HashMap<>();
    final Set<String> relevant=new HashSet<>(), seeds=new HashSet<>();
    final Set<String> classLiterals=new HashSet<>();
    final Map<String,Set<String>> referencedFields=new HashMap<>(), allocations=new HashMap<>(),fieldWriters=new HashMap<>();
    final Map<String,Set<String>> reflectionFields=new HashMap<>();
    final Map<String,String> messageRegistries=new HashMap<>();
    final Map<String,Set<List<String>>> namespaceRegistries=new HashMap<>();
    final Map<String,Set<String>> registryHandlerShapes=new HashMap<>();
    final Map<String,List<String>> subtypeCandidates=new HashMap<>();
    final Set<String> bindingObjects=new HashSet<>();
    final Map<String,Set<String>> callbackEntries=new HashMap<>();
    final Map<String,Boolean> subtypeCache=new HashMap<>();
    final List<String> diagnostics=new ArrayList<>();
    long instructions;
    static String cls(String desc){return Util.className(desc);}
    static String key(MethodReference m){return m.getDefiningClass()+"->"+shape(m);}
    static String shape(MethodReference m){return m.getName()+"("+String.join("",m.getParameterTypes())+")"+m.getReturnType();}
    static String field(FieldReference f){return f.getDefiningClass()+"->"+f.getName()+":"+f.getType();}
    static String display(MethodReference m){return cls(m.getDefiningClass())+"."+m.getName()+"("+String.join(",",m.getParameterTypes().stream().map(Object::toString).toList())+"):"+m.getReturnType();}
    void read(Path apk,long deadline) throws Exception {
        var container=DexFileFactory.loadDexContainer(apk.toFile(),Opcodes.getDefault());
        for(String name:container.getDexEntryNames())for(ClassDef c:container.getEntry(name).getDexFile().getClasses()){
            classes.put(cls(c.getType()),c);
            for(Method m:c.getMethods()){
                methods.put(key(m),m); byClass.computeIfAbsent(cls(m.getDefiningClass()),k->new ArrayList<>()).add(m);
                byShape.computeIfAbsent(shape(m),k->new ArrayList<>()).add(m);
            }
        }
        for(ClassDef c:classes.values())for(Field f:c.getFields())if((f.getAccessFlags()&8)==0&&(webview(cls(f.getType()))||settings(cls(f.getType()))))bindingObjects.add(cls(c.getType()));
        for(Method m:methods.values())if(m.getName().equals("invoke")&&m.getParameterTypes().isEmpty()&&webview(cls(m.getReturnType())))bindingObjects.add(cls(m.getDefiningClass()));
        for(Method m:methods.values()){
            if(System.nanoTime()>deadline)throw new IllegalStateException("index_deadline");
            if(m.getImplementation()==null)continue;
            String id=key(m); Set<String> refs=new HashSet<>(), fields=new HashSet<>();
            try {for(Instruction i:m.getImplementation().getInstructions()){
                instructions++;
                if(i instanceof ReferenceInstruction rr){
                    if(rr.getReference() instanceof FieldReference f){fields.add(field(f));if(i.getOpcode().name.startsWith("iput")||i.getOpcode().name.startsWith("sput"))fieldWriters.computeIfAbsent(field(f),k->new HashSet<>()).add(id);}
                    if(i.getOpcode()==Opcode.CONST_CLASS&&rr.getReference() instanceof TypeReference tr)classLiterals.add(cls(tr.getType()));
                    if(i.getOpcode()==Opcode.NEW_INSTANCE&&rr.getReference() instanceof TypeReference tr)allocations.computeIfAbsent(id,k->new HashSet<>()).add(cls(tr.getType()));
                }
                if(i instanceof ReferenceInstruction r&&r.getReference() instanceof MethodReference target && i.getOpcode().name.startsWith("invoke-")){
                    refs.add(key(target));
                    if(kind(target)!=null)seeds.add(id);
                }
            }}catch(RuntimeException ex){diagnostics.add("method_index_failed:"+id+":"+ex.getClass().getSimpleName());}
            calls.put(id,refs);referencedFields.put(id,fields);
            for(String ref:refs)callers.computeIfAbsent(ref,k->new HashSet<>()).add(id);
        }
        // Resolve inherited calls to their actual implementation before reverse closure.
        for(var e:new ArrayList<>(calls.entrySet()))for(String ref:new ArrayList<>(e.getValue())){
            Method m=resolve(ref);if(m!=null){String target=key(m);e.getValue().add(target);callers.computeIfAbsent(target,k->new HashSet<>()).add(e.getKey());}
        }
        discoverMessageRegistries(deadline);
        for(var e:calls.entrySet())for(String ref:e.getValue())if(messageRegistries.containsKey(ref))seeds.add(e.getKey());
        // Abstract/interface dispatch candidates participate in reverse relevance, not just exact keys.
        for(Method implementation:methods.values()){
            if(implementation.getImplementation()==null)continue;
            if(!(seeds.contains(key(implementation))&&Set.of("addJavascriptInterface","getSettings","setWebViewClient","setWebChromeClient").contains(implementation.getName()))&&!implementation.getParameterTypes().stream().anyMatch(t->webview(cls(t.toString()))||settings(cls(t.toString())))&&!webview(cls(implementation.getReturnType())))continue;
            for(Method declaration:byShape.getOrDefault(shape(implementation),List.of())){
                if(declaration.getImplementation()!=null)continue;
                if(subtype(cls(implementation.getDefiningClass()),cls(declaration.getDefiningClass())))
                    for(String caller:callers.getOrDefault(key(declaration),Set.of()))callers.computeIfAbsent(key(implementation),k->new HashSet<>()).add(caller);
            }
        }
        Map<String,Integer> seedDistance=new HashMap<>();ArrayDeque<String> local=new ArrayDeque<>(seeds);for(String seed:seeds)seedDistance.put(seed,0);
        while(!local.isEmpty()){String id=local.remove();int distance=seedDistance.get(id);if(distance>=3)continue;for(String caller:callers.getOrDefault(id,Set.of()))if(!seedDistance.containsKey(caller)){seedDistance.put(caller,distance+1);local.add(caller);}}
        Set<String> carriers=new HashSet<>();for(String seed:seeds){Method m=methods.get(seed);if(m!=null)carriers.add(cls(m.getDefiningClass()));}
        // Resolve capability-carrier overrides only near a real registration/configuration site.
        // This includes concrete SDK adapter base methods, not only abstract declarations.
        for(var entry:seedDistance.entrySet())if(entry.getValue()<=2){Method implementation=methods.get(entry.getKey());if(implementation==null||implementation.getName().startsWith("<"))continue;
            String owner=cls(implementation.getDefiningClass());if(!carriers.contains(owner)||component(owner)||activity(owner))continue;
            for(Method declaration:byShape.getOrDefault(shape(implementation),List.of()))if(!declaration.getDefiningClass().equals(implementation.getDefiningClass())&&subtype(owner,cls(declaration.getDefiningClass())))
                for(String caller:callers.getOrDefault(key(declaration),Set.of()))callers.computeIfAbsent(key(implementation),k->new HashSet<>()).add(caller);
        }
        var queue=new ArrayDeque<>(seeds); relevant.addAll(seeds);
        while(!queue.isEmpty()) {
            String callee=queue.remove();
            for(String field:referencedFields.getOrDefault(callee,Set.of())){
                String type=cls(field.substring(field.indexOf(':')+1));
                boolean binding=webview(type)||settings(type)||bindingObjects.contains(type)||collection(type)&&(seeds.contains(callee)||webview(CapabilityEngine.owner(field)))||subtype(type,"android.webkit.WebViewClient")||subtype(type,"android.webkit.WebChromeClient")||subtype(type,"com.tencent.smtt.sdk.WebViewClient")||subtype(type,"com.tencent.smtt.sdk.WebChromeClient");
                if(binding)for(String writer:fieldWriters.getOrDefault(field,Set.of()))if(relevant.add(writer))queue.add(writer);
            }
            for(String caller:callers.getOrDefault(callee,Set.of()))if(relevant.add(caller))queue.add(caller);
            Method cm=methods.get(callee);
            if(cm!=null){String owner=cls(cm.getDefiningClass());
                if(carriers.contains(owner)&&!component(owner)&&!activity(owner)&&!cm.getName().startsWith("<"))
                    for(Method declaration:byShape.getOrDefault(shape(cm),List.of())){
                        String base=cls(declaration.getDefiningClass());
                        // Generic platform listeners do not identify a concrete SDK carrier.
                        if(base.startsWith("java.")||base.startsWith("android.")||base.startsWith("androidx.")||base.startsWith("kotlin."))continue;
                        if(!base.equals(owner)&&subtype(owner,base))for(String caller:callers.getOrDefault(key(declaration),Set.of()))if(relevant.add(caller))queue.add(caller);
                    }
                boolean callback=seedDistance.getOrDefault(callee,99)<=2&&!cm.getName().startsWith("<")&&(cm.getAccessFlags()&8)==0&&!component(owner)&&!activity(owner)&&byShape.getOrDefault(shape(cm),List.of()).stream().anyMatch(declaration->declaration.getImplementation()==null&&subtype(owner,cls(declaration.getDefiningClass())));
                if(callback)callbackEntries.computeIfAbsent(owner,k->new HashSet<>()).add(key(cm));
                if(callback||component(owner)||scheduled(owner)&&(cm.getName().equals("run")||cm.getName().equals("call")))for(Method init:byClass.getOrDefault(owner,List.of()))if(init.getName().equals("<init>")&&relevant.add(key(init)))queue.add(key(init));
            }
        }
    }
    void discoverMessageRegistries(long deadline){
        // Registries must share a Map with a JS or installed-client transport call chain.
        Set<String> transportFields=new HashSet<>(),transportCalls=new HashSet<>();Map<String,Set<List<String>>> namespaceFields=new HashMap<>();
        for(Method m:methods.values())if(m.getAnnotations().stream().anyMatch(a->a.getType().endsWith("/JavascriptInterface;"))||clientTransport(m)){
            bindingObjects.add(cls(m.getDefiningClass()));
            Set<String> seen=new HashSet<>();ArrayDeque<String> q=new ArrayDeque<>();q.add(key(m));int budget=150;
            while(!q.isEmpty()&&budget>0){String id=q.remove();if(!seen.add(id))continue;budget--;
                transportFields.addAll(referencedFields.getOrDefault(id,Set.of()));transportCalls.addAll(calls.getOrDefault(id,Set.of()));
                for(String target:calls.getOrDefault(id,Set.of()))if(methods.containsKey(target))q.add(target);
                for(String type:allocations.getOrDefault(id,Set.of()))for(Method callback:byClass.getOrDefault(type,List.of())){
                    boolean async=scheduled(type)&&(callback.getName().equals("run")||callback.getName().equals("call"));
                    if(!async&&!callback.getName().startsWith("<")&&!callback.getParameterTypes().isEmpty()&&callback.getParameterTypes().get(0).equals("Ljava/lang/String;"))
                        async=byShape.getOrDefault(shape(callback),List.of()).stream().anyMatch(declaration->declaration.getImplementation()==null&&subtype(type,cls(declaration.getDefiningClass())));
                    if(async)q.addFirst(key(callback));
                }
                // Custom client adapters delegate framework callbacks into overridable helper methods.
                for(String target:calls.getOrDefault(id,Set.of())){Method helper=methods.get(target);if(helper==null)continue;String owner=cls(helper.getDefiningClass());
                    if(owner.startsWith("android.")||owner.startsWith("com.tencent.smtt.sdk."))continue;
                    if(!subtype(owner,"android.webkit.WebViewClient")&&!subtype(owner,"com.tencent.smtt.sdk.WebViewClient"))continue;
                    if(helper.getParameterTypes().stream().noneMatch(t->webview(cls(t.toString()))))continue;
                    for(Method implementation:byShape.getOrDefault(shape(helper),List.of()))if(implementation.getImplementation()!=null&&subtype(cls(implementation.getDefiningClass()),owner))q.addFirst(key(implementation));
                }
            }
            if(!q.isEmpty())diagnostics.add("transport_discovery_budget:"+key(m));
            Set<List<String>> shapes=new HashSet<>();boolean annotationGate=false;
            for(String id:seen){Method dispatch=methods.get(id);if(dispatch==null)continue;
                if(!calls.getOrDefault(id,Set.of()).stream().anyMatch(c->c.startsWith("Ljava/lang/Class;->getMethod(")||c.startsWith("Ljava/lang/reflect/Method;->getAnnotation(")||c.startsWith("Ljava/lang/reflect/Method;->isAnnotationPresent(")))continue;
                var summary=new DexFlow(this,deadline).summary(dispatch);
                for(var call:summary.calls()){
                    if((call.method().contains("->getAnnotation(")||call.method().contains("->isAnnotationPresent("))&&call.args().stream().anyMatch(v->v.kind().equals("class")&&"android.webkit.JavascriptInterface".equals(v.type())))annotationGate=true;
                    if(call.method().startsWith("Ljava/lang/Class;->getMethod(")&&call.args().size()==3){var array=call.args().get(2);TreeMap<Integer,String> params=new TreeMap<>();
                        if(array.kind().equals("array"))for(int i=0;i<array.args().size();i++)if(array.args().get(i).kind().equals("class"))params.put(i,array.args().get(i).id());
                        for(var write:summary.writes())if(write.receiver().equals(array)&&write.field().startsWith("$element:")&&write.value().kind().equals("class"))try{params.put(Integer.parseInt(write.field().substring(9)),write.value().id());}catch(NumberFormatException ignored){}
                        if(!params.isEmpty())shapes.add(List.copyOf(params.values()));
                    }
                }
            }
            boolean invokes=seen.stream().anyMatch(id->calls.getOrDefault(id,Set.of()).stream().anyMatch(c->c.startsWith("Ljava/lang/reflect/Method;->invoke(")));
            if(invokes&&annotationGate&&!shapes.isEmpty())for(String id:seen)for(String field:referencedFields.getOrDefault(id,Set.of()))namespaceFields.computeIfAbsent(field,k->new HashSet<>()).addAll(shapes);
            if(invokes)for(String id:seen)if(calls.getOrDefault(id,Set.of()).stream().anyMatch(c->c.startsWith("Ljava/lang/Class;->getMethod("))){
                Method resolver=methods.get(id);if(resolver==null)continue;
                for(DexFlow.Call c:new DexFlow(this,deadline).summary(resolver).calls())if(c.method().endsWith("->getClass()Ljava/lang/Class;")&&!c.args().isEmpty()){
                    var v=c.args().get(0);if(v.kind().equals("field")){reflectionFields.computeIfAbsent(key(m),k->new HashSet<>()).add(v.id());bindingObjects.add(CapabilityEngine.owner(v.id()));}
                }
            }
        }
        for(Method m:methods.values()){
            if(webview(cls(m.getDefiningClass()))&&m.getParameterTypes().equals(List.of("Ljava/lang/Object;","Ljava/lang/String;"))&&calls.getOrDefault(key(m),Set.of()).stream().anyMatch(c->c.contains("->put(Ljava/lang/Object;Ljava/lang/Object;)"))){
                var summary=new DexFlow(this,deadline).summary(m);
                for(var call:summary.calls())if(call.method().contains("->put(Ljava/lang/Object;Ljava/lang/Object;)")&&call.args().size()==3){var receiver=call.args().get(0);var name=call.args().get(1);var value=call.args().get(2);
                    if(receiver.kind().equals("field")&&namespaceFields.containsKey(receiver.id())&&DexFlow.alternatives(name).stream().anyMatch(v->v.kind().equals("param")&&v.id().equals("2"))&&value.kind().equals("param")&&value.id().equals("1")){
                        messageRegistries.put(key(m),receiver.id());namespaceRegistries.put(key(m),namespaceFields.get(receiver.id()));
                    }
                }
            }
            if(!(webview(cls(m.getDefiningClass()))||bindingObjects.contains(cls(m.getDefiningClass())))||m.getParameterTypes().size()!=2||!m.getParameterTypes().get(0).equals("Ljava/lang/String;"))continue;
            String handler=cls(m.getParameterTypes().get(1).toString());ClassDef hc=classes.get(handler);
            if(hc==null||(hc.getAccessFlags()&0x200)==0)continue;
            if(!byClass.getOrDefault(handler,List.of()).stream().anyMatch(hm->!hm.getParameterTypes().isEmpty()&&hm.getParameterTypes().get(0).equals("Ljava/lang/String;")))continue;
            if(!calls.getOrDefault(key(m),Set.of()).stream().anyMatch(c->c.contains("->put(Ljava/lang/Object;Ljava/lang/Object;)")))continue;
            DexFlow.Summary summary=new DexFlow(this,deadline).summary(m);
            for(DexFlow.Call call:summary.calls())if(call.method().contains("->put(Ljava/lang/Object;Ljava/lang/Object;)")&&call.args().size()==3){
                var receiver=call.args().get(0);var name=call.args().get(1);var value=call.args().get(2);
                if(receiver.kind().equals("field")&&transportFields.contains(receiver.id())&&name.kind().equals("param")&&name.id().equals("1")&&value.kind().equals("param")&&value.id().equals("2")){
                    messageRegistries.put(key(m),receiver.id());Set<String> shapes=new HashSet<>();for(Method hm:byClass.getOrDefault(handler,List.of()))if(transportCalls.contains(key(hm)))shapes.add(shape(hm));registryHandlerShapes.put(key(m),shapes);
                }
            }
        }
    }
    boolean clientTransport(Method m){
        String owner=cls(m.getDefiningClass());String shape=shape(m);
        for(String prefix:List.of("android.webkit.","com.tencent.smtt.sdk.")){
            String view="L"+prefix.replace('.', '/')+"WebView;";
            if(subtype(owner,prefix+"WebViewClient")&&(shape.equals("shouldOverrideUrlLoading("+view+"Ljava/lang/String;)Z")||shape.equals("onPageFinished("+view+"Ljava/lang/String;)V")))return true;
            if(subtype(owner,prefix+"WebChromeClient")&&shape.equals("onJsPrompt("+view+"Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;L"+prefix.replace('.', '/')+"JsPromptResult;)Z"))return true;
        }
        return false;
    }
    List<String> possibleTypes(String base){
        if(base==null||base.equals("java.lang.Object"))return List.of();
        return subtypeCandidates.computeIfAbsent(base,k->classes.keySet().stream().filter(t->subtype(t,base)).sorted().toList());
    }
    boolean subtype(String type,String base){
        if(type==null)return false;
        String cache=type+"|"+base;Boolean found=subtypeCache.get(cache);if(found!=null)return found;
        var q=new ArrayDeque<String>();q.add(type);var seen=new HashSet<String>();boolean result=false;
        while(!q.isEmpty()){
            String t=q.remove();if(!seen.add(t))continue;if(t.equals(base)){result=true;break;}
            ClassDef c=classes.get(t);if(c!=null){if(cls(c.getSuperclass())!=null)q.add(cls(c.getSuperclass()));for(String iface:c.getInterfaces())q.add(cls(iface));}
        }
        subtypeCache.put(cache,result);return result;
    }
    boolean webview(String t){return Cfg.WEBVIEWS.stream().anyMatch(b->subtype(t,b));}
    boolean settings(String t){return subtype(t,"android.webkit.WebSettings")||subtype(t,"com.tencent.smtt.sdk.WebSettings")||subtype(t,"com.uc.webview.export.WebSettings");}
    boolean activity(String t){return Cfg.ACTIVITIES.stream().anyMatch(b->subtype(t,b));}
    boolean collection(String t){return t!=null&&(Set.of("java.util.List","java.util.Collection","java.util.ArrayList","java.util.LinkedList","java.util.Set","java.util.HashSet").contains(t)||subtype(t,"java.util.Collection"));}
    boolean scheduled(String t){return subtype(t,"java.lang.Runnable")||subtype(t,"java.util.concurrent.Callable");}
    boolean component(String t){return webview(t)||subtype(t,"android.app.Fragment")||subtype(t,"androidx.fragment.app.Fragment")||subtype(t,"android.support.v4.app.Fragment")||subtype(t,"android.view.View")||subtype(t,"android.app.Dialog");}
    String kind(MethodReference m){
        String owner=cls(m.getDefiningClass()),n=m.getName();var p=m.getParameterTypes();
        if(webview(owner)){
            if((n.equals("loadUrl")||n.equals("loadData")||n.equals("loadDataWithBaseURL")||n.equals("evaluateJavascript"))&&!p.isEmpty()&&p.get(0).equals("Ljava/lang/String;"))return "webview_operation";
            if(n.equals("addJavascriptInterface")&&p.size()==2&&p.get(0).equals("Ljava/lang/Object;")&&p.get(1).equals("Ljava/lang/String;"))return "bridge";
            if((n.equals("setWebViewClient")||n.equals("setWebChromeClient"))&&p.size()==1)return "callback";
            if(n.equals("removeJavascriptInterface")&&p.size()==1)return "bridge_removal";
            if(n.equals("setWebContentsDebuggingEnabled")&&p.size()==1)return "global_setting";
        }
        if(settings(owner)&&n.startsWith("set")&&!p.isEmpty())return "setting";
        if((n.equals("addWebMessageListener")||n.equals("addDocumentStartJavaScript"))&&owner!=null&&owner.equals("androidx.webkit.WebViewCompat"))return "message_bridge";
        return null;
    }
    Method resolve(String id){
        Method exact=methods.get(id);if(exact!=null)return exact;
        int sep=id.indexOf("->");if(sep<0)return null;String type=cls(id.substring(0,sep)),tail=id.substring(sep+2);var seen=new HashSet<String>();
        while(type!=null&&seen.add(type)){
            ClassDef c=classes.get(type);if(c==null)return null;
            Method m=methods.get(c.getType()+"->"+tail);if(m!=null)return m;
            type=cls(c.getSuperclass());
        }return null;
    }
    List<Method> hierarchyMethods(String type){
        LinkedHashMap<String,Method> result=new LinkedHashMap<>();var seen=new HashSet<String>();
        while(type!=null&&seen.add(type)){
            if(type.equals("android.app.Activity")||type.equals("android.view.View")||Cfg.WEBVIEWS.contains(type))break;
            for(Method m:byClass.getOrDefault(type,List.of()))result.putIfAbsent(shape(m),m);
            ClassDef c=classes.get(type);type=c==null?null:cls(c.getSuperclass());
        }return new ArrayList<>(result.values());
    }
}
