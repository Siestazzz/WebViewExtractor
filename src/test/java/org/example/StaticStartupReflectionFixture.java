package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;

/** Actual static lookup/invoke must execute registration; a Class/name alone must not. */
final class StaticStartupReflectionFixture {
 public static void main(String[] args)throws Exception{run();}
 static ImmutableDexFile variant(String variant){
  var base=ServiceStartupRegistrationProbe.buildStartup("array",variant.equals("direct")?"direct":"reflect");
  if(variant.equals("direct")||variant.equals("reflect"))return base;
  List<ClassDef> classes=new ArrayList<>();
  for(ClassDef type:base.getClasses()){
   if(type.getType().equals(ServiceStartupRegistrationProbe.BOOT)&&variant.equals("private_class")){classes.add(new ImmutableClassDef(type.getType(),0,type.getSuperclass(),type.getInterfaces(),null,type.getAnnotations(),type.getFields(),type.getMethods()));continue;}
   if(type.getType().equals(ServiceStartupRegistrationProbe.BOOT)&&Set.of("private_method","instance_method").contains(variant)){
    List<ImmutableMethod> changed=new ArrayList<>();for(Method method:type.getMethods())changed.add(new ImmutableMethod(method.getDefiningClass(),method.getName(),method.getParameters(),method.getReturnType(),variant.equals("private_method")?10:1,method.getAnnotations(),method.getHiddenApiRestrictions(),method.getImplementation()));
    classes.add(new ImmutableClassDef(type.getType(),type.getAccessFlags(),type.getSuperclass(),type.getInterfaces(),null,type.getAnnotations(),type.getFields(),changed));continue;
   }
   if(!type.getType().equals(ServiceStartupRegistrationProbe.START)){classes.add(type);continue;}
   List<ImmutableMethod> methods=new ArrayList<>();for(Method method:type.getMethods()){
    List<org.jf.dexlib2.iface.instruction.Instruction> instructions=new ArrayList<>();method.getImplementation().getInstructions().forEach(instructions::add);
    if(variant.equals("wrong_name"))instructions.set(1,ServiceConstructorArrayProbe.str(1,"notInit"));
    if(variant.equals("no_invoke"))instructions.remove(instructions.size()-2);
    if(variant.equals("wrong_arity")){instructions.set(7,new ImmutableInstruction11n(Opcode.CONST_4,2,1));instructions.add(9,new ImmutableInstruction23x(Opcode.APUT_OBJECT,1,2,1));}
    if(variant.equals("wrong_parameters"))instructions.set(2,new ImmutableInstruction11n(Opcode.CONST_4,2,1));
    methods.add(new ImmutableMethod(method.getDefiningClass(),method.getName(),method.getParameters(),method.getReturnType(),method.getAccessFlags(),method.getAnnotations(),method.getHiddenApiRestrictions(),new ImmutableMethodImplementation(method.getImplementation().getRegisterCount(),instructions,List.of(),List.of())));
   }classes.add(new ImmutableClassDef(type.getType(),type.getAccessFlags(),type.getSuperclass(),type.getInterfaces(),null,type.getAnnotations(),type.getFields(),methods));
  }return new ImmutableDexFile(Opcodes.getDefault(),classes);
 }
 static void run()throws Exception{
  for(String variant:List.of("direct","reflect","wrong_name","no_invoke","private_method","instance_method","wrong_arity","wrong_parameters","private_class")){
   Path dex=Files.createTempFile("static-startup-",".dex");try{
    DexFileFactory.writeDexFile(dex.toString(),variant(variant));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("probe.Activity");var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("probe.Activity");
    Set<String> kinds=new HashSet<>();for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)kinds.add((String)fact.get("kind"));}
    boolean positive=Set.of("direct","reflect").contains(variant);if(positive?!kinds.containsAll(Set.of("bridge","setting","callback")):!Collections.disjoint(kinds,Set.of("bridge","setting","callback")))throw new AssertionError("Static startup "+variant+" capabilities="+kinds+" reports="+engine.activities);
   }finally{Files.deleteIfExists(dex);}
  }
  System.out.println("StaticStartupReflectionFixture PASS: actual exact public static lookup+wrapper invoke executes registration; wrong-name and lookup-only do not.");
 }
}
