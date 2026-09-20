# Tencent News v3f ownership audit

All 24 emitted Activities were checked against the decompiled source, including alternative evidence paths and the concrete receiver object for each capability. Results: **22 valid Activity hosts, 2 wrong, 0 uncertain**. Counting wrong plus uncertain candidates, the conservative ownership-error upper bound is **2/24 = 8.33%**. `valid` only means the Activity has at least one real WebView capability path; it does not approve every emitted fact.

The two wrong Activities are `MobileQQActivity` and `QzoneShareActivity`. They call Tencent share APIs but never receive or store the SDK WebViews. The emitted `PKDialog`, `TDialog`, `SocialApiIml`, and `com.tencent.open.d.b` facts belong to SDK-created alternatives and cannot be assigned to the launcher Activity.

Most high-count records are mixed. A recurring invalid chain starts from an Activity's X5/Android WebView type, enters a broad constructor/type fallback, then crosses through `DKMosaicWebViewComponentImpl.getView`, `mosaic.a.getDKWebView`, and `mosaic.e.<init>`. This incorrectly attaches `AdWebView`, `BaseSysWebView`, `DKDefaultWebView`, and generic `WebViewBridge` facts to unrelated Activities. A setter adapter such as `setWebViewClient(Object)` or `setWebChromeClient(Object)` is valid only when the same receiver object is proven from the Activity factory chain; matching the adapter type alone is insufficient.

Clean or narrowly scoped subsets include the two privacy Activities, `SupportActivity`, the concrete Ad landing wrapper branch, and both Midas system/X5 branches. The remaining valid Activities have mixed output and must use the independently proven group paths in `canonical-facts.jsonl` rather than treating all v3f facts as owned.

`VerticalVideoVideoActivity` is a newly valid conditional host in v3f. Its shortest proven emitted path is:

`VerticalVideoVideoActivity.ˊʽ()` → `shortvideo.m.ʽᴵ(m1)` → `shortvideov2.view.tips.e.setData(View,View,PendantConfig)` → `new CommonPendantFloatView(...)`.

Only the `CommonPendantFloatView` facts on that chain are approved. The sibling DK/mosaic facts are cross-bound. This Activity was not added to development gold during this audit because the current request was ownership review and the independent full callback/bridge surface has not yet been expanded.
