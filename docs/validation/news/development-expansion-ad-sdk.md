# Ad and third-party development expansion

## `com.tencent.ads.landing.AdLandingPageActivity`

The manifest Activity creates `AdLandingPageWrapper`; its listener constructs `AdPage` (`AdLandingPageActivity.java:92-104`). `AdPage` extends `AdCorePage`. The wrapper calls `mAdPage.loadWebView(mUrl)` (`AdLandingPageWrapper.java:281`). `AdCorePage.loadWebView` creates `AdWebViewWrapper`, installs page WebView/WebChrome clients, and calls `AdCoreWebViewHelper.setJsWebChromeClient` (`AdCorePage.java:772-810`, `AdCoreWebViewHelper.java:85-104`). The system wrapper settings, both concrete page clients plus their explicit superclass implementations, and all AdCore prompt-router handlers are expanded in the gold. The prompt transport remains `MraidBridge`; no public method was included merely because it is public.

## `com.tencent.ams.splash.preview.SplashAdDynamicPreviewActivity`

The Activity's runnable constructs `SplashAdDynamicView(this, loader, this)` and attaches it (`SplashAdDynamicPreviewActivity.java:112-129`). `SplashAdDynamicView extends SplashAdView` (`SplashAdDynamicView.java:59`). The inherited conditional gesture path calls `AdDrawGestureManager`; its `createController/createWebview` path constructs `InteractiveEastEggController` and `EasterEggWebView` (`AdDrawGestureManager.java:201-217,824-831,1304-1308`). The controller bind installs itself as the WebViewClient. This reuses the independently verified EasterEgg capability group only for that conditional path.

## `com.tencent.midas.jsbridge.APWebJSBridgeActivity`

This manifest Activity selects `APX5WebPage` or `APSystemWebPage` (`APWebJSBridgeActivity.java:164-176`). The pages create `APX5WebView`/`APWebView` with the Activity's concrete WebView (`APX5WebPage.java:167,193`; `APSystemWebPage.java:166,191`). Each wrapper constructor calls `InitWebView`, applying all settings and installing its two anonymous clients (`APX5WebView.java:108-131`; `APWebView.java:109-134`). Both branches' six actual overrides are included with exact APK descriptors. These wrappers remove legacy JS interfaces and do not register an application bridge, so bridge-removal was not counted as a positive capability.

## Exhaustive remainder audit

All manifest Activity names were cross-checked against: direct WebView/configuration calls, subclasses of the known WebView Activity bases, references to custom WebView classes, every unowned `addJavascriptInterface`/client/settings owner, and manager/factory reverse references. This produced 23 publishable development positives. Together with the 10 independently retained, undisclosed Activity holdouts, the source-confirmed population is at least 33, but only the 23 non-holdout identities appear in development gold.

The remaining prominent candidates were not promoted:

- `MobileQQActivity` and `QzoneShareActivity` call Tencent sharing APIs; their source owns no WebView and does not receive a concrete SDK dialog/WebView instance. SDK dialog capability cannot be attributed to these launch Activities.
- `com.tencent.open.yyb.AppbarActivity` has a WebView implementation but is absent from this APK's manifest, so it is not an application Activity entry point.
- Manifest `com.tencent.tads.splash.AdLandingPageActivity` has no matching DEX class/source. A similarly named `com.tencent.ams.splash.core.AdLandingPageActivity` exists in DEX but is not the declared component. This remains unresolved rather than being treated as a positive.
- `DocQQAuthActivity` and `IdentityAuthorizeActivity` route authentication and contain no owned WebView path.
- Context-only custom views/services in `unowned_entrypoints.jsonl` remain unowned where no unique manifest Activity can be proven.

The 10 holdout identities and their capability paths are intentionally absent here.
