# Generic v16: Fragment superclass dispatch, manager arguments and deferred consumers

No application names, business values or private registration signatures are introduced. The v15 frozen ten-APK regressions remain evidence; the changes below must be measured against the same oracle before claiming recovery.

## Explicit SDK superclass calls

See GENERIC_V16_FRAGMENT_SUPER.md. Actual application override bodies execute; their precise invoke-super calls resolve the declared public SDK contract rather than repeating virtual dispatch to the override. Ordinary virtual dispatch and null override negatives remain unchanged. The native Activity implemented manager getter is recognized alongside the exact AndroidX/support-v4 contract classes.

FragmentSuperProtocolFixture covers native, AndroidX and support-v4 super returns, actual static return aliasing, and null overrides. beginTransaction expressions preserve both instruction offset and super dispatch mode.

## Stable manager argument capture

The residual Txws HalfWebViewActivity loss has an additional concrete analysis symptom: v15 reports constructor_capture_argument_order_unresolved for the ordinary controller's FragmentManager parameter. Source evidence in txws/v14-installed-fragment-regression-source.json shows actual new Controller(getSupportFragmentManager()) followed by the retained manager's add/commit chain. The generic allocation-capture safety check previously rejected every returned expression, including this already modeled stable SDK manager acquisition.

ConstructorCaptures now permits that exact acquisition expression only after its receiver expression passes the original argument safety checks, its actual receiver is compatible, and actual dispatch selects the precise SDK contract (or a declared SDK super call). It evaluates and freezes the manager value at the allocation; it does not delay the constructor or refresh an old concrete captured argument. Application overrides, unknown receivers and unproven mutable-field timing retain diagnostic unknown.

FragmentManagerCaptureFixture proves that a reached ordinary controller actually uses allocationCaptures, preserves the acquired manager through its field, and installs the Fragment with all three categories. The application-null-override control stays empty, and the global relevance set stays unchanged. ConstructorCaptureOrderingFixture's six temporal cases remain enforced.

## Deferred WebView consumer replay

The independent two-holder/two-cache probe proves the existing refreshBinding machinery resolves the exact deferred source immediately and retains receiver isolation. Frozen v15 already recovers the concrete callback in its second analysis phase. Therefore this is not evidence of a terminal refresh semantic failure.

The consumer observer now also records a field_object only when its type is a WebView and its ID has an actual source record in Host.deferredFields. It reuses the existing bounded consumer replay and deadline at queue quiescence. A consumer can consequently rerun in the same phase when its own registered deferred field chain resolves. It does not parse IDs to invent sources, merge receivers by type, replace frozen unknown constructor parameters, or overwrite concrete captures. Deferred ordering uncertainty remains diagnostic and replay is candidate evidence.

DeferredFieldAliasReplayProbe.run requires the concrete first callback before a whole-phase restart, keeps the unwritten second holder unresolved, and keeps a concrete captured-old value unchanged after later source writes. The older diagnostic mode retains terminal comparison. This is queue-quiescent replay, not immediate replay at every field write. The legacy tracked_xml_consumers counter now includes these registered deferred WebView consumers.

The real Fanqie intermediate trace has thousands of pending jobs: a concrete holder field is visible while a cached consumer still has an earlier placeholder. The trace cannot establish a terminal missing callback or sole root cause; pending consumers and eventual replay may still resolve it.

## Remaining frozen regressions

- HalfWebViewActivity: the stable manager capture repair targets the source-positive transparent-controller chain. The separate public DialogFragment.show/child-manager route remains unmodeled; no new-as-install fallback is introduced.
- MGVideoPlayActivity: v13 and v15 both exhausted the per-host context cap. v15 has no ImgoAdWebView surface; the previous evidence begins at constructor-seeded NBFreeWatchAdFloatFragment lifecycle. Its real installation is being independently source-checked, so neither a budget-only cause nor a particular protocol repair is asserted.
- Sohu PreviewActivity: the source-positive QF DialogFragment.show route is independently documented but unmodeled. FarmExerciseActivity has a real factory path and both frozen versions still have pending contexts; its actual install gap needs further evidence.
- Free WebPageActivity: both versions have pending contexts. v13's old capability evidence begins at construction-triggered WebPageFragment lifecycle, so it cannot justify restoring unrestricted allocation roots. Actual install evidence and queued progress remain to be distinguished.

The stable constructor, super and deferred-consumer changes passed full capabilitySelfTest (latest focused integration 18 seconds, test/runs/generic-v16-manager-tests.log). Final complete build and ten-APK results are recorded separately by the parent; source fixtures alone do not establish quality acceptance.

Follow-up source clarification before freeze: the independent Mango review confirms the ad Fragment has a real standard installation chain: NBFloatFragmentHelper.p1 constructs the actual ad Fragment and passes it through n1/P0; the retained manager performs replace(int,Fragment) and commitAllowingStateLoss. The Activity constructs the helper with its actual support manager. This rules out treating the old constructor-seeded path as sufficient installation evidence and establishes a positive to trace, but it does not prove which step remained unprocessed or unknown in the capped v15 traversal. No speculative constructor-body or relevance expansion is included for this host.

The independent Free review confirms actual public SDK transaction calls were renamed in the packaged DEX (manager d() and transaction n(int,Fragment,String)/e()). Those are not recognized by the current literal public API models. Resolving them requires SDK-body/contract evidence and precise semantic aliasing, not a per-application name rule or blanket new-as-install. This boundary is left visible for a later version.

Parent's latest complete capabilitySelfTest passed in 18 seconds (`test/runs/generic-v16-renderer-tests.log`) after integrating the independent standard WebViewRenderProcessClient family change. This covers the constructor ordering, manager capture, superclass and deferred-replay fixtures together. The current working source is ready for the parent's unified freeze; remaining host gaps are preserved for actual batch measurement.
