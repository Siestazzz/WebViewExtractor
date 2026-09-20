package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;

/** Executable regression fixtures, no network or test-framework dependency. */
public final class CapabilitySelfTest {
 static final String A="Ltest/AppActivity;",W="Landroid/webkit/WebView;",S="Landroid/webkit/WebSettings;",B="Ltest/Bridge;",C="Ltest/Client;",H="Ltest/Helpers;";
 static ImmutableMethod method(String owner,String name,List<String> params,int flags,int regs,List<Instruction> ins,boolean annotated){
  var ps=params.stream().map(t->new ImmutableMethodParameter(t,Set.of(),null)).toList();
  var annotations=annotated?Set.of(new ImmutableAnnotation(1,"Landroid/webkit/JavascriptInterface;",Set.of())):Set.<ImmutableAnnotation>of();
  return new ImmutableMethod(owner,name,ps,"V",flags,annotations,Set.of(),new ImmutableMethodImplementation(regs,ins,List.of(),List.of()));
 }
 static ImmutableClassDef clazz(String type,String parent,ImmutableMethod... methods){return new ImmutableClassDef(type,1,parent,List.of(),null,Set.of(),List.of(),List.of(methods));}
 static Instruction invoke(Opcode opcode,String owner,String name,List<String> params,String ret,int... regs){
  int[] r=Arrays.copyOf(regs,5);return new ImmutableInstruction35c(opcode,regs.length,r[0],r[1],r[2],r[3],r[4],new ImmutableMethodReference(owner,name,params,ret));
 }
 static Instruction make(int reg,String type){return new ImmutableInstruction21c(Opcode.NEW_INSTANCE,reg,new ImmutableTypeReference(type));}
 static Instruction str(int reg,String text){return new ImmutableInstruction21c(Opcode.CONST_STRING,reg,new ImmutableStringReference(text));}
 static Instruction end(){return new ImmutableInstruction10x(Opcode.RETURN_VOID);}
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
 public static void main(String[] args)throws Exception {
  var helper=method(H,"configure",List.of(W,B),9,4,List.of(
   invoke(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,2),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),
   new ImmutableInstruction11n(Opcode.CONST_4,1,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",0,1),
   str(1,"native"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,3,1),end()),false);
  var entry=method(A,"onCreate",List.of(),1,6,List.of(make(0,W),make(1,W),make(2,B),make(3,C),
   invoke(Opcode.INVOKE_STATIC,H,"configure",List.of(W,B),"V",0,2),
   invoke(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V",1,3),end()),false);
  var bridge=method(B,"exposed",List.of("Ljava/lang/String;"),1,2,List.of(end()),true);
  var hidden=method(B,"hidden",List.of(),1,1,List.of(end()),false);
  var callback=method(C,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false);
  var dex=new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(H,"Ljava/lang/Object;",helper),clazz(B,"Ljava/lang/Object;",bridge,hidden),clazz(C,"Landroid/webkit/WebViewClient;",callback)));
  Path file=Files.createTempFile("wv-regression-",".dex");DexFileFactory.writeDexFile(file.toString(),dex);
  try {
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("test.AppActivity");
   var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Activity missing");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   Map<String,Object> bf=facts.stream().filter(f->f.get("kind").equals("bridge")).findFirst().orElseThrow();
   Map<String,Object> cf=facts.stream().filter(f->f.get("kind").equals("callback")).findFirst().orElseThrow();
   Map<String,Object> sf=facts.stream().filter(f->f.get("kind").equals("setting")).findFirst().orElseThrow();
   check(!bf.get("webview").equals(cf.get("webview")),"Two WebViews merged");
   check(bf.get("webview").equals(sf.get("webview")),"Settings lost receiver identity");
   check(bf.get("registration_name").equals("native"),"Bridge name lost through helper");
   check(bf.get("implementation").equals("test.Bridge"),"Bridge type lost through helper");
   check(((List<?>)bf.get("members")).size()==1,"Annotated bridge exposure wrong");
   check(((List<?>)cf.get("members")).size()==1,"Callback signature missing");
   check(sf.get("value").equals("true"),"Setting value missing");
   // Previous scorer retained stale constants after move; this must resolve false.
   var moves=method(H,"moves",List.of(),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction11n(Opcode.CONST_4,1,0),new ImmutableInstruction12x(Opcode.MOVE,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",2,0),end()),false);
   var flow=new DexFlow(idx,deadline);var summary=flow.summary(moves);check(summary.calls().get(0).args().get(1).literal().equals("0"),"Stale constant survived move");
   // Arithmetic writes must invalidate a previous constant.
   var overwrite=method(H,"overwrite",List.of(),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction12x(Opcode.NEG_INT,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",2,0),end()),false);
   check(flow.summary(overwrite).calls().get(0).args().get(1).kind().equals("unknown"),"Arithmetic overwrite retained constant");
   check(idx.kind(new ImmutableMethodReference("Ltest/Unrelated;","setJavaScriptEnabled",List.of("Z"),"V"))==null,"Unrelated method matched by name");
   System.out.println("PASS: separate WebViews, helper binding, settings identity, bridge annotation, callbacks, constant overwrite, API owner");
  }finally{Files.deleteIfExists(file);}
 }
}
