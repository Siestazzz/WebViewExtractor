# Mango v11b validation

v11b retains exactly the 89 v10a hosts, with no additions or removals. The ownership verdicts remain 84 valid and 5 uncertain. The report contains 20,666 raw facts versus 20,676 in v10a; the net reduction is explained by identity refinement and removal of speculative bridge duplicates rather than lost capability surfaces.

## Antique WebView identity

All 16 `AntiqueDetailActivity` settings and its `setWebViewClient` registration now use the identical underlying alternative:

`android.webkit.WebView / union:47aeb8ec-8880-3465-b1ca-b94ea7dc35d8`.

No restored fact uses `constructor_parameter`. This matches the independently checked source chain `ShNftWebviewLayoutBinding.webview -> getSettings()` and fixes v10a's presentation of settings as a separate `WebSettings` union.

The same normalization changes one `setLoadsImagesAutomatically(true)` fact in each of `ErlangLiveActivity` and `SanlangLiveActivity`. The former WebSettings union becomes two actual receiver alternatives, `ImgoWebView` and `NestedImgoWebView`, because the same `ImgoWebView$2.onPageFinished` client can run for either registered WebView. The API/value is unchanged; the raw count increases by one per host because alternatives are emitted separately.

## Map registry differences

Nine Pangle hosts lose bridge rows: eight lose one and `TTMiddlePageActivity` loses four. Inspection shows that every removed row had `implementation=unknown` and `resolution=declared_bridge_contract_only`. Concrete registrations at the same sites remain:

- `IESJSBridge` at `bp.c(WebView,r,String)` retains implementation `com.bytedance.sdk.component.adexpress.bp.z` and its annotated `invokeMethod(String)` member.
- `SDK_INJECT_GLOBAL` at `SSWebView.c(Object,String)` retains implementation `com.bytedance.sdk.component.adexpress.bp.zx` and its 13 annotated members.

The source confirms the exact per-instance map semantics. `bp.c(WebView,r,String)` gets `z` from `Map<Integer,z>` using `webView.hashCode()`, updates it with `z.c(r)` when present, otherwise constructs `new z(r)` and stores it under that key, then registers that same `z` object. The SSWebView overload performs the parallel operation on `Map<Integer,zx>` before registering `SDK_INJECT_GLOBAL`. Thus the removed unknown rows were duplicates, while the retained concrete rows are the real registrations.

The report diagnostics continue to record uncertain cases such as `unresolved_map_key`, `unresolved_map_lookup_key`, `map_update_order_unresolved`, and `map_mutation_order_unresolved`. No endpoint is asserted from those diagnostics.

No other host changes fact count. The NFT 48 settings and six required registration/member facts remain present. `v11b-validation.json` records all 11 changed hosts and their kind counts. `audit_v11b.py` regenerates it and the complete `v11b-ownership.jsonl` without reading sealed data.
