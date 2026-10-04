# Activity capability analysis

Current implementation: generic v2 on the resumable scheduler. See [latest iteration](validation/GENERIC_V2.md) for ten-App measurements and limits. Start with [USAGE](../USAGE.md) for commands,
[core design](../CORE_IDEA.md) for the current/legacy distinction, and the
[validation index](validation/README.md) for measured results. This document describes
capability semantics, not an acceptance claim.

For a source-grounded Chinese walkthrough of propagation, binding, sensitivity and rule categories, see the [analysis tutorial](ANALYSIS_TUTORIAL.md).

## Current scheduling and coverage

All Manifest Activity roots receive an initial slice (up to 8 method contexts or a cooperative
50 ms); unfinished states then resume round-robin (up to 100 contexts or 50 ms). Host state
retains queue, arguments, heap/collections, bindings, facts and refinement phase. Single method
work may overrun a slice. These scheduling passes are separate from the internal two-phase
refinement. Recovery is in-memory only; reports cannot restore a killed worker.

`activity_coverage` records not_started, initial_analysis, pending_deep_analysis,
deadline_interrupted, traversal_finished and budget_exhausted, including roots with no emitted
facts. `metrics.initial_pass_activities` is the number given an initial pass;
`processed_activities` includes locally budget-terminated analyses, while
`traversal_finished_activities` excludes those. Pending roots and discarded contexts are explicit.
Finished traversal does not prove all capabilities resolved. See [scheduler v3](validation/SCHEDULER_V3.md)
for exact state transitions, budgets, memory costs and six-App results.

Previous-phase-only facts remain candidate/provisional. Replacement requires compatible receiver
and argument provenance and a non-shrinking member set. Superseded placeholders are retained in
`superseded_provisional_facts`. Candidate retention can increase uncertain host/receiver aliases;
old ownership/error-rate percentages must not be applied to the new output without review.

## Reports and execution

