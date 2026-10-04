package org.example;

import java.util.*;
import static org.example.CapabilitySelfTest.check;
import static org.example.DexFlow.*;

final class FieldHeapFixture {
 public static void main(String[] args){run();}
 static void run(){
  var heap=new FieldHeap();V one=V.literal("number","1"),two=V.literal("number","2"),three=V.literal("number","3");
  String field="Lfixture/Holder;->child:Lfixture/Value;",a="receiverA::"+field,b="receiverB::"+field;
  heap.put(a,one);heap.put("other::Lfixture/Holder;->unrelated:I",three);
  check(new HashSet<>(heap.valuesForField(field)).equals(Set.of(one)),"Field index crosses field identity");
  long scans=heap.fieldIndexScans;
  heap.put(b,two);heap.put(a,three);
  check(new HashSet<>(heap.valuesForField(field)).equals(Set.of(two,three))&&heap.fieldIndexScans==scans,"Write does not update cached field values or forces full rescan");
  heap.compute(a,(k,v)->one);heap.merge(b,three,(x,y)->y);
  check(new HashSet<>(heap.valuesForField(field)).equals(Set.of(one,three)),"Map default writes bypass field index");
  heap.remove(b);check(new HashSet<>(heap.valuesForField(field)).equals(Set.of(one)),"Removed binding retained");
  boolean rejected=false;try{heap.entrySet().iterator().next().setValue(two);}catch(UnsupportedOperationException expected){rejected=true;}
  check(rejected,"Entry mutation can bypass index");
  heap.clear();check(heap.valuesForField(field).isEmpty(),"Clear retains old host bindings");
  heap.put(a,two);check(new HashSet<>(heap.valuesForField(field)).equals(Set.of(two)),"Post-clear index fails to follow write");
  var interrupted=new FieldHeap();interrupted.put(a,one);interrupted.put(b,two);
  boolean stopped=false;try{interrupted.valuesForField(field,()->{throw new IllegalStateException("deadline");});}catch(IllegalStateException expected){stopped=true;}
  check(stopped&&new HashSet<>(interrupted.valuesForField(field)).equals(Set.of(one,two)),"Interrupted scan publishes a partial index");
  System.out.println("FieldHeapFixture PASS: cached field queries, exact owner keys, replacement, compute/merge/remove/clear and mutation protection.");
 }
}
