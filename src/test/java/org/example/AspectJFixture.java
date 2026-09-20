package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import static org.example.CapabilitySelfTest.*;

/** Actual linked closures, indexed state, and non-executed closures must stay separate. */
final class AspectJFixture {
 static ImmutableMethod returning(ImmutableMethod m,String type){return new ImmutableMethod(m.getDefiningClass(),m.getName(),m.getParameters(),type,m.getAccessFlags(),m.getAnnotations(),Set.of(),m.getImplementation());}
 static void run()throws Exception {
  String concretePoint="Ltest/JoinPoint;";
  String base="Lorg/aspectj/runtime/internal/a;",closure="Ltest/Closure;",jp="Lorg/aspectj/lang/d;",array="[Ljava/lang/Object;",advice="Ltest/Advice;";
  var state=new ImmutableField(base,"state",array,4,null,Set.of(),Set.of());
  var ctor=method(base,"<init>",List.of(array),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(base,"state",array)),end()),false);
  var link=returning(method(base,"linkClosureAndJoinPoint",List.of("I"),1,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),jp);
  var abstractRun=new ImmutableMethod(base,"run",List.of(new ImmutableMethodParameter(array,Set.of(),null)),"Ljava/lang/Object;",1025,Set.of(),Set.of(),null);
  var childCtor=method(closure,"<init>",List.of(array),1,2,List.of(invoke(Opcode.INVOKE_DIRECT,base,"<init>",List.of(array),"V",0,1),end()),false);
  var run=returning(method(closure,"run",List.of(array),1,7,List.of(
   new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,5,new ImmutableFieldReference(base,"state",array)),
   new ImmutableInstruction11n(Opcode.CONST_4,1,0),new ImmutableInstruction23x(Opcode.AGET_OBJECT,2,0,1),
   new ImmutableInstruction11n(Opcode.CONST_4,1,1),new ImmutableInstruction23x(Opcode.AGET_OBJECT,3,0,1),
   new ImmutableInstruction11n(Opcode.CONST_4,1,2),new ImmutableInstruction23x(Opcode.AGET_OBJECT,4,0,1),
   invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,3,4),
   new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),"Ljava/lang/Object;");
  var proceedArgs=new ImmutableMethod(jp,"proceed",List.of(new ImmutableMethodParameter(array,Set.of(),null)),"Ljava/lang/Object;",1025,Set.of(),Set.of(),null);
  var query=new ImmutableMethod(jp,"query",List.of(new ImmutableMethodParameter(array,Set.of(),null)),"Ljava/lang/Object;",1025,Set.of(),Set.of(),null);
  var proceed=new ImmutableMethod(jp,"proceed",List.of(),"Ljava/lang/Object;",1025,Set.of(),Set.of(),null);
  var around=method(advice,"around",List.of(jp),9,1,List.of(invoke(Opcode.INVOKE_INTERFACE,jp,"proceed",List.of(),"Ljava/lang/Object;",0),end()),false);
  List<Instruction> body=new ArrayList<>();
  for(int i=0;i<4;i++){
   body.add(make(0,W));body.add(make(1,B));body.add(str(2,"bridge"+i));body.add(make(6,concretePoint));
   body.add(new ImmutableInstruction35c(Opcode.FILLED_NEW_ARRAY,4,0,1,2,6,0,new ImmutableTypeReference(array)));
   body.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,3));body.add(make(4,closure));
   body.add(invoke(Opcode.INVOKE_DIRECT,closure,"<init>",List.of(array),"V",4,3));
   if(i<3){body.add(new ImmutableInstruction11n(Opcode.CONST_4,5,0));body.add(invoke(Opcode.INVOKE_VIRTUAL,base,"linkClosureAndJoinPoint",List.of("I"),jp,4,5));body.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,5));}
   if(i<2)body.add(invoke(Opcode.INVOKE_STATIC,advice,"around",List.of(jp),"V",i==1?6:5));
  }
  body.add(end());
  var entry=method(A,"onCreate",List.of(),1,8,body,false);
  Path file=Files.createTempFile("wv-aspectj-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(
    clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(base,1025,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(state),List.of(ctor,link,abstractRun)),
    clazz(closure,base,childCtor,run),new ImmutableClassDef(jp,1537,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(proceed,proceedArgs,query)),
    new ImmutableClassDef(concretePoint,1,"Ljava/lang/Object;",List.of(jp),null,Set.of(),List.of(),List.of()),clazz(advice,"Ljava/lang/Object;",around),clazz(B,"Ljava/lang/Object;",method(B,"exposed",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   check(idx.closureProceedArguments(CapabilityIndex.key(proceedArgs)),"Unsupported proceed(args) was not recognized for diagnostics");
   check(!idx.closureProceedArguments(CapabilityIndex.key(query)),"Unrelated join-point array method was mistaken for proceed(args)");
   check(idx.relevant.contains(CapabilityIndex.key(entry)),"Closure link failed reverse relevance");
   check(!idx.callbackEntries.containsKey("test.Closure"),"Closure execution inferred from construction");
   var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Linked closure capabilities missing");
   @SuppressWarnings("unchecked")var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   var bridges=facts.stream().filter(f->"bridge".equals(f.get("kind"))).toList();
   check(bridges.size()==2,"Unlinked/non-proceeded closure executed or actual closure lost: "+bridges);
   check(bridges.stream().map(f->f.get("registration_name")).collect(java.util.stream.Collectors.toSet()).equals(Set.of("bridge0","bridge1")),"Closure state indices mixed");
   check(bridges.stream().map(f->f.get("webview")).distinct().count()==2,"Two closure receivers merged");
   var factoryState=engine.new Host("test.AppActivity");
   var staticPart=DexFlow.V.of("object","test.StaticPart","shared-static-part");
   var newPoint=DexFlow.V.of("new","test.JoinPoint","factory@0");
   var firstJob=new CapabilityEngine.Job(around,List.of(staticPart,DexFlow.V.of("object","test.Owner","owner-one")),List.of(),false);
   var secondJob=new CapabilityEngine.Job(around,List.of(staticPart,DexFlow.V.of("object","test.Owner","owner-two")),List.of(),false);
   var pointOne=engine.eval(newPoint,firstJob,factoryState,0,new HashSet<>());
   var pointTwo=engine.eval(newPoint,secondJob,factoryState,0,new HashSet<>());
   check(!pointOne.id().equals(pointTwo.id()),"Join-point factory conflated distinct targets sharing the same static part");
   check(pointOne.equals(engine.eval(newPoint,firstJob,factoryState,0,new HashSet<>())),"Join-point factory identity was unstable");
   var h=engine.new Host("test.AppActivity");var recv=DexFlow.V.of("object",array,"array");
   engine.applyWrite(h,"$element:0",recv,DexFlow.V.literal("number","7"));engine.applyWrite(h,"$element:1",recv,DexFlow.V.literal("number","9"));
   check(engine.arrayElement(h,recv,DexFlow.V.literal("number","0"),null).literal().equals("7"),"Indexed array read lost precision");
   check(DexFlow.alternatives(engine.arrayElement(h,recv,DexFlow.UNKNOWN,null)).size()==2,"Unknown index silently dropped possible values");
  }finally{Files.deleteIfExists(file);}
 }
}
