# v4d minimal cross-binding counterexample

The selected v4d fact is `CustomWebBrowserForItemActivity` fact 295:

`TencentVideoWebView.init()@15 -> setWebChromeClient(null)`

v4d assigns that operation to the receiver identified by the medal dialog field `c.ˑˑ:NewsWebView`. That receiver is concrete and is not a `TencentVideoWebView`: `layout_dialog_medal_rule.xml:11` declares `com.tencent.news.webview.NewsWebView`, and `c.mo23171()` stores the inflated view in the field at `c.java:210`.

The evidence is valid until it reaches the medal dialog `NewsWebView` and its `BaseWebView` constructor. The first invalid edge is:

```text
BaseWebView.<init>(Context, AttributeSet, int, boolean)
    -> TencentVideoWebView.init()
```

This is a **constructor receiver type fallback / constructor mismatch**. `TencentVideoWebView.init()` is private and is called only from the exact `TencentVideoWebView(Context, AttributeSet, int)` constructor after `super(...)` returns (`TencentVideoWebView.java:340-350`). Calling a base constructor for a concrete `NewsWebView` does not allow the analysis to replace that receiver with a subclass and execute the subclass-only initializer.

The broader path from the Activity's script interface to `showRankIntroduceDialog` and the medal dialog may be a legal business call, but it cannot change the dialog view's runtime class. Thus even accepting the dialog as conditionally reachable does not make the selected TencentVideo-specific capability valid for that object.

A regression test should reject a subclass initializer reached solely by reverse expansion from a shared base constructor. It should accept the capability only when the allocation or XML inflation type is `TencentVideoWebView` and the same allocation's exact constructor invokes `init()`. Receiver identity must remain tied to the allocation/inflation site across constructor edges.
