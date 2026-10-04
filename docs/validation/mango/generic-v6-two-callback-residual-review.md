# Generic v6 two callback residuals

Comparing identical frozen oracle hashes in generic-v3-mango-frozen-score.json and generic-v6-mango-frozen-score.json finds exactly two callback rows that v3 matched and v6 does not: ThirdWebActivity and ThirdFullWebActivity each require `WebFragment$c.shouldOverrideUrlLoading(WebView,String)Z` on ProgressWebView. Other v3/v6 callback differences are not these two residuals. v6 restored 58 of v4's 60 callback losses.

The frozen v3 report matched this member by emitting the original ProgressWebView.setWebViewClient invocation at WebFragment.b(String)@21 and enumerating the supplied WebFragment$c's full callback surface. That native setter effect was emitted before the actual override, so the match did not prove that the installed wrapper called this particular delegate callback.

The previously authorized Mango development source explains the distinction. ProgressWebView.setWebViewClient at 467-468 stores the user client through its holder setter; WebClientImpl.setWebViewClient at 811-812 only assigns mWebViewClient. The system-installed client is WebClientImpl$a, extending MoWebViewClient. Its onLoadResource/onPageFinished/onPageStarted methods at 438-475 actually read mWebViewClient and call corresponding delegates, consistent with the v6 recorded delegation sites and object identity.

However, WebClientImpl$a.shouldOverrideUrlLoading(WebView,WebResourceRequest) at 513-517 calls its own String overload. Its String overload at 527-617 handles URL routing and calls only its superclass fallback; it never reads mWebViewClient or invokes that stored user's shouldOverrideUrlLoading. MoWebViewClient extends Android WebViewClient and declares no shouldOverrideUrlLoading method. WebFragment$c's String override at 148-150 exists but is not reached through this installed wrapper callback path. Merely storing a Client containing that member is insufficient to expose that callback.

These two residuals therefore have source evidence for an unsupported golden delegation claim on the reviewed path, rather than evidence that v6 must re-enable the original parent setter effect. This review does not assert absence of every alternative dynamic path in the APK. No canonical rows or source code are changed; the coordinating reviewer should adjudicate the golden rows explicitly and preserve the frozen historical scores.

Source root: `test/decompiled/com.hunantv.imgo.activity/sources/`. APK SHA-256 `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5`.

Source SHA-256:

- ProgressWebView.java: `4f9a3b806a326a6ab5495d46326739a389e1ac6170c28d4ffbeca9cf4a9c31b1`.
- WebClientImpl.java: `d5580f58ceae24fc12ab3a7fbcf5a52cfc6e39185092f8a8ac6ac1697ae5c5bb`.
- WebFragment.java: `4b52ed807117e62bf0d814b2d125b4b24d7e736f627e880b10e71f2f695bc653`.
- MoWebViewClient.java: `17f8971a663bed65820260f52658c1d8602bcd0450048066323fff09f0f460af`.

Frozen reports: `test/runs/generic-v3/com.hunantv.imgo.activity/capabilities.json` and `test/runs/generic-v6/com.hunantv.imgo.activity/capabilities.json`. The existing source-audit document `two-host-expansion.md` lists these two delegate rows but its descriptor membership check proves their existence, not this missing installed-wrapper invocation edge.
