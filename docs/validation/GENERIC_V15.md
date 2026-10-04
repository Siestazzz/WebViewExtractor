# Generic v15: packaged Fragment contracts and bounded factory refinement

Strict acceptance remains **0/10**. This version repairs most v14 Fragment regressions; it does not establish complete capabilities or the requested quality gates. All ten reports remain partial. No private App-name adapters were added.

## Implementation and tests

- Exact public native/AndroidX/support Fragment protocol declaring classes are recognized even when SDK implementations are packaged in DEX. Actual App overrides still execute their bodies. A null-return override cannot create a transaction. See [protocol boundary](GENERIC_V15_PACKAGED_FRAGMENT_BOUNDARY.md).
- Known Class identity refines bounded forward-only factory selectors; unknown/union values retain conservative summaries. Limits remain 4096 instructions, 16 variants and 100000 decode-work budget. See [Class identity](GENERIC_V15_CLASS_IDENTITY.md).
- Reflection registry discovery checks the necessary referenced transport field before decoding candidate writers. It preserves the existing registration certificate. See [reflection discovery](GENERIC_V15_REFLECTION_DISCOVERY.md).

Frozen jar SHA256: `251c897f6330ba8b8616fcac898af313b6ff8e36745117437444fd0c0b718d29`. Source and test hashes: generic-v15-build.json. capabilitySelfTest, compactReportTest and shadowJar passed (20 seconds). Fixtures include packaged SDK bodies, uninstalled/override negatives, 2/80/398-branch factories, bounded fallback and unrelated reflection writers. All ten compact/counts structure checks pass. Forced short-deadline smoke testing leaves three atomic reports in 1.292 seconds; it is not a quality test.

## Ten-App results

The v15 oracle is identical to v14 (15276 rows), so changes below compare matched counts on the same denominator. Each cell is matched/expected (change from v14). Measurements use eight disjoint logical CPUs and 16 GiB heap per App in one parallel development batch, not the required final three isolated repetitions.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 180.278 | 1260/1393 (+0) | 228/366 (+0) | 189/240 (+0) |
| mango | 250.023 | 1185/1259 (+673) | 565/608 (+234) | 581/662 (+285) |
| ctrip | 589.863 | 3682/3970 (+3128) | 369/391 (+192) | 412/444 (+252) |
| xigua | 589.177 | 30/65 (+0) | 193/278 (+13) | 7/420 (+0) |
| freereels | 590.940 | 52/69 (+0) | 104/182 (+0) | 172/420 (+0) |
| yangshipin | 50.483 | 17/17 (+0) | 189/229 (+0) | 582/624 (+0) |
| txws | 108.964 | 8/8 (+0) | 162/188 (+65) | 148/190 (+108) |
| sohuvideo | 590.102 | 504/703 (+448) | 491/501 (+390) | 1440/1592 (+1274) |
| fanqiexiaoshuo | 593.655 | 12/12 (+6) | 21/21 (+8) | 10/16 (+5) |
| breaking-news | 588.647 | 10/10 (+0) | 14/14 (+0) | 8/8 (+0) |

## Remaining failures and evidence limits

Comparison against v13 is also necessary: v14 was a regression, not an adequate success baseline. Ctrip returns to exactly the same matched facts as v13. Txws still loses 35 facts, all in HalfWebViewActivity; Mango loses 36 in MGVideoPlayActivity; FreeReels loses 34 and Sohu loses 97. Hash-bound semantic deltas are retained as generic-v15-*-vs-v13-expanded-delta.json. Source auditing and bounded runtime diagnostics drive the next repairs; v13 matches caused by uninstalled constructor seeding must not be restored without actual installation evidence.

Class-factory synthetic success has not produced a demonstrated real Fanqie recall improvement over v13. The reflection prefilter lets a diagnostic reach later holder writes, but its unfinished queue does not prove terminal recovery. Xigua Bridge/callback coverage remains poor. Full navigation/XML/restoration attachment, delayed consumers, framework registration and scheduling budgets remain open.

Current-report ownership verification, capability precision, full deep/stratum coverage, fresh traceable holdouts and repeated isolated timing are incomplete. No ownership percentage is inferred from recall. The News historical holdout claim lacks a traceable seal in the inspected directory; this is recorded as an evidence gap. New independently reviewed FreeReels development facts are not in this frozen oracle and must be appended in the next version. Old failures remain retained; sample expansion must not be confused with an algorithm change. See acceptance/generic-v15-status.json for failing gates.

## Reproduction

Build this commit in a separate checkout: the active worktree may already contain v16 changes.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
# Preserve the built jar as test/runs/generic-v15.jar before running.
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v15.jar --out test/runs/generic-v15
python3 scripts/replay_iteration.py --version v15 --previous v14 --oracle-manifest docs/validation/generic-v15-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v15.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v15-spec.json --reports test/runs/generic-v15 --out docs/validation/acceptance/generic-v15-status.json
```

Acceptance currently exits 2. Full formatted capability/compact/counts reports are under test/runs/generic-v15/<package>/. APKs, jars and full reports are excluded from Git.
