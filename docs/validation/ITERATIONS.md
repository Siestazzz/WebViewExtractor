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

## v8 — object provenance, constructor capture, and nested transport evidence

- Removed context-free WebView helper parameter seeds, unresolved field-object component seeds,
  and uninvoked inherited business methods on callback objects. Real caller arguments now drive
  these paths. Lookup-derived views replay only their XML constructor contract in the second pass;
  this preserves real constructor settings without activating unrelated programmatic bridge paths.
- Materialized actual constructor captures before dereferencing objects returned by factories;
  same-receiver delegated constructors retain the original object. Null receiver alternatives do
  not become WebViews. Static false-valued settings remain valid operations.
- Added receiver-field writer dependencies for composed controller/manager calls, bounded and
  relevant-call driven. Abstract-class handler contracts are accepted alongside interfaces.
  This was NOT the YSP root cause: its handler contract was already an interface and extracted.
- Added `transport_bindings` for a handler's actual native Bridge object, injected namespace and
  WebView. Evaluator revision3 uses that edge to match nested handler selectors without treating
  the selector as the injected native name. This accounts for24 recovered YSP matches in the
  existing oracle; these are corrected evidence/scoring matches, not newly discovered endpoints.
- Obfuscated Kotlin Lazy recognition checks the Lazy/Function0 interface contracts, the actual
  returned allocation, and constructor capture of the exact initializer argument. Per-object
  captures and Object-returning invoke methods are evaluated; unrelated initializer implementations
  are not enumerated. A factory discarding its initializer is a negative regression fixture.
- Added regression fixtures for nullable instance receivers versus static false, composed receiver
  initialization, helper and inherited-callback isolation, XML constructor replay, two-view
  constructor capture, abstract handlers, native namespace/object isolation and obfuscated Lazy.
  JVM fixtures and all six evaluator tests pass. The full requested synthetic matrix is not yet
  complete (in particular temporal replacement/removal and more complex cross-framework cases).
- Exploratory fresh serial runs retained: v8a73.30/77.81/154.32s;
  v8b58.40/57.99/109.40s; v8c69.05/61.72/116.21s; v8d69.46/62.64/121.09s
  (news/mango/ctrip). Some development builds/probes overlapped these exploratory runs; these are
  not final isolated repeat measurements. v8b/c/d exposed27 news settings lost when fabricated
  field objects were removed; v8d's interface-only Lazy change did not recover them. The final v8
  adds actual initializer captures and Object-returning invoke traversal. All reports remain partial.
- Accepted60 Sol source/DEX facts for two conditional SDK Dialog hosts into the versioned public
  news oracle (30 hosts1991 raw rows). These are non-blind development cases, not final holdouts.
  The original28-host oracle is preserved. Replayed v7 against the expanded set:1264/1385 Bridge,
  303/366 Settings,205/240 callbacks. No failing oracle case was removed.
- Independent v8c Ctrip object audit confirms the two checked old-H5 hosts each now have one
  receiver for60 semantic operations (previously nine IDs). CMB now has its concrete layout-view
  identity. New H5 still has factory alternatives and self-field alias duplication:453 rows for72
  semantic operations over12 IDs. Its100% Activity-level recall does not resolve this defect.
- Sol confirmed unchanged v8c host sets: news37 valid; mango85 hosts80 valid5 uncertain;
  ctrip55 hosts54 valid1 wrong. Candidate Activities count in full. These ownership numbers do
  not measure wrong capability combinations. See the new `OPEN_GAPS.json` and retained per-fact
  evaluation misses for systemic defects, cause, evidence and status.
- Outstanding framework gaps include QQ console routing, AdCore wrappers/transports, long-video
  composition, Rainbow pending/inject semantics, custom callback setters and fluent configurator
  defaults. Final fresh holdouts, full wrong-capability audit, on-demand Soot fallback and isolated
  three-repeat performance validation remain pending. QUALITY ACCEPTANCE NOT MET.

