package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Two transactions from one manager must not install each other's fragments. */
public final class FragmentTransactionIsolationProbe {
 static final String HOST="Ltransactions/Host;",F="Landroid/app/Fragment;",FM="Landroid/app/FragmentManager;",TX="Landroid/app/FragmentTransaction;",FA="Ltransactions/First;",FB="Ltransactions/Second;",BR="Ltransactions/Bridge;",BUNDLE="Landroid/os/Bundle;",STR="Ljava/lang/String;";
 static ImmutableClassDef fragment(String type,String bridgeName){
  return clazz(type,F,List.of(),List.of(),method(type,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),method(type,"onActivityCreated",List.of(BUNDLE),"V",1,6,List.of(call(Opcode.INVOKE_VIRTUAL,F,"getActivity",List.of(),"Landroid/app/Activity;",4),result(3),make(0,W),call(Opcode.INVOKE_DIRECT,W,"<init>",List.of("Landroid/content/Context;"),"V",0,3),make(1,BR),call(Opcode.INVOKE_DIRECT,BR,"<init>",List.of(),"V",1),str(2,bridgeName),call(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(OBJ,STR),"V",0,1,2),end()),false));
 }
 static ImmutableDexFile build(String mode){
  List<Instruction> c=new ArrayList<>(List.of(call(Opcode.INVOKE_VIRTUAL,"Landroid/app/Activity;","getFragmentManager",List.of(),FM,10),result(0),call(Opcode.INVOKE_VIRTUAL,FM,"beginTransaction",List.of(),TX,0),result(1),make(2,FA),call(Opcode.INVOKE_DIRECT,FA,"<init>",List.of(),"V",2),str(3,"first"),call(Opcode.INVOKE_VIRTUAL,TX,"add",List.of(F,STR),TX,1,2,3)));
  if(mode.equals("two"))c.addAll(List.of(call(Opcode.INVOKE_VIRTUAL,FM,"beginTransaction",List.of(),TX,0),result(4),make(5,FB),call(Opcode.INVOKE_DIRECT,FB,"<init>",List.of(),"V",5),str(3,"second"),call(Opcode.INVOKE_VIRTUAL,TX,"add",List.of(F,STR),TX,4,5,3),call(Opcode.INVOKE_VIRTUAL,TX,"commit",List.of(),"I",4)));
  if(mode.equals("single"))c.add(call(Opcode.INVOKE_VIRTUAL,TX,"commit",List.of(),"I",1));c.add(end());
  return new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(HOST,"Landroid/app/Activity;",List.of(),List.of(),method(HOST,"onCreate",List.of(BUNDLE),"V",1,12,c,false)),fragment(FA,"first"),fragment(FB,"second"),clazz(BR,OBJ,List.of(),List.of(),method(BR,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),method(BR,"expose",List.of(),"V",1,1,List.of(end()),true))));
 }
 static void check(String mode)throws Exception{
  Path dex=Files.createTempFile("fragment-transactions-",".dex");try{
   DexFileFactory.writeDexFile(dex.toString(),build(mode));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var apk=new ApkInventory();apk.activities.add("transactions.Host");var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("transactions.Host");Set<String> names=new TreeSet<>();
   for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)if("bridge".equals(fact.get("kind")))names.add(String.valueOf(fact.get("registration_name")));}
   Set<String> expected=mode.equals("two")?Set.of("second"):mode.equals("single")?Set.of("first"):Set.of();System.out.println(mode+" bridgeNames="+names+" expected="+expected);if(!names.equals(expected))throw new AssertionError("Transaction cross-binding: "+names+" expected="+expected);
  }finally{Files.deleteIfExists(dex);}
 }
 public static void main(String[]args)throws Exception{if(args.length>0)check(args[0]);else for(String m:List.of("single","uncommitted","two"))check(m);}
}
