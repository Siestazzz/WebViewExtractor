# v16a Ctrip deletion source review

v16a removes 214 semantic tuples from v15a and adds none. The removals are not uniformly precision improvements.

## Disappearing hosts

### QQSSOEntryActivity and QQEntryActivity: true conditional regressions

Both Activities call `Lcom/tencent/tauth/Tencent;->login(Landroid/app/Activity;Ljava/lang/String;Lcom/tencent/tauth/IUiListener;)I` from their DEX `qqLogin()V`. Tencent delegates through `AuthAgent.doLogin`. When native/SSO login is unavailable or forced web login applies, `AuthAgent` takes its web fallback:

`AuthAgent.doLogin(...) → AuthAgent.a(Z,IUiListener,Z,Map) → Lcom/tencent/open/utils/k;->b(Ljava/lang/Runnable;)V → AuthAgent$1.run() → Activity.runOnUiThread(AuthAgent$1$1) → new com.tencent.connect.auth.a(...) or new TDialog(...) → show()`.

The inner UI runnable constructs one of two real WebView dialogs. Their initialization methods install WebViewClient/WebChromeClient, apply the reported WebSettings, and call `loadUrl`. The deletion of construction-only Runnable seeding correctly removes an unsound generic seed, but the host has an actual utility-executor call and an actual `runOnUiThread` call. Losing both Activities and all 106 tuples is a relevance/caller-following regression, not proof that the dialogs were unreachable. Source: `QQSSOEntryActivity.java:200-210`, `QQEntryActivity.java:169-179`, `AuthAgent.java:238-258,276-343`.

### CTTourSearchActivity2: real host, but old four-row path was wrong

The Activity is a conditional H5 host: `createPages()` passes `SearchH5Fragment2.class` and a Bundle to the pager factory; `SearchH5Fragment2 extends H5Fragment`, so a matching custom tab creates a real old-H5 Fragment. The host disappearance is therefore an ownership recall regression.

However, the four deleted v15a facts were attributed through `initData() → new SearchNavBarPlugin(this)` and the plugin's inherited `mWebView`. `initData` calls only `refreshSearch`; it never calls `H5Plugin.init`, `setAttachedView`, or otherwise assigns that field. Construction-only seeding manufactured the old receiver chain. Those four specific tuples are correctly removed and must not be used as the evidence for the real Fragment host. The replacement source edge is `Lctrip/android/tour/search/view/CTTourSearchActivity2;->createPages()V → pager factory(..., Lctrip/android/tour/search/view/v2/SearchH5Fragment2;, Bundle, ...)`, with `Lctrip/android/tour/search/view/v2/SearchH5Fragment2;` inheriting `Lctrip/android/view/h5/view/H5Fragment;`. Source: `CTTourSearchActivity2.java:2372`, `SearchH5Fragment2.java:38`.

## Remaining removals

Most remaining operation deletions are true conditional operation regressions caused by removing broad Runnable seeds without preserving their actual schedulers:

- `Lctrip/android/view/h5v2/g/c;->b(Ljava/lang/String;L...JavaScriptExecuteResultListener;)V` explicitly calls `ThreadUtils.runOnUiThread(new c$a(...))`; `c$a.run()` calls `WebView.evaluateJavascript`. This accounts for the shared `g/c$a` losses across confirmed H5v2 hosts.
- H5v2 `e$g` is passed to `ThreadUtils.post` in the installed H5 WebViewClient flow and calls the captured H5WebView's `loadUrl`. The associated `util/f$b` paths likewise arise from real PDF/helper callbacks rather than free-standing construction.
- Old H5 `H5WebView$d` is explicitly passed to `ThreadUtils.post`, and its `H5WebView$j` callback performs `evaluateJavascript`; the two-row losses on old-H5 hosts remain source-reachable.
- `WebActivity.onCreate` passes `new WebActivity$a` to its request/helper callback registration and the callback calls its owned WebView's `loadUrl`; the one Sina loss is real conditional behavior.
- `CGoogleMapView` has explicit `ThreadUtils.post` sites and real `mGoogleWebView.loadUrl` calls; the TouristMap deletion is not a demonstrated false positive.

The gallery-derived three-row deletion on `CtripCommonFeedBackActivity` is the exception already established by ownership review: its empty `bottomWebViewUrl` prevents construction, so removing those rows is consistent with the known wrong host. `PhotoViewDetailActivity` and the two Hotel gallery Activities have nonempty/source-reachable gallery WebView paths, so their analogous deletions remain regressions.

Evaluate loses three operation rows (`g/c$a` evaluateJavascript plus an `e$g` H5 load path), but retains the separately repaired callback-to-VideoEnabled bridge chain. The deletion is still a partial operation recall regression; the canonical supplement does not enumerate the removed helper operations.

This classification is scoped to the deleted tuples. It does not claim every v15a callback-neighborhood row was precise; the known unregistered near-callback negative remains unresolved.
