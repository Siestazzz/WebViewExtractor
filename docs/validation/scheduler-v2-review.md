# Scheduler v2 independent review

Frozen artifact: `test/runs/scheduler-v2.jar`, SHA-256 `cf716d5913de08918fed0b6fb529759a40cdbfe3a7f489258be2834b6e201c03`. Current source and frozen bytecode were reviewed. Existing capability tests and `PhaseRetentionFixture` are reported passing. No production source, oracle, sealed sample, or new-App decompilation was changed or read for this review.

## Confirmed fixes

The v1 finalization loss is fixed. `finishActivity` now computes `remainingProvisional`, builds `completedReport`, and only then clears phase state and releases the Host. First-phase facts not rederived in phase 2 remain in the final `facts` array with `binding_status=candidate` and `analysis_stage=previous_phase_provisional`.

Member shrinkage is guarded: `refinesFact` requires every old member signature to exist in the fresh fact. WebView IDs must match; a receiver type may stay equal or become a subtype. Values and argument counts must match. A map-lookup refinement requires one recorded lookup source, a determinate key, the same map object, no wildcard entry, and a resolved object matching the fresh argument ID and type. Superseded placeholders are removed from scoring facts and retained under `superseded_provisional_facts`.

Progress reporting is materially better:

- `discarded_contexts` counts queued contexts abandoned at host caps.
- `phase_contexts` separates phase work.
- unfinished hosts at the deadline report `deadline_interrupted`.
- checkpoint time is subtracted from Activity analysis time and reported independently.
- aggregate retained hosts, heap entries and queued contexts are exposed.
- global scheduling uses `finished_with_limits` when a host is capped or retains provisional facts.

The checkpoint code also checks the global deadline again after report writing, before starting another atomic job.

## High residual: identical opaque argument can refine without provenance

The stated boundary says an unknown object without a source must not be replaced. The implementation still accepts this case when old and fresh argument values are equal. In the argument loop, `x.equals(y)` continues before checking `lookupSources`; the later implementation rule allows `unknown -> concrete` whenever the argument list is non-empty. Therefore the same opaque `V` can acquire a concrete implementation without any object-refinement edge.

Frozen-JAR result:

```text
refines_without_source=true
```

Require concrete object provenance for every `implementation=unknown -> concrete` transition. Equality of an opaque abstract value is not provenance. Safe cases are an already-concrete identical registered object, or an unknown resolved through an exact lookup/factory/field relation to the fresh concrete object.

## High residual: registry receiver argument is skipped

`refinesFact` compares arguments starting at index 1. That is appropriate for instance WebView APIs when argument 0 is already represented by the exact `webview.id`. It is unsafe for message/keyed registry APIs, where argument 0 is the registry/router receiver. Two different registry instances with the same site, WebView, selector and handler currently refine each other.

Frozen-JAR result:

```text
refines_different_registry=true
```

The comparison must be API-shape aware. For bridge/callback WebView setters, receiver equality can come from `webview.id`. For registry transports, compare argument 0 or an explicit `registry_receiver_id`, plus `registry_field` and namespace/selector. Do not globally skip index 0.

Reproducer for both cases:

```bash
cd /home/d3008/phy/workspace/WebViewGPT/webview-extractor
mkdir -p /tmp/scheduler-v2-probe
javac -cp test/runs/scheduler-v2.jar -d /tmp/scheduler-v2-probe \
  docs/validation/regressions/SchedulerV2RefinementBoundaryProbe.java
java -cp /tmp/scheduler-v2-probe:test/runs/scheduler-v2.jar \
  org.example.SchedulerV2RefinementBoundaryProbe
```

## Supersession audit limitations

`superseded_provisional_facts` preserves the old fact and a generic reason, but does not identify the replacement fact/key or the exact refinement edge used. Add the fresh semantic key and provenance type (`same concrete object`, `exact map lookup`, `receiver subtype`) so a reviewer can replay the decision.

`remainingProvisional` recomputes candidates and calls `refinesFact` repeatedly during report generation, coverage updates and finalization. For large capped hosts this is potentially quadratic in provisional and fresh facts. Cache the merge decision per phase snapshot or build a refinement index keyed by the full identity tuple.

## Phase and memory limitations retained from v1

The selective phase reset still retains heap, maps, arrays, `constructed`, deferred fields and layout aliases while clearing facts, visited work, materialized objects and XML consumers. Preserving provisional facts prevents silent loss, but the second phase remains a hybrid continuation rather than a clean rerun. The new provisional marker makes this uncertainty visible; it does not remove the order dependence.

The initial pass still opens and retains a full Host for every unfinished Activity before deep round-robin begins. New memory metrics expose the risk, but no bounded active window or serialized continuation limits peak memory. Checkpoint reports remain observational snapshots, not restartable process checkpoints.

## Progress/status residuals

`finished_with_limits` currently tests only `limited` and `provisionalFacts > 0`. A host can finish with unresolved terminal XML consumers, deferred fields, decode/resolve gaps or other diagnostics while the scheduling stage says `finished`. The top-level report may still be `partial`, but the stage label is narrower than its name. Include terminal deferred work and material analysis-limit gaps, or rename it to describe only cap/provisional conditions.

`pending_contexts` remains the direct queue size. The separately reported XML/deferred counts help, but consumers must sum multiple fields to understand pending work. This is acceptable if documented; it should not be described as total pending work.

## Test assessment

`PhaseRetentionFixture` now covers:

- a first-phase-only fact surviving normal completion;
- member-list non-shrinkage;
- exact map receiver/key/object refinement;
- different-object and wildcard lookup negatives;
- capped completion status.

It still injects facts/state directly rather than exercising a real DEX constructor that runs only in phase 1. Add a DEX fixture where `AppActivity.onCreate` constructs `OneShotComponent`, whose constructor registers a concrete annotated bridge. Retained `constructed` should suppress that constructor in phase 2, while the final report must retain its phase-1 bridge and member provisionally. This would validate the actual seed/enqueue/constructor path rather than only the merge container.

Also add the two frozen-JAR negatives above to the main suite. The existing different-object test changes the fresh object ID; it does not cover an unchanged opaque object with no lookup source. The existing registry test validates map provenance but does not test two different registry receivers at argument 0.
