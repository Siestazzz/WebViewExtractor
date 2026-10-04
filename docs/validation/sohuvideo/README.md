# sohuvideo independent source audit

Current input is the patched APK explicitly selected by the user. SHA256: `07780de1d6d08e62da19fc2a98eaa4f53796cf7d3f02d8c0bebb02f3b3aa07a2`. Predecompiled build hash matches metadata patched hash and the copied APK. The earlier original-APK manifest inventory is superseded in `history-origin/` and contributes no current canonical facts.

Source root: `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/sohuvideo-1`. Reviewer: GPT-6 Codex (exact runtime model identifier unavailable). The first 93 facts across 2 confirmed Activities were derived before reading extractor outputs; `source-first-freeze.json` records their hash. Every fact includes source locations, host evidence, APK hash and reviewer.

The manifest inventory contains 407 entries; the 51 source registration files are search inventory, not verified Activity ownership. This is an initial development audit, not protocol acceptance: fewer than 30 positive hosts reviewed, no exhaustive negative search, final holdout or all-output precision audit. JADX completed with errors; missing/decompiler-failed bodies remain unknown.

Core binding: WebViewActivity instantiates WebViewFragment and binds it to its container. Fragment configures VideoEnabledWebView, registers handler/SohuJsBridge and its 34 annotated members, BaseWebViewClient and ProgressVideoEnabledWebChromeClient inheriting ng. VideoEnabledWebView overrides setWebChromeClient to enable JavaScript and lazily registers _VideoEnabledWebView during load methods. The empty inner bridge has no source-visible annotated member, so only registration is canonical. Passport WebViewActivity independently binds an Android WebView, settings and a ChromeClient. FarmExercise and Pgc factory overrides were discovered but are still unknown; base capabilities are not copied to them.

Raw source-first rows are preserved in `canonical-source-first-raw.jsonl`; after first scoring, registration/enum/dynamic fields were normalized for scorer revision 5 without removing facts. Dynamic value matching checks argument presence and cannot establish exact expression identity.
The 58 FarmExercise factory/inherited-bridge follow-up rows are separate in `canonical-additional-report-guided.jsonl` and explicitly marked report-guided; they are not a blind holdout.
