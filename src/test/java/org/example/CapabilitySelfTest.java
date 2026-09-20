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
   var bitwise=method(H,"bitwise",List.of(),9,2,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction22b(Opcode.XOR_INT_LIT8,0,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setBlockNetworkImage",List.of("Z"),"V",1,0),end()),false);
   var bitValue=flow.summary(bitwise).calls().get(0).args().get(1);var arithmeticHost=engine.new Host("test.AppActivity");var arithmeticJob=new CapabilityEngine.Job(bitwise,List.of(),List.of(),false);
   check(engine.eval(bitValue,arithmeticJob,arithmeticHost,0,new HashSet<>()).literal().equals("0"),"Boolean XOR negation did not preserve value");
   for(int initial:List.of(0,1)){
    var config=DexFlow.V.of("object","test.Config","config:"+initial);engine.applyWrite(arithmeticHost,"Ltest/Config;->enabled:Z",config,DexFlow.V.literal("number",String.valueOf(initial)));
    var read=DexFlow.expr("field","boolean","Ltest/Config;->enabled:Z",List.of(config));
    var negated=DexFlow.expr("int_binary","number","xor",List.of(read,DexFlow.V.literal("number","1")));
    check(engine.eval(negated,arithmeticJob,arithmeticHost,0,new HashSet<>()).literal().equals(String.valueOf(initial^1)),"Bitwise evaluation mixed configurator instance fields");
   }
   var unknownBit=DexFlow.expr("int_binary","number","xor",List.of(DexFlow.UNKNOWN,DexFlow.V.literal("number","1")));
   check(engine.eval(unknownBit,arithmeticJob,arithmeticHost,0,new HashSet<>()).kind().equals("unknown"),"Unknown bitwise operand became a constant");
   var choices=DexFlow.union(DexFlow.V.literal("number","0"),DexFlow.V.literal("number","1"));
   check(DexFlow.alternatives(engine.eval(DexFlow.expr("int_binary","number","xor",List.of(choices,DexFlow.V.literal("number","1"))),arithmeticJob,arithmeticHost,0,new HashSet<>())).size()==2,"Conditional bitwise alternatives collapsed");
   engine.eval(DexFlow.UNKNOWN,arithmeticJob,arithmeticHost,15,new HashSet<>());check(arithmeticHost.gaps.contains("resolve_depth"),"Resolution budget exhaustion lacked a diagnostic");
   // Arithmetic writes must invalidate a previous constant.
   var overwrite=method(H,"overwrite",List.of(),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction12x(Opcode.NEG_INT,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",2,0),end()),false);
   check(flow.summary(overwrite).calls().get(0).args().get(1).kind().equals("unknown"),"Arithmetic overwrite retained constant");
   check(idx.kind(new ImmutableMethodReference("Ltest/Unrelated;","setJavaScriptEnabled",List.of("Z"),"V"))==null,"Unrelated method matched by name");
   var branch=method(H,"branch",List.of("Z"),9,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction21t(Opcode.IF_EQZ,2,3),new ImmutableInstruction11n(Opcode.CONST_4,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",1,0),end()),false);
   var branchSummary=flow.summary(branch);check(branchSummary.branched(),"Branch not recorded");check(DexFlow.alternatives(branchSummary.calls().get(0).args().get(1)).size()==2,"Conditional setting alternatives collapsed");
   var loop=method(H,"loop",List.of(),9,1,List.of(new ImmutableInstruction10t(Opcode.GOTO,0)),false);check(flow.summary(loop).calls().isEmpty(),"Empty loop should reach stable summary");
   var guarded=method(H,"guardedFactory",List.of("Landroid/content/Context;"),9,3,List.of(new ImmutableInstruction22c(Opcode.INSTANCE_OF,0,2,new ImmutableTypeReference(A)),new ImmutableInstruction21t(Opcode.IF_EQZ,0,5),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",1,0),end()),false);
   check(flow.summary(guarded,v->v.kind().equals("param")?DexFlow.V.of("host","test.AppActivity","host"):v).calls().size()==1,"Matching Activity factory branch lost");
   check(flow.summary(guarded,v->v.kind().equals("param")?DexFlow.V.of("host","test.UnrelatedActivity","other"):v).calls().isEmpty(),"Unrelated Activity factory branch included");
   int refinements=flow.refined;
   flow.summary(guarded,v->v.kind().equals("param")?DexFlow.V.of("host","test.AppActivity","host"):v);
   check(flow.refined==refinements,"Repeated type guard was decoded instead of cached");
   messageFixture();
   collectionFixture();
   installedProviderFixture();
   privateDispatchFixture();
   unknownBridgeFixture();
   reflectiveEndpointFixture();
   registeredServiceFixture();
   nullableReceiverFixture();
   composedReceiverFixture();
   componentHelperFixture();
   callbackEntryIsolationFixture();
   xmlConstructorReplayFixture();
   returnedCaptureFixture();
   obfuscatedLazyFixture();
   customCallbackFixture();
   nestedLayoutFixture();
   returnedViewBindingFixture();
   platformHierarchyFixture();
   viewContractDispatchFixture();
   System.out.println("PASS: separate WebViews, helper binding, settings identity, bridge annotation, callbacks, constant overwrite, API owner, message registry semantics, instance TAG reflection, field-writer dependency, explicit super callbacks, factory element isolation, branch join, loop convergence, installed provider isolation, exact private dispatch, reflective empty endpoints, registered services, nullable receivers, composed receivers, helper/callback entry isolation, abstract-class handlers");
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
   var abstractHandler=new ImmutableClassDef(handler,0x401,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(hm));
   var abstractDispatch=method(transport,"dispatch",List.of("Ljava/lang/String;"),1,4,List.of(
    new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,2,new ImmutableFieldReference(transport,"view",mw)),new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,0,new ImmutableFieldReference(mw,"handlers",map)),
    invoke(Opcode.INVOKE_INTERFACE,map,"get",List.of("Ljava/lang/Object;"),"Ljava/lang/Object;",0,3),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_VIRTUAL,handler,"handle",List.of("Ljava/lang/String;"),"V",0,3),end()),true);
   String concrete="Ltest/ConcreteHandler;";var concreteMethod=method(concrete,"handle",List.of("Ljava/lang/String;"),1,2,List.of(end()),false);
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(wc,new ImmutableClassDef(transport,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(transportField),List.of(abstractDispatch)),abstractHandler,clazz(concrete,handler,concreteMethod))));
   var abstractIndex=new CapabilityIndex();abstractIndex.read(path,System.nanoTime()+20_000_000_000L);
   check(abstractIndex.messageRegistries.containsKey(CapabilityIndex.key(register)),"Abstract-class handler registry was excluded");
   var abstractEngine=new CapabilityEngine(abstractIndex,new ApkInventory(),System.nanoTime()+10_000_000_000L);var abstractHost=abstractEngine.new Host("test.AppActivity");
   var nativeCall=new DexFlow.Call(W+"->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V",1,List.of(),false,false,false);var registrationJob=new CapabilityEngine.Job(register,List.of(),List.of(),false);
   abstractEngine.emit(abstractHost,registrationJob,nativeCall,List.of(DexFlow.V.of("object","android.webkit.WebView","native-one"),DexFlow.V.of("object","test.MessageWebView","view"),DexFlow.V.literal("java.lang.String","NativeOne")),"bridge",false);
   abstractEngine.emit(abstractHost,registrationJob,nativeCall,List.of(DexFlow.V.of("object","android.webkit.WebView","native-two"),DexFlow.V.of("object","test.MessageWebView","other-registry"),DexFlow.V.literal("java.lang.String","NativeTwo")),"bridge",false);
   abstractEngine.emit(abstractHost,new CapabilityEngine.Job(register,List.of(),List.of(),false),new DexFlow.Call(CapabilityIndex.key(register),0,List.of(),false,false,false),List.of(DexFlow.V.of("object","test.MessageWebView","view"),DexFlow.V.literal("java.lang.String","route"),DexFlow.V.of("object","test.ConcreteHandler","handler")),"message_bridge",false);
   var routed=abstractHost.facts.values().stream().filter(f->"route".equals(f.get("registration_name"))).findFirst().orElseThrow();
   @SuppressWarnings("unchecked") var transportLinks=(List<Map<String,Object>>)routed.get("transport_bindings");
   check(transportLinks.size()==1&&transportLinks.get(0).get("registration_name").equals("NativeOne")&&((Map<?,?>)routed.get("webview")).get("id").equals("native-one"),"Handler transport name mixed with a different registry object/WebView");
   check(abstractHost.facts.values().stream().anyMatch(f->f.get("members").toString().contains(CapabilityIndex.key(concreteMethod))),"Transport's abstract dispatch shape did not resolve the concrete handler signature");
   String client="Ltest/UrlClient;";
   var callback=method(client,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,4,List.of(
    make(0,transport),invoke(Opcode.INVOKE_VIRTUAL,transport,"dispatch",List.of("Ljava/lang/String;"),"V",0,3),end()),false);
   var unannotated=new ImmutableMethod(transport,"dispatch",dispatch.getParameters(),"V",1,Set.of(),Set.of(),dispatch.getImplementation());
   var plainTransport=new ImmutableClassDef(transport,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(transportField),List.of(unannotated));
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(wc,plainTransport,hc,clazz(client,"Landroid/webkit/WebViewClient;",callback))));
   var urlIndex=new CapabilityIndex();urlIndex.read(path,System.nanoTime()+20_000_000_000L);
   check(urlIndex.messageRegistries.containsKey(CapabilityIndex.key(register)),"URL callback message registry not found");
   check(!urlIndex.messageRegistries.containsKey(CapabilityIndex.key(fake)),"URL transport turned headers into registry");
   String async="Ltest/AsyncResult;";
   var callbackDecl=new ImmutableMethod(async,"onCallBack",List.of(new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
   var queueCallback=new ImmutableMethod(transport,"onCallBack",dispatch.getParameters(),"V",1,Set.of(),Set.of(),dispatch.getImplementation());
   var asyncTransport=new ImmutableClassDef(transport,1,"Ljava/lang/Object;",List.of(async),null,Set.of(),List.of(transportField),List.of(queueCallback));
   var schedule=method(H,"schedule",List.of(async),9,2,List.of(str(0,"queue"),invoke(Opcode.INVOKE_INTERFACE,async,"onCallBack",List.of("Ljava/lang/String;"),"V",1,0),end()),false);
   var asyncEntry=method(client,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,4,List.of(make(0,transport),invoke(Opcode.INVOKE_STATIC,H,"schedule",List.of(async),"V",0),end()),false);
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(wc,asyncTransport,hc,clazz(client,"Landroid/webkit/WebViewClient;",asyncEntry),clazz(H,"Ljava/lang/Object;",schedule),new ImmutableClassDef(async,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(callbackDecl)))));
   var asyncIndex=new CapabilityIndex();asyncIndex.read(path,System.nanoTime()+20_000_000_000L);
   check(asyncIndex.messageRegistries.containsKey(CapabilityIndex.key(register)),"Map read in allocated async callback was missed");
  }finally{Files.deleteIfExists(path);}
 }

 static void unknownBridgeFixture()throws Exception {
  String unused="Ltest/NeverAllocatedBridge;";var field=new ImmutableField(A,"unresolved",B,1,null,Set.of(),Set.of());
  var entry=method(A,"onCreate",List.of(),1,4,List.of(make(0,W),new ImmutableInstruction22c(Opcode.IGET_OBJECT,1,3,new ImmutableFieldReference(A,"unresolved",B)),str(2,"unknown-object"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  Path path=Files.createTempFile("wv-unknown-bridge-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(new ImmutableClassDef(A,1,"Landroid/app/Activity;",List.of(),null,Set.of(),List.of(field),List.of(entry)),clazz(B,"Ljava/lang/Object;"),clazz(unused,B,method(unused,"extra",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"unknown-object".equals(f.get("registration_name"))&&"unknown".equals(f.get("implementation"))),"Unknown bridge registration lost or falsely concretized");
   check(facts.stream().noneMatch(f->"test.NeverAllocatedBridge".equals(f.get("implementation"))),"Unallocated bridge subtype inferred from field declaration");
  }finally{Files.deleteIfExists(path);}
 }

 static void privateDispatchFixture()throws Exception {
  String base="Ltest/BaseHolder;",sub="Ltest/SubHolder;";
  var original=method(base,"init",List.of(W),2,4,List.of(make(0,B),str(1,"base-private"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var sibling=method(sub,"init",List.of(W),2,4,List.of(make(0,B),str(1,"wrong-private"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var baseCtor=method(base,"<init>",List.of(W),1,2,List.of(invoke(Opcode.INVOKE_DIRECT,base,"init",List.of(W),"V",0,1),end()),false);
  var subCtor=method(sub,"<init>",List.of(W),1,2,List.of(invoke(Opcode.INVOKE_DIRECT,base,"<init>",List.of(W),"V",0,1),end()),false);
  var entry=method(A,"onCreate",List.of(),1,3,List.of(make(0,W),make(1,sub),invoke(Opcode.INVOKE_DIRECT,sub,"<init>",List.of(W),"V",1,0),end()),false);
  Path path=Files.createTempFile("wv-private-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(base,"Ljava/lang/Object;",original,baseCtor),clazz(sub,base,sibling,subCtor),clazz(B,"Ljava/lang/Object;"))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"base-private".equals(f.get("registration_name"))),"Exact private initializer lost");
   check(facts.stream().noneMatch(f->"wrong-private".equals(f.get("registration_name"))),"Private initializer dispatched to same-name subclass member");
  }finally{Files.deleteIfExists(path);}
 }
 static void reflectiveEndpointFixture()throws Exception {
  String handler="Ltest/ReflectiveHandler;",target="Ltest/ReflectionTarget;";
  var dispatch=method(handler,"dispatch",List.of("Ljava/lang/String;"),1,6,List.of(
   new ImmutableInstruction21c(Opcode.CONST_CLASS,0,new ImmutableTypeReference(target)),
   new ImmutableInstruction11n(Opcode.CONST_4,1,1),new ImmutableInstruction22c(Opcode.NEW_ARRAY,1,1,new ImmutableTypeReference("[Ljava/lang/Class;")),
   new ImmutableInstruction11n(Opcode.CONST_4,2,0),new ImmutableInstruction21c(Opcode.CONST_CLASS,3,new ImmutableTypeReference("Ljava/lang/String;")),
   new ImmutableInstruction23x(Opcode.APUT_OBJECT,3,1,2),
   invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/Class;","getDeclaredMethod",List.of("Ljava/lang/String;","[Ljava/lang/Class;"),"Ljava/lang/reflect/Method;",0,5,1),
   new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),
   invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/reflect/Method;","invoke",List.of("Ljava/lang/Object;","[Ljava/lang/Object;"),"Ljava/lang/Object;",0,2,2),end()),false);
  var existing=method(target,"existing",List.of("Ljava/lang/String;"),1,2,List.of(end()),false);
  var business=method("Ltest/BusinessHandler;","handle",List.of("Ljava/lang/String;"),1,4,List.of(
   new ImmutableInstruction21c(Opcode.CONST_CLASS,0,new ImmutableTypeReference(target)),str(1,"businessOnly"),new ImmutableInstruction11n(Opcode.CONST_4,2,0),
   invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/Class;","getDeclaredMethod",List.of("Ljava/lang/String;","[Ljava/lang/Class;"),"Ljava/lang/reflect/Method;",0,1,2),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),
   invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/reflect/Method;","invoke",List.of("Ljava/lang/Object;","[Ljava/lang/Object;"),"Ljava/lang/Object;",0,2,2),end()),false);
  Path path=Files.createTempFile("wv-reflection-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(handler,"Ljava/lang/Object;",dispatch),clazz(target,"Ljava/lang/Object;",existing),clazz("Ltest/BusinessHandler;","Ljava/lang/Object;",business))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(dispatch,List.of(),List.of(),true);
   check(!engine.messageMembers("test.BusinessHandler","registeredName",job,host).reflective(),"Downstream business reflection erased known handler endpoint");
   var empty=engine.messageMembers("test.ReflectiveHandler","missing",job,host);check(empty.reflective()&&empty.resolved()&&empty.members().isEmpty(),"Known absent reflection endpoint not distinguished from unknown dispatcher");
   var present=engine.messageMembers("test.ReflectiveHandler","existing",job,host);check(present.members().size()==1&&present.members().get(0).get("signature").equals(CapabilityIndex.key(existing)),"Exact reflection endpoint missing");
  }finally{Files.deleteIfExists(path);}
 }

 static void viewContractDispatchFixture()throws Exception {
  String widget="Ltest/Widget;",contract="Ltest/WidgetContract;";
  var declared=new ImmutableMethod(contract,"show",List.of(new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var implementation=method(widget,"show",List.of("Ljava/lang/String;"),1,3,List.of(make(0,W),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",0,2),end()),false);
  var entry=method(A,"onCreate",List.of(),1,3,List.of(make(0,widget),str(1,"https://example.invalid/"),invoke(Opcode.INVOKE_INTERFACE,contract,"show",List.of("Ljava/lang/String;"),"V",0,1),end()),false);
  Path path=Files.createTempFile("wv-view-contract-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(widget,1,"Landroid/view/View;",List.of(contract),null,Set.of(),List.of(),List.of(implementation)),new ImmutableClassDef(contract,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declared)))));long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);check(idx.relevant.contains(CapabilityIndex.key(entry)),"View carrier's interface dispatch was excluded from relevance");
   var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");check(!engine.activities.isEmpty(),"Activity lost WebView behind concrete View contract");
  }finally{Files.deleteIfExists(path);}
 }

 static void platformHierarchyFixture()throws Exception {
  Path jar=Files.createTempFile("wv-platform-headers-",".jar");
  try{
   try(var output=new java.util.jar.JarOutputStream(Files.newOutputStream(jar))){
    for(var pair:List.of(List.of("android/widget/FrameLayout","android/view/ViewGroup"),List.of("android/view/ViewGroup","android/view/View"),List.of("android/widget/NonViewHelper","java/lang/Object"))){
     var writer=new org.objectweb.asm.ClassWriter(0);writer.visit(52,1,pair.get(0),null,pair.get(1),null);writer.visitEnd();output.putNextEntry(new java.util.jar.JarEntry(pair.get(0)+".class"));output.write(writer.toByteArray());output.closeEntry();
    }
   }
   var idx=new CapabilityIndex();idx.platformParents.putAll(PlatformHierarchy.read(jar,System.nanoTime()+5_000_000_000L));idx.classes.put("test.FrameWrapper",clazz("Ltest/FrameWrapper;","Landroid/widget/FrameLayout;"));
   check(idx.component("test.FrameWrapper"),"SDK header hierarchy did not classify FrameLayout wrapper as View");check(!idx.component("android.widget.NonViewHelper"),"Widget namespace alone was treated as View hierarchy");check(!idx.webview("test.FrameWrapper"),"Ordinary SDK View was turned into WebView");
  }finally{Files.deleteIfExists(jar);}
 }

 static void nestedLayoutFixture()throws Exception {
  String wrapper="Ltest/LayoutWrapper;",inner="Ltest/InnerView;",context="Landroid/content/Context;",attrs="Landroid/util/AttributeSet;";
  var field=new ImmutableField(wrapper,"web",inner,1,null,Set.of(),Set.of());
  var init=method(inner,"<init>",List.of(context),1,4,List.of(invoke(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,2),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction11n(Opcode.CONST_4,1,0),invoke(Opcode.INVOKE_VIRTUAL,S,"setMixedContentMode",List.of("I"),"V",0,1),end()),false);
  var ctor=method(wrapper,"<init>",List.of(context,attrs),1,4,List.of(make(0,inner),invoke(Opcode.INVOKE_DIRECT,inner,"<init>",List.of(context),"V",0,2),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(wrapper,"web",inner)),end()),false);
  var getBody=method(wrapper,"getWeb",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(wrapper,"web",inner)),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var getter=new ImmutableMethod(wrapper,"getWeb",List.of(),inner,1,Set.of(),Set.of(),getBody.getImplementation());
  List<Instruction> body=new ArrayList<>();
  for(int resource:List.of(1,2)){body.add(new ImmutableInstruction11n(Opcode.CONST_4,0,resource));body.add(invoke(Opcode.INVOKE_VIRTUAL,A,"findViewById",List.of("I"),"Landroid/view/View;",3,0));body.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1));body.add(new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference(wrapper)));body.add(invoke(Opcode.INVOKE_VIRTUAL,wrapper,"getWeb",List.of(),inner,1));body.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1));body.add(str(0,"https://example.invalid/"));body.add(invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",1,0));}body.add(end());
  var entry=method(A,"onCreate",List.of(),1,4,body,false);Path path=Files.createTempFile("wv-layout-wrapper-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(inner,W,init),new ImmutableClassDef(wrapper,1,"Landroid/view/View;",List.of(),null,Set.of(),List.of(field),List.of(ctor,getter)))));long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");var settings=facts.stream().filter(f->"setMixedContentMode".equals(f.get("name"))).toList();
   check(idx.relevant.contains(CapabilityIndex.key(getter)),"Pure WebView getter excluded from field-writer relevance");
   check(settings.size()==2,"Layout wrapper lost inner constructor settings or fabricated extra receiver");check(!settings.get(0).get("webview").equals(settings.get(1).get("webview")),"Separate layout wrappers shared the inner WebView");
  }finally{Files.deleteIfExists(path);}
 }

 static void customCallbackFixture()throws Exception {
  String view="Ltest/CustomView;",listener="Ltest/Listener;",parentListener="Ltest/ParentListener;",first="Ltest/FirstListener;",second="Ltest/SecondListener;";
  var field=new ImmutableField(view,"listener",listener,1,null,Set.of(),Set.of());
  var setter=method(view,"attach",List.of(listener),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(view,"listener",listener)),end()),false);
  var fake=method(view,"ignore",List.of(listener),1,2,List.of(end()),false);
  var decl=new ImmutableMethod(parentListener,"onPageFinished",List.of(new ImmutableMethodParameter(W,Set.of(),null),new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var title=new ImmutableMethod(listener,"onTitle",List.of(new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  String forwarding="Ltest/CustomView$Forwarder;";var outer=new ImmutableField(forwarding,"outer",view,1,null,Set.of(),Set.of());
  var dispatch=method(forwarding,"forward",List.of("Ljava/lang/String;"),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,1,2,new ImmutableFieldReference(forwarding,"outer",view)),new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(view,"listener",listener)),invoke(Opcode.INVOKE_INTERFACE,listener,"onPageFinished",List.of(W,"Ljava/lang/String;"),"V",0,1,3),end()),false);
  var entry=method(A,"onCreate",List.of(),1,5,List.of(make(0,view),make(1,view),make(2,first),make(3,second),invoke(Opcode.INVOKE_VIRTUAL,view,"attach",List.of(listener),"V",0,2),invoke(Opcode.INVOKE_VIRTUAL,view,"attach",List.of(listener),"V",1,3),end()),false);
  List<ImmutableClassDef> classes=new ArrayList<>(List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(view,1,W,List.of(),null,Set.of(),List.of(field),List.of(setter,fake)),new ImmutableClassDef(forwarding,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(outer),List.of(dispatch)),new ImmutableClassDef(listener,0x601,"Ljava/lang/Object;",List.of(parentListener),null,Set.of(),List.of(),List.of(title)),new ImmutableClassDef(parentListener,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(decl))));
  for(String type:List.of(first,second))classes.add(new ImmutableClassDef(type,1,"Ljava/lang/Object;",List.of(listener),null,Set.of(),List.of(),List.of(method(type,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false),method(type,"onTitle",List.of("Ljava/lang/String;"),1,2,List.of(end()),false),method(type,"unrelated",List.of(),1,1,List.of(end()),false))));
  Path path=Files.createTempFile("wv-custom-callback-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);
   check(idx.customCallbacks.containsKey(CapabilityIndex.key(setter)),"Stored custom callback not discovered");check(!idx.customCallbacks.containsKey(CapabilityIndex.key(fake)),"Ignoring callback argument was treated as registration");
   var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");var callbacks=facts.stream().filter(f->"callback".equals(f.get("kind"))).toList();
   check(callbacks.size()==2,"Unexpected custom callback registrations");check(!callbacks.get(0).get("webview").equals(callbacks.get(1).get("webview")),"Custom callbacks merged across WebViews");
   for(var fact:callbacks){@SuppressWarnings("unchecked")var members=(List<Map<String,Object>>)fact.get("members");check(members.size()==2,"Custom listener contract lost a method or included unrelated method");check(members.stream().filter(m->Boolean.TRUE.equals(m.get("dispatch_observed"))).count()==1,"Undispatched contract override falsely claimed observed dispatch");}
  }finally{Files.deleteIfExists(path);}
 }

 static void obfuscatedLazyFixture()throws Exception {
  String lazy="Lkotlin/ObfuscatedLazy;",fn="Lkotlin/jvm/functions/ObfuscatedFunction;",init="Ltest/LazyInitializer;",factoryOwner="Lkotlin/ObfuscatedFactory;";
  var get=new ImmutableMethod(lazy,"getValue",List.of(),"Ljava/lang/Object;",0x401,Set.of(),Set.of(),null);
  var initialized=new ImmutableMethod(lazy,"isInitialized",List.of(),"Z",0x401,Set.of(),Set.of(),null);
  var invokeDecl=new ImmutableMethod(fn,"invoke",List.of(),"Ljava/lang/Object;",0x401,Set.of(),Set.of(),null);
  var captured=new ImmutableField(init,"captured",W,1,null,Set.of(),Set.of());
  var invokeBody=method(init,"invoke",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(init,"captured",W)),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var typedInvoke=new ImmutableMethod(init,"invoke",List.of(),"Ljava/lang/Object;",1,Set.of(),Set.of(),invokeBody.getImplementation());
  String impl="Lkotlin/LazyImpl;";var fnField=new ImmutableField(impl,"initializer",fn,1,null,Set.of(),Set.of());
  var lazyCtor=method(impl,"<init>",List.of(fn),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(impl,"initializer",fn)),end()),false);
  var factory=new ImmutableMethod(factoryOwner,"x",List.of(new ImmutableMethodParameter(fn,Set.of(),null)),lazy,9,Set.of(),Set.of(),new ImmutableMethodImplementation(2,List.of(make(0,impl),invoke(Opcode.INVOKE_DIRECT,impl,"<init>",List.of(fn),"V",0,1),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),List.of(),List.of()));
  var ignoredFactory=new ImmutableMethod(factoryOwner,"ignored",factory.getParameters(),lazy,9,Set.of(),Set.of(),new ImmutableMethodImplementation(2,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),List.of(),List.of()));
  Path path=Files.createTempFile("wv-obfuscated-lazy-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(
   new ImmutableClassDef(lazy,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(get,initialized)),
   new ImmutableClassDef(fn,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(invokeDecl)),
   new ImmutableClassDef(init,1,"Ljava/lang/Object;",List.of(fn),null,Set.of(),List.of(captured),List.of(typedInvoke)),clazz(factoryOwner,"Ljava/lang/Object;",factory,ignoredFactory),new ImmutableClassDef(impl,1,"Ljava/lang/Object;",List.of(lazy),null,Set.of(),List.of(fnField),List.of(lazyCtor)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(factory,List.of(),List.of(),false);
   check(engine.lazyInitializerParameters(ignoredFactory).isEmpty(),"Factory discarding initializer was treated as Lazy capture");
   check(!idx.lazyType("test.Unrelated"),"Unrelated type recognized as Kotlin Lazy");
   for(String id:List.of("one","two")){
    var view=DexFlow.V.of("object","android.webkit.WebView",id);var initializer=DexFlow.V.of("object","test.LazyInitializer","initializer:"+id);
    engine.applyWrite(host,CapabilityIndex.field(captured),initializer,view);
    var deferred=engine.eval(DexFlow.expr("return","kotlin.ObfuscatedLazy",CapabilityIndex.key(factory),List.of(initializer)),job,host,0,new HashSet<>());
    var result=engine.eval(DexFlow.expr("return","java.lang.Object",CapabilityIndex.key(get),List.of(deferred)),job,host,0,new HashSet<>());
    check(result.equals(view),"Obfuscated Lazy lost initializer capture or mixed separate WebViews");
   }
  }finally{Files.deleteIfExists(path);}
 }

 static void returnedCaptureFixture()throws Exception {
  String box="Ltest/CapturedView;";var field=new ImmutableField(box,"view",W,1,null,Set.of(),Set.of());
  var ctor=method(box,"<init>",List.of(W),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(box,"view",W)),end()),false);
  var body=method(H,"capture",List.of(W),9,2,List.of(make(0,box),invoke(Opcode.INVOKE_DIRECT,box,"<init>",List.of(W),"V",0,1),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var factory=new ImmutableMethod(H,"capture",body.getParameters(),box,9,Set.of(),Set.of(),body.getImplementation());
  Path path=Files.createTempFile("wv-return-capture-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(H,"Ljava/lang/Object;",factory),new ImmutableClassDef(box,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(field),List.of(ctor)))));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(factory,List.of(),List.of(),false);
   for(String id:List.of("first-view","second-view")){
    var view=DexFlow.V.of("object","android.webkit.WebView",id);
    var captured=engine.eval(DexFlow.expr("return","test.CapturedView",CapabilityIndex.key(factory),List.of(view)),job,host,0,new HashSet<>());
    var resolved=engine.eval(DexFlow.expr("field","android.webkit.WebView",CapabilityIndex.field(field),List.of(captured)),job,host,0,new HashSet<>());
    check(resolved.equals(view),"Factory-returned object's constructor capture was unresolved or mixed with another WebView");
   }
  }finally{Files.deleteIfExists(path);}
 }

 static void returnedViewBindingFixture()throws Exception {
  String W="Ltest/CustomLayout;"; String box="Ltest/GeneratedBinding;";var field=new ImmutableField(box,"view",W,1,null,Set.of(),Set.of());
  var ctor=method(box,"<init>",List.of(W),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(box,"view",W)),end()),false);
  var body=method(H,"capture",List.of(W),9,2,List.of(make(0,box),invoke(Opcode.INVOKE_DIRECT,box,"<init>",List.of(W),"V",0,1),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var factory=new ImmutableMethod(H,"capture",body.getParameters(),box,9,Set.of(),Set.of(),body.getImplementation());
  Path path=Files.createTempFile("wv-return-capture-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(H,"Ljava/lang/Object;",factory),new ImmutableClassDef(box,1,"Ljava/lang/Object;",List.of("Landroidx/viewbinding/ViewBinding;"),null,Set.of(),List.of(field),List.of(ctor)))));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(factory,List.of(),List.of(),false);
   for(String id:List.of("first-view","second-view")){
    var view=DexFlow.V.of("object","test.CustomLayout",id);
    var captured=engine.eval(DexFlow.expr("return","test.GeneratedBinding",CapabilityIndex.key(factory),List.of(view)),job,host,0,new HashSet<>());
    var resolved=engine.eval(DexFlow.expr("field","test.CustomLayout",CapabilityIndex.field(field),List.of(captured)),job,host,0,new HashSet<>());
    check(resolved.equals(view),"Generated ViewBinding constructor lost or mixed actual custom View captures");
   }
  }finally{Files.deleteIfExists(path);}
 }

 static void xmlConstructorReplayFixture()throws Exception {
  String parent="Ltest/XmlBaseView;",child="Ltest/XmlChildView;",context="Landroid/content/Context;",attrs="Landroid/util/AttributeSet;";
  var configure=method(parent,"supportHtml5",List.of(S),2,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setDatabaseEnabled",List.of("Z"),"V",2,0),end()),false);
  var baseInit=method(parent,"init",List.of(context),2,3,List.of(invoke(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_DIRECT,parent,"supportHtml5",List.of(S),"V",1,0),end()),false);
  var baseCtor=method(parent,"<init>",List.of(context,attrs),1,3,List.of(invoke(Opcode.INVOKE_DIRECT,parent,"init",List.of(context),"V",0,1),end()),false);
  var xmlCtor=method(child,"<init>",List.of(context,attrs),1,3,List.of(invoke(Opcode.INVOKE_DIRECT,parent,"<init>",List.of(context,attrs),"V",0,1,2),end()),false);
  var programCtor=method(child,"<init>",List.of(context,B),1,4,List.of(str(0,"programmatic-only"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",1,3,0),end()),false);
  var shadow=method(child,"init",List.of(context),1,2,List.of(end()),false);
  var entry=method(A,"onCreate",List.of(),1,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,7),invoke(Opcode.INVOKE_VIRTUAL,A,"findViewById",List.of("I"),"Landroid/view/View;",2,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference(child)),str(0,"https://example.invalid/"),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",1,0),end()),false);
  Path path=Files.createTempFile("wv-xml-replay-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(parent,W,baseCtor,baseInit,configure),clazz(child,parent,xmlCtor,programCtor,shadow),clazz(B,"Ljava/lang/Object;"))));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"setDatabaseEnabled".equals(f.get("name"))&&"true".equals(f.get("value"))),"Second pass lost XML constructor's private Settings helper");
   check(facts.stream().noneMatch(f->"programmatic-only".equals(f.get("registration_name"))),"Lookup-derived view seeded unrelated programmatic constructor");
  }finally{Files.deleteIfExists(path);}
 }

 static void callbackEntryIsolationFixture()throws Exception {
  String listener="Ltest/ReadyListener;",parent="Ltest/BusinessParent;",child="Ltest/ReadyCallback;";
  var declaration=new ImmutableMethod(listener,"onReady",List.of(new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var wrong=method(parent,"uninvokedBusiness",List.of(),1,4,List.of(make(0,W),make(1,B),str(2,"uninvoked-parent"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var right=method(child,"onReady",List.of("Ljava/lang/String;"),1,5,List.of(make(0,W),make(1,B),str(2,"actual-callback"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var init=method(child,"<init>",List.of(),1,1,List.of(end()),false);var entry=method(A,"onCreate",List.of(),1,2,List.of(make(0,child),invoke(Opcode.INVOKE_DIRECT,child,"<init>",List.of(),"V",0),end()),false);
  Path path=Files.createTempFile("wv-callback-entry-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(B,"Ljava/lang/Object;"),clazz(parent,"Ljava/lang/Object;",wrong),new ImmutableClassDef(listener,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),new ImmutableClassDef(child,1,parent,List.of(listener),null,Set.of(),List.of(),List.of(init,right)))));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");check(!engine.activities.isEmpty(),"Allocated callback entry was lost");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");check(facts.stream().anyMatch(f->"actual-callback".equals(f.get("registration_name"))),"Callback entry capability missing");check(facts.stream().noneMatch(f->"uninvoked-parent".equals(f.get("registration_name"))),"Constructor seeded an uninvoked inherited business method");
  }finally{Files.deleteIfExists(path);}
 }

 static void componentHelperFixture()throws Exception {
  String custom="Ltest/HelperWebView;";
  var helper=method(custom,"configureOther",List.of(W),1,4,List.of(make(0,B),str(1,"other-only"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var init=method(custom,"<init>",List.of(),1,1,List.of(end()),false);
  var entry=method(A,"onCreate",List.of(),1,3,List.of(make(0,custom),invoke(Opcode.INVOKE_DIRECT,custom,"<init>",List.of(),"V",0),make(1,W),invoke(Opcode.INVOKE_VIRTUAL,custom,"configureOther",List.of(W),"V",0,1),end()),false);
  Path path=Files.createTempFile("wv-helper-entry-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(custom,W,init,helper),clazz(B,"Ljava/lang/Object;"))));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   var bindings=facts.stream().filter(f->"other-only".equals(f.get("registration_name"))).toList();check(bindings.size()==1,"Helper seeded an extra context-free WebView binding");
   check(!((Map<?,?>)bindings.get(0).get("webview")).get("type").equals("test.HelperWebView"),"Helper argument was conflated with its own WebView receiver");
  }finally{Files.deleteIfExists(path);}
 }

 static void composedReceiverFixture()throws Exception {
  String holder="Ltest/Controller;",api="Ltest/Worker;",good="Ltest/ChosenWorker;",bad="Ltest/OtherWorker;";
  var slot=new ImmutableField(holder,"worker",api,1,null,Set.of(),Set.of());
  var init=method(holder,"<init>",List.of(api),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(holder,"worker",api)),end()),false);
  var run=method(holder,"run",List.of(W),1,3,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(holder,"worker",api)),invoke(Opcode.INVOKE_INTERFACE,api,"configure",List.of(W),"V",0,2),end()),false);
  var declaration=new ImmutableMethod(api,"configure",List.of(new ImmutableMethodParameter(W,Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var apply=method(good,"configure",List.of(W),1,4,List.of(make(0,B),str(1,"composed"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),
   new ImmutableInstruction21c(Opcode.SGET_BOOLEAN,0,new ImmutableFieldReference(good,"enabled","Z")),end()),false);
  var other=method(bad,"configure",List.of(W),1,4,List.of(make(0,B),str(1,"unselected"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var entry=method(A,"onCreate",List.of(),1,4,List.of(make(0,holder),make(1,good),invoke(Opcode.INVOKE_DIRECT,holder,"<init>",List.of(api),"V",0,1),make(2,W),invoke(Opcode.INVOKE_VIRTUAL,holder,"run",List.of(W),"V",0,2),end()),false);
  var classes=List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(B,"Ljava/lang/Object;"),new ImmutableClassDef(holder,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(slot),List.of(init,run)),
   new ImmutableClassDef(api,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),new ImmutableClassDef(good,1,"Ljava/lang/Object;",List.of(api),null,Set.of(),List.of(),List.of(apply)),new ImmutableClassDef(bad,1,"Ljava/lang/Object;",List.of(api),null,Set.of(),List.of(),List.of(other)));
  Path path=Files.createTempFile("wv-composition-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);check(idx.relevant.contains(CapabilityIndex.key(init)),"Composed receiver constructor was excluded from dependencies");
   var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");check(!engine.activities.isEmpty(),"Composed receiver lost Activity binding");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");check(facts.stream().anyMatch(f->"composed".equals(f.get("registration_name"))),"Concrete constructor argument was lost across controller field");check(facts.stream().noneMatch(f->"unselected".equals(f.get("registration_name"))),"Unselected worker subtype leaked into controller");
  }finally{Files.deleteIfExists(path);}
 }

 static void nullableReceiverFixture(){
  var idx=new CapabilityIndex();var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,System.nanoTime()+10_000_000_000L);var h=engine.new Host("test.AppActivity");
  var entry=method(A,"onCreate",List.of(),1,1,List.of(end()),false);var job=new CapabilityEngine.Job(entry,List.of(),List.of("test.AppActivity"),false);
  var nil=DexFlow.V.literal(null,"0");var view=DexFlow.V.of("view","android.webkit.WebView","layout:one");
  var call=new DexFlow.Call(W+"->setWebViewClient(Landroid/webkit/WebViewClient;)V",0,List.of(),false,false,false);
  engine.emit(h,job,call,List.of(nil,DexFlow.V.of("object","test.Client","client")),"callback",false);
  check(h.facts.isEmpty(),"Constant null receiver created a WebView capability");
  engine.emit(h,job,call,List.of(DexFlow.union(nil,view),DexFlow.V.of("object","test.Client","client")),"callback",true);
  check(h.facts.size()==1,"Nullable receiver lost its live alternative");var fact=h.facts.values().iterator().next();
  check(((Map<?,?>)fact.get("webview")).get("id").equals("layout:one"),"Null branch became a second symbolic WebView");
  check(fact.get("receiver_condition").equals("non_null"),"Receiver nullability condition was erased");
  var global=new DexFlow.Call(W+"->setWebContentsDebuggingEnabled(Z)V",1,List.of(),true,false,false);
  engine.emit(h,job,global,List.of(nil),"global_setting",false);
  check(h.facts.values().stream().anyMatch(f->"global_setting".equals(f.get("kind"))&&"false".equals(f.get("value"))),"Static boolean false was confused with a null receiver");
 }

 static void registeredServiceFixture()throws Exception {
  String api="Ltest/Service;",impl="Ltest/RegisteredService;",unused="Ltest/UnregisteredService;";
  String meta="Lcom/tencent/news/qnrouter/service/APIMeta;",registry="Lcom/tencent/news/qnrouter/service/ServiceMap;",services="Lcom/tencent/news/qnrouter/service/Services;";
  var declaration=new ImmutableMethod(api,"configure",List.of(new ImmutableMethodParameter(W,Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var good=method(impl,"configure",List.of(W),1,4,List.of(make(0,B),str(1,"registered"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var bad=method(unused,"configure",List.of(W),1,4,List.of(make(0,B),str(1,"unregistered"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var init=method(H,"register",List.of(),9,5,List.of(
   new ImmutableInstruction21c(Opcode.CONST_CLASS,0,new ImmutableTypeReference(api)),new ImmutableInstruction21c(Opcode.CONST_CLASS,1,new ImmutableTypeReference(impl)),make(2,meta),new ImmutableInstruction11n(Opcode.CONST_4,3,1),
   invoke(Opcode.INVOKE_DIRECT,meta,"<init>",List.of("Ljava/lang/Class;","Ljava/lang/Class;","Z"),"V",2,0,1,3),str(4,"_default_impl_"),
   invoke(Opcode.INVOKE_STATIC,registry,"autoRegister",List.of("Ljava/lang/Class;","Ljava/lang/String;",meta),"V",0,4,2),end()),false);
  var entry=method(A,"onCreate",List.of(),1,3,List.of(make(0,W),new ImmutableInstruction21c(Opcode.CONST_CLASS,1,new ImmutableTypeReference(api)),invoke(Opcode.INVOKE_STATIC,services,"call",List.of("Ljava/lang/Class;"),"Ljava/lang/Object;",1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_INTERFACE,api,"configure",List.of(W),"V",1,0),end()),false);
  var classes=List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(H,"Ljava/lang/Object;",init),clazz(B,"Ljava/lang/Object;"),
   new ImmutableClassDef(api,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),
   new ImmutableClassDef(impl,1,"Ljava/lang/Object;",List.of(api),null,Set.of(),List.of(),List.of(good)),
   new ImmutableClassDef(unused,1,"Ljava/lang/Object;",List.of(api),null,Set.of(),List.of(),List.of(bad)));
  Path path=Files.createTempFile("wv-service-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);check(idx.services.bindings.size()==1,"Generated service metadata not indexed");
   var missing=idx.services.lookup(services+"->get(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APICreator;)Ljava/lang/Object;",List.of(DexFlow.V.of("class","test.Service",api),new DexFlow.V("literal","java.lang.String","missing","missing",List.of()),new DexFlow.V("literal",null,"0","0",List.of())),"test");
   check(missing==null,"Unregistered service qualifier was accepted");
   String qualified=services+"->get(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APICreator;)Ljava/lang/Object;";
   var klass=DexFlow.V.of("class","test.Service",api);var nil=new DexFlow.V("literal",null,"0","0",List.of());
   check(idx.services.lookup(qualified,List.of(klass,new DexFlow.V("literal","java.lang.String","empty","",List.of()),nil),"test").type().equals("test.RegisteredService"),"Empty lookup qualifier did not resolve default");
   check(idx.services.lookup(qualified,List.of(klass,nil,nil),"test").type().equals("test.RegisteredService"),"Null lookup qualifier did not resolve default");
   check(idx.services.lookup(qualified,List.of(klass,nil,DexFlow.UNKNOWN),"test").kind().equals("unknown"),"Unresolved creator was replaced by default implementation");
   var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");check(!engine.activities.isEmpty(),"Registered service lookup did not bind host");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"registered".equals(f.get("registration_name"))),"Registered service capability missing");
   check(facts.stream().noneMatch(f->"unregistered".equals(f.get("registration_name"))),"Unregistered implementation leaked into service host");
  }finally{Files.deleteIfExists(path);}
 }

 static void installedProviderFixture()throws Exception {
  String api="Ltest/Provider;",installed="Ltest/InstalledProvider;",unused="Ltest/UnusedProvider;",initializer="Ltest/ApplicationInit;";
  var field=new ImmutableField(H,"provider",api,9,null,Set.of(),Set.of());
  var setter=method(H,"install",List.of(api),9,1,List.of(new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(H,"provider",api)),end()),false);
  var getterBody=method(H,"get",List.of(),9,1,List.of(new ImmutableInstruction21c(Opcode.SGET_OBJECT,0,new ImmutableFieldReference(H,"provider",api)),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var getter=new ImmutableMethod(H,"get",List.of(),api,9,Set.of(),Set.of(),getterBody.getImplementation());
  var init=method(initializer,"init",List.of(),9,1,List.of(make(0,installed),invoke(Opcode.INVOKE_STATIC,H,"install",List.of(api),"V",0),end()),false);
  var declaration=new ImmutableMethod(api,"configure",List.of(new ImmutableMethodParameter(W,Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var good=method(installed,"configure",List.of(W),1,4,List.of(make(0,B),str(1,"installed"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var bad=method(unused,"configure",List.of(W),1,4,List.of(make(0,B),str(1,"unused"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var entry=method(A,"onCreate",List.of(),1,3,List.of(make(0,W),invoke(Opcode.INVOKE_STATIC,H,"get",List.of(),api),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_INTERFACE,api,"configure",List.of(W),"V",1,0),end()),false);
  var classes=List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(initializer,"Ljava/lang/Object;",init),clazz(B,"Ljava/lang/Object;"),
   new ImmutableClassDef(H,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(field),List.of(setter,getter)),
   new ImmutableClassDef(api,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),
   new ImmutableClassDef(installed,1,"Ljava/lang/Object;",List.of(api),null,Set.of(),List.of(),List.of(good)),
   new ImmutableClassDef(unused,1,"Ljava/lang/Object;",List.of(api),null,Set.of(),List.of(),List.of(bad)));
  Path path=Files.createTempFile("wv-provider-",".dex");
  try{DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+20_000_000_000L;
   var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(!engine.activities.isEmpty(),"Installed global provider was not resolved");
   @SuppressWarnings("unchecked") var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"installed".equals(f.get("registration_name"))),"Installed provider capability missing");
   check(facts.stream().noneMatch(f->"unused".equals(f.get("registration_name"))),"Uninstalled provider subtype leaked into host");
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
