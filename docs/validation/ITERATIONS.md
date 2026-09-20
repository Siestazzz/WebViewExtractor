# Iteration ledger

## v1 — DEX index and bounded object/parameter binding

- Original source: 78afea4; input/protocol setup: a87f9c6.
- Implementation: binary manifest/layout inventory; complete DEX call index; lazy,
  CFG-based symbolic method summaries; object/field/parameter binding; per-Activity
  aggregation; native JS interface members; client overrides; observed Settings values;
  diagnostics and supervisor-enforced deadlines. Original CLI available via --legacy.
- Tests: `./gradlew capabilitySelfTest shadowJar --offline --console=plain` passes.
  Covers separate WebViews, helper argument substitution, settings receiver identity,
  annotation-based bridge exposure, callbacks, stale constants and API owner matching.
- Serial v1 run (`scripts/benchmark.py --label v1 --jar test/runs/v1.jar --cpu-start 8`):
  news 24.13 s; mango 30.84 s; ctrip 16.71 s. CPUs 8–15, heap 16 GiB. Original baseline
  ran on CPUs 0–7 concurrently, so these are development measurements, not final isolated
  acceptance runs. See v1-benchmark.json for exact commands and samples.
- All three emitted partial reports; time targets alone do NOT establish success.
  News: 23 emitted Activities, 162 unowned entries; mango: 70 Activities, 196 unowned entries.
- Independent gold is still being expanded and normalized. Initial mechanical replay found
  incomplete signatures, anonymous-client owner mismatches in oracle, and true gaps in
  plugin/message registrations. These require evidence-based correction; no 95% claim.
- Known remaining implementation work: registry/plugin models, virtual dispatch alternatives,
  implicit framework entrypoints, better field aliasing, flow budgets, Soot fallback,
  partial timeout mid-host persistence, legacy exports, full synthetic suite, strict complete
  gold replay, all-output host review and held-out verification.
- Key lesson: full DEX indexing is inexpensive (~13–21 s here). Main remaining problem is
  coverage and correct association, not parsing every application method into Jimple.

## v2 — shared registries, reflective surfaces and bounded dispatch (v2d build)

- Added annotated transport/shared Map recognition, scheduled transport handlers, reflective
  registered endpoints, object-field paths to reflective receivers, plugin instance TAG constants,
  callback entry expansion and narrower inherited Activity entrypoints. No Activity names encoded.
- Tests: capabilitySelfTest passes, including message registry vs HTTP header map and instance TAG.
- Original baseline: news 383 s, mango 507 s, ctrip 259 s (see baseline-benchmark.json).
- Development experiments retained: v2/v2b, v2c (broad dispatch caused a major ctrip regression),
  v2d (restrict dispatch and avoid evaluating irrelevant arguments). The earlier v2-dev broad
  dispatch experiment was manually stopped after relevance reached 212,257 methods; not a pass.
- v2d serial apps: news 37.16 s, mango 54.00 s, ctrip 41.07 s. Heap 16 GiB, 8 CPUs (8–15),
  but v2c was concurrently running on 0–7. These are NOT final isolated performance results.
  Exact commands, RSS, phases and environment: v2d-benchmark.json, v2-environment.json.
- Candidate-inclusive replay on current independent canonical facts:

  | App | Bridge | Settings | Callbacks |
  |---|---:|---:|---:|
  | news | 1246/1246 | 139/164 | 128/168 |
  | mango | 8/977* | 237/237 | 236/236 |
  | ctrip | 3659/3922 | 261/274 | 279/325 |

  * Mango oracle still contains interface/runtime placeholder implementations and symbolic
  registration names; this comparison is deliberately not relaxed or labelled a quality pass.
  Sol is correcting these against source/DEX with an audit trail and expanding partial hosts.
  Explicit-only recall is recorded separately in evaluation JSON and is much lower.
- Independent Sol found a real false-positive family: unrestricted H5Plugin subtype expansion
  adds 6 types that the corresponding factory cannot produce (330 duplicated v2b facts).
  Conditional plugins and core factory plugins must remain separate. This is unresolved in v2.
- Remaining: collection/factory precision; delegated/super callbacks; news rich-editor hosts;
  full mango oracle; all emitted Activity/capability ownership audit; fresh held-out testing;
  mid-host checkpoints; full synthetic suite; on-demand Soot fallback; compatibility exports;
  three isolated repeats per APK. All reports remain partial. QUALITY ACCEPTANCE NOT MET.
