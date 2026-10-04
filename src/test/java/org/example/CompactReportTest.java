package org.example;
import com.google.gson.*;
import java.nio.file.*;
public class CompactReportTest {
    public static void main(String[] args)throws Exception{
        JsonObject full=JsonParser.parseString("""
        {"status":"partial","activities":[{"activity":"test.Host","facts":[
          {"kind":"bridge","webview":{"id":"a","type":"test.View"},"implementation":"test.Bridge","members":[{"signature":"Ltest/Bridge;->go()V"}]},
          {"kind":"bridge","webview":{"id":"a","type":"test.View"},"implementation":"test.Bridge","members":[{"signature":"Ltest/Bridge;->go()V"}]},
          {"kind":"bridge","webview":{"id":"b","type":"test.View"},"implementation":"test.Other","members":[]},
          {"kind":"callback","webview":{"id":"a","type":"test.View"},"members":[{"signature":"Ltest/Client;->onPageFinished()V"}]},
          {"kind":"setting","webview":{"id":"a","type":"test.View"},"api":"Ltest/Settings;->setJavaScriptEnabled(Z)V","arguments":[{}, {"kind":"literal","literal":"1"}]},
          {"kind":"setting","webview":{"id":"a","type":"test.View"},"api":"Ltest/Settings;->setPair(ILjava/lang/String;)V","arguments":[{}, {"kind":"union","args":[{"literal":"1"},{"literal":"2"}]}, {"kind":"unknown"}]}
        ]}]}
        """).getAsJsonObject();
        JsonObject compact=CompactReport.project(full),counts=compact.getAsJsonObject("counts");
        if(counts.get("webviews").getAsInt()!=2||counts.get("bridges").getAsInt()!=2||counts.get("settings").getAsInt()!=2||counts.get("callbacks").getAsInt()!=1)throw new AssertionError(compact);
        String text=compact.toString();if(!text.contains("\"parameters\":[true]")||!text.contains("\"alternatives\":[\"1\",\"2\"]")||!text.contains("Ltest/Other;"))throw new AssertionError(text);
        Path dir=Files.createTempDirectory("compact-report-test");Main.write(dir.resolve("capabilities.json"),full);
        if(!JsonParser.parseString(Files.readString(dir.resolve("capabilities.compact.json"))).equals(compact))throw new AssertionError("projection differs");
        JsonObject countOnly=CompactReport.countsOnly(compact);
        if(!countOnly.get("counts").equals(compact.get("counts"))||countOnly.toString().contains("->"))throw new AssertionError("counts-only leaks methods or changes totals");
        if(!JsonParser.parseString(Files.readString(dir.resolve("capabilities.counts.json"))).equals(countOnly))throw new AssertionError("counts projection differs");
        if(!Files.readString(dir.resolve("capabilities.counts.json")).contains("\n  "))throw new AssertionError("counts not formatted");
        JsonArray shuffled=JsonParser.parseString("""
          [{"signature":"Z","counts":{"bridges":1,"callbacks":2,"settings":9}},
           {"signature":"B","counts":{"bridges":2,"callbacks":0,"settings":0}},
           {"signature":"Y","counts":{"bridges":1,"callbacks":3,"settings":0}},
           {"signature":"A","counts":{"bridges":1,"callbacks":2,"settings":9}},
           {"signature":"X","counts":{"bridges":1,"callbacks":2,"settings":10}}]
          """).getAsJsonArray();
        JsonArray ranked=CompactReport.sorted(shuffled);
        String names="";for(JsonElement row:ranked)names+=row.getAsJsonObject().get("signature").getAsString();
        if(!names.equals("BYXAZ"))throw new AssertionError("priority order: "+names);
        JsonObject hostCopy=full.getAsJsonArray("activities").get(0).getAsJsonObject().deepCopy();
        hostCopy.addProperty("activity","test.Higher");hostCopy.getAsJsonArray("facts").add(hostCopy.getAsJsonArray("facts").get(2).deepCopy());
        JsonObject extra=hostCopy.getAsJsonArray("facts").get(6).getAsJsonObject();extra.addProperty("implementation","test.Extra");
        full.getAsJsonArray("activities").add(hostCopy);
        JsonArray sortedHosts=CompactReport.project(full).getAsJsonArray("activities");
        if(!sortedHosts.get(0).getAsJsonObject().get("signature").getAsString().equals("Ltest/Higher;"))throw new AssertionError("host sort");
        JsonArray sortedViews=sortedHosts.get(1).getAsJsonObject().getAsJsonArray("webviews");
        if(sortedViews.get(0).getAsJsonObject().getAsJsonObject("counts").get("callbacks").getAsInt()!=1)throw new AssertionError("view sort");
        if(CompactReport.project(new JsonObject()).getAsJsonArray("activities").size()!=0)throw new AssertionError();
        System.out.println("CompactReportTest PASS: receiver isolation, deduplication, settings parameters, union, unknown, fallback, counts, atomic export.");
    }
}
