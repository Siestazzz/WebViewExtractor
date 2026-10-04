package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** A reached capability must not turn an incidental Class holder into a global factory root. */
final class ClassCarrierRelevanceFixture {
 static final String HOLDER="Lcarrier/Metadata;",FACTORY="Lcarrier/Factory;",CONTROLLER="Lcarrier/Controller;",UNRELATED="Lcarrier/Unrelated;";
 public static void main(String[] args)throws Exception{run();}
 static void run()throws Exception{
  var constructor=method(HOLDER,"<init>",List.of(CLS),"V",0x10001,2,List.of(put(1,0,HOLDER,"clazz",CLS),end()),false);
  var factory=method(FACTORY,"make",List.of(),HOLDER,9,2,List.of(klass(1,OBJ),make(0,HOLDER),call(Opcode.INVOKE_DIRECT,HOLDER,"<init>",List.of(CLS),"V",0,1),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var writer=method(CONTROLLER,"prepareUnrelatedMetadata",List.of(),"V",1,2,List.of(call(Opcode.INVOKE_STATIC,FACTORY,"make",List.of(),HOLDER),result(0),put(0,1,CONTROLLER,"metadata",HOLDER),end()),false);
  var seed=method(CONTROLLER,"configure",List.of(W),"V",1,3,List.of(get(0,1,CONTROLLER,"metadata",HOLDER),call(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,2),result(0),new ImmutableInstruction11n(Opcode.CONST_4,1,1),call(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",0,1),end()),false);
  var caller=method(UNRELATED,"unusedFactoryCaller",List.of(),HOLDER,9,1,List.of(call(Opcode.INVOKE_STATIC,FACTORY,"make",List.of(),HOLDER),result(0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  Path file=Files.createTempFile("class-carrier-relevance-",".dex");try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(HOLDER,OBJ,List.of(),List.of(field(HOLDER,"clazz",CLS)),constructor),clazz(FACTORY,OBJ,List.of(),List.of(),factory),clazz(CONTROLLER,OBJ,List.of(),List.of(field(CONTROLLER,"metadata",HOLDER)),writer,seed),clazz(UNRELATED,OBJ,List.of(),List.of(),caller))));
   var index=new CapabilityIndex();index.read(file,System.nanoTime()+30_000_000_000L);
   if(!index.relevant.contains(CapabilityIndex.key(seed)))throw new AssertionError("Actual capability seed was lost");
   Set<String> unrelated=Set.of(CapabilityIndex.key(writer),CapabilityIndex.key(factory),CapabilityIndex.key(caller));
   for(String method:unrelated)if(index.relevant.contains(method))throw new AssertionError("Class-only metadata globally expanded unrelated factory relevance: "+method+" relevant="+index.relevant);
   if(index.bindingObjects.contains("carrier.Metadata"))throw new AssertionError("Class-only holder is still a global capability binding object");
  }finally{Files.deleteIfExists(file);}
  // Positive counterpart exercises reached Class capture through ordinary and nested registry
  // service construction, with public Class/Constructor reflection and real WebView capabilities.
  ServiceConstructorArrayProbe.runRegression();
  System.out.println("ClassCarrierRelevanceFixture PASS: incidental Class metadata stays out of capability relevance; reached service captures retain bridge/setting/callback and absent-key reflection negatives.");
 }
}
