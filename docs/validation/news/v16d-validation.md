# Tencent News v16d failed-report audit

The frozen v16d News report is not a completed result. Its top-level status is `failed`; it processed 2,233 of 2,329 manifest Activities and then threw `NullPointerException` at `CapabilityEngine.inflateLayout:272`. A dynamic `attachToRoot` value produced a null `Long` which was implicitly unboxed. The file contains 34 emitted hosts and 4,242 facts, but host absence and removals after this failure are not valid semantic results.

For traceability, `v16d-partial-delta-from-v15a.json` and `v16d-partial-delta-from-v16a.json` retain the mechanical partial comparisons with an explicit invalid-for-full-comparison flag. `v16d-partial-ownership.jsonl` contains the 34 emitted rows using historical source verdicts and is explicitly not a complete v16d ownership acceptance. Current-canonical recall is not scored against this failed report.

## APWebJSBridge completed-host result

`APWebJSBridgeActivity` was processed before the crash and returns with 29 raw facts. The source-confirmed concentrated surface is restored: 14 settings and four concrete client installations across the system and X5 branches. Those four installations carry the expected 12 concrete callback override facts in scoring.

The binding is correct. `APWebJSBridgeActivity.onCreate(Bundle)` calls `initWebPage()`, which writes a concrete `APSystemWebPage` or `APX5WebPage` into field `webPage:IAPWebPage`. A reached call to `IAPWebPage.initUI(Activity)` or `toPureH5Pay(Activity,APMidasBaseRequest)` dispatches to that concrete object. The page obtains its layout WebView and passes it as constructor parameter 1 to `APWebView` or `APX5WebView`. Each wrapper constructor writes that exact argument to `mWebview` and directly invokes its private `InitWebView()`. The initializer applies settings and installs its `mChromeClient` and `mWebViewClient` fields on the same WebView. This validates the restored settings and installed-client callbacks without treating client construction as callback execution.

Compared with v15a, the settings and client tuples are receiver-identity replacements rather than missing surface. The remaining raw difference is in duplicated load-operation aliases and is not part of the AP26 concentrated scoring gap.

## Editor Lazy completed-host result

Both `QAEditorActivity` and `RichEditorActivity` were processed, but each remains at 51 facts, identical to v16a. The expected editor Lazy settings/client subset is not restored. In particular, the two source-confirmed `richeditor.l$a` WebViewClient installations per Activity remain absent alongside the Lazy-reached initializer settings.

The actual registration entry is:

`Lcom/tencent/news/activity/BaseEditorActivity;-><init>()V` allocates `Lcom/tencent/news/activity/d;-><init>(Lcom/tencent/news/activity/BaseEditorActivity;)V`, then passes that exact initializer to `Lkotlin/j;->ʼ(Lkotlin/jvm/functions/a;)Lkotlin/i;`. The returned concrete Lazy object is stored in `Lcom/tencent/news/activity/BaseEditorActivity;->ʻʼ:Lkotlin/i;`.

The consumer is `Lcom/tencent/news/activity/BaseEditorActivity;->ˊˏ()Lcom/tencent/news/richeditor/REWebView;`: it loads that exact field, invokes `Lkotlin/i;->getValue()Ljava/lang/Object;`, and casts the result to `REWebView`. The initializer body `Lcom/tencent/news/activity/d;->invoke()Ljava/lang/Object;` calls `Lcom/tencent/news/activity/BaseEditorActivity;->ˊˆ(Lcom/tencent/news/activity/BaseEditorActivity;)Lcom/tencent/news/richeditor/REWebView;` on its captured host. `BaseEditorActivity.initView()` consumes the accessor result in `richeditor.l.ʻ(REWebView)`, constructs the editor chrome client with that same value, changes its user agent, and installs the client.

The remaining missing relation is instance identity across factory input and result: initializer object → concrete Lazy implementation returned as `kotlin.i` → Activity lazy field → the same object's `getValue()` cache/result → typed `REWebView`. Recognizing the initializer body alone is insufficient if its result is not returned through the exact Lazy instance and accessor. The rule must not invoke an arbitrary constructed `Function0`; it must require the reached Lazy factory, exact stored Lazy receiver, and reached `getValue()` consumer.

## Deferred findings

The apparent loss of UserSearch, Read24Hours, VideoPreview, and other hosts cannot be classified from v16d because the report terminated early. The same applies to all other apparent removals versus v15a/v16a. Their existing source ownership and regression records remain in force until a successful rerun. No canonical or production files were changed, and sealed holdouts were not read.
