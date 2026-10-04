# WebView 能力分析实现教程

最新实测为 [generic v17](validation/GENERIC_V17.md)：十 App 约 52～594 秒，全部仍为 partial，严格验收 **0/10**。在同一经过源事实状态校正的核验集上，十个 App 召回计数均未提升；西瓜启动状态保留与待处理队列有所改善，但浏览器能力缺口仍在。完整能力精度核验、新样本验收及三次隔离性能测试尚未完成；以下旧版本说明保留作为历史记录。


本文面向希望理解或修改分析器的读者，解释 Activity 如何追踪到 WebView，以及能力如何绑定到具体对象。它描述当前源码的实际行为，不把设计目标当成已实现能力。

版本基准：`a91b113`，默认引擎为 scheduler v3；本文新增时没有修改分析代码。后续代码变化应同步更新规则和边界。旧入口 `--legacy` 不在本文范围内，见 [旧版设计](LEGACY_CORE_IDEA.md)。默认流程直接使用 dexlib2，不启动全 APK Soot 分析；按需 Soot 回退尚未实现。

> 版本更新：下文保留 `a91b113` 的实现教学基线。后续 generic rules 迭代已删除第 8 节中的 QNRouter 私有适配，并收紧第 7 节标准 Client 回调到公开 SDK 家族、完整签名及非 private/static 方法。该适配和名称筛选的描述仅用于理解历史版本，第二轮还增加了已知常量 switch 的有限摘要细化，以及具体接收对象能力任务的有界优先调度。当前差异见 [通用规则调整](validation/GENERIC_RULES_V1.md) 和 [第二轮记录](validation/GENERIC_V2.md)。

> generic v3/v4 后续修订：非 Activity 组件按正式 SDK 生命周期完整签名进入，普通 helper 须通过实际调用到达；未知实例字段不再扫描任意构造函数；实际 Client getter 和继承生命周期的接收对象参与相关性判断；已解析的 API override 由实际方法体决定副作用。见 [v3 实现](validation/GENERIC_V3_IMPLEMENTATION.md)、[v4 实现](validation/GENERIC_V4_IMPLEMENTATION.md)。下文旧实现细节遇到冲突时以这些修订为准，v6 已补充实际安装 Client 的 delegate 调用传播，详见 [委派和对象隔离](validation/GENERIC_V6_CLIENT_DELEGATION.md)。v5 工厂扩展与成本回退见 [v5 结果](validation/GENERIC_V5.md)。这些新增机制仍需真实 App 独立核验，不能据合成测试宣称全覆盖。

> generic v9/v10：分析器按宿主使用最多 8 个工作线程，Host 状态不共享，方法摘要共享；初扫与深扫有全局阶段边界，停稳后导出报告。已发布摘要的缓存命中不等待细化锁，报告收尾依据实际导出耗时预留预算。见 [v10 实测和限制](validation/GENERIC_V10.md)。v11 的 [跨 Client 委托](validation/GENERIC_V11_CROSS_CLIENT_DELEGATION.md)、[反射工厂](validation/GENERIC_V11_REFLECTIVE_FACTORIES.md) 和 [Manifest 模块](validation/GENERIC_V11_MANIFEST_IMPLEMENTATION.md) 已完成[十 App 测量](validation/GENERIC_V11.md)：新闻回调增加，但芒果性能和携程部分命中回退；这些是有条件的协议模型，不是全反射解析。

> generic v14：增加实际构造捕获、Fragment 安装和字段委托；合成测试通过，但真实 APK 内 AndroidX SDK 方法体被误当 App 重写，多个 App 召回回退。见 [v14 结果](validation/GENERIC_V14.md)。

> generic v13：在实际 Manifest Application 初始化后复制静态对象图，并支持公开无参静态反射；十 App 同核验集命中未提升，局部预算存在超限。见 [v13 结果](validation/GENERIC_V13.md)。

