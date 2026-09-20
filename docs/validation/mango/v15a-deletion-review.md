# v15a deletion review

I normalized each removed row by kind, API and callsite, actual WebView type, registration name, implementation, and sorted exposed member signatures. Receiver IDs and argument object IDs were excluded. This distinguishes repeated aliases from a lost capability while retaining differences in endpoint semantics.

InteractVod's one removed `closeGamePanel` row is an alias duplicate: an identical normalized registration remains in v15a. The same holds for all eight MGVideoPlay removals. Its four handler names (`showCustomShareMenus`, `getReportInfo`, `joinQQGroup`, and `openWXApplet`) and its four operation callsites remain on another receiver identity. VideoSquare's eight removed registrations likewise all have retained equivalents with the same names, implementation, endpoint signatures, callsite, and `ImgoWebView` type. These three hosts show receiver alias consolidation rather than capability loss.

ErlangLive is different. Of 500 removed and 306 added receiver-specific rows, normalization reduces the churn to 197 removed and three added multiplicities. Almost all removed semantic keys still occur at least once in v15a, so their count reduction is alias consolidation. Two unique registrations disappear completely:

- `mgTransfer` → `Lcom/mgtv/h5/ImgoWebJavascriptImpl;->mgTransfer(Ljava/lang/String;)V`
- `showMangPushShare` → `Lcom/mgtv/h5/ImgoWebJavascriptImpl;->showMangPushShare(Ljava/lang/String;)V`

These are real capability regressions. `ErlangLiveActivity` owns `fullWebView` and `detailWebView` as `WebUIFragment`; `WebUIFragment.initWebView_aroundBody8` initializes its `ImgoWebView`; and `ImgoWebView.java:666-667` explicitly calls `registerHandler("mgTransfer", this)` and `registerHandler("showMangPushShare", this)`. `ImgoWebJavascriptImpl` declares both exact one-String endpoint methods. Neither normalized registration remains anywhere in the v15a Erlang host output.

The conclusion is independent of canonical coverage: three hosts only shed duplicate aliases, while Erlang loses two source-proven registrations amid otherwise beneficial identity consolidation.
