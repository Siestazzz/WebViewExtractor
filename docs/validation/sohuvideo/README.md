# sohuvideo independent source audit

Current input is the patched APK explicitly selected by the user. SHA256: `07780de1d6d08e62da19fc2a98eaa4f53796cf7d3f02d8c0bebb02f3b3aa07a2`. Predecompiled build hash matches metadata patched hash and the copied APK. The earlier original-APK manifest inventory is superseded in `history-origin/` and contributes no current canonical facts.

Source root: `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/sohuvideo-1`. Reviewer: GPT-6 Codex (exact runtime model identifier unavailable). The first 93 facts across 2 confirmed Activities were derived before reading extractor outputs; `source-first-freeze.json` records their hash. Every fact includes source locations, host evidence, APK hash and reviewer.

The manifest inventory contains 407 entries; the 51 source registration files are search inventory, not verified Activity ownership. This is an initial development audit, not protocol acceptance: fewer than 30 positive hosts reviewed, no exhaustive negative search, final holdout or all-output precision audit. JADX completed with errors; missing/decompiler-failed bodies remain unknown.

Core binding: WebViewActivity instantiates WebViewFragment and binds it to its container. Fragment configures VideoEnabledWebView, registers handler/SohuJsBridge and its 34 annotated members, BaseWebViewClient and ProgressVideoEnabledWebChromeClient inheriting ng. VideoEnabledWebView overrides setWebChromeClient to enable JavaScript and lazily registers _VideoEnabledWebView during load methods. The empty inner bridge has no source-visible annotated member, so only registration is canonical. Passport WebViewActivity independently binds an Android WebView, settings and a ChromeClient. FarmExercise and Pgc factory overrides were discovered but are still unknown; base capabilities are not copied to them.

Raw source-first rows are preserved in `canonical-source-first-raw.jsonl`; after first scoring, registration/enum/dynamic fields were normalized for scorer revision 5 without removing facts. Dynamic value matching checks argument presence and cannot establish exact expression identity.
The 58 FarmExercise factory/inherited-bridge follow-up rows are separate in `canonical-additional-report-guided.jsonl` and explicitly marked report-guided; they are not a blind holdout.

Expanded follow-up: 169 report-guided rows, 4 total confirmed positive Activities, 262 combined source rows. The original canonical set remains unchanged. Additional rows have their own scoring file. Remaining major framework groups are unknown rather than exhaustively negative.

Latest follow-up totals 215 rows (308 combined rows), six capability-positive Activities plus Facebook dialog ownership-only (seven hosts reviewed). Advertising group has two separately proved host chains; its 46 shared settings/bridge/WebViewClient facts all match generic-v1. Whole-APK coverage, other SDK group minimums and exact-instance precision remain unproven. Five emitted noncore hosts were spot-checked: three correct conditional ownerships, two unresolved QQ-share dialog paths.

Generic-v2 final re-evaluation used the unchanged source-first and report-guided oracles: no previously matched fact lost and no new canonical match gained. See `generic-v2-comparison.md` and `generic-v1-v2-*-delta.json`. Sohu core/Farm/Pgc remain deadline-interrupted; txws retains all 168 row matches. This does not establish protocol acceptance.

## v3 continuation (partial source audit)

The same frozen development set still has settings 37/88, bridge 6/115 and callback 17/99 matches, with identical missing facts to v2. See generic-v3-comparison.md and generic-v2-v3-expanded-delta.json. The 598.8-second supervisor-terminated run produced valid JSON within the external 600 seconds, but internal analysis remains incomplete; three-run performance acceptance is unproven.

All 305 emitted Activity hosts are enumerated in generic-v3-all-host-ownership.jsonl: 12 source-supported valid and 293 unresolved. Newly reviewed SDK paths include Ali inherited layout, Alipay inherited pay/auth wrapper construction, and direct Weibo attachment. These additional partial audits do not raise the complete deep review count to 30 or prove source exhaustion.

The separate canonical-sdk-probes-report-guided.jsonl freezes 63 additional SDK settings probes after v3 report inspection; all 63 match v3. It is not merged into the earlier fixed development set, so its matching facts cannot conceal the earlier 242 missing facts. The SDK bridge and client inventories remain partial.

empty-virtual-api-override.md and generic-v{2,3}-empty-override-capability-precision.jsonl document six confirmed instrumentation bridge false positives per run on the concrete AuthWebView receiver, whose final addJavascriptInterface override is empty. The two Activities remain valid hosts. AuthWebView's map plus BridgeWebChromeClient's prompt/annotation/reflection dispatcher is a distinct bridge protocol and remains outside the audited injected-bridge gold; it does not validate the false NBS injection claims.
