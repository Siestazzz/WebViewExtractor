# 验证资料索引与当前状态

最近完成十 App 测量的版本为 **generic v13**，严格验收仍为 **0/10**。新增实际 Application 初始化与有限静态反射，但同一事实集三类能力命中与 v12 相同。四个新增样本使用用户指定补丁版；并行开发测量不能代替最终三次隔离测量。

- [v13 实现、测试、结果与复现](GENERIC_V13.md)、[实测](generic-v13-summary.json)、[严格验收](acceptance/generic-v13-status.json)。
- [v12 实现、测试、结果与复现](GENERIC_V12.md)、[实测](generic-v12-summary.json)、[严格验收](acceptance/generic-v12-status.json)。
- [v11 实现、测试、结果与复现](GENERIC_V11.md)、[实测](generic-v11-summary.json)、[严格验收](acceptance/generic-v11-status.json)。
- [v10 实现、测试、结果与复现](GENERIC_V10.md)、[实测](generic-v10-summary.json)、[严格验收](acceptance/generic-v10-status.json)。
- v11 实现说明：[跨 Client 委托](GENERIC_V11_CROSS_CLIENT_DELEGATION.md)、[反射工厂](GENERIC_V11_REFLECTIVE_FACTORIES.md)、[Manifest 模块](GENERIC_V11_MANIFEST_IMPLEMENTATION.md)。以下早期版本数据均为历史记录。

- [generic v1](GENERIC_V1.md)：私有适配删除、排序/数量报告、十 App 初轮。
- [generic v2](GENERIC_V2.md)：通用分支修复、最终十 App 结果、事实重放与限制。
- [当前样本](generic-ten-samples.json)、[v2 历史实测](generic-v2-summary.json)、[报告结构验证](generic-v2-structure.json)、[交付文件哈希](generic-v2-delivery.json)。
- 新样本证据：[微视](txws/README.md)、[搜狐](sohuvideo/README.md)、[番茄](fanqiexiaoshuo/README.md)、[Breaking News](breaking-news/README.md)。审计模型为 GPT-6.1 Sol。

以下保留 scheduler v3 历史导航，不应把旧版误报/召回率当成当前版本结论。

## scheduler v3 历史记录

| 内容 | 文件 |
|---|---|
| 最新实现、六 App 结果、限制及复现命令 | [SCHEDULER_V3.md](SCHEDULER_V3.md) |
| 实际耗时、CPU、堆和峰值内存 | [结果](scheduler-v3-results.json)、[环境](scheduler-v3-environment.json) |
| 初扫、完成、上限终止、待深入及宿主增删 | [效果变化](scheduler-v3-effects.json) |
| 合成测试、结构检查、外部截止测试 | [测试](scheduler-v3-tests.json)、[结构检查](scheduler-v3-structure-check.json)、[截止测试](scheduler-v3-deadline.json) |
| Sol 对冻结构建的独立实现审查 | [scheduler-v3-review.md](scheduler-v3-review.md) |
| 新闻、芒果、携程累计事实重放 | [新闻](scheduler-v3-news.json)、[芒果](scheduler-v3-mango.json)、[携程](scheduler-v3-ctrip.json) |
| 完整事实身份的新增/丢失比较 | [新闻](scheduler-v3-news-fact-changes.json)、[芒果](scheduler-v3-mango-fact-changes.json)、[携程](scheduler-v3-ctrip-fact-changes.json) |
| 验收规则及后续并行开发范围 | [PROTOCOL.md](PROTOCOL.md) |
| 历史迭代 | [ITERATIONS.md](ITERATIONS.md) |
| 框架/能力缺口与当前调度限制 | [OPEN_GAPS.json](OPEN_GAPS.json) |

## 如何理解 scheduler v3 结果

六 App 均在约 19～58 秒完成全部 Activity 的初扫，包含索引时间。初扫并不代表确认有没有 WebView，更不代表能力完整。最终西瓜有 344 个、FreeReels 有 105 个 Activity 待深入；新闻、芒果、携程分别有 2、18、3 个宿主触及内部上下文上限。

同一累计开发事实集上，新闻/携程已命中事实没有减少，芒果增加命中 81 条。这个比较包含候选，没有证明能力级误报率或新输出宿主的归属正确率。新增三个 App 没有在本轮完成相同规模的独立源码核验。FreeReels 有一个旧版宿主未输出，其分析仍被截止打断，已保留为待查变化。

当前仍未完成：逐 App 全部质量门槛、新版本所有输出的归属/能力精度审计、最终独立新样本核验，以及隔离的三次性能复测。旧版的误报/未决百分比不能沿用到当前输出。

## 历史文件怎样使用

- [COMPACT_REPORT.md](COMPACT_REPORT.md) 的格式规则仍适用，但其中 compact-six 表格是引入调度器之前的基线。
- [SCHEDULER_V1.md](SCHEDULER_V1.md) 保留第一版调度测试及已发现的事实丢失问题。
- [SCHEDULER_V2.md](SCHEDULER_V2.md) 的测试因发现合并边界缺陷而主动中止，不能当成完整性能结果。
- App 子目录中的 `v*a-validation`、`ownership`、`delta` 和事实备份只代表命名版本。历史事实集持续扩充，比较版本应使用相同 canonical 和评分器重放。
- `news/mango/ctrip` 的 `canonical-facts.jsonl` 是当前累计开发核验输入；早期 source-only 盘点和后续根据报告挑选、再经源码核实的补充需要区分。它不是盲测集。
- [regressions](regressions/README.md) 中一些历史探针故意断言旧缺陷存在，应按对应版本说明解释退出码。

## 样本和运行产物

原三个样本清单为 [samples.json](samples.json)，六 App 清单为 [compact-six-samples.json](compact-six-samples.json)。原目录只读，测试使用本地副本；FreeReels 使用 base APK。APK 和完整反编译树不加入 Git。

文档中 `test/runs/...` 的 JAR、完整/精简报告、ZIP、日志及源代码快照为本地忽略产物，远端仓库没有这些文件。新环境请按 [USAGE.md](../../USAGE.md) 构建、准备 APK 并重新运行。Git 中保存源码、可复用核验事实、审计文档和测量摘要。
