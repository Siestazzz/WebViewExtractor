package org.example;
import java.nio.file.*;import java.util.*;import org.jf.dexlib2.*;import static org.example.DexFlow.*;
public class ConstructorCaptureTimeProbe {
 public static void main(String[] args)throws Exception{
  Path p=Files.createTempFile("capture-time-",".dex");try{
   DexFileFactory.writeDexFile(p.toString(),LifecycleContainerFactoryProbe.buildContainer(args.length==0?"direct-factory-call-valueparam":"direct-factory-call-nodefield"));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(p,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var h=engine.new Host("container.Host");var m=idx.resolve("Lcontainer/Host;->onCreate(Landroid/os/Bundle;)V");var self=V.of("host","container.Host","activity:container.Host");var job=new CapabilityEngine.Job(m,List.of(self,UNKNOWN),List.of("time-probe"),false);
   var old=V.of("object","container.Content","old-content");var next=V.of("object","container.Content","new-content");String field="Lcontainer/Host;->root:Lcontainer/ContentContract;";h.heap.put(CapabilityEngine.heapKey(field,self),old);
   var ctor=engine.flow.summary(m).calls().stream().filter(c->c.method().equals(args.length==0?"Lcontainer/CapturedFactory;-><init>(Lcontainer/ContentContract;)V":"Lcontainer/CapturedFactory;-><init>(Lcontainer/Host;)V")).findFirst().orElseThrow();var factory=engine.eval(ctor.args().get(0),job,h,0,new HashSet<>());
   h.heap.put(CapabilityEngine.heapKey(field,self),next);
   var captured=engine.eval(expr("field","container.ContentContract","Lcontainer/CapturedFactory;->capturedRoot:Lcontainer/ContentContract;",List.of(factory)),job,h,0,new HashSet<>());
   if(!captured.equals(old))throw new AssertionError("Constructor capture changed after external field overwrite: "+captured);
   System.out.println("PASS captured old object despite later outer field overwrite");
  }finally{Files.deleteIfExists(p);}
 }
}
