# Scheduler v2 phase-fact merge boundary review

The proposed direction is correct: retain first-phase facts that phase 2 does not reproduce, but suppress an epistemic `implementation=unknown, members=[]` placeholder when phase 2 proves the concrete object and exposed members. The replacement key must be stricter than `(site, API, WebView, registration_name)`.

## Required identity boundary

A placeholder may be superseded only when all of these match:

- capability kind and normalized API/transport kind;
- exact registration call site, including caller descriptor and code offset;
- exact WebView identity, not only WebView type;
- exact registration name/namespace/selector;
- exact registry/router receiver where applicable;
- exact registered object identity or a recorded refinement edge proving the concrete object is the same abstract object as the placeholder;
- registry field and transport binding for message bridges.

The current emitted bridge fact does not carry `bridge_object_id`; that identity exists only in `nativeBindings`. Site, WebView and registration name alone are insufficient. A loop or repeated callback can execute one call site with different handler objects under the same JavaScript name. Branches can also register different implementations at the same setter site. In those cases the concrete fact must not erase the unresolved alternative.

For callback registrations, use the installed callback object identity. For keyed/message registries, use `(registry receiver identity, registry field, key/selector, handler object identity)`. Do not treat a shared handler interface or declared type as object identity.

## Replacement semantics

Replacement should be directional and narrow:

1. The old fact is explicitly unresolved in the dimension being refined, such as `implementation=unknown` with no endpoint members.
2. The new fact is strictly more informative for the same object: concrete implementation and DEX-verified members, or a concrete registered-no-compatible-endpoint result.
3. All already-known dimensions remain equal. A concrete implementation must not replace a fact whose WebView, registration name or registry receiver is independently unknown.
4. If the same placeholder refines to multiple concrete alternatives, retain every concrete candidate. Do not choose one by phase order.
5. Merge members by full DEX signature. Never discard a first-phase concrete member merely because phase 2 returned a smaller list.
6. Record the removed placeholder in a `superseded_facts`/diagnostic section with its phase, semantic key and replacement keys. Do not leave it in the scoring `facts` array.
7. A first-phase concrete fact that has no phase-2 equivalent remains as `previous_phase_provisional`; normal completion must not delete it.

This solves the keyed-registry failure without reviving the old duplicate. The first-phase `unknown, []` and second-phase `test.FirstPlugin, [first()V]` may collapse only if their registry receiver, key, registered object provenance, call site and WebView are the same. If the old fact lacks object provenance, it is not safe to call it the same registration; retain it unresolved or improve provenance before replacement.

## Real DEX fixture plan

Add a phase-specific constructor fixture rather than relying only on manual map injection:

- `AppActivity.onCreate` allocates `OneShotComponent` and invokes its constructor.
- `OneShotComponent.<init>` creates or receives one WebView, creates concrete `FirstPlugin`, and calls `addJavascriptInterface(plugin,"oneShot")`.
- The scheduler's retained `constructed` set prevents the constructor body from being re-enqueued in phase 2, reproducing a concrete phase-1-only fact.
- Assert the final completed report still contains `FirstPlugin` and its annotated `first()V`, marked provisional if it was not rederived.

Add four merge fixtures:

1. Same object provenance: phase 1 unknown/empty, phase 2 concrete/member; final scoring facts contain only the concrete fact and one supersession record.
2. Same site/WebView/name but different handler object IDs; unresolved and concrete alternatives both remain.
3. One unresolved object refines to two concrete alternatives; both concrete facts remain and neither overwrites the other.
4. Same object with a concrete member in phase 1 and a smaller/empty member list in phase 2; the concrete first-phase member is retained.

The existing keyed-registry fixture should additionally assert the exact registry receiver and handler provenance used for replacement, not only `matches.size()==1`. Otherwise a broad site/name collapse can make the test pass while incorrectly merging two runtime registry instances.

## Interpretation of News provisional additions

The NewsDetail/PushDetail 47/46 additions show why replacement cannot be based on implementation specificity alone. They mix concrete detail clients/bridges with collection-return, PatchRedirector, union and `WebViewForCell` receiver identities. Some have concrete implementations but do not share one proven receiver object. Preserve or supersede them per exact identity; do not globally prefer phase-2 facts or globally retain every first-phase alias as a positive capability.