- Reproduce: `./gradlew capabilitySelfTest shadowJar --offline --console=plain`, copy jar to a
  new immutable test/runs label, then `python3 scripts/benchmark.py --label LABEL --jar JAR`;
  `python3 scripts/evaluate.py --report REPORT --oracle docs/validation/APP/canonical-facts.jsonl --out RESULT`.

## v3 — object field dependencies, namespaces, super callbacks, checkpoints (v3f)

- Preserves explicit invoke-super callbacks without adding shadowed ancestors; models collection
  factories, array/asList/addAll/iterator flow, concrete interface factory returns, constructor and
  field-writer dependencies, DSBridge namespace registries and annotation/parameter-shape gates.
- Added bounded callback-allocation expansion (within two reverse calls of a capability seed),
  two-site allocation context, synthetic/static field exclusions and referenced-field host gating.
- Added mid-Activity snapshots, grouped WebView capability indices, failed-worker diagnostics,
  benchmark RSS/environment/JAR hashes and ownership-aware scoring. Cleaned generated pyc files.
- Tests: helper/field binding, two-WebView isolation, instance TAG, annotated vs hidden bridge,
  super vs uncalled ancestor callbacks, factory element isolation, branch join, loop convergence,
  stale-register overwrite, API identity and registry/header distinction all pass.
  `scripts/check_deadline.py` forced a 1-second limit: 1.303 s total, 57 parseable atomic snapshots,
  final timeout report with index_not_finished/supervisor_hard_deadline diagnostics.
- v3f three serial fresh processes, CPUs 0–7 / heap16GiB: news 38.00s (RSS 6.84GiB), mango
  50.75s (7.43GiB), ctrip 27.85s (7.51GiB). Full commands/phases in v3f-benchmark.json.
- On expanded source/DEX oracle at replay time: news Bridge1252/1338, Settings209/283,
  Callbacks181/217; mango Bridge1085/1102, Settings464/505, Callbacks512/540;
  ctrip Bridge3666/3666, Settings257/257, Callbacks299/299. Oracle/report SHA256 are recorded
  in replay files. Counts are evidence assertions on the independent development set, not proof
  of whole-APK completeness. New frameworks exposed genuine missed capabilities.
- Ctrip emitted-host review:52 valid,1 wrong,2 uncertain; conservative3/55=5.5%. Original
  uncertain hotel decisions were independently revisited along actual map/gallery/login paths.
  Known wrong feedback Gallery path remains. Plugin fallback facts decreased2632→188 but
  unconstrained subtype fallback still creates incorrect capability associations; not accepted.
- Oracle corrections retain source/DEX evidence and prior Git versions: SimpleOversea was not a
  general H5 host, EvaluateDialog had no Fragment Chrome client, news shadowed ancestor callbacks
  were not executable, CMB's true writer is initJsInterface (constructor only writes null), and
  mango message endpoint names/owners were independently normalized. No failed case was silently
  converted into a pass. New canonical facts continue expanding the denominator.
- Retained development regressions: v3b cast-driven expansion exhausted host context budgets;
  v3c bounded object context recovered; v3e unrestricted callback closure reached56483 relevant
  methods and was explicitly aborted (v3e-aborted.json; other apps not run in that experiment).
  v3f narrows it to1840 methods for news. These are experiments, not successful acceptance runs.
- Still unmet: news new SDK/player/prompt and editor surfaces; remaining mango SDK surfaces;
  precision of unresolved plugin/host edges; complete all-output ownership/capability review;
  held-out final validation; full synthetic suite and three final isolated repeats. QUALITY
  ACCEPTANCE NOT MET. No whole-APK completeness or final performance claim.

## v4 — bounded refinement, wrapper discovery, explicit scoring limits (v4e)

- Added receiver-union field distribution and distinct union identities, Kotlin lazy WebView
  factories, ViewBinding wrapper constructor discovery, usage-only WebView seeds, bounded SDK
  override reachability, and type-guard branch specialization. Specializations are cached and
  limited to 16 contexts per eligible method (at most 500 instructions); conservative summaries
  remain when no specialization is possible. Unknown receiver types no longer trigger every
  compatible subclass method. This reduces spurious ownership but loses unresolved true edges.
- Added URL/client callback transport discovery for registry maps, plus regressions for callback
  transport vs header maps and cached type guards. Existing JVM synthetic tests pass. Five Python
  evaluator regressions pass (duplicate rows, unknown targets, exact Settings owners, normalized
  member signatures, and proven-empty endpoint surfaces). A supervisor deadline check finished
  in 1.275s with 57 parseable atomic snapshots and an explicit timeout report.