Current-oracle candidate-inclusive replay (all explicit-only counts are retained in evaluation JSON):

| App | Bridge | Settings | Callbacks |
|---|---:|---:|---:|
| news | 1288/1385 (93.00%) | 298/366 (81.42%) | 205/240 (85.42%) |
| mango | 1137/1153 (98.61%) | 523/526 (99.43%) | 584/591 (98.82%) |
| ctrip | 3666/3666 (100%) | 233/233 (100%) | 299/299 (100%) |

The Lazy fix recovered22 of27 removed news settings; WebNovel3/WebAdvert2 remain misses.
Against the expanded v7 baseline this is +24 Bridge matches, -5 Settings matches and unchanged
callback matches. The settings regression is retained for correction, not hidden by the stronger
object-identity results. Every remaining failed expectation is preserved in the evaluation files.

Reproduce v8 with the documented Gradle build, freeze the resulting jar, then
`python3 scripts/benchmark.py --label v8 --jar test/runs/v8.jar` and the three canonical
`evaluate.py` replays using news/v8a-ownership.jsonl, mango/v8a-ownership.jsonl and
ctrip/v4e-ownership.jsonl. Development variants and before-expansion reports retain their own
oracle hashes; use v7-news-development-v3-evaluation.json for same-oracle comparison.

Frozen v8 serial runs: news70.97s, mango62.39s, ctrip120.86s; peak RSS
8760900/7618728/7768876KiB. All emitted host sets match the independently reviewed v8c sets
(37/85/55); conservative Activity error bounds are0%,5.88%,1.82%. These are development runs,
not the final three-repeat acceptance series. The all-output capability-precision audit is still
incomplete and none of these bounds establishes correct per-WebView capability combinations.

The v8 one-second deadline regression exited in1.233s with55 readable atomic snapshots and
explicit index-not-finished/hard-deadline diagnostics; retained in v8-deadline-check.json.

Final Sol follow-up confirms the remaining WebNovel3/WebAdvert2 regressions are real: nested
layout/loading-wrapper getters assign a concrete constructed WebView into an Activity field.
The repair must follow that actual chain, not restore fabricated field-object seeds. The restored
editor core settings and client share the same captured-host layout WebView; unrelated callback
alternatives are not validated by that finding. See news/v8-settings-regressions.{json,md}.

## v9 — stored listener callbacks, bitwise values and platform wrapper inheritance

- Custom listener registrations require an instance WebView receiver, an exact argument-to-field
  store, and a dispatch through that field. Member enumeration follows parent interfaces/classes;
  concrete and inherited implementations retain full DEX signatures. Dispatch scans exact field
  readers, including inner Client -> outer WebView -> listener chains, and normalizes inherited
  interface method owners. No Activity-name rule was added.
- `dispatch_status` is observed/unresolved. A declared registered override can exist without a
  proven forwarding call; unresolved is not a negative result. Source review caught five omitted
  parent-contract members and two inner-client dispatch gaps in v9a; both drove regression fixes.
- Integer XOR/AND/OR expressions preserve per-instance operands and bounded branch alternatives;
  unknown operands stay unknown. Sol's DEX check confirms Pangle e=true plus XOR yields false.
  Earlier unknown output did NOT independently prove loss of configurator field state. The three
  previously missed Pangle facts now match without fabricating a constant or app-specific value.
- Added pure WebView/Settings/carrier getter dependencies and XML constructor seeding for ordinary
  layout wrappers. v9a/b still miss the five news wrapper settings. The next diagnosed gap is
  absent Android platform superclass definitions; SDK class headers provide those inheritance
  edges without loading method bodies or a whole-program scene. Source confirms nested XML
  inflation rather than application-level new/factory allocation in the two regression cases.
