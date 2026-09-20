# v13 XML layout implementation audit

The implementation can represent all three source-confirmed chains, but it is not yet sufficient to treat every resulting XML type as an unconditional binding. Privacy exercises the direct path only. Support depends on the analyzer entering the implemented `BaseActivity.setContentView(int)` override, carrying its `i` parameter into `inflate(i,parent,false)`, preserving the inflate return through `addView`, and later traversing that child edge from the Activity root. Novel adds two XML-created custom-view constructor chains and two `inflate(layout,this,true)` operations.

## Chain coverage

Privacy is directly covered. `layoutContent` recognizes the framework `Activity.setContentView(int)` call, `mountLayout` scopes `init_web_browser_layout` to the Activity receiver, and `lookupView` searches that receiver. The changed cast rule retains `X5WrapperWebView` when it is assignable to the source cast type `smtt.WebView`.

Support is covered only when receiver relevance dispatch queues `BaseActivity.setContentView(int)`. The override itself is deliberately excluded from `layoutContent` because it has code. Its body must then mount the base layout through the `super` call, create the `activity_support` detached root for `inflate(i,contentParent,false)`, and record `contentParent -> returnedRoot` at `addView`. The current code has each required operation. The existing layout fixture does not replay this entire wrapper chain, so an end-to-end wrapper fixture remains necessary.

Novel first requires `WebNovelActivity.ˈᵢ()I` to yield its normal constant while retaining the PatchRedirector branch as unknown. After the same BaseActivity wrapper, the Activity lookup must seed the XML `NovelWebView` component. Its constructor reaches `init(Context)`, which mounts `web_advert_view_layout_novel` on `this` because attach is true. Lookup then seeds `NovelLoadingWebView`; constructor delegation reaches `initialize(Context)`, whose `<merge>` layout attaches to that second `this`. The final lookup can preserve `AdWebView` through the `BaseSysWebView` cast. The code supports these individual operations, but the current fixture does not prove their combined constructor and nested-root chain.

## Soundness risks

The largest general risk is path-insensitive mounting. `prepareLayouts` processes summarized layout calls without branch predicates, and `mountLayout` mounts every resource-configuration path for an ID. Mutually exclusive layouts or configuration variants can therefore contribute incompatible nodes to one root. The existing diagnostic does not prevent those alternatives from being reported as confirmed ownership.

A `<merge>` root with `attachToRoot=false` is invalid at runtime. The current detached-root behavior would manufacture a tree that Android never returns. This should emit an invalid-inflate diagnostic and expose no children. A dynamic attach flag is also currently mounted onto the parent after recording a gap; it needs attached and detached alternatives with candidate provenance.

Inflate identities are derived from the method/allocation context and evaluated argument text. There is no explicit call offset or invocation occurrence. Repeated inflation of the same layout with equal-looking arguments can collapse distinct roots and their inner WebViews. `layoutChildren` then monotonically accumulates edges by symbolic ID, which can amplify that collision across branches or repeated instances. This is the known same-type/multiple-instance alias risk in a concrete form.

Include traversal correctly scopes IDs to selected layout trees and prevents simple cycles, but reaching its depth limit has no diagnostic. Resource configuration alternatives are diagnosed yet still merged. Both cases should remain visibly incomplete or candidate rather than silently becoming a definitive binding.

## Fixture gaps before acceptance

The current fixture covers resource aliases, unrelated same-ID exclusion, root isolation, ordinary attach true/false behavior, and preservation of a concrete XML subtype through a parent cast. It should be extended with:

- a full BaseActivity-style `setContentView` wrapper with `inflate(...,false)` and `addView`;
- a virtual layout-return method with one constant normal branch and one unknown patch branch;
- XML custom-view constructor seeding through two nested `inflate(layout,this,true)` calls, with the inner layout rooted at `<merge>`;
- rejection of `<merge>` plus `attachToRoot=false`;
- two instances of the same wrapper/layout that must retain distinct inner WebView identities;
- mutually exclusive and resource-configuration layouts sharing one view ID but declaring different classes.

The Privacy host probe is useful diagnosis, but it does not validate the wrapper or nested-constructor paths. Acceptance should require all three source chains and the negative fixtures above.

## Revision after v13b fixes

