package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.CapabilitySelfTest.*;

/** Diagnostic only: nullable override distinguishes reset from actual wrapper installation. */
final class NullClientOverrideDiagnosticFixture {
    public static void main(String[] args)throws Exception {
        String view="Lnullable/GuardedView;",wrapper="Lnullable/Wrapper;",delegate="Lnullable/Delegate;",client="Landroid/webkit/WebViewClient;";
        var setter=method(view,"setWebViewClient",List.of(client),1,4,List.of(new ImmutableInstruction21t(Opcode.IF_EQZ,3,11),make(0,wrapper),invoke(Opcode.INVOKE_DIRECT,wrapper,"<init>",List.of(),"V",0),invoke(Opcode.INVOKE_SUPER,W,"setWebViewClient",List.of(client),"V",2,0),end(),invoke(Opcode.INVOKE_SUPER,W,"setWebViewClient",List.of(client),"V",2,3),end()),false);
        var finished=method(wrapper,"onPageFinished",List.of(W,"Ljava/lang/String;"),1,3,List.of(end()),false);
        var intercepted=AspectJFixture.returning(method(wrapper,"shouldInterceptRequest",List.of(W,"Landroid/webkit/WebResourceRequest;"),1,3,List.of(new ImmutableInstruction11n(Opcode.CONST_4,0,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),"Landroid/webkit/WebResourceResponse;");
        var entry=method(A,"onCreate",List.of(),1,4,List.of(make(0,view),new ImmutableInstruction11n(Opcode.CONST_4,1,0),invoke(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of(client),"V",0,1),make(0,view),make(1,delegate),invoke(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of(client),"V",0,1),end()),false);
        Path file=Files.createTempFile("nullable-client-override-",".dex");try{
            DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(view,W,setter),clazz(wrapper,client,method(wrapper,"<init>",List.of(),0x10001,1,List.of(end()),false),finished,intercepted),clazz(delegate,client))));
            long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(file,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
            @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
            List<Map<String,Object>> wrappers=facts.stream().filter(f->"nullable.Wrapper".equals(f.get("implementation"))).toList();
            System.out.println("wrapper_webviews="+wrappers.stream().map(f->f.get("webview")).distinct().toList());
            System.out.println("wrapper_members="+wrappers.stream().flatMap(f->((List<?>)f.get("members")).stream()).toList());
            System.out.println("refined_summaries="+engine.flow.refined);
            System.out.println("first_webview_null_reset_false_wrapper="+wrappers.stream().anyMatch(f->f.get("webview").toString().contains("onCreate()V@0|")));
            check(wrappers.stream().anyMatch(f->!f.get("webview").toString().contains("onCreate()V@0|")),"Actual non-null installation was lost");
        }finally{Files.deleteIfExists(file);}
    }
}
