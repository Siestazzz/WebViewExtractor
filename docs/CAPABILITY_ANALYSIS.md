# Activity capability analysis

Build and run:

```sh
./gradlew capabilitySelfTest shadowJar --offline --console=plain
java -Xmx16g -XX:ActiveProcessorCount=8 -jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --apkpath app.apk --out output/app --target-seconds 300 --hard-seconds 600
```

`capabilities.json` is atomically replaced during analysis and when the supervisor ends.
Every Activity contains `facts`, plus `webviews` grouping those facts by symbolic receiver identity;
Different symbolic IDs can still alias the same runtime WebView; current H5 helper/factory
alias reconciliation is incomplete. Do not interpret group count as a count of distinct views.
`capability_indices` indexes the enclosing Activity's facts without duplicating member lists.
Facts include DEX signatures, registration names, implementation classes, settings values,
client implementations, call/field evidence and explicit/candidate status. An explicit static
binding is not proof of runtime execution. Settings describe observed operations, not final
runtime state; conditional branches and later replacements can change the runtime result.

`message_bridge` covers recognized shared registries and reflective dispatch surfaces.
A namespace registration is separate from its injected transport. The empty namespace is
represented by an empty string, not a fabricated registration name. Unknown names and
objects are retained. Native bridge removal and client removal are separate operations.

For recognized QNRouter metadata, `service_bindings` records the exact lookup arguments,
registration sites and implementation types. These bindings remain candidates: packaged metadata
alone does not prove initialization, replacement order or successful runtime construction. A custom
unresolved creator is not replaced with the registered default implementation. No arbitrary service
subtype enumeration is used. Null/empty lookup qualifiers select the framework default, whereas
empty registration keys remain distinct.

A report can be partial even when every Manifest Activity was visited: unresolved entries,
flow/context limits, parser errors and unsupported dynamic behavior remain material limits.
Check `status`, `diagnostics`, `index_diagnostics`, `manifest_diagnostics`, `unattributed` and
per-Activity `limitations`. Empty output is not proof that no capability exists. The supervisor
returns exit code 2 on worker failure/hard timeout while retaining the last valid snapshot.
The output's `metrics` record index time, traversal progress, decoded summaries and budgets.

The new engine indexes all DEX references and instantiates method summaries on demand.
It does not initialize a whole-APK Soot scene. The original implementation and its original
reports remain available with `--legacy`; they are not the quality acceptance oracle.
A separate on-demand Soot fallback is still pending, not silently reported as implemented.

## Reproduce validation

Samples and hashes: `docs/validation/samples.json`. APKs and complete decompiled sources
are intentionally excluded from Git. Reusable independent Sol source/DEX evidence lives
under `docs/validation/{news,mango,ctrip}`. Never construct expected facts from extractor output.

```sh
python3 scripts/benchmark.py --label fresh-label --jar test/runs/frozen-version.jar
python3 scripts/evaluate.py --report output/app/capabilities.json \
  --oracle docs/validation/news/canonical-facts.jsonl --out replay.json
python3 scripts/check_deadline.py --jar test/runs/frozen-version.jar \
  --apk test/apks/com.tencent.news.apk
```

Each benchmark launches a fresh JVM, fixes eight logical CPUs and a 16 GiB maximum heap,
and enforces an external 600-second limit. Use `--repeat 3` for repeat measurements; the OS
page cache is not flushed, and the first launch is distinguished from subsequent launches.
Benchmark and environment JSON record commands, APK/JAR hashes, peak RSS, phases and status.

The evaluator reports both explicit-only and candidate-inclusive per-category fact recall.
It does not establish complete APK coverage or correct WebView identity by itself. Acceptance
also needs independent review of every emitted Activity, wrong-capability associations,
unknown ownership, full framework coverage, held-out cases and isolated repeated timings.
The current acceptance state and retained regressions are recorded in `validation/ITERATIONS.md`.

Scoring revision 2 deduplicates semantic oracle rows without deleting source evidence, checks
Settings API owners, and separates Bridge registrations from exposed members. A known registration
with unknown target cannot count as a complete capability surface: it remains an unresolved miss
in aggregate coverage. `capability_granularity` shows registration/member scores independently.
Object identity and false capability associations still require independent review; these scores
must not be described as complete end-to-end binding accuracy. Historical revision-1 scores remain
available and are not directly comparable without replaying the same oracle and evaluator.

Scoring revision 3 additionally recognizes a handler selector inside a separately injected native
transport namespace, only with an explicit `transport_bindings` edge carrying the same native
Bridge object and WebView identity. Selector, implementation and full endpoint signature must
still match. Namespace text alone cannot create a match. Historical scores need same-oracle replay.

Kotlin delayed initialization recognizes standard or obfuscated Kotlin interfaces by their
`getValue`/`isInitialized` and zero-argument `invoke` contracts. Actual initializer objects are
substituted per factory call; unrelated initializer implementations are not enumerated. Unknown
initializers remain unresolved. Factory recognition is limited to Kotlin namespace static methods whose returned allocation
passes the exact initializer parameter into a constructor field capture (including delegation).

Custom callback setters are recognized structurally: the receiver is a WebView subtype, the
listener argument is stored in an instance field, and that same field has a contract dispatch
site. Inherited contract members are included. `callback_field`, `callback_contract` and
`dispatch_status` describe the evidence. `observed` means a matching static dispatch site was
found, not proof of runtime invocation; `unresolved` is not proof of absence.

The default engine reads Android SDK class headers to resolve platform inheritance such as
FrameLayout -> ViewGroup -> View. It selects the highest numbered installed SDK platform from
ANDROID_HOME / ANDROID_SDK_ROOT or local.properties. `platform_hierarchy_source` and
`platform_hierarchy_classes` record the selection. SDK methods are not loaded or traversed.
Missing SDK metadata leaves an explicit index diagnostic. Pure WebView/carrier getters are
included when determining required field writers and wrapper initialization.

Scoring revision4 adds strict callback-registration checks (setter API and concrete Client type),
keeps callback members as separate evidence, and reports WebView operations and positive-host
recall independently. Operation matches cannot inflate the three capability categories. The
expanded NFT development oracle demonstrates why an unchanged old score cannot establish that
an entire framework's capability surface was retained.
