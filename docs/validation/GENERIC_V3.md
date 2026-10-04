# Generic v3: stricter component entry and instance construction

**Experimental iteration; not accepted.** Synthetic correctness improves, but real-App recall regresses. The next iteration must repair actual lifecycle, constructor and dispatch paths without restoring arbitrary helper seeding or private App adapters.

Implementation and positive/negative tests: [GENERIC_V3_IMPLEMENTATION.md](GENERIC_V3_IMPLEMENTATION.md). Frozen jar SHA-256: `c81d5de49a1c79b0895e5f9f285f771032f97c85539a29a03eaa95ba0bd318c2`.

## Fresh ten-App measurements

Runs used ten concurrent processes with disjoint 8-CPU affinities and 16 GiB heaps, target300 / supervisor595 / external600. These are development comparisons, not the required three isolated acceptance runs. All final reports are partial or timeout. Sohu and Ctrip hit the supervisor deadline and retained valid reports; this is not a quality pass. Sohu had two diagnostic jcmd thread samples, so its runtime also has a small observation perturbation.

| App | Seconds | Hosts output | Pending | Bridge | Settings | Callbacks |
|---|---:|---:|---:|---:|---:|---:|
| news | 99.5 | 33 | 0 | 1260/1393 | 228/366 | 185/240 |
| mango | 126.4 | 92 | 0 | 1184/1259 | 536/608 | 536/662 |
| ctrip | 594.7 | 53 | 6 | 3682/3970 | 375/391 | 421/444 |
| xigua | 591.5 | 39 | 342 | not sampled | 0/35 | 0/20 |
| freereels | 592.9 | 109 | 96 | 7/7 | 15/15 | 4/4 |
| yangshipin | 26.8 | 23 | 0 | 10/10 | 40/40 | not sampled |
| txws | 41.0 | 15 | 0 | 8/8 | 175/175 | 160/160 |
| sohuvideo | 598.8 | 305 | 297 | 6/115 | 37/88 | 17/99 |
| fanqiexiaoshuo | 592.0 | 48 | 292 | 12/12 | 21/21 | 12/16 |
| breaking-news | 591.2 | 22 | 38 | 10/10 | 14/14 | 8/8 |

## Same-set effects and known failures

- Mango loses 211 previously matched expectations (including one WebView operation). Bridge 1221→1184, Settings 608→536, callbacks 637→536. Faster execution (447.5→126.4s) is not success. XWeb, NFT, Pangle, wallet Client and video container groups need actual call/constructor analysis.
- Fanqie loses four previously matched callbacks (16→12); all other sampled categories retain their matches.
- News, Ctrip, Xigua, FreeReels, Yangshipin, Txws, Sohu and Breaking retain the same frozen-set matches. Ctrip slows from416.1s to594.7s and ends with six pending hosts.
- Xigua first source-first Browser set still matches0/55. Sohu core remains Bridge0/36, Settings9/26, callbacks3/29; expanding output from301 to305 hosts does not resolve it.
- Yangshipin initial wrapper-signature expectations were normalized only after independent source verification of same-object, unchanged-value forwarding to X5 WebSettings. Both v2 and v3 match40/40 normalized settings. Original wrapper facts remain preserved; this is an oracle correction, not a production improvement.
- Txws expanded343 deduplicated development facts all match both versions. Its15 output hosts were freshly source-rechecked and all valid. This proves host ownership on this output, not full capability precision, an exhaustive APK inventory, holdout success, or overall acceptance.
- Source audit found an API-override false positive: an empty addJavascriptInterface override still causes a bridge emission through a base-typed call. Generic virtual-dispatch side-effect handling remains open for v4.

Missing facts and regressions are preserved in generic-v3-*-frozen-score.json and generic-v3-*-frozen-delta.json. The immutable gold used for both versions is in generic-v3-facts/, with source paths and hashes in manifest.json. These are development sets, not final held-out evaluation.

## Verification and reproduction

Synthetic suite, compact export suite, build and ten-report structural validation passed. Forced1s watchdog test retained all three parseable atomic JSON outputs. See generic-v3-tests.json, generic-v3-deadline.json, generic-v3-structure.json, generic-v3-summary.json and generic-v3-environment.json.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v3
python3 scripts/check_compact.py test/runs/generic-v3
python3 scripts/evaluate.py --report test/runs/generic-v3/com.sohu.sohuvideo/capabilities.json --oracle docs/validation/generic-v3-facts/sohuvideo.jsonl --out output/sohu-v3-score.json
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v3-spec.json --reports test/runs/generic-v3 --out docs/validation/acceptance/generic-v3-status.json
```

An acceptance exit2 is expected while gates are incomplete. Compact and counts-only reports remain local in each package directory. No APK-wide completeness claim is made.
