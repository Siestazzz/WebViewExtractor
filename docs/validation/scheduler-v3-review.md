# Scheduler v3 independent review

Reviewed frozen artifact: `test/runs/scheduler-v3.jar`

- SHA-256: `da6d25d30764f8631856bc86ac32fd0babc18a6755c8045afed625104c8972b6`
- Review scope: phase retention/refinement boundaries, progress/finalization behavior, and an independently generated real-DEX constructor case.
- The aborted scheduler-v2 six-App run is not treated as a completed measurement.

## Refinement boundary replay

`docs/validation/regressions/SchedulerV2RefinementBoundaryProbe.java` was compiled and run unchanged against the frozen v3 jar. Its observations were:

```text
refines_without_source=false
refines_different_registry=false
```

The process then exited 1 because the historical probe deliberately asserts the old buggy values (`true`). That final assertion is expected and is not a v3 failure. The two relevant observations establish that v3 no longer:

1. replaces an opaque unknown bridge object with a concrete implementation when the unknown has no lookup provenance; or
2. treats message-registry facts with different arg0 registry receiver identities as the same refinement.

Source inspection of the frozen-equivalent implementation agrees with the replay. `refinesFact` compares every argument. A differing argument is accepted only for the native capability receiver with stable object identity and a concrete subtype, or for an unknown whose `lookupSources` resolve through one exact map receiver and one definite key to the new object. Wildcard map entries do not qualify. Changing `implementation` from `unknown` additionally requires a concrete target argument or that exact map resolution. Existing exposed members must be a subset of the replacement members, so refinement cannot erase members.

## Real-DEX phase-retention probe

`docs/validation/regressions/SchedulerV3ConstructorRetentionProbe.java` builds a DEX containing this actual bytecode path:

```text
probe.AppActivity.onCreate(Bundle)
  -> new probe.OneShotComponent(this)
  -> OneShotComponent.<init>(Context)
  -> new android.webkit.WebView(context)
  -> new probe.Bridge()
  -> WebView.addJavascriptInterface(bridge, "constructorOnly")
  -> @JavascriptInterface probe.Bridge.exposed()V
```

The probe runs phase one through `CapabilityIndex` and `CapabilityEngine`, then suppresses second-phase jobs to model a constructor path that cannot be re-derived after the phase boundary. This is intentionally a scheduler-boundary test, not a claim that this small fixture naturally becomes unreachable in phase two. The first-phase fact itself is produced only by analysis of generated DEX; it is not inserted into an engine map by the test.

Frozen-v3 result:

```text
constructor_only_retained=true
analysis_stage=previous_phase_provisional
binding_status=candidate
implementation=probe.Bridge
member=Lprobe/Bridge;->exposed()V
phase_contexts=[3, 1]
```

The retained fact remains visible in the final Activity report, is conservatively marked `candidate`, and carries `analysis_stage=previous_phase_provisional`. Its concrete implementation and annotated member survive. `provisionalFacts` is 1. This closes the v1 failure mode where phase transition cleared an independently established fact merely because phase two did not recreate it.

To replay:

```sh
probe_dir=$(mktemp -d /tmp/scheduler-v3-probe.XXXXXX)
javac -cp test/runs/scheduler-v3.jar -d "$probe_dir" docs/validation/regressions/SchedulerV3ConstructorRetentionProbe.java
java -cp test/runs/scheduler-v3.jar:"$probe_dir" org.example.SchedulerV3ConstructorRetentionProbe
```

## State and progress review

The reviewed state machine preserves first-phase facts in `previousFacts`, combines unresolved facts into reports, and records replaced entries in `superseded_provisional_facts`. It does not clear all first-phase facts when phase two starts. `finishActivity` snapshots the report before releasing the host heap, queue, and receiver state, so finished Activities remain reportable while retained analysis memory is released.

Progress distinguishes phase context counts, pending queue size, discarded contexts, checkpoint time, provisional facts, and deadline interruption. Checkpoint serialization time is accumulated separately and extends a slice stop time, avoiding a report-write charge against the Activity analysis slice. The final global stage becomes `finished_with_limits` when any Activity is budget-limited or retains provisional facts.

Residual limitations:

- This is result retention rather than resumable persistence across processes. Partial JSON is a report snapshot; it does not serialize queues, heap identities, maps, or other `ActivityState` needed to resume after process termination.
- During a long run, every unfinished Activity retains its host heap and queue. Finished hosts are released, but peak memory can still approach the sum of all initially started unfinished hosts. The emitted `peak_retained_hosts` and heap/queue counters make that condition observable.
- A context-budget cap discards the remaining queue and records the count. The Activity is finalized with limits; discarded work cannot later be resumed.
- The real-DEX probe forces the absence of phase-two replay at the boundary. It validates retention and labeling, but it does not prove that all naturally occurring constructor paths are scheduled only once or that all phase-two equivalence decisions are correct.
- `remainingProvisional` compares prior facts against current candidates when progress/report snapshots are requested. Large fact sets can add checkpoint CPU cost; that cost is reported as checkpoint time, but no explicit comparison-count metric is emitted.

Within this scope, v3 fixes both known over-broad replacement boundaries and retains a real DEX-derived, first-phase-only bridge fact without promoting it to a fully confirmed second-phase result.
