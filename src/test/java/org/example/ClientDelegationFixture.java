package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Installed wrapper delegates only actually called complete standard callback signatures. */
final class ClientDelegationFixture {
 static final String C="Landroid/webkit/WebChromeClient;", HOLDER="Ldelegate/Holder;",WRAPPER="Ldelegate/Wrapper;",D="Ldelegate/Client;";
 public static void main(String[] args)throws Exception{run();}
 static void run()throws Exception{
  var slot=new ImmutableField(HOLDER,"client",C,0x11,null,Set.of(),Set.of());var slotRef=new ImmutableFieldReference(HOLDER,"client",C);
  var capture=new ImmutableField(WRAPPER,"holder",HOLDER,0x11,null,Set.of(),Set.of());var captureRef=new ImmutableFieldReference(WRAPPER,"holder",HOLDER);
  String child="Ldelegate/ChildWrapper;";
  var childCtor=method(child,"<init>",List.of(HOLDER),0x10001,2,List.of(invoke(Opcode.INVOKE_DIRECT,WRAPPER,"<init>",List.of(HOLDER),"V",0,1),end()),false);
  var childProgress=method(child,"onProgressChanged",List.of(W,"I"),1,3,List.of(invoke(Opcode.INVOKE_SUPER,WRAPPER,"onProgressChanged",List.of(W,"I"),"V",0,1,2),end()),false);
  var holderCtor=method(HOLDER,"<init>",List.of(C),0x10001,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,slotRef),end()),false);
  var wrapperCtor=method(WRAPPER,"<init>",List.of(HOLDER),0x10001,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,captureRef),end()),false);
  var progress=method(WRAPPER,"onProgressChanged",List.of(W,"I"),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,captureRef),new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,0,slotRef),invoke(Opcode.INVOKE_VIRTUAL,C,"onProgressChanged",List.of(W,"I"),"V",0,2,3),end()),false);
  var history=method(WRAPPER,"getVisitedHistory",List.of("Landroid/webkit/ValueCallback;"),1,2,List.of(invoke(Opcode.INVOKE_DIRECT,WRAPPER,"forwardHistory",List.of("Landroid/webkit/ValueCallback;"),"V",0,1),end()),false);
  var helper=method(WRAPPER,"forwardHistory",List.of("Landroid/webkit/ValueCallback;"),2,3,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,captureRef),new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,0,slotRef),invoke(Opcode.INVOKE_VIRTUAL,C,"getVisitedHistory",List.of("Landroid/webkit/ValueCallback;"),"V",0,2),end()),false);
  var wrong=method(WRAPPER,"onProgressChanged",List.of("Ljava/lang/String;"),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,0,captureRef),new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,0,slotRef),invoke(Opcode.INVOKE_VIRTUAL,C,"onHideCustomView",List.of(),"V",0),end()),false);
  var empty=method(WRAPPER,"onHideCustomView",List.of(),1,1,List.of(end()),false);
  List<org.jf.dexlib2.iface.instruction.Instruction> code=new ArrayList<>();
  for(int i=0;i<3;i++){
   code.add(make(0,W));code.add(make(1,D));code.add(make(2,HOLDER));code.add(invoke(Opcode.INVOKE_DIRECT,HOLDER,"<init>",List.of(C),"V",2,1));code.add(make(3,child));code.add(invoke(Opcode.INVOKE_DIRECT,child,"<init>",List.of(HOLDER),"V",3,2));
   if(i==0)code.add(new ImmutableInstruction12x(Opcode.MOVE_OBJECT,4,3));
   if(i<2)code.add(invoke(Opcode.INVOKE_VIRTUAL,W,"setWebChromeClient",List.of(C),"V",0,3));
  }code.add(make(0,W));code.add(invoke(Opcode.INVOKE_VIRTUAL,W,"setWebChromeClient",List.of(C),"V",0,4));code.add(end());var entry=method(A,"onCreate",List.of(),1,6,code,false);
  Path file=Files.createTempFile("client-delegation-",".dex");try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(HOLDER,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(slot),List.of(holderCtor)),new ImmutableClassDef(WRAPPER,1,C,List.of(),null,Set.of(),List.of(capture),List.of(wrapperCtor,progress,history,helper,wrong,empty)),clazz(child,WRAPPER,childCtor,childProgress),clazz(D,C,method(D,"onProgressChanged",List.of(W,"I"),1,3,List.of(end()),false),method(D,"getVisitedHistory",List.of("Landroid/webkit/ValueCallback;"),1,2,List.of(end()),false),method(D,"onHideCustomView",List.of(),1,1,List.of(end()),false)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");var delegated=facts.stream().filter(f->"delegate.Client".equals(f.get("implementation"))).toList();
   check(!delegated.isEmpty(),"Installed wrapper actual field delegate has no callback capability");
   Map<Object,Set<Object>> membersByView=new HashMap<>();Set<Object> clientIds=new HashSet<>();for(var fact:delegated){
    @SuppressWarnings("unchecked")var members=(List<Map<String,Object>>)fact.get("members");for(var member:members)membersByView.computeIfAbsent(fact.get("webview"),k->new HashSet<>()).add(member.get("name"));
    clientIds.add(fact.get("delegate_object_id"));
   }
   check(membersByView.size()==3&&clientIds.size()==2,"Uninstalled wrapper activated or separate objects conflated: "+delegated);
   check(membersByView.values().stream().allMatch(names->names.equals(Set.of("onProgressChanged","getVisitedHistory"))),"Uncalled/wrong overload/empty callback falsely activated delegate members: "+membersByView);
   var host=engine.new Host("delegate.UnionHost");var view=DexFlow.V.of("object","android.webkit.WebView","shared_view");
   host.activeClientContext=DexFlow.expr("installed_client_context",W+"->setWebChromeClient("+C+")V","installation",List.of(view,DexFlow.V.of("object","delegate.ChildWrapper","installed")));
   var receivers=DexFlow.union(DexFlow.V.of("object","delegate.Client","delegate_one"),DexFlow.V.of("object","delegate.Client","delegate_two"));
   engine.followClientDelegate(host,new CapabilityEngine.Job(history,List.of(),List.of(),true),new DexFlow.Call(C+"->getVisitedHistory(Landroid/webkit/ValueCallback;)V",1,List.of(receivers,DexFlow.V.of("unknown","android.webkit.ValueCallback","history")),false,false,false));
   check(host.facts.size()==2&&host.facts.values().stream().map(f->f.get("delegate_object_id")).distinct().count()==2,"Same-class union delegates on one WebView lost object provenance");
  }finally{Files.deleteIfExists(file);}
  System.out.println("ClientDelegationFixture PASS: real installed field delegates, helper/no-WebView callbacks, two-object isolation, uninstalled wrapper, invalid overload and unused callbacks excluded.");
 }
}
