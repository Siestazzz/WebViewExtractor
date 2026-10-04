# Generic v10: cached-summary reads and measured export reservation

This iteration removes a shared-lock wait on already-published base summaries and reserves final report time based on the largest observed checkpoint construction/export cost. No App-specific rule is added. Manifest reflection and cross-Client custom callback delegation are subsequent-version work, not part of this jar.

## Implementation and verification

See GENERIC_V10_SUMMARY_CACHE.md and GENERIC_V10_EXPORT_RESERVE.md. The frozen full capabilitySelfTest, compactReportTest and shadowJar passed in12s. The cached-read concurrency fixture fails against v9 and passes against this implementation; concurrent misses still decode once. Export reserve tests cover measured cost, bounded final epochs, expiry and overflow. Short deadline smoke testing and all ten compact/counts consistency checks pass.

## Same-oracle results

Both v9 and v10 are evaluated on generic-v10-expanded-facts using scorer version6. The expanded development set is not a holdout set. All matched counts below are unchanged between v9 and v10; the new oracle includes additional independent FreeReels callback expectations that expose much lower coverage than earlier small samples.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 129.639 | 0 | 1260/1393 | 228/366 | 185/240 |
| mango | 139.860 | 0 | 1215/1259 | 565/608 | 587/662 |
| ctrip | 588.959 | 0 | 3682/3970 | 375/391 | 421/444 |
| xigua | 589.425 | 0 | 30/65 | 193/278 | 7/150 |
| freereels | 588.454 | 0 | 7/15 | 78/130 | 29/198 |
| yangshipin | 38.628 | 0 | 17/17 | 189/229 | 582/624 |
| txws | 69.389 | 0 | 8/8 | 175/188 | 170/190 |
| sohuvideo | 591.401 | 0 | 521/703 | 501/501 | 1510/1592 |
| fanqiexiaoshuo | 590.638 | 0 | 12/12 | 21/21 | 10/16 |
| breaking-news | 590.201 | 0 | 10/10 | 14/14 | 8/8 |

## Interpretation and remaining gaps

- All ten runs finish normally below600s in this development batch. Sohu improves from600.034s/exit124 to591.401s/exit0; FreeReels improves from596.608s/exit2 to588.454s/exit0. Six Apps still take roughly589–591s. This is not the required three isolated final performance repetitions.
- All reports remain partial, and the reserve deliberately trades some potential traversal time for report completion. Normal exit is not complete coverage or acceptance. The external watchdog and last atomic report remain necessary for unexpectedly slow jobs or report growth.
- Same-oracle matching and fixed-gold regression checks show no recall change in this iteration. Cross-contract custom delegation, service-constructor reflection, Fragment factories, dynamic setting values and conditional mutable-field precision remain known gaps.
- FreeReels expanded callbacks29/198 demonstrate why small earlier samples were insufficient. Newly source-confirmed cases are retained even when unsupported. No source-negative conclusion follows from missing output.
- Five source-audit snapshots are preserved with source hashes under generic-v10-source-audits; they explicitly retain incomplete inventory/structural coverage. Current-report ownership, capability precision, fresh holdouts and final repetitions remain incomplete. Strict acceptance remains0/10.
- Raw Yangshipin expectations retain historic source-schema issues; the separately documented hash-verified corrections remain measurement changes rather than extractor improvements.

## Reproduction

Frozen jar SHA256: `0752179a3f51dd48ae254493b883e54aae0d002b0c9b57e43b026e4fdbe48ab9`. Eight disjoint logical CPUs and16GiB maximum heap per APK; concurrent development timing only.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v10
python3 scripts/replay_iteration.py --version v10 --previous v9 --oracle-manifest docs/validation/generic-v10-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v10.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v10-spec.json --reports test/runs/generic-v10 --out docs/validation/acceptance/generic-v10-status.json
```

The replay command checks terminal run summaries, evaluates both versions against the same expanded oracle, compares the fixed regression set, records environment/timings and verifies all compact/counts projections. `--wait-seconds` can watch an ongoing batch without re-running analysis. Local reports are test/runs/generic-v10/<package>/capabilities.compact.json and capabilities.counts.json; detailed reports remain available for auditing.
