# Independent review of generic-v2 queue and fixtures

Read-only review of ContextQueue, concreteCapabilitySeed/priorityBinding, ContextPriorityFixture and SwitchOwnershipFixture. No production or canonical changes.

Both lanes retain every queued context. When ordinary work is waiting, remove services it after at most three priority removals; each lane rotates method buckets. A method already in the lane cannot be starved indefinitely by a continuously populated other bucket. Fixture checks deterministic removal, task retention, lane accounting, ordinary fairness, clear/reset, concrete-only preference and two-WebView binding isolation. No definite queue starvation or side-effect defect was reproduced.

Scheduling classification is enqueue-time only. An unknown stored WebView field can become concrete after its method was placed in the ordinary lane; no automatic promotion occurs. This can limit speedups for deferred fragment setup, but does not delete that work. Fairness is a bound in dequeue operations, not wall-clock time: expensive jobs can still delay other work. The fixture has fixed initial lane contents; it does not simulate continuously arriving methods or deferred reclassification.

priorityBinding reads existing parameters, casts, unions, settings aliases and heap fields; it does not evaluate factories or infer new allocations. applyWrite weakly joins ordinary heap values, which avoids an obvious last-writer-wins dependence on changed method order. End-to-end semantic preservation still requires unchanged-oracle comparison of completed APK runs.

Switch refinement selects exact int cases, preserves default when unmatched, and conservatively retains all arms for unknown/union/out-of-range selectors. guardValue reads only final-field values, keeping mutable field guards unknown. Fixtures cover constructor discriminator ownership, packed/sparse cases, exception edges, weakly joined values and refinement-budget fallback. No definite semantic defect was found in this read-only review.
