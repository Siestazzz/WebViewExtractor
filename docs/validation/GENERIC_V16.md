# Generic v16: stable Fragment manager propagation and renderer callbacks

Strict acceptance remains **0/10**. This version restores the 35 HalfWebViewActivity facts lost after v14. It does not establish complete capabilities or pass the per-App quality gates. No private App-name rules were added.

## Implementation and tests

- Explicit SDK super calls resolve the declared Fragment contract, while ordinary App overrides still execute their actual bodies.
- Constructor capture accepts a proven stable SDK FragmentManager acquisition at the allocation point. Unknown/mutable argument timing remains unresolved.
- Registered deferred WebView field consumers can replay within the same phase when the queue becomes quiescent. This preserves receiver identity; it does not refresh frozen constructor arguments.
- Exact platform WebViewRenderProcessClient registration and its two full callback signatures are supported, including inherited members and the Executor overload.

Details: [Fragment and capture mechanisms](GENERIC_V16_IMPLEMENTATION.md), [super dispatch](GENERIC_V16_FRAGMENT_SUPER.md), [renderer contract](GENERIC_V16_RENDER_PROCESS_CLIENT.md). Full capabilitySelfTest, compactReportTest and shadowJar passed in18s; new tests retain null/uninstalled/wrong-signature and temporal-order negatives. Jar SHA256 `6bf20826825e84a2cbe090aaa998a9ecff90be92e66665e7b8a6afdc1585789f`; source/test hashes are in generic-v16-build.json. The forced short-deadline smoke check retains three atomic reports in1.289s, not a quality pass.

## Ten-App measurement

Each cell shows matched/expected and the change from v15 evaluated against the same v16 oracle. The oracle has15386 rows, including110 newly reviewed FreeReels observations; four unproved zoom facts remain preserved but unaccepted. Source snapshots are not claims of whole-APK coverage. This is one parallel development batch, eight disjoint logical CPUs and16GiB JVM heap per App, not the required three isolated final repetitions.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 180.695 | 1260/1393 (+0) | 228/366 (+0) | 189/240 (+0) |
| mango | 258.266 | 1185/1259 (+0) | 565/608 (+0) | 581/662 (+0) |
| ctrip | 588.954 | 3682/3970 (+0) | 369/391 (+0) | 412/444 (+0) |
| xigua | 588.929 | 30/65 (+0) | 193/278 (+0) | 7/420 (+0) |
| freereels | 589.292 | 63/82 (+0) | 179/261 (+0) | 176/424 (+3) |
| yangshipin | 52.687 | 17/17 (+0) | 189/229 (+0) | 582/624 (+0) |
| txws | 105.181 | 8/8 (+0) | 175/188 (+13) | 170/190 (+22) |
| sohuvideo | 592.794 | 504/703 (+0) | 491/501 (+0) | 1440/1592 (+0) |
| fanqiexiaoshuo | 581.501 | 12/12 (+0) | 21/21 (+0) | 10/16 (+0) |
| breaking-news | 590.222 | 10/10 (+0) | 14/14 (+0) | 8/8 (+0) |

All ten processes exit0 below600 seconds, but every report remains partial. Only four complete within300 seconds. All ten compact/counts structure checks pass. The renderer addition recovers three independently verified FreeReels facts; the other eight Apps have no matched-count gain on the shared oracle.

## Gaps and follow-up

Half recovery validates the actual transparent-controller manager path; the separate DialogFragment.show route is still unmodeled. Mango's helper has independently proved actual standard installation, but the measured surface has not recovered. Source review also identifies obfuscated packaged SDK transaction entry methods in FreeReels: the exact public-name protocol does not cover these aliases. No speculative allocation-as-install fallback is added.

Xigua remains a systemic gap. Its v15 measurement retained approximately6.6million heap entries and499thousand pending contexts, while startup imported no static entries; these observations motivate runtime localization, not an assertion of a unique cause. The next worktree separately reserves startup snapshot time within the existing total budget; that change is not in this jar and has no real-App quality claim yet.

Current-report ownership and capability precision, complete deep/stratum source coverage, traceable fresh holdouts and isolated repeated timing remain incomplete. Candidates remain included. Unknown source/runtime values are not interpreted as successful matches or evidence of no capability. Independent source expansion continues after this freeze and must be appended in future runs. See acceptance/generic-v16-status.json for the failing gates.

## Reproduction

Build this commit in a separate checkout; the continuing worktree may contain v17 changes.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain
# Preserve the built jar as test/runs/generic-v16.jar.
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v16.jar --out test/runs/generic-v16
python3 scripts/replay_iteration.py --version v16 --previous v15 --oracle-manifest docs/validation/generic-v16-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v16.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v16-spec.json --reports test/runs/generic-v16 --out docs/validation/acceptance/generic-v16-status.json
```

Full, formatted compact and counts-only reports: test/runs/generic-v16/<package>/. Full reports, jars and APKs are excluded from Git. Compact/counts entries sort by Bridge count, callback count, settings count descending.
