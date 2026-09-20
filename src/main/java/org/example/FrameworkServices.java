package org.example;

import java.util.*;
import static org.example.DexFlow.*;

/** Framework protocol rules bind registered implementations, never arbitrary subtypes. */
final class FrameworkServices {
    static final String REGISTER="Lcom/tencent/news/qnrouter/service/ServiceMap;->autoRegister(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APIMeta;)V";
    static final String META="Lcom/tencent/news/qnrouter/service/APIMeta;-><init>(Ljava/lang/Class;Ljava/lang/Class;Z)V";
    static final Set<String> LOOKUPS=Set.of(
        "Lcom/tencent/news/qnrouter/service/Services;->call(Ljava/lang/Class;)Ljava/lang/Object;",
        "Lcom/tencent/news/qnrouter/service/Services;->get(Ljava/lang/Class;)Ljava/lang/Object;",
        "Lcom/tencent/news/qnrouter/service/Services;->call(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;",
        "Lcom/tencent/news/qnrouter/service/Services;->get(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APICreator;)Ljava/lang/Object;");
    record Binding(String api,String qualifier,String implementation,boolean singleton,String site){}
    final Map<String,List<Binding>> bindings=new HashMap<>();
    void index(CapabilityIndex idx,long deadline){
        DexFlow flow=new DexFlow(idx,deadline);
        for(var entry:idx.calls.entrySet()){
            if(!entry.getValue().contains(REGISTER))continue;
            var method=idx.methods.get(entry.getKey());if(method==null)continue;
            var summary=flow.summary(method);
            if(summary.truncated())idx.diagnostics.add("service_registration_flow_budget:"+entry.getKey());
            for(Call register:summary.calls())if(register.method().equals(REGISTER)&&register.args().size()==3){
                V api=register.args().get(0),qualifier=register.args().get(1),meta=register.args().get(2);boolean resolved=false;
                if(api.kind().equals("class")&&qualifier.literal()!=null)
                    for(Call ctor:summary.calls())if(ctor.method().equals(META)&&ctor.args().size()==4&&alternatives(meta).contains(ctor.args().get(0))){
                        V implementation=ctor.args().get(2);
                        if(!implementation.kind().equals("class"))continue;
                        if(!idx.classes.containsKey(implementation.type())||!idx.subtype(implementation.type(),api.type()))continue;
                        String site=entry.getKey()+"@"+register.offset();
                        Binding binding=new Binding(api.type(),qualifier.literal(),implementation.type(),"1".equals(ctor.args().get(3).literal()),site);
                        var values=bindings.computeIfAbsent(api.type(),k->new ArrayList<>());if(!values.contains(binding))values.add(binding);resolved=true;
                        // A registered service method is a concrete dispatch target for that API.
                        for(var implemented:idx.byClass.getOrDefault(implementation.type(),List.of()))
                            for(var declaration:idx.hierarchyMethods(api.type()))if(CapabilityIndex.shape(implemented).equals(CapabilityIndex.shape(declaration)))
                                idx.callers.computeIfAbsent(CapabilityIndex.key(implemented),k->new HashSet<>()).addAll(idx.callers.getOrDefault(CapabilityIndex.key(declaration),Set.of()));
                    }
                if(!resolved)idx.diagnostics.add("unresolved_service_registration:"+entry.getKey()+"@"+register.offset());
            }
        }
    }
    List<Binding> matchingBindings(String method,List<V> args,String context){
        V value=lookup(method,args,context);if(value==null)return List.of();
        return bindings.getOrDefault(args.get(0).type(),List.of()).stream().filter(b->alternatives(value).stream().anyMatch(v->v.id().startsWith("registered_service:"+b.site()+":"+b.qualifier()))).toList();
    }
    V lookup(String method,List<V> args,String context){
        if(!LOOKUPS.contains(method)||args.isEmpty()||!args.get(0).kind().equals("class"))return null;
        // The resolver normalizes null/empty LOOKUP names, but registration keys stay exact.
        // An unresolved custom creator can replace the registered implementation.
        if(args.size()>2&&!(args.get(2).kind().equals("literal")&&"0".equals(args.get(2).literal())))
            return V.of("unknown",args.get(0).type(),"unresolved_service_creator:"+method);
        String qualifier=args.size()==1?"_default_impl_":args.get(1).literal();
        if(args.size()>1&&args.get(1).kind().equals("literal")&&("".equals(qualifier)||"0".equals(qualifier)&&!"java.lang.String".equals(args.get(1).type())))qualifier="_default_impl_";
        V result=null;
        for(Binding binding:bindings.getOrDefault(args.get(0).type(),List.of())){
            if(qualifier!=null&&!qualifier.equals(binding.qualifier()))continue;
            String id="registered_service:"+binding.site()+":"+binding.qualifier()+(binding.singleton()?"":":"+context);
            result=union(result,V.of("object",binding.implementation(),id));
        }
        return result;
    }
}
