# v4e constructor cross-binding conclusion

The v4d medal-dialog counterexample is gone. None of the 60 remaining `TencentVideoWebView.init()` facts for `CustomWebBrowserForItemActivity` contains the medal dialog field or its `NewsWebView` allocation.

The first remaining fact, whose receiver is `activity:com.tencent.news.webview.CustomWebBrowserForItemActivity/view:2131302026`, is a **valid conditional receiver** despite the shortened evidence `addImageClickListner() -> TencentVideoWebView.init()`:

- Decimal resource ID `2131302026` is `0x7f09168a`, declared as `@id/tencent_video_web_view` in `public.xml:10703`.
- `CustomWebBrowserForItemActivity.getWebView()` checks `isTencentVideoDomain()`. On the true branch it inflates layout `tencent_video_web_view`, then casts `findViewById(f73986)` to `TencentVideoWebView` (`CustomWebBrowserForItemActivity.java:2167-2185`).
- `tencent_video_web_view.xml:6-9` declares that exact ID as `com.tencent.news.video.auth.webview.TencentVideoWebView`.
- The exact `TencentVideoWebView(Context,AttributeSet,int)` constructor invokes its private `init()` after `super(...)` (`TencentVideoWebView.java:340-350`).
- `addImageClickListner()` operates on `mWebView`, which is assigned from `getWebView()` during `initView`; it does not itself allocate the widget. The receiver identity, rather than the abbreviated call edge, makes this fact valid.

There are four groups of 15 duplicated init facts: Activity view ID, constructor parameter with the same view ID, entry parameter, and an unknown union. The first three can represent the same conditional Tencent widget. The union also includes the normal `BaseWebView` resource `2131303140`, patch redirect output, and null. It must remain branch-qualified: the Tencent-specific initializer is valid only for alternative `2131302026`, never for the other union alternatives.
