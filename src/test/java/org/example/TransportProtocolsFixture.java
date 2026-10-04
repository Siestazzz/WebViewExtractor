package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** DEX-derived registration expressions retain caller identity and swapped argument positions. */
final class TransportProtocolsFixture {
    static final String ROUTER="Lprotocol/Router;", HANDLER="Lprotocol/Handler;", MAP="Ljava/util/Map;";
    public static void main(String[] args) throws Exception { run(); }
    static void run() throws Exception {
        String registry=ROUTER+"->handlers:"+MAP;
        var register=method(ROUTER,"register",List.of(HANDLER,"Ljava/lang/String;"),1,4,List.of(
                new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(ROUTER,"handlers",MAP)),
                invoke(Opcode.INVOKE_INTERFACE,MAP,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,3,2),
                new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(ROUTER,"unrelated",MAP)),
                invoke(Opcode.INVOKE_INTERFACE,MAP,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,3,2),end()),false);
        var staticRegister=method(ROUTER,"install",List.of(ROUTER,"Ljava/lang/String;",HANDLER),9,4,List.of(
                new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(ROUTER,"handlers",MAP)),
                invoke(Opcode.INVOKE_INTERFACE,MAP,"put",List.of("Ljava/lang/Object;","Ljava/lang/Object;"),"Ljava/lang/Object;",0,2,3),end()),false);
        var fields=List.of(new ImmutableField(ROUTER,"handlers",MAP,1,null,Set.of(),Set.of()),
                new ImmutableField(ROUTER,"unrelated",MAP,1,null,Set.of(),Set.of()));
        var classes=new ArrayList<ImmutableClassDef>();
        classes.add(new ImmutableClassDef(ROUTER,1,"Ljava/lang/Object;",List.of(),null,Set.of(),fields,List.of(register,staticRegister)));
        classes.add(clazz(HANDLER,"Ljava/lang/Object;"));
        classes.add(clazz("Lprotocol/Chrome;","Landroid/webkit/WebChromeClient;"));
        classes.add(clazz("Lprotocol/X5Chrome;","Lcom/tencent/smtt/sdk/WebChromeClient;"));
        classes.add(clazz("Lprotocol/Unrelated;","Ljava/lang/Object;"));
        Path dex=Files.createTempFile("transport-protocols-",".dex");
        try {
            DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));
            long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);
            var flow=new DexFlow(index,deadline);
            var forms=TransportProtocols.registrations(index,register,flow.summary(register),Set.of(registry));
            check(forms.size()==1,"Unrelated map must not join the transport registry");
            var form=forms.get(0);check(form.owner().id().equals("0")&&form.name().id().equals("2")&&form.handler().id().equals("1"),"Swapped registration parameters were not preserved");
            var first=DexFlow.V.of("object","protocol.Router","first-router");var second=DexFlow.V.of("object","protocol.Router","second-router");
            var firstHandler=DexFlow.V.of("object","protocol.Handler","first-handler");var secondHandler=DexFlow.V.of("object","protocol.Handler","second-handler");
            var firstArgs=List.of(first,firstHandler,DexFlow.V.literal("java.lang.String","same-name"));
            var secondArgs=List.of(second,secondHandler,DexFlow.V.literal("java.lang.String","same-name"));
            check(!TransportProtocols.bind(form.owner(),firstArgs).equals(TransportProtocols.bind(form.owner(),secondArgs)),"Two router instances merged by registration name");
            check(TransportProtocols.bind(form.handler(),secondArgs).equals(secondHandler),"Second call reused first handler");
            check(TransportProtocols.bind(form.name(),List.of(first,firstHandler,DexFlow.UNKNOWN)).equals(DexFlow.UNKNOWN),"Dynamic name must remain unresolved");
            var stat=TransportProtocols.registrations(index,staticRegister,flow.summary(staticRegister),Set.of(registry)).get(0);
            check(stat.owner().id().equals("0")&&stat.name().id().equals("1")&&stat.handler().id().equals("2"),"Static argument numbering changed");
            check(TransportProtocols.registrations(index,register,flow.summary(register),Set.of()).isEmpty(),"Map registration alone became a transport");
            for(String owner:List.of("Lprotocol/Chrome;","Lprotocol/X5Chrome;","Lprotocol/Unrelated;")) {
                String family=owner.contains("X5")?"Lcom/tencent/smtt/sdk/ConsoleMessage;":"Landroid/webkit/ConsoleMessage;";
                var callback=new ImmutableMethod(owner,"onConsoleMessage",List.of(new ImmutableMethodParameter(family,Set.of(),null)),"Z",1,Set.of(),Set.of(),null);
                check(TransportProtocols.clientEntry(index,callback)==!owner.contains("Unrelated"),"Transport entry must respect SDK inheritance");
                var wrong=new ImmutableMethod(owner,"onConsoleMessage",List.of(),"Z",1,Set.of(),Set.of(),null);
                check(!TransportProtocols.clientEntry(index,wrong),"Same-name overload became a callback");
                var statik=new ImmutableMethod(owner,"onConsoleMessage",callback.getParameters(),"Z",9,Set.of(),Set.of(),null);
                check(!TransportProtocols.clientEntry(index,statik),"Static helper became a callback");
            }
        } finally { Files.deleteIfExists(dex); }
        System.out.println("TransportProtocolsFixture PASS: swapped/static arguments, same-name object isolation, dynamic names, unrelated maps and exact SDK callbacks. Discovery alone emits no capability.");
    }
}
