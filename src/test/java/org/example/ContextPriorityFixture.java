package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

final class ContextPriorityFixture {
 static void run()throws Exception {
  var first=method(H,"first",List.of(),9,1,List.of(end()),false);
  var second=method(H,"second",List.of(),9,1,List.of(end()),false);
  List<String> expected=null;
  for(int pass=0;pass<2;pass++){
   var queue=new ContextQueue();Set<String> added=new HashSet<>();
   for(int n=0;n<40;n++){
    String id="priority:"+n;added.add(id);
    queue.add(new CapabilityEngine.Job(n%2==0?first:second,List.of(DexFlow.V.of("object","test.Value",id)),List.of(),false),true);
   }
   for(int n=0;n<8;n++){
    String id="ordinary:"+n;added.add(id);
    queue.add(new CapabilityEngine.Job(n%2==0?first:second,List.of(DexFlow.V.of("object","test.Value",id)),List.of(),false));
   }
   check(queue.prioritySize()==40&&queue.ordinarySize()==8&&queue.size()==48,"Lane accounting differs");
   List<String> observed=new ArrayList<>();Set<String> removed=new HashSet<>();int priorityRun=0;
   while(!queue.isEmpty()){
    boolean ordinaryWaiting=queue.ordinarySize()>0;
    var job=queue.remove();String id=job.args().get(0).id();observed.add(id);
    check(removed.add(id),"Priority scheduling repeated a task");
    if(id.startsWith("ordinary:"))priorityRun=0;
    else if(ordinaryWaiting)check(++priorityRun<=3,"Ordinary work starved behind priority tasks");
    check(queue.size()==queue.prioritySize()+queue.ordinarySize(),"Remaining lane counts differ");
   }
   check(removed.equals(added),"Priority scheduling dropped a task");
   check(observed.subList(0,4).equals(List.of("priority:0","priority:1","priority:2","ordinary:0")),"Bounded preference or method rotation differs");
   if(expected==null)expected=observed;else check(expected.equals(observed),"Scheduling is not deterministic");
   queue.add(new CapabilityEngine.Job(first,List.of(),List.of(),false),true);queue.clear();
   check(queue.size()==0&&queue.prioritySize()==0&&queue.ordinarySize()==0,"Clear retained lane tasks");
  }
  String helper="Lpriority/Helper;",bridge="Lpriority/Bridge;";
  var configure=method(helper,"configure",List.of(W,bridge,"Ljava/lang/String;"),9,3,List.of(
   invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var ordinary=method(helper,"ordinary",List.of(),9,1,List.of(end()),false);
  var configureSettings=method(helper,"configureSettings",List.of(W),9,2,List.of(
   invoke(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),
   new ImmutableInstruction11n(Opcode.CONST_4,1,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",0,1),end()),false);
  var field=method(helper,"fieldSeed",List.of(),1,3,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,2,new ImmutableFieldReference(helper,"view",W)),str(1,"test"),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",0,1),end()),false);
  Path file=Files.createTempFile("priority-isolation-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(helper,"Ljava/lang/Object;",configure,ordinary,configureSettings,field),clazz(bridge,"Ljava/lang/Object;",method(bridge,"expose",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var host=engine.new Host("test.PriorityHost");
   var object=DexFlow.V.of("object","priority.Bridge","bridge");
   var unknownArgs=List.of(DexFlow.V.of("unknown","android.webkit.WebView","entry_parameter"),object,DexFlow.V.literal("java.lang.String","unknown"));
   check(!engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(configure,unknownArgs,List.of(),false)),"Unknown capability receiver gained priority");
   var fieldArgs=List.of(DexFlow.V.of("field_object","android.webkit.WebView","unresolved-field"),object,DexFlow.V.literal("java.lang.String","field"));
   check(!engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(configure,fieldArgs,List.of(),false)),"Abstract field receiver gained priority");
   var knownView=DexFlow.V.of("object","android.webkit.WebView","settings-view");
   check(engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(configureSettings,List.of(knownView),List.of(),false)),"Known WebView-derived settings did not gain priority");
   check(!engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(configureSettings,List.of(unknownArgs.get(0)),List.of(),false)),"Unknown WebView-derived settings gained priority");
   var mixed=new ArrayList<>(unknownArgs);mixed.set(0,DexFlow.union(knownView,unknownArgs.get(0)));
   check(engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(configure,mixed,List.of(),false)),"Concrete union branch lost preference");
   var owner=DexFlow.V.of("object","priority.Helper","holder");
   check(!engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(field,List.of(owner),List.of(),false)),"Unbound field gained priority");
   engine.applyWrite(host,helper+"->view:"+W,owner,knownView);
   check(engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(field,List.of(owner),List.of(),false)),"Known stored WebView field lost preference");
   var malformed=new ArrayList<>(unknownArgs);malformed.set(0,DexFlow.V.of("param","android.webkit.WebView","malformed"));
   check(!engine.concreteCapabilitySeed(host,new CapabilityEngine.Job(configure,malformed,List.of(),false))&&host.gaps.stream().anyMatch(g->g.startsWith("priority_hint_failed:")),"Hint failure did not fall back to ordinary scheduling");
   engine.enqueue(host,ordinary,List.of(),List.of(),false);
   for(String id:List.of("first-view","second-view"))engine.enqueue(host,configure,List.of(DexFlow.V.of("object","android.webkit.WebView",id),object,DexFlow.V.literal("java.lang.String",id)),List.of(),false);
   check(host.queue.prioritySize()==2&&host.queue.ordinarySize()==1,"Concrete seeds not classified separately");
   while(!host.queue.isEmpty())engine.processJob(host);
   check(host.facts.size()==2,"Priority processing dropped or duplicated capabilities");
   for(String id:List.of("first-view","second-view")){
    var fact=host.facts.values().stream().filter(f->id.equals(f.get("registration_name"))).findFirst().orElseThrow();
    check(id.equals(((Map<?,?>)fact.get("webview")).get("id")),"Priority scheduling mixed WebView bindings");
   }
   check(host.visited.size()==3&&host.pending.isEmpty(),"Priority processing lost context bookkeeping");
  }finally{Files.deleteIfExists(file);}
  System.out.println("ContextPriorityFixture PASS: deterministic 3:1 preference; method rotation; ordinary fairness; exact task retention; concrete-only seeds; two WebView isolation.");
 }
}