- New synthetic cases cover stored versus ignored listener arguments, two independent WebViews,
  inherited interface contracts, inner forwarding clients, unresolved dispatch, nested wrapper
  constructor settings, SDK header inheritance and non-View namespace negatives, bitwise boolean
  negation, branch alternatives, per-instance operands and explicit resolution-budget diagnostics.
- v9a exploratory serial runs72.84/66.26/125.34s (news/mango/ctrip): Mango callbacks591/591;
  v9b79.58/66.89/125.74s: Mango settings526/526, callbacks591/591, Bridge1137/1153.
  News remains1288/1385 Bridge,298/366 settings,205/240 callbacks in v9a/b; Ctrip's canonical
  Activity-level recall remains100%. These are development measurements, not final repeated
  acceptance runs, and some exploratory builds overlap them.
- Independently reviewed v9a custom listener facts: all seven mgadplus registrations have actual
  same-object field binding; the additional WebContainer registration is real but initially had
  incomplete parent-member and dispatch evidence, retained in the audit. v9b restores all five
  parent members (18 ->23). Sol separately verifies all three Pangle false settings.

- v9c fresh serial runs105.47/71.91/128.82s; SDK android-36 supplied4709 class-header
  inheritance entries. News's five nested-wrapper settings are restored (303/366); Sol verifies
  their actual XML object chain. News emits43 hosts:42 valid1 uncertain (Shell fallback attachment
  not proven); all six new hosts are non-blind development cases, not final held-out samples.
- v9c also exposed a real regression hidden by the old gold scope: three independently valid NFT
  Activities disappeared. Their platform View classification hit an old reverse-dispatch exclusion
  for components. The next change allows exact custom View carrier/interface dispatch while still
  excluding Activity/WebView-wide dispatch expansion. A synthetic interface-dispatched View fixture
  reproduces the missing host. Independent NFT capability facts are being added, retaining these
  failures rather than relying on the unchanged aggregate score. Two other Mango hosts are new
  and require independent ownership verdicts; candidate status does not exempt them.

- The first broad View-carrier dispatch attempt (artifact label v9) was manually aborted after
  251.42s during news; its reverse closure grew from11719 to24799 methods and pulled unrelated
  concrete base UI initialization paths. The report remains valid with status failed. The batch
  did not run its remaining two Apps, and the interrupted time wrapper left no usable RSS record.
  This attempt is neither a completed performance measurement nor a quality pass. Its retained
  record is v9-aborted-experiment.json. The replacement v9d narrows View/Fragment expansion to
  interface/abstract declarations; a focused NFT probe restores its actual interface path.
- NFT source auditing was itself corrected: an initial loadUrl-only inventory omitted unconditional
  constructor -> initialize -> initWebview calls. Challenging that claim against the DEX index led
  to48 settings,3 client registrations and3 callback-member facts being added, with the original3
  operations retained. The public Mango set now has2427 raw rows. No failure was deleted;
  canonical-facts-before-nft-v2.jsonl and schema migration records preserve the previous scope.
- Evaluator revision4 separates client registration API+concrete type from callback members, and
  reports WebView operations/positive-host recall separately. Eight black-box tests pass, including
  wrong callback API/type rejection and operation isolation from three-category scoring. Replayed
  v8 on expanded Mango:1137/1153 Bridge,523/574 settings,584/597 callbacks. v9c:1137/1153,
  526/574,591/597, with all three NFT hosts absent. The former apparent100% settings did not hold
  once this previously omitted framework surface was added.

- Narrowed v9d restores all three NFT operation hosts, but expanded-oracle replay still misses
  all48 constructor settings and6 Client/member facts. The restored Activity names alone are not
  a successful repair. The remaining first binding issue is generated outer ViewBinding capture:
  its NftWebviewLayout field resolves to constructor_parameter instead of the actual XML child.
  Source follow-up is required before a generic binding-object fix. Two additional NFT-family
  hosts are also emitted and require the same independent ownership review.

