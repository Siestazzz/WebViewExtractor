package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** A registered JS route can configure its captured view; an unused sibling cannot. */
final class RegisteredMessageFixture {
 static void run()throws Exception{
  String mw="Ltest/ProtocolWebView;",contract="Ltest/Route;",handler="Ltest/MediaRoute;",map="Ljava/util/Map;";
  var registryField=new ImmutableField(mw,"routes",map,1,null,Set.of(),Set.of());
  var register=method(mw,"register",List.of("Ljava/lang/String;",contract),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(mw,"routes",map)),invoke(Opcode.INVOKE_INTERFACE,map,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,2,3),end()),false);
  var dispatch=method(mw,"dispatch",List.of("Ljava/lang/String;"),1,3,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(mw,"routes",map)),invoke(Opcode.INVOKE_INTERFACE,map,"get",List.of("Ljava/lang/Object;"),"Ljava/lang/Object;",0,2),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_INTERFACE,contract,"handle",List.of("Ljava/lang/String;"),"V",0,2),end()),true);
  var handlerField=new ImmutableField(handler,"view",W,1,null,Set.of(),Set.of());
  var ctor=method(handler,"<init>",List.of(W),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(handler,"view",W)),end()),false);
  var handle=method(handler,"handle",List.of("Ljava/lang/String;"),1,5,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,3,new ImmutableFieldReference(handler,"view",W)),make(1,B),str(2,"conditional-media"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var declaration=new ImmutableMethod(contract,"handle",List.of(new ImmutableMethodParameter("Ljava/lang/String;",Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
  var entry=method(A,"onCreate",List.of(),1,6,List.of(make(0,mw),str(3,"native"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,0,3),make(1,handler),invoke(Opcode.INVOKE_DIRECT,handler,"<init>",List.of(W),"V",1,0),str(3,"showMedia"),invoke(Opcode.INVOKE_VIRTUAL,mw,"register",List.of("Ljava/lang/String;",contract),"V",0,3,1),make(2,W),make(4,handler),invoke(Opcode.INVOKE_DIRECT,handler,"<init>",List.of(W),"V",4,2),end()),false);
  Path path=Files.createTempFile("registered-message-",".dex");
  try{
   DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(mw,1,W,List.of(),null,Set.of(),List.of(registryField),List.of(register,dispatch)),new ImmutableClassDef(contract,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(declaration)),new ImmutableClassDef(handler,1,"Ljava/lang/Object;",List.of(contract),null,Set.of(),List.of(handlerField),List.of(ctor,handle)),clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(path,deadline);var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   var conditional=facts.stream().filter(f->"conditional-media".equals(f.get("registration_name"))).toList();
   check(conditional.size()==1,"Registered handler missing or unregistered sibling executed: "+conditional);
   check(((Map<?,?>)conditional.get(0).get("webview")).get("id").toString().startsWith(A+"->onCreate()V@0|"),"Registered handler bound to wrong WebView");
   check("candidate".equals(conditional.get(0).get("binding_status")),"Message-triggered initialization must remain conditional");
  }finally{Files.deleteIfExists(path);}
 }
}