- Scoring revision 2 removes semantic duplicate weighting, separates registration/member scores,
  checks exact Settings owners and preserves unresolved surfaces as misses. It does NOT verify
  WebView object identity. Sol identified and black-box tested scoring defects independently.
  Same-oracle v3f/v4d/v4e replays are retained as `*-scoring-v2.json`; original score files remain
  historical and must not be compared across oracle/scoring revisions.
- Sol completed Mango's 30-host development oracle and independently established that 56 Imgo
  registrations have no compatible declared String endpoint. The registrations remain in all
  evidence files, with a source/DEX audit reference and a reproducible state transition. The
  engine currently invents the dispatcher as an endpoint for some of these routes; the stricter
  evaluator rejects them. Only two dynamic target-unknown registrations remain in that oracle.
- v4e serial fresh JVMs, CPUs0–7,16GiB: news77.64s, mango63.58s, ctrip123.83s. All outputs remain
  partial. These are development measurements, NOT final isolated three-repeat acceptance runs.

| App | v3f Bridge | v4e Bridge | v3f Settings | v4e Settings | v3f Callbacks | v4e Callbacks |
|---|---:|---:|---:|---:|---:|---:|
| news | 1252/1338 | 1252/1338 | 201/283 | 212/283 | 181/217 | 181/217 |
| mango | 1086/1153 | 906/1153 | 479/526 | 519/526 | 544/591 | 584/591 |
| ctrip | 3666/3666 | 2642/3666 | 233/233 | 233/233 | 299/299 | 299/299 |

- News all-output review:27 valid/3 wrong/0 uncertain, upper bound3/30=10%. Mango:77 valid/
  0 wrong/5 uncertain, upper bound5/82=6.10%. Six source-valid news hosts and seven source-valid
  mango hosts disappeared relative to v4d: these are recall regressions, not ownership fixes.
  Some surviving News TencentVideoWebView paths were independently confirmed as legitimate
  conditional layouts; union alternatives still mix sibling receivers and need filtering.
- Ctrip all-output review:54 valid/1 wrong/0 uncertain, upper bound1/55=1.82%. Three new
  source-valid hosts replace three prior outputs (one source-valid and two cleanup-only uncertain).
  The 1024 lost Bridge facts are exactly four v1 H5Fragment hosts ×256: the missing edge is
  the installed provider interface dispatch to `ctrip.base.init.m$c.n`, not absent plugin classes.
- Failed experiments retained: v4b and v4c repeatedly decoded branches and were stopped with
  partial snapshots; v4d overbroad override reachability expanded Ctrip to20895 relevant methods,
  was stopped at402.98s with153/410 hosts processed, and exited2 with a valid failed report.
  v4e bounds specialization and propagation; no failed run is called a performance pass.
- Reproduction: build using the documented Gradle command, freeze the jar, then
  `python3 scripts/benchmark.py --label LABEL --jar test/runs/LABEL.jar` and replay all three
  canonical oracles using `scripts/evaluate.py --ownership docs/validation/APP/v4e-ownership.jsonl`.
- Next fixes: per-alternative dispatch receiver substitution (including private/direct invokes),
  reflective registry endpoint outcomes, lost WebUI/factory receiver-field edges, bounded player
  adapter propagation, AdCore/Loading prompt semantics and URL bridge dispatch. Retain every
  discovered miss and independently review output capability identity. Soot fallback, full
  synthetic coverage, held-out final verification and three final repeats remain unfinished.
  QUALITY ACCEPTANCE NOT MET. No whole-APK completeness claim.

## v5 — concrete provider recovery and receiver-specific dispatch

- Direct/private calls retain exact method identity. Virtual jobs substitute individual receiver
  alternatives, including equal aliased arguments, instead of running each target with the full
  sibling-object union. The previously source-confirmed News TencentVideo initializer counterexample
  now has45 correctly typed facts and0 unknown-union facts (v4e:45 typed+15 unknown unions).
- Static interface providers are recovered from actual indexed field setter callsites and concrete
  object arguments, bounded to64 inspected callers. This restores the Ctrip v1 installed-provider
  path without selecting every class that implements the interface. Unknown dispatches remain gaps.
- Reflective handler lookup now distinguishes a known missing declared endpoint from an unknown
  target. A reflective dispatcher is not itself emitted as the named JavaScript endpoint. Registration
  remains present even when its callable member list is empty.
- Added executable DEX fixtures for installed-vs-uninstalled providers, exact private initializer
  dispatch and known empty/existing reflection endpoints. Existing JVM and evaluator tests pass.
