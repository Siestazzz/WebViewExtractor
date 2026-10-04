package org.example;

import java.util.*;

/** Per-host heap with a lazy field index that follows every supported mutation. */
final class FieldHeap extends AbstractMap<String,DexFlow.V> {
 private final Map<String,DexFlow.V> values=new HashMap<>();
 private final Map<String,Map<String,DexFlow.V>> fields=new HashMap<>();
 long fieldIndexScans;
 Collection<DexFlow.V> valuesForField(String field){return valuesForField(field,()->{});}
 Collection<DexFlow.V> valuesForField(String field,Runnable checkBudget){
  Map<String,DexFlow.V> indexed=fields.get(field);
  if(indexed==null){
   indexed=new HashMap<>();fieldIndexScans++;
   for(var entry:values.entrySet()){checkBudget.run();if(field.equals(field(entry.getKey())))indexed.put(entry.getKey(),entry.getValue());}
   fields.put(field,indexed);
  }
  return Collections.unmodifiableCollection(indexed.values());
 }
 private static String field(String key){int split=key.lastIndexOf("::");return split<0?null:key.substring(split+2);}
 @Override public DexFlow.V get(Object key){return values.get(key);}
 @Override public boolean containsKey(Object key){return values.containsKey(key);}
 @Override public int size(){return values.size();}
 @Override public DexFlow.V put(String key,DexFlow.V value){
  Objects.requireNonNull(key);Objects.requireNonNull(value);
  DexFlow.V previous=values.put(key,value);
  if(!fields.isEmpty()){var indexed=fields.get(field(key));if(indexed!=null)indexed.put(key,value);}
  return previous;
 }
 @Override public DexFlow.V remove(Object key){
  DexFlow.V previous=values.remove(key);
  if(key instanceof String text&&!fields.isEmpty()){var indexed=fields.get(field(text));if(indexed!=null)indexed.remove(key);}
  return previous;
 }
 @Override public void clear(){values.clear();fields.clear();}
 // Direct entry mutation would bypass the index. Callers use put/remove or Map defaults.
 @Override public Set<Entry<String,DexFlow.V>> entrySet(){return Collections.unmodifiableMap(values).entrySet();}
}
