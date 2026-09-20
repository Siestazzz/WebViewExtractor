# v9b settings and callback audit

## Pangle boolean settings

The three restored Pangle facts are correct. `TTWebPageActivity`, `TTVideoWebPageActivity`, and inherited `TTVideoScrollWebPageActivity` each reach `Lcom/bytedance/sdk/openadsdk/core/widget/c/tn;->c(WebView)V@72`, and both raw carrier copies for every host now emit `setBlockNetworkImage(false)`.

Independent DEX instructions establish the value. `tn.<init>(Context)` loads integer `1` and stores it into `tn.e:Z` at offset `0x000e`. In `tn.c(WebView)`, offset `0x0045` reads `e`, offset `0x0047` applies `xor-int/2addr` to the initial true operand, and offset `0x0048` calls `WebSettings.setBlockNetworkImage`. The landing chain calls `tn.c(context).c(true).c(webView)`; the video chain calls `tn.c(context).c(true).tn(false).c(webView)`; the scroll Activity inherits the video chain. The fluent boolean setters write `pb` and `zx`, respectively, and do not change `e`. Thus `true XOR true` yields false on all three paths.

The historical v8c output proved that the correct setting call and WebView receiver were reached, but its argument remained unknown. v9b's bitwise-expression repair now evaluates that argument. The earlier unknown should not be described as independently proving loss of the configurator's field state; inability to evaluate the XOR operation was sufficient to produce it.

## WebContainer parent contract

The WebContainer custom callback registration remains correctly bound from `WebViewFragment.nc()` to `ImgoWebView.q0:kq.d` with concrete listener `WebViewFragment$b`. Its member set grows from 18 in v9a to 23 in v9b. All five previously missing methods from parent interface `kq.f` are restored and resolved to their effective implementations:

- inherited `kq.e.onPageFinished`
- concrete `WebViewFragment$b.onPageStarted`
- concrete `WebViewFragment$b.onReceivedError(WebView,int,String,String)`
- inherited `kq.e.onReceivedHttpError`
- inherited `kq.e.shouldOverrideUrlLoading`

This confirms that parent-class and parent-interface contract traversal is working for member enumeration.

Dispatch scanning is unchanged in v9b. The restored members remain `dispatch_observed=false`, and `onProgressChanged` plus `onRenderProcessGone` also remain false even though their exact two-`iget` inner-client DEX chains were established in the v9a audit. These values mean unresolved in this version; they do not prove the methods are never dispatched. The pending inner-client scan is a separate change from the now-correct contract enumeration.

The report contains 85 Activities. No holdout material was read, and no production or canonical file was changed.
