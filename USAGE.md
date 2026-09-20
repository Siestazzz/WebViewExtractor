# WebView Extractor JAR 使用说明

## 当前能力分析入口

```sh
./gradlew capabilitySelfTest shadowJar --offline --console=plain
java -Xmx16g -XX:ActiveProcessorCount=8 -jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --apkpath app.apk --out output/app --target-seconds 300 --hard-seconds 600
```

输出 `output/app/capabilities.json`，以 Activity → WebView 对象 → 能力索引组织。
`facts` 包含 Bridge 名称/实现/完整成员签名、Settings 调用和值、Client 实现及回调。
分析过程原子更新报告；超时或失败保留有效阶段报告并返回非零退出码。
`partial`、候选绑定和未知值都不是“没有能力”的结论。当前质量验收尚未通过。

服务工厂的条件绑定会在 Activity 的 `service_bindings` 中保存查询、元数据注册点和条件。
静态发现注册不能证明初始化和覆盖顺序；这些关联仍标为候选。

详细语义与复现方法见 [能力分析说明](docs/CAPABILITY_ANALYSIS.md)，
实际测量、独立核验与未完成项见 [迭代记录](docs/validation/ITERATIONS.md)。

## 旧版路径分析入口

以下旧版入口须显式加 `--legacy`；`--pathcount` 仅控制旧版路径枚举。

### 运行

```powershell
java -jar build\libs\webview_extractor-1.0-SNAPSHOT-all.jar --legacy --apkpath test\txxw_7.9.50.apk
```

如果需要指定 Android SDK：

```powershell
$env:ANDROID_HOME='D:\WorkSpace\IDE\Android\Sdk'
java -jar build\libs\webview_extractor-1.0-SNAPSHOT-all.jar --legacy --apkpath test\txxw_7.9.50.apk
```

## 指定每个 Activity 保留的路径数

默认每个 Activity 保留 20 条最短路径，可用 `--pathcount` 修改：

```powershell
java -jar build\libs\webview_extractor-1.0-SNAPSHOT-all.jar --legacy --apkpath test\txxw_7.9.50.apk --pathcount 50
```

## 输出文件

输出在 `output` 目录：

- `*_webview_container_classes.json`：候选 WebView 容器类
- `*_webview_outer_paths.json`：Activity 到 WebView 的路径
- `*_webview_outermost_classes.json`：Activity 摘要、路径长度、风险分
- `*_webview_activity_risk_scores.json`：Activity 风险打分详情
