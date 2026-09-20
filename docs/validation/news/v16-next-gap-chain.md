# Tencent News v16 next-gap chain

The highest-impact remaining public gap is the missing LongVideo host chain. It affects both `LongVideoDetailActivity` and `TvLongVideoDetailActivity`. Existing source and heap audits already establish the Activity-created `services.e/p`, page registry lookup, `player.g` constructor argument, `n2`, `VideoPlayManager`, and the final YSP `BaseWebView`. The earliest still-useful event rule is the presenter lifecycle registration and dispatch, not another constructor seed.

## Shortest actual registration chain

`AbsLongVideoDetailPage.י(View,b)` invokes the concrete `ˋˋ(View,b): Pair` factory. The TV/IP factory constructs `tv.m` or `ip.v`; before either constructor returns, it obtains the page's lifecycle object and calls:

`Lcom/tencent/news/kkvideo/detail/longvideo/l;->ʻ(Lcom/tencent/news/kkvideo/detail/longvideo/g;)V`

with `this`. The registrar checks the argument for null and duplicate membership, then adds that exact object to:

`Lcom/tencent/news/kkvideo/detail/longvideo/l;->ʻ:Ljava/util/ArrayList;`

After the factory returns and `Pair.second` is stored as the presenter, the same `AbsLongVideoDetailPage.י` call invokes:

`Lcom/tencent/news/kkvideo/detail/longvideo/l;->ʼ()V`.

That method iterates the same ArrayList, casts each retrieved element to `longvideo.g`, and invokes `onPageCreateView()` on the exact stored object. Dynamic dispatch therefore selects `tv.m.onPageCreateView()` or `ip.v.onPageCreateView()`. Pair storage preserves presenter control identity, but it is not the event registration; the earlier `l.ʻ(this)` call is.

The conditional playback suffix is:

`player.g.ʻʽ(int,boolean)` → `player.g.ʻʼ(playlist.c)` → `player.g.ʻˈ(Item,boolean,List)` → stored `qnplayer.n.ʾˏ(f)` → `h0.ʾˏ(f)` → `b1.ˆ(VideoDataSource)` → `n2.setVideoParams(VideoParams)`.

`player.g.ˈˈ` is constructor parameter 0, already source-verified as the Activity-created `services.e/p` on the normal branch. Downstream, `YspMediaPlayer.ʽ(Context,ViewGroup)` stores the exact `TNVideoView` container, creates one `BaseWebView` in its own field `ʻ`, and attaches that same WebView to the container. The WebView capabilities belong to this attached `BaseWebView`; the `TNVideoView` is the ownership container.

Playback remains conditional. `onPageCreateView()` begins model setup, but a non-empty model result and valid initial or user selection are required to enter `g.ʻʽ/ʻʼ`. PatchRedirector branches remain unknown.

## Reusable rule

Model an instance callback only when a reached registrar stores the exact callback argument into a concrete storage object and a reached dispatcher reads the same storage on the same registry receiver before invoking the callback interface. Preserve four identities: callback object, registry receiver, storage field, and dispatch result. Follow `add/put` or field assignment through iterator/map/array retrieval, cast, and interface dispatch. Keep null, duplicate, removal, replacement, branch, and asynchronous-data conditions.

Construction is not activation. Reaching `new tv.m`, `new ip.v`, `new player.g`, an interface-shaped callback constructor, `Pair.second`, or an ordinary presenter field must not independently execute callback methods. This directly separates the LongVideo positive from the known near-API negative where an unregistered second callback is merely constructed.

## Synthetic regression constraints

Positive cases should require one exact `register(C) → same storage → dispatch() → C.callback()` chain and should deduplicate the same `C` when it is also returned in a Pair. A two-registry fixture must execute only the callback stored in the dispatched registry.

Negative cases must cover: a constructed but unregistered callback; registration into `R1` followed only by dispatch of `R2`; removal before dispatch; Pair/ordinary-field storage without a dispatcher; and a registered lifecycle callback whose empty model or invalid position prevents the later player-selection suffix.

All descriptors in the structured companion were checked against `test/runs/symbols/news.jsonl`. This audit uses public development hosts only and does not read sealed holdouts.
