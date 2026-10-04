package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** A framework restore implementation is not a globally relevant factory carrier. */
final class FrameworkFactoryBoundaryFixture {
 public static void main(String[] args)throws Exception{run();}
 static void run()throws Exception{
  String manager="Landroidx/fragment/app/FragmentManager;",F="Landroidx/fragment/app/Fragment;";
  var field=new ImmutableField(manager,"restoredName","Ljava/lang/String;",1,null,Set.of(),Set.of());var ref=new ImmutableFieldReference(manager,"restoredName","Ljava/lang/String;");
  var ctor=method(manager,"<init>",List.of("Ljava/lang/String;"),0x10001,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,ref),end()),false);
  var restore=method(manager,"restoreState",List.of("Landroid/content/Context;","Landroid/os/Bundle;"),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,ref),invoke(Opcode.INVOKE_STATIC,F,"instantiate",List.of("Landroid/content/Context;","Ljava/lang/String;","Landroid/os/Bundle;"),F,2,0,3),end()),false);
  var state=method(manager,"dispatchState",List.of("Landroid/content/Context;","Landroid/os/Bundle;"),1,3,List.of(invoke(Opcode.INVOKE_VIRTUAL,manager,"restoreState",List.of("Landroid/content/Context;","Landroid/os/Bundle;"),"V",0,1,2),end()),false);
  var entry=method(A,"onCreate",List.of("Landroid/os/Bundle;"),1,4,List.of(make(0,manager),str(1,"unknown.RestoredFragment"),invoke(Opcode.INVOKE_DIRECT,manager,"<init>",List.of("Ljava/lang/String;"),"V",0,1),invoke(Opcode.INVOKE_VIRTUAL,manager,"dispatchState",List.of("Landroid/content/Context;","Landroid/os/Bundle;"),"V",0,2,3),end()),false);
  Path file=Files.createTempFile("framework-factory-boundary-",".dex");try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(manager,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(field),List.of(ctor,restore,state)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   check(!idx.relevant.contains(CapabilityIndex.key(restore))&&!idx.relevant.contains(CapabilityIndex.key(state))&&!idx.relevant.contains(CapabilityIndex.key(entry)),"Framework restore factory forced a global state-machine relevance root");
   check(!idx.bindingObjects.contains("androidx.fragment.app.FragmentManager")&&!idx.fragmentFactoryFields.contains(CapabilityIndex.field(ref)),"Framework restore fields/carrier leaked into global constructor relevance");
  }finally{Files.deleteIfExists(file);}
  System.out.println("FrameworkFactoryBoundaryFixture PASS: SDK restore factory creates no global relevance root, state-machine closure or factory-field carrier.");
 }
}
