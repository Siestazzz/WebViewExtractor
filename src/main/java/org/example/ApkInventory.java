package org.example;

import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import pxb.android.axml.*;

/** Binary XML inventory; no decompiler or Android SDK executable required. */
final class ApkInventory {
    String packageName="", version="", applicationName="";
    int targetSdk;
    final Set<String> activities=new TreeSet<>();
    final Map<String,String> aliases=new TreeMap<>();
    record MetadataValue(int type,Object value) {}
    final Map<String,MetadataValue> applicationMetadata=new TreeMap<>();
    final Map<String,Set<String>> layoutTypes=new TreeMap<>();
    static final class LayoutNode {
        String type,source; Integer id,include;
        final List<LayoutNode> children=new ArrayList<>();
        LayoutNode(String type){this.type=type;}
    }
    final Map<String,List<LayoutNode>> layoutRoots=new TreeMap<>();
    final Map<Integer,Set<String>> layoutResources=new TreeMap<>();
    final Map<Integer,Set<Integer>> layoutAliases=new TreeMap<>();
    Set<String> layouts(int id){var result=new TreeSet<String>();collectLayouts(id,result,new HashSet<>());return result;}
    void collectLayouts(int id,Set<String> result,Set<Integer> seen){
        if(!seen.add(id)||seen.size()>64)return;
        result.addAll(layoutResources.getOrDefault(id,Set.of()));
        for(int alias:layoutAliases.getOrDefault(id,Set.of()))collectLayouts(alias,result,seen);
    }
    void resources(byte[] bytes)throws Exception {
        new LayoutResources(bytes).read(this);
    }
    final List<String> errors=new ArrayList<>();
    static ApkInventory read(Path apk) throws Exception {
        ApkInventory result=new ApkInventory();
        try(ZipFile zip=new ZipFile(apk.toFile())) {
            var entry=zip.getEntry("AndroidManifest.xml");
            if(entry!=null) new AxmlReader(zip.getInputStream(entry).readAllBytes()).accept(result.visitor(null));
            else result.errors.add("missing_manifest");
            var table=zip.getEntry("resources.arsc");
            if(table!=null)try{result.resources(zip.getInputStream(table).readAllBytes());}
            catch(Exception ex){result.errors.add("resource_table_parse:"+ex.getClass().getSimpleName());}
            Set<String> layoutFiles=new HashSet<>();result.layoutResources.values().forEach(layoutFiles::addAll);
            var entries=zip.entries();
            while(entries.hasMoreElements()) {
                var e=entries.nextElement();
                if(!layoutFiles.contains(e.getName())&&(!e.getName().startsWith("res/layout")||!e.getName().endsWith(".xml"))) continue;
                try { new AxmlReader(zip.getInputStream(e).readAllBytes()).accept(result.visitor(e.getName())); }
                catch(Exception ex) {result.errors.add("layout_parse:"+e.getName()+":"+ex.getClass().getSimpleName());}
            }
        }
        return result;
    }
    AxmlVisitor visitor(String layout) {
        return new AxmlVisitor(){@Override public NodeVisitor child(String ns,String name){return node(name,layout,null);}};
    }
    NodeVisitor node(String tag,String layout,LayoutNode parent) {
        LayoutNode item=new LayoutNode(tag.equals("WebView")?"android.webkit.WebView":tag);item.source=layout;
        if(layout!=null){if(parent==null)layoutRoots.computeIfAbsent(layout,k->new ArrayList<>()).add(item);else parent.children.add(item);}
        return new NodeVisitor(){
            String name,target; MetadataValue metadataValue;
            @Override public void attr(String ns,String key,int resource,int type,Object value) {
                String text=value instanceof ValueWrapper w ? (w.raw!=null?w.raw:String.valueOf(w.ref)):String.valueOf(value);
                if(layout==null) {
                    if(tag.equals("manifest")&&key.equals("package")) packageName=text;
                    if(tag.equals("manifest")&&key.equals("versionName")) version=text;
                    if(tag.equals("uses-sdk")&&key.equals("targetSdkVersion")&&value instanceof Number n)targetSdk=n.intValue();
                    if(key.equals("name")&&(!tag.equals("meta-data")||"http://schemas.android.com/apk/res/android".equals(ns)))name=text;
                    if(key.equals("targetActivity"))target=text;
                    if(tag.equals("meta-data")&&"http://schemas.android.com/apk/res/android".equals(ns)&&key.equals("value"))metadataValue=new MetadataValue(type,value instanceof ValueWrapper wrapper?wrapper.raw:value);
                    if(tag.equals("meta-data")&&"http://schemas.android.com/apk/res/android".equals(ns)&&key.equals("resource"))metadataValue=new MetadataValue(1,value);
                } else {
                    if(key.equals("class")||key.equals("name")){layoutTypes.computeIfAbsent(layout,k->new TreeSet<>()).add(text);if(tag.equals("view"))item.type=text;}
                    Integer reference=value instanceof Number n?n.intValue():value instanceof ValueWrapper w?w.ref:null;
                    if(key.equals("id"))item.id=reference;
                    if(tag.equals("include")&&key.equals("layout"))item.include=reference;
                }
            }
            @Override public NodeVisitor child(String ns,String name){return node(name,layout,item);}
            @Override public void end(){
                if(layout!=null){if(tag.contains(".")||tag.equals("WebView"))layoutTypes.computeIfAbsent(layout,k->new TreeSet<>()).add(tag);return;}
                if(name!=null&&tag.equals("meta-data")&&parent!=null&&parent.type.equals("application")&&metadataValue!=null)applicationMetadata.put(name,metadataValue);
                if(name!=null&&tag.equals("application"))applicationName=full(name);
                if(name!=null&&tag.equals("activity"))activities.add(full(name));
                if(name!=null&&target!=null&&tag.equals("activity-alias"))aliases.put(full(name),full(target));
            }
        };
    }
    String full(String name){return name.startsWith(".")?packageName+name:name.contains(".")?name:packageName+"."+name;}
}
