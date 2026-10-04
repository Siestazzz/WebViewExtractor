# WebViewExtractor 使用说明

## 构建与环境

已验证使用 JDK 21。配置安装了平台包的 Android SDK，以便读取平台继承关系；SDK 方法体不会参与默认分析。APK 无须预先反编译。

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar
export ANDROID_HOME="/你的路径/Android/Sdk"
```

首次构建可能需要下载 Gradle 和依赖。缓存齐全时可加 `--offline`。
产物：`build/libs/webview_extractor-1.0-SNAPSHOT-all.jar`。

## 分析单个 APK

```sh
java -Xmx16g -XX:ActiveProcessorCount=8 \
  -jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --apkpath "/你的路径/app.apk" \
  --out "output/app" \
  --target-seconds 300 \
  --hard-seconds 600
```

| 参数 | 含义 |
|---|---|
| `--apkpath` | 必填，一次一个 APK |
| `--out` | 输出目录；省略时使用 `output/` 下按 APK 文件名生成的目录 |
| `--target-seconds` | 目标时间，默认 300；用于初扫预算分配，不保证届时分析完整 |
| `--hard-seconds` | 总硬时限，默认 600；内部会预留监督和收尾时间 |

目标时间必须不大于硬时限。每个 APK 使用独立输出目录；重复使用会覆盖旧报告。
`-Xmx16g` 限制 JVM 堆；`ActiveProcessorCount=8` 设置 JVM 处理器计数，不绑定 CPU。
在 Linux 上需要固定八个逻辑 CPU 时，可在 `java` 前加 `taskset -c 0-7`，并按环境实际允许的 CPU 编号调整。

默认先让各 Activity 获得初扫，再轮流深入；现场保存在同一进程的内存中。报告文件不支持进程退出后的续跑。

## 输出与统计

- `capabilities.counts.json`：Activity → WebView 数量版，仅保留类签名、各层统计及覆盖元数据，不含具体函数。
- `capabilities.compact.json`：带缩进/换行的 Activity → WebView → Bridge/回调/Settings 列表。前两层为类签名，能力为完整方法签名，Settings 额外保留参数。
- `capabilities.json`：完整事实、注册名、实现、成员、对象来源、证据、候选状态、未归属入口、覆盖和诊断。

能力版和数量版的 Activity、WebView 列表均按 Bridge 数降序、回调数降序、Settings 数降序排序；三项相同按签名升序。同签名同数量的对象保持输入顺序。

每层 `counts` 都有统计。Bridge 数量是展示的暴露方法条目数（无法解析方法时回退为实现类），不是注册名或 Bridge 实例数量。Settings 按签名和参数去重，回调按签名去重；去重在每个 WebView 内进行，上层求和。候选与第一阶段待复核事实也计入。

Settings 的布尔值为 JSON 布尔值，其他已知常量保留分析器的字符串表示；`null` 表示无法解析，不证明运行时参数为 Java null。分支值保留为 `alternatives`。两个相同类签名的 WebView 条目可能对应不同符号对象，也可能是同一运行时对象的别名。详见 [精简格式说明](docs/validation/COMPACT_REPORT.md)。

## 怎样判断是否分析完整

初扫对象是 Manifest 声明的全部 Activity，不只是输出的 WebView 宿主。输出宿主列表与覆盖列表的含义不同。

| 字段/状态 | 含义 |
|---|---|
| `initial_pass_activities` | 已获得初扫机会的 Activity 数 |
| `processed_activities` | 已结束本轮宿主分析的数量，包含内部上限终止 |
| `traversal_finished_activities` | 工作队列完成且没有宿主上下文上限终止的数量 |
| `budget_exhausted_activities` | 因内部宿主上限终止的数量 |
| `pending_activities` | 尚未结束、仍待深入的数量 |
| `not_started_activities` | 尚未开始的数量 |
| `activity_coverage` | 完整报告中的逐 Activity 状态、阶段、上下文计数和耗时 |

`deadline_interrupted` 表示截止时未完成；`finished_with_limits` 表示调度队列结束但有宿主限额或待复核事实。遍历结束仍可能存在其他解析限制。

**退出码 0 不代表质量通过，也不保证全部 Activity 分析完。** 内部预算耗尽可以正常返回，并留下 `partial` 报告。监督进程强制超时或 worker 失败返回 2，尽量保留最后一个有效快照；输入错误可能直接报错，不能保证产生报告。三个报告文件分别原子更新，不是跨文件事务。

检查 `status`、覆盖字段、`diagnostics`、`index_diagnostics`、`manifest_diagnostics`、`unattributed` 和宿主 `limitations`。空结果不证明没有 WebView 或能力。

## 多 APK 并行

JAR 本身一次接受一个 APK；`scripts/run_parallel.py` 启动多个独立进程。脚本需要 Linux、Python 3、`taskset`、GNU `time` 和 `timeout`，每个 APK 分配不重叠的八个逻辑 CPU、16 GiB 最大堆。六个 APK 要求至少 48 个可用逻辑 CPU，并需足够内存。

创建样本清单，例如 `samples.local.json`（`package` 填实际包名，`local` 为绝对路径或相对项目根目录的路径）：

```json
[
  {"package": "com.example.one", "local": "/路径/one.apk"},
  {"package": "com.example.two", "local": "/路径/two.apk"}
]
```

```sh
python3 scripts/run_parallel.py \
  --samples samples.local.json \
  --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --out test/runs/my-batch
```

每个包名单独输出两个报告、运行日志和资源记录；批次目录保存 `summary.json` 和 `environment.json`。脚本固定目标 300 秒、内部监督时限 595 秒、外部 600 秒，不支持通过该脚本另外指定时限。资源不足时可在 shell 循环中串行调用单 APK 命令。

仓库已有六个样本的来源清单 `docs/validation/compact-six-samples.json`，但不附带 APK。克隆仓库后需自行准备文件并调整清单路径。并行测量不等同于隔离的串行性能验收。

## 旧版入口

```sh
java -jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar \
  --legacy --apkpath app.apk --pathcount 20
```

旧版输出在 `output/` 下，包括 `*_webview_container_classes.json`、`*_webview_outer_paths.json`、`*_webview_outermost_classes.json` 和 `*_webview_activity_risk_scores.json`。`--pathcount` 仅用于旧版路径枚举，默认 20。新入口的 `--out` 和时间监督机制不适用于旧版入口；测试旧版需要额外的外部 timeout。

当前设计见 [CORE_IDEA.md](CORE_IDEA.md)，最新效果和限制见 [验证索引](docs/validation/README.md)。
