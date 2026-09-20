# v14b Privacy single-host validation

The v14b probe restores the targeted chain for `PrivacyWebActivity`. Both bridge registrations use the same XML receiver, `activity:...PrivacyWebActivity/view:2131303140`, typed `X5WrapperWebView`. The XML evidence points to `res/88e.xml`, the obfuscated APK path for source layout `init_web_browser_layout`, and resource ID `2131303140` (`web_detail_webview`).

The recovered dispatch chain is correct. `PrivacyWebActivity.parseIntent()` invokes the signature declared by its parent-typed field, `smtt.WebView.loadUrl(String)`. The actual XML allocation selects `X5WrapperWebView.loadUrl(String)`. Its `invoke-super` reference names `smtt.WebView`, but Java/Dalvik super dispatch must select the nearest implementation in the actual superclass chain, `DtX5WebView.loadUrl(String)`. That method calls the smtt implementation and then `DtX5WebView.injectBridge()`.

`DTJsBridgeInterface` registers a `BridgeInterface` object. Its complete JavaScript-exposed surface is `bridgeCall(String): String`, DEX signature `Lcom/tencent/qqlive/module/videoreport/inject/webview/jsbridge/v1/BridgeInterface;->bridgeCall(Ljava/lang/String;)Ljava/lang/String;`.

`dtBridge` registers a `JsBridgeInterfaceV2` object. Its complete exposed surface is `postMessage(String): String`, DEX signature `Lcom/tencent/qqlive/module/videoreport/inject/webview/jsbridge/v2/JsBridgeInterfaceV2;->postMessage(Ljava/lang/String;)Ljava/lang/String;`. `dispatchVisibilityChange(boolean)` is public but lacks `@JavascriptInterface` and is not exposed.

Both registrations require `JsBinderHelper.allowInjectOnLoad()`. The v1 registration additionally requires `mIsJsInterfaceInject == false`; the v2 registration requires `mIsJsInterfaceV2Inject == false`. The flags make registration once-per-instance under the normal path. Candidate status remains appropriate because the XML/configuration and PatchRedirector branches are not proven to co-occur in every execution.

Two representation issues remain. The Activity `loadUrl` site is emitted twice: an older parent-typed `smtt.WebView` union and a replayed union containing the concrete XML type. They describe one call on one allocation. Both also retain a PatchRedirector-return alternative typed `smtt.WebView`, so the union remains unknown even though the known XML alternative is sufficient to follow the conditional override chain.

This result validates only the frozen single-host development probe. It does not establish `PrivacyWebItemActivity` or full-APK behavior.
