# Tencent News v12a validation

APK 7.9.50 (`c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`) completed with status `partial`, 43 emitted Activities and 6,263 fact rows. The emitted Activity set and every per-Activity fact count equal v11b. Source ownership remains 42 valid and one uncertain (`ShellActivity`), for a conservative upper bound of 1/43 = 2.33%. The complete review is in `v12a-ownership.jsonl`.

The two long-video hosts did not recover: neither `LongVideoDetailActivity` nor `TvLongVideoDetailActivity` appears in `activities`. The fixture result therefore does not establish APK behavior.

The v12a diagnostics show partial progress but do not prove the full same-object chain. Both long-video Activities now have candidate diagnostics after their `n2` construction. Their common directly relevant diagnostic includes:

`unresolved_receiver_dispatch:Lcom/tencent/news/ui/listitem/n0;->ʽ(Ljava/lang/Class;Ljava/lang/Object;)V`

This is the registration call in each concrete `onViewCreated(a,View)`. It is consistent with the new setter relation reaching the override and exposing the call. It does **not** mean every branch of the registration failed: the diagnostic also covers unresolved entry/PatchRedirector alternatives. The independent v12a heap probe confirms that the real `class:com.tencent.news.qnplayer.n` Map registration exists, while `player.g` construction is still not visited. Thus the real branch passes registration, and the remaining report limitation lies between that registered page state and the later page factory/presenter dispatch. The report itself supplies no receiver-specific path from the Map entry to `player.g`, so it cannot determine the complete next missing relation.

There are no added or removed hosts, and no added or removed semantic capabilities when WebView identity aliases are excluded. The raw delta in each of `NewsDetailActivity` and `PushDetailActivity` is eight replaced operation rows: two union receivers, each referenced by four operation facts. In v11b the two union summaries had top-level type `com.tencent.smtt.sdk.WebView`; v12a assigns new union IDs and top-level type `unknown`.

This top-level aggregation change is not loss of all concrete receiver types. The v12a `webview_alternatives` remain populated. For the shared union they contain concrete `NewsWebView` alternatives from `NewsDetailView.mWebView`, `NewsDetailWebViewCreator`, the pool, `LinkedList.removeFirst`, and `LruCache.remove`, plus an X5 `com.tencent.smtt.sdk.WebView` PatchRedirector alternative. The Activity-specific companion union has the same mixed family. The operation API and sites are unchanged. The unresolved impact is limited to how the report chooses one summary type for a heterogeneous union; source ownership and the available concrete alternatives are preserved.

The long-video source truth remains:

`AbsLongVideoDetailActivity.onCreate(Bundle)` → `createPage()` → `kkvideo.detail.longvideo.i.ʻ(Context,Item)` → `setPage(a)` writes `AbsLongVideoDetailActivity.page` → final `getPage()` returns that field → virtual `onViewCreated(getPage(),getContentView())` → subclass override invokes `a.ʼ()` and then `n0.ʽ(n.class,e/p)`.

This source chain is documented for interpreting the diagnostic; v12a output does not yet demonstrate that all these identities survived analysis.
