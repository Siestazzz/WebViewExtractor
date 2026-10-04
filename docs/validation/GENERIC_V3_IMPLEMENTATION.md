# Generic v3 implementation

The frozen v3 jar SHA-256 is `c81d5de49a1c79b0895e5f9f285f771032f97c85539a29a03eaa95ba0bd318c2`. Its four changed source files are copied under `test/runs/generic-v3-source/` with their repository paths preserved. The ten-App evaluation is a separate validation step; this implementation note does not claim overall recall or precision from the synthetic tests.

## Component entries

`CapabilityEngine.seed` previously started every relevant method on allocated non-Activity components. Programmatically created custom Views therefore ran unused business helpers, and Fragment inheritance could start helpers unrelated to a lifecycle call. XML Views had a name-only filter that also accepted overloads sharing callback names.

Non-Activity component entry selection now uses `CapabilityIndex.componentEntry`: an instance method must be public/protected and match a complete recognized framework callback signature. View and ViewGroup callback families are separate. Fragment lifecycle entries preserve inherited implementations for native, AndroidX and support-v4 families; Dialog lifecycle entries use their own signatures. AndroidX/support primary-navigation callbacks and native Fragment memory callbacks remain family-specific. Platform signatures were checked against the local official Android API 36 `android.jar` using `javap -s`.

Constructors and class initialization remain modeled through construction. A called private helper remains reachable through normal invoke-direct dispatch, and a public helper remains reachable through its actual call. Installed WebView client/listener callbacks and asynchronous registrations continue to use their existing SDK/structural registration protocols. A callback-shaped name or a class literal alone does not install a component or listener.

Activity entry selection was not narrowed in v3. Recognized component lifecycle callbacks remain conditional candidates: allocation alone does not prove that every lifecycle event actually occurs. The finite callback table is conservative for SDK-specific callbacks outside the listed contracts. Fragment factories and adapter installation protocols are not added in v3; they are separate v4 work.

## Instance field origins

`CapabilityEngine.eval(field)` previously attempted all constructors of a field's declaring class when the receiver had not been marked constructed. An unresolved `field_object` could consequently obtain a fresh WebView from a constructor that had never been called on a known object.

Instance field values now come from actual constructor materialization, actual writes/calls, and existing XML construction jobs. Missing values remain deferred candidates. A static field read still models its declaring class initializer. The field declaration does not itself establish an instance allocation or select a constructor overload.

This removes speculative constructor-derived receivers rather than increasing any queue, context, summary or time budget. It can reduce recall where runtime instance construction cannot be established. Existing phase replay and deferred binding remain in place.

## Positive and negative fixtures

`ComponentEntryFixture` reproduces both errors against the frozen v2 jar:

- Allocating a custom View and Fragment emitted `unused-view-helper`, `unused-fragment-helper`, and a wrong callback overload. Logs: `test/runs/generic-v3-entry-repro.log`.
- Reading a WebView field on an unresolved owner returned a concrete object allocated in that owner's unobserved constructor. Log: `test/runs/generic-v3-field-repro.log`.

After the fix, that fixture rejects the unused helpers, invalid overloads, a ViewGroup-only callback on an ordinary View, and constructor origins on unresolved receivers. It retains a real constructor calling a private init helper, a parent's View attachment callback, a parent's Fragment `onViewCreated` calling adjacent init helpers with a field write/read, a `Handler.post` callback capturing the actual View, a real instance constructor's stored field, and a genuine static class initializer. A ViewGroup callback remains accepted on ViewGroup.

The existing `FragmentLayoutFixture` verifies that two Fragment instances retain separate WebViews and that lifecycle View arguments, `getView`, `findViewById`, and `getChildAt` preserve the same allocation. Existing XML replay, constructor capture, composed receiver, callback registration, queue fairness, switch ownership and phase retention regressions remain enabled.

## Old fixture correction

`deepEvidenceFixture` used `Fragment.onCreateView()V` as an implicit framework entry. That signature has no framework lifecycle contract: the actual `onCreateView` receives LayoutInflater, ViewGroup and Bundle and returns View. The fixture now uses the legitimate `onCreate(Bundle)V` lifecycle signature while preserving its 72-call evidence chain and its positive Fragment capability assertion. No positive assertion was deleted to accommodate the new filter.

## Validation and remaining boundary

`./gradlew capabilitySelfTest compactReportTest` passed after the final v3 changes; log: `test/runs/generic-v3-entry-build.log` (BUILD SUCCESSFUL, 6 seconds). The coordinating agent subsequently built and froze the jar identified above.

BreakingNews v2 showed remaining candidate settings/operations reached through an unresolved lifecycle owner and conservative synthetic callback branches. These general entry/origin fixes may remove unsupported paths, but this note does not mark those sampled ownership errors fixed without checking the frozen v3 report. Missing or joined selector values still retain all switch branches; v2's bounded exact-selector specialization is preserved.
