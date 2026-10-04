# Generic v3 regression source review

This review uses the already authorized Mango development sample, APK SHA-256 `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5`. Source paths below are relative to `test/decompiled/com.hunantv.imgo.activity/sources/`. It does not inspect a new holdout or claim restored golden recall before a frozen APK run.

## Actual client getter construction

`com/mgtb/money/web/webview/ProgressWebView.java:244-261` constructs a WebClientImpl holder during initProgress and installs its getter results using explicit superclass client setters. `WebClientImpl.java:75-79` initializes its client fields, while its constructor at 656-659 stores Context/ProgressBar rather than a WebView. The getters at 735-741 return the stored clients. The actual binding is ProgressWebView construction → initProgress → holder construction/field initialization → getter → client installation.

v3 retained that actual initProgress path but degraded the WebViewClient implementation to the SDK base client. The index's getter dependency propagation recognized WebView/Settings return types but omitted standard client return types. Therefore the real holder constructor's client write was not relevant after speculative constructor inference was removed. v4 adds Android/Tencent standard client types to that existing dependency propagation; it does not construct unresolved field owners. ClientGetterLifecycleFixture's direct construction case reproduces the failure against frozen v3 (`generic-v4-getter-repro.log`) and preserves the exact stored client with the fix.

Source SHA-256: ProgressWebView `4f9a3b806a326a6ab5495d46326739a389e1ac6170c28d4ffbeca9cf4a9c31b1`; WebClientImpl `d5580f58ceae24fc12ab3a7fbcf5a52cfc6e39185092f8a8ac6ac1697ae5c5bb`.

## Inherited lifecycle and actual virtual hook

`com/hunantv/imgo/base/RootFragment.java:265-270` implements onActivityCreated(Bundle) and calls virtual onInitializeData(Bundle); its base hook at 619-620 has no capability. Its onViewCreated at 506-509 similarly calls onInitializeUI. `com/hunantv/imgo/xweb/XWebViewFragment.java:2905` supplies the actual onInitializeData override; its woven body at 1659-1664 calls initWebView, and the body at 765-779 constructs XWebView. XWebActivity's onInitializeUI creates this Fragment and installs it with an actual FragmentTransaction replacement.

The inherited framework lifecycle was excluded because global relevance did not expand an empty concrete base hook into every subtype. v4 retains that restriction but checks recognized lifecycle reachability on the actual allocated receiver. An actual subtype override may make an inherited lifecycle relevant; an unused arbitrary helper still cannot become a component root. ClientGetterLifecycleFixture's inherited lifecycle case fails against frozen v3 (`generic-v4-lifecycle-repro.log`) and passes with the receiver-specific check. ComponentEntryFixture continues to reject unused helpers.

Source SHA-256: RootFragment `5854f60da79e38794fbbf39d40ec89b31d5a0d2cb0399a128bd90dfe73025d59`; XWebViewFragment `0892bacd23f98eaec707d1e91f7924ec714e48b6742e02b9836f046ef4619d3b`.

## Pangle wrapper boundary remains unresolved

`com/bytedance/sdk/component/widget/SSWebView.java:34,91,131-149` is a FrameLayout wrapper holding and constructing an inner MoWebView. Its child setup at 193-202 adds that real inner view; setters at 595-615 delegate clients to it. Consequently an SSWebView wrapper name must not substitute for the capability receiver's actual WebView identity.

The observed v2/v3 receiver distributions differ, and neither report uses SSWebView as an exact WebView receiver. This review does not prove a complete factory/child binding repair or recover arbitrary wrapper helpers as roots. That regression needs further actual-binding validation. Source SHA-256: SSWebView `da0009ca374ff69ec3f7d944c736b4fb6204e959897de1b86fc6484e7f6600ef`.

## Validation scope

The focused v4 fixtures separately test actual client construction, inherited lifecycle dispatch, empty API override suppression, explicit super/native effects, receiver-union isolation, and unresolved candidates. Full selfTest and compactReportTest must pass before freezing. Class.getName/Fragment.instantiate and installed ViewPager2 adapter models are not implemented by these fixes. v3 remains an unsuccessful experiment: its ten-App validation includes the recorded 211 Mango and four Fanqie golden regressions and timeouts; fixture success does not overturn that result.
