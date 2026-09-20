# Tencent News v13e validation

v13e emits the same 42 hosts as v13d and adds 182 semantic rows across 20 hosts, with no removals. Ownership among emitted hosts is 41 valid and one uncertain (`ShellActivity`). This host count is lower than v13c because two earlier outputs were false ownerships rather than newly introduced recall failures.

`AlbumPreviewActivity` owns an `AlbumPreView`, `PreVideo`, and native video container. Its v13c facts started from generic `VideoPlayManager -> YspPlayerService` registry reachability, without an Album object, field, listener, or service chain to that YSP WebView. Its removal is correct. `LoginWithPhoneNumTransparentActivity` only inherits the phone-number login screen; the parent initializes phone views and phone OAuth. It has no source `QQAuth`, `AuthDialog`, or displayed `TDialog` path. The v13c OpenSDK rows were unrelated login-family expansion, so this removal is also correct.

The override-follow change restores plausible super calls only when the original receiver is sound. The five ilive/okweb hosts gain the `BaseWebView.loadUrl` super call and `setWebViewClient(null)` override call, but each appears twice through alternative receiver identities. Editor, vertical-video, SecurityTicket, Novel, and Support additions are narrow superclass operations consistent with existing real calls, though duplicate or unknown alternatives remain candidates.

The larger TencentVideo additions are mixed. `TencentVideoWebView.init`, its client override, and its deferred `loadUrl` override are real for a correctly bound TencentVideo instance. v13e emits repeated copies in `CustomWebBrowserForItemActivity`, `CustomWebGameForItemActivity`, and the TencentVideo hosts. The former groups already had known receiver cross-binding risk. Following an override preserves that upstream error; it does not prove the receiver belongs to the Activity. Identical rows across many alternative IDs are aliases, not separate capabilities.

WebAdvert/DtWebView additions are source-consistent when an actual AdWebView load reaches `DtWebView.onLoad` and bridge injection, but repeated alternatives again overcount the same capability. The known Privacy recall defect is not restored: neither Privacy host changes, although its real XML field receives `loadUrl`, which dispatches through `X5WrapperWebView.loadUrl`, `DtX5WebView.loadUrl`, and conditional `injectBridge`.

The component-wide false `AdWebView.initWebViewSettings` group remains absent, which confirms v13e did not undo the XML seed restriction. Both long-video Activities remain absent.
