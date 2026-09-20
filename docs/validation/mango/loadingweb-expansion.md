# LoadingWebActivity expansion

`LoadingWebActivity.getFragment()` always creates `CommonWebFragment`, whose superclass constructs `TimeoutCheckWebView`. The carrier configures 13 observed setting calls (including both repeated `setSupportZoom(false)` calls and the subclass's final `setCacheMode(2)`) and installs the effective WebView clients.

The effective callbacks are five overrides on `FragmentWebLoadingBase$5`, inherited `onJsAlert` and `onJsConfirm` from `JsBridgeWebChromeClient`, and the subclass override of `onJsPrompt`. All eight descriptors are present in the independent DEX export.

The JavaScript transport is `onJsPrompt`, followed by `JsCallJava` and `NativeMethodInjectHelper.findMethod`. The registry key is the injected class simple name, and reflection admits only public static methods with exactly `(WebView, JSONObject, JsCallback, Handler)`. `JSCommondMethod` therefore exposes 14 DEX-confirmed message handlers; `printLog(WebView,String,JsCallback,Handler)` is intentionally excluded. Runtime classes supplied through the `method_class_names` intent extra are retained as one registration with unknown class/name/endpoints and no fabricated signature.

Reproduce with `build_loadingweb_truth.py` before `finalize_canonical.py` in the full pipeline.
