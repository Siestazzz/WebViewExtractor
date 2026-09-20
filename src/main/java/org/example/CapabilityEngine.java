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
    static final Set<String> CALLBACKS=Set.of("onPageStarted","onPageFinished","onPageCommitVisible","onLoadResource","shouldOverrideUrlLoading","shouldInterceptRequest","onTooManyRedirects","onReceivedError","onReceivedHttpError","onFormResubmission","doUpdateVisitedHistory","onReceivedSslError","onReceivedClientCertRequest","onReceivedHttpAuthRequest","shouldOverrideKeyEvent","onUnhandledKeyEvent","onScaleChanged","onReceivedLoginRequest","onRenderProcessGone","onSafeBrowsingHit","onProgressChanged","onReceivedTitle","onReceivedIcon","onReceivedTouchIconUrl","onShowCustomView","onHideCustomView","onCreateWindow","onRequestFocus","onCloseWindow","onJsAlert","onJsConfirm","onJsPrompt","onJsBeforeUnload","onExceededDatabaseQuota","onReachedMaxAppCacheSize","onGeolocationPermissionsShowPrompt","onGeolocationPermissionsHidePrompt","onPermissionRequest","onPermissionRequestCanceled","onJsTimeout","onConsoleMessage","getDefaultVideoPoster","getVideoLoadingProgressView","getVisitedHistory","onShowFileChooser","openFileChooser");
    CapabilityEngine(CapabilityIndex idx,ApkInventory apk,long deadline){this.idx=idx;this.apk=apk;this.deadline=deadline;flow=new DexFlow(idx,deadline);}
    record Job(Method method,List<V> args,List<String> path,boolean candidate){}
    final class Host {
        final String activity;
        final Map<String,V> heap=new HashMap<>();
        final ArrayDeque<Job> queue=new ArrayDeque<>();
        final Set<String> visited=new HashSet<>(),components=new HashSet<>(),expanding=new HashSet<>();
        final Map<String,Map<String,Object>> facts=new TreeMap<>();
        final List<String> gaps=new ArrayList<>();
        Host(String a){activity=a;}
    }
    void analyzeActivity(String activity){
        Host h=new Host(activity);V self=V.of("host",activity,"activity:"+activity);
        seed(h,activity,self,List.of(activity),false);
        for(int phase=0;phase<2;phase++){
            if(phase==1){h.visited.clear();h.components.clear();seed(h,activity,self,List.of(activity),false);}
            while(!h.queue.isEmpty()){
                if(System.nanoTime()>deadline){h.gaps.add("global_deadline");break;}
                if(h.visited.size()>4000){h.gaps.add("host_context_budget");break;}
                Job job=h.queue.remove();String id=CapabilityIndex.key(job.method);
                String context=id+"|"+job.args;if(!h.visited.add(context))continue;
                Summary summary;
                try{summary=flow.summary(job.method);}catch(RuntimeException ex){h.gaps.add("decode_failed:"+id+":"+ex.getClass().getSimpleName());continue;}
                if(summary.truncated())h.gaps.add("flow_budget:"+id);
                for(Write w:summary.writes()){
                    V receiver=eval(w.receiver(),job,h,0,new HashSet<>()),value=eval(w.value(),job,h,0,new HashSet<>());
                    String fieldKey=heapKey(w.field(),receiver);h.heap.put(fieldKey,union(h.heap.get(fieldKey),value));
                }
                for(Call call:summary.calls()){
                    Method target=idx.resolve(call.method());String owner=owner(call.method()),name=name(call.method());
                    List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
                    String kind=kind(call.method());
                    if(kind!=null){emit(h,job,call,args,kind,summary.branched());continue;}
                    if(target!=null&&idx.relevant.contains(CapabilityIndex.key(target))){
                        Method actual=target;
                        if(!call.isStatic()&&!args.isEmpty()&&!name.equals("<init>")){
                            for(V recv:alternatives(args.get(0)))if(recv.type()!=null){Method candidate=idx.resolve(desc(recv.type())+"->"+CapabilityIndex.shape(target));if(candidate!=null&&idx.relevant.contains(CapabilityIndex.key(candidate)))actual=candidate;}
                        }
                        enqueue(h,actual,args,job.path,job.candidate||summary.branched());
                    }
                    if(name.equals("<init>")&&!args.isEmpty()&&idx.component(owner)&&!idx.activity(owner))seed(h,owner,args.get(0),extend(job.path,id+"@"+call.offset()),true);
                }
            }
        }
        if(!h.facts.isEmpty()){
            Map<String,Object> report=new LinkedHashMap<>();report.put("activity",activity);report.put("declared",apk.activities.contains(activity));report.put("facts",h.facts.values());report.put("limitations",h.gaps.stream().distinct().sorted().toList());activities.add(report);
        }
        if(!h.gaps.isEmpty())diagnostics.add(activity+":"+String.join(",",h.gaps.stream().distinct().limit(20).toList()));
    }
    void seed(Host h,String type,V self,List<String> path,boolean candidate){
        if(path.size()>16){h.gaps.add("component_depth:"+type);return;}
        if(h.expanding.contains(type)||!h.components.add(type+"|"+self.id()))return;
        h.expanding.add(type);
        for(Method m:idx.hierarchyMethods(type)){
            if(!idx.relevant.contains(CapabilityIndex.key(m)))continue;
            List<V> args=new ArrayList<>();if((m.getAccessFlags()&8)==0)args.add(self);
            for(CharSequence p:m.getParameterTypes())args.add(V.of("unknown",CapabilityIndex.cls(p.toString()),"entry_parameter"));
            enqueue(h,m,args,path,candidate);
        }
        // Component-typed fields are lifecycle entry candidates, not generic class-reference closure.
        String current=type;Set<String> seen=new HashSet<>();
        while(current!=null&&seen.add(current)){
            ClassDef c=idx.classes.get(current);if(c==null)break;
            for(Field f:c.getFields()){
                String ft=CapabilityIndex.cls(f.getType());
                if(ft!=null&&idx.component(ft)&&!idx.activity(ft)&&!Cfg.WEBVIEWS.contains(ft)){
                    V fv=V.of("field_object",ft,self.id()+"."+CapabilityIndex.field(f));
                    seed(h,ft,fv,extend(path,CapabilityIndex.field(f)),true);
                }
            }
            current=CapabilityIndex.cls(c.getSuperclass());
            if(current!=null&&(current.startsWith("android.")||current.startsWith("androidx.")))break;
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
        if(depth>10)return V.of("unknown",v.type(),"resolve_depth");
        if(v.kind().equals("param")){int i=Integer.parseInt(v.id());return i<job.args.size()?job.args.get(i):V.of("unknown",v.type(),"parameter");}
        if(v.kind().equals("new"))return V.of("object",v.type(),v.id()+"|"+allocationContext(job));
        if(v.kind().equals("union")){V result=null;for(V x:v.args())result=union(result,eval(x,job,h,depth+1,new HashSet<>(visiting)));return result==null?UNKNOWN:result;}
        if(v.kind().equals("cast")){
            V original=eval(v.args().get(0),job,h,depth+1,visiting);
            return new V(original.kind(),v.type(),original.id(),original.literal(),original.args());
        }
        if(v.kind().equals("settings"))return expr("settings",v.type(),v.id(),List.of(eval(v.args().get(0),job,h,depth+1,visiting)));
        if(v.kind().equals("field")){
            V receiver=eval(v.args().get(0),job,h,depth+1,new HashSet<>(visiting));String hk=heapKey(v.id(),receiver);
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
            Method method=idx.resolve(v.id());V result=null;
            if(method!=null&&visiting.add(v.id())){
                Summary summary=flow.summary(method);Job nested=new Job(method,args,job.path,true);
                for(V ret:summary.returns())result=union(result,eval(ret,nested,h,depth+1,new HashSet<>(visiting)));
            }
            return result==null?new V("unknown",v.type(),v.id(),null,List.of()):result;
        }
        return v;
    }
    static String allocationContext(Job job){return job.args.isEmpty()?"static":job.args.get(0).id();}
    static String heapKey(String field,V receiver){return receiver.id()+"::"+field;}
    static String owner(String id){int sep=id.indexOf("->");return sep<0?null:CapabilityIndex.cls(id.substring(0,sep));}
    static String name(String id){int p=id.indexOf("->"),e=id.indexOf('(',p);return p<0?"":id.substring(p+2,e<0?id.length():e);}
    static String desc(String cls){return "L"+cls.replace('.','/')+";";}
    String kind(String method){Method m=idx.resolve(method);if(m!=null)return idx.kind(m);
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
            for(V obj:alternatives(args.get(1))){Map<String,Object> b=new LinkedHashMap<>(base);b.put("implementation",obj.type()==null?"unknown":obj.type());b.put("members",bridgeMembers(obj.type()));add(h,b);}return;
        }
        if(kind.equals("callback")&&args.size()>=2){
            for(V client:alternatives(args.get(1))){Map<String,Object>b=new LinkedHashMap<>(base);b.put("implementation",client.type()==null?"unknown":client.type());b.put("members",callbackMembers(client.type()));add(h,b);}return;
        }
        add(h,base);
    }
    void add(Host h,Map<String,Object> fact){
        String key=fact.get("site")+"|"+fact.get("webview")+"|"+fact.get("implementation")+"|"+fact.get("registration_name")+"|"+fact.get("values");
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
        if(type==null)return List.of();List<Map<String,Object>> result=new ArrayList<>();
        for(Method m:idx.hierarchyMethods(type))if(CALLBACKS.contains(m.getName())&&(m.getAccessFlags()&8)==0&&m.getImplementation()!=null){
            String owner=CapabilityIndex.cls(m.getDefiningClass());if(owner.startsWith("android.webkit.")||owner.equals("com.tencent.smtt.sdk.WebViewClient")||owner.equals("com.tencent.smtt.sdk.WebChromeClient"))continue;
            result.add(Map.of("signature",CapabilityIndex.key(m),"display",CapabilityIndex.display(m),"name",m.getName()));
        }return result;
    }
    Map<String,Object> report(String hash,String status,Map<String,Object> metrics){
        List<Map<String,Object>> unbound=new ArrayList<>();
        for(String id:idx.seeds){Method m=idx.methods.get(id);if(m==null)continue;Summary s=flow.cache.get(id);
            if(s==null){unbound.add(Map.of("method",id,"reason","not_expanded"));continue;}
            for(Call call:s.calls())if(kind(call.method())!=null&&!boundSites.contains(id+"@"+call.offset()))unbound.add(Map.of("site",id+"@"+call.offset(),"api",call.method(),"reason","no_activity_owner"));
        }
        Map<String,Object> out=new LinkedHashMap<>();out.put("schema_version",1);out.put("package",apk.packageName);out.put("version",apk.version);out.put("apk_sha256",hash);out.put("status",status);out.put("activities",activities);out.put("unattributed",unbound);out.put("diagnostics",diagnostics);out.put("index_diagnostics",idx.diagnostics);out.put("manifest_diagnostics",apk.errors);out.put("metrics",metrics);out.put("semantics","Static binding evidence; explicit does not prove runtime execution. Candidate bindings are retained. Settings are observed operations, not final runtime state.");return out;
    }
}
