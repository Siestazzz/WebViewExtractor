# v3f misses for published development gold

Four published development Activities are absent from v3f. These are the shortest source/DEX chains useful for generalization.

## AdMosaicXiJingPageActivity

The decisive relation is a returned interface object, followed by a concrete implementation chosen from the Activity context:

- `Lcom/tencent/news/tad/business/ui/activity/AdMosaicXiJingPageActivity;` constructs `com.tencent.news.tad.business.ui.mosaic.a` (`AdMosaicXiJingPageActivity.java:945`).
- `Lcom/tencent/news/tad/business/ui/mosaic/a;->getDKWebView(Landroid/content/Context;)Lcom/tencent/ams/dsdk/view/webview/DKWebView;` returns `new e(context)` for this Activity (`mosaic/a.java:46-53`).
- `Lcom/tencent/news/tad/business/ui/mosaic/e;-><init>(Landroid/content/Context;)V` constructs its concrete `AdWebView` and installs `mosaic.d`/`mosaic.f` clients (`mosaic/e.java:102-145`).

Model rule: propagate the returned `DKWebView` implementation only from the matching factory branch and retain the Activity-context predicate. Do not propagate every possible `DKWebView` implementation through the interface return type.

## RoseLiveVideoActivity and VideoPreviewActivity

Both miss the same field/interface-to-manager chain:

- Rose field `f97551:Lcom/tencent/news/video/n2;` is assigned by `f97554.m152929()` (`RoseLiveVideoActivity.java:446`) and its delayed inner `e.run()` invokes `Lcom/tencent/news/video/n2;->ˋי(Ljava/lang/String;J)V` (`RoseLiveVideoActivity.java:278`).
- VideoPreview field `f121686:Lcom/tencent/news/video/n2;` is assigned by `f121687.m152929()` (`VideoPreviewActivity.java:116`) and `Lcom/tencent/news/video/js/VideoPreviewActivity;->ˈי(Ljava/lang/String;)V` calls `n2.ˋי` (`VideoPreviewActivity.java:271-283`).
- `Lcom/tencent/news/video/n2;->ˋי(Ljava/lang/String;J)V` calls `Lcom/tencent/news/video/VideoPlayManager;->openByUrl(Ljava/lang/String;J)V` (`n2.java:2922-2934`).
- The YSP branch reaches `Lcom/tencent/news/video/VideoPlayManager;->ʿʼ(Ljava/lang/String;J)V`, then `Lcom/tencent/news/ysp/YspMediaPlayer;->ʽ(Landroid/content/Context;Landroid/view/ViewGroup;)V` (`VideoPlayManager.java:3930-3970`).

Model rule: follow the controller field assignment and concrete interface-return implementation through `m152929()`, then preserve the `VideoDataSource.isYspPlayer()` branch. The WebView allocation is inside the player attach method, not near the Activity.

## WeiboGraphicDetailActivity

This miss requires a view-field and callback/controller chain:

- Activity field `f109067:Lcom/tencent/news/topic/weibo/detail/graphic/view/WeiBoDetailHeadView;` is assigned from the layout (`WeiboGraphicDetailActivity.java:622`).
- `setItemData(...)` is called at line 788 and invokes `bindWeiboVideoData(...)` (`WeiBoDetailHeadView.java:497-520`).
- The bound `WeiboGraphicVideoView.checkAndStart(J)` calls its `n2` controller's `ˋי(Ljava/lang/String;J)V` (`WeiboGraphicVideoView.java:580-609`).
- The remainder is the same `n2 -> VideoPlayManager -> YspMediaPlayer.ʽ` chain above.

Model rule: preserve the concrete inflated view type stored in the Activity field, then follow its owned child/controller field. A general view type or Context match is not sufficient.

## Callback allocation trial guard

Expanding callback allocation within two registration edges is useful for `new Client(...) -> wrapper.setWebViewClient(Object)`, but accept the callback only when allocation and registration share the same receiver identity. Reject alternatives introduced solely by interface return types, generic `Object` adapter parameters, Context, or common WebView superclass constructors. Explicit `super`/delegate calls should add the reached implementation; unrelated sibling implementations should not be unioned.
