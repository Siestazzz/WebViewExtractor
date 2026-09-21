# Two-pass resumable Activity analysis: scheduler v3

## Behavior

Activity analysis now has an initial pass followed by deeper round-robin work. The first pass starts every declared Activity in the existing relevance-first order, allowing up to eight method contexts or a cooperative 50 ms slice. Its time share shrinks if the remaining first-pass target budget is small. The second pass resumes unfinished states, up to 100 contexts or 50 ms per slice. Target/hard CLI limits remain 300/600 seconds by default.

Each state retains its queue, actual arguments, per-Activity object/field/collection state, discovered capabilities, visited contexts, deferred/XML bindings and internal refinement phase. Pauses occur between atomic method tasks. No Java stack snapshot or process-restart recovery is provided. A complex task can exceed 50 ms, and total coverage is still bounded by the APK deadline and internal analysis limits.

A completed Activity releases its live heap. All unfinished Activity heaps remain in memory; retained-host/heap/queue metrics and peak process RSS expose this cost, but there is no disk spill or bounded active-window implementation in this version.

## Fact retention and refinement

The existing internal two-phase traversal is retained; it is separate from the scheduling passes. Phase-one facts are kept as a fallback during phase two. Facts not re-derived remain `candidate` with `analysis_stage: previous_phase_provisional`, including at normal completion. They are unresolved static candidates, not automatically validated capabilities. If later analysis contradicts or cannot justify them, independent review is still required. This prevents silent deletion but can increase uncertain/duplicate receiver aliases.

Replacing an earlier placeholder requires the same capability kind, call site/API, WebView identity and registration name; values and arguments must agree or follow a supported refinement. Native WebView receiver arguments may narrow from a base type to a subtype while retaining the same identity. Registry receiver argument zero is compared normally; different registries cannot be merged. Implementation concretization requires a real matching concrete target argument or an explicit exact map-receiver/key lookup provenance edge, without wildcard entries. An unchanged opaque unknown is insufficient. Full exposed-member signatures must not shrink. Earlier placeholders replaced through this process are retained separately as `superseded_provisional_facts` in the detailed host report.

The v1 finalization defect and v2 over-broad refinement cases remain documented in SCHEDULER_V1.md / SCHEDULER_V2.md with frozen reproductions. They are not hidden by rewriting earlier results.

## Coverage and statistics

`activity_coverage` includes each declared Activity, even when no capability was emitted. Status distinguishes not_started, initial_analysis, pending_deep_analysis, deadline_interrupted, traversal_finished and budget_exhausted. Rows include per-phase/total contexts, direct queue length, discarded contexts at local caps, tracked XML consumers/deferred fields, provisional facts and analysis/checkpoint seconds. Tracked deferred items are not necessarily executable pending tasks, and an empty direct queue does not prove every binding resolved.

Aggregate metadata distinguishes started, initial-pass visited, terminated (`processed_activities`), traversal finished, budget-exhausted and still-pending Activities. `finished_with_limits` indicates an empty scheduling queue with host caps/provisional facts. Other diagnostics can still make the report `partial`; traversal completion is not a statement of capability completeness or runtime execution.

Checkpoint I/O time is measured separately from the active host's analysis time. The global watchdog budget includes indexing, analysis and report writing. Detailed and formatted compact reports continue to be atomically replaced individually, not as a transactional pair.

## Tests and reproduction

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
cp build/libs/webview_extractor-1.0-SNAPSHOT-all.jar test/runs/scheduler-v3.jar
python3 scripts/run_parallel.py \
  --samples docs/validation/compact-six-samples.json \
  --jar test/runs/scheduler-v3.jar \
  --out test/runs/scheduler-v3
