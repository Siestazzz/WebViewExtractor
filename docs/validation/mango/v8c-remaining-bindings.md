# v8c remaining Pangle and mgadplus bindings

The v8c Activity set is identical to v8a: 85 hosts, with no addition or removal.

## Pangle: three `setBlockNetworkImage` value misses

All three receivers are already bound correctly. `TTWebPageActivity` uses field `f15161z: SSWebView`; `TTVideoWebPageActivity` uses `f15095z: SSWebView`; `TTVideoScrollWebPageActivity` inherits the latter configuration. v8c reaches `Lcom/bytedance/sdk/openadsdk/core/widget/c/tn;->c(Landroid/webkit/WebView;)V@72` on those WebViews but emits the argument as `unknown`.

The call is `settings.setBlockNetworkImage(true ^ this.e)`. The configurator is allocated by static factory `tn.c(Context)`, whose constructor initializes `e:Z` to true. The landing path is `z.onPageStarted -> tn.c(context).c(true).c(webView)`. The video path is `tn.c(context).c(true).tn(false).c(f15095z.getWebView())`, and the scroll host inherits it. `c(true)` writes field `pb`; `tn(false)` writes field `zx`; neither writes `e`. The setting value is therefore false.

The earliest lost edge is instance state, not WebView ownership: the factory return and return-this fluent calls retain one `tn` allocation, but the analysis loses its initialized `e=true` before the final `c(WebView)` read. The reusable rule is to preserve allocation identity through static factories and fluent methods returning `this`, apply each setter's field effect, and retain defaults for untouched fields. This must remain object-sensitive so unrelated `tn` instances do not merge.

## mgadplus: seven custom lifecycle callback members

`CustomWebActivity.onCreate` stores `findViewById(R.id.webViewPage)` in field `E: ImgoAdWebView`. `CustomWebActivity.h(String)` calls `E.setWebViewLifeCycleCallback(new CustomWebActivity$c())`. The exact setter is `Lcom/mgadplus/brower/ImgoAdWebView;->setWebViewLifeCycleCallback(Lcom/mgadplus/brower/g;)V`; it stores parameter 0 in `W:g`. Independent DEX contains all seven expected `CustomWebActivity$c` descriptors listed in the JSON companion.

The earliest lost edge is this custom setter/store. It is not an Android `setWebViewClient` or `setWebChromeClient` API, so v8c does not treat the newly constructed `CustomWebActivity$c` as an installed callback target even though the Activity receiver and setter call are concrete. Dispatch evidence continues through `ImgoAdWebView.W`: custom-scheme, page start/finish, legacy error, HTTP error, and title paths forward to that field. The registered concrete target also overrides `onProgressChanged`; no direct `W.onProgressChanged` forwarding call was found in the inspected dispatcher, so the registration is proven while that individual runtime invocation path remains unobserved.

A reusable rule can recognize a custom listener setter when its callback-typed parameter is stored into a receiver field. It should bind a concrete `new` argument through the Activity-owned receiver and enumerate the concrete class overrides of the declared callback base or interface. Attribution still requires the setter call on the host-bound WebView; class existence elsewhere in the APK is insufficient.

Loading/RainbowBridge is intentionally omitted here because its stateful registration rule is already documented separately. No sealed holdout material was read.
