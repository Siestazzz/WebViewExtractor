package org.example;
import java.nio.file.*;
import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.ServiceConstructorArrayProbe.*;
import static org.example.DexFlow.*;
/** Standalone diagnostic of frozen v11 boundaries; deliberately not an acceptance test. */
final class ManifestBoundaryAuditProbe {
 public static void main(String[] args)throws Exception{run(false);}
 static void runRegression()throws Exception{run(true);}
 static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 static void run(boolean acceptance)throws Exception{
  String app="Laudit/OverrideApplication;";
  var override=method(app,"getPackageName",List.of(),"Ljava/lang/String;",1,2,List.of(str(0,"foreign.package"),new ImmutableInstruction11x(Opcode.RETURN_OBJECT,0)),false);
  Path dex=Files.createTempFile("manifest-boundary-audit-",".dex");
  try{
   DexFileFactory.writeDexFile(dex.toString(),new ImmutableDexFile(Opcodes.getDefault(),List.of(clazz(app,"Landroid/app/Application;",List.of(),List.of(),override))));
   long deadline=System.nanoTime()+30_000_000_000L;var index=new CapabilityIndex();index.read(dex,deadline);var apk=new ApkInventory();apk.packageName="audit.package";
   var engine=new CapabilityEngine(index,apk,deadline);var host=engine.new Host("audit.Host");var job=new CapabilityEngine.Job(override,List.of(),List.of(),true);
   V actualApp=V.of("object","audit.OverrideApplication","actual-app");
   V baseCall=new V("return","java.lang.String","Landroid/content/Context;->getPackageName()Ljava/lang/String;",null,List.of(actualApp));
   V result=ManifestProtocols.resolve(baseCall,engine,job,host,0);
   System.out.println("base_typed_actual_override expected=unknown_or_actual_foreign_body actual="+result);
   if(acceptance){require(result.equals(UNKNOWN),"Actual Application override was bypassed via base MethodReference");
    V superCall=new V("return_super",baseCall.type(),baseCall.id(),null,baseCall.args());require("audit.package".equals(ManifestProtocols.resolve(superCall,engine,job,host,0).literal()),"Explicit super native getter was blocked");
    V unionCall=new V(baseCall.kind(),baseCall.type(),baseCall.id(),null,List.of(new V("union",null,"two-contexts",null,List.of(actualApp,V.of("host","audit.Host","activity:audit.Host")))));require(ManifestProtocols.resolve(unionCall,engine,job,host,0).equals(UNKNOWN),"Union overriding receiver bypassed");
   }
   V missing=new V("return","java.lang.Object","Landroid/os/Bundle;->get(Ljava/lang/String;)Ljava/lang/Object;",null,List.of(V.of("manifest_bundle","android.os.Bundle","scoped-bundle"),V.literal("java.lang.String","absent")));
   V equality=new V("return","boolean","Landroid/text/TextUtils;->equals(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Z",null,List.of(missing,V.literal("java.lang.String","0")));
   System.out.println("missing_bundle_value_equals_string_zero expected=0 actual="+ManifestProtocols.resolve(equality,engine,job,host,0));
   if(acceptance){
    require("0".equals(ManifestProtocols.resolve(equality,engine,job,host,0).literal()),"Null Bundle value equaled string zero");
    V bothNull=new V(equality.kind(),equality.type(),equality.id(),null,List.of(missing,missing));require("1".equals(ManifestProtocols.resolve(bothNull,engine,job,host,0).literal()),"TextUtils null-null equality lost");
    V nullReceiver=new V("return","boolean","Ljava/lang/String;->equals(Ljava/lang/Object;)Z",null,List.of(missing,V.literal("java.lang.String","0")));require(ManifestProtocols.resolve(nullReceiver,engine,job,host,0).equals(UNKNOWN),"Null String.equals receiver evaluated");
    apk.applicationMetadata.put("present",new ApkInventory.MetadataValue(3,"0"));
    V presentGet=new V(missing.kind(),missing.type(),missing.id(),null,List.of(missing.args().get(0),V.literal("java.lang.String","present")));
    V presentEquality=new V(equality.kind(),equality.type(),equality.id(),null,List.of(presentGet,V.literal("java.lang.String","0")));require("1".equals(ManifestProtocols.resolve(presentEquality,engine,job,host,0).literal()),"Actual string-zero equality lost");
   }
   apk.applicationMetadata.put("numeric",new ApkInventory.MetadataValue(16,0));
   V numericGet=new V("return","java.lang.Object","Landroid/os/Bundle;->get(Ljava/lang/String;)Ljava/lang/Object;",null,List.of(V.of("manifest_bundle","android.os.Bundle","scoped-bundle"),V.literal("java.lang.String","numeric")));
   V typedEquality=new V("return","boolean","Landroid/text/TextUtils;->equals(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Z",null,List.of(numericGet,V.literal("java.lang.String","0")));
   System.out.println("integer_metadata_equals_string_zero expected=unknown_or_false actual="+ManifestProtocols.resolve(typedEquality,engine,job,host,0));
   if(acceptance)require(ManifestProtocols.resolve(typedEquality,engine,job,host,0).equals(UNKNOWN),"Incompatible literal types compared as string text");
  }finally{Files.deleteIfExists(dex);}
  if(acceptance)System.out.println("ManifestBoundaryAuditProbe regression PASS: actual virtual override/super/union and null/string/typed metadata equality.");
 }
}
