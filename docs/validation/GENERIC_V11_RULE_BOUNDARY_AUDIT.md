# Frozen v11 generic rule boundary audit

Audit target: frozen generic-v11.jar SHA256 `63d636f46a8e913653406c30a877c09ef8027145bf2d624612d44428f1e16238`. No application decompilation or holdout source was read. Standalone synthetic probes were compiled against this jar without changing production code or the frozen self-test.

## Confirmed Manifest issues

`ManifestBoundaryAuditProbe` output is in `test/runs/generic-v11-boundary-audit/manifest-results.txt`.

1. A real allocated Application subclass overrides `getPackageName()` to return another package. A call whose MethodReference owner is Context incorrectly resolves to the current APK package. The guard checks only `idx.resolve(declared API)`; it does not check the actual receiver's inherited/overridden method. The existing explicit-subclass-owner negative therefore does not cover ordinary virtual dispatch. Minimal regression: base-typed Application override, plus inherited native getter positive and a union containing an overriding receiver negative. The resolver should decline the native shortcut and let ordinary body analysis handle the override; a super call must retain native semantics.
2. An absent Bundle key is represented as `literal(type=null, text="0")`. Metadata equality compares literal text alone, so `TextUtils.equals(bundle.getString("absent"), "0")` becomes true, while Android returns null and equality is false. Minimal regression: absent/null versus string zero, both-null equality, actual string-zero positive, and wrong typed numeric value. Fold only compatible String/null operands and keep String.equals(null-receiver) exceptional/unknown. The lower-level probe also demonstrates that Integer-zero and String-zero share the same textual comparison; that row is a resolver type-boundary diagnostic, not a claim that an uncast Integer is valid Java CharSequence.

These are semantic resolver reproductions. Neither proves a wrong fact in the actual ten-APK run. They justify focused regressions before using this protocol as exact evidence.

## Class-key nested registry: confirmed remaining error

The complete32-case ServiceConstructorArrayProbe was rerun against frozen v11. `test/runs/generic-v11-boundary-audit/services/results.json` records jar/source hashes, reports and verdicts. Map + direct-new + absent inner key wrongly emits Bridge, setting and callback with and without singleton cache. The four Class/Constructor-reflection absent-key variants correctly emit none.

The recognizer still uses the actual Map field, Class parameter/getName key and same fresh inner Map/value parameter; it does not encode the default-key spelling. The remaining error is downstream: `mapLookup` represents a missing known key as unresolved unknown rather than a proven null. Summary guard refinement cannot prove the null branch, and evaluating `unknownMeta.getCreator()` can follow a static default creator that unconditionally performs direct allocation, independently of the missing Class field. Reflection variants happen to stop because they require a resolved Class; this is not general missing-key precision.

Recommended small regression sequence: retain real matched-key direct-new positive and both reflected positives; assert absent-key direct-new zero across cached/uncached; test dynamic key and wildcard writes remain unknown; test two registry receivers and same inner key mapped to different value objects remain isolated. A safe production fix needs either a proven closed/local Map with authoritative absence and null-aware guards, or a receiver-presence obligation before following nonstatic calls. Treating every missing observed entry as null would be unsound with weak/partial heap state, unseen lifecycle writes or escaped Maps. This audit does not recommend that blanket change.

## Association checks and remaining limits

Application versus Activity metadata scope and four WebView/two-Activity isolation have executable coverage. Consumer tokens carry method scope, bundle identity, key and typed value; nested consumer contexts replace the outer selection, and Client installation context remains the last token. Classes are materialized only on real selected key reflection calls and used through actual retained object/interface arguments. No key/class declaration is an independent capability root.

Potential completeness gaps remain explicit: same-method acquisition/enumeration/reflection recognition, per-method16 refinement variants, resource selectors unknown, and conservative allocated-Application APK ownership. A different runtime Context supplied by a provider must not be declared proven merely because its static type is Application. General collection closure, exceptions and object-null guard semantics need separate fixtures; neither restored reflected positives nor source cardinality establishes those properties.
