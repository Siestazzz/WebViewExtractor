# Fanqie patched 源码事实

Reviewer: GPT-6.1 Sol。APK SHA-256 `0273a075d7835d7d0d3367f06b717266aefbb7a428975b1cf7e6874c6b557b9f`，匹配metadata vuln/fanqie_7.2.7.32_patched.apk与预反编译build.json。原先fix/origin选择已撤销，原版jadx停止exit130，没有使用原版事实。

已核验Manifest WebViewActivity，经SwipeRefreshWebViewLazyLayout.c得到SwipeRefreshWebView，再经ReadingWebViewPlaceHolder和NsCommonDependImpl.createReadingWebView构造ReadingWebView（fallback分支；另有预加载实例分支）。继承ReadingWebView→webx.c→WebViewContainer→WebViewContainerInner→android.webkit.WebView。Settings在构造z→s调用，确认JS/DOM/FileAccess/Database/mixed content等。

ReadingWebView构造GlobalPropsHandler(this)，z→handler.c→a条件注册__globalProps，五个JavascriptInterface返回String。props非null且tag为空是注册条件，不能据此推定所有运行时URL都注册。Bridge并非l34.d；l34.d继承t11.d，是privilege service实现，patched b(String)相关授权逻辑需单独分析。

SwipeRefreshWebView绑定c客户端；ReadingWebView.setWebViewClient非null分支new m，m继承o→r64.j，保存delegate字段f364446a，r64.j转发到原客户端。确认c的page/ssl/error回调与m的pageFinished/request-intercept入口。Webx内层进一步包装、Chrome链、所有SDK/宿主与桥成员尚未穷举。

canonical附源码位置hash和宿主链证据。inventory词法文件计数仅候选规模，不能视为完整验收。未读取提取器输出、未修改生产代码。

Report-guided扩展：ExternalWebActivity→new WebFragment→inflate aun.xml→ReadingWebView#dar，确认resource构造Settings/GlobalProps链及WebFragment$b绑定回调。现核验2宿主，39条canonical事实，低于30宿主，未遍历全部733 Manifest activities。冻结source-first-facts为原20条。类型约束纠正另见generic-bridge-defect.md。

Report-guided再扩展：mobile_auth.webview.WebViewActivity直接new WebView，确认5项设置（file access=false）与5个b回调（SSL cancel），现3宿主49事实。
