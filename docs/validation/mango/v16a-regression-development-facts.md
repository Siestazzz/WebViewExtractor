# v16a source regression development facts

`v16a-regression-development-facts.jsonl` contains 120 source/DEX-authored facts and does not modify canonical. It separates every registration from its callable member or callback contract member. v15a matches all 120; v16a matches none. The split is 92 bridge facts and 28 callback facts.

The Pangle set covers nine affected hosts. Its shortest registration chain is the already audited host renderer to `com.bytedance.sdk.component.adexpress.bp.bp.c(WebView,r,String)`, which creates or reuses the exact `com.bytedance.sdk.component.adexpress.bp.z`, then calls `WebView.addJavascriptInterface(z,name)`. The name resolves to `IESJSBridge`; DEX confirms the sole exposed member `z.invokeMethod(Ljava/lang/String;)V` carries `@JavascriptInterface`.

The mgadplus set covers RewardAdFreeActivity and MGVideoPlayActivity. The concrete H5 media branch calls `am.a.Q()`; Q reads the same `e:ImgoAdWebView` field and invokes `t(literalName,new handler)` fifteen times. Each handler class and its exact handler descriptor is independently read from DEX. `am.a.U()` separately registers `am.a$w` through `setWebViewLifeCycleCallback`; DEX confirms `am.a$w` and its five declared lifecycle overrides; the registration and those members are emitted separately.

For both DTF activities, inheritance reaches `FaceLoadingActivity.J()`, which constructs `new ToygerWebView(this,null)` into field `M`. The constructor calls `init`, which explicitly installs `ToygerWebChromeClient`, `ToygerWebViewClient`, and `DTFJSBridge` under `AndroidInterface`. DEX supplies the exact six framework callback members and annotated `getStyleOptions()Ljava/lang/String;` member.

The Erlang addition covers five more explicit registrations from `ImgoWebView.java:661-665`; the two constant-backed names resolve in source to `VideoInteractionEvent` and `VideoSetPlayerMuted`. The fixed `e0.handler` rule resolves their exact declared `(String)V` endpoints in `ImgoWebJavascriptImpl`.

Saved scoring: `v16a-regression-v15a-score.json` is 120/120; `v16a-regression-v16a-score.json` is 0/120. The generator is `build_v16a_regression_facts.py` and is byte-identical on repeat execution.
