# v12i Ctrip validation

The report was read only after the benchmark contained the completed Ctrip row: all 410 manifest activities were processed, 55 activities were emitted, and the APK SHA-256 remains `cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66`. Status remains `partial`; queue completion is not a claim that every capability is correct.

## v12h to v12i delta

The emitted host set is exactly the same 55 hosts. `unattributed`, `index_diagnostics`, and `manifest_diagnostics` are exactly equal. Thirty-six activity records differ: 22 are ordering or representation changes and 14 contain semantic fact changes. There are 46 added and 2 removed facts under the stable tuple `(kind, site, api, name, registration_name, implementation, signature, value)`. Every semantic delta is a `webview_operation`; bridges, bridge members, settings, callbacks, and host ownership do not change.

The additions are source-real operations reached through the new-H5 client: `e$c.run` invokes `evaluateJavascript` (source line 205), `e$g.run` invokes `H5WebView.loadUrl` (line 282), and `e.shouldOverrideUrlLoading` invokes the client field's `H5WebView.loadUrl` (line 1086). They appear across the source-supported new-H5 hosts. Multiplicity varies with report object identities; this audit confirms the operation sites and receiver class, not every duplicate count.

The two removals are the same real site, `CGoogleMapView.executeJSAction(String)` calling its `mGoogleWebView.loadUrl` at source line 604, from `HotelDetailMapActivity` and `HotelListMapActivity`. Earlier source review proves both hosts reach `CtripUnitedMapView -> initMapViewWithMapType -> CGoogleMapView -> initWebView`. These removals are v12i false negatives/regressions, not ownership corrections.

`v12i-delta.json` records every changed host and every semantic addition/removal. `v12i-ownership.jsonl` covers all 55 output hosts, reusing the prior source verdict and anchor because no host was added or removed.

## Four public object samples

`H5Container`, `MyCtripOrderModalActivity`, and `CMBApiEntryActivity` have no semantic capability delta, so their established single-object conclusions remain unchanged. `MktH5ContainerV2` adds the four source-real new-H5 operations above. Its earlier object caveat remains: cached/fresh acquisition are runtime alternatives, while `A.x=A` is a self-alias, and duplicate report identities must not be treated as additional WebViews.

Files:

- `docs/validation/ctrip/v12i-delta.json`
- `docs/validation/ctrip/v12i-ownership.jsonl`
- `docs/validation/ctrip/v12i-validation.md`
