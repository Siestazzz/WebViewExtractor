package org.example;
import java.nio.file.*;import java.util.*;import org.jf.dexlib2.*;
public class CaptureTrace {
 public static void main(String[] args)throws Exception {
  Path p=Files.createTempFile("capture-trace-",".dex");
  try {DexFileFactory.writeDexFile(p.toString(),LifecycleContainerFactoryProbe.buildContainer(args[0]));long deadline=System.nanoTime()+30_000_000_000L;CapabilityIndex i=new CapabilityIndex();i.read(p,deadline);ApkInventory a=new ApkInventory();a.activities.add("container.Host");CapabilityEngine e=new CapabilityEngine(i,a,deadline);
   System.out.println("bindingObjects="+i.bindingObjects);System.out.println("relevant="+i.relevant.stream().sorted().toList());
   Set<String> printed=new HashSet<>();e.checkpoint=()->{var h=e.currentHost;if(h!=null)for(var x:h.heap.entrySet())if(x.getKey().contains("owner")||x.getKey().contains("->root:")){String s=x.toString();if(printed.add(s))System.out.println("HEAP "+s);}};
   e.analyzeActivity("container.Host");System.out.println("FACTS "+e.activities);System.out.println("DIAG "+e.diagnostics);
  }finally{Files.deleteIfExists(p);}
 }
}
