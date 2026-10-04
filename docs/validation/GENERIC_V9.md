# Generic v9: parallel Activity traversal and actual X5 transport types

This version allows up to eight Activity analysis workers with independent host state and shared method summaries. The complete first-pass barrier precedes resumable deep analysis. Public X5 prompt/console transport types now use their actual exported-interface descriptors. No private App rules were added.

Implementation and synthetic validation: GENERIC_V9_ACTIVITY_PARALLELISM.md and GENERIC_V9_X5_TRANSPORT_IDENTITIES.md. Frozen capabilitySelfTest, compactReportTest and shadowJar passed; frozen-jar deadline smoke check and ten-App compact/counts consistency passed. The short smoke test does not establish large-report shutdown performance.

## Same-oracle measurements

All versions below use generic-v9-expanded-facts with scorer version 6. These are accumulated development facts, not fresh holdouts. Changes are relative to v8 on exactly that oracle.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 132.434 | 0 | 1260/1393 (+0) | 228/366 (+0) | 185/240 (+0) |
| mango | 148.495 | 0 | 1215/1259 (+0) | 565/608 (+0) | 587/662 (+0) |
| ctrip | 593.166 | 0 | 3682/3970 (+0) | 375/391 (+32) | 421/444 (+24) |
| xigua | 591.962 | 0 | 30/65 (+0) | 193/278 (+0) | 7/150 (+0) |
| freereels | 596.608 | 2 | 7/15 (+0) | 78/122 (+0) | 22/27 (+0) |
| yangshipin | 39.645 | 0 | 17/17 (+0) | 189/229 (+0) | 582/624 (+0) |
| txws | 70.445 | 0 | 8/8 (+0) | 175/188 (+0) | 170/190 (+0) |
| sohuvideo | 600.034 | 124 | 521/703 (+0) | 501/501 (+0) | 1509/1591 (+0) |
| fanqiexiaoshuo | 593.638 | 0 | 12/12 (+0) | 21/21 (+0) | 10/16 (+0) |
| breaking-news | 591.494 | 0 | 10/10 (+0) | 14/14 (+0) | 8/8 (+0) |

## Interpretation and outstanding failures

- Mango improves 215.3→148.5s and News 145.3→132.4s, with unchanged frozen-gold recall. Ctrip reaches more settings and callbacks within essentially the same deadline, but Bridge remains below95%. Six Apps still approach the time limit.
- Sohu takes600.034s and exits124 at the external watchdog: the strict600s condition fails. FreeReels exits2 after596.608s. Their valid retained reports do not turn these into quality passes. Large final reports and repeated supervisor projection/export remain a shutdown risk; measured export reservation is next-version work.
- No category regresses on the fixed v3 regression set in this run. This does not prove absence of new false bindings; current-report ownership review confirms Txws15/15 valid and Sohu279 valid/26 unresolved of305 (8.52% conservative upper bound). Other Apps remain unreviewed for this gate; host validity alone does not establish capability precision.
- Fanqie retains two missing real initialized Client callbacks. Independent source evidence confirms the prior v7 apparent matches came from an impossible null-reset installation; v8 correctly removes that false path but still misses the actual nonnull initialization chain. Gold is retained. See fanqiexiaoshuo/webviewactivity-wrapper-install-vs-cleanup-source-proof.json.
- Sohu null-container allocation paths remain falsely emitted (118 path facts in v9 versus140 in v8; fewer false facts do not establish a correct field binding). The field is public/nonfinal and receives null through a constructor chain; ordinary final-field pruning cannot safely resolve it. See sohuvideo/constructor-field-chain.json and null-container-dex-evidence.json.
- Live Fanqie thread sampling reveals base cache reads blocked behind contextual summary resolution. A next-version ConcurrentHashMap fast read path has a meaningful old-version failing test, but is not included in this jar.
- Hash-verified source corrections are separate from extractor improvement. See ORACLE_SOURCE_CORRECTIONS.md: corrected Yangshipin source expectations score settings189/221, bridge17/17, callbacks582/608 for both v8 and v9. Raw scores above remain available; runtime-expression settings remain unresolved.
- Source deep-audit scope, all-output ownership/precision, fresh holdouts and three isolated final runs remain incomplete. Strict combined acceptance remains0/10.

## Reproduction

Frozen jar SHA256: `1b7f64c889cad0866004d225feba9e8ba01160ac627db1a3c38370c04b51e569`. Each App had8 disjoint logical CPUs and16GiB heap. These concurrent development measurements are not final isolated repetitions.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
python3 scripts/test_correct_oracle.py
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v9-reproduce
python3 scripts/check_compact.py test/runs/generic-v9
python3 scripts/check_deadline.py --jar test/runs/generic-v9.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v9-spec.json --reports test/runs/generic-v9 --out docs/validation/acceptance/generic-v9-status.json
```

Detailed, compact and counts-only reports are local under test/runs/generic-v9/<package>/. Versioned generic-v9 JSON artifacts contain environment, all ten timings, strict replay results and structure checks.
