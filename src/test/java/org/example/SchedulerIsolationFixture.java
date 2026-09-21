package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.CapabilitySelfTest.*;
final class SchedulerIsolationFixture {
 static void run()throws Exception{
  String h="Lslice/Helper;",b="Lslice/Bridge;",first="Lslice/First;",second="Lslice/Second;",empty="Lslice/Empty;";
  var configure=method(h,"configure",List.of(W,b,"Ljava/lang/String;"),9,3,List.of(invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var classes=new ArrayList<ImmutableClassDef>();
  for(String host:List.of(first,second))classes.add(clazz(host,"Landroid/app/Activity;",method(host,"onCreate",List.of(),1,4,List.of(make(0,W),make(1,b),str(2,host),invoke(Opcode.INVOKE_STATIC,h,"configure",List.of(W,b,"Ljava/lang/String;"),"V",0,1,2),end()),false)));
  classes.add(clazz(empty,"Landroid/app/Activity;"));classes.add(clazz(h,"Ljava/lang/Object;",configure));classes.add(clazz(b,"Ljava/lang/Object;",method(b,"expose",List.of(),1,1,List.of(end()),true)));
  Path file=Files.createTempFile("scheduler-isolation-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var apk=new ApkInventory();apk.activities.addAll(List.of("slice.First","slice.Second","slice.Empty"));
   var sequential=new CapabilityEngine(idx,apk,deadline);for(String host:apk.activities)sequential.analyzeActivity(host);
   var sliced=new CapabilityEngine(idx,apk,deadline);var pending=new ArrayDeque<CapabilityEngine.ActivityState>();for(String host:apk.activities)pending.add(sliced.beginActivity(host));
   int slices=0;while(!pending.isEmpty()){var state=pending.remove();sliced.advanceActivity(state,50_000_000L,1);if(!state.done)pending.add(state);check(++slices<100,"Round robin failed to converge");}
   Map<String,Object> expected=new TreeMap<>(),actual=new TreeMap<>();sequential.activities.forEach(a->expected.put((String)a.get("activity"),a));sliced.activities.forEach(a->actual.put((String)a.get("activity"),a));check(expected.equals(actual),"Interleaving hosts changed bindings");
   check(actual.size()==2,"Empty host emitted capability facts");check(sliced.coverage(new ArrayList<>(apk.activities)).stream().allMatch(c->c.get("status").equals("traversal_finished")),"Empty or resumed host not marked finished");
   check(((List<?>)sliced.report("test","partial",Map.of()).get("activities")).size()==2,"Checkpoint duplicated a finished host");
   System.out.println("SchedulerIsolationFixture PASS: interleaved hosts match sequential results; empty host coverage; no duplicate snapshots.");
  }finally{Files.deleteIfExists(file);}
 }
}