- Three serial fresh-JVM runs: news78.60s, mango74.21s, ctrip136.20s; all outputs partial. Ctrip's
  four-host256-fact-per-host regression is recovered. These are development runs, not final repeats.

| App | Bridge | Settings | Callbacks |
|---|---:|---:|---:|
| news | 1252/1338 | 212/283 | 181/217 |
| mango | 1092/1153 | 519/526 | 584/591 |
| ctrip | 3666/3666 | 233/233 | 299/299 |

- Ctrip still emits969 unsupported subtype-inferred Bridge facts. A separate diagnostic replay
  excluding only that inference (retaining all Activity identities) loses no current canonical fact;
  see v5-ctrip-subtype-dependence.json. This is a dependency experiment, NOT an acceptance report.
  Remove the actual production inference next and rerun all apps before claiming that precision fix.
- News and Ctrip emitted Activity sets are unchanged from independently reviewed v4e. Mango removes
  two previously valid Activity candidates; a smaller output is not evidence that their paths are
  impossible. All-source/new-sample validation and bound-object precision remain necessary.
- Independent Sol v5 audit confirms all56 Imgo semantic registrations retained, all416 duplicate
  carrier records have empty members and the verified no-compatible-endpoint status. Mango
  ownership is75 valid/0 wrong/5 uncertain (5/80=6.25%). Async registry evidence identifies
  mglive BridgeWebView$a and mgadplus BridgeWebView$c queue callbacks as the missing map-read edge.
- Still failing: News settings/callback and several frameworks, Mango Bridge below95%, dynamic
  registrations, unsupported message/prompt transports, false capability associations, known wrong
  Activity hosts. No final holdout or three-repeat acceptance run yet. QUALITY ACCEPTANCE NOT MET.
- Reproduce with the documented build, `scripts/benchmark.py --label v5 --jar test/runs/v5.jar`,
  then the three canonical evaluator commands. Benchmark JSON preserves exact commands/hashes.

## v6 — asynchronous registries and removal of unsupported subtype inference

- Removed native and reflective Bridge implementation enumeration based solely on a declared
  receiver type. Unresolved native implementations retain the registration and declared contract,
  explicitly mark implementation unknown and record a diagnostic. No v6 app emits the old
  `type_compatible_bridge_implementation` inference. Ctrip keeps full development recall without it.
- Registry discovery follows allocated String-result callbacks and custom client adapter helper
  methods. Transport budgets now count unique visited methods and emit exhaustion diagnostics.
  Added executable fixtures for async registry map reads and unknown Bridge retention without an
  unallocated subtype. Scoped SDK override propagation excludes generic platform ancestors.
- All JVM fixtures pass. Serial fresh-JVM measurements: news76.71s, mango81.96s, ctrip121.09s.
  No output is labelled complete; these are development runs, not final repeats.
- Current Mango recall: Bridge1136/1153 (98.53%), Settings519/526 (98.67%), Callbacks584/591
  (98.82%). Sol independently verifies all44 newly matched message endpoints (25 mgadplus,
  19 mglive) by registration, host and exact DEX member, not by tool counts. Ownership is77 valid,
  5 uncertain,0 wrong across82 outputs; two previously verified channel hosts are recovered.
- Negative check: WebContainer has108 facts but ZERO programmatic Imgo handler registrations.
  The XML constructor path keeps its separate native transport. The checkUpdate endpoint remains
  a real miss: its known handler's downstream business reflection must not erase that handler.
- Ctrip: Bridge3666/3666, Settings233/233, Callbacks299/299; emitted Activity identities match the
  previously reviewed55. Bound-object precision and final fresh cases remain pending.
- News public source coverage was expanded from23 to28 deep positive hosts (1931 canonical rows).
  Same-oracle v5/v6 replay is1252/1365 Bridge,221/338 Settings,181/228 Callbacks. The old23-host
  replay is retained separately.28 public positives does NOT satisfy the minimum30 gate; merge
  final sealed cases and verify the count or add further evidence before acceptance. An unbound
  QADetailPage remains in activity-null evidence rather than being assigned by module/name.
- Source-verified next edges: QNRouter generated Class-key/APIMeta providers returning YSP players;
  AdCore page and system-wrapper factory identity; AdCore prompt annotation protocol; Loading static
  class prompt registry. Mango's Loading14+dynamic1 remains a systemic miss despite aggregate95%.
  New verified evidence lives under news/ysp-provider-binding and adcore-wrapper-factory.
- Still pending: those framework fixes, lower callback/settings gaps, all-output capability
  precision, final new samples, full synthetic matrix, Soot fallback and three isolated repeats.
  QUALITY ACCEPTANCE NOT MET; no all-APK completeness claim.
