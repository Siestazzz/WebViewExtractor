# Tencent News v16a source review

Scope: Tencent News 7.9.50, SHA-256 `c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`. v16a is a development variant. Sealed holdouts were not read, and reduced output was not treated as proof that old facts were false.

## Delta and ownership

Against v15a, v16a falls from 42 to 36 emitted Activities and from 5,386 to 4,521 raw facts. Normalized comparison finds 101 additions and 966 removals, net -865. The 101 additions are almost entirely replacement receiver identities for existing YSP groups and load operations; they do not create a new host or new implementation surface. `v16a-delta.json` retains every changed tuple.

The emitted set has 35 source-valid hosts and one uncertain host (`ShellActivity`), a conservative non-valid upper bound of 1/36 (2.78%). `v16a-ownership.jsonl` covers all 36 emitted Activities. This emitted-only ownership result does not excuse the six missing source-valid hosts.

## Six disappeared hosts

All six disappearances contain a real regression, although several old reports also contained duplicate or cross-bound rows.

- `SplashAdDynamicPreviewActivity`: its reached callback posts `d` through `View.post(Runnable)`. `d.run()` constructs `SplashAdDynamicView`, attaches it to the Activity container, and calls `showSplashAd()`. The inherited conditional gesture path constructs and binds `EasterEggWebView`. The source-confirmed conditional settings/client/bridge subset must survive; arbitrary EasterEgg instances elsewhere must not be joined.
- `APWebJSBridgeActivity`: this is a direct regression. `onCreate(Bundle)` calls `initWebPage()`, writes either a new `APSystemWebPage` or `APX5WebPage` to `webPage:IAPWebPage`, and then calls `initUI()` or `toPureH5Pay()` on that same field. Each concrete page gets its Activity layout WebView and passes it into the corresponding wrapper constructor. The wrapper constructor directly calls `InitWebView()` on itself.
- `VerticalVideoVideoActivity`: the source-valid conditional suffix is Activity → concrete short-video controller → `tips.e.setData(...)` → `new CommonPendantFloatView(...)` → owned pendant WebView. Removal of that subset is a regression; the full old 127 rows were not all independently validated.
- `VisitVerticalVideoActivity`: the manifest subclass executes the inherited vertical-video path. Its conditional pendant subset is source-valid, though it was not part of the current positive canonical set. Its absence is still an analyzer regression rather than evidence that the inherited path is false.
- `RoseLiveVideoActivity`: the Activity owns `n2`, invokes its playback path, and conditionally reaches `VideoPlayManager → YspPlayerService → YspMediaPlayer → attached BaseWebView`. The confirmed YSP settings, client, bridge and four message handlers are real conditional capabilities.
- `FullPlayVideoActivity`: its owned `FullVideoPlayer` supplies the concrete `n2`; the same conditional YSP suffix attaches the WebView. Its confirmed YSP group is likewise a regression.

## APWebJSBridge exact implementation rule

This is the largest concentrated loss: 14 setting facts and 12 callback facts across the system and X5 branches.

The polymorphic carrier is field `Lcom/tencent/midas/jsbridge/APWebJSBridgeActivity;->webPage:Lcom/tencent/midas/jsbridge/IAPWebPage;`. The normal branch writes either `new APSystemWebPage()` or `new APX5WebPage()` in `APWebJSBridgeActivity.initWebPage()V`. The reached `onCreate(Bundle)` then calls one of these interface methods on that exact field:

- `Lcom/tencent/midas/jsbridge/IAPWebPage;->initUI(Landroid/app/Activity;)V`
- `Lcom/tencent/midas/jsbridge/IAPWebPage;->toPureH5Pay(Landroid/app/Activity;Lcom/tencent/midas/api/request/APMidasBaseRequest;)V`

Concrete interface dispatch selects `APSystemWebPage.initUI/toPureH5Pay` or `APX5WebPage.initUI/toPureH5Pay`. Each method calls `Activity.setContentView`, obtains the branch-specific layout WebView by `Activity.findViewById`, and immediately constructs:

- `Lcom/tencent/midas/jsbridge/APWebView;-><init>(Landroid/app/Activity;Landroid/webkit/WebView;Lcom/tencent/midas/jsbridge/IAPWebViewCallback;)V`, or
- `Lcom/tencent/midas/jsbridge/APX5WebView;-><init>(Landroid/app/Activity;Lcom/tencent/smtt/sdk/WebView;Lcom/tencent/midas/jsbridge/IAPX5WebViewCallback;)V`.

The constructor stores parameter 1 in its `mWebview` field and directly invokes its own `InitWebView()V`. `InitWebView` gets settings from that exact field, applies the seven branch settings, and installs the wrapper's already-constructed `mChromeClient` and `mWebViewClient` on the same field using the platform/X5 client APIs. These are direct constructor-body calls, not callback execution. The clients' override members become relevant only because the exact client objects are installed by `setWebChromeClient`/`setWebViewClient`.

A safe rule is: when a reached constructor directly invokes a private initializer on the same newly allocated receiver, execute that initializer; preserve constructor argument → receiver field identity; when it installs a client field on that exact WebView, expose only overrides of the installed client. Do not seed arbitrary methods of the wrapper or arbitrary constructed callbacks.

## Other substantive removals

The two editor Activities lose a real subset. `BaseEditorActivity` passes its initializer to the Kotlin Lazy factory, which stores it; `getValue()` later invokes the same initializer and returns the typed `REWebView`. The removed editor clients are installed on that exact accessor result. This is an actual factory/register/use chain, not construction-only callback reachability.

The four Login Activities retain some facts but lose shown Dialog capabilities. Existing source evidence proves the relevant flows construct `AuthDialog`/`TDialog` with the Activity and call `show`; their `onCreate` creates and attaches the WebView, and their setup method installs clients/settings. A Dialog constructor alone would be insufficient, but the actual show/onCreate path makes this subset real.

`WebAdvertActivity` loses a source-valid nested wrapper initialization subset through `WebAdvertView/AdLoadingWebView`; duplicated Dt bridge aliases are not automatically accepted. The five ILive hosts each lose two loads and a preload bridge; this audit leaves those three-row groups unresolved rather than classifying them from the count change. Operation-only removals on retained hosts are mixed: some are alias cleanup, while any source-reached call on the concrete object remains a regression.

## Rule boundary

Do not restore global `callbackEntries` or “constructor means callback executes.” Required repairs are bounded relations: reached same-receiver constructor initializer, actual interface dispatch from a concrete field value, Kotlin Lazy registration/getValue invocation, `View.post`/Handler registration with the same Runnable, shown Dialog lifecycle, and exact installed-client registration. The known constructed-but-unregistered callback negative must remain negative.