Build and run:

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar
java -Xmx16g -XX:ActiveProcessorCount=8 -jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --apkpath app.apk --out output/app --target-seconds 300 --hard-seconds 600
```

`capabilities.json`, pretty-printed `capabilities.compact.json` and `capabilities.counts.json` are each atomically
replaced during analysis and supervisor finalization; they are not a cross-file transaction.
The compact report retains class/member signatures, Settings parameters, coverage metadata and
per-level counts. Counts include candidates and sum per-WebView entries; Bridge counts are
exposed signatures/class fallbacks, not registration names or runtime object counts.
Unknown Settings parameters use JSON null; branch alternatives stay in their argument position.
See the [compact schema](validation/COMPACT_REPORT.md).
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

Private QNRouter metadata adaptation has been removed. Service returns are resolved only through generic code/data-flow relationships; unknown targets remain unresolved. Standard client callback members now require a matching public SDK family and full method shape, excluding private/static methods. See [generic rules](validation/GENERIC_RULES_V1.md).

A report can be partial even when every Manifest Activity was visited: unresolved entries,
flow/context limits, parser errors and unsupported dynamic behavior remain material limits.
Check `status`, `diagnostics`, `index_diagnostics`, `manifest_diagnostics`, `unattributed` and
per-Activity `limitations`. Empty output is not proof that no capability exists. Normal internal-budget exhaustion can return exit code 0 with a partial report. The supervisor
returns exit code 2 on worker failure/hard timeout while retaining the last valid snapshot;
invalid inputs can fail before creating one.
The output's `metrics` record index time, traversal progress, decoded summaries and budgets.

The new engine indexes all DEX references and instantiates method summaries on demand.
It does not initialize a whole-APK Soot scene. The original implementation and its original
reports remain available with `--legacy`; they are not the quality acceptance oracle.
A separate on-demand Soot fallback is still pending, not silently reported as implemented.

## Reproduce validation

The original three samples/hashes are in `docs/validation/samples.json`; the later six-App
parallel development batch uses `docs/validation/compact-six-samples.json`. Clone users must
supply APKs and build their own JAR; `test/runs/*` binaries/reports referenced by historical
commands are ignored local artifacts, not downloadable repository contents. APKs and complete decompiled sources
are intentionally excluded from Git. Reusable Sol source/DEX evidence lives
under `docs/validation/{news,mango,ctrip}`. Initial source inventories and later report-guided,
source-verified development extensions must be distinguished: the cumulative canonical set is
not a blind holdout. Never construct expected facts from extractor output alone.

```sh
python3 scripts/benchmark.py --label fresh-label --jar test/runs/frozen-version.jar
python3 scripts/evaluate.py --report output/app/capabilities.json \
  --oracle docs/validation/news/canonical-facts.jsonl --out replay.json
python3 scripts/check_deadline.py --jar test/runs/frozen-version.jar \
  --apk test/apks/com.tencent.news.apk
```

`scripts/benchmark.py` is the original three-App **serial** benchmark. The later user-requested
six-App **parallel** development runner is `scripts/run_parallel.py`; see [USAGE](../USAGE.md).
Those concurrent timings do not replace final isolated serial repetitions.
Each benchmark launches a fresh JVM, fixes eight logical CPUs and a 16 GiB maximum heap,
and enforces an external 600-second limit. Use `--repeat 3` on the serial benchmark only (the parallel runner has no repeat option); the OS
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

Generated classes implementing androidx.viewbinding.ViewBinding retain allocation-local
constructor argument captures through bind/inflate returns, including nested custom Views.
This does not infer an object from a field declaration. View aliases across entry parameters,
layout roots and holder fields can still duplicate a single WebView; see OPEN_GAPS.json.

Settings unions are grouped by their underlying WebView alternatives. Keyed Map summaries
retain actual instance identity and Class/literal-key distinctions, including returned Map
contents. Class-keyed registration wrappers require a matching read/write of the same Map field
and exact argument positions. Registrations are followed at actual calls, never used as global
capability seeds. Opaque Map parameters do not create a shared instance. Dynamic keys and
mutation/replacement ordering remain unresolved diagnostics; the model reports possible
registrations rather than proving a final runtime Map state.

Scoring revision 5 enforces explicit `webview_constraint: {"types": [...]}` from independent
source facts. An identical API/value/member on a different WebView type in the same Activity
cannot satisfy that expectation. A reported union may satisfy the constraint through one of its
explicit alternatives, so the result remains candidate-inclusive. `webview_constraint_coverage`
exposes facts still scored only at Activity level. Concrete type agreement does not establish
exact allocation identity. Source mappings and pre-migration oracle snapshots are versioned;
historical scores must be replayed on the same constraints before comparing versions.

Small actual object-field setter calls preserve their argument even when not globally relevant.
Virtual helper lookahead is bounded and uses the actual receiver subtype; it does not enumerate
unallocated sibling implementations. Mutable fields do not provide closed-world negative type
proofs for pruning other conditional branches. Captured arrays retain bounded indexed cells as
well as collection contents; unknown indices/updates are conservative and diagnosed.

The AspectJ adapter recognizes runtime closure contracts, captures actual constructor state,
links the concrete closure to its join point, and dispatches no-argument `proceed` to that closure's
`run`. Construction or linking alone does not execute the closure. Original join-point aliases
are retained when resolved. Different event captures can still alias under bounded allocation
contexts, and repeated attachment is conservative rather than a proof of final order.
`proceed(Object[])` state/flag rewriting remains unsupported and explicitly diagnosed; no App
Activity names are hardcoded into this adapter.

Pending contexts are deduplicated and scheduled round-robin by method, preventing one widely
parameterized helper from occupying the whole worklist. Traversal is limited by context,
component, queue, summary/evaluation and external time budgets. The presentation length of an
evidence path does not stop traversal: paths longer than 64 steps retain the root and tail with
an omission marker and `evidence_path_truncated`. Such an abbreviated path alone is not a complete
binding proof; inspect retained source/DEX evidence and limitations when auditing it.
