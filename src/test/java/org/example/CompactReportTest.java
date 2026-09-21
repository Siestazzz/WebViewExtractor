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
        if(CompactReport.project(new JsonObject()).getAsJsonArray("activities").size()!=0)throw new AssertionError();
        System.out.println("CompactReportTest PASS: receiver isolation, deduplication, settings parameters, union, unknown, fallback, counts, atomic export.");
    }
}
