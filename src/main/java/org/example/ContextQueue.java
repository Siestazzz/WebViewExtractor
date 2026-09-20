package org.example;

import java.util.*;

/** Round-robin methods so one many-argument registration helper cannot starve new chains. */
final class ContextQueue {
    private final Map<String,ArrayDeque<CapabilityEngine.Job>> methods=new LinkedHashMap<>();
    private final ArrayDeque<String> ready=new ArrayDeque<>();
    private int size;
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
    int size(){return size;}
    boolean isEmpty(){return size==0;}
    void clear(){methods.clear();ready.clear();size=0;}
}
