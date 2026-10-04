package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.DexFlow.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Allocation snapshots, conservative temporal refusal and no global factory roots. */
final class AllocationCaptureFixture {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static void run()throws Exception{
  for(String route:List.of("direct-factory-call-valueparam","direct-factory-call-nodefield")){
   Path dex=Files.createTempFile("allocation-capture-time-",".dex");try{
    DexFileFactory.writeDexFile(dex.toString(),LifecycleContainerFactoryProbe.buildContainer(route));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);Set<String> relevant=Set.copyOf(index.relevant);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);var host=engine.new Host("container.Host");var method=index.resolve("Lcontainer/Host;->onCreate(Landroid/os/Bundle;)V");var ctor=engine.flow.summary(method).calls().stream().filter(call->call.method().startsWith("Lcontainer/CapturedFactory;-><init>")).findFirst().orElseThrow();
    V first=V.of("host","container.Host","activity:first"),second=V.of("host","container.Host","activity:second"),old=V.of("object","container.Content","old"),next=V.of("object","container.Content","next");String root="Lcontainer/Host;->root:Lcontainer/ContentContract;",captured="Lcontainer/CapturedFactory;->capturedRoot:Lcontainer/ContentContract;";
    host.heap.put(CapabilityEngine.heapKey(root,first),old);host.heap.put(CapabilityEngine.heapKey(root,second),next);
    var job1=new CapabilityEngine.Job(method,List.of(first,UNKNOWN),List.of("allocation"),false);var job2=new CapabilityEngine.Job(method,List.of(second,UNKNOWN),List.of("allocation"),false);
    V object1=engine.eval(ctor.args().get(0),job1,host,0,new HashSet<>()),object2=engine.eval(ctor.args().get(0),job2,host,0,new HashSet<>());
    host.heap.put(CapabilityEngine.heapKey(root,first),next);host.heap.put(CapabilityEngine.heapKey(root,second),old);
    require(engine.eval(expr("field","container.ContentContract",captured,List.of(object1)),job1,host,0,new HashSet<>()).equals(old),"Captured argument/body field changed after allocation: "+route);
    require(engine.eval(expr("field","container.ContentContract",captured,List.of(object2)),job2,host,0,new HashSet<>()).equals(next),"Two capture receivers merged: "+route);
    var unresolved=engine.new Host("unknown");V unknownFactory=engine.eval(ctor.args().get(0),job1,unresolved,0,new HashSet<>());unresolved.heap.put(CapabilityEngine.heapKey(root,first),next);
    require(engine.eval(expr("field","container.ContentContract",captured,List.of(unknownFactory)),job1,unresolved,0,new HashSet<>()).kind().equals("unknown"),"Unknown constructor input was refreshed from a future write");
    var capped=engine.new Host("budget");for(int i=0;i<12000;i++)capped.allocationCaptures.add("budget"+i);engine.eval(ctor.args().get(0),job1,capped,0,new HashSet<>());require(capped.gaps.contains("constructor_capture_object_budget"),"Allocation capture cap not diagnosed");
    require(index.relevant.equals(relevant),"Allocation capture modified global relevance");
   }finally{Files.deleteIfExists(dex);}
  }
  matrix();
  System.out.println("AllocationCaptureFixture PASS: allocation-time actual value/body-field snapshots, two receiver isolation, frozen unknown,12000 bound, unchanged global relevance and ten direct factory capability routes.");
 }
 static void matrix()throws Exception{
  for(String route:List.of("direct-node","direct-factory-call","direct-factory-call-unconditional","direct-factory-call-new","direct-factory-call-nodefield","direct-factory-call-valueparam","direct-factory-concrete","direct-factory-new","direct-unconditional","direct","installed","late-setter","uninstalled","wrong-selector")){
   Path dex=Files.createTempFile("allocation-direct-route-",".dex");try{
    DexFileFactory.writeDexFile(dex.toString(),LifecycleContainerFactoryProbe.buildContainer(route));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var apk=new ApkInventory();apk.activities.add("container.Host");var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("container.Host");Set<String> kinds=new HashSet<>();for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)kinds.add(String.valueOf(fact.get("kind")));}
    require((route.equals("wrong-selector")||route.equals("uninstalled"))?Collections.disjoint(kinds,Set.of("bridge","setting","callback")):kinds.containsAll(Set.of("bridge","setting","callback")),"Factory route changed: "+route+" "+kinds);
   }finally{Files.deleteIfExists(dex);}
  }
 }
}
