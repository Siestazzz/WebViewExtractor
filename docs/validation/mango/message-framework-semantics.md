# Message framework semantics

The four JSON files describe reusable framework rules rather than host lists. Each separates transport installation, registry mutation, dispatch, target flow, and member eligibility.

- Loading uses `onJsPrompt`, parses a class and method route, then reflects only public static four-argument methods from classes committed through `NativeMethodInjectHelper`.
- Mglive and mgadplus install WebView clients that bootstrap and drain `WebViewJavascriptBridge` queues. Their registration helpers store the passed handler object by route name; the exposed member is the concrete two-argument handler implementation.
- Land combines an annotated `jsobj.callNative` transport with the same named-handler map pattern. The first JavaScript argument selects the registered route.

`WebContainerActivity` is absent from both v3f and v4 `activities`, so its 67 misses are an ownership discovery failure rather than capability extraction after host entry. The shortest required index chain is:

`WebContainerActivity.initData()` → field `S:Lcom/mgsz/h5/WebViewFragment;` from `WebViewFragment.pc(String,boolean)` → Fragment transaction → `WebViewFragment.Tb()`/generated binding inflate → binding field `webview:Lcom/mgsz/h5/ImgoWebView;`.

A general fix needs Activity-to-Fragment return/value flow plus generated ViewBinding field typing. Once the binding field is associated with the Fragment, ordinary constructor/superclass analysis reaches `RootWebView -> BridgeWebView -> ImgoWebView`.