> generic v12：将 Class 字段持有者的实际构造捕获与全局相关性分开，避免无关工厂链进入追踪；Manifest 原生 API 折叠检查实际接收对象的 override，并区分 null 与字符串 "0"。芒果性能恢复，但三类事实命中与 v11 相同；见 [v12 结果](validation/GENERIC_V12.md)。

## 阅读路线

1. [目标和结果含义](#1-目标和结果含义)
2. [贯穿示例](#2-贯穿示例)
3. [全局索引和相关性筛选](#3-全局索引和相关性筛选)
4. [Activity 入口与宿主现场](#4-activity-入口与宿主现场)
5. [符号对象、摘要和上下文](#5-符号对象摘要和上下文)
6. [从寄存器到跨方法传播](#6-从寄存器到跨方法传播)
7. [能力绑定](#7-能力绑定)
8. [通用机制与适配规则清单](#8-通用机制与适配规则清单)
9. [精度、预算和恢复](#9-精度预算和恢复)
10. [代码导航、验证和扩展](#10-代码导航验证和扩展)

## 1. 目标和结果含义

目标是建立以下静态画像：

```text
Activity
└── WebView 符号对象
    ├── Bridge 注册名、实现类型、暴露成员
    ├── Settings 配置调用和参数
    └── Client 实现类型和回调成员
```

这里的 WebView 是分析器表示的对象，不是实际运行时实例。一个符号对象可能代表同一分配点创建的多个实例；两个符号对象也可能是实际同一对象的别名。条目数不能直接解释为页面里的控件数。

`explicit` 表示当前静态传播链得到直接绑定，不证明运行时一定执行，也不证明入口可达性和抽象完全准确。`candidate` 表示有条件、推测或尚未充分消歧的绑定。未知实现和未知注册名应保留诊断，不能当成“没有 Bridge”。

Settings 记录观察到的配置操作，不是最终运行状态。回调列表记录实现成员，不解释业务行为，也不证明回调必然触发。

精简输出用于浏览，详细输出用于核验；两者语义见 [报告说明](CAPABILITY_ANALYSIS.md) 和 [精简格式](validation/COMPACT_REPORT.md)。

## 2. 贯穿示例

以下是教学伪代码，省略 Android 必需的签名和样板；`W1` 等是方便阅读的符号，不是工具原始 JSON。

```java
class BasePage extends Activity {
    WebView primary;
    void initPrimary() {
        primary = new WebView(this);              // 分配点 A
        Helpers.configure(primary, new AppBridge());
    }
}
class Page extends BasePage {
    void onCreate(...) {
        initPrimary();
        WebView secondary = new WebView(this);    // 分配点 B
        secondary.setWebViewClient(new PageClient());
        // 另有一个实际挂载的 DetailFragment，加载包含 WebView 的布局。
    }
}
class Helpers {
    static void configure(WebView w, Object b) {
        w.addJavascriptInterface(b, "app");
        w.getSettings().setJavaScriptEnabled(true);
    }
}
```

沿这条调用链得到的关系应当是：

| 分析动作 | 抽象状态或事实 |
|---|---|
| 创建 Page 的 Host | `this → H_Page` |
| 分析父类初始化中的分配点 A | 创建 `W1`，保存 `H_Page.primary → W1` |
| 调用 `configure(primary, bridge)` | 实参为 `[W1, B1]` |
| 实例化 configure 摘要 | `param0 → W1`，`param1 → B1`，因为这是静态方法 |
| 处理 Bridge 注册 | `W1 → ("app", B1, AppBridge 的暴露成员)` |
| 处理 Settings | Settings 表达式关联 W1，记录 `W1 → setJavaScriptEnabled(true)` |
| 处理分配点 B 和 Client 注册 | `W2 → PageClient → 回调实现` |

这里不应该把 `AppBridge` 归给 W2，也不应该把 PageClient 归给 W1。实际报告可能还包含宽入口、未知值等产生的候选，因此这张表不是对完整输出的逐条保证。

Fragment 内的 WebView 需要额外的宿主和布局链：在 Page 的分析现场中识别 Fragment 对象及相关入口，建立其根 View 与布局的关系，再由资源 ID 查找 WebView。不能仅因为某个 Fragment 类存在于 APK，就宣布它属于 Page。当前模型仍可能用候选关系扩展这条链，细节见第 4、6 节。

## 3. 全局索引和相关性筛选

[CapabilityIndex](../src/main/java/org/example/CapabilityIndex.java) 读取所有 DEX，建立类继承、方法定义、调用者/被调用者、字段访问、对象分配和能力入口等索引。[ApkInventory](../src/main/java/org/example/ApkInventory.java) 与 [LayoutResources](../src/main/java/org/example/LayoutResources.java) 提供 Manifest、资源和布局信息。

Manifest 给出 Activity 根列表；缺少 Activity 时有基于继承类型的回退。已配置 WebView 基类及其子类用于识别对象类型。

索引从能力入口反向扩展相关方法，并补充部分分派、字段和框架关联。作用是减少后续正向处理的无关代码。

```text
DEX 中的能力调用点
        ↓ 反向相关性标记
值得处理的方法集合
        ↓ 从各 Activity 正向实例化
对象、参数、字段和能力归属
```

反向相关性不是能力归属证明；正向传播仍须判断接收对象和参数。筛选本身也可能漏掉未建模的动态调用，不能视为保留所有真实可达代码的严格保证。

## 4. Activity 入口与宿主现场

`CapabilityEngine.beginActivity` 为每个 Activity 创建 `ActivityState` 和 `Host`，用 `host(activity)` 表示宿主对象，再调用 `seed` 加入初始任务。

### 4.1 入口不只包含 onCreate

当前 `seed` 会查看类型层次中的方法，对 Activity 本类声明的相关方法采用较宽的入口策略。父类方法再结合构造方法、`on` 开头方法及被引用的方法形状等条件筛选。普通入口参数初始化为带声明类型的未知值。

所以一个本类普通方法即使没有被证明由生命周期调用，也可能进入分析。这有助于发现事件入口后的能力，但会带来不可达方法误报。`explicit` 标签不能弥补入口模型的这一限制。

组件扩展包括部分 Fragment、View、Dialog、相关构造器及已识别回调。XML 得到的 View 对象限制到构造器和特定 View 回调，不把所有自定义辅助方法直接当成已执行。接受 WebView/Settings 参数的非 Activity 辅助方法，原则上通过真实调用实参进入，避免凭入口未知参数制造新 WebView。

### 4.2 Host 保存什么

| 状态 | 作用 |
|---|---|
| `heap` | 对象字段的抽象值 |
| `maps / arrays / contents` | 容器里的键值、槽位或成员 |
| `queue / pending / visited` | 待处理、已排队和已处理的方法上下文 |
| 布局、Fragment、延迟字段状态 | 记录尚待补充或重放的绑定 |
| `bridgeViews / nativeBindings` | Bridge/注册器对象与 WebView、注册点之间的关系 |
| `facts / gaps` | 已发现能力及限制诊断 |

不同 Activity 的 Host 分开保存；相同方法摘要可以共用。静态字段也在各 Host 的抽象状态中处理，这不是对全 App 共享运行时状态和 Activity 切换顺序的模拟。

## 5. 符号对象、摘要和上下文

### 5.1 符号值 V

[DexFlow](../src/main/java/org/example/DexFlow.java) 用 `V(kind, type, id, literal, args)` 表示值。常见形式包括：

```text
param(1)                     方法参数
literal("app")               已知常量
new(方法签名@指令偏移)        分配位置
field(接收对象, 字段签名)     字段读取表达式
return(目标方法, 实参列表)    返回值表达式
settings(WebView对象)        关联某个 WebView 的设置对象
union(W1, W2)                多个可能值
unknown                      无法解析
```

`new` 在 Host 中求值后成为有上下文的 `object`；布局查找产生 `view` 等对象。对象类型和对象身份是不同信息，不能只凭类名判定两个对象相同。

### 5.2 方法摘要

`Summary` 保存调用列表、字段写入、返回值以及分支/截断标记。它是带参数的模板，不是执行结果。

非静态方法的 `param0` 是 `this`，后续才是显式参数；静态方法的 `param0` 就是第一个显式参数。DEX 的宽寄存器参数由解析器处理。

基础摘要按方法签名缓存。含特定 `instance-of` 判断且不超过 500 条指令的方法，可按已解析的探测值生成有限细化摘要，每方法最多 16 个变体；超出后回退基础摘要。并非所有分支都会按调用者特化。

### 5.3 方法上下文与堆对象上下文不是一回事

排队和去重使用：

```text
方法上下文键 = 方法签名 + 实际符号参数列表
```

它包括实例方法的接收对象，也包括其他参数。证据路径不在键中；不同调用链只要方法和参数相同，就可能合并。

普通分配对象的身份基于分配点再加 `allocationContext(job)`。后者读取任务第一个实参的对象 ID，并按 `|` 分段截断到最多前两段；无参数时使用 `static`。因此实例方法通常携带接收对象上下文，静态方法有参数时则使用第一个参数的身份。

这是项目自定义的有限堆上下文，**不应直接称为严格的 2-object-sensitive 或 k-call-site-sensitive 分析**。同一接收对象上的工厂反复在同一分配点创建对象，可能合并；不同调用参数也不保证所有新对象都能区分。XML 和服务模型另有自己的身份构造方式；AspectJ JoinPoint 对象还会把全部实参的摘要加入身份，避免仅按首参数合并不同捕获对象。

## 6. 从寄存器到跨方法传播

### 6.1 方法内：沿控制流图传播

每条指令位置保存寄存器到符号值的映射，工作队列推动变化：

- `move` 复制值，新的寄存器赋值覆盖旧值。
- `const` 保存常量，`new-instance` 保存分配位置。
- 字段读取形成带接收对象的表达式，字段写入进入摘要。
- 调用记录当时参数；返回对象通过返回值表达式表示。
- 控制流汇合点对寄存器逐项求并集；异常处理入口也参与合并。

例如直线代码 `x=W1; x=W2; use(x)` 的调用实参是 W2。但分支分别赋 W1/W2 后汇合，得到 `union(W1,W2)`。

已知数值比较可以剪枝；不确定分支合并。不同参数分别合并会丢失配对关系：原本只有 `(W1,B1)` 或 `(W2,B2)` 两种组合，合并后可能出现交叉候选。这里没有通用路径条件求解器。

### 6.2 跨方法：在调用点代入实参

以静态 `configure(w,b)` 为例，其摘要含有：

```text
call addJavascriptInterface(receiver=param0, bridge=param1, name="app")
call setJavaScriptEnabled(receiver=settings(param0), value=true)
```

`eval` 用当前 Job 的实参替换 `param`：

```text
configure(W1,B1) → W1 的 Bridge/Settings
configure(W2,B2) → W2 的 Bridge/Settings
```

两个任务可以复用同一模板而保留不同绑定。返回值也用被调用方法的实际参数求值，再传回调用者；工厂、辅助 getter 和父类返回因此可以继续传播。

虚调用优先利用接收对象类型选择实现，`super` 与直接调用另行处理。未知接口工厂有受限的候选实现扩展，例如部分返回 WebView、Client 或集合的工厂最多扩展 16 个目标。不能把它理解为已经完整求解任意接口分派。

### 6.3 字段：对象敏感的键，弱更新的值

字段键是：

```text
heap[receiver.id + "::" + fieldSignature]
```

所以 `holder1.view` 和 `holder2.view` 可分开。但普通字段写入是并集合并，即弱更新：

```java
holder.view = W1;
holder.view = W2;
```

通常得到 `holder.view → {W1,W2}`，不按最终覆盖只保留 W2。更关键的是，`processJob` 先应用摘要中的字段写入，再处理调用；堆字段读写不保持完整指令时序。

例如 `use(holder.view); holder.view=W2;` 中，后面的写入可能影响前面调用的求值。这是精度限制，而不是 Java 的实际执行语义。字段变化也没有通用依赖工作队列保证所有旧方法上下文迭代到不动点；当前靠内部细化阶段及特定延迟/布局重放补充。

### 6.4 集合、布局和异步

Map 按容器身份和可解析的键区分；重复写入会合并，未知键可能扩展多个值。数组可以保留部分固定槽位；List 迭代和部分索引读取主要返回成员集合，不能保证位置与注册名逐项对应。部分 Map 修改操作只留下顺序未决诊断。

布局通过 inflate、根 View、资源 ID、`findViewById`、Fragment `getView` 等关系建立对象。分支布局、重复实例和配置选择未必能消歧，因此 XML 绑定会标候选并保留证据。

异步按识别出的注册协议，把实际传入的 Runnable、ServiceConnection 等对象代入回调入口。该模型不模拟线程时序、取消或一定执行；“创建了回调对象”本身不能证明注册成功。具体支持范围见第 8 节。

## 7. 能力绑定

### 7.1 原生 Bridge

对 `webview.addJavascriptInterface(bridge,name)`，`emit` 读取求值后的调用参数：接收对象是 WebView，后两个参数是实现对象与注册名。

记录调用位置、Host Activity、WebView 身份、注册名、实现类型和暴露成员，同时保存 Bridge 对象到 WebView 的关系，供后续消息桥关联复用。

`bridgeMembers` 遍历实现类层次，筛选 public 非 static 的非构造方法。通常要求注解类型以 `/JavascriptInterface;` 结尾；已知 `targetSdk < 17` 时扩大到 public 实例方法。这是当前工具的建模规则，不是对所有 Android 运行环境的可访问性证明。

实现对象未知时可以保留声明接口信息，但不能把它伪装成已解析实现。名称未知也不会丢弃整个注册。

### 7.2 Settings

`getSettings()` 的返回值保留所属 WebView：

```text
S1 = settings(W1)
S1.setJavaScriptEnabled(true)
→ W1 的 setting 事实
```

这样 Settings 即使通过字段或参数传给其他方法，仍可能回到原 WebView。已知布尔参数转成布尔语义；未知值、分支值分别保留。工具没有为全部配置计算最终覆盖顺序或完整默认值表。

### 7.3 Client 与回调

`setWebViewClient(client)` / `setWebChromeClient(client)` 把实际 client 参数绑定到接收 WebView。随后提取实现类型层次中的回调成员；部分相关回调还会进入任务队列，其 WebView 参数代入当前接收对象。

**当前标准回调成员筛选主要使用 `CALLBACKS` 名称集合，再检查实例方法、方法体及类来源，不能宣称所有回调都经过完整标准签名校验。** 输出完整签名与识别时严格检查签名是两回事。同名非标准重载存在误识别风险。

自定义 Client 包装有额外结构发现：在 WebView 子类中，识别“单参数 setter 写入 this 的字段”，并查找该字段对回调契约的分派。契约成员可带 `dispatch_observed` 标记，未观察到分派不等于一定不会被调用。

### 7.4 消息桥与反射

消息桥需要把“传输入口—注册器—处理对象—WebView”连起来。索引从 JS 注解方法及部分 Client 传输回调出发，查找传输链访问的 Map 字段，再识别向同一字段写入名称和处理对象的注册方法。

满足模式的注册可能产生 `message_bridge`；处理对象若有原生注入关系，可通过 `bridgeViews` 回到 WebView。部分 namespace 注册还结合反射方法查找、注解检查和参数类型恢复暴露面。

这是有预算的结构识别，不是任意 JS 协议理解。反射目标、名称和参数无法解析时必须留下未知；注册器字段相似不自动证明同一个运行时实例。

### 7.5 移除与替换

移除原生 Bridge、将 Client 设为空等记录为独立操作。报告主要表达可能观察到的能力面，不是按时间重放后得到的最终注册表。不要从列表中同时存在注册和移除，就推导任意时刻都存在该能力。

## 8. 通用机制与适配规则清单

规则分四类：G 为通用传播机制，A 为标准 API 模型，S 为框架结构规则，P 为具体私有框架签名适配。结构规则也有模式边界，不能因为没有 App 类名就视为完全泛化。

| 类别 / 规则 | 匹配依据与传播动作 | 边界 / 反例 | 实现入口 |
|---|---|---|---|
| G 参数、字段、返回值 | 符号参数代入、对象字段键、返回表达式求值 | 弱更新、别名合并、深度/任务限制 | `DexFlow`；`CapabilityEngine.eval/applyWrite` |
| G 集合与工厂 | 已识别集合 API、Map 键值、实际返回摘要 | 未知键、位置丢失、动态实现 | `DexFlow.decode`；`eval/mapLookup` |
| A WebView 类型 | 已配置基类和继承链 | 加入类型不代表其全部 API 均支持 | `Cfg.WEBVIEWS` |
| A 原生 Bridge | WebView owner、`addJavascriptInterface`、Object/String 参数 | 未知实现、注解后缀匹配、动态名称 | `CapabilityIndex.kind`；`bridgeMembers` |
| A Client | WebView owner、setter 名称及单参数；成员名称名单 | setter 并非全面参数类型校验；同名回调重载 | `kind`；`callbackMembers` |
| A Settings | Android/X5/UC WebSettings 子类型、`set` 前缀且有参数 | 不是完整 setter 白名单，最终值未求解 | `settings/kind`；`emit` |
| A AndroidX 消息入口 | `WebViewCompat` owner 与 `addWebMessageListener` / `addDocumentStartJavaScript` 名称 | 入口识别不代表完整协议和成员恢复；并非完整重载校验 | `kind` |
| A 布局与 Fragment | inflate、资源 ID、View/Fragment 契约及类型关系 | 布局配置、重复实例和动态资源 | `prepareLayouts/lookupView/fragmentView` |
| A 部分异步注册 | Handler/View post、Activity.runOnUiThread、Executor.execute、指定 bindService 签名 | 不涵盖所有重载、任务框架及取消语义；应用覆写要追其方法体 | `AsyncRegistrations` |
| S OkHttp | okhttp3 命名空间、接口形状、IOException 与 Closeable 响应契约 | namespace 改名或契约变化可能失配 | `AsyncRegistrations.discover` |
| S Kotlin lazy | lazy/Function0 契约、工厂、捕获初始化器和 invoke | 不是任意 Kotlin 协程或委托支持 | `eval/lazyInitializerParameters` |
| S AspectJ | runtime 命名空间、state、run、link、实际闭包/JoinPoint/proceed 关系 | 部分带参数 proceed 不建模；仅分配闭包不代表执行 | `discoverAroundClosures`；闭包传播 |
| S 自定义回调包装 | WebView 子类 setter 存字段，契约成员及字段分派 | 外形不同的包装可能漏识别 | `discoverCustomCallbacks` |
| S 键控注册器 | 同一 Map 字段读写、参数位置对应 | 注册器本身不自动成为全局能力 | `discoverKeyedRegistries` |
| S 消息桥/反射桥 | JS/Client 传输链、共享 Map、处理契约、部分反射证据 | 传输发现预算、动态键、跨实例混淆 | `discoverMessageRegistries/reflectEndpoints` |
| P 腾讯新闻 QNRouter | 精确匹配 ServiceMap、APIMeta、Services 的私有签名 | 框架改名/升级、动态 creator、初始化顺序 | `FrameworkServices` |

已配置 WebView 根类型包括 Android、X5、UC、MIUI 和华为；Settings、Client 传输等模型覆盖范围并不完全对称。

### 私有适配实例：QNRouter

`FrameworkServices`（历史文件，已删除，可在 `a91b113` 中查看） 明确包含 `com.tencent.news.qnrouter.service.*` 完整签名。这是样例驱动增加的私有框架适配，应直接承认其范围。

它读取 `ServiceMap.autoRegister` 和 `APIMeta` 中的 API、名称、实现类及单例信息；在已列出的 `Services.call/get` 查询处按 API 和名称恢复服务对象。它不会把任意实现该接口的类都作为已注册服务。

查询的空/null 名称按该模型归一化为默认服务；注册键保持原值。未知自定义 creator 不能直接用默认实现代替。即使读到了打包的注册元数据，仍不能证明初始化、覆盖顺序或构造成功，所以这类传播保留候选和服务证据。

### 怎样评价“通用”

目前没有为每个 App 单独写一套分析器，但确实存在上述私有适配。其他规则即使基于结构，也主要在开发样例上发现和改进。未完成关闭适配的消融实验及充分的未知 App 核验，不能量化通用引擎与适配分别贡献了多少召回。

## 9. 精度、预算和恢复

### 9.1 敏感性结论

| 维度 | 当前实现 | 不能声称什么 |
|---|---|---|
| 上下文 | 方法签名 + 符号实参，Host 内去重 | 不是纯上下文无关；也不是完整调用链敏感 |
| 摘要 | 方法级参数化缓存，少量类型判断特化 | 缓存一次不代表所有调用结果混在一起 |
| 堆对象 | 分配点 + 自定义截断上下文，布局等另有模型 | 不是每次运行时分配都独立，也非严格标准 k-object 模型 |
| 字段 | 接收对象身份 + 字段签名 | 对象身份不精确时仍会混淆 |
| 流 | 方法内寄存器按 CFG；堆写入摘要化、主要弱更新 | 不能称为全过程流敏感 |
| 路径 | 分支合并，有限常量/类型剪枝 | 无通用路径约束，也不保留全部参数相关性 |
| 集合 | 部分键/槽位敏感，普通集合主要成员集合 | 不是完整容器语义 |
| 生命周期/线程 | 入口和注册契约扩展 | 不模拟真实时序与并发 |

它是有预算的符号摘要传播实现，不能宣称严格 sound（覆盖所有真实行为）或 complete（所有结果都真实）。宽入口和弱更新会增加误报；筛选、模型缺失、unknown 和预算截断会造成漏报。

### 9.2 不要把三种循环混为一谈

1. **方法内 CFG 工作队列**：传播寄存器，有限次迭代。
2. **Host 内两个细化阶段**：第一次收集字段等信息，再重新播种入口；保留堆状态，重置部分任务/事实状态，尝试补齐绑定。不是无限求不动点。
3. **Activity 初扫/深入调度**：先让各宿主获得初扫机会，再轮流恢复未完成 Host；这是时间分配策略。

调度第一轮每宿主最多 8 个方法上下文或约 50 ms，深入轮每批最多 100 个或约 50 ms。暂停发生在方法任务之间，单任务可能超过软切片。

保存的现场是 `ActivityState/Host` 对象中的队列、参数、堆、集合、能力和阶段，不是 Java 调用栈快照，也不支持进程退出后从 JSON 恢复。完成的宿主释放现场；未完成现场仍留内存，尚无磁盘换出和有界活动窗口。

### 9.3 限额和未决事实

实现含多种成本上限，例如普通 union 超过 12 个不同值退为 unknown、表达式深度限制、CFG 单点访问限制、方法总步数限制、Host 上下文和队列限制，以及 APK 总截止时间。它们作用范围不同，并非单一“最大调用深度”。

每个 Host 每内部阶段超过 12000 个已访问上下文会触发上限处理；任务队列超过 6000 的入队检查也会放弃任务并记录诊断。时间上默认目标 300 秒、硬上限 600 秒，监督进程与原子文件替换保存最后有效报告。

第一内部阶段独有而第二阶段未重新推导的事实会作为 `previous_phase_provisional` 候选保留。占位替换要求对象身份、参数/来源、能力入口等兼容，成员不能缩水；这减少静默丢失，但仍可能扩大不确定和重复能力。

`traversal_finished` 只表示当前遍历任务结束，`processed_activities` 还包含内部上限终止。全体初扫、退出码 0、存在报告都不等于质量验收通过。详细与精简报告各自原子替换，不是跨两个文件的事务。

## 10. 代码导航、验证和扩展

### 10.1 建议阅读顺序

以下链接指向文件，符号名用于文件内定位，避免行号随编辑失效。

| 文件 | 优先阅读符号 | 回答的问题 |
|---|---|---|
| [Main](../src/main/java/org/example/Main.java) | worker、调度和监督逻辑 | 谁控制时间与落盘？ |
| [CapabilityIndex](../src/main/java/org/example/CapabilityIndex.java) | `kind`、`discover*`、`resolve` | 如何筛选代码和识别协议？ |
| [DexFlow](../src/main/java/org/example/DexFlow.java) | `V/Summary`、`decode`、`merge`、`union` | 方法内怎样传播？ |
| [CapabilityEngine](../src/main/java/org/example/CapabilityEngine.java) | `beginActivity/seed/enqueue/processJob` | 如何选择入口和区分上下文？ |
| 同上 | `eval/applyWrite/allocationContext` | 实参、字段、返回对象如何绑定？ |
| 同上 | `emit/bridgeMembers/callbackMembers/hostReport` | 能力如何生成和分组？ |
| [AsyncRegistrations](../src/main/java/org/example/AsyncRegistrations.java) | `discover` | 异步入口支持到哪里？ |
| `FrameworkServices`（历史文件，已删除，可在 `a91b113` 中查看） | `index/lookup` | 私有框架规则做了什么？ |
| [CompactReport](../src/main/java/org/example/CompactReport.java) | 导出与统计逻辑 | 签名列表为何不等于对象或注册数？ |

### 10.2 验证材料能证明什么

本教程示例用于解释机制，不冒充新运行的测试结果。已有合成测试入口为 [CapabilitySelfTest](../src/test/java/org/example/CapabilitySelfTest.java)，布局、异步、Fragment、AspectJ、消息注册及调度分别有 fixture。执行：

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
```

`--offline` 要求依赖已缓存。测试通过只证明对应样例，不证明任意程序上的精度。

六 App 最新调度实测见 [SCHEDULER_V3](validation/SCHEDULER_V3.md)：全部初扫约 19～58 秒，最终报告约 48～594 秒；西瓜和 FreeReels 深入任务仍受截止限制。三个原有 App 的累计开发事实重放保持原命中并增加部分事实，但不是独立盲测。当前全部输出的错误归属率尚未完成核验，95% 召回及其他总验收门槛没有整体通过。

这里不复制全部指标，以免产生第二份容易过期的结果表。结果、事实与历史版本入口见 [验证索引](validation/README.md)，已知缺口见 [OPEN_GAPS.json](validation/OPEN_GAPS.json)。

### 10.3 新增规则的记录模板

每次扩展建议记录以下项目，并把私有规则与通用机制分开：

| 项目 | 必须说明的内容 |
|---|---|
| 分类与动机 | G/A/S/P 哪一类；哪个已核验缺口触发 |
| 匹配条件 | owner、签名、参数、字段、结构约束 |
| 传播动作 | 接收对象/实参/返回值如何代入，更新什么关系 |
| 证据与置信 | 注册来源、调用来源；哪些结果必须候选 |
| 正反例 | 应命中例子；同名不同协议、不同对象、未注册对象等不应命中例子 |
| 限制 | 动态情况、时序、混淆、预算耗尽时的行为 |
| 代码与验证 | 对应源码、回归测试、累计事实变化及新样本核验 |

例如新增消息注册器识别时，不能只加一个 `registerHandler` 方法名；应验证名称/对象流向哪个容器，以及容器如何参与 JS 传输。还要加“同名但仅用于普通业务事件”的负例。

后续值得优先验证的精度问题包括字段时序、分支参数配对、重复工厂实例、宽 Activity 入口及回调完整签名校验。它们属于当前限制和改进方向，本文没有把它们列为已实现能力。
