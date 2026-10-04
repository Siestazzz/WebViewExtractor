# Generic v7 deferred-binding memoization

The frozen v6 Ctrip worker spent at least roughly 150 seconds inside one refreshBinding invocation without returning a checkpoint. Main-thread samples at 310, 330 and 362 seconds showed the same union/deferred-field recursion, including repeated HashSet copies and union construction. Evidence is preserved in `generic-v6-ctrip-refresh-profile.json`, `generic-v6-runtime-stack-survey.json` and the corresponding stack files. This is analyzer work on a shared heap graph, not a new application signature rule.

## Exact local cache

refreshBinding previously revisited shared union and deferred-field descendants for each incoming path. The existing depth limit of twelve bounds path length but not the number of paths through a wide diamond.

Each top-level refresh now owns a local memo. Its key contains the actual abstract value object's identity, the current depth and the relevant visited-field context. Values are keyed by identity instead of recursively hashing an entire value graph. The visited context retains only IDs that the current value might reach within the remaining original depth. IDs outside that dependency set cannot affect this refresh's cycle guards, so they do not prevent reuse of the same acyclic descendant reached through different ancestors.

Dependency analysis is iterative and depth-aware. A concrete receiver uses its exact heap key. When a receiver is itself deferred and might change identity, all heap values for that same field form a conservative dependency superset. This superset is used only for cache correctness; evaluation still reads actual resolved receiver heap keys and never joins unrelated receiver objects. Dependency results are cached within the same refresh invocation.

Depth and visited-field context both matter. A depth-limited result cannot be reused at a shallow call. Cyclic paths with different relevant visited sets cannot share an incompatible result. The old algorithm also mutates the receiver's visited set before refreshing stored values; memo outcomes retain these added IDs and replay them on a cache hit. This preserves that existing receiver-path behavior rather than silently treating the function as pure.

The memo and dependency snapshot are discarded after every top-level refresh. Heap writes, deferred bindings, XML bindings or phase changes in later calls therefore cannot reuse stale results. No context, queue, dispatch, summary or depth budget is increased or reduced. The cache changes repeated work, not the represented alternatives.

## Cooperative deadline

The original engine deadline is now checked at refresh entry and inside union, field, dependency and XML loops. Deadline expiration unwinds the local traversal and returns the original top-level unresolved value, with `deferred_binding_deadline`. It does not turn the remaining value into an empty result or claim that it was fully resolved. Existing depth diagnostics and weak field-order diagnostics remain.

The deadline path is a resource-bound fallback, distinct from the exact memo path. Runtime validation must distinguish a real reduction in repeated work from deadline termination. No new shorter job/time threshold was added.

## Semantic and work tests

RefreshBindingFixture reproduces the missing deadline check against frozen v6 (`test/runs/generic-v7-refresh-repro.log`). The new implementation passes:

- A shared five-level, nine-way heap diamond resolves to the same leaf as the prior recursive algorithm. The prior reference executes 199,288 recursive calls; the memo expands 56 states. The measured synthetic memo call took about three milliseconds in the first full test run. Work-count assertions, not machine-dependent time thresholds, enforce this improvement.
- A shared node reached at different depths preserves the prior depth-limited alternatives.
- Cycles with different visited paths preserve their unresolved alternatives, including an already-visited root.
- Deferred receiver paths retain visited-set propagation before stored-value evaluation.
- A heap mutation between top-level refresh calls changes the result, proving no stale cross-call cache.
- Eighty deterministic random cyclic graphs, with different initial depths and visited sets, match both the prior reference's returned value and its visited-set mutation.
- An already expired deadline returns the original unresolved input and records the deadline diagnostic.

The full capabilitySelfTest and compactReportTest pass after these changes (`test/runs/generic-v7-final-core-build.log`). Existing installed-client isolation, static bridge endpoints, framework-factory boundary, queue fairness, switch ownership and phase-resume fixtures remain enabled. Real Ctrip runtime/checkpoint and golden-score improvement require a frozen v7 run; the synthetic diamond result does not prove every heap graph has the same sharing pattern.
