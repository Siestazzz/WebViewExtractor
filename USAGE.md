# WebView Extractor JAR 使用说明

## 运行

```powershell
java -jar build\libs\webview_extractor-1.0-SNAPSHOT-all.jar --apkpath test\txxw_7.9.50.apk
```

如果需要指定 Android SDK：

```powershell
$env:ANDROID_HOME='D:\WorkSpace\IDE\Android\Sdk'
java -jar build\libs\webview_extractor-1.0-SNAPSHOT-all.jar --apkpath test\txxw_7.9.50.apk
```

## 指定每个 Activity 保留的路径数

默认每个 Activity 保留 20 条最短路径，可用 `--pathcount` 修改：

```powershell
java -jar build\libs\webview_extractor-1.0-SNAPSHOT-all.jar --apkpath test\txxw_7.9.50.apk --pathcount 50
```

## 输出文件

输出在 `output` 目录：

- `*_webview_container_classes.json`：候选 WebView 容器类
- `*_webview_outer_paths.json`：Activity 到 WebView 的路径
- `*_webview_outermost_classes.json`：Activity 摘要、路径长度、风险分
- `*_webview_activity_risk_scores.json`：Activity 风险打分详情
