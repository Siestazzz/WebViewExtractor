package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;

/** Real DEX probe: a bridge exists only in a constructor suppressed on phase-two replay. */
public final class SchedulerV3ConstructorRetentionProbe {
  static final String A="Lprobe/AppActivity;", C="Lprobe/OneShotComponent;", B="Lprobe/Bridge;";
  static final String W="Landroid/webkit/WebView;", O="Ljava/lang/Object;", S="Ljava/lang/String;";

  static ImmutableMethod method(String owner,String name,List<String> params,int flags,int regs,List<Instruction> ins,boolean js){
    var ps=params.stream().map(t->new ImmutableMethodParameter(t,Set.of(),null)).toList();
    var annotations=js?Set.of(new ImmutableAnnotation(1,"Landroid/webkit/JavascriptInterface;",Set.of())):Set.<ImmutableAnnotation>of();
    return new ImmutableMethod(owner,name,ps,"V",flags,annotations,Set.of(),new ImmutableMethodImplementation(regs,ins,List.of(),List.of()));
  }
  static ImmutableClassDef clazz(String type,String parent,ImmutableMethod... methods){
    return new ImmutableClassDef(type,1,parent,List.of(),null,Set.of(),List.of(),List.of(methods));
  }
  static Instruction invoke(Opcode op,String owner,String name,List<String> params,String ret,int... regs){
    int[] r=Arrays.copyOf(regs,5);return new ImmutableInstruction35c(op,regs.length,r[0],r[1],r[2],r[3],r[4],new ImmutableMethodReference(owner,name,params,ret));
  }
  static Instruction make(int r,String type){return new ImmutableInstruction21c(Opcode.NEW_INSTANCE,r,new ImmutableTypeReference(type));}
  static Instruction string(int r,String value){return new ImmutableInstruction21c(Opcode.CONST_STRING,r,new ImmutableStringReference(value));}
  static Instruction end(){return new ImmutableInstruction10x(Opcode.RETURN_VOID);}
  static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}

  public static void main(String[] args)throws Exception{
    // onCreate(Bundle): new OneShotComponent(this). The component is deliberately not stored.
    var entry=method(A,"onCreate",List.of("Landroid/os/Bundle;"),1,3,List.of(
      make(0,C),invoke(Opcode.INVOKE_DIRECT,C,"<init>",List.of("Landroid/content/Context;"),"V",0,1),end()),false);
    var componentCtor=method(C,"<init>",List.of("Landroid/content/Context;"),1,5,List.of(
      make(0,W),invoke(Opcode.INVOKE_DIRECT,W,"<init>",List.of("Landroid/content/Context;"),"V",0,4),
      make(1,B),invoke(Opcode.INVOKE_DIRECT,B,"<init>",List.of(),"V",1),string(2,"constructorOnly"),
      invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(O,S),"V",0,1,2),end()),false);
    var bridgeCtor=method(B,"<init>",List.of(),1,1,List.of(end()),false);
    var exposed=method(B,"exposed",List.of(),1,1,List.of(end()),true);
    var dex=new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(C,O,componentCtor),clazz(B,O,bridgeCtor,exposed)));
    Path path=Files.createTempFile("scheduler-v3-constructor-retention-",".dex");
    try{
      DexFileFactory.writeDexFile(path.toString(),dex);
      long deadline=System.nanoTime()+20_000_000_000L;
      var index=new CapabilityIndex();index.read(path,deadline);
      var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("probe.AppActivity");
      var engine=new CapabilityEngine(index,apk,deadline);
      // Drive genuine DEX analysis through phase one. At the phase boundary, deliberately
      // suppress phase-two jobs to model a constructor path that cannot be re-derived.
      var state=engine.beginActivity("probe.AppActivity");
      while(state.phase==0&&!state.done)engine.advanceActivity(state,1_000_000_000L,1);
      require(state.phase==1&&!state.previousFacts.isEmpty(),"DEX phase one did not produce the constructor bridge");
      state.host.queue.clear();state.host.pending.clear();engine.finishActivity(state);
      require(engine.activities.size()==1,"constructor-only bridge host disappeared");
      @SuppressWarnings("unchecked") var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
      var matches=facts.stream().filter(f->"constructorOnly".equals(f.get("registration_name"))).toList();
      require(matches.size()==1,"expected one retained constructor bridge, got "+matches.size());
      var fact=matches.get(0);
      require("probe.Bridge".equals(fact.get("implementation")),"concrete bridge implementation lost: "+fact);
      require("previous_phase_provisional".equals(fact.get("analysis_stage")),"fixture no longer exercises phase-only retention: "+fact.get("analysis_stage"));
      require("candidate".equals(fact.get("binding_status")),"provisional fact status not conservative: "+fact.get("binding_status"));
      require(String.valueOf(fact.get("members")).contains("Lprobe/Bridge;->exposed()V"),"annotated member lost: "+fact.get("members"));
      require(state.done&&state.provisionalFacts==1,"phase accounting mismatch: done="+state.done+", provisional="+state.provisionalFacts);
      System.out.println("constructor_only_retained=true");
      System.out.println("analysis_stage="+fact.get("analysis_stage"));
      System.out.println("binding_status="+fact.get("binding_status"));
      System.out.println("implementation="+fact.get("implementation"));
      System.out.println("member=Lprobe/Bridge;->exposed()V");
      System.out.println("phase_contexts="+engine.coverage(List.of("probe.AppActivity")).get(0).get("phase_contexts"));
    }finally{Files.deleteIfExists(path);}
  }
}
