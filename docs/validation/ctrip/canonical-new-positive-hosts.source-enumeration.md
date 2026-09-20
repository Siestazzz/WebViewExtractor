# Source enumeration coverage for 13 added Ctrip hosts

This is a non-blind public development set. Host candidates had appeared in prior ownership reports. The expected capability boundary in v2 was rebuilt without using extractor membership as the enumeration boundary.

For every host, source inspection established the concrete WebView allocation/layout field and every settings, client, or bridge registration entry in the relevant initialization path. Client implementations were then resolved from those source registrations. `test/runs/symbols/ctrip.jsonl` supplied the class parent chain and exact DEX descriptors. The enumeration walks each concrete client and every non-platform ancestor, retains recognized WebViewClient/WebChromeClient overrides, and applies Java override shadowing, except when the overriding method explicitly invokes `super`, in which case both the concrete override and the actually executed ancestor implementation are recorded. Bridge members are exactly the DEX methods carrying `Landroid/webkit/JavascriptInterface;` on the two source-registered bridge types.

The resulting pending file has 196 facts:

- 90 settings
- 22 `callback_registration` rows
- 70 callback execution rows
- 2 bridge registration rows
- 12 bridge-method rows

All 192 rows contain `source_webview_identity`, `app_version: 8.78.0`, and validator metadata. Settings retain literal/enum/dynamic value classification. Every callback and bridge member has a full DEX signature present in the independent symbol table.

Comparison with the preserved v1 report-assisted snapshot found no member delta after applying explicit-super semantics. A first shadow-only pass provisionally removed four Sina ancestor facts; direct source-body review showed that both `a` and `d` explicitly call `super.onPageStarted` and `super.onPageFinished`. Those calls execute `b` implementations, so all four ancestor callback facts are restored with `super_call_evidence`. Plain Java shadowing alone was insufficient. The conclusion comes from source bodies and independently verified DEX method descriptors, not extractor presence.

`canonical-new-positive-hosts.source-enumeration.jsonl` records each source registration, every traversed DEX type, the final signatures, and unknowns. All registrations yielded recognized members; there are no unresolved member sets in these 13 hosts. The preserved earlier file is `canonical-new-positive-hosts.pending-v1-report-assisted.jsonl`.

## Configuration-entry completeness

A second source-only pass scans the bound initialization sources for every `WebSettings.set*`, `setWebViewClient`, `setWebChromeClient`, and `addJavascriptInterface` call. `canonical-new-positive-hosts.source-config-coverage.jsonl` records the source and exported API-name sets per host. Across all 13 hosts there are no missing or extra setting API names and no missing client-registration API names. Counts remain site-sensitive in the pending facts, so the Hotel flagship main and popup WebViews retain separate registration rows even where their API names coincide.
