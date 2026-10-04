package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import static org.example.ServiceConstructorArrayProbe.*;

/** Standard SDK renderer callbacks: actual registration, inheritance and receiver isolation. */
public final class RenderProcessClientFixture {
 static final String HOST="Lrenderer/Host;", BASE="Lrenderer/Base;", CHILD="Lrenderer/Child;", API="Landroid/webkit/WebViewRenderProcessClient;", PROCESS="Landroid/webkit/WebViewRenderProcess;", HELP="Lrenderer/Helper;";
 static ImmutableDexFile build(String mode) {
  List<ImmutableClassDef> classes=new ArrayList<>();
  classes.add(clazz(BASE,API,List.of(),List.of(),
   method(BASE,"onRenderProcessResponsive",List.of(W,PROCESS),"V",1,3,List.of(end()),false),
   method(BASE,"onRenderProcessUnresponsive",List.of(W,PROCESS),"V",1,3,List.of(end()),false),
   method(BASE,"onRenderProcessResponsive",List.of(W),"V",1,2,List.of(end()),false),
   method(BASE,"helper",List.of(W,PROCESS),"V",1,3,List.of(end()),false)));
  classes.add(clazz(CHILD,BASE,List.of(),List.of(),method(CHILD,"<init>",List.of(),"V",0x10001,1,List.of(end()),false)));
  List<Instruction> code=new ArrayList<>(List.of(make(0,W),make(1,W),make(2,CHILD),call(Opcode.INVOKE_DIRECT,CHILD,"<init>",List.of(),"V",2)));
  if(mode.equals("executor")){code.add(zero(3));code.add(call(Opcode.INVOKE_VIRTUAL,W,"setWebViewRenderProcessClient",List.of("Ljava/util/concurrent/Executor;",API),"V",0,3,2));}
  else if(mode.equals("wrapper")){code.add(call(Opcode.INVOKE_STATIC,HELP,"install",List.of(W,API),"V",0,2));}
  else if(mode.equals("null")){code.add(zero(2));code.add(call(Opcode.INVOKE_VIRTUAL,W,"setWebViewRenderProcessClient",List.of(API),"V",0,2));}
  else if(mode.equals("wrong-signature")){code.add(call(Opcode.INVOKE_VIRTUAL,W,"setWebViewRenderProcessClient",List.of(OBJ),"V",0,2));}
  else if(!mode.equals("uninstalled")){code.add(call(Opcode.INVOKE_VIRTUAL,W,"setWebViewRenderProcessClient",List.of(API),"V",0,2));}
  code.add(end());
  classes.add(clazz(HOST,"Landroid/app/Activity;",List.of(),List.of(),method(HOST,"onCreate",List.of("Landroid/os/Bundle;"),"V",1,6,code,false)));
  classes.add(clazz(HELP,OBJ,List.of(),List.of(),method(HELP,"install",List.of(W,API),"V",9,2,List.of(call(Opcode.INVOKE_VIRTUAL,W,"setWebViewRenderProcessClient",List.of(API),"V",0,1),end()),false)));
  return new ImmutableDexFile(Opcodes.getDefault(),classes);
 }
 public static void run()throws Exception {
  for(String mode:List.of("direct","executor","wrapper","null","wrong-signature","uninstalled")) {
   Path dex=Files.createTempFile("render-client-",".dex");
   try {
    DexFileFactory.writeDexFile(dex.toString(),build(mode));long deadline=System.nanoTime()+30_000_000_000L;
    CapabilityIndex idx=new CapabilityIndex();idx.read(dex,deadline);ApkInventory apk=new ApkInventory();apk.activities.add("renderer.Host");CapabilityEngine engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("renderer.Host");
    List<Map<?,?>> facts=new ArrayList<>();for(var activity:engine.activities)for(Object fact:(List<?>)activity.get("facts")){Map<?,?> f=(Map<?,?>)fact;if("callback".equals(f.get("kind")))facts.add(f);}
    boolean positive=Set.of("direct","executor","wrapper").contains(mode);
    if(positive){if(facts.size()!=1)throw new AssertionError(mode+" registration/isolation "+facts);
     Map<?,?> f=facts.get(0);if(!String.valueOf(((Map<?,?>)f.get("webview")).get("id")).contains("->onCreate(Landroid/os/Bundle;)V@0"))throw new AssertionError("Wrong WebView receiver: "+f);if(!"renderer.Child".equals(f.get("implementation")))throw new AssertionError("Executor selected as client: "+f);
     List<?> members=(List<?>)f.get("members");if(members.size()!=2||!members.toString().contains("renderer/Base;->onRenderProcessResponsive")||!members.toString().contains("renderer/Base;->onRenderProcessUnresponsive"))throw new AssertionError("Inherited full SDK surface: "+members);
    }else if(!facts.isEmpty())throw new AssertionError("False renderer registration: "+mode+facts);
   }finally{Files.deleteIfExists(dex);}
  }
 }
 public static void main(String[] args)throws Exception{run();System.out.println("RenderProcessClientFixture PASS");}
}
