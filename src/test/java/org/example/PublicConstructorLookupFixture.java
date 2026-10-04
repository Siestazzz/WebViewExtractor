package org.example;
import java.nio.file.*;import java.util.*;import org.jf.dexlib2.*;import org.jf.dexlib2.iface.Method;import org.jf.dexlib2.immutable.*;import org.jf.dexlib2.immutable.instruction.*;import org.jf.dexlib2.immutable.reference.*;import static org.example.ServiceConstructorArrayProbe.*;
/** Actual field-backed Class, precise public constructor lookup and subsequent interface dispatch. */
final class PublicConstructorLookupFixture {
 static ImmutableDexFile build(boolean map,boolean cached,boolean missing,boolean wrongTypes){
  var input=ServiceConstructorArrayProbe.build("class"+(missing?"missingkey":""),map,cached);List<ImmutableClassDef> defs=new ArrayList<>();
  for(var cls:input.getClasses()){
   if(!cls.getType().equals(CREATOR)){defs.add(ImmutableClassDef.of(cls));continue;}
   List<Method> methods=new ArrayList<>();for(var m:cls.getMethods())if(!m.getName().equals("create"))methods.add(m);
   methods.add(method(CREATOR,"create",List.of(CLS),OBJ,1,6,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,wrongTypes?1:0),new ImmutableInstruction22c(Opcode.NEW_ARRAY,0,0,new ImmutableTypeReference("[Ljava/lang/Class;")),call(Opcode.INVOKE_VIRTUAL,CLS,"getConstructor",List.of("[Ljava/lang/Class;"),CTOR,5,0),result(1),zero(0),new ImmutableInstruction22c(Opcode.NEW_ARRAY,0,0,new ImmutableTypeReference("[Ljava/lang/Object;")),call(Opcode.INVOKE_VIRTUAL,CTOR,"newInstance",List.of("[Ljava/lang/Object;"),OBJ,1,0),result(0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false));
   defs.add(new ImmutableClassDef(CREATOR,1,OBJ,List.of(ICREATOR),null,Set.of(),List.of(),methods));
  }return new ImmutableDexFile(Opcodes.getDefault(),defs);
 }
 static void run()throws Exception{
  for(boolean map:new boolean[]{false,true})for(boolean cache:new boolean[]{false,true})for(String mode:map?List.of("actual","missing","wrong-types"):List.of("actual","wrong-types")){
   Path path=Files.createTempFile("public-constructor-lookup-",".dex");try{
    DexFileFactory.writeDexFile(path.toString(),build(map,cache,mode.equals("missing"),mode.equals("wrong-types")));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(path,deadline);var apk=new ApkInventory();apk.activities.add("probe.Activity");var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("probe.Activity");Set<String> kinds=new HashSet<>();for(var activity:engine.activities)for(Object fact:(List<?>)activity.get("facts"))kinds.add(String.valueOf(((Map<?,?>)fact).get("kind")));
    if(mode.equals("actual")?!kinds.containsAll(Set.of("bridge","setting","callback")):!Collections.disjoint(kinds,Set.of("bridge","setting","callback")))throw new AssertionError("Public constructor actual chain: map="+map+" cache="+cache+" mode="+mode+" kinds="+kinds);
   }finally{Files.deleteIfExists(path);}
  }
  System.out.println("PublicConstructorLookupFixture PASS: actual captured Class, exact empty lookup, interface factory and three categories, cache/map and missing/nonempty negatives.");
 }
 public static void main(String[]args)throws Exception{run();}
}
