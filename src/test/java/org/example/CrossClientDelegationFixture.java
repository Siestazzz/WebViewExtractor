package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** An installed Chrome callback can forward to a nonstandard method on another Client. */
final class CrossClientDelegationFixture {
 static final String CHROME="Landroid/webkit/WebChromeClient;", CLIENT="Landroid/webkit/WebViewClient;", WRAP="Lcross/Wrapper;", DELEGATE="Lcross/Delegate;", PARENT="Lcross/Parent;";
 public static void main(String[] args)throws Exception {run();}
 static void run()throws Exception {
  var ref=new ImmutableFieldReference(WRAP,"delegate",DELEGATE);
  var field=new ImmutableField(WRAP,"delegate",DELEGATE,0x11,null,Set.of(),Set.of());
  var ctor=method(WRAP,"<init>",List.of(DELEGATE),0x10001,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,ref),end()),false);
  var title=method(WRAP,"onReceivedTitle",List.of(W,"Ljava/lang/String;"),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,ref),invoke(Opcode.INVOKE_VIRTUAL,DELEGATE,"onReceivedTitle",List.of(W,"Ljava/lang/String;"),"V",0,2,3),end()),false);
  var wrong=method(WRAP,"onReceivedTitle",List.of("Ljava/lang/String;"),1,3,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,ref),invoke(Opcode.INVOKE_VIRTUAL,DELEGATE,"onPageFinished",List.of(W,"Ljava/lang/String;"),"V",0,1,2),end()),false);
  List<org.jf.dexlib2.iface.instruction.Instruction> code=new ArrayList<>();
  for(int i=0;i<3;i++){
   code.add(make(0,W));code.add(make(1,DELEGATE));code.add(make(2,WRAP));code.add(invoke(Opcode.INVOKE_DIRECT,WRAP,"<init>",List.of(DELEGATE),"V",2,1));
   if(i<2)code.add(invoke(Opcode.INVOKE_VIRTUAL,W,"setWebChromeClient",List.of(CHROME),"V",0,2));
  }
  code.add(end());
  Path file=Files.createTempFile("cross-client-",".dex");
  try {
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",method(A,"onCreate",List.of(),1,4,code,false)),new ImmutableClassDef(WRAP,1,CHROME,List.of(),null,Set.of(),List.of(field),List.of(ctor,title,wrong)),clazz(DELEGATE,PARENT,method(DELEGATE,"onReceivedTitle",List.of(W,"Ljava/lang/String;"),1,3,List.of(invoke(Opcode.INVOKE_SUPER,PARENT,"onReceivedTitle",List.of(W,"Ljava/lang/String;"),"V",0,1,2),end()),false),method(DELEGATE,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false)),clazz(PARENT,CLIENT,method(PARENT,"onReceivedTitle",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   var delegated=facts.stream().filter(f->"cross.Delegate".equals(f.get("implementation"))).toList();
   check(delegated.size()==2,"Actual cross-contract delegates missing or uninstalled object activated: "+delegated);
   check(delegated.stream().map(f->f.get("webview")).distinct().count()==2&&delegated.stream().map(f->f.get("delegate_object_id")).distinct().count()==2,"Cross-contract receiver identities merged");
   for(var fact:delegated){
    @SuppressWarnings("unchecked")var members=(List<Map<String,Object>>)fact.get("members");
    check(members.size()==2&&members.stream().map(m->m.get("signature")).collect(java.util.stream.Collectors.toSet()).equals(Set.of(DELEGATE+"->onReceivedTitle("+W+"Ljava/lang/String;)V",PARENT+"->onReceivedTitle("+W+"Ljava/lang/String;)V")),"Explicit inherited implementation missing or unrelated member included: "+members);
   }
  } finally {Files.deleteIfExists(file);}
  System.out.println("CrossClientDelegationFixture PASS: actual installed same-signature cross-contract forwarding, isolated objects, unused overload excluded.");
 }
}
