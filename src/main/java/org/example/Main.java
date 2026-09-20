package org.example;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Supervisor enforces a wall-clock deadline even if a library fails to cooperate. */
public class Main {
    static final Gson JSON=new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static void main(String[] args) throws Exception {
        if(Arrays.asList(args).contains("--legacy")||Arrays.asList(args).contains("--read-apk-only")){LegacyMain.main(args);return;}
        Map<String,String> options=options(args);
        Path apk=Path.of(required(options,"--apkpath")).toAbsolutePath();
        if(!Files.isRegularFile(apk))throw new IllegalArgumentException("APK not found: "+apk);
        int hard=number(options,"--hard-seconds",600),target=number(options,"--target-seconds",300);
        if(target>hard)throw new IllegalArgumentException("target seconds must not exceed hard seconds");
        Path out=Path.of(options.getOrDefault("--out","output/"+Util.apkTag(apk))).toAbsolutePath();Files.createDirectories(out);
        if(options.containsKey("--worker")){worker(apk,out,target,hard);return;}
        long start=System.nanoTime();Path report=out.resolve("capabilities.json");
        write(report,Map.of("schema_version",1,"status","starting","apk",apk.toString(),"activities",List.of(),"unattributed",List.of()));
        List<String> cmd=new ArrayList<>(List.of(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-Xmx"+Runtime.getRuntime().maxMemory(),"-XX:ActiveProcessorCount="+Runtime.getRuntime().availableProcessors(),"-cp",System.getProperty("java.class.path"),Main.class.getName(),"--worker","--apkpath",apk.toString(),"--out",out.toString(),"--target-seconds",String.valueOf(target),"--hard-seconds",String.valueOf(Math.max(1,hard-5))));
        Process process=new ProcessBuilder(cmd).inheritIO().start();
        Thread shutdown=new Thread(()->{process.descendants().forEach(ProcessHandle::destroyForcibly);process.destroyForcibly();});Runtime.getRuntime().addShutdownHook(shutdown);
        boolean done=process.waitFor(Math.max(1,hard-2),TimeUnit.SECONDS);
        if(!done){process.destroy();if(!process.waitFor(250,TimeUnit.MILLISECONDS))process.destroyForcibly();}
        Runtime.getRuntime().removeShutdownHook(shutdown);
        JsonObject finalReport;
        try(Reader r=Files.newBufferedReader(report)){finalReport=JsonParser.parseReader(r).getAsJsonObject();}
        if(!done)finalReport.addProperty("status","timeout");else if(process.exitValue()!=0)finalReport.addProperty("status","failed");
        if(!done||process.exitValue()!=0){JsonArray issues=finalReport.has("diagnostics")?finalReport.getAsJsonArray("diagnostics"):new JsonArray();issues.add(!done?"supervisor_hard_deadline":"worker_exit_code:"+process.exitValue());finalReport.add("diagnostics",issues);}
        finalReport.addProperty("wall_seconds",(System.nanoTime()-start)/1e9);finalReport.addProperty("target_seconds",target);finalReport.addProperty("hard_seconds",hard);
        write(report,finalReport);
        System.out.println("Wrote: "+report+" status="+finalReport.get("status"));
        if(!done||process.exitValue()!=0)System.exit(2);
    }
    static void worker(Path apk,Path out,int target,int hard) throws Exception {
        long start=System.nanoTime(),deadline=start+hard*1_000_000_000L;Map<String,Object> metrics=new LinkedHashMap<>();
        String hash;try(InputStream stream=Files.newInputStream(apk)){MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] b=new byte[1024*1024];int n;while((n=stream.read(b))!=-1)digest.update(b,0,n);hash=HexFormat.of().formatHex(digest.digest());}
        ApkInventory inventory=ApkInventory.read(apk);
        write(out.resolve("capabilities.json"),Map.of("schema_version",1,"status","indexing","apk_sha256",hash,"package",inventory.packageName,"version",inventory.version,"activities",List.of(),"unattributed",List.of(),"diagnostics",List.of("index_not_finished")));
        long t=System.nanoTime();CapabilityIndex idx=new CapabilityIndex();idx.read(apk,deadline);
        metrics.put("index_seconds",(System.nanoTime()-t)/1e9);metrics.put("classes",idx.classes.size());metrics.put("methods",idx.methods.size());metrics.put("instructions",idx.instructions);metrics.put("seed_methods",idx.seeds.size());metrics.put("relevant_methods",idx.relevant.size());metrics.put("manifest_activities",inventory.activities.size());
        System.out.println("Indexed: "+JSON.toJson(metrics));
        CapabilityEngine engine=new CapabilityEngine(idx,inventory,deadline);List<String> roots=new ArrayList<>(inventory.activities);
        if(roots.isEmpty())idx.classes.keySet().stream().filter(idx::activity).sorted().forEach(roots::add);
        // Direct seed hosts first; remaining hosts are still examined, not silently excluded.
        roots.sort(Comparator.comparingInt((String a)->idx.hierarchyMethods(a).stream().anyMatch(m->idx.seeds.contains(CapabilityIndex.key(m)))?0:1).thenComparing(a->a));
        long[] checkpoint={System.nanoTime()};int processed=0;
        engine.checkpoint=()->{
            long now=System.nanoTime();if(now-checkpoint[0]<10_000_000_000L)return;
            metrics.put("decoded_methods",engine.flow.decoded);metrics.put("elapsed_seconds",(now-start)/1e9);
            try{write(out.resolve("capabilities.json"),engine.report(hash,"partial",metrics));}catch(IOException ex){throw new UncheckedIOException(ex);}
            checkpoint[0]=System.nanoTime();
        };
        metrics.put("target_seconds",target);metrics.put("worker_budget_seconds",hard);
        metrics.put("analysis_strategy","dex_index_parameterized_summaries");
        write(out.resolve("capabilities.json"),engine.report(hash,"partial",metrics));
        for(String activity:roots){
            if(System.nanoTime()>deadline)break;
            engine.analyzeActivity(activity);processed++;metrics.put("processed_activities",processed);metrics.put("decoded_methods",engine.flow.decoded);metrics.put("elapsed_seconds",(System.nanoTime()-start)/1e9);
            if(System.nanoTime()-checkpoint[0]>10_000_000_000L){engine.checkpoint.run();System.out.println("Progress: "+processed+"/"+roots.size()+" activities="+engine.activities.size()+" decoded="+engine.flow.decoded);}
        }
        metrics.put("elapsed_seconds",(System.nanoTime()-start)/1e9);metrics.put("processed_activities",processed);metrics.put("decoded_methods",engine.flow.decoded);
        String status=processed==roots.size()&&engine.diagnostics.isEmpty()&&idx.diagnostics.isEmpty()?"complete":"partial";
        write(out.resolve("capabilities.json"),engine.report(hash,status,metrics));
    }
    static void write(Path path,Object value)throws IOException{
        Path temp=path.resolveSibling(path.getFileName()+".tmp");try(Writer w=Files.newBufferedWriter(temp)){JSON.toJson(value,w);}
        try{Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}
    }
    static Map<String,String> options(String[] args){
        Map<String,String> result=new LinkedHashMap<>();Set<String> allowed=Set.of("--apkpath","--out","--target-seconds","--hard-seconds","--worker","--pathcount");
        for(int i=0;i<args.length;i++){
            String key=args[i];if(!allowed.contains(key))throw new IllegalArgumentException("Unknown argument: "+key);
            if(key.equals("--worker")){result.put(key,"true");continue;}
            if(i+1==args.length)throw new IllegalArgumentException("Missing value: "+key);result.put(key,args[++i]);
        }return result;
    }
    static String required(Map<String,String> opts,String key){if(!opts.containsKey(key))throw new IllegalArgumentException("Missing "+key);return opts.get(key);}
    static int number(Map<String,String> opts,String key,int fallback){int n=Integer.parseInt(opts.getOrDefault(key,String.valueOf(fallback)));if(n<1)throw new IllegalArgumentException(key+" must be positive");return n;}
}
