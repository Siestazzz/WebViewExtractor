package org.example;
import java.util.*;
import java.nio.file.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;
public class LifecycleFixture {
 public static void main(String[] args)throws Exception {
  String base="Ltest/BaseActivity;",page="Ltest/Page;",impl="Ltest/ConcretePage;",object="Ljava/lang/Object;";
  var pageField=new ImmutableField(base,"page",page,1,null,Set.of(),Set.of());var viewField=new ImmutableField(impl,"view",W,1,null,Set.of(),Set.of());
  var ctor=method(impl,"<init>",List.of(),1,4,List.of(make(0,W),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,3,new ImmutableFieldReference(impl,"view",W)),invoke(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),new ImmutableInstruction11n(Opcode.CONST_4,2,1),invoke(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",1,2),end()),false);
  var configure=method(impl,"configureLeaf",List.of(),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,3,new ImmutableFieldReference(impl,"view",W)),make(1,B),str(2,"page-bridge"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(object,"Ljava/lang/String;"),"V",0,1,2),end()),false);
  var c2=method(impl,"configure2",List.of(),1,1,List.of(invoke(Opcode.INVOKE_VIRTUAL,impl,"configureLeaf",List.of(),"V",0),end()),false);
  var c1=method(impl,"configure1",List.of(),1,1,List.of(invoke(Opcode.INVOKE_VIRTUAL,impl,"configure2",List.of(),"V",0),end()),false);
  var entryConfigure=method(impl,"configure",List.of(),1,1,List.of(invoke(Opcode.INVOKE_VIRTUAL,impl,"configure1",List.of(),"V",0),end()),false);
  var declaration=new ImmutableMethod(page,"configure",List.of(),"V",0x401,Set.of(),Set.of(),null);
  var setter=method(base,"setPage",List.of(page),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(base,"page",page)),end()),false);
  var getterBody=method(base,"getPage",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(base,"page",page)),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var getter=new ImmutableMethod(base,"getPage",List.of(),page,0x11,Set.of(),Set.of(),getterBody.getImplementation());
  var factoryBody=method(base,"createPage",List.of(),1,2,List.of(make(0,impl),invoke(Opcode.INVOKE_DIRECT,impl,"<init>",List.of(),"V",0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var factory=new ImmutableMethod(base,"createPage",List.of(),page,1,Set.of(),Set.of(),factoryBody.getImplementation());
  var parentHook=new ImmutableMethod(base,"onReady",List.of(new ImmutableMethodParameter(page,Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var override=method(A,"onReady",List.of(page),1,2,List.of(invoke(Opcode.INVOKE_INTERFACE,page,"configure",List.of(),"V",1),end()),false);
  var entry=method(base,"onCreate",List.of(),1,2,List.of(invoke(Opcode.INVOKE_VIRTUAL,base,"createPage",List.of(),page,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_VIRTUAL,base,"setPage",List.of(page),"V",1,0),invoke(Opcode.INVOKE_VIRTUAL,base,"getPage",List.of(),page,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_VIRTUAL,base,"onReady",List.of(page),"V",1,0),end()),false);
  var defs=List.of(clazz(A,base,override),new ImmutableClassDef(base,0x401,"Landroid/app/Activity;",List.of(),null,Set.of(),List.of(pageField),List.of(entry,setter,getter,factory,parentHook)),new ImmutableClassDef(page,0x601,object,List.of(),null,Set.of(),List.of(),List.of(declaration)),new ImmutableClassDef(impl,1,object,List.of(page),null,Set.of(),List.of(viewField),List.of(ctor,configure,c1,c2,entryConfigure)),clazz(B,object,method(B,"exposed",List.of(),1,1,List.of(end()),true)));
  Path dex=Files.createTempFile("wv-lifecycle-",".dex");
  try{DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),defs));long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   System.out.println("setterRelevant="+idx.relevant.contains(CapabilityIndex.key(setter))+", getterRelevant="+idx.relevant.contains(CapabilityIndex.key(getter)));
   @SuppressWarnings("unchecked")var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   
   check(facts.stream().anyMatch(f->"page-bridge".equals(f.get("registration_name"))),"Base lifecycle setter/getter lost concrete page argument to subclass override");
  }finally{Files.deleteIfExists(dex);}
 }
}
