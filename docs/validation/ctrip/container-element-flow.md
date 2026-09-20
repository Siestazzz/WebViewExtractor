# Container element and factory flow evidence

The H5 v2 core path is an erased DEX container flow with a source generic constraint. The exact steps are:

1. `Lctrip/base/init/n$c;->t(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)Ljava/util/List;` returns source type `List<H5Plugin>`.
2. The unconditional core instances are created in `n.java:520`, packed through `Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;`, and copied through `Ljava/util/ArrayList;->addAll(Ljava/util/Collection;)Z`.
3. Conditional Bus results use `Lctrip/android/bus/Bus;->callData(Landroid/content/Context;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;`, are cast to `Lctrip/android/view/h5v2/plugin/H5Plugin;`, and enter the same list through `Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z`. Samsung Wallet enters through `add` only under `obj instanceof H5Fragment`.
4. `Lctrip/android/view/h5v2/view/H5WebView;->E(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)V` invokes `t` and stores the erased list in `Lctrip/android/view/h5v2/view/H5WebView;->y:Ljava/util/List;` (`H5WebView.java:218`).
5. `Lctrip/android/view/h5v2/view/H5WebView;->D(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)V` calls `Ljava/util/List;->iterator()Ljava/util/Iterator;`, then `Ljava/util/Iterator;->next()Ljava/lang/Object;`, casts each result to `Lctrip/android/view/h5v2/plugin/H5Plugin;`, and passes that same value to `Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V` (`H5WebView.java:182-186`).

The parameterized summary must attach concrete element types to the list value produced by `t`, preserve them through `asList`, `addAll`, field `y`, `iterator`, `next`, and `check-cast`, and distinguish unconditional constructors from conditional `Bus.callData`/guarded `add`. Expanding the erased iterator type to every APK subtype is unsound.

The QMP SDK demonstrates a multi-layer helper return. `AuthActivity.onCreate` passes `WebViewHelper.getChromeClient(this)` and `getViewClient(this)` into the WebView. Those static methods invoke interface methods on `WebViewHelper.IMPL`, whose field initializer is `new WebViewHelperSdk8()`. The implementations return `new ClientSdk8(callback)` and `new ViewClientSdk8(callback)`. A factory summary therefore needs receiver-sensitive interface dispatch and return-object propagation across both helper layers. The Activity's `IWebCallback` methods are delegates invoked by those clients; they are not WebView callback overrides.
