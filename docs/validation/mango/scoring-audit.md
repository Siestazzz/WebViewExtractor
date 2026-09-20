# Mango scoring-framework audit

Scope: `scripts/evaluate.py` and the disclosed `docs/validation/mango/canonical-facts.jsonl`. This audit does not read holdout data and does not change facts, scoring, or production code.

## What the current score measures

The evaluator replays each oracle row independently against every emitted fact under the same Activity string. It reports positive-fact recall. It does not compute capability precision, prove Activity ownership, or prove that a matched capability belongs to the same runtime WebView object as the oracle carrier.

The canonical file has 2,370 rows: 30 Activity bindings, 596 settings, 591 callbacks, 1,086 `bridge` rows, 64 message handlers, and 3 bridge methods. Of these, 2,312 have `binding_status=confirmed` and 58 have `binding_status=registered-target-unknown`. `symbol_status` contains 1,685 confirmed DEX symbols, 596 external APIs, and 89 not-applicable rows.

“Zero unresolved signatures” only means that every nonempty signature submitted to symbol validation was found or classified as an external API. It does not resolve the 58 unknown targets: all 58 deliberately have blank endpoint signatures. Fifty-six retain a known registration name; two have a runtime-dependent name and target.

## Bridge registration versus method coverage

The endpoint branches are sound in one important respect. A `bridge_method` or `message_handler`, and any `bridge` row carrying a signature, matches only when an emitted member has the exact normalized signature (`evaluate.py:33-35`). A plain `bridge` row with no signature returns true after registration-name and optional implementation checks (`evaluate.py:29-36`). Thus a registration-only row does not directly satisfy an endpoint row.

The aggregate `bridge` metric nevertheless combines three different claims: registration existence, annotated/reflected bridge members, and message handlers (`evaluate.py:41`). It can therefore be described incorrectly as “method coverage.” In the current v3f replay, all 56 known-name `registered-target-unknown` Imgo rows match solely by registration name even though their implementation and endpoint remain unknown. Those 56 successes raise the aggregate bridge numerator but prove no callable member. The two runtime-name registrations are not endpoint facts at all.

Recommendation: publish separate counters for `bridge_registration`, `bridge_member`, and `message_handler`. Keep unknown registration recall in the first counter only. Never label the combined bridge percentage as exposed-method recall.

## WebView identity is not verified

Candidate selection uses only `actual[g.activity]` (`evaluate.py:42`). Matching does not compare the oracle `webview`, `carrier_path`, installation site, namespace object identity, or the concrete client passed to a setter. Any same-Activity emitted fact with the matching API/value, member descriptor, or registration can satisfy the row even if it belongs to another WebView instance or a detached construction path.

Activity binding rows are skipped entirely (`evaluate.py:39`), so the binding evidence that connects an Activity to its carrier never gates capability matching. This is particularly material for Activities containing multiple WebViews, Fragments, SDK wrappers, or conditional factories.

Recommendation: assign stable carrier IDs to binding and capability facts. Require the report to emit the same bound-object ID, or at minimum match a validated Activity→Fragment/view field→constructor/install chain. Treat facts without object identity as candidate matches pending ownership review.

## Unknown and unscorable behavior

Unknown handling depends on whether the report emitted any facts for the Activity:

- A row with `registration_name=null` makes `match` return `None` for each bridge candidate (`evaluate.py:30`) and is counted unscorable when at least one candidate exists (`evaluate.py:46`). The OPOS dynamic map follows this path in v3f.
- If the Activity has no candidates, `outcomes` is empty. `any(None)` is false, and kind `bridge` is omitted from the fallback blank-signature check at line 46. The same unknown is then reported as `not_matched`, not unscorable. Loading's dynamic `method_class_names` row currently exhibits this host-absence-dependent result.
- Known-name unknown targets can match as registrations. In v3f, all 56 such rows match, although their endpoint identity remains unknown.

Consequently, unknown facts participate inconsistently in the denominator and failure reason. The published bridge metric currently has 1,153 expected, 1,086 matched, and 1 unscorable, even though the oracle contains 58 unknown-target registrations.

Recommendation: classify oracle facts before candidate matching. Registration-known unknowns should be scored only for registration recall and reported with `endpoint_status=unknown`. Runtime-name unknowns should always be excluded from scored endpoint recall and counted in a stable `unknown/unscorable` bucket, regardless of whether the Activity was emitted.

## Repeated facts are not matched one-to-one

Each oracle row independently scans the full candidate list, and matched emitted facts are never consumed. One emitted fact can satisfy multiple identical oracle rows. The canonical file contains 90 repeated scoring keys across 85 groups. Examples include three `setJavaScriptEnabled(true)` rows in each Alipay host and three same-valued JavaScript or file-access calls in XWeb/WebContainer.

Preserving repeated calls is correct evidence, but the current evaluator measures presence, not invocation multiplicity or ordering. Counting every repeated row in both numerator and denominator gives the appearance of call-level recall while allowing one emitted call to receive credit several times.

Recommendation: either collapse canonical rows to unique capability keys for presence recall, or implement bipartite/consuming matches using callsite or occurrence IDs. Report ordered-call coverage separately when successive configuration calls matter.

## Setting matching can be broader than the oracle API

Settings compare only the text after `->` (`evaluate.py:12`). This discards the declaring owner, so Android and X5 methods with the same method descriptor are interchangeable for scoring. Dynamic values match any nonempty argument list (`evaluate.py:14`), without checking the expression, source, or value domain. Enum matching checks only the enum member name in an argument ID (`evaluate.py:16-18`).

Recommendation: compare the complete normalized API descriptor, including owner and parameter types. Treat dynamic-value recall as API-presence recall and publish it separately from exact-value recall.

## Ownership and possible inflation

Ownership is optional. Without `--ownership`, every emitted Activity is `unreviewed`; the latest v3f Mango replay has 74 emitted Activities, zero reviewed valid Activities, and a conservative error upper bound of 1.0. The `wrong=0` field must not be presented as zero ownership errors. The evaluator itself states that capability precision requires independent bound-object review.

Even with an ownership file, verdicts are Activity-level only. They do not validate each WebView instance or each capability association. Recall can therefore be high while ownership or capability precision is poor. The dictionary construction at line 6 also silently overwrites earlier entries if a report contains duplicate Activity records.

Recommendation: require an ownership artifact for acceptance claims, reject duplicate Activity records, and distinguish Activity ownership from capability-to-object ownership. Publish recall beside reviewed coverage and a precision/error bound; do not infer completeness from recall alone.

## Safe interpretation of the current result

The v3f figures are candidate-inclusive presence recall against disclosed positive facts. They show whether a same-Activity report contains matching capability shapes. They do not show full app coverage, endpoint precision, correct WebView identity, invocation counts, or reviewed ownership. It is supportable to say that all 30 disclosed hosts were audited and that every nonempty canonical signature resolved. “Complete coverage” is not supportable while 58 target-unknown registrations remain and emitted ownership/capability identity has not been independently validated.