The merge, identity, dynamic-attach, evidence, resource-reader, and fixture changes materially improve the implementation. Literal `<merge>` plus `attachToRoot=false` now returns an unknown value with `invalid_merge_inflation` before mounting children. `DexFlow` carries the inflate instruction offset in `return_inflate:<offset>`, so distinct static call sites no longer collapse merely because their arguments match. A dynamic attach flag now returns root/parent alternatives and records a diagnostic. Every fact with XML identity evidence is forced to `candidate` and states that branch, configuration, and repeated-instance co-occurrence is unproven.

The new `LayoutResources` parser is a necessary improvement for these APKs because discovery now follows resource-table paths, including obfuscated XML paths that do not start with `res/layout`. Its bounded parsing and sparse, offset16, compact-entry, alias, and malformed-size fixtures cover the reported formats. Dynamic packages remain unsupported, complex entries are skipped, configuration qualifiers are unioned, and alias-budget exhaustion still lacks a specific truncation diagnostic.

The synthetic coverage now includes a BaseActivity-style `inflate(...,false)` followed by `addView`, plus two XML component constructor/field/getter stages with nested attach-true inflation. One assertion needs tightening: the wrapped-host check uses `allMatch`, which succeeds for an empty bridge stream. It should first require at least one bridge fact or use an `anyMatch` assertion with a count check. The nested-host assertion already requires a concrete match.

Privacy and Novel reporting their expected concrete XML types in diagnostic probes supports the direct and nested mechanisms, but does not turn candidate XML evidence into acceptance. Support still fails to recover its concrete type in the real APK. That leaves the real `BaseActivity.setContentView` summary/receiver/root binding unresolved even though the synthetic wrapper passes. The three-chain claim therefore remains incomplete.

The earlier path-insensitive risks are mitigated rather than removed. Branch and configuration layouts still share one receiver scope; candidate marking makes the uncertainty explicit. Unknown attach still mounts the parent alternative into a shared monotonic graph, so a later lookup can observe it without branch correlation. Call offsets distinguish static sites, while repeated executions of the same site still share an abstract identity. `layoutChildren` also remains monotonic by symbolic ID. These are acceptable conservative abstractions only while consumers honor `binding_status=candidate` and the XML evidence semantics.

## Revision after v13b fixes

The merge, identity, dynamic-attach, evidence, resource-reader, and fixture changes materially improve the implementation. Literal `<merge>` plus `attachToRoot=false` now returns an unknown value with `invalid_merge_inflation` before mounting children. `DexFlow` carries the inflate instruction offset in `return_inflate:<offset>`, so distinct static call sites no longer collapse merely because their arguments match. A dynamic attach flag now returns root/parent alternatives and records a diagnostic. Every fact with XML identity evidence is forced to `candidate` and states that branch, configuration, and repeated-instance co-occurrence is unproven.

The new `LayoutResources` parser is necessary for these APKs because discovery now follows resource-table paths, including obfuscated XML paths that do not start with `res/layout`. Its bounded parsing and sparse, offset16, compact-entry, alias, and malformed-size fixtures cover the reported formats. Dynamic packages remain unsupported, complex entries are skipped, configuration qualifiers are unioned, and alias-budget exhaustion still lacks a specific truncation diagnostic.

The synthetic coverage now includes a BaseActivity-style `inflate(...,false)` followed by `addView`, plus two XML component constructor/field/getter stages with nested attach-true inflation. One assertion needs tightening: the wrapped-host check uses `allMatch`, which succeeds for an empty bridge stream. It should first require at least one bridge fact or use an `anyMatch` assertion with a count check. The nested-host assertion already requires a concrete match.

Privacy and Novel reporting their expected concrete XML types in diagnostic probes supports the direct and nested mechanisms, but does not turn candidate XML evidence into acceptance. Support still fails to recover its concrete type in the real APK. That leaves the real `BaseActivity.setContentView` summary/receiver/root binding unresolved even though the synthetic wrapper passes. The three-chain claim therefore remains incomplete.

The earlier path-insensitive risks are mitigated rather than removed. Branch and configuration layouts still share one receiver scope; candidate marking makes the uncertainty explicit. Unknown attach still mounts the parent alternative into a shared monotonic graph, so a later lookup can observe it without branch correlation. Call offsets distinguish static sites, while repeated executions of the same site still share an abstract identity. `layoutChildren` also remains monotonic by symbolic ID. These are acceptable conservative abstractions only while consumers honor `binding_status=candidate` and the XML evidence semantics.
