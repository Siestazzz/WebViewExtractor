package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.ServiceConstructorArrayProbe.*;
import static org.example.LifecycleContainerFactoryProbe.*;
/** Layer isolation; does not mutate the fourteen-case frozen factory matrix. */
public final class LifecycleLayerBoundaryProbe {
 static ImmutableDexFile layer(String variant){
  var base=LifecycleContainerFactoryProbe.buildContainer(variant.equals("direct-navigation")?"direct-factory-new":"installed");List<ClassDef> defs=new ArrayList<>();
  for(var c:base.getClasses()){
   if(variant.equals("direct-navigation")&&c.getType().equals(ACT)){
    var m=c.getMethods().iterator().next();List<Instruction> code=new ArrayList<>();for(var ins:m.getImplementation().getInstructions()){
     if(ins instanceof ReferenceInstruction ri&&ri.getReference() instanceof MethodReference mr&&mr.getDefiningClass().equals(DISPATCH)&&mr.getName().equals("onActivityCreated"))code.add(call(Opcode.INVOKE_VIRTUAL,NAV,"launch",List.of(CTX,GROUP,BUNDLE),"V",1,9,2,5));else code.add(ins);
    }defs.add(clazz(ACT,"Landroid/app/Activity;",List.of(),List.of(field(ACT,"root",CONTENT)),method(ACT,"onCreate",List.of(BUNDLE),"V",1,11,code,false)));
   }else if(!variant.equals("direct-navigation")&&c.getType().equals(LIFE)){
    List<Method> ms=new ArrayList<>();for(var m:c.getMethods())if(!m.getName().equals("onActivityCreated"))ms.add(m);
    List<Instruction> code=List.of(make(0,NODE),call(Opcode.INVOKE_DIRECT,NODE,"<init>",List.of(),"V",0),call(Opcode.INVOKE_VIRTUAL,FRAG,"getActivity",List.of(),"Landroid/app/Activity;",4),result(1),new ImmutableInstruction11n(Opcode.CONST_4,2,1),call(Opcode.INVOKE_VIRTUAL,"Landroid/app/Activity;","findViewById",List.of("I"),VIEW,1,2),result(2),new ImmutableInstruction21c(Opcode.CHECK_CAST,2,new ImmutableTypeReference(GROUP)),call(Opcode.INVOKE_VIRTUAL,NODE,"onCreateView",List.of(CTX,GROUP),VIEW,0,1,2),result(3),call(Opcode.INVOKE_VIRTUAL,GROUP,"addView",List.of(VIEW),"V",2,3),call(Opcode.INVOKE_VIRTUAL,NODE,"onActivityCreated",List.of(BUNDLE),"V",0,5),end());
    ms.add(method(LIFE,variant.equals("fragment-create")?"onCreate":"onActivityCreated",List.of(BUNDLE),"V",1,6,code,false));
    // Uninstalled negative uses same callback body, but removes actual transaction install from Activity below.
    defs.add(new ImmutableClassDef(LIFE,1,FRAG,List.of(),null,Set.of(),List.of(field(LIFE,"delegate",CALLBACK)),ms));
   }else if(variant.equals("fragment-uninstalled")&&c.getType().equals(ACT)){
    var noinstall=LifecycleContainerFactoryProbe.buildContainer("uninstalled").getClasses().stream().filter(x->x.getType().equals(ACT)).findFirst().orElseThrow();defs.add(noinstall);
   }else defs.add(c);
  }
  return new ImmutableDexFile(Opcodes.getDefault(),defs);
 }
 public static void main(String[]args)throws Exception{for(String variant:List.of("direct-navigation","fragment-create","fragment-activity-created","fragment-uninstalled")){Path p=Files.createTempFile("layer-probe-",".dex");try{DexFileFactory.writeDexFile(p.toString(),layer(variant));long start=System.nanoTime(),deadline=start+30_000_000_000L;var idx=new CapabilityIndex();idx.read(p,deadline);var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("container.Host");var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("container.Host");System.out.println(variant+"\t"+engine.activities);System.out.println(variant+" diagnostics\t"+engine.diagnostics);System.out.println(variant+" seconds\t"+((System.nanoTime()-start)/1_000_000_000.0));}finally{Files.deleteIfExists(p);}}}
}
