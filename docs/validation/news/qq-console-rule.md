# Tencent OpenSDK console-message bridge rule

This rule models the public `TDialog` samples in Tencent News 7.9.50. It was verified against source and `test/runs/symbols/news.jsonl`; it does not use sealed holdout data.

`com.tencent.open.c.onCreate` creates one `com.tencent.open.b` router per Dialog and stores it in inherited field `c.jsBridge`. `TDialog.b()` registers a newly constructed `TDialog.JsListener` under `sdk_js_if` by calling `b.a(b$b, String)`. For that instance method, argument 0 is the handler object and argument 1 is the registration name. The method writes `router.a.put(name, object)` into the router's `HashMap`.

The same `TDialog.b()` installs inherited `c.mChromeClient` on `TDialog.i`. The modern `c$1.onConsoleMessage(ConsoleMessage)` forwards `ConsoleMessage.message()` through the outer `c` reference to virtual `TDialog.onConsoleMessage(String)`. That override invokes the same Dialog's `jsBridge.a(this.i, message)`. In dispatcher `b.a(WebView,String)`, argument 0 is therefore exactly `TDialog.i`, and the receiver is exactly the router that owns the registration map. The legacy three-argument console callback only logs and is not a transport.

The dispatcher accepts the `jsbridge` URI scheme, parses registration name and method name from URI path positions 2 and 3, URL-decodes the remaining arguments, looks up the handler in the receiver router's map, and calls `b$b.call`. Its callback object holds a weak reference to the dispatcher WebView argument and uses only that WebView for JavaScript result callbacks.

Reflection uses `getClass().getDeclaredMethods()`. It selects by method name and parameter count only. Inherited methods are excluded. Every supplied value is a decoded `String`; there is no parameter-type conversion. Same-name/same-arity overloads are ambiguous, non-public methods are not made accessible, and reflection errors return the bridge's error callback. Although the invoke ladder can pass zero through six strings, a larger selected arity fails because only six values are supplied.

A non-void return is delivered only when `customCallback()` is true. The base implementation returns false and `TDialog.JsListener` does not override it. Its nine real endpoints all return void and are listed with exact DEX descriptors in the JSON rule.

Registration with an existing name replaces that entry through `HashMap.put`. This implementation has no unregister or clear method. Each new Dialog gets a fresh router in `c.onCreate`; disposal ends the instance lifetime.

The binding key must be **router object identity plus registration name**. A valid endpoint binding requires these aliases on one Dialog instance:

- registration receiver equals `TDialog.jsBridge`;
- dispatcher receiver equals the same `TDialog.jsBridge`;
- dispatcher WebView argument 0 equals the same `TDialog.i`;
- the registered `JsListener.this$0` equals that TDialog.

Registration name, Dialog class, or handler class alone must never merge registries. Two displayed Dialog instances with `sdk_js_if` still own separate router maps, listener objects, and WebViews.
