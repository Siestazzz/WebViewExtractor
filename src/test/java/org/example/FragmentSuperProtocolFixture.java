package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.ServiceConstructorArrayProbe.*;
/** Explicit super uses the declared SDK contract; virtual calls still obey application overrides. */
final class FragmentSuperProtocolFixture {
 static void run()throws Exception{
  for(String family:List.of("android.app","androidx.fragment.app","android.support.v4.app"))for(String mode:List.of("super","super-alias","null")){
   String prefix="L"+family.replace('.','/')+"/",sdk=family.equals("android.app")?"Landroid/app/Activity;":prefix+"FragmentActivity;",manager=prefix+"FragmentManager;",transaction=prefix+"FragmentTransaction;",fragment=prefix+"Fragment;",host="Lsuperprotocol/Host;",content="Lsuperprotocol/Content;",alias="Lsuperprotocol/Alias;",getter=family.equals("android.app")?"getFragmentManager":"getSupportFragmentManager",bundle="Landroid/os/Bundle;";
   List<Instruction> getterBody=new ArrayList<>();
   if(mode.equals("null"))getterBody.add(zero(0));else{
    getterBody.add(call(Opcode.INVOKE_SUPER,sdk,getter,List.of(),manager,1));getterBody.add(result(0));
    if(mode.equals("super-alias")){getterBody.add(call(Opcode.INVOKE_STATIC,alias,"pass",List.of(manager),manager,0));getterBody.add(result(0));}
   }getterBody.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0));
   var entry=method(host,"onCreate",List.of(bundle),"V",1,8,List.of(make(0,content),call(Opcode.INVOKE_DIRECT,content,"<init>",List.of(),"V",0),call(Opcode.INVOKE_VIRTUAL,sdk,getter,List.of(),manager,6),result(1),call(Opcode.INVOKE_VIRTUAL,manager,"beginTransaction",List.of(),transaction,1),result(1),str(2,"installed"),call(Opcode.INVOKE_VIRTUAL,transaction,"add",List.of(fragment,"Ljava/lang/String;"),transaction,1,0,2),call(Opcode.INVOKE_VIRTUAL,transaction,"commit",List.of(),"I",1),end()),false);
   var callback=method(content,"onCreate",List.of(bundle),"V",1,6,List.of(make(0,W),call(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,0),result(1),new ImmutableInstruction11n(Opcode.CONST_4,2,1),call(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",1,2),make(1,C),call(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V",0,1),make(1,B),str(2,"super"),call(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(OBJ,"Ljava/lang/String;"),"V",0,1,2),end()),false);
   List<ImmutableClassDef> defs=List.of(clazz(host,sdk,List.of(),List.of(),entry,method(host,getter,List.of(),manager,1,2,getterBody,false)),clazz(sdk,sdk.equals("Landroid/app/Activity;")?OBJ:"Landroid/app/Activity;",List.of(),List.of(),method(sdk,getter,List.of(),manager,1,2,List.of(make(0,manager),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false)),clazz(manager,OBJ,List.of(),List.of(),method(manager,"beginTransaction",List.of(),transaction,1,2,List.of(make(0,transaction),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false)),clazz(transaction,OBJ,List.of(),List.of(),method(transaction,"add",List.of(fragment,"Ljava/lang/String;"),transaction,1,3,List.of(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),method(transaction,"commit",List.of(),"I",1,2,List.of(zero(0),new ImmutableInstruction11x(Opcode.RETURN,0)),false)),clazz(content,fragment,List.of(),List.of(),method(content,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),callback),clazz(alias,OBJ,List.of(),List.of(),method(alias,"pass",List.of(manager),manager,9,1,List.of(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false)),clazz(B,OBJ,List.of(),List.of(),method(B,"endpoint",List.of(),"V",1,1,List.of(end()),true)),clazz(C,"Landroid/webkit/WebViewClient;",List.of(),List.of(),method(C,"onPageFinished",List.of(W,"Ljava/lang/String;"),"V",1,3,List.of(end()),false)));
   Path path=Files.createTempFile("fragment-super-",".dex");try{
    DexFileFactory.writeDexFile(path.toString(),new ImmutableDexFile(Opcodes.getDefault(),defs));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(path,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);engine.analyzeActivity("superprotocol.Host");Set<String> kinds=new HashSet<>();
    for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)kinds.add(String.valueOf(fact.get("kind")));}
    if(mode.equals("null")?!Collections.disjoint(kinds,Set.of("bridge","setting","callback")):!kinds.containsAll(Set.of("bridge","setting","callback")))throw new AssertionError("Explicit super protocol boundary: "+family+" "+mode+" "+kinds+" "+engine.diagnostics);
   }finally{Files.deleteIfExists(path);}
  }
  System.out.println("FragmentSuperProtocolFixture PASS: three SDK families, actual application super bodies, returned alias and null override negative.");
 }
}
