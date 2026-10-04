package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Constant discriminators select callback behavior without mixing unrelated owners. */
final class SwitchOwnershipFixture {
 public static void main(String[] args)throws Exception{run();}
 static void run()throws Exception {
  String runner="Lswitchtest/Runner;",helper="Lswitchtest/Helper;",bridge="Lswitchtest/Bridge;";
  var target=new ImmutableField(runner,"target",W,0x11,null,Set.of(),Set.of());
  var tag=new ImmutableField(runner,"selector","I",0x11,null,Set.of(),Set.of());
  var ctor=method(runner,"<init>",List.of(W,"I"),1,3,List.of(
   new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(runner,"target",W)),
   new ImmutableInstruction22c(Opcode.IPUT,2,0,new ImmutableFieldReference(runner,"selector","I")),end()),false);
  var callback=method(runner,"run",List.of(),1,3,List.of(
   new ImmutableInstruction22c(Opcode.IGET_OBJECT,1,2,new ImmutableFieldReference(runner,"target",W)),
   new ImmutableInstruction22c(Opcode.IGET,0,2,new ImmutableFieldReference(runner,"selector","I")),
   new ImmutableInstruction31t(Opcode.PACKED_SWITCH,0,16),
   invoke(Opcode.INVOKE_STATIC,helper,"defaultBridge",List.of(W),"V",1),end(),
   invoke(Opcode.INVOKE_STATIC,helper,"safe",List.of(),"V"),end(),
   invoke(Opcode.INVOKE_STATIC,helper,"caseBridge",List.of(W),"V",1),end(),
   new ImmutableInstruction10x(Opcode.NOP),
   new ImmutablePackedSwitchPayload(List.of(new ImmutableSwitchElement(0,7),new ImmutableSwitchElement(1,11)))),false);
  var safe=method(helper,"safe",List.of(),9,1,List.of(end()),false);
  List<ImmutableMethod> helpers=new ArrayList<>(List.of(safe));
  for(String name:List.of("defaultBridge","caseBridge"))helpers.add(method(helper,name,List.of(W),9,3,List.of(make(0,bridge),str(1,name),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),end()),false));
  List<ImmutableClassDef> classes=new ArrayList<>();
  classes.add(new ImmutableClassDef(runner,1,"Ljava/lang/Object;",List.of("Ljava/lang/Runnable;"),null,Set.of(),List.of(target,tag),List.of(ctor,callback)));
  classes.add(clazz(helper,"Ljava/lang/Object;",helpers.toArray(ImmutableMethod[]::new)));
  classes.add(clazz(bridge,"Ljava/lang/Object;",method(bridge,"expose",List.of(),1,1,List.of(end()),true)));
  for(int discriminator:List.of(0,1,9)){
   String activity="Lswitchtest/Host"+discriminator+";";
   var entry=method(activity,"onCreate",List.of(),1,5,List.of(make(0,W),make(1,runner),make(2,"Landroid/os/Handler;"),new ImmutableInstruction11n(Opcode.CONST_4,3,discriminator==9?7:discriminator),
    invoke(Opcode.INVOKE_DIRECT,runner,"<init>",List.of(W,"I"),"V",1,0,3),
    invoke(Opcode.INVOKE_VIRTUAL,"Landroid/os/Handler;","post",List.of("Ljava/lang/Runnable;"),"Z",2,1),end()),false);
   classes.add(clazz(activity,"Landroid/app/Activity;",entry));
  }
  Path file=Files.createTempFile("switch-ownership-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+30_000_000_000L;
   var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);
   for(int discriminator:List.of(0,1,9)){
    var state=engine.beginActivity("switchtest.Host"+discriminator);while(!state.done)engine.advanceActivity(state,1_000_000_000L,100);
    var facts=(List<?>)engine.stateReport(state).get("facts");
    if(discriminator==0)check(facts.isEmpty(),"Safe callback acquired another switch arm's bridge: "+facts);
    else check(facts.size()==1&&facts.toString().contains(discriminator==1?"caseBridge":"defaultBridge")&&!facts.toString().contains(discriminator==1?"defaultBridge":"caseBridge"),"Selected callback lost its bridge or acquired another arm: "+facts);
   }
   var host=engine.new Host("switchtest.Conservative");
   var receiver=DexFlow.V.of("object","switchtest.Runner","callback");
   var job=new CapabilityEngine.Job(callback,List.of(receiver),List.of(),true);
   String selectorField=runner+"->selector:I";
   check(engine.flow.summary(callback,v->engine.guardValue(v,job,host,0)).calls().size()==3,"Unknown selector did not retain every arm");
   engine.applyWrite(host,selectorField,receiver,DexFlow.V.literal("number","0"));
   check(engine.flow.summary(callback,v->engine.guardValue(v,job,host,0)).calls().size()==1,"Known final selector did not specialize");
   idx.finalFields.remove(selectorField);
   check(engine.flow.summary(callback,v->engine.guardValue(v,job,host,0)).calls().size()==3,"Mutable observed field unsafely pruned switch arms");
   idx.finalFields.add(selectorField);
   engine.applyWrite(host,selectorField,receiver,DexFlow.V.literal("number","1"));
   check(engine.flow.summary(callback,v->engine.guardValue(v,job,host,0)).calls().size()==3,"Weakly joined final-field values unsafely pruned switch arms");
   for(boolean sparse:List.of(false,true)){
    String name=sparse?"sparseParameter":"packedParameter";
    var parameterSwitch=method(helper,name,List.of("I",W),9,2,List.of(
     new ImmutableInstruction31t(sparse?Opcode.SPARSE_SWITCH:Opcode.PACKED_SWITCH,0,16),
     invoke(Opcode.INVOKE_STATIC,helper,"defaultBridge",List.of(W),"V",1),end(),
     invoke(Opcode.INVOKE_STATIC,helper,"safe",List.of(),"V"),end(),
     invoke(Opcode.INVOKE_STATIC,helper,"caseBridge",List.of(W),"V",1),end(),
     new ImmutableInstruction10x(Opcode.NOP),
     sparse?new ImmutableSparseSwitchPayload(List.of(new ImmutableSwitchElement(0,7),new ImmutableSwitchElement(7,11))):new ImmutablePackedSwitchPayload(List.of(new ImmutableSwitchElement(0,7),new ImmutableSwitchElement(1,11)))),false);
    var flow=new DexFlow(idx,deadline);
    check(flow.summary(parameterSwitch).calls().size()==3,"Unknown parameter switch lost an arm");
    for(int value:List.of(0,sparse?7:1,9)){
     var summary=flow.summary(parameterSwitch,v->v.kind().equals("param")&&v.id().equals("0")?DexFlow.V.literal("number",String.valueOf(value)):v);
     String selected=value==0?"safe":value==9?"defaultBridge":"caseBridge";
     check(summary.calls().size()==1&&summary.calls().get(0).method().contains("->"+selected+"("),"Known "+name+" selector chose wrong/default extra arm");
    }
    var exceptional=new ImmutableMethod(helper,name+"WithCatch",parameterSwitch.getParameters(),"V",9,Set.of(),Set.of(),new ImmutableMethodImplementation(2,parameterSwitch.getImplementation().getInstructions(),List.of(new ImmutableTryBlock(0,3,List.of(new ImmutableExceptionHandler("Ljava/lang/Exception;",3)))),List.of()));
    var withCatch=flow.summary(exceptional,v->v.kind().equals("param")&&v.id().equals("0")?DexFlow.V.literal("number","0"):v);
    check(withCatch.calls().size()==2&&withCatch.calls().stream().noneMatch(c->c.method().contains("->caseBridge(")),"Selector refinement lost exception edge or entered another case");
    var union=DexFlow.union(DexFlow.V.literal("number","0"),DexFlow.V.literal("number","1"));
    check(flow.summary(parameterSwitch,v->v.kind().equals("param")&&v.id().equals("0")?union:v).calls().size()==3,"Union parameter unsafely pruned switch");
    check(flow.summary(parameterSwitch,v->v.kind().equals("param")&&v.id().equals("0")?DexFlow.V.literal("number","2147483648"):v).calls().size()==3,"Out-of-range selector treated as a proven int");
    for(int value=10;value<40;value++){
     int discriminator=value;
     flow.summary(parameterSwitch,v->v.kind().equals("param")&&v.id().equals("0")?DexFlow.V.literal("number",String.valueOf(discriminator)):v);
    }
    check(flow.refinements.get(CapabilityIndex.key(parameterSwitch)).size()==16,"Refinement budget changed");
    check(flow.summary(parameterSwitch,v->v.kind().equals("param")&&v.id().equals("0")?DexFlow.V.literal("number","99"):v).calls().size()==3,"Exhausted specialization budget lost conservative fallback");
    check(flow.summary(parameterSwitch,v->v.kind().equals("param")&&v.id().equals("0")?DexFlow.V.literal("number","0"):v).calls().size()==1,"Existing specialization not reused after budget exhaustion");
    long diagnostics=idx.diagnostics.stream().filter(d->d.equals("summary_refinement_budget:"+CapabilityIndex.key(parameterSwitch))).count();
    check(diagnostics==1,"Refinement budget diagnostic missing or repeated");
   }
  }finally{Files.deleteIfExists(file);}
  System.out.println("SwitchOwnershipFixture PASS: packed/sparse exact selectors; default isolation; constructor ownership; unknown/mutable/weak-union and budget fallback.");
 }
}
