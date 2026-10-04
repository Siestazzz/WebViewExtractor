package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;
final class ManifestExpressionDiagnosticFixture {
 static final String CONTEXT="Landroid/content/Context;",PM="Landroid/content/pm/PackageManager;",INFO="Landroid/content/pm/ApplicationInfo;",BUNDLE="Landroid/os/Bundle;",SET="Ljava/util/Set;",ITER="Ljava/util/Iterator;",CLASS="Ljava/lang/Class;",MODULE="Lmanifest/Module;";
 static ImmutableMethod loader(){
  return method("Lmanifest/Loader;","load",List.of(CONTEXT,W),9,12,List.of(invoke(Opcode.INVOKE_VIRTUAL,CONTEXT,"getPackageManager",List.of(),PM,10),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_VIRTUAL,CONTEXT,"getPackageName",List.of(),"Ljava/lang/String;",10),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),new ImmutableInstruction21s(Opcode.CONST_16,2,128),invoke(Opcode.INVOKE_VIRTUAL,PM,"getApplicationInfo",List.of("Ljava/lang/String;","I"),INFO,0,1,2),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction22c(Opcode.IGET_OBJECT,3,0,new ImmutableFieldReference(INFO,"metaData",BUNDLE)),invoke(Opcode.INVOKE_VIRTUAL,BUNDLE,"keySet",List.of(),SET,3),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_INTERFACE,SET,"iterator",List.of(),ITER,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_INTERFACE,ITER,"next",List.of(),"Ljava/lang/Object;",0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference("Ljava/lang/String;")),invoke(Opcode.INVOKE_STATIC,CLASS,"forName",List.of("Ljava/lang/String;"),CLASS,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_VIRTUAL,CLASS,"newInstance",List.of(),"Ljava/lang/Object;",1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference(MODULE)),invoke(Opcode.INVOKE_INTERFACE,MODULE,"apply",List.of(W),"V",1,11),end()),false);
 }
 public static void main(String[] args)throws Exception{
  Path dex=Files.createTempFile("manifest-expression-",".dex");try{var method=loader();DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz("Lmanifest/Loader;","Ljava/lang/Object;",method))));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var summary=new DexFlow(idx,deadline).summary(idx.resolve("Lmanifest/Loader;->load("+CONTEXT+W+")V"));for(var call:summary.calls())if(call.method().contains("->forName(")||call.method().contains("->newInstance(")||call.method().contains("->apply("))System.out.println(call.method()+" args="+call.args());}finally{Files.deleteIfExists(dex);}
 }
}
