package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.CapabilitySelfTest.*;

/** An ancestor-named super factory must use the invoking class's immediate parent. */
final class SuperReturnFixture {
 static void run()throws Exception {
  String base="Ltest/FactoryBase;",middle="Ltest/FactoryMiddle;",leaf="Ltest/FactoryLeaf;";
  var ancestor=AspectJFixture.returning(method(base,"make",List.of(),1,2,List.of(make(0,W),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),W);
  var parent=AspectJFixture.returning(method(middle,"make",List.of(),1,2,List.of(make(0,W),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),W);
  var override=AspectJFixture.returning(method(leaf,"make",List.of(),1,2,List.of(invoke(Opcode.INVOKE_SUPER,base,"make",List.of(),W,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),W);
  var entry=method(A,"onCreate",List.of(),1,4,List.of(make(0,leaf),invoke(Opcode.INVOKE_VIRTUAL,leaf,"make",List.of(),W,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),make(1,B),str(2,"super_factory"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  Path file=Files.createTempFile("super-return-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(base,"Ljava/lang/Object;",ancestor),clazz(middle,base,parent),clazz(leaf,middle,override),clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var apk=new ApkInventory();apk.targetSdk=30;
   var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Super-return host missing");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   var bridge=facts.stream().filter(f->"super_factory".equals(f.get("registration_name"))).findFirst().orElseThrow();
   String id=(String)((Map<?,?>)bridge.get("webview")).get("id");
   check(id.startsWith(middle+"->make()"),"Returned super dispatch used ancestor or redispatched recursively: "+id);
  }finally{Files.deleteIfExists(file);}
 }
}
