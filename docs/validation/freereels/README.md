# freereels source-first audit

Reviewer: GPT-6.1 Sol。APK SHA-256 `8c022d3feec4e3699d27230d46c2e65cd8e32a1b8c2e223ab47d204a2b6bf080`；scope `base APK only`。源码root `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/FreeReels_2.3.11`，APK/build身份已核验；西瓜本地jadx exit1、4线程8g，其余预反编译build hash匹配。

全Manifest库存164个activities、67295个Java文件；入口词法库存见source-entry-inventory.jsonl。它们是候选清单，不是已验收覆盖。

目前深核4个开发宿主、26条可评分source-first事实，设置、绑定对象和callbacks均附源码位置hash。source-audit.json含deep positives及当前已证分层。尚未达到30正例宿主，未证明全部正例少于30；不能用事实条数替代宿主条数。尚未完整识别所有实际分层及其>=2宿主要求。

本次完全未读取该App提取器输出来建真值；未改生产代码。首批source-first-facts.jsonl冻结，后续源码扩展仍保持来源标识。所有开发host均与封存10host不重叠。holdout创建时机/selection hash见holdout-seal.json，事实内容尚未分析且未用于修复。

尚欠全输出activity_host归属审核、capability_precision、最终评分和holdout truth/evaluation；这些不从当前小样本匹配推定。
