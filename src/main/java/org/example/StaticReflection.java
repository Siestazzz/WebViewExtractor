package org.example;

import java.util.*;
import org.jf.dexlib2.iface.Method;
import static org.example.DexFlow.*;

/** Exact public static reflection at actual lookup and invoke sites; no class-name roots. */
final class StaticReflection {
 static final String LOOKUP="Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;";
 static final String INVOKE="Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
 static final Set<String> APIS=Set.of(LOOKUP,INVOKE);
 enum Reach { YES,NO,BUDGET }
 static boolean reachable(Method method,CapabilityEngine engine,CapabilityEngine.Host host){
  String root=CapabilityIndex.key(method);Reach cached=engine.staticReflectionReachable.get(root);if(cached!=null){if(cached==Reach.BUDGET)host.gaps.add("static_reflection_reachability_budget:"+root);return cached==Reach.YES;}
  engine.staticReflectionGraphQueries++;
  var pending=new ArrayDeque<String>();var seen=new HashSet<String>();pending.add(root);
  while(!pending.isEmpty()){
   if(System.nanoTime()>=Math.min(engine.deadline,host.localDeadline)){host.gaps.add("static_reflection_reachability_deadline");return false;}
   if(seen.size()>=128){host.gaps.add("static_reflection_reachability_budget:"+root);engine.staticReflectionReachable.put(root,Reach.BUDGET);return false;}
   String key=pending.remove();if(!seen.add(key))continue;
   for(String call:engine.idx.calls.getOrDefault(key,Set.of())){
    if(APIS.contains(call)){engine.staticReflectionReachable.put(root,Reach.YES);return true;}
    Method callee=engine.idx.resolve(call);if(callee!=null&&callee.getImplementation()!=null&&!CapabilityIndex.frameworkComponentImplementation(CapabilityIndex.cls(callee.getDefiningClass())))pending.add(CapabilityIndex.key(callee));
   }
  }
  engine.staticReflectionReachable.put(root,Reach.NO);return false;
 }
 static List<V> array(V array,CapabilityEngine engine,CapabilityEngine.Host host){
  Long size=host.arrayLengths.get(array.id());
  if(size==null||size<0||size>256){host.gaps.add("static_reflection_array_unresolved");return null;}
  Map<String,V> values=host.arrays.getOrDefault(array.id(),Map.of());if(values.containsKey("*")){host.gaps.add("static_reflection_array_partial");return null;}
  List<V> result=new ArrayList<>();for(int i=0;i<size;i++){
   if(System.nanoTime()>=Math.min(engine.deadline,host.localDeadline)){host.gaps.add("static_reflection_array_deadline");return null;}
   V value=values.get(String.valueOf(i));if(value==null){host.gaps.add("static_reflection_array_partial");return null;}result.add(value);
  }return result;
 }
 static V resolve(V expression,List<V> args,CapabilityEngine engine,CapabilityEngine.Job job,CapabilityEngine.Host host){
  if(!APIS.contains(expression.id()))return null;
  if(args.size()!=3)return UNKNOWN;V result=null;
  if(expression.id().equals(LOOKUP)){
   List<V> parameters=array(args.get(2),engine,host);if(parameters==null)return UNKNOWN;
   if(!parameters.isEmpty()){host.gaps.add("static_reflection_nonempty_signature_unmodeled");return UNKNOWN;}
   List<String> types=new ArrayList<>();for(V parameter:parameters){if(!parameter.kind().equals("class")||parameter.type()==null){host.gaps.add("static_reflection_parameter_types_unresolved");return UNKNOWN;}types.add(CapabilityEngine.desc(parameter.type()));}
   for(V type:alternatives(args.get(0)))for(V name:alternatives(args.get(1))){
    V methodValue=UNKNOWN;
    if(type.kind().equals("class")&&name.kind().equals("literal")&&"java.lang.String".equals(name.type())&&name.literal()!=null){
     List<Method> methods=engine.idx.hierarchyMethods(type.type()).stream().filter(m->m.getName().equals(name.literal())&&m.getParameterTypes().stream().map(Object::toString).toList().equals(types)&&(m.getAccessFlags()&9)==9).toList();
     if(methods.size()==1){
      Method method=methods.get(0);var declaration=engine.idx.classes.get(CapabilityIndex.cls(method.getDefiningClass()));
      if(declaration!=null&&(declaration.getAccessFlags()&1)!=0)methodValue=V.of("reflect_static_method","java.lang.reflect.Method",CapabilityIndex.key(method));
      else host.gaps.add("static_reflection_declaring_class_inaccessible:"+method.getDefiningClass());
     }
     else host.gaps.add("static_reflection_public_static_lookup_unresolved:"+type.type()+":"+name.literal());
    }else host.gaps.add("static_reflection_lookup_identity_unresolved");
    result=union(result,methodValue);
   }
  }else{
   List<V> arguments=array(args.get(2),engine,host);if(arguments==null)return UNKNOWN;
   for(V value:alternatives(args.get(0))){
    Method method=value.kind().equals("reflect_static_method")?engine.idx.resolve(value.id()):null;
    if(method==null||(method.getAccessFlags()&9)!=9||method.getParameterTypes().size()!=arguments.size()){host.gaps.add("static_reflection_invoke_unresolved");result=union(result,UNKNOWN);continue;}
    // Static invocation ignores its target argument. Actual lookup identity and exact arity
    // remain mandatory; setAccessible never expands the public/static contract.
    engine.enqueue(host,method,arguments,engine.extend(job.path(),"static_reflection_invoke:"+CapabilityIndex.key(method)),true);
    result=union(result,UNKNOWN);
   }
  }
  return result==null?UNKNOWN:result;
 }
}
