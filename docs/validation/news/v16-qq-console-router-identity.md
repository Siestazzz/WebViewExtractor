# Tencent OpenSDK console router identity supplement

The existing `qq-console-rule.json` already contains the registration API, map field, console transport, reflection behavior, and all nine endpoint descriptors needed for the 20 public Dialog bridge facts. The missing implementation detail is the complete instance-alias tuple from Dialog construction through client installation and dispatch.

Both `c(Context)` constructors allocate one `c$1(this)`, store that exact object in `c.mChromeClient`, and store the Dialog in `c$1.a`. `TDialog.onCreate(Bundle)` first calls `c.onCreate(Bundle)`, which allocates one router and writes it to `c.jsBridge`. It then calls `TDialog.a()`, which constructs one `com.tencent.open.d.b`, writes it to `TDialog.i`, adds that same object to container `h`, and installs `h` with `Dialog.setContentView`.

`TDialog.b()` performs both relevant bindings on those already-created instances. It installs `this.mChromeClient` on `this.i`, then constructs `JsListener(this)` and calls `this.jsBridge.a(listener,"sdk_js_if")`. The listener constructor writes the same Dialog to `JsListener.this$0`. Router registration writes the exact listener to `b.a:HashMap` under `sdk_js_if`.

A modern console event is valid only through the installed client:

`c$1.onConsoleMessage(ConsoleMessage)` → field `c$1.a` virtual `onConsoleMessage(String)` → `TDialog.onConsoleMessage(String)` → field `TDialog.jsBridge.a(TDialog.i,message)`.

Thus the registration receiver and dispatcher receiver are the same router object, while the installation receiver, UI child, and dispatcher WebView argument are the same `TDialog.i` object. The legacy three-argument console callback only logs.

The implementation key is the tuple `(Dialog, router, installed client, WebView, listener, registration name)`. Matching only `sdk_js_if`, handler class, client class, Dialog class, or WebView type can mix separate displayed Dialogs. Likewise, constructing another `c$1`, router, listener, or callback is not enough: the client must be installed, the listener must be registered on the dispatching router, and the reached callback must pass the exact installed WebView to that router.

Reflection remains as documented: `getDeclaredMethods()`, match by name and arity, decoded String arguments, no `setAccessible`, inherited methods excluded, and an invoke ladder of zero through six arguments. `JsListener` exposes exactly nine DEX members listed in the JSON companion. `customCallback()` remains false, so endpoint return values are not transported.

All field and method descriptors in the companion were checked against `test/runs/symbols/news.jsonl`. This is a narrow identity supplement; it does not change canonical facts and does not use sealed holdouts.
