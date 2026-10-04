package org.example;
import java.util.*;
import static org.example.DexFlow.*;
/** Only literal writes to the actual freshly allocated static metadata map are folded. */
final class StaticMapConstants {
 static void populate(String field,Summary summary,CapabilityEngine.Job job,V actual,CapabilityEngine engine,CapabilityEngine.Host host){
  List<Write> roots=summary.writes().stream().filter(w->w.field().equals(field)&&w.receiver().kind().equals("static")).toList();
  if(roots.size()!=1||!roots.get(0).value().kind().equals("new")||!actual.kind().equals("object")||!Set.of("java.util.HashMap","java.util.LinkedHashMap").contains(actual.type()))return;
  V raw=roots.get(0).value();List<Write> entries=summary.writes().stream().filter(w->w.receiver().equals(raw)&&w.field().equals("$map_entry")).toList();
  if(entries.isEmpty()||host.maps.containsKey(actual.id()))return;
  if(summary.branched()||summary.truncated()||entries.size()>256||summary.writes().stream().anyMatch(w->w.receiver().equals(raw)&&w.field().startsWith("$map_mutation:"))){host.gaps.add("static_map_initializer_unresolved:"+field);return;}
  int instructions=0;for(var instruction:job.method().getImplementation().getInstructions())if(++instructions>4096){host.gaps.add("static_map_initializer_budget:"+field);return;}
  if(summary.writes().stream().anyMatch(w->!w.field().equals(field)&&!w.field().equals("$map_entry")&&contains(w.value(),raw,field))||summary.calls().stream().anyMatch(call->call.args().stream().anyMatch(arg->contains(arg,raw,field))&&!safeCall(call,raw))){host.gaps.add("static_map_initializer_escape_unresolved:"+field);return;}
  // Preflight the whole closed literal entry set: no arbitrary calls, fields, aliases,
  // or partially interpreted initializer bodies become strong metadata evidence.
  Map<String,V> values=new LinkedHashMap<>();for(Write write:entries){engine.refreshDeadline();V value=constant(write.value());if(value==null){host.gaps.add("static_map_initializer_value_unresolved:"+field);return;}String key=engine.mapKey(value.args().get(0));if(key==null||values.containsKey(key)){host.gaps.add("static_map_initializer_key_unresolved:"+field);return;}values.put(key,value.args().get(1));}
  engine.refreshDeadline();host.maps.put(actual.id(),values);host.heap.put(CapabilityEngine.heapKey(field,roots.get(0).receiver()),actual);host.constantMapSnapshots.add(actual.id());
 }
 static boolean contains(V value,V raw,String field){return value.equals(raw)||value.kind().equals("field")&&value.id().equals(field)||value.args().stream().anyMatch(arg->contains(arg,raw,field));}
 static boolean safeCall(Call call,V raw){
  if(call.args().isEmpty()||!call.args().get(0).equals(raw))return false;
  String prefix=CapabilityEngine.desc(raw.type())+"->";
  if(call.method().equals(prefix+"<init>()V"))return true;
  for(String type:List.of("java.util.Map","java.util.HashMap","java.util.LinkedHashMap"))if(call.method().equals(CapabilityEngine.desc(type)+"->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))return true;
  return false;
 }
 static V lookup(V expression,CapabilityEngine.Job job,CapabilityEngine.Host host,CapabilityEngine engine,int depth){
  if(depth>6||!expression.kind().startsWith("return")||expression.args().size()!=2||!expression.id().endsWith("->get(Ljava/lang/Object;)Ljava/lang/Object;")||!engine.idx.map(CapabilityEngine.owner(expression.id())))return null;
  V receiver=engine.priorityBinding(expression.args().get(0),job,host,0);
  if(receiver.kind().equals("field"))receiver=engine.eval(receiver,job,host,0,new HashSet<>());
  // Only a direct actual static field or an actual object alias participates; generic
  // method return expressions never trigger arbitrary body evaluation here.
  if(!receiver.kind().equals("object")||!host.constantMapSnapshots.contains(receiver.id())){
   V raw=expression.args().get(0);if(raw.kind().equals("field")&&raw.args().size()==1&&raw.args().get(0).kind().equals("static"))receiver=engine.eval(raw,job,host,0,new HashSet<>());
  }
  if(!receiver.kind().equals("object")||!host.constantMapSnapshots.contains(receiver.id()))return null;
  V key=engine.guardValue(expression.args().get(1),job,host,depth+1);String identity=engine.mapKey(key);if(identity==null)return null;
  V value=host.maps.getOrDefault(receiver.id(),Map.of()).get(identity);return value==null?V.literal("number","0"):value;
 }
 static V constant(V raw){
  if(raw.kind().equals("literal")||raw.kind().equals("class"))return raw;
  if(raw.kind().equals("map_entry")&&raw.args().size()==2){V key=constant(raw.args().get(0)),value=constant(raw.args().get(1));return key!=null&&key.kind().equals("literal")&&value!=null?expr("map_entry",null,"entry",List.of(key,value)):null;}
  if(raw.kind().startsWith("return")&&raw.id().equals(IntegerConstants.BOX)&&raw.args().size()==1){V number=constant(raw.args().get(0));V value=number==null?null:IntegerConstants.resolve(raw.id(),List.of(number));return value!=null&&!value.equals(UNKNOWN)?value:null;}
  return null;
 }
}
