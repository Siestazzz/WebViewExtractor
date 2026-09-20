# v13e Ctrip source delta and regression replay

v13e completed 410/410 manifest Activities in 177.434 seconds and emitted the same 55 hosts as v13d. Ownership is unchanged and remains source-derived.

## v13d to v13e delta

Twenty-one Activity records differ; 17 contain semantic additions. There are 119 added tuple instances and no removals: 42 WebView operations, 36 callbacks, 21 bridges, and 20 settings. Unattributed, index diagnostics, and manifest diagnostics are exactly equal. The additions arise from dispatching actual WebView API calls to concrete subclass overrides and following capabilities inside those overrides.

For `EvaluateDialogActivity`, v13e adds one reported operation at `H5WebView.loadUrl(String)`, with the dispatched API represented as the inherited map overload. This corresponds to one part of the source chain established in `v13d-source-regressions.jsonl`. The cumulative replay confirms only one of the two expected operation facts matches.

The `_VideoEnabledWebView` registration is still absent. Source proves `VideoEnabledWebView.loadUrl(String,Map)` calls `c()`, and `c()` installs `VideoEnabledWebView$a` under that name before calling the platform implementation. Its annotated `notifyVideoEnd()V` is also still absent. Thus v13e is a partial repair: one operation fact recovered, one operation representation remains unmatched, and both bridge rows remain missing.

## Four public object samples

`CMBApiEntryActivity` is record-for-record unchanged. `H5Container` and `MyCtripOrderModalActivity` each gain one override-dispatch operation. `MktH5ContainerV2` gains eleven operations. These additions attach to the existing source types and do not change the object conclusions: old H5/modal remain a single Fragment WebView; CMB remains its layout WebView; Mkt still has cached/fresh runtime alternatives and the `A.x=A` self-alias. No exact allocation alias defect is resolved.

## Current and cumulative scoring

The unchanged current 30-host canonical oracle still reports:

- settings 323/323
- bridges 3,680/3,680
- callbacks 391/391
- positive hosts 30/30

The cumulative replay appends the four source-regression rows without changing canonical:

- WebView operations 1/2
- bridges/bridge members 3,680/3,682
- three total missing regression facts

The missing rows are the unmatched loadUrl override representation, `_VideoEnabledWebView` registration, and `notifyVideoEnd()V`. `v13e-public30-plus-regressions-scoring.json` preserves this failed cumulative result. Perfect recall on the existing three categories is therefore not treated as proof that v13e fixed the source regression.

Files:

- `v13e-delta.json`
- `v13e-ownership.jsonl`
- `v13e-public30-scoring.json`
- `v13e-public30-plus-regressions-scoring.json`
- `v13d-source-regressions.jsonl`
