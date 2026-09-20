package org.example;

import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import pxb.android.axml.*;

/** Binary XML inventory; no decompiler or Android SDK executable required. */
final class ApkInventory {
    String packageName="", version="";
    int targetSdk;
    final Set<String> activities=new TreeSet<>();
    final Map<String,String> aliases=new TreeMap<>();
    final Map<String,Set<String>> layoutTypes=new TreeMap<>();
    final List<String> errors=new ArrayList<>();
    static ApkInventory read(Path apk) throws Exception {
        ApkInventory result=new ApkInventory();
        try(ZipFile zip=new ZipFile(apk.toFile())) {
            var entry=zip.getEntry("AndroidManifest.xml");
            if(entry!=null) new AxmlReader(zip.getInputStream(entry).readAllBytes()).accept(result.visitor(null));
            else result.errors.add("missing_manifest");
            var entries=zip.entries();
            while(entries.hasMoreElements()) {
                var e=entries.nextElement();
                if(!e.getName().startsWith("res/layout")||!e.getName().endsWith(".xml")) continue;
                try { new AxmlReader(zip.getInputStream(e).readAllBytes()).accept(result.visitor(e.getName())); }
                catch(Exception ex) {result.errors.add("layout_parse:"+e.getName()+":"+ex.getClass().getSimpleName());}
            }
        }
        return result;
    }
    AxmlVisitor visitor(String layout) {
        return new AxmlVisitor(){@Override public NodeVisitor child(String ns,String name){return node(name,layout);}};
    }
    NodeVisitor node(String tag,String layout) {
        return new NodeVisitor(){
            String name,target;
            @Override public void attr(String ns,String key,int resource,int type,Object value) {
                String text=value instanceof ValueWrapper w ? (w.raw!=null?w.raw:String.valueOf(w.ref)):String.valueOf(value);
                if(layout==null) {
                    if(tag.equals("manifest")&&key.equals("package")) packageName=text;
                    if(tag.equals("manifest")&&key.equals("versionName")) version=text;
                    if(tag.equals("uses-sdk")&&key.equals("targetSdkVersion")&&value instanceof Number n)targetSdk=n.intValue();
                    if(key.equals("name"))name=text;
                    if(key.equals("targetActivity"))target=text;
                } else if(key.equals("class")||key.equals("name")) layoutTypes.computeIfAbsent(layout,k->new TreeSet<>()).add(text);
            }
            @Override public NodeVisitor child(String ns,String name){return node(name,layout);}
            @Override public void end(){
                if(layout!=null){if(tag.contains(".")||tag.equals("WebView"))layoutTypes.computeIfAbsent(layout,k->new TreeSet<>()).add(tag);return;}
                if(name!=null&&tag.equals("activity"))activities.add(full(name));
                if(name!=null&&target!=null&&tag.equals("activity-alias"))aliases.put(full(name),full(target));
            }
        };
    }
    String full(String name){return name.startsWith(".")?packageName+name:name.contains(".")?name:packageName+"."+name;}
}
