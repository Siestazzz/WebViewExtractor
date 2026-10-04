package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.ServiceConstructorArrayProbe.*;
/** A reached ordinary controller captures its actual stable SDK manager argument. */
final class FragmentManagerCaptureFixture {
 static final String HOLDER="Lmanagercapture/Controller;";
 static void run()throws Exception{
  for(String mode:List.of("packaged-sdk-bodies","application-override")){
   String host=PackagedFragmentProtocolProbe.ACT,sdk=PackagedFragmentProtocolProbe.FA,manager=PackagedFragmentProtocolProbe.FM,tx=PackagedFragmentProtocolProbe.TX,fragment=PackagedFragmentProtocolProbe.FRAG,content=PackagedFragmentProtocolProbe.F,bundle=PackagedFragmentProtocolProbe.BUNDLE;
   List<ClassDef> defs=new ArrayList<>();
   for(ClassDef c:PackagedFragmentProtocolProbe.build(mode).getClasses()){
    if(!c.getType().equals(host)){defs.add(c);continue;}
    List<Method> methods=new ArrayList<>();for(Method m:c.getMethods())if(!m.getName().equals("onCreate"))methods.add(m);
    methods.add(method(host,"onCreate",List.of(bundle),"V",1,8,List.of(call(Opcode.INVOKE_VIRTUAL,sdk,"getSupportFragmentManager",List.of(),manager,6),result(0),make(1,HOLDER),call(Opcode.INVOKE_DIRECT,HOLDER,"<init>",List.of(manager),"V",1,0),make(2,content),call(Opcode.INVOKE_DIRECT,content,"<init>",List.of(),"V",2),call(Opcode.INVOKE_VIRTUAL,HOLDER,"install",List.of(fragment),"V",1,2),end()),false));
    defs.add(new ImmutableClassDef(c.getType(),c.getAccessFlags(),c.getSuperclass(),c.getInterfaces(),c.getSourceFile(),c.getAnnotations(),List.of(),methods));
   }
   defs.add(clazz(HOLDER,OBJ,List.of(),List.of(field(HOLDER,"manager",manager)),method(HOLDER,"<init>",List.of(manager),"V",0x10001,2,List.of(put(1,0,HOLDER,"manager",manager),end()),false),method(HOLDER,"install",List.of(fragment),"V",1,5,List.of(get(0,3,HOLDER,"manager",manager),call(Opcode.INVOKE_VIRTUAL,manager,"beginTransaction",List.of(),tx,0),result(0),str(1,"installed"),call(Opcode.INVOKE_VIRTUAL,tx,"add",List.of(fragment,"Ljava/lang/String;"),tx,0,4,1),call(Opcode.INVOKE_VIRTUAL,tx,"commit",List.of(),"I",0),end()),false)));
   Path path=Files.createTempFile("manager-capture-",".dex");try{
    DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),defs));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);Set<String> relevant=Set.copyOf(idx.relevant);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var state=engine.beginActivity("packaged.Host");var h=state.host;
    while(!state.done&&System.nanoTime()<deadline)engine.advanceActivity(state,1_000_000_000L,100);
    Set<String> kinds=new HashSet<>();for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)kinds.add(String.valueOf(fact.get("kind")));}
    if(mode.equals("application-override")?!Collections.disjoint(kinds,Set.of("bridge","setting","callback")):!kinds.containsAll(Set.of("bridge","setting","callback")))throw new AssertionError("Actual manager capture boundary: "+mode+" "+kinds+" "+engine.diagnostics);
    if(h.allocationCaptures.stream().noneMatch(key->key.contains(HOLDER+"-><init>")))throw new AssertionError("Fixture bypassed ordinary allocation capture");
    if(!idx.relevant.equals(relevant))throw new AssertionError("Manager argument capture expanded global relevance");
   }finally{Files.deleteIfExists(path);}
  }
  System.out.println("FragmentManagerCaptureFixture PASS: actual SDK acquisition captured by ordinary controller, application-null override remains unknown, unchanged global relevance.");
 }
}
