package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Only the callback actually invoked by a forwarding helper may contribute capabilities. */
public final class NearCallbackProbe {
 public static void main(String[] args)throws Exception {run();}
 static void run()throws Exception {
  String callback="Ltest/Completion;",impl="Ltest/CapturedCompletion;",dispatch="Ltest/Dispatcher;";
  var contract=new ImmutableMethod(callback,"complete",List.of(),"V",0x401,Set.of(),Set.of(),null);
  var field=new ImmutableField(impl,"view",W,1,null,Set.of(),Set.of());
  var ctor=method(impl,"<init>",List.of(W),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(impl,"view",W)),end()),false);
  var complete=method(impl,"complete",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(impl,"view",W)),invoke(Opcode.INVOKE_STATIC,"Ltest/Step4;","run",List.of(W),"V",0),end()),false);
  var forward=method(dispatch,"forward",List.of(callback),9,1,List.of(invoke(Opcode.INVOKE_INTERFACE,callback,"complete",List.of(),"V",0),end()),false);
  var ignore=method(dispatch,"ignore",List.of(callback),9,1,List.of(end()),false);
  var entry=method(A,"onCreate",List.of(),1,5,List.of(
   make(0,W),str(3,"https://fixture.invalid"),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",0,3),
   make(1,impl),invoke(Opcode.INVOKE_DIRECT,impl,"<init>",List.of(W),"V",1,0),invoke(Opcode.INVOKE_STATIC,dispatch,"forward",List.of(callback),"V",1),
   make(2,W),make(1,impl),invoke(Opcode.INVOKE_DIRECT,impl,"<init>",List.of(W),"V",1,2),invoke(Opcode.INVOKE_STATIC,dispatch,"ignore",List.of(callback),"V",1),end()),false);
  List<ImmutableClassDef> classes=new ArrayList<>();
  classes.add(clazz(A,"Landroid/app/Activity;",entry));
  classes.add(new ImmutableClassDef(callback,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(contract)));
  classes.add(new ImmutableClassDef(impl,1,"Ljava/lang/Object;",List.of(callback),null,Set.of(),List.of(field),List.of(ctor,complete)));
  classes.add(clazz(dispatch,"Ljava/lang/Object;",forward,ignore));
  for(int i=1;i<=3;i++)classes.add(clazz("Ltest/Step"+i+";","Ljava/lang/Object;",method("Ltest/Step"+i+";","run",List.of(W),9,1,List.of(invoke(Opcode.INVOKE_STATIC,"Ltest/Step"+(i+1)+";","run",List.of(W),"V",0),end()),false)));
  classes.add(clazz("Ltest/Step4;","Ljava/lang/Object;",method("Ltest/Step4;","run",List.of(W),9,3,List.of(make(0,B),str(1,"completed"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),end()),false)));
  classes.add(clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)));
  Path file=Files.createTempFile("callback-argument-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   var apk=new ApkInventory();apk.targetSdk=30;var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   var bridges=facts.stream().filter(f->"bridge".equals(f.get("kind"))).toList();
   System.out.println("OBSERVED_BRIDGES="+bridges.size()+" EXPECTED=1");
   check(bridges.size()==1,"Constructed but unregistered near-API callback executed");
   String identity=(String)((Map<?,?>)bridges.get(0).get("webview")).get("id");
   check(identity.startsWith(A+"->onCreate()V@0|"),"Callback attached capabilities to the other WebView: "+identity);
  }finally{Files.deleteIfExists(file);}
 }
}
