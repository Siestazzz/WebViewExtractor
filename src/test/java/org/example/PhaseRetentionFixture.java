package org.example;
import java.util.*;
import static org.example.CapabilitySelfTest.check;
final class PhaseRetentionFixture {
 static Map<String,Object> fact(String impl,DexFlow.V object,List<Map<String,Object>> members){
  var f=new LinkedHashMap<String,Object>();f.put("kind","bridge");f.put("site","Ltest/A;->onCreate()V@1");f.put("api","Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V");f.put("webview",Map.of("id","wv","type","android.webkit.WebView"));f.put("registration_name","bridge");f.put("implementation",impl);f.put("members",members);f.put("arguments",List.of(DexFlow.V.of("object","android.webkit.WebView","wv"),object,DexFlow.V.literal("java.lang.String","bridge")));return f;
 }
 static void run(){
  var engine=new CapabilityEngine(new CapabilityIndex(),new ApkInventory(),System.nanoTime()+10_000_000_000L);var s=engine.beginActivity("test.A");s.phase=1;
  var object=DexFlow.V.of("object","test.Bridge","object-a");var member=Map.<String,Object>of("signature","Ltest/Bridge;->expose()V");
  s.previousFacts.put("phase-only",fact("test.Bridge",object,List.of(member)));engine.finishActivity(s);
  check(((List<?>)s.completedReport.get("facts")).size()==1&&s.provisionalFacts==1,"Normal completion lost a first-phase-only capability");
  var second=engine.beginActivity("test.B");second.phase=1;second.previousFacts.put("same-key",fact("test.Bridge",object,List.of(member)));second.host.facts.put("same-key",fact("test.Bridge",object,List.of()));
  check(engine.remainingProvisional(second).size()==1,"Smaller member set erased an exposed method");engine.finishActivity(second);check(((List<?>)second.completedReport.get("facts")).size()==2,"Same-key override erased provisional member");
  var third=engine.beginActivity("test.C");var map=DexFlow.V.of("object","java.util.HashMap","registry-a");var key=DexFlow.V.literal("java.lang.String","slot");var unknown=DexFlow.V.of("unknown","java.lang.Object","lookup-a");
  third.host.lookupSources.put(unknown.id(),List.of(map,key));third.host.maps.put(map.id(),new HashMap<>(Map.of(CapabilityEngine.mapKey(key),object)));
  var old=fact("unknown",unknown,List.of());var good=fact("test.Bridge",object,List.of(member));
  check(engine.refinesFact(third.host,old,good),"Exact map receiver/key refinement rejected");
  check(!engine.refinesFact(third.host,old,fact("test.Bridge",DexFlow.V.of("object","test.Bridge","object-b"),List.of(member))),"Different registered object merged by site/name");
  var opaque=DexFlow.V.of("unknown","java.lang.Object","opaque");
  check(!engine.refinesFact(third.host,fact("unknown",opaque,List.of()),fact("test.Bridge",opaque,List.of(member))),"Opaque unknown became concrete without provenance");
  var oldRegistry=fact("test.Bridge",object,List.of(member));var newRegistry=fact("test.Bridge",object,List.of(member));oldRegistry.put("kind","message_bridge");newRegistry.put("kind","message_bridge");
  oldRegistry.put("arguments",List.of(DexFlow.V.of("object","test.Registry","registry-1"),object));newRegistry.put("arguments",List.of(DexFlow.V.of("object","test.Registry","registry-2"),object));
  check(!engine.refinesFact(third.host,oldRegistry,newRegistry),"Different registry receivers merged");
  third.host.maps.get(map.id()).put("*",object);check(!engine.refinesFact(third.host,old,good),"Dynamic selector incorrectly treated as exact provenance");
  var capped=engine.beginActivity("test.Cap");capped.phase=1;for(int i=0;i<12001;i++)capped.host.visited.add("context"+i);
  engine.advanceActivity(capped,50_000_000L,1);check(capped.done&&capped.limited,"Context cap reported normal traversal");
  System.out.println("PhaseRetentionFixture PASS: first-phase fact survives finalization; members cannot shrink; exact object/registry provenance; wildcard and different-object negatives; cap status.");
 }
}
