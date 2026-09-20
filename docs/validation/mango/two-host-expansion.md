# Two-host expansion: wallet WebViews

## Host evidence

- `ThirdWebActivity`: `ThirdWebActivity.java:14` owns `WebFragment`; line 87 obtains its `ProgressWebView`. The fragment carrier reaches `com.mgtb.money.web.webview.ProgressWebView`, whose WebView implementation is `com.mgtb.money.web.dsbridge.DWebView`.
- `ThirdFullWebActivity`: `ThirdFullWebActivity.java:14` owns `BaseFragment`; line 58 obtains its `ProgressWebView`, reaching the same DWebView implementation.

Both paths are conditional on fragment creation and are separate host bindings to one shared capability group.

## Complete shared surface and evidence

- Settings are configured by `ProgressWebView`/its contained DWebView and DWebView initialization. Exact Android `WebSettings` API descriptors are external APIs; their invocation sites belong to the wallet source chain.
- Client surface is the concrete WebViewClient and WebChromeClient installed by `ProgressWebView`; callback truth must use the concrete DEX owner, never the Activity or Fragment owner.
- DSBridge transport is `_dsbridge` plumbing in `DWebView`. Annotated inner transport members at `DWebView.java:112,673-737` are transport exposure. `DefaultWebBridgeAPI` annotated methods at lines 463-1462 are namespace API members only when that API object is registered on this DWebView instance.

## Unresolved edge

JADX does not preserve a stable descriptive name for every anonymous client/transport class. The required resolution is exact owner lookup in `test/runs/symbols/mango.jsonl`. Until each owner is matched, those methods remain unknown and are excluded from canonical callback/member denominators. No registration API is substituted for an exposed member.

Next pair with the largest reusable shared surface: `TTPlayableWebPageActivity` and `TTWebPageActivity` (Pangle `SSWebView` family), keeping playable-renderer and landing-page WebViews as separate conditional identities.
