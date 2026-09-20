package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.iface.*;
import static org.example.DexFlow.*;

/** Demand-driven instantiation of method summaries for each Activity host. */
final class CapabilityEngine {
    final CapabilityIndex idx;
    final DexFlow flow;
    final ApkInventory apk;
    final long deadline;
    final List<Map<String,Object>> activities=new ArrayList<>();
    final Set<String> boundSites=new HashSet<>();
    final List<String> diagnostics=new ArrayList<>();
    final Map<String,V> constants=new HashMap<>();
    final Map<String,List<Map<String,Object>>> callbackCache=new HashMap<>();
    final Map<String,Set<Integer>> lazyFactoryParameters=new HashMap<>();
    Runnable checkpoint=()->{};
    Host currentHost;
    static final Set<String> CALLBACKS=Set.of("onPageStarted","onPageFinished","onPageCommitVisible","onLoadResource","shouldOverrideUrlLoading","shouldInterceptRequest","onTooManyRedirects","onReceivedError","onReceivedHttpError","onFormResubmission","doUpdateVisitedHistory","onReceivedSslError","onReceivedClientCertRequest","onReceivedHttpAuthRequest","shouldOverrideKeyEvent","onUnhandledKeyEvent","onScaleChanged","onReceivedLoginRequest","onRenderProcessGone","onSafeBrowsingHit","onProgressChanged","onReceivedTitle","onReceivedIcon","onReceivedTouchIconUrl","onShowCustomView","onHideCustomView","onCreateWindow","onRequestFocus","onCloseWindow","onJsAlert","onJsConfirm","onJsPrompt","onJsBeforeUnload","onExceededDatabaseQuota","onReachedMaxAppCacheSize","onGeolocationPermissionsShowPrompt","onGeolocationPermissionsHidePrompt","onPermissionRequest","onPermissionRequestCanceled","onJsTimeout","onConsoleMessage","getDefaultVideoPoster","getVideoLoadingProgressView","getVisitedHistory","onShowFileChooser","openFileChooser");
    CapabilityEngine(CapabilityIndex idx,ApkInventory apk,long deadline){this.idx=idx;this.apk=apk;this.deadline=deadline;flow=new DexFlow(idx,deadline);}
    record Job(Method method,List<V> args,List<String> path,boolean candidate){}
    final class Host {
        final String activity;
        final Map<String,V> heap=new HashMap<>();
        final Map<String,Set<V>> contents=new HashMap<>();
        final Map<String,V> bridgeViews=new HashMap<>();
        final Map<String,List<Map<String,Object>>> nativeBindings=new HashMap<>();
        final Map<String,Object> serviceEvidence=new TreeMap<>();
        final ArrayDeque<Job> queue=new ArrayDeque<>();
        final Set<String> visited=new HashSet<>(),components=new HashSet<>(),expanding=new HashSet<>(),constructed=new HashSet<>(),materialized=new HashSet<>();
        final Map<String,Map<String,Object>> facts=new TreeMap<>();
        final Set<String> gaps=new LinkedHashSet<>();
        Host(String a){activity=a;}
    }
    void analyzeActivity(String activity){
        Host h=new Host(activity);currentHost=h;V self=V.of("host",activity,"activity:"+activity);
        seed(h,activity,self,List.of(activity),false);
        for(int phase=0;phase<2;phase++){
            if(phase==1){h.facts.clear();h.visited.clear();h.components.clear();h.materialized.clear();seed(h,activity,self,List.of(activity),false);}
            while(!h.queue.isEmpty()){
                if(System.nanoTime()>deadline){h.gaps.add("global_deadline");break;}
                if(h.visited.size()>12000){h.gaps.add("host_context_budget");break;}
                checkpoint.run();
                Job job=h.queue.remove();String id=CapabilityIndex.key(job.method);
                String context=id+"|"+job.args;if(!h.visited.add(context))continue;
                Summary summary;
                try{summary=flow.summary(job.method,v->guardValue(v,job,h,0));}catch(RuntimeException ex){h.gaps.add("decode_failed:"+id+":"+ex.getClass().getSimpleName());continue;}
                if(summary.truncated())h.gaps.add("flow_budget:"+id);
                for(Write w:summary.writes()){
                    V receiver=eval(w.receiver(),job,h,0,new HashSet<>()),value=eval(w.value(),job,h,0,new HashSet<>());
                    applyWrite(h,w.field(),receiver,value);
                }
                for(Call call:summary.calls()){
                    Method target=idx.resolve(call.method());String owner=owner(call.method()),name=name(call.method());
                    String kind=kind(call.method());
                    boolean lifecycle=name.equals("<init>")&&(idx.component(owner)||idx.activity(owner)||idx.scheduled(owner)||idx.callbackEntries.containsKey(owner)||idx.bindingObjects.contains(owner));
                    boolean relevant=target!=null&&(idx.relevant.contains(CapabilityIndex.key(target))||target.getImplementation()==null&&idx.byShape.getOrDefault(CapabilityIndex.shape(target),List.of()).stream().anyMatch(m->idx.relevant.contains(CapabilityIndex.key(m))));
                    if(kind==null&&!lifecycle&&!relevant)continue;
                    List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
                    if(kind!=null){emit(h,job,call,args,kind,summary.branched());continue;}
                    if(target!=null){
                        boolean virtual=!call.isStatic()&&!call.isSuper()&&!call.isDirect()&&!args.isEmpty();
                        if(!virtual){
                            if(idx.relevant.contains(CapabilityIndex.key(target))||lifecycle)enqueue(h,target,args,job.path,job.candidate||summary.branched());
                        }else{
                            int dispatched=0;
                            for(V receiver:alternatives(args.get(0))){
                                if(receiver.kind().equals("literal")&&"0".equals(receiver.literal()))continue;
                                Method concrete=receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+CapabilityIndex.shape(target));
                                if(concrete==null||concrete.getImplementation()==null){h.gaps.add("unresolved_receiver_dispatch:"+call.method());continue;}
                                if(!idx.relevant.contains(CapabilityIndex.key(concrete)))continue;
                                if(dispatched++>=64){h.gaps.add("dispatch_budget:"+call.method());break;}
                                enqueue(h,concrete,specializeReceiver(args,receiver),job.path,job.candidate||summary.branched()||args.get(0).kind().equals("union"));
                            }
                        }
                    }

                    if(name.equals("<init>")&&!args.isEmpty()&&(idx.component(owner)||idx.scheduled(owner)||idx.callbackEntries.containsKey(owner))&&!idx.activity(owner)){h.constructed.add(args.get(0).id());seed(h,owner,args.get(0),extend(job.path,id+"@"+call.offset()),true);}
                }
            }
        }
        if(!h.facts.isEmpty()){
            activities.add(hostReport(h));
        }
        currentHost=null;
        if(!h.gaps.isEmpty())diagnostics.add(activity+":"+String.join(",",h.gaps.stream().distinct().limit(20).toList()));
    }
    void seed(Host h,String type,V self,List<String> path,boolean candidate){
        if(Cfg.WEBVIEWS.contains(type))return;
        if(path.size()>16){h.gaps.add("component_depth:"+type);return;}
        if(h.expanding.contains(type)||!h.components.add(type+"|"+self.id()))return;
        h.expanding.add(type);
        List<Method> hierarchy=idx.hierarchyMethods(type);
        Set<String> referencedShapes=new HashSet<>();
        for(Method entry:hierarchy)if(CapabilityIndex.cls(entry.getDefiningClass()).equals(type)||entry.getName().startsWith("on"))
            for(String call:idx.calls.getOrDefault(CapabilityIndex.key(entry),Set.of()))referencedShapes.add(call.substring(call.indexOf("->")+2));
        Set<String> usedFields=new HashSet<>();ArrayDeque<String> fieldMethods=new ArrayDeque<>();
        for(Method m:hierarchy){
            if(!m.getName().startsWith("<")){
                if(!idx.activity(type)&&!idx.component(type)&&!idx.scheduled(type)&&!idx.callbackEntries.getOrDefault(type,Set.of()).contains(CapabilityIndex.key(m)))continue;
                // A WebView-taking helper gets its receiver arguments from actual call sites.
                // Seeding it with entry_parameter fabricates extra WebViews and merges capabilities.
                if(!idx.activity(type)&&m.getParameterTypes().stream().anyMatch(p->idx.webview(CapabilityIndex.cls(p.toString()))||idx.settings(CapabilityIndex.cls(p.toString()))))continue;
            }
            if(m.getName().equals("<init>")&&!idx.activity(type)){
                if(self.kind().equals("view")){
                    // Lookup-derived views use the XML constructor contract, including replay.
                    // Its initialization must survive the second analysis phase.
                    if(!m.getParameterTypes().equals(List.of("Landroid/content/Context;","Landroid/util/AttributeSet;")))continue;
                }else if(h.constructed.contains(self.id()))continue;
            }
            if(!idx.relevant.contains(CapabilityIndex.key(m))&&!(idx.activity(type)&&m.getName().equals("<init>")))continue;
            if(idx.activity(type)&&!CapabilityIndex.cls(m.getDefiningClass()).equals(type)&&!m.getName().equals("<init>")&&!m.getName().startsWith("on")&&!referencedShapes.contains(CapabilityIndex.shape(m)))continue;
            fieldMethods.add(CapabilityIndex.key(m));
            List<V> args=new ArrayList<>();if((m.getAccessFlags()&8)==0)args.add(self);
            for(CharSequence p:m.getParameterTypes())args.add(V.of("unknown",CapabilityIndex.cls(p.toString()),"entry_parameter"));
            enqueue(h,m,args,path,candidate);
        }
        Set<String> traversed=new HashSet<>();
        while(!fieldMethods.isEmpty()&&traversed.size()<1000){String id=fieldMethods.remove();if(!traversed.add(id))continue;usedFields.addAll(idx.referencedFields.getOrDefault(id,Set.of()));for(String callee:idx.calls.getOrDefault(id,Set.of()))if(idx.relevant.contains(callee))fieldMethods.add(callee);}
        // Only fields read by eligible entrypoints or their relevant helper chain establish a host edge.
        String current=type;Set<String> seen=new HashSet<>();
        while(current!=null&&seen.add(current)){
            ClassDef c=idx.classes.get(current);if(c==null)break;
            for(Field f:c.getFields()){
                if((f.getAccessFlags()&(8|0x1000))!=0||!usedFields.contains(CapabilityIndex.field(f)))continue;
                String ft=CapabilityIndex.cls(f.getType());
                if(ft!=null&&idx.component(ft)&&!idx.activity(ft)&&!Cfg.WEBVIEWS.contains(ft)){
                    V fv=h.heap.get(heapKey(CapabilityIndex.field(f),self));
                    // A field declaration is not another allocation. Wait for a real object/view
                    // origin; actual field dereferences still retain unresolved candidate facts.
                    if(fv!=null)for(V origin:alternatives(fv))if(origin.kind().equals("object")||origin.kind().equals("view"))
                        seed(h,origin.type()==null?ft:origin.type(),origin,extend(path,CapabilityIndex.field(f)),true);
                }
            }
            current=CapabilityIndex.cls(c.getSuperclass());
            if(current!=null&&(Cfg.WEBVIEWS.contains(current)||current.startsWith("android.")||current.startsWith("androidx.")))break;
        }
        h.expanding.remove(type);
    }
    void enqueue(Host h,Method m,List<V> args,List<String> path,boolean candidate){
        if(path.size()>24){h.gaps.add("call_depth:"+CapabilityIndex.key(m));return;}
        if(h.queue.size()>6000){h.gaps.add("queue_budget");return;}
        h.queue.add(new Job(m,args,extend(path,CapabilityIndex.key(m)),candidate||args.stream().anyMatch(v->alternatives(v).stream().anyMatch(a->a.id().startsWith("registered_service:")))));
    }
    static List<V> specializeReceiver(List<V> args,V receiver){
        V original=args.get(0);return args.stream().map(v->v.equals(original)?receiver:v).toList();
    }
    static List<String> extend(List<String> path,String s){var p=new ArrayList<>(path);p.add(s);return List.copyOf(p);}
    Set<Integer> lazyInitializerParameters(Method factory){
        String key=CapabilityIndex.key(factory);Set<Integer> cached=lazyFactoryParameters.get(key);if(cached!=null)return cached;
        Set<Integer> result=new HashSet<>();lazyFactoryParameters.put(key,result);
        Summary summary=flow.summary(factory);
        for(Call call:summary.calls())if(name(call.method()).equals("<init>")&&!call.args().isEmpty()){
            V receiver=call.args().get(0);String type=receiver.type();
            if(!receiver.kind().equals("new")||type==null||!type.startsWith("kotlin.")||!idx.lazyType(type)||!summary.returns().contains(receiver))continue;
            Method constructor=idx.resolve(call.method());if(constructor==null)continue;
            for(int i:capturedInitializerParameters(constructor,new HashSet<>()))if(i<call.args().size()){
                V argument=call.args().get(i);if(argument.kind().equals("param")&&idx.function0Type(argument.type()))result.add(Integer.parseInt(argument.id()));
            }
        }
        return result;
    }
    Set<Integer> capturedInitializerParameters(Method constructor,Set<String> seen){
        Set<Integer> result=new HashSet<>();if(seen.size()>=6||!seen.add(CapabilityIndex.key(constructor)))return result;
        Summary summary=flow.summary(constructor);
        for(Write write:summary.writes())if(write.receiver().kind().equals("param")&&write.receiver().id().equals("0")&&write.value().kind().equals("param")&&idx.function0Type(write.value().type()))result.add(Integer.parseInt(write.value().id()));
        for(Call call:summary.calls())if(name(call.method()).equals("<init>")&&!call.args().isEmpty()&&call.args().get(0).kind().equals("param")&&call.args().get(0).id().equals("0")){
            Method delegate=idx.resolve(call.method());if(delegate==null)continue;
            for(int i:capturedInitializerParameters(delegate,new HashSet<>(seen)))if(i<call.args().size()&&call.args().get(i).kind().equals("param"))result.add(Integer.parseInt(call.args().get(i).id()));
        }
        return result;
    }
    V guardValue(V value,Job job,Host h,int depth){
        if(depth>6)return UNKNOWN;
        if(value.kind().equals("param")){int index=Integer.parseInt(value.id());return index<job.args.size()?job.args.get(index):UNKNOWN;}
        if(value.kind().equals("cast"))return guardValue(value.args().get(0),job,h,depth+1);
        if(value.kind().equals("field")){V receiver=guardValue(value.args().get(0),job,h,depth+1);return h.heap.getOrDefault(heapKey(value.id(),receiver),UNKNOWN);}
        if(value.kind().startsWith("return"))return UNKNOWN;
        return value;
    }
    V eval(V v,Job job,Host h,int depth,Set<String> visiting){
        if(System.nanoTime()>deadline){h.gaps.add("global_deadline");return V.of("unknown",v.type(),"global_deadline");}
        if(depth>14){h.gaps.add("resolve_depth");return V.of("unknown",v.type(),"resolve_depth");}
        if(v.kind().equals("param")){int i=Integer.parseInt(v.id());return i<job.args.size()?job.args.get(i):V.of("unknown",v.type(),"parameter");}
        if(v.kind().equals("array")){V array=V.of("object",v.type(),v.id()+"|"+allocationContext(job));for(V element:v.args())applyWrite(h,"$contents",array,eval(element,job,h,depth+1,new HashSet<>(visiting)));return array;}
        if(v.kind().equals("array_element"))return elements(h,eval(v.args().get(0),job,h,depth+1,visiting),v.type());
        if(v.kind().equals("new")){
            V object=V.of("object",v.type(),v.id()+"|"+allocationContext(job));
            for(Call ctor:flow.summary(job.method).calls())if(name(ctor.method()).equals("<init>")&&!ctor.args().isEmpty()&&ctor.args().get(0).equals(v)){
                Method target=idx.resolve(ctor.method());
                if(target!=null&&(idx.component(v.type())||idx.bindingObjects.contains(v.type())||idx.relevant.contains(CapabilityIndex.key(target)))){
                    List<V> args=new ArrayList<>();args.add(object);
                    for(int i=1;i<ctor.args().size();i++)args.add(eval(ctor.args().get(i),job,h,depth+1,new HashSet<>(visiting)));
                    materializeConstructor(target,args,job,h,depth+1,visiting);
                }
            }
            return object;
        }
        if(v.kind().equals("union")){V result=null;for(V x:v.args())result=union(result,eval(x,job,h,depth+1,new HashSet<>(visiting)));return result==null?UNKNOWN:result;}
        if(v.kind().equals("int_binary")){
            V left=eval(v.args().get(0),job,h,depth+1,new HashSet<>(visiting)),right=eval(v.args().get(1),job,h,depth+1,new HashSet<>(visiting)),result=null;
            for(V a:alternatives(left))for(V b:alternatives(right)){
                Long x=number(a),y=number(b);if(x==null||y==null)return V.of("unknown","number","dynamic_int_"+v.id());
                int value=switch(v.id()){case "xor"->x.intValue()^y.intValue();case "and"->x.intValue()&y.intValue();case "or"->x.intValue()|y.intValue();default->throw new IllegalStateException("unsupported_int_operator");};
                result=union(result,V.literal("number",String.valueOf(value)));
            }return result==null?UNKNOWN:result;
        }
        if(v.kind().equals("cast")){
            V original=eval(v.args().get(0),job,h,depth+1,visiting);
            List<V> choices=new ArrayList<>();for(V alternative:alternatives(original)){
                V cast=new V(alternative.kind(),alternative.kind().equals("object")?alternative.type():v.type(),alternative.id(),alternative.literal(),alternative.args());choices.add(cast);
                if(alternative.kind().equals("view")&&idx.component(v.type())&&!idx.activity(v.type())&&!Cfg.WEBVIEWS.contains(v.type()))seed(h,v.type(),cast,job.path,true);
            }
            return choices.size()==1?choices.get(0):new V("union",v.type(),original.id(),null,List.copyOf(choices));
        }
        if(v.kind().equals("settings"))return expr("settings",v.type(),v.id(),List.of(eval(v.args().get(0),job,h,depth+1,visiting)));
        if(v.kind().equals("field")){
            V receiver=eval(v.args().get(0),job,h,depth+1,new HashSet<>(visiting));
            if(receiver.kind().equals("union")){V joined=null;for(V alternative:alternatives(receiver))joined=union(joined,eval(expr("field",v.type(),v.id(),List.of(alternative)),job,h,depth+1,new HashSet<>(visiting)));return joined==null?UNKNOWN:joined;}
            String hk=heapKey(v.id(),receiver);
            if(idx.subtype(v.type(),"java.lang.Enum"))return new V("enum",v.type(),v.id(),v.id().substring(v.id().indexOf("->")+2,v.id().indexOf(':')),List.of());
            V result=h.heap.get(hk);if(result!=null)return result;
            if(!visiting.add(hk))return V.of("field_object",v.type(),hk);
            String owner=owner(v.id());ClassDef c=idx.classes.get(owner);
            if(c!=null){
                for(Field f:c.getFields())if(CapabilityIndex.field(f).equals(v.id())&&f.getInitialValue()!=null){V initial=encoded(f.getInitialValue());if(!initial.equals(UNKNOWN))return initial;}
                for(Method m:idx.byClass.getOrDefault(owner,List.of()))if(m.getName().equals("<clinit>")||m.getName().equals("<init>")&&!h.constructed.contains(receiver.id())){
                    if(m.getImplementation()==null)continue;
                    Summary init=flow.summary(m);List<V> args=new ArrayList<>();if((m.getAccessFlags()&8)==0)args.add(receiver);
                    for(CharSequence t:m.getParameterTypes())args.add(V.of("unknown",CapabilityIndex.cls(t.toString()),"constructor_parameter"));
                    Job nested=new Job(m,args,job.path,true);
                    for(Write w:init.writes())if(w.field().equals(v.id()))result=union(result,eval(w.value(),nested,h,depth+1,new HashSet<>(visiting)));
                }
            }
            if(receiver.kind().equals("static")&&c!=null){
                ClassDef declared=idx.classes.get(v.type());
                if(declared!=null&&(declared.getAccessFlags()&0x600)!=0){V provider=installedProvider(v.id(),job,h,depth,visiting);if(provider!=null)result=union(result,provider);}
            }
            return result!=null?result:V.of("field_object",v.type(),hk);
        }
        if(v.kind().startsWith("return")){
            List<V> args=v.args().stream().map(x->eval(x,job,h,depth+1,new HashSet<>(visiting))).toList();String name=name(v.id());
            String serviceContext=CapabilityIndex.key(job.method)+"|"+allocationContext(job);
            V service=idx.services.lookup(v.id(),args,serviceContext);if(service!=null){
                var registrations=idx.services.matchingBindings(v.id(),args,serviceContext);
                h.serviceEvidence.put(serviceContext+"|"+v.id(),Map.of("lookup",v.id(),"caller",CapabilityIndex.key(job.method),"arguments",args,"registrations",registrations,"binding_status","candidate","conditions",List.of("registration_initialization_not_proven_by_analysis","replacement_order_not_proven","runtime_creation_may_fail")));
                if(service.kind().equals("unknown"))h.gaps.add(service.id());
                return service;
            }
            if((name.equals("findViewById")||name.equals("requireViewById"))&&args.size()>1)return V.of("view",v.type(),args.get(0).id()+"/view:"+args.get(1).id());
            if((name.equals("getActivity")||name.equals("requireActivity")))return V.of("host",h.activity,"activity:"+h.activity);
            Method lazyFactory=idx.resolve(v.id());
            if(lazyFactory!=null&&owner(v.id()).startsWith("kotlin.")&&(lazyFactory.getAccessFlags()&8)!=0&&idx.lazyType(CapabilityIndex.cls(lazyFactory.getReturnType()))){
                V initializer=null;for(int parameter:lazyInitializerParameters(lazyFactory))if(parameter<args.size())for(V alternative:alternatives(args.get(parameter)))if(idx.function0Type(alternative.type()))initializer=union(initializer,alternative);
                if(initializer!=null)return expr("lazy",CapabilityIndex.cls(lazyFactory.getReturnType()),"lazy:"+initializer.id(),List.of(initializer));
            }
            if(lazyFactory==null&&name.equals("lazy")&&owner(v.id())!=null&&owner(v.id()).startsWith("kotlin.LazyKt")&&!args.isEmpty())return expr("lazy","kotlin.Lazy","lazy:"+args.get(args.size()-1).id(),List.of(args.get(args.size()-1)));
            if(name.equals("getValue")&&idx.lazyType(owner(v.id()))&&!args.isEmpty()){
                V result=null;for(V lazy:alternatives(args.get(0)))if(lazy.kind().equals("lazy"))for(V initializer:alternatives(lazy.args().get(0)))if(initializer.type()!=null)
                    for(Method invoke:idx.byClass.getOrDefault(initializer.type(),List.of()))if(invoke.getName().equals("invoke")&&invoke.getParameterTypes().isEmpty()&&invoke.getImplementation()!=null)result=union(result,eval(expr("return",CapabilityIndex.cls(invoke.getReturnType()),CapabilityIndex.key(invoke),List.of(initializer)),job,h,depth+1,new HashSet<>(visiting)));
                if(result!=null)return result;
            }
            if(!args.isEmpty()&&name.equals("asList")&&"java.util.Arrays".equals(owner(v.id())))return args.get(0);
            if(!args.isEmpty()&&name.equals("singletonList")&&"java.util.Collections".equals(owner(v.id()))){V container=V.of("object","java.util.List","singleton:"+args.get(0).id());applyWrite(h,"$contents",container,args.get(0));return container;}
            if(!args.isEmpty()&&name.equals("iterator")&&idx.collection(owner(v.id())))return expr("iterator","java.util.Iterator","iterator:"+args.get(0).id(),List.of(args.get(0)));
            if(!args.isEmpty()&&name.equals("next")&&owner(v.id()).equals("java.util.Iterator")&&args.get(0).kind().equals("iterator"))return elements(h,args.get(0).args().get(0),v.type());
            if(!args.isEmpty()&&name.equals("get")&&idx.collection(owner(v.id())))return elements(h,args.get(0),v.type());
            Method method=idx.resolve(v.id());V result=null;Set<Method> targets=new LinkedHashSet<>();
            if(method!=null){
                if(!v.kind().equals("return_direct")&&(method.getAccessFlags()&8)==0&&!args.isEmpty())for(V recv:alternatives(args.get(0)))if(recv.type()!=null){Method concrete=idx.resolve(desc(recv.type())+"->"+CapabilityIndex.shape(method));if(concrete!=null&&concrete.getImplementation()!=null)targets.add(concrete);}
                if(targets.isEmpty()&&method.getImplementation()==null){
                    String returnType=CapabilityIndex.cls(method.getReturnType());
                    boolean factory=idx.collection(returnType)||idx.webview(returnType)||idx.subtype(returnType,"android.webkit.WebViewClient")||idx.subtype(returnType,"android.webkit.WebChromeClient");
                    if(factory)for(Method possible:idx.byShape.getOrDefault(CapabilityIndex.shape(method),List.of()))if(possible.getImplementation()!=null&&idx.subtype(CapabilityIndex.cls(possible.getDefiningClass()),CapabilityIndex.cls(method.getDefiningClass()))){
                        if(targets.size()>=16){h.gaps.add("factory_dispatch_budget:"+v.id());break;}targets.add(possible);
                    }
                }
                if(targets.isEmpty())targets.add(method);
            }
            for(Method actual:targets)if(visiting.add(CapabilityIndex.key(actual))){
                Job nested=new Job(actual,args,job.path,true);Summary summary=flow.summary(actual,v0->guardValue(v0,nested,h,0));
                if(idx.collection(CapabilityIndex.cls(actual.getReturnType())))for(Write w:summary.writes()){
                    V receiver=eval(w.receiver(),nested,h,depth+1,new HashSet<>(visiting));V value=eval(w.value(),nested,h,depth+1,new HashSet<>(visiting));applyWrite(h,w.field(),receiver,value);
                }
                for(V ret:summary.returns())result=union(result,eval(ret,nested,h,depth+1,new HashSet<>(visiting)));
            }
            return result==null?new V("unknown",v.type(),v.id(),null,List.of()):result;
        }
        return v;
    }
    void materializeConstructor(Method ctor,List<V> args,Job outer,Host h,int depth,Set<String> visiting){
        if(args.isEmpty())return;
        if(depth>12){h.gaps.add("constructor_capture_depth:"+CapabilityIndex.key(ctor));return;}
        if(System.nanoTime()>deadline){h.gaps.add("global_deadline");return;}
        String context=CapabilityIndex.key(ctor)+"|"+args;
        if(!h.materialized.add(context))return;
        if(h.materialized.size()>12000){h.gaps.add("constructor_materialization_budget");return;}
        h.constructed.add(args.get(0).id());
        Job job=new Job(ctor,args,extend(outer.path,CapabilityIndex.key(ctor)),outer.candidate);
        Summary summary=flow.summary(ctor,v->guardValue(v,job,h,0));
        // Bind captured fields now, before a factory return is dereferenced by its caller.
        // Keep the actual constructor job so initialization capabilities are also emitted.
        for(Write write:summary.writes())applyWrite(h,write.field(),eval(write.receiver(),job,h,depth+1,new HashSet<>(visiting)),eval(write.value(),job,h,depth+1,new HashSet<>(visiting)));
        for(Call call:summary.calls())if(name(call.method()).equals("<init>")&&!call.args().isEmpty()){
            V receiver=eval(call.args().get(0),job,h,depth+1,new HashSet<>(visiting));
            if(!receiver.id().equals(args.get(0).id()))continue;
            Method parent=idx.resolve(call.method());if(parent==null)continue;
            List<V> bound=new ArrayList<>();bound.add(receiver);
            for(int i=1;i<call.args().size();i++)bound.add(eval(call.args().get(i),job,h,depth+1,new HashSet<>(visiting)));
            materializeConstructor(parent,bound,job,h,depth+1,visiting);
        }
        enqueue(h,ctor,args,outer.path,outer.candidate||summary.branched());
    }
    V installedProvider(String field,Job outer,Host h,int depth,Set<String> visiting){
        if(depth>10||!visiting.add("provider:"+field))return null;
        V result=null;int inspected=0;
        for(String writerId:idx.fieldWriters.getOrDefault(field,Set.of())){
            Method writer=idx.methods.get(writerId);if(writer==null||writer.getImplementation()==null)continue;
            for(Write write:flow.summary(writer).writes()){
                if(!write.field().equals(field)||!write.receiver().kind().equals("static")||!write.value().kind().equals("param"))continue;
                int parameter=Integer.parseInt(write.value().id());
                for(String callerId:idx.callers.getOrDefault(writerId,Set.of())){
                    if(inspected++>=64){h.gaps.add("provider_registration_budget:"+field);return result;}
                    Method caller=idx.methods.get(callerId);if(caller==null)continue;
                    List<V> callerArgs=new ArrayList<>();if((caller.getAccessFlags()&8)==0)callerArgs.add(V.of("object",CapabilityIndex.cls(caller.getDefiningClass()),"global_initializer:"+callerId));
                    for(CharSequence p:caller.getParameterTypes())callerArgs.add(V.of("unknown",CapabilityIndex.cls(p.toString()),"global_initializer_parameter"));
                    Job context=new Job(caller,callerArgs,outer.path,true);
                    for(Call registration:flow.summary(caller).calls()){
                        Method target=idx.resolve(registration.method());
                        if(target==null||!CapabilityIndex.key(target).equals(writerId)||parameter>=registration.args().size())continue;
                        V candidate=eval(registration.args().get(parameter),context,h,depth+1,new HashSet<>(visiting));
                        for(V alternative:alternatives(candidate))if(alternative.kind().equals("object"))result=union(result,alternative);
                    }
                }
            }
        }
        return result;
    }
    void applyWrite(Host h,String field,V receiver,V value){
        if(field.equals("$contents")||field.equals("$contentsAll")||field.startsWith("$element:")){
            for(V recv:alternatives(receiver)){Set<V> values=h.contents.computeIfAbsent(recv.id(),k->new LinkedHashSet<>());
                for(V val:alternatives(value))if(field.equals("$contentsAll"))values.addAll(h.contents.getOrDefault(val.id(),Set.of()));else values.add(val);
                if(values.size()>256){h.gaps.add("collection_element_budget:"+recv.id());values.clear();values.add(UNKNOWN);}
            }
        }else for(V alternative:alternatives(receiver)){String key=heapKey(field,alternative);h.heap.put(key,union(h.heap.get(key),value));}
    }
    V elements(Host h,V collection,String type){
        Set<V> values=new LinkedHashSet<>();for(V recv:alternatives(collection))values.addAll(h.contents.getOrDefault(recv.id(),Set.of()));
        if(values.isEmpty())return V.of("unknown",type,"unresolved_collection:"+collection.id());
        if(values.size()==1)return values.iterator().next();
        return new V("union",type,"elements:"+collection.id(),null,List.copyOf(values));
    }
    static String allocationContext(Job job){
        if(job.args.isEmpty())return "static";
        String id=job.args.get(0).id();String[] chain=id.split("\\|",-1);
        // Two allocation sites of object sensitivity bound recursive component factories.
        if(chain.length<=2)return id;
        return chain[0]+"|"+chain[1];
    }
    static String heapKey(String field,V receiver){return receiver.id()+"::"+field;}
    static String owner(String id){int sep=id.indexOf("->");return sep<0?null:CapabilityIndex.cls(id.substring(0,sep));}
    static String name(String id){int p=id.indexOf("->"),e=id.indexOf('(',p);return p<0?"":id.substring(p+2,e<0?id.length():e);}
    static String desc(String cls){return "L"+cls.replace('.','/')+";";}
    String kind(String method){if(idx.messageRegistries.containsKey(method))return "message_bridge";Method m=idx.resolve(method);if(m!=null)return idx.messageRegistries.containsKey(CapabilityIndex.key(m))?"message_bridge":idx.kind(m);
        // Framework methods need not be packaged inside the APK.
        int sep=method.indexOf("->"),start=method.indexOf('(',sep),end=method.indexOf(')',start);
        if(start<0)return null;
        var ref=new org.jf.dexlib2.immutable.reference.ImmutableMethodReference(method.substring(0,sep),method.substring(sep+2,start),parameters(method.substring(start+1,end)),method.substring(end+1));return idx.kind(ref);
    }
    static List<String> parameters(String descriptor){
        List<String> p=new ArrayList<>();int i=0;while(i<descriptor.length()){int start=i;while(descriptor.charAt(i)=='[')i++;if(descriptor.charAt(i)=='L')i=descriptor.indexOf(';',i)+1;else i++;p.add(descriptor.substring(start,i));}return p;
    }
    void emit(Host h,Job job,Call call,List<V> args,String kind,boolean conditional){
        String site=CapabilityIndex.key(job.method)+"@"+call.offset();boundSites.add(site);
        V recv=args.isEmpty()?UNKNOWN:args.get(0);if(kind.equals("setting")&&recv.kind().equals("settings"))recv=recv.args().get(0);
        if(kind.equals("message_bridge")&&h.bridgeViews.containsKey(recv.id()))recv=h.bridgeViews.get(recv.id());
        // A nullable receiver describes a condition, never an additional WebView object.
        boolean nullable=false;V live=null;
        for(V alternative:alternatives(recv)){
            if(!call.isStatic()&&alternative.kind().equals("literal")&&"0".equals(alternative.literal()))nullable=true;
            else live=union(live,alternative);
        }
        if(live==null)return;recv=live;
        for(V view:alternatives(recv))if((view.kind().equals("object")||view.kind().equals("view"))&&view.type()!=null&&idx.webview(view.type())&&!Cfg.WEBVIEWS.contains(view.type()))seed(h,view.type(),view,job.path,true);
        String op=name(call.method());
        Map<String,Object> base=new LinkedHashMap<>();base.put("activity",h.activity);base.put("kind",kind);base.put("name",op);base.put("site",site);base.put("api",call.method());base.put("webview",Map.of("id",recv.id(),"type",recv.type()==null?"unknown":recv.type()));if(recv.kind().equals("union"))base.put("webview_alternatives",alternatives(recv).stream().map(v->Map.of("id",v.id(),"type",v.type()==null?"unknown":v.type())).toList());base.put("binding_status",job.candidate||conditional||nullable||recv.kind().equals("unknown")?"candidate":"explicit");base.put("conditional",conditional||nullable);if(nullable)base.put("receiver_condition","non_null");base.put("evidence",job.path);base.put("arguments",args);
        if(kind.equals("message_bridge")&&!args.isEmpty()){
            List<Map<String,Object>> transports=new ArrayList<>();
            for(V registry:alternatives(args.get(0)))for(var binding:h.nativeBindings.getOrDefault(registry.id(),List.of()))if(!transports.contains(binding))transports.add(binding);
            if(!transports.isEmpty())base.put("transport_bindings",transports);
        }
        if(kind.equals("setting")||kind.equals("global_setting")){
            List<String> values=new ArrayList<>();int from=call.isStatic()?0:1;
            for(int i=from;i<args.size();i++){V v=args.get(i);for(V option:alternatives(v))values.add(option.literal()==null?"unknown":option.literal());}
            // Boolean signatures express boolean values, not raw Dalvik integers.
            if(call.method().endsWith("(Z)V"))values=values.stream().map(v->v.equals("1")?"true":v.equals("0")?"false":v).toList();
            base.put("values",values);base.put("value",values.size()==1?values.get(0):"unknown");add(h,base);return;
        }
        if(kind.equals("bridge")&&args.size()>=3){
            base.put("registration_name",args.get(2).literal()==null?"unknown":args.get(2).literal());
            for(V obj:alternatives(args.get(1))){
                if(obj.kind().equals("literal")&&"0".equals(obj.literal()))continue;
                h.bridgeViews.put(obj.id(),union(h.bridgeViews.get(obj.id()),recv));
                var transports=h.nativeBindings.computeIfAbsent(obj.id(),k->new ArrayList<>());
                for(V view:alternatives(recv)){
                    Map<String,Object> binding=Map.of("registration_name",args.get(2).literal()==null?"unknown":args.get(2).literal(),"webview",Map.of("id",view.id(),"type",view.type()==null?"unknown":view.type()),"site",site,"bridge_object_id",obj.id());
                    if(!transports.contains(binding))transports.add(binding);
                }
                List<String> types=new ArrayList<>();if(obj.type()!=null)types.add(obj.type());
                if(obj.kind().equals("unknown")||obj.kind().equals("field_object"))h.gaps.add("unresolved_bridge_implementation:"+site);
                if(types.isEmpty())types.add("unknown");
                for(String type:types){
                    Map<String,Object> b=new LinkedHashMap<>(base);b.put("implementation",type);b.put("members",bridgeMembers(type));
                    if(obj.kind().equals("unknown")||obj.kind().equals("field_object")){
                        b.put("binding_status","candidate");b.put("resolution","declared_bridge_contract_only");b.put("declared_type",type);b.put("implementation","unknown");
                    }
                    if(b.get("registration_name").equals("unknown")){
                        for(Call reflect:flow.summary(job.method).calls())if(name(reflect.method()).equals("getField")&&reflect.args().size()>1){
                            V fieldName=eval(reflect.args().get(1),job,h,0,new HashSet<>());
                            if(fieldName.literal()!=null){V tag=staticConstant(type,fieldName.literal());if(tag.literal()!=null){b.put("registration_name",tag.literal());b.put("name_resolution","reflective_public_field:"+fieldName.literal());}}
                        }
                    }
                    add(h,b);
                    reflectEndpoints(h,job,obj,type,b);
                }
            }return;
        }
        if(kind.equals("callback")&&args.size()>=2){
            Method setter=idx.resolve(call.method());var custom=idx.customCallbacks.get(setter==null?call.method():CapabilityIndex.key(setter));
            if(custom!=null){base.put("callback_field",custom.field());base.put("callback_contract",custom.contract());base.put("resolution","receiver_field_stored_listener");}
            for(V client:alternatives(args.get(1))){
                if(custom!=null)applyWrite(h,custom.field(),recv,client);
                if(client.kind().equals("literal")&&"0".equals(client.literal())){Map<String,Object> reset=new LinkedHashMap<>(base);reset.put("kind","callback_removal");add(h,reset);continue;}
                Map<String,Object>b=new LinkedHashMap<>(base);b.put("implementation",client.type()==null?"unknown":client.type());b.put("members",custom==null?callbackMembers(client.type()):customCallbackMembers(client.type(),custom));add(h,b);
                if(client.type()!=null)for(Method callback:idx.hierarchyMethods(client.type()))if((custom==null?CALLBACKS.contains(callback.getName()):idx.contractMethods(custom.contract()).stream().anyMatch(m->CapabilityIndex.shape(m).equals(CapabilityIndex.shape(callback))))&&idx.relevant.contains(CapabilityIndex.key(callback))){
                    List<V> callbackArgs=new ArrayList<>();callbackArgs.add(client);
                    for(CharSequence p:callback.getParameterTypes())callbackArgs.add(idx.webview(CapabilityIndex.cls(p.toString()))?recv:V.of("unknown",CapabilityIndex.cls(p.toString()),"callback_parameter"));
                    enqueue(h,callback,callbackArgs,job.path,true);
                }
            }return;
        }
        Method registry=idx.resolve(call.method());String registryKey=registry==null?call.method():CapabilityIndex.key(registry);
        if(kind.equals("message_bridge")&&args.size()>=3&&idx.namespaceRegistries.containsKey(registryKey)){
            String namespace=args.get(2).literal();if("0".equals(namespace))namespace="";
            base.put("registration_name",namespace==null?"unknown":namespace);base.put("namespace",namespace==null?"unknown":namespace);base.put("registry_field",registryField(call.method()));
            for(V target:alternatives(args.get(1))){Map<String,Object> fact=new LinkedHashMap<>(base);fact.put("implementation",target.type()==null?"unknown":target.type());
                List<Map<String,Object>> members=bridgeMembers(target.type()).stream().filter(member->{String id=(String)member.get("signature");return idx.namespaceRegistries.get(registryKey).contains(parameters(id.substring(id.indexOf('(')+1,id.indexOf(')'))));}).toList();
                fact.put("members",members);fact.put("resolution","annotated_transport_namespace_registry_reflection_shapes");add(h,fact);
            }return;
        }
        if(kind.equals("message_bridge")&&args.size()>=3&&registryField(call.method())!=null){
            base.put("registration_name",args.get(1).literal()==null?"unknown":args.get(1).literal());
            base.put("registry_field",registryField(call.method()));
            for(V handler:alternatives(args.get(2))){
                Map<String,Object> b=new LinkedHashMap<>(base);b.put("implementation",handler.type()==null?"unknown":handler.type());
                HandlerSurface surface=messageMembers(handler.type(),args.get(1).literal(),job,h);List<Map<String,Object>> members=new ArrayList<>(surface.members());
                if(surface.reflective())b.put("endpoint_status",surface.resolved()?members.isEmpty()?"registered-no-compatible-endpoint":"resolved":"registered-target-unknown");
                if(members.isEmpty()&&!surface.reflective()&&handler.type()!=null){Set<String> shapes=idx.registryHandlerShapes.getOrDefault(registryKey,Set.of());for(Method member:idx.hierarchyMethods(handler.type()))if(shapes.contains(CapabilityIndex.shape(member))&&member.getImplementation()!=null)members.add(Map.of("signature",CapabilityIndex.key(member),"display",CapabilityIndex.display(member),"resolution","registered_handler_interface_dispatch"));}
                b.put("members",members);b.put("resolution","annotated_transport_shared_registry");
                if(!members.isEmpty())b.put("implementation",owner((String)members.get(0).get("signature")));
                add(h,b);
            }return;
        }
        add(h,base);
    }
    void reflectEndpoints(Host h,Job job,V bridge,String type,Map<String,Object> base){
        for(Method transport:idx.hierarchyMethods(type)){
            Set<String> fields=idx.reflectionFields.getOrDefault(CapabilityIndex.key(transport),Set.of());
            if(fields.isEmpty())continue;
            // Follow only fields rooted at this injected bridge instance, never all objects in the Activity.
            ArrayDeque<V> queue=new ArrayDeque<>();queue.add(bridge);Set<String> seen=new HashSet<>();int budget=40;
            Map<String,Set<V>> targets=new LinkedHashMap<>();
            while(!queue.isEmpty()&&budget-->0){V obj=queue.remove();if(!seen.add(obj.id()))continue;
                for(var entry:h.heap.entrySet())if(entry.getKey().startsWith(obj.id()+"::")){
                    String field=entry.getKey().substring(obj.id().length()+2);
                    for(V value:alternatives(entry.getValue())){
                        if(fields.contains(field))targets.computeIfAbsent(field,k->new LinkedHashSet<>()).add(value);
                        else if(value.type()!=null&&idx.bindingObjects.contains(value.type()))queue.add(value);
                    }
                }
            }
            for(var entry:targets.entrySet())for(V target:entry.getValue()){
                List<String> types=target.type()==null?List.of():List.of(target.type());
                if(target.kind().equals("unknown")||target.kind().equals("field_object"))h.gaps.add("unresolved_reflective_receiver:"+entry.getKey());
                for(String impl:types){
                    List<Map<String,Object>> members=new ArrayList<>();
                    for(Method m:idx.hierarchyMethods(impl))if((m.getAccessFlags()&1)!=0&&!m.getName().startsWith("<"))members.add(Map.of("signature",CapabilityIndex.key(m),"display",CapabilityIndex.display(m),"resolution","public_reflective_dispatch"));
                    if(members.isEmpty())continue;
                    Map<String,Object> fact=new LinkedHashMap<>(base);fact.put("kind","message_bridge");fact.put("implementation",impl);fact.put("members",members);fact.put("transport_signature",CapabilityIndex.key(transport));fact.put("router_field",entry.getKey());fact.put("resolution","injected_bridge_object_fields_to_reflective_receiver");fact.put("binding_status","candidate");add(h,fact);
                }
            }
        }
    }
    String registryField(String method){Method m=idx.resolve(method);return idx.messageRegistries.get(m==null?method:CapabilityIndex.key(m));}
    V staticConstant(String type,String name){
        String cacheKey=type+"|"+name;if(constants.containsKey(cacheKey))return constants.get(cacheKey);
        constants.put(cacheKey,UNKNOWN);
        for(String t=type;t!=null;){ClassDef c=idx.classes.get(t);if(c==null)break;
            for(Field f:c.getFields())if(f.getName().equals(name)){
                if(f.getInitialValue()!=null){V v=encoded(f.getInitialValue());if(!v.equals(UNKNOWN)){constants.put(cacheKey,v);return v;}}
                for(Method init:idx.byClass.getOrDefault(t,List.of()))if(init.getName().equals("<clinit>")||init.getName().equals("<init>")){
                    Host host=new Host("static");Job job=new Job(init,List.of(),List.of(),true);
                    for(Write write:flow.summary(init).writes())if(write.field().equals(CapabilityIndex.field(f))){V v=eval(write.value(),job,host,0,new HashSet<>());if(v.literal()!=null){constants.put(cacheKey,v);return v;}}
                }
            }
            t=CapabilityIndex.cls(c.getSuperclass());
        }return UNKNOWN;
    }
    record HandlerSurface(List<Map<String,Object>> members,boolean reflective,boolean resolved){}
    HandlerSurface messageMembers(String type,String registered,Job outer,Host h){
        if(type==null)return new HandlerSurface(List.of(),false,false);List<Map<String,Object>> result=new ArrayList<>();boolean reflective=false,resolved=false;
        for(Method method:idx.hierarchyMethods(type)){
            if(method.getImplementation()==null||method.getName().startsWith("<"))continue;
            Summary summary=flow.summary(method);
            if(summary.calls().stream().noneMatch(c->c.method().startsWith("Ljava/lang/reflect/Method;->invoke(")))continue;
            List<V> args=new ArrayList<>();if((method.getAccessFlags()&8)==0)args.add(V.of("unknown",type,"handler"));
            for(CharSequence p:method.getParameterTypes())args.add(V.of("unknown",CapabilityIndex.cls(p.toString()),"handler_parameter"));
            Job job=new Job(method,args,outer.path,true);
            for(Call ref:summary.calls())if(name(ref.method()).equals("getDeclaredMethod")&&ref.args().size()==3&&registered!=null){
                V clazz=eval(ref.args().get(0),job,h,0,new HashSet<>());V array=ref.args().get(2);
                if(!clazz.kind().equals("class"))continue;
                V selector=eval(ref.args().get(1),job,h,0,new HashSet<>());
                if(selector.literal()!=null&&!selector.literal().equals(registered))continue;
                reflective=true;
                TreeMap<Integer,String> params=new TreeMap<>();
                if(array.kind().equals("array"))for(int i=0;i<array.args().size();i++)if(array.args().get(i).kind().equals("class"))params.put(i,array.args().get(i).id());
                for(Write w:summary.writes())if(w.field().startsWith("$element:")&&w.receiver().equals(array)){
                    try{V p=eval(w.value(),job,h,0,new HashSet<>());if(p.kind().equals("class"))params.put(Integer.parseInt(w.field().substring(9)),p.id());}catch(NumberFormatException ignored){}
                }
                if(params.isEmpty())continue;
                resolved=true;
                for(Method exposed:idx.byClass.getOrDefault(clazz.type(),List.of()))if(exposed.getName().equals(registered)&&exposed.getParameterTypes().equals(new ArrayList<>(params.values())))
                    result.add(Map.of("signature",CapabilityIndex.key(exposed),"display",CapabilityIndex.display(exposed),"resolution","reflective_registered_handler","handler_signature",CapabilityIndex.key(method)));
            }
        }return new HandlerSurface(result,reflective,resolved);
    }
    void add(Host h,Map<String,Object> fact){
        String key=fact.get("kind")+"|"+fact.get("site")+"|"+fact.get("webview")+"|"+fact.get("implementation")+"|"+fact.get("registration_name")+"|"+fact.get("values");
        Map<String,Object> old=h.facts.get(key);if(old==null||fact.get("binding_status").equals("explicit"))h.facts.put(key,fact);
    }
    List<Map<String,Object>> bridgeMembers(String type){
        if(type==null)return List.of();List<Map<String,Object>> result=new ArrayList<>();
        for(Method m:idx.hierarchyMethods(type))if((m.getAccessFlags()&1)!=0&&(m.getAccessFlags()&8)==0&&!m.getName().startsWith("<")){
            boolean annotated=m.getAnnotations().stream().anyMatch(a->a.getType().endsWith("/JavascriptInterface;"));
            if(annotated||apk.targetSdk>0&&apk.targetSdk<17)result.add(Map.of("signature",CapabilityIndex.key(m),"display",CapabilityIndex.display(m),"annotated",annotated));
        }return result;
    }
    List<Map<String,Object>> customCallbackMembers(String type,CapabilityIndex.CustomCallback custom){
        if(type==null)return List.of();Set<String> shapes=new HashSet<>();
        for(Method declaration:idx.contractMethods(custom.contract()))if((declaration.getAccessFlags()&8)==0&&!declaration.getName().startsWith("<"))shapes.add(CapabilityIndex.shape(declaration));
        List<Map<String,Object>> result=new ArrayList<>();
        for(Method method:idx.hierarchyMethods(type))if(method.getImplementation()!=null&&(method.getAccessFlags()&8)==0&&shapes.contains(CapabilityIndex.shape(method)))
            result.add(Map.of("signature",CapabilityIndex.key(method),"display",CapabilityIndex.display(method),"name",method.getName(),"dispatch_observed",custom.dispatchedShapes().contains(CapabilityIndex.shape(method)),"dispatch_status",custom.dispatchedShapes().contains(CapabilityIndex.shape(method))?"observed":"unresolved"));
        return result;
    }
    List<Map<String,Object>> callbackMembers(String type){
        if(type==null)return List.of();
        if(callbackCache.containsKey(type))return callbackCache.get(type);
        List<Map<String,Object>> result=new ArrayList<>();Set<String> seen=new HashSet<>();ArrayDeque<Method> queue=new ArrayDeque<>();
        for(Method m:idx.hierarchyMethods(type))if(CALLBACKS.contains(m.getName()))queue.add(m);
        while(!queue.isEmpty()&&seen.size()<256){Method m=queue.remove();String id=CapabilityIndex.key(m);
            if(!seen.add(id)||(m.getAccessFlags()&8)!=0||m.getImplementation()==null)continue;
            String owner=CapabilityIndex.cls(m.getDefiningClass());if(owner.startsWith("android.webkit.")||owner.equals("com.tencent.smtt.sdk.WebViewClient")||owner.equals("com.tencent.smtt.sdk.WebChromeClient"))continue;
            result.add(Map.of("signature",id,"display",CapabilityIndex.display(m),"name",m.getName()));
            for(Call call:flow.summary(m).calls())if(call.isSuper()&&call.method().endsWith("->"+CapabilityIndex.shape(m))){Method parent=idx.resolve(call.method());if(parent!=null)queue.add(parent);}
        }
        callbackCache.put(type,result);return result;
    }
    Map<String,Object> hostReport(Host h){
        Map<String,Object> report=new LinkedHashMap<>();report.put("activity",h.activity);report.put("declared",apk.activities.contains(h.activity));
        if(!h.serviceEvidence.isEmpty())report.put("service_bindings",new ArrayList<>(h.serviceEvidence.values()));
        List<Map<String,Object>> facts=new ArrayList<>(h.facts.values());report.put("facts",facts);
        Map<Object,Map<String,Object>> views=new LinkedHashMap<>();
        for(int i=0;i<facts.size();i++){
            Map<String,Object> fact=facts.get(i);Object view=fact.get("webview");
            @SuppressWarnings("unchecked") Map<String,Object> identity=(Map<String,Object>)view;
            Map<String,Object> group=views.computeIfAbsent(view,k->{var g=new LinkedHashMap<String,Object>(identity);g.put("capability_indices",new TreeMap<String,List<Integer>>());return g;});
            @SuppressWarnings("unchecked") Map<String,List<Integer>> categories=(Map<String,List<Integer>>)group.get("capability_indices");
            categories.computeIfAbsent((String)fact.get("kind"),k->new ArrayList<>()).add(i);
        }
        report.put("webviews",new ArrayList<>(views.values()));report.put("limitations",h.gaps.stream().distinct().sorted().toList());return report;
    }
    Map<String,Object> report(String hash,String status,Map<String,Object> metrics){
        List<Map<String,Object>> unbound=new ArrayList<>();
        for(String id:idx.seeds){Method m=idx.methods.get(id);if(m==null)continue;Summary s=flow.cache.get(id);
            if(s==null){unbound.add(Map.of("method",id,"reason","not_expanded"));continue;}
            for(Call call:s.calls())if(kind(call.method())!=null&&!boundSites.contains(id+"@"+call.offset()))unbound.add(Map.of("site",id+"@"+call.offset(),"api",call.method(),"reason","no_activity_owner"));
        }
        Map<String,Object> out=new LinkedHashMap<>();out.put("schema_version",1);out.put("package",apk.packageName);out.put("version",apk.version);out.put("apk_sha256",hash);out.put("status",status);List<Map<String,Object>> snapshots=new ArrayList<>(activities);if(currentHost!=null&&!currentHost.facts.isEmpty())snapshots.add(hostReport(currentHost));out.put("activities",snapshots);out.put("unattributed",unbound);out.put("diagnostics",diagnostics);out.put("index_diagnostics",idx.diagnostics);out.put("manifest_diagnostics",apk.errors);out.put("metrics",metrics);out.put("semantics","Static binding evidence; explicit does not prove runtime execution. Candidate bindings are retained. Settings are observed operations, not final runtime state.");return out;
    }
}
