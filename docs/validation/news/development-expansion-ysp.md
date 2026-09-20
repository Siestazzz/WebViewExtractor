# YSP media-player development positives

This group is conditional: an Activity owns the player/controller path, but the WebView exists only when the selected `VideoDataSource` is a YSP item. No callback is propagated merely because an Activity has a video view.

## Shared implementation

`VideoPlayManager.openByUrl(String,long)` reaches `VideoPlayManager.ʿʼ(String,long)` (`VideoPlayManager.java:2849-2877,3930-3970`). Its YSP branch calls `YspMediaPlayer.ʽ(Context,ViewGroup)`, which creates the anonymous `BaseWebView`, applies seven direct settings, installs `YspMediaPlayer$b`, creates `com.tencent.news.ysp.f`, and binds it (`YspMediaPlayer.java:1197-1253`). `BaseWebView` contributes four constructor settings (`BaseWebView.java:145-159`).

The concrete JS registration is `f.ʽ() -> addJavascriptInterface(this,"YspJSBridge")` (`f.java:114-135`). The only annotated transport is `f.invoke(String):String` (`f.java:70-111`). It parses JSON `method` and `params`, selects a listener map entry, and invokes interface `c.ʻ(String,JSONObject)`. `YspMediaPlayer.ٴ(f)` registers four concrete handlers (`YspMediaPlayer.java:1409-1420`): `videoStatus`, `videoProcess`, `resume`, and `getScreenSize`. Their endpoint descriptors are recorded separately in canonical facts; arbitrary public methods are not exposed.

## Individually checked hosts

- `VideoPreviewActivity`: `m153855(String)` calls owned controller `n2.m154236(String,long)` (`VideoPreviewActivity.java:271-285`), which calls its `VideoPlayManager.openByUrl` (`n2.java:2922-2934`).
- `RoseLiveVideoActivity`: its delayed inner `e.run()` calls the Activity-owned `n2.m154236` (`RoseLiveVideoActivity.java:268-280`); `m119004(boolean)` schedules that runnable when the URL is not ready (`RoseLiveVideoActivity.java:958-981`).
- `WeiboGraphicDetailActivity`: `m135249()` passes its item to `WeiBoDetailHeadView.setItemData` (`WeiboGraphicDetailActivity.java:775-790`), which calls `bindWeiboVideoData` (`WeiBoDetailHeadView.java:497-520`). That binds `WeiboGraphicVideoView`; its start path owns `n2`, supplies `VideoDataSource`, and calls `checkAndStart` (`WeiboGraphicVideoView.java:920-938`), eventually using the same controller path for a YSP source.
- `KkVideoDetailDarkModeActivity`: the Activity owns `KkVideoDetailDarkModeFragment`; the fragment delegates video binding through `kkvideo.detail.controller.g` to `kkvideo.player.x1`. The player uses the shared video controller/manager path for a YSP data source. This binding is conditional on the detail item selecting YSP.
- `KkAlbumDarkModeActivity`: this is a concrete manifest Activity subclass of `KkVideoDetailDarkModeActivity` (`KkAlbumDarkModeActivity.java:17`), so it inherits the same fragment creation and conditional player path. It was checked separately rather than inferred from a similarly named screen.

## Exact DEX route symbols

- `Lcom/tencent/news/ysp/f;->invoke(Ljava/lang/String;)Ljava/lang/String;`
- `Lcom/tencent/news/ysp/f;->ˋ(Ljava/lang/String;Lcom/tencent/news/ysp/c;)V`
- `Lcom/tencent/news/ysp/YspMediaPlayer;->ʽ(Landroid/content/Context;Landroid/view/ViewGroup;)V`
- `Lcom/tencent/news/ysp/YspMediaPlayer$c;->ʻ(Ljava/lang/String;Lorg/json/JSONObject;)Ljava/lang/Object;`
- `Lcom/tencent/news/ysp/YspMediaPlayer$d;->ʻ(Ljava/lang/String;Lorg/json/JSONObject;)Ljava/lang/Object;`
- `Lcom/tencent/news/ysp/YspMediaPlayer$e;->ʻ(Ljava/lang/String;Lorg/json/JSONObject;)Ljava/lang/Object;`
- `Lcom/tencent/news/ysp/YspMediaPlayer$f;->ʻ(Ljava/lang/String;Lorg/json/JSONObject;)Ljava/lang/Object;`

All method descriptors were checked against both the APK method table and `test/runs/symbols/news.jsonl`.
