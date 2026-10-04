package org.example;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.CapabilitySelfTest.*;
import static org.example.DexFlow.*;

/** Serial/parallel semantic equality, host ownership and quiescent snapshot contracts. */
final class ParallelActivityFixture {
    static Map<String,Object> semantic(CapabilityEngine engine){
        var report=engine.report("fixture","partial",Map.of());var selected=new TreeMap<String,Object>();
        for(String key:List.of("activities","unattributed","diagnostics"))selected.put(key,report.get(key));
        return selected;
    }
    static void run()throws Exception {
        String helper="Lparallel/Helper;",bridge="Lparallel/Bridge;",guard="Lparallel/Guard;";
        var configure=method(helper,"configure",List.of(W,bridge,"Ljava/lang/String;"),9,3,List.of(invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
        var never=method(helper,"neverCalled",List.of(W,bridge),9,2,List.of(invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1),end()),false);
        var conditional=method(guard,"conditional",List.of(W,bridge,"Z"),9,4,List.of(new ImmutableInstruction21t(Opcode.IF_EQZ,3,7),str(0,"guard"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",1,2,0),end()),false);
        List<ImmutableClassDef> classes=new ArrayList<>();List<String> roots=new ArrayList<>();
        for(int i=0;i<18;i++){
            String descriptor="Lparallel/Host"+i+";";roots.add("parallel.Host"+i);
            List<org.jf.dexlib2.iface.instruction.Instruction> code=new ArrayList<>();
            for(int j=0;j<2;j++){code.add(make(0,W));code.add(make(1,bridge));code.add(str(2,"h"+i+"v"+j));code.add(invoke(Opcode.INVOKE_STATIC,helper,"relay0",List.of(W,bridge,"Ljava/lang/String;"),"V",0,1,2));}
            code.add(end());classes.add(clazz(descriptor,"Landroid/app/Activity;",method(descriptor,"onCreate",List.of(),1,4,code,false)));
        }
        roots.add("parallel.Empty");classes.add(clazz("Lparallel/Empty;","Landroid/app/Activity;"));List<ImmutableMethod> helperMethods=new ArrayList<>(List.of(configure,never));for(int i=0;i<4;i++)helperMethods.add(method(helper,"relay"+i,List.of(W,bridge,"Ljava/lang/String;"),9,3,List.of(invoke(Opcode.INVOKE_STATIC,helper,i==3?"configure":"relay"+(i+1),List.of(W,bridge,"Ljava/lang/String;"),"V",0,1,2),end()),false));classes.add(clazz(helper,"Ljava/lang/Object;",helperMethods.toArray(ImmutableMethod[]::new)));classes.add(clazz(guard,"Ljava/lang/Object;",conditional));classes.add(clazz(bridge,"Ljava/lang/Object;",method(bridge,"expose",List.of(),1,1,List.of(end()),true)));
        Path file=Files.createTempFile("parallel-activities-",".dex"),output=Files.createTempDirectory("parallel-snapshot-");
        long oldWrites=Main.reportWrites,oldNanos=Main.reportWriteNanos;
        try{
            DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var apk=new ApkInventory();apk.activities.addAll(roots);
            var baseline=new CapabilityEngine(idx,apk,deadline);for(String root:roots)baseline.analyzeActivity(root);
            Map<String,Object> expected=semantic(baseline);
            for(int workers:List.of(1,2,8,8))try(var scheduler=new ParallelActivityScheduler(idx,apk,roots,System.nanoTime(),deadline,deadline,workers)){
                AtomicInteger snapshots=new AtomicInteger();Runnable checkpoint=()->{
                    check(scheduler.quiescent,"Checkpoint read live worker state");
                    check(scheduler.lanes.stream().allMatch(lane->lane.engine.currentHost==null),"Worker still executing at barrier");
                    snapshots.incrementAndGet();
                };
                scheduler.initialPass(checkpoint);
                check(scheduler.attempted.get()==roots.size()&&scheduler.aggregate.states.values().stream().allMatch(s->s.initialPass&&s.jobs<=8),"Deep work bypassed all-Activity first-pass barrier");
                check(scheduler.pending(),"Fixture failed to exercise resumable deep work");
                check(scheduler.lanes.stream().allMatch(lane->lane.engine.flow==scheduler.aggregate.flow),"Workers duplicated base summaries");
                int epochs=0;while(scheduler.pending()){scheduler.deepEpoch(5_000_000L,checkpoint);check(++epochs<100,"Parallel continuation did not converge");}
                check(scheduler.finished()&&semantic(scheduler.aggregate).equals(expected),"Worker count changed facts, ownership or unattributed sites: "+workers);
                check(scheduler.aggregate.flow.decoded==baseline.flow.decoded,"Shared base decode metrics were duplicated");
                check(scheduler.aggregate.coverage(roots).stream().allMatch(row->row.get("status").equals("traversal_finished")),"Empty host or worker root was lost");
                check(snapshots.get()>0,"Quiescent checkpoints were not called");
                @SuppressWarnings("unchecked")var reports=(List<Map<String,Object>>)scheduler.aggregate.report("fixture","partial",Map.of()).get("activities");
                for(var host:reports){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)host.get("facts");check(facts.size()==2&&facts.stream().map(f->f.get("webview")).distinct().count()==2,"Two WebViews merged inside an Activity");for(var fact:facts)check(fact.get("activity").equals(host.get("activity")),"Capability crossed Activity ownership");}
                Main.write(output.resolve("capabilities.json"),scheduler.aggregate.report("fixture","complete",Map.of()));
                check(com.google.gson.JsonParser.parseString(Files.readString(output.resolve("capabilities.json"))).getAsJsonObject().getAsJsonArray("activities").size()==18,"Atomic snapshot lost hosts");
                for(String name:List.of("capabilities.json","capabilities.compact.json","capabilities.counts.json"))check(Files.isRegularFile(output.resolve(name))&&!Files.exists(output.resolve(name+".tmp")),"Atomic export left incomplete snapshot: "+name);
            }
            sharedRefinement(idx,apk,conditional,deadline);
            quiescenceAndDeadline(idx,apk,roots,output);
            check(Main.analysisWorkers(Main.options(new String[]{"--analysis-workers","1"}))==1,"Serial CLI override lost");
            check(Main.analysisWorkers(Map.of())==Math.min(8,Runtime.getRuntime().availableProcessors()),"Default worker count exceeds CPU/bound");
            boolean rejected=false;try{Main.analysisWorkers(Map.of("--analysis-workers","9"));}catch(IllegalArgumentException expectedError){rejected=true;}check(rejected,"Unbounded worker request accepted");
            var command=Main.workerCommand(file,output,300,600,2);check(command.get(command.indexOf("--analysis-workers")+1).equals("2")&&command.get(command.indexOf("--hard-seconds")+1).equals("595"),"Supervisor dropped worker setting or watchdog reserve");
        }finally{
            Main.reportWrites=oldWrites;Main.reportWriteNanos=oldNanos;Files.deleteIfExists(file);try(var files=Files.list(output)){for(Path child:files.toList())Files.deleteIfExists(child);}Files.deleteIfExists(output);
        }
        System.out.println("ParallelActivityFixture PASS: serial/1/2/8-worker facts, two-WebView/two-Activity isolation, all-root first pass, shared decode/refinement budget, quiescent atomic snapshots, interrupted barrier and expired deadline.");
    }
    static void sharedRefinement(CapabilityIndex idx,ApkInventory apk,org.jf.dexlib2.iface.Method method,long deadline)throws Exception {
        try(var scheduler=new ParallelActivityScheduler(idx,apk,List.of(),System.nanoTime(),deadline,deadline,8)){
            AtomicInteger active=new AtomicInteger(),peak=new AtomicInteger();CyclicBarrier concurrentStart=new CyclicBarrier(8);
            List<Callable<Void>> concurrent=new ArrayList<>();for(int i=0;i<8;i++)concurrent.add(()->{int count=active.incrementAndGet();peak.accumulateAndGet(count,Math::max);concurrentStart.await(5,TimeUnit.SECONDS);active.decrementAndGet();return null;});scheduler.barrier(concurrent);check(peak.get()==8&&active.get()==0,"Configured worker pool did not permit bounded 8-way execution");
            List<Callable<Void>> tasks=new ArrayList<>();for(int i=0;i<8;i++){final int value=i%2;tasks.add(()->{
                Summary summary=scheduler.aggregate.flow.summary(method,v->v.kind().equals("param")&&v.id().equals("2")?V.literal("number",String.valueOf(value)):v);
                check(summary.calls().isEmpty()==(value==0),"Resolver ran against another worker context");return null;
            });}scheduler.barrier(tasks);
            check(scheduler.aggregate.flow.decoded==1&&scheduler.aggregate.flow.refined==2,"Same base/context summary was decoded per worker");
            for(int i=2;i<20;i++){int value=i;scheduler.aggregate.flow.summary(method,v->v.kind().equals("param")&&v.id().equals("2")?V.literal("number",String.valueOf(value)):v);}
            check(scheduler.aggregate.flow.refinements.get(CapabilityIndex.key(method)).size()==16&&scheduler.aggregate.flow.refined==16,"Parallelism expanded the 16-variant budget");
        }
    }
    static void quiescenceAndDeadline(CapabilityIndex idx,ApkInventory apk,List<String> roots,Path output)throws Exception {
        long expired=System.nanoTime()-1;
        try(var scheduler=new ParallelActivityScheduler(idx,apk,roots,expired,expired,expired,2)){
            AtomicInteger callbacks=new AtomicInteger();scheduler.initialPass(callbacks::incrementAndGet);scheduler.deepEpoch(1_000_000L,callbacks::incrementAndGet);
            check(scheduler.aggregate.states.isEmpty()&&scheduler.aggregate.coverage(roots).size()==roots.size()&&!scheduler.finished(),"Expired deadline started work or hid unstarted roots");
            Main.write(output.resolve("capabilities.json"),scheduler.aggregate.report("fixture","partial",Map.of("scheduling_stage","deadline")));
            check(com.google.gson.JsonParser.parseString(Files.readString(output.resolve("capabilities.json"))).getAsJsonObject().getAsJsonArray("activity_coverage").size()==roots.size(),"Timeout snapshot lost root coverage");
        }
        long partialDeadline=System.nanoTime()+200_000_000L;
        try(var scheduler=new ParallelActivityScheduler(idx,apk,roots,System.nanoTime(),partialDeadline,partialDeadline,2)){
            scheduler.barrier(List.of(()->{var lane=scheduler.lanes.get(0);var state=lane.engine.beginActivity(roots.get(0));lane.engine.advanceActivity(state,100_000_000L,9);state.initialPass=true;lane.pending.add(state);scheduler.attempted.incrementAndGet();return null;},()->{var lane=scheduler.lanes.get(1);var state=lane.engine.beginActivity(roots.get(1));lane.engine.advanceActivity(state,100_000_000L,1);state.initialPass=true;lane.pending.add(state);scheduler.attempted.incrementAndGet();return null;}));
            var before=semantic(scheduler.aggregate);check(!((List<?>)before.get("activities")).isEmpty()&&scheduler.pending(),"Partial timeout fixture failed to retain a real capability and pending work");
            int jobs=scheduler.aggregate.states.values().stream().mapToInt(state->state.jobs).sum();
            long remaining=partialDeadline-System.nanoTime();if(remaining>0)TimeUnit.NANOSECONDS.sleep(remaining);
            scheduler.deepEpoch(1_000_000L,()->check(scheduler.quiescent,"Expired partial snapshot was not quiescent"));
            check(semantic(scheduler.aggregate).equals(before)&&scheduler.aggregate.states.values().stream().mapToInt(state->state.jobs).sum()==jobs,"Deadline changed prior facts or consumed pending jobs");
            check(scheduler.aggregate.coverage(roots).stream().filter(row->row.get("status").equals("deadline_interrupted")).count()==2,"Timeout lost pending host status");
            Main.write(output.resolve("capabilities.json"),scheduler.aggregate.report("fixture","partial",Map.of("scheduling_stage","deadline")));
            check(com.google.gson.JsonParser.parseString(Files.readString(output.resolve("capabilities.json"))).getAsJsonObject().getAsJsonArray("activities").size()>0,"Timeout atomic snapshot lost retained capability");
        }
        long deadline=System.nanoTime()+2_000_000_000L;
        try(var scheduler=new ParallelActivityScheduler(idx,apk,List.of(),System.nanoTime(),deadline,deadline,2)){
            AtomicInteger completed=new AtomicInteger();CountDownLatch started=new CountDownLatch(2),release=new CountDownLatch(1);
            List<Callable<Void>> tasks=new ArrayList<>();for(int i=0;i<2;i++)tasks.add(()->{started.countDown();release.await();completed.incrementAndGet();return null;});
            Thread releaser=new Thread(()->{try{started.await();release.countDown();}catch(InterruptedException error){throw new RuntimeException(error);}});releaser.start();
            Thread.currentThread().interrupt();boolean interrupted=false;try{scheduler.barrier(tasks);}catch(InterruptedException expected){interrupted=true;}finally{Thread.interrupted();releaser.join();}
            check(interrupted&&completed.get()==2&&scheduler.quiescent,"Interrupted checkpoint escaped before worker quiescence");
            boolean failed=false;try{scheduler.barrier(List.of(()->{throw new IllegalStateException("fixture");},()->{completed.incrementAndGet();return null;}));}catch(IllegalStateException expected){failed=true;}
            check(failed&&completed.get()==3&&scheduler.quiescent,"Worker failure bypassed remaining task barrier");
        }
    }
}
