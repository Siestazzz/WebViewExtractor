package org.example;
import java.util.*;
import static org.example.CapabilitySelfTest.*;
import static org.example.DexFlow.*;

/** A shallow branching expression needs a total work bound as well as a depth bound. */
final class ResolutionBudgetFixture {
 static void run(){
  var method=method(A,"onCreate",List.of(),1,1,List.of(end()),false);
  var engine=new CapabilityEngine(new CapabilityIndex(),new ApkInventory(),System.nanoTime()+10_000_000_000L);
  var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(method,List.of(),List.of(),false);
  V value=V.literal("java.lang.String","known");
  for(int i=0;i<5;i++){
   V left=new V("cast","java.lang.String","left:"+i,null,List.of(value));
   V right=new V("cast","java.lang.String","right:"+i,null,List.of(value));
   value=new V("union","java.lang.String","branches:"+i,null,List.of(left,right));
  }
  engine.evaluationStepLimit=32;engine.eval(value,job,host,0,new HashSet<>());
  check(host.gaps.stream().anyMatch(g->g.startsWith("resolve_work_budget:")),"Shallow expression fanout must expose budget exhaustion");
  check(engine.eval(V.literal("java.lang.String","next"),job,host,0,new HashSet<>()).literal().equals("next"),"One exhausted expression poisoned the next independent evaluation");
  engine.evaluationStepLimit=1000;var complete=engine.eval(value,job,host,0,new HashSet<>());
  check("known".equals(complete.literal()),"Adequate work budget changed the resolved value");
 }
}
