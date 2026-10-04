package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Generic differential extension: startup-held global registry and static reflective initialization. */
public final class ServiceStartupRegistrationProbe {
 static final String HOLD="Lprobe/Global;",BOOT="Lprobe/GeneratedLoader;",START="Lprobe/Startup;",APP="Lprobe/Application;",METHOD="Ljava/lang/reflect/Method;",MON="Lprobe/ReflectionWrapper;";
 static ImmutableDexFile buildStartup(String creator,String startup){
  var original=build(creator,true,true);List<ClassDef> defs=new ArrayList<>();var entry=original.getClasses().stream().filter(c->c.getType().equals(A)).findFirst().orElseThrow().getMethods().iterator().next();List<Instruction> code=new ArrayList<>();entry.getImplementation().getInstructions().forEach(code::add);
  List<Instruction> initialize=new ArrayList<>(code.subList(0,7));initialize.add(new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(HOLD,"registry",REG)));initialize.add(end());
  defs.add(clazz(HOLD,OBJ,List.of(),List.of(new ImmutableField(HOLD,"registry",REG,9,null,Set.of(),Set.of()))));
  defs.add(clazz(BOOT,OBJ,List.of(),List.of(),method(BOOT,"init",List.of(),"V",9,4,initialize,false)));
  var wrapper=method(MON,"invoke",List.of(METHOD,OBJ,"[Ljava/lang/Object;"),OBJ,9,4,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,1),call(Opcode.INVOKE_VIRTUAL,METHOD,"setAccessible",List.of("Z"),"V",1,0),call(Opcode.INVOKE_VIRTUAL,METHOD,"invoke",List.of(OBJ,"[Ljava/lang/Object;"),OBJ,1,2,3),result(0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);defs.add(clazz(MON,OBJ,List.of(),List.of(),wrapper));
  List<Instruction> activate=new ArrayList<>();
  if(startup.contains("reflect")){activate.add(klass(0,BOOT));activate.add(str(1,"init"));activate.add(zero(2));activate.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY,2,2,new ImmutableTypeReference("[Ljava/lang/Class;")));activate.add(call(Opcode.INVOKE_VIRTUAL,CLS,"getMethod",List.of("Ljava/lang/String;","[Ljava/lang/Class;"),METHOD,0,1,2));activate.add(result(0));activate.add(zero(1));activate.add(zero(2));activate.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY,2,2,new ImmutableTypeReference("[Ljava/lang/Object;")));if(!startup.endsWith("noinvoke"))activate.add(call(Opcode.INVOKE_STATIC,MON,"invoke",List.of(METHOD,OBJ,"[Ljava/lang/Object;"),OBJ,0,1,2));}
  else activate.add(call(Opcode.INVOKE_STATIC,BOOT,"init",List.of(),"V"));activate.add(end());defs.add(clazz(START,OBJ,List.of(),List.of(),method(START,"activate",List.of(),"V",9,3,activate,false)));
  defs.add(clazz(APP,"Landroid/app/Application;",List.of(),List.of(),method(APP,"attachBaseContext",List.of("Landroid/content/Context;"),"V",1,2,List.of(call(Opcode.INVOKE_STATIC,START,"activate",List.of(),"V"),end()),false)));
  List<Instruction> current=new ArrayList<>();if(!startup.startsWith("application"))current.add(call(Opcode.INVOKE_STATIC,START,"activate",List.of(),"V"));current.add(new ImmutableInstruction21c(Opcode.SGET_OBJECT,0,new ImmutableFieldReference(HOLD,"registry",REG)));current.add(klass(1,API));current.addAll(code.subList(7,code.size()));
  for(var c:original.getClasses())if(c.getType().equals(A))defs.add(clazz(A,"Landroid/app/Activity;",List.of(),List.of(field(A,"player",PRODUCT)),method(A,"onCreate",List.of(),"V",1,7,current,false)));else defs.add(c);
  return new ImmutableDexFile(Opcodes.getDefault(),defs);
 }
 public static void main(String[]args)throws Exception{
  for(String creator:List.of("direct","classconstant","array"))for(String startup:List.of("direct","reflect","reflect-noinvoke","application-direct","application-reflect")){
   String name=creator+"/"+startup;Path dex=Files.createTempFile("startup-probe-",".dex");try{DexFileFactory.writeDexFile(dex.toString(),buildStartup(creator,startup));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("probe.Activity");var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("probe.Activity");System.out.println(name+"\t"+engine.activities);System.out.println(name+" diagnostics\t"+engine.diagnostics);}finally{Files.deleteIfExists(dex);}
  }
 }
}
