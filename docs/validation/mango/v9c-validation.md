# Mango v9c ownership and callback validation

The v9c report contains 84 activities, versus 85 in v9b. The unchanged gold scores do not establish absence of regression: the three removed NFT activities have no current canonical capability rows and therefore do not affect the score.

## Host-set changes

Both additions are valid ownerships.

- `com.mgtv.miniplay.MiniPlayActivity` owns `MiniPlayFragment`; its comment action reaches `MiniPlayerCommentDialogFragment`, `CommentInfoView.X6`, `WebViewFragment.ue`, and the fragment's `ImgoWebView`. The path is conditional but belongs to the activity.
- `com.bytedance.sdk.openadsdk.core.activity.base.TTDelegateActivity` dispatches on its intent and constructs/stores Pangle widget/dialog instances. Its `kc`, `vr`, `xw`, `pb`, `s`, and `p` fields lead to widget-owned `SSWebView` fields. These are instance paths, not a package-wide class match.

All three removals are regressions against independently confirmed valid ownership:

- `AntiqueDetailActivity.W -> AntiqueDetailFragment.E7 -> com.mgsz.hunantv.nft.MgNftViewer.setNftData -> NftWebviewLayout.setupData`.
- `DigitalDetailActivity.x3 -> com.hunantv.nft.MgNftViewer.setNftData -> NftWebviewLayout.setupData`.
- `DigitalModelViewActivity.U2 -> com.hunantv.nft.MgNftViewer.setNftData -> NftWebviewLayout.setupData`.

In both NFT implementations, `NftWebviewLayout` is a `FrameLayout` carrier. `setupData` posts a `Runnable` which reads `mViewBinding.webview` and invokes `android.webkit.WebView.loadUrl(String)`. The earliest common missing binding in v9c is the carrier/ViewBinding field edge from `NftWebviewLayout.mViewBinding.webview` to the WebView receiver; the activity-to-`setupData` call path remains independently established. This aligns with the release's platform hierarchy and carrier relevance changes, but is a data-flow finding rather than an inference from the score.

## WebContainer q0 callback

The q0 registration contains all 23 contract members. Thirteen have `dispatch_status=observed`; ten remain `unresolved`. Cross-inner-client recognition is fixed for `onPageStarted`, legacy `onReceivedError`, `onPageFinished`, `onReceivedHttpError`, `shouldOverrideUrlLoading`, `onProgressChanged`, and `onRenderProcessGone`. The last two use the independently checked two-`iget` chain from the inner client's `this$0` to `ImgoWebView.q0`, followed by the interface invocation.

`dispatch_status=unresolved` only says that the analyzer did not observe a call. It is not evidence that the member is never dispatched.

The machine-readable audit is `v9c-validation.json`; `audit_v9c.py` regenerates it and the 84-row `v9c-ownership.jsonl`. Two consecutive runs produced identical files. The ownership distribution is 79 valid and 5 uncertain.
