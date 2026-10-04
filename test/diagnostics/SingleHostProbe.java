package org.example;
import java.nio.file.*;
import java.util.*;
import java.security.*;
import com.google.gson.*;
/** Diagnostic isolation only: not an APK benchmark or acceptance run. */
public final class SingleHostProbe {
 public static void main(String[] args)throws Exception {
  Path apkPath=Path.of(args[0]),out=Path.of(args[2]);String host=args[1];
  long start=System.nanoTime(),deadline=start+170_000_000_000L;
  String hash;try(var in=Files.newInputStream(apkPath)){var md=MessageDigest.getInstance("SHA-256");byte[] bytes=new byte[1048576];int n;while((n=in.read(bytes))!=-1)md.update(bytes,0,n);hash=HexFormat.of().formatHex(md.digest());}
  ApkInventory apk=ApkInventory.read(apkPath);CapabilityIndex idx=new CapabilityIndex();idx.read(apkPath,deadline);
  double indexed=(System.nanoTime()-start)/1e9;System.out.println("Indexed seconds="+indexed+" relevant="+idx.relevant.size());
  CapabilityEngine engine=new CapabilityEngine(idx,apk,deadline);engine.analyzeActivity(host);
  Map<String,Object> metrics=new LinkedHashMap<>();metrics.put("diagnostic_only",true);metrics.put("selected_host",host);metrics.put("index_and_inventory_seconds",indexed);metrics.put("seconds",(System.nanoTime()-start)/1e9);
  var report=engine.report(hash,"diagnostic_single_host",metrics);
  Files.createDirectories(out.getParent());Files.writeString(out,new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report)+"\n");
  System.out.println("Diagnostic complete "+metrics);
 }
}
