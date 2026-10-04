package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.CapabilitySelfTest.*;

/** A concrete override replaces an API effect; native/super and unresolved paths differ. */
final class CapabilityDispatchFixture {
 public static void main(String[] args)throws Exception{run();}
 static void run()throws Exception {
  String blocked="Ldispatch/Blocked;",forward="Ldispatch/Forward;",client="Ldispatch/Client;";
  var empty=method(blocked,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),0x11,3,List.of(end()),false);
  var superCall=method(forward,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),1,3,List.of(invoke(Opcode.INVOKE_SUPER,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var helper=method(H,"inject",List.of(W,B,"Ljava/lang/String;"),9,3,List.of(invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var entry=method(A,"onCreate",List.of(),1,6,List.of(make(0,W),make(1,blocked),make(2,forward),make(3,B),str(4,"native"),invoke(Opcode.INVOKE_STATIC,H,"inject",List.of(W,B,"Ljava/lang/String;"),"V",0,3,4),str(4,"blocked"),invoke(Opcode.INVOKE_STATIC,H,"inject",List.of(W,B,"Ljava/lang/String;"),"V",1,3,4),str(4,"forward"),invoke(Opcode.INVOKE_STATIC,H,"inject",List.of(W,B,"Ljava/lang/String;"),"V",2,3,4),end()),false);
  var emptyClient=method(blocked,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),0x11,2,List.of(end()),false);
  var emptyRemoval=method(blocked,"removeJavascriptInterface",List.of("Ljava/lang/String;"),0x11,2,List.of(end()),false);
  String settings="Ldispatch/BlockedSettings;";
  var emptySetting=method(settings,"setJavaScriptEnabled",List.of("Z"),0x11,2,List.of(end()),false);
  Path file=Files.createTempFile("capability-dispatch-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(H,"Ljava/lang/Object;",helper),clazz(blocked,W,empty,emptyClient,emptyRemoval),clazz(forward,W,superCall),clazz(B,"Ljava/lang/Object;",method(B,"expose",List.of(),1,1,List.of(end()),true)),clazz(client,"Landroid/webkit/WebViewClient;",method(client,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false)),clazz(settings,S,emptySetting))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   Set<Object> registrations=new HashSet<>();for(var fact:facts)if("bridge".equals(fact.get("kind")))registrations.add(fact.get("registration_name"));
   check(registrations.equals(Set.of("native","forward")),"Virtual empty override produced original API effect: "+registrations);
   check(facts.stream().filter(f->"forward".equals(f.get("registration_name"))).count()==1,"Super-forwarding override emitted duplicated parent effect");
   var host=engine.new Host("dispatch.Direct");var job=new CapabilityEngine.Job(helper,List.of(),List.of(),false);
   var bridge=DexFlow.V.of("object","test.Bridge","bridge");var blockedView=DexFlow.V.of("object","dispatch.Blocked","blocked");var nativeView=DexFlow.V.of("object","android.webkit.WebView","native");
   var call=new DexFlow.Call(W+"->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V",0,List.of(),false,false,false);
   engine.dispatchCapability(host,job,call,List.of(DexFlow.union(blockedView,nativeView),bridge,DexFlow.V.literal("java.lang.String","mixed")),"bridge",false);
   while(!host.queue.isEmpty())engine.processJob(host);
   check(host.facts.size()==1&&"native".equals(((Map<?,?>)host.facts.values().iterator().next().get("webview")).get("id")),"Mixed receivers leaked blocked capability");
   var unknown=DexFlow.V.of("unknown","android.webkit.WebView","unresolved");
   engine.dispatchCapability(host,job,call,List.of(unknown,bridge,DexFlow.V.literal("java.lang.String","unknown")),"bridge",false);
   check(host.facts.values().stream().anyMatch(f->"unknown".equals(f.get("registration_name"))&&"candidate".equals(f.get("binding_status"))),"Unresolved polymorphic receiver lost its conditional candidate");
   int prior=host.facts.size();
   engine.dispatchCapability(host,job,new DexFlow.Call(W+"->setWebViewClient(Landroid/webkit/WebViewClient;)V",1,List.of(),false,false,false),List.of(blockedView,DexFlow.V.of("object","dispatch.Client","client")),"callback",false);
   engine.dispatchCapability(host,job,new DexFlow.Call(W+"->removeJavascriptInterface(Ljava/lang/String;)V",2,List.of(),false,false,false),List.of(blockedView,DexFlow.V.literal("java.lang.String","removed")),"bridge_removal",false);
   engine.dispatchCapability(host,job,new DexFlow.Call(S+"->setJavaScriptEnabled(Z)V",3,List.of(),false,false,false),List.of(DexFlow.V.of("object","dispatch.BlockedSettings","settings"),DexFlow.V.literal("number","1")),"setting",false);
   while(!host.queue.isEmpty())engine.processJob(host);
   check(host.facts.size()==prior,"Client/settings/removal empty overrides produced API effects");
  }finally{Files.deleteIfExists(file);}
  System.out.println("CapabilityDispatchFixture PASS: empty virtual overrides suppress effects; explicit super/native retained; union isolation; unknown candidates; client/settings/removal dispatch.");
 }
}
