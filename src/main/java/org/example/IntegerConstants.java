package org.example;
import java.util.*;
import static org.example.DexFlow.*;
/** Exact Integer boxing/unboxing, preserving the distinction from strings and null. */
final class IntegerConstants {
 static final String BOX="Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;",UNBOX="Ljava/lang/Integer;->intValue()I";
 static V resolve(String method,List<V> args){
  if(!method.equals(BOX)&&!method.equals(UNBOX))return null;
  if(args.size()!=1)return UNKNOWN;
  V result=null;for(V value:alternatives(args.get(0))){
   V next=UNKNOWN;
   if(method.equals(BOX)){Long n=number(value);if(n!=null&&n>=Integer.MIN_VALUE&&n<=Integer.MAX_VALUE)next=new V("boxed_integer","java.lang.Integer","integer:"+n,String.valueOf(n),List.of());}
   else if(value.kind().equals("boxed_integer")&&value.type().equals("java.lang.Integer")&&value.literal()!=null)next=V.literal("number",value.literal());
   result=union(result,next);
  }return result==null?UNKNOWN:result;
 }
}
