package org.example;
import java.util.*;
import org.jf.dexlib2.iface.Method;
import static org.example.DexFlow.*;

/** Manifest Application entry and a read-only reachable static-object snapshot. */
final class ApplicationBootstrap {
 record State(Map<String,V> heap,Map<String,Map<String,V>> maps,Map<String,Map<String,V>> arrays,Map<String,Set<V>> contents,Map<String,Long> lengths,Set<String> constructed,List<Map<String,Object>> facts,List<String> diagnostics) {
  static State empty(){return new State(Map.of(),Map.of(),Map.of(),Map.of(),Map.of(),Set.of(),List.of(),List.of());}
  int entryCount(){return heap.size()+maps.values().stream().mapToInt(Map::size).sum()+arrays.values().stream().mapToInt(Map::size).sum()+contents.values().stream().mapToInt(Set::size).sum();}
  void install(CapabilityEngine.Host host){
   if(heap.isEmpty()&&maps.isEmpty()&&arrays.isEmpty()&&contents.isEmpty())return;
   host.heap.putAll(heap);maps.forEach((key,value)->host.maps.put(key,new HashMap<>(value)));arrays.forEach((key,value)->host.arrays.put(key,new HashMap<>(value)));contents.forEach((key,value)->host.contents.put(key,new LinkedHashSet<>(value)));host.arrayLengths.putAll(lengths);host.constructed.addAll(constructed);

  }
 }
 static State build(CapabilityEngine engine){
  long bootstrapStart=System.nanoTime();String type=engine.apk.applicationName;if(type.isEmpty())return State.empty();
  var host=engine.new Host("application:"+type);host.applicationStartup=true;List<String> path=List.of("manifest_application:"+type);
  if(!engine.idx.classes.containsKey(type)||!engine.idx.subtype(type,"android.app.Application"))return diagnosed("application_bootstrap_type_unresolved:"+type);
  var declaration=engine.idx.classes.get(type);
  if((declaration.getAccessFlags()&1)==0||(declaration.getAccessFlags()&(0x200|0x400))!=0)return diagnosed("application_bootstrap_class_not_constructible:"+type);
  Method constructor=engine.idx.byClass.getOrDefault(type,List.of()).stream().filter(method->method.getName().equals("<init>")&&method.getParameterTypes().isEmpty()&&(method.getAccessFlags()&1)!=0&&method.getImplementation()!=null).findFirst().orElse(null);
  if(constructor==null)return diagnosed("application_bootstrap_public_noarg_constructor_missing:"+type);
  V application=V.of("object",type,"application:"+engine.apk.packageName+":"+type);long finish=Math.min(engine.deadline,bootstrapStart+1_000_000_000L);
  long stop=executionDeadline(bootstrapStart,finish);host.localDeadline=stop;
  // Each exact lifecycle root is completed before the next, preserving attach-before-create.
  for(String shape:List.of("<init>()V","attachBaseContext(Landroid/content/Context;)V","onCreate()V")){
   Method entry=shape.startsWith("<init>")?constructor:engine.idx.resolve(CapabilityEngine.desc(type)+"->"+shape);
   if(entry==null||entry.getImplementation()==null)continue;
   List<V> args=new ArrayList<>();args.add(application);
   if(shape.startsWith("attachBaseContext"))args.add(application);
   engine.enqueue(host,entry,args,path,true);
   while(!host.queue.isEmpty()&&host.visited.size()<12000&&System.nanoTime()<stop){engine.currentHost=host;try{engine.processJob(host);}finally{engine.currentHost=null;}}
   if(!host.queue.isEmpty()){host.gaps.add(System.nanoTime()>=engine.deadline?"application_bootstrap_deadline":host.visited.size()>=12000?"application_bootstrap_context_budget":"application_bootstrap_time_budget");break;}
  }
  return snapshot(host,type,bootstrapStart,finish);
 }
 // Keep ten percent (at most 100 ms) of the existing one-second total for copying
 // already executed static writes. Traversal exhaustion must not erase those writes.
 static long executionDeadline(long start,long finish){return finish-Math.min(100_000_000L,Math.max(0,finish-start)/10);}
 static State snapshot(CapabilityEngine.Host host,String type,long bootstrapStart,long stop){
  host.localDeadline=stop;
  Set<String> reachable=new HashSet<>();Set<V> seenValues=Collections.newSetFromMap(new IdentityHashMap<>());Map<String,V> heap=new HashMap<>();Map<String,Map<String,V>> fieldsByReceiver=new HashMap<>();
  for(var entry:host.heap.entrySet()){
   if(System.nanoTime()>=stop){host.gaps.add("application_bootstrap_static_closure_budget");break;}
   int split=entry.getKey().lastIndexOf("::");if(split<0)continue;String field=entry.getKey().substring(split+2),receiver=entry.getKey().substring(0,split);
   fieldsByReceiver.computeIfAbsent(receiver,key->new HashMap<>()).put(entry.getKey(),entry.getValue());
   if(Objects.equals(receiver,CapabilityEngine.desc(CapabilityEngine.owner(field)))){heap.put(entry.getKey(),entry.getValue());collect(entry.getValue(),reachable,seenValues,host);}
  }
  Map<String,Map<String,V>> maps=new HashMap<>(),arrays=new HashMap<>();Map<String,Set<V>> contents=new HashMap<>();Map<String,Long> lengths=new HashMap<>();
  Set<String> scanned=new HashSet<>();boolean changed=true;
  while(changed){changed=false;
   if(System.nanoTime()>=stop||reachable.size()>12000||seenValues.size()>12000){host.gaps.add("application_bootstrap_static_closure_budget");break;}
   for(String object:new ArrayList<>(reachable))if(scanned.add(object)){
    changed=true;for(var entry:fieldsByReceiver.getOrDefault(object,Map.of()).entrySet()){heap.put(entry.getKey(),entry.getValue());collect(entry.getValue(),reachable,seenValues,host);}
    if(host.maps.containsKey(object)){var entries=host.maps.get(object);maps.put(object,Map.copyOf(entries));entries.values().forEach(value->collect(value,reachable,seenValues,host));}
    if(host.arrays.containsKey(object)){var entries=host.arrays.get(object);arrays.put(object,Map.copyOf(entries));entries.values().forEach(value->collect(value,reachable,seenValues,host));}
    if(host.contents.containsKey(object)){var entries=host.contents.get(object);contents.put(object,Set.copyOf(entries));entries.forEach(value->collect(value,reachable,seenValues,host));}
    if(host.arrayLengths.containsKey(object))lengths.put(object,host.arrayLengths.get(object));
   }
  }
  List<Map<String,Object>> facts=new ArrayList<>();for(var fact:host.facts.values()){Map<String,Object> copy=new LinkedHashMap<>(fact);copy.remove("activity");copy.put("application",type);facts.add(Collections.unmodifiableMap(copy));
   if(fact.get("webview") instanceof Map<?,?> webview&&reachable.contains(String.valueOf(webview.get("id"))))host.gaps.add("application_bootstrap_shared_webview_surface_unmodeled");}
  List<String> diagnostics=new ArrayList<>();diagnostics.add("application_bootstrap:"+type+":analysis_seconds="+(System.nanoTime()-bootstrapStart)/1e9);diagnostics.add("application_bootstrap:"+type+":contexts="+host.visited.size()+":static_heap="+heap.size());host.gaps.stream().sorted().map(gap->"application_bootstrap:"+type+":"+gap).forEach(diagnostics::add);
  Set<String> constructed=new HashSet<>(host.constructed);constructed.retainAll(reachable);
  return new State(Map.copyOf(heap),Map.copyOf(maps),Map.copyOf(arrays),Map.copyOf(contents),Map.copyOf(lengths),Set.copyOf(constructed),List.copyOf(facts),List.copyOf(diagnostics));
 }
 static void collect(V value,Set<String> objects,Set<V> seen,CapabilityEngine.Host host){var queue=new ArrayDeque<V>();queue.add(value);while(!queue.isEmpty()){if(System.nanoTime()>=host.localDeadline||seen.size()>=12000){host.gaps.add("application_bootstrap_static_closure_budget");return;}V item=queue.remove();if(!seen.add(item))continue;if(Set.of("object","view","host","field_object").contains(item.kind()))objects.add(item.id());queue.addAll(item.args());}}
 static State diagnosed(String diagnostic){return new State(Map.of(),Map.of(),Map.of(),Map.of(),Map.of(),Set.of(),List.of(),List.of(diagnostic));}
}
