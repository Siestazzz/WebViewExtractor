Vungle renderer callback family: independent source and DEX evidence

Platform family `android.webkit.WebViewRenderProcessClient`, API 29. The actual installed WebViewClient onPageFinished installs this client on its nonnull WebView argument. Both abstract platform callback slots are implemented with exact descriptors. The responsive implementation is still a callback despite doing only a nonnull check.

DEX reveals a hidden R8 API shim: `VungleWebClient.onPageFinished` calls `androidx.appcompat.widget.c0.e(WebView,WebViewRenderProcessClient)`; this calls the native one-argument setter. JADX renders this as a direct setter and omits the synthetic method body. The JSON proof records each hop, source/APK/DEX hashes, actual signatures and three existing source facts. This is a general platform callback family and static wrapper propagation pattern. The overall host remains partial.

Public contract: https://developer.android.com/reference/android/webkit/WebViewRenderProcessClient and https://developer.android.com/reference/android/webkit/WebView#setWebViewRenderProcessClient(android.webkit.WebViewRenderProcessClient) (checked 2026-10-04).
