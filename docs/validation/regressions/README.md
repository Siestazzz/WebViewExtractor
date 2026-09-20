# Preserved regression fixtures

These fixtures preserve previously demonstrated defects and their versioned status.

`LifecycleFixture.java` reproduces the v11c long-video binding defect without app-specific names:
a base Activity creates a concrete page, calls an interface-typed field setter/getter, and forwards
that object to a subclass callback. The setter and getter are excluded from relevance. A direct
one-method page callback would be independently seeded by the existing near-capability callback
heuristic and hide this defect, so the fixture uses three ordinary wrapper calls before the bridge
registration. Constructor settings are still found; the callback's bridge is missing.

Reproduction from repository root after building capabilitySelfTest and shadowJar:

```sh
javac -cp build/libs/webview_extractor-1.0-SNAPSHOT-all.jar:build/classes/java/test -d test/runs/debug docs/validation/regressions/LifecycleFixture.java
java -cp build/libs/webview_extractor-1.0-SNAPSHOT-all.jar:build/classes/java/test:test/runs/debug org.example.LifecycleFixture
```

Observed with frozen v11c: `setterRelevant=false, getterRelevant=false`, exit1 with
`Base lifecycle setter/getter lost concrete page argument to subclass override`.
This must become a passing regression after actual binding repair; do not delete it to improve
validation status. Source evidence: news/v11b-validation.md. Debug heap/index evidence separately
confirms news setPage/getPage are excluded while its inherited onCreate is visited.

In v12 development, the unchanged fixture passes after actual instance setters retain their
reference argument. It is also included as inheritedPageArgumentFixture in capabilitySelfTest.
The global relevance flags can remain false: the new rule follows the setter at its real call,
rather than making every setter an Activity entry. This synthetic recovery alone does not prove
that the actual news long-video framework has been fully repaired.


`CompactProbe.java` is an optional bounded single-host diagnostic driver (180 seconds of internal
analysis budget). It uses the real Manifest/SDK/layout inventory and reports the APK hash. It is
not a performance or completeness acceptance run. Compile against a frozen production jar:

```sh
javac -cp test/runs/v12i.jar -d test/runs/debug docs/validation/regressions/CompactProbe.java
ANDROID_HOME=/home/d3008/phy/workspace/languages/Android/Sdk timeout --kill-after=2 190 taskset -c 0-7 java -Xmx16g -XX:ActiveProcessorCount=8 -cp test/runs/v12i.jar:test/runs/debug org.example.CompactProbe test/apks/com.hunantv.imgo.activity.apk com.mgtv.ui.videoplay.MGVideoPlayActivity test/runs/mg-diagnostic.json 'NBFloatFragmentHelper;->F1'
```

Historical v12e/f/g local probes used an empty inventory (targetSdk=0, no layout inventory), so
only their method reachability/budget diagnostics were used to guide repairs. Their capability
counts are not acceptance evidence. The checked-in driver corrects this limitation. Full frozen
APK reports and source-selected canonical facts remain the basis of all per-App metrics.

New automated v12 fixtures also cover mutable-field type guards, concrete-receiver overrides
behind irrelevant base helpers (with an unallocated sibling negative), indexed captured arrays,
linked/no-proceed/unlinked AspectJ closure isolation, original join-point aliases, distinct targets
sharing one static join point, unsupported proceed(args) versus an unrelated array-taking member,
duplicate pending-context suppression, and fairness across methods with many argument contexts.


The deepEvidenceFixture adds a 72-helper call chain followed by actual Fragment creation. It
checks both direct and Fragment-owned capabilities, bounded 64-step evidence presentation, and
an explicit omission diagnostic. Context and external deadline budgets still apply; a long
presentation path no longer silently removes a reachable capability from analysis.


`LayoutBindingFixture` (v13) covers actual resource-table paths and normal/sparse/offset16/compact
entries, aliases and malformed bounds; scoped reused view IDs; real inflate/addView wrappers;
nested XML constructor/field/getter chains; concrete subtype preservation through widening casts;
merge attachment; static inflate-site separation; owner-specific deferred fields and cycles;
uncalled XML helper exclusion; and actual WebView API override registration. These are structural
regressions, not substitutes for full source ownership audits. Configuration alternatives, repeated
runtime allocations at one site and heap-write temporal order remain candidate approximations.
