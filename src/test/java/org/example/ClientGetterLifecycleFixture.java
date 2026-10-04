package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Reach a virtual lifecycle hook and the client getter's actual constructor field writes. */
final class ClientGetterLifecycleFixture {
 public static void main(String[] args)throws Exception{run(args.length>0);}
 static void run()throws Exception{run(false);run(true);}
 static void run(boolean direct)throws Exception {
  String view="Lgetter/CustomView;",holder="Lgetter/ClientHolder;",client="Lgetter/SpecificClient;",base="Lgetter/BaseFragment;",child="Lgetter/ChildFragment;";
  var field=new ImmutableField(holder,"client","Landroid/webkit/WebViewClient;",0x11,null,Set.of(),Set.of());
  var holderCtor=method(holder,"<init>",List.of(),1,2,List.of(make(0,client),invoke(Opcode.INVOKE_DIRECT,client,"<init>",List.of(),"V",0),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(holder,"client","Landroid/webkit/WebViewClient;")),end()),false);
  var getterBody=method(holder,"getClient",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(holder,"client","Landroid/webkit/WebViewClient;")),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  var getter=new ImmutableMethod(holder,"getClient",List.of(),"Landroid/webkit/WebViewClient;",1,Set.of(),Set.of(),getterBody.getImplementation());
  var ctor=method(view,"<init>",List.of(),1,3,List.of(make(0,holder),invoke(Opcode.INVOKE_DIRECT,holder,"<init>",List.of(),"V",0),invoke(Opcode.INVOKE_VIRTUAL,holder,"getClient",List.of(),"Landroid/webkit/WebViewClient;",0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),invoke(Opcode.INVOKE_SUPER,W,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V",2,0),end()),false);
  var lifecycle=method(base,"onActivityCreated",List.of("Landroid/os/Bundle;"),1,2,List.of(invoke(Opcode.INVOKE_VIRTUAL,base,"prepare",List.of("Landroid/os/Bundle;"),"V",0,1),end()),false);
  var hook=method(base,"prepare",List.of("Landroid/os/Bundle;"),1,2,List.of(end()),false);
  var actualHook=method(child,"prepare",List.of("Landroid/os/Bundle;"),1,3,List.of(make(0,view),invoke(Opcode.INVOKE_DIRECT,view,"<init>",List.of(),"V",0),end()),false);
  var unused=method(child,"unused",List.of(),1,4,List.of(make(0,W),make(1,B),str(2,"unused-lifecycle-helper"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  String made=direct?view:child;
  var entryCode=new ArrayList<org.jf.dexlib2.iface.instruction.Instruction>(List.of(make(0,made),invoke(Opcode.INVOKE_DIRECT,made,"<init>",List.of(),"V",0)));
  if(!direct)entryCode.addAll(List.of(invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentActivity;","getSupportFragmentManager",List.of(),"Landroidx/fragment/app/FragmentManager;",3),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentManager;","beginTransaction",List.of(),"Landroidx/fragment/app/FragmentTransaction;",1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),str(2,"child"),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentTransaction;","add",List.of("Landroidx/fragment/app/Fragment;","Ljava/lang/String;"),"Landroidx/fragment/app/FragmentTransaction;",1,0,2),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentTransaction;","commit",List.of(),"I",1)));
  entryCode.add(end());var entry=method(A,"onCreate",List.of(),1,4,entryCode,false);
  Path file=Files.createTempFile("client-getter-lifecycle-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroidx/fragment/app/FragmentActivity;",entry),clazz(view,W,ctor),new ImmutableClassDef(holder,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(field),List.of(holderCtor,getter)),clazz(client,"Landroid/webkit/WebViewClient;",method(client,"<init>",List.of(),1,1,List.of(end()),false),method(client,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false)),clazz(base,"Landroidx/fragment/app/Fragment;",lifecycle,hook),clazz(child,base,method(child,"<init>",List.of(),1,1,List.of(end()),false),actualHook,unused),clazz(B,"Ljava/lang/Object;"))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   check(!engine.activities.isEmpty(),"Inherited lifecycle calling an actual override did not reach its capability");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   check(facts.stream().anyMatch(f->"callback".equals(f.get("kind"))&&"getter.SpecificClient".equals(f.get("implementation"))),"Actual SDK-client getter constructor binding lost: "+facts);
   check(facts.stream().noneMatch(f->"unused-lifecycle-helper".equals(f.get("registration_name"))),"Virtual lifecycle resolution seeded an uncalled helper");
  }finally{Files.deleteIfExists(file);}
  System.out.println("ClientGetterLifecycleFixture PASS: "+(direct?"actual client getter constructor origin":"inherited lifecycle actual receiver override"));
 }
}
