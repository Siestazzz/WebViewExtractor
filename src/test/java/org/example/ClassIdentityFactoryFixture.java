package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.DexFlow.*;
import static org.example.ServiceConstructorArrayProbe.*;
/** Known Class identities select reachable factory returns without merging other products. */
final class ClassIdentityFactoryFixture {
 static final String F="Lidentity/Factory;",KEY=F+"->create(Ljava/lang/Class;)Ljava/lang/Object;";
 static ImmutableDexFile build(int size){
  List<Instruction> code=new ArrayList<>();List<ImmutableClassDef> classes=new ArrayList<>();
  for(int n=0;n<size;n++){String type="Lidentity/Product"+n+";";code.add(new ImmutableInstruction21c(Opcode.CONST_CLASS,0,new ImmutableTypeReference(type)));code.add(new ImmutableInstruction22t(Opcode.IF_NE,0,1,8));code.add(make(0,type));code.add(call(Opcode.INVOKE_DIRECT,type,"<init>",List.of(),"V",0));code.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0));classes.add(clazz(type,OBJ,List.of(),List.of(),method(type,"<init>",List.of(),"V",0x10001,1,List.of(end()),false)));}
  code.add(zero(0));code.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0));classes.add(clazz(F,OBJ,List.of(),List.of(),method(F,"create",List.of("Ljava/lang/Class;"),OBJ,9,2,code,false)));
  return new ImmutableDexFile(Opcodes.getDefault(),classes);
 }
 static V actual(int n){return V.of("class","identity.Product"+n,"Lidentity/Product"+n+";");}
 static Summary select(DexFlow flow,org.jf.dexlib2.iface.Method method,V key){return flow.summary(method,v->v.kind().equals("param")?key:v);}
 static void require(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 static void run()throws Exception{
  for(int size:List.of(2,80,398,850)){
   Path path=Files.createTempFile("class-identity-",".dex");try{
    DexFileFactory.writeDexFile(path.toString(),build(size));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var flow=new DexFlow(idx,deadline);var method=idx.resolve(KEY);
    for(int product:List.of(0,size-1)){
     Summary selected=select(flow,method,actual(product));
     if(size<=398)require(selected.returns().size()==1&&selected.returns().get(0).kind().equals("new")&&selected.returns().get(0).type().equals("identity.Product"+product),"Class factory selected wrong product size="+size+" actual="+product+" "+selected);
     else require(idx.diagnostics.contains("summary_class_factory_budget:"+KEY)&&flow.refined==0,"Oversized factory silently expanded contextual budget");
    }
    int refined=flow.refined;select(flow,method,UNKNOWN);select(flow,method,union(actual(0),actual(1)));require(flow.refined==refined,"Unknown/union Class created a misleading specialization");
    if(size==80){
     for(int n=2;n<16;n++)select(flow,method,actual(n));
     require(flow.refinements.get(KEY).size()==16,"Factory variant cap changed");
     Summary overflow=select(flow,method,actual(16));require(overflow.equals(flow.summary(method))&&idx.diagnostics.contains("summary_refinement_budget:"+KEY),"Variant exhaustion pruned a product");
     require(select(flow,method,actual(0)).returns().get(0).type().equals("identity.Product0"),"Existing exact variant unavailable after exhaustion");
    }
   }finally{Files.deleteIfExists(path);}
  }
  require(DexFlow.condition("if-eq",actual(0),actual(1)).equals(false)&&DexFlow.condition("if-ne",actual(0),actual(1)).equals(true),"Class equality inverted");
  require(DexFlow.condition("if-eq",actual(0),union(actual(0),actual(1)))==null,"Class union was unsafely pruned");
  System.out.println("ClassIdentityFactoryFixture PASS: two/80/398 branch factories, distinct actual Class, unknown/union,16 variant fallback and >4096 instruction diagnostic.");
 }
}
