package org.example;
import java.nio.file.*;
import java.util.*;
public class CompactProbe {
 public static void main(String[] a)throws Exception{
  long deadline=System.nanoTime()+180_000_000_000L;var idx=new CapabilityIndex();idx.read(Path.of(a[0]),deadline);var engine=new CapabilityEngine(idx,ApkInventory.read(Path.of(a[0])),deadline);
  final CapabilityEngine.Host[] holder=new CapabilityEngine.Host[1];engine.checkpoint=()->holder[0]=engine.currentHost;engine.analyzeActivity(a[1]);var host=holder[0];
  for(int i=3;i<a.length;i++){String part=a[i];for(var m:idx.methods.values())if(CapabilityIndex.key(m).contains(part)){String id=CapabilityIndex.key(m);System.out.println("METHOD "+id+" relevant="+idx.relevant.contains(id));for(var call:engine.flow.summary(m).calls())System.out.println("CALL "+call.toString().substring(0,Math.min(call.toString().length(),2300)));}
  if(host!=null)host.visited.stream().filter(s->s.substring(0,s.indexOf('|')).contains(part)).limit(5).forEach(s->System.out.println("VISITED "+s.substring(0,Math.min(s.length(),2700))));}
  if(host!=null){Files.writeString(Path.of(a[2]+".contexts.json"),new com.google.gson.Gson().toJson(Map.of("visited",host.visited,"xml_bindings",host.xmlBindings,"heap",host.heap)));System.out.println("DIAGNOSTICS "+host.gaps);System.out.println("STATS visited="+host.visited.size()+" facts="+host.facts.size());host.visited.stream().collect(java.util.stream.Collectors.groupingBy(s->s.substring(0,s.indexOf('|')),java.util.stream.Collectors.counting())).entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(25).forEach(e->System.out.println("CONTEXTS "+e));}
  var digest=java.security.MessageDigest.getInstance("SHA-256");
  try(var input=new java.security.DigestInputStream(Files.newInputStream(Path.of(a[0])),digest)){input.transferTo(java.io.OutputStream.nullOutputStream());}
  String hash=java.util.HexFormat.of().formatHex(digest.digest());
  Files.writeString(Path.of(a[2]),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(engine.report(hash,"partial_development_single_activity_probe",Map.of("scope","single selected Activity; not a full APK benchmark"))));
 }
}
