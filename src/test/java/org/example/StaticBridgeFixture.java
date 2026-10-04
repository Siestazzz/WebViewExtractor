package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.CapabilitySelfTest.*;

/** The bridge reflection contract differs from a virtual Client override contract. */
final class StaticBridgeFixture {
    public static void main(String[] args)throws Exception { run(); }
    static void run()throws Exception {
        String parent="Lstaticbridge/Parent;",child="Lstaticbridge/Child;";
        var inherited=method(parent,"inherited",List.of(),9,0,List.of(end()),true);
        var hiddenParent=method(parent,"hidden",List.of(),9,0,List.of(end()),true);
        var hiddenChild=method(child,"hidden",List.of(),9,0,List.of(end()),true);
        var instance=method(child,"instance",List.of(),1,1,List.of(end()),true);
        var entry=method(A,"onCreate",List.of(),1,4,List.of(make(0,W),make(1,child),str(2,"bridge"),
                invoke(org.jf.dexlib2.Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
        var classes=List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(parent,"Ljava/lang/Object;",inherited,hiddenParent),
                clazz(child,parent,hiddenChild,instance,
                        method(child,"unannotated",List.of(),9,0,List.of(end()),false),
                        method(child,"privateStatic",List.of(),10,0,List.of(end()),true),
                        method(child,"protectedStatic",List.of(),12,0,List.of(end()),true)));
        Path dex=Files.createTempFile("static-bridge-",".dex");
        try {
            DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));
            long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);
            var inventory=new ApkInventory();inventory.targetSdk=35;
            var engine=new CapabilityEngine(index,inventory,deadline);engine.analyzeActivity("test.AppActivity");
            @SuppressWarnings("unchecked") var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
            var bridges=facts.stream().filter(f->f.get("kind").equals("bridge")).toList();check(bridges.size()==1,"Expected one registration");
            @SuppressWarnings("unchecked") var members=(List<Map<String,Object>>)bridges.get(0).get("members");
            Set<Object> signatures=new HashSet<>();for(var member:members)signatures.add(member.get("signature"));
            check(signatures.equals(Set.of(CapabilityIndex.key(inherited),CapabilityIndex.key(hiddenChild),CapabilityIndex.key(instance))),"Static inheritance, hiding or access/annotation filtering failed: "+signatures);
        } finally {Files.deleteIfExists(dex);}
        System.out.println("StaticBridgeFixture PASS: annotated public static and instance members; inheritance/hiding; private/protected/unannotated negatives.");
    }
}
