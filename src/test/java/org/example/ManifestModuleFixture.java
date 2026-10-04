package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import pxb.android.axml.*;
import static org.example.CapabilitySelfTest.*;

/** Actual public metadata enumeration, guarded key factory, static cache and bound interface call. */
final class ManifestModuleFixture {
    static final String LOADER="Lmanifest/Loader;",CONTRACT="Lmanifest/Contract;",GOOD="Lmanifest/Accepted;",BAD="Lmanifest/Rejected;",PRIVATE="Lmanifest/PrivateConstructor;",BRIDGE="Lmanifest/Bridge;",ACTIVITY_ONLY="Lmanifest/ActivityOnly;";
    static final String CTX="Landroid/content/Context;",PM="Landroid/content/pm/PackageManager;",INFO="Landroid/content/pm/ApplicationInfo;",BUNDLE="Landroid/os/Bundle;",ITER="Ljava/util/Iterator;",SET="Ljava/util/Set;",CLASS="Ljava/lang/Class;",NS="http://schemas.android.com/apk/res/android";
    static final ImmutableFieldReference CACHE=new ImmutableFieldReference(LOADER,"cache",CONTRACT);
    static Instruction result(int register){return new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT,register);}
    static ImmutableMethod scan(String name,int flags,boolean foreign){
        List<Instruction> code=new ArrayList<>(List.of(invoke(Opcode.INVOKE_VIRTUAL,CTX,"getPackageManager",List.of(),PM,10),result(0)));
        if(foreign)code.add(str(1,"foreign.package"));else{code.add(invoke(Opcode.INVOKE_VIRTUAL,CTX,"getPackageName",List.of(),"Ljava/lang/String;",10));code.add(result(1));}
        code.add(new ImmutableInstruction21s(Opcode.CONST_16,2,flags));code.add(invoke(Opcode.INVOKE_VIRTUAL,PM,"getApplicationInfo",List.of("Ljava/lang/String;","I"),INFO,0,1,2));code.add(result(0));code.add(new ImmutableInstruction22c(Opcode.IGET_OBJECT,3,0,new ImmutableFieldReference(INFO,"metaData",BUNDLE)));code.add(invoke(Opcode.INVOKE_VIRTUAL,BUNDLE,"keySet",List.of(),SET,3));code.add(result(0));code.add(invoke(Opcode.INVOKE_INTERFACE,SET,"iterator",List.of(),ITER,0));code.add(result(0));
        int loop=code.size();code.add(invoke(Opcode.INVOKE_INTERFACE,ITER,"hasNext",List.of(),"Z",0));code.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT,4));int exitBranch=code.size();code.add(new ImmutableInstruction21t(Opcode.IF_EQZ,4,1));code.add(invoke(Opcode.INVOKE_INTERFACE,ITER,"next",List.of(),"Ljava/lang/Object;",0));code.add(result(1));code.add(new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference("Ljava/lang/String;")));code.add(invoke(Opcode.INVOKE_VIRTUAL,BUNDLE,"get",List.of("Ljava/lang/String;"),"Ljava/lang/Object;",3,1));code.add(result(2));code.add(str(4,"selected"));code.add(invoke(Opcode.INVOKE_STATIC,"Landroid/text/TextUtils;","equals",List.of("Ljava/lang/CharSequence;","Ljava/lang/CharSequence;"),"Z",2,4));code.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT,4));int filterBranch=code.size();code.add(new ImmutableInstruction21t(Opcode.IF_EQZ,4,1));code.add(invoke(Opcode.INVOKE_STATIC,CLASS,"forName",List.of("Ljava/lang/String;"),CLASS,1));code.add(result(1));code.add(invoke(Opcode.INVOKE_VIRTUAL,CLASS,"newInstance",List.of(),"Ljava/lang/Object;",1));code.add(result(1));code.add(new ImmutableInstruction21c(Opcode.CHECK_CAST,1,new ImmutableTypeReference(CONTRACT)));code.add(new ImmutableInstruction21c(Opcode.SPUT_OBJECT,1,CACHE));int again=code.size();code.add(new ImmutableInstruction30t(Opcode.GOTO_32,1));int endIndex=code.size();code.add(end());
        int[] offsets=new int[code.size()];for(int i=1;i<offsets.length;i++)offsets[i]=offsets[i-1]+code.get(i-1).getCodeUnits();code.set(exitBranch,new ImmutableInstruction21t(Opcode.IF_EQZ,4,offsets[endIndex]-offsets[exitBranch]));code.set(filterBranch,new ImmutableInstruction21t(Opcode.IF_EQZ,4,offsets[again]-offsets[filterBranch]));code.set(again,new ImmutableInstruction30t(Opcode.GOTO_32,offsets[loop]-offsets[again]));
        return method(LOADER,name,List.of(CTX),9,11,code,false);
    }
    static ImmutableClassDef module(String owner,int ctorFlags){
        var init=method(owner,"apply",List.of(W),1,4,List.of(make(0,BRIDGE),str(1,owner.equals(GOOD)?"manifest":owner.equals(PRIVATE)?"private_ctor_must_not_run":"rejected_value_must_not_run"),invoke(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of("Ljava/lang/Object;","Ljava/lang/String;"),"V",3,0,1),end()),false);
        return new ImmutableClassDef(owner,1,"Ljava/lang/Object;",List.of(CONTRACT),null,Set.of(),List.of(),List.of(method(owner,"<init>",List.of(),ctorFlags,1,List.of(end()),false),init));
    }
    static void metadata(NodeVisitor parent,String key,Object value,int type){var item=parent.child(null,"meta-data");item.attr(NS,"name",0x01010003,3,key);item.attr(NS,"value",0x01010024,type,value);item.end();}
    static ApkInventory inventory()throws Exception{
        var writer=new AxmlWriter();var manifest=writer.child(null,"manifest");manifest.attr(null,"package",0,3,"manifest.fixture");var app=manifest.child(null,"application");metadata(app,"manifest.Accepted","selected",3);metadata(app,"manifest.Rejected","other",3);metadata(app,"manifest.PrivateConstructor","selected",3);metadata(app,"missing.RuntimeModule","selected",3);var activity=app.child(null,"activity");activity.attr(NS,"name",0x01010003,3,"manifest.Host0");metadata(activity,"manifest.ActivityOnly","selected",3);activity.end();app.end();manifest.end();writer.end();var inventory=new ApkInventory();new AxmlReader(writer.toByteArray()).accept(inventory.visitor(null));return inventory;
    }
    static void contextBoundaries(CapabilityIndex index,ApkInventory apk,CapabilityEngine engine){
        var host=engine.new Host("manifest.Host0");var job=new CapabilityEngine.Job(index.resolve(LOADER+"->scan("+CTX+")V"),List.of(DexFlow.UNKNOWN),List.of(),true);
        check(ManifestProtocols.canonicalApi(index,"Lmanifest/Application;->getPackageName()Ljava/lang/String;").equals("Landroid/content/Context;->getPackageName()Ljava/lang/String;"),"Inherited Application native contract lost");
        check(!ManifestProtocols.pure(index,"Lmanifest/Application;->getPackageName(I)Ljava/lang/String;"),"Wrong Context overload recognized");
        var application=DexFlow.V.of("object","manifest.Application","actual_application");
        var packageCall=new DexFlow.V("return","java.lang.String","Lmanifest/Application;->getPackageName()Ljava/lang/String;",null,List.of(application));
        check("manifest.fixture".equals(ManifestProtocols.resolve(packageCall,engine,job,host,0).literal()),"Actual Application Context not bound to APK");
        var overrideCall=new DexFlow.V("return","java.lang.String","Lmanifest/ContextOverride;->getPackageName()Ljava/lang/String;",null,List.of(DexFlow.V.of("object","manifest.ContextOverride","override")));
        check(ManifestProtocols.resolve(overrideCall,engine,job,host,0).equals(DexFlow.UNKNOWN),"Application Context override bypassed");
        check(!ManifestProtocols.currentContext(DexFlow.UNKNOWN,engine,host),"Unknown Context acquired current APK scope");
        check(ManifestProtocols.typed(new ApkInventory.MetadataValue(1,123)).kind().equals("unknown"),"Resource metadata treated as class selector literal");
        var iterator=new DexFlow.V("manifest_iterator","java.util.Iterator","empty",null,List.of(DexFlow.V.of("manifest_bundle","android.os.Bundle","empty")));
        var hasNext=new DexFlow.V("return","boolean","Ljava/util/Iterator;->hasNext()Z",null,List.of(iterator));
        var saved=new TreeMap<>(apk.applicationMetadata);apk.applicationMetadata.clear();
        check("0".equals(ManifestProtocols.resolve(hasNext,engine,job,host,0).literal()),"Empty metadata iterator not empty");apk.applicationMetadata.putAll(saved);
    }
    public static void main(String[] args)throws Exception{run();}
    static void run()throws Exception{
        List<ImmutableClassDef> classes=new ArrayList<>();List<String> positives=List.of("manifest.Host0","manifest.Host1");
        for(int i=0;i<5;i++){
            String host="Lmanifest/Host"+i+";";List<Instruction> code=new ArrayList<>();String scanner=i==2?"noFlags":i==3?"foreign":"scan";
            if(i!=4)code.add(invoke(Opcode.INVOKE_STATIC,LOADER,scanner,List.of(CTX),"V",3));
            for(int j=0;j<2;j++){code.add(make(0,W));code.add(invoke(Opcode.INVOKE_STATIC,LOADER,"bind",List.of(W),"V",0));}code.add(end());classes.add(clazz(host,"Landroid/app/Activity;",method(host,"onCreate",List.of(),1,4,code,false)));
        }
        var bind=method(LOADER,"bind",List.of(W),9,2,List.of(new ImmutableInstruction21c(Opcode.SGET_OBJECT,0,CACHE),invoke(Opcode.INVOKE_INTERFACE,CONTRACT,"apply",List.of(W),"V",0,1),end()),false);
        var contract=new ImmutableMethod(CONTRACT,"apply",List.of(new ImmutableMethodParameter(W,Set.of(),null)),"V",0x401,Set.of(),Set.of(),null);
        classes.add(new ImmutableClassDef(CONTRACT,0x601,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(contract)));classes.add(new ImmutableClassDef(LOADER,1,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(new ImmutableField(LOADER,"cache",CONTRACT,9,null,Set.of(),Set.of())),List.of(scan("scan",128,false),scan("noFlags",0,false),scan("foreign",128,true),bind)));
        classes.add(clazz("Lmanifest/Application;","Landroid/app/Application;"));classes.add(clazz("Lmanifest/ContextOverride;",CTX,method("Lmanifest/ContextOverride;","getPackageName",List.of(),1,2,List.of(str(0,"foreign.package"),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false)));
        classes.add(module(GOOD,0x10001));classes.add(module(BAD,0x10001));classes.add(module(PRIVATE,0x10002));classes.add(module(ACTIVITY_ONLY,0x10001));classes.add(clazz(BRIDGE,"Ljava/lang/Object;",method(BRIDGE,"expose",List.of(),1,1,List.of(end()),true)));
        Path file=Files.createTempFile("manifest-modules-",".dex");try{
            DexFileFactory.writeDexFile(file.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(file,deadline);var apk=inventory();
            var engine=new CapabilityEngine(index,apk,deadline);contextBoundaries(index,apk,engine);for(int i=0;i<5;i++)engine.analyzeActivity("manifest.Host"+i);
            List<Map<String,Object>> bridges=new ArrayList<>();for(var report:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)report.get("facts");for(var fact:facts)if(fact.get("kind").equals("bridge"))bridges.add(fact);}
            check(bridges.size()==4,"Manifest factory binding missing or broadened: "+bridges+" reports="+engine.activities);
            check(bridges.stream().allMatch(f->positives.contains(f.get("activity"))&&f.get("registration_name").equals("manifest")),"Uncalled/foreign/missing flag/wrong selector/private ctor metadata leaked bridge");
            check(bridges.stream().map(f->f.get("webview")).distinct().count()==4,"Two WebViews or two host heaps merged");
            check(apk.applicationMetadata.size()==4&&!apk.applicationMetadata.containsKey("manifest.ActivityOnly"),"Activity metadata leaked into ApplicationInfo inventory");
            check(engine.activities.toString().contains("manifest_class_unresolved")&&engine.activities.toString().contains("manifest_public_noarg_constructor_missing"),"Unresolved class/constructor diagnosis was lost");
            System.out.println("ManifestModuleFixture PASS: typed application metadata, actual GET_META_DATA/current-package iteration, correlated selector, reflective constructor/static cache/interface call, two-WebView/two-Activity isolation and wrong value/flags/package/unconsumed/private/unresolved negatives.");
        }finally{Files.deleteIfExists(file);}
    }
}
