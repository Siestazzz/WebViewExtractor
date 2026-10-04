package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;

/** Standalone source-guided probe: compile only this file against a frozen engine jar. */
public final class ServiceConstructorArrayProbe {
 static final String A="Lprobe/Activity;",REG="Lprobe/Registry;",META="Lprobe/Meta;",CREATOR="Lprobe/Creator;",ICREATOR="Lprobe/CreatorContract;",API="Lprobe/Service;",IMPL="Lprobe/Implementation;",PRODUCT="Lprobe/Product;",P="Lprobe/Player;",B="Lprobe/Bridge;",C="Lprobe/Client;",MAP="Ljava/util/HashMap;",W="Landroid/webkit/WebView;",S="Landroid/webkit/WebSettings;",CLS="Ljava/lang/Class;",OBJ="Ljava/lang/Object;",CTOR="Ljava/lang/reflect/Constructor;";
 static Instruction call(Opcode op,String owner,String name,List<String> params,String ret,int...regs){int[] r=Arrays.copyOf(regs,5);return new ImmutableInstruction35c(op,regs.length,r[0],r[1],r[2],r[3],r[4],new ImmutableMethodReference(owner,name,params,ret));}
 static Instruction make(int r,String t){return new ImmutableInstruction21c(Opcode.NEW_INSTANCE,r,new ImmutableTypeReference(t));}
 static Instruction klass(int r,String t){return new ImmutableInstruction21c(Opcode.CONST_CLASS,r,new ImmutableTypeReference(t));}
 static Instruction str(int r,String t){return new ImmutableInstruction21c(Opcode.CONST_STRING,r,new ImmutableStringReference(t));}
 static Instruction result(int r){return new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,r);}
 static Instruction zero(int r){return new ImmutableInstruction11n(Opcode.CONST_4,r,0);}
 static Instruction end(){return new ImmutableInstruction10x(Opcode.RETURN_VOID);}
 static Instruction get(int r,int obj,String owner,String name,String t){return new ImmutableInstruction22c(Opcode.IGET_OBJECT,r,obj,new ImmutableFieldReference(owner,name,t));}
 static Instruction put(int r,int obj,String owner,String name,String t){return new ImmutableInstruction22c(Opcode.IPUT_OBJECT,r,obj,new ImmutableFieldReference(owner,name,t));}
 static ImmutableField field(String owner,String name,String t){return new ImmutableField(owner,name,t,1,null,Set.of(),Set.of());}
 static ImmutableMethod method(String owner,String name,List<String> params,String ret,int flags,int regs,List<Instruction> code,boolean anno){return new ImmutableMethod(owner,name,params.stream().map(t->new ImmutableMethodParameter(t,Set.of(),null)).toList(),ret,flags,anno?Set.of(new ImmutableAnnotation(1,"Landroid/webkit/JavascriptInterface;",Set.of())):Set.of(),Set.of(),code==null?null:new ImmutableMethodImplementation(regs,code,List.of(),List.of()));}
 static ImmutableClassDef clazz(String t,String parent,List<String> interfaces,List<ImmutableField> fields,ImmutableMethod... methods){return new ImmutableClassDef(t,1,parent,interfaces,null,Set.of(),fields,List.of(methods));}
 static ImmutableClassDef iface(String t,ImmutableMethod m){return new ImmutableClassDef(t,0x601,OBJ,List.of(),null,Set.of(),List.of(),List.of(m));}
 static ImmutableDexFile build(String mode,boolean map,boolean cached){
  List<ImmutableClassDef> defs=new ArrayList<>();
  var bridgeCtor=method(B,"<init>",List.of(W),"V",0x10001,2,List.of(put(1,0,B,"view",W),end()),false);
  var bind=method(B,"bind",List.of(),"V",1,3,List.of(get(0,2,B,"view",W),str(1,"genericBridge"),call(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(OBJ,"Ljava/lang/String;"),"V",0,2,1),end()),false);
  defs.add(clazz(B,OBJ,List.of(),List.of(field(B,"view",W)),bridgeCtor,bind,method(B,"invoke",List.of("Ljava/lang/String;"),"V",1,2,List.of(end()),true)));
  defs.add(clazz(C,"Landroid/webkit/WebViewClient;",List.of(),List.of(),method(C,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),method(C,"onPageFinished",List.of(W,"Ljava/lang/String;"),"V",1,3,List.of(end()),false)));
  defs.add(iface(PRODUCT,method(PRODUCT,"attach",List.of("Landroid/content/Context;","Landroid/view/ViewGroup;"),"V",0x401,3,null,false)));
  var attach=method(P,"attach",List.of("Landroid/content/Context;","Landroid/view/ViewGroup;"),"V",1,7,List.of(make(0,W),call(Opcode.INVOKE_DIRECT,W,"<init>",List.of("Landroid/content/Context;"),"V",0,5),put(0,4,P,"view",W),call(Opcode.INVOKE_VIRTUAL,W,"getSettings",List.of(),S,0),result(1),new ImmutableInstruction11n(Opcode.CONST_4,2,1),call(Opcode.INVOKE_VIRTUAL,S,"setJavaScriptEnabled",List.of("Z"),"V",1,2),make(1,C),call(Opcode.INVOKE_DIRECT,C,"<init>",List.of(),"V",1),call(Opcode.INVOKE_VIRTUAL,W,"setWebViewClient",List.of("Landroid/webkit/WebViewClient;"),"V",0,1),make(1,B),call(Opcode.INVOKE_DIRECT,B,"<init>",List.of(W),"V",1,0),call(Opcode.INVOKE_VIRTUAL,B,"bind",List.of(),"V",1),call(Opcode.INVOKE_VIRTUAL,"Landroid/view/ViewGroup;","addView",List.of("Landroid/view/View;"),"V",6,0),end()),false);
  defs.add(clazz(P,OBJ,List.of(PRODUCT),List.of(field(P,"view",W)),method(P,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),attach));
  defs.add(iface(API,method(API,"create",List.of(),PRODUCT,0x401,1,null,false)));
  defs.add(clazz(IMPL,OBJ,List.of(API),List.of(),method(IMPL,"<init>",List.of(),"V",0x10001,1,List.of(end()),false),method(IMPL,"create",List.of(),PRODUCT,1,2,List.of(make(0,P),call(Opcode.INVOKE_DIRECT,P,"<init>",List.of(),"V",0),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false)));
  List<Instruction> cc=new ArrayList<>();
  int classReg=5;if(mode.endsWith("constant")){cc.add(klass(3,IMPL));classReg=3;}
  if(mode.startsWith("direct")){cc.add(make(0,IMPL));cc.add(call(Opcode.INVOKE_DIRECT,IMPL,"<init>",List.of(),"V",0));}
  else if(mode.startsWith("class")){cc.add(call(Opcode.INVOKE_VIRTUAL,CLS,"newInstance",List.of(),OBJ,classReg));cc.add(result(0));}
  else {cc.add(call(Opcode.INVOKE_VIRTUAL,CLS,"getDeclaredConstructors",List.of(),"[Ljava/lang/reflect/Constructor;",classReg));cc.add(result(0));cc.add(zero(1));cc.add(new ImmutableInstruction23x(Opcode.AGET_OBJECT,0,0,1));cc.add(new ImmutableInstruction11n(Opcode.CONST_4,2,1));cc.add(call(Opcode.INVOKE_VIRTUAL,CTOR,"setAccessible",List.of("Z"),"V",0,2));cc.add(zero(1));cc.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY,1,1,new ImmutableTypeReference("[Ljava/lang/Object;")));cc.add(call(Opcode.INVOKE_VIRTUAL,CTOR,"newInstance",List.of("[Ljava/lang/Object;"),OBJ,0,1));cc.add(result(0));}
  cc.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0));
  defs.add(iface(ICREATOR,method(ICREATOR,"create",List.of(CLS),OBJ,0x401,2,null,false)));
  defs.add(clazz(CREATOR,OBJ,List.of(ICREATOR),List.of(),method(CREATOR,"create",List.of(CLS),OBJ,1,6,cc,false),method(CREATOR,"<init>",List.of(),"V",0x10001,1,List.of(end()),false)));
  var defaultCreator=new ImmutableField(META,"defaultCreator",ICREATOR,0x9,null,Set.of(),Set.of());
  defs.add(clazz(META,OBJ,List.of(),List.of(field(META,"implClazz",CLS),field(META,"creator",ICREATOR),field(META,"cached",OBJ),defaultCreator),
   method(META,"<clinit>",List.of(),"V",0x10008,1,List.of(make(0,CREATOR),call(Opcode.INVOKE_DIRECT,CREATOR,"<init>",List.of(),"V",0),new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(META,"defaultCreator",ICREATOR)),end()),false),
   method(META,"<init>",List.of(CLS),"V",0x10001,3,List.of(put(2,1,META,"implClazz",CLS),end()),false),
   method(META,"getCreator",List.of(),ICREATOR,1,2,List.of(get(0,1,META,"creator",ICREATOR),new ImmutableInstruction21t(Opcode.IF_NEZ,0,4),new ImmutableInstruction21c(Opcode.SGET_OBJECT,0,new ImmutableFieldReference(META,"defaultCreator",ICREATOR)),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false)));

  var regField=field(REG,"table",MAP);
  var regCtor=method(REG,"<init>",List.of(),"V",0x10001,2,List.of(make(0,MAP),call(Opcode.INVOKE_DIRECT,MAP,"<init>",List.of(),"V",0),put(0,1,REG,"table",MAP),end()),false);
  var register=method(REG,"register",List.of(CLS,META),"V",1,7,List.of(get(0,4,REG,"table",MAP),call(Opcode.INVOKE_VIRTUAL,CLS,"getName",List.of(),"Ljava/lang/String;",5),result(1),make(2,MAP),call(Opcode.INVOKE_DIRECT,MAP,"<init>",List.of(),"V",2),str(3,"_default_impl_"),call(Opcode.INVOKE_VIRTUAL,MAP,"put",List.of(OBJ,OBJ),OBJ,2,3,6),call(Opcode.INVOKE_VIRTUAL,MAP,"put",List.of(OBJ,OBJ),OBJ,0,1,2),end()),false);
  List<Instruction> lookup=new ArrayList<>();
  if(map){lookup.add(get(0,5,REG,"table",MAP));lookup.add(call(Opcode.INVOKE_VIRTUAL,CLS,"getName",List.of(),"Ljava/lang/String;",6));lookup.add(result(1));lookup.add(call(Opcode.INVOKE_VIRTUAL,MAP,"get",List.of(OBJ),OBJ,0,1));lookup.add(result(0));lookup.add(str(1,mode.endsWith("missingkey")?"_missing_":"_default_impl_"));lookup.add(call(Opcode.INVOKE_VIRTUAL,MAP,"get",List.of(OBJ),OBJ,0,1));lookup.add(result(0));}
  else {lookup.add(klass(1,IMPL));lookup.add(make(0,META));lookup.add(call(Opcode.INVOKE_DIRECT,META,"<init>",List.of(CLS),"V",0,1));}
  int nullBranch=-1;if(map){nullBranch=lookup.size();lookup.add(new ImmutableInstruction21t(Opcode.IF_EQZ,0,1));}
  // Cache branch offset computed in code units, never by instruction count.
  int branch=-1;if(cached){lookup.add(get(1,0,META,"cached",OBJ));branch=lookup.size();lookup.add(new ImmutableInstruction21t(Opcode.IF_NEZ,1,1));}
  lookup.add(call(Opcode.INVOKE_VIRTUAL,META,"getCreator",List.of(),ICREATOR,0));lookup.add(result(2));lookup.add(get(3,0,META,"implClazz",CLS));lookup.add(call(Opcode.INVOKE_INTERFACE,ICREATOR,"create",List.of(CLS),OBJ,2,3));lookup.add(result(1));if(cached)lookup.add(put(1,0,META,"cached",OBJ));lookup.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,1));
  int successReturn=lookup.size()-1;
  if(map){int nullTarget=lookup.size();lookup.add(zero(1));lookup.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT,1));patchBranch(lookup,nullBranch,nullTarget,Opcode.IF_EQZ,0);}
  if(cached)patchBranch(lookup,branch,successReturn,Opcode.IF_NEZ,1);

  defs.add(clazz(REG,OBJ,List.of(),List.of(regField),regCtor,register,method(REG,"get",List.of(CLS),OBJ,1,7,lookup,false)));
  var entry=method(A,"onCreate",List.of(),"V",1,7,List.of(make(0,REG),call(Opcode.INVOKE_DIRECT,REG,"<init>",List.of(),"V",0),klass(1,API),klass(2,IMPL),make(3,META),call(Opcode.INVOKE_DIRECT,META,"<init>",List.of(CLS),"V",3,2),call(Opcode.INVOKE_VIRTUAL,REG,"register",List.of(CLS,META),"V",0,1,3),call(Opcode.INVOKE_VIRTUAL,REG,"get",List.of(CLS),OBJ,0,1),result(2),new ImmutableInstruction21c(Opcode.CHECK_CAST,2,new ImmutableTypeReference(API)),call(Opcode.INVOKE_INTERFACE,API,"create",List.of(),PRODUCT,2),result(3),put(3,6,A,"player",PRODUCT),make(4,"Landroid/widget/FrameLayout;"),call(Opcode.INVOKE_DIRECT,"Landroid/widget/FrameLayout;","<init>",List.of("Landroid/content/Context;"),"V",4,6),get(3,6,A,"player",PRODUCT),call(Opcode.INVOKE_INTERFACE,PRODUCT,"attach",List.of("Landroid/content/Context;","Landroid/view/ViewGroup;"),"V",3,6,4),end()),false);
  defs.add(clazz(A,"Landroid/app/Activity;",List.of(),List.of(field(A,"player",PRODUCT)),entry));return new ImmutableDexFile(Opcodes.getDefault(),defs);
 }
 static void patchBranch(List<Instruction> code,int branch,int target,Opcode op,int register){int start=0,dest=0;for(int i=0;i<code.size();i++){if(i<branch)start+=code.get(i).getCodeUnits();if(i<target)dest+=code.get(i).getCodeUnits();}code.set(branch,new ImmutableInstruction21t(op,register,dest-start));}
 static void runRegression()throws Exception {
  for(boolean map:new boolean[]{false,true})for(boolean cache:new boolean[]{false,true})for(String base:List.of("class","array")){
   for(boolean missing:map?new boolean[]{false,true}:new boolean[]{false}){
    String mode=base+(missing?"missingkey":"");Path dex=Files.createTempFile("service-regression-",".dex");
    try{
     DexFileFactory.writeDexFile(dex.toString(),build(mode,map,cache));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("probe.Activity");var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("probe.Activity");
     Set<String> kinds=new HashSet<>();for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts)kinds.add((String)fact.get("kind"));}
     boolean complete=kinds.containsAll(Set.of("bridge","setting","callback"));
     if(missing?!Collections.disjoint(kinds,Set.of("bridge","setting","callback")):!complete)throw new AssertionError("Reflective service map="+map+" cache="+cache+" mode="+mode+" capabilities="+kinds);
    }finally{Files.deleteIfExists(dex);}
   }
  }
  System.out.println("ServiceConstructorArrayProbe regression PASS: actual Class field, keyed nested registry, singleton cache, exact reflection construction and missing-key negatives.");
 }
 public static void main(String[]args)throws Exception{
  for(boolean map:new boolean[]{false,true})for(boolean cache:new boolean[]{false,true})for(String mode:List.of("direct","directmissingkey","class","classconstant","classmissingkey","array","arrayconstant","arraymissingkey")){
   String name=(map?"map":"nomap")+"-"+(cache?"cache":"nocache")+"-"+mode;Path dex=Files.createTempFile("service-"+name,".dex");
   try{DexFileFactory.writeDexFile(dex.toString(),build(mode,map,cache));long deadline=System.nanoTime()+30_000_000_000L;var idx=new CapabilityIndex();idx.read(dex,deadline);var apk=new ApkInventory();apk.targetSdk=30;apk.activities.add("probe.Activity");var engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity("probe.Activity");System.out.println(name+"\t"+engine.activities);System.out.println(name+" diagnostics\t"+engine.diagnostics);System.out.println(name+" relevance\t"+idx.relevant.contains(CREATOR+"->create("+CLS+")"+OBJ));System.out.println(name+" creatorSummary\t"+engine.flow.summary(idx.resolve(CREATOR+"->create("+CLS+")"+OBJ)));}finally{Files.deleteIfExists(dex);}
  }
 }
}