python3 scripts/check_compact.py test/runs/scheduler-v3
```

All existing fixtures plus ActivityResumeFixture, SchedulerIsolationFixture and PhaseRetentionFixture pass. These test uninterrupted versus sliced equivalence, two interleaved hosts, empty-host coverage, phase snapshots/final retention, deadline preservation, no duplicate finalization, member-set preservation, exact map-key/object provenance and the opaque-unknown/different-registry negatives. The external watchdog test is scheduler-v3-deadline.json. Sol independently checks frozen artifacts and records its findings separately.

These are six concurrent fresh runs (eight distinct logical CPUs and 16 GiB heap each), compared to the preceding concurrent compact-six baseline. Source-oracle replays use the unchanged cumulative development facts for News/Mango/Ctrip. FreeReels is base-APK only. New-App accuracy and final sealed holdouts are not claimed verified. Small test builds/scoring may overlap these development measurements; they are not isolated final acceptance repetitions.

## Final six-App development results

| Package | Seconds | Initial pass / all | Traversal finished | Local cap termination | Pending at deadline | Emitted hosts |
|---|---:|---:|---:|---:|---:|---:|
| com.cctv.yangshipin.app.androidp | 48.4 | 229/229 | 229 | 0 | 0 | 24 |
| com.freereels.app | 593.6 | 164/164 | 59 | 0 | 105 | 128 |
| com.hunantv.imgo.activity | 432.8 | 657/657 | 639 | 18 | 0 | 113 |
| com.ss.android.article.video | 591.5 | 628/628 | 284 | 0 | 344 | 36 |
| com.tencent.news | 179.4 | 2329/2329 | 2327 | 2 | 0 | 41 |
| ctrip.android.view | 425.3 | 410/410 | 407 | 3 | 0 | 55 |

All six first passes finished in 19.3–57.8 seconds, including indexing. Xigua and FreeReels still exhaust the deep-analysis deadline: 344 and 105 hosts remain interrupted. Compared with the old batch, these Apps previously terminated only 18/628 and 33/164 host analyses; they now give every declared host an initial slice, with 284/628 and 59/164 traversals finished. This is wider initial coverage, not complete analysis. News/Mango/Ctrip also have 2/18/3 hosts terminated at internal context limits. All final report statuses remain partial.

Candidate-inclusive cumulative development oracle replay: News remains 1284/1393 Bridge, 272/366 Settings, 197/240 callbacks; Ctrip remains 3682/3970 Bridge, 375/391 Settings, 421/444 callbacks. Mango improves from 1197 to 1207/1259 Bridge, 574 to 608/608 Settings, and 609 to 646/662 callbacks. A full semantic-oracle-identity comparison (including values and implementation types, using the unchanged matcher) finds zero lost previously matched expectations in all three and 81 gains in Mango. This does not establish capability precision or independently validate the new Apps. Added provisional hosts/aliases must not be counted as independently confirmed ownership.

All six compact/full structural checks and coverage-accounting checks pass; compact JSON is pretty-printed. Independent Sol boundary negatives and the real-DEX constructor retention probe pass; see scheduler-v3-review.md. Final artifacts are in test/runs/scheduler-v3, with compact-reports.zip for the six compact reports. Machine-readable results/environment/effects and fact-change records accompany this document. Final quality gates and isolated repeat measurements remain outside this step and are not claimed passed.

### Explicit output regression / unresolved ownership

FreeReels no longer emits `com.applovin.mediation.MaxDebuggerMultiAdActivity` in this fixed-time run. Its coverage row is `deadline_interrupted`, phase 1, 5753 processed contexts and 1389 queued contexts. The prior output is not independent truth, and this disappearance has not been source-verified; retain it as an unresolved budget/distribution regression to investigate, not as proof that the Activity has no WebView. The new scheduler spreads time across all hosts, so broader initial coverage can leave a previously early-completed host unfinished.

New output hosts (especially first-phase provisional candidates) are enumerated in scheduler-v3-effects.json. No all-output ownership/error-rate audit was completed for this scheduling step; the prior version’s ownership percentages must not be reused for these outputs.
