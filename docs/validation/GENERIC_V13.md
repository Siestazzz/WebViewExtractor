# Generic v13: Application initialization and bounded static reflection

This version follows the actual Manifest Application constructor, attachBaseContext and onCreate before Activity workers, then imports independent copies of the resulting static-object graph. It adds exact public static no-argument reflection for Class.getMethod and Method.invoke. It does not substitute an application delegate class for the actual Manifest root, open private methods, or attach startup WebViews to arbitrary Activities. Implementation and limits: [Application bootstrap](GENERIC_V13_APPLICATION_BOOTSTRAP.md).

## Identity and tests

Jar SHA256: `893801996f1b4822fdeb79c006544527e071a2b381d743c913025cb03ec2f22a`. Frozen source, source hashes and passing self-test/compact test log are recorded in generic-v13-build.json. Tests cover actual versus invalid Application roots, initialization ordering, heap-copy isolation, public static reflection and access/arity/instance negatives. All ten compact/counts projection checks pass. The short external-deadline smoke test retains three atomic reports in1.285s; it is not a full-App quality or performance pass.

## Same-oracle real-App comparison

All ten processes exited0 below600s, with partial reports. On the same expanded v13 oracle, every Bridge, setting and callback numerator is unchanged from v12. FreeReels has more source expectations than the v12 publication; evaluating both jars on those expectations prevents attributing the changed denominator or newly measured matches to this code change.

| App | Seconds | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|
| news | 143.584 | 1260/1393 | 228/366 | 189/240 |
| mango | 162.080 | 1215/1259 | 565/608 | 587/662 |
| ctrip | 589.377 | 3682/3970 | 369/391 | 412/444 |
| xigua | 589.177 | 30/65 | 193/278 | 7/420 |
| freereels | 589.748 | 35/43 | 98/162 | 102/310 |
| yangshipin | 43.663 | 17/17 | 189/229 | 582/624 |
| txws | 81.205 | 8/8 | 175/188 | 170/190 |
| sohuvideo | 588.139 | 521/703 | 501/501 | 1510/1592 |
| fanqiexiaoshuo | 585.409 | 12/12 | 21/21 | 10/16 |
| breaking-news | 589.204 | 10/10 | 14/14 | 8/8 |

This is one parallel development batch with eight disjoint logical CPUs and16GiB maximum JVM heap per App. It does not satisfy three isolated final repetitions. Strict acceptance remains0/10; see acceptance/generic-v13-status.json. News all-output host ownership was re-bound to this report:32valid,0wrong,1unresolved of33, upper bound3.03%. This reuses independently checked source evidence, not fresh holdouts, and does not certify attached capability precision.

## Remaining failures and next actions

News initialization consumes15.386s in its first context despite a cooperative1s startup budget, ending with no retained static heap. A frozen-jar isolated bootstrap reproduces12.405s wall/12.155s CPU: all503 retained stack samples contain ReflectionProtocols.discoverFields,270 also contain DexFlow.decode. The first context invokes reflection.writer, triggering global lazy discovery without the local deadline. See news/v13-bootstrap-local-budget-diagnostics.md. This CPU observation is distinct from queue-count evidence. Its actual proxy/hidden attach chain is also outside the supported static reflection subset. No Ysp recovery is claimed.

Application-owned shared WebViews are explicitly unmodeled for later Activity capability reattribution. Static startup facts remain unattributed. Ctrip's fifteen lost expectations remain absent in the full batch; the prior isolated-host diagnostic is not substituted into these scores. Scene/Fragment factory propagation, runtime values and incomplete source/ownership/holdout coverage remain material gaps.

Separate experiments identify two generic failures for the next version. Constructor arguments captured eagerly still do not make a deferred constructor body temporally sound when it reads a mutable outer field. An installed Fragment can retain the correct delegate object yet have its field-delegating lifecycle excluded by relevance filtering. Diagnostic insertion on the same actual Fragment receiver recovers the synthetic abilities; an uninstalled Fragment control also exposes an ownership false positive. These experiments are not production fixes or real-App gains. See experiments/ALLOCATION_CONSTRUCTOR_CAPTURE.md and xigua/v12-installed-fragment-receiver-trace.md.

Source-only audit expansion continues append-only. Later FreeReels facts are reserved for the next frozen oracle, not silently removed or used as acceptance holdouts.

## Reproduction

Rebuild this commit in a separate checkout; later working-tree changes are not this jar.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar test/runs/generic-v13.jar --out test/runs/generic-v13
python3 scripts/replay_iteration.py --version v13 --previous v12 --oracle-manifest docs/validation/generic-v13-expanded-facts/manifest.json
python3 scripts/check_deadline.py --jar test/runs/generic-v13.jar --apk test/apks/txws-patched.apk
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v13-spec.json --reports test/runs/generic-v13 --out docs/validation/acceptance/generic-v13-status.json
```

Full, formatted compact and counts-only reports: `test/runs/generic-v13/<package>/`. APKs, jars and full reports remain excluded from Git.
