package org.example;

import java.util.*;

/** Reproduces two over-broad scheduler-v2 provisional refinement cases. */
public final class SchedulerV2RefinementBoundaryProbe {
    static Map<String,Object> bridge(String implementation, DexFlow.V object) {
        Map<String,Object> fact = base("bridge", "Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V", implementation);
        fact.put("arguments", List.of(DexFlow.V.of("object", "android.webkit.WebView", "wv"), object,
                DexFlow.V.literal("java.lang.String", "slot")));
        return fact;
    }

    static Map<String,Object> registry(DexFlow.V receiver) {
        Map<String,Object> fact = base("message_bridge", "Ltest/Registry;->put(Ljava/lang/String;Ljava/lang/Object;)V", "test.Plugin");
        fact.put("arguments", List.of(receiver, DexFlow.V.of("object", "test.Plugin", "plugin"),
                DexFlow.V.literal("java.lang.String", "slot")));
        return fact;
    }

    static Map<String,Object> base(String kind, String api, String implementation) {
        Map<String,Object> fact = new LinkedHashMap<>();
        fact.put("kind", kind);
        fact.put("site", "Ltest/A;->register()V@1");
        fact.put("api", api);
        fact.put("webview", Map.of("id", "wv", "type", "android.webkit.WebView"));
        fact.put("registration_name", "slot");
        fact.put("implementation", implementation);
        fact.put("members", List.of());
        return fact;
    }

    public static void main(String[] args) {
        CapabilityEngine engine = new CapabilityEngine(new CapabilityIndex(), new ApkInventory(), System.nanoTime() + 2_000_000_000L);
        CapabilityEngine.Host host = engine.new Host("test.A");
        DexFlow.V opaque = DexFlow.V.of("unknown", "java.lang.Object", "opaque-without-lookup-source");
        boolean noSource = engine.refinesFact(host, bridge("unknown", opaque), bridge("test.Plugin", opaque));
        boolean differentRegistry = engine.refinesFact(host,
                registry(DexFlow.V.of("object", "test.Registry", "registry-1")),
                registry(DexFlow.V.of("object", "test.Registry", "registry-2")));
        System.out.println("refines_without_source=" + noSource);
        System.out.println("refines_different_registry=" + differentRegistry);
        if (!noSource || !differentRegistry) throw new AssertionError("Known boundary bug no longer reproduces");
    }
}
