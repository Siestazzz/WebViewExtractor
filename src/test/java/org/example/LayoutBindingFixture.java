package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import pxb.android.axml.*;
import static org.example.CapabilitySelfTest.*;
import static org.example.DexFlow.*;

/** Selected layouts, not globally reused resource IDs, determine concrete receiver types. */
final class LayoutBindingFixture {
 static void run()throws Exception {
  String child="Ltest/XmlWebView;",other="Ltest/OtherWebView;";
  int layout=0x7f010000,id=0x7f020000;
  var entry=method(A,"onCreate",List.of(),1,4,List.of(
   new ImmutableInstruction31i(Opcode.CONST,0,layout),invoke(Opcode.INVOKE_VIRTUAL,"Landroid/app/Activity;","setContentView",List.of("I"),"V",3,0),
   new ImmutableInstruction31i(Opcode.CONST,0,id),invoke(Opcode.INVOKE_VIRTUAL,"Landroid/app/Activity;","findViewById",List.of("I"),"Landroid/view/View;",3,0),
   new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference(W)),
   str(2,"https://fixture.invalid/"),invoke(Opcode.INVOKE_VIRTUAL,W,"loadUrl",List.of("Ljava/lang/String;"),"V",0,2),
   make(1,B),str(2,"xml"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  String base="Ltest/BaseActivity;",wrapped="Ltest/WrappedActivity;";
  var wrap=method(base,"setContentView",List.of("I"),1,5,List.of(
   new ImmutableInstruction31i(Opcode.CONST,0,layout+3),invoke(Opcode.INVOKE_SUPER,"Landroid/app/Activity;","setContentView",List.of("I"),"V",3,0),
   new ImmutableInstruction31i(Opcode.CONST,0,id+3),invoke(Opcode.INVOKE_VIRTUAL,"Landroid/app/Activity;","findViewById",List.of("I"),"Landroid/view/View;",3,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),
   make(0,"Landroid/view/LayoutInflater;"),new ImmutableInstruction11n(Opcode.CONST_4,2,0),
   invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/LayoutInflater;","inflate",List.of("I","Landroid/view/ViewGroup;","Z"),"Landroid/view/View;",0,4,1,2),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,2),
   invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/ViewGroup;","addView",List.of("Landroid/view/View;"),"V",1,2),end()),false);
  var wrappedBody=new ArrayList<org.jf.dexlib2.iface.instruction.Instruction>();entry.getImplementation().getInstructions().forEach(wrappedBody::add);
  wrappedBody.set(1,invoke(Opcode.INVOKE_VIRTUAL,base,"setContentView",List.of("I"),"V",3,0));
  var wrappedEntry=method(wrapped,"onCreate",List.of(),1,4,wrappedBody,false);
  String outer="Ltest/OuterView;",inner="Ltest/InnerView;",nestedHost="Ltest/NestedActivity;";
  var outerField=new ImmutableField(outer,"child",inner,1,null,Set.of(),Set.of());
  var innerField=new ImmutableField(inner,"web",W,1,null,Set.of(),Set.of());
  var outerCtor=xmlConstructor(outer,"child",inner,layout+5,id+5);
  var innerCtor=xmlConstructor(inner,"web",W,layout+6,id+6);
  var innerGetter=AspectJFixture.returning(method(inner,"getWeb",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(inner,"web",W)),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),W);
  var outerGetter=AspectJFixture.returning(method(outer,"getWeb",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(outer,"child",inner)),invoke(Opcode.INVOKE_VIRTUAL,inner,"getWeb",List.of(),W,0),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),W);
  var nestedBody=new ArrayList<org.jf.dexlib2.iface.instruction.Instruction>();entry.getImplementation().getInstructions().forEach(nestedBody::add);
  nestedBody.set(0,new ImmutableInstruction31i(Opcode.CONST,0,layout+4));nestedBody.set(2,new ImmutableInstruction31i(Opcode.CONST,0,id+4));
  nestedBody.set(5,new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference(outer)));
  nestedBody.add(6,invoke(Opcode.INVOKE_VIRTUAL,outer,"getWeb",List.of(),W,0));nestedBody.add(7,new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0));
  var nestedEntry=method(nestedHost,"onCreate",List.of(),1,4,nestedBody,false);
  Path file=Files.createTempFile("wv-layout-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(nestedHost,"Landroid/app/Activity;",nestedEntry),
   new ImmutableClassDef(outer,1,"Landroid/widget/FrameLayout;",List.of(),null,Set.of(),List.of(outerField),List.of(outerCtor,outerGetter)),
   new ImmutableClassDef(inner,1,"Landroid/widget/FrameLayout;",List.of(),null,Set.of(),List.of(innerField),List.of(innerCtor,innerGetter)),clazz(base,"Landroid/app/Activity;",wrap),clazz(wrapped,base,wrappedEntry),clazz("Ltest/MiddleWebView;",W,method("Ltest/MiddleWebView;","loadUrl",List.of("Ljava/lang/String;"),1,4,List.of(make(0,B),str(1,"super_override"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),invoke(Opcode.INVOKE_SUPER,W,"loadUrl",List.of("Ljava/lang/String;"),"V",2,3),end()),false)),clazz(child,"Ltest/MiddleWebView;",method(child,"loadUrl",List.of("Ljava/lang/String;"),1,4,List.of(
    make(0,B),str(1,"load_override"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),
    invoke(Opcode.INVOKE_SUPER,W,"loadUrl",List.of("Ljava/lang/String;"),"V",2,3),end()),false),method(child,"unusedHelper",List.of(),1,3,List.of(make(0,B),str(1,"must_not_execute"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),end()),false)),clazz(other,W),clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   var apk=new ApkInventory();apk.targetSdk=30;
   var writer=new AxmlWriter();var xml=writer.child(null,"test.XmlWebView");xml.attr("http://schemas.android.com/apk/res/android","id",0x010100d0,1,id);xml.end();writer.end();
   new AxmlReader(writer.toByteArray()).accept(apk.visitor("res/layout/chosen.xml"));
   var wrong=new ApkInventory.LayoutNode("test.OtherWebView");wrong.id=id;
   apk.layoutRoots.put("res/layout/other.xml",List.of(wrong));
   // Exercise resource-table encoding/decoding including an alias, not a global R-field guess.
   var pkg=new pxb.android.arsc.Pkg(0x7f,"test");var type=pkg.getType(1,"layout",2);
   type.getSpec(0).updateName("chosen");type.getSpec(1).updateName("alias");
   var config=new pxb.android.arsc.Config(new byte[28],2);
   var e=new pxb.android.arsc.ResEntry(0,type.getSpec(0));e.value=new pxb.android.arsc.Value(3,0,"res/layout/chosen.xml");config.resources.put(0,e);
   var alias=new pxb.android.arsc.ResEntry(0,type.getSpec(1));alias.value=new pxb.android.arsc.Value(1,layout,null);config.resources.put(1,alias);type.addConfig(config);
   byte[] table=new pxb.android.arsc.ArscWriter(List.of(pkg)).toByteArray();
   resourceFormats(table,layout);
   apk.resources(table);
   check(apk.layouts(layout+1).equals(Set.of("res/layout/chosen.xml")),"Resource layout alias lost");
   var baseRoot=new ApkInventory.LayoutNode("android.widget.FrameLayout");baseRoot.id=id+3;
   apk.layoutRoots.put("res/layout/base.xml",List.of(baseRoot));apk.layoutResources.put(layout+3,Set.of("res/layout/base.xml"));
   var outerNode=new ApkInventory.LayoutNode("test.OuterView");outerNode.id=id+4;
   var innerNode=new ApkInventory.LayoutNode("test.InnerView");innerNode.id=id+5;
   var innerMerge=new ApkInventory.LayoutNode("merge");var innerWeb=new ApkInventory.LayoutNode("test.XmlWebView");innerWeb.id=id+6;innerMerge.children.add(innerWeb);
   for(var binding:Map.of(layout+4,outerNode,layout+5,innerNode,layout+6,innerMerge).entrySet()){
    String path="res/layout/nested"+binding.getKey()+".xml";apk.layoutResources.put(binding.getKey(),Set.of(path));apk.layoutRoots.put(path,List.of(binding.getValue()));
   }
   var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"XML host missing");
   @SuppressWarnings("unchecked")var facts=(Collection<Map<String,Object>>)engine.activities.get(0).get("facts");
   var bridge=facts.stream().filter(f->"bridge".equals(f.get("kind"))).findFirst().orElseThrow();
   check(bridge.get("webview").toString().contains("test.XmlWebView"),"Superclass cast erased XML type: "+bridge);
   check(facts.stream().anyMatch(f->"load_override".equals(f.get("registration_name"))),"Actual API override registration was swallowed by emit");
   check(facts.stream().anyMatch(f->"super_override".equals(f.get("registration_name"))),"Ancestor method_id bypassed immediate superclass override");
   check(facts.stream().noneMatch(f->"must_not_execute".equals(f.get("registration_name"))),"XML inflation executed an uncalled helper");
   check(!facts.toString().contains("OtherWebView"),"Same resource ID from unselected layout contaminated receiver");
   engine.analyzeActivity("test.WrappedActivity");
   @SuppressWarnings("unchecked")var wrappedFacts=(Collection<Map<String,Object>>)engine.activities.get(1).get("facts");
   check(wrappedFacts.stream().anyMatch(f->"bridge".equals(f.get("kind"))),"Wrapped Activity has no bridge facts");
   check(wrappedFacts.stream().filter(f->"bridge".equals(f.get("kind"))).allMatch(f->f.get("webview").toString().contains("test.XmlWebView")),"Base Activity detached inflate/addView lost bound root: "+wrappedFacts);
   engine.analyzeActivity("test.NestedActivity");
   @SuppressWarnings("unchecked")var nestedFacts=(Collection<Map<String,Object>>)engine.activities.get(2).get("facts");
   check(nestedFacts.stream().filter(f->"bridge".equals(f.get("kind"))).anyMatch(f->f.get("webview").toString().contains("test.XmlWebView")),"Nested XML constructors/field/getter lost concrete WebView: "+nestedFacts);
   var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(entry,List.of(),List.of(),false);
   V first=V.of("view","android.view.ViewGroup","first"),second=V.of("view","android.view.ViewGroup","second");
   engine.mountLayout(first,V.literal("number",String.valueOf(layout)),host);
   V found=engine.lookupView(first,V.literal("number",String.valueOf(id)),"android.view.View",job,host);
   V absent=engine.lookupView(second,V.literal("number",String.valueOf(id)),"android.view.View",job,host);
   check(found.type().equals("test.XmlWebView")&&!absent.type().equals("test.XmlWebView"),"Different inflation roots shared layouts");
   V early=V.of("view","android.webkit.WebView",found.id());
   check(engine.refreshBinding(early,host,0,new HashSet<>()).type().equals("test.XmlWebView"),"Known XML object stayed at its earlier parent type");
   var lateHost=engine.new Host("test.AppActivity");
   var consumer=new CapabilityEngine.Job(entry,List.of(V.of("host","test.AppActivity","consumer")),List.of("consumer"),false);
   String consumerKey=CapabilityIndex.key(entry)+"|"+consumer.args();lateHost.visited.add(consumerKey);
   engine.observeXmlConsumer(lateHost,consumer,List.of(early));
   check(!engine.replayXmlConsumers(lateHost),"Unresolved view caused an unbounded replay");
   lateHost.xmlBindings.put("different-root",Set.of(Map.of("concrete_type","test.XmlWebView")));
   check(!engine.replayXmlConsumers(lateHost),"Different XML object invalidated consumer");
   lateHost.xmlBindings.put(early.id(),Set.of(Map.of("concrete_type","test.XmlWebView")));
   check(engine.replayXmlConsumers(lateHost)&&!lateHost.visited.contains(consumerKey),"Late concrete XML type did not invalidate actual consuming context");
   lateHost.queue.clear();lateHost.pending.clear();lateHost.visited.add(consumerKey);
   check(!engine.replayXmlConsumers(lateHost),"Unchanged concrete binding replayed forever");
   V one=V.of("object","test.Owner","owner-one"),two=V.of("object","test.Owner","owner-two");
   String field="Ltest/Owner;->child:Ltest/Inner;",webField="Ltest/Inner;->web:Landroid/webkit/WebView;";
   V delayedChild=engine.deferredField(host,field,one,"test.Inner");
   V delayedWeb=engine.deferredField(host,webField,delayedChild,"android.webkit.WebView");
   V actualChild=V.of("object","test.Inner","actual-inner");
   host.heap.put(CapabilityEngine.heapKey(field,two),actualChild);host.heap.put(CapabilityEngine.heapKey(webField,actualChild),found);
   check(engine.refreshBinding(delayedWeb,host,0,new HashSet<>()).equals(delayedWeb),"Deferred field used another object's write");
   host.heap.put(CapabilityEngine.heapKey(field,one),actualChild);
   check(engine.refreshBinding(delayedWeb,host,0,new HashSet<>()).equals(found),"Delayed two-field capture was not rebound after actual writes");
   host.heap.put(CapabilityEngine.heapKey(field,one),delayedChild);
   check(engine.refreshBinding(delayedWeb,host,0,new HashSet<>()).equals(delayedWeb),"Deferred self-cycle failed to remain unresolved");
   var merge=new ApkInventory.LayoutNode("merge");var nested=new ApkInventory.LayoutNode("test.OtherWebView");nested.id=id+1;merge.children.add(nested);
   apk.layoutRoots.put("res/layout/nested.xml",List.of(merge));apk.layoutResources.put(layout+2,Set.of("res/layout/nested.xml"));
   var inflater=V.of("object","android.view.LayoutInflater","inflater");
   engine.inflateLayout("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;",List.of(inflater,V.literal("number",String.valueOf(layout+2)),first,V.literal("number","1")),job,host);
   check(engine.lookupView(first,V.literal("number",String.valueOf(id+1)),"android.view.View",job,host).type().equals("test.OtherWebView"),"Attached merge layout lost");
   var invalid=engine.inflateLayout("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;",List.of(inflater,V.literal("number",String.valueOf(layout+2)),second,V.literal("number","0")),job,host);
   check(invalid.kind().equals("unknown")&&host.gaps.stream().anyMatch(g->g.startsWith("invalid_merge_inflation")),"Invalid detached merge silently accepted");
   merge.type="android.widget.FrameLayout"; // A detached merge is invalid; ordinary roots can detach.
   var detached=engine.inflateLayout("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;",List.of(inflater,V.literal("number",String.valueOf(layout+2)),second,V.literal("number","0")),job,host);
   check(!engine.lookupView(second,V.literal("number",String.valueOf(id+1)),"android.view.View",job,host).type().equals("test.OtherWebView"),"attach=false polluted parent");
   var another=engine.inflateLayout("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;",List.of(inflater,V.literal("number",String.valueOf(layout+2)),second,V.literal("number","0")),job,host,"different_site");
   check(!detached.id().equals(another.id()),"Different static inflation sites merged");
   var dynamic=engine.inflateLayout("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;",List.of(inflater,V.literal("number",String.valueOf(layout+2)),second,V.of("unknown","boolean","dynamic_attach")),job,host,"dynamic_site");
   check(host.gaps.contains("dynamic_inflate_attachment")&&alternatives(dynamic).stream().anyMatch(v->v.id().equals(second.id()))&&alternatives(dynamic).stream().anyMatch(v->!v.id().equals(second.id())),"Dynamic attachment must preserve conditional parent/root alternatives without null unboxing");
   check(engine.lookupView(detached,V.literal("number",String.valueOf(id+1)),"android.view.View",job,host).type().equals("test.OtherWebView"),"Detached layout root lost");
  }finally{Files.deleteIfExists(file);}
 }
 static ImmutableMethod xmlConstructor(String owner,String field,String child,int layout,int view){
  return method(owner,"<init>",List.of("Landroid/content/Context;","Landroid/util/AttributeSet;"),1,6,List.of(
   make(0,"Landroid/view/LayoutInflater;"),new ImmutableInstruction31i(Opcode.CONST,1,layout),new ImmutableInstruction11n(Opcode.CONST_4,2,1),
   invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/LayoutInflater;","inflate",List.of("I","Landroid/view/ViewGroup;","Z"),"Landroid/view/View;",0,1,3,2),
   new ImmutableInstruction31i(Opcode.CONST,1,view),invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/View;","findViewById",List.of("I"),"Landroid/view/View;",3,1),
   new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference(child)),
   new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,3,new ImmutableFieldReference(owner,field,child)),end()),false);
 }
 static void resourceFormats(byte[] original,int id)throws Exception {
  var reader=new LayoutResources(original);int typeChunk=-1;
  for(int p=reader.u16(2);p<original.length;p=reader.end(p,original.length))if(reader.u16(p)==0x200)
   for(int c=p+reader.u16(p+2),limit=reader.end(p,original.length);c<limit;c=reader.end(c,limit))if(reader.u16(c)==0x201)typeChunk=c;
  check(typeChunk>=0,"Fixture resource type missing");int p=typeChunk,header=reader.u16(p+2),start=reader.i32(p+16);
  for(int flags:List.of(1,2)){
   byte[] data=original.clone();var b=java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN);b.put(p+9,(byte)flags);
   for(int i=0;i<2;i++){int off=reader.i32(p+header+i*4);if(flags==1){b.putShort(p+header+i*4,(short)i);b.putShort(p+header+i*4+2,(short)(off/4));}else b.putShort(p+header+i*2,(short)(off/4));}
   var apk=new ApkInventory();apk.resources(data);check(apk.layouts(id+1).equals(Set.of("res/layout/chosen.xml")),"Sparse/offset16 resource table failed: "+flags);
  }
  byte[] compact=original.clone();var b=java.nio.ByteBuffer.wrap(compact).order(java.nio.ByteOrder.LITTLE_ENDIAN);
  for(int i=0;i<2;i++){int e=p+start+reader.i32(p+header+i*4),v=e+reader.u16(e);b.putShort(e,(short)reader.i32(e+4));b.putShort(e+2,(short)(8|(reader.u8(v+3)<<8)));b.putInt(e+4,reader.i32(v+4));}
  var apk=new ApkInventory();apk.resources(compact);check(apk.layouts(id+1).equals(Set.of("res/layout/chosen.xml")),"Compact resource entries failed");
  byte[] bad=original.clone();java.nio.ByteBuffer.wrap(bad).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(4,Integer.MAX_VALUE);
  boolean rejected=false;try{new ApkInventory().resources(bad);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Malformed resource size accepted");
 }

}
