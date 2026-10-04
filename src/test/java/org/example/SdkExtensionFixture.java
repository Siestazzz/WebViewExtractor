package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

final class SdkExtensionFixture {
 static final String VIEW="Lcom/tencent/smtt/sdk/WebView;",EXT="Lcom/tencent/smtt/export/external/extension/interfaces/IX5WebViewClientExtension;",CB="Lcom/tencent/smtt/sdk/WebViewCallbackClient;",P="Lextension/Parent;",CHILD="Lextension/Child;";
 public static void main(String[] args)throws Exception{run();}
 static ImmutableMethod objectReturn(ImmutableMethod m){return new ImmutableMethod(m.getDefiningClass(),m.getName(),m.getParameters(),"Ljava/lang/Object;",m.getAccessFlags(),m.getAnnotations(),Set.of(),m.getImplementation());}
 static ImmutableClassDef contract(String type,ImmutableMethod method){return new ImmutableClassDef(type,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(new ImmutableMethod(type,method.getName(),method.getParameters(),method.getReturnType(),0x401,Set.of(),Set.of(),null)));}
 static void run()throws Exception {
  var extended=objectReturn(method(P,"onMiscCallBack",List.of("Ljava/lang/String;","Landroid/os/Bundle;"),1,4,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false));
  var standard=method(P,"computeScroll",List.of("Landroid/view/View;"),1,2,List.of(end()),false);
  var parent=new ImmutableClassDef(P,1,"Ljava/lang/Object;",List.of(EXT,CB),null,Set.of(),List.of(),List.of(extended,standard));
  var create=method(A,"onCreate",List.of(),1,4,List.of(make(0,VIEW),make(1,VIEW),make(2,CHILD),invoke(Opcode.INVOKE_VIRTUAL,VIEW,"setWebViewClientExtension",List.of(EXT),"V",0,2),invoke(Opcode.INVOKE_VIRTUAL,VIEW,"setWebViewCallbackClient",List.of(CB),"V",1,2),end()),false);
  Path dex=Files.createTempFile("sdk-extension-",".dex");
  try {
   DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",create),contract(EXT,extended),contract(CB,standard),parent,clazz(CHILD,P,method(CHILD,"computeScroll",List.of("I"),1,2,List.of(end()),false),method(CHILD,"helper",List.of(),1,1,List.of(end()),false)))));
   long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);
   check(index.kind(new ImmutableMethodReference(W,"setWebViewCallbackClient",List.of(CB),"V"))==null,"X5 extension matched Android WebView");
   check(index.kind(new ImmutableMethodReference(VIEW,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V"))==null,"Wrong SDK family setter matched");
   check(index.kind(new ImmutableMethodReference(VIEW,"setWebViewClient",List.of("Lcom/tencent/smtt/sdk/WebViewClient;"),"I"))==null,"Wrong setter return type matched");
   var engine=new CapabilityEngine(index,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   @SuppressWarnings("unchecked") var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   var callbacks=facts.stream().filter(f->"callback".equals(f.get("kind"))).toList();check(callbacks.size()==2,"Both actual extension installations must be emitted: "+callbacks);
   Set<Object> views=new HashSet<>();
   for(var fact:callbacks){
    views.add(fact.get("webview"));check("extension.Child".equals(fact.get("implementation")),"Inherited callback lost actual implementation type");
    @SuppressWarnings("unchecked") var members=(List<Map<String,Object>>)fact.get("members");
    String expected=fact.get("api").toString().contains("setWebViewClientExtension")?CapabilityIndex.key(extended):CapabilityIndex.key(standard);
    check(members.size()==1&&expected.equals(members.get(0).get("signature")),"Installed contract mixed another interface or wrong overload: "+members);
   }
   check(views.size()==2,"Two extension receivers merged");
  }finally{Files.deleteIfExists(dex);}
  System.out.println("SdkExtensionFixture PASS: exact SDK setters, inherited members, two receivers, per-install contract and wrong family/return/overload negatives.");
 }
}
