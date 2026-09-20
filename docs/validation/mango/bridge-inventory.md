# Bridge and framework inventory

## Message bridge

- `com.hunantv.imgo.h5.jsbridge.BridgeWebView` registers JavaScript object `jsobj` at `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/h5/jsbridge/BridgeWebView.java:223`.
- Complete annotated exposure in its registered object: `public String callNative(String str, String str2, String str3)` at lines 63–64. The bridge dispatches by the first argument to `messageHandlers`; `registerHandler(String, BridgeHandler)` is declared at line 413. This documents signatures and binding only.
- `ImgoWebView` registers 138 named handlers through `registerWebHandler()` at lines 529–673. `ImgoWebJavascriptImpl$e0.handler()` reflects `getDeclaredMethod(registrationName, String.class)` at `ImgoWebJavascriptImpl.java:732-758`. Constant-backed names are resolved to their source values; only exact DEX `(String)V` targets populate `bridge_method`.

## XWeb message bridge

- `com.hunantv.imgo.xweb.jsbridge.BridgeWebView` registers `new JSInterface(iBridge)` under `jsobj` at `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/xweb/jsbridge/BridgeWebView.java:292`.
- Complete annotated exposure in `com.hunantv.imgo.xweb.jsbridge.JSInterface`: `public void callNative(String str, String str2, String str3)` at `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/xweb/jsbridge/JSInterface.java:18-19`.

## Wrapper / factory / manager

- Wrapper: `BaseWebActivity` constructs `ImgoWebView` at `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/browser/BaseWebActivity.java:1639`.
- Wrapper: `BaseWebView` / `ProgressWebView` carry `com.imgo.webbase.jsbridge.BridgeWebView` for wallet pages.
- Factory: `WebUIActivity`, `XWebActivity`, and browser Activities construct or attach their corresponding fragments; evidence is in `inventory.md`.
- Manager: `ImgoWebView` owns settings, clients, handler registration, and lifecycle callbacks. `XWebView` is the X5-side manager.

## Annotated third-party bridge

- CCB registers object name `javaObj` at `test/decompiled/com.hunantv.imgo.activity/sources/com/ccb/ccbnetpay/H5PayActivity.java:206`; its distinct annotated members are `sdkCallBack` and `showFinish` at lines 100–115.
- Wallet DSBridge uses `_dsbridge`-family plumbing in `com.mgtb.money.web.dsbridge.DWebView`; its annotated signatures are held for the final holdout pass.
