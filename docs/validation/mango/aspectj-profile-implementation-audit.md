# AspectJ production profile implementation audit

This review compares `CapabilityIndex.discoverAroundClosures` and the closure handling in `CapabilityEngine` with the independently verified Mango AspectJ runtime contract. It reviews the current implementation contract only; it does not validate a frozen capability report.

## Confirmed contract matches

The profile correctly requires a structural runtime base with `state:Object[]`, `run(Object[])`, and `linkClosureAndJoinPoint`. It models the link return as the existing final state element, records a closure attachment on the concrete join-point identity, and executes a closure only after an observed no-argument `proceed()` on that linked identity. The `run` job receives both the closure receiver and the captured state. This agrees with `p61.h.proceed() → attachedClosure.run(attachedClosure.getState())`.

The implementation also preserves indexed filled-array elements, computes the final slot from the observed array length, distinguishes concrete join-point factory objects using all evaluated factory arguments, and emits diagnostics for unresolved aliases, replacement order, and unimplemented proceed arguments. The current fixture covers linked/proceeded, linked/unproceeded, and unlinked closures, as well as two factory objects sharing a static part but differing in target.

## Defects and material limitations

### 1. Closure allocation identity omits captured non-receiver values

Ordinary `new` identity is `allocation site + allocationContext(job)`, and `allocationContext` is derived only from `job.args[0]`, truncated after two `|` components. Unlike join-point factories, around-closure allocations do not incorporate the complete evaluated constructor state.

Repeated executions of the same closure allocation for the same helper receiver but different captured strings, targets, or join points can therefore collapse to one closure and one array identity. Array slots are unioned in place. This may cross-combine a closure receiver from one runtime event with captured values from another. The existing join-point factory UUID fix does not fix the closure allocation itself.

Practical effect: capability existence remains a conservative overapproximation, but registration names, target objects, and WebView identities can be spuriously paired. This is especially relevant for closures created repeatedly by one long-lived helper.

### 2. Reattaching one concrete join point unions closures although runtime assignment overwrites

`p61.h.g(a)` performs a simple field assignment to `r`. `linkClosure` stores `union(previous, marker)` under the join-point id when it sees another attachment. A later `proceed()` may consequently execute both closures, while the real runtime executes the last closure stored before that proceed.

The emitted `aspectj_closure_replacement_order_unresolved` diagnostic is accurate, but it does not prevent false capability pairings. Resolving this requires call-order-sensitive strong updates for a definite singleton join-point instance; uncertain ordering can retain the union.

### 3. Allocation-context truncation can reintroduce instance conflation

`allocationContext` retains at most the first two pipe-delimited identity components. Deeply nested factory/carrier paths that are distinct only after the second component become identical. This applies to state arrays and closure objects, even though concrete join-point factories receive an additional full-argument UUID.

This bound is deliberate, but the resulting facts have no dedicated diagnostic. A collision can therefore look fully resolved. It should be treated as bounded object sensitivity rather than proof of exact runtime identity.

### 4. `proceed(Object[])` detection is structural but execution is entirely skipped

Any join-point-owner method with exactly one `Object[]` parameter and `Object` return is classified as `proceedArguments`, regardless of method name, and the call is replaced by `aspectj_proceed_arguments_not_modeled`. For Mango, this matches obfuscated `p61.h.a(Object[])`, but it can suppress an unrelated method of the same shape in another AspectJ-compatible runtime.

For true proceed-with-arguments calls, none of the verified flag-dependent in-place state transformation is applied and `run` is not enqueued. Capabilities reachable only through that form remain false negatives. The diagnostic prevents silent completeness claims.

### 5. Runtime discovery is namespace and member-name dependent

Discovery is restricted to class names beginning with `org.aspectj.runtime.internal.` and exact member names `state`, `run`, and `linkClosureAndJoinPoint`. A relocated/shaded AspectJ runtime, renamed field, `linkStackClosureAndJoinPoint`, or a compatible generated runtime outside that namespace is invisible even if its bytecode has identical semantics.

This restriction is safe for the independently checked Mango runtime. It is a portability limitation for a generic framework profile.

### 6. Only the final-slot link convention is modeled

The verified Mango runtime reads `state[state.length-1]`. The implementation assumes that convention for every discovered structural base, without inspecting the link method's instructions. A runtime base satisfying the current structural test but obtaining its join point from another field or index would be mis-modeled.

For a reusable profile, the index should either be proven from the link bytecode or the runtime should be matched against a known contract fingerprint.

### 7. `bindingObjects` creates a relevance-dependent execution gate

`linkClosure` accepts only closure types present in `idx.bindingObjects`; around closures enter that set only when their exact `run(Object[])` is already marked relevant. This usefully blocks irrelevant closures, but it means a missed reverse-relevance edge at an intermediate generated wrapper prevents runtime linking even when construction, link, and proceed are all present.

The Mango two-layer path illustrates the sensitivity: relevance must traverse `t.run → H1 → G1 → F1`. If host traversal reaches the link but relevance did not reach the exact `t.run`, the profile emits `aspectj_unresolved_link` rather than executing it. A host budget can independently stop before the link, as currently observed for MGVideoPlayActivity.

### 8. Budget termination remains indistinguishable from a semantic negative

The engine stops a host after 12,000 visited contexts and emits `host_context_budget`. MGVideoPlayActivity currently reaches this limit before `F1`. The runtime profile may be contract-correct while producing no Diana capability for that host. Any report containing this diagnostic must treat the host as incomplete; absence of the facts is not negative evidence.

## Non-defects confirmed by the current code

- A closure merely made relevant by `discoverAroundClosures` is not executed without a concrete runtime link and proceed.
- Returning an unresolved link marker after an actually observed link is conservative: the returned object is the join point that owns that closure even when its original alias cannot be recovered.
- The `H1` layer requires no second closure attachment. Once `t.run` executes, ordinary join-point-carrying call propagation can traverse `H1 → G1 → F1`, provided budgets and relevance gates permit it.
- Not modeling `bitflags` is harmless for no-argument `proceed()` because the verified runtime passes the unchanged captured state directly to `run`. It is material only for `proceed(Object[])`.

The corresponding machine-readable findings are in `aspectj-profile-implementation-audit.json`.
