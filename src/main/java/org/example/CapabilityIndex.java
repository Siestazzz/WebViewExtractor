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
    final Map<String,List<String>> platformParents=new HashMap<>();
    String platformSource="unavailable";
    final Map<String,Method> methods=new LinkedHashMap<>();
    final Map<String,Set<String>> callers=new HashMap<>(), calls=new HashMap<>();
    final Map<String,List<Method>> byClass=new HashMap<>();
    final Map<String,List<Method>> byShape=new HashMap<>();
    final Set<String> relevant=new HashSet<>(), seeds=new HashSet<>();
    final Set<String> classLiterals=new HashSet<>(), finalFields=new HashSet<>();
    final Map<String,Set<String>> referencedFields=new HashMap<>(), allocations=new HashMap<>(),fieldWriters=new HashMap<>();
    final Map<String,Set<String>> reflectionFields=new HashMap<>();
    final Map<String,String> messageRegistries=new HashMap<>();
    final Map<String,Set<List<String>>> namespaceRegistries=new HashMap<>();
    final Map<String,Set<String>> registryHandlerShapes=new HashMap<>();
    final Map<String,List<String>> subtypeCandidates=new HashMap<>();
    final Set<String> bindingObjects=new HashSet<>();
    final Set<String> keyedRegistryWrites=new HashSet<>();
    final Set<String> aroundClosureBases=new HashSet<>(), joinPointContracts=new HashSet<>(), proceedArgumentMethods=new HashSet<>();
    final Set<String> lazyContracts=new HashSet<>(Set.of("kotlin.Lazy")), function0Contracts=new HashSet<>(Set.of("kotlin.jvm.functions.Function0"));
    final Map<String,Set<String>> callbackEntries=new HashMap<>();
    record CustomCallback(String field,String contract,Set<String> dispatchedShapes){}
    final Map<String,CustomCallback> customCallbacks=new HashMap<>();
    final Map<String,Boolean> subtypeCache=new HashMap<>();
    final List<String> diagnostics=new ArrayList<>();
    long instructions;
    static String cls(String desc){return Util.className(desc);}
    static String key(MethodReference m){return m.getDefiningClass()+"->"+shape(m);}
    static String shape(MethodReference m){return m.getName()+"("+String.join("",m.getParameterTypes())+")"+m.getReturnType();}
    static String field(FieldReference f){return f.getDefiningClass()+"->"+f.getName()+":"+f.getType();}
    static String display(MethodReference m){return cls(m.getDefiningClass())+"."+m.getName()+"("+String.join(",",m.getParameterTypes().stream().map(Object::toString).toList())+"):"+m.getReturnType();}
    final FrameworkServices services=new FrameworkServices();
    void read(Path apk,long deadline) throws Exception {
        try{
            Path platforms=SootReader.androidJars();
            if(platforms!=null){
                try(var paths=java.nio.file.Files.list(platforms)){
                    Path jar=paths.filter(p->p.getFileName().toString().matches("android-[0-9]+"))
                        .sorted(Comparator.comparingInt((Path p)->Integer.parseInt(p.getFileName().toString().substring(8))).reversed())
                        .map(p->p.resolve("android.jar")).filter(java.nio.file.Files::isRegularFile).findFirst().orElse(null);
                    if(jar!=null){platformParents.putAll(PlatformHierarchy.read(jar,deadline));platformSource=jar.toString();}
                }
            }
            if(platformParents.isEmpty())diagnostics.add("platform_hierarchy_unavailable");
        }catch(java.io.IOException ex){diagnostics.add("platform_hierarchy_failed:"+ex.getClass().getSimpleName());}
        var container=DexFileFactory.loadDexContainer(apk.toFile(),Opcodes.getDefault());
        for(String name:container.getDexEntryNames())for(ClassDef c:container.getEntry(name).getDexFile().getClasses()){
            classes.put(cls(c.getType()),c);
            for(Field field:c.getFields())if((field.getAccessFlags()&16)!=0)finalFields.add(field(field));
            for(Method m:c.getMethods()){
                methods.put(key(m),m); byClass.computeIfAbsent(cls(m.getDefiningClass()),k->new ArrayList<>()).add(m);
                byShape.computeIfAbsent(shape(m),k->new ArrayList<>()).add(m);
            }
        }
        for(ClassDef c:classes.values())if((c.getAccessFlags()&0x200)!=0){
            String type=cls(c.getType());Set<String> shapes=new HashSet<>();for(Method m:c.getMethods())shapes.add(shape(m));
            if(type.startsWith("kotlin.")&&shapes.contains("getValue()Ljava/lang/Object;")&&shapes.contains("isInitialized()Z"))lazyContracts.add(type);
            if(type.startsWith("kotlin.jvm.functions.")&&shapes.contains("invoke()Ljava/lang/Object;"))function0Contracts.add(type);
        }
        // Generated bindings can hold nested custom Views rather than a direct WebView.
        // Keep their actual allocation-local constructor captures through bind/inflate returns.
        for(String type:classes.keySet())if(subtype(type,"androidx.viewbinding.ViewBinding"))bindingObjects.add(type);
        for(ClassDef c:classes.values())for(Field f:c.getFields())if((f.getAccessFlags()&8)==0&&(webview(cls(f.getType()))||settings(cls(f.getType()))))bindingObjects.add(cls(c.getType()));
        for(Method m:methods.values())if(m.getName().equals("invoke")&&m.getParameterTypes().isEmpty()&&(webview(cls(m.getReturnType()))||function0Type(cls(m.getDefiningClass()))))bindingObjects.add(cls(m.getDefiningClass()));
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
        discoverAroundClosures(deadline);
        discoverKeyedRegistries(deadline);
        discoverCustomCallbacks(deadline);
        services.index(this,deadline);
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
            String owner=cls(implementation.getDefiningClass());if(!carriers.contains(owner)||webview(owner)||activity(owner))continue;
            for(Method declaration:byShape.getOrDefault(shape(implementation),List.of()))if((!component(owner)||declaration.getImplementation()==null)&&!declaration.getDefiningClass().equals(implementation.getDefiningClass())&&subtype(owner,cls(declaration.getDefiningClass())))
                for(String caller:callers.getOrDefault(key(declaration),Set.of()))callers.computeIfAbsent(key(implementation),k->new HashSet<>()).add(caller);
        }
        var queue=new ArrayDeque<>(seeds); relevant.addAll(seeds);
        while(!queue.isEmpty()) {
            String callee=queue.remove();
            Set<String> receiverOwners=new HashSet<>();
            for(String call:calls.getOrDefault(callee,Set.of())){
                Method target=methods.get(call);
                // A pure getter can carry a WebView without invoking any capability API.
                // Follow only returned WebView/Settings/carrier objects, then discover their
                // field writers through the same bounded relevance closure.
                if(target!=null){String returned=cls(target.getReturnType());
                    if((webview(returned)||settings(returned)||bindingObjects.contains(returned))&&relevant.add(call))queue.add(call);
                }
                if(relevant.contains(call)||target!=null&&target.getImplementation()==null&&byShape.getOrDefault(shape(target),List.of()).stream().anyMatch(m->relevant.contains(key(m))))receiverOwners.add(CapabilityEngine.owner(call));
            }
            for(String field:referencedFields.getOrDefault(callee,Set.of())){
                String type=cls(field.substring(field.indexOf(':')+1));
                boolean binding=webview(type)||settings(type)||bindingObjects.contains(type)||collection(type)&&(seeds.contains(callee)||webview(CapabilityEngine.owner(field)))||subtype(type,"android.webkit.WebViewClient")||subtype(type,"android.webkit.WebChromeClient")||subtype(type,"com.tencent.smtt.sdk.WebViewClient")||subtype(type,"com.tencent.smtt.sdk.WebChromeClient");
                // Constructors/setters of a composed receiver are needed even when that receiver
                // is not itself a WebView carrier (controller -> manager -> factory is common).
                // Require a relevant call on the field's declared receiver type, not mere co-location.
                if(!binding&&type!=null&&!type.startsWith("java.")&&!type.startsWith("android.")&&!type.startsWith("kotlin."))
                    for(String receiverOwner:receiverOwners)if(subtype(type,receiverOwner)){binding=true;break;}
                if(binding)for(String writer:fieldWriters.getOrDefault(field,Set.of()))if(relevant.add(writer))queue.add(writer);
            }
            for(String caller:callers.getOrDefault(callee,Set.of()))if(relevant.add(caller))queue.add(caller);
            Method cm=methods.get(callee);
            if(cm!=null){String owner=cls(cm.getDefiningClass());
                if(carriers.contains(owner)&&!webview(owner)&&!activity(owner)&&!cm.getName().startsWith("<"))
                    for(Method declaration:byShape.getOrDefault(shape(cm),List.of())){
                        // View/Fragment carriers need interface/abstract contracts. Expanding
                        // every concrete base initializer links unrelated UI entrypoints.
                        if(component(owner)&&declaration.getImplementation()!=null)continue;
                        String base=cls(declaration.getDefiningClass());
                        // Generic platform listeners do not identify a concrete SDK carrier.
                        if(base.startsWith("java.")||base.startsWith("android.")||base.startsWith("androidx.")||base.startsWith("kotlin."))continue;
                        if(!base.equals(owner)&&subtype(owner,base))for(String caller:callers.getOrDefault(key(declaration),Set.of()))if(relevant.add(caller))queue.add(caller);
                    }
                boolean callback=!aroundClosure(owner)&&seedDistance.getOrDefault(callee,99)<=2&&!cm.getName().startsWith("<")&&(cm.getAccessFlags()&8)==0&&!component(owner)&&!activity(owner)&&byShape.getOrDefault(shape(cm),List.of()).stream().anyMatch(declaration->declaration.getImplementation()==null&&subtype(owner,cls(declaration.getDefiningClass())));
                if(callback)callbackEntries.computeIfAbsent(owner,k->new HashSet<>()).add(key(cm));
                if(callback||component(owner)||scheduled(owner)&&(cm.getName().equals("run")||cm.getName().equals("call")))for(Method init:byClass.getOrDefault(owner,List.of()))if(init.getName().equals("<init>")&&relevant.add(key(init)))queue.add(key(init));
            }
        }
        for(String type:classes.keySet())if(aroundClosure(type)){Method run=resolve(CapabilityEngine.desc(type)+"->run([Ljava/lang/Object;)Ljava/lang/Object;");if(run!=null&&relevant.contains(key(run)))bindingObjects.add(type);}
    }
    // AspectJ keeps these runtime member names even when class names are obfuscated.
    // Discover contracts structurally inside the runtime namespace, never app host names.
    void discoverAroundClosures(long deadline){
        for(String type:classes.keySet())if(type.startsWith("org.aspectj.runtime.internal.")){
            Method run=resolve(CapabilityEngine.desc(type)+"->run([Ljava/lang/Object;)Ljava/lang/Object;");
            Method link=byClass.getOrDefault(type,List.of()).stream().filter(m->m.getName().equals("linkClosureAndJoinPoint")&&(m.getParameterTypes().equals(List.of("I"))||m.getParameterTypes().isEmpty())).findFirst().orElse(null);
            boolean state=false;for(Field f:classes.get(type).getFields())if(f.getName().equals("state")&&f.getType().equals("[Ljava/lang/Object;"))state=true;
            if(run!=null&&link!=null&&state){aroundClosureBases.add(type);joinPointContracts.add(cls(link.getReturnType()));}
        }
        if(aroundClosureBases.isEmpty())return;
        for(Method method:methods.values())if(joinPoint(cls(method.getDefiningClass()))&&method.getParameterTypes().equals(List.of("[Ljava/lang/Object;"))&&method.getReturnType().equals("Ljava/lang/Object;")){
            boolean runtimeDispatch=calls.getOrDefault(key(method),Set.of()).stream().anyMatch(id->id.endsWith("->run([Ljava/lang/Object;)Ljava/lang/Object;")&&aroundClosure(CapabilityEngine.owner(id)));
            if(runtimeDispatch||method.getName().equals("proceed")){
                proceedArgumentMethods.add(key(method));
                for(Method declaration:byShape.getOrDefault(shape(method),List.of()))if(joinPoint(cls(declaration.getDefiningClass())))proceedArgumentMethods.add(key(declaration));
            }
        }
        DexFlow flow=new DexFlow(this,deadline);
        for(Method caller:methods.values()){
            if(System.nanoTime()>deadline)throw new IllegalStateException("aspectj_index_deadline");
            if(calls.getOrDefault(key(caller),Set.of()).stream().noneMatch(this::closureLink))continue;
            // This is only a relevance edge. Execution still requires a linked concrete
            // closure to flow into an actual proceed call in the host interpreter.
            for(DexFlow.Call call:flow.summary(caller).calls())if(closureLink(call.method())&&!call.args().isEmpty())
                for(DexFlow.V recv:DexFlow.alternatives(call.args().get(0)))if(recv.kind().equals("new")&&aroundClosure(recv.type())){
                    Method run=resolve(CapabilityEngine.desc(recv.type())+"->run([Ljava/lang/Object;)Ljava/lang/Object;");
                    if(run!=null)callers.computeIfAbsent(key(run),k->new HashSet<>()).add(key(caller));
                }
        }
    }
    boolean aroundClosure(String type){for(String base:aroundClosureBases)if(subtype(type,base))return true;return false;}
    boolean joinPoint(String type){for(String contract:joinPointContracts)if(subtype(type,contract))return true;return false;}
    boolean closureLink(String id){return (id.contains("->linkClosureAndJoinPoint(I)")||id.contains("->linkClosureAndJoinPoint()"))&&aroundClosure(CapabilityEngine.owner(id));}
    boolean closureProceedArguments(String id){Method method=resolve(id);return proceedArgumentMethods.contains(id)||method!=null&&proceedArgumentMethods.contains(key(method));}
    boolean closureProceed(String id){return joinPoint(CapabilityEngine.owner(id))&&id.endsWith("->proceed()Ljava/lang/Object;");}
    void discoverCustomCallbacks(long deadline){
        DexFlow flow=new DexFlow(this,deadline);Map<String,CustomCallback> candidates=new HashMap<>();Set<String> fields=new HashSet<>();
        for(Method setter:methods.values()){
            String owner=cls(setter.getDefiningClass());
            if(!webview(owner)||(setter.getAccessFlags()&8)!=0||setter.getImplementation()==null||setter.getName().startsWith("<")||setter.getParameterTypes().size()!=1)continue;
            String contract=cls(setter.getParameterTypes().get(0).toString());
            if(contract==null||!classes.containsKey(contract)||subtype(contract,"android.webkit.WebViewClient")||subtype(contract,"android.webkit.WebChromeClient"))continue;
            if(contractMethods(contract).stream().noneMatch(m->CapabilityEngine.CALLBACKS.contains(m.getName())&&m.getParameterTypes().stream().anyMatch(p->webview(cls(p.toString())))))continue;
            if(System.nanoTime()>deadline)throw new IllegalStateException("custom_callback_index_deadline");
            for(DexFlow.Write write:flow.summary(setter).writes())if(write.receiver().kind().equals("param")&&write.receiver().id().equals("0")&&write.value().kind().equals("param")&&write.value().id().equals("1")){
                candidates.put(key(setter),new CustomCallback(write.field(),contract,Set.of()));fields.add(write.field());
            }
        }
        Map<String,Set<String>> readers=new HashMap<>();
        for(var entry:referencedFields.entrySet())for(String field:entry.getValue())if(fields.contains(field))readers.computeIfAbsent(field,k->new HashSet<>()).add(entry.getKey());
        for(var entry:candidates.entrySet()){
            CustomCallback candidate=entry.getValue();Set<String> dispatched=new HashSet<>();
            for(String id:readers.getOrDefault(candidate.field(),Set.of())){
                if(System.nanoTime()>deadline)throw new IllegalStateException("custom_callback_dispatch_deadline");
                Method dispatch=methods.get(id);if(dispatch==null)continue;
                for(DexFlow.Call call:flow.summary(dispatch).calls())if(!call.args().isEmpty()&&call.args().get(0).kind().equals("field")&&call.args().get(0).id().equals(candidate.field()))
                    for(Method declaration:contractMethods(candidate.contract()))if(call.method().endsWith("->"+shape(declaration))&&subtype(candidate.contract(),CapabilityEngine.owner(call.method())))dispatched.add(shape(declaration));
            }
            if(dispatched.isEmpty())continue;
            customCallbacks.put(entry.getKey(),new CustomCallback(candidate.field(),candidate.contract(),Set.copyOf(dispatched)));
            for(String caller:callers.getOrDefault(entry.getKey(),Set.of()))seeds.add(caller);
        }
    }
    void discoverKeyedRegistries(long deadline){
        // Recognize Class-keyed registries by the actual Map field and parameter stores.
        // No registration method is a global capability seed.
        Map<String,Set<String>> readers=new HashMap<>(),writers=new HashMap<>();
        DexFlow flow=new DexFlow(this,deadline);
        for(Method method:methods.values()){
            if(System.nanoTime()>deadline)throw new IllegalStateException("registry_index_deadline");
            if(method.getImplementation()==null||(method.getAccessFlags()&8)!=0||method.getParameterTypes().isEmpty()||!method.getParameterTypes().get(0).equals("Ljava/lang/Class;"))continue;
            if(!calls.getOrDefault(key(method),Set.of()).stream().anyMatch(c->c.endsWith("->get(Ljava/lang/Object;)Ljava/lang/Object;")||c.endsWith("->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")))continue;
            for(DexFlow.Call call:flow.summary(method).calls()){
                if(!map(CapabilityEngine.owner(call.method()))||call.args().size()<2)continue;
                var receiver=call.args().get(0);var argument=call.args().get(1);
                if(!receiver.kind().equals("field")||!receiver.args().get(0).kind().equals("param")||!receiver.args().get(0).id().equals("0")||!argument.kind().equals("param")||!argument.id().equals("1"))continue;
                if(call.method().endsWith("->get(Ljava/lang/Object;)Ljava/lang/Object;"))readers.computeIfAbsent(receiver.id(),k->new HashSet<>()).add(key(method));
                if(call.method().endsWith("->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")&&call.args().size()==3&&call.args().get(2).kind().equals("param")&&call.args().get(2).id().equals("2"))writers.computeIfAbsent(receiver.id(),k->new HashSet<>()).add(key(method));
            }
        }
        Set<String> carriers=new HashSet<>();
        for(String field:readers.keySet())if(writers.containsKey(field)){
            keyedRegistryWrites.addAll(writers.get(field));carriers.add(CapabilityEngine.owner(field));
        }
        for(String type:classes.keySet())for(String carrier:carriers)if(subtype(type,carrier)){bindingObjects.add(type);break;}
    }
    void discoverMessageRegistries(long deadline){
        // Registries must share a Map with a JS or installed-client transport call chain.
        Set<String> transportFields=new HashSet<>(),transportCalls=new HashSet<>();Map<String,Set<List<String>>> namespaceFields=new HashMap<>();
        for(Method m:methods.values())if(m.getAnnotations().stream().anyMatch(a->a.getType().endsWith("/JavascriptInterface;"))||clientTransport(m)){
            bindingObjects.add(cls(m.getDefiningClass()));
            Set<String> seen=new HashSet<>();ArrayDeque<String> q=new ArrayDeque<>();q.add(key(m));int budget=150;
            while(!q.isEmpty()&&budget>0){String id=q.remove();if(!seen.add(id))continue;budget--;
                transportFields.addAll(referencedFields.getOrDefault(id,Set.of()));transportCalls.addAll(calls.getOrDefault(id,Set.of()));
                for(String target:calls.getOrDefault(id,Set.of()))if(methods.containsKey(target)){
                    String transportOwner=cls(m.getDefiningClass()),calledOwner=CapabilityEngine.owner(target);
                    // Stay near the transport before spending its budget inside JSON/logging/SDK code.
                    if(calledOwner.equals(transportOwner)||calledOwner.startsWith(transportOwner+"$"))q.addFirst(target);else q.addLast(target);
                }
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
            if(hc==null||(hc.getAccessFlags()&0x600)==0)continue;
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
            ClassDef c=classes.get(t);if(c!=null){if(cls(c.getSuperclass())!=null)q.add(cls(c.getSuperclass()));for(String iface:c.getInterfaces())q.add(cls(iface));}else q.addAll(platformParents.getOrDefault(t,List.of()));
        }
        subtypeCache.put(cache,result);return result;
    }
    boolean webview(String t){return Cfg.WEBVIEWS.stream().anyMatch(b->subtype(t,b));}
    boolean lazyType(String t){return lazyContracts.stream().anyMatch(b->subtype(t,b));}
    boolean function0Type(String t){return function0Contracts.stream().anyMatch(b->subtype(t,b));}
    boolean settings(String t){return subtype(t,"android.webkit.WebSettings")||subtype(t,"com.tencent.smtt.sdk.WebSettings")||subtype(t,"com.uc.webview.export.WebSettings");}
    boolean activity(String t){return Cfg.ACTIVITIES.stream().anyMatch(b->subtype(t,b));}
    boolean map(String t){return t!=null&&(Set.of("java.util.Map","java.util.HashMap","java.util.LinkedHashMap","java.util.TreeMap","java.util.concurrent.ConcurrentMap","java.util.concurrent.ConcurrentHashMap").contains(t)||subtype(t,"java.util.Map"));}
    boolean collection(String t){return t!=null&&(Set.of("java.util.List","java.util.Collection","java.util.ArrayList","java.util.LinkedList","java.util.Set","java.util.HashSet").contains(t)||subtype(t,"java.util.Collection"));}
    boolean scheduled(String t){return subtype(t,"java.lang.Runnable")||subtype(t,"java.util.concurrent.Callable");}
    boolean component(String t){return webview(t)||subtype(t,"android.app.Fragment")||subtype(t,"androidx.fragment.app.Fragment")||subtype(t,"android.support.v4.app.Fragment")||subtype(t,"android.view.View")||subtype(t,"android.app.Dialog");}
    String kind(MethodReference m){
        String owner=cls(m.getDefiningClass()),n=m.getName();var p=m.getParameterTypes();
        if(customCallbacks.containsKey(key(m)))return "callback";
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
    List<Method> contractMethods(String type){
        Map<String,Method> result=new LinkedHashMap<>();Set<String> seen=new HashSet<>();ArrayDeque<String> queue=new ArrayDeque<>();if(type!=null)queue.add(type);
        while(!queue.isEmpty()){
            String current=queue.remove();if(current.equals("java.lang.Object")||!seen.add(current))continue;
            for(Method method:byClass.getOrDefault(current,List.of()))result.putIfAbsent(shape(method),method);
            ClassDef definition=classes.get(current);if(definition==null)continue;
            String parent=cls(definition.getSuperclass());if(parent!=null)queue.add(parent);for(String iface:definition.getInterfaces())queue.add(cls(iface));
        }return List.copyOf(result.values());
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
