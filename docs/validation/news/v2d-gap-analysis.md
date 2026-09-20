# v2d callback and Activity-host gap analysis

This audit compares `test/runs/v2c/com.tencent.news/0/output/capabilities.json`
and the later `docs/validation/v2d-news-evaluation.json` with
`canonical-facts.jsonl`, then checks every apparent callback miss against JADX
source and `test/runs/symbols/news.jsonl`. The TencentNewsJsBridge reflection
binding is covered separately in `message-routing.md`.

## Callback dispatch must retain explicit super calls

A callback hierarchy cannot be reduced to one method per descriptor solely by
virtual override shape. The most-derived method is the framework entry point,
but every explicit `invoke-super`/`super.method(...)` is another executed
implementation and should be emitted with its own DEX owner.

For the four custom-browser-family Activities, the installed Chrome client is
`CustomWebBrowserForItemActivity$k`, which extends `CustomWebChromeClient`,
which extends `JavascriptBridgeChromeClient`:

- `CustomWebChromeClient.onJsPrompt` explicitly returns
  `super.onJsPrompt(...)` (`CustomWebChromeClient.java:55-59`). Therefore both
  `Lcom/tencent/news/webview/webchromeclient/CustomWebChromeClient;->onJsPrompt(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/tencent/smtt/export/external/interfaces/JsPromptResult;)Z`
  and
  `Lcom/tencent/news/webview/jsbridge/JavascriptBridgeChromeClient;->onJsPrompt(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/tencent/smtt/export/external/interfaces/JsPromptResult;)Z`
  execute.
- `$k.onProgressChanged` explicitly calls `super`, so
  `Lcom/tencent/news/webview/webchromeclient/CustomWebChromeClient;->onProgressChanged(Lcom/tencent/smtt/sdk/WebView;I)V`
  executes (`CustomWebBrowserForItemActivity.java:782-790`). However that
  method does **not** call its own superclass (`CustomWebChromeClient.java:62-69`).
  Consequently
  `Lcom/tencent/news/webview/jsbridge/JavascriptBridgeChromeClient;->onProgressChanged(Lcom/tencent/smtt/sdk/WebView;I)V`
  is shadowed on this installed path and should not be counted for these four
  Activities. This is a gold overcount, not a v2d miss.

The same distinction applies to `WebDetailActivity$m.onProgressChanged`: it
does not call `super` (`WebDetailActivity.java:577-603`), so the canonical
`JavascriptBridgeChromeClient.onProgressChanged` fact for WebDetail is also a
gold overcount.

The installed View clients are subclasses of `com.tencent.news.ui.view.ht`.
Their overrides of `onPageFinished`, `onPageStarted`, legacy
`onReceivedError`, and `onReceivedSslError` all explicitly call `super`
(`CustomWebBrowserForItemActivity.java:253-350`; `WebDetailActivity.java:629-696`).
Thus the following four `ht` implementations are real executed callbacks and
v2d misses, even though a derived method has the same descriptor:

- `Lcom/tencent/news/ui/view/ht;->onPageFinished(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;)V`
- `Lcom/tencent/news/ui/view/ht;->onPageStarted(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V`
- `Lcom/tencent/news/ui/view/ht;->onReceivedError(Lcom/tencent/smtt/sdk/WebView;ILjava/lang/String;Ljava/lang/String;)V`
- `Lcom/tencent/news/ui/view/ht;->onReceivedSslError(Lcom/tencent/smtt/sdk/WebView;Lcom/tencent/smtt/export/external/interfaces/SslErrorHandler;Lcom/tencent/smtt/export/external/interfaces/SslError;)V`

`WebAdvertActivity` has the same pattern. It installs class `d`
(`WebAdvertActivity.java:1695-1715`); `d.onPageStarted` calls `super`, reaching
`Lcom/tencent/news/tad/business/ui/activity/a;->onPageStarted(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V`
(`d.java:483-490`). This is a real v2d miss.

Reusable rule: build the virtual dispatch target first, then walk explicit
super-call edges for that same callback descriptor. Do not emit every
same-shaped ancestor, and do not discard an ancestor that a reachable override
explicitly calls.

## Editor host and helper chains

