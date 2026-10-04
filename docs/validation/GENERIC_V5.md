# Generic v5: rejected performance regression

This iteration adds generic Fragment factories and ViewPager2 installation, Android/Tencent DownloadListener contracts, and corrects nested export timing. It is retained as a reproducible failed experiment, not an accepted release. v6 will restrict framework-internal factory relevance and test installed Client delegates.

## Frozen ten-App results

Recall below uses the unchanged generic-v3 fact set, includes candidate bindings, and is not an all-APK completeness claim. Expanded cumulative fact replay is retained separately in generic-v5-*-expanded-v5-score.json. No facts were removed.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 594.7 | 2 | 1252/1393 | 208/366 | 162/240 |
| mango | 593.2 | 0 | 1166/1259 | 532/608 | 493/662 |
| ctrip | 594.1 | 2 | 2123/3970 | 269/391 | 319/444 |
| xigua | 591.8 | 0 | Not audited | 0/35 | 0/20 |
| freereels | 593.5 | 0 | 7/7 | 15/15 | 4/4 |
| yangshipin | 594.2 | 2 | 10/10 | 40/40 | Not audited |
| txws | 591.9 | 0 | 8/8 | 175/175 | 160/160 |
| sohuvideo | 599.0 | 2 | 7/115 | 88/88 | 17/99 |
| fanqiexiaoshuo | 592.8 | 0 | 12/12 | 21/21 | 7/16 |
| breaking-news | 591.6 | 0 | 10/10 | 14/14 | 8/8 |

## Interpretation and outstanding gaps

All ten runs approach the 600-second limit. Previously fast Apps also regress; this version fails the 300-second target and does not establish the quality gates. Factory protocol discovery expands framework restore and lifecycle traversal too broadly. Sohu settings coverage increases, but its causal attribution is not isolated and fixed-gold losses elsewhere dominate; capability counts are not evidence of precision or recall. Sohu settings rise from 37/88 to 88/88; Ctrip bridge facts fall from 3682/3970 to 2123/3970.

Known remaining gaps include installed Client delegate callbacks, public static JavaScript endpoints, reflective prompt/console registries, scene lifecycle routing, source-audit completeness, all-output ownership/precision, sealed holdouts, and three isolated final runs. Missing ownership evidence remains unresolved, not valid. Strict acceptance remains 0/10.

## Validation and reproduction

Full capabilitySelfTest, compactReportTest and shadowJar passed in 10 seconds. Synthetic tests include FragmentFactoryFixture, DownloadListenerFixture and nested export accounting. Jar SHA256: `38657883746109cf6957113365a5490e3af7228c9df7582797743cc7203a7457`. The committed source is the frozen v5 snapshot, separate from ongoing v6 edits.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v5-reproduce
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v5-spec.json --reports test/runs/generic-v5 --out docs/validation/acceptance/generic-v5-status.json
```

Parallel development measurements assign eight disjoint logical CPUs and a 16 GiB heap per App. They are not the final isolated performance trials. Raw reports remain local under test/runs/generic-v5; committed summary, environment, score and delta files bind results to their hashes. Details: GENERIC_V5_COMPONENT_FACTORY.md and GENERIC_V5_DOWNLOAD_AND_TIMING.md.
