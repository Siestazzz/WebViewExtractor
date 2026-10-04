# xigua source-first audit

Reviewer: GPT-6.1 Sol。APK SHA-256 `6f95637b1730e39d493f67d398ab8b81d2ebe9ec41e66eccbee6d0a816fc532e`；scope `single APK`。源码root `/home/d3008/phy/workspace/WebViewGPT/webview-extractor/test/decompiled/xigua-1`，APK/build身份已核验；西瓜本地jadx exit1、4线程8g，其余预反编译build hash匹配。

全Manifest库存628个activities、77448个Java文件；入口词法库存见source-entry-inventory.jsonl。它们是候选清单，不是已验收覆盖。

目前深核5个开发宿主、55条可评分source-first事实，设置、绑定对象和callbacks均附源码位置hash。source-audit.json含deep positives及当前已证分层。尚未达到30正例宿主，未证明全部正例少于30；不能用事实条数替代宿主条数。尚未完整识别所有实际分层及其>=2宿主要求。

本次完全未读取该App提取器输出来建真值；未改生产代码。首批source-first-facts.jsonl冻结，后续源码扩展仍保持来源标识。所有开发host均与封存10host不重叠。holdout创建时机/selection hash见holdout-seal.json，事实内容尚未分析且未用于修复。

尚欠全输出activity_host归属审核、capability_precision、最终评分和holdout truth/evaluation；这些不从当前小样本匹配推定。

源码独立扩展：Turing SDK 两个 XML 自定义 WebView 宿主，共新增 28 条事实（设置及 androidJsBridge 的构造注入与3方法）；当前83事实/7宿主。TuringVerifyWebView 直接继承 Android WebView，不能与 VerifyWebView 混用继承链。仍不足30宿主，未构成完整验收。原 source-first/development 冻结快照不变。

覆盖口径纠正：source-positive仅证明宿主有真实View绑定，不等于完成该宿主全Bridge/Settings/Client表面深审。host-deep-audit-coverage.jsonl逐宿主三类别记partial/unknown；deep_positive_activities暂为空集合，source_positive_activities单列。旧数值deep计数已纠正，不作为验收证据。
