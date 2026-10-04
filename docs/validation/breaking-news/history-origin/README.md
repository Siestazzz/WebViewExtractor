# Breaking News fix base 源码事实

Reviewer: GPT-6.1 Sol。APK SHA-256: `0cdeb638b0f85e908d7704ccd5b8b46ef9fccaeadb14f82024da6433167c046e`。本地源码 `test/decompiled/breaking-news-1-origin` 来自fix/com.news.app.global.apk；原预反编译匹配vuln base，未复用其结论。仅base，未合并动态/配置split，不代表完整安装集。jadx完成exit1，有反编译错误。

已源码核验两个Manifest宿主：WebviewActivity→new hkd→yt7→WebView；ShakeWinMainActivity→new hbe→new ije→new hkd。两条构造链均进入hkd Settings配置，确认JS、DOM storage、mixed content等字面值。

yt7.setWebViewClient经dmh.j包装：API<26或dmh$d实例原样返回，否则new dmh$f(client)；f转发callbacks并兜底render-process-gone。WebviewActivity绑定平台WebViewClient，API26+拥有wrapper回调。ShakeWin的ije$e→hkd$b→ikd实现dmh$d，走原样返回分支，确认继承的onPageFinished、onReceivedSslError和shouldInterceptRequest。

shakewin与shakewin_report注册对象分别为hbe$a/$b，包装u8e/w8e并提供8个JavascriptInterface入口。SecureJsInterface只有两个字段，没有继承JS方法；yt7$a条件注册opera_action_select_interface，需要搜索host/feature条件，未作为无条件事实评分。hbe另注册FreeData/opera_social/news_data_h5/operamini_clientInfoJsApi/ucMobileSdk，尚未穷举成员。

canonical事实附文件line/text/hash、宿主证据和reviewer。inventory词法计数是候选规模，不是验收范围。其余宿主、SDK、所有重载/Chrome回调尚未全审；不能宣称全部验收。未读取提取器输出、未改生产代码。
