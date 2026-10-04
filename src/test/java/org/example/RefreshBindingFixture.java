package org.example;
import java.util.*;
import static org.example.DexFlow.*;
import static org.example.CapabilitySelfTest.check;

/** Shared heap diamonds preserve path/depth semantics and stop cooperatively at deadline. */
final class RefreshBindingFixture {
 static long legacyWork;
 public static void main(String[] args)throws Exception{run();}
 static V legacy(V value,CapabilityEngine.Host h,int depth,Set<String> seen){
  legacyWork++;if(depth>12)return value;
  if(value.kind().equals("union")){V result=null;for(V choice:alternatives(value))result=union(result,legacy(choice,h,depth+1,new HashSet<>(seen)));return result==null?value:result;}
  if(value.kind().equals("field_object")){
   var field=h.deferredFields.get(value.id());if(field==null||!seen.add(value.id()))return value;
   V receiver=legacy(field.receiver(),h,depth+1,seen),result=null;
   for(V actual:alternatives(receiver)){V stored=h.heap.get(CapabilityEngine.heapKey(field.field(),actual));if(stored!=null&&!stored.equals(value))result=union(result,legacy(stored,h,depth+1,new HashSet<>(seen)));}
   return result==null?value:result;
  }return value;
 }
 static V field(CapabilityEngine engine,CapabilityEngine.Host host,String name,V receiver,V stored){
  String key=CapabilityEngine.heapKey(name,receiver);V value=engine.deferredField(host,name,receiver,"android.webkit.WebView");if(stored!=null)host.heap.put(key,stored);return value;
 }
 static void run(){
  var expired=new CapabilityEngine(new CapabilityIndex(),new ApkInventory(),System.nanoTime()-1);var expiredHost=expired.new Host("expired");
  var receiver=V.of("object","refresh.Owner","owner");var leaf=V.of("object","android.webkit.WebView","leaf");
  V unresolved=field(expired,expiredHost,"expiredField",receiver,leaf);
  check(expired.refreshBinding(unresolved,expiredHost,0,new HashSet<>()).equals(unresolved)&&expiredHost.gaps.contains("deferred_binding_deadline"),"Refresh ignores deadline or drops its original unresolved value");
  var engine=new CapabilityEngine(new CapabilityIndex(),new ApkInventory(),System.nanoTime()+60_000_000_000L);var host=engine.new Host("refresh");
  V diamond=leaf;
  for(int level=0;level<5;level++){
   V next=null;for(int branch=0;branch<9;branch++)next=union(next,field(engine,host,"level"+level+"branch"+branch,receiver,diamond));diamond=next;
  }
  long started=System.nanoTime();V actual=engine.refreshBinding(diamond,host,0,new HashSet<>());long elapsed=System.nanoTime()-started;
  legacyWork=0;V expected=legacy(diamond,host,0,new HashSet<>());
  check(actual.equals(expected)&&actual.equals(leaf),"Memoized shared diamond changes resolved leaves");
  check(legacyWork>100_000&&engine.lastRefreshWork<1000,"Shared diamond remains exponential: "+engine.lastRefreshWork+" legacy="+legacyWork);
  System.out.println("Refresh diamond elapsed_ns="+elapsed+" memo_expansions="+engine.lastRefreshWork+" legacy_calls="+legacyWork);
  // Same deferred value occurs once near the depth limit and once shallowly.
  V shared=field(engine,host,"shared",receiver,leaf),deep=shared;for(int i=0;i<12;i++)deep=new V("union",null,"nested"+i,null,List.of(deep));
  V depthUnion=new V("union",null,"depth_union",null,List.of(deep,shared));
  check(engine.refreshBinding(depthUnion,host,0,new HashSet<>()).equals(legacy(depthUnion,host,0,new HashSet<>())),"Memo reused a depth-limited result in a shallow path");
  // Different sibling paths carry different seen sets into a shared cyclic node.
  V a=field(engine,host,"a",receiver,null),b=field(engine,host,"b",receiver,null);
  host.heap.put(CapabilityEngine.heapKey("a",receiver),union(b,leaf));host.heap.put(CapabilityEngine.heapKey("b",receiver),union(a,leaf));
  V cycle=union(a,b);check(engine.refreshBinding(cycle,host,0,new HashSet<>()).equals(legacy(cycle,host,0,new HashSet<>())) ,"Memo erased path-dependent cycle alternatives");
  check(engine.refreshBinding(a,host,0,new HashSet<>(Set.of(a.id()))).equals(a),"Seen field must remain deferred");
  // Receiver refresh mutates the same seen path before stored-value refresh.
  V receiverField=engine.deferredField(host,"receiverField",receiver,"refresh.Owner");host.heap.put(CapabilityEngine.heapKey("receiverField",receiver),receiver);
  V nested=field(engine,host,"nested",receiverField,leaf);host.heap.put(CapabilityEngine.heapKey("nested",receiver),union(receiverField,leaf));
  check(engine.refreshBinding(union(nested,a),host,0,new HashSet<>()).equals(legacy(union(nested,a),host,0,new HashSet<>())) ,"Memo lost receiver-path seen propagation");
  host.heap.put(CapabilityEngine.heapKey("shared",receiver),V.of("object","android.webkit.WebView","new_leaf"));
  check(!engine.refreshBinding(shared,host,0,new HashSet<>()).equals(leaf),"Memo survives heap mutation across refresh invocations");
  Random random=new Random(71);
  for(int sample=0;sample<80;sample++){
   var randomHost=engine.new Host("random"+sample);List<V> nodes=new ArrayList<>();
   for(int i=0;i<7;i++)nodes.add(field(engine,randomHost,"randomField"+i,receiver,null));
   for(int i=0;i<nodes.size();i++){
    V stored=union(nodes.get(random.nextInt(nodes.size())),random.nextBoolean()?leaf:nodes.get(random.nextInt(nodes.size())));
    randomHost.heap.put(CapabilityEngine.heapKey("randomField"+i,receiver),stored);
   }
   V root=union(nodes.get(random.nextInt(nodes.size())),nodes.get(random.nextInt(nodes.size())));
   Set<String> actualSeen=new HashSet<>();if(random.nextBoolean())actualSeen.add(nodes.get(random.nextInt(nodes.size())).id());
   Set<String> expectedSeen=new HashSet<>(actualSeen);int initialDepth=random.nextInt(8);
   V memoValue=engine.refreshBinding(root,randomHost,initialDepth,actualSeen),legacyValue=legacy(root,randomHost,initialDepth,expectedSeen);
   check(memoValue.equals(legacyValue)&&actualSeen.equals(expectedSeen),"Seeded cyclic graph differs from legacy result/seen mutation at sample "+sample);
  }
  System.out.println("RefreshBindingFixture PASS: deadline retention, shared diamond, remaining depth, cyclic seen paths, dynamic receiver paths and heap mutation isolation.");
 }
}
