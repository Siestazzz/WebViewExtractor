# Generic v19: closed literal Map and numeric switch factories

Strict acceptance remains **0/10**. This is a failed development iteration: Mango's worker throws a NullPointerException in new escape invalidation; Fanqie times out. Nine Apps show no matched-count gain on the same source oracle. The failing version and its evidence are preserved; v20 repairs the primitive-argument exception and must be independently rerun.

## Implementation and evidence

Actual freshly allocated HashMap/LinkedHashMap objects may receive a bounded certificate for a complete unbranched literal initialization. Exact Integer boxing/unboxing preserves known values; a forward single packed/sparse switch factory can specialize a known numeric selector within existing size/variant/work budgets. Mutation, alias and unknown-reference escape paths invalidate the certificate. This is a local execution model, not proof that unexamined callees cannot mutate public static state. See [implementation and tests](GENERIC_V19_INTEGER_FACTORY.md).

The selected real packaged factory changes from unknown to the correct allocation; an absent key returns null. This replay does not execute the full Application/service/cache/Activity chain. Full Xigua recall remains unchanged. See [scope and frozen evidence](xigua/v19-selected-service-factory-replay.md).

capabilitySelfTest, compactReportTest and shadowJar pass in34s; jar SHA256 `47dab88ae21c4c9849515ee8685cfc82734fa98b864eb4b4815b658c13f8c9f0`. All ten report-format checks pass. The one-second forced deadline leaves three atomic reports. Synthetic passes did not prevent the real Mango exception: an unknown primitive parameter produces no reference class, which was incorrectly passed to Set.of.contains(null). The exception is present in the preserved run.log and worker_exit_code:1 diagnostics; supervisor exit code is2 and report status is failed. Its shorter runtime is not a performance improvement.

## Evaluation and source corrections

Scoring7 enforces the actual Client implementation when source specifies it, in addition to the complete callback member signature. Shared parent bodies no longer permit matching another concrete Client. Native-name-free reflection transport and runtime-expression setting support limitations are reported independently of emitted candidates; they remain nonmatches in the denominator. Fifteen evaluator tests pass.

The immutable reviewed oracle contains16334 evidence rows. Hash-bound corrections revise206 source records (News126, Breaking60, FreeReels20); original rows and correction ledgers are retained. News conditional receiver branches and new Novel facts add357 rows; FreeReels adds26 instance capability rows. Global WebView process configuration is separate and is not silently treated as an instance setting. Five freeze tests and four correction tests pass; predecessor hashes are checked. See [scoring and source-view details](GENERIC_V19_CALLBACK_SCORING.md).

Legacy receiver semantics in other App fact sets still require source review. The subsequent154 Ctrip corrections are not retroactively applied to v19 and are included in the v20 oracle. Their existence is not analyzer improvement. Source uncertainty, explicit-super implementation and actual virtual receiver identity must remain distinct; real super-body execution must not be dropped to improve a percentage.

## Ten-App results

Matched/expected, with change from frozen v18 under the identical reviewed v19 oracle and scorer. One parallel batch, eight disjoint logical CPUs and16GiB JVM heap per APK; this is not the final three isolated runs. Sample identities are the exact hashes in generic-ten-samples.json; original vulnerable samples must not be mislabeled as patched samples.

| App | Seconds | Status | Bridge | Settings | Callback |
|---|---:|---|---:|---:|---:|
| news | 181.380 | partial | 1264/1698 (+0) | 244/384 (+0) | 248/313 (+0) |
| mango | 160.027 | failed | 5/1259 (-1180) | 11/608 (-554) | 40/662 (-490) |
| ctrip | 588.548 | partial | 3682/3970 (+0) | 369/391 (+0) | 269/444 (+0) |
| xigua | 592.129 | partial | 30/65 (+0) | 193/278 (+0) | 1/420 (+0) |
| freereels | 591.404 | partial | 87/106 (+0) | 203/338 (+0) | 247/541 (+0) |
| yangshipin | 53.863 | partial | 17/17 (+0) | 189/229 (+0) | 566/624 (+0) |
| txws | 99.162 | partial | 8/8 (+0) | 175/188 (+0) | 158/190 (+0) |
| sohuvideo | 588.572 | partial | 504/703 (+0) | 491/501 (+0) | 1255/1592 (+0) |
| fanqiexiaoshuo | 594.254 | timeout | 12/12 (+0) | 21/21 (+0) | 10/16 (+0) |
| breaking-news | 588.745 | partial | 70/70 (+0) | 16/16 (+0) | 94/94 (+0) |

## Residual work

The selected-method certificate is not preserved through ApplicationBootstrap.State; already populated heap reads also bypass initializer population. These are hypotheses for the remaining complete-chain failure and need runtime or regression evidence before generalizing the model. Do not recertify arbitrary previously observed mutable Maps.

Installed-target public prompt reflection still lacks a generic full protocol model. Source-first selector certificates now distinguish callable public members, exact parameter grammar, same target/argument vectors and conditional monitor replacement; no App-specific hardcoded receiver is permitted. Independent deep source coverage, all-output ownership/capability precision, fresh holdouts and isolated repeated performance remain unfinished. Known unresolved facts are retained. There is no quality acceptance claim.

## Reproduction

Build this commit in a separate checkout; the current worktree can contain v20/v21 changes.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
python3 -m unittest discover -s scripts -p test_evaluate.py
python3 -m unittest discover -s scripts -p 'test_*oracle*.py'
# Preserve the built jar as test/runs/generic-v19.jar.
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v19.jar --out test/runs/generic-v19
python3 scripts/replay_iteration.py --version v19 --previous v18 --oracle-manifest docs/validation/generic-v19-reviewed-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v19.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v19-spec.json --reports test/runs/generic-v19 --out docs/validation/acceptance/generic-v19-status.json
```

Acceptance exits2 (0/10). Formatted compact and counts-only files are under test/runs/generic-v19/<package>/ and sort Bridge, callback, setting counts descending. APKs, jars and full run reports remain outside Git.
