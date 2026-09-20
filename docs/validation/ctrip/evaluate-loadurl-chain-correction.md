# EvaluateDialog H5v2 load chain correction

Scope: public `EvaluateDialogActivity` evidence only. This corrects the earlier shorthand that treated the two-argument load as an immediately inherited `VideoEnabledWebView` call. It does not use a sealed holdout and does not change canonical facts.

## Correct chain

`H5WebView` declares both overloads in DEX:

1. `Lctrip/android/view/h5v2/view/H5WebView;->loadUrl(Ljava/lang/String;)V` calls its own `loadUrl(String,Map)` with a null map (`H5WebView.java:458-465`).
2. `Lctrip/android/view/h5v2/view/H5WebView;->loadUrl(Ljava/lang/String;Ljava/util/Map;)V` obtains the interceptor/package list using `Lctrip/android/view/h5v2/view/c;->a(Ljava/lang/Object;)Ljava/util/List;` with the literal `"ubt"`, constructs `H5WebView$a(url,map)`, and calls `Lctrip/android/view/h5v2/view/H5BaseWebView;->j(Ljava/lang/String;Ljava/util/List;Lctrip/android/view/h5v2/view/H5BaseWebView$j;)V` (`H5WebView.java:363-372`). Thus this overload is an H5 declaration and execution enters it; it is not a dispatch directly to `VideoEnabledWebView.loadUrl(String,Map)`.
3. `H5BaseWebView.j` invokes `j.onComplete()` immediately when package interception is unnecessary or the required package list is empty. Otherwise it starts `downloadNewestPackageForProducts` with `H5BaseWebView$e`; `H5BaseWebView$e.onPackagesDownloadCallback` invokes the same callback after the download/install processing (`H5BaseWebView.java:212-247,814-847`). The callback boundary can therefore be synchronous or asynchronous.
4. `H5WebView$a.onComplete()` calls `Lctrip/android/view/h5v2/view/H5BaseWebView;->i(Ljava/lang/String;Ljava/util/Map;Z)V` on the same `H5WebView` receiver with `z=false` (`H5WebView.java:63-85`). `i` normally calls `p` immediately; a missing-local-package branch first downloads packages via `H5BaseWebView$f`, whose completion calls `p` (`H5BaseWebView.java:251-289,787-810`).
5. For a non-`javascript:` URL, `p` reaches `t` for online/nonlocal URLs; `t` reaches private `q`; local-package branches either reach `q` directly or after install/download callbacks (`H5BaseWebView.java:601-620,950-994`). In `q`, the DEX `invoke-super` at source line 567 resolves from owner `H5BaseWebView` to its immediate superclass method `Lctrip/android/view/h5v2/view/VideoEnabledWebView;->loadUrl(Ljava/lang/String;Ljava/util/Map;)V`. That method calls private `c()`, which registers `VideoEnabledWebView$a` as `_VideoEnabledWebView`, then invokes the platform `WebView.loadUrl` (`VideoEnabledWebView.java:77-86,151-159`).
6. The `javascript:` branch uses `H5BaseWebView.k`, whose `invoke-super` at line 861 resolves to `VideoEnabledWebView.loadUrl(String)`; it also calls `c()` before the platform load (`H5BaseWebView.java:850-866`; `VideoEnabledWebView.java:111-123`).

The same receiver identity is retained throughout: the callback captures `H5WebView.this`, and the inherited/private H5Base methods operate on that receiver. The `_VideoEnabledWebView` registration therefore belongs to the Evaluate `H5WebView` when a nonempty load proceeds beyond the package callback and reaches either final load branch. It is source-valid but conditional on normal continuation (including Robust patch bypasses and package callbacks); it is not established merely by entering the H5 map overload.

## Probe interpretation

`test/runs/v14c-evaluate-probe.log` explicitly visits `H5WebView.loadUrl(String)` and three contexts of `H5WebView.loadUrl(String,Map)`, and records the `c.a("ubt")`, callback construction, and `H5BaseWebView.j` calls. It does not visit either `VideoEnabledWebView.loadUrl` overload. Therefore the v14c gap is after the already-entered H5 map overload: callback/interface continuation plus the later H5Base private routing and `invoke-super`. Describing it as a failure to enter the self/map overload is incorrect.

## Supplement disposition

- Keep the H5 one-argument operation fact.
- Keep the eventual `VideoEnabledWebView.loadUrl(String,Map)` and `_VideoEnabledWebView` facts as conditional source facts, but replace any direct-inheritance evidence with the chain above.
- The H5-owned `H5WebView.loadUrl(String,Map)` is a separate executed operation. If the operation oracle aims to enumerate every executed override, add it separately; it must not be represented by relabeling the eventual `VideoEnabledWebView` invocation.

DEX signatures were checked against `test/runs/symbols/ctrip.jsonl`. Source paths refer to `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/ctrip-1/sources/ctrip/android/view/h5v2/view/`.
