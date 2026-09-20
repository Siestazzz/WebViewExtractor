# Message bridge signature and ownership evidence

## Concrete chain

1. `BridgeWebView` calls `addJavascriptInterface(new JsObject(), "jsobj")` at `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/h5/jsbridge/BridgeWebView.java:223`.
2. The JavaScript-visible transport member is `BridgeWebView.JsObject.callNative(String,String,String):String` at lines 63–64. Its normalized signature is `Lcom/hunantv/imgo/h5/jsbridge/BridgeWebView$JsObject;->callNative(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;`.
3. The first argument selects `messageHandlers.get(str)` at line 68, so this is a WebView message channel rather than an unrelated registry that happens to use the same method name.
4. `ImgoWebJavascriptInterface.registerHandler` has Java signature `void registerHandler(String, ImgoWebView)` at `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/h5/callback/ImgoWebJavascriptInterface.java:211`. Normalized: `Lcom/hunantv/imgo/h5/callback/ImgoWebJavascriptInterface;->registerHandler(Ljava/lang/String;Lcom/hunantv/imgo/h5/ImgoWebView;)V`.
5. Runtime owner `com.mgtv.h5.ImgoWebJavascriptImpl` implements it at lines 5115–5116 and installs `new e0(name, webView)` through `BridgeWebView.registerHandler(String, BridgeHandler)`.
6. `ImgoWebJavascriptImpl.e0` implements `BridgeHandler.handler(String, CallBackFunction):void` at lines 732–762. Normalized: `Lcom/mgtv/h5/ImgoWebJavascriptImpl$e0;->handler(Ljava/lang/String;Lcom/hunantv/imgo/h5/jsbridge/CallBackFunction;)V`.
7. The handler resolves `ImgoWebJavascriptImpl.class.getDeclaredMethod(name, String.class)` and invokes it at lines 754–755. Therefore a confirmed final bridge member has owner `Lcom/mgtv/h5/ImgoWebJavascriptImpl;`, registered name `name`, argument descriptor `(Ljava/lang/String;)`, and return `V`.

## Reusable mapping rule

A registration call does not by itself prove a final exposed member signature. Record three separate facts:

1. Transport exposure: prove `addJavascriptInterface(object, namespace)` and enumerate annotated public members on the concrete object owner.
2. Message registration: prove that an annotated transport member dispatches a message name into the same registry populated by the registration call. If no such data flow exists, classify it as an ordinary registry rather than a message bridge.
3. Final member resolution: follow the registered handler object. When it reflectively calls `Target.getDeclaredMethod(name, T1.class, ...)`, resolve the constant registration name against an actual declared method on `Target` with exactly those parameter descriptors and return type. Emit that DEX signature as `bridge_method`. Keep constant-backed names or missing target members unknown.

Do not derive `(String)void` merely from a matching registration name. The reflection parameter array, target owner, and declared target method must all agree.

