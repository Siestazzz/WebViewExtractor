package org.example;

import java.util.*;
import org.jf.dexlib2.iface.Method;
import static org.example.DexFlow.*;

/** Actual, resolved reflection construction; no global class or service enumeration. */
final class ReflectiveFactories {
    static final String FOR_NAME="Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;";
    static final String CLASS_NEW="Ljava/lang/Class;->newInstance()Ljava/lang/Object;";
    static final String CONSTRUCTORS="Ljava/lang/Class;->getDeclaredConstructors()[Ljava/lang/reflect/Constructor;";
    static final String CONSTRUCTOR_NEW="Ljava/lang/reflect/Constructor;->newInstance([Ljava/lang/Object;)Ljava/lang/Object;";
    static final Set<String> APIS=Set.of(FOR_NAME,CLASS_NEW,CONSTRUCTORS,CONSTRUCTOR_NEW);
    static V resolve(V expression,List<V> args,CapabilityEngine engine,CapabilityEngine.Job job,CapabilityEngine.Host host,int depth,Set<String> visiting){
        if(!APIS.contains(expression.id()))return null;
        if(args.isEmpty())return UNKNOWN;
        V result=null;
        for(V receiver:alternatives(args.get(0))){
            V value=UNKNOWN;
            if(expression.id().equals(FOR_NAME)&&args.size()==1){
                if(receiver.literal()!=null&&engine.idx.classes.containsKey(receiver.literal()))value=V.of("class",receiver.literal(),CapabilityEngine.desc(receiver.literal()));
                else host.gaps.add("reflective_class_name_unresolved:"+CapabilityIndex.key(job.method()));
            }else if(expression.id().equals(CLASS_NEW)&&args.size()==1&&receiver.kind().equals("class")){
                Method ctor=engine.idx.byClass.getOrDefault(receiver.type(),List.of()).stream().filter(m->m.getName().equals("<init>")&&m.getParameterTypes().isEmpty()).findFirst().orElse(null);
                value=create(expression,receiver.type(),ctor,engine,job,host,depth,visiting);
            }else if(expression.id().equals(CONSTRUCTORS)&&args.size()==1&&receiver.kind().equals("class")){
                List<Method> constructors=engine.idx.byClass.getOrDefault(receiver.type(),List.of()).stream().filter(m->m.getName().equals("<init>")).toList();
                // Reflection array order is unspecified. Only a singleton is exact.
                if(constructors.size()==1){
                    value=V.of("object","[Ljava/lang/reflect/Constructor;","reflective_constructors:"+receiver.type()+"|"+engine.allocationContext(job));
                    host.arrayLengths.put(value.id(),1L);
                    engine.applyWrite(host,"$element:0",value,V.of("reflect_constructor",receiver.type(),CapabilityIndex.key(constructors.get(0))));
                }else host.gaps.add("reflective_constructor_array_order_unresolved:"+receiver.type());
            }else if(expression.id().equals(CONSTRUCTOR_NEW)&&args.size()==2&&receiver.kind().equals("reflect_constructor")){
                V arguments=args.get(1);
                if(Long.valueOf(0).equals(host.arrayLengths.get(arguments.id())))value=create(expression,receiver.type(),engine.idx.resolve(receiver.id()),engine,job,host,depth,visiting);
                else host.gaps.add("reflective_constructor_arguments_unresolved:"+receiver.type());
            }else host.gaps.add("reflective_factory_receiver_unresolved:"+expression.id());
            result=union(result,value);
        }
        return result==null?UNKNOWN:result;
    }
    static V create(V expression,String type,Method ctor,CapabilityEngine engine,CapabilityEngine.Job job,CapabilityEngine.Host host,int depth,Set<String> visiting){
        var declaration=engine.idx.classes.get(type);
        if(declaration==null||(declaration.getAccessFlags()&1)==0||(declaration.getAccessFlags()&(0x200|0x400))!=0||ctor==null||(ctor.getAccessFlags()&1)==0||!ctor.getParameterTypes().isEmpty()){
            host.gaps.add("reflective_public_noarg_constructor_unresolved:"+type);return UNKNOWN;
        }
        V object=V.of("object",type,"reflective_factory:"+CapabilityIndex.key(job.method())+"@"+expression.kind()+":"+expression.id()+"|"+engine.allocationContext(job)+"|"+type);
        engine.materializeConstructor(ctor,List.of(object),job,host,depth+1,new HashSet<>(visiting));
        return object;
    }
}
