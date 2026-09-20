# Nineteen-host capability audit

Each entry names the concrete carrier reached from the Activity. “Conditional” means the construction branch is URL, ad-format, payment-flow, or runtime-SDK dependent. DEX owners and signatures must be accepted only when present in `test/runs/symbols/mango.jsonl`.

| Activity | concrete carrier / chain | capability source | status and exact gap |
|---|---|---|---|
| XWebActivity | XWebViewFragment → XWebView → xweb BridgeWebView | XWebView.java; xweb/browser/RootWebView.java; xweb/jsbridge/BridgeWebView.java and JSInterface.java | settings/clients/`jsobj.callNative` located; conditional X5 versus system-WebView delegate must be emitted as separate identities |
| WebContainerActivity | mgsz WebViewFragment → mgsz BridgeWebView | mgsz/h5/WebViewFragment.java; mgsz/h5/jsbridge/BridgeWebView.java | bridge transport located; fragment factory arguments resolved; callback owner names require DEX-symbol lookup because JADX uses short names |
| LoadingWebActivity | FragmentWebLoadingBase/CommonWebFragment → WebView | platform/oms/ui/LoadingWebActivity.java and base fragment | direct cache setting and JS-prompt/client callbacks located; base fragment implementation remains split by OAuth/loading subclass |
| mglive WebViewActivity | mglive BridgeWebView | activity line 129; mglive/h5/jsbridge/BridgeWebView.java; live/webview/a.java | direct carrier; settings/client installer is helper `a`, conditional on live page setup |
| LandWebActivity | webbase BaseWebView → webbase BridgeWebView | LandWebActivity.java:134–138; webbase/hybird/BaseWebView.java; webbase/jsbridge/BridgeWebView.java | wrapper resolved; bridge and clients located; configuration includes both wrapper defaults and Activity replacement client |
| CustomWebActivity | Mgadplus ImgoAdWebView | CustomWebActivity.java:176–185; ImgoAdWebView.java | direct field resolved; full settings inherited from ad WebView; Activity lifecycle client is concrete nested `c` |
| ThirdWebActivity | WebFragment → ProgressWebView → DWebView | ThirdWebActivity.java:87; wallet WebFragment/ProgressWebView/DWebView | DSBridge group; `_dsbridge` transport and annotated default API are conditional on wallet fragment initialization |
| ThirdFullWebActivity | BaseFragment → ProgressWebView → DWebView | ThirdFullWebActivity.java:58; wallet BaseFragment/ProgressWebView/DWebView | same DSBridge shared group; different host fragment edge |
| OPOS AdWebActivity | OPOS BaseWebActivity → biz web component | activity superclass; opos/cmn/biz/web | factory return is WebView; settings/client/bridge map belongs to returned instance, conditional on ad request |
| OPOS FeedBackWebViewActivity | cmn.module.ui.WebViewActivity wrapper | subclass and module WebViewActivity | inherited wrapper resolved; module helper owns configuration |
| OPOS module WebViewActivity | module UI helper `a` | WebViewActivity.java:126–146 and helper implementation | direct helper field; concrete WebView is constructed inside helper |
| TTWebPageActivity | SSWebView | activity field and setup around lines 348–420 | direct Pangle group; settings plus installed WebViewClient/ChromeClient and landing-page bridge conditional on material |
| TTPlayableWebPageActivity | SSWebView | activity setup and adexpress renderer | two conditional SSWebView paths (page and playable renderer); must not merge identities |
| TTVideoWebPageActivity | SSWebView | activity field line 178 and client line 447 | direct Pangle video-page group |
| TTVideoScrollWebPageActivity | inherited TTVideoWebPageActivity | subclass declaration → parent field/setup | inherited complete group; subclass adds no independent WebView |
| Huawei PayWebviewActivity | BaseWebViewActivity.mWebView → PaySafeWebView | PayWebviewActivity superclass/field and MyWebViewClient | inherited payment WebView; client owner resolved; superclass settings require symbols because some methods are vendor wrappers |
| Alipay H5AuthActivity | H5PayActivity → H5PayResultActivity/WebView helper | subclass declaration and Alipay web helper | inherited conditional H5 flow; bridge removals are hardening calls, not exposed bridge members |
| Alipay H5OpenAuthActivity | H5PayActivity → H5PayResultActivity/WebView helper | subclass declaration and Alipay web helper | same shared group with distinct launch mode |
| UBiXWebViewActivity | UBiX wrapper `c` → android.webkit.WebView | activity lines 107–108 and wrapper class | direct wrapper/client owners located; six bridge removals are not bridge exposures |

## Remaining mechanical work

The ownership/carrier resolution above is complete for all nineteen. Exact truth-row expansion is blocked only where a method owner is represented by a JADX short/anonymous name or a vendor wrapper method: resolve each against `mango.jsonl`, then emit only symbol-table signatures. Pangle playable paths and XWeb engine alternatives must remain conditional and retain separate WebView identities. Bridge-removal APIs must never count as exposed members.
