# Breaking News 源码独立审计补全（v18候选）

Reviewer: GPT-6.1 Sol。APK SHA256 `2f815b97847ec9121ed301f1ed048e042f16c1926083ff8e19cd5c13f1f36dac`，本地APK重新sha256验证与build.json相符。source只读：`/HDD/d3008/WebViewBench/WebViewBench-predecompiled/breaking-news-1`。JADX exit1保留为失败，不将方法业务语义纳入核验。

全Manifest inventory列举71个activity；此为声明全集，不等于WebView-positive穷尽。70行三面闭合状态保留partial/unknown，AppLovinWebViewActivity三面done，deep positive为1；尚无30 deep positive或不足30的独立穷尽证明，每结构组至少2 deep亦未满足。旧canonical/source-first记录未删除。

追加150条hash-bound事实：60个ShakeWin辅助桥JavascriptInterface签名，50个Chrome wrapper回调事实（两个宿主各25），23个WebviewActivity API26+实际WebViewClient wrapper回调，两个dmh$c poster回调及11个ShakeWin有效继承/实现回调。完整DEX签名、平台normalized_api、继承链、实例来源与注册证据逐行保存。5辅助桥继承SecureJsInterface，其源头仅两个字段，无额外JavascriptInterface方法。Chrome实际dmh$c继承dmh$g；既有文档遗漏这层wrapper。

事实增量文件为source-closure-facts.jsonl，未改生产代码、未读取工具能力/得分/holdout、未执行重型分析或提交。下一批应补dmh$c的getDefaultVideoPoster、hkd Settings/helper最终值/条件、ShakeWin e→hkd$b→ikd覆盖与注销路径，再逐Manifest宿主正向链；现源头positive证据不等于深审闭合。

AppLovin闭合证明：AppLovinWebViewActivity-full-surface-source-proof.json。完整Activity、t4及精确工厂两个方法核验无相关反编译失败；直接平台实例、无桥或Chrome安装、两Settings值、两个有效Client回调、renderer销毁/重建与无普通lifecycle cleanup均记录。独立平台默认回调不计app override。

不可变snapshot：source-closure-v18-snapshot.jsonl，150行，SHA256 a286ced59a207e65610d69fa68bd568ade51757468c54b3f2432a2b50c7b8a83；元数据source-closure-v18-snapshot.json。只读权限0444；mutable事实留source-closure-facts.jsonl。

续审mutable追加14条DTB cached-instance已明确绑定事实，总164行（v18 snapshot仍150行未改）。amazon-cache-source-surface-proof.json保留三面partial：公开getAdView/setWebClient存在逃逸，尚未完成全部外部receiver调用闭合；未凭这14事实提升deep。全base DEX class_defs独立轻量probe确认48个Manifest类存在、23缺失，证据manifest-base-dex-class-definition-inventory.json；缺失仅针对base，不当作完整安装集negative。