Final v9 artifact is **v9d**, not the aborted exploratory artifact named v9. Its fresh serial
8-CPU/16-GiB runs took102.92/74.71/130.85s, with peak RSS9002340/8152896/7443792KiB.
All reports remain partial. Independent source ownership review is complete for every emitted
Activity; unresolved owners count against the conservative bound.

| App | Bridge recall | Settings recall | Callback recall | Valid / wrong / unresolved hosts | Conservative host error bound |
|---|---|---|---|---|---|
| News |1288/1385 (93.00%)|303/366 (82.79%)|205/240 (85.42%)|42 /0 /1|2.33%|
| Mango |1137/1153 (98.61%)|526/574 (91.64%)|591/597 (98.99%)|84 /0 /5|5.62%|
| Ctrip |3666/3666 (100%)|233/233 (100%)|299/299 (100%)|54 /1 /0|1.82%|

Mango's three additional operation facts match3/3 and are excluded from capability-category
recall. Sol confirms both additional NFT hosts and documents generated ViewBinding constructor
captures in mango/v9d-nft-binding-break.md. The48 settings and6 callback facts remain missing.
Ctrip's development recall does not establish correct WebView object partitioning: duplicated
new-H5 receiver aliases remain open. Host bounds do not establish capability precision.

Validation: capabilitySelfTest and shadowJar passed; eight evaluator black-box tests passed.
The v9d short-deadline check exited in1.213462s with54 readable atomic snapshots and explicit
index-not-finished/supervisor-hard-deadline diagnostics. Final sealed samples, isolated three-run
performance series, complete capability precision auditing and on-demand Soot fallback remain
unfinished. This iteration does not pass acceptance.

Reproduction: ./gradlew capabilitySelfTest shadowJar --offline --console=plain;
python3 scripts/test_evaluate.py;
python3 scripts/benchmark.py --label v9d --jar test/runs/v9d.jar.
Replay each canonical-facts.jsonl using scripts/evaluate.py and ownership files
news/v9c-ownership.jsonl, mango/v9d-ownership.jsonl and ctrip/v4e-ownership.jsonl.
Retained environment records identify every frozen jar. Earlier v9a/b Mango scores use the old
oracle; compare v8-mango-nft-v2-evaluation.json with v9d-mango-evaluation.json for equal scope.

## v10 development — generated ViewBinding constructor captures

The first change recognizes actual implementations of androidx.viewbinding.ViewBinding as
constructor-capture carriers, including generated bindings holding ordinary custom Views rather
than direct WebView fields. It reuses allocation-local constructor argument materialization;
it does not seed arbitrary field objects or enumerate all View subclasses.
A regression fixture with two separately returned Binding allocations failed before the change
and passes afterward, preserving the exact two supplied custom-View identities. Existing
capabilitySelfTest and shadowJar checks passed. Frozen v10a runs and Sol review are pending;
no recovery or acceptance claim is made from the synthetic case alone.

Final v10 frozen artifact: v10a. Fresh serial runs took104.31/79.58/130.76s (news/Mango/Ctrip),
peak RSS8198432/8480168/7927456KiB, each8 CPUs and16GiB JVM heap. No previous analysis
result was reused. These remain development runs, not final three-repeat acceptance tests.

| App | Bridge | Settings | Callbacks | Emitted hosts | Wrong + unresolved upper bound |
|---|---|---|---|---|---|
| News |1288/1385 (93.00%)|303/366 (82.79%)|205/240 (85.42%)|43|1/43 (2.33%)|
| Mango |1137/1153 (98.61%)|574/574 (100%)|597/597 (100%)|89|5/89 (5.62%)|
| Ctrip |3666/3666 (100%)|233/233 (100%)|299/299 (100%)|55|1/55 (1.82%)|

