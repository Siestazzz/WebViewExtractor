package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.ServiceConstructorArrayProbe.*;

/** Immutable graph budget uncertainty is cached; binding context and deadline are not. */
final class StaticReflectionBudgetFixture {
 static void run()throws Exception{
  String graph="Lreflection/BudgetGraph;";List<ImmutableMethod> methods=new ArrayList<>();
  for(int i=0;i<200;i++)methods.add(method(graph,"step"+i,List.of(),"V",9,3,i<199?List.of(call(Opcode.INVOKE_STATIC,graph,"step"+(i+1),List.of(),"V"),end()):List.of(call(Opcode.INVOKE_VIRTUAL,CLS,"getMethod",List.of("Ljava/lang/String;","[Ljava/lang/Class;"),ServiceStartupRegistrationProbe.METHOD,0,1,2),end()),false));
  Path dex=Files.createTempFile("reflection-graph-budget-",".dex");try{
   DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(graph,OBJ,List.of(),List.of(),methods.toArray(ImmutableMethod[]::new)))));
   long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);var first=engine.new Host("first");var second=engine.new Host("second");var root=index.resolve(graph+"->step0()V");
   if(StaticReflection.reachable(root,engine,first))throw new AssertionError("Budget-exhausted graph reported proven reachable");long queries=engine.staticReflectionGraphQueries;
   for(int i=0;i<40;i++)if(StaticReflection.reachable(root,engine,i%2==0?first:second))throw new AssertionError("Budget uncertainty converted to proven reachable");
   if(engine.staticReflectionGraphQueries!=queries||engine.staticReflectionReachable.get(CapabilityIndex.key(root))!=StaticReflection.Reach.BUDGET)throw new AssertionError("Repeated graph budget re-traversal or false cache classification");
   if(first.gaps.stream().noneMatch(gap->gap.contains("reachability_budget"))||second.gaps.stream().noneMatch(gap->gap.contains("reachability_budget")))throw new AssertionError("Cached uncertainty was not diagnosed in both hosts");
   if(!StaticReflection.reachable(index.resolve(graph+"->step199()V"),engine,second))throw new AssertionError("Exact short positive path lost after another root hit budget");
   var interrupted=new CapabilityEngine(index,new ApkInventory(),System.nanoTime()-1);var interruptedHost=interrupted.new Host("deadline");StaticReflection.reachable(root,interrupted,interruptedHost);
   if(interrupted.staticReflectionReachable.containsKey(CapabilityIndex.key(root))||interruptedHost.gaps.stream().noneMatch(gap->gap.contains("reachability_deadline")))throw new AssertionError("Deadline interruption cached as immutable graph result");
  }finally{Files.deleteIfExists(dex);}
  System.out.println("StaticReflectionBudgetFixture PASS: repeated128-state uncertainty traverses once, two-host diagnostics, independent short positive and uncached deadline interruption.");
 }
}
