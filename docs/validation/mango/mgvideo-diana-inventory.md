# MGVideoPlayActivity Diana nonblind development facts

This extension was derived from the Mango 9.3.0 decompiled source and independently checked against `test/runs/symbols/mango.jsonl` (APK SHA-256 `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5`). The v11c/v12a tool output was used only to identify the regression; its 64 removed rows were not copied as truth.

`build_mgvideo_diana_truth.py` emits 81 semantic facts for `com.mgtv.ui.videoplay.MGVideoPlayActivity` and appends them idempotently to the unified canonical file. The pre-extension 2,427-row canonical is preserved as `canonical-facts-before-mgvideo-diana-v1.jsonl` (SHA-256 `ef2a0d0573bdb368ef9197a64baa695bfa6a58ded23565261e5a04209a7100ad`). Before adding type constraints, the 2,508-row canonical was preserved as `canonical-facts-before-mgvideo-webview-constraint-v1.jsonl` (SHA-256 `4cd8f9da65c8c3f7c72972b8a1f432507fb3e79747048d4e21697b9658c300cd`). Two consecutive constrained-generator runs produced the same 2,508-row canonical SHA-256 `b22404aed295f50a3e14251da61e6e55b5068281833b13e289e375e35e39f8b2` and development-file SHA-256 `92304b837d6e5dc2876384ca013e69a097dc168311e54750deccd3c25af8e1fc`.

| WebView identity | settings | bridge registrations | bridge members | callback registrations | callback members |
|---|---:|---:|---:|---:|---:|
| `com.mgtv.diana.sdk.page.view.PageWebView` | 20 | 1 | 4 | 5 | 21 |
| `com.mgtv.diana.sdk.service.view.ServiceWebView` | 14 | 1 | 4 | 3 | 8 |

Settings are deduplicated by WebView identity, exact platform API descriptor, and value. Therefore the inherited `setAppCacheEnabled(true)` and `setSavePassword(false)` calls are not repeated for `WebViewContainer.settingWebView`, while later values such as `setUseWideViewPort(true)` and `setCacheMode(-1)` remain separate from the inherited false/2 values. The `setJavaScriptEnabled(false)` fact is the real `BaseWebView.destroy()` transition, not an inferred initial state. Dynamic user-agent content remains `value_kind=dynamic`.

Bridge registration and exposed members are separate facts. Both WebViews register a concrete `com.mgtv.diana.sdk.api.core.JSInterface` as `JSCore`; only its four `@JavascriptInterface` methods are endpoints. Callback setters likewise remain registration facts, and each callback member uses its declaring DEX owner and full descriptor.

All 81 facts now carry `webview_constraint.types` with exactly one independently proven concrete type: the 51 Page facts require `com.mgtv.diana.sdk.page.view.PageWebView`, and the 30 Service facts require `com.mgtv.diana.sdk.service.view.ServiceWebView`. No report-derived subtype was added. This constraint checks runtime WebView type only; it does not prove that two facts refer to the same object instance. Object identity remains part of the source binding audit.

The source binding is:

`MGVideoPlayActivity` event case 9 (`MGVideoPlayActivity.java:9258`) → `y9()` (`:13634`) → `NBFloatFragmentHelper.showMiniApp(String)` (`NBFloatFragmentHelper.java:2139`) → AspectJ closure `t.run(Object[])` → `H1` → `G1` → `F1` (`:623`) → `MiniAppFragment.newInstance(Bundle)` → `n1(Fragment)` stores DEX field `h` (`:2049`) → `MiniAppFragment.onViewCreated` (`MiniAppFragment.java:261`) → `DianaView.renderMiniApp(FrameLayout)` (`DianaView.java:731`). `renderMiniApp` constructs `AppService` (`:763`), whose `createJsEngine` constructs the ServiceWebView; the game/page path constructs `WebViewContainer` and its PageWebView (`GamePage.java:656`, `WebViewContainer.java:533`). `O0()` reads the same field `h` and calls `MiniAppFragment.onBackPressed()`, independently confirming the stored fragment identity.

The exact helper/closure descriptors and field-name mapping are in `nbfloat-fragment-helper-dex.json`. Source names `f88158a`, `f88159b`, and `f88165h` correspond to DEX fields `a`, `b`, and `h` respectively.

The earlier Erlang pruning remains valid. `ErlangSingClashH5Fragment` does not set `RouterConfig.INTENT_WEB_USE_NESTED`; `WebUIFragment.initWebView` follows its false default and constructs `ImgoWebView`. The 20 removed NestedImgoWebView alternatives are infeasible, while the same capabilities remain on the actual ImgoWebView. This development extension does not restore those alternatives.

The dataset is explicitly nonblind (`mango-mgvideo-diana-nonblind-development`). It adds source truth for regression testing and does not claim evidence from a sealed holdout.
