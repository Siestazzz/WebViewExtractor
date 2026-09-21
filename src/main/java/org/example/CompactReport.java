package org.example;

import com.google.gson.*;
import java.util.*;

/** Presentation-only projection: never changes analysis or merges distinct receivers. */
final class CompactReport {
    static JsonArray array(JsonObject o,String key){return o.has(key)&&o.get(key).isJsonArray()?o.getAsJsonArray(key):new JsonArray();}
    static String string(JsonObject o,String key){return o.has(key)&&!o.get(key).isJsonNull()?o.get(key).getAsString():"unknown";}
    static String descriptor(String s){return s.equals("unknown")||s.startsWith("L")&&s.endsWith(";")?s:"L"+s.replace('.','/')+";";}
    static JsonObject counts(int activities,int views,int bridges,int settings,int callbacks){
        JsonObject c=new JsonObject();c.addProperty("activities",activities);c.addProperty("webviews",views);c.addProperty("bridges",bridges);c.addProperty("settings",settings);c.addProperty("callbacks",callbacks);return c;
    }
    static JsonObject project(JsonObject full){
        JsonObject out=new JsonObject(),meta=new JsonObject();meta.addProperty("schema_version",1);
        for(String key:List.of("package","version","apk_sha256","status","wall_seconds","target_seconds","hard_seconds"))if(full.has(key))meta.add(key,full.get(key));
        if(full.has("metrics")){JsonObject m=full.getAsJsonObject("metrics");for(String key:List.of("elapsed_seconds","processed_activities","manifest_activities"))if(m.has(key))meta.add(key,m.get(key));}
        meta.addProperty("detailed_report","capabilities.json");
        meta.addProperty("counts_semantics","Deduplicated displayed entries per WebView, summed across WebViews; bridges count exposed signatures (class fallback if unresolved), not registration names. Candidates included.");
        meta.addProperty("settings_semantics","Observed argument alternatives, not final state; null means unresolved.");
        out.add("metadata",meta);JsonArray hosts=new JsonArray();int tv=0,tb=0,ts=0,tc=0;
        for(JsonElement ae:array(full,"activities")){
            JsonObject a=ae.getAsJsonObject(),host=new JsonObject();host.addProperty("signature",descriptor(string(a,"activity")));
            Map<String,JsonObject> identities=new LinkedHashMap<>();Map<String,List<JsonObject>> facts=new LinkedHashMap<>();
            for(JsonElement fe:array(a,"facts")){JsonObject f=fe.getAsJsonObject();if(!f.has("webview"))continue;JsonObject w=f.getAsJsonObject("webview");String id=w.toString();identities.putIfAbsent(id,w);facts.computeIfAbsent(id,k->new ArrayList<>()).add(f);}
            JsonArray views=new JsonArray();int hb=0,hs=0,hc=0;
            for(var entry:facts.entrySet()){
                TreeSet<String> bridges=new TreeSet<>(),callbacks=new TreeSet<>();TreeMap<String,JsonObject> settings=new TreeMap<>();
                for(JsonObject f:entry.getValue()){
                    String kind=string(f,"kind");
                    if(kind.equals("bridge")||kind.equals("message_bridge")||kind.equals("callback")){
                        Set<String> target=kind.equals("callback")?callbacks:bridges;JsonArray members=array(f,"members");
                        for(JsonElement me:members){String sig=string(me.getAsJsonObject(),"signature");if(!sig.equals("unknown"))target.add(sig);}
                        if(members.isEmpty())target.add(descriptor(string(f,"implementation")));
                    }else if(kind.equals("setting")||kind.equals("global_setting")){
                        JsonObject setting=new JsonObject();String api=string(f,"api");setting.addProperty("signature",api);
                        JsonArray params=new JsonArray(),args=array(f,"arguments");List<String> types=parameters(api);int start=Math.max(0,args.size()-types.size());
                        for(int i=0;i<types.size();i++)params.add(start+i<args.size()?value(args.get(start+i).getAsJsonObject(),types.get(i)):JsonNull.INSTANCE);
                        setting.add("parameters",params);settings.put(setting.toString(),setting);
                    }
                }
                JsonObject view=new JsonObject();view.addProperty("signature",descriptor(string(identities.get(entry.getKey()),"type")));
                view.add("counts",counts(0,1,bridges.size(),settings.size(),callbacks.size()));
                view.add("bridges",Main.JSON.toJsonTree(bridges));JsonArray ss=new JsonArray();settings.values().forEach(ss::add);view.add("settings",ss);view.add("callbacks",Main.JSON.toJsonTree(callbacks));views.add(view);
                hb+=bridges.size();hs+=settings.size();hc+=callbacks.size();
            }
            host.add("counts",counts(1,views.size(),hb,hs,hc));host.add("webviews",views);hosts.add(host);tv+=views.size();tb+=hb;ts+=hs;tc+=hc;
        }
        out.add("counts",counts(hosts.size(),tv,tb,ts,tc));out.add("activities",hosts);return out;
    }
    static List<String> parameters(String signature){
        List<String> types=new ArrayList<>();int i=signature.indexOf('(')+1,end=signature.indexOf(')');if(i==0||end<0)return types;
        while(i<end){int start=i;while(signature.charAt(i)=='[')i++;if(signature.charAt(i)=='L')i=signature.indexOf(';',i)+1;else i++;types.add(signature.substring(start,i));}return types;
    }
    static JsonElement value(JsonObject v,String type){
        if(string(v,"kind").equals("union")){TreeMap<String,JsonElement> alternatives=new TreeMap<>();for(JsonElement a:array(v,"args")){JsonElement val=value(a.getAsJsonObject(),type);alternatives.put(val.toString(),val);}JsonArray values=new JsonArray();alternatives.values().forEach(values::add);JsonObject result=new JsonObject();result.add("alternatives",values);return result;}
        if(!v.has("literal")||v.get("literal").isJsonNull())return JsonNull.INSTANCE;
        String literal=v.get("literal").getAsString();if(type.equals("Z")){if(literal.equals("1")||literal.equals("true"))return new JsonPrimitive(true);if(literal.equals("0")||literal.equals("false"))return new JsonPrimitive(false);}
        return new JsonPrimitive(literal);
    }
}
