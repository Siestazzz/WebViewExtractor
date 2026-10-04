package org.example;
import java.nio.file.*;import java.util.*;import java.util.concurrent.atomic.*;import java.lang.management.*;
/** Isolated bootstrap stack sampler; no Activity analysis. */
public final class BootstrapTimingProbe {
 public static void main(String[]args)throws Exception{
  long start=System.nanoTime(),deadline=start+180_000_000_000L;Path apkPath=Path.of(args[0]);ApkInventory apk=ApkInventory.read(apkPath);CapabilityIndex idx=new CapabilityIndex();idx.read(apkPath,deadline);CapabilityEngine e=new CapabilityEngine(idx,apk,deadline);System.out.println("INDEX_SECONDS "+(System.nanoTime()-start)/1e9);
  Thread target=Thread.currentThread();AtomicBoolean running=new AtomicBoolean(true);Map<String,Integer> stacks=new LinkedHashMap<>();Thread sampler=new Thread(()->{while(running.get()){StringBuilder b=new StringBuilder();for(StackTraceElement f:target.getStackTrace())b.append(f).append("\n");stacks.merge(b.toString(),1,Integer::sum);try{Thread.sleep(10);}catch(InterruptedException ex){return;}}},"bootstrap-sampler");sampler.setDaemon(true);ThreadMXBean bean=ManagementFactory.getThreadMXBean();long cpu=bean.getCurrentThreadCpuTime(),wall=System.nanoTime();sampler.start();var state=e.applicationBootstrap();running.set(false);sampler.join();System.out.println("BOOTSTRAP_WALL_SECONDS "+(System.nanoTime()-wall)/1e9);System.out.println("BOOTSTRAP_CPU_SECONDS "+(bean.getCurrentThreadCpuTime()-cpu)/1e9);System.out.println("DIAGNOSTICS "+state.diagnostics());stacks.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(30).forEach(x->System.out.println("STACK_COUNT "+x.getValue()+"\n"+x.getKey()));
 }
}
