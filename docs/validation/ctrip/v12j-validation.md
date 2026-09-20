# v12j Ctrip validation

The report was read after the benchmark recorded Ctrip completion: 410/410 manifest activities processed, 55 emitted hosts, status `partial`, and APK SHA-256 `cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66`.

## v12i to v12j delta

The 55-host set is exactly unchanged. Eight activity records differ: six only in order or nested representation and two have one semantic addition each. There are no semantic removals. The two additions are the same `webview_operation`, `Lcom/tencent/connect/auth/a$a$1;->run()V@16 -> WebView.loadUrl(String)`, attributed to `QQSSOEntryActivity` and `QQEntryActivity`. Source independently confirms the delayed anonymous `Runnable` calls the enclosing auth dialog's `j.loadUrl(o)` (source `com/tencent/connect/auth/a.java`, around lines 150–153). v12i listed this method as unattributed `not_expanded`; v12j removes that diagnostic. This is a real reachability recovery.

All bridges, bridge members, settings, and callbacks are semantically unchanged. `index_diagnostics` and `manifest_diagnostics` are exactly equal. `unattributed` differs by exactly the one recovered QQ runnable diagnostic described above.

The two Hotel map operations lost in v12i are **not restored**. `HotelDetailMapActivity` and `HotelListMapActivity` still omit the source-real `CGoogleMapView.executeJSAction(String) -> mGoogleWebView.loadUrl(String)` site. The established host chain remains `CtripUnitedMapView -> initMapViewWithMapType -> CGoogleMapView -> initWebView`; v12j therefore retains this known operation false negative.

## Object and ownership continuity

All four public object samples (`H5Container`, `MyCtripOrderModalActivity`, `MktH5ContainerV2`, and `CMBApiEntryActivity`) are record-for-record equal between v12i and v12j. Old H5, modal Fragment, and CMB retain their proven single receiver. New-H5 Mkt retains the cached/fresh runtime alternatives and the `A.x=A` self-alias; no exact allocation alias defect is fixed by this release.

`v12j-ownership.jsonl` contains all 55 emitted hosts and replays the source-reviewed ownership verdicts with explicit v12j provenance. The two semantic additions occur in already confirmed QQ hosts, so no ownership decision changes.

Machine details are in `v12j-delta.json`; they include every changed host and exact semantic additions/removals.
