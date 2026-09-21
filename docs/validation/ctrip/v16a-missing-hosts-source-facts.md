# v16a missing-host source fact increment

`v16a-missing-hosts-source-facts.pending.jsonl` contains 119 nonblind, source-derived rows and is intentionally not merged into canonical.

`v16a-missing-hosts-source-facts.scoreable.jsonl` contains the directly cumulative scorer schema: 414 rows using only `setting`, `bridge`, `bridge_method`, `callback_registration`, `callback`, and `webview_operation` kinds.

For each of the two QQ login Activities it records both conditional SDK fallback Dialog implementations. Each host has its own `qqLogin → Tencent.login → AuthAgent → executor → UI Runnable → Dialog` evidence. Shared implementation facts are expanded per host only after that binding is established.

Per QQ host the file contains 26 settings, four client registrations, all 11 concrete client overrides, two console-message bridge registrations, all 14 DEX-visible bridge methods, and two authorization `loadUrl` operations. The two message surfaces are `SecureJsInterface` on the secure auth dialog and `sdk_js_if`/`TDialog$JsListener` on `TDialog`. These are marked `message_bridge`, since the SDK routes them through its console-message registry rather than Android `addJavascriptInterface`.

In the scoreable file those message rows use `bridge` and `bridge_method`, with `transport_kind=console_jsbridge` and the actual registry API retained in `normalized_api`. Every `loadUrl` row carries `value_kind=dynamic` and the source expression describing the AuthAgent-built authorization URL.

The TourSearch row is a source binding to the independently audited `ctrip-h5-v1` group. Its evidence is the pager factory's `SearchH5Fragment2.class` argument and `SearchH5Fragment2 extends H5Fragment`. Expansion must use that existing source group. The uninitialized `SearchNavBarPlugin.mWebView` path is explicitly excluded.

The scoreable file expands that group to 17 settings, 34 bridges, 222 bridge methods, two client registrations, and 21 callback overrides. Applicability is independently established by `SearchH5Fragment2.addWebView()` assigning `new SearchWebView` to inherited `mWebView`, `onCreateView()` invoking `super.onCreateView()`, and `loadWebview()` invoking `super.loadWebview()`. Its exact WebView constraint is `ctrip.android.tour.search.view.widget.SearchWebView`.

All 50 method-bearing callback/message rows were checked against `test/runs/symbols/ctrip.jsonl`; none is missing. Settings preserve literal, enum, and dynamic value kinds. `setDatabasePath` is dynamic rather than compared to a fabricated constant.

The shortest common repair entry for the QQ hosts is actual caller relevance at `Lcom/tencent/open/utils/k;->b(Ljava/lang/Runnable;)V`, followed by the concrete `AuthAgent$1.run()` and the existing `Activity.runOnUiThread(AuthAgent$1$1)` semantic. The rule should follow only the Runnable argument passed at that call. For TourSearch, the reusable entry is the pager factory's concrete Fragment `Class` argument and Fragment lifecycle construction, not plugin construction.

Regenerate with:

```sh
python3 scripts/validation/build_ctrip_v16a_missing_source_facts.py
```
