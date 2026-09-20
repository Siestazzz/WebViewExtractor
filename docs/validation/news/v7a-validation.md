# Tencent News v7a independent validation

## Ownership

All 34 emitted Activities were reviewed. The result is 31 `valid`, 3 `wrong`, and 0 `uncertain`; the conservative ownership error upper bound is therefore 3/34 = 8.82%. `v7a-ownership.jsonl` contains one normalized row per emitted Activity.

The unchanged hosts reuse their earlier source paths from the v4d/v4e audits. The three persistent false owners remain `HippyDetailActivity`, `MobileQQActivity`, and `QzoneShareActivity`: none owns or receives the emitted WebView objects. Their facts still arrive through unrelated business callbacks or SDK calls.

## QNRouter metadata change

v7a newly emits four Activities: `RoseLiveVideoActivity`, `WeiboGraphicDetailActivity`, `FullPlayVideoActivity`, and `VideoPreviewActivity`. Each is a real conditional YSP WebView host. The concrete Activity/player chain reaches `VideoPlayManager`; for a YSP `VideoParams`, `VideoPlayManager.setVideoParams` resolves `com.tencent.news.ysp.b` through `Services.call`. `ServiceMapGenL4qnplayer.init()` registers that interface to `YspPlayerService`, `YspPlayerService.ʻ()` returns a new `YspMediaPlayer`, and `YspMediaPlayer.ʽ(Context, ViewGroup)` constructs and configures its `BaseWebView`.

Exact DEX chain:

- `Lcom/tencent/news/qnrouter/service/ServiceMapGenL4qnplayer;->init()V`
- registry entry: interface `Lcom/tencent/news/ysp/b;`, key `_default_impl_`, implementation `Lcom/tencent/news/ysp/YspPlayerService;`
- `Lcom/tencent/news/ysp/YspPlayerService;->ʻ()Lcom/tencent/news/ysp/a;`
- `Lcom/tencent/news/ysp/YspMediaPlayer;->ʽ(Landroid/content/Context;Landroid/view/ViewGroup;)V`

The extra YSP facts on the two Kk detail Activities use the same valid service-provider resolution. This validates the new metadata edge, though it does not make unrelated candidate paths on those Activities precise. Candidate facts reached through reporting helpers, generic `Context`, or unrelated recycled WebViews remain cross-binding risks; ownership is valid while fact-level binding is mixed.

Compared with v6, the four new hosts account for the output-host increase from 30 to 34. Bridge recall rose from 1252/1365 to 1264/1365 and callback recall from 181/228 to 193/228. Settings rose from 221/338 to 275/338, including YSP/BaseWebView settings on the newly resolved paths.

## First remaining public YSP break

`LongVideoDetailActivity` is the first public YSP host absent from v7a. Its manifest Activity extends `AbsLongVideoDetailActivity`. `onViewCreated` constructs `longvideo.services.e` with the concrete `TNVideoView`; `e` extends `longvideo.services.i`, which extends `com.tencent.news.qnplayer.h0`. That inherited player exposes the `n2` controller used by `VideoPlayManager`, after which the already-supported QNRouter provider edge reaches `YspMediaPlayer`.

The first missing edge is therefore the host-to-player composition, before provider resolution: the analysis does not connect the `new longvideo.services.e(...)` allocation owned by `LongVideoDetailActivity.onViewCreated` through superclass `i -> h0` to DEX `Lcom/tencent/news/qnplayer/h0;->ˆˑ()Lcom/tencent/news/video/n2;` (jadx `m117080()`) and its `n2`/`VideoPlayManager`. The provider/registry edge itself is already modeled, as shown by the four new hosts.

`v7a-ysp-break.json` records this as a reusable receiver/superclass-factory clue. `TvLongVideoDetailActivity` shares the same base/service family and is expected to benefit from the same generalized edge, but it is not used as the first example.

This review uses only the public canonical facts and decompiled source. Sealed holdout facts were not opened or changed.
