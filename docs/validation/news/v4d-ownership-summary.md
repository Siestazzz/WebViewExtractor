# Tencent News v4d ownership audit

All 36 emitted Activities were checked against their source allocation, stored fields, concrete factory branches, and callback/adapter receiver identities. Result: **33 valid, 3 wrong, 0 uncertain**. Counting wrong plus uncertain candidates, the conservative ownership-error upper bound is **3/36 = 8.33%**. A `valid` verdict establishes at least one real WebView path; it does not approve every emitted fact. Per-Activity evidence and fact ceilings are in `v4d-ownership.jsonl`.

The wrong Activities are:

- `MobileQQActivity` and `QzoneShareActivity`: they only invoke Tencent share APIs. `PKDialog`, `TDialog`, and `com.tencent.open.d.b` WebViews are SDK alternatives not returned to or stored by either launcher Activity.
- `HippyDetailActivity`: it owns a `QnHippyRootView`, not a WebView. v4d reaches `NewsWebView` and `H5JsApiScriptInterface` through unrelated business-listener methods and downstream allocations; no object-return or field chain binds those WebViews to the Activity.

New valid hosts were accepted only with concrete source paths. The ilive Activities reach the `BaseWebActivity.f21854:BaseWebView` field through the framework callback that assigns the created WebView. `WebAdvertActivityV2` constructs and stores its `a0` WebView controller and delegates `getWebViewBridge()` to it. The video Activities reach `n2`/`VideoPlayManager` and conditionally attach `YspMediaPlayer`. `VisitVerticalVideoActivity` also inherits the concrete vertical-video host path.

## Binding quality relative to v3f

The direct `SecurityTicketActivity` result improved from 111 to 37 facts, removing much of the former DK/mosaic alternative contamination. Privacy and Support remain narrowly bound. Additional true hosts are now discovered, including the Rose, Weibo, VideoPreview, ilive, V2 advert, and full-player paths.

Overall precision visibly worsened for several major hosts. The first 13 Activities grew from 1,636 facts in v3f to 8,803 in v4d. Examples are `WebNovelActivity` 33→1,027, `CustomWebBrowserForItemActivity` 294→1,356, `WebDetailActivity` 195→1,253, and Ad landing 8→101. These expansions union sibling `NewsWebView`, `BaseWebView`, `TencentVideoWebView`, `RecyclableWebView`, X5, and system-WebView implementations reached through broad call-graph or type alternatives. They are not all objects owned by the Activity.

The required guard remains object identity: a fact may cross a field, return value, factory, wrapper, or callback only when the same concrete receiver is preserved. Context equality, a common WebView superclass, a generic adapter parameter, or ordinary business-call reachability does not establish ownership. `webview_operation` reachability should not promote an Activity by itself unless its receiver is already bound to an Activity-owned WebView.
