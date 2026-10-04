# Generic analysis iteration 2: bounded scheduling and constant-discriminator branches

This iteration excludes private methods from standard Client callback contracts, introduces bounded priority scheduling for known-receiver capability tasks, and addresses source-confirmed synthetic Runnable discriminator branch mixing. It does not use private App names or signatures.

## Scheduling

Priority and ordinary lanes each retain round-robin method scheduling. At most three priority tasks execute before one ordinary task while both lanes are populated. This changes order, not the set of enqueued tasks or the global/local budgets. Hint evaluation must not execute factories or infer new objects. Unknown receivers stay ordinary; a union containing a concrete receiver may receive priority without discarding unknown alternatives. Hint failure falls back to ordinary work with a diagnostic. Existing queue caps still apply and are not eliminated by this change.

## Evidence-driven branch work

The Breaking News source reviewer confirmed three erroneous FreeData capability attributions caused by synthetic Runnable classes that use a final integer discriminator. Constructors provide known constants, but the former summary traversed every switch case. The ownership sample identifies wrong capability-host pairs, not Activities proven to have no WebView. Packed/sparse switch now selects only a matching case or default when an exact int is known. Both forms, default handling, exception edges, mutable/unknown/union selectors and specialization-budget fallback are covered by regression tests. The method-size (500 instructions) and variant (16) limits remain unchanged; overflow emits summary_refinement_budget. The frozen v1 artifact fails the ownership fixture; v2 passes. Dynamic or mutable discriminators must retain conservative alternatives.

## Validation and limits

The full synthetic suite, export tests and shadowJar build passed. The external watchdog test also verified all three JSON reports remain parseable and agree in final status; elapsed approximately 1.29s for a forced 1s deadline. Ten fresh analyses completed inside 600s, each with 8 disjoint CPUs and 16 GiB heap, target300/supervisor595/external600. All reports remain partial. See generic-v2-tests.json, generic-v2-deadline.json, generic-v2-structure.json, generic-v2-summary.json and generic-v2-environment.json.

Compared on identical cumulative facts, News and Ctrip retain all previously matched facts; Mango gains 14 Bridge expectations with no lost previous matches. All four new-App combined sets retain prior matches, but Sohu gains none. Source review confirms the three audited Breaking News FreeData misattributions disappeared; this does not prove all remaining host facts correct. Some residual settings/operation paths remain suspect or provisional. Existing private-adapter removal regressions versus scheduler-v3 remain retained; this version does not restore the adapter.

## Measured final reports

| Package | Seconds | Activities output | Bridge entries | Callbacks | Settings | Pending hosts |
|---|---:|---:|---:|---:|---:|---:|
| com.cctv.yangshipin.app.androidp | 48.0 | 24 | 53 | 13151 | 1168 | 0 |
| com.dragon.read | 592.2 | 47 | 157 | 2526 | 805 | 294 |
| com.freereels.app | 593.8 | 111 | 2046 | 5921 | 8463 | 81 |
| com.hunantv.imgo.activity | 447.5 | 113 | 27616 | 18320 | 12481 | 0 |
| com.news.app.global | 591.2 | 40 | 80 | 4214 | 388 | 40 |
| com.sohu.sohuvideo | 593.8 | 301 | 3567 | 22762 | 4305 | 297 |
| com.ss.android.article.video | 591.3 | 36 | 66 | 763 | 418 | 344 |
| com.tencent.news | 141.6 | 33 | 11872 | 1380 | 1450 | 0 |
| com.tencent.weishi | 45.9 | 15 | 4 | 514 | 263 | 0 |
| ctrip.android.view | 416.1 | 55 | 15178 | 1597 | 1254 | 0 |

Counts include candidates, repeated symbolic aliases and class fallbacks. Sohu's output counts increased without improving the audited core facts; raw totals are not a recall measure. FreeReels output host count decreased despite more completed traversals; source truth is not established for those changes. Every added/removed host is retained in generic-v2-effects.json as unresolved unless separately audited.

## Same-set candidate-inclusive recall

| App | Bridge matched/expected | Settings matched/expected | Callbacks matched/expected |
|---|---:|---:|---:|
| news | 1260/1393 | 228/366 | 185/240 |
| mango | 1221/1259 | 608/608 | 637/662 |
| ctrip | 3682/3970 | 375/391 | 421/444 |
| txws | not sampled | 68/68 | 100/100 |
| sohuvideo | 6/115 | 37/88 | 17/99 |
| fanqiexiaoshuo | 12/12 | 21/21 | 16/16 |
| breaking-news | 10/10 | 14/14 | 8/8 |

The new-App sets cover only 5 Txws, 6 Sohu, 3 Fanqie and 3 Breaking News capability-positive hosts, plus separately documented ownership spot checks. Txws has no Bridge positive in this fact set. Expanded rows are report-guided and source-verified, not sealed holdouts. No all-output false-positive rate or whole-APK completeness is claimed. Original 30-positive-host/holdout/three-isolated-repeat requirements remain incomplete; the user explicitly permits lower recall instead of private adapters.

Sohu's core set remains Bridge0/36, Settings9/26, callbacks3/29. Its core/Farm/Pgc hosts are still phase-one deadline interruptions with no queued priority work at final inspection. This iteration did not solve that bottleneck. Enqueue-time priority hints do not automatically reclassify jobs when fields resolve later. Broad entry seeding, receiver inference, lifecycle ordering and pending-host heap cost remain open.

## Near-target snapshots

scripts/capture_checkpoints.py retained snapshots near the 300s target and completed reports for short runs. The observed snapshot times are approximately 295–306s for continuing analyses, so those over300 are NOT proof of meeting a strict 300s deadline. See generic-v2-target300-index.json and generic-v2-target300-*.json. Fanqie's 12 expected Bridge facts were still absent in its near-target snapshot and present at final; Sohu remains incomplete even at final.

## Artifacts and reproduction

Final user-facing bundles contain only sorted compact JSON or counts-only JSON, one directory per package. Full reports remain local under test/runs/generic-v2 for evidence replay. Deliveries are test/runs/generic-v2/delivery/capabilities-compact.zip and capabilities-counts.zip. APKs, runtime reports and ZIPs are ignored by Git; sample hashes and measurements are versioned.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v2
python3 scripts/check_compact.py test/runs/generic-v2
python3 scripts/check_deadline.py --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --apk test/apks/txws-patched.apk
python3 scripts/evaluate.py --report test/runs/generic-v2/com.sohu.sohuvideo/capabilities.json --oracle docs/validation/sohuvideo/generic-evaluation-facts.jsonl --out output/sohu-score.json
```

To capture near-target snapshots during a fresh run, launch capture_checkpoints.py with `--batch`, `--samples`, `--out` and `--seconds 295` in parallel. It observes already-written reports and does not reuse analysis results. Iteration1 and iteration2 use fresh JVMs; these remain concurrent development measurements, not isolated repeat-performance acceptance.
