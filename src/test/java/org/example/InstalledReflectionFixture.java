package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Installed prompt -> actual same-map handler -> exact annotated reflective invocation. */
final class InstalledReflectionFixture {
 static final String R="Lexact/Router;",C="Lexact/Chrome;",CONSOLE="Lexact/ConsoleChrome;",CTX="Lexact/Context;",ANN="Lexact/Callable;",GOOD="Lexact/Handler;",BAD="Lexact/Bad;",MAP="Ljava/util/HashMap;",METHOD="Ljava/lang/reflect/Method;";
 static Instruction klass(int register,String type){return new ImmutableInstruction21c(Opcode.CONST_CLASS,register,new ImmutableTypeReference(type));}
 static Instruction filled(String type,int... registers){int[] r=Arrays.copyOf(registers,5);return new ImmutableInstruction35c(Opcode.FILLED_NEW_ARRAY,registers.length,r[0],r[1],r[2],r[3],r[4],new ImmutableTypeReference(type));}
 static Instruction result(int register){return new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,register);}
 static ImmutableMethod endpoint(String owner,String name,List<String> params,int flags,boolean annotated){
  var original=method(owner,name,params,flags,params.size()+1,List.of(end()),false);
  return new ImmutableMethod(owner,name,original.getParameters(),"V",flags,annotated?Set.of(new ImmutableAnnotation(AnnotationVisibility.RUNTIME,ANN,Set.of(new ImmutableAnnotationElement("ignoredAlias",new org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue("unused-alias"))))):Set.of(),Set.of(),original.getImplementation());
 }
 static ImmutableMethod dispatch(boolean guarded){return dispatch(guarded,false,false);}
 static ImmutableMethod dispatch(boolean guarded,boolean declared,boolean accessible){
  var map=new ImmutableFieldReference(R,"objects",MAP);List<Instruction> code=new ArrayList<>(List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,7,map),str(1,"same"),invoke(Opcode.INVOKE_VIRTUAL,MAP,"get",List.of("Ljava/lang/Object;"),"Ljava/lang/Object;",0,1),result(0),invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/Object;","getClass",List.of(),"Ljava/lang/Class;",0),result(1),klass(2,CTX),klass(3,"Ljava/lang/String;"),filled("[Ljava/lang/Class;",2,3),result(2),invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/Class;",declared?"getDeclaredMethod":"getMethod",List.of("Ljava/lang/String;","[Ljava/lang/Class;"),METHOD,1,9,2),result(3),klass(4,ANN),invoke(Opcode.INVOKE_VIRTUAL,METHOD,"isAnnotationPresent",List.of("Ljava/lang/Class;"),"Z",3,4),new ImmutableInstruction11x(Opcode.MOVE_RESULT,4)));
  if(accessible){code.add(12,new ImmutableInstruction11n(Opcode.CONST_4,5,1));code.add(13,invoke(Opcode.INVOKE_VIRTUAL,METHOD,"setAccessible",List.of("Z"),"V",3,5));}
  int branch=code.size();if(guarded)code.add(new ImmutableInstruction21t(Opcode.IF_EQZ,4,1));
  code.add(make(4,CTX));code.add(invoke(Opcode.INVOKE_DIRECT,CTX,"<init>",List.of(W),"V",4,8));code.add(filled("[Ljava/lang/Object;",4,9));code.add(result(5));code.add(invoke(Opcode.INVOKE_VIRTUAL,METHOD,"invoke",List.of("Ljava/lang/Object;","[Ljava/lang/Object;"),"Ljava/lang/Object;",3,0,5));code.add(end());
  if(guarded){int offset=0,end=0;for(int i=0;i<code.size();i++){if(i==branch)offset=end;end+=code.get(i).getCodeUnits();}code.set(branch,new ImmutableInstruction21t(Opcode.IF_EQZ,4,end-code.get(code.size()-1).getCodeUnits()-offset));}
  return method(R,declared?(accessible?"accessibleDispatch":"declaredDispatch"):guarded?"dispatch":"unprotected",List.of(W,"Ljava/lang/String;"),1,10,code,false);
 }
 public static void main(String[] args)throws Exception{run();}
 static void run()throws Exception{
  var mapField=new ImmutableField(R,"objects",MAP,1,null,Set.of(),Set.of());var mapRef=new ImmutableFieldReference(R,"objects",MAP);
  var routerCtor=method(R,"<init>",List.of(),0x10001,2,List.of(make(0,MAP),invoke(Opcode.INVOKE_DIRECT,MAP,"<init>",List.of(),"V",0),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,mapRef),end()),false);
  var register=method(R,"register",List.of("Ljava/lang/Object;","Ljava/lang/String;"),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,mapRef),invoke(Opcode.INVOKE_VIRTUAL,MAP,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,3,2),end()),false);
  var capture=new ImmutableField(C,"router",R,0x11,null,Set.of(),Set.of());var captureRef=new ImmutableFieldReference(C,"router",R);
  var ctor=method(C,"<init>",List.of(R),0x10001,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,captureRef),end()),false);
  var callback=AspectJFixture.returning(method(C,"onJsPrompt",List.of(W,"Ljava/lang/String;","Ljava/lang/String;","Ljava/lang/String;","Landroid/webkit/JsPromptResult;"),1,12,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,6,captureRef),invoke(Opcode.INVOKE_VIRTUAL,R,"dispatch",List.of(W,"Ljava/lang/String;"),"V",0,7,9),new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction11x(Opcode.RETURN,0)),false),"Z");
  var consoleView=new ImmutableField(CONSOLE,"view",W,0x11,null,Set.of(),Set.of());var consoleViewRef=new ImmutableFieldReference(CONSOLE,"view",W);
  var consoleCtor=method(CONSOLE,"<init>",List.of(R,W),0x10001,3,List.of(invoke(Opcode.INVOKE_DIRECT,C,"<init>",List.of(R),"V",0,1),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,2,0,consoleViewRef),end()),false);
  var consoleCallback=AspectJFixture.returning(method(CONSOLE,"onConsoleMessage",List.of("Landroid/webkit/ConsoleMessage;"),1,5,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,3,captureRef),new ImmutableInstruction22c(Opcode.IGET_OBJECT,1,3,consoleViewRef),str(2,"allowed"),invoke(Opcode.INVOKE_VIRTUAL,R,"dispatch",List.of(W,"Ljava/lang/String;"),"V",0,1,2),new ImmutableInstruction11n(Opcode.CONST_4,0,1),new ImmutableInstruction11x(Opcode.RETURN,0)),false),"Z");
  List<Instruction> code=new ArrayList<>();for(int i=0;i<5;i++){
   code.add(make(0,W));code.add(make(1,R));code.add(invoke(Opcode.INVOKE_DIRECT,R,"<init>",List.of(),"V",1));code.add(make(2,i==4?CONSOLE:C));if(i==4)code.add(invoke(Opcode.INVOKE_DIRECT,CONSOLE,"<init>",List.of(R,W),"V",2,1,0));else code.add(invoke(Opcode.INVOKE_DIRECT,C,"<init>",List.of(R),"V",2,1));code.add(make(3,i==3?BAD:GOOD));code.add(str(4,"same"));code.add(invoke(Opcode.INVOKE_VIRTUAL,R,"register",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",1,3,4));if(i!=2)code.add(invoke(Opcode.INVOKE_VIRTUAL,W,"setWebChromeClient",List.of("Landroid/webkit/WebChromeClient;"),"V",0,2));
  }code.add(end());var entry=method(A,"onCreate",List.of(),1,6,code,false);
  Path file=Files.createTempFile("installed-reflection-",".dex");try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(R,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(mapField),List.of(routerCtor,register,dispatch(true),dispatch(false),dispatch(true,true,false),dispatch(true,true,true))),new ImmutableClassDef(C,1,"Landroid/webkit/WebChromeClient;",List.of(),null,Set.of(),List.of(capture),List.of(ctor,callback)),new ImmutableClassDef(CONSOLE,1,C,List.of(),null,Set.of(),List.of(consoleView),List.of(consoleCtor,consoleCallback)),clazz(CTX,"Ljava/lang/Object;",method(CTX,"<init>",List.of(W),0x10001,2,List.of(end()),false)),clazz(GOOD,"Ljava/lang/Object;",endpoint(GOOD,"allowed",List.of(CTX,"Ljava/lang/String;"),1,true),endpoint(GOOD,"notAnnotated",List.of(CTX,"Ljava/lang/String;"),1,false),endpoint(GOOD,"privateEndpoint",List.of(CTX,"Ljava/lang/String;"),2,true),endpoint(GOOD,"wrongTypes",List.of("Ljava/lang/String;",CTX),1,true)),clazz(BAD,"Ljava/lang/Object;",endpoint(BAD,"wrongTypes",List.of("Ljava/lang/String;",CTX),1,true)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");var transports=facts.stream().filter(f->"installed_client_same_map_exact_reflection".equals(f.get("resolution"))).toList();
   check(transports.size()==3,"Installed reflection lost/overexposed endpoints: "+transports+" report="+engine.activities);
   check(engine.reflection.plans(idx.resolve(R+"->unprotected("+W+"Ljava/lang/String;)V")).isEmpty(),"Annotation API without controlling invoke became a certificate");
   var declaredPlans=engine.reflection.plans(idx.resolve(R+"->declaredDispatch("+W+"Ljava/lang/String;)V"));
   var accessiblePlans=engine.reflection.plans(idx.resolve(R+"->accessibleDispatch("+W+"Ljava/lang/String;)V"));
   check(declaredPlans.size()==1&&declaredPlans.get(0).declared()&&declaredPlans.get(0).accessibleFlags().isEmpty(),"Declared lookup became public/inherited or fabricated accessibility");
   check(accessiblePlans.size()==1&&accessiblePlans.get(0).accessibleFlags().size()==1,"Same selected Method dominating accessibility flag was lost: "+accessiblePlans);
   long discoveryVisits=engine.reflection.discoveryMethodVisits;for(int i=0;i<100;i++)engine.reflection.writer(idx.resolve(R+"->register(Ljava/lang/Object;Ljava/lang/String;)V"));
   check(engine.reflection.discoveryMethodVisits==discoveryVisits&&discoveryVisits<=2L*idx.methods.size(),"Protocol metadata repeated global discovery scans");
   var expiredIndex=new ReflectionProtocols(idx,new DexFlow(idx,System.nanoTime()-1));
   check(expiredIndex.plans(idx.resolve(R+"->dispatch("+W+"Ljava/lang/String;)V")).isEmpty()&&idx.diagnostics.contains("transport_protocol_deadline"),"Expired protocol work lost deadline diagnostic");

   check(transports.stream().map(f->f.get("webview")).distinct().count()==3&&transports.stream().map(f->f.get("registry_object_id")).distinct().count()==3&&transports.stream().map(f->f.get("reflective_target_object_id")).distinct().count()==3,"Same-name maps or same-class handlers merged across WebViews");
   for(var fact:transports)check(fact.get("registration_name").equals("same")&&fact.get("name").equals("allowed")&&!fact.toString().contains("unused-alias"),"Unused annotation value renamed endpoint or wrong parameters exposed");
   check(engine.activities.toString().contains("transport_registered_no_compatible_endpoint"),"Incompatible registered target lost explicit diagnostic");
  }finally{Files.deleteIfExists(file);}
  System.out.println("InstalledReflectionFixture PASS: actual installed prompt/console, same-map target, exact parameters, controlling annotation gate, two-WebView isolation, uninstalled/private/wrong-type negatives and unused annotation value.");
 }
}
