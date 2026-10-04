# Generic v4 implementation

This version addresses two source-confirmed legitimate construction/lifecycle gaps exposed by v3 and one confirmed capability precision error. It changes no App signature rules or budgets. Frozen APK validation remains the acceptance step; synthetic results are not a claim that all v3 regressions are recovered.

## Actual construction and lifecycle

The index now propagates getter dependencies for Android/Tencent WebViewClient and WebChromeClient return types alongside the existing WebView/Settings types. Actual holder construction can therefore materialize its stored client before the getter result is installed. Unresolved instance fields still do not trigger arbitrary constructors.

Recognized non-Activity framework lifecycle entries may additionally pass relevance based on the actual concrete receiver. This uses the existing bounded receiver-specific call search, preserving normal virtual dispatch into the allocated subtype's hook. The framework signature filter still applies; arbitrary helper methods remain excluded as roots. The existing receiver relevance budgets and gap diagnostics are unchanged.

ClientGetterLifecycleFixture isolates both cases. Its direct holder getter and inherited lifecycle cases fail against the frozen v3 jar and pass with v4. Mango source paths, SHA-256 hashes and real binding chains are recorded in `mango/generic-v3-regression-source-review.md`. The Pangle wrapper regression is not declared solved by those two fixtures.

## Resolved API overrides

Previously recognized API calls emitted their parent API effect before following an application override. A known concrete WebView with an empty final addJavascriptInterface override therefore falsely registered a bridge through a base-typed helper.

Capability dispatch now resolves the actual receiver before emitting ordinary WebView/Settings API effects. A known concrete application override supplies the behavior: the original effect occurs only if its actual implementation reaches a framework/native call, including an explicit super invocation. Empty overrides suppress that original effect. Native terminal SDK APIs continue to emit, and unresolved polymorphic receivers retain conditional candidates. Receiver union arms are processed separately; a blocked concrete arm cannot install a bridge on another arm. Nullable arms retain non-null conditions. Existing structural custom callback/message registrations keep their prior protocol handling.

CapabilityDispatchFixture reproduces the false bridge against frozen v3 (`test/runs/generic-v4-override-repro.log`), then verifies empty/forwarding/native behavior, single super effect, mixed receiver isolation, unresolved candidates and empty client/settings/removal overrides. The real source evidence is in `sohuvideo/empty-virtual-api-override.md`; no private class or method name is part of the production rule.

## Queue diagnostics and scope

The coordinating agent added bounded ContextQueue diagnostics and non-mutating inspection tests. Detailed coverage includes up to twelve pending-method samples with lane counts and bounded receiver/argument/path information. This observation does not change queue order, fairness, context caps or compact output.

`./gradlew capabilitySelfTest compactReportTest` passed after the final queue changes (BUILD SUCCESSFUL, six seconds), log `test/runs/generic-v4-final-core-build.log`. Existing component helper negatives, constructor origins, switch ownership, queue fairness, phase replay and WebView isolation assertions remain enabled.

Class.getName/Fragment.instantiate and installed ViewPager2 adapter protocols remain future work. The separate TransportProtocols helper is currently unintegrated infrastructure and has no production recall claim. v3's recorded failed real-App evaluation remains valid; v4 requires a fresh frozen run before any recall/precision conclusion.