Sol independently verifies the48 restored NFT settings and6 registration/member facts; the
five NFT hosts now resolve real XML objects. MgNftPreview's two groups are two actual XML
MgNftViewer instances. All89 Mango hosts retain84 valid and5 uncertain verdicts. News/Ctrip
fact comparison (excluding evidence, raw arguments, binding_status and conditional) finds no
changes, including receiver IDs, values and full member signatures; this delta is not a new
source audit. Mango ownership review is v10a-ownership.jsonl.

A material limitation remains: six player hosts gain real reachable framework capabilities,
but raw fact growth includes multiple aliases for the same ServiceWebView/PageWebView.
Sol checked the shared source chains and found no cross-host association in these cases;
raw counts must not be interpreted as separate capabilities or WebViews. Antique's settings
union is still labeled WebSettings while callbacks show its underlying WebView alternatives.
This needs receiver normalization and alias repair, not deletion of failures. Evidence and
repeatable audit: mango/v10a-validation.{json,md}, mango/audit_v10a.py.

News still misses systemically: AdCore wrappers, long-video controller/provider chains and QQ
console-message bridges. Mango still misses Rainbow reflection endpoints. Final held-out tests,
output capability precision and repeated performance acceptance remain pending. All reports
are partial; v10 does not pass overall acceptance.

Reproduction: ./gradlew capabilitySelfTest shadowJar --offline --console=plain;
python3 scripts/benchmark.py --label v10a --jar test/runs/v10a.jar;
python3 scripts/evaluate.py with each canonical-facts.jsonl and news/v9c-ownership.jsonl,
mango/v10a-ownership.jsonl or ctrip/v4e-ownership.jsonl. The evaluator's8 tests also passed.

## v11 development — settings receiver alternatives and keyed object registries

A union of Settings objects now unwraps each branch to the underlying WebView before grouping.
The reproducing synthetic fixture failed before this change and passed afterward, preserving both
separate view IDs and the configured value. Frozen settings-only v11a ran three Apps in
104.91/78.51/130.99s; canonical recall is unchanged from v10. These are development timings:
builds and a bounded news debug probe on CPUs8-15 overlapped parts of the run.

Added object-local Map.put/get propagation with exact Class/literal keys, factory-returned Map
contents, separate instance identities and explicit diagnostics for dynamic keys, mutations,
ambiguous replacement order and entry budgets. Class-keyed registry wrappers are recognized by
actual parameter stores/reads through the same Map field, not names. They are not global
capability seeds. Actual registration calls propagate through inherited constructors and
interface dispatch. Two independent registries using the same key retain distinct plugin objects;
Class keys do not match same-name Strings. Unknown Map receiver parameters do not establish
an invented shared Map instance (guard added after frozen v11b).

Source follow-up corrected the earlier presumed long-video break: the existing analyzer already
materializes h0's b1 and n2 returns. Sol identifies the subsequent service registry flow and its
same-object/Class-key requirement (news/v11-longvideo-service-dispatch.{json,md}). Frozen v11b
still does not recover either long-video host; this remains a real failure, not a completed repair.
A bounded heap probe is checking the remaining registry-receiver/callback argument provenance.

Frozen v11b completed118.19/85.29/150.37s,43/89/55 hosts, all reports partial. Sol confirms
Mango's NFT settings/client now share the same underlying WebView alternatives. Two live-player
Settings unions resolve their actual views; nine Pangle hosts lose only redundant unknown bridge
implementation rows, retaining the concrete registrations/members. News adds174 raw VerticalVideo
rows but zero semantic capabilities: this is alias expansion and remains an open precision issue.
No new or removed hosts were observed. The long-video first unresolved relation is now documented
as inherited onCreate -> createPage/setPage/getPage -> subclass onViewCreated(page,view), before
Class-keyed registration. See news/v11b-validation.md. Subsequent implementation must preserve
that actual virtual-call argument, not expand every implementation of the page interface.

