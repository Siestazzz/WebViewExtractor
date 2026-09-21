package org.example;

import java.util.*;

/** Reproduces scheduler-v1 clearing first-phase-only facts before final snapshot. */
public final class SchedulerPhaseFactLossProbe {
    public static void main(String[] args) {
        CapabilityIndex index = new CapabilityIndex();
        ApkInventory apk = new ApkInventory();
        apk.activities.add("test.A");
        CapabilityEngine engine = new CapabilityEngine(index, apk, System.nanoTime() + 5_000_000_000L);
        CapabilityEngine.ActivityState state = engine.beginActivity("test.A");
        Map<String,Object> fact = new LinkedHashMap<>();
        fact.put("kind", "setting");
        fact.put("name", "phase0-only");
        fact.put("webview", Map.of("id", "w", "type", "android.webkit.WebView"));
        state.phase = 1;
        state.previousFacts.put("phase0-only", fact);
        state.host.facts.clear();
        System.out.println("before_finish=" + ((List<?>) engine.stateReport(state).get("facts")).size());
        engine.finishActivity(state);
        System.out.println("after_finish=" + ((List<?>) state.completedReport.get("facts")).size());
        if (!((List<?>) state.completedReport.get("facts")).isEmpty()) throw new AssertionError("bug no longer reproduces");
    }
}
