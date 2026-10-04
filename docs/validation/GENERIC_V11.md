# Generic v11: protocol extensions and ten-App regressions

The frozen implementation adds actual cross-Client full-signature forwarding, constrained reflective construction, Class-valued metadata captures, Class/name keyed nested registries, and Manifest Application metadata module loading. No private App rule is introduced. See [delegation](GENERIC_V11_CROSS_CLIENT_DELEGATION.md), [reflection](GENERIC_V11_REFLECTIVE_FACTORIES.md), and [Manifest](GENERIC_V11_MANIFEST_IMPLEMENTATION.md) for mechanism and limits.

## Verification

Full capabilitySelfTest and compactReportTest passed (12s); source and log hashes are recorded in generic-v11-build.json. All ten compact/counts projections passed. The short watchdog test retained three parseable reports under a forced deadline (generic-v11-deadline.json); it is not a large-App performance proof. The independent [boundary audit](GENERIC_V11_RULE_BOUNDARY_AUDIT.md) subsequently found Manifest actual-receiver override and null/string comparison defects. Their fixes are v12 work, not part of this measured jar. Direct construction after a missing registry key remains a known false positive.

## Same development oracle: v10 → v11

Both versions are replayed on generic-v11-expanded-facts. The oracle retains all historical failures and adds independent Xigua/FreeReels expectations; it is not a fresh holdout. Numerators before the arrow are v10, after it v11, with the common denominator.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 135.124 | 0 | 1260 → 1260/1393 | 228 → 228/366 | 185 → 189/240 |
| mango | 588.274 | 0 | 1215 → 1215/1259 | 565 → 565/608 | 587 → 587/662 |
| ctrip | 589.399 | 0 | 3682 → 3682/3970 | 375 → 369/391 | 421 → 412/444 |
| xigua | 589.383 | 0 | 30 → 30/65 | 193 → 193/278 | 7 → 7/420 |
| freereels | 588.900 | 0 | 23 → 23/31 | 84 → 84/136 | 56 → 56/225 |
| yangshipin | 40.202 | 0 | 17 → 17/17 | 189 → 189/229 | 582 → 582/624 |
| txws | 73.423 | 0 | 8 → 8/8 | 175 → 175/188 | 170 → 170/190 |
| sohuvideo | 591.038 | 0 | 521 → 521/703 | 501 → 501/501 | 1510 → 1510/1592 |
| fanqiexiaoshuo | 589.277 | 0 | 12 → 12/12 | 21 → 21/21 | 10 → 10/16 |
| breaking-news | 589.004 | 0 | 10 → 10/10 | 14 → 14/14 | 8 → 8/8 |

## What this proves and does not prove

- News gains four independently expected title callback delegations. Its nine Ysp hosts still miss the service surface: the synthetic factory test omitted Application startup registration through a static reflective method wrapper. Those missing facts remain recorded.
- Mango slows from139.860s to588.274s. Relevant methods grow26,862→46,819 while capability seeds remain670. Broad Class-field holder relevance is a identified contributor under investigation; v12 separates allocation-local capture from global relevance, but that recovery is not attributed to v11.
- Ctrip loses six setting and nine callback matches on the same oracle. Budget/ordering and relevance expansion remain to be checked; no failed facts are removed or downgraded to mask the regression.
- Sohu's Manifest protocol additions produce no gain in the current measured facts. Synthetic protocol success is not proof of real framework closure.
- All runs exit normally below600s, but seven now approach588–591s and every report remains partial. This is one parallel development batch, not three isolated final repetitions.
- News current-report host ownership is32valid/0wrong/1unresolved of33 (3.03% conservative upper bound), with explicit source evidence reuse. It does not certify capability-level precision. Other current-report ownership, complete deep/stratum audits, fresh holdouts and final repeats remain incomplete. Strict acceptance remains0/10.
- Yangshipin source-corrected expectations are reported separately using the unchanged hash-bound correction ledgers; raw scores above remain available. Denominator corrections are not extraction improvements.

## Reproduction and artifacts

Frozen jar SHA256: `63d636f46a8e913653406c30a877c09ef8027145bf2d624612d44428f1e16238`. Each APK gets eight disjoint logical CPUs and a16GiB maximum JVM heap. Environment, timing, memory, deltas, and scores are saved as generic-v11-*; full local output is test/runs/generic-v11/<package>/capabilities.compact.json and capabilities.counts.json.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v11.jar --out test/runs/generic-v11
python3 scripts/replay_iteration.py --version v11 --previous v10 --oracle-manifest docs/validation/generic-v11-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v11.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v11-spec.json --reports test/runs/generic-v11 --out docs/validation/acceptance/generic-v11-status.json
```

The jar and source snapshot are local ignored artifacts. Rebuild the v11 commit in a fresh checkout to reproduce; do not rebuild a later worktree and label it v11.