Final v11 artifact is **v11c**, which additionally refuses to merge opaque Map receivers into a
shared object. All three Apps have exactly the same compared fact sets and host sets as v11b
(receiver IDs, member signatures, values included; raw arguments/evidence/status excluded).
Each per-App v11c-report-delta.json records its precise comparison scope and both report hashes.
Sol's v11b ownership and object audits therefore remain applicable within that documented scope.

| App | Total seconds | Peak RSS KiB | Bridge recall | Settings recall | Callback recall | Hosts / conservative owner error |
|---|---:|---:|---|---|---|---|
| News |120.42|8351364|1288/1385 (93.00%)|303/366 (82.79%)|205/240 (85.42%)|43 /1 uncertain (2.33%)|
| Mango |83.99|8475528|1137/1153 (98.61%)|574/574 (100%)|597/597 (100%)|89 /5 uncertain (5.62%)|
| Ctrip |142.21|7188456|3666/3666 (100%)|233/233 (100%)|299/299 (100%)|55 /1 wrong (1.82%)|

All are fresh JVM runs with8 CPUs and16GiB heap; no prior analysis results were reused. A bounded
news lifecycle probe on CPUs8-15 overlapped development measurements. These are not final isolated
three-repeat performance acceptance runs. All reports remain partial.

Validation: capabilitySelfTest and shadowJar passed, including two-registry instance isolation,
Class/String key separation, unknown-key alternatives, opaque-receiver isolation, ambiguous-update
diagnostics and Settings receiver alternatives. The final jar matches the built jar. The1s
supervisor regression exited in1.274s with57 readable atomic snapshots and explicit timeout
diagnostics (v11c-deadline-check.json).

A separate known-failing LifecycleFixture is deliberately retained under validation/regressions.
It reproduces the missing concrete page argument across inherited lifecycle -> setter/getter ->
subclass callback. This is not counted as a passing test. The next repair must pass it and recover
the real source-verified long-video chain. News's quality gate, raw-object alias precision, Rainbow
and QQ/AdCore gaps, final sealed samples, final repeated timings and on-demand Soot fallback remain
unfinished; v11 does not pass acceptance.

Reproduction: ./gradlew capabilitySelfTest shadowJar --offline --console=plain;
python3 scripts/benchmark.py --label v11c --jar test/runs/v11c.jar;
canonical evaluate.py replays using news/v11b-ownership.jsonl, mango/v11b-ownership.jsonl,
ctrip/v4e-ownership.jsonl. report_delta.py compares selected report fields without claiming source
correctness. Known-failing fixture reproduction is in regressions/README.md.

## v12 development — preserve actual object-field setter arguments

Small instance methods storing their reference argument directly into a field of the same
receiver now participate at actual reached call sites. Recognition uses the DEX store and
parameter relation, not setter names or Activity names. It does not globally seed setters.
A cheap IPUT_OBJECT prefilter precedes bounded summary inspection; the result is cached per
method. This repairs the generic inherited lifecycle factory -> setter/getter -> override
argument fixture retained in v11, now promoted to capabilitySelfTest. All existing tests and
shadowJar pass. Frozen v12a three-App runs finished at 122.34 / 85.07 / 147.69 seconds
(news / mango / ctrip). No actual long-video capability recovery is claimed yet.

Independent Mango verification found a real MGVideo Diana regression hidden by the old oracle.
Sol expanded the nonblind development set by 81 source/DEX facts (2,427 -> 2,508 raw rows),
retaining the complete earlier snapshot. Replaying both versions on this same expanded set:
v11c Bridge 1147/1163, Settings 608/608, callbacks 634/634; v12a Bridge 1137/1163,
Settings 603/608, callbacks 598/634 (94.32%, below the callback gate). The v12a old-oracle
evaluation is retained, explicitly superseded for MGVideo coverage by the mgvideo-v1 replay.
Unknown names and the two unscorable Bridge rows remain visible.

