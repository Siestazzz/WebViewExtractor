package org.example;
import java.util.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.iface.reference.*;
import static org.example.DexFlow.*;

/** Allocation-time field capture only; never delays arbitrary constructor execution. */
final class ConstructorCaptures {
 static void capture(V object,Call call,CapabilityEngine engine,CapabilityEngine.Job caller,CapabilityEngine.Host host,int depth,Set<String> visiting){
  Method constructor=engine.idx.resolve(call.method());if(constructor==null||constructor.getImplementation()==null)return;
  String key=object.id()+"::"+CapabilityIndex.key(constructor);
  if(host.allocationCaptures.contains(key))return;
  if(host.allocationCaptures.size()>=12000){host.gaps.add("constructor_capture_object_budget");return;}
  if(depth>10||System.nanoTime()>=Math.min(engine.deadline,host.localDeadline)){host.gaps.add("constructor_capture_budget");return;}
  host.allocationCaptures.add(key);
  List<V> args=new ArrayList<>();args.add(object);
  for(V expression:call.args().subList(1,call.args().size())){
   V value=UNKNOWN;
   if(argumentSafe(expression,caller,call.offset(),engine,host))value=engine.eval(expression,caller,host,depth+1,new HashSet<>(visiting));
   else host.gaps.add("constructor_capture_argument_order_unresolved:"+CapabilityIndex.key(constructor));
   args.add(freeze(value,host));
  }
  bind(constructor,args,object,caller,call.offset(),engine,host,depth,visiting,new HashSet<>());
 }
 static void bind(Method constructor,List<V> args,V object,CapabilityEngine.Job caller,int allocationOffset,CapabilityEngine engine,CapabilityEngine.Host host,int depth,Set<String> visiting,Set<String> constructors){
  if(depth>10||!constructors.add(CapabilityIndex.key(constructor))||constructor.getImplementation()==null)return;
  int count=0;for(var ignored:constructor.getImplementation().getInstructions())if(++count>500){host.gaps.add("constructor_capture_summary_budget:"+CapabilityIndex.key(constructor));return;}
  if(System.nanoTime()>=Math.min(engine.deadline,host.localDeadline)){host.gaps.add("constructor_capture_deadline");return;}
  var job=new CapabilityEngine.Job(constructor,args,caller.path(),true);Summary summary=engine.flow.summary(constructor,value->engine.guardValue(value,job,host,0));
  if(summary.truncated()){host.gaps.add("constructor_capture_summary_incomplete");return;}
  for(Call parent:summary.calls())if(CapabilityEngine.name(parent.method()).equals("<init>")&&!parent.args().isEmpty()&&parent.args().get(0).kind().equals("param")&&parent.args().get(0).id().equals("0")){
   Method target=engine.idx.resolve(parent.method());if(target==null||Objects.equals(target.getDefiningClass(),constructor.getDefiningClass()))continue;
   List<V> bound=new ArrayList<>();bound.add(object);for(V argument:parent.args().subList(1,parent.args().size()))bound.add(value(argument,args,object,caller,allocationOffset,engine,host,0));
   bind(target,bound,object,caller,allocationOffset,engine,host,depth+1,visiting,constructors);
  }
  // Capture only this-object assignments. Calls, static writes and allocations in bodies
  // are left to existing reached-constructor execution, never invented on field demand.
  boolean bodyEffects=summary.calls().stream().anyMatch(call->!CapabilityEngine.name(call.method()).equals("<init>")||call.args().isEmpty()||!call.args().get(0).kind().equals("param")||!call.args().get(0).id().equals("0"));
  Set<String> bodyFields=new HashSet<>();for(Write write:summary.writes())bodyFields.add(write.field());
  for(Write write:summary.writes())if(write.receiver().kind().equals("param")&&write.receiver().id().equals("0")){
   V captured=bodyEffects||readsWrittenField(write.value(),bodyFields)?UNKNOWN:value(write.value(),args,object,caller,allocationOffset,engine,host,0);
   if(bodyEffects||readsWrittenField(write.value(),bodyFields))host.gaps.add("constructor_capture_body_order_unresolved:"+CapabilityIndex.key(constructor));
   engine.applyWrite(host,write.field(),object,freeze(captured,host));
  }
 }
 static boolean readsWrittenField(V value,Set<String> writes){if(value.kind().equals("field")&&writes.contains(value.id()))return true;for(V arg:value.args())if(readsWrittenField(arg,writes))return true;return false;}
 static V value(V expression,List<V> args,V object,CapabilityEngine.Job caller,int allocationOffset,CapabilityEngine engine,CapabilityEngine.Host host,int depth){
  if(depth>10)return UNKNOWN;
  if(expression.kind().equals("param")){int index=Integer.parseInt(expression.id());return index<args.size()?args.get(index):UNKNOWN;}
  if(expression.kind().equals("literal")||expression.kind().equals("class"))return expression;
  if(expression.kind().equals("cast"))return value(expression.args().get(0),args,object,caller,allocationOffset,engine,host,depth+1);
  if(expression.kind().equals("field")&&expression.args().size()==1){
   V receiver=value(expression.args().get(0),args,object,caller,allocationOffset,engine,host,depth+1);
   if(!Set.of("object","host","view").contains(receiver.kind()))return UNKNOWN;
   if(!receiver.id().equals(object.id())&&!fieldOrderKnown(expression.id(),caller,allocationOffset,engine,host)){host.gaps.add("constructor_capture_body_field_order_unresolved:"+expression.id());return UNKNOWN;}
   V observed=host.heap.get(CapabilityEngine.heapKey(expression.id(),receiver));if(observed!=null)return freeze(observed,host);
   host.gaps.add("constructor_capture_field_unresolved:"+expression.id());return UNKNOWN;
  }
  if(expression.kind().equals("union")){V result=null;for(V branch:expression.args())result=union(result,value(branch,args,object,caller,allocationOffset,engine,host,depth+1));return result==null?UNKNOWN:result;}
  host.gaps.add("constructor_capture_expression_unmodeled:"+expression.kind());return UNKNOWN;
 }
 static boolean argumentSafe(V expression,CapabilityEngine.Job caller,int offset,CapabilityEngine engine,CapabilityEngine.Host host){
  if(Set.of("param","literal","class","new","unknown").contains(expression.kind()))return true;
  if(expression.kind().equals("cast"))return argumentSafe(expression.args().get(0),caller,offset,engine,host);
  if(expression.kind().equals("field"))return fieldOrderKnown(expression.id(),caller,offset,engine,host)&&expression.args().stream().allMatch(value->argumentSafe(value,caller,offset,engine,host));
  if(expression.kind().equals("union"))return expression.args().stream().allMatch(value->argumentSafe(value,caller,offset,engine,host));
  if(expression.kind().startsWith("return")&&FragmentTransactions.manager(expression.id())&&expression.args().size()==1&&expression.args().stream().allMatch(value->argumentSafe(value,caller,offset,engine,host))){
   List<V> receivers=expression.args().stream().map(value->engine.eval(value,caller,host,0,new HashSet<>())).toList();
   return engine.fragmentProtocolReceiver(receivers.get(0),CapabilityEngine.owner(expression.id()))&&engine.frameworkFragmentAccess(expression.id(),receivers,expression.kind().equals("return_super"));
  }
  return false;
 }
 static boolean fieldOrderKnown(String field,CapabilityEngine.Job caller,int offset,CapabilityEngine engine,CapabilityEngine.Host host){
  Summary summary=engine.flow.summary(caller.method());if(summary.branched()||summary.truncated())return false;
  int at=0;for(Instruction instruction:caller.method().getImplementation().getInstructions()){
   if(System.nanoTime()>=Math.min(engine.deadline,host.localDeadline))return false;
   if(instruction instanceof ReferenceInstruction reference&&reference.getReference() instanceof FieldReference target&&instruction.getOpcode().name.contains("put")&&CapabilityIndex.field(target).equals(field)&&at>offset)return false;
   at+=instruction.getCodeUnits();
  }
  // If a preceding call may write the captured field, symbolic field-load timing is
  // unavailable. Refuse that argument instead of reevaluating it after the call.
  for(Call call:summary.calls())if(call.offset()<offset&&mutationPossible(call.method(),field,engine,host))return false;
  return true;
 }
 static boolean mutationPossible(String root,String field,CapabilityEngine engine,CapabilityEngine.Host host){
  var pending=new ArrayDeque<String>();var seen=new HashSet<String>();pending.add(root);
  while(!pending.isEmpty()){
   if(seen.size()>=32||System.nanoTime()>=Math.min(engine.deadline,host.localDeadline)){host.gaps.add("constructor_capture_mutation_proof_budget");return true;}
   String key=pending.remove();if(!seen.add(key))continue;
   if(engine.idx.fieldWriters.getOrDefault(field,Set.of()).contains(key))return true;
   Method method=engine.idx.resolve(key);
   if(method!=null&&engine.idx.fieldWriters.getOrDefault(field,Set.of()).contains(CapabilityIndex.key(method)))return true;
   if(method!=null&&(method.getAccessFlags()&(8|16|2))==0){
    for(Method override:engine.idx.byShape.getOrDefault(CapabilityIndex.shape(method),List.of()))if(!CapabilityIndex.key(override).equals(CapabilityIndex.key(method))&&engine.idx.subtype(CapabilityIndex.cls(override.getDefiningClass()),CapabilityIndex.cls(method.getDefiningClass())))pending.add(CapabilityIndex.key(override));
   }
   if(method==null||method.getImplementation()==null){if(key.equals("Ljava/lang/Object;-><init>()V"))continue;return true;}
   pending.addAll(engine.idx.calls.getOrDefault(CapabilityIndex.key(method),Set.of()));
  }return false;
 }
 static V freeze(V value,CapabilityEngine.Host host){
  if(Set.of("unknown","field_object","field","return","return_super","return_direct").contains(value.kind())||value.kind().startsWith("return")){host.gaps.add("constructor_capture_argument_unresolved");return V.of("unknown",value.type(),"constructor_capture_frozen_unknown");}
  if(value.kind().equals("union")){V result=null;for(V branch:value.args())result=union(result,freeze(branch,host));return result==null?UNKNOWN:result;}
  return value;
 }
}
