# Development-set expansion: two independently verified Activities

These two Activities are additional development examples. They are separate
from the ten reserved holdout Activities. Their complete normalized facts are
generated through reusable groups in `build_groups.py` and exported in
`canonical-facts.jsonl`.

## AdGyrosEasterEggActivity

`AdGyrosEasterEggActivity.createContentView` fetches or creates a
`WebEastEggController`, calls `bind`, and falls back to its own `createWebview`
path (`AdGyrosEasterEggActivity.java:121-129`). The fallback calls
`AdDrawGestureManager.createWebview(controller, this)` and binds the same
controller (`:58-67`). The concrete controller is
`InteractiveEastEggController`; its `bindWebview` installs itself as the
Android `WebViewClient` (`InteractiveEastEggController.java:135-160`).

Verified DEX chain:

- `Lcom/tencent/ams/adcore/gesture/AdGyrosEasterEggActivity;->createWebview(Lcom/tencent/ams/adcore/interactive/toolbox/WebEastEggController;)Lcom/tencent/ams/adcore/interactive/toolbox/EasterEggWebView;`
- `Lcom/tencent/ams/adcore/gesture/AdDrawGestureManager;->createController(Landroid/content/Context;)Lcom/tencent/ams/adcore/interactive/toolbox/WebEastEggController;`
- `Lcom/tencent/ams/adcore/gesture/AdDrawGestureManager;->createWebview(Lcom/tencent/ams/adcore/interactive/toolbox/WebEastEggController;Landroid/content/Context;)Lcom/tencent/ams/adcore/interactive/toolbox/EasterEggWebView;`
- `Lcom/tencent/ams/adcore/interactive/toolbox/InteractiveEastEggController;->bind(Landroid/content/Context;Ljava/lang/Boolean;Lcom/tencent/ams/adcore/gesture/AdGestureInfo;Lcom/tencent/ams/adcore/interactive/toolbox/WebEastEggController$WebEastEggListener;)Lcom/tencent/ams/adcore/interactive/toolbox/EasterEggWebView;`
- `Lcom/tencent/ams/adcore/interactive/toolbox/InteractiveEastEggController;->bindWebview(Lcom/tencent/ams/adcore/interactive/toolbox/EasterEggWebView;)V`
- `Lcom/tencent/ams/adcore/interactive/toolbox/EasterEggWebView;->init()V`

The resulting 17 facts comprise 11 settings, three callback implementations,
one bridge registration, and both annotated bridge methods. The registered
name is `_interactBridge`; the real bridge owner is
`Lcom/tencent/ams/adcore/interactive/toolbox/EasterEggWebView$EasterEggBridge;`,
with methods:

- `->invoke(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V`
- `->on(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;`

## AdMosaicXiJingPageActivity

The Activity constructs `com.tencent.news.tad.business.ui.mosaic.a` as its DSDK
custom ability provider (`AdMosaicXiJingPageActivity.java:940-948`). The
provider returns `new mosaic.e(context)` only when the context resolves to this
Activity (`mosaic/a.java:44-55`). `mosaic.e` constructs its `AdWebView`, applies
the `AdWebView` and `BaseSysWebView` settings, then installs concrete Chrome
client `mosaic.d` and View client `mosaic.f` (`mosaic/e.java:303-345`).

Verified DEX chain:

- `Lcom/tencent/news/tad/business/ui/activity/AdMosaicXiJingPageActivity;->onCreate(Landroid/os/Bundle;)V`
- `Lcom/tencent/news/tad/business/ui/mosaic/a;-><init>(Landroid/content/Context;Lcom/tencent/news/tad/common/data/AdOrder;)V`
- `Lcom/tencent/news/tad/business/ui/mosaic/a;->getDKWebView(Landroid/content/Context;)Lcom/tencent/ams/dsdk/view/webview/DKWebView;`
- `Lcom/tencent/news/tad/business/ui/mosaic/e;-><init>(Landroid/content/Context;)V`

The complete group has 56 facts: 15 settings, 13 executed callback
implementations including explicit superclass calls, one prompt-message bridge,
and 27 routed handlers.

The prompt transport is
`Lcom/tencent/ams/adwebview/adapter/client/AdCoreJsWebChromeClient;->onJsPrompt(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsPromptResult;)Z`.
It calls
`Lcom/tencent/ams/adcore/js/AdCoreJsBridge;->invokeJavascriptInterface(Ljava/lang/String;)Ljava/lang/String;`.
`AdCoreJsBridge.initJsMap` indexes public methods annotated with
`AdBasicInterface` by a generated method/type key; the prompt JSON `method`,
`types`, and `args` build that key before the stored `Method` is invoked
(`AdCoreJsBridge.java:671-681,691-770`). All 27 indexed handler descriptors in
the group exist in the APK DEX table. The pass-through
`mosaic.e.addJavascriptInterface(obj, str)` is not misreported as a concrete
registration because neither object nor name is fixed at that wrapper method.

## Validation

After all development expansion, `facts_expanded.jsonl` has 1,843 rows and
`canonical-facts.jsonl` has 1,838 rows. The canonical set contains 283 settings,
217 callbacks, 1,284 message handlers, 35 direct bridge methods, and 19 bridge
registrations. All 383 distinct normalized method descriptors pass both the
APK DEX-table verifier and the independent DEX symbol export. All 283 setting
values pass the source-line semantic verifier.
