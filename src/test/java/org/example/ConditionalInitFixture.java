package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.CapabilitySelfTest.*;

/** A false XML-constructor flag must not inherit another instance's true branch. */
final class ConditionalInitFixture {
 static final String VIEW="Lconditional/Browser;",CTX="Landroid/content/Context;",ATTR="Landroid/util/AttributeSet;";
 public static void main(String[] args)throws Exception {run();}
 static void run()throws Exception {
  int layout=0x7f010001,id=0x7f020002;
  var configure=method(VIEW,"configure",List.of("Z"),2,4,List.of(
    new ImmutableInstruction21t(Opcode.IF_EQZ,3,9),make(0,B),str(1,"conditional"),
    invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),end()),false);
  var init=method(VIEW,"init",List.of("Z"),2,2,List.of(invoke(Opcode.INVOKE_DIRECT,VIEW,"configure",List.of("Z"),"V",0,1),end()),false);
  var xml=method(VIEW,"<init>",List.of(CTX,ATTR),0x10001,4,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),invoke(Opcode.INVOKE_DIRECT,VIEW,"init",List.of("Z"),"V",1,0),str(0,"xml-confirmed"),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",1,0),end()),false);
  var direct=method(VIEW,"<init>",List.of(CTX,"Z"),0x10001,3,List.of(invoke(Opcode.INVOKE_DIRECT,VIEW,"init",List.of("Z"),"V",0,2),end()),false);
  var create=method(A,"onCreate",List.of(),1,4,List.of(new ImmutableInstruction31i(Opcode.CONST,0,layout),invoke(Opcode.INVOKE_VIRTUAL,A,"setContentView",List.of("I"),"V",3,0),make(0,VIEW),new ImmutableInstruction11n(Opcode.CONST_4,1,1),invoke(Opcode.INVOKE_DIRECT,VIEW,"<init>",List.of(CTX,"Z"),"V",0,3,1),make(0,VIEW),new ImmutableInstruction11n(Opcode.CONST_4,1,0),invoke(Opcode.INVOKE_DIRECT,VIEW,"<init>",List.of(CTX,"Z"),"V",0,3,1),new ImmutableInstruction31i(Opcode.CONST,0,id),invoke(Opcode.INVOKE_VIRTUAL,A,"findViewById",List.of("I"),"Landroid/view/View;",3,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),str(1,"entry"),invoke(Opcode.INVOKE_VIRTUAL,VIEW,"loadUrl",List.of("Ljava/lang/String;"),"V",0,1),end()),false);
  Path dex=Files.createTempFile("conditional-init-",".dex");
  try {
   DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",create),clazz(VIEW,W,xml,direct,init,configure),clazz(B,"Ljava/lang/Object;",method(B,"call",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);
   var apk=new ApkInventory();var node=new ApkInventory.LayoutNode("conditional.Browser");node.id=id;apk.layoutRoots.put("res/layout/page.xml",List.of(node));apk.layoutResources.put(layout,Set.of("res/layout/page.xml"));
   var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"webview_operation".equals(f.get("kind"))&&f.get("webview").toString().contains("view:"+id)),"XML constructor was not actually analyzed");
   // An unknown condition must keep the conservative branch, and must not consume
   // a new specialization variant merely because a numeric IF opcode exists.
   var flow=new DexFlow(index,deadline);var base=flow.summary(configure);int before=flow.refined;
   check(flow.summary(configure,v->DexFlow.UNKNOWN).calls().size()==base.calls().size()&&flow.refined==before,"Unknown guard dropped a branch or needlessly specialized");
   var bridges=facts.stream().filter(f->"bridge".equals(f.get("kind"))).toList();
   check(bridges.size()==1,"False initializer produced extra bridge bindings: "+bridges);
   @SuppressWarnings("unchecked") var view=(Map<String,Object>)bridges.get(0).get("webview");
   check(view.get("id").toString().contains("onCreate()V@6"),"Bridge does not belong to actual true allocation: "+view);
  } finally {Files.deleteIfExists(dex);}
  System.out.println("ConditionalInitFixture PASS: XML false and direct false remain isolated from actual true constructor.");
 }
}
