# 通用桥注册链漏检（partial checkpoint）

源码真值来自匹配patched APK；报告仅用于定位漏检，不建立真值。ReadingWebView构造new Handler(this)，调用z→handler.c→private a，holder保存WebView参数到字段，最后以new GlobalProps和字面量__globalProps调用addJavascriptInterface。证据位置/hash见generic-bridge-defect.json。

报告WebViewActivity仅出现Webx API wrapper中的4条桥，name/implementation均unknown，参数为entry_parameter；真实handler注入点未出现。尚未检查生产代码，根因假设是constructor-created holder字段/跨对象初始化调用追踪丢失，需要通用fixture复现。

```java
class V extends WebView {
  Handler holder;
  V(Context c) { super(c); holder = new Handler(this); init(); }
  void init() { holder.register(); }
}
class Handler {
  WebView view;
  Handler(WebView v) { view = v; }
  void register() { inject(); }
  void inject() { view.addJavascriptInterface(new Bridge(), "props"); }
}
class Bridge { @JavascriptInterface public String value() { return "v"; } }
```

通用预期：构造字段holder保持对象身份；holder.view绑定V实例；register/inject派发到holder实际类；桥名props及Bridge.value归属宿主的同一V。

另有oracle纠正：冻结事实限制type ReadingWebView遗漏预加载d→e→ReadingWebView子类。settings实际已检出，其0/8冻结分数不能作为代码漏检或后续代码提升。canonical加入源码已证子类类型，source-first保持原始副本。

更新：报告在源码核验期间更新。最新expanded partial评分39/39，包括WebViewActivity的__globalProps，早期漏检观测目前不再成立。两个report hash与最新bridge site记录于JSON。不能将早期观测直接认作生产根因；需要fixture或最终稳定报告进一步确认。
