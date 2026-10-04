package org.example;

import java.util.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.*;
import static org.example.DexFlow.*;

/** Exact reflective dispatch certificates; discovery alone installs no transport. */
final class ReflectionProtocols {
 static final String INVOKE="Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
 static final Set<String> LOOKUPS=Set.of("Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;","Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
 static final Set<String> ANNOTATIONS=Set.of("Ljava/lang/reflect/Method;->getAnnotation(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;","Ljava/lang/reflect/Method;->isAnnotationPresent(Ljava/lang/Class;)Z");
 record Plan(int offset,V method,V lookupClass,V name,V parameterArray,V invokeReceiver,V invokeArray,V annotationClass,boolean declared,List<V> accessibleFlags){}
 final CapabilityIndex index;final DexFlow flow;final java.util.function.LongSupplier deadline;
 final Map<String,List<Plan>> cache=new HashMap<>();final Map<String,Boolean> reachability=new HashMap<>();
 final Set<String> reported=new HashSet<>();
 void gap(String reason){if(reported.size()>=128){if(reported.add("transport_protocol_diagnostic_budget"))index.diagnostics.add("transport_protocol_diagnostic_budget");return;}if(reported.add(reason))index.diagnostics.add(reason);}
 boolean expired(){if(System.nanoTime()<Math.min(flow.deadline,deadline.getAsLong()))return false;gap("transport_protocol_deadline");return true;}
 static final class Expired extends RuntimeException {}
 void check(){if(expired())throw new Expired();}
 long discoveryMethodVisits;
 boolean fieldsDiscovered;final Set<String> fields=new HashSet<>();final Map<String,List<TransportProtocols.Registration>> registrations=new HashMap<>();
 ReflectionProtocols(CapabilityIndex index,DexFlow flow){this(index,flow,()->flow.deadline);}
 ReflectionProtocols(CapabilityIndex index,DexFlow flow,java.util.function.LongSupplier deadline){this.index=index;this.flow=flow;this.deadline=deadline;}
 boolean candidate(Method method){if(method==null)return false;Set<String> calls=index.calls.getOrDefault(CapabilityIndex.key(method),Set.of());return calls.contains(INVOKE)&&calls.stream().anyMatch(LOOKUPS::contains)&&calls.stream().anyMatch(ANNOTATIONS::contains);}
 List<Plan> plans(Method method){
  if(method==null||!candidate(method))return List.of();String id=CapabilityIndex.key(method);var old=cache.get(id);if(old!=null)return old;
  if(expired())return List.of();
  Summary summary=flow.summary(method);if(summary.truncated()){gap("transport_protocol_summary_incomplete:"+id);return List.of();}Graph graph;try{graph=new Graph(method,this::check);}catch(Expired exhausted){return List.of();}List<Plan> result=new ArrayList<>();
  for(Call invoke:summary.calls()){
   if(expired())return List.of();
   if(!invoke.method().equals(INVOKE)||invoke.args().size()!=3)continue;
   V selected=TransportProtocols.unwrap(invoke.args().get(0));if(!selected.kind().startsWith("return")||!LOOKUPS.contains(selected.id())||selected.args().size()!=3)continue;
   for(Call annotation:summary.calls()){
    if(expired())return List.of();
    if(!ANNOTATIONS.contains(annotation.method())||annotation.args().size()!=2||!TransportProtocols.unwrap(annotation.args().get(0)).equals(selected)||!safeGate(graph,annotation.offset(),invoke.offset()))continue;
    List<V> access=new ArrayList<>();for(Call flag:summary.calls())if(flag.method().equals("Ljava/lang/reflect/AccessibleObject;->setAccessible(Z)V")||flag.method().equals("Ljava/lang/reflect/Method;->setAccessible(Z)V")){
     if(flag.args().size()==2&&TransportProtocols.unwrap(flag.args().get(0)).equals(selected)&&safeDominates(graph,flag.offset(),invoke.offset()))access.add(flag.args().get(1));
    }
    result.add(new Plan(invoke.offset(),selected,selected.args().get(0),selected.args().get(1),selected.args().get(2),invoke.args().get(1),invoke.args().get(2),annotation.args().get(1),selected.id().contains("->getDeclaredMethod("),List.copyOf(access)));
   }
  }
  cache.put(id,List.copyOf(result));return cache.get(id);
 }
 boolean safeDominates(Graph graph,int start,int end){try{return graph.dominates(start,end);}catch(Expired exhausted){return false;}}
 boolean safeGate(Graph graph,int annotation,int invoke){try{return graph.gates(annotation,invoke);}catch(Expired exhausted){return false;}}
 boolean reachable(Method method){
  if(method==null)return false;String id=CapabilityIndex.key(method);var old=reachability.get(id);if(old!=null)return old;
  boolean result=reachable(id,new HashSet<>(),0);if(!expired())reachability.put(id,result);return result;
 }
 boolean reachable(String id,Set<String> seen,int depth){
  if(expired())return false;Method method=index.methods.get(id);if(method!=null&&!plans(method).isEmpty())return true;
  if(depth>=8||seen.size()>=128){gap("transport_reachability_budget:"+id);return false;}if(!seen.add(id))return false;
  for(String target:index.calls.getOrDefault(id,Set.of()))if(reachable(target,seen,depth+1))return true;return false;
 }
 boolean carrier(String type){for(Method method:index.byClass.getOrDefault(type,List.of()))if(!plans(method).isEmpty())return true;return false;}
 void discoverFields(){
  if(fieldsDiscovered)return;fieldsDiscovered=true;
  // Every accepted dispatch certificate contains this exact Method.invoke call.
  // Its existing reverse-call index is a complete necessary candidate set; avoid
  // re-enumerating every unrelated APK method in each worker's discovery pass.
  for(String id:index.callers.getOrDefault(INVOKE,Set.of())){
   if(expired()){fieldsDiscovered=false;return;}
   Method method=index.methods.get(id);if(method==null||!candidate(method))continue;
   discoveryMethodVisits++;
   for(Plan plan:plans(method)){collectMapFields(plan.lookupClass(),0);collectMapFields(plan.invokeReceiver(),0);}
  }
  for(Method method:index.methods.values()){
   if(expired()){fieldsDiscovered=false;return;}
   if(method.getImplementation()==null||Collections.disjoint(index.referencedFields.getOrDefault(CapabilityIndex.key(method),Set.of()),fields))continue;boolean put=false;
   for(String call:index.calls.getOrDefault(CapabilityIndex.key(method),Set.of())){if(expired()){fieldsDiscovered=false;return;}if(call.endsWith("->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")&&index.map(CapabilityEngine.owner(call))){put=true;break;}}
   if(!put)continue;
   discoveryMethodVisits++;var forms=TransportProtocols.registrations(index,method,flow.summary(method),fields);if(!forms.isEmpty())registrations.put(CapabilityIndex.key(method),forms);
  }
 }
 void collectMapFields(V value,int depth){if(expired())return;if(depth>12){gap("transport_field_expression_budget");return;}if(value.kind().equals("field")&&index.map(value.type()))fields.add(value.id());for(V child:value.args())collectMapFields(child,depth+1);}
 boolean writer(Method method){
  if(method==null)return false;
  boolean put=false;
  for(String call:index.calls.getOrDefault(CapabilityIndex.key(method),Set.of())){
   if(!call.endsWith("->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))continue;
   if(expired())return false;
   if(index.map(CapabilityEngine.owner(call))){put=true;break;}
  }
  if(!put)return false;
  discoverFields();return registrations.containsKey(CapabilityIndex.key(method));
 }
 boolean mapField(String field){discoverFields();return fields.contains(field);}

 /** Structural CFG proof: positive annotation branch is necessary to reach invoke. */
 static final class Graph {
  final List<Instruction> instructions=new ArrayList<>();final List<Integer> offsets=new ArrayList<>();final Map<Integer,Integer> positions=new HashMap<>();final List<Set<Integer>> edges=new ArrayList<>();
  final Runnable deadlineCheck;
  Graph(Method method,Runnable deadlineCheck){
   this.deadlineCheck=deadlineCheck;
   if(method.getImplementation()==null)return;int offset=0;for(Instruction instruction:method.getImplementation().getInstructions()){deadlineCheck.run();positions.put(offset,instructions.size());offsets.add(offset);instructions.add(instruction);edges.add(new LinkedHashSet<>());offset+=instruction.getCodeUnits();}
   for(int i=0;i<instructions.size();i++){deadlineCheck.run();
    var instruction=instructions.get(i);String op=instruction.getOpcode().name;
    if(instruction instanceof OffsetInstruction jump){
     int target=offsets.get(i)+jump.getCodeOffset();Integer position=positions.get(target);
     if(op.startsWith("goto")||op.startsWith("if-")){if(position!=null)edges.get(i).add(position);}
     if((op.equals("packed-switch")||op.equals("sparse-switch"))&&position!=null&&instructions.get(position) instanceof SwitchPayload payload)
      for(var element:payload.getSwitchElements()){Integer branch=positions.get(offsets.get(i)+element.getOffset());if(branch!=null)edges.get(i).add(branch);}
    }
    if(!op.startsWith("goto")&&instruction.getOpcode().canContinue()&&i+1<instructions.size())edges.get(i).add(i+1);
   }
   for(var block:method.getImplementation().getTryBlocks())for(int i=0;i<instructions.size();i++)if(offsets.get(i)>=block.getStartCodeAddress()&&offsets.get(i)<block.getStartCodeAddress()+block.getCodeUnitCount())
    for(var handler:block.getExceptionHandlers()){deadlineCheck.run();Integer target=positions.get(handler.getHandlerCodeAddress());if(target!=null)edges.get(i).add(target);}
  }
  boolean reaches(int start,int target,int blocked){
   if(start<0||target<0)return false;ArrayDeque<Integer> pending=new ArrayDeque<>();Set<Integer> seen=new HashSet<>();pending.add(start);
   while(!pending.isEmpty()){deadlineCheck.run();int node=pending.remove();if(node==blocked||!seen.add(node))continue;if(node==target)return true;pending.addAll(edges.get(node));}return false;
  }
  boolean dominates(int offset,int target){Integer start=positions.get(offset),end=positions.get(target);return start!=null&&end!=null&&reaches(0,end,-1)&&!reaches(0,end,start);}
  boolean gates(int annotationOffset,int invokeOffset){
   Integer annotation=positions.get(annotationOffset),invoke=positions.get(invokeOffset);if(annotation==null||invoke==null||annotation+1>=instructions.size())return false;
   var move=instructions.get(annotation+1);if(!move.getOpcode().name.startsWith("move-result")||!(move instanceof OneRegisterInstruction first))return false;
   Set<Integer> registers=new HashSet<>();registers.add(first.getRegisterA());
   for(int i=annotation+2;i<Math.min(instructions.size(),annotation+8);i++){
    var instruction=instructions.get(i);String op=instruction.getOpcode().name;
    if((op.equals("if-eqz")||op.equals("if-nez"))&&instruction instanceof OneRegisterInstruction register&&registers.contains(register.getRegisterA())&&instruction instanceof OffsetInstruction jump){
     Integer target=positions.get(offsets.get(i)+jump.getCodeOffset());int fall=i+1;if(target==null||fall>=instructions.size())return false;
     int positive=op.equals("if-eqz")?fall:target,negative=op.equals("if-eqz")?target:fall;
     return dominates(offsets.get(i),invokeOffset)&&reaches(positive,invoke,-1)&&!reaches(negative,invoke,i)&&!reaches(negative,invoke,-1);
    }
    if(op.startsWith("if-")||op.startsWith("goto")||op.startsWith("invoke-")||op.startsWith("return"))return false;
    if(op.startsWith("move")&&instruction instanceof TwoRegisterInstruction register){if(registers.contains(register.getRegisterB()))registers.add(register.getRegisterA());else registers.remove(register.getRegisterA());}
    else if(!op.equals("check-cast")&&instruction.getOpcode().setsRegister()&&instruction instanceof OneRegisterInstruction register)registers.remove(register.getRegisterA());
   }return false;
  }
 }
}
