# News generic-v10: all output Activity ownership

All 33 output Activities are included, including candidate bindings: **32 valid, 0 wrong, 1 unresolved**. Wrong plus unresolved is **1/33 = 3.03%**. This is Activity host ownership, not capability precision or acceptance. There is no candidate exclusion.

APK SHA-256: `c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`. Report SHA-256: `aee135f33864a2a5a68010867b255c1f016aafd58f84bb08344a18f8855cf208`. The source build.json uses the same APK hash. The JSONL binds every row to this report, actual output count and receiver types, concrete ownership-chain references and freshly hashed source bytes/quoted locations (62 unique source files, 2020 quoted locations).

For the 32 previously established hosts, the audit explicitly reuses `v16e-ownership.jsonl` and its same-APK independent source chains. Current source locations and hashes were validated; this is not a claim that every full implementation was freshly audited. Existing annotations about mixed or incomplete capability precision remain separate. In particular, valid editor/login/player hosts do not validate unrelated capture/report/dialog alternatives or restore their missing capability surface.

The new `SplashAdDynamicPreviewActivity` remains unresolved. Its posted runnable constructs, retains, adds and shows SplashAdDynamicView using the actual Activity; the view extends SplashAdView. However, the emitted EasterEgg path invokes preloadWebView with **AdCoreUtils.CONTEXT**, which is set from getApplicationContext. It creates a singleton controller-owned WebView; this path does not establish an attachment or owner transfer to the PreviewActivity. The bonus-page path launches another Activity. Other dynamic Mosaic WebView possibilities have not been exhausted, so this is not a source-negative Activity verdict.

Frozen oracle files were not changed. The numeric ownership ratio satisfies the requested ten-percent bound for this report; it does not imply per-category recall, capability precision, 30 complete positives, holdout or performance acceptance.

| Activity | Verdict | Output facts | Review |
|---|---|---:|---|
| com.tencent.ams.adcore.gesture.AdGyrosEasterEggActivity | valid | 18 | reuse with current hash/location validation |
| com.tencent.news.login.module.security.SecurityTicketActivity | valid | 31 | reuse with current hash/location validation |
| com.tencent.news.startup.privacy.PrivacyWebActivity | valid | 39 | reuse with current hash/location validation |
| com.tencent.news.startup.privacy.PrivacyWebItemActivity | valid | 38 | reuse with current hash/location validation |
| com.tencent.news.tad.business.novel.WebNovelActivity | valid | 80 | reuse with current hash/location validation |
| com.tencent.news.tad.business.ui.activity.CustomWebGameForItemActivity | valid | 492 | reuse with current hash/location validation |
| com.tencent.news.tad.business.ui.activity.WebAdvertActivity | valid | 223 | reuse with current hash/location validation |
| com.tencent.news.ui.SupportActivity | valid | 46 | reuse with current hash/location validation |
| com.tencent.news.webview.CustomWebBrowserForItemActivity | valid | 545 | reuse with current hash/location validation |
| com.tencent.news.webview.HalfTencentVideoWebActivity | valid | 490 | reuse with current hash/location validation |
| com.tencent.news.webview.TencentVideoWebActivity | valid | 487 | reuse with current hash/location validation |
| com.tencent.news.webview.WebDetailActivity | valid | 144 | reuse with current hash/location validation |
| com.tencent.ams.splash.preview.SplashAdDynamicPreviewActivity | unresolved | 16 | fresh guided; remaining source surface unknown |
| com.tencent.ilive.authcomponent.LiveAuthActivity | valid | 156 | reuse with current hash/location validation |
| com.tencent.ilive.pages.activity.AuthWebActivity | valid | 156 | reuse with current hash/location validation |
| com.tencent.ilivesdk.webcomponent.activity.SingleTransparentTitleWebActivity | valid | 156 | reuse with current hash/location validation |
| com.tencent.ilivesdk.webcomponent.activity.TransparentTitleWebActivity | valid | 156 | reuse with current hash/location validation |
| com.tencent.ilivesdk.webcomponent.activity.WebActivity | valid | 156 | reuse with current hash/location validation |
| com.tencent.midas.jsbridge.APWebJSBridgeActivity | valid | 34 | reuse with current hash/location validation |
| com.tencent.news.activity.QAEditorActivity | valid | 62 | reuse with current hash/location validation |
| com.tencent.news.activity.RichEditorActivity | valid | 62 | reuse with current hash/location validation |
| com.tencent.news.kkvideo.detail.KkAlbumDarkModeActivity | valid | 29 | reuse with current hash/location validation |
| com.tencent.news.kkvideo.detail.KkVideoDetailDarkModeActivity | valid | 29 | reuse with current hash/location validation |
| com.tencent.news.login.module.LoginActivity | valid | 83 | reuse with current hash/location validation |
| com.tencent.news.login.module.LoginWithBackgroundActivity | valid | 83 | reuse with current hash/location validation |
| com.tencent.news.login.module.LoginWithPhoneNumActivity | valid | 83 | reuse with current hash/location validation |
| com.tencent.news.login.module.LoginWithVerCodeActivity | valid | 83 | reuse with current hash/location validation |
| com.tencent.news.share.activity.MobileQQActivity | valid | 27 | reuse with current hash/location validation |
| com.tencent.news.share.activity.QzoneShareActivity | valid | 51 | reuse with current hash/location validation |
| com.tencent.news.tad.business.ui.activity.WebAdvertActivityV2 | valid | 18 | reuse with current hash/location validation |
| com.tencent.news.topic.weibo.detail.graphic.WeiboGraphicDetailActivity | valid | 29 | reuse with current hash/location validation |
| com.tencent.news.ui.NewsDetailActivity | valid | 1296 | reuse with current hash/location validation |
| com.tencent.news.ui.PushDetailActivity | valid | 1304 | reuse with current hash/location validation |
