# freereels source-first audit

Reviewer: GPT-6.1 Sol。APK SHA-256 `8c022d3feec4e3699d27230d46c2e65cd8e32a1b8c2e223ab47d204a2b6bf080`；scope `base APK only`。源码root `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/FreeReels_2.3.11`，APK/build身份已核验；西瓜本地jadx exit1、4线程8g，其余预反编译build hash匹配。

全Manifest库存164个activities、67295个Java文件；入口词法库存见source-entry-inventory.jsonl。它们是候选清单，不是已验收覆盖。

目前深核4个开发宿主、26条可评分source-first事实，设置、绑定对象和callbacks均附源码位置hash。source-audit.json含deep positives及当前已证分层。尚未达到30正例宿主，未证明全部正例少于30；不能用事实条数替代宿主条数。尚未完整识别所有实际分层及其>=2宿主要求。

本次完全未读取该App提取器输出来建真值；未改生产代码。首批source-first-facts.jsonl冻结，后续源码扩展仍保持来源标识。所有开发host均与封存10host不重叠。holdout创建时机/selection hash见holdout-seal.json，事实内容尚未分析且未用于修复。

尚欠全输出activity_host归属审核、capability_precision、最终评分和holdout truth/evaluation；这些不从当前小样本匹配推定。

覆盖口径纠正：source-positive仅证明宿主有真实View绑定，不等于完成该宿主全Bridge/Settings/Client表面深审。host-deep-audit-coverage.jsonl逐宿主三类别记partial/unknown；deep_positive_activities暂为空集合，source_positive_activities单列。旧数值deep计数已纠正，不作为验收证据。

Taurus native SDK framework two-host deep audit completed: TaxWebViewActivity and TaxBrowserActivity (exact real p9.b synthetic touch listener class proven from DEX). Full settings include reflected constant display-zoom false and dynamic cache-directory expression; all standard/custom Client/Chrome/touch entry surfaces checked; bridge absence proven for both. Current166 rows,18source-positive,3deep (nativefragment1 + SDKdirect2). This is not complete164Manifest coverage nor acceptance. Unity settings runtime reflection namespace remains a separate unresolved surface and was not silently used as a deep-complete substitute.

MraidBrowser complete source surface: native construction/attachment, four observed settings, directly installed Client4 and Chrome1, no bridge injector and no late config in lifecycle. Canonical173 facts /18source-positive /4deep; this remains incomplete Manifest coverage and does not establish full-app acceptance. Evidence: mraid-browser-full-surface-source-proof.json.

Unity four existing hosts: independently reviewed all WebPlayerView declared client bodies and direct actual installs, plus reflection settings receiver/keys/types/values/cache/late exposed API chain. Added constructor enum OFF/NORMAL and exact standard Client/Chrome/Download full signatures; SafeDK renamed helper bodies are not extra callbacks. Open runtime JSON reflection stays unresolved without fabricated normalized APIs or values; four hosts remain partial, including pending actual anonymous layout-listener identity. Source inventory: unity-runtime-reflection-and-installed-client-source-inventory.json. Current 337 facts /18source-positive /4deep, not whole-App acceptance.

Direct APK DEX confirms all36 standard installed member signatures, actual WebPlayerView$1 OnLayoutChangeListener identity/interface/member and annotated bridge handleEvent descriptor. Added8 layout registration/member facts across4hosts; canonical345. Anonymous identity unresolved item closed; open runtime method namespace still partial, deep4 unchanged. Proof: unity-installed-members-source-dex.json.

OfferWallActivity all three surfaces closed: FyberSDK/self single annotated endpoint, four reachable observedSettings, installed real DEXj Client own5+inheritedxw1 andgm Chrome1. C5423j is a JADX renamed filename, actual ownerj verified from APK DEX. SDK<20/<19 two helper setting declarations are unreachable on manifestmin23, saved source nonpositive evidence; Cookie acceptThirdPartyCookies is a getter. Current358 facts /18source-positive /5deep, wholeManifest164 coverage remains incomplete. Source proof: offerwall-full-surface-source-proof.json.
