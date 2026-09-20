# Shared WebView settings audit for the 13 published Activities

The prior flat export omitted settings applied in shared WebView subclasses.
`build_groups.py` now binds those subclasses to the corresponding capability
groups and regenerates `groups.jsonl` and `facts_expanded.jsonl`.

`BaseWebView.init()` always calls `allowMixedContent()` and
`setDefaultSetting()` (`BaseWebView.java:249-258`). These apply
`setMixedContentMode(0)`, `setTextZoom(100)`, remote-valued
`setDomStorageEnabled(...)`, and `setAllowFileAccess(true)`
(`:220-228,287-320`). They are now inherited by the custom-browser family and
`WebDetailActivity`.

`BaseSysWebView.init()` likewise applies `setMixedContentMode(0)`,
`setTextZoom(100)`, and remote-valued `setDomStorageEnabled(...)`
(`BaseSysWebView.java:75-108,149-181`). They are now inherited by
`WebNovelActivity` and `WebAdvertActivity`.

`REWebView extends NewsWebView extends BaseWebView`
(`REWebView.java:15,52-54`; `NewsWebView.java:27`). Both editor Activities now
inherit the four `BaseWebView` settings plus the `NewsWebView.initView()`
settings: default encoding, zoom support, conditional JavaScript, DOM storage,
database, render priority, cache mode, user agent, mixed content, and built-in
zoom controls (`NewsWebView.java:223-248`). Their editor-specific settings are
retained as separate calls.

After regeneration, setting fact counts are:

| Activities | Count each | Shared source included |
|---|---:|---|
| CustomWebBrowserForItemActivity, CustomWebGameForItemActivity, TencentVideoWebActivity, HalfTencentVideoWebActivity | 19 | BaseWebView |
| WebDetailActivity | 18 | BaseWebView |
| QAEditorActivity, RichEditorActivity | 19 | BaseWebView + NewsWebView |
| WebNovelActivity, WebAdvertActivity | 4 | BaseSysWebView |
| PrivacyWebActivity, PrivacyWebItemActivity | 8 | raw Android WebView path; no omitted Tencent base class |
| SecurityTicketActivity | 5 | X5WrapperWebView has no additional per-instance WebSettings calls |
| SupportActivity | 3 | raw X5 WebView path; no omitted Tencent base class |

The expanded export has 164 setting facts and 1,578 total facts. The canonical
export has 1,573 facts after a later callback dispatch audit removed five
shadowed superclass implementations; see `v2d-gap-analysis.md`. The larger
total also reflects reuse of the same 242 routed H5 message handlers by each of
the four custom-browser-family Activities, as proven in `message-routing.md`.
Duplicate
API names are retained when distinct initialization layers call them, because
later calls may override earlier values and each call is independently present.
All 315 normalized method facts still pass both APK DEX-table verification and
the independent symbol export; settings retain their exact X5 or Android
`normalized_api` owner.

`canonical-facts.jsonl` adds typed setting semantics while preserving
`facts_expanded.jsonl`. Every setting has `source_expression` containing the
source argument and a `value_kind`: `literal` uses a JSON boolean, integer, or
string; `enum` retains the symbolic enum constant; `dynamic` uses `null` for
`value`. `settings-value-verification.jsonl` independently checks the type and
classification and requires the exact API name and expression on the cited
source line.
