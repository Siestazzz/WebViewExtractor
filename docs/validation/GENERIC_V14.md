# Generic v14: measured regressions after Fragment activation changes

v14 captures ordinary factory constructor fields at allocation time, activates Fragment lifecycle entries through installation protocols, follows actual field-held delegates, and limits unnecessary reflective discovery during bootstrap. Synthetic coverage improves, but the ten-App measurement reveals major real-App recall regressions. This version does not pass acceptance.

Implementation and known limits: [detailed design](GENERIC_V14_IMPLEMENTATION.md). Allocation timing and transaction isolation failures/fixes are retained in experiments/V14_CONSTRUCTOR_CAPTURE_ORDERING.md and experiments/V14_FRAGMENT_TRANSACTION_ISOLATION.md. No private App adapters were added.

## Identity and tests

Jar SHA256 `edd16e6c48b976f452034af7f9ce93ba9aec920bff6e7bcedf0fe06c9146e815`; source/log hashes in generic-v14-build.json. capabilitySelfTest, compactReportTest and shadowJar passed in18s. Tests include six constructor-order cases, installed/uninstalled Fragment controls, same-class delegate isolation, late field update, transaction callsite isolation and fourteen factory routes. All ten compact/counts checks pass. A forced short deadline retains three atomic reports in1.308s; that check is not a quality pass.

## Real-App results

v13 and v14 are evaluated on the same immutable v14 expanded oracle. Parentheses show matched-count changes, not percentage-point changes. All ten exit0 below600s, all reports remain partial. Six take approximately589–592s. This single parallel development batch does not replace three isolated final repetitions.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 189.044 | 1260/1393 (+0) | 228/366 (+0) | 189/240 (+0) |
| mango | 218.616 | 512/1259 (-703) | 331/608 (-234) | 296/662 (-291) |
| ctrip | 589.412 | 554/3970 (-3128) | 177/391 (-192) | 160/444 (-252) |
| xigua | 591.830 | 30/65 (+0) | 180/278 (-13) | 7/420 (+0) |
| freereels | 590.528 | 52/69 (-5) | 104/182 (-13) | 172/420 (-16) |
| yangshipin | 55.126 | 17/17 (+0) | 189/229 (+0) | 582/624 (+0) |
| txws | 113.488 | 8/8 (+0) | 97/188 (-78) | 40/190 (-130) |
| sohuvideo | 591.153 | 56/703 (-465) | 101/501 (-400) | 166/1592 (-1344) |
| fanqiexiaoshuo | 592.307 | 6/12 (-6) | 13/21 (-8) | 5/16 (-5) |
| breaking-news | 589.653 | 10/10 (+0) | 14/14 (+0) | 8/8 (+0) |

Strict acceptance remains0/10: see acceptance/generic-v14-status.json. Current-report ownership ledgers, full deep/stratum coverage, independent holdouts and repeated performance evidence remain incomplete. Even unchanged or perfect scores in a small development subset are not whole-APK completeness claims.

The source oracle adds reviewed FreeReels evidence while retaining old failures. Four new zoom-true observations remain in the source snapshot with positive_acceptance=false because their provider-path reachability is unproved; the hash-bound source correction ledger records this independently of tool output. Later source expansion belongs to subsequent freezes.

## Confirmed regression and next version

Microvideo's six known WebView hosts lose the same surface after Fragment installation gating. Independent source/DEX review confirms standard AndroidX getSupportFragmentManager, beginTransaction, three-argument replace and commit calls. Runtime inspection identifies an incorrect boundary: frameworkViewAccess rejects bundled AndroidX FragmentActivity/FragmentManager implementation bodies as App overrides. Earlier synthetic fixtures lacked these SDK bodies and therefore failed to expose this mistake.

A development Engine-only override on frozen v14, permitting exact public Fragment protocol declaring classes while retaining App-override checks, restores WebviewBaseActivity's13/13 settings and22/22 callback facts in a selected-host diagnostic. Index74.355s and total91.144s are diagnostic measurements only. That host result is not substituted into v14 batch scores. The repair, a packaged-SDK-body fixture and all-App remeasurement are required in v15. Mango, Ctrip and Sohu also lose large surfaces; it remains unproved that every loss shares this cause.

Additional open gaps include XML/navigation/restored Fragment attachment, repeated allocation abstraction, proxy Application startup, large Class-keyed factory dispatch and full-map discovery cost. Reduced bootstrap CPU does not mean startup registrations recovered. The Xigua source-derived synthetic route now succeeds, but the real App still gains no measured Bridge/callback facts; do not equate those outcomes.

## Reproduction

Rebuild this commit in a separate checkout; the continuing worktree contains next-version repairs.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v14.jar --out test/runs/generic-v14
python3 scripts/replay_iteration.py --version v14 --previous v13 --oracle-manifest docs/validation/generic-v14-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v14.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v14-spec.json --reports test/runs/generic-v14 --out docs/validation/acceptance/generic-v14-status.json
```

Eight disjoint logical CPUs and16GiB JVM heap per APK. Reports: test/runs/generic-v14/<package>/. APKs, jars and full reports are excluded from Git.
