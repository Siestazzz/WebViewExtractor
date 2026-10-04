package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.ServiceConstructorArrayProbe.*;
/** Discovery must preserve actual registry writers without decoding unrelated Map users. */
public final class ReflectionDiscoveryPrefilterFixture {
 static final String R=InstalledReflectionFixture.R, MAP=InstalledReflectionFixture.MAP, NOISE="Lprefilter/Unrelated;";
 static void run()throws Exception{
  List<ImmutableMethod> noise=new ArrayList<>();for(int i=0;i<64;i++)noise.add(method(NOISE,"put"+i,List.of(MAP,OBJ,OBJ),"V",9,3,List.of(call(Opcode.INVOKE_VIRTUAL,MAP,"put",List.of(OBJ,OBJ),OBJ,0,1,2),end()),false));
  noise.add(method(NOISE,"fieldPut",List.of(OBJ,OBJ),"V",1,4,List.of(get(0,1,NOISE,"other",MAP),call(Opcode.INVOKE_VIRTUAL,MAP,"put",List.of(OBJ,OBJ),OBJ,0,2,3),end()),false));
  var good=method(R,"register",List.of(OBJ,OBJ),"V",1,4,List.of(get(0,1,R,"objects",MAP),call(Opcode.INVOKE_VIRTUAL,MAP,"put",List.of(OBJ,OBJ),OBJ,0,2,3),end()),false);
  Path dex=Files.createTempFile("reflection-prefilter-",".dex");try{
   DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(R,OBJ,List.of(),List.of(field(R,"objects",MAP)),InstalledReflectionFixture.dispatch(true),good),clazz(NOISE,OBJ,List.of(),List.of(field(NOISE,"other",MAP)),noise.toArray(ImmutableMethod[]::new)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var flow=new DexFlow(idx,deadline);var reflection=new ReflectionProtocols(idx,flow);
   if(!reflection.writer(idx.resolve(CapabilityIndex.key(good))))throw new AssertionError("Actual certified-field writer lost");
   for(var m:noise){String key=CapabilityIndex.key(m);if(reflection.writer(idx.resolve(key)))throw new AssertionError("Unrelated Map became writer");if(flow.cache.containsKey(key))throw new AssertionError("Unrelated Map decoded: "+key);}
   if(!flow.cache.containsKey(CapabilityIndex.key(good)))throw new AssertionError("Positive writer not decoded");
   System.out.println("ReflectionDiscoveryPrefilterFixture PASS: certified writer retained;65 unrelated Map methods not decoded.");
  }finally{Files.deleteIfExists(dex);}
 }
 public static void main(String[]args)throws Exception{run();}
}
