# v14c Ctrip source audit

v14c completed 410/410 manifest Activities in 180.508 seconds and emitted the same 55 hosts as v13e. The APK hash and source ownership set are unchanged.

## v13e to v14c delta

Only six Activity records differ, all in ordering or nested representation. Their fact counts are unchanged. There are zero semantic additions and zero semantic removals under the stable capability tuple. `unattributed`, index diagnostics, and manifest diagnostics are exactly equal.

The six representation-only hosts are `FlightHalfMaskH5Container`, `FlightMaskH5Container`, `TouristMapActivity`, old `H5Container`, `MyCtripOrderModalActivity`, and `CtripCommonFeedBackActivity`. No new Fragment/getView/child/lifecycle fact changes Ctrip capability semantics in this report.

## Evaluate source supplement

The four supplement facts remain source-valid:

- `EvaluateDialogActivity.onCreate` invokes its layout `H5WebView.loadUrl(String)`.
- That override calls the inherited map overload on the same runtime `H5WebView`.
- `VideoEnabledWebView.loadUrl(String,Map)` calls `c()` before `super.loadUrl`.
- `c()` registers `VideoEnabledWebView$a` as `_VideoEnabledWebView`; its DEX-confirmed annotated member is `notifyVideoEnd()V`.

v14c does **not** recover the auxiliary bridge. `EvaluateDialogActivity` is record-for-record equal to v13e. Current canonical replay, which now directly includes the four supplement rows, still misses three facts: one of the two operation identities, the `_VideoEnabledWebView` bridge, and `notifyVideoEnd()V`. The score is operations 1/2 and added bridge rows 0/2. This is report evidence of an unfixed regression, not a revision of the source truth.

The invoke-super change does not affect this chain's missing bridge because its first missing edge is the virtual/self dispatch from `H5WebView.loadUrl(String)` into the inherited overload and then the private `c()` call; the report remains identical at this host.

## Four public object samples

`MktH5ContainerV2` and CMB are record-for-record unchanged. Old `H5Container` and `MyCtripOrderModalActivity` retain the same 85 facts and differ only in representation/order. Their source object conclusions remain unchanged. Mkt still has cached/fresh runtime alternatives and the `A.x=A` self-alias; v14c does not establish exact allocation identity.

## Current cumulative replay

Against the 4,530-row canonical oracle:

- settings: 323/323
- bridges and bridge members: 3,680/3,682
- callbacks: 391/391
- WebView operations: 1/2
- positive hosts: 30/30
- total missing supplement facts: 3

The scorer acceptance remains `unproven`, and the failed supplement cases are preserved in `v14c-current-canonical-scoring.json`.

Files:

- `v14c-delta.json`
- `v14c-ownership.jsonl`
- `v14c-current-canonical-scoring.json`
- `v14c-validation.md`
