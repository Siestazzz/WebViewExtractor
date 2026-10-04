package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import static org.example.DexFlow.*;
/** Actual transaction activation and heap-sensitive callback relevance. */
final class FragmentActivationFixture {
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 static void run()throws Exception{
  for(String variant:List.of("fragment-create","fragment-activity-created","fragment-uninstalled")){
   Path path=Files.createTempFile("fragment-activation-",".dex");try{
    DexFileFactory.writeDexFile(path.toString(),LifecycleLayerBoundaryProbe.layer(variant));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(path,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);engine.analyzeActivity("container.Host");Set<String> kinds=new HashSet<>();
    for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)kinds.add(String.valueOf(fact.get("kind")));}
    require(variant.equals("fragment-uninstalled")?Collections.disjoint(kinds,Set.of("bridge","setting","callback")):kinds.containsAll(Set.of("bridge","setting","callback")),"Fragment activation boundary changed: "+variant+" "+kinds);
   }finally{Files.deleteIfExists(path);}
  }
  Path path=Files.createTempFile("fragment-relevance-",".dex");try{
   DexFileFactory.writeDexFile(path.toString(),LifecycleContainerFactoryProbe.buildContainer("installed"));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(path,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);var host=engine.new Host("container.Host");var callback=index.resolve("Lcontainer/LifecycleFragment;->onActivityCreated(Landroid/os/Bundle;)V");
   V first=V.of("object","container.LifecycleFragment","first"),second=V.of("object","container.LifecycleFragment","second"),delegate=V.of("object","container.Dispatcher","delegate");
   require(!engine.relevantOnReceiver(callback,first,host),"Unknown delegate fabricated callback relevance");
   host.heap.put(CapabilityEngine.heapKey("Lcontainer/LifecycleFragment;->delegate:Lcontainer/ReadyCallback;",first),delegate);
   require(engine.relevantOnReceiver(callback,first,host),"Negative type cache hid later actual delegate");
   require(!engine.relevantOnReceiver(callback,second,host),"Same-class receiver inherited another object's delegate relevance");
  }finally{Files.deleteIfExists(path);}
  System.out.println("FragmentActivationFixture PASS: installed onCreate/onActivityCreated, uninstalled direct-content negative and changing/two-instance delegate relevance.");
 }
}
