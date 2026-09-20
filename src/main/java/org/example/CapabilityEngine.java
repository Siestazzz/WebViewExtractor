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
    Runnable checkpoint=()->{};
    Host currentHost;
    static final Set<String> CALLBACKS=Set.of("onPageStarted","onPageFinished","onPageCommitVisible","onLoadResource","shouldOverrideUrlLoading","shouldInterceptRequest","onTooManyRedirects","onReceivedError","onReceivedHttpError","onFormResubmission","doUpdateVisitedHistory","onReceivedSslError","onReceivedClientCertRequest","onReceivedHttpAuthRequest","shouldOverrideKeyEvent","onUnhandledKeyEvent","onScaleChanged","onReceivedLoginRequest","onRenderProcessGone","onSafeBrowsingHit","onProgressChanged","onReceivedTitle","onReceivedIcon","onReceivedTouchIconUrl","onShowCustomView","onHideCustomView","onCreateWindow","onRequestFocus","onCloseWindow","onJsAlert","onJsConfirm","onJsPrompt","onJsBeforeUnload","onExceededDatabaseQuota","onReachedMaxAppCacheSize","onGeolocationPermissionsShowPrompt","onGeolocationPermissionsHidePrompt","onPermissionRequest","onPermissionRequestCanceled","onJsTimeout","onConsoleMessage","getDefaultVideoPoster","getVideoLoadingProgressView","getVisitedHistory","onShowFileChooser","openFileChooser");
    CapabilityEngine(CapabilityIndex idx,ApkInventory apk,long deadline){this.idx=idx;this.apk=apk;this.deadline=deadline;flow=new DexFlow(idx,deadline);}
    record Job(Method method,List<V> args,List<String> path,boolean candidate){}
    final class Host {
        final String activity;
        final Map<String,V> heap=new HashMap<>();
        final Map<String,Set<V>> contents=new HashMap<>();
        final ArrayDeque<Job> queue=new ArrayDeque<>();
        final Set<String> visited=new HashSet<>(),components=new HashSet<>(),expanding=new HashSet<>();
        final Map<String,Map<String,Object>> facts=new TreeMap<>();
        final List<String> gaps=new ArrayList<>();
        Host(String a){activity=a;}
    }
    void analyzeActivity(String activity){
        Host h=new Host(activity);currentHost=h;V self=V.of("host",activity,"activity:"+activity);
        seed(h,activity,self,List.of(activity),false);
        for(int phase=0;phase<2;phase++){
            if(phase==1){h.facts.clear();h.visited.clear();h.components.clear();seed(h,activity,self,List.of(activity),false);}
            while(!h.queue.isEmpty()){
                if(System.nanoTime()>deadline){h.gaps.add("global_deadline");break;}
                if(h.visited.size()>4000){h.gaps.add("host_context_budget");break;}
                checkpoint.run();
                Job job=h.queue.remove();String id=CapabilityIndex.key(job.method);
                String context=id+"|"+job.args;if(!h.visited.add(context))continue;
                Summary summary;
                try{summary=flow.summary(job.method);}catch(RuntimeException ex){h.gaps.add("decode_failed:"+id+":"+ex.getClass().getSimpleName());continue;}
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
                        Set<Method> targets=new LinkedHashSet<>();
                        if(idx.relevant.contains(CapabilityIndex.key(target))||name.equals("<init>")&&lifecycle)targets.add(target);
                        if(!call.isStatic()&&!call.isSuper()&&!args.isEmpty()&&!name.equals("<init>")){
                            for(V recv:alternatives(args.get(0)))if(recv.type()!=null){
                                Method concrete=idx.resolve(desc(recv.type())+"->"+CapabilityIndex.shape(target));
                                if(concrete!=null&&idx.relevant.contains(CapabilityIndex.key(concrete)))targets.add(concrete);
                                if(recv.kind().equals("unknown")||recv.kind().equals("field_object")||concrete==null||concrete.getImplementation()==null){
                                    for(Method possible:idx.byShape.getOrDefault(CapabilityIndex.shape(target),List.of()))
                                        if(idx.relevant.contains(CapabilityIndex.key(possible))&&idx.subtype(CapabilityIndex.cls(possible.getDefiningClass()),recv.type()))targets.add(possible);
                                }
                            }
                        }
                        if(targets.size()>64){h.gaps.add("dispatch_budget:"+call.method());}
                        int dispatched=0;
                        for(Method actual:targets){if(dispatched++>=64)break;enqueue(h,actual,args,job.path,job.candidate||summary.branched()||targets.size()>1);}
                    }

                    if(name.equals("<init>")&&!args.isEmpty()&&(idx.component(owner)||idx.scheduled(owner)||idx.callbackEntries.containsKey(owner))&&!idx.activity(owner))seed(h,owner,args.get(0),extend(job.path,id+"@"+call.offset()),true);
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
                    V fv=h.heap.getOrDefault(heapKey(CapabilityIndex.field(f),self),V.of("field_object",ft,heapKey(CapabilityIndex.field(f),self)));
                    seed(h,ft,fv,extend(path,CapabilityIndex.field(f)),true);
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
        h.queue.add(new Job(m,args,extend(path,CapabilityIndex.key(m)),candidate));
    }
    static List<String> extend(List<String> path,String s){var p=new ArrayList<>(path);p.add(s);return List.copyOf(p);}
    V eval(V v,Job job,Host h,int depth,Set<String> visiting){
        if(System.nanoTime()>deadline)return V.of("unknown",v.type(),"global_deadline");
        if(depth>10)return V.of("unknown",v.type(),"resolve_depth");
        if(v.kind().equals("param")){int i=Integer.parseInt(v.id());return i<job.args.size()?job.args.get(i):V.of("unknown",v.type(),"parameter");}
        if(v.kind().equals("array")){V array=V.of("object",v.type(),v.id()+"|"+allocationContext(job));for(V element:v.args())applyWrite(h,"$contents",array,eval(element,job,h,depth+1,new HashSet<>(visiting)));return array;}
        if(v.kind().equals("array_element"))return elements(h,eval(v.args().get(0),job,h,depth+1,visiting),v.type());
        if(v.kind().equals("new"))return V.of("object",v.type(),v.id()+"|"+allocationContext(job));
        if(v.kind().equals("union")){V result=null;for(V x:v.args())result=union(result,eval(x,job,h,depth+1,new HashSet<>(visiting)));return result==null?UNKNOWN:result;}
        if(v.kind().equals("cast")){
            V original=eval(v.args().get(0),job,h,depth+1,visiting);
            V cast=original.kind().equals("union")?original:new V(original.kind(),original.kind().equals("object")?original.type():v.type(),original.id(),original.literal(),original.args());
            if(original.kind().equals("view")&&idx.webview(v.type())&&!Cfg.WEBVIEWS.contains(v.type()))seed(h,v.type(),cast,job.path,true);
            return cast;
        }
        if(v.kind().equals("settings"))return expr("settings",v.type(),v.id(),List.of(eval(v.args().get(0),job,h,depth+1,visiting)));
        if(v.kind().equals("field")){
            V receiver=eval(v.args().get(0),job,h,depth+1,new HashSet<>(visiting));String hk=heapKey(v.id(),receiver);
            if(idx.subtype(v.type(),"java.lang.Enum"))return new V("enum",v.type(),v.id(),v.id().substring(v.id().indexOf("->")+2,v.id().indexOf(':')),List.of());
            V result=h.heap.get(hk);if(result!=null)return result;
            if(!visiting.add(hk))return V.of("field_object",v.type(),hk);
            String owner=owner(v.id());ClassDef c=idx.classes.get(owner);
            if(c!=null){
                for(Field f:c.getFields())if(CapabilityIndex.field(f).equals(v.id())&&f.getInitialValue()!=null){V initial=encoded(f.getInitialValue());if(!initial.equals(UNKNOWN))return initial;}
                for(Method m:idx.byClass.getOrDefault(owner,List.of()))if(m.getName().equals("<init>")||m.getName().equals("<clinit>")){
                    if(m.getImplementation()==null)continue;
                    Summary init=flow.summary(m);List<V> args=new ArrayList<>();if((m.getAccessFlags()&8)==0)args.add(receiver);
                    for(CharSequence t:m.getParameterTypes())args.add(V.of("unknown",CapabilityIndex.cls(t.toString()),"constructor_parameter"));
                    Job nested=new Job(m,args,job.path,true);
                    for(Write w:init.writes())if(w.field().equals(v.id()))result=union(result,eval(w.value(),nested,h,depth+1,new HashSet<>(visiting)));
                }
            }
            return result!=null?result:V.of("field_object",v.type(),hk);
        }
        if(v.kind().equals("return")){
            List<V> args=v.args().stream().map(x->eval(x,job,h,depth+1,new HashSet<>(visiting))).toList();String name=name(v.id());
            if((name.equals("findViewById")||name.equals("requireViewById"))&&args.size()>1)return V.of("view",v.type(),args.get(0).id()+"/view:"+args.get(1).id());
            if((name.equals("getActivity")||name.equals("requireActivity")))return V.of("host",h.activity,"activity:"+h.activity);
            if(!args.isEmpty()&&name.equals("asList")&&"java.util.Arrays".equals(owner(v.id())))return args.get(0);
            if(!args.isEmpty()&&name.equals("singletonList")&&"java.util.Collections".equals(owner(v.id()))){V container=V.of("object","java.util.List","singleton:"+args.get(0).id());applyWrite(h,"$contents",container,args.get(0));return container;}
            if(!args.isEmpty()&&name.equals("iterator")&&idx.collection(owner(v.id())))return expr("iterator","java.util.Iterator","iterator:"+args.get(0).id(),List.of(args.get(0)));
            if(!args.isEmpty()&&name.equals("next")&&owner(v.id()).equals("java.util.Iterator")&&args.get(0).kind().equals("iterator"))return elements(h,args.get(0).args().get(0),v.type());
            if(!args.isEmpty()&&name.equals("get")&&idx.collection(owner(v.id())))return elements(h,args.get(0),v.type());
            Method method=idx.resolve(v.id());V result=null;Set<Method> targets=new LinkedHashSet<>();
            if(method!=null){
                if((method.getAccessFlags()&8)==0&&!args.isEmpty())for(V recv:alternatives(args.get(0)))if(recv.type()!=null){Method concrete=idx.resolve(desc(recv.type())+"->"+CapabilityIndex.shape(method));if(concrete!=null&&concrete.getImplementation()!=null)targets.add(concrete);}
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
                Summary summary=flow.summary(actual);Job nested=new Job(actual,args,job.path,true);
                if(idx.collection(CapabilityIndex.cls(actual.getReturnType())))for(Write w:summary.writes()){
                    V receiver=eval(w.receiver(),nested,h,depth+1,new HashSet<>(visiting));V value=eval(w.value(),nested,h,depth+1,new HashSet<>(visiting));applyWrite(h,w.field(),receiver,value);
                }
                for(V ret:summary.returns())result=union(result,eval(ret,nested,h,depth+1,new HashSet<>(visiting)));
            }
            return result==null?new V("unknown",v.type(),v.id(),null,List.of()):result;
        }
        return v;
    }
    void applyWrite(Host h,String field,V receiver,V value){
        if(field.equals("$contents")||field.equals("$contentsAll")||field.startsWith("$element:")){
            for(V recv:alternatives(receiver)){Set<V> values=h.contents.computeIfAbsent(recv.id(),k->new LinkedHashSet<>());
                for(V val:alternatives(value))if(field.equals("$contentsAll"))values.addAll(h.contents.getOrDefault(val.id(),Set.of()));else values.add(val);
                if(values.size()>256){h.gaps.add("collection_element_budget:"+recv.id());values.clear();values.add(UNKNOWN);}
            }
        }else{String key=heapKey(field,receiver);h.heap.put(key,union(h.heap.get(key),value));}
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
        String op=name(call.method());
        Map<String,Object> base=new LinkedHashMap<>();base.put("activity",h.activity);base.put("kind",kind);base.put("name",op);base.put("site",site);base.put("api",call.method());base.put("webview",Map.of("id",recv.id(),"type",recv.type()==null?"unknown":recv.type()));base.put("binding_status",job.candidate||conditional||recv.kind().equals("unknown")?"candidate":"explicit");base.put("conditional",conditional);base.put("evidence",job.path);base.put("arguments",args);
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
                List<String> types=new ArrayList<>();if(obj.type()!=null)types.add(obj.type());
                if(obj.kind().equals("unknown")||obj.kind().equals("field_object"))for(String type:idx.possibleTypes(obj.type()))
                    if(!types.contains(type)&&!bridgeMembers(type).isEmpty())types.add(type);
                if(types.isEmpty())types.add("unknown");
                for(String type:types){
                    Map<String,Object> b=new LinkedHashMap<>(base);b.put("implementation",type);b.put("members",bridgeMembers(type));
                    if(!type.equals(obj.type())){b.put("binding_status","candidate");b.put("resolution","type_compatible_bridge_implementation");}
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
            for(V client:alternatives(args.get(1))){
                if(client.kind().equals("literal")&&"0".equals(client.literal())){Map<String,Object> reset=new LinkedHashMap<>(base);reset.put("kind","callback_removal");add(h,reset);continue;}
                Map<String,Object>b=new LinkedHashMap<>(base);b.put("implementation",client.type()==null?"unknown":client.type());b.put("members",callbackMembers(client.type()));add(h,b);
                if(client.type()!=null)for(Method callback:idx.hierarchyMethods(client.type()))if(CALLBACKS.contains(callback.getName())&&idx.relevant.contains(CapabilityIndex.key(callback))){
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
                List<Map<String,Object>> members=messageMembers(handler.type(),args.get(1).literal(),job,h);
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
                if(target.kind().equals("unknown")||target.kind().equals("field_object"))types=idx.possibleTypes(target.type());
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
    List<Map<String,Object>> messageMembers(String type,String registered,Job outer,Host h){
        if(type==null)return List.of();List<Map<String,Object>> result=new ArrayList<>();
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
                TreeMap<Integer,String> params=new TreeMap<>();
                for(Write w:summary.writes())if(w.field().startsWith("$element:")&&w.receiver().equals(array)){
                    try{V p=eval(w.value(),job,h,0,new HashSet<>());if(p.kind().equals("class"))params.put(Integer.parseInt(w.field().substring(9)),p.id());}catch(NumberFormatException ignored){}
                }
                if(params.isEmpty())continue;
                for(Method exposed:idx.byClass.getOrDefault(clazz.type(),List.of()))if(exposed.getName().equals(registered)&&exposed.getParameterTypes().equals(new ArrayList<>(params.values())))
                    result.add(Map.of("signature",CapabilityIndex.key(exposed),"display",CapabilityIndex.display(exposed),"resolution","reflective_registered_handler","handler_signature",CapabilityIndex.key(method)));
            }
        }return result;
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
