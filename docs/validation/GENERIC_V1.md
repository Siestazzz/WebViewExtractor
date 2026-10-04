# Generic analysis iteration 1: private adapter removal and sorted reports

This iteration removes the private QNRouter adapter, tightens standard Client family/signature recognition, and adds sorted capability/counts projections. Frozen executable: `test/runs/generic-v1.jar`; its hash and resource configuration are in generic-v1-environment.json. The later private-access callback fix is NOT in this frozen build and is tracked for iteration 2.

## Scope and execution

The four new samples are user-selected patched APKs, with hashes verified against predecompiled build metadata. The earlier six retain their established regression hashes. Breaking News and FreeReels are base-APK scope. Source directories remain read-only. Original-version exploratory runs were interrupted after the user selected patched APKs and are excluded from scoring. Each run uses 8 disjoint CPUs, 16 GiB heap, target 300s, supervisor595/external600s. These are concurrent development measurements, not isolated performance acceptance repeats. The four baseline analyses and ten regression analyses overlapped on disjoint CPU sets; shared memory/I/O and small test workloads can affect timings.

## Results

| Package | Seconds | Output Activities | Bridge entries | Callbacks | Settings | Pending hosts |
|---|---:|---:|---:|---:|---:|---:|
| com.cctv.yangshipin.app.androidp | 50.1 | 24 | 61 | 13070 | 1168 | 0 |
| com.dragon.read | 591.9 | 47 | 144 | 1799 | 733 | 294 |
| com.freereels.app | 593.4 | 128 | 1578 | 4982 | 7411 | 105 |
| com.hunantv.imgo.activity | 435.0 | 113 | 20538 | 19141 | 12889 | 0 |
| com.news.app.global | 592.5 | 41 | 2912 | 17612 | 4205 | 41 |
| com.sohu.sohuvideo | 592.5 | 299 | 2975 | 12510 | 610 | 297 |
| com.ss.android.article.video | 591.5 | 36 | 66 | 763 | 418 | 344 |
| com.tencent.news | 137.5 | 33 | 11872 | 1373 | 1448 | 0 |
| com.tencent.weishi | 47.5 | 15 | 4 | 514 | 263 | 0 |
| ctrip.android.view | 418.3 | 55 | 14278 | 1597 | 1254 | 0 |

All reports are partial; displayed counts include candidates and aliases and are not correctness measurements. The new-App source audits are deliberately small, not full acceptance. Source-first snapshots and report-guided expansions remain distinguishable under each App directory.

## Original three source-oracle replay

| App | Bridge matched/expected | Settings matched/expected | Callbacks matched/expected |
|---|---:|---:|---:|
| news | 1260/1393 | 228/366 | 185/240 |
| mango | 1207/1259 | 608/608 | 637/662 |
| ctrip | 3682/3970 | 375/391 | 421/444 |

News loses 80 previously matched facts following private adapter removal and callback tightening. Mango loses 9 callback expectations, all legacy openFileChooser overloads lacking accepted SDK declaration evidence; no facts were removed from its oracle to conceal this regression. Ctrip's matched cumulative facts are unchanged. Candidate-inclusive matching is not an all-output precision audit. The standard callback policy now favors supported public contracts over guessing hidden overloads.

## Tests and remaining work

`capabilitySelfTest`, `compactReportTest`, and `shadowJar` passed for the frozen v1 build. Export tests cover count preservation, deduplication, Settings parameters, sort priorities, independent WebViews and counts-only export. Ten report structural checks passed. External watchdog smoke test retained a valid report at about 1.27 seconds. Checker negatives reject empty input and duplicated/missing hosts. An independent review found private methods need exclusion from virtual callback contracts; the correction is deferred to v2 with a regression fixture.

Pending hosts in large Apps remain a cost limitation. Unknown field/factory propagation and broad entry seeding can over-approximate. No private names are reintroduced to recover recall. Current all-output precision, 30-positive-host-per-new-App coverage, independent final holdouts and three isolated repeats are not achieved. Early checkpoint misses that later resolve are not counted as persistent semantic defects.

## Reproduction

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v1.jar --out test/runs/generic-v1
python3 scripts/check_compact.py test/runs/generic-v1
python3 scripts/evaluate.py --report test/runs/generic-v1/com.tencent.news/capabilities.json --oracle docs/validation/news/canonical-facts.jsonl --out output/news-score.json
```

Frozen jars and APKs are local ignored artifacts. Rebuild from this iteration's source to reproduce; sample manifests record exact inputs. Final user delivery is the sorted compact and counts-only JSON; detailed reports remain available locally for evidence replay.
