# WebView 能力提取的当前核心思路

当前默认流程以 Activity 为宿主，追踪 WebView 对象及其 Bridge、Settings 和 Client/回调绑定。本文对应两轮调度实现；旧版类关系图仍通过 `--legacy` 保留，历史说明见 [旧版设计文档](docs/LEGACY_CORE_IDEA.md)。

## 与最初版的区别

| 维度 | 最初版 | 当前默认实现 |
|---|---|---|
| 分析单位 | 类及其关系 | 方法调用上下文、符号对象、字段与参数 |
| 数据获取 | dexlib2 类/字段信息，加 Soot/Jimple 局部类型 | dexlib2 全 DEX 索引，按需构建方法摘要 |
| 宿主发现 | 从 WebView 基类沿 inherits/holds/declares 反向传播候选容器 | 从 Manifest Activity 出发，在能力相关方法范围内追踪对象和调用 |
| 能力归属 | 在选定路径上的类中汇总 API 命中及风险分 | 依据调用参数、接收对象、字段/返回值等，将能力绑定到 Activity 内的 WebView |
| 输出 | 候选容器、有限条类路径、风险信息 | Activity → WebView → Bridge/Settings/回调，及证据、候选状态、未归属入口 |
| 调度 | 原始流程没有当前的按宿主暂停恢复机制 | 全部 Activity 先获得初扫机会，再恢复未完成现场、轮流深入 |

共同点是利用静态关系寻找可能的 WebView 宿主；具体分析对象、绑定方法、调度和报告已经改变。不能仅凭有继承或字段类型关系就声称新实现已证明具体能力归属。

## 1. 全局索引：确定哪些方法值得追踪

[CapabilityIndex](src/main/java/org/example/CapabilityIndex.java) 扫描 DEX，建立类继承、方法、调用、字段读写、能力调用点及部分框架注册关系。API 识别考虑方法身份、签名和类型关系。

从能力调用点反向标记相关方法，用于缩小后续展开范围。这个反向标记不直接决定 Activity 归属，也不是一个已经证明某个 Activity 使用 WebView 的筛选器。

[ApkInventory](src/main/java/org/example/ApkInventory.java) 和布局解析器读取 Manifest、必要布局及资源信息。默认主流程不构建全 APK Soot scene；按需 Soot 回退仍未实现。

## 2. 从 Activity 出发建立绑定

[CapabilityEngine](src/main/java/org/example/CapabilityEngine.java) 为每个 Activity 创建独立 Host 状态，从相关入口开始处理方法任务。

[DexFlow](src/main/java/org/example/DexFlow.java) 提取方法内的符号寄存器传播、调用、字段写入和返回值摘要。调用者将实际符号对象和参数代入摘要；对象身份保留分配点、接收对象等来源信息。字段和集合状态保存在对应 Host 中。

例如：

```java
WebView first = new WebView(this);
WebView second = new WebView(this);
configure(first, new AppBridge());
second.setWebViewClient(new PageClient());
```

追踪目标是把 `configure` 内部的 Bridge/设置归给 `first`，把 Client 归给 `second`，再共同归到当前 Activity。不能把相关类里出现的所有 API 混成一个 WebView 的能力。

已实现的传播包括若干字段/参数/返回值、继承、Fragment、布局、自定义 View、共享注册器和实际异步注册路径。它们均有边界和预算；反射、动态工厂、未知接收对象等仍可能留下候选或未决结果。符号对象也可能互为别名，WebView 条目数不是运行时对象数量。

## 3. 能力记录

遇到能力调用后，记录具体 WebView 接收对象和关联证据：

- Bridge：注册名、实现类型、暴露方法及完整 DEX 签名；识别部分消息桥接和注册器协议。
- Settings：配置调用、参数及未知/分支值；表示观察到的配置操作，不保证是最终状态。
- Client/回调：注册实现类型、回调完整签名，以及相关继承实现。

匹配不到具体宿主的入口和解析限制仍写入详细报告。静态明确绑定也不证明运行时一定执行；未输出某个 Activity 不证明它没有 WebView。

## 4. 先初扫，再恢复现场深入分析

[Main](src/main/java/org/example/Main.java) 调度全部 Manifest Activity；Manifest 没有 Activity 时有基于继承类型的回退。

1. 第一轮每个 Activity 最多先处理 8 个方法上下文或约 50 毫秒；临近目标时间时还会缩小时间份额。
2. 暂停时保留 ActivityState/Host：任务队列、参数、对象/字段/集合状态、已发现能力和处理记录。
3. 第二轮按轮转顺序恢复未完成宿主，每批最多 100 个上下文或约 50 毫秒。
4. 默认目标 300 秒、硬时限 600 秒；定期落盘，独立进程监督截止时间。

暂停发生在方法任务之间，因此 50 毫秒是软预算，单个复杂任务可能超出。现场仅保留在当前进程内，不支持退出后从报告恢复。

这两轮调度与引擎内部两个细化阶段不同。第一阶段独有的事实在后续未重新推导时保留为待复核候选；替换旧占位需满足对象来源、参数及成员不缩减等约束。内部上下文限额仍可能放弃任务，报告会记录数量。

## 5. 报告与当前效果

- `capabilities.json`：完整能力事实、对象分组、证据、覆盖状态、诊断、未归属入口。
- `capabilities.compact.json`：格式化的三层列表，仅展示签名、Settings 参数和各层统计，保留覆盖元数据。
- `activity_coverage`：区分未开始、初扫/待深入、截止中断、遍历结束、内部预算耗尽。遍历结束不等于能力完整。

最近六 App 并行测试中，全部 Activity 在约 19～58 秒内获得初扫；西瓜和 FreeReels 仍未在截止前完成深入分析。三个已有累计核验集未出现新增遗漏，但新增候选的整体误报率尚未核验，质量总验收没有通过。

实现细节、测试、实际结果和残留问题见 [两轮调度记录](docs/validation/SCHEDULER_V3.md)、[能力分析说明](docs/CAPABILITY_ANALYSIS.md) 和 [迭代总账](docs/validation/ITERATIONS.md)。使用命令见 [USAGE.md](USAGE.md)。
