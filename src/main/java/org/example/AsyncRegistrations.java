package org.example;

import java.util.*;
import org.jf.dexlib2.iface.*;

/** Framework entry contracts; construction is never an asynchronous registration. */
final class AsyncRegistrations {
    record Entry(int argument,String contract,List<String> members){}
    private final CapabilityIndex idx;
    private final Map<String,List<Entry>> cache=new HashMap<>();
    AsyncRegistrations(CapabilityIndex idx){this.idx=idx;}
    List<Entry> entries(String method){return cache.computeIfAbsent(method,this::discover);}
    private List<Entry> discover(String method){
        String owner=CapabilityEngine.owner(method),shape=method.substring(method.indexOf("->")+2);
        Method declared=idx.resolve(method);
        // An application override can reject a post/bind; follow its body and only model
        // the eventual framework call. A matching inherited method_id alone is sufficient
        // only when no application implementation replaces it.
        if(!owner.startsWith("okhttp3.")&&declared!=null&&declared.getImplementation()!=null){
            String defined=CapabilityIndex.cls(declared.getDefiningClass());
            if(!defined.startsWith("android.")&&!defined.startsWith("java."))return List.of();
        }
        if((idx.subtype(owner,"android.os.Handler")||idx.subtype(owner,"android.view.View"))&&
            Set.of("post(Ljava/lang/Runnable;)Z","postDelayed(Ljava/lang/Runnable;J)Z","postAtTime(Ljava/lang/Runnable;J)Z","postAtTime(Ljava/lang/Runnable;Ljava/lang/Object;J)Z","postOnAnimation(Ljava/lang/Runnable;)V","postOnAnimationDelayed(Ljava/lang/Runnable;J)V").contains(shape))
            return List.of(new Entry(1,"java.lang.Runnable",List.of("run()V")));
        if(idx.subtype(owner,"android.app.Activity")&&shape.equals("runOnUiThread(Ljava/lang/Runnable;)V"))
            return List.of(new Entry(1,"java.lang.Runnable",List.of("run()V")));
        if(idx.subtype(owner,"java.util.concurrent.Executor")&&shape.equals("execute(Ljava/lang/Runnable;)V"))
            return List.of(new Entry(1,"java.lang.Runnable",List.of("run()V")));
        if(idx.subtype(owner,"android.content.Context")&&shape.equals("bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z"))
            return List.of(new Entry(2,"android.content.ServiceConnection",List.of("onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V","onServiceDisconnected(Landroid/content/ComponentName;)V")));
        // Obfuscated OkHttp retains its namespace and the Call/Callback/Closeable response
        // structure. Require the complete two-member contract, not an enqueue-like name.
        if(!owner.startsWith("okhttp3."))return List.of();
        Method registration=idx.resolve(method);
        ClassDef call=idx.classes.get(owner);
        if(registration==null||call==null||(call.getAccessFlags()&0x200)==0||!registration.getReturnType().equals("V")||registration.getParameterTypes().size()!=1)return List.of();
        String callback=CapabilityIndex.cls(registration.getParameterTypes().get(0).toString());
        ClassDef definition=idx.classes.get(callback);
        if(definition==null||!callback.startsWith("okhttp3.")||(definition.getAccessFlags()&0x200)==0)return List.of();
        List<String> members=new ArrayList<>();boolean failure=false,response=false;
        for(Method member:definition.getMethods()){
            if((member.getAccessFlags()&8)!=0||member.getName().startsWith("<"))continue;
            if(!member.getReturnType().equals("V")||member.getParameterTypes().size()!=2||!member.getParameterTypes().get(0).equals(call.getType()))return List.of();
            String data=CapabilityIndex.cls(member.getParameterTypes().get(1).toString());
            if(data.equals("java.io.IOException"))failure=true;
            else if(data.startsWith("okhttp3.")&&idx.subtype(data,"java.io.Closeable"))response=true;
            else return List.of();
            members.add(CapabilityIndex.shape(member));
        }
        return members.size()==2&&failure&&response?List.of(new Entry(1,callback,List.copyOf(members))):List.of();
    }
}
