package org.example;

import java.util.*;
import org.jf.dexlib2.iface.Method;
import static org.example.DexFlow.*;

/** Public metadata acquisition and reflection, bound to the current APK and actual context. */
final class ManifestProtocols {
    static final String METADATA="Landroid/content/pm/ApplicationInfo;->metaData:Landroid/os/Bundle;";
    static final String APPLICATION_INFO="Landroid/content/pm/PackageManager;->getApplicationInfo(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;";
    static final String KEYS="Landroid/os/Bundle;->keySet()Ljava/util/Set;";
    static final String FOR_NAME="Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;";
    static final String NEW_INSTANCE="Ljava/lang/Class;->newInstance()Ljava/lang/Object;";
    static final Set<String> PURE=Set.of(APPLICATION_INFO,KEYS,FOR_NAME,NEW_INSTANCE,
            "Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;",
            "Landroid/content/Context;->getPackageName()Ljava/lang/String;",
            "Ljava/util/Set;->iterator()Ljava/util/Iterator;","Ljava/util/Iterator;->next()Ljava/lang/Object;","Ljava/util/Iterator;->hasNext()Z",
            "Landroid/os/Bundle;->get(Ljava/lang/String;)Ljava/lang/Object;",
            "Landroid/os/Bundle;->getString(Ljava/lang/String;)Ljava/lang/String;",
            "Landroid/text/TextUtils;->equals(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Z",
            "Ljava/lang/String;->equals(Ljava/lang/Object;)Z","Ljava/lang/Class;->isInstance(Ljava/lang/Object;)Z");
    static String canonicalApi(CapabilityIndex index,String api){
        if(PURE.contains(api))return api;
        int split=api.indexOf("->");if(split<0)return api;String shape=api.substring(split+2),owner=CapabilityEngine.owner(api);
        if((shape.equals("getPackageManager()Landroid/content/pm/PackageManager;")||shape.equals("getPackageName()Ljava/lang/String;"))&&index.subtype(owner,"android.content.Context"))return "Landroid/content/Context;->"+shape;
        if(shape.equals("getApplicationInfo(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;")&&index.subtype(owner,"android.content.pm.PackageManager"))return APPLICATION_INFO;
        return api;
    }
    static boolean pure(CapabilityIndex index,String api){return PURE.contains(canonicalApi(index,api));}
    static boolean consumer(CapabilityIndex index,Method method){
        Set<String> calls=index.calls.getOrDefault(CapabilityIndex.key(method),Set.of());
        return calls.contains(KEYS)&&calls.contains(FOR_NAME)&&calls.contains(NEW_INSTANCE)&&index.referencedFields.getOrDefault(CapabilityIndex.key(method),Set.of()).contains(METADATA);
    }
    static V entry(List<V> args){for(V value:args)if(value.kind().equals("manifest_entry_context"))return value;return null;}
    static V typed(ApkInventory.MetadataValue value){
        if(value.type()==1)return V.of("unknown",null,"manifest_metadata_resource_or_unknown");
        if(value.type()==3&&value.value() instanceof String text)return V.literal("java.lang.String",text);
        if(value.value() instanceof Number number)return V.literal(value.type()==18?"java.lang.Boolean":"java.lang.Integer",number.toString());
        return V.of("unknown",null,"manifest_metadata_resource_or_unknown");
    }
    static V resolve(V value,CapabilityEngine engine,CapabilityEngine.Job job,CapabilityEngine.Host host,int depth){
        if(depth>12||System.nanoTime()>=engine.deadline)return UNKNOWN;
        if(value.kind().equals("param")){int index=Integer.parseInt(value.id());return index<job.args().size()?job.args().get(index):UNKNOWN;}
        if(value.kind().equals("cast"))return resolve(value.args().get(0),engine,job,host,depth+1);
        if(value.kind().equals("field")&&value.args().size()==1){
            V receiver=resolve(value.args().get(0),engine,job,host,depth+1);
            if(value.id().equals(METADATA)&&receiver.kind().equals("manifest_application_info"))return V.of("manifest_bundle","android.os.Bundle",receiver.id()+":metadata");
            if(!engine.idx.finalFields.contains(value.id())&&!engine.idx.subtype(value.type(),"android.content.Context"))return UNKNOWN;
            return host.heap.getOrDefault(CapabilityEngine.heapKey(value.id(),receiver),UNKNOWN);
        }
        if(!value.kind().startsWith("return"))return value;
        List<V> args=new ArrayList<>();for(V argument:value.args())args.add(resolve(argument,engine,job,host,depth+1));
        String api=canonicalApi(engine.idx,value.id());V selection=entry(job.args());
        // Only native framework identities are modeled; application overrides keep their bodies.
        Method implementation=engine.idx.resolve(value.id());
        if(implementation!=null&&implementation.getImplementation()!=null&&!CapabilityIndex.cls(implementation.getDefiningClass()).startsWith("android."))return UNKNOWN;
        if(api.equals("Landroid/content/Context;->getPackageName()Ljava/lang/String;")&&args.size()==1&&currentContext(args.get(0),engine,host))return V.literal("java.lang.String",engine.apk.packageName);
        if(api.equals("Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;")&&args.size()==1&&currentContext(args.get(0),engine,host))return V.of("manifest_package_manager","android.content.pm.PackageManager","manifest_manager:"+engine.apk.packageName);
        if(api.equals(APPLICATION_INFO)&&args.size()==3&&args.get(0).kind().equals("manifest_package_manager")){
            Long flags=DexFlow.number(args.get(2));
            if(!engine.apk.packageName.isEmpty()&&engine.apk.packageName.equals(args.get(1).literal())&&flags!=null&&(flags&128)!=0)return V.of("manifest_application_info","android.content.pm.ApplicationInfo","manifest_app:"+engine.apk.packageName);
            return UNKNOWN;
        }
        if(api.equals(KEYS)&&args.size()==1&&args.get(0).kind().equals("manifest_bundle"))return new V("manifest_keys","java.util.Set",args.get(0).id()+":keys",null,List.of(args.get(0)));
        if(api.equals("Ljava/util/Set;->iterator()Ljava/util/Iterator;")&&args.size()==1&&args.get(0).kind().equals("manifest_keys"))return new V("manifest_iterator","java.util.Iterator",args.get(0).id()+":iterator",null,args.get(0).args());
        if(api.equals("Ljava/util/Iterator;->hasNext()Z")&&args.size()==1&&args.get(0).kind().equals("manifest_iterator")&&engine.apk.applicationMetadata.isEmpty())return V.literal("number","0");
        if(api.equals("Ljava/util/Iterator;->next()Ljava/lang/Object;")&&args.size()==1&&args.get(0).kind().equals("manifest_iterator")&&selection!=null&&selection.args().get(0).id().equals(args.get(0).args().get(0).id())){
            V key=selection.args().get(1);return new V("literal","java.lang.String",key.id(),key.literal(),List.of(V.of("manifest_key_origin",null,selection.id())));
        }
        if((api.equals("Landroid/os/Bundle;->get(Ljava/lang/String;)Ljava/lang/Object;")||api.equals("Landroid/os/Bundle;->getString(Ljava/lang/String;)Ljava/lang/String;"))&&args.size()==2&&args.get(0).kind().equals("manifest_bundle")&&args.get(1).literal()!=null){
            var actual=engine.apk.applicationMetadata.get(args.get(1).literal());V result=actual==null?V.literal(null,"0"):typed(actual);
            if(api.contains("->getString(")&&result.type()!=null&&!result.type().equals("java.lang.String"))return UNKNOWN;
            return new V(result.kind(),result.type(),result.id(),result.literal(),List.of(V.of("manifest_value_origin",null,args.get(0).id()+":"+args.get(1).literal())));
        }
        if((api.equals("Landroid/text/TextUtils;->equals(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Z")||api.equals("Ljava/lang/String;->equals(Ljava/lang/Object;)Z"))&&args.size()==2&&args.get(0).literal()!=null&&args.get(1).literal()!=null&&args.stream().anyMatch(v->v.args().stream().anyMatch(marker->marker.kind().equals("manifest_value_origin"))))return V.literal("number",args.get(0).literal().equals(args.get(1).literal())?"1":"0");
        if(api.equals("Ljava/lang/Class;->isInstance(Ljava/lang/Object;)Z")&&args.size()==2&&args.get(0).kind().equals("class")&&args.get(1).type()!=null&&args.get(1).args().stream().anyMatch(marker->marker.kind().equals("manifest_value_origin")))return V.literal("number",engine.idx.subtype(args.get(1).type(),args.get(0).type())||Objects.equals(args.get(1).type(),args.get(0).type())?"1":"0");
        if(api.equals(FOR_NAME)&&args.size()==1&&args.get(0).literal()!=null&&selection!=null&&args.get(0).args().stream().anyMatch(v->v.kind().equals("manifest_key_origin")&&v.id().equals(selection.id()))){
            String type=args.get(0).literal();if(engine.idx.classes.containsKey(type))return new V("class",type,CapabilityEngine.desc(type),null,List.of(V.of("manifest_class_origin",null,selection.id())));
            return V.of("unknown","java.lang.Class","manifest_class_unresolved:"+type);
        }
        if(api.equals(NEW_INSTANCE)&&args.size()==1&&args.get(0).kind().equals("class")&&selection!=null&&args.get(0).args().stream().anyMatch(v->v.kind().equals("manifest_class_origin")&&v.id().equals(selection.id())))
            return new V("manifest_new",args.get(0).type(),CapabilityIndex.key(job.method())+"@"+value.kind()+":"+args.get(0).id(),null,List.of(args.get(0)));
        return UNKNOWN;
    }
    static boolean currentContext(V context,CapabilityEngine engine,CapabilityEngine.Host host){
        if(engine.apk.packageName.isEmpty())return false;
        if(context.kind().equals("union"))return context.args().stream().allMatch(value->currentContext(value,engine,host));
        if(context.kind().equals("host")&&context.id().startsWith("activity:"+host.activity))return true;
        // An actually allocated Application object belongs to this APK. Unknown fields,
        // arbitrary ContextWrapper objects and foreign package contexts do not qualify.
        return context.kind().equals("object")&&engine.idx.subtype(context.type(),"android.app.Application");
    }
}
