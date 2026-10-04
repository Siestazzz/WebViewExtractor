# News frozen v13 bootstrap local-budget overrun

An isolated actual-APK run reproduced the one-second bootstrap budget overrun: bootstrap wall time **12.405s**, main-thread CPU **12.155s**, one context and zero static heap entries. Inventory/index time57.047s was measured separately. No Activity analysis or ten-App run was started.

The observed call chain is:

```text
ApplicationBootstrap.build:33
CapabilityEngine.processJob:244
ReflectionProtocols.writer:68
ReflectionProtocols.discoverFields:62
Map-call anyMatch / CapabilityIndex.map / subtype / dex string decode
```

All503 samples in the retained top30 distinct stack groups contain `discoverFields`. Of those,270 also contain `DexFlow.decode`, nested under discovery's `plans/flow.summary` path. These counts describe retained groups, not an exact CPU percentage. The first root is `TNApplication.<init>()V`: frozen bootstrap bytecode orders constructor before attach/create and the completed diagnostic records only one processed context.

The hidden cost is global lazy discovery invoked by a target relevance predicate inside that first context. Frozen discovery scans indexed methods for reflective plans, then scans Map put call sites with repeated hierarchy queries. Base decoding uses the shared/global deadline. The outer bootstrap queue deadline does not interrupt this operation at one second. Queue size is not used as a CPU-time explanation.

A generic correction should propagate the local deadline into discovery and decoding, check it inside expensive per-method/per-call operations, and preserve partial-discovery status rather than caching incomplete negatives. Cheap exact signature tests should precede repeated subtype work. Shared discovery accounting must be explicit. This is separate from semantic bootstrap support.

Reproduce:

```sh
javac -cp test/runs/generic-v13.jar -d test/runs/news-bootstrap-probe-v13/classes test/diagnostics/BootstrapTimingProbe.java
java -Xmx8g -cp test/runs/news-bootstrap-probe-v13/classes:test/runs/generic-v13.jar org.example.BootstrapTimingProbe test/apks/com.tencent.news.apk
```

Raw stack groups, wall/CPU timings and frozen `javap -p -c -l` evidence reside in `test/runs/news-bootstrap-probe-v13`. Artifact hashes and limitations are recorded in the sibling JSON. Current production source changed during sampling, so the frozen jar and its bytecode are authoritative. This single isolated run is diagnosis, not performance acceptance or actual bootstrap-state recovery.
