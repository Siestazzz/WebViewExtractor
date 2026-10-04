# Generic v6: installed Client delegation and factory relevance repair

This iteration recovers the v5 fast-App regression and some missing delegated callbacks, but is not accepted: six Apps still approach the hard deadline, known systematic omissions remain, and independent audit/holdout/repeated isolated performance gates are incomplete. No App-specific production signatures were added.

## Frozen regression set

Same generic-v3 oracle for all comparisons; candidate bindings count. Unscored categories are not presumed complete.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 124.1 | 0 | 1260/1393 | 228/366 | 185/240 |
| mango | 204.6 | 0 | 1215/1259 | 565/608 | 587/662 |
| ctrip | 594.0 | 2 | 2123/3970 | 295/391 | 334/444 |
| xigua | 591.7 | 0 | Not audited | 0/35 | 0/20 |
| freereels | 594.0 | 0 | 7/7 | 15/15 | 4/4 |
| yangshipin | 33.9 | 0 | 10/10 | 40/40 | Not audited |
| txws | 49.3 | 0 | 8/8 | 175/175 | 160/160 |
| sohuvideo | 598.4 | 2 | 7/115 | 88/88 | 17/99 |
| fanqiexiaoshuo | 592.5 | 0 | 12/12 | 21/21 | 12/16 |
| breaking-news | 591.4 | 0 | 10/10 | 14/14 | 8/8 |

## Expanded cumulative development set

Frozen v6 union preserves v3/v5 facts and appends source-audited discoveries. Later source corrections remain documented, not silently removed to improve recall. These results are not holdout results.

| App | Bridge | Settings | Callback |
|---|---:|---:|---:|
| news | 1260/1393 | 228/366 | 185/240 |
| mango | 1215/1259 | 565/608 | 587/662 |
| ctrip | 2123/3970 | 295/391 | 334/444 |
| xigua | 26/61 | 175/260 | 6/96 |
| freereels | 7/15 | 69/112 | 16/19 |
| yangshipin | 17/17 | 89/89 | 24/24 |
| txws | 8/8 | 175/175 | 170/170 |
| sohuvideo | 475/647 | 487/487 | 1383/1465 |
| fanqiexiaoshuo | 12/12 | 21/21 | 12/16 |
| breaking-news | 10/10 | 14/14 | 8/8 |

## Findings and limitations

- Factory protocol roots no longer promote framework internal restore helpers. Yangshipin 33.9s, Txws 49.3s, News124.1s, Mango204.6s recover from v5 near600. This does not solve the other six Apps.
- Actual installed Client callback forwarding recovers Mango58 and Fanqie5 fixed-gold callbacks relative to v4. Mango two historical callbacks lack an actual delegate edge; see mango/generic-v6-two-callback-residual-review.md. Gold remains unchanged.
- Public static annotated JavaScript methods are included; private/static Client callback exclusions remain. Static fixture fails on v5 and passes v6.
- Ctrip repeatedly sampled in refreshBinding recursion after its211s checkpoint, with no later stage progress before supervisor timeout. See generic-v6-ctrip-refresh-profile.json. A shared-graph repeated-expansion fix is in progress for v7, not part of this snapshot.
- Source audit found six newly expanded Sohu expectations applied a wrapper/constructor branch to an incompatible XML host. Frozen scores retain them pending explicit source-rejection records. Do not equate these disputed rows with proven extractor omissions.
- Independent Txws source recheck covers all15 emitted hosts as valid, report-hash bound. This does not prove capability precision, exhaustive positive-host coverage, holdout quality, or other Apps ownership.
- Newly expanded source audit continues to find missing surface (including Yangshipin settings and Xigua frameworks). Apparent perfect ratios on older small sets do not establish full coverage.
- Strict combined acceptance remains0/10. Reflective prompt/console, scene routing, all-output precision, independent holdouts and final isolated repetitions remain outstanding.

## Implementation and tests

See GENERIC_V6_CLIENT_DELEGATION.md and GENERIC_V6_STATIC_BRIDGES.md for contracts, context identity, negative fixtures and boundaries. Full capabilitySelfTest, compactReportTest and shadowJar passed in9s. Deadline smoke test observed three valid atomic report projections and supervisor timeout in1.30s. Compact/counts projection consistency and sorting checks passed for all ten terminal reports.

Frozen jar SHA256: `5a5e647312aa435dabb8ddb89c4e37457e1aa5a13c64be911bc3cf2088330efb`. Development parallel measurements use8 logical CPUs and16GiB perApp. Sampling adds small overhead and these are not final isolated performance trials.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v6-reproduce
python3 scripts/check_compact.py test/runs/generic-v6
python3 scripts/evaluate.py --report test/runs/generic-v6/com.tencent.news/capabilities.json --oracle docs/validation/generic-v6-expanded-facts/news.jsonl --out /tmp/news-v6-score.json
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v6-spec.json --reports test/runs/generic-v6 --out docs/validation/acceptance/generic-v6-status.json
```
