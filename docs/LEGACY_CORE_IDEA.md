> 历史设计文档，保留原文供版本对照；不代表当前默认分析入口，也不保证与现有 `--legacy` 的每个细节完全一致。当前设计见 [CORE_IDEA.md](../CORE_IDEA.md)。

# WebView 容器提取核心思路

## 1. APK 信息读取

项目同时使用 dexlib2 和 Soot：

- dexlib2 读取 APK 中的全部 DEX，提取类名、直接父类和成员字段类型。
- Soot 将 APK 方法体转换为 Jimple，用于提取方法内部声明的引用类型。
- 两部分结果按完整类名合并。

Soot 使用 APK 输入模式，开启 MultiDex，并允许 phantom reference，避免部分依赖缺失时直接终止。

## 2. 三类关系

图以类为节点，边方向为“外层候选类指向它使用的内层类”。

### `inherits`

类 `A` 的直接父类是 `B` 时建立：

```text
A --inherits--> B
```

父类来自 DEX 的 superclass 描述符。传播可以逐层进行，因此支持间接继承 WebView。

### `holds`

类 `A` 的成员字段类型是 `B` 时建立：

```text
A --holds--> B
```

字段由 dexlib2 读取。对象数组会去掉数组维度后记录元素类，基本类型忽略。

内部类还会建立所属关系：

```text
Outer --holds--> Outer$Inner
```

多层内部类按直接层级连接，如 `A -> A$B -> A$B$C`。内部类编译产生的外部类回指字段会造成反向环，因此字段类型如果指向任意外层类会被过滤。

### `declares`

`declares` 通过 Soot 的 Jimple 方法体提取，不依赖 DEX debug local 信息。

对每个 application class 的 concrete method：

1. 调用 `retrieveActiveBody()` 获取 Jimple `Body`。
2. 遍历 `body.getUnits()`。
3. 只处理赋值类语句 `DefinitionStmt`。
4. 跳过 `IdentityStmt`。Jimple 用它绑定 `this` 和方法参数，因此参数不会被算作方法内声明。
5. 要求赋值左侧是 `Local`，读取该局部变量的静态类型。
6. 只记录 `RefType`；如果是 `ArrayType`，先逐层取元素类型。

以下右值会被排除：

- `ParameterRef`：方法参数。
- `ThisRef`：当前对象。
- `FieldRef`：成员字段读取。
- `Local`：局部变量复制。
- `CastExpr`：类型转换产生的临时变量。

例如：

```java
void run(WebView input) {
    WebView a = input;
    QYWebviewCorePanel panel = new QYWebviewCorePanel(...);
}
```

参数 `input` 和复制变量 `a` 不产生关系；对象创建会产生：

```text
CurrentClass --declares--> QYWebviewCorePanel
```

结果按类使用集合去重。方法体解析失败时只跳过该方法，不影响其他类。

注意：Jimple 是字节码中间表示，并不完全等同于反编译 Java。当前规则能排除参数和常见搬运语句，但其他编译器临时变量仍可能被识别为 `declares`。

## 3. 候选容器传播

初始节点包括 Android、腾讯 X5、UC、MIUI 和 Huawei WebView 基类。

程序先建立“目标类 -> 所有指向它的关系”的反向索引，再执行队列传播：

1. 从队列取出已知 WebView 候选类。
2. 找到所有继承、持有或声明它的类。
3. 将这些类标记为新候选容器并记录原因。
4. 新候选继续入队，直到没有新类。

因此，由 `inherits`、`holds`、`declares` 或内部类关系连接到 WebView 的类都会成为候选容器。

## 4. 路径选择

图可能包含环和多条路径。搜索使用递归栈避免环，并缓存每个节点的一条最佳路径。

每个起点只保留一条到 WebView 基类的路径：

1. 优先选择包含更多 Activity/Fragment 的路径。
2. 数量相同时按完整路径文本排序，保证结果稳定。

Activity/Fragment 通过沿继承链查找预设 Android、AndroidX 和 support 基类判定。

起点依次为：

1. 没有被其他候选容器包裹的 Activity/Fragment。
2. 已被包裹、但自身也是候选容器的 Activity/Fragment。
3. 没有被包裹，且路径中不含 Activity/Fragment 的普通 class。

第二类起点可以避免宿主回指导致有效 Activity/Fragment 从结果中消失。

## 5. 输出文件

程序根据 APK 文件名在 `output` 目录生成：

- `<apk>_webview_container_classes.json`：候选类及其发现原因。
- `<apk>_webview_outer_paths.json`：起点到 WebView 基类的详细路径。
- `<apk>_webview_outermost_classes.json`：按 Activity/Fragment 和普通 class 分组的简化类名。
