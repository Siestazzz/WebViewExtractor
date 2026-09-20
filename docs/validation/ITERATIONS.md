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
