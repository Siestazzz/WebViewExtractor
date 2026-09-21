package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** A posted runnable and an unregistered sibling capture different WebViews. */
final class AsyncRegistrationFixture {
 static void run() throws Exception { run(false,false);run(true,false);run(false,true);run(true,true); }
 static void run(boolean wrapped,boolean captureless) throws Exception {
  String runnable="Ljava/lang/Runnable;",impl="Ltest/Posted;",handler="Landroid/os/Handler;";
  var field=new ImmutableField(impl,"view",W,1,null,Set.of(),Set.of());
  var ctor=method(impl,"<init>",List.of(W),1,2,captureless?List.of(end()):List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(impl,"view",W)),end()),false);
  var run=method(impl,"run",List.of(),1,4,List.of(captureless?make(0,W):new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,3,new ImmutableFieldReference(impl,"view",W)),make(1,B),str(2,"posted"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var entry=method(A,"onCreate",List.of(),1,6,List.of(
   make(0,W),make(1,impl),invoke(Opcode.INVOKE_DIRECT,impl,"<init>",List.of(W),"V",1,0),
   make(2,handler),wrapped?invoke(Opcode.INVOKE_STATIC,"Ltest/PostWrapper;","schedule",List.of(handler,runnable),"V",2,1):invoke(Opcode.INVOKE_VIRTUAL,handler,"post",List.of(runnable),"Z",2,1),
   make(3,W),make(4,impl),invoke(Opcode.INVOKE_DIRECT,impl,"<init>",List.of(W),"V",4,3),end()),false);
  var contract=new ImmutableMethod(runnable,"run",List.of(),"V",0x401,Set.of(),Set.of(),null);
  var reject=new ImmutableMethod("Ltest/RejectingHandler;","post",List.of(new ImmutableMethodParameter(runnable,Set.of(),null)),"Z",1,Set.of(),Set.of(),new ImmutableMethodImplementation(3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction11x(Opcode.RETURN,0)),List.of(),List.of()));
  List<ImmutableClassDef> classes=new ArrayList<>(List.of(clazz("Ltest/RejectingHandler;",handler,reject),clazz(A,"Landroid/app/Activity;",entry),
   new ImmutableClassDef(runnable,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(contract)),
   new ImmutableClassDef(impl,1,"Ljava/lang/Object;",List.of(runnable),null,Set.of(),captureless?List.of():List.of(field),List.of(ctor,run)),
   clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true))));
  addNetworkContracts(classes);
  var wrapper=method("Ltest/PostWrapper;","schedule",List.of(handler,runnable),9,2,List.of(invoke(Opcode.INVOKE_VIRTUAL,handler,"post",List.of(runnable),"Z",0,1),end()),false);
  classes.add(clazz("Ltest/PostWrapper;","Ljava/lang/Object;",wrapper));
  Path file=Files.createTempFile("async-registration-",".dex");
  try {
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   if(wrapped)check(!idx.relevant.contains(CapabilityIndex.key(wrapper)),"Fixture must require actual argument registration lookahead");
   var registrations=new AsyncRegistrations(idx);
   check(registrations.entries("Ltest/RejectingHandler;->post(Ljava/lang/Runnable;)Z").isEmpty(),"App override must not receive platform scheduling semantics");
   check(registrations.entries("Ltest/Unrelated;->post(Ljava/lang/Runnable;)Z").isEmpty(),"Matching method name is not framework identity");
   check(registrations.entries("Lokhttp3/ObfuscatedCall;->a(Lokhttp3/ObfuscatedCallback;)V").size()==1,"Structural obfuscated Call contract missing");
   check(registrations.entries("Lokhttp3/FalseCall;->a(Lokhttp3/FalseCallback;)V").isEmpty(),"Network names alone must not establish a registration");
   var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Actual posted runnable was lost");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   var bridges=facts.stream().filter(f->"bridge".equals(f.get("kind"))).toList();
   check(bridges.size()==1,"Only the registered runnable may execute: "+bridges);
   check(((Map<?,?>)bridges.get(0).get("webview")).get("id").toString().startsWith(captureless?impl+"->run()V@0|":A+"->onCreate()V@0|"),"Registered runnable receiver isolation failed");
  }finally{Files.deleteIfExists(file);}
 }
 static ImmutableMethod declaration(String owner,String name,List<String> parameters,String result){
  return new ImmutableMethod(owner,name,parameters.stream().map(t->new ImmutableMethodParameter(t,Set.of(),null)).toList(),result,0x401,Set.of(),Set.of(),null);
 }
 static void addNetworkContracts(List<ImmutableClassDef> classes){
  for(boolean valid:List.of(true,false)){
   String call=valid?"Lokhttp3/ObfuscatedCall;":"Lokhttp3/FalseCall;",callback=valid?"Lokhttp3/ObfuscatedCallback;":"Lokhttp3/FalseCallback;",response="Lokhttp3/ResponseCarrier;";
   classes.add(new ImmutableClassDef(call,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration(call,"a",List.of(callback),"V"))));
   classes.add(new ImmutableClassDef(callback,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration(callback,"b",List.of(call,"Ljava/io/IOException;"),"V"),declaration(callback,"c",List.of(call,valid?response:"Ljava/lang/String;"),"V"))));
  }
  classes.add(new ImmutableClassDef("Lokhttp3/ResponseCarrier;",1,"Ljava/lang/Object;",List.of("Ljava/io/Closeable;"),null,Set.of(),List.of(),List.of()));
 }

}
