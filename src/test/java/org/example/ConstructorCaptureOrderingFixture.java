package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.DexFlow.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Temporal counterexamples: unknown is permitted, a later concrete object is not. */
public final class ConstructorCaptureOrderingFixture {
 static final String H="Lordering/Holder;",F="Lordering/Capture;",BASE="Lordering/BaseCapture;",HELPER="Lordering/Helper;",SUB="Lordering/SubHelper;",OBJ="Ljava/lang/Object;";
 static ImmutableDexFile build(String mode){
  List<ImmutableClassDef> classes=new ArrayList<>();
  List<Instruction> ctor=new ArrayList<>(),caller=new ArrayList<>();
  String slot=mode.equals("super")?BASE:F;
  if(mode.equals("body")||mode.equals("super")){
   if(mode.equals("super"))ctor.add(call(Opcode.INVOKE_DIRECT,BASE,"<init>",List.of(OBJ),"V",2,3));
   else ctor.add(put(3,2,F,"slot",OBJ));
   ctor.add(get(0,2,slot,"slot",OBJ));ctor.add(put(4,2,slot,"slot",OBJ));ctor.add(put(0,2,F,"saved",OBJ));ctor.add(end());
   caller.add(make(0,F));caller.add(call(Opcode.INVOKE_DIRECT,F,"<init>",List.of(OBJ,OBJ),"V",0,9,10));
  }else{
   ctor.add(put(1,0,F,"saved",OBJ));ctor.add(end());
   caller.add(get(1,8,H,"root",OBJ));
   if(mode.equals("indirect"))caller.add(call(Opcode.INVOKE_STATIC,HELPER,"forward",List.of(H,OBJ),"V",8,10));
   if(mode.equals("override"))caller.add(call(Opcode.INVOKE_VIRTUAL,HELPER,"mutate",List.of(H,OBJ),"V",11,8,10));
   if(mode.equals("inherited"))caller.add(call(Opcode.INVOKE_VIRTUAL,SUB,"mutate",List.of(H,OBJ),"V",11,8,10));
   caller.add(make(0,F));caller.add(call(Opcode.INVOKE_DIRECT,F,"<init>",List.of(OBJ),"V",0,1));
  }
  caller.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0));
  boolean pair=mode.equals("body")||mode.equals("super");
  classes.add(clazz(F,mode.equals("super")?BASE:OBJ,List.of(),List.of(field(F,"saved",OBJ),field(F,"slot",OBJ)),method(F,"<init>",pair?List.of(OBJ,OBJ):List.of(OBJ),"V",0x10001,pair?5:2,ctor,false)));
  classes.add(clazz(BASE,OBJ,List.of(),List.of(field(BASE,"slot",OBJ)),method(BASE,"<init>",List.of(OBJ),"V",0x10001,2,List.of(put(1,0,BASE,"slot",OBJ),end()),false)));
  classes.add(clazz(H,OBJ,List.of(),List.of(field(H,"root",OBJ)),method(H,"build",List.of(H,OBJ,OBJ,SUB),F,9,12,caller,false)));
  classes.add(clazz(HELPER,OBJ,List.of(),List.of(),method(HELPER,"mutate",List.of(H,OBJ),"V",1,3,(mode.equals("override")?List.of(end()):List.of(put(2,1,H,"root",OBJ),end())),false),method(HELPER,"write",List.of(H,OBJ),"V",9,2,List.of(put(1,0,H,"root",OBJ),end()),false),method(HELPER,"forward",List.of(H,OBJ),"V",9,2,List.of(call(Opcode.INVOKE_STATIC,HELPER,"write",List.of(H,OBJ),"V",0,1),end()),false)));
  classes.add(mode.equals("override")?clazz(SUB,HELPER,List.of(),List.of(),method(SUB,"mutate",List.of(H,OBJ),"V",1,3,List.of(put(2,1,H,"root",OBJ),end()),false)):clazz(SUB,HELPER,List.of(),List.of()));
  return new ImmutableDexFile(Opcodes.getDefault(),classes);
 }
 static void check(String mode)throws Exception{
  Path dex=Files.createTempFile("constructor-order-",".dex");try{
   DexFileFactory.writeDexFile(dex.toString(),build(mode));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);var host=engine.new Host("ordering.Test");var method=index.resolve(H+"->build("+H+OBJ+OBJ+SUB+")"+F);
   V holder=V.of("object","ordering.Holder","holder"),old=V.of("object","ordering.Old","old"),next=V.of("object","ordering.Next","next"),helper=V.of("object","ordering.SubHelper","helper");
   // Model the actual allocation-time heap. The register load occurred before
   // the preceding helper changed Holder.root; the constructor must not reread it.
   host.heap.put(CapabilityEngine.heapKey(H+"->root:"+OBJ,holder),mode.equals("indirect")||mode.equals("inherited")||mode.equals("override")?next:old);
   var job=new CapabilityEngine.Job(method,List.of(holder,old,next,helper),List.of(mode),false);
   var ctorCall=engine.flow.summary(method).calls().stream().filter(c->c.method().startsWith(F+"-><init>")).findFirst().orElseThrow();
   V instance=engine.eval(ctorCall.args().get(0),job,host,0,new HashSet<>());
   V saved=engine.eval(expr("field","java.lang.Object",F+"->saved:"+OBJ,List.of(instance)),job,host,0,new HashSet<>());
   System.out.println(mode+" saved="+saved+" gaps="+host.gaps);
   if(!saved.equals(old)&&!saved.kind().equals("unknown"))throw new AssertionError(mode+" retained later/unproved object: "+saved);
   if(mode.equals("plain")&&!saved.equals(old))throw new AssertionError("Unmodified direct field capture lost precision");
   if(saved.kind().equals("unknown")&&host.gaps.stream().noneMatch(g->g.contains("constructor_capture")))throw new AssertionError("Unknown ordering has no diagnostic");
  }finally{Files.deleteIfExists(dex);}
 }
 static void run()throws Exception{for(String mode:List.of("plain","body","super","indirect","inherited","override"))check(mode);}
 public static void main(String[]args)throws Exception{if(args.length==0)run();else check(args[0]);}
}
