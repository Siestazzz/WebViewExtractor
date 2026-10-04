# Selected actual service-factory replay

This diagnostic preserves actual packaged method bodies but indexes only ServiceManager, IServiceFactory and BrowserServiceFactory. It invokes createFactory(String) with the exact source-confirmed interface binary name. No full index, Application/Activity roots, plugin restoration or WebView attribution is performed. Artifact hashes, input, timing and complete before/after/absence results are recorded in the companion JSON.

Frozen v18 returns unknown in 1.95 seconds. Frozen v19 returns BrowserServiceFactory from the actual offset 572 in 2.50 seconds, with one refined summary and 223 certified metadata entries. The independent source reviewer proved actual integer case 130 -> this constructor and return. A different absent metadata key returns null in 2.65 seconds; it does not become integer zero or an arbitrary factory. These observations establish that the selected Map/boxing/switch prefix now passes. They do not establish the later cache/wrapper/Application/plugin or Browser Scene installation chain.

Reproduction uses test/diagnostics/SelectedFactoryReplayProbe.java compiled against the frozen jar. The classpath begins with only the diagnostic classes; no current production class directory overlays the frozen version:

```sh
javac -cp test/runs/generic-v19.jar -d test/runs/xigua-v19-selected-factory/classes test/diagnostics/SelectedFactoryReplayProbe.java
timeout 25 taskset -c 32-39 java -Xmx4g -XX:ActiveProcessorCount=8 \
  -cp test/runs/xigua-v19-selected-factory/classes:test/runs/generic-v19.jar \
  org.example.SelectedFactoryReplayProbe test/apks/com.ss.android.article.video.apk \
  test/runs/xigua-v19-selected-factory/after-final.json \
  'Lcom/jupiter/builddependencies/dependency/ServiceManager;->createFactory(Ljava/lang/String;)Lcom/jupiter/builddependencies/dependency/IServiceFactory;' \
  com.ixigua.browser.protocol.IBrowserService \
  com.ixigua.browser.specific.impl.BrowserServiceFactory \
  com.jupiter.builddependencies.dependency.ServiceManager \
  com.jupiter.builddependencies.dependency.IServiceFactory \
  com.ixigua.browser.specific.impl.BrowserServiceFactory
```

For the baseline replace the classpath jar with generic-v18.jar and the expected result argument with diagnose. For the absence case use diagnostic.AbsentService as the input and diagnose as the expected result. Generic production rules contain none of these application identifiers.
