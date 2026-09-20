# v15a Ctrip source review of semantic additions

This review covers the 85 v14c-to-v15a semantic additions. It reuses the previously source-verified host construction and H5v2 inheritance evidence; report output is used only to identify the changed sites.

## Evaluate callback continuation

`EvaluateDialogActivity` accounts for five additions: one bridge and four operations. The complete source review is in `v15a-evaluate-callback-audit.md`. The concrete `H5WebView$a` argument is actually invoked through `H5BaseWebView$j.onComplete`; it captures the same XML `H5WebView`. The recovered `H5BaseWebView` routing reaches `VideoEnabledWebView.loadUrl`, whose `c()` registers `_VideoEnabledWebView` with the annotated `notifyVideoEnd()V` member. These additions are source-valid and conditional.

## Shared H5v2 host additions

Ten already-confirmed H5v2 hosts each gain eight operation facts: `MktH5ContainerV2`, `ChatFloatingWebView`, `GSHybridPageActivity`, `CFHyWebActivityV2`, `CTFinanceHomeActivity`, `FlightPageH5Container`, `TrainContainerActivity`, H5v2 `H5Container`, `H5PreRender`, and `AgingHomeActivity`.

For each host, the added operations are the same four method identities on two previously reported runtime allocation alternatives:

- `VideoEnabledWebView.loadUrl(String)` and terminal `WebView.loadUrl(String)`;
- `VideoEnabledWebView.loadUrl(String,Map)` and terminal `WebView.loadUrl(String,Map)`;
- one receiver identity comes from `PreloadWebView.acquireWebViewInternal`'s cached/fresh union;
- one receiver identity is the `Stack.pop()` cached path.

The method ownership is source-valid: `H5WebView extends H5BaseWebView extends VideoEnabledWebView extends WebView`, and the reviewed H5 load chain reaches the superclass overloads through `H5BaseWebView.k/q`. The ten hosts already had source-confirmed H5v2 construction/Fragment ownership in the canonical host-group and v4e/v14c ownership audits. v15a does not introduce a new host or a new host-to-H5 binding.

These rows do not establish that the two allocation IDs are distinct runtime objects. Cached and fresh acquisition are alternatives, and the prior exact-allocation caveat remains. Likewise, the String and String/Map terminal paths are conditional URL branches rather than operations guaranteed to execute together.

## Removals and known limit

There are no semantic removals, so no source capability regression is hidden by aggregate oracle counts. Sixteen additional changed Activity records contain only ordering or nested representation differences.

The known unregistered near-callback negative fixture still fails, as it did in v14c. This review therefore confirms the concrete Evaluate callback argument and these inherited H5 operations only. It does not claim that v15a excludes every callback-neighborhood false positive.
