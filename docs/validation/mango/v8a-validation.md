# v8a Mango validation

v8a emits 85 Activities. Relative to v7a it adds `com.mgsz.detail.ui.AntiqueDetailActivity`, `com.mgtv.digital.ui.DigitalDetailActivity`, and `com.mgtv.digital.ui.DigitalModelViewActivity`, with no removal. All three already have independently reviewed valid v4d chains through `MgNftViewer -> NftWebviewLayout`. The other 82 hosts are identical to v7a/v6, so their prior verdicts remain reusable. The updated ownership result is 80 valid and five uncertain; all rows are in `v8a-ownership.jsonl`.

The four new WebContainer misses are real settings on its XML-created WebView. The bound view is `com.mgsz.h5.ImgoWebView`, and layout inflation selects the DEX constructor `Lcom/mgsz/h5/ImgoWebView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V`. Its superclass chain reaches `Lcom/mgsz/h5/browser/RootWebView;-><init>(Context,AttributeSet)V`, which calls `RootWebView.init(Context)`. That method obtains `WebSettings settings = getSettings()` and passes that exact value as parameter 0 to `RootWebView.supportHtml5(WebSettings)`.

`supportHtml5` contains the four missing calls:

- Offset 1: `WebSettings.setGeolocationEnabled(true)`.
- Offset 4: `WebSettings.setDatabaseEnabled(true)`.
- Offset 52: `WebSettings.setGeolocationDatabasePath(getContext().getDir("geolocation",0).getPath())`.
- Offset 55: `WebSettings.setDatabasePath(getContext().getDir("databases",0).getPath())`.

The first lost analysis edge is therefore the context-bound call from `RootWebView.init(Context)` into `supportHtml5(WebSettings)`, including propagation of the `getSettings()` result into helper parameter 0. Removing a context-free `WebSettings` helper entry seed appropriately removes the old standalone `entry_parameter` route, but the helper remains reachable from the XML constructor and should be analyzed through that invocation. A repair can follow this actual caller edge without reintroducing global helper seeds.

This setting path is separate from the Imgo programmatic bridge path. v8a still emits no `registerHandler`/`registerWebHandler` facts for WebContainer. On the XML path, `K0` remains null and the guarded registration list returns immediately. Recovering the four superclass settings must not connect the XML receiver to `Lcom/mgsz/h5/ImgoWebView;-><init>(Context,ImgoWebJavascriptInterface)V` or restore its programmatic bridge registrations.

The machine-readable companion records the precise constructors, helper signature, values, offsets, ownership delta, and negative bridge guard. No holdout material was read.
