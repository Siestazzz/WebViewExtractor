# v15a Evaluate actual-callback audit

This is a bounded review of `test/runs/v15a-evaluate-probe.json` against the public source chain documented in `evaluate-loadurl-chain-correction.md`. It does not generalize the result to other callbacks or hosts.

## Result

The missing continuation is restored. Compared with v14c, v15a adds exactly five facts to `EvaluateDialogActivity`: the `_VideoEnabledWebView` bridge, the two superclass `VideoEnabledWebView.loadUrl` operations, and their two terminal platform `WebView.loadUrl` calls. The bridge embeds the sole annotated member `Lctrip/android/view/h5v2/view/VideoEnabledWebView$a;->notifyVideoEnd()V`.

The report follows the real argument, rather than treating interface construction as execution:

`H5WebView.loadUrl(String,Map)` constructs `H5WebView$a(url,map)` and passes that concrete object as parameter 3 to `H5BaseWebView.j(String,List,H5BaseWebView$j)`. The only relevant interface invocation in `j` is `j.onComplete()`. It is invoked directly on the immediate branches, or retained in `H5BaseWebView$e` and invoked by `onPackagesDownloadCallback`. Dispatching that invoked interface method to `H5WebView$a.onComplete()` is source-correct. The callback captures `H5WebView.this`, URL, and map; `onComplete` calls `H5BaseWebView.i` on that captured receiver. This recovers `i → p → k` for `javascript:` and `i → p → t/q` for ordinary URLs, followed by the actual `invoke-super` to the relevant `VideoEnabledWebView.loadUrl` overload.

Both branch families appear in the static report because the URL is a union. A single runtime URL follows one family. This is conditional path coverage, not evidence that both overloads execute for every load.

## Object identity

All five additions, including the bridge, use:

`activity:ctrip.business.evaluation.EvaluateDialogActivity/view:2131313182`

with concrete type `ctrip.android.view.h5v2.view.H5WebView` and XML evidence `r/a9/wi.xml`, resource ID `2131313182`. The callback object ID also ends in this same activity/view identity. Source agrees: `H5WebView$a` captures `H5WebView.this`; inherited H5Base methods preserve that receiver; `invoke-super` changes method selection without replacing the object. The bridge implementation object is newly allocated, but its `addJavascriptInterface` receiver is the same XML H5WebView. The embedded `notifyVideoEnd` member therefore belongs to that registration on that same WebView.

## Rule boundary review

For this case, the new rule has the necessary precision boundary:

- It follows parameter 3 because `H5BaseWebView.j` actually invokes `H5BaseWebView$j.onComplete()`.
- It follows the concrete `H5WebView$a` implementation supplied at the call site and its captured outer receiver.
- It also preserves the callback through the concrete `H5BaseWebView$e` forwarding wrapper, whose callback method explicitly invokes the stored `j.onComplete()`.
- No other member of `H5BaseWebView$j`, `H5WebView$a`, or the download listener is inferred as executed merely from allocation or storage.

Remaining precision limits visible in this probe are branch feasibility and callback completion. Empty URLs, Robust interception, failed/deferred package flows, and URL class determine whether the terminal call executes. Consequently the recovered bridge remains correctly marked `conditional: true` and `binding_status: candidate`. The probe supports this exact callback-forwarding pattern; it does not justify dispatching arbitrary methods of stored listener objects.

DEX owners and signatures were checked against `test/runs/symbols/ctrip.jsonl`. Probe SHA-256: `test/runs/v15a-evaluate-probe.json` = `936b3d2ec842697fa7fe3d73810ae042d82152cfc6a8a150086f032528906596`.
