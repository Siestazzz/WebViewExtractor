package org.example;
import java.nio.file.*;import java.util.*;import org.jf.dexlib2.*;import org.jf.dexlib2.iface.Method;import org.jf.dexlib2.iface.instruction.*;import org.jf.dexlib2.iface.reference.*;import org.jf.dexlib2.immutable.*;import org.jf.dexlib2.immutable.instruction.*;import org.jf.dexlib2.immutable.reference.*;import static org.example.ServiceConstructorArrayProbe.*;import static org.example.DexFlow.*;
/** Standard interface dispatch, initializer aliases/escapes, and cyclic/nonpacked boundaries. */
final class StaticMapCertificateFixture {
 static ImmutableDexFile build(String mode){List<ImmutableClassDef> defs=new ArrayList<>();for(var cls:IntegerSwitchFactoryFixture.build(200,false,false).getClasses()){
  if(!cls.getType().equals(IntegerSwitchFactoryFixture.ROUTER)){defs.add(ImmutableClassDef.of(cls));continue;}
  List<Method> methods=new ArrayList<>();for(var m:cls.getMethods()){List<Instruction> code=new ArrayList<>();m.getImplementation().getInstructions().forEach(code::add);
   if(m.getName().equals("<clinit>")){
    if(mode.equals("interface"))for(int n=0;n<code.size();n++)if(code.get(n) instanceof ReferenceInstruction reference&&reference.getReference() instanceof MethodReference method&&method.getName().equals("put"))code.set(n,call(Opcode.INVOKE_INTERFACE,"Ljava/util/Map;","put",List.of(OBJ,OBJ),OBJ,0,1,2));
    if(mode.equals("escape"))code.add(code.size()-1,call(Opcode.INVOKE_STATIC,"Lunknown/Consumer;","consume",List.of(MAP),"V",0));
    if(mode.equals("alias"))code.add(code.size()-1,new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(cls.getType(),"alias",MAP)));
    if(mode.equals("overwrite")){code.add(code.size()-1,make(0,MAP));code.add(code.size()-1,call(Opcode.INVOKE_DIRECT,MAP,"<init>",List.of(),"V",0));code.add(code.size()-1,new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(cls.getType(),"ids",MAP)));}
   }
   if(m.getName().equals("select")){
    if(mode.equals("sparse"))for(int n=0;n<code.size();n++){Instruction old=code.get(n);if(old.getOpcode()==Opcode.PACKED_SWITCH)code.set(n,new ImmutableInstruction31t(Opcode.SPARSE_SWITCH,((OneRegisterInstruction)old).getRegisterA(),((OffsetInstruction)old).getCodeOffset()));else if(old instanceof SwitchPayload payload)code.set(n,new ImmutableSparseSwitchPayload(payload.getSwitchElements()));}
    if(mode.equals("backedge"))code.add(new ImmutableInstruction20t(Opcode.GOTO_16,-IntegerSwitchFactoryFixture.units(code)));
   }
   methods.add(method(m.getDefiningClass(),m.getName(),m.getParameterTypes().stream().map(Object::toString).toList(),m.getReturnType(),m.getAccessFlags(),m.getImplementation().getRegisterCount(),code,false));
  }List<org.jf.dexlib2.iface.Field> fields=new ArrayList<>();cls.getFields().forEach(fields::add);fields.add(new ImmutableField(cls.getType(),"alias",MAP,9,null,Set.of(),Set.of()));defs.add(new ImmutableClassDef(cls.getType(),1,OBJ,List.of(),null,Set.of(),fields,methods));
 }return new ImmutableDexFile(Opcodes.getDefault(),defs);}
 static void run()throws Exception{for(String mode:List.of("interface","sparse","escape","alias","overwrite","backedge")){
  Path dex=Files.createTempFile("static-map-certificate-",".dex");try{DexFileFactory.writeDexFile(dex.toString(),build(mode));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);var host=engine.new Host("switchfactory.Host");var method=idx.resolve(IntegerSwitchFactoryFixture.ROUTER+"->select(Ljava/lang/String;)"+PRODUCT);var job=new CapabilityEngine.Job(method,List.of(V.literal("java.lang.String","switchfactory.Slot130")),List.of("certificate"),false);V value=IntegerSwitchFactoryFixture.select(engine,job,host,130);
   boolean positive=mode.equals("interface")||mode.equals("sparse");if(positive?!value.kind().equals("object")||!value.type().equals("switchfactory.Slot130"):!value.kind().equals("unknown")||!host.constantMapSnapshots.isEmpty())throw new AssertionError("Certificate boundary "+mode+" "+value+" "+host.gaps);
   if(positive){
    // An unknown primitive argument cannot alias the certified Map. A primitive
    // descriptor has no reference class name and must never reach Set.of.contains(null).
    for(String primitive:List.of("Z","B","C","S","I","J","F","D")){
     engine.invalidateConstantMapEscape(host,job,new Call("Lunknown/Consumer;->consume("+primitive+")V",0,List.of(UNKNOWN),true,false,false));
     if(host.constantMapSnapshots.isEmpty())throw new AssertionError("Primitive escape invalidated Map certificate: "+primitive);
    }
    engine.invalidateConstantMapEscape(host,job,new Call("Lunknown/Consumer;->consume(Ljava/lang/Object;)V",0,List.of(expr("return","java.lang.Object","Lalias/Helper;->get()Ljava/lang/Object;",List.of())),true,false,false));if(!host.constantMapSnapshots.isEmpty()||!IntegerSwitchFactoryFixture.select(engine,job,host,130).kind().equals("unknown"))throw new AssertionError("Erased returned alias escaped without invalidation");}
  }finally{Files.deleteIfExists(dex);}
 }System.out.println("StaticMapCertificateFixture PASS: standard Map interface, sparse factory, branch/alias/escape/overwrite/backedge and erased alias invalidation.");}
 public static void main(String[]args)throws Exception{run();}
}
