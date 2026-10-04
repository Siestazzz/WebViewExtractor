# yangshipin source-first audit

Reviewer: GPT-6.1 Sol。APK SHA-256 `16ce4954ebbf29effe54c73736863acbd78808895a6be2cfff71ceddc81bad1a`；scope `single APK`。源码root `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/ysp-1`，APK/build身份已核验；西瓜本地jadx exit1、4线程8g，其余预反编译build hash匹配。

全Manifest库存229个activities、28618个Java文件；入口词法库存见source-entry-inventory.jsonl。它们是候选清单，不是已验收覆盖。

目前深核5个开发宿主、50条可评分source-first事实，设置、绑定对象和callbacks均附源码位置hash。source-audit.json含deep positives及当前已证分层。尚未达到30正例宿主，未证明全部正例少于30；不能用事实条数替代宿主条数。尚未完整识别所有实际分层及其>=2宿主要求。

本次完全未读取该App提取器输出来建真值；未改生产代码。首批source-first-facts.jsonl冻结，后续源码扩展仍保持来源标识。所有开发host均与封存10host不重叠。holdout创建时机/selection hash见holdout-seal.json，事实内容尚未分析且未用于修复。

尚欠全输出activity_host归属审核、capability_precision、最终评分和holdout truth/evaluation；这些不从当前小样本匹配推定。

覆盖口径纠正：source-positive仅证明宿主有真实View绑定，不等于完成该宿主全Bridge/Settings/Client表面深审。host-deep-audit-coverage.jsonl逐宿主三类别记partial/unknown；deep_positive_activities暂为空集合，source_positive_activities单列。旧数值deep计数已纠正，不作为验收证据。

Source-first shared-surface expansion: 8 H5 hosts now include 21 initialization settings plus first UA setter (empty constructor UA input selects existing-UA/version branch), and 18 effective custom Client/Chrome callback signatures. Dynamic paths and UA preserve source expressions without wildcard value acceptance. Whole-host deep completion remains pending custom host entry overrides and SDK initialization/wrapper branches. Original frozen source facts are retained.

Early safe-client lifecycle audit adds conditional X5SafeWebViewClient registration before Mtt replacement: config gate + SDK >=26 + null/platform-default current client. Its 21 custom members are recorded, including onRenderProcessGone returning true without delegate forward. Total current canonical facts: 530; source-positive hosts: 11; deep-complete hosts: 0 pending the documented whole-host closure. Evidence line/text/SHA validation passed across 33 source files; holdout hosts remain excluded.
