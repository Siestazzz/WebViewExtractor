# v7a persistent false-owner breaks

The first emitted fact for each persistent false owner was traced against its current v7a evidence chain. All three failures come from propagating Activity ownership across a reachable call without a WebView object-flow path.

## HippyDetailActivity

The first fact is `NewsWebView.removeAllJavascriptInterface()`. The chain is valid only through `HippyDetailActivity.initListener()` constructing `com.tencent.news.hippy.ui.detail.a`. That class extends `H5JsApiScriptInterface`, but its constructor only calls the superclass constructor. The next reported edge, from the constructor to `H5JsApiScriptInterface.showRankIntroduceDialog(JSONObject)`, has no invoke instruction, callback registration, selector, or argument flow. It is method-set expansion caused by inheritance.

`showRankIntroduceDialog` can access a medal dialog whose field owns the `NewsWebView`; constructing an unrelated subclass instance does not execute that method. A general fix must require a real call or modeled dispatch before traversing inherited business methods.

## MobileQQActivity

The application Activity calls the Tencent share SDK. In the unsupported-native-share branch, `QQShare.shareToQQ` constructs `TDialog` using the Activity as a context. `TDialog` itself owns field `i`, the `com.tencent.open.d.b` WebView that removes the interface.

The control-flow path is real, but the ownership path is absent: `MobileQQActivity` neither receives nor stores the dialog/WebView. Passing `this` as an `Activity` parameter must not make all SDK-internal allocations Activity-owned.

## QzoneShareActivity

The current chain enters `QzoneShare.shareToQzone`, follows an SDK fallback into `QQShare.shareToQQ`, and then reaches the same `TDialog` field. Even when the fallback edge is real, the downstream WebView remains owned by the transient SDK dialog. There is no return, caller-field assignment, or proven Activity view-tree attachment.

The reusable rule is to preserve control reachability separately from object ownership through delegates and fallbacks. An Activity/Context argument alone cannot bind a callee-internal WebView to the caller.

The structured file records exact descriptors, receiver IDs, actual owners, and the first unsupported edge. No Activity blacklist is proposed. Sealed holdout data was not used.
