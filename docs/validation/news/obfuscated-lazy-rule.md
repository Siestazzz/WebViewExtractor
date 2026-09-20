# Obfuscated Kotlin Lazy rule for the editor WebView

Tencent News 7.9.50 bundles Kotlin with renamed declaration owners. `Lkotlin/i;` is the Lazy interface: it declares `getValue()Object` and `isInitialized()Z`. `Lkotlin/jvm/functions/a;` is the zero-argument initializer interface with `invoke()Object`. The default lazy factory is `Lkotlin/j;->ʼ(Lkotlin/jvm/functions/a;)Lkotlin/i;`; JADX renders it as `j.m172914(...)`. It constructs `SynchronizedLazyImpl` while preserving initializer argument 0. The mode-aware sibling selects one of the three standard implementations.

The editor chain is exact and shared by `RichEditorActivity` and `QAEditorActivity` through `BaseEditorActivity`:

1. `BaseEditorActivity.<init>()` allocates `Lcom/tencent/news/activity/d;` with the current `BaseEditorActivity` receiver. The object stores that receiver in field `d.ˈˈ`.
2. The constructor passes this initializer to `kotlin.j.ʼ` and stores the returned `kotlin.i` in `BaseEditorActivity.ʻʼ`.
3. `d.invoke()Object` calls the typed synthetic helper `BaseEditorActivity.ˊˆ(BaseEditorActivity): REWebView` with its captured receiver.
4. That helper calls `capturedHost.findViewById(com.tencent.news.publish.p.f91997)` and casts the result to `REWebView` on the ordinary path.
5. Typed accessor `BaseEditorActivity.ˊˏ(): REWebView` loads the same `ʻʼ` field, invokes `kotlin.i.getValue()Object`, and casts the result to `REWebView`.
6. `BaseEditorActivity.initView()` uses this accessor, passes the result to the rich-editor initializer, constructs the chrome client with the same WebView, updates its user agent, and installs the client.

The initializer captures the runtime Activity object, although its field is typed as `BaseEditorActivity`. Both concrete editor Activities inherit this constructor and `initView`, so the `findViewById` receiver is the respective concrete host. This establishes the settings and client ownership for both editors.

A reusable detector should recognize the factory semantically, then preserve object identity across initializer allocation, lazy field, `getValue`, typed cast, and accessor. Interface shape by itself is unsafe: many unrelated suppliers expose `invoke()Object`, and arbitrary property containers can expose `getValue()Object`. Require the factory body to retain the initializer in a known lazy implementation, the result to enter a stable field, and the typed accessor and initializer to converge on the same WebView type and captured host. Do not merge separate initializer objects merely because they use the same interfaces or factory.

The v8a and v8c reports contain the same 37 Activity names. There are no added or removed hosts, so the v8a Activity-level ownership verdicts remain reusable. v8c changes recovered capabilities for existing hosts, especially the two editors. Reuse of an Activity verdict does not validate unrelated receiver alternatives; editor facts should follow the exact lazy identity chain above.

All APK-defined descriptors named in the JSON rule were checked in `test/runs/symbols/news.jsonl`. Source aliases `m172914`, `m37976`, and `m38000` correspond to DEX names `kotlin.j.ʼ`, `BaseEditorActivity.ˊˆ`, and `BaseEditorActivity.ˊˏ`.
