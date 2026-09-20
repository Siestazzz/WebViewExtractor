# YSP provider binding

## Result

`com.tencent.news.ysp.b` is implemented by `com.tencent.news.ysp.YspPlayerService`. The binding uses QNRouter's generated class-key service map. It does not use `ServiceLoader`, a hand-written static provider field, or runtime annotation scanning.

The `@Service` annotation on `YspPlayerService` is compile-time input. Generated `ServiceMapGenL4qnplayer.init()` performs the actual registration:

```text
service class: Lcom/tencent/news/ysp/b;
implementation name: "_default_impl_"
APIMeta implementation: Lcom/tencent/news/ysp/YspPlayerService;
singleton service flag: true
```

Application router loading calls `Loader -> L4qnplayer.RouterEntry.init -> ServiceMapGenL4qnplayer.init`. `ServiceMap.autoRegister` stores the `APIMeta` in `ServiceMap.sImplMap`, whose outer key is `b.class.getName()` and inner key is `_default_impl_`.

## Shortest registration, lookup, and return chain

```text
Loader
 -> Lcom/tencent/news/L4qnplayer/RouterEntry;->init()V
 -> Lcom/tencent/news/qnrouter/service/ServiceMapGenL4qnplayer;->init()V
 -> ServiceMap.autoRegister(b.class, "_default_impl_", APIMeta(b, YspPlayerService, true))
 -> ServiceMap.sImplMap[b.class.getName()]["_default_impl_"]

Services.call(b.class) or Services.get(b.class)
 -> Services.get(b.class, "_default_impl_", null)
 -> Services.get(b.class.getName(), "_default_impl_", null)
 -> ServiceMap.get(b.class.getName())
 -> APIMeta.getImplClazz() == YspPlayerService.class
 -> APIMeta default creator invokes YspPlayerService.<init>()
 -> singleton instance cached in APIMeta.f95514
 -> YspPlayerService.ʻ()
 -> new YspMediaPlayer()
 -> a.ʽ(Context, ViewGroup) dispatches to YspMediaPlayer.ʽ(Context, ViewGroup)
```

The unknown receiver at `Lcom/tencent/news/ysp/b;->ʻ()Lcom/tencent/news/ysp/a;` can therefore be resolved to `YspPlayerService` when the generated L4qnplayer router entry has initialized and the default implementation has not been replaced by a QFix redirector.

## Call-site conditions

- `VideoPlayManager.setVideoParams` selects this provider only when `VideoParams.isYspPlayer()` is true and its cached YSP player is null. It uses `Services.call(b.class)`, stores the returned `a`, and later invokes `a.ʽ(context, playerView)`.
- The live player selects it only when its `f82668` YSP-mode flag is true. It uses nullable `Services.get(b.class)`, stores the returned `a`, and attaches only when both that object and the target `ViewGroup` are non-null.
- `APIMeta(..., true)` makes the **service** singleton. `YspPlayerService.ʻ()` still constructs a fresh `YspMediaPlayer` on each normal invocation.
- Both `YspPlayerService.<init>` and `YspPlayerService.ʻ` first check QFix `PatchRedirectCenter`. A present redirector may replace construction or the returned player; `new YspMediaPlayer()` is the unpatched/default path shipped in the APK.

## Verified DEX symbols and fields

- `Lcom/tencent/news/ysp/b;->ʻ()Lcom/tencent/news/ysp/a;`
- `Lcom/tencent/news/ysp/YspPlayerService;-><init>()V`
- `Lcom/tencent/news/ysp/YspPlayerService;->ʻ()Lcom/tencent/news/ysp/a;`
- `Lcom/tencent/news/ysp/YspMediaPlayer;-><init>()V`
- `Lcom/tencent/news/ysp/a;->ʽ(Landroid/content/Context;Landroid/view/ViewGroup;)V`
- `Lcom/tencent/news/ysp/YspMediaPlayer;->ʽ(Landroid/content/Context;Landroid/view/ViewGroup;)V`
- `Lcom/tencent/news/qnrouter/service/ServiceMapGenL4qnplayer;->init()V`
- `Lcom/tencent/news/qnrouter/service/ServiceMap;->autoRegister(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APIMeta;)V`
- `Lcom/tencent/news/qnrouter/service/ServiceMap;->ʻ:Ljava/util/concurrent/ConcurrentHashMap;` (`sImplMap` in source metadata)
- `Lcom/tencent/news/qnrouter/service/Services;->call(Ljava/lang/Class;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/Services;->get(Ljava/lang/Class;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/Services;->get(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APICreator;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/APIMeta;->ʼ:Ljava/lang/Class;` (implementation class)
- `Lcom/tencent/news/qnrouter/service/APIMeta;->ʽ:Z` (singleton flag)
- `Lcom/tencent/news/qnrouter/service/APIMeta;->ʿ:Ljava/lang/Object;` (cached singleton)

Source anchors: `ServiceMapGenL4qnplayer.java:21-30`, `RouterEntry.java:18-24`, `ServiceMap.java:28-32,90-96`, `Services.java:34-38,185-238,331-373`, `APIMeta.java:9-15,33-62`, and `YspPlayerService.java:19-23`.
