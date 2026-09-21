# Scheduler v1 independent review

Reviewed source: current `CapabilityEngine.java` and `Main.java`. Frozen artifact: `test/runs/scheduler-v1.jar`, SHA-256 `dcbafdde42259aade8e2f029e70b144eed64cfb7f99f94eb1d1c076350ce1ea9`. The JAR bytecode contains the same critical `finishActivity` ordering described below. No sealed samples, new-App decompilation, oracle edits, or production changes were used.

## Findings

### High: successful completion deletes first-phase-only facts

At the phase boundary, `advanceActivity` copies `h.facts` to `state.previousFacts`, clears current facts, and starts phase 2. `stateReport` correctly combines both sets while the Activity is pending. But `finishActivity` executes:

```java
if (!state.limited) state.previousFacts.clear();
state.completedReport = stateReport(state);
```

A normal, non-limited completion therefore deletes phase-1 facts before creating the final snapshot. Any capability not regenerated in phase 2 vanishes. This is not only theoretical nondeterminism: the phase reset clears `visited`, `materialized`, facts, and queues but retains `constructed`, heap, maps, arrays, deferred fields, fragment/layout state, and several alias sets. A second seed can skip already-constructed work while the corresponding first-phase fact has been cleared.

The frozen JAR reproduces the loss:

```text
before_finish=1
after_finish=0
```

Reproducer:

```bash
cd /home/d3008/phy/workspace/WebViewGPT/webview-extractor
mkdir -p /tmp/scheduler-probe
javac -cp test/runs/scheduler-v1.jar -d /tmp/scheduler-probe \
  docs/validation/regressions/SchedulerPhaseFactLossProbe.java
java -cp /tmp/scheduler-probe:test/runs/scheduler-v1.jar \
  org.example.SchedulerPhaseFactLossProbe
```

Build the final combined report before clearing `previousFacts`, with phase-2 keys overriding matching provisional keys. Clear retained maps only after the immutable completed snapshot exists.

The six-App News comparison exposes the inverse side of the same bug. `NewsDetailActivity` and `PushDetailActivity` each hit the context cap (`contexts_processed=24002`, `status=budget_exhausted`). Because `limited=true`, `finishActivity` does **not** clear `previousFacts`; scheduler-v1 consequently adds 47 and 46 facts versus compact-six. Every added row is marked `analysis_stage=previous_phase_provisional`. Thus a budget-exhausted Activity retains phase-1 facts while an otherwise identical normally completed Activity deletes them. Output semantics depend on whether the cap is hit.

These 93 rows are not evidence of a shared-cache leak. Their receiver identities show a mixed provisional set: real News detail implementations, duplicate union/field aliases, collection-return identities (`LruCache.remove`, `LinkedList.removeFirst`), PatchRedirector alternatives, and `WebViewForCell` paths that are not independently owned by the detail Activity. Canonical recall stays unchanged because the already-matched canonical surface does not need these extra aliases. They should remain provisional diagnostics unless independently bound, not be promoted merely because the host exhausted its budget.

### High: phase 2 is neither a clean rerun nor a fully monotone continuation

The transition selectively clears queue, pending, facts, visited, components, materialized, XML consumers and replay markers. It retains heap, deferred fields, fragment views, layout scopes/children/bindings, contents, maps, arrays, native/bridge bindings, linked closures, `constructed`, `expanding`, service evidence and gaps.

This hybrid state makes phase behavior order-dependent. Retained `constructed` can suppress initialization that produced cleared facts. Retained maps/heap can make phase 2 observe aliases unavailable at a clean start. Cleared XML consumers can discard deferred replay intent while retaining the objects it depended on.

Define the phase contract explicitly. A refinement continuation should preserve all work/facts monotonically and only add or supersede evidence. An independent rerun should create a fresh `Host` and merge its finished result. The current selective reset supports neither contract.

### Medium: the two-round scheduler retains every unfinished Activity heap

The initial loop calls `beginActivity` for every reachable manifest Activity and stores every unfinished `ActivityState` in `pending`. Each state retains a full `Host`: heap, queue, values, layout scopes, maps, arrays, XML bindings and facts. Deep analysis begins only after all initial slices. On large APKs this makes peak memory proportional to the sum of all partially explored Activities, not the active slice.

Completed states release `host`, which is good, but that does not help when most 8-context initial slices leave work pending. Use a bounded active window, or serialize a compact continuation and release inactive hosts. At minimum record aggregate retained queue/heap/object counts and peak active-host count.

### Medium: checkpoint I/O is charged to whichever Activity happens to run

