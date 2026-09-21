# Resumable Activity scheduling, v1

This iteration changes scheduling and checkpoint presentation, not the capability matching rules. It builds on the exact extractor prototype used by `compact-six.jar` (the previously pending asynchronous registration/handler and expression-budget changes), and retains the new compact export with pretty printing.

## State and scheduling

`CapabilityEngine.beginActivity()` creates one ActivityState containing a Host. Host retains actual pending method/argument contexts, visited/pending sets, per-host field heap, collections, XML consumers/replay state, callback/bridge object associations and capability facts. `advanceActivity()` consumes a bounded batch and retains that same object for later calls. Completed state releases the live heap and keeps its report. State is in-memory; cross-process recovery is not implemented.

The first pass visits all Manifest roots in existing relevance-first order. Each gets at most 8 method tasks or a cooperative 50 ms slice. Time is further divided by the number of remaining roots if the 300-second target is approaching. The second pass visits unfinished states in a round-robin queue, up to 100 tasks or 50 ms per visit. No host receives a second first-pass slice before later roots get their first opportunity. Both passes share the existing per-APK index and summaries. Analysis work is not restarted on every slice.

The pause boundary is a method task, not a Java stack or individual DEX instruction. Initialization, a single method, and XML replay may overrun a soft slice. Existing recursive-expression, flow/context and global limits still apply. Thus this version does not guarantee a strict 50 ms upper bound or that every APK finishes its first pass before 300 seconds. First-pass completion does not establish capability completeness.

The engine's existing two internal refinement phases are distinct from the two scheduling passes: the first scheduling pass may finish both internal phases for a simple host; a complex host may still be in its first internal phase during deep scheduling.

## Checkpoints and coverage

Transitioning into internal refinement preserves previous-phase facts as a fallback. Until traversal completes, facts not yet re-derived remain candidate entries marked `analysis_stage: previous_phase_provisional`. Successful refinement replaces that provisional snapshot with refined facts; it does not permanently union obsolete aliases. Budget-limited refinement retains the fallback, explicitly provisional, rather than silently dropping already observed capabilities. Thus provisional facts may disappear after successful refinement, intentionally.

Detailed output includes `activity_coverage` for every declared root, including hosts with no emitted capability: not_started, initial_analysis, pending_deep_analysis, traversal_finished, or budget_exhausted. Metrics distinguish started, first-pass visited, processed/terminated, finished traversal, budget-limited and pending hosts. `processed_activities` counts terminated host analyses (including explicit local-budget termination); `traversal_finished_activities` excludes that termination. Neither proves all runtime capabilities were found. Total per-host timing includes checkpoint time spent during that host's slice.

Compact metadata includes those aggregate coverage counts and the scheduling stage, while retaining Activity → WebView → signature/setting-parameter lists. Candidate and provisional entries contribute to display counts, not independently verified capability counts.

## Validation and reproduction

- Existing capability regression suite and compact tests pass.
- `ActivityResumeFixture`: one-context slices reproduce uninterrupted field/helper/two-WebView results; refinement checkpoints retain earlier facts; expired global budget preserves queue/facts; finished heaps are released.
- `SchedulerIsolationFixture`: two different hosts interleaved one context at a time match sequential results; an empty host gets a finished coverage entry without spurious capabilities; reports have no duplicate hosts.
- External watchdog test leaves a valid atomic report; see scheduler-v1-deadline.json.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
cp build/libs/webview_extractor-1.0-SNAPSHOT-all.jar test/runs/scheduler-v1.jar
python3 scripts/run_parallel.py \
  --samples docs/validation/compact-six-samples.json \
  --jar test/runs/scheduler-v1.jar \
  --out test/runs/scheduler-v1
python3 scripts/check_compact.py test/runs/scheduler-v1
```

Six APKs run concurrently with separate eight-CPU affinities and 16 GiB heaps. Compare to the previous concurrent `compact-six` batch, not historical isolated runs. Sol performs a separate implementation review. The three existing source fact sets are replayed unchanged against both batches; new App results are not claimed source-complete. Sealed holdouts are untouched. Development test builds and report scoring may overlap APK execution; these are not isolated final acceptance measurements.

## Results and independent rejection

| App | Seconds | Initial pass | Traversal finished | Local budget terminated | Still pending | Output hosts |
|---|---:|---:|---:|---:|---:|---:|
| com.cctv.yangshipin.app.androidp | 47.0 | 229/229 | 229 | 0 | 0 | 24 |
| com.freereels.app | 593.2 | 164/164 | 59 | 0 | 105 | 126 |
| com.hunantv.imgo.activity | 430.2 | 657/657 | 639 | 18 | 0 | 85 |
| com.ss.android.article.video | 591.5 | 628/628 | 284 | 0 | 344 | 36 |
| com.tencent.news | 178.0 | 2329/2329 | 2327 | 2 | 0 | 40 |
| ctrip.android.view | 415.8 | 410/410 | 407 | 3 | 0 | 55 |

Sol independent review found that v1 normal completion still drops phase-one-only facts, while budget-limited completion retains them. Reproducer and analysis: scheduler-v1-review.md / regressions/SchedulerPhaseFactLossProbe.java. NewsDetail +47 and PushDetail +46 are all provisional first-phase facts, not proven new independent capabilities. Context caps also discard work without a count. These are known defects, not accepted behavior; v2 addresses retention/refinement and coverage diagnostics.

The original cumulative fact sets were replayed on all three previously verified Apps; machine-readable comparisons are scheduler-baseline-*.json and scheduler-v1-*.json. No final accuracy acceptance is claimed.
