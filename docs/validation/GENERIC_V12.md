# Generic v12: narrow Class capture and Manifest boundary fixes

This version separates allocation-local Class metadata captures from the global relevance closure and fixes two Manifest protocol errors: actual receiver overrides must not be bypassed through a Context-declared call, and a missing Bundle value must not equal the string "0". Explicit super dispatch and typed literal comparisons retain their intended semantics. No App-private adapter is added.

See [Class carrier rationale](GENERIC_V12_CLASS_CARRIERS.md), [focused relevance regression](GENERIC_V12_CLASS_CARRIER_RELEVANCE_TEST.md), and [Manifest boundaries](GENERIC_V12_MANIFEST_BOUNDARIES.md).

## Tests and identity

Full capabilitySelfTest and compactReportTest pass (12s). The Class carrier regression fails on frozen v11 by pulling an unrelated writer/factory/caller into relevant; v12 excludes them while retaining the real capability seed and twelve service factory positive/negative paths. The Manifest regression reproduces both old errors and verifies the fixes, including explicit super versus virtual dispatch. Source/log/jar hashes are in generic-v12-build.json.

All ten compact/counts consistency checks pass. A short forced deadline retains three valid reports (generic-v12-deadline.json). That smoke check does not replace full performance repetitions. Static reflective method invocation and Application bootstrap are later work, not in this jar.

## Real-App results

Both v11 and v12 are scored on the same generic-v12-expanded-facts oracle. All three category numerators are unchanged from v11. Development source facts remain distinct from sealed holdouts.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 135.701 | 0 | 1260/1393 | 228/366 | 189/240 |
| mango | 146.524 | 0 | 1215/1259 | 565/608 | 587/662 |
| ctrip | 588.844 | 0 | 3682/3970 | 369/391 | 412/444 |
| xigua | 589.192 | 0 | 30/65 | 193/278 | 7/420 |
| freereels | 588.547 | 0 | 23/31 | 84/136 | 56/225 |
| yangshipin | 40.459 | 0 | 17/17 | 189/229 | 582/624 |
| txws | 73.098 | 0 | 8/8 | 175/188 | 170/190 |
| sohuvideo | 586.886 | 0 | 521/703 | 501/501 | 1510/1592 |
| fanqiexiaoshuo | 588.275 | 0 | 12/12 | 21/21 | 10/16 |
| breaking-news | 589.388 | 0 | 10/10 | 14/14 | 8/8 |

Mango recovers from588.274s to146.524s with no loss in measured facts. News retains the four title callback gains introduced in v11. Six Apps still approach587–589s; all reports remain partial. Every process exits normally below600s, but this is one parallel development batch, not the required three isolated final runs.

Ctrip's v11 loss of six setting and nine callback matches relative to v10 persists. All fifteen involve HotelFlagShipLoginActivity's primary/popup WebViews. Terminal reports show this host still pending: v10 processed11,891 contexts and emitted14 fact records, v11 processed7,903/emitted0, v12 processed8,969/emitted0. A single-host diagnostic using the frozen v10 and v12 jars recovers all fifteen expectations in both versions, with no semantic match delta: each emits14 fact records. v10 processes19,669 contexts in75.36s and v12 processes21,613 in79.95s, including indexing; both still report local budget exhaustion. This supports a work-volume/budget explanation rather than a total inability to bind this host, but does not establish which individual extension caused the extra work. These isolated diagnostics are excluded from ten-App recall and all performance gates. See ctrip/generic-v12-login-regression-runtime.json and ctrip/generic-v12-login-single-host-results.json. No lost expectations were deleted.

News's hash-bound all-output ownership audit remains32valid/0wrong/1unresolved out of33 (3.03% upper bound). Source chains are explicitly reused; capability precision is not certified by this host audit. Most current-report ownership, complete deep/stratum audits, fresh holdouts and three-run performance evidence remain missing. Strict acceptance is0/10.

Known gaps include Application/SDK registration initialization, Scene/Fragment factory installation, runtime settings expressions, and dynamic dispatch. News Ysp and Sohu Manifest module expectations still do not recover. Source-only FreeReels audits subsequently expanded beyond this frozen oracle; their new failures will be appended to the next freeze, not silently omitted.

## Reproduction

Jar SHA256: `153037dce2e818432ee3e555064c5d55cf5860a2ff0672f6c9c6e1ab1bbce6c6`. Eight disjoint logical CPUs and16GiB JVM maximum heap per APK. Local source snapshot is test/runs/generic-v12-source; jars/APKs/reports are ignored by Git.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v12.jar --out test/runs/generic-v12
python3 scripts/replay_iteration.py --version v12 --previous v11 --oracle-manifest docs/validation/generic-v12-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v12.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v12-spec.json --reports test/runs/generic-v12 --out docs/validation/acceptance/generic-v12-status.json
```

Rebuild this version's commit in a separate checkout; do not use a later worktree build under the v12 label. The full, compact and counts-only reports are under test/runs/generic-v12/<package>/.
