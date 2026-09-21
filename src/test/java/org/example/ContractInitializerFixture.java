package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** A reached interface initializer is distinct from constructing its implementation. */
final class ContractInitializerFixture {
 static void run()throws Exception{
  String contract="Ltest/Page;",page="Ltest/ConcretePage;",wrapper="Ltest/WebWrapper;";
  var field=new ImmutableField(A,"page",contract,1,null,Set.of(),Set.of());
  var init=method(wrapper,"initialize",List.of(W),1,4,List.of(make(0,B),str(1,"initialized"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
  var wrapperCtor=method(wrapper,"<init>",List.of(W),1,2,List.of(invoke(Opcode.INVOKE_VIRTUAL,wrapper,"initialize",List.of(W),"V",0,1),end()),false);
  var pageInit=method(page,"initUI",List.of(),1,3,List.of(make(0,W),make(1,wrapper),invoke(Opcode.INVOKE_DIRECT,wrapper,"<init>",List.of(W),"V",1,0),end()),false);
  var pageCtor=method(page,"<init>",List.of(),1,1,List.of(end()),false);
  var setPage=method(A,"initPage",List.of(),1,2,List.of(make(0,page),invoke(Opcode.INVOKE_DIRECT,page,"<init>",List.of(),"V",0),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(A,"page",contract)),end()),false);
  var entry=method(A,"onCreate",List.of(),1,2,List.of(invoke(Opcode.INVOKE_VIRTUAL,A,"initPage",List.of(),"V",1),new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(A,"page",contract)),invoke(Opcode.INVOKE_INTERFACE,contract,"initUI",List.of(),"V",0),end()),false);
  var declaration=new ImmutableMethod(contract,"initUI",List.of(),"V",0x401,Set.of(),Set.of(),null);
  List<ImmutableClassDef> classes=List.of(new ImmutableClassDef(A,1,"Landroid/app/Activity;",List.of(),null,Set.of(),List.of(field),List.of(entry,setPage)),new ImmutableClassDef(contract,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),new ImmutableClassDef(page,1,"Ljava/lang/Object;",List.of(contract),null,Set.of(),List.of(),List.of(pageCtor,pageInit)),clazz(wrapper,"Ljava/lang/Object;",wrapperCtor,init),clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)));
  Path path=Files.createTempFile("contract-initializer-",".dex");
  try{
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);
   check(idx.relevant.contains(CapabilityIndex.key(entry)),"Relevant concrete implementation did not reach its actual contract caller");
   var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Concrete page initializer was not dispatched from its actual field value");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().filter(f->"initialized".equals(f.get("registration_name"))).count()==1,"Initializer must execute on the actual wrapper/WebView only");
  }finally{Files.deleteIfExists(path);}
 }
}
