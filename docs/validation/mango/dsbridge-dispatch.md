# DSBridge dispatch semantics

This rule describes the framework independently of any Activity name. A host is connected by proving the concrete `DWebView` instance and every object passed to `addJavascriptObject` on that instance.

## Transport and registry

`DWebView.d()` registers `DWebView$InnerJavascriptInterface` as Android JavaScript object `_dsbridge` at `DWebView.java:814-831`. Its sole DEX-confirmed annotated entrypoint is:

`Lcom/mgtb/money/web/dsbridge/DWebView$InnerJavascriptInterface;->call(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;`

`addJavascriptObject(Object, String)` stores the supplied object in `javaScriptNamespaceInterfaces`; a null namespace is normalized to the empty string (`DWebView.java:754-760`). `e(String)` splits an incoming name at the last dot into namespace and method (`DWebView.java:853-862`). A name without a dot selects the empty namespace.

## Reachability rule

`InnerJavascriptInterface.call()` obtains the registered object, then tries these public reflection shapes in order (`DWebView.java:117-179`):

1. `target.getClass().getMethod(method, Object.class, com.mgtb.money.web.dsbridge.a.class)` for an asynchronous endpoint.
2. `target.getClass().getMethod(method, Object.class)` for a synchronous endpoint.

The returned method must carry `android.webkit.JavascriptInterface`; otherwise dispatch rejects it. `getMethod` also means the method must be public, while allowing an inherited public method. The canonical endpoint is the concrete DEX descriptor on the registered target type, not `addJavascriptObject` and not `_dsbridge.call`.

## Wallet object edges

The common transport installs internal namespace `_dsb` using an anonymous `DWebView$1` target at `DWebView.java:625-742`. Its five DEX-confirmed annotated endpoints are `closePage(Object)`, `disableJavascriptDialogBlock(Object)`, `dsinit(Object)`, `hasNativeMethod(Object)`, and `returnValue(Object)`.

The wallet carrier constructs `new DefaultWebBridgeAPI(mActivity, this)` and registers it with a null namespace at `ProgressWebView.java:271-273`. The field chain is:

`ThirdWebActivity | ThirdFullWebActivity → WebFragment.W → ProgressWebView → DWebView.javaScriptNamespaceInterfaces[""] → DefaultWebBridgeAPI`

The constructor descriptor is:

`Lcom/mgtb/money/web/webview/DefaultWebBridgeAPI;-><init>(Lcom/mgtb/common/config/ConfigActivity;Lec1/a;)V`

Its 42 public methods with `JavascriptInterface` in the independent DEX export satisfy one of the two reflection shapes. Together with five `_dsb` endpoints and `_dsbridge.call`, this explains the 48 bridge facts per wallet host without embedding either host in the generic dispatch rule.

Machine-readable form: `docs/validation/mango/dsbridge-dispatch.json`.
