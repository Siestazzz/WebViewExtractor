package org.example;

import java.util.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.iface.value.*;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.iface.instruction.formats.*;

/** Bounded, flow-sensitive symbolic register propagation with explicit unknowns. */
final class DexFlow {
    record V(String kind,String type,String id,String literal,List<V> args) {
        static V of(String k,String t,String id){return new V(k,t,id,null,List.of());}
        static V literal(String t,String value){return new V("literal",t,value,value,List.of());}
    }
    record Call(String method,int offset,List<V> args,boolean isStatic) {}
    record Write(String field,V receiver,V value) {}
    record Summary(List<Call> calls,List<Write> writes,List<V> returns,boolean branched,boolean truncated) {}
    static final V UNKNOWN=V.of("unknown",null,"unknown");
    final CapabilityIndex idx;
    final Map<String,Summary> cache=new HashMap<>();
    final long deadline;
    int decoded;
    DexFlow(CapabilityIndex i,long deadline){idx=i;this.deadline=deadline;}
    static V union(V a,V b){
        if(a==null)return b;if(b==null||a.equals(b))return a;
        if(a.equals(UNKNOWN)||b.equals(UNKNOWN))return UNKNOWN;
        Set<V> vs=new LinkedHashSet<>();if(a.kind.equals("union"))vs.addAll(a.args);else vs.add(a);if(b.kind.equals("union"))vs.addAll(b.args);else vs.add(b);
        if(vs.size()>12)return UNKNOWN;
        return new V("union",Objects.equals(a.type,b.type)?a.type:null,"union",null,List.copyOf(vs));
    }
    static List<V> alternatives(V v){return v.kind.equals("union")?v.args:List.of(v);}
    static V expr(String kind,String type,String id,List<V> args){
        if(args.stream().anyMatch(v->depth(v)>6))return V.of("unknown",type,"expression_depth");
        return new V(kind,type,id,null,List.copyOf(args));
    }
    static int depth(V v){return v.args.isEmpty()?0:1+v.args.stream().mapToInt(DexFlow::depth).max().orElse(0);}
    Summary summary(Method m){
        String key=CapabilityIndex.key(m);Summary old=cache.get(key);if(old!=null)return old;
        Summary result=decode(m);cache.put(key,result);decoded++;return result;
    }
    Summary decode(Method m){
        MethodImplementation impl=m.getImplementation();if(impl==null)return new Summary(List.of(),List.of(),List.of(),false,false);
        List<Instruction> ins=new ArrayList<>();List<Integer> offsets=new ArrayList<>();Map<Integer,Integer> positions=new HashMap<>();int off=0;
        for(Instruction i:impl.getInstructions()){positions.put(off,ins.size());offsets.add(off);ins.add(i);off+=i.getCodeUnits();}
        if(ins.isEmpty())return new Summary(List.of(),List.of(),List.of(),false,false);
        String key=CapabilityIndex.key(m);Map<Integer,V> init=new HashMap<>();int words=(m.getAccessFlags()&8)==0?1:0;
        for(CharSequence p:m.getParameterTypes())words+=p.toString().equals("J")||p.toString().equals("D")?2:1;
        int reg=impl.getRegisterCount()-words,index=0;
        if((m.getAccessFlags()&8)==0)init.put(reg++,V.of("param",CapabilityIndex.cls(m.getDefiningClass()),"0"));
        else index=-1;
        for(CharSequence p:m.getParameterTypes()){
            init.put(reg,V.of("param",CapabilityIndex.cls(p.toString()),String.valueOf(++index)));reg+=p.toString().equals("J")||p.toString().equals("D")?2:1;
        }
        List<Map<Integer,V>> states=new ArrayList<>(Collections.nCopies(ins.size(),null));states.set(0,init);
        ArrayDeque<Integer> work=new ArrayDeque<>();work.add(0);int[] visits=new int[ins.size()];
        Map<Integer,Call> calls=new TreeMap<>();Set<Write> writes=new LinkedHashSet<>();V returns=null;boolean branched=false,truncated=false;int steps=0;
        // Exception entries are merged from each protected instruction, rather than assumed unreachable.
        Map<Integer,List<Integer>> handlers=new HashMap<>();
        for(var block:impl.getTryBlocks())for(int j=0;j<ins.size();j++)if(offsets.get(j)>=block.getStartCodeAddress()&&offsets.get(j)<block.getStartCodeAddress()+block.getCodeUnitCount()){
            for(var h:block.getExceptionHandlers()){Integer pos=positions.get(h.getHandlerCodeAddress());if(pos!=null)handlers.computeIfAbsent(j,k->new ArrayList<>()).add(pos);}
        }
        while(!work.isEmpty()){
            if(++steps>100000||System.nanoTime()>deadline){truncated=true;break;}
            int pc=work.remove();if(++visits[pc]>24){truncated=true;continue;}
            var before=states.get(pc);Map<Integer,V> s=new HashMap<>(before);Instruction in=ins.get(pc);String op=in.getOpcode().name;int at=offsets.get(pc);
            V pending=s.remove(-1);int a=in instanceof OneRegisterInstruction one?one.getRegisterA():-2;
            V output=null;
            if(op.startsWith("move-result"))output=pending==null?UNKNOWN:pending;
            else if(op.startsWith("move-exception"))output=V.of("unknown","java.lang.Throwable","exception");
            else if(op.startsWith("move")&&in instanceof TwoRegisterInstruction two)output=s.getOrDefault(two.getRegisterB(),UNKNOWN);
            else if(op.startsWith("const-string")&&in instanceof ReferenceInstruction r&&r.getReference() instanceof StringReference str)output=V.literal("java.lang.String",str.getString());
            else if(op.equals("const-class")&&in instanceof ReferenceInstruction r&&r.getReference() instanceof TypeReference t)output=V.of("class",CapabilityIndex.cls(t.getType()),t.getType());
            else if(op.startsWith("const")&&in instanceof WideLiteralInstruction lit)output=V.literal("number",String.valueOf(lit.getWideLiteral()));
            else if(op.equals("new-instance")&&in instanceof ReferenceInstruction r&&r.getReference() instanceof TypeReference t)output=V.of("new",CapabilityIndex.cls(t.getType()),key+"@"+at);
            else if(op.equals("check-cast")&&in instanceof ReferenceInstruction r&&r.getReference() instanceof TypeReference t)output=expr("cast",CapabilityIndex.cls(t.getType()),"cast",List.of(s.getOrDefault(a,UNKNOWN)));
            else if(in instanceof ReferenceInstruction r&&r.getReference() instanceof FieldReference f){
                String field=CapabilityIndex.field(f);V receiver=op.startsWith("s")?V.of("static",CapabilityIndex.cls(f.getDefiningClass()),f.getDefiningClass()):in instanceof TwoRegisterInstruction two?s.getOrDefault(two.getRegisterB(),UNKNOWN):UNKNOWN;
                if(op.contains("get"))output=expr("field",CapabilityIndex.cls(f.getType()),field,List.of(receiver));
                if(op.contains("put"))writes.add(new Write(field,receiver,s.getOrDefault(a,UNKNOWN)));
            } else if(op.startsWith("invoke-")&&in instanceof ReferenceInstruction r&&r.getReference() instanceof MethodReference target){
                boolean stat=op.startsWith("invoke-static");List<Integer> regs=registers(in);List<V> args=new ArrayList<>();int cursor=0;
                if(!stat&&!regs.isEmpty())args.add(s.getOrDefault(regs.get(cursor++),UNKNOWN));
                for(CharSequence type:target.getParameterTypes()){
                    args.add(cursor<regs.size()?s.getOrDefault(regs.get(cursor),UNKNOWN):UNKNOWN);cursor+=type.toString().equals("J")||type.toString().equals("D")?2:1;
                }
                String targetKey=CapabilityIndex.key(target);Call previous=calls.get(at);
                if(previous!=null){for(int k=0;k<args.size();k++)args.set(k,union(previous.args.get(k),args.get(k)));}
                calls.put(at,new Call(targetKey,at,List.copyOf(args),stat));
                if(target.getName().equals("getSettings")&&idx.webview(CapabilityIndex.cls(target.getDefiningClass()))&&!args.isEmpty())s.put(-1,expr("settings",CapabilityIndex.cls(target.getReturnType()),"settings",List.of(args.get(0))));
                else if(!target.getReturnType().equals("V"))s.put(-1,expr("return",CapabilityIndex.cls(target.getReturnType()),targetKey,args));
            }
            if(output!=null)s.put(a,output);
            else if(in.getOpcode().setsRegister())s.put(a,UNKNOWN);
            if(op.startsWith("return")&&!op.equals("return-void"))returns=union(returns,s.getOrDefault(a,UNKNOWN));
            List<Integer> next=new ArrayList<>();
            if(op.startsWith("goto")&&in instanceof OffsetInstruction jump){Integer p=positions.get(at+jump.getCodeOffset());if(p!=null)next.add(p);}
            else if(op.startsWith("if-")&&in instanceof OffsetInstruction jump){branched=true;Integer p=positions.get(at+jump.getCodeOffset());if(p!=null)next.add(p);if(pc+1<ins.size())next.add(pc+1);}
            else if((op.equals("packed-switch")||op.equals("sparse-switch"))&&in instanceof OffsetInstruction jump){
                branched=true;Integer payload=positions.get(at+jump.getCodeOffset());if(payload!=null&&ins.get(payload) instanceof SwitchPayload sw)for(var e:sw.getSwitchElements()){Integer p=positions.get(at+e.getOffset());if(p!=null)next.add(p);}if(pc+1<ins.size())next.add(pc+1);
            }else if(in.getOpcode().canContinue()&&pc+1<ins.size())next.add(pc+1);
            for(int p:next)if(merge(states,p,s))work.add(p);
            for(int p:handlers.getOrDefault(pc,List.of())){branched=true;if(merge(states,p,before))work.add(p);}
        }
        return new Summary(List.copyOf(calls.values()),List.copyOf(writes),returns==null?List.of():alternatives(returns),branched,truncated);
    }
    static boolean merge(List<Map<Integer,V>> states,int p,Map<Integer,V> s){
        Map<Integer,V> old=states.get(p);if(old==null){states.set(p,new HashMap<>(s));return true;}
        boolean changed=false;Set<Integer> keys=new HashSet<>(s.keySet());keys.addAll(old.keySet());
        for(int k:keys){V n=union(old.getOrDefault(k,UNKNOWN),s.getOrDefault(k,UNKNOWN));if(!n.equals(old.get(k))){old.put(k,n);changed=true;}}return changed;
    }
    static List<Integer> registers(Instruction in){
        if(in instanceof Instruction35c i)return List.of(i.getRegisterC(),i.getRegisterD(),i.getRegisterE(),i.getRegisterF(),i.getRegisterG()).subList(0,i.getRegisterCount());
        if(in instanceof RegisterRangeInstruction i){List<Integer> r=new ArrayList<>();for(int k=0;k<i.getRegisterCount();k++)r.add(i.getStartRegister()+k);return r;}return List.of();
    }
    static V encoded(EncodedValue e){
        if(e instanceof StringEncodedValue v)return V.literal("java.lang.String",v.getValue());
        if(e instanceof BooleanEncodedValue v)return V.literal("boolean",String.valueOf(v.getValue()));
        if(e instanceof IntEncodedValue v)return V.literal("number",String.valueOf(v.getValue()));
        if(e instanceof TypeEncodedValue v)return V.of("class",CapabilityIndex.cls(v.getValue()),v.getValue());
        return UNKNOWN;
    }
}