Development probes v12c through v12f are not full-App performance measurements. Receiver-specific
base-helper dispatch reaches News player.g construction but still misses the downstream lifecycle
collection and event chain. AspectJ captured-state execution is being repaired for Mango. The
v12e probe reached the first closure but exhausted the host context budget before the inner
closure/F1; this is an unresolved failure, not successful recovery. Sol confirmed the runtime
contract independently; indexed arrays and linked-closure/no-proceed isolation fixtures pass.
The iteration remains in development pending a final frozen three-App run and source audit.


v12h full measurements (fresh serial JVMs, 8 CPUs / 16 GiB): News 119.27s / 8,017,612 KiB /
44 hosts; Mango 227.20s / 9,555,620 KiB / 91 hosts; Ctrip 167.37s / 8,105,572 KiB / 55 hosts.
All reports remain partial. News adds one source-confirmed inherited pendant host; Mango adds
two source-confirmed conditional WebView hosts. Expanded ownership and removed-capability audits
are stored per App. A disappearing fact is not automatically a true regression: source inspection
must distinguish earlier over-approximation from actual loss. News's three suspected missing
families have a separately retained 288-row development record. A subsequent reverse host audit
found that actual listeners pass selector 3/4, not Capture 107 or Feedback 1021; Shell additionally
lacks a proven lifecycle prefix. All 288 rows are therefore pending_host_binding with
positive_acceptance=false, not positive oracle cases and not merged into canonical. Shared
implementation existence and an earlier report path did not prove host association. No row was
deleted. These are not sealed holdouts.

Sol found a further scorer defect: the added MGVideo Diana facts could match unrelated WebViews
within the same Activity. The apparent 29 settings + 1 callback matches in v12a/v12h were not
Diana recovery. Scorer v5 enforces explicit source-authored `webview_constraint.types`, records
constraint coverage and keeps unconstrained matches explicitly Activity-only. Exact allocation
identity still requires independent review. Eleven black-box scorer tests pass, including the
same-Activity/wrong-WebView negative for all three capability families, union alternatives,
malformed constraints, and unresolved targets in both aggregate and granular scores. Historical
v4 files are retained as superseded measurements, not acceptance evidence. Source agents are
preparing independent constraint mappings; no mappings may be inferred from tool outputs.

v12i freezes round-robin method work scheduling, retaining every pending context but preventing
one registration helper's thousands of argument combinations from starving new method chains.
It also recognizes obfuscated proceed(args) only through the actual runtime run-dispatch relation,
or the standard proceed member, and diagnoses unsupported state rewriting. Array overwrite/order
ambiguity is explicit. v12j additionally removes call/component evidence-path length as a
reachability cutoff. Real work remains bounded by contexts, components, queue, symbolic-summary
budgets and the external deadline; evidence presentation is capped at 64 steps with a visible
omission marker and diagnostic. A 72-helper chain plus actual Fragment initialization fixture
passes. Both frozen candidates are measured serially; their final outcomes and audits remain
pending, and v12 has not passed acceptance.


The v12i full runs finished at 123.01 / 236.83 / 180.82 seconds with 44 / 91 / 55 hosts.
Independent MGVideo review confirms all 81 constrained Diana facts now match on their intended
PageWebView/ServiceWebView types, with 124 of 130 concrete Diana report rows carrying the
H1/G1/F1 chain. This repairs the actual closure regression, unlike v12h's cross-WebView matches.
The host still has budget/alias limitations. Ctrip's three core categories are unchanged; two
Hotel Map loadUrl operations remain a separate replay regression. News remains below quality gates.

