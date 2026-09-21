package org.example;

import java.util.*;
import static org.example.CapabilitySelfTest.check;

/** Resume a real field/helper/two-WebView fixture one context at a time. */
final class ActivityResumeFixture {
    static void run(CapabilityIndex index,ApkInventory apk,Map<String,Object> expected){
        var resumed=new CapabilityEngine(index,apk,System.nanoTime()+30_000_000_000L);
        var state=resumed.beginActivity("test.AppActivity");
        var originalHost=state.host;int slices=0;boolean sawRefinement=false;
        while(!state.done){
            resumed.advanceActivity(state,50_000_000L,1);slices++;
            check(slices<1000,"Resuming failed to converge");
            if(!state.done){
                check(state.host==originalHost,"Pause recreated the heap");
                if(state.phase==1&&!state.previousFacts.isEmpty()){
                    sawRefinement=true;
                    @SuppressWarnings("unchecked") var snapshot=(List<Map<String,Object>>)resumed.stateReport(state).get("facts");
                    check(snapshot.size()>=state.previousFacts.size(),"Refinement checkpoint lost first-phase facts");
                    check(resumed.report("test","partial",Map.of()).get("activities") instanceof List,"Checkpoint missing");
                }
            }
        }
        check(slices>1&&sawRefinement,"Fixture never paused across refinement");
        check(expected.equals(resumed.activities.get(0)),"Resume changed field binding, receiver isolation or capability facts");
        resumed.advanceActivity(state,50_000_000L,1);
        check(resumed.activities.size()==1,"Resuming completed state duplicated output");
        check(state.host==null,"Completed heap retained unnecessarily");
        var expired=new CapabilityEngine(index,apk,System.nanoTime()-1);
        var stopped=expired.beginActivity("test.AppActivity");
        stopped.host.facts.put("known",new LinkedHashMap<>(Map.of("kind","bridge","webview",Map.of("id","known","type","android.webkit.WebView"))));
        int pending=stopped.host.queue.size();expired.advanceActivity(stopped,50_000_000L,1);
        check(!stopped.done&&stopped.host.queue.size()==pending&&stopped.host.facts.size()==1,"Expired budget discarded pending work/facts");
        check(expired.coverage(List.of("test.AppActivity","test.Unseen")).get(1).get("status").equals("not_started"),"Unstarted host mislabeled");
        System.out.println("ActivityResumeFixture PASS: one-context resume equals uninterrupted; phase snapshot retained; deadline preserves work; finished heap released.");
    }
}
