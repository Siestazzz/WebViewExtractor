package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

final class DownloadListenerFixture {
    public static void main(String[] args)throws Exception { run(); }
    static void run()throws Exception {for(String prefix:List.of("android/webkit/","com/tencent/smtt/sdk/"))run(prefix);}
    static void run(String prefix)throws Exception {
        String listener="L"+prefix+"DownloadListener;",view="L"+prefix+"WebView;",parent="Ldownload/Parent;",child="Ldownload/Child;";
        List<String> parameters=List.of("Ljava/lang/String;","Ljava/lang/String;","Ljava/lang/String;","Ljava/lang/String;","J");
        var callback=method(parent,"onDownloadStart",parameters,1,7,List.of(end()),false);
        var overload=method(parent,"onDownloadStart",List.of("Ljava/lang/String;"),1,2,List.of(end()),false);
        var classes=new ArrayList<ImmutableClassDef>();
        classes.add(new ImmutableClassDef(parent,1,"Ljava/lang/Object;",List.of(listener),null,Set.of(),List.of(),List.of(callback,overload)));
        classes.add(clazz(child,parent));
        var entry=method(A,"onCreate",List.of(),1,5,List.of(
                make(0,view),make(1,child),invoke(Opcode.INVOKE_VIRTUAL,view,"setDownloadListener",List.of(listener),"V",0,1),
                make(2,view),new ImmutableInstruction11n(Opcode.CONST_4,3,0),
                invoke(Opcode.INVOKE_VIRTUAL,view,"setDownloadListener",List.of(listener),"V",2,3),end()),false);
        classes.add(clazz(A,"Landroid/app/Activity;",entry));
        Path dex=Files.createTempFile("download-listener-",".dex");
        try {
            DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));
            long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);
            var engine=new CapabilityEngine(index,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
            check(!engine.activities.isEmpty(),"Download registration is missing as an Activity capability");
            @SuppressWarnings("unchecked") var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
            var registered=facts.stream().filter(f->f.get("kind").equals("callback")).toList();
            var resets=facts.stream().filter(f->f.get("kind").equals("callback_removal")).toList();
            check(registered.size()==1&&resets.size()==1,"Download registration/removal mixed: "+facts);
            var fact=registered.get(0);check(fact.get("implementation").equals("download.Child"),"Anonymous/subclass identity was replaced by parent");
            @SuppressWarnings("unchecked") var members=(List<Map<String,Object>>)fact.get("members");
            check(members.size()==1&&members.get(0).get("signature").equals(CapabilityIndex.key(callback)),"Inherited callback signature or overload exclusion failed: "+members);
            check(!fact.get("webview").equals(resets.get(0).get("webview")),"Two WebViews share download capability");
            check(index.kind(new ImmutableMethodReference(view,"setDownloadListener",List.of("Ljava/lang/String;"),"V"))==null,"Same-name wrong parameter is a registration");
            String other=prefix.equals("android/webkit/")?"Lcom/tencent/smtt/sdk/DownloadListener;":"Landroid/webkit/DownloadListener;";
            check(index.kind(new ImmutableMethodReference(view,"setDownloadListener",List.of(other),"V"))==null,"Wrong SDK family became a registration");
            check(index.kind(new ImmutableMethodReference("Ldownload/Unrelated;","setDownloadListener",List.of(listener),"V"))==null,"Unrelated owner is a WebView registration");
            check(!index.standardClientCallback("download.Child",method(parent,"onDownloadStart",parameters,9,6,List.of(end()),false)),"Static method is a DownloadListener callback");
        } finally {Files.deleteIfExists(dex);}
        System.out.println("DownloadListenerFixture PASS: inherited exact callback, actual child identity, two WebViews, null reset and unrelated overload negatives.");
    }
}
