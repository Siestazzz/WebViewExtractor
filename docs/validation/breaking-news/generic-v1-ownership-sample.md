# Generic-v1 宿主归属抽样

审核3个非核心输出Activity，各选一条FreeData桥能力：PreviewActivity、HiddenActivity、SignInHubActivity，均为wrong能力归属。此结论针对抽样能力，不表示这些Activity绝对没有任何WebView，也不代表整体误报率。报告进程结束，但报告status仍partial且部分事实analysis_stage=previous_phase_provisional。APK/report及源码hash、具体line/text见JSONL。

源码确认两种synthetic Runnable共性：final int tag在构造时保存已知常量；run按tag切换互斥逻辑。Fragment创建rb2(tag=2)并存进生命周期owner，owner调用run，case2调用Fragment.lambda$performCreateView$0；报告却进入case3新闻流g$i.l0继而桥。AndroidComposeView创建oz(tag=0)，case0处理hover事件并return；报告却进入case2 GraphRequest链继而桥。

```java
final class R implements Runnable {
  final int tag; final Object target;
  R(Object target, int tag) { this.target=target; this.tag=tag; }
  public void run() {
    switch(tag) {
      case 0: ((Safe)target).safe(); return;
      case 1: ((Other)target).web.addJavascriptInterface(new Bridge(), "props"); return;
    }
  }
}
class Host { void init() { new R(new Safe(), 0).run(); } }
```

预期Host不拥有case1桥能力。应保留实际构造对象tag字段并选择可行switch分支；禁止因错误输出而按App/Activity名字删除宿主。已将证据和最小结构发genericize_engine。
