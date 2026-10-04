# Generic v4: actual lifecycle, Client getters and API overrides

**Not accepted.** This iteration recovers several source-confirmed v3 losses and removes six confirmed wrong Bridge facts, but it introduces new callback losses and does not meet the ten-App gates.

Implementation: [GENERIC_V4_IMPLEMENTATION.md](GENERIC_V4_IMPLEMENTATION.md). Frozen jar SHA-256 `079d3f747b62bab15a205ecdc48b2beb12adfa616ba463c229fe7ddad676defb`.

## Ten fresh parallel runs

Each App used8 disjoint logical CPUs and16 GiB heap, target300/supervisor595/external600. These are development measurements, not the final isolated3-repeat performance checks. Sohu and Ctrip hit the internal supervisor deadline while leaving valid reports within600s; this does not satisfy quality.

| App | Seconds | Hosts | Pending | Bridge | Settings | Callbacks |
|---|---:|---:|---:|---:|---:|---:|
| news | 105.7 | 33 | 0 | 1260/1393 | 228/366 | 185/240 |
| mango | 162.8 | 98 | 0 | 1215/1259 | 565/608 | 529/662 |
| ctrip | 594.5 | 53 | 6 | 3682/3970 | 375/391 | 421/444 |
| xigua | 591.6 | 41 | 342 | not sampled | 0/35 | 0/20 |
| freereels | 593.2 | 109 | 96 | 7/7 | 15/15 | 4/4 |
| yangshipin | 27.8 | 23 | 0 | 10/10 | 40/40 | not sampled |
| txws | 40.7 | 15 | 0 | 8/8 | 175/175 | 160/160 |
| sohuvideo | 598.0 | 305 | 297 | 6/115 | 37/88 | 17/99 |
| fanqiexiaoshuo | 592.3 | 48 | 292 | 12/12 | 21/21 | 7/16 |
| breaking-news | 592.4 | 22 | 38 | 10/10 | 14/14 | 8/8 |

These use the exact immutable generic-v3-facts/ oracle for cross-version comparison. Separate expanded source-first snapshots and same-set v3/v4 scores are in generic-v4-expanded-facts/ and *-expanded-score.json; no old failure is deleted.

## Effects and remaining failures

- Mango gains113 facts relative to v3 but loses60 callbacks, concentrated in ThirdWebActivity/ThirdFullWebActivity. Source review confirms actual installed Client wrappers delegate callbacks into stored user Clients. Recording only the outer installed wrapper loses legitimate delegated implementations. This needs same-WebView callback delegation analysis; restoring unconditional parent API effects would restore confirmed false positives.
- XWeb lifecycle facts and actual Client getter facts recover. NFT/Pangle boundaries and original generic-v1 private-adapter-removal losses remain open.
- Fanqie loses five more callback facts (v3 12/16→v4 7/16). Ctrip loses two WebView operations outside the three scored categories; its Bridge/Settings/callback matches remain unchanged. All losses remain in frozen-delta.json artifacts.
- Other frozen development matches remain unchanged. Sohu core is still incomplete, and Xigua Browser set still0/55. Raw host totals are not recall or precision.
- Six previously independently verified NBS bridge misattributions on AuthWebView disappear, while both legitimate hosts remain. See sohuvideo/generic-v4-empty-override-recheck.json. This proves that narrow repair only, not overall capability precision or host ownership.
- Bounded queue samples identify real pending work inside AndroidX FragmentManager restoration and shared bus/player paths. generic-v4-sohu-pending-sample.json is an intermediate observation, not proof that any one pending method caused every miss.
- Independent Sohu restored-player audit adds DownloadListener registration and inherited callback expectations. The API is within the requested callback surface and must be supported generically; it is not removed from truth to raise recall. Implementation is subsequent v5 work.

## Tests and reproducibility

Full synthetic suite, compact export tests and shadowJar passed in9s. Newly exercised fixtures cover actual Client getters, inherited lifecycle virtual hooks, API override suppression/super forwarding, read-only queue observation, and parameterized transport registration candidates. TransportProtocols remains unintegrated infrastructure and has no production recall claim.

Forced1s timeout and all-ten report structure checks passed. The v4 export subtime metric still double-counts nested compact writes; a tested correction belongs to v5 and is not represented as analysis speedup or retroactively applied to history. Total wall-clock figures above are external measurements and unaffected.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v4
python3 scripts/check_compact.py test/runs/generic-v4
python3 scripts/evaluate.py --report test/runs/generic-v4/com.hunantv.imgo.activity/capabilities.json --oracle docs/validation/generic-v3-facts/mango.jsonl --out output/mango-v4-score.json
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v4-spec.json --reports test/runs/generic-v4 --out docs/validation/acceptance/generic-v4-status.json
```

Expected acceptance exit2: missing final source coverage, holdout, current all-output precision/ownership evidence, and isolated repeats remain failures, alongside recall gaps. User-facing compact and counts-only reports are under each package directory in test/runs/generic-v4/.
