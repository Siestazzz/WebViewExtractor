# TencentNewsJsBridge object binding and message routing

APK SHA-256: `c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`.
All descriptors below occur in the independent APK DEX symbol export at
`test/runs/symbols/news.jsonl`; source paths are JADX evidence, not the source of
the descriptors.

## Object binding chain

1. An Activity creates one concrete `H5JsApiScriptInterface` and supplies the
   same object to its Chrome client. `WebDetailActivity.initListener()` creates
   it with the Activity, a `WebViewBridge(mWebView)`, and a
   `WebDetailJsApiAdapter`, then passes `mScriptInterface` to inner client `m`
   (`WebDetailActivity.java:1889-1894`). The real method and fields are:

   - `Lcom/tencent/news/webview/WebDetailActivity;->initListener()V`
   - `Lcom/tencent/news/webview/WebDetailActivity;->mScriptInterface:Lcom/tencent/news/webview/jsapi/H5JsApiScriptInterface;`
   - `Lcom/tencent/news/webview/WebDetailActivity;->mWebView:Lcom/tencent/news/webview/BaseWebView;`
   - `Lcom/tencent/news/webview/jsapi/H5JsApiScriptInterface;-><init>(Landroid/app/Activity;Lcom/tencent/news/webview/api/WebViewBridge;Lcom/tencent/news/webview/jsapi/jsapiadapter/IJsApiAdapter;)V`

2. `WebDetailActivity$m` calls the `JavascriptBridgeChromeClient` constructor
   with that exact object (`WebDetailActivity.java:558-564`). Its constructor
   calls `BaseWebChromeClient(context, interface)` and also creates the prompt
   transport's `new JavascriptBridge(interface)`
   (`JavascriptBridgeChromeClient.java:62-69`):

   - `Lcom/tencent/news/webview/jsbridge/JavascriptBridgeChromeClient;-><init>(Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;Landroid/content/Context;)V`
   - `Lcom/tencent/news/webview/jsbridge/JavascriptBridgeChromeClient;->javascriptBridge:Lcom/tencent/news/webview/jsbridge/JavascriptBridge;`

3. `BaseWebChromeClient` stores the interface in `jsi` and creates
   `WebChromeClientBridge(context, interface)` (`BaseWebChromeClient.java:40-49`).
   That constructor calls `BridgeInterface.bindBridge(interface)`
   (`WebChromeClientBridge.java:86-93`):

   - `Lcom/tencent/news/webview/webchromeclient/BaseWebChromeClient;-><init>(Landroid/content/Context;Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;)V`
   - `Lcom/tencent/news/webview/webchromeclient/BaseWebChromeClient;->jsi:Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;`
   - `Lcom/tencent/news/webview/webchromeclient/WebChromeClientBridge;-><init>(Landroid/content/Context;Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;)V`
   - `Lcom/tencent/news/webview/BridgeInterface;->bindBridge(Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;)V`

4. `bindBridge` obtains the WebView from `interface.getWebViewBridge()`, creates
   `JavascriptBridge(interface)`, wraps it and the WebView in `BridgeInterface`,
   then calls `addJavascriptInterface(..., "TencentNewsJsBridge")`
   (`BridgeInterface.java:29-39`). `BridgeInterface` is therefore created here,
   rather than by either Activity directly:

   - `Lcom/tencent/news/webview/BridgeInterface;-><init>(Lcom/tencent/news/webview/jsbridge/JavascriptBridge;Lcom/tencent/news/webview/api/WebViewBridge;)V`
   - fields `mBridge:Lcom/tencent/news/webview/jsbridge/JavascriptBridge;` and
     `mWebViewBridge:Lcom/tencent/news/webview/api/WebViewBridge;`

5. The only write to `JavascriptBridge.interfaceObj` in this class is its
   constructor assignment `this.interfaceObj = iJsApiScriptInterface`
   (`JavascriptBridge.java:162-169`):

   - `Lcom/tencent/news/webview/jsbridge/JavascriptBridge;-><init>(Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;)V`
   - `Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->interfaceObj:Lcom/tencent/news/webview/jsapi/IJsApiScriptInterface;`

Thus for `WebDetailActivity`, `interfaceObj` is the concrete
`H5JsApiScriptInterface` constructed at line 1889. It is not the adapter. The
adapter is a constructor dependency stored inside that interface object.

## Dispatch rule

The directly annotated transport surface is only
`Lcom/tencent/news/webview/BridgeInterface;->bridgeCall(Ljava/lang/String;)Ljava/lang/String;`
(`BridgeInterface.java:42-49`). It accepts only the
`jsbridge://get_with_json_data` route and forwards its `json` payload through
`JavascriptBridge.bridgeCall` to private `call`.

`call` reads JSON keys `method`, `types`, `args`, and `instanceName`, converts
the declared types/arguments, and resolves the endpoint using
`interfaceObj.getClass().getMethod(method, parameterTypes)`
(`JavascriptBridge.java:193-234,301-310`). Invocation uses that resolved method
on the same `interfaceObj`. Relevant DEX descriptors are:

- `Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->bridgeCall(Lcom/tencent/news/webview/api/WebViewBridge;Ljava/lang/String;I)Ljava/lang/String;`
- `Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->call(Lcom/tencent/news/webview/api/WebViewBridge;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;`
- `Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->parseArgValues(Lcom/tencent/news/webview/api/WebViewBridge;Lorg/json/JSONArray;Lorg/json/JSONArray;Ljava/lang/String;I[Ljava/lang/Class;[Ljava/lang/Object;)Lcom/tencent/news/webview/jsbridge/JavascriptCallback;`
- `Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->findJsApiMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;`

A sound general model is therefore: identify the registered transport method;
trace the constructor argument into the router receiver field; retain the JSON
method name and exact parameter-type selector; and include an endpoint only if
it is a public method on the proven concrete receiver class with the selected
descriptor. Public methods on adapters, managers, or unrelated bridge-looking
classes are not endpoints of this route merely because they are public.

## CustomWebBrowserForItemActivity comparison

`CustomWebBrowserForItemActivity.getScriptInterface()` constructs the same
concrete receiver class, `H5JsApiScriptInterface`, but with a
`CustomWebBrowserForItemJsApiAdapter` (`CustomWebBrowserForItemActivity.java:2157-2160`).
`initWebViewListener()` stores it and passes the same object to inner Chrome
client `k` (`:2315-2327`); `k` calls `CustomWebChromeClient`, which inherits the
same `JavascriptBridgeChromeClient -> BaseWebChromeClient ->
WebChromeClientBridge -> BridgeInterface.bindBridge` chain (`:766-777`). DEX
entry points:

- `Lcom/tencent/news/webview/CustomWebBrowserForItemActivity;->getScriptInterface()Lcom/tencent/news/webview/jsapi/H5JsApiScriptInterface;`
- `Lcom/tencent/news/webview/CustomWebBrowserForItemActivity;->initWebViewListener()V`
- `Lcom/tencent/news/webview/CustomWebBrowserForItemActivity;->mScriptInterface:Lcom/tencent/news/webview/jsapi/H5JsApiScriptInterface;`

It uses the same registration name, transport, router, receiver class, and H5
endpoint set. Its receiver instance, Activity context, WebViewBridge, and
adapter differ. Adapter-only public methods must not be added as reflected H5
endpoints because reflection targets the `H5JsApiScriptInterface` object.

