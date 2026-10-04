package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import pxb.android.axml.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Actual Manifest application lifecycle initializes a static service graph, not Activity views. */
final class ApplicationBootstrapFixture {
 public static void main(String[] args)throws Exception{run();}
 static ApkInventory inventory(String application)throws Exception{
  var writer=new AxmlWriter();var manifest=writer.child(null,"manifest");manifest.attr(null,"package",0,3,"probe");var app=manifest.child(null,"application");if(application!=null)app.attr("http://schemas.android.com/apk/res/android","name",0x01010003,3,application);app.end();manifest.end();writer.end();var inventory=new ApkInventory();new AxmlReader(writer.toByteArray()).accept(inventory.visitor(null));inventory.targetSdk=30;inventory.activities.addAll(List.of("probe.Activity","probe.SecondActivity"));return inventory;
 }
 static void run()throws Exception{
  for(String mode:List.of("direct","reflect","inherited","own_view","shared_view","unmanifest","other_application","abstract_application","private_application","missing_constructor")){
   List<ClassDef> classes=new ArrayList<>(ServiceStartupRegistrationProbe.buildStartup("array",mode.equals("direct")?"application-direct":"application-reflect").getClasses());
   if(Set.of("own_view","shared_view").contains(mode)){
    List<ClassDef> changed=new ArrayList<>();for(ClassDef type:classes){
     if(type.getType().equals(ServiceStartupRegistrationProbe.APP)){
      List<Method> methods=new ArrayList<>();type.getMethods().forEach(methods::add);List<org.jf.dexlib2.iface.instruction.Instruction> body=new ArrayList<>(List.of(make(0,W),call(Opcode.INVOKE_DIRECT,W,"<init>",List.of("Landroid/content/Context;"),"V",0,3),make(1,B),call(Opcode.INVOKE_DIRECT,B,"<init>",List.of(W),"V",1,0),str(2,"bootstrap_only"),call(Opcode.INVOKE_VIRTUAL,W,"addJavascriptInterface",List.of(OBJ,"Ljava/lang/String;"),"V",0,1,2)));
      if(mode.equals("shared_view"))body.add(new ImmutableInstruction21c(Opcode.SPUT_OBJECT,0,new ImmutableFieldReference(ServiceStartupRegistrationProbe.HOLD,"applicationView",W)));body.add(end());methods.add(method(type.getType(),"onCreate",List.of(),"V",1,4,body,false));
      changed.add(new ImmutableClassDef(type.getType(),type.getAccessFlags(),type.getSuperclass(),type.getInterfaces(),null,type.getAnnotations(),type.getFields(),methods));
     }else if(type.getType().equals(ServiceStartupRegistrationProbe.HOLD)&&mode.equals("shared_view")){
      List<ImmutableField> fields=new ArrayList<>();for(Field f:type.getFields())fields.add(ImmutableField.of(f));fields.add(new ImmutableField(type.getType(),"applicationView",W,9,null,Set.of(),Set.of()));changed.add(new ImmutableClassDef(type.getType(),type.getAccessFlags(),type.getSuperclass(),type.getInterfaces(),null,type.getAnnotations(),fields,type.getMethods()));
     }else changed.add(type);
    }classes=changed;
   }
   classes.add(clazz("Lprobe/SecondActivity;",A,List.of(),List.of()));classes.add(clazz("Lprobe/InheritedApplication;",ServiceStartupRegistrationProbe.APP,List.of(),List.of()));classes.add(clazz("Lprobe/UnusedApplication;","Landroid/app/Application;",List.of(),List.of(),method("Lprobe/UnusedApplication;","unusedInitialize",List.of(),"V",1,1,List.of(call(Opcode.INVOKE_STATIC,ServiceStartupRegistrationProbe.START,"activate",List.of(),"V"),end()),false)));

   List<ClassDef> withConstructors=new ArrayList<>();for(ClassDef type:classes){
    if(Set.of(ServiceStartupRegistrationProbe.APP,"Lprobe/InheritedApplication;","Lprobe/UnusedApplication;").contains(type.getType())){
     List<Method> methods=new ArrayList<>();type.getMethods().forEach(methods::add);methods.add(method(type.getType(),"<init>",List.of(),"V",0x10001,1,List.of(call(Opcode.INVOKE_DIRECT,type.getSuperclass(),"<init>",List.of(),"V",0),end()),false));withConstructors.add(new ImmutableClassDef(type.getType(),type.getAccessFlags(),type.getSuperclass(),type.getInterfaces(),null,type.getAnnotations(),type.getFields(),methods));
    }else withConstructors.add(type);
   }classes=withConstructors;
   classes.add(new ImmutableClassDef("Lprobe/AbstractApplication;",0x401,ServiceStartupRegistrationProbe.APP,List.of(),null,Set.of(),List.of(),List.of(method("Lprobe/AbstractApplication;","<init>",List.of(),"V",0x10001,1,List.of(end()),false))));
   classes.add(new ImmutableClassDef("Lprobe/PrivateApplication;",0,ServiceStartupRegistrationProbe.APP,List.of(),null,Set.of(),List.of(),List.of(method("Lprobe/PrivateApplication;","<init>",List.of(),"V",0x10001,1,List.of(end()),false))));
   classes.add(clazz("Lprobe/MissingConstructorApplication;",ServiceStartupRegistrationProbe.APP,List.of(),List.of(),method("Lprobe/MissingConstructorApplication;","<init>",List.of("Landroid/content/Context;"),"V",0x10001,2,List.of(end()),false)));
   Path dex=Files.createTempFile("application-bootstrap-",".dex");try{
    DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),classes));long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);
    String application=mode.equals("unmanifest")?null:mode.equals("other_application")?".UnusedApplication":mode.equals("inherited")?".InheritedApplication":mode.equals("abstract_application")?".AbstractApplication":mode.equals("private_application")?".PrivateApplication":mode.equals("missing_constructor")?".MissingConstructorApplication":".Application";
    var apk=inventory(application);var engine=new CapabilityEngine(index,apk,deadline);engine.analyzeActivity("probe.Activity");engine.analyzeActivity("probe.SecondActivity");
    boolean positive=Set.of("direct","reflect","inherited","own_view","shared_view").contains(mode);Set<String> kinds=new HashSet<>();Set<Object> webviews=new HashSet<>();
    for(var activity:engine.activities){@SuppressWarnings("unchecked")var facts=(List<Map<String,Object>>)activity.get("facts");for(var fact:facts){kinds.add((String)fact.get("kind"));webviews.add(List.of(activity.get("activity"),fact.get("webview")));}}
    if(positive?!kinds.containsAll(Set.of("bridge","setting","callback"))||webviews.size()!=2:!Collections.disjoint(kinds,Set.of("bridge","setting","callback")))throw new AssertionError("Application bootstrap "+mode+" capabilities="+kinds+" webviews="+webviews+" reports="+engine.activities+" bootstrap="+engine.applicationBootstrap().diagnostics());
    if(Set.of("abstract_application","private_application","missing_constructor").contains(mode)&&engine.applicationBootstrap().diagnostics().stream().noneMatch(gap->gap.contains("not_constructible")||gap.contains("constructor_missing")))throw new AssertionError("Invalid Manifest Application was not diagnosed");
    if(Set.of("own_view","shared_view").contains(mode)){
     var report=engine.report("fixture","finished",Map.of());@SuppressWarnings("unchecked")var unattributed=(List<Map<String,Object>>)report.get("unattributed");long count=unattributed.stream().filter(row->"application_bootstrap_no_activity_owner".equals(row.get("reason"))).count();if(count!=1)throw new AssertionError("Bootstrap own WebView capability lost or assigned to every Activity: "+report);
     if(mode.equals("shared_view")&&engine.applicationBootstrap().diagnostics().stream().noneMatch(gap->gap.contains("shared_webview_surface_unmodeled")))throw new AssertionError("Retained bootstrap WebView surface gap undiagnosed");
    }
    if(Set.of("reflect","own_view","shared_view").contains(mode)){
     try(var scheduler=new ParallelActivityScheduler(index,apk,List.of("probe.Activity","probe.SecondActivity"),System.nanoTime(),deadline,deadline,2)){
      scheduler.initialPass(()->{});while(!scheduler.finished()&&System.nanoTime()<deadline)scheduler.deepEpoch(100_000_000L,()->{});
      if(!scheduler.finished())throw new AssertionError("Parallel bootstrap fixture timed out");

      if(Set.of("own_view","shared_view").contains(mode)){
       @SuppressWarnings("unchecked")var unattributed=(List<Map<String,Object>>)scheduler.aggregate.report("fixture","finished",Map.of()).get("unattributed");
       if(unattributed.stream().filter(row->"application_bootstrap_no_activity_owner".equals(row.get("reason"))).count()!=1||unattributed.stream().anyMatch(row->row.containsKey("site")&&String.valueOf(row.get("site")).startsWith(ServiceStartupRegistrationProbe.APP+"->onCreate")))throw new AssertionError("Parallel report lost/duplicated bootstrap unattributed site");
      }
      // Timing/coverage counters differ by slicing; compare actual fact collections by root.
      for(String root:List.of("probe.Activity","probe.SecondActivity"))if(!engine.stateReport(engine.states.get(root)).get("facts").equals(scheduler.aggregate.stateReport(scheduler.aggregate.states.get(root)).get("facts")))throw new AssertionError("Serial/parallel bootstrap facts differ for "+root);
     }
    }
    if(mode.equals("reflect")){
     var expired=new CapabilityEngine(index,apk,System.nanoTime()-1);var partial=expired.applicationBootstrap();if(partial.diagnostics().stream().noneMatch(gap->gap.contains("bootstrap_deadline")))throw new AssertionError("Expired bootstrap falsely reported genuinely empty state");
    }
    if(positive){
     var snapshot=engine.applicationBootstrap();var first=engine.new Host("copy1");var second=engine.new Host("copy2");snapshot.install(first);snapshot.install(second);
     if(first.maps.isEmpty())throw new AssertionError("Static registry object closure missing");String key=first.maps.keySet().iterator().next();int size=second.maps.get(key).size();first.maps.get(key).clear();if(second.maps.get(key).size()!=size||snapshot.maps().get(key).size()!=size)throw new AssertionError("Bootstrap Map snapshot is shared mutable Host state");
    }
   }finally{Files.deleteIfExists(dex);}
  }
  System.out.println("ApplicationBootstrapFixture PASS: actual Manifest relative name, attach before create, inherited actual lifecycle, static registry object closure, two-Activity/two-WebView/independent mutable copy isolation, unmanifest and unused helper negatives.");
 }
}
