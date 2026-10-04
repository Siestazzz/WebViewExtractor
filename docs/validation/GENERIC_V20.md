# Generic v20: recover from unknown primitive escape arguments

Strict acceptance remains **0/10**. The Mango worker crash is fixed and its source-fact matches return to the pre-crash level. The other nine Apps have unchanged matches against v19 under the same reviewed oracle. All ten reports remain partial; no App passes full quality/source/ownership/holdout/performance acceptance.

## Cause, fix and regression evidence

v19 constant-Map escape invalidation resolved an unknown primitive descriptor to a null reference-class name, then called Set.of.contains(null). The actual Mango worker threw NullPointerException at that line; its supervisor retained the report and returned2. v20 checks reference type presence before reference-type membership. Direct object-identity invalidation and unknown erased-reference invalidation remain active; primitive values cannot carry a Map object.

The added regression reproduces the same exception against the frozen v19 jar. It checks all eight primitive descriptors preserve the certificate without throwing, while the existing returned Object alias still invalidates it. Integrated capabilitySelfTest, compactReportTest and shadowJar pass in26s. Evidence and log hashes: experiments/v20-primitive-escape-crash.json. Frozen jar SHA256: `5815699c2fa58d9700d84cc6ab5fc699502ef5b108d31688ebe122cfd0e88d0f`; source hashes: generic-v20-build.json.

All ten compact/counts structural checks pass. The forced one-second deadline leaves three parseable atomic reports in1.287s. These checks establish reporting behavior, not complete analysis.

## Oracle and measured results

The16334-row immutable v20 oracle applies154 independently source-reviewed Ctrip Client identity corrections to the v19 source union. Full old rows and hash-bound correction ledger are retained. Method declaring signatures remain unchanged; concrete receiver names no longer contain composite “+ inherited” labels. This is source-schema correction, not analyzer improvement. Both v19 and v20 are replayed on the same v20 oracle with scoring7. Additional Ctrip dispatch annotations, Mango receiver/phase expansion and FreeReels source expansion are later evidence, not retroactive changes to this freeze.

Cells show matched/expected and change from v19 on that same oracle. Single parallel development batch,8 disjoint logical CPUs and16GiB maximum JVM heap per APK. APK identities are the exact sample manifest hashes, including the originally supplied vulnerable samples; do not describe the entire collection as patched. No previous analysis result is reused for these fresh runs.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 177.634 | 1264/1698 (+0) | 244/384 (+0) | 248/313 (+0) |
| mango | 267.025 | 1185/1259 (+1180) | 565/608 (+554) | 530/662 (+490) |
| ctrip | 588.965 | 3682/3970 (+0) | 369/391 (+0) | 412/444 (+0) |
| xigua | 589.158 | 30/65 (+0) | 193/278 (+0) | 1/420 (+0) |
| freereels | 591.505 | 87/106 (+0) | 203/338 (+0) | 247/541 (+0) |
| yangshipin | 52.116 | 17/17 (+0) | 189/229 (+0) | 566/624 (+0) |
| txws | 99.876 | 8/8 (+0) | 175/188 (+0) | 158/190 (+0) |
| sohuvideo | 590.036 | 504/703 (+0) | 491/501 (+0) | 1255/1592 (+0) |
| fanqiexiaoshuo | 591.759 | 12/12 (+0) | 21/21 (+0) | 10/16 (+0) |
| breaking-news | 589.789 | 70/70 (+0) | 16/16 (+0) | 94/94 (+0) |

The Mango increases restore losses caused by v19 crashing; they do not exceed its pre-crash source recall. All runs terminate below600s, but six exceed the preferred300s target and quality remains incomplete. These parallel measurements do not replace the final three fresh isolated runs.

## Residual work

Xigua still has the service/browser chain gap. A separate v21 fixture proves loss of certified Map provenance across ApplicationBootstrap snapshot/install and tests faithful transfer without recertifying arbitrary Maps. A selected real method also reproduces this transfer loss; it does not prove that the actual Browser host failure follows that path. A bounded real frozen-v20 trace is being collected after this batch to identify the actual first unknown. The full report also flags the getService wrapper's summary-variant budget; this must be distinguished from constructor, initializer and receiver identity gaps rather than blindly increasing budgets.

Unannotated installed-target prompt reflection, dynamic settings equivalence, inherited/delegated receiver source normalization in remaining Apps and deferred/unfinished contexts remain gaps. Complete independent source coverage, all-output ownership/capability precision, fresh sealed holdouts and repeated isolated performance remain unproven. Do not infer whole-App completeness from a small perfect-scoring subset or drop real explicit-super execution from expectations.

## Reproduction

Use this commit in a separate checkout; the current worktree may contain v21 code.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
# Preserve the built jar as test/runs/generic-v20.jar.
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v20.jar --out test/runs/generic-v20
python3 scripts/replay_iteration.py --version v20 --previous v19 --oracle-manifest docs/validation/generic-v20-reviewed-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v20.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v20-spec.json --reports test/runs/generic-v20 --out docs/validation/acceptance/generic-v20-status.json
```

Acceptance exits2. Formatted compact and counts-only files are in test/runs/generic-v20/<package>/, with Bridge/callback/setting counts descending. APKs, jars and full run reports stay outside Git.
