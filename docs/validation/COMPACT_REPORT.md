# Compact capability report

Current default output is pretty-printed with two-space indentation and a trailing newline. Coverage metadata includes initial-pass, finished, locally limited and pending counts, discarded contexts and provisional facts. See [USAGE](../../USAGE.md) and [scheduler v3](SCHEDULER_V3.md). The six-App measurements further below are the historical **pre-scheduler compact-six baseline**, not the latest results.

`capabilities.compact.json` is now exported beside `capabilities.json` at every checkpoint and supervisor finalization. Both files are individually atomically replaced; they are not a transactional pair. Analysis is unchanged by this projection. Full evidence and diagnostic messages remain in the detailed report.

Structure: metadata/counts/activities → signature/counts/webviews → signature/counts/bridges/settings/callbacks. Activity and WebView signatures use DEX class descriptors. Bridge and callback entries use member descriptors, falling back to the implementation descriptor if members cannot be resolved. `unknown` explicitly represents an unavailable implementation.

Settings entries contain `signature` and ordered `parameters`. Booleans use JSON booleans; other known literals retain the analyzer's string representation (floating-point encoded bits are not reinterpreted). Unknown expressions are JSON null, not a proven Java null. A branch union is `{ "alternatives": [...] }` within its original argument position. This preserves multi-argument positions rather than flattening the analyzer's `values` list. Settings are observed configurations, not final runtime values.

Counts at the report, Activity and WebView levels include `activities`, `webviews`, `bridges`, `settings`, `callbacks`. Bridge counts mean displayed exposed-member signatures (or unresolved class entries), **not JavaScript registration names or bridge objects**. Settings count distinct signature-plus-parameters entries; callbacks count distinct signatures. Each WebView is deduplicated separately. Activity/report counts sum their children and are not global unique-method counts. Candidate bindings are included. Distinct WebView identities remain separate even when they have identical class signatures; the compact projection intentionally omits their internal object IDs. Entries can be empty when a WebView only has other operations, which remain in the full report. Registration/removal ordering, loadUrl operations, binding evidence and diagnostics are omitted from compact lists.

Validation: `./gradlew compactReportTest capabilitySelfTest shadowJar --offline --console=plain`. Projection regression verifies duplicate methods, two receivers of one class, unresolved bridge fallback, Boolean settings, multi-argument branch alternatives/unknowns, recursive counts and on-disk export.

## Sorted capability and counts projections (schema 2)

Current compact metadata uses schema_version 2. Both Activity and nested WebView lists sort by bridges descending, callbacks descending, settings descending, then class signature ascending. Ties preserve input order; receivers are not merged. `capabilities.counts.json` is a deep projection of the sorted compact report with each WebView's bridges/settings/callbacks arrays removed. It retains metadata, class signatures, hierarchy and exactly the same counts. Both projections are pretty-printed and independently atomically replaced alongside the detailed report; the three files are not a transaction.

Counts-only output is not a new analysis or a precision claim. Candidates and unresolved class fallbacks contribute exactly as in compact output. Its metadata uses projection=counts_only. `check_compact.py` checks unique/complete host membership, sorting, recursive counts and counts-projection equality; no input reports is an error.

## Historical compact-six parallel run

Sample provenance/hashes: `compact-six-samples.json`. Original files are read-only sources, copied under ignored `test/apks/`. Package/version identities checked using Android SDK aapt. FreeReels uses its base APK only, excluding resource/native-library splits. Xigua uses original.apk, not the patched variant.

Run:

```sh
python3 scripts/run_parallel.py \
  --samples docs/validation/compact-six-samples.json \
  --jar test/runs/compact-six.jar \
  --out test/runs/compact-six
```

Six fresh analyses run simultaneously, with disjoint sets of eight logical CPUs and 16 GiB heap each. Each supervisor gets 595 seconds inside a 600-second external timeout. This is an explicitly requested concurrent batch, not the original isolated serial acceptance benchmark. Results are development output, not independent completeness validation of the three new Apps.

At the time of this historical measurement, the frozen jar was built from the working tree, including the then-uncommitted v17 prototype described in the preceding development work (actual registered handlers, Runnable forwarding and expression work budget), plus compact export. It is **not** the v15 or v16e binary. Exact source snapshot is retained at ignored `test/runs/compact-six-source.tar.gz`; frozen jar SHA-256 and runtime configuration are recorded in the batch environment file. Underlying extractor changes have not been claimed as accepted.

## Historical compact-six measured results

All six fresh processes exited normally, but all reports retain `partial` because diagnostics remain. Xigua and FreeReels exhausted the internal analysis budget: normal exit does not mean full coverage. The other four completed Activity traversal, not proof of complete capability recall.

| Package | Seconds | Processed / manifest | Output hosts | Bridge entries | Settings | Callbacks | Size reduction |
|---|---:|---:|---:|---:|---:|---:|---:|
| com.cctv.yangshipin.app.androidp | 49.4 | 229/229 | 24 | 50 | 694 | 10362 | 84.6% |
| com.freereels.app | 591.0 | 33/164 | 30 | 156 | 685 | 548 | 97.5% |
| com.hunantv.imgo.activity | 411.1 | 657/657 | 85 | 17571 | 10187 | 17533 | 96.7% |
| com.ss.android.article.video | 591.0 | 18/628 | 17 | 29 | 161 | 137 | 96.6% |
| com.tencent.news | 175.0 | 2329/2329 | 40 | 10651 | 1450 | 1113 | 92.2% |
| ctrip.android.view | 426.6 | 410/410 | 55 | 14266 | 1254 | 1591 | 96.0% |

Counts can be inflated by analyzer receiver aliases and candidate bindings. They are display counts, not independently verified unique runtime objects/capabilities. Full-batch structural checks passed: detailed-host/receiver preservation, list deduplication and recursive counts. See compact-six-structure-check.json, compact-tests.json and compact-six-results.json.
