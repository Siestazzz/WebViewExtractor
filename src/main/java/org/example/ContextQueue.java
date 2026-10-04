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
    /** Read-only bounded sample; does not rotate work or retain completed contexts. */
    List<Map<String,Object>> diagnostics(int limit){
        if(limit<=0)return List.of();
        Set<String> names=new HashSet<>(priority.methods.keySet());names.addAll(ordinary.methods.keySet());
        List<String> ranked=new ArrayList<>(names);
        ranked.sort(Comparator.comparingInt((String name)->laneSize(priority,name)+laneSize(ordinary,name)).reversed().thenComparing(name->name));
        List<Map<String,Object>> result=new ArrayList<>();
        for(String name:ranked.subList(0,Math.min(limit,ranked.size()))){
            var preferred=priority.methods.get(name);var normal=ordinary.methods.get(name);
            CapabilityEngine.Job example=preferred!=null?preferred.peek():normal.peek();
            Map<String,Object> row=new LinkedHashMap<>();row.put("method",name);
            row.put("priority",laneSize(priority,name));row.put("ordinary",laneSize(ordinary,name));
            row.put("example_argument_kinds",example.args().stream().map(DexFlow.V::kind).toList());
            row.put("example_entry_parameter_count",example.args().stream().filter(v->v.id().equals("entry_parameter")).count());
            List<String> path=example.path();row.put("example_path_tail",path.subList(Math.max(0,path.size()-4),path.size()));
            row.put("example_candidate",example.candidate());result.add(Collections.unmodifiableMap(row));
        }
        return List.copyOf(result);
    }
    private static int laneSize(Lane lane,String name){var jobs=lane.methods.get(name);return jobs==null?0:jobs.size();}
}
