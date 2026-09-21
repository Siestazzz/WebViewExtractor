package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.CapabilitySelfTest.*;

/** getValue executes only its own initializer, including pre-return capability calls. */
final class LazyInvocationFixture {
 static void run()throws Exception{
  String fn="Lkotlin/jvm/functions/Function0;",init="Ltest/DeferredWebView;",lazy="Lkotlin/Lazy;",factory="Lkotlin/LazyKt;";
  var ctor=method(init,"<init>",List.of(),1,1,List.of(end()),false);
  var invoke=new ImmutableMethod(init,"invoke",List.of(),"Ljava/lang/Object;",1,Set.of(),Set.of(),new ImmutableMethodImplementation(4,List.of(make(0,W),make(1,B),str(2,"lazy-used"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),List.of(),List.of()));
  var entry=method(A,"onCreate",List.of(),1,5,List.of(
   make(0,init),invoke(Opcode.INVOKE_DIRECT,init,"<init>",List.of(),"V",0),invoke(Opcode.INVOKE_STATIC,factory,"lazy",List.of(fn),lazy,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),
   make(2,init),invoke(Opcode.INVOKE_DIRECT,init,"<init>",List.of(),"V",2),invoke(Opcode.INVOKE_STATIC,factory,"lazy",List.of(fn),lazy,2),
   invoke(Opcode.INVOKE_INTERFACE,lazy,"getValue",List.of(),"Ljava/lang/Object;",1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),str(3,"https://fixture.invalid"),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",0,3),end()),false);
  var declaration=new ImmutableMethod(fn,"invoke",List.of(),"Ljava/lang/Object;",0x401,Set.of(),Set.of(),null);
  Path path=Files.createTempFile("lazy-invocation-",".dex");
  try{
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(fn,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),new ImmutableClassDef(init,1,"Ljava/lang/Object;",List.of(fn),null,Set.of(),List.of(),List.of(ctor,invoke)),clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().filter(f->"lazy-used".equals(f.get("registration_name"))).count()==1,"Lazy initializer body lost or unused initializer executed");
  }finally{Files.deleteIfExists(path);}
 }
}