All three public canonical sets now carry source-authored WebView type constraints (1,991 /
2,508 / 4,330 raw rows). An initial Mango mapping incorrectly used the SSWebView wrapper type;
source reinspection proved its FrameLayout wrapper allocates MoWebView and delegates the actual
APIs. The 159 affected constraints were corrected with before/after snapshots and unchanged
capability facts; bound-v2 replay files retain this failed mapping experiment. News's corresponding
X5Wrapper suspicion was disproved: it really inherits DtX5 -> smtt.WebView, so its strict type
constraint was retained. Remaining type imprecision must be repaired in the extractor, not hidden
by widening the oracle. Source-selected exact instance associations still require review beyond
these type checks. Scorer tests now total 12, including retained unconfirmed-source cases which
cannot inflate positive acceptance denominators.

v12j News finishes at 191.36s (44 hosts), Mango at 355.49s (92 hosts). Mango misses the preferred
300s performance target; being below the 600s hard limit does not pass that target or quality.
News's source audit finds no reintroduction of the disallowed Capture/Feedback selector chains.
Additional rows include real inherited operations, aliases, and unknown receiver alternatives;
raw row growth is not reported as full capability recovery. The final audits are now complete; final results are below.


### v12 final candidate v12j (acceptance NOT passed)

The final source-only selector correction changes 16 Mango constraint rows: CCB's declared
platform field actually contains MoWebView, and seven popup callbacks belong to separately
created MoWebViews. Capability expectations are unchanged; all failed mappings remain versioned.
Final scoring v5 uses `v12j-*-bound-v4-evaluation.json`; `bound-v4` identifies this replay round,
not scorer version. The corresponding v11c replay uses the identical final oracle.

| App | Seconds | Peak RSS KiB | Bridge | Settings | Callbacks | Emitted hosts |
|---|---:|---:|---:|---:|---:|---:|
| News | 191.36 | 8064384 | 1288/1385 (93.00%) | 271/366 (74.04%) | 182/240 (75.83%) | 44 |
| Mango | 355.49 | 9182524 | 1147/1163 (98.62%) | 608/608 | 634/634 | 92 |
| Ctrip | 202.02 | 7964432 | 3666/3666 | 233/233 | 299/299 | 55 |

These are candidate-inclusive development-set scores, not blind or whole-APK completeness.
Exact instance identity and false capability association remain unproven; all reports are partial.
News fails all three 95% gates; Mango fails the 300-second target. Source ownership audit covers
all output hosts: News 43 valid/1 uncertain, Mango 86 valid/1 conditional/5 uncertain, Ctrip 54 valid/1 wrong.
Conservative wrong+uncertain bounds are 2.27%, 6.52%, and 1.82% (conditional hosts count conservatively as unresolved). This does not substitute for
capability-level precision. Ctrip additionally retains 108 unassigned oracle facts.
No final sealed samples or final three-repeat performance claims are made.

Sol verified Mango's actual Diana closure recovery (81 facts), the additional VideoSquare host,
and Ctrip's two QQ delayed loadUrl calls. The Hotel Map operation loss, Mango XWeb loss, News XML
receiver identity, QQ/AdCore/long-video omissions, aliases and budgets remain in OPEN_GAPS.json.
The 288 disputed News development facts remain pending host binding and excluded from positives.

Validation: capabilitySelfTest/shadowJar pass; 12 evaluator black-box tests pass. The external
one-second deadline test produced a valid atomic timeout report after 1.273 seconds, with
57 observed snapshots. This checks report survival, not analysis quality. All three real runs use
8 logical CPUs, 16 GiB heap and fresh serial JVMs; these are development runs, not final isolated repeats.

Reproduce: `./gradlew capabilitySelfTest shadowJar --offline --console=plain`,
`python3 scripts/test_evaluate.py`, then freeze the built jar and run
`python3 scripts/benchmark.py --label v12j --jar test/runs/v12j.jar` in a fresh output location.
Replay each report with `scripts/evaluate.py --report <capabilities.json> --oracle
 docs/validation/<app>/canonical-facts.jsonl --ownership docs/validation/<app>/v12j-ownership.jsonl
 --out <evaluation.json>`. Benchmark/environment JSON, source audits and individual missing facts
are retained alongside this log.
