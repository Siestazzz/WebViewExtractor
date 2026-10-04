package org.example;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Host ownership stays in fixed lanes; snapshots are read only after all lanes stop. */
final class ParallelActivityScheduler implements AutoCloseable {
    final List<String> roots;
    final List<Lane> lanes=new ArrayList<>();
    final CapabilityEngine aggregate;
    final ExecutorService executor;
    final long start,deadline,targetDeadline;
    final AtomicInteger attempted=new AtomicInteger();
    boolean quiescent=true;
    int barriers;
    static final class Lane {
        final CapabilityEngine engine;
        final List<String> roots=new ArrayList<>();
        final ArrayDeque<CapabilityEngine.ActivityState> pending=new ArrayDeque<>();
        int initialIndex;
        Lane(CapabilityEngine engine){this.engine=engine;}
    }
    ParallelActivityScheduler(CapabilityIndex index,ApkInventory apk,List<String> roots,long start,long deadline,long targetDeadline,int workers){
        if(workers<1||workers>8)throw new IllegalArgumentException("analysis workers must be between 1 and 8");
        this.roots=List.copyOf(roots);this.start=start;this.deadline=deadline;this.targetDeadline=targetDeadline;
        DexFlow shared=new DexFlow(index,deadline);aggregate=new CapabilityEngine(index,apk,deadline,shared);
        ApplicationBootstrap.State bootstrap=aggregate.applicationBootstrap();
        for(int i=0;i<workers;i++){var engine=new CapabilityEngine(index,apk,deadline,shared);engine.bootstrapState=bootstrap;lanes.add(new Lane(engine));}
        for(int i=0;i<roots.size();i++)lanes.get(i%workers).roots.add(roots.get(i));
        AtomicInteger ids=new AtomicInteger();
        executor=Executors.newFixedThreadPool(workers,task->{Thread thread=new Thread(task,"activity-analysis-"+ids.incrementAndGet());thread.setDaemon(true);return thread;});
    }
    void barrier(List<Callable<Void>> tasks)throws Exception {
        if(!quiescent)throw new IllegalStateException("Concurrent scheduler batch");
        quiescent=false;
        // Await every task, even after one failure, before any caller can read hosts.
        List<Future<Void>> futures=new ArrayList<>();Throwable failure=null;boolean interrupted=false;
        try{for(var task:tasks)futures.add(executor.submit(task));}
        catch(RuntimeException error){failure=error;}
        for(var future:futures){
            boolean complete=false;
            while(!complete)try{future.get();complete=true;}
            catch(InterruptedException error){interrupted=true;}
            catch(ExecutionException error){if(failure==null)failure=error.getCause();complete=true;}
        }
        quiescent=true;barriers++;refreshAggregate();
        if(interrupted){Thread.currentThread().interrupt();if(failure==null)failure=new InterruptedException("Interrupted while waiting for worker quiescence");}
        if(failure instanceof Error error)throw error;
        if(failure instanceof Exception error)throw error;
        if(failure!=null)throw new RuntimeException(failure);
    }
    void refreshAggregate(){
        if(!quiescent)throw new IllegalStateException("Snapshot while workers mutate hosts");
        aggregate.states.clear();aggregate.boundSites.clear();aggregate.diagnostics.clear();
        if(aggregate.bootstrapState!=null)aggregate.diagnostics.addAll(aggregate.bootstrapState.diagnostics());
        for(String root:roots)for(Lane lane:lanes){var state=lane.engine.states.get(root);if(state!=null){aggregate.states.put(root,state);break;}}
        for(Lane lane:lanes){aggregate.boundSites.addAll(lane.engine.boundSites);aggregate.diagnostics.addAll(lane.engine.diagnostics);}
        Collections.sort(aggregate.diagnostics);
    }
    void initialPass(Runnable checkpoint)throws Exception {
        while(System.nanoTime()<deadline&&lanes.stream().anyMatch(lane->lane.initialIndex<lane.roots.size())){
            List<Callable<Void>> tasks=new ArrayList<>();
            for(Lane lane:lanes)if(lane.initialIndex<lane.roots.size())tasks.add(()->{
                if(System.nanoTime()>=deadline)return null;
                String root=lane.roots.get(lane.initialIndex++);int before=attempted.getAndIncrement();
                long share=Math.max(1_000_000L,(Math.min(deadline,targetDeadline)-System.nanoTime())/Math.max(1,roots.size()-before));
                var state=lane.engine.beginActivity(root);lane.engine.advanceActivity(state,Math.min(50_000_000L,share),8);state.initialPass=true;
                if(!state.done)lane.pending.add(state);return null;
            });
            barrier(tasks);checkpoint.run();
        }
    }
    boolean pending(){return lanes.stream().anyMatch(lane->!lane.pending.isEmpty());}
    void deepEpoch(long durationNanos,Runnable checkpoint)throws Exception {
        long stop=Math.min(deadline,System.nanoTime()+durationNanos);List<Callable<Void>> tasks=new ArrayList<>();
        for(Lane lane:lanes)if(!lane.pending.isEmpty())tasks.add(()->{
            while(!lane.pending.isEmpty()&&System.nanoTime()<stop){
                var state=lane.pending.remove();lane.engine.advanceActivity(state,Math.min(50_000_000L,Math.max(1,stop-System.nanoTime())),100);
                if(!state.done)lane.pending.add(state);
            }return null;
        });
        barrier(tasks);checkpoint.run();
    }
    boolean finished(){return attempted.get()==roots.size()&&!pending();}
    @Override public void close(){executor.shutdownNow();}
}
