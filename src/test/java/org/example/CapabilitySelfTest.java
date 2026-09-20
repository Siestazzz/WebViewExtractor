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
  var savedBridge=new ImmutableField(A,"savedBridge",B,1,null,Set.of(),Set.of());
  var prepare=method(A,"prepareBridge",List.of(),1,2,List.of(make(0,B),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(A,"savedBridge",B)),end()),false);
  var entry=method(A,"onCreate",List.of(),1,6,List.of(make(0,W),make(1,W),invoke(Opcode.INVOKE_VIRTUAL,A,"prepareBridge",List.of(),"V",5),new ImmutableInstruction22c(Opcode.IGET_OBJECT,2,5,new ImmutableFieldReference(A,"savedBridge",B)),make(3,C),
   invoke(Opcode.INVOKE_STATIC,H,"configure",List.of(W,B),"V",0,2),
   invoke(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V",1,3),end()),false);
  var bridge=method(B,"exposed",List.of("Ljava/lang/String;"),1,2,List.of(end()),true);
  var tagField=new ImmutableField(B,"TAG","Ljava/lang/String;",1,null,Set.of(),Set.of());
  var tagInit=method(B,"<init>",List.of(),1,2,List.of(str(0,"instanceTag"),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(B,"TAG","Ljava/lang/String;")),end()),false);
  var hidden=method(B,"hidden",List.of(),1,1,List.of(end()),false);
  var callback=method(C,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(invoke(Opcode.INVOKE_SUPER,"Ltest/ParentClient;","onPageFinished",List.of(W,"Ljava/lang/String;"),"V",0,1,2),end()),false);
  var parentCallback=method("Ltest/ParentClient;","onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false);
  var parentShadowed=method("Ltest/ParentClient;","onLoadResource",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false);
  var shadow=method(C,"onLoadResource",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false);
  var dex=new ImmutableDexFile(Opcodes.getDefault(),List.of(new ImmutableClassDef(A,1,"Landroid/app/Activity;",List.of(),null,Set.of(),List.of(savedBridge),List.of(entry,prepare)),clazz(H,"Ljava/lang/Object;",helper),new ImmutableClassDef(B,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(tagField),List.of(bridge,hidden,tagInit)),clazz(C,"Ltest/ParentClient;",callback,shadow),clazz("Ltest/ParentClient;","Landroid/webkit/WebViewClient;",parentCallback,parentShadowed)));
  Path file=Files.createTempFile("wv-regression-",".dex");DexFileFactory.writeDexFile(file.toString(),dex);
  try {
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   check(idx.relevant.contains(CapabilityIndex.key(prepare)),"Registration field-writer helper was excluded");
   var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("test.AppActivity");
   var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Activity missing");
   check(engine.staticConstant("test.Bridge","TAG").literal().equals("instanceTag"),"Reflective instance TAG constructor constant missing");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   Map<String,Object> bf=facts.stream().filter(f->f.get("kind").equals("bridge")).findFirst().orElseThrow();
   Map<String,Object> cf=facts.stream().filter(f->f.get("kind").equals("callback")).findFirst().orElseThrow();
   Map<String,Object> sf=facts.stream().filter(f->f.get("kind").equals("setting")).findFirst().orElseThrow();
   check(!bf.get("webview").equals(cf.get("webview")),"Two WebViews merged");
   check(bf.get("webview").equals(sf.get("webview")),"Settings lost receiver identity");
   check(bf.get("registration_name").equals("native"),"Bridge name lost through helper");
   check(bf.get("implementation").equals("test.Bridge"),"Bridge type lost through helper");
   check(((List<?>)bf.get("members")).size()==1,"Annotated bridge exposure wrong");
   check(((List<?>)cf.get("members")).size()==3,"Explicit super callback missing or shadowed ancestor incorrectly included");
   check(!cf.get("members").toString().contains("ParentClient;->onLoadResource"),"Uncalled ancestor callback counted");
   check(sf.get("value").equals("true"),"Setting value missing");
   // Previous scorer retained stale constants after move; this must resolve false.
   var moves=method(H,"moves",List.of(),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction11n(Opcode.CONST_4,1,0),new ImmutableInstruction12x(Opcode.MOVE,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",2,0),end()),false);
   var flow=new DexFlow(idx,deadline);var summary=flow.summary(moves);check(summary.calls().get(0).args().get(1).literal().equals("0"),"Stale constant survived move");
   // Arithmetic writes must invalidate a previous constant.
   var overwrite=method(H,"overwrite",List.of(),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction12x(Opcode.NEG_INT,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",2,0),end()),false);
   check(flow.summary(overwrite).calls().get(0).args().get(1).kind().equals("unknown"),"Arithmetic overwrite retained constant");
   check(idx.kind(new ImmutableMethodReference("Ltest/Unrelated;","setJavaScriptEnabled",List.of("Z"),"V"))==null,"Unrelated method matched by name");
   var branch=method(H,"branch",List.of("Z"),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction21t(Opcode.IF_EQZ,2,3),new ImmutableInstruction11n(Opcode.CONST_4,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",1,0),end()),false);
   var branchSummary=flow.summary(branch);check(branchSummary.branched(),"Branch not recorded");check(DexFlow.alternatives(branchSummary.calls().get(0).args().get(1)).size()==2,"Conditional setting alternatives collapsed");
   var loop=method(H,"loop",List.of(),9,1,List.of(new ImmutableInstruction10t(Opcode.GOTO,0)),false);check(flow.summary(loop).calls().isEmpty(),"Empty loop should reach stable summary");
   messageFixture();
   collectionFixture();
   System.out.println("PASS: separate WebViews, helper binding, settings identity, bridge annotation, callbacks, constant overwrite, API owner, message registry semantics, instance TAG reflection, field-writer dependency, explicit super callbacks, factory element isolation, branch join, loop convergence");
  }finally{Files.deleteIfExists(file);}
 }
 static void messageFixture()throws Exception {
  String mw="Ltest/MessageWebView;",handler="Ltest/Handler;",transport="Ltest/Transport;",map="Ljava/util/Map;";
  var field=new ImmutableField(mw,"handlers",map,1,null,Set.of(),Set.of());
  var register=method(mw,"register",List.of("Ljava/lang/String;",handler),1,4,List.of(
   new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(mw,"handlers",map)),
   invoke(Opcode.INVOKE_INTERFACE,map,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,2,3),end()),false);
  var fake=method(mw,"loadUrl",List.of("Ljava/lang/String;",map),1,4,List.of(
   new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(mw,"handlers",map)),
   invoke(Opcode.INVOKE_INTERFACE,map,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,2,3),end()),false);
  var transportField=new ImmutableField(transport,"view",mw,1,null,Set.of(),Set.of());
  var dispatch=method(transport,"dispatch",List.of("Ljava/lang/String;"),1,4,List.of(
   new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,2,new ImmutableFieldReference(transport,"view",mw)),
   new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,0,new ImmutableFieldReference(mw,"handlers",map)),
   invoke(Opcode.INVOKE_INTERFACE,map,"get",List.of("Ljava/lang/Object;"),"Ljava/lang/Object;",0,3),end()),true);
  var hm=new ImmutableMethod(handler,"handle",List.of(new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var wc=new ImmutableClassDef(mw,1,W,List.of(),null,Set.of(),List.of(field),List.of(register,fake));
  var tc=new ImmutableClassDef(transport,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(transportField),List.of(dispatch));
  var hc=new ImmutableClassDef(handler,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(hm));
  Path path=Files.createTempFile("wv-message-",".dex");
  try{
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(wc,tc,hc)));
   var idx=new CapabilityIndex();idx.read(path,System.nanoTime()+20_000_000_000L);
   check(idx.messageRegistries.containsKey(CapabilityIndex.key(register)),"Annotated transport registry not found");
   check(!idx.messageRegistries.containsKey(CapabilityIndex.key(fake)),"HTTP header map incorrectly classified as message registry");
  }finally{Files.deleteIfExists(path);}
 }

 static void collectionFixture()throws Exception {
  String list="Ljava/util/ArrayList;",iter="Ljava/util/Iterator;",b1="Ltest/FirstBridge;",b2="Ltest/SecondBridge;",unused="Ltest/UnusedBridge;";
  var body=method(H,"factory",List.of(),9,2,List.of(make(0,list),make(1,b1),invoke(Opcode.INVOKE_VIRTUAL,list,"add",List.of("Ljava/lang/Object;"),"Z",0,1),make(1,b2),invoke(Opcode.INVOKE_VIRTUAL,list,"add",List.of("Ljava/lang/Object;"),"Z",0,1),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var factory=new ImmutableMethod(H,"factory",List.of(),"Ljava/util/List;",9,Set.of(),Set.of(),body.getImplementation());
  var entry=method(A,"onCreate",List.of(),1,5,List.of(make(0,W),invoke(Opcode.INVOKE_STATIC,H,"factory",List.of(),"Ljava/util/List;"),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_INTERFACE,"Ljava/util/List;","iterator",List.of(),iter,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_INTERFACE,iter,"next",List.of(),"Ljava/lang/Object;",1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference(B)),str(2,"plugins"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var dex=new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(H,"Ljava/lang/Object;",factory),clazz(B,"Ljava/lang/Object;"),clazz(b1,B,method(b1,"one",List.of(),1,1,List.of(end()),true)),clazz(b2,B,method(b2,"two",List.of(),1,1,List.of(end()),true)),clazz(unused,B,method(unused,"unused",List.of(),1,1,List.of(end()),true))));
  Path path=Files.createTempFile("wv-collection-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),dex);long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("test.AppActivity");var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   Set<Object> implementations=new HashSet<>();for(var f:facts)if(f.get("kind").equals("bridge"))implementations.add(f.get("implementation"));
   check(implementations.equals(Set.of("test.FirstBridge","test.SecondBridge")),"Factory collection widened to unrelated subtype: "+implementations);
  }finally{Files.deleteIfExists(path);}
 }

}
