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