Both `QAEditorActivity` and `RichEditorActivity` inherit
`BaseEditorActivity`. `BaseEditorActivity.initView()` retrieves an `REWebView`,
configures it, constructs `BaseEditorActivity$a`, and installs it as the Chrome
client (`BaseEditorActivity.java:889-905`). The real inheritance chain is:

`REWebView -> NewsWebView -> BaseWebView -> RecyclableWebView`.

DEX confirms:

- `Lcom/tencent/news/richeditor/REWebView;` parent
  `Lcom/tencent/news/webview/NewsWebView;`
- `Lcom/tencent/news/webview/NewsWebView;` parent
  `Lcom/tencent/news/webview/BaseWebView;`
- `Lcom/tencent/news/richeditor/REWebView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V`
- `Lcom/tencent/news/webview/NewsWebView;->initView(Landroid/content/Context;)V`
- `Lcom/tencent/news/webview/BaseWebView;->init()V`

This constructor-owned receiver chain explains the nine missing settings per
editor Activity: seven calls in `NewsWebView.initView` plus mixed-content and
DOM-storage calls in `BaseWebView.init`. The extractor found the direct editor
calls but did not propagate the layout/inherited `REWebView` field through both
custom WebView superclass initializers.

There are two distinct callback gaps:

1. `BaseEditorActivity$a` extends `com.tencent.news.richeditor.d`. Its
   `onProgressChanged` explicitly calls `super` (`BaseEditorActivity.java:130-149`),
   reaching
   `Lcom/tencent/news/richeditor/d;->onProgressChanged(Lcom/tencent/smtt/sdk/WebView;I)V`
   (`d.java:184-196`). Shape dedup retained only the derived owner.
2. Resource loading calls
   `Lcom/tencent/news/richeditor/l;->ʽ(Lcom/tencent/news/webview/NewsWebView;Ljava/lang/String;Ljava/lang/String;)V`
   from the `BaseEditorActivity` resource callback (`BaseEditorActivity.java:264-280`).
   It delegates to
   `Lcom/tencent/news/richeditor/l;->ʼ(Lcom/tencent/news/webview/NewsWebView;Ljava/lang/String;)V`,
   which creates `l$a` and calls `setWebViewClient`
   (`l.java:69-87`). The missed implementations are:

   - `Lcom/tencent/news/richeditor/l$a;->onPageStarted(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V`
   - `Lcom/tencent/news/richeditor/l$a;->shouldInterceptRequest(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;)Lcom/tencent/smtt/export/external/interfaces/WebResourceResponse;`

Reusable rule: callback discovery must follow helper calls where the WebView is
a parameter and the client allocation plus `setWebViewClient` occur in the
callee. It must also follow asynchronous interface/callback implementations
whose body invokes that helper.

## Additional Activity-host probe

Five new manifest Activities were independently verified and reserved as
holdout rather than used to guide a repair. They share the source-proven base
chain `Activity -> com.tencent.ilivesdk.webcomponent.activity.BaseWebActivity
-> okweb component/binding -> com.tencent.okweb.webview.BaseWebView`.
`BaseWebActivity.onCreate` builds its component and binds it into the view
container (`BaseWebActivity.java:317-340`); binding callback `c$a` stores the
concrete WebView in the Activity field (`:143-173`). DEX evidence includes:

- `Lcom/tencent/ilivesdk/webcomponent/activity/BaseWebActivity;->onCreate(Landroid/os/Bundle;)V`
- `Lcom/tencent/ilivesdk/webcomponent/activity/BaseWebActivity;->ˆʻ()Lcom/tencent/okweb/framework/component/c;`
- field `Lcom/tencent/ilivesdk/webcomponent/activity/BaseWebActivity;->ʻʿ:Lcom/tencent/okweb/webview/BaseWebView;`
- `Lcom/tencent/okweb/webview/BaseWebView;->init()V`
- `Lcom/tencent/okweb/webview/BaseWebView;->setWebViewClient(Lcom/tencent/smtt/sdk/WebViewClient;)V`

`BaseWebView.init` installs its created client and registers
`__webview_preload` (`com/tencent/okweb/webview/BaseWebView.java:39-58`). This
is a positive WebView capability path independent of global settings or bridge
removal. The five Activity identities and their flat facts remain withheld;
the probe only raises the holdout count to the requested ten.

