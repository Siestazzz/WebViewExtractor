package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Lifecycle View arguments and getView/getChildAt must name the same Fragment allocation. */
final class FragmentLayoutFixture {
 static void run()throws Exception {
  String f="Ltest/BoundFragment;",base="Landroidx/fragment/app/Fragment;";
  int layout=0x7f010001,id=0x7f020001;
  var create=AspectJFixture.returning(method(f,"onCreateView",List.of("Landroid/view/LayoutInflater;","Landroid/view/ViewGroup;","Landroid/os/Bundle;"),1,6,List.of(
   new ImmutableInstruction31i(Opcode.CONST,0,layout),new ImmutableInstruction11n(Opcode.CONST_4,1,0),
   invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/LayoutInflater;","inflate",List.of("I","Landroid/view/ViewGroup;","Z"),"Landroid/view/View;",3,0,4,1),
   new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),"Landroid/view/View;");
  var viewed=method(f,"onViewCreated",List.of("Landroid/view/View;","Landroid/os/Bundle;"),1,6,List.of(
   new ImmutableInstruction31i(Opcode.CONST,0,id),invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/View;","findViewById",List.of("I"),"Landroid/view/View;",4,0),
   new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference(W)),
   make(1,B),str(2,"by_id"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var attached=method(f,"onActivityCreated",List.of("Landroid/os/Bundle;"),1,5,List.of(
   invoke(Opcode.INVOKE_VIRTUAL,base,"getView",List.of(),"Landroid/view/View;",3),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),
   new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference("Landroid/view/ViewGroup;")),new ImmutableInstruction11n(Opcode.CONST_4,1,0),
   invoke(Opcode.INVOKE_VIRTUAL,"Landroid/view/ViewGroup;","getChildAt",List.of("I"),"Landroid/view/View;",0,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),
   new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference(W)),make(1,B),str(2,"by_child"),
   invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);
  var entries=new ArrayList<org.jf.dexlib2.iface.instruction.Instruction>();
  for(int i=0;i<2;i++)entries.addAll(List.of(make(0,f),invoke(Opcode.INVOKE_DIRECT,f,"<init>",List.of(),"V",0),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentActivity;","getSupportFragmentManager",List.of(),"Landroidx/fragment/app/FragmentManager;",3),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentManager;","beginTransaction",List.of(),"Landroidx/fragment/app/FragmentTransaction;",1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1),str(2,"child"+i),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentTransaction;","add",List.of("Landroidx/fragment/app/Fragment;","Ljava/lang/String;"),"Landroidx/fragment/app/FragmentTransaction;",1,0,2),invoke(Opcode.INVOKE_VIRTUAL,"Landroidx/fragment/app/FragmentTransaction;","commit",List.of(),"I",1)));
  entries.add(end());var entry=method(A,"onCreate",List.of(),1,4,entries,false);
  Path file=Files.createTempFile("fragment-root-",".dex");
  try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroidx/fragment/app/FragmentActivity;",entry),clazz(f,base,method(f,"<init>",List.of(),1,1,List.of(end()),false),create,viewed,attached),clazz(B,"Ljava/lang/Object;",method(B,"hello",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+20_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);
   var apk=new ApkInventory();apk.targetSdk=30;
   var root=new ApkInventory.LayoutNode("android.widget.FrameLayout");var child=new ApkInventory.LayoutNode("android.webkit.WebView");child.id=id;root.children.add(child);
   apk.layoutRoots.put("res/layout/fragment.xml",List.of(root));apk.layoutResources.put(layout,Set.of("res/layout/fragment.xml"));
   var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("test.AppActivity");
   check(engine.activities.size()==1,"Fragment host missing");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");
   Set<Object> ids=new HashSet<>();Set<Object> names=new HashSet<>();Map<Object,Set<Object>> byObject=new HashMap<>();
   for(var fact:facts)if("bridge".equals(fact.get("kind"))){ids.add(((Map<?,?>)fact.get("webview")).get("id"));names.add(fact.get("registration_name"));byObject.computeIfAbsent(((Map<?,?>)fact.get("webview")).get("id"),k->new HashSet<>()).add(fact.get("registration_name"));}
   check(names.equals(Set.of("by_id","by_child")),"Fragment lifecycle access paths lost registrations: "+names);
   check(ids.size()==2&&byObject.values().stream().allMatch(n->n.equals(Set.of("by_id","by_child"))),"Fragment instances merged or getView/getChildAt and lifecycle lookup split: "+byObject);
  }finally{Files.deleteIfExists(file);}
 }
}
