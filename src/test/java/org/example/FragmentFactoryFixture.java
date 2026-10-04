package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import static org.example.CapabilitySelfTest.*;

/** Only an installed adapter can request a Fragment from its stored constant descriptor. */
final class FragmentFactoryFixture {
 static final String F="Landroidx/fragment/app/Fragment;",AD="Lfactory/Adapter;",P="Landroidx/viewpager2/widget/ViewPager2;";
 public static void main(String[] args)throws Exception{run();}
 static ImmutableMethod fragment(String type,String label){return method(type,"onCreate",List.of("Landroid/os/Bundle;"),1,5,List.of(make(0,W),make(1,B),str(2,label),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",0,1,2),end()),false);}
 static void run()throws Exception{
  String first="Lfactory/First;",second="Lfactory/Second;",unused="Lfactory/Unused;",literal="Lfactory/LiteralOnly;";
  String descriptor="Lfactory/Descriptor;",list="Ljava/util/ArrayList;";
  var field=new ImmutableField(descriptor,"name","Ljava/lang/String;",0x11,null,Set.of(),Set.of());var ref=new ImmutableFieldReference(descriptor,"name","Ljava/lang/String;");
  var descriptorCtor=method(descriptor,"<init>",List.of("Ljava/lang/String;"),0x10001,2,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,ref),end()),false);
  var factory=AspectJFixture.returning(method(descriptor,"create",List.of("Landroid/content/Context;"),1,4,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,2,ref),new ImmutableInstruction11n(Opcode.CONST_4,1,0),invoke(Opcode.INVOKE_STATIC,F,"instantiate",List.of("Landroid/content/Context;","Ljava/lang/String;","Landroid/os/Bundle;"),F,3,0,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),F);
  var pages=new ImmutableField(AD,"pages",list,0x11,null,Set.of(),Set.of());var pagesRef=new ImmutableFieldReference(AD,"pages",list);
  var context=new ImmutableField(AD,"context","Landroid/content/Context;",0x11,null,Set.of(),Set.of());var contextRef=new ImmutableFieldReference(AD,"context","Landroid/content/Context;");
  var ctor=method(AD,"<init>",List.of(list,"Landroid/content/Context;"),0x10001,3,List.of(new ImmutableInstruction22c(Opcode.IPUT_OBJECT,1,0,pagesRef),new ImmutableInstruction22c(Opcode.IPUT_OBJECT,2,0,contextRef),end()),false);
  var callback=AspectJFixture.returning(method(AD,"createFragment",List.of("I"),1,5,List.of(new ImmutableInstruction22c(Opcode.IGET_OBJECT,0,3,pagesRef),invoke(Opcode.INVOKE_VIRTUAL,list,"get",List.of("I"),"Ljava/lang/Object;",0,4),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction21c(Opcode.CHECK_CAST,0,new ImmutableTypeReference(descriptor)),new ImmutableInstruction22c(Opcode.IGET_OBJECT,1,3,contextRef),invoke(Opcode.INVOKE_VIRTUAL,descriptor,"create",List.of("Landroid/content/Context;"),F,0,1),new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false),F);
  var instructions=new ArrayList<org.jf.dexlib2.iface.instruction.Instruction>();
  instructions.add(make(0,P));
  for(String type:List.of(first,second,unused,"dynamic")){
   if(!type.equals("dynamic")){
    instructions.add(new ImmutableInstruction21c(Opcode.CONST_CLASS,1,new ImmutableTypeReference(type)));
    instructions.add(invoke(Opcode.INVOKE_VIRTUAL,"Ljava/lang/Class;","getName",List.of(),"Ljava/lang/String;",1));instructions.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,1));
   }
   instructions.add(make(3,descriptor));instructions.add(invoke(Opcode.INVOKE_DIRECT,descriptor,"<init>",List.of("Ljava/lang/String;"),"V",3,type.equals("dynamic")?5:1));
   instructions.add(make(2,list));instructions.add(invoke(Opcode.INVOKE_DIRECT,list,"<init>",List.of(),"V",2));instructions.add(invoke(Opcode.INVOKE_VIRTUAL,list,"add",List.of("Ljava/lang/Object;"),"Z",2,3));
   instructions.add(make(3,AD));instructions.add(invoke(Opcode.INVOKE_DIRECT,AD,"<init>",List.of(list,"Landroid/content/Context;"),"V",3,2,4));
   if(!type.equals(unused))instructions.add(invoke(Opcode.INVOKE_VIRTUAL,P,"setAdapter",List.of("Landroidx/recyclerview/widget/RecyclerView$Adapter;"),"V",0,3));
  }
  instructions.add(new ImmutableInstruction21c(Opcode.CONST_CLASS,1,new ImmutableTypeReference(literal)));instructions.add(end());
  var entry=method(A,"onCreate",List.of("Ljava/lang/String;"),1,6,instructions,false);
  Path file=Files.createTempFile("fragment-factory-",".dex");try{
   DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),new ImmutableClassDef(AD,1,"Landroidx/viewpager2/adapter/FragmentStateAdapter;",List.of(),null,Set.of(),List.of(pages,context),List.of(ctor,callback)),new ImmutableClassDef(descriptor,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(field),List.of(descriptorCtor,factory)),clazz(first,F,method(first,"<init>",List.of(),0x10001,1,List.of(end()),false),fragment(first,"first")),clazz(second,F,method(second,"<init>",List.of(),0x10001,1,List.of(end()),false),fragment(second,"second")),clazz(unused,F,fragment(unused,"unused")),clazz(literal,F,fragment(literal,"literal")),clazz(B,"Ljava/lang/Object;",method(B,"expose",List.of(),1,1,List.of(end()),true)))));
   long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(file,deadline);var engine=new CapabilityEngine(idx,new ApkInventory(),deadline);engine.analyzeActivity("test.AppActivity");
   check(!engine.activities.isEmpty(),"Actual adapter installation never reached Fragment lifecycle");
   @SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)engine.activities.get(0).get("facts");var bridges=facts.stream().filter(f->"bridge".equals(f.get("kind"))).toList();Set<Object> names=new HashSet<>();for(var fact:bridges)names.add(fact.get("registration_name"));
   check(names.equals(Set.of("first","second")),"Installed constant factory fragments missing or inactive fragments seeded: "+names);
   check(bridges.size()==2,"Factory duplicated or conflated registrations: "+bridges);
   check(!bridges.get(0).get("webview").equals(bridges.get(1).get("webview")),"Different adapter instances lost WebView isolation");
   check(engine.activities.get(0).toString().contains("fragment_factory_dynamic_name"),"Unresolved dynamic factory name lacks diagnostic");
   var host=engine.new Host("factory.BundleHost");var job=new CapabilityEngine.Job(entry,List.of(),List.of(),true);
   var bundle=DexFlow.V.of("object","android.os.Bundle","supplied_arguments");
   String factoryId=F+"->instantiate(Landroid/content/Context;Ljava/lang/String;Landroid/os/Bundle;)"+F;
   var created=engine.eval(DexFlow.expr("return_fragment_factory:bundle", "androidx.fragment.app.Fragment",factoryId,List.of(DexFlow.V.of("host","factory.BundleHost","context"),DexFlow.V.literal("java.lang.String","factory.First"),bundle)),job,host,0,new HashSet<>());
   var arguments=engine.eval(DexFlow.expr("return","android.os.Bundle",F+"->getArguments()Landroid/os/Bundle;",List.of(created)),job,host,0,new HashSet<>());
   check(arguments.equals(bundle),"Factory getArguments lost the actual supplied Bundle");
  }finally{Files.deleteIfExists(file);}
  System.out.println("FragmentFactoryFixture PASS: constant name, actual adapter install, lifecycle, uninstalled/literal negatives, per-object WebView isolation, dynamic name diagnostic.");
 }
}
