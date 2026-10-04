package org.example;

import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import static org.example.CapabilitySelfTest.*;
import static org.example.DexFlow.*;

/** Reflection construction preserves exact class, constructor access and allocation site. */
final class ReflectiveFactoryFixture {
 public static void main(String[] args)throws Exception {run();}
 static void run()throws Exception {
  String good="Lfactory/Public;",hidden="Lfactory/Private;",many="Lfactory/Overloaded;",abstractType="Lfactory/Abstract;";
  var entry=method(A,"entry",List.of(),9,0,List.of(end()),false);
  Path dex=Files.createTempFile("reflective-factory-",".dex");
  try {
   DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(A,"Landroid/app/Activity;",entry),clazz(good,"Ljava/lang/Object;",method(good,"<init>",List.of(),0x10001,1,List.of(end()),false)),clazz(hidden,"Ljava/lang/Object;",method(hidden,"<init>",List.of(),0x10002,1,List.of(end()),false)),clazz(many,"Ljava/lang/Object;",method(many,"<init>",List.of(),0x10001,1,List.of(end()),false),method(many,"<init>",List.of("I"),0x10001,2,List.of(end()),false)),new ImmutableClassDef(abstractType,0x401,"Ljava/lang/Object;",List.of(),null,Set.of(),List.of(),List.of(method(abstractType,"<init>",List.of(),0x10001,1,List.of(end()),false))))));
   long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var engine=new CapabilityEngine(index,new ApkInventory(),deadline);var host=engine.new Host("test.AppActivity");var job=new CapabilityEngine.Job(entry,List.of(),List.of("factory-test"),false);
   V clazz=V.of("class","factory.Public",good);
   V first=resolve(ReflectiveFactories.CLASS_NEW,"return@1",List.of(clazz),engine,job,host),second=resolve(ReflectiveFactories.CLASS_NEW,"return@2",List.of(clazz),engine,job,host);
   check(first.kind().equals("object")&&first.type().equals("factory.Public")&&!first.id().equals(second.id()),"Reflective allocations lost class/site identity");
   check(host.constructed.size()==2,"Uncalled classes or constructors activated");
   V named=resolve(ReflectiveFactories.FOR_NAME,"return@3",List.of(V.literal("java.lang.String","factory.Public")),engine,job,host);
   check(named.kind().equals("class")&&named.type().equals("factory.Public"),"Exact class name unresolved");
   check(resolve(ReflectiveFactories.FOR_NAME,"return@4",List.of(UNKNOWN),engine,job,host).equals(UNKNOWN),"Unknown class became a guessed implementation");
   for(String type:List.of("factory.Private","factory.Abstract"))check(resolve(ReflectiveFactories.CLASS_NEW,"return@5",List.of(V.of("class",type,CapabilityEngine.desc(type))),engine,job,host).equals(UNKNOWN),"Inaccessible/abstract class constructed");
   V array=resolve(ReflectiveFactories.CONSTRUCTORS,"return@6",List.of(clazz),engine,job,host);
   V ctor=engine.arrayElement(host,array,V.literal("number","0"),"java.lang.reflect.Constructor");
   check(ctor.kind().equals("reflect_constructor"),"Singleton constructor identity lost");
   V args=V.of("object","[Ljava/lang/Object;","empty-arguments");host.arrayLengths.put(args.id(),0L);
   check(resolve(ReflectiveFactories.CONSTRUCTOR_NEW,"return@7",List.of(ctor,args),engine,job,host).kind().equals("object"),"Exact public noarg constructor not called");
   host.arrayLengths.put(args.id(),1L);
   check(resolve(ReflectiveFactories.CONSTRUCTOR_NEW,"return@8",List.of(ctor,args),engine,job,host).equals(UNKNOWN),"Wrong constructor arity ignored");
   check(resolve(ReflectiveFactories.CONSTRUCTORS,"return@9",List.of(V.of("class","factory.Overloaded",many)),engine,job,host).equals(UNKNOWN),"Unspecified multi-constructor order guessed");
   V types=V.of("object","[Ljava/lang/Class;","empty-types");host.arrayLengths.put(types.id(),0L);
   V exact=resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@10",List.of(V.of("class","factory.Overloaded",many),types),engine,job,host);
   check(exact.kind().equals("reflect_constructor")&&exact.id().equals(many+"-><init>()V"),"Exact empty lookup confused overload ordering");
   host.arrayLengths.put(args.id(),0L);
   check(resolve(ReflectiveFactories.CONSTRUCTOR_NEW,"return@11",List.of(exact,args),engine,job,host).type().equals("factory.Overloaded"),"Exact looked-up constructor lost concrete result");
   V other=resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@12",List.of(clazz,types),engine,job,host);
   check(!other.equals(exact),"Different class constructor identities merged");
   check(resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@13",List.of(V.of("class","factory.Private",hidden),types),engine,job,host).equals(UNKNOWN),"Private constructor selected by public lookup");
   check(resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@14",List.of(UNKNOWN,types),engine,job,host).equals(UNKNOWN),"Unknown class broadened public lookup");
   check(resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@15",List.of(clazz,args),engine,job,host).equals(UNKNOWN),"Object[] accepted as parameter-type array");
   host.arrayLengths.put(types.id(),1L);
   check(resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@16",List.of(clazz,types),engine,job,host).equals(UNKNOWN),"Nonempty constructor signature guessed");
   host.arrayLengths.remove(types.id());
   check(resolve(ReflectiveFactories.PUBLIC_CONSTRUCTOR,"return@17",List.of(clazz,types),engine,job,host).equals(UNKNOWN),"Unknown parameter arity guessed empty");
  } finally {Files.deleteIfExists(dex);}
  System.out.println("ReflectiveFactoryFixture PASS: exact literal/name classes, actual public noarg construction, singleton constructor array, access/arity/order negatives, distinct allocations.");
 }
 static V resolve(String api,String kind,List<V> args,CapabilityEngine e,CapabilityEngine.Job job,CapabilityEngine.Host host){return ReflectiveFactories.resolve(new V(kind,"java.lang.Object",api,null,List.of()),args,e,job,host,0,new HashSet<>());}
}
