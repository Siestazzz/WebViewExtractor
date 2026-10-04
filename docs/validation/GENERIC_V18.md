# Generic v18: exact public constructors and reflection discovery prefilter

Strict acceptance remains **0/10**. All ten reports are partial. No App-specific production rule is introduced.

## Implementation and tests

The reflection protocol discovery pass uses the existing reverse index of the exact Method.invoke signature as a necessary candidate set, then applies the existing full protocol certificate. Its regression fixture reduces discovery visits from68 to2 while retaining the certified writer; unrelated Map methods are excluded. See [prefilter proof](GENERIC_V18_REFLECTION_INVOKE_PREFILTER.md).

Exact Class.getConstructor(Class[]) now resolves a known Class with an actual zero-length Class[] to its declared public noarg constructor. Existing allocation, access, concrete-class and argument checks still apply. The fixture covers complete registry/cache/factory paths and unknown/private/abstract/wrong-array/nonempty negative cases. The new positive fixture fails against frozen v17 and passes with v18. See [constructor details](GENERIC_V18_PUBLIC_CONSTRUCTOR.md).

Integrated capabilitySelfTest, compactReportTest and shadowJar passed in30s. Four source-freeze tests pass. All ten compact/counts report checks pass. The one-second deadline smoke leaves all three parseable atomic reports in1.287s; this is not a quality pass. Jar SHA256: `afc52bcb7c7ff9ae1cc78dc432c11b0bdbd6ebfb42e8a9de0c1eb6aaacd3234c`; source/test hashes are in generic-v18-build.json.

## Source input review

The authoritative oracle is generic-v18-reviewed-facts/manifest.json:15951 retained evidence rows, adding126 FreeReels and150 Breaking News rows to the previous union. The first Breaking snapshot incorrectly classified callback members as registrations, used JADX defpackage aliases and added an unsupported field matcher constraint. Independent GPT-6.1 Sol source/DEX review corrected those records without looking at output. The original snapshot, rejected initial input, initial diagnostic scores and row-by-row hash ledger remain available; see generic-v18-oracle-input-correction.json. This correction is not analyzer improvement. Both v17 and v18 are scored on the same reviewed facts. Strict new-row validation now rejects the discovered shape mistakes rather than silently scoring them as misses.

## Results

Matched/expected, followed by matched-count change from v17 on the same oracle. One parallel development batch,8 disjoint logical CPUs and16GiB maximum JVM heap per App. These measurements do not replace three isolated final runs.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 173.824 | 1264/1407 (+0) | 228/366 (+0) | 205/272 (+0) |
| mango | 261.815 | 1185/1259 (+0) | 565/608 (+0) | 581/662 (+0) |
| ctrip | 589.239 | 3682/3970 (+0) | 369/391 (+0) | 412/444 (+0) |
| xigua | 591.520 | 30/65 (+0) | 193/278 (+0) | 7/420 (+0) |
| freereels | 590.635 | 87/106 (+0) | 201/332 (+0) | 247/521 (+0) |
| yangshipin | 51.490 | 17/17 (+0) | 189/229 (+0) | 582/624 (+0) |
| txws | 99.583 | 8/8 (+0) | 175/188 (+0) | 170/190 (+0) |
| sohuvideo | 589.572 | 504/703 (+0) | 491/501 (+0) | 1440/1592 (+0) |
| fanqiexiaoshuo | 581.597 | 12/12 (+0) | 21/21 (+0) | 10/16 (+0) |
| breaking-news | 590.928 | 70/70 (+0) | 16/16 (+0) | 94/94 (+0) |

All ten matched-count totals are unchanged. Expanded Breaking facts score100% for this subset only; they do not establish complete App coverage. FreeReels runtime-expression settings remain explicitly unscorable where the evaluator cannot verify their expression semantics, rather than being relabeled as wildcard matches.

## Remaining gaps and next action

The Xigua constructor fix does not by itself recover the browser chain: the independently identified service object path still loses Class-name keyed static Map contents, numeric boxing/unboxing and a bounded switch factory selector. That separate v19 change is under development and is not part of this frozen jar. Unknown-type fanout is not an acceptable replacement for object provenance.

Unannotated public prompt reflection, actual SDK wrapper/delegation paths, unfinished host queues and deferred framework bindings remain material gaps. News/FreeReels/Breaking source audits are still incomplete. Exact WebView instance capability precision, current-report ownership, fresh sealed holdouts and three isolated repeated runs remain unproven. No quality gate is waived because one source subset scores well.

## Reproduction

Use this commit in a separate checkout; the active worktree can contain later changes.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
python3 -m unittest discover -s scripts -p test_freeze_oracle.py
# Preserve the built jar as test/runs/generic-v18.jar.
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v18.jar --out test/runs/generic-v18
python3 scripts/replay_iteration.py --version v18 --previous v17 --oracle-manifest docs/validation/generic-v18-reviewed-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v18.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v18-spec.json --reports test/runs/generic-v18 --out docs/validation/acceptance/generic-v18-status.json
```

Acceptance exits2 (0/10). Formatted compact signatures and counts-only reports are in test/runs/generic-v18/<package>/, sorted by Bridge, callback and settings counts descending. APKs, complete reports and jars remain outside Git.
