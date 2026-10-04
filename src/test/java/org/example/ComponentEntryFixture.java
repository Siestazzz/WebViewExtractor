package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Components have framework entries; helper methods and instance origins need real calls. */
final class ComponentEntryFixture {
 public static void main(String[] args)throws Exception{run(args.length>0);}
 static void run()throws Exception{run(false);}
 static ImmutableMethod inject(String owner,String name,String registration,int flags,int receiver){
  return method(owner,name,List.of(),flags,3,List.of(make(0,B),str(1,registration),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",receiver,0,1),end()),false);
 }
 static void run(boolean fieldOnly)throws Exception {
  String view="Lentry/ChildView;",parent="Lentry/ParentView;",fragment="Lentry/ChildFragment;",parentFragment="Lentry/ParentFragment;",callback="Lentry/Callback;",helper="Lentry/Helper;",owner="Lentry/Owner;";
  var classes=new ArrayList<ImmutableClassDef>();
  var init=method(view,"<init>",List.of(),1,3,List.of(invoke(Opcode.INVOKE_DIRECT,view,"initSetup",List.of(),"V",2),make(0,callback),invoke(Opcode.INVOKE_DIRECT,callback,"<init>",List.of(W),"V",0,2),make(1,"Landroid/os/Handler;"),invoke(Opcode.INVOKE_VIRTUAL,"Landroid/os/Handler;","post",List.of("Ljava/lang/Runnable;"),"Z",1,0),end()),false);
  var parentAttached=inject(parent,"onAttachedToWindow","inherited-view-callback",4,2);
  classes.add(clazz(parent,W,parentAttached));
  var wrongOverload=method(view,"onAttachedToWindow",List.of("Ljava/lang/String;"),1,4,List.of(make(0,B),str(1,"wrong-view-overload"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),end()),false);
  classes.add(clazz(view,parent,init,inject(view,"initSetup","actual-constructor-init",2,2),inject(view,"unused","unused-view-helper",1,2),wrongOverload));
  var callbackField=new ImmutableField(callback,"view",W,0x11,null,Set.of(),Set.of());
  var callbackCtor=method(callback,"<init>",List.of(W),1,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,new ImmutableFieldReference(callback,"view",W)),end()),false);
  var run=method(callback,"run",List.of(),1,2,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,1,new ImmutableFieldReference(callback,"view",W)),invoke(Opcode.INVOKE_STATIC,helper,"registered",List.of(W),"V",0),end()),false);
  classes.add(new ImmutableClassDef(callback,1,"Ljava/lang/Object;",List.of("Ljava/lang/Runnable;"),null,Set.of(),List.of(callbackField),List.of(callbackCtor,run)));
  var registered=method(helper,"registered",List.of(W),9,3,List.of(make(0,B),str(1,"actual-registration"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",2,0,1),end()),false);
  classes.add(clazz(helper,"Ljava/lang/Object;",registered));
  var fragmentField=new ImmutableField(parentFragment,"view",W,1,null,Set.of(),Set.of());
  var viewed=method(parentFragment,"onViewCreated",List.of("Landroid/view/View;","Landroid/os/Bundle;"),1,4,List.of(invoke(Opcode.INVOKE_DIRECT,parentFragment,"initView",List.of("Landroid/view/View;"),"V",1,2),invoke(Opcode.INVOKE_DIRECT,parentFragment,"initByData",List.of(),"V",1),end()),false);
  var initView=method(parentFragment,"initView",List.of("Landroid/view/View;"),2,3,List.of(make(0,W),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(parentFragment,"view",W)),end()),false);
  var initByData=method(parentFragment,"initByData",List.of(),2,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,3,new ImmutableFieldReference(parentFragment,"view",W)),make(1,B),str(2,"inherited-fragment-init"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var unusedFragment=method(parentFragment,"unused",List.of(),1,4,List.of(make(0,W),make(1,B),str(2,"unused-fragment-helper"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  classes.add(new ImmutableClassDef(parentFragment,1,"Landroidx/fragment/app/Fragment;",List.of(),null,Set.of(),List.of(fragmentField),List.of(viewed,initView,initByData,unusedFragment,new ImmutableMethod(parentFragment,"onCreateView",List.of(),"V",1,Set.of(),Set.of(),unusedFragment.getImplementation()))));
  classes.add(clazz(fragment,parentFragment,method(fragment,"<init>",List.of(),1,1,List.of(end()),false)));
  var ownerField=new ImmutableField(owner,"view",W,1,null,Set.of(),Set.of());
  var ownerCtor=method(owner,"<init>",List.of(),1,2,List.of(make(0,W),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,0,1,new ImmutableFieldReference(owner,"view",W)),end()),false);
  var sharedField=new ImmutableField(owner,"shared",W,9,null,Set.of(),Set.of());
  var clinit=method(owner,"<clinit>",List.of(),8,1,List.of(make(0,W),new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(owner,"shared",W)),end()),false);
  classes.add(new ImmutableClassDef(owner,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(ownerField,sharedField),List.of(ownerCtor,clinit)));
  var entry=method(A,"onCreate",List.of(),1,4,List.of(make(0,view),invoke(Opcode.INVOKE_DIRECT,view,"<init>",List.of(),"V",0),make(1,fragment),invoke(Opcode.INVOKE_DIRECT,fragment,"<init>",List.of(),"V",1),make(2,owner),invoke(Opcode.INVOKE_DIRECT,owner,"<init>",List.of(),"V",2),end()),false);
  classes.add(clazz(A,"Landroid/app/Activity;",entry));classes.add(clazz(B,"Ljava/lang/Object;",method(B,"expose",List.of(),1,1,List.of(end()),true)));
  Path file=Files.createTempFile("component-entries-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);
   if(!fieldOnly){
    engine.analyzeActivity("test.AppActivity");
    @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
    Set<Object> names=new HashSet<>();for(var fact:facts)if("bridge".equals(fact.get("kind")))names.add(fact.get("registration_name"));
    check(!names.contains("unused-view-helper")&&!names.contains("unused-fragment-helper")&&!names.contains("wrong-view-overload"),"Component construction executed uncalled helpers: "+names);
    check(names.containsAll(Set.of("actual-constructor-init","inherited-view-callback","inherited-fragment-init","actual-registration")),"Actual initialization/lifecycle/registration lost: "+names);
   }
   var groupCallback=method(view,"onViewAdded",List.of("Landroid/view/View;"),1,2,List.of(end()),false);
   idx.classes.put("entry.OrdinaryView",clazz("Lentry/OrdinaryView;","Landroid/view/View;"));
   check(!idx.componentEntry("entry.OrdinaryView",groupCallback),"ViewGroup-only callback recognized on an ordinary View");
   idx.classes.put("entry.Container",clazz("Lentry/Container;","Landroid/view/ViewGroup;"));
   check(idx.componentEntry("entry.Container",groupCallback),"ViewGroup callback rejected on its actual framework family");
   var fakeFragment=method(fragment,"onCreateView",List.of(),1,1,List.of(end()),false);
   check(!idx.componentEntry("entry.ChildFragment",fakeFragment),"Invalid Fragment lifecycle overload recognized");
   var host=engine.new Host("test.FieldHost");var job=new CapabilityEngine.Job(entry,List.of(),List.of(),false);
   var unknown=DexFlow.V.of("field_object","entry.Owner","unknown-owner");
   var result=engine.eval(DexFlow.expr("field","android.webkit.WebView",owner+"->view:"+W,List.of(unknown)),job,host,0,new HashSet<>());
   check(result.kind().equals("field_object")&&host.heap.isEmpty(),"Unknown field receiver acquired a constructor allocation: "+result);
   var allocation=engine.flow.summary(entry).calls().stream().filter(c->c.method().equals(owner+"-><init>()V")).findFirst().orElseThrow().args().get(0);
   var actual=engine.eval(allocation,job,host,0,new HashSet<>());
   var stored=engine.eval(DexFlow.expr("field","android.webkit.WebView",owner+"->view:"+W,List.of(actual)),job,host,0,new HashSet<>());
   check(stored.kind().equals("object")&&stored.type().equals("android.webkit.WebView"),"Actual instance constructor field initialization lost");
   var shared=engine.eval(DexFlow.expr("field","android.webkit.WebView",owner+"->shared:"+W,List.of(DexFlow.V.of("static","entry.Owner",owner))),job,host,0,new HashSet<>());
   check(shared.kind().equals("object")&&shared.type().equals("android.webkit.WebView"),"Real static field initialization lost");
  }finally{Files.deleteIfExists(file);}
  System.out.println("ComponentEntryFixture PASS: uncalled helpers/overloads excluded; inherited lifecycle, init and registration retained; unknown instance field has no fabricated origin.");
 }
}
