# generic-v1 emitted-host ownership spot check

Five report-guided, bridge-heavy noncore hosts checked: three correct conditional ownerships and two unresolved share-dialog paths. None is declared wrong solely because its Activity has no direct WebView field. This is 5/299 emitted hosts, not an all-output precision estimate.

`com.sohu.app.ads.sdk.common.widget.webview.SohuVideoDetailPageActivity`: **correct**. Activity onCreate obtains CustomDetailPageView from its own layout and invokes setAd; CustomDetailPageView inflates its child SohuWebView and setAd loads its URL. This proves conditional source ownership for the ad-detail WebView, not correctness of every emitted bridge/callback.

`com.facebook.FacebookActivity`: **correct**. onCreate calls getFragment; action FacebookDialogFragment constructs and shows a DialogFragment tied to this Activity manager. That fragment builds WebDialog with getActivity, whose setUpWebView constructs custom WebView. Conditional dialog host is source-supported; all-output capability precision not established.

`com.sohu.sohuvideo.ui.BytedanceSchemeFilterActivity`: **unresolved**. Small redirect Activity dispatches action c(this,...).N then finishes. Emitted bridges are QQ share TDialog through asynchronous share callbacks, not an onCreate layout. Action dispatch f0 passes the caller Context into TencentShareClient, which later casts it to Activity and QQShare may show TDialog(activity). Static context chain exists, but after finish/async timing and branch conditions were not exhaustively validated; lack of direct WebView is not treated as negative evidence.

`com.sohu.sohuvideo.wxapi.WXEntryActivity`: **unresolved**. onReq dispatches c(this,str).N; emitted bridges use the same QQ share dialog branch as redirect Activity. Caller context is plausible but only the selected share path was checked; per-fact branch and live dialog ownership remain unresolved, not wrong from absence of direct fields.

No selected emitted fact carried markFragmentsCreated evidence; no claim about that lifecycle family is made.

`com.sohu.app.ads.sdk.common.widget.webview.SohuAdActivity`: **correct**. onCreate constructs SohuTitleWebView(this), installs it via setContentView and loads URL. Title wrapper owns SohuProgressWebView; progress wrapper constructs SohuWebView with the same context and adds it as child. Conditional ad host binding is correct; per-capability precision remains unchecked.
