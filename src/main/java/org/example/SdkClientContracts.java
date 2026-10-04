package org.example;

import java.util.*;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.reference.MethodReference;

/** Public SDK setter identities and extension members from the APK's SDK contracts. */
final class SdkClientContracts {
 static final String X5_EXTENSION="com.tencent.smtt.export.external.extension.interfaces.IX5WebViewClientExtension";
 static final String X5_CALLBACK="com.tencent.smtt.sdk.WebViewCallbackClient";
 static final Set<String> EXTENSIONS=Set.of(X5_EXTENSION,X5_CALLBACK);
 final CapabilityIndex index;
 final Map<String,Set<String>> memberShapes=new java.util.concurrent.ConcurrentHashMap<>();
 SdkClientContracts(CapabilityIndex index){this.index=index;}
 boolean extensionType(String type){return EXTENSIONS.stream().anyMatch(contract->index.subtype(type,contract));}
 boolean setter(MethodReference method){
  String owner=CapabilityIndex.cls(method.getDefiningClass()),shape=CapabilityIndex.shape(method);
  for(String family:List.of("android.webkit.","com.tencent.smtt.sdk.")){
   if(!index.subtype(owner,family+"WebView"))continue;
   String prefix="L"+family.replace('.','/');
   if(shape.equals("setWebViewClient("+prefix+"WebViewClient;)V")||shape.equals("setWebChromeClient("+prefix+"WebChromeClient;)V"))return true;
  }
  if(!index.subtype(owner,"com.tencent.smtt.sdk.WebView"))return false;
  return shape.equals("setWebViewClientExtension(L"+X5_EXTENSION.replace('.','/')+";)V")||shape.equals("setWebViewCallbackClient(L"+X5_CALLBACK.replace('.','/')+";)V");
 }
 boolean extensionMember(String type,Method method){
  return (method.getAccessFlags()&1)!=0&&(method.getAccessFlags()&8)==0&&extensionShape(type,CapabilityIndex.shape(method));
 }
 boolean extensionShape(String type,String shape){
  for(String contract:EXTENSIONS)if(index.subtype(type,contract)){
   Set<String> shapes=memberShapes.computeIfAbsent(contract,key->{
    Set<String> result=new HashSet<>();
    if(!index.classes.containsKey(key))index.diagnostics.add("sdk_extension_contract_missing:"+key);
    for(Method method:index.contractMethods(key))if((method.getAccessFlags()&1)!=0&&(method.getAccessFlags()&8)==0&&!method.getName().startsWith("<"))result.add(CapabilityIndex.shape(method));
    return Set.copyOf(result);
   });
   if(shapes.contains(shape))return true;
  }
  return false;
 }
}
