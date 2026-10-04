package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.ServiceConstructorArrayProbe.*;
/** Exact modeled SDK entries are terminal; actual application override bodies still execute. */
final class FragmentSdkBoundaryFixture {
 static final String NOISE="Lboundary/StateMachine;";
 static void run()throws Exception{
  for(String mode:List.of("packaged-sdk-show","application-override","uncommitted","allocated-only")){
   var input=PackagedFragmentProtocolProbe.build(mode);List<ImmutableClassDef> defs=new ArrayList<>();
   for(var cls:input.getClasses()){
    List<Method> methods=new ArrayList<>();
    for(var method:cls.getMethods()){
     String key=CapabilityIndex.key(method);List<Instruction> body=new ArrayList<>();
     if(FragmentTransactions.manager(key)||FragmentTransactions.begin(key)||FragmentTransactions.fluent(key)){
      if(cls.getType().equals(PackagedFragmentProtocolProbe.ACT)){
       // A real override side effect must be visited, even when it returns null.
       body.add(make(0,W));body.add(make(1,B));body.add(str(2,"override"));
       body.add(call(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(OBJ,"Ljava/lang/String;"),"V",0,1,2));
      }else body.add(call(Opcode.INVOKE_STATIC,NOISE,"tick",List.of(),"V"));
     }
     if(body.isEmpty()){methods.add(method);continue;}
     method.getImplementation().getInstructions().forEach(body::add);
     methods.add(method(method.getDefiningClass(),method.getName(),method.getParameterTypes().stream().map(Object::toString).toList(),method.getReturnType(),method.getAccessFlags(),Math.max(4,method.getImplementation().getRegisterCount()),body,false));
    }
    defs.add(new ImmutableClassDef(cls.getType(),cls.getAccessFlags(),cls.getSuperclass(),cls.getInterfaces(),null,cls.getAnnotations(),cls.getFields(),methods));
   }
   defs.add(clazz(NOISE,OBJ,List.of(),List.of(),method(NOISE,"tick",List.of(),"V",9,1,List.of(end()),false)));
   // Add the packaged fluent SDK body: a modeled show must preserve the pending transaction.
   if(mode.equals("packaged-sdk-show")){
    for(int n=0;n<defs.size();n++)if(defs.get(n).getType().equals(PackagedFragmentProtocolProbe.TX)){
     var old=defs.get(n);List<Method> methods=new ArrayList<>();old.getMethods().forEach(methods::add);
     methods.add(method(old.getType(),"show",List.of(PackagedFragmentProtocolProbe.FRAG),old.getType(),1,2,List.of(call(Opcode.INVOKE_STATIC,NOISE,"tick",List.of(),"V"),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false));
     defs.set(n,new ImmutableClassDef(old.getType(),1,OBJ,List.of(),null,Set.of(),List.of(),methods));
    }
   }
   Path path=Files.createTempFile("fragment-sdk-boundary-",".dex");try{
    DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),defs));long deadline=System.nanoTime()+30_000_000_000L;
    var index=new CapabilityIndex();index.read(path,deadline);
    // Force ordinary relevance, so a skipped body is proven by dispatch rather than a cold index.
    for(var cls:defs)for(var m:cls.getMethods())if(cls.getType().startsWith("Landroidx/fragment/app/")||cls.getType().equals(NOISE))index.relevant.add(CapabilityIndex.key(m));
    var engine=new CapabilityEngine(index,new ApkInventory(),deadline);var state=engine.beginActivity("packaged.Host");
    while(!state.host.queue.isEmpty()&&System.nanoTime()<deadline)engine.processJob(state.host);
    if(state.host.visited.stream().anyMatch(s->s.startsWith(NOISE+"->tick")))throw new AssertionError("Modeled SDK body still traversed: "+mode);
    Set<String> kinds=new HashSet<>();for(var fact:state.host.facts.values())kinds.add(String.valueOf(fact.get("kind")));
    if(mode.equals("packaged-sdk-show")&&!kinds.containsAll(Set.of("bridge","setting","callback")))throw new AssertionError("Installation lost: "+kinds);
    if(mode.equals("application-override")&&!kinds.contains("bridge"))throw new AssertionError("Actual override body skipped");
    if((mode.equals("uncommitted")||mode.equals("allocated-only"))&&!kinds.isEmpty())throw new AssertionError("Uninstalled Fragment activated: "+mode);
   }finally{Files.deleteIfExists(path);}
  }
  System.out.println("FragmentSdkBoundaryFixture PASS");
 }
 public static void main(String[] args)throws Exception{run();}
}
