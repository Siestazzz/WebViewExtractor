package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.iface.*;
import static org.example.DexFlow.*;

/** Demand-driven instantiation of method summaries for each Activity host. */
final class CapabilityEngine {
    final CapabilityIndex idx;
    final DexFlow flow;
    final AsyncRegistrations async;
    final ReflectionProtocols reflection;
    final ApkInventory apk;
    final long deadline;
    final List<Map<String,Object>> activities=new ArrayList<>();
    final Set<String> boundSites=new HashSet<>();
    final List<String> diagnostics=new ArrayList<>();
    final Map<String,V> constants=new HashMap<>();
    final Map<String,List<Map<String,Object>>> callbackCache=new HashMap<>();
    final Map<String,StaticReflection.Reach> staticReflectionReachable=new HashMap<>();
    long staticReflectionGraphQueries;
    final Map<String,Set<Integer>> lazyFactoryParameters=new HashMap<>();
    final Map<String,Boolean> objectFieldSetters=new HashMap<>(), receiverRelevance=new HashMap<>(), callbackCarrierTypes=new HashMap<>();
    Runnable checkpoint=()->{};
    Host currentHost;
    ApplicationBootstrap.State bootstrapState;
    ApplicationBootstrap.State applicationBootstrap(){if(bootstrapState==null){bootstrapState=ApplicationBootstrap.build(this);diagnostics.addAll(bootstrapState.diagnostics());}return bootstrapState;}
    int activeEvaluations, evaluationSteps, evaluationStepLimit=12000;
    static final Set<String> CALLBACKS=Set.of("onPageStarted","onPageFinished","onPageCommitVisible","onLoadResource","shouldOverrideUrlLoading","shouldInterceptRequest","onTooManyRedirects","onReceivedError","onReceivedHttpError","onFormResubmission","doUpdateVisitedHistory","onReceivedSslError","onReceivedClientCertRequest","onReceivedHttpAuthRequest","shouldOverrideKeyEvent","onUnhandledKeyEvent","onScaleChanged","onReceivedLoginRequest","onRenderProcessGone","onSafeBrowsingHit","onProgressChanged","onReceivedTitle","onReceivedIcon","onReceivedTouchIconUrl","onShowCustomView","onHideCustomView","onCreateWindow","onRequestFocus","onCloseWindow","onJsAlert","onJsConfirm","onJsPrompt","onJsBeforeUnload","onExceededDatabaseQuota","onReachedMaxAppCacheSize","onGeolocationPermissionsShowPrompt","onGeolocationPermissionsHidePrompt","onPermissionRequest","onPermissionRequestCanceled","onJsTimeout","onConsoleMessage","getDefaultVideoPoster","getVideoLoadingProgressView","getVisitedHistory","onShowFileChooser","openFileChooser");
    CapabilityEngine(CapabilityIndex idx,ApkInventory apk,long deadline){this(idx,apk,deadline,new DexFlow(idx,deadline));}
    CapabilityEngine(CapabilityIndex idx,ApkInventory apk,long deadline,DexFlow sharedFlow){this.idx=idx;this.apk=apk;this.deadline=deadline;flow=sharedFlow;async=new AsyncRegistrations(idx);reflection=new ReflectionProtocols(idx,flow,()->currentHost==null?deadline:Math.min(deadline,currentHost.localDeadline));}
    record Job(Method method,List<V> args,List<String> path,boolean candidate){}
    record DeferredField(String field,V receiver){}
    record XmlConsumer(Job job,Set<V> observed){}
    final class Host {
        final String activity;
        final FieldHeap heap=new FieldHeap();
        final Map<String,List<V>> lookupSources=new HashMap<>();
        final Map<String,DeferredField> deferredFields=new HashMap<>();
        final Map<String,XmlConsumer> xmlConsumers=new LinkedHashMap<>();
        final Map<String,V> fragmentViews=new HashMap<>();
        final Set<String> xmlReplays=new HashSet<>();
        final Map<String,Set<ApkInventory.LayoutNode>> layoutScopes=new HashMap<>();
        final Set<String> preparingLayouts=new HashSet<>();
        final Map<String,Set<String>> layoutChildren=new HashMap<>();
        final Map<String,Set<Map<String,Object>>> xmlBindings=new HashMap<>();
        final Map<String,Set<V>> contents=new HashMap<>();
        final Map<String,Map<String,V>> maps=new HashMap<>(), arrays=new HashMap<>();
        final Map<String,V> bridgeViews=new HashMap<>(), linkedClosures=new HashMap<>();
        final Map<String,Long> arrayLengths=new HashMap<>();
        final Map<String,Set<V>> fragmentTransactions=new HashMap<>();
        final Set<String> installedFragments=new HashSet<>(),fragmentReplays=new HashSet<>();
        final Map<String,List<Map<String,Object>>> nativeBindings=new HashMap<>();
        final ContextQueue queue=new ContextQueue();
        final Set<String> pending=new HashSet<>(),visited=new HashSet<>(),components=new HashSet<>(),expanding=new HashSet<>(),constructed=new HashSet<>(),materialized=new HashSet<>();
        final Map<String,Map<String,Object>> facts=new TreeMap<>();
        final Set<String> gaps=new LinkedHashSet<>();
        V activeClientContext,activeManifestEntry;
        long localDeadline=Long.MAX_VALUE;
        boolean applicationStartup;
        final Set<String> transportMaps=new HashSet<>(),allocationCaptures=new HashSet<>();
        Host(String a){activity=a;}
    }
    final Map<String,ActivityState> states=new LinkedHashMap<>();
    final class ActivityState {
        Host host;
        final String activity;
        int phase, jobs, slices, discardedContexts, provisionalFacts, terminalXmlConsumers, terminalDeferredFields;
        final int[] phaseJobs=new int[2];
        long nanos, checkpointNanos,bootstrapImportNanos;
        int bootstrapImportedEntries;
        boolean done, limited, initialPass;
        Map<String,Object> completedReport;
        Map<String,Map<String,Object>> previousFacts=new TreeMap<>();
        ActivityState(String activity){this.activity=activity;this.host=new Host(activity);}
    }
    ActivityState beginActivity(String activity){
        ActivityState existing=states.get(activity);if(existing!=null)return existing;
        ActivityState state=new ActivityState(activity);states.put(activity,state);
        ApplicationBootstrap.State snapshot=applicationBootstrap();long importStart=System.nanoTime();snapshot.install(state.host);state.bootstrapImportNanos=System.nanoTime()-importStart;state.bootstrapImportedEntries=snapshot.entryCount();
        long start=System.nanoTime();
        seed(state.host,activity,V.of("host",activity,"activity:"+activity),List.of(activity),false);
        state.nanos+=System.nanoTime()-start;return state;
    }
    // A task is atomic. Slice deadlines are cooperative, checked between method contexts.
    void advanceActivity(ActivityState state,long budgetNanos,int maxJobs){
        if(state.done||System.nanoTime()>=deadline)return;
        long start=System.nanoTime(),stop=Math.min(deadline,start+budgetNanos);int processed=0;long checkpointSpent=0;
        Host h=state.host;currentHost=h;state.slices++;
        try {
            while(System.nanoTime()<deadline){
                if(processed>=maxJobs||processed>0&&System.nanoTime()>=stop)break;
                boolean capped=h.visited.size()>12000;
                if(capped){h.gaps.add("host_context_budget");state.limited=true;state.discardedContexts+=h.queue.size();}
                if(capped||h.queue.isEmpty()&&!replayXmlConsumers(h)){
                    if(state.phase==1){finishActivity(state);break;}
                    state.previousFacts.putAll(h.facts);state.phase=1;
                    h.queue.clear();h.pending.clear();h.facts.clear();h.visited.clear();h.components.clear();h.materialized.clear();h.xmlConsumers.clear();h.xmlReplays.clear();
                    seed(h,state.activity,V.of("host",state.activity,"activity:"+state.activity),List.of(state.activity),false);
                    continue;
                }
                long checkpointStart=System.nanoTime();checkpoint.run();long checkpointTime=System.nanoTime()-checkpointStart;checkpointSpent+=checkpointTime;stop=Math.min(deadline,stop+checkpointTime);
                if(System.nanoTime()>=deadline)break;
                processJob(h);processed++;state.jobs++;state.phaseJobs[state.phase]++;
            }
            if(!state.done&&System.nanoTime()>=deadline)h.gaps.add("global_deadline");
        }finally{state.nanos+=System.nanoTime()-start-checkpointSpent;state.checkpointNanos+=checkpointSpent;currentHost=null;}
    }
    void finishActivity(ActivityState state){
        Host h=state.host;state.done=true;
        state.provisionalFacts=remainingProvisional(state).size();
        if(state.provisionalFacts>0)h.gaps.add("previous_phase_not_rederived:"+state.provisionalFacts);
        state.terminalXmlConsumers=h.xmlConsumers.size();state.terminalDeferredFields=h.deferredFields.size();
        state.completedReport=stateReport(state);
        if(!((List<?>)state.completedReport.get("facts")).isEmpty())activities.add(state.completedReport);
        if(!h.gaps.isEmpty()){
            Map<String,Long> limits=new TreeMap<>();
            for(String gap:h.gaps)if(gap.contains("budget")||gap.contains("deadline")||gap.startsWith("decode_failed:")||gap.equals("resolve_depth"))limits.merge(gap.split(":",2)[0],1L,Long::sum);
            if(!limits.isEmpty())diagnostics.add(state.activity+":analysis_limits:"+limits);
            diagnostics.add(state.activity+":"+String.join(",",h.gaps.stream().distinct().limit(20).toList()));
        }
        // Completed receivers no longer need their full heap/queues retained in memory.
        state.previousFacts.clear();state.host=null;
    }
    Map<String,Object> stateReport(ActivityState state){
        if(state.completedReport!=null)return state.completedReport;
        Host h=state.host;
        if(state.previousFacts.isEmpty())return hostReport(h);
        Map<String,Map<String,Object>> combined=new TreeMap<>();
        for(var entry:remainingProvisional(state).entrySet()){
            Map<String,Object> fact=new LinkedHashMap<>(entry.getValue());fact.put("binding_status","candidate");fact.put("analysis_stage","previous_phase_provisional");combined.put("previous:"+entry.getKey(),fact);
        }
        combined.putAll(h.facts);Map<String,Object> report=hostReport(h,new ArrayList<>(combined.values()));
        Map<String,Map<String,Object>> unresolved=remainingProvisional(state);
        List<Map<String,Object>> superseded=new ArrayList<>();
        for(var entry:state.previousFacts.entrySet())if(!unresolved.containsKey(entry.getKey())&&!h.facts.containsKey(entry.getKey()))superseded.add(Map.of("reason","same_receiver_and_argument_refinement","previous_fact",entry.getValue()));
        if(!superseded.isEmpty())report.put("superseded_provisional_facts",superseded);return report;
    }
    static List<Object> refinementKey(Map<String,Object> fact){
        Object view=fact.get("webview");Object id=view instanceof Map<?,?> m?m.get("id"):view;
        return Arrays.asList(fact.get("kind"),fact.get("site"),fact.get("api"),id,fact.get("registration_name"));
    }
    @SuppressWarnings("unchecked")
    boolean refinesFact(Host h,Map<String,Object> old,Map<String,Object> fresh){
        Map<String,Object> before=(Map<String,Object>)old.get("webview"),after=(Map<String,Object>)fresh.get("webview");
        if(before==null||after==null||!Objects.equals(before.get("id"),after.get("id")))return false;
        String oldType=String.valueOf(before.get("type")),newType=String.valueOf(after.get("type"));
        if(!oldType.equals(newType)&&!idx.subtype(newType,oldType))return false;
        if(!Objects.equals(old.get("values"),fresh.get("values")))return false;
        List<V> oldArgs=(List<V>)old.getOrDefault("arguments",List.of()),newArgs=(List<V>)fresh.getOrDefault("arguments",List.of());
        if(oldArgs.size()!=newArgs.size())return false;
        Object oldImpl=old.get("implementation"),newImpl=fresh.get("implementation");
        boolean implementationProven=Objects.equals(oldImpl,newImpl);
        for(int i=0;i<oldArgs.size();i++){
            V x=oldArgs.get(i),y=newArgs.get(i);
            boolean concreteTarget=i>0&&Set.of("object","new","view","host").contains(y.kind())&&Objects.equals(y.type(),newImpl);
            if(x.equals(y)){if(concreteTarget)implementationProven=true;continue;}
            boolean directReceiver=i==0&&Set.of("bridge","callback","webview_operation").contains(old.get("kind"));
            if(directReceiver&&x.kind().equals(y.kind())&&Set.of("object","new","view","host").contains(x.kind())&&x.id().equals(y.id())&&x.type()!=null&&y.type()!=null&&idx.subtype(y.type(),x.type()))continue;
            List<V> source=h.lookupSources.get(x.id());
            if(!x.kind().equals("unknown")||source==null||source.size()!=2||mapKey(source.get(1))==null)return false;
            Map<String,V> entries=h.maps.getOrDefault(source.get(0).id(),Map.of());
            V resolved=entries.get(mapKey(source.get(1)));
            if(entries.containsKey("*")||resolved==null||alternatives(resolved).stream().noneMatch(v->v.id().equals(y.id())&&Objects.equals(v.type(),y.type())))return false;
            if(concreteTarget)implementationProven=true;
        }
        if(!Objects.equals(oldImpl,newImpl)&&!("unknown".equals(oldImpl)&&newImpl!=null&&!newImpl.equals("unknown")&&implementationProven))return false;
        Set<String> members=new HashSet<>();for(var member:(List<Map<String,Object>>)fresh.getOrDefault("members",List.of()))members.add((String)member.get("signature"));
        for(var member:(List<Map<String,Object>>)old.getOrDefault("members",List.of()))if(!members.contains(member.get("signature")))return false;
        return true;
    }
    Map<String,Map<String,Object>> remainingProvisional(ActivityState state){
        Map<List<Object>,List<Map<String,Object>>> candidates=new HashMap<>();
        for(var fact:state.host.facts.values())candidates.computeIfAbsent(refinementKey(fact),k->new ArrayList<>()).add(fact);
        Map<String,Map<String,Object>> result=new TreeMap<>();
        for(var entry:state.previousFacts.entrySet()){
            if(state.host.facts.containsKey(entry.getKey())&&refinesFact(state.host,entry.getValue(),state.host.facts.get(entry.getKey())))continue;
            Map<String,Object> fact=entry.getValue();
            if(candidates.getOrDefault(refinementKey(fact),List.of()).stream().anyMatch(f->refinesFact(state.host,fact,f)))continue;
            result.put(entry.getKey(),fact);
        }
        return result;
    }
    List<Map<String,Object>> coverage(List<String> roots){
        List<Map<String,Object>> out=new ArrayList<>();
        for(String activity:roots){ActivityState s=states.get(activity);Map<String,Object> row=new LinkedHashMap<>();row.put("activity",activity);
            row.put("status",s==null?"not_started":s.done?(s.limited?"budget_exhausted":"traversal_finished"):System.nanoTime()>=deadline?"deadline_interrupted":s.initialPass?"pending_deep_analysis":"initial_analysis");
            row.put("phase",s==null?0:s.phase+1);row.put("contexts_processed",s==null?0:s.jobs);row.put("pending_contexts",s==null||s.host==null?0:s.host.queue.size());row.put("analysis_seconds",s==null?0:s.nanos/1e9);row.put("slices",s==null?0:s.slices);
            row.put("pending_priority_contexts",s==null||s.host==null?0:s.host.queue.prioritySize());
            row.put("pending_ordinary_contexts",s==null||s.host==null?0:s.host.queue.ordinarySize());
            row.put("application_bootstrap_import_seconds",s==null?0:s.bootstrapImportNanos/1e9);row.put("application_bootstrap_import_entries",s==null?0:s.bootstrapImportedEntries);
            row.put("pending_method_sample",s==null||s.host==null?List.of():s.host.queue.diagnostics(12));
            row.put("discarded_contexts",s==null?0:s.discardedContexts);row.put("phase_contexts",s==null?List.of(0,0):List.of(s.phaseJobs[0],s.phaseJobs[1]));
            row.put("tracked_xml_consumers",s==null?0:s.host==null?s.terminalXmlConsumers:s.host.xmlConsumers.size());
            row.put("tracked_deferred_fields",s==null?0:s.host==null?s.terminalDeferredFields:s.host.deferredFields.size());
            row.put("provisional_facts",s==null?0:s.host==null?s.provisionalFacts:remainingProvisional(s).size());
            row.put("checkpoint_seconds",s==null?0:s.checkpointNanos/1e9);out.add(row);
        }return out;
    }
    void analyzeActivity(String activity){
        ActivityState state=beginActivity(activity);
        while(!state.done&&System.nanoTime()<deadline)advanceActivity(state,Long.MAX_VALUE/4,Integer.MAX_VALUE);
        if(!state.done&&!((List<?>)stateReport(state).get("facts")).isEmpty())activities.add(stateReport(state));
    }
    void processJob(Host h){
                Job job=h.queue.remove();String id=CapabilityIndex.key(job.method);
                String context=id+"|"+job.args;h.pending.remove(context);if(!h.visited.add(context))return;
                h.activeClientContext=clientContext(job.args);h.activeManifestEntry=ManifestProtocols.entry(job.args);
                try{
                if(splitManifestEntries(h,job))return;
                Summary summary;
                try{summary=flow.summary(job.method,v->guardValue(v,job,h,0));}catch(RuntimeException ex){h.gaps.add("decode_failed:"+id+":"+ex.getClass().getSimpleName());return;}
                if(summary.truncated())h.gaps.add("flow_budget:"+id);
                prepareLayouts(summary,job,h,0,new HashSet<>());
                for(Write w:summary.writes()){
                    V receiver=eval(w.receiver(),job,h,0,new HashSet<>()),value=eval(w.value(),job,h,0,new HashSet<>());
                    applyWrite(h,w.field(),receiver,value);
                }
                for(Call call:summary.calls()){
                    Method target=call.isSuper()?resolveSuper(job.method,call.method()):idx.resolve(call.method());String owner=owner(call.method()),name=name(call.method());
                    String kind=kind(call.method());
                    if(call.method().equals(StaticReflection.INVOKE)){
                        List<V> actual=call.args().stream().map(argument->eval(argument,job,h,0,new HashSet<>())).toList();
                        boolean knownStatic=!actual.isEmpty()&&alternatives(actual.get(0)).stream().anyMatch(value->value.kind().equals("reflect_static_method"));
                        if(knownStatic||h.activeClientContext==null){StaticReflection.resolve(new V("return",null,call.method(),null,call.args()),actual,this,job,h);continue;}
                    }
                    if(h.activeManifestEntry!=null&&(call.method().equals(ManifestProtocols.FOR_NAME)||call.method().equals(ManifestProtocols.NEW_INSTANCE))){
                        eval(new V("return_manifest_native:"+call.offset(),call.method().equals(ManifestProtocols.FOR_NAME)?"java.lang.Class":"java.lang.Object",call.method(),null,call.args()),job,h,0,new HashSet<>());continue;
                    }
                    if(h.activeClientContext!=null&&call.method().equals(ReflectionProtocols.INVOKE)){
                        reflectInstalledTransport(h,job,call);continue;
                    }
                    if(h.activeClientContext!=null&&!call.isStatic()&&!call.isSuper()&&!call.isDirect()&&(clientCallbackReference(call.method())||sameCallbackForward(job.method,call.method()))){
                        followClientDelegate(h,job,call);continue;
                    }
                    if(FragmentTransactions.operation(call.method())){
                        fragmentTransaction(h,job,call);continue;
                    }
                    if(modeledFragmentAccess(h,job,call))continue;
                    if(CapabilityIndex.pagerInstall(call.method())){
                        installFragmentAdapter(h,job,call);continue;
                    }
                    if(CapabilityIndex.fragmentFactory(call.method())){
                        eval(expr("return_fragment_factory:"+call.offset(),owner(call.method()),call.method(),call.args()),job,h,0,new HashSet<>());continue;
                    }
                    List<AsyncRegistrations.Entry> registrations=async.entries(call.method());
                    boolean lifecycle=name.equals("<init>")&&(idx.component(owner)||idx.activity(owner)||idx.scheduled(owner)||idx.callbackEntries.containsKey(owner)||idx.bindingObjects.contains(owner)||idx.classValueCarriers.contains(owner)||h.applicationStartup&&idx.subtype(owner,"android.app.Application"));
                    boolean fieldSetter=target!=null&&objectFieldSetter(target);
                    boolean registryWrite=target!=null&&(reflection.writer(target)||idx.keyedRegistryWrites.contains(CapabilityIndex.key(target))||target.getImplementation()==null&&idx.byShape.getOrDefault(CapabilityIndex.shape(target),List.of()).stream().anyMatch(m->idx.keyedRegistryWrites.contains(CapabilityIndex.key(m))));
                    boolean relevant=target!=null&&(idx.relevant.contains(CapabilityIndex.key(target))||h.activeClientContext!=null&&(idx.clientDelegationReachable(target)||reflection.reachable(target))||target.getImplementation()==null&&idx.byShape.getOrDefault(CapabilityIndex.shape(target),List.of()).stream().anyMatch(m->idx.relevant.contains(CapabilityIndex.key(m)))||StaticReflection.reachable(target,this,h));
                    boolean receiverRelevant=false;
                    if(kind==null&&!lifecycle&&!relevant&&!registryWrite&&!fieldSetter&&target!=null&&!call.isStatic()&&!call.args().isEmpty()){
                        V hint=dispatchHint(call.args().get(0),job,h,0);
                        for(V receiver:alternatives(hint)){
                            Method concrete=call.isDirect()||call.isSuper()?target:receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+CapabilityIndex.shape(target));
                            if(concrete!=null&&relevantOnReceiver(concrete,receiver,h)){receiverRelevant=true;break;}
                        }
                    }
                    boolean argumentRelevant=false;
                    if(kind==null&&!lifecycle&&!relevant&&!registryWrite&&!fieldSetter&&!receiverRelevant&&target!=null&&
                        call.args().stream().map(v->dispatchHint(v,job,h,0)).flatMap(v->alternatives(v).stream()).anyMatch(this::callbackCarrier)){
                        List<V> bound=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
                        Method concrete=target;
                        if(!call.isStatic()&&!call.isDirect()&&!call.isSuper()&&!bound.isEmpty()&&bound.get(0).type()!=null)
                            concrete=idx.resolve(desc(bound.get(0).type())+"->"+CapabilityIndex.shape(target));
                        if(concrete!=null)argumentRelevant=relevantOnArguments(concrete,bound,job,h,new HashSet<>(),0);
                    }
                    boolean proceedArguments=idx.closureProceedArguments(call.method());
                    boolean closureCall=idx.closureLink(call.method())||idx.closureProceed(call.method())||proceedArguments;
                    boolean carriesJoinPoint=false;
                    if(target!=null&&target.getParameterTypes().stream().anyMatch(t->idx.joinPoint(CapabilityIndex.cls(t.toString()))))
                        for(V arg:call.args())if(alternatives(eval(arg,job,h,0,new HashSet<>())).stream().anyMatch(x->x.kind().equals("aspectj_joinpoint")||h.linkedClosures.containsKey(x.id()))){carriesJoinPoint=true;break;}
                    if(kind==null&&!lifecycle&&!relevant&&!registryWrite&&!fieldSetter&&!receiverRelevant&&!argumentRelevant&&!closureCall&&!carriesJoinPoint&&registrations.isEmpty())continue;
                    List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
                    observeXmlConsumer(h,job,args);
                    if(!registrations.isEmpty())dispatchRegistered(h,job,call,args,registrations);
                    if(kind!=null){dispatchCapability(h,job,call,args,kind,summary.branched());continue;}
                    if(idx.closureProceed(call.method())&&!args.isEmpty()){
                        boolean linked=false;
                        for(V point:alternatives(linkedJoinPoint(h,args.get(0))))if(point.kind().equals("aspectj_joinpoint")){
                            V closure=point.args().get(0),state=point.args().get(1);
                            Method run=idx.resolve(desc(closure.type())+"->run([Ljava/lang/Object;)Ljava/lang/Object;");
                            if(run!=null&&idx.relevant.contains(CapabilityIndex.key(run))){enqueue(h,run,List.of(closure,state),extend(job.path,"aspectj_proceed:"+call.method()),true);linked=true;}
                        }
                        if(!linked)h.gaps.add("aspectj_unresolved_proceed:"+call.method());
                        continue;
                    }
                    if(idx.closureLink(call.method())){linkClosure(h,job,args,CapabilityIndex.cls(target.getReturnType()),0,new HashSet<>());continue;}
                    if(proceedArguments){h.gaps.add("aspectj_proceed_arguments_not_modeled:"+call.method());continue;}
                    if(target!=null){
                        boolean virtual=!call.isStatic()&&!call.isSuper()&&!call.isDirect()&&!args.isEmpty();
                        if(!virtual){
                            if(relevant||lifecycle||registryWrite||fieldSetter||receiverRelevant||argumentRelevant||carriesJoinPoint)enqueue(h,target,args,job.path,job.candidate||summary.branched());
                        }else{
                            int dispatched=0;
                            for(V receiver:alternatives(args.get(0))){
                                if(receiver.kind().equals("literal")&&"0".equals(receiver.literal()))continue;
                                Method concrete=receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+CapabilityIndex.shape(target));
                                if(concrete==null||concrete.getImplementation()==null){h.gaps.add("unresolved_receiver_dispatch:"+call.method());continue;}
                                if(!idx.relevant.contains(CapabilityIndex.key(concrete))&&!(h.activeClientContext!=null&&(idx.clientDelegationReachable(concrete)||reflection.reachable(concrete)))&&!reflection.writer(concrete)&&!idx.keyedRegistryWrites.contains(CapabilityIndex.key(concrete))&&!objectFieldSetter(concrete)&&!relevantOnReceiver(concrete,receiver,h)&&!argumentRelevant&&!carriesJoinPoint)continue;
                                if(dispatched++>=64){h.gaps.add("dispatch_budget:"+call.method());break;}
                                enqueue(h,concrete,specializeReceiver(args,receiver),job.path,job.candidate||summary.branched()||args.get(0).kind().equals("union"));
                            }
                        }
                    }

                    if(name.equals("<init>")&&!args.isEmpty()&&idx.component(owner)&&!idx.activity(owner)){h.constructed.add(args.get(0).id());seed(h,owner,args.get(0),extend(job.path,id+"@"+call.offset()),true);}
                }
                }finally{h.activeClientContext=null;h.activeManifestEntry=null;}
    }
    V clientContext(List<V> args){return !args.isEmpty()&&args.get(args.size()-1).kind().equals("installed_client_context")?args.get(args.size()-1):null;}
    List<V> reflectionArray(V expression,Job job,Host h){
        V array=eval(expression,job,h,0,new HashSet<>());Long length=h.arrayLengths.get(array.id());
        if(length==null||length<0||length>256){h.gaps.add("transport_reflection_array_unresolved");return null;}
        List<V> values=new ArrayList<>();Map<String,V> elements=h.arrays.getOrDefault(array.id(),Map.of());
        for(int i=0;i<length;i++){
            V value=elements.get(String.valueOf(i));if(value==null||elements.containsKey("*")){h.gaps.add("transport_reflection_array_partial");return null;}values.add(value);
        }return values;
    }
    void reflectInstalledTransport(Host h,Job job,Call invoke){
        boolean certified=false;
        for(var plan:reflection.plans(job.method))if(plan.offset()==invoke.offset()){
            certified=true;List<V> parameterClasses=reflectionArray(plan.parameterArray(),job,h),arguments=reflectionArray(plan.invokeArray(),job,h);
            if(parameterClasses==null||arguments==null)continue;
            List<String> parameters=new ArrayList<>();boolean exact=true;
            for(V clazz:parameterClasses){if(!clazz.kind().equals("class")||clazz.id()==null||!(clazz.id().startsWith("L")&&clazz.id().endsWith(";")||clazz.id().startsWith("[")||Set.of("Z","B","C","S","I","J","F","D").contains(clazz.id()))){exact=false;break;}parameters.add(clazz.id());}
            if(!exact){h.gaps.add("transport_reflection_parameter_vector_unknown");continue;}
            V annotation=eval(plan.annotationClass(),job,h,0,new HashSet<>()),requested=eval(plan.name(),job,h,0,new HashSet<>());
            if(!annotation.kind().equals("class")||annotation.type()==null){h.gaps.add("transport_annotation_class_unresolved");continue;}
            V lookup=TransportProtocols.unwrap(plan.lookupClass()),lookupReceiver=null;
            if(lookup.kind().startsWith("return")&&lookup.id().endsWith("->getClass()Ljava/lang/Class;")&&lookup.args().size()==1)lookupReceiver=eval(lookup.args().get(0),job,h,0,new HashSet<>());
            else if(!lookup.kind().equals("class")){h.gaps.add("transport_reflection_class_unresolved");continue;}
            V targets=eval(plan.invokeReceiver(),job,h,0,new HashSet<>());
            boolean accessible=false;for(V flag:plan.accessibleFlags())if(Long.valueOf(1).equals(number(eval(flag,job,h,0,new HashSet<>()))))accessible=true;
            for(V target:alternatives(targets)){
                if(!Set.of("object","new","view").contains(target.kind())||target.type()==null){h.gaps.add("transport_reflection_target_unresolved");continue;}
                String selectedType=lookup.kind().equals("class")?lookup.type():null;
                if(lookupReceiver!=null)for(V receiver:alternatives(lookupReceiver))if(receiver.id().equals(target.id()))selectedType=receiver.type();
                if(selectedType==null){h.gaps.add("transport_reflection_receiver_mismatch");continue;}
                List<V> provenance=target.args().stream().filter(v->v.kind().equals("transport_map_entry")&&v.args().size()==3&&v.args().get(2).equals(h.activeClientContext)).toList();
                if(provenance.isEmpty()){h.gaps.add("transport_registry_receiver_unbound");continue;}
                List<Method> candidates=plan.declared()?idx.byClass.getOrDefault(selectedType,List.of()):idx.hierarchyMethods(selectedType);
                boolean matched=false;
                for(Method selected:candidates){
                    if(selected.getName().startsWith("<")||!selected.getParameterTypes().stream().map(Object::toString).toList().equals(parameters)||requested.literal()!=null&&!requested.literal().equals(selected.getName()))continue;
                    if(!plan.declared()&&(selected.getAccessFlags()&1)==0||plan.declared()&&(selected.getAccessFlags()&1)==0&&!accessible)continue;
                    if(selected.getAnnotations().stream().noneMatch(a->a.getType().equals(desc(annotation.type()))&&a.getVisibility()==org.jf.dexlib2.AnnotationVisibility.RUNTIME))continue;
                    if(arguments.size()!=parameters.size())continue;
                    boolean compatible=true;for(int i=0;i<arguments.size();i++)if(!reflectionArgument(arguments.get(i),parameters.get(i))){compatible=false;break;}
                    if(!compatible){h.gaps.add("transport_reflection_argument_incompatible");continue;}
                    Method actual=(selected.getAccessFlags()&8)!=0?selected:idx.resolve(desc(target.type())+"->"+CapabilityIndex.shape(selected));
                    if(actual==null||actual.getImplementation()==null)continue;
                    matched=true;V webview=h.activeClientContext.args().get(0);int first=(job.method.getAccessFlags()&8)==0?1:0;
                    for(int i=0;i<job.method.getParameterTypes().size();i++)if(idx.webview(CapabilityIndex.cls(job.method.getParameterTypes().get(i).toString()))&&i+first<job.args.size())webview=job.args.get(i+first);
                    for(V marker:provenance){
                        Map<String,Object> fact=new LinkedHashMap<>();fact.put("activity",h.activity);fact.put("kind","message_bridge");fact.put("name",selected.getName());fact.put("site",CapabilityIndex.key(job.method)+"@"+invoke.offset()+":"+CapabilityIndex.key(selected));fact.put("api",invoke.method());
                        fact.put("webview",Map.of("id",webview.id(),"type",webview.type()==null?"unknown":webview.type()));fact.put("binding_status","candidate");fact.put("conditional",true);fact.put("implementation",target.type());fact.put("registration_name",marker.args().get(1).literal()==null?"unknown":marker.args().get(1).literal());
                        fact.put("members",List.of(Map.of("signature",CapabilityIndex.key(actual),"display",CapabilityIndex.display(actual),"name",actual.getName(),"selected_method",CapabilityIndex.key(selected))));
                        fact.put("resolution","installed_client_same_map_exact_reflection");fact.put("registry_object_id",marker.args().get(0).id());fact.put("reflective_target_object_id",target.id());fact.put("installed_client_object_id",h.activeClientContext.args().get(1).id());fact.put("annotation_gate",annotation.type());fact.put("evidence",extend(job.path,"reflective_transport:"+invoke.method()));fact.put("arguments",arguments);add(h,fact);
                    }
                    if(idx.relevant.contains(CapabilityIndex.key(actual))){List<V> bound=new ArrayList<>();if((actual.getAccessFlags()&8)==0)bound.add(target);bound.addAll(arguments);enqueue(h,actual,bound,extend(job.path,"reflective_endpoint:"+CapabilityIndex.key(actual)),true);}
                }
                if(!matched)h.gaps.add("transport_registered_no_compatible_endpoint:"+target.id());
            }
        }
        if(!certified)h.gaps.add("transport_reflection_gate_unproven:"+CapabilityIndex.key(job.method));
    }
    boolean reflectionArgument(V value,String descriptor){
        if(value.kind().equals("union"))return alternatives(value).stream().allMatch(v->reflectionArgument(v,descriptor));
        if(descriptor.startsWith("L")||descriptor.startsWith("["))return value.kind().equals("literal")&&"0".equals(value.literal())||value.type()!=null&&(desc(value.type()).equals(descriptor)||descriptor.equals("Ljava/lang/Object;")||idx.subtype(value.type(),CapabilityIndex.cls(descriptor)));
        return false;
    }
    boolean clientCallbackReference(String id){
        Method actual=idx.resolve(id);if(actual!=null)return idx.standardClientReference(actual);
        String type=owner(id),shape=id.substring(id.indexOf("->")+2);
        return idx.standardClientShape(type,shape)||(idx.subtype(type,"android.webkit.DownloadListener")||idx.subtype(type,"com.tencent.smtt.sdk.DownloadListener"))&&shape.equals("onDownloadStart(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V");
    }
    boolean sameCallbackForward(Method source,String target){
        return idx.standardClientReference(source)&&CapabilityIndex.shape(source).equals(target.substring(target.indexOf("->")+2));
    }
    void followClientDelegate(Host h,Job job,Call call){
        V binding=h.activeClientContext;List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
        if(args.isEmpty())return;
        String shape=call.method().substring(call.method().indexOf("->")+2);
        for(V receiver:alternatives(args.get(0))){
            if(receiver.kind().equals("literal")&&"0".equals(receiver.literal()))continue;
            Method actual=receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+shape);
            boolean crossContract=sameCallbackForward(job.method,call.method());
            if(!Set.of("object","new","view").contains(receiver.kind())||actual==null||actual.getImplementation()==null||!idx.standardClientCallback(receiver.type(),actual)&&!crossContract){
                h.gaps.add("client_delegate_unresolved:"+call.method());continue;
            }
            List<V> bound=specializeReceiver(args,receiver);V webview=binding.args().get(0);
            // A WebView argument is an actual call argument, not interchangeable with
            // the wrapper's installed receiver. Keep that identity when supplied.
            for(int i=0;i<actual.getParameterTypes().size();i++)if(idx.webview(CapabilityIndex.cls(actual.getParameterTypes().get(i).toString()))&&i+1<bound.size())webview=bound.get(i+1);
            List<String> path=extend(job.path,"client_delegate:"+call.method()+"@"+call.offset());
            if(!receiver.id().equals(binding.args().get(1).id())){
                Map<String,Object> fact=new LinkedHashMap<>();fact.put("activity",h.activity);fact.put("kind","callback");fact.put("name",name(binding.type()));
                fact.put("api",binding.type());fact.put("site",CapabilityIndex.key(job.method)+"@"+call.offset());fact.put("implementation",receiver.type());
                fact.put("members",delegatedCallbackMembers(receiver.type(),actual,crossContract));
                fact.put("webview",Map.of("id",webview.id(),"type",webview.type()==null?"unknown":webview.type()));fact.put("binding_status","candidate");fact.put("conditional",true);
                fact.put("resolution","installed_client_actual_callback_delegation");fact.put("delegate_object_id",receiver.id());fact.put("installed_client_object_id",binding.args().get(1).id());fact.put("evidence",path);fact.put("arguments",args);add(h,fact);
            }
            enqueue(h,actual,bound,path,true);
        }
    }
    List<Map<String,Object>> delegatedCallbackMembers(String type,Method first,boolean crossContract){
        List<Map<String,Object>> members=new ArrayList<>();Set<String> seen=new HashSet<>();ArrayDeque<Method> methods=new ArrayDeque<>();methods.add(first);
        while(!methods.isEmpty()&&seen.size()<32){
            Method method=methods.remove();String id=CapabilityIndex.key(method);
            if(!seen.add(id)||method.getImplementation()==null||!idx.standardClientCallback(type,method)&&!(crossContract&&CapabilityIndex.shape(method).equals(CapabilityIndex.shape(first)))||CapabilityIndex.cls(method.getDefiningClass()).startsWith("android.webkit.")||Set.of("com.tencent.smtt.sdk.WebViewClient","com.tencent.smtt.sdk.WebChromeClient").contains(CapabilityIndex.cls(method.getDefiningClass())))continue;
            members.add(Map.of("signature",id,"display",CapabilityIndex.display(method),"name",method.getName()));
            for(Call call:flow.summary(method).calls())if(call.isSuper()&&call.method().endsWith("->"+CapabilityIndex.shape(method))){Method parent=resolveSuper(method,call.method());if(parent!=null)methods.add(parent);}
        }
        return members;
    }
    void activateFragment(Host h,V value,List<String> path){
        if(!value.kind().equals("object")||!fragment(value.type())){h.gaps.add("installed_fragment_receiver_unresolved");return;}
        h.installedFragments.add(value.id());seed(h,value.type(),value,path,true);
    }
    // These exact framework accessors already have an identity-preserving return model.
    // Application overrides and unresolved receivers must retain ordinary body traversal.
    boolean modeledFragmentAccess(Host h,Job job,Call call){
        if(!FragmentTransactions.manager(call.method())&&!FragmentTransactions.begin(call.method())&&!FragmentTransactions.fluent(call.method()))return false;
        List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
        if(args.isEmpty()||!fragmentProtocolReceiver(args.get(0),owner(call.method()))||!frameworkFragmentAccess(call.method(),args,call.isSuper()))return false;
        V value=eval(new V(FragmentTransactions.begin(call.method())?(call.isSuper()?"return_fragment_transaction_super:":"return_fragment_transaction:")+call.offset():call.isSuper()?"return_super":"return",CapabilityIndex.cls(call.method().substring(call.method().indexOf(')')+1)),call.method(),null,call.args()),job,h,0,new HashSet<>());
        return FragmentTransactions.manager(call.method())?value.kind().equals("fragment_manager"):value.kind().equals("fragment_transaction");
    }
    void fragmentTransaction(Host h,Job job,Call call){
        List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
        if(args.isEmpty()||!frameworkFragmentAccess(call.method(),args,call.isSuper()))return;
        for(V tx:alternatives(args.get(0))){
            if(!tx.kind().equals("fragment_transaction")){h.gaps.add("fragment_transaction_receiver_unresolved");continue;}
            if(FragmentTransactions.commit(call.method())){
                for(V value:h.fragmentTransactions.getOrDefault(tx.id(),Set.of()))activateFragment(h,value,extend(job.path,"installed_fragment_transaction:"+call.method()+"@"+call.offset()));
            }else for(int i=1;i<args.size();i++)for(V value:alternatives(args.get(i)))if(fragment(value.type())&&value.kind().equals("object")){
                Set<V> pending=h.fragmentTransactions.computeIfAbsent(tx.id(),k->new LinkedHashSet<>());
                if(FragmentTransactions.remove(call.method()))pending.remove(value);else if(pending.size()<256)pending.add(value);else h.gaps.add("fragment_transaction_instance_budget");
            }
        }
    }
    void installFragmentAdapter(Host h,Job job,Call call){
        List<V> args=call.args().stream().map(v->eval(v,job,h,0,new HashSet<>())).toList();
        if(args.size()!=2){h.gaps.add("fragment_adapter_arguments");return;}
        for(V pager:alternatives(args.get(0)))for(V adapter:alternatives(args.get(1))){
            if(!Set.of("object","new","view").contains(pager.kind())||!idx.subtype(pager.type(),"androidx.viewpager2.widget.ViewPager2")){
                h.gaps.add("fragment_adapter_pager_unresolved");continue;
            }
            if(adapter.kind().equals("literal")&&"0".equals(adapter.literal()))continue;
            if(!Set.of("object","new").contains(adapter.kind())||!idx.subtype(adapter.type(),"androidx.viewpager2.adapter.FragmentStateAdapter")){
                h.gaps.add("fragment_adapter_unresolved");continue;
            }
            String signature=desc(adapter.type())+"->createFragment(I)Landroidx/fragment/app/Fragment;";
            Method creator=idx.resolve(signature);
            if(creator==null||creator.getImplementation()==null){h.gaps.add("fragment_adapter_factory_unresolved:"+adapter.type());continue;}
            List<V> bound=List.of(adapter,V.of("unknown","number","fragment_requested_position:"+adapter.id()));
            List<String> path=extend(job.path,"installed_fragment_adapter:"+call.method()+"@"+call.offset()+":"+pager.id());
            enqueue(h,creator,bound,path,true);
            Job installed=new Job(job.method,job.args,path,true);
            V result=eval(expr("return", "androidx.fragment.app.Fragment",signature,bound),installed,h,0,new HashSet<>());
            for(V created:alternatives(result))if(created.kind().equals("object")&&fragment(created.type()))
                activateFragment(h,created,extend(path,"adapter_fragment_result:"+signature));
        }
    }
    void dispatchRegistered(Host h,Job job,Call call,List<V> args,List<AsyncRegistrations.Entry> registrations){
        for(var registration:registrations){
            if(registration.argument()>=args.size()){h.gaps.add("async_registration_arguments:"+call.method());continue;}
            for(V callback:alternatives(args.get(registration.argument()))){
                if(callback.kind().equals("literal")&&"0".equals(callback.literal()))continue;
                if(!Set.of("object","new","view","host").contains(callback.kind())||callback.type()==null||!idx.subtype(callback.type(),registration.contract())){
                    h.gaps.add("async_unresolved_callback:"+call.method());continue;
                }
                for(String shape:registration.members()){
                    Method member=idx.resolve(desc(callback.type())+"->"+shape);
                    if(member==null||member.getImplementation()==null){h.gaps.add("async_unresolved_member:"+callback.type()+"->"+shape);continue;}
                    if(!idx.relevant.contains(CapabilityIndex.key(member)))continue;
                    List<V> bound=new ArrayList<>();bound.add(callback);
                    for(CharSequence parameter:member.getParameterTypes())bound.add(V.of("unknown",CapabilityIndex.cls(parameter.toString()),"async_callback_parameter"));
                    enqueue(h,member,bound,extend(job.path,"conditional_async_registration:"+CapabilityIndex.key(job.method)+"@"+call.offset()+":"+call.method()),true);
                }
            }
        }
    }
    void observeXmlConsumer(Host h,Job job,List<V> args){
        Set<V> views=new LinkedHashSet<>();
        for(V arg:args)for(V choice:alternatives(arg)){
            V value=choice.kind().equals("settings")&&!choice.args().isEmpty()?choice.args().get(0):choice;
            for(V view:alternatives(value))if(view.kind().equals("view")||view.kind().equals("field_object")&&idx.webview(view.type())&&h.deferredFields.containsKey(view.id()))views.add(view);
        }
        if(views.isEmpty())return;
        String context=CapabilityIndex.key(job.method)+"|"+job.args;
        XmlConsumer previous=h.xmlConsumers.get(context);
        if(previous!=null)views.addAll(previous.observed());
        h.xmlConsumers.put(context,new XmlConsumer(job,views));
    }
    boolean replayXmlConsumers(Host h){
        if(System.nanoTime()>deadline)return false;
        // Layout discovery can occur after a consumer was visited. Replay only actual
        // consuming contexts whose own receiver ID gained a concrete compatible type.
        for(var entry:h.xmlConsumers.entrySet()){
            XmlConsumer consumer=entry.getValue();
            for(V observed:consumer.observed()){
                V current=refreshBinding(observed,h,0,new HashSet<>());
                if(current.equals(observed))continue;
                String revision=entry.getKey()+"|"+observed+"=>"+current;
                if(!h.xmlReplays.add(revision))continue;
                if(h.xmlReplays.size()>12000){h.gaps.add("xml_consumer_replay_budget");return !h.queue.isEmpty();}
                h.visited.remove(entry.getKey());
                Job job=consumer.job();enqueue(h,job.method,job.args,job.path,true);
            }
        }
        return !h.queue.isEmpty();
    }
    Method resolveSuper(Method caller,String reference){
        Method declared=idx.resolve(reference);ClassDef referenced=idx.classes.get(owner(reference));
        // DEX invoke-super with a class reference selects the closest superclass
        // implementation of the invoking class, even if method_id names an ancestor.
        // Interface default calls keep the referenced interface contract.
        if(referenced!=null&&(referenced.getAccessFlags()&0x200)!=0)return declared;
        ClassDef invoking=idx.classes.get(CapabilityIndex.cls(caller.getDefiningClass()));
        if(invoking==null||invoking.getSuperclass()==null)return declared;
        String parent=CapabilityIndex.cls(invoking.getSuperclass());
        if(!idx.subtype(parent,owner(reference)))return declared;
        Method actual=idx.resolve(invoking.getSuperclass()+"->"+reference.substring(reference.indexOf("->")+2));
        return actual==null?declared:actual;
    }
    void dispatchCapability(Host h,Job job,Call call,List<V> args,String kind,boolean conditional){
        if(call.isStatic()||args.isEmpty()||kind.equals("message_bridge")||idx.customCallbacks.containsKey(call.method())){
            emit(h,job,call,args,kind,conditional);followApiOverride(h,job,call,args,conditional);return;
        }
        Method declared=call.isSuper()?resolveSuper(job.method,call.method()):idx.resolve(call.method());
        String shape=call.method().substring(call.method().indexOf("->")+2);
        boolean nullable=alternatives(args.get(0)).stream().anyMatch(v->v.kind().equals("literal")&&"0".equals(v.literal()));
        for(V receiver:alternatives(args.get(0))){
            if(receiver.kind().equals("literal")&&"0".equals(receiver.literal()))continue;
            boolean known=Set.of("object","new","view","host").contains(receiver.kind());
            Method actual=call.isDirect()||call.isSuper()?declared:receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+shape);
            String owner=actual==null?null:CapabilityIndex.cls(actual.getDefiningClass());
            boolean override=actual!=null&&actual.getImplementation()!=null&&!owner.startsWith("android.")&&!Cfg.WEBVIEWS.contains(owner)&&
                !Set.of("com.tencent.smtt.sdk.WebSettings","com.uc.webview.export.WebSettings").contains(owner)&&
                (idx.webview(owner)||idx.settings(owner));
            List<V> bound=specializeReceiver(args,receiver);
            if(override&&(known||call.isDirect()||call.isSuper())){
                // A resolved override supplies the behavior. Only its real framework calls
                // can establish the original capability effect, including explicit super.
                enqueue(h,actual,bound,extend(job.path,"api_override:"+call.method()),job.candidate||conditional||args.get(0).kind().equals("union"));
            }else{
                Job observed=new Job(job.method,job.args,job.path,job.candidate||!known||args.get(0).kind().equals("union"));
                if(nullable){bound=new ArrayList<>(bound);bound.set(0,union(receiver,V.literal("number","0")));}
                emit(h,observed,call,bound,kind,conditional);
                if(override)followApiOverride(h,observed,call,bound,conditional);
            }
        }
    }
    void followApiOverride(Host h,Job job,Call call,List<V> args,boolean conditional){
        if(call.isStatic()||args.isEmpty())return;
        Method declared=call.isSuper()?resolveSuper(job.method,call.method()):idx.resolve(call.method());String shape=call.method().substring(call.method().indexOf("->")+2);
        for(V receiver:alternatives(args.get(0))){
            Method actual=call.isDirect()||call.isSuper()?declared:receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+shape);
            if(actual==null||actual.getImplementation()==null)continue;
            String owner=CapabilityIndex.cls(actual.getDefiningClass());
            if(!idx.webview(owner)||Cfg.WEBVIEWS.contains(owner)||owner.startsWith("android.")||!idx.relevant.contains(CapabilityIndex.key(actual)))continue;
            // Recording an API invocation must not swallow an actual app override. Its
            // additional registration/settings effects are reached through this exact call.
            enqueue(h,actual,specializeReceiver(args,receiver),extend(job.path,"api_override:"+call.method()),job.candidate||conditional||args.get(0).kind().equals("union"));
        }
    }
    boolean layoutInflate(String method){
        if(!name(method).equals("inflate"))return false;
        String owner=owner(method);Method resolved=idx.resolve(method);
        if(resolved!=null&&resolved.getImplementation()!=null&&!CapabilityIndex.cls(resolved.getDefiningClass()).startsWith("android."))return false;
        return name(method).equals("inflate")&&((idx.subtype(owner,"android.view.LayoutInflater")&&
            (method.endsWith("(ILandroid/view/ViewGroup;)Landroid/view/View;")||method.endsWith("(ILandroid/view/ViewGroup;Z)Landroid/view/View;")))||
            idx.subtype(owner,"android.view.View")&&method.endsWith("(Landroid/content/Context;ILandroid/view/ViewGroup;)Landroid/view/View;"));
    }
    boolean layoutContent(String method){
        if(!name(method).equals("setContentView"))return false;
        Method resolved=idx.resolve(method);
        return name(method).equals("setContentView")&&(idx.activity(owner(method))||idx.subtype(owner(method),"android.app.Dialog"))&&method.endsWith("(I)V")&&
            (resolved==null||resolved.getImplementation()==null||CapabilityIndex.cls(resolved.getDefiningClass()).startsWith("android."));
    }
    boolean layoutAddView(String method){
        return name(method).equals("addView")&&idx.subtype(owner(method),"android.view.ViewGroup")&&method.substring(method.indexOf('(')).startsWith("(Landroid/view/View;");
    }
    void prepareLayouts(Summary summary,Job job,Host h,int depth,Set<String> visiting){
        if(depth>12||System.nanoTime()>deadline)return;
        String context=CapabilityIndex.key(job.method)+"|"+job.args;
        if(!h.preparingLayouts.add(context))return;
        try{for(Call call:summary.calls())if(layoutInflate(call.method())||layoutContent(call.method())||layoutAddView(call.method())){
            List<V> args=call.args().stream().map(x->eval(x,job,h,depth+1,new HashSet<>(visiting))).toList();
            if(layoutContent(call.method())&&args.size()==2){
                for(V receiver:alternatives(args.get(0))){
                    Method actual=call.isSuper()||call.isDirect()||receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+call.method().substring(call.method().indexOf("->")+2));
                    if(actual==null||actual.getImplementation()==null||CapabilityIndex.cls(actual.getDefiningClass()).startsWith("android."))mountLayout(receiver,args.get(1),h);
                }
            }
            else if(layoutInflate(call.method()))inflateLayout(call.method(),args,job,h,String.valueOf(call.offset()));
            else if(layoutAddView(call.method())&&args.size()>1)for(V parent:alternatives(args.get(0)))for(V child:alternatives(args.get(1)))
                h.layoutChildren.computeIfAbsent(parent.id(),k->new LinkedHashSet<>()).add(child.id());
        }}finally{h.preparingLayouts.remove(context);}
    }
    void mountLayout(V root,V resource,Host h){
        for(V r:alternatives(root))for(V id:alternatives(resource)){
            Long value=number(id);if(value==null){h.gaps.add("dynamic_layout_resource");continue;}
            Set<String> layouts=apk.layouts(value.intValue());
            if(layouts.isEmpty()){h.gaps.add("unresolved_layout_resource:"+value);continue;}
            var scope=h.layoutScopes.computeIfAbsent(r.id(),k->new LinkedHashSet<>());
            for(String path:layouts){var nodes=apk.layoutRoots.get(path);if(nodes==null)h.gaps.add("missing_layout_xml:"+path);else scope.addAll(nodes);}
            if(layouts.size()>1)h.gaps.add("layout_configuration_alternatives");
            if(scope.size()>1)h.gaps.add("layout_union_not_cooccurrence");
        }
    }
    V inflateLayout(String method,List<V> args,Job job,Host h){return inflateLayout(method,args,job,h,"synthetic");}
    V inflateLayout(String method,List<V> args,Job job,Host h,String site){
        if(args.size()<3)return V.of("unknown","android.view.View","inflate_arguments");
        // Both View.inflate(context,id,parent) and LayoutInflater.inflate(id,parent[,attach])
        // carry their resource at argument 1; the former is static, the latter has this.
        Long resource=number(args.get(1));
        boolean hasParent=alternatives(args.get(2)).stream().anyMatch(p->!p.kind().equals("unknown")&&!(p.kind().equals("literal")&&"0".equals(p.literal())));
        Long attach=args.size()>3?number(args.get(3)):Long.valueOf(hasParent?1L:0L);
        if(resource!=null&&(!hasParent||Long.valueOf(0).equals(attach))&&apk.layouts(resource.intValue()).stream()
            .flatMap(path->apk.layoutRoots.getOrDefault(path,List.of()).stream()).anyMatch(node->node.type.equals("merge"))){
            h.gaps.add("invalid_merge_inflation:"+resource);return V.of("unknown","android.view.View","invalid_merge_inflation");
        }
        V root=V.of("view","android.view.View","inflate:"+CapabilityIndex.key(job.method)+"@"+site+"|"+allocationContext(job)+"|"+UUID.nameUUIDFromBytes(args.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        mountLayout(root,args.get(1),h);
        h.gaps.add("xml_allocation_site_abstraction");
        boolean staticView=idx.subtype(owner(method),"android.view.View");
        V attached=null;
        for(V parent:alternatives(args.get(2))){
            if(parent.kind().equals("literal")&&"0".equals(parent.literal()))continue;
            if(parent.kind().equals("unknown")){h.gaps.add("unresolved_inflate_parent");continue;}
            Long flag=args.size()>3?number(args.get(3)):Long.valueOf(1L);
            if(flag==null)h.gaps.add("dynamic_inflate_attachment");
            if(staticView||flag==null||flag!=0){mountLayout(parent,args.get(1),h);attached=union(attached,parent);}
        }
        V typedRoot=null;
        for(var node:h.layoutScopes.getOrDefault(root.id(),Set.of()))if(node.type.contains(".")){
            V choice=V.of("view",node.type,root.id());typedRoot=union(typedRoot,choice);
            h.xmlBindings.computeIfAbsent(root.id(),k->new LinkedHashSet<>()).add(Map.of("lookup_root",root.id(),"layout",node.source==null?"synthetic":node.source,"concrete_type",node.type));
            if(idx.component(node.type)&&!idx.activity(node.type)&&!Cfg.WEBVIEWS.contains(node.type))seed(h,node.type,choice,extend(job.path,"xml_inflate:"+node.source),true);
        }
        V returned=typedRoot==null?root:typedRoot;
        if(attached!=null){
            if(staticView||args.size()==3||attach!=null&&attach!=0)return attached;
            if(attach==null)return union(returned,attached);
        }
        return returned;
    }
    V lookupView(V receiver,V resource,String declared,Job job,Host h){
        V result=null;
        for(V root:alternatives(receiver))for(V id:alternatives(resource)){
            Long value=number(id);List<ApkInventory.LayoutNode> matches=new ArrayList<>();
            if(value!=null){
                var roots=new ArrayDeque<String>();var seen=new HashSet<String>();roots.add(root.id());
                while(!roots.isEmpty()&&seen.size()<512){String r=roots.remove();if(!seen.add(r))continue;
                    for(var node:h.layoutScopes.getOrDefault(r,Set.of()))findLayoutNodes(node,value.intValue(),matches,new HashSet<>(),0);
                    roots.addAll(h.layoutChildren.getOrDefault(r,Set.of()));
                }
                if(!roots.isEmpty())h.gaps.add("layout_root_budget");
            }
            matches=new ArrayList<>(new LinkedHashSet<>(matches));
            String identity=root.id()+"/view:"+id.id();
            // Even before a parent layout is resolved, a successful findViewById result is
            // scoped beneath its receiver. Preserve that conditional edge so later addView
            // effects can be replayed through multi-level Activity container wrappers.
            h.layoutChildren.computeIfAbsent(root.id(),k->new LinkedHashSet<>()).add(identity);
            if(matches.isEmpty()){h.gaps.add("xml_lookup_unresolved_candidate");result=union(result,V.of("view",declared,identity));continue;}
            for(var node:matches){
                String type=node.type.contains(".")?node.type:declared;
                V view=V.of("view",type,identity);
                h.layoutScopes.computeIfAbsent(identity,k->new LinkedHashSet<>()).add(node);
                h.layoutChildren.computeIfAbsent(root.id(),k->new LinkedHashSet<>()).add(identity);
                h.xmlBindings.computeIfAbsent(identity,k->new LinkedHashSet<>()).add(Map.of(
                    "lookup_root",root.id(),"view_resource_id",value,"layout",node.source==null?"synthetic":node.source,"concrete_type",type));
                result=union(result,view);
                if(idx.component(type)&&!idx.activity(type)&&!Cfg.WEBVIEWS.contains(type))seed(h,type,view,extend(job.path,"xml_view:"+value+":"+type),true);
            }
            if(matches.size()>1)h.gaps.add("xml_lookup_alternatives:"+value);
        }
        return result==null?V.of("unknown",declared,"view_lookup"):result;
    }
    boolean fragmentProtocolReceiver(V value,String contract){
        return alternatives(value).stream().allMatch(receiver->Set.of("object","new","host","view","fragment_manager","fragment_transaction").contains(receiver.kind())&&receiver.type()!=null&&idx.subtype(receiver.type(),contract));
    }
    boolean frameworkFragmentAccess(String method,List<V> args){return frameworkFragmentAccess(method,args,false);}
    boolean frameworkFragmentAccess(String method,List<V> args,boolean explicitSuper){
        // Only this exact public Fragment protocol crosses packaged SDK bodies.
        if(!FragmentTransactions.manager(method)&&!FragmentTransactions.begin(method)&&!FragmentTransactions.operation(method)&&!FragmentTransactions.fluent(method))return false;
        for(V receiver:args.isEmpty()?List.<V>of():alternatives(args.get(0))){
            Method actual=explicitSuper?idx.resolve(method):receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+method.substring(method.indexOf("->")+2));
            if(actual==null||actual.getImplementation()==null)continue;
            String declaration=CapabilityIndex.cls(actual.getDefiningClass());
            boolean contract=declaration.equals("android.app.Activity")&&FragmentTransactions.manager(method);
            for(String family:FragmentTransactions.PREFIXES)for(String type:List.of("Fragment","FragmentActivity","FragmentManager","FragmentTransaction"))if(declaration.equals(family+"."+type))contract=true;
            if(!contract)return false;
        }return true;
    }
    boolean frameworkViewAccess(String method,List<V> args){
        for(V receiver:args.isEmpty()?List.<V>of():alternatives(args.get(0))){
            Method actual=receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+method.substring(method.indexOf("->")+2));
            if(actual!=null&&actual.getImplementation()!=null){String owner=CapabilityIndex.cls(actual.getDefiningClass());
                if(!owner.startsWith("android.")&&!owner.equals("androidx.fragment.app.Fragment"))return false;
            }
        }
        return true;
    }
    boolean fragment(String type){return idx.subtype(type,"android.app.Fragment")||idx.subtype(type,"androidx.fragment.app.Fragment")||idx.subtype(type,"android.support.v4.app.Fragment");}
    V fragmentView(V receiver,Job job,Host h,int depth,Set<String> visiting){
        V result=null;
        for(V actual:alternatives(receiver)){
            V known=h.fragmentViews.get(actual.id());if(known!=null){result=union(result,known);continue;}
            if(actual.type()==null||!fragment(actual.type())||!actual.kind().equals("object")){h.gaps.add("fragment_receiver_unresolved");continue;}
            Method creator=idx.resolve(desc(actual.type())+"->onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
            if(creator==null||creator.getImplementation()==null){h.gaps.add("fragment_view_factory_unresolved:"+actual.type());continue;}
            String key="fragment_view:"+actual.id();if(!visiting.add(key)){h.gaps.add("fragment_view_factory_cycle");continue;}
            List<V> args=List.of(actual,V.of("object","android.view.LayoutInflater","fragment_inflater:"+actual.id()),
                V.of("unknown","android.view.ViewGroup","fragment_container:"+actual.id()),V.of("unknown","android.os.Bundle","fragment_state:"+actual.id()));
            V root=eval(expr("return","android.view.View",CapabilityIndex.key(creator),args),job,h,depth+1,new HashSet<>(visiting));
            if(!root.kind().equals("unknown")){h.fragmentViews.put(actual.id(),root);result=union(result,root);}
        }
        return result==null?V.of("unknown","android.view.View","fragment_view:"+receiver.id()):result;
    }
    V layoutChild(V receiver,V index,Job job,Host h){
        Long position=number(index);V result=null;
        if(position==null||position<0||position>4096){h.gaps.add("dynamic_layout_child_index");return V.of("unknown","android.view.View","layout_child:"+receiver.id());}
        for(V root:alternatives(receiver))for(var node:h.layoutScopes.getOrDefault(root.id(),Set.of())){
            if(position>=node.children.size())continue;
            var child=node.children.get(position.intValue());
            if(child.type.equals("include")||child.type.equals("fragment")||child.type.equals("merge")){h.gaps.add("layout_child_structural_tag");continue;}
            String identity=root.id()+(child.id==null?"/child:"+position:"/view:"+child.id);
            String type=child.type.contains(".")?child.type:"android.view.View";
            V value=V.of("view",type,identity);result=union(result,value);
            h.layoutScopes.computeIfAbsent(identity,k->new LinkedHashSet<>()).add(child);
            h.xmlBindings.computeIfAbsent(identity,k->new LinkedHashSet<>()).add(Map.of("lookup_root",root.id(),"child_index",position,"layout",child.source==null?"synthetic":child.source,"concrete_type",type));
            if(idx.component(type)&&!idx.activity(type)&&!Cfg.WEBVIEWS.contains(type))seed(h,type,value,extend(job.path,"xml_child:"+position),true);
        }
        if(result==null)h.gaps.add("layout_child_unresolved");
        return result==null?V.of("unknown","android.view.View","layout_child:"+receiver.id()+":"+position):result;
    }
    void findLayoutNodes(ApkInventory.LayoutNode node,int id,List<ApkInventory.LayoutNode> matches,Set<Integer> includes,int depth){
        if(depth>32)return;
        if(Objects.equals(node.id,id))matches.add(node);
        for(var child:node.children)findLayoutNodes(child,id,matches,includes,depth+1);
        if(node.include!=null&&includes.add(node.include)){
            for(String path:apk.layouts(node.include))for(var child:apk.layoutRoots.getOrDefault(path,List.of()))findLayoutNodes(child,id,matches,includes,depth+1);
            includes.remove(node.include);
        }
    }
    V dispatchHint(V value,Job job,Host h,int depth){
        if(depth>6)return UNKNOWN;
        if(value.kind().equals("param")){int i=Integer.parseInt(value.id());return i<job.args.size()?job.args.get(i):UNKNOWN;}
        if(value.kind().equals("cast"))return dispatchHint(value.args().get(0),job,h,depth+1);
        if(value.kind().equals("field")){V owner=dispatchHint(value.args().get(0),job,h,depth+1);V result=null;for(V receiver:alternatives(owner))result=union(result,h.heap.get(heapKey(value.id(),receiver)));return result==null?UNKNOWN:result;}
        return value;
    }
    boolean relevantOnReceiver(Method method,V receiver,Host h){
        if(!Set.of("object","new","host","view").contains(receiver.kind())||receiver.type()==null||!idx.subtype(receiver.type(),CapabilityIndex.cls(method.getDefiningClass())))return false;
        String key=CapabilityIndex.key(method)+"|"+receiver.type();Boolean cached=receiverRelevance.get(key);if(cached!=null&&cached)return true;
        boolean result=relevantOnReceiver(method,receiver.type(),h,new HashSet<>(),0);receiverRelevance.put(key,result);return result||instanceRelevant(method,receiver,h,new HashSet<>(),0);
    }
    boolean instanceRelevant(Method method,V receiver,Host h,Set<String> visited,int depth){
        if(System.nanoTime()>=Math.min(deadline,h.localDeadline))return false;
        String id=CapabilityIndex.key(method);if(idx.relevant.contains(id))return true;
        if(depth>=8||visited.size()>=32){h.gaps.add("instance_receiver_relevance_budget:"+id);return false;}
        if(method.getImplementation()==null||!visited.add(id+"|"+receiver.id()))return false;
        Job context=new Job(method,List.of(receiver),List.of(),true);
        for(Call call:flow.summary(method).calls()){
            if(call.isStatic()||call.args().isEmpty()||name(call.method()).startsWith("<"))continue;
            for(V actual:alternatives(dispatchHint(call.args().get(0),context,h,0))){
                if(actual.type()==null||!Set.of("object","host","view","new").contains(actual.kind()))continue;
                Method target=call.isSuper()?resolveSuper(method,call.method()):call.isDirect()?idx.resolve(call.method()):idx.resolve(desc(actual.type())+"->"+call.method().substring(call.method().indexOf("->")+2));
                if(target!=null&&instanceRelevant(target,actual,h,visited,depth+1))return true;
            }
        }return false;
    }
    boolean relevantOnReceiver(Method method,String type,Host h,Set<String> visited,int depth){
        String id=CapabilityIndex.key(method);if(idx.relevant.contains(id))return true;
        if(method.getImplementation()==null||!visited.add(id))return false;
        if(depth>3||visited.size()>32){h.gaps.add("receiver_relevance_budget:"+id);return false;}
        int count=0;for(var instruction:method.getImplementation().getInstructions())if(++count>512){h.gaps.add("receiver_relevance_body_budget:"+id);return false;}
        for(Call call:flow.summary(method).calls()){
            if(layoutInflate(call.method())||layoutContent(call.method()))return true;
            if(call.isStatic()||call.args().isEmpty())continue;
            V self=call.args().get(0);while(self.kind().equals("cast")&&!self.args().isEmpty())self=self.args().get(0);
            if(!self.kind().equals("param")||!self.id().equals("0"))continue;
            Method declared=idx.resolve(call.method());if(declared==null||name(call.method()).startsWith("<"))continue;
            Method actual=call.isDirect()||call.isSuper()?declared:idx.resolve(desc(type)+"->"+CapabilityIndex.shape(declared));
            if(layoutInflate(call.method())||layoutContent(call.method()))return true;
            if(actual!=null&&relevantOnReceiver(actual,type,h,visited,depth+1))return true;
        }
        return false;
    }
    boolean callbackCarrier(V value){
        if(!Set.of("new","object","view").contains(value.kind())||value.type()==null)return false;
        return callbackCarrierTypes.computeIfAbsent(value.type(),type->{
            ClassDef definition=idx.classes.get(type);if(definition==null||idx.component(type))return false;
            if(idx.scheduled(type))for(String shape:List.of("run()V","call()Ljava/lang/Object;")){
                Method entry=idx.resolve(desc(type)+"->"+shape);
                if(entry!=null&&idx.relevant.contains(CapabilityIndex.key(entry)))return true;
            }
            boolean captured=false;for(Field field:definition.getFields())if((field.getAccessFlags()&8)==0&&(idx.component(CapabilityIndex.cls(field.getType()))||idx.activity(CapabilityIndex.cls(field.getType())))){captured=true;break;}
            if(!captured)return false;
            for(Method method:idx.hierarchyMethods(type))if(method.getImplementation()!=null&&idx.relevant.contains(CapabilityIndex.key(method))&&!method.getName().startsWith("<"))
                for(Method contract:idx.byShape.getOrDefault(CapabilityIndex.shape(method),List.of()))
                    if(contract.getImplementation()==null&&idx.subtype(type,CapabilityIndex.cls(contract.getDefiningClass())))return true;
            return false;
        });
    }
    boolean relevantOnArguments(Method method,List<V> args,Job outer,Host h,Set<String> seen,int depth){
        String id=CapabilityIndex.key(method);if(idx.relevant.contains(id))return true;
        if(method.getImplementation()==null||!seen.add(id+"|"+args))return false;
        if(depth>3||seen.size()>32){h.gaps.add("argument_relevance_budget:"+id);return false;}
        int count=0;for(var instruction:method.getImplementation().getInstructions())if(++count>512){h.gaps.add("argument_relevance_body_budget:"+id);return false;}
        Job nested=new Job(method,args,outer.path,true);
        for(Call call:flow.summary(method).calls()){
            if(call.args().isEmpty())continue;
            // Only follow forwarding of a concrete component-capturing callback. A stored
            // or constructed callback alone does not cause any of its methods to execute.
            List<V> hints=call.args().stream().map(v->dispatchHint(v,nested,h,0)).toList();
            if(hints.stream().flatMap(v->alternatives(v).stream()).noneMatch(this::callbackCarrier))continue;
            List<V> bound=call.args().stream().map(v->eval(v,nested,h,0,new HashSet<>())).toList();
            for(var registration:async.entries(call.method()))if(registration.argument()<bound.size())
                for(V callback:alternatives(bound.get(registration.argument())))
                    if(callback.type()!=null&&idx.subtype(callback.type(),registration.contract()))
                        for(String member:registration.members()){
                            Method implementation=idx.resolve(desc(callback.type())+"->"+member);
                            if(implementation!=null&&idx.relevant.contains(CapabilityIndex.key(implementation)))return true;
                        }
            Method target=call.isSuper()?resolveSuper(method,call.method()):idx.resolve(call.method());
            if(target==null)continue;
            if(call.isStatic()||call.isDirect()||call.isSuper()){
                if(relevantOnArguments(target,bound,nested,h,seen,depth+1))return true;
            }else for(V receiver:alternatives(bound.get(0))){
                Method actual=receiver.type()==null?null:idx.resolve(desc(receiver.type())+"->"+CapabilityIndex.shape(target));
                if(actual!=null&&relevantOnArguments(actual,specializeReceiver(bound,receiver),nested,h,seen,depth+1))return true;
            }
        }
        return false;
    }
    boolean objectFieldSetter(Method method){
        String id=CapabilityIndex.key(method);Boolean cached=objectFieldSetters.get(id);if(cached!=null)return cached;
        boolean found=false;
        if(method.getImplementation()!=null&&(method.getAccessFlags()&8)==0&&!method.getName().startsWith("<")&&method.getReturnType().equals("V")&&method.getParameterTypes().size()==1&&method.getParameterTypes().get(0).toString().startsWith("L")){
            int count=0;boolean objectStore=false;
            for(var instruction:method.getImplementation().getInstructions()){if(++count>256)break;if(instruction.getOpcode()==org.jf.dexlib2.Opcode.IPUT_OBJECT)objectStore=true;}
            if(count<=256&&objectStore)for(Write write:flow.summary(method).writes()){
                V value=write.value();while(value.kind().equals("cast")&&!value.args().isEmpty())value=value.args().get(0);
                if(!write.field().startsWith("$")&&write.receiver().kind().equals("param")&&write.receiver().id().equals("0")&&value.kind().equals("param")&&value.id().equals("1")){found=true;break;}
            }
        }
        objectFieldSetters.put(id,found);return found;
    }
    void seed(Host h,String type,V self,List<String> path,boolean candidate){
        if(Cfg.WEBVIEWS.contains(type))return;
        if(fragment(type)&&!h.installedFragments.contains(self.id())){h.gaps.add("fragment_lifecycle_not_installed:"+type);return;}
        if(h.components.size()>=12000){h.gaps.add("component_instance_budget:"+type);return;}
        if(h.expanding.contains(type)||!h.components.add(type+"|"+self.id()))return;
        h.expanding.add(type);
        List<Method> hierarchy=idx.hierarchyMethods(type);
        Set<String> referencedShapes=new HashSet<>();
        for(Method entry:hierarchy)if(CapabilityIndex.cls(entry.getDefiningClass()).equals(type)||entry.getName().startsWith("on"))
            for(String call:idx.calls.getOrDefault(CapabilityIndex.key(entry),Set.of()))referencedShapes.add(call.substring(call.indexOf("->")+2));
        Set<String> usedFields=new HashSet<>();ArrayDeque<String> fieldMethods=new ArrayDeque<>();
        for(Method m:hierarchy){
            // A modeled inherited SDK accessor is consumed at its actual call site;
            // seeding its implementation would independently enter the state machine.
            if(FragmentTransactions.manager(CapabilityIndex.key(m))&&fragmentProtocolReceiver(self,CapabilityIndex.cls(m.getDefiningClass()))&&frameworkFragmentAccess(CapabilityIndex.key(m),List.of(self)))continue;
            if(!m.getName().startsWith("<")){
                // Component allocation permits framework callbacks, not arbitrary helpers.
                if(!idx.activity(type)&&idx.component(type)&&!idx.componentEntry(type,m))continue;
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
            boolean actualLifecycle=!idx.activity(type)&&idx.componentEntry(type,m)&&relevantOnReceiver(m,self,h);
            if(!idx.relevant.contains(CapabilityIndex.key(m))&&!actualLifecycle&&!(idx.activity(type)&&m.getName().equals("<init>")))continue;
            if(idx.activity(type)&&!CapabilityIndex.cls(m.getDefiningClass()).equals(type)&&!m.getName().equals("<init>")&&!m.getName().startsWith("on")&&!referencedShapes.contains(CapabilityIndex.shape(m)))continue;
            fieldMethods.add(CapabilityIndex.key(m));
            List<V> args=new ArrayList<>();if((m.getAccessFlags()&8)==0)args.add(self);
            for(CharSequence p:m.getParameterTypes()){
                if(fragment(type)&&m.getName().equals("onAttach")&&Set.of("Landroid/content/Context;","Landroid/app/Activity;").contains(p.toString()))args.add(V.of("host",h.activity,"activity:"+h.activity));
                else if(fragment(type)&&m.getName().equals("onViewCreated")&&args.size()==1&&p.toString().equals("Landroid/view/View;"))args.add(expr("fragment_view","android.view.View","fragment_view:"+self.id(),List.of(self)));
                else args.add(V.of("unknown",CapabilityIndex.cls(p.toString()),"entry_parameter"));
            }
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
        if(h.activeManifestEntry!=null&&ManifestProtocols.entry(args)==null){args=new ArrayList<>(args);args.add(clientContext(args)==null?args.size():args.size()-1,h.activeManifestEntry);}
        if(h.activeClientContext!=null&&clientContext(args)==null){args=new ArrayList<>(args);args.add(h.activeClientContext);}
        String context=CapabilityIndex.key(m)+"|"+args;if(h.visited.contains(context)||h.pending.contains(context))return;
        if(path.contains("[earlier evidence steps omitted]"))h.gaps.add("evidence_path_truncated");
        if(h.queue.size()>6000){h.gaps.add("queue_budget");return;}
        h.pending.add(context);
        Job job=new Job(m,args,extend(path,CapabilityIndex.key(m)),candidate);
        h.queue.add(job,concreteCapabilitySeed(h,job));
    }
    boolean concreteCapabilitySeed(Host h,Job job){
        if(!idx.seeds.contains(CapabilityIndex.key(job.method))||System.nanoTime()>=deadline)return false;
        // This is a scheduling hint only: no factory evaluation, allocation, or field inference.
        try{
            for(Call call:flow.summary(job.method).calls()){
                if(call.isStatic()||call.args().isEmpty()||kind(call.method())==null)continue;
                if(concretePriorityReceiver(priorityBinding(call.args().get(0),job,h,0)))return true;
            }
        }catch(RuntimeException ex){
            // Scheduling preference must never turn a decode failure into a host failure.
            h.gaps.add("priority_hint_failed:"+CapabilityIndex.key(job.method)+":"+ex.getClass().getSimpleName());
        }
        return false;
    }
    V priorityBinding(V value,Job job,Host h,int depth){
        if(depth>6)return UNKNOWN;
        if(value.kind().equals("param")){int i=Integer.parseInt(value.id());return i<job.args.size()?priorityBinding(job.args.get(i),job,h,depth+1):UNKNOWN;}
        if(value.kind().equals("cast")&&!value.args().isEmpty())return priorityBinding(value.args().get(0),job,h,depth+1);
        if(value.kind().equals("union")){V result=null;for(V choice:alternatives(value))result=union(result,priorityBinding(choice,job,h,depth+1));return result==null?UNKNOWN:result;}
        if(value.kind().equals("field")&&!value.args().isEmpty()){
            V result=null;for(V receiver:alternatives(priorityBinding(value.args().get(0),job,h,depth+1))){
                V stored=h.heap.get(heapKey(value.id(),receiver));if(stored!=null)result=union(result,priorityBinding(stored,job,h,depth+1));
            }return result==null?UNKNOWN:result;
        }
        if(value.kind().equals("settings")&&!value.args().isEmpty())return priorityBinding(value.args().get(0),job,h,depth+1);
        return value;
    }
    boolean concretePriorityReceiver(V receiver){
        // A concrete branch earns preference; unknown branches retain all original semantics.
        for(V concrete:alternatives(receiver))if(Set.of("object","new","view").contains(concrete.kind())&&
            (idx.webview(concrete.type())||idx.settings(concrete.type())))return true;
        return false;
    }
    static List<V> specializeReceiver(List<V> args,V receiver){
        V original=args.get(0);return args.stream().map(v->v.equals(original)?receiver:v).toList();
    }
    static List<String> extend(List<String> path,String s){
        var p=new ArrayList<>(path);p.add(s);
        // Evidence presentation length must not decide reachability. Actual work is bounded
        // by visited contexts, component instances, queue size, summary depth and deadline.
        if(p.size()>64){var compact=new ArrayList<>(p.subList(0,8));compact.add("[earlier evidence steps omitted]");compact.addAll(p.subList(p.size()-55,p.size()));return List.copyOf(compact);}
        return List.copyOf(p);
    }
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
    V deferredField(Host h,String field,V receiver,String type){
        String key=heapKey(field,receiver);h.deferredFields.putIfAbsent(key,new DeferredField(field,receiver));
        return V.of("field_object",type,key);
    }
    record RefreshKey(int depth,Set<String> relevantSeen){}
    record RefreshOutcome(V value,Set<String> addedSeen){}
    record RefreshDependency(V value,int remaining){}
    static final class RefreshExpired extends RuntimeException {
        RefreshExpired(){super(null,null,false,false);}
    }
    static final class RefreshTraversal {
        final IdentityHashMap<V,Map<RefreshKey,RefreshOutcome>> memo=new IdentityHashMap<>();
        final IdentityHashMap<V,Map<Integer,Set<String>>> dependencies=new IdentityHashMap<>();
        long work;
    }
    long lastRefreshWork;
    void refreshDeadline(){if(System.nanoTime()>=deadline||currentHost!=null&&System.nanoTime()>=currentHost.localDeadline)throw new RefreshExpired();}
    V refreshBinding(V value,Host h,int depth,Set<String> seen){
        RefreshTraversal traversal=new RefreshTraversal();
        try{return refreshBinding(value,h,depth,seen,traversal);}
        catch(RefreshExpired expired){h.gaps.add("deferred_binding_deadline");return value;}
        finally{lastRefreshWork=traversal.work;}
    }
    Set<String> refreshDependencies(V root,Host h,int remaining,RefreshTraversal traversal){
        Map<Integer,Set<String>> depths=traversal.dependencies.computeIfAbsent(root,k->new HashMap<>());
        Set<String> cached=depths.get(remaining);if(cached!=null)return cached;
        Set<String> fields=new HashSet<>();IdentityHashMap<V,Integer> explored=new IdentityHashMap<>();
        ArrayDeque<RefreshDependency> pending=new ArrayDeque<>();pending.add(new RefreshDependency(root,remaining));
        while(!pending.isEmpty()){
            refreshDeadline();var item=pending.remove();V value=item.value();int left=item.remaining();
            if(left<0||explored.getOrDefault(value,-1)>=left)continue;explored.put(value,left);
            if(value.kind().equals("union")){for(V choice:alternatives(value)){refreshDeadline();pending.add(new RefreshDependency(choice,left-1));}continue;}
            if(!value.kind().equals("field_object"))continue;
            fields.add(value.id());DeferredField field=h.deferredFields.get(value.id());if(field==null)continue;
            pending.add(new RefreshDependency(field.receiver(),left-1));
            ArrayDeque<V> receivers=new ArrayDeque<>();receivers.add(field.receiver());List<V> direct=new ArrayList<>();boolean dynamic=false;
            while(!receivers.isEmpty()){
                refreshDeadline();V receiver=receivers.remove();
                if(receiver.kind().equals("union"))receivers.addAll(alternatives(receiver));
                else if(receiver.kind().equals("field_object"))dynamic=true;
                else direct.add(receiver);
            }
            for(V receiver:direct){refreshDeadline();V stored=h.heap.get(heapKey(field.field(),receiver));if(stored!=null)pending.add(new RefreshDependency(stored,left-1));}
            if(dynamic){
                // Receiver refresh can change its identity. Every same-field heap value
                // is a safe dependency superset, not an evaluation/aliasing join.
                for(V stored:h.heap.valuesForField(field.field(),this::refreshDeadline)){refreshDeadline();pending.add(new RefreshDependency(stored,left-1));}
            }
        }
        Set<String> result=Set.copyOf(fields);depths.put(remaining,result);return result;
    }
    V refreshBinding(V value,Host h,int depth,Set<String> seen,RefreshTraversal traversal){
        refreshDeadline();
        if(depth>12){h.gaps.add("deferred_binding_depth");return value;}
        Set<String> dependencies=refreshDependencies(value,h,12-depth,traversal),relevantSeen=new HashSet<>();
        for(String field:seen)if(dependencies.contains(field))relevantSeen.add(field);
        RefreshKey key=new RefreshKey(depth,Set.copyOf(relevantSeen));
        Map<RefreshKey,RefreshOutcome> outcomes=traversal.memo.computeIfAbsent(value,k->new HashMap<>());
        RefreshOutcome cached=outcomes.get(key);if(cached!=null){seen.addAll(cached.addedSeen());return cached.value();}
        traversal.work++;Set<String> before=new HashSet<>(seen);V result=value;
        if(value.kind().equals("union")){
            V joined=null;for(V choice:alternatives(value)){refreshDeadline();joined=union(joined,refreshBinding(choice,h,depth+1,new HashSet<>(seen),traversal));}
            if(joined!=null)result=joined;
        }else if(value.kind().equals("field_object")){
            DeferredField field=h.deferredFields.get(value.id());
            if(field!=null&&seen.add(value.id())){
                V receiver=refreshBinding(field.receiver(),h,depth+1,seen,traversal),joined=null;
                for(V actual:alternatives(receiver)){
                    refreshDeadline();V stored=h.heap.get(heapKey(field.field(),actual));
                    if(stored!=null&&!stored.equals(value))joined=union(joined,refreshBinding(stored,h,depth+1,new HashSet<>(seen),traversal));
                }
                if(joined!=null){h.gaps.add("deferred_field_order_unproven");result=joined;}
            }
        }else if(value.kind().equals("view")){
            V joined=null;for(var binding:h.xmlBindings.getOrDefault(value.id(),Set.of())){
                refreshDeadline();String concrete=(String)binding.get("concrete_type");
                if(value.type()==null||idx.subtype(concrete,value.type()))joined=union(joined,V.of("view",concrete,value.id()));
            }
            if(joined!=null)result=joined;
        }
        Set<String> added=new HashSet<>(seen);added.removeAll(before);outcomes.put(key,new RefreshOutcome(result,Set.copyOf(added)));return result;
    }
    boolean splitManifestEntries(Host h,Job job){
        if(!ManifestProtocols.consumer(idx,job.method)||(h.activeManifestEntry!=null&&Objects.equals(h.activeManifestEntry.type(),CapabilityIndex.key(job.method))))return false;
        Summary base=flow.summary(job.method);if(base.truncated()){h.gaps.add("manifest_consumer_summary_incomplete");return false;}
        for(Call call:base.calls())if(call.method().equals(ManifestProtocols.KEYS)&&call.args().size()==1){
            V bundle=ManifestProtocols.resolve(call.args().get(0),this,job,h,0);
            if(!bundle.kind().equals("manifest_bundle")){h.gaps.add("manifest_metadata_scope_unresolved");continue;}
            List<String> keys=new ArrayList<>(apk.applicationMetadata.keySet());
            // Resolvable entries first are useful within the unchanged refinement cap.
            keys.sort(Comparator.comparingInt((String key)->idx.classes.containsKey(key)?0:1).thenComparing(key->key));
            int count=0;for(String key:keys){
                if(System.nanoTime()>=deadline){h.gaps.add("manifest_metadata_deadline");break;}
                if(count++>=256){h.gaps.add("manifest_entry_context_budget");break;}
                V value=ManifestProtocols.typed(apk.applicationMetadata.get(key));
                if(value.kind().equals("unknown"))h.gaps.add("manifest_metadata_value_unresolved:"+key);
                V selection=new V("manifest_entry_context",CapabilityIndex.key(job.method),bundle.id()+":"+key,null,List.of(bundle,V.literal("java.lang.String",key),value));
                List<V> bound=new ArrayList<>(job.args);bound.removeIf(argument->argument.kind().equals("manifest_entry_context"));bound.add(clientContext(bound)==null?bound.size():bound.size()-1,selection);
                enqueue(h,job.method,bound,extend(job.path,"manifest_entry:"+key),true);
            }
            return !keys.isEmpty();
        }return false;
    }
    V materializeManifest(V expression,Job job,Host h,int depth,Set<String> visiting){
        ClassDef declaration=idx.classes.get(expression.type());
        if(declaration==null){h.gaps.add("manifest_class_unresolved:"+expression.type());return UNKNOWN;}
        if((declaration.getAccessFlags()&1)==0||(declaration.getAccessFlags()&(0x200|0x400))!=0){h.gaps.add("manifest_class_not_constructible:"+expression.type());return UNKNOWN;}
        Method ctor=idx.byClass.getOrDefault(expression.type(),List.of()).stream().filter(method->method.getName().equals("<init>")&&method.getParameterTypes().isEmpty()&&(method.getAccessFlags()&1)!=0).findFirst().orElse(null);
        if(ctor==null){h.gaps.add("manifest_public_noarg_constructor_missing:"+expression.type());return UNKNOWN;}
        V object=V.of("object",expression.type(),expression.id()+"|"+allocationContext(job));
        materializeConstructor(ctor,List.of(object),job,h,depth+1,visiting);return object;
    }
    V guardValue(V value,Job job,Host h,int depth){
        if(depth>6)return UNKNOWN;
        if(value.kind().equals("param")){int index=Integer.parseInt(value.id());return index<job.args.size()?job.args.get(index):UNKNOWN;}
        if(value.kind().equals("cast"))return guardValue(value.args().get(0),job,h,depth+1);
        if(value.kind().equals("field")){
            // A mutable field's observed heap value is not proof that other lifecycle writes
            // cannot occur. Keep both guard branches unless the field is immutable.
            if(!idx.finalFields.contains(value.id()))return V.of("unknown",value.type(),"mutable_field_guard");
            V receiver=guardValue(value.args().get(0),job,h,depth+1);return h.heap.getOrDefault(heapKey(value.id(),receiver),UNKNOWN);
        }
        if(value.kind().startsWith("return")&&value.id().equals("Ljava/lang/String;->equals(Ljava/lang/Object;)Z")&&value.args().size()==2){
            V left=guardValue(value.args().get(0),job,h,depth+1),right=guardValue(value.args().get(1),job,h,depth+1);
            if(left.kind().equals("literal")&&"java.lang.String".equals(left.type())&&left.literal()!=null&&right.kind().equals("literal")&&"java.lang.String".equals(right.type())&&right.literal()!=null)return V.literal("number",left.literal().equals(right.literal())?"1":"0");
        }
        if(value.kind().startsWith("return")){V semantic=ManifestProtocols.resolve(value,this,job,h,0);return semantic.equals(UNKNOWN)?UNKNOWN:semantic;}
        return value;
    }
    V eval(V v,Job job,Host h,int depth,Set<String> visiting){
        if(activeEvaluations++==0)evaluationSteps=0;
        try{
            if(++evaluationSteps>evaluationStepLimit){h.gaps.add("resolve_work_budget:"+CapabilityIndex.key(job.method));return V.of("unknown",v.type(),"resolve_work_budget");}
            return evalInner(v,job,h,depth,visiting);
        }finally{activeEvaluations--;}
    }
    V evalInner(V v,Job job,Host h,int depth,Set<String> visiting){
        if(System.nanoTime()>Math.min(deadline,h.localDeadline)){String reason=System.nanoTime()>deadline?"global_deadline":"application_bootstrap_time_budget";h.gaps.add(reason);return V.of("unknown",v.type(),reason);}
        if(depth>14){h.gaps.add("resolve_depth");return V.of("unknown",v.type(),"resolve_depth");}
        if(v.kind().equals("param")){int i=Integer.parseInt(v.id());if(i>=job.args.size())return V.of("unknown",v.type(),"parameter");V arg=job.args.get(i);return arg.kind().equals("fragment_view")?fragmentView(arg.args().get(0),job,h,depth+1,visiting):refreshBinding(arg,h,0,new HashSet<>());}
        if(v.kind().equals("fragment_view"))return fragmentView(v.args().get(0),job,h,depth+1,visiting);
        if(v.kind().equals("array_new")){V array=V.of("object",v.type(),v.id()+"|"+allocationContext(job));Long size=number(eval(v.args().get(0),job,h,depth+1,visiting));if(size!=null&&size>=0)h.arrayLengths.put(array.id(),size);return array;}
        if(v.kind().equals("array")){V array=V.of("object",v.type(),v.id()+"|"+allocationContext(job));h.arrayLengths.put(array.id(),(long)v.args().size());for(int i=0;i<v.args().size();i++)applyWrite(h,"$element:"+i,array,eval(v.args().get(i),job,h,depth+1,new HashSet<>(visiting)));return array;}
        if(v.kind().equals("map_entry"))return new V("map_entry",null,"entry",null,v.args().stream().map(x->eval(x,job,h,depth+1,new HashSet<>(visiting))).toList());
        if(v.kind().equals("array_element"))return arrayElement(h,eval(v.args().get(0),job,h,depth+1,new HashSet<>(visiting)),v.args().size()>1?eval(v.args().get(1),job,h,depth+1,new HashSet<>(visiting)):UNKNOWN,v.type());
        if(v.kind().equals("new")){
            String allocation=allocationContext(job);
            // Runtime join-point factories take the static part first and the actual this/target
            // later. First-argument-only allocation conflates captures of distinct receivers.
            if(idx.joinPoint(v.type()))allocation+="|joinpoint_args:"+UUID.nameUUIDFromBytes(job.args.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            V object=V.of("object",v.type(),v.id()+"|"+allocation);
            for(Call ctor:flow.summary(job.method).calls())if(name(ctor.method()).equals("<init>")&&!ctor.args().isEmpty()&&ctor.args().get(0).equals(v)){
                Method target=idx.resolve(ctor.method());
                if(target!=null&&(idx.component(v.type())||idx.client(v.type())||reflection.carrier(v.type())||idx.bindingObjects.contains(v.type())||idx.classValueCarriers.contains(v.type())||idx.relevant.contains(CapabilityIndex.key(target)))){
                    List<V> args=new ArrayList<>();args.add(object);
                    for(int i=1;i<ctor.args().size();i++)args.add(eval(ctor.args().get(i),job,h,depth+1,new HashSet<>(visiting)));
                    materializeConstructor(target,args,job,h,depth+1,visiting);
                }else if(target!=null)ConstructorCaptures.capture(object,ctor,this,job,h,depth,visiting);
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
                V cast=new V(alternative.kind(),(alternative.kind().equals("object")||alternative.kind().equals("view")&&idx.subtype(alternative.type(),v.type()))?alternative.type():v.type(),alternative.id(),alternative.literal(),alternative.args());choices.add(cast);
                if(alternative.kind().equals("view")&&idx.component(cast.type())&&!idx.activity(cast.type())&&!Cfg.WEBVIEWS.contains(cast.type()))seed(h,cast.type(),cast,job.path,true);
            }
            return choices.size()==1?choices.get(0):new V("union",v.type(),original.id(),null,List.copyOf(choices));
        }
        if(v.kind().equals("settings"))return expr("settings",v.type(),v.id(),List.of(eval(v.args().get(0),job,h,depth+1,visiting)));
        if(v.kind().equals("manifest_new"))return materializeManifest(v,job,h,depth,visiting);
        if(v.kind().equals("field")){
            if(v.id().equals(ManifestProtocols.METADATA)){V nativeValue=ManifestProtocols.resolve(v,this,job,h,0);if(!nativeValue.equals(UNKNOWN))return nativeValue;}
            V receiver=eval(v.args().get(0),job,h,depth+1,new HashSet<>(visiting));
            if(receiver.kind().equals("union")){V joined=null;for(V alternative:alternatives(receiver))joined=union(joined,eval(expr("field",v.type(),v.id(),List.of(alternative)),job,h,depth+1,new HashSet<>(visiting)));return joined==null?UNKNOWN:joined;}
            String hk=heapKey(v.id(),receiver);
            if(idx.subtype(v.type(),"java.lang.Enum"))return new V("enum",v.type(),v.id(),v.id().substring(v.id().indexOf("->")+2,v.id().indexOf(':')),List.of());
            V result=h.heap.get(hk);if(result!=null)return observeTransportMap(v,refreshBinding(result,h,0,new HashSet<>()),h);
            if(!visiting.add(hk))return deferredField(h,v.id(),receiver,v.type());
            String owner=owner(v.id());ClassDef c=idx.classes.get(owner);
            if(c!=null){
                for(Field f:c.getFields())if(CapabilityIndex.field(f).equals(v.id())&&f.getInitialValue()!=null){V initial=encoded(f.getInitialValue());if(!initial.equals(UNKNOWN))return initial;}
                // Static reads load the declaring class. Instance field declarations do not
                // establish which constructor ran, or even that the receiver was allocated.
                // Actual allocation/constructor and XML entry jobs supply instance writes.
                for(Method m:idx.byClass.getOrDefault(owner,List.of()))if(receiver.kind().equals("static")&&m.getName().equals("<clinit>")){
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
            return result!=null?observeTransportMap(v,refreshBinding(result,h,0,new HashSet<>()),h):deferredField(h,v.id(),receiver,v.type());
        }
        if(v.kind().startsWith("return")){
            if(ManifestProtocols.pure(idx,v.id())){V semantic=ManifestProtocols.resolve(v,this,job,h,0);if(!semantic.equals(UNKNOWN)){if(semantic.kind().equals("unknown")&&semantic.id().startsWith("manifest_class_unresolved:"))h.gaps.add(semantic.id());return semantic.kind().equals("manifest_new")?materializeManifest(semantic,job,h,depth,visiting):semantic;}}
            List<V> args=v.args().stream().map(x->eval(x,job,h,depth+1,new HashSet<>(visiting))).toList();String name=name(v.id());
            V staticReflection=StaticReflection.resolve(v,args,this,job,h);if(staticReflection!=null)return staticReflection;
            V reflectiveFactory=ReflectiveFactories.resolve(v,args,this,job,h,depth,visiting);if(reflectiveFactory!=null)return reflectiveFactory;
            if(v.id().equals("Ljava/lang/Class;->getName()Ljava/lang/String;")&&args.size()==1){
                V result=null;for(V clazz:alternatives(args.get(0)))
                    result=union(result,clazz.kind().equals("class")&&clazz.type()!=null?V.literal("java.lang.String",clazz.type()):V.of("unknown","java.lang.String","dynamic_class_name"));
                return result==null?UNKNOWN:result;
            }
            if(FragmentTransactions.begin(v.id())&&args.size()==1&&fragmentProtocolReceiver(args.get(0),owner(v.id()))&&frameworkFragmentAccess(v.id(),args,v.kind().equals("return_super")||v.kind().startsWith("return_fragment_transaction_super:")))return V.of("fragment_transaction",v.type(),"fragment_transaction:"+CapabilityIndex.key(job.method)+":"+allocationContext(job)+":"+v.kind()+":"+args.get(0).id());
            if((FragmentTransactions.operation(v.id())||FragmentTransactions.fluent(v.id()))&&!args.isEmpty()&&frameworkFragmentAccess(v.id(),args,v.kind().equals("return_super")))return args.get(0);
            if(FragmentTransactions.manager(v.id())&&args.size()==1&&(idx.activity(owner(v.id()))||fragment(owner(v.id())))&&fragmentProtocolReceiver(args.get(0),owner(v.id()))&&frameworkFragmentAccess(v.id(),args,v.kind().equals("return_super")))return V.of("fragment_manager",v.type(),"fragment_manager:"+args.get(0).id());
            if(CapabilityIndex.fragmentFactory(v.id()))return instantiateFragment(v,args,job,h,depth,visiting);
            if(name.equals("getArguments")&&fragment(owner(v.id()))&&v.id().endsWith("()Landroid/os/Bundle;")&&args.size()==1&&frameworkViewAccess(v.id(),args)){
                V result=null;for(V receiver:alternatives(args.get(0)))result=union(result,h.heap.get(heapKey("$fragment_arguments",receiver)));
                if(result!=null)return result;
            }
            if(idx.closureLink(v.id())&&!args.isEmpty())return linkClosure(h,job,args,v.type(),depth,visiting);
            if(name.equals("getView")&&fragment(owner(v.id()))&&v.id().endsWith("()Landroid/view/View;")&&args.size()==1&&frameworkViewAccess(v.id(),args))return fragmentView(args.get(0),job,h,depth+1,visiting);
            if(name.equals("getChildAt")&&idx.subtype(owner(v.id()),"android.view.ViewGroup")&&v.id().endsWith("(I)Landroid/view/View;")&&args.size()==2&&frameworkViewAccess(v.id(),args))return layoutChild(args.get(0),args.get(1),job,h);
            if(layoutInflate(v.id()))return inflateLayout(v.id(),args,job,h,v.kind().startsWith("return_inflate:")?v.kind().substring(15):"unknown");
            if((name.equals("findViewById")||name.equals("requireViewById"))&&args.size()>1)return lookupView(args.get(0),args.get(1),v.type(),job,h);
            if((name.equals("getActivity")||name.equals("requireActivity")))return V.of("host",h.activity,"activity:"+h.activity);
            Method lazyFactory=idx.resolve(v.id());
            if(lazyFactory!=null&&owner(v.id()).startsWith("kotlin.")&&(lazyFactory.getAccessFlags()&8)!=0&&idx.lazyType(CapabilityIndex.cls(lazyFactory.getReturnType()))){
                V initializer=null;for(int parameter:lazyInitializerParameters(lazyFactory))if(parameter<args.size())for(V alternative:alternatives(args.get(parameter)))if(idx.function0Type(alternative.type()))initializer=union(initializer,alternative);
                if(initializer!=null)return expr("lazy",CapabilityIndex.cls(lazyFactory.getReturnType()),"lazy:"+initializer.id(),List.of(initializer));
            }
            if(lazyFactory==null&&name.equals("lazy")&&owner(v.id())!=null&&owner(v.id()).startsWith("kotlin.LazyKt")&&!args.isEmpty())return expr("lazy","kotlin.Lazy","lazy:"+args.get(args.size()-1).id(),List.of(args.get(args.size()-1)));
            if(name.equals("getValue")&&idx.lazyType(owner(v.id()))&&!args.isEmpty()){
                V result=null;for(V lazy:alternatives(args.get(0)))if(lazy.kind().equals("lazy"))for(V initializer:alternatives(lazy.args().get(0)))if(initializer.type()!=null)
                    for(Method invoke:idx.byClass.getOrDefault(initializer.type(),List.of()))if(invoke.getName().equals("invoke")&&invoke.getParameterTypes().isEmpty()&&invoke.getImplementation()!=null){
                        enqueue(h,invoke,List.of(initializer),extend(job.path,"lazy_getValue:"+v.id()),true);
                        result=union(result,eval(expr("return",CapabilityIndex.cls(invoke.getReturnType()),CapabilityIndex.key(invoke),List.of(initializer)),job,h,depth+1,new HashSet<>(visiting)));
                    }
                if(result!=null)return result;
            }
            if(idx.map(owner(v.id()))&&name.equals("get")&&args.size()==2&&v.id().endsWith("(Ljava/lang/Object;)Ljava/lang/Object;"))return mapLookup(h,args.get(0),args.get(1),v.type());
            if(!args.isEmpty()&&name.equals("asList")&&"java.util.Arrays".equals(owner(v.id())))return args.get(0);
            if(!args.isEmpty()&&name.equals("singletonList")&&"java.util.Collections".equals(owner(v.id()))){V container=V.of("object","java.util.List","singleton:"+args.get(0).id());applyWrite(h,"$contents",container,args.get(0));return container;}
            if(!args.isEmpty()&&name.equals("iterator")&&idx.collection(owner(v.id())))return expr("iterator","java.util.Iterator","iterator:"+args.get(0).id(),List.of(args.get(0)));
            if(!args.isEmpty()&&name.equals("next")&&owner(v.id()).equals("java.util.Iterator")&&args.get(0).kind().equals("iterator"))return elements(h,args.get(0).args().get(0),v.type());
            if(!args.isEmpty()&&name.equals("get")&&idx.collection(owner(v.id())))return elements(h,args.get(0),v.type());
            Method method=v.kind().equals("return_super")?resolveSuper(job.method,v.id()):idx.resolve(v.id());V result=null;Set<Method> targets=new LinkedHashSet<>();
            if(method!=null){
                if(!v.kind().equals("return_direct")&&!v.kind().equals("return_super")&&(method.getAccessFlags()&8)==0&&!args.isEmpty())for(V recv:alternatives(args.get(0)))if(recv.type()!=null){Method concrete=idx.resolve(desc(recv.type())+"->"+CapabilityIndex.shape(method));if(concrete!=null&&concrete.getImplementation()!=null)targets.add(concrete);}
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
                prepareLayouts(summary,nested,h,depth+1,visiting);
                if(idx.collection(CapabilityIndex.cls(actual.getReturnType()))||idx.map(CapabilityIndex.cls(actual.getReturnType())))for(Write w:summary.writes()){
                    V receiver=eval(w.receiver(),nested,h,depth+1,new HashSet<>(visiting));V value=eval(w.value(),nested,h,depth+1,new HashSet<>(visiting));applyWrite(h,w.field(),receiver,value);
                }
                for(V ret:summary.returns())result=union(result,eval(ret,nested,h,depth+1,new HashSet<>(visiting)));
            }
            return result==null?new V("unknown",v.type(),v.id(),null,List.of()):result;
        }
        return v;
    }
    V instantiateFragment(V expression,List<V> args,Job job,Host h,int depth,Set<String> visiting){
        if(args.size()<2){h.gaps.add("fragment_factory_arguments");return V.of("unknown",expression.type(),"fragment_factory_arguments");}
        V result=null;
        for(V name:alternatives(args.get(1))){
            if(!name.kind().equals("literal")||name.literal()==null){h.gaps.add("fragment_factory_dynamic_name:"+expression.id());result=union(result,V.of("unknown",expression.type(),"dynamic_fragment_factory"));continue;}
            String type=name.literal();ClassDef declaration=idx.classes.get(type);
            if(declaration==null||!idx.subtype(type,owner(expression.id()))||(declaration.getAccessFlags()&1)==0||(declaration.getAccessFlags()&0x600)!=0){
                h.gaps.add("fragment_factory_type_unresolved:"+type);result=union(result,V.of("unknown",expression.type(),"unresolved_fragment_factory:"+type));continue;
            }
            Method ctor=idx.resolve(desc(type)+"-><init>()V");
            if(ctor==null||!CapabilityIndex.cls(ctor.getDefiningClass()).equals(type)||(ctor.getAccessFlags()&1)==0){
                h.gaps.add("fragment_factory_constructor_unresolved:"+type);result=union(result,V.of("unknown",type,"unresolved_fragment_constructor:"+type));continue;
            }
            String identity="fragment_factory:"+expression.id()+"@"+expression.kind()+"|"+allocationContext(job)+"|"+type;
            V created=V.of("object",type,identity);
            materializeConstructor(ctor,List.of(created),job,h,depth+1,new HashSet<>(visiting));
            if(args.size()>2)applyWrite(h,"$fragment_arguments",created,args.get(2));
            // As with existing allocation entry modeling, construction is conditional,
            // not proof of attach. Adapter install supplies a separate evidence path.
            // Lifecycle is activated by an actual installation protocol, not construction.
            result=union(result,created);
        }
        return result==null?V.of("unknown",expression.type(),"unresolved_fragment_factory"):result;
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
        if(field.startsWith("$map_mutation:")){h.gaps.add("map_mutation_order_unresolved:"+field.substring(14));return;}
        if(field.equals("$map_entry")){
            if(!value.kind().equals("map_entry")||value.args().size()!=2){h.gaps.add("unresolved_map_entry");return;}
            for(V recv:alternatives(receiver)){
                if(!recv.kind().equals("object")){h.gaps.add("unresolved_map_receiver:"+recv.id());continue;}
                Map<String,V> entries=h.maps.computeIfAbsent(recv.id(),k->new LinkedHashMap<>());
                for(V key:alternatives(value.args().get(0))){
                    String identity=mapKey(key);if(identity==null){identity="*";h.gaps.add("unresolved_map_key:"+recv.id());}
                    V prior=entries.get(identity),next=value.args().get(1);
                    if(prior!=null&&!prior.equals(next))h.gaps.add("map_update_order_unresolved:"+recv.id());
                    if(entries.size()>=256&&!entries.containsKey(identity)){h.gaps.add("map_entry_budget:"+recv.id());entries.put("*",UNKNOWN);continue;}
                    entries.put(identity,union(prior,next));
                }
            }return;
        }

        if(field.equals("$contents")||field.equals("$contentsAll")||field.startsWith("$element:")){
            for(V recv:alternatives(receiver)){
                if(field.startsWith("$element:")){
                    String slot=field.substring(9);if(!slot.matches("[0-9]+"))slot="*";
                    Map<String,V> entries=h.arrays.computeIfAbsent(recv.id(),k->new LinkedHashMap<>());
                    if(entries.size()>=256&&!entries.containsKey(slot)){h.gaps.add("array_element_budget:"+recv.id());entries.put("*",UNKNOWN);}
                    else {V prior=entries.get(slot);if(prior!=null&&!prior.equals(value))h.gaps.add("array_update_order_unresolved:"+recv.id());entries.put(slot,union(prior,value));}
                }
                Set<V> values=h.contents.computeIfAbsent(recv.id(),k->new LinkedHashSet<>());
                for(V val:alternatives(value))if(field.equals("$contentsAll"))values.addAll(h.contents.getOrDefault(val.id(),Set.of()));else values.add(val);
                if(values.size()>256){h.gaps.add("collection_element_budget:"+recv.id());values.clear();values.add(UNKNOWN);}
            }
        }else for(V alternative:alternatives(receiver)){
            String key=heapKey(field,alternative);V old=h.heap.get(key),next=union(old,value);h.heap.put(key,next);
            if(!Objects.equals(old,next)&&h.installedFragments.contains(alternative.id())){
                String revision=key+"|"+next;
                if(h.fragmentReplays.size()>=12000){h.gaps.add("fragment_field_replay_budget");continue;}
                if(!h.fragmentReplays.add(revision))continue;
                for(Method callback:idx.hierarchyMethods(alternative.type()))if(idx.componentEntry(alternative.type(),callback)){
                    String prefix=CapabilityIndex.key(callback)+"|["+alternative;
                    h.visited.removeIf(context->context.startsWith(prefix));
                }
                h.components.remove(alternative.type()+"|"+alternative.id());
                seed(h,alternative.type(),alternative,List.of(h.activity,"installed_fragment_field_update:"+field),true);
            }
        }
    }
    V linkedJoinPoint(Host h,V receiver){
        V result=null;
        for(V point:alternatives(receiver))result=union(result,point.kind().equals("aspectj_joinpoint")?point:h.linkedClosures.getOrDefault(point.id(),point));
        return result==null?UNKNOWN:result;
    }
    V linkClosure(Host h,Job job,List<V> args,String type,int depth,Set<String> visiting){
        V linked=null;
        for(V closure:alternatives(args.get(0)))if(closure.kind().equals("object")&&idx.aroundClosure(closure.type())&&idx.bindingObjects.contains(closure.type())){
            String base=idx.aroundClosureBases.stream().filter(t->idx.subtype(closure.type(),t)).findFirst().orElseThrow();
            V state=eval(expr("field","java.lang.Object[]",desc(base)+"->state:[Ljava/lang/Object;",List.of(closure)),job,h,depth+1,new HashSet<>(visiting));
            V points=null;
            for(V array:alternatives(state)){
                Long length=h.arrayLengths.get(array.id());
                if(length!=null&&length>0)points=union(points,arrayElement(h,array,V.literal("number",String.valueOf(length-1)),type));
            }
            if(points==null)points=V.of("unknown",type,"unresolved_joinpoint:"+closure.id());
            for(V point:alternatives(points)){
                boolean concrete=point.kind().equals("object")&&idx.joinPoint(point.type());
                if(!concrete)h.gaps.add("aspectj_joinpoint_alias_unresolved:"+closure.type());
                V marker=expr("aspectj_joinpoint",type,concrete?point.id():"linked:"+closure.id(),List.of(closure,state));
                if(concrete){V previous=h.linkedClosures.get(point.id());if(previous!=null&&!previous.equals(marker))h.gaps.add("aspectj_closure_replacement_order_unresolved:"+point.id());h.linkedClosures.put(point.id(),union(previous,marker));}
                linked=union(linked,marker);
            }
        }
        if(linked!=null)return linked;
        h.gaps.add("aspectj_unresolved_link");return V.of("unknown",type,"aspectj_unresolved_link");
    }
    V arrayElement(Host h,V array,V index,String type){
        V result=null;
        for(V recv:alternatives(array))for(V slot:alternatives(index)){
            Long n=number(slot);
            if(n==null){h.gaps.add("dynamic_array_index:"+recv.id());result=union(result,elements(h,recv,type));continue;}
            Map<String,V> entries=h.arrays.getOrDefault(recv.id(),Map.of());
            result=union(result,entries.get(String.valueOf(n)));result=union(result,entries.get("*"));
        }
        return result==null?V.of("unknown",type,"unresolved_array_element"):result;
    }
    static String mapKey(V key){
        if(key.kind().equals("class"))return "class:"+key.type();
        if(key.kind().equals("literal"))return "literal:"+key.type()+":"+key.literal();
        return null;
    }
    V mapLookup(Host h,V receiver,V key,String type){
        V result=null;
        for(V recv:alternatives(receiver)){
            Map<String,V> entries=h.maps.getOrDefault(recv.id(),Map.of());
            for(V choice:alternatives(key)){
                String identity=mapKey(choice);
                if(identity==null){h.gaps.add("unresolved_map_lookup_key:"+recv.id());for(var entry:entries.entrySet())result=union(result,transportRegistryValue(h,entry.getValue(),recv,entry.getKey()));}
                else result=union(result,transportRegistryValue(h,entries.get(identity),recv,identity));
                result=union(result,transportRegistryValue(h,entries.get("*"),recv,"*"));
            }
        }
        if(result==null){String id="unresolved_map_lookup:"+receiver.id()+":"+key.id();h.lookupSources.putIfAbsent(id,List.of(receiver,key));return V.of("unknown",type,id);}return result;
    }
    V transportRegistryValue(Host h,V value,V map,String key){
        if(value==null||h.activeClientContext==null||!h.transportMaps.contains(map.id()))return value;
        V name=key.startsWith("literal:java.lang.String:")?V.literal("java.lang.String",key.substring("literal:java.lang.String:".length())):V.of("unknown","java.lang.String","transport_registry_dynamic_name");
        V marker=expr("transport_map_entry",null,map.id(),List.of(map,name,h.activeClientContext)),result=null;
        for(V alternative:alternatives(value)){
            if(!Set.of("object","new","view").contains(alternative.kind())){result=union(result,alternative);continue;}
            List<V> provenance=new ArrayList<>(alternative.args());if(!provenance.contains(marker))provenance.add(marker);
            result=union(result,new V(alternative.kind(),alternative.type(),alternative.id(),alternative.literal(),List.copyOf(provenance)));
        }return result;
    }
    V observeTransportMap(V field,V value,Host h){
        if(h.activeClientContext!=null&&idx.map(field.type())&&reflection.mapField(field.id()))for(V map:alternatives(value))if(map.kind().equals("object"))h.transportMaps.add(map.id());
        return value;
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
        V recv=args.isEmpty()?UNKNOWN:args.get(0);
        if(kind.equals("setting")){
            V views=null;
            for(V alternative:alternatives(recv))views=union(views,alternative.kind().equals("settings")?alternative.args().get(0):alternative);
            recv=views==null?UNKNOWN:views;
        }
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
        var xmlEvidence=new LinkedHashSet<Map<String,Object>>();for(V view:alternatives(recv))xmlEvidence.addAll(h.xmlBindings.getOrDefault(view.id(),Set.of()));
        if(!xmlEvidence.isEmpty()){base.put("xml_binding_evidence",xmlEvidence);base.put("binding_status","candidate");base.put("xml_binding_semantics","possible layout receivers; branch/configuration and repeated-instance cooccurrence not proven");}
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
            List<String> setterParameters=parameters(call.method().substring(call.method().indexOf('(')+1,call.method().indexOf(')')));
            // Standard SDK setters may take an Executor before the listener. The last
            // parameter is the installed contract; custom field setters retain index 1.
            int clientArgument=custom==null?setterParameters.size():1;
            if(clientArgument>=args.size())return;
            String installedContract=custom==null?CapabilityIndex.cls(setterParameters.get(setterParameters.size()-1)):custom.contract();
            if(custom!=null){base.put("callback_field",custom.field());base.put("callback_contract",custom.contract());base.put("resolution","receiver_field_stored_listener");}
            for(V client:alternatives(args.get(clientArgument))){
                if(custom!=null)applyWrite(h,custom.field(),recv,client);
                if(client.kind().equals("literal")&&"0".equals(client.literal())){Map<String,Object> reset=new LinkedHashMap<>(base);reset.put("kind","callback_removal");add(h,reset);continue;}
                Map<String,Object>b=new LinkedHashMap<>(base);b.put("implementation",client.type()==null?"unknown":client.type());b.put("members",custom==null?callbackMembers(client.type(),installedContract):customCallbackMembers(client.type(),custom));add(h,b);
                if(client.type()!=null)for(Method callback:idx.hierarchyMethods(client.type()))if((custom==null?idx.standardClientCallback(installedContract,callback):idx.contractMethods(custom.contract()).stream().anyMatch(m->CapabilityIndex.shape(m).equals(CapabilityIndex.shape(callback))))&&(idx.relevant.contains(CapabilityIndex.key(callback))||custom==null&&(idx.clientDelegationReachable(callback)||TransportProtocols.clientEntry(idx,callback)&&reflection.reachable(callback)))){
                    List<V> callbackArgs=new ArrayList<>();callbackArgs.add(client);
                    for(CharSequence p:callback.getParameterTypes())callbackArgs.add(idx.webview(CapabilityIndex.cls(p.toString()))?recv:V.of("unknown",CapabilityIndex.cls(p.toString()),"callback_parameter"));
                    if(custom==null)callbackArgs.add(expr("installed_client_context",call.method(),site,List.of(recv,client)));
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
                enqueueRegisteredHandler(h,job,registryKey,handler,args.get(1),site);
            }return;
        }
        add(h,base);
    }
    void enqueueRegisteredHandler(Host h,Job job,String registry,V handler,V registrationName,String site){
        if(handler.type()==null||!Set.of("new","object").contains(handler.kind()))return;
        for(String shape:idx.registryHandlerShapes.getOrDefault(registry,Set.of())){
            Method member=idx.resolve(desc(handler.type())+"->"+shape);
            if(member==null||member.getImplementation()==null||!idx.relevant.contains(CapabilityIndex.key(member)))continue;
            List<V> args=new ArrayList<>();args.add(handler);
            for(CharSequence parameter:member.getParameterTypes())args.add(V.of("unknown",CapabilityIndex.cls(parameter.toString()),"message_handler_parameter"));
            enqueue(h,member,args,extend(job.path,"conditional_registered_message_handler:"+site+":"+registrationName.literal()),true);
        }
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
        String key=fact.get("kind")+"|"+fact.get("site")+"|"+fact.get("webview")+"|"+fact.get("implementation")+"|"+fact.get("registration_name")+"|"+fact.get("values")+"|"+fact.get("delegate_object_id")+"|"+fact.get("registry_object_id")+"|"+fact.get("reflective_target_object_id");
        Map<String,Object> old=h.facts.get(key);if(old==null||fact.get("binding_status").equals("explicit"))h.facts.put(key,fact);
    }
    List<Map<String,Object>> bridgeMembers(String type){
        if(type==null)return List.of();List<Map<String,Object>> result=new ArrayList<>();
        // Java bridge reflection can invoke public static methods too. Static exclusion
        // belongs to virtual Client callbacks, not to annotated Java bridge endpoints.
        for(Method m:idx.hierarchyMethods(type))if((m.getAccessFlags()&1)!=0&&!m.getName().startsWith("<")){
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
    List<Map<String,Object>> callbackMembers(String type,String contract){
        if(type==null)return List.of();
        String cacheKey=type+"|"+contract;
        if(callbackCache.containsKey(cacheKey))return callbackCache.get(cacheKey);
        List<Map<String,Object>> result=new ArrayList<>();Set<String> seen=new HashSet<>();ArrayDeque<Method> queue=new ArrayDeque<>();
        for(Method m:idx.hierarchyMethods(type))if(idx.standardClientCallback(contract,m))queue.add(m);
        while(!queue.isEmpty()&&seen.size()<256){Method m=queue.remove();String id=CapabilityIndex.key(m);
            if(!seen.add(id)||(m.getAccessFlags()&8)!=0||m.getImplementation()==null)continue;
            String owner=CapabilityIndex.cls(m.getDefiningClass());if(owner.startsWith("android.webkit.")||owner.equals("com.tencent.smtt.sdk.WebViewClient")||owner.equals("com.tencent.smtt.sdk.WebChromeClient"))continue;
            result.add(Map.of("signature",id,"display",CapabilityIndex.display(m),"name",m.getName()));
            for(Call call:flow.summary(m).calls())if(call.isSuper()&&call.method().endsWith("->"+CapabilityIndex.shape(m))){Method parent=idx.resolve(call.method());if(parent!=null)queue.add(parent);}
        }
        callbackCache.put(cacheKey,result);return result;
    }
    Map<String,Object> hostReport(Host h){return hostReport(h,new ArrayList<>(h.facts.values()));}
    Map<String,Object> hostReport(Host h,List<Map<String,Object>> facts){
        Map<String,Object> report=new LinkedHashMap<>();report.put("activity",h.activity);report.put("declared",apk.activities.contains(h.activity));
        report.put("facts",facts);
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
        List<Map<String,Object>> unbound=new ArrayList<>();Set<Object> bootstrapSites=new HashSet<>();
        if(bootstrapState!=null)for(var fact:bootstrapState.facts()){unbound.add(Map.of("reason","application_bootstrap_no_activity_owner","fact",fact));bootstrapSites.add(fact.get("site"));}
        for(String id:idx.seeds){Method m=idx.methods.get(id);if(m==null)continue;Summary s=flow.cache.get(id);
            if(s==null){unbound.add(Map.of("method",id,"reason","not_expanded"));continue;}
            for(Call call:s.calls())if(kind(call.method())!=null&&!boundSites.contains(id+"@"+call.offset())&&!bootstrapSites.contains(id+"@"+call.offset()))unbound.add(Map.of("site",id+"@"+call.offset(),"api",call.method(),"reason","no_activity_owner"));
        }
        Map<String,Object> out=new LinkedHashMap<>();out.put("schema_version",1);out.put("package",apk.packageName);out.put("version",apk.version);out.put("apk_sha256",hash);out.put("status",status);List<Map<String,Object>> snapshots=new ArrayList<>();for(ActivityState state:states.values()){Map<String,Object> snapshot=stateReport(state);if(!((List<?>)snapshot.get("facts")).isEmpty())snapshots.add(snapshot);}out.put("activities",snapshots);out.put("activity_coverage",coverage(apk.activities.isEmpty()?new ArrayList<>(states.keySet()):new ArrayList<>(apk.activities)));out.put("unattributed",unbound);out.put("diagnostics",diagnostics);out.put("index_diagnostics",idx.diagnostics.stream().sorted().toList());out.put("manifest_diagnostics",apk.errors);out.put("metrics",metrics);out.put("semantics","Static binding evidence; explicit does not prove runtime execution. Candidate bindings are retained. Settings are observed operations, not final runtime state.");return out;
    }
}
