# WebViewExtractor

从 Android APK 提取 Activity → WebView → Bridge、Settings、Client/回调能力画像。默认实现使用 DEX 索引、按需方法摘要和对象绑定；先给全部 Activity 初扫机会，再恢复现场轮流深入分析。

## 开始使用

已验证环境为 JDK 21、Android SDK 和 Python 3（批处理/核验脚本）。APK 不需要预先反编译。

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar
export ANDROID_HOME="/你的路径/Android/Sdk"
java -Xmx16g -XX:ActiveProcessorCount=8 \
  -jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --apkpath "/你的路径/app.apk" --out output/app \
  --target-seconds 300 --hard-seconds 600
```

查看 `output/app/capabilities.compact.json`：格式化的三层列表，保留 Settings 参数和统计。只看数量可打开同目录 `capabilities.counts.json`。两份精简报告按 Bridge/回调/设置数量依次降序排列。完整证据、注册名、候选状态、覆盖与诊断在同目录的 `capabilities.json`。

首次构建需要获取 Gradle/依赖；缓存齐全时可加 `--offline`。`ActiveProcessorCount` 不等于 CPU 绑定；固定 CPU 测量需使用 Linux `taskset` 或项目测试脚本。

私有 QNRouter 签名适配已移除；标准 Client 回调按公开 SDK 家族及完整签名识别，见 [通用规则调整](docs/validation/GENERIC_RULES_V1.md)。

## 当前状态

当前为通用规则第二轮版本，已移除私有适配并测试十个 App（四个新增样本使用补丁版）。十份报告约 46～594 秒产出，但均为 partial。修复了三条已核验的错误 Bridge 归属，芒果累计核验集新增命中 14 条 Bridge 事实；搜狐核心链仍大量漏检，整体误报率与完整性未验收。详见 [本轮结果](docs/validation/GENERIC_V2.md)。

APK、完整反编译文件、构建 JAR 和完整运行报告不在 Git 中。文档里的 `test/runs/...` 是本地实验产物；克隆后需自行构建并提供 APK。

## 文档导航

- [使用说明](USAGE.md)：单 APK、多个 APK、参数、输出和退出状态。
- [核心设计](CORE_IDEA.md)：当前流程与初版区别。
- [分析原理教程](docs/ANALYSIS_TUTORIAL.md)：对象传播、能力绑定、上下文敏感性、通用规则与私有适配。
- [能力与报告语义](docs/CAPABILITY_ANALYSIS.md)：绑定、覆盖、候选和评估边界。
- [验证资料索引](docs/validation/README.md)：当前结果、历史记录和已知问题。
- [通用规则第二轮结果](docs/validation/GENERIC_V2.md)：十 App 实测、累计事实重放、残留问题和精简报告。
- [历史两轮调度记录](docs/validation/SCHEDULER_V3.md)：scheduler v3 的六 App 结果和限制。

旧版类关系图入口保留为 `--legacy`，不用于当前能力质量验收。
