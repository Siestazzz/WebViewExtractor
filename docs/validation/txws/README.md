# txws independent source audit

Current input is the patched APK explicitly selected by the user. SHA256: `199a194170a2fcb7e3d9261b77deaaa1d803289fc627c9f75bc68af8652a70d5`. Predecompiled build hash matches metadata patched hash and the copied APK. The earlier original-APK manifest inventory is superseded in `history-origin/` and contributes no current canonical facts.

Source root: `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/txws-1`. Reviewer: GPT-6 Codex (exact runtime model identifier unavailable). The first 99 facts across 3 confirmed Activities were derived before reading extractor outputs; `source-first-freeze.json` records their hash. Every fact includes source locations, host evidence, APK hash and reviewer.

The manifest inventory contains 246 entries; the 55 source registration files are search inventory, not verified Activity ownership. This is an initial development audit, not protocol acceptance: fewer than 30 positive hosts reviewed, no exhaustive negative search, final holdout or all-output precision audit. JADX completed with errors; missing/decompiler-failed bodies remain unknown.

Core binding: WebviewBaseActivity creates WebViewBaseFragment; onCreateWebView returns cached ScrollObservableWebView, inheriting CustomWebView and X5 WebView. onWebViewCreated installs clients and settings. Anonymous Chrome overrides inherit onJsPrompt from CustomWebChromeClient. MainProcessWebviewActivity inherits the complete lifecycle, and NotifyDialogWebViewActivity explicitly calls super.onCreate. Implicit plugin engine dispatch is inventoried, but full plugin member membership is not yet canonical.

Raw source-first rows are preserved in `canonical-source-first-raw.jsonl`; after first scoring, registration/enum/dynamic fields were normalized for scorer revision 5 without removing facts. Dynamic value matching checks argument presence and cannot establish exact expression identity.
