package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.DexFlow.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Staged two-receiver alias refresh; distinguishes immediate reads from scheduled replay. */
public final class DeferredFieldAliasReplayProbe {
 static final String A="Lalias/Activity;",H="Lalias/Holder;",CACHE="Lalias/Cache;",USE="Lalias/Use;",CLIENT="Lalias/Client;",REAL="Lalias/ActualView;",BUNDLE="Landroid/os/Bundle;";
 static ImmutableDexFile build(){
  return new ImmutableDexFile(Opcodes.getDefault(),List.of(
   clazz(A,"Landroid/app/Activity;",List.of(),List.of(field(A,"one",CACHE),field(A,"two",CACHE)),method(A,"onCreate",List.of(BUNDLE),"V",1,4,List.of(get(0,2,A,"one",CACHE),call(Opcode.INVOKE_STATIC,USE,"bind",List.of(CACHE),"V",0),get(0,2,A,"two",CACHE),call(Opcode.INVOKE_STATIC,USE,"bind",List.of(CACHE),"V",0),end()),false)),
   clazz(H,OBJ,List.of(),List.of(field(H,"view",W))),clazz(CACHE,OBJ,List.of(),List.of(field(CACHE,"cached",W))),
   clazz(USE,OBJ,List.of(),List.of(),method(USE,"bind",List.of(CACHE),"V",9,3,List.of(get(0,2,CACHE,"cached",W),make(1,CLIENT),call(Opcode.INVOKE_DIRECT,CLIENT,"<init>",List.of(),"V",1),call(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V",0,1),end()),false)),
   clazz(CLIENT,"Landroid/webkit/WebViewClient;",List.of(),List.of(),method(CLIENT,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),method(CLIENT,"onPageFinished",List.of(W,"Ljava/lang/String;"),"V",1,3,List.of(end()),false)),clazz(REAL,W,List.of(),List.of())));
 }
 static Set<String> callbackViews(CapabilityEngine.Host host){Set<String> out=new TreeSet<>();for(var f:host.facts.values())if("callback".equals(f.get("kind"))){@SuppressWarnings("unchecked")var w=(Map<String,Object>)f.get("webview");out.add(w.toString());}return out;}
 public static void run()throws Exception{run(true);}
 public static void main(String[]args)throws Exception{run(args.length>0&&args[0].equals("verify"));}
 static void run(boolean requireSamePhase)throws Exception{
  Path dex=Files.createTempFile("deferred-alias-",".dex");try{
   DexFileFactory.writeDexFile(dex.toString(),build());long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var apk=new ApkInventory();apk.activities.add("alias.Activity");var engine=new CapabilityEngine(idx,apk,deadline);var state=engine.beginActivity("alias.Activity");var h=state.host;
   V activity=V.of("host","alias.Activity","activity:alias.Activity"),one=V.of("object","alias.Holder","holder-one"),two=V.of("object","alias.Holder","holder-two"),cacheOne=V.of("object","alias.Cache","cache-one"),cacheTwo=V.of("object","alias.Cache","cache-two"),actual=V.of("object","alias.ActualView","actual-one");
   V aliasOne=engine.deferredField(h,H+"->view:"+W,one,"android.webkit.WebView"),aliasTwo=engine.deferredField(h,H+"->view:"+W,two,"android.webkit.WebView");
   engine.applyWrite(h,A+"->one:"+CACHE,activity,cacheOne);engine.applyWrite(h,A+"->two:"+CACHE,activity,cacheTwo);engine.applyWrite(h,CACHE+"->cached:"+W,cacheOne,aliasOne);engine.applyWrite(h,CACHE+"->cached:"+W,cacheTwo,aliasTwo);
   while(!state.done&&state.phase==0&&callbackViews(h).size()<2)engine.advanceActivity(state,1_000_000_000L,1);
   if(callbackViews(h).size()!=2)throw new AssertionError("Initial two consumer contexts not reached: "+callbackViews(h));
   System.out.println("BEFORE phase="+state.phase+" views="+callbackViews(h));engine.applyWrite(h,H+"->view:"+W,one,actual);
   V resolved=engine.refreshBinding(aliasOne,h,0,new HashSet<>()),unresolved=engine.refreshBinding(aliasTwo,h,0,new HashSet<>());
   if(!resolved.equals(actual)||!unresolved.equals(aliasTwo))throw new AssertionError("Deferred receiver isolation failed: "+resolved+" / "+unresolved);
   System.out.println("AFTER_WRITE phase="+state.phase+" immediate="+resolved+" views="+callbackViews(h));
   V captured=V.of("object","alias.ActualView","captured-old");
   engine.applyWrite(h,CACHE+"->captured:"+W,cacheOne,captured);
   engine.applyWrite(h,H+"->view:"+W,one,V.of("object","alias.ActualView","later-view"));
   if(!engine.refreshBinding(h.heap.get(CapabilityEngine.heapKey(CACHE+"->captured:"+W,cacheOne)),h,0,new HashSet<>()).equals(captured))throw new AssertionError("Concrete captured value was late rebound");
   if(requireSamePhase){
    while(!state.done&&state.phase==0&&callbackViews(h).stream().noneMatch(v->v.contains("actual-one")))engine.advanceActivity(state,1_000_000_000L,1);
    if(state.phase!=0||callbackViews(h).stream().noneMatch(v->v.contains("actual-one")))throw new AssertionError("Deferred consumer required whole-phase restart: phase="+state.phase+" "+callbackViews(h));
   }
   while(!state.done&&System.nanoTime()<deadline)engine.advanceActivity(state,1_000_000_000L,100);
   var report=state.completedReport;System.out.println("TERMINAL "+report);
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)report.get("facts");boolean real=false;for(var f:facts)if("callback".equals(f.get("kind"))){@SuppressWarnings("unchecked")var w=(Map<String,Object>)f.get("webview");if("actual-one".equals(w.get("id")))real=true;if(String.valueOf(w.get("id")).contains("holder-two")&&"alias.ActualView".equals(w.get("type")))throw new AssertionError("Unwritten second holder acquired first view");}
   if(!real)throw new AssertionError("Terminal callback lost concrete first view");
   System.out.println("PASS: deferred alias lookup and terminal replay preserve two receiver identities; immediate facts separately reported.");
  }finally{Files.deleteIfExists(dex);}
 }
}
