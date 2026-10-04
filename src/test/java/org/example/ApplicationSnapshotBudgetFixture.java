package org.example;
import java.util.*;
import static org.example.DexFlow.*;
/** Executed startup state survives traversal exhaustion, without crossing the total deadline. */
final class ApplicationSnapshotBudgetFixture {
 static void run(){
  long start=System.nanoTime(),finish=start+1_000_000_000L;
  if(ApplicationBootstrap.executionDeadline(start,finish)!=start+900_000_000L)throw new AssertionError("No closure reserve");
  if(ApplicationBootstrap.executionDeadline(start,start+100)!=start+90)throw new AssertionError("Near-global deadline reserve exceeds total");
  var engine=new CapabilityEngine(new CapabilityIndex(),new ApkInventory(),finish);
  var host=engine.new Host("application:budget.App");host.localDeadline=0;host.gaps.add("application_bootstrap_time_budget");
  String root="Lbudget/Registry;::Lbudget/Registry;->ROOT:Lbudget/Service;",child="service::Lbudget/Service;->child:Lbudget/Child;";
  host.heap.put(root,V.of("object","budget.Service","service"));host.heap.put(child,V.of("object","budget.Child","child"));
  host.heap.put("unused::Lbudget/Other;->field:Lbudget/Child;",V.of("object","budget.Child","unreachable"));
  host.maps.put("child",new HashMap<>(Map.of("key",V.of("literal","java.lang.String","value"))));
  var state=ApplicationBootstrap.snapshot(host,"budget.App",start,finish);
  if(!state.heap().keySet().equals(Set.of(root,child))||!state.maps().containsKey("child"))throw new AssertionError("Executed static closure lost or unrelated instance copied: "+state);
  if(state.diagnostics().stream().noneMatch(x->x.contains("application_bootstrap_time_budget")))throw new AssertionError("Partial traversal concealed");
  var first=engine.new Host("first");var second=engine.new Host("second");state.install(first);state.install(second);first.maps.get("child").clear();
  if(second.maps.get("child").isEmpty()||state.maps().get("child").isEmpty())throw new AssertionError("Mutable startup map shared across hosts");
  var exhausted=ApplicationBootstrap.snapshot(host,"budget.App",start,0);
  if(!exhausted.heap().isEmpty()||exhausted.diagnostics().stream().noneMatch(x->x.contains("static_closure_budget")))throw new AssertionError("Hard deadline bypassed");
  System.out.println("ApplicationSnapshotBudgetFixture PASS: reserve, executed static closure, unreachable instance exclusion, host isolation, partial diagnostics and hard deadline.");
 }
}
