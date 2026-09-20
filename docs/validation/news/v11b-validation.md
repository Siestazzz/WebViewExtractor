# Tencent News v11b validation

APK 7.9.50 (`c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`) completed with status `partial`, 43 emitted Activities and 6,263 fact rows. The Activity set is identical to v10a/v9c. Reusing the existing source ownership evidence gives 42 valid hosts and one uncertain host (`ShellActivity`), so the conservative ownership-error upper bound is 1/43 = 2.33%. `v11b-ownership.jsonl` records all 43 rows.

Neither `LongVideoDetailActivity` nor `TvLongVideoDetailActivity` was emitted. The new Map summary therefore did not restore the long-video path. Report evidence cannot show whether the later `y2.put/get` rule failed because neither host has a reported path to inspect.

The earlier registry audit remains correct after a concrete page receiver reaches `onViewCreated`, but it is not the earliest missing edge. The actual pre-registry binding is:

1. `Lcom/tencent/news/video/detail/longvideo/AbsLongVideoDetailActivity;->onCreate(Landroid/os/Bundle;)V` calls virtual `createPage()`.
2. `Lcom/tencent/news/video/detail/longvideo/AbsLongVideoDetailActivity;->createPage()Lcom/tencent/news/video/detail/longvideo/a;` calls `Lcom/tencent/news/kkvideo/detail/longvideo/i;->ʻ(Landroid/content/Context;Lcom/tencent/news/model/pojo/Item;)Lcom/tencent/news/video/detail/longvideo/a;`. The normal branch returns a concrete `IpLongVideoDetailPage`, `TvLongVideoDetailPage`, or `CctvVideoDetailPage` according to the item; JADX could not reconstruct the complete string switch, so the remaining factory cases are DEX-confirmed types but the full branch table is not claimed here.
3. `Lcom/tencent/news/video/detail/longvideo/AbsLongVideoDetailActivity;->setPage(Lcom/tencent/news/video/detail/longvideo/a;)V` writes that exact return to `AbsLongVideoDetailActivity.page:Lcom/tencent/news/video/detail/longvideo/a;`.
4. Final `Lcom/tencent/news/video/detail/longvideo/AbsLongVideoDetailActivity;->getPage()Lcom/tencent/news/video/detail/longvideo/a;` returns that field.
5. The same `onCreate` invokes virtual `onViewCreated(getPage(), getContentView())`. Dynamic dispatch selects `LongVideoDetailActivity.onViewCreated(a,View)` or `TvLongVideoDetailActivity.onViewCreated(a,View)`, so parameter 0 is the same concrete page object created and stored above.
6. Only then does the override call `a.ʼ()`/JADX `mo93686()` on that parameter and register `n.class` in its `j/y2` registry.

The first reusable missing relation is therefore virtual callback argument binding from a base lifecycle method: factory return → base field write → final field getter → argument of an overridden subclass method. It must preserve the concrete Activity receiver and page object. Treating `onViewCreated(a,View)` as an unrelated entry parameter prevents its `a.ʼ()` call and consequently the `n.class` registry write from executing. This agrees with the v11c heap observation that the concrete page's `j/y2` exists and contains other entries but lacks the `qnplayer.n` entry and never reaches `player.g` construction.

The only v10a→v11b fact-row change is `VerticalVideoVideoActivity`, 108→282 rows. Its semantic capability set is unchanged: 21 unique tuples before and after, with zero additions and zero removals when WebView identity/evidence aliases are excluded. All 174 extra rows are alternate/union identities for the already source-valid `VerticalVideoVideoActivity → shortvideo controller → tips.e → CommonPendantFloatView/PendantWebView` path. This is reasonable ownership but duplicate expansion, rather than newly discovered capabilities. No host or semantic capability disappeared.

Sources: `AbsLongVideoDetailActivity.java:46,117-120,169-180,213-225,277-283`; `kkvideo/detail/longvideo/i.java:41-64`; `LongVideoDetailActivity.java:111-125`; `TvLongVideoDetailActivity.java:163-176`; plus the service-dispatch evidence in `v11-longvideo-service-dispatch.md` after the callback parameter is bound.
