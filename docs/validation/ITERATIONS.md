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
