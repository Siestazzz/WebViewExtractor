# Tencent share Dialog development capability inventory

## Scope and status

This inventory deeply expands two source-confirmed conditional Dialog hosts: `MobileQQActivity` and `QzoneShareActivity`. Both were seen in earlier extractor output, so these are **non-blind development samples** and cannot count as fresh final holdout cases.

The facts remain separate in `dialog-development-facts.jsonl`; they are not merged into `canonical-facts.jsonl`, preserving v7 comparability. If the independent facts are later accepted and merged, the public deeply verified candidate count would rise from 28 to 30. Until that merge and review, the accepted public canonical count remains 28; ownership proof alone is not counted as deep verification.

## Conditional host bindings

`MobileQQActivity` passes its exact `this` instance through `news.share.channel.b`, `Tencent.shareToQQ`, and `QQShare.shareToQQ`. When native QQ sharing is unsupported, QQShare constructs `TDialog(activity, ...)` and invokes `show()`.

`QzoneShareActivity` passes its exact `this` through channel `c`, `Tencent.shareToQzone`, and `QzoneShare.shareToQzone`. Depending on installed QQ version, the SDK either displays `TDialog` directly or enters the QQShare compatibility fallback with the same Activity and displays it there.

`TDialog.onCreate` invokes `a()`, which constructs `com.tencent.open.d.b`, stores it in field `TDialog.i`, adds it to the Dialog container, and calls `setContentView`. `TDialog.b()` configures that same field. The stable WebView identity in every fact is `Lcom/tencent/open/TDialog;->i:Lcom/tencent/open/d/b;`.

## Complete capability surface

Each Activity has 30 expanded facts:

- one message-bridge registration named `sdk_js_if`;
- nine reflection-routed message endpoints declared by `TDialog.JsListener`;
- fourteen WebSettings calls, including the five settings applied by `com.tencent.open.web.a` and the nine calls in `TDialog.b()`;
- four `TDialog.FbWebViewClient` overrides;
- two `com.tencent.open.c$1` WebChromeClient overrides.

The bridge is not `addJavascriptInterface`. `com.tencent.open.c$1.onConsoleMessage(ConsoleMessage)` forwards console text to `TDialog.onConsoleMessage`, which calls `com.tencent.open.b.a(WebView,String)`. That router accepts the `jsbridge` URI scheme, selects registry object `sdk_js_if`, and dispatches by method name and argument count over `JsListener.getDeclaredMethods()`. Only the nine methods actually declared on `JsListener` are included.

Settings use canonical value semantics: booleans and integers are literal, `WebSettings.RenderPriority.HIGH` is an enum, and the database path is dynamic with `value: null` plus its source expression. The database calls are conditional on the Dialog's weak Context being non-null.

All 30 method-bearing facts per host were checked against `test/runs/symbols/news.jsonl`; no DEX method descriptor is missing. Platform WebSettings APIs use their fully qualified Android descriptors.

The ownership definition and correction history remain documented in `v7a-share-dialog-supplement.md`. No sealed holdout identity or fact was read.

## Reuse metadata and v7 development replay

Every JSONL fact records application version `7.9.50`, validator `GPT-5.6 Sol`, evidence status `source_verified_dex_verified`, and an explicit Activity-to-Dialog-to-WebView `binding_chain`. Existing source evidence and `non_blind` status are retained.

The separate v7 development replay is recorded in `docs/validation/v7-news-dialog-development-evaluation.json`: all 28 settings and all 12 callbacks matched, while all 20 bridge facts missed. The bridge misses are a framework gap for the real console-message transport (`onConsoleMessage` → `TDialog.onConsoleMessage` → `com.tencent.open.b` URI router → `sdk_js_if` reflection endpoints), rather than evidence against the source facts.