- Reproduce with the documented build, frozen v6 jar and benchmark script; run the three canonical
  evaluators. News uses v6-news-development-v2-evaluation.json for the expanded current oracle.

## v7 — registered services, endpoint reflection boundaries, and independent object audit

- Added QNRouter Class-key/qualifier/APIMeta service resolution. Only registered implementation
  classes become candidates; unregistered subtypes do not. Queries preserve exact nonempty names,
  normalize null/empty lookup names to the default, and retain an unknown result for unresolved
  custom creators. Every used registration carries per-Activity `service_bindings` evidence and
  explicit initialization/replacement/construction conditions. Runtime mutation ordering is not
  proven and no candidate is promoted merely because a generated registration exists.
- A known message handler's downstream business reflection no longer suppresses its real interface
  endpoint. Fixed-class registered reflection still distinguishes an existing endpoint from a
  proven empty surface. This restores Mango's `checkUpdate` endpoint without exporting its body.
- New DEX fixtures check metadata resolution, unregistered subtype/qualifier exclusion, null/empty
  lookup keys, custom creator uncertainty, and business-reflection separation. JVM fixtures and all
  five evaluator tests pass. Updated usage distinguishes the default engine from `--legacy`.
- v7a exploratory full runs: news91.87s, mango85.89s, ctrip124.97s. After Sol's qualifier/creator
  review, frozen v7 full serial runs: news91.99s, mango86.52s, ctrip128.40s. All use fresh JVMs,
  eight logical CPUs and16GiB heap. Peak RSS respectively7842432/7902252/7638324KiB. All reports
  remain partial. These are development measurements, not final isolated three-repeat acceptance.

| App | Bridge | Settings | Callbacks |
|---|---:|---:|---:|
| news | 1264/1365 (92.60%) | 275/338 (81.36%) | 193/228 (84.65%) |
| mango | 1137/1153 (98.61%) | 519/526 (98.67%) | 584/591 (98.82%) |
| ctrip | 3666/3666 (100%) | 233/233 (100%) | 299/299 (100%) |

- News uses the same28-host expanded public oracle as v6. Four newly emitted YSP hosts and the
  recovered provider edge were independently source-verified. The long-video host-to-inherited
  controller path, four YSP message handlers per reached host, and AdCore transports remain gaps.
- Ownership review itself was corrected: the older MobileQQ/Qzone `wrong` labels incorrectly
  required the SDK WebView to escape into an Activity field. Source confirms actual
  `new TDialog(activity,...).show()` with the same Activity. Such displayed Dialogs count as host
  capabilities. Original v7a labels are preserved; the independent supplement and v7 ownership
  record33 valid/1 wrong/0 uncertain (1/34=2.94%). Hippy's unsupported inherited-method expansion
  remains wrong. This evidence correction is not an Activity-name production exception.
- Mango's82 hosts are unchanged (77 valid/5 uncertain,6.10% upper bound); Ctrip's55 are unchanged
  (54 valid/1 wrong,1.82%). These ownership numbers do not measure capability binding precision.
- Independent four-host Ctrip object audit found a material defect despite100% Activity-level
  recall: old H5's one runtime WebView is split into nine symbolic IDs, newer H5's cache/fresh
  alternatives and helper aliases are duplicated, and nullability can appear as a receiver
  alternative. Unknown collection summaries also create duplicate unresolved Bridge candidates.
  These findings remain open; raw symbolic group count must not be read as a runtime view count.
- Source-verified Loading/RainbowBridge commit semantics are now documented: pending clazz entries,
  nonempty inject clears/replaces the active registry, simple-name collisions, and exact public
  static declared-method filtering. Its14 known endpoints plus dynamic registration remain missed.
- Other residuals: settings/callback gaps, false inherited entrypoint expansion, object aliasing,
  dynamic uncertainty, full negative/multi-WebView tests, on-demand Soot fallback, >=30 deeply
  checked News positives, final fresh holdouts, and three-repeat performance validation.
  QUALITY ACCEPTANCE NOT MET. No APK-wide completeness claim.
- Reproduce: build per docs/CAPABILITY_ANALYSIS.md, freeze the jar, run
  `python3 scripts/benchmark.py --label v7 --jar test/runs/v7.jar`, then scripts/evaluate.py with
  the three canonical JSONL files and news/v7-ownership.jsonl, mango/v6-ownership.jsonl,
  ctrip/v4e-ownership.jsonl. Exact commands, hashes, resource usage and replays are retained.