`checkpoint.run()` executes inside `advanceActivity` before `processJob`, and `state.nanos` is updated in the surrounding `finally`. A full report write serializes every Activity state, yet all of that time is charged to the current Activity's `analysis_seconds`. It also consumes the Activity slice and global deadline without incrementing `contexts_processed`.

Track checkpoint/report time separately and subtract it from the current Activity's analysis time. Check the slice deadline again after checkpoint writing and before starting the next atomic job.

### Medium: progress fields hide pending work and deadline interruption

`pending_contexts` reports only `host.queue.size()`. It omits replayable XML consumers, deferred fields, unresolved layout replay state, and other work that can enqueue contexts. It can be zero immediately before `replayXmlConsumers` creates additional work.

At the global deadline, unfinished states remain `pending_deep_analysis` or `initial_analysis`; per-Activity coverage does not say `deadline_interrupted`. `phase` reports `phase + 1`, but there is no per-phase context/fact count, so it cannot explain how much work was discarded or superseded. `contexts_processed` combines both phases.

Expose direct queue, XML consumer and other deferred-work counts, current phase, per-phase processed counts, and an explicit interruption reason. Keep the global scheduling stage, but do not require consumers to infer per-host deadline state from it.

### Medium: context-cap transition drops queued tasks without quantifying them

When `visited.size() > 12000`, `advanceActivity` takes the phase transition even if the queue is non-empty, then clears `queue` and `pending`. On the second cap it finishes the host. `budget_exhausted` is reported, but the number and kind of dropped contexts are lost, and `pending_contexts` becomes zero. Record dropped queue/deferred counts and preserve them in the host snapshot. Do not present zero pending as completed traversal.

The frozen News run demonstrates this presentation problem: both capped detail Activities report `pending_contexts=0` and the global `scheduling_stage=finished`, even though their coverage status is `budget_exhausted` and work was discarded at the cap. `finished` currently means the round-robin deque is empty, not that traversal completed.

### Low/latent: shared constant cache can capture host-dependent evaluation

`constants` is engine-global. Static-field recovery evaluates a `<clinit>` write using the current `job` and `host`, then caches a literal by type/field. A malformed or parameter-dependent summary can therefore cache a value resolved under the first Activity and reuse it later. Static initialization should be evaluated in a host-independent context; cache only values proven independent of job arguments and host heap.

Object heaps themselves are correctly separated per `Host`, and execution is single-threaded, so `currentHost` is not a concurrent race. The shared cache is the remaining isolation risk.

## Main-loop behavior

Normal slice pause/resume is in-memory and preserves the queue: `processJob` is atomic, and `advanceActivity` returns without clearing state when `maxJobs` or slice time is reached. The initial queue includes every begun unfinished state once, and round-robin removal/reinsertion does not lose an Activity. Not-started Activities are reported when the global deadline stops the initial loop.

This is not process-resumable checkpointing. `capabilities.json` stores facts and coverage only; the worker cannot restore queues, heap, visited contexts, phase state, or pending callbacks after a crash or supervisor timeout. If “可暂停恢复” includes restart recovery, the implementation does not provide it and should not label report checkpoints as resumable state.

The final status is overly coupled to diagnostics: even when every Activity finishes, any engine/index diagnostic forces `partial`. Distinguish traversal completion from analysis limitations instead of collapsing both into one status.

## Test gaps

`ActivityResumeFixture` checks one Activity and compares resumed output with an uninterrupted run using the same implementation. Both paths share the `finishActivity` deletion bug, so equality cannot detect it. Its checkpoint assertion occurs before completion, exactly when `stateReport` still includes `previousFacts`.

Add tests for:

1. A phase-1-only fact that must remain in the final completed report.
2. A constructor/field fact whose producer is suppressed by retained `constructed` in phase 2.
3. Two interleaved Activities with identical method shapes but distinct heaps, maps, WebViews and static values.
4. A context cap with a non-empty queue, asserting dropped-work counts and `budget_exhausted` coverage.
5. Deadline interruption during initial pass, deep pass, phase transition and checkpoint write.
6. XML/deferred work when the direct queue is empty.
7. Thousands of pending hosts with a peak-memory assertion or bounded-active-window invariant.
8. Report idempotence before/after completion and no mutation of previously emitted snapshots.
9. If restart recovery is intended, serialization and restoration of a continuation followed by equality against an uninterrupted run.

The existing fixture does verify useful properties: one-context slicing converges, the same Host object survives ordinary pauses, completed heaps are released, repeated advance on a done state does not duplicate the legacy `activities` list, and an already-expired engine does not clear a pending queue.
