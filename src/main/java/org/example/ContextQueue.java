package org.example;

import java.util.*;

/** Bounded preference for concrete capability work, with method rotation in both lanes. */
final class ContextQueue {
    private static final class Lane {
        final Map<String,ArrayDeque<CapabilityEngine.Job>> methods=new LinkedHashMap<>();
        final ArrayDeque<String> ready=new ArrayDeque<>();
        int size;
        void add(CapabilityEngine.Job job){
            String method=CapabilityIndex.key(job.method());
            ArrayDeque<CapabilityEngine.Job> jobs=methods.get(method);
            if(jobs==null){jobs=new ArrayDeque<>();methods.put(method,jobs);ready.add(method);}
            jobs.add(job);size++;
        }
        CapabilityEngine.Job remove(){
            String method=ready.remove();ArrayDeque<CapabilityEngine.Job> jobs=methods.get(method);
            CapabilityEngine.Job result=jobs.remove();size--;
            if(jobs.isEmpty())methods.remove(method);else ready.add(method);
            return result;
        }
        void clear(){methods.clear();ready.clear();size=0;}
    }
    private final Lane priority=new Lane(),ordinary=new Lane();
    private int priorityStreak;
    void add(CapabilityEngine.Job job){add(job,false);}
    void add(CapabilityEngine.Job job,boolean preferred){(preferred?priority:ordinary).add(job);}
    CapabilityEngine.Job remove(){
        if(priority.size>0&&(ordinary.size==0||priorityStreak<3)){
            priorityStreak=Math.min(3,priorityStreak+1);return priority.remove();
        }
        priorityStreak=0;return ordinary.remove();
    }
    int prioritySize(){return priority.size;}
    int ordinarySize(){return ordinary.size;}
    int size(){return priority.size+ordinary.size;}
    boolean isEmpty(){return size()==0;}
    void clear(){priority.clear();ordinary.clear();priorityStreak=0;}
}
