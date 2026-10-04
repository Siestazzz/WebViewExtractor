# Generic v8: field lookup cost, conditional initialization and installed client contracts

This iteration improves several measured capabilities, but does not pass ten-App acceptance. Every report remains partial; Sohu reaches the supervisor deadline. Parallel Activity scheduling and corrected X5 transport parameter identities belong to v9, not this frozen implementation.

## Implementation and tests

- Lazy mutable FieldHeap indexes replace repeated full-heap dependency scans. Index mutation preserves exact receiver keys; interrupted scans do not publish partial indexes. See GENERIC_V8_FIELD_HEAP.md.
- Numeric/boolean IF refinement is enabled only when actual invocation arguments resolve a previously unknown guard, within existing refinement budgets. Unknown and mutable field conditions remain conservative. See GENERIC_V8_CONDITIONAL_INIT.md.
- SDK family and full setter signatures identify installed clients. Tencent extension interfaces are read from the APK; callbacks are restricted to the actually installed interface even when one object implements several interfaces. See GENERIC_V8_X5_CONTRACTS.md.
- Installed prompt/console clients can expose exact reflective handler methods through a same-map, same-handler, exact-parameter and annotation-control-flow proof. Unsupported name/arity enumeration remains unresolved. See GENERIC_V8_REFLECTION_IMPLEMENTATION.md.
- Frozen build capabilitySelfTest, compactReportTest and shadowJar passed. Oracle freezer 2 tests and scorer 13 tests pass. Ten-App compact/counts consistency passed. Frozen-jar deadline smoke test observed 166 snapshots with all three reports valid (1.27 seconds, intentionally forced timeout).

## Ten-App results on the same expanded development oracle

Both v7 and v8 are replayed against generic-v8-expanded-final-facts using scoring version 6. These are development facts, not final fresh holdouts. Entries show matched/expected; parentheses give the change in matched facts relative to v7 on the identical oracle.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 145.3 | 0 | 1260/1393 (+0) | 228/366 (+0) | 185/240 (+0) |
| mango | 215.3 | 0 | 1215/1259 (+0) | 565/608 (+0) | 587/662 (+0) |
| ctrip | 592.3 | 0 | 3682/3970 (+2) | 343/391 (+0) | 397/444 (+0) |
| xigua | 591.6 | 0 | 30/65 (+0) | 193/278 (+0) | 7/150 (+0) |
| freereels | 593.2 | 0 | 7/15 (+0) | 70/112 (+0) | 16/19 (+0) |
| yangshipin | 44.5 | 0 | 17/17 (+0) | 164/204 (+0) | 494/536 (+144) |
| txws | 69.2 | 0 | 8/8 (+0) | 175/188 (+0) | 170/190 (+0) |
| sohuvideo | 598.4 | 2 | 489/663 (+78) | 488/488 (+336) | 1432/1514 (+892) |
| fanqiexiaoshuo | 592.3 | 0 | 12/12 (+0) | 21/21 (+0) | 10/16 (-2) |
| breaking-news | 591.2 | 0 | 10/10 (+0) | 14/14 (+0) | 8/8 (+0) |

## Limitations and regressions

- Six Apps remain near the hard deadline. Sohu exits 2 at 598.4 seconds; its 219 MB detailed report adds meaningful shutdown cost. A report surviving the deadline is not a quality pass.
- Fanqie loses two previously matched callbacks on WebViewActivity: ReadingWebView$m.onPageFinished and shouldInterceptRequest. The exact facts are retained in generic-v8-fanqiexiaoshuo-vs-v7-frozen-delta.json. The cause is not yet established; no source-negative conclusion is drawn.
- Xigua, FreeReels, News and Ctrip retain substantial known omissions. Txws newly source-confirmed MainActivity factory/Fragment routing is absent. Sohu has unresolved manifest-selected module bridges and conditional null-container binding precision.
- Yangshipin callbacks improve by 144, but settings include 40 unscorable source expressions and old enum representations. Sixteen historical declared-but-unforwarded callback expectations remain in the raw cumulative oracle pending an explicit hash-verified correction overlay. These are measurement issues recorded separately, not claimed code improvements.
- The initial v8 expanded freeze and the later final freeze are both immutable and retained. Exact duplicate JSON rows may be collapsed; distinct evidence and failed facts remain. See ORACLE_FREEZING.md and each manifest for source hashes.
- Report-bound source ownership review now confirms Txws 15/15 valid and Sohu 279 valid/26 unresolved out of 305 (8.52% conservative upper bound). The actual init(false) Ajax false bindings fall from two to zero, while null-container path false capability facts grow from 126 to 140; valid host ownership does not certify capability precision. Other Apps still need complete current-report ownership review. Deep audits, fresh holdouts and three isolated final performance repetitions remain incomplete. The conservative acceptance ledger therefore passes 0/10 Apps.

## Reproduction

Frozen jar SHA256: `250bba370e850d7c40bc726ceb010877261e24efad8f5b4b17b0a1423aeeba52`. Each development run used eight disjoint logical CPUs and a 16 GiB maximum JVM heap. Parallel multi-App timings do not count as isolated final repetitions.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
python3 scripts/test_freeze_oracle.py
python3 scripts/test_evaluate.py
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v8-reproduce
python3 scripts/check_compact.py test/runs/generic-v8
python3 scripts/check_deadline.py --jar test/runs/generic-v8.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v8-spec.json --reports test/runs/generic-v8 --out docs/validation/acceptance/generic-v8-status.json
```

Summary, environment, per-App scores, same-oracle deltas and structure checks are stored as generic-v8-*.json. Detailed/compact/counts reports are local under test/runs/generic-v8/<package>/.
