# Generic v17: modeled SDK boundaries and startup snapshot reserve

Strict acceptance remains **0/10**. Synthetic correctness and measured source recall remain separate. No App-name rule is introduced.

## Implementation and tests

Exact public Fragment manager getters, beginTransaction and modeled fluent show calls no longer also enqueue their SDK bodies after their object identity has been modeled. The skip requires known compatible receivers and actual SDK dispatch; App overrides remain real calls. Inherited modeled SDK getters are not independently seeded as Activity roots. Other framework APIs remain outside this narrow boundary. See [SDK boundary and assumptions](GENERIC_V17_FRAGMENT_SDK_BOUNDARY.md).

Application startup retains its one-second total budget but reserves ten percent, capped at100ms, to copy already executed reachable static state. Partial traversal and closure limits remain diagnostic. Copying does not interpret new bodies or share mutable host state. See [startup reserve](GENERIC_V17_BOOTSTRAP_RESERVE.md).

Full capabilitySelfTest, compactReportTest and shadowJar passed in29s. Tests require preserved installed capabilities, real App override side effects, absence of SDK state-machine traversal, uncommitted/uninstalled negatives, static closure preservation, unrelated object exclusion and host isolation. Frozen jar SHA256 `e0d7ed7bd131d08c2a39bb56f49f080562035900d4eda67e2a6515c8f778cc75`; source/test hashes: generic-v17-build.json. All ten compact/counts structure checks and the forced short-deadline atomic-report smoke check pass; smoke success is not a quality gate.

## Independent oracle correction

Use generic-v17-reviewed-facts/manifest.json, not the earlier generic-v17-expanded-facts input. The174 OKWeb observations were documented as unconfirmed, but originally lacked a row-level verdict. The original input and partial scores are preserved as rejected evidence. A hash-bound normalization adds positive_acceptance=false to every unconfirmed row and normalizes registration kind aliases. Both v16 and v17 are re-scored against the corrected immutable union; no APK analysis result is reused for a fresh performance run.

The reviewed union retains15675 evidence rows: the old15386 plus69 FreeReels rows,46 confirmed News rows and174 unconfirmed News rows. Source uncertainty is not promoted to an accepted positive or silently discarded. The freeze script now offers --require-append-verdicts, rejecting new rows without an explicit boolean verdict and unsupported positive kinds. Three unit tests pass; replaying all ten reviewed source unions with strict checks reproduces the frozen bytes. See generic-v17-oracle-input-correction.json and news/okweb-v1-unconfirmed-row-normalization.json.

## Ten-App results

Cells show matched/expected (change from v16 on the same reviewed v17 oracle). Single parallel development batch, eight disjoint logical CPUs and16GiB heap per App; this does not replace the final three isolated fresh runs.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 180.536 | 1264/1407 (+0) | 228/366 (+0) | 205/272 (+0) |
| mango | 267.546 | 1185/1259 (+0) | 565/608 (+0) | 581/662 (+0) |
| ctrip | 588.817 | 3682/3970 (+0) | 369/391 (+0) | 412/444 (+0) |
| xigua | 590.094 | 30/65 (+0) | 193/278 (+0) | 7/420 (+0) |
| freereels | 591.835 | 71/90 (+0) | 201/286 (+0) | 193/459 (+0) |
| yangshipin | 52.395 | 17/17 (+0) | 189/229 (+0) | 582/624 (+0) |
| txws | 106.410 | 8/8 (+0) | 175/188 (+0) | 170/190 (+0) |
| sohuvideo | 593.414 | 504/703 (+0) | 491/501 (+0) | 1440/1592 (+0) |
| fanqiexiaoshuo | 582.630 | 12/12 (+0) | 21/21 (+0) | 10/16 (+0) |
| breaking-news | 589.761 | 10/10 (+0) | 14/14 (+0) | 8/8 (+0) |

All ten processes exit0 below600 seconds; all reports remain partial. Matched counts are unchanged for every App on the shared reviewed oracle. Xigua startup now retains18 static heap entries rather than0, while only2 startup contexts execute. Its end-of-run queued contexts fall from516675 to444707 and retained heap entries from6883761 to6018449, but the same345 Activities remain pending and missing Browser capabilities do not recover. This single parallel comparison is not a causal speedup claim; see generic-v17-xigua-runtime-comparison.json.

 Current-report ownership and capability precision, complete deep/stratum source coverage, traceable fresh holdouts and isolated repeated performance remain incomplete. Candidate-inclusive recall does not prove exact WebView instance identity. Newly discovered Breaking News bridge/wrapper surfaces and subsequent FreeReels/News source expansions are not in this freeze and must be evaluated in the next version; a small old subset scoring100% is not whole-App completeness.

Remaining systemic gaps include obfuscated packaged SDK transaction aliases, actual factory/deferred-wrapper paths, unannotated public reflection dispatch, XML/navigation/restoration attachment and large unfinished host queues. The next worktree narrows the first reflection discovery pass to necessary Method.invoke reverse-index candidates; that code is not in this jar and has no real-App improvement claim yet.

## Reproduction

Build this commit in a separate checkout; the active worktree may contain v18 code.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
python3 -m unittest discover -s scripts -p test_freeze_oracle.py
# Preserve the built jar as test/runs/generic-v17.jar.
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v17.jar --out test/runs/generic-v17
python3 scripts/replay_iteration.py --version v17 --previous v16 --oracle-manifest docs/validation/generic-v17-reviewed-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v17.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v17-spec.json --reports test/runs/generic-v17 --out docs/validation/acceptance/generic-v17-status.json
```

Acceptance currently exits2. Reports: test/runs/generic-v17/<package>/. Full/compact/counts reports, jars and APKs stay out of Git. Compact and counts-only output remain formatted and sorted by Bridge, callback, settings counts descending.
