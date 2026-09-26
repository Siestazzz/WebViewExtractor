# 验证资料索引与当前状态

当前默认实现为 **scheduler v3**。六 App 初扫/深入调度、格式化精简报告及对应回归测试已完成；**能力质量总验收尚未通过**。本页是导航，具体证据保留在各版本文件中。

## 当前记录

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

## 如何理解当前结果

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
