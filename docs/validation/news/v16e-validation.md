# Tencent News v16e final validation

v16e successfully processes all 2,329 manifest Activities. Its report status is `partial` in the normal capability-report sense, with no worker failure. It emits 37 hosts and 4,552 raw facts. Ownership is 36 valid and one uncertain (`ShellActivity`), giving a conservative non-valid upper bound of 1/37 (2.70%). The complete emitted ownership file is `v16e-ownership.jsonl`.

Against v16a, v16e adds `APWebJSBridgeActivity`, 31 normalized tuples, and removes nothing. Against the last successful broad baseline v15a, it still lacks five emitted hosts and has 131 additions versus 965 removals. Both complete comparisons are stored in `v16e-delta-from-v16a.json` and `v16e-delta-from-v15a.json`.

## Current canonical replay

Strict scoring against the unchanged current canonical produces:

| Category | Matched / Expected | Recall |
|---|---:|---:|
| Bridge | 1281 / 1393 | 91.96% |
| Setting | 261 / 366 | 71.31% |
| Callback | 194 / 240 | 80.83% |

Positive-host recall is 21/30 (70%). The nine missing canonical hosts are AdLanding, AdBonus, Splash preview, Vertical video, Rose live, AdMosaic, FullPlay, and both LongVideo Activities. The full replay is `v16e-canonical-evaluation.json`. AdBonus requires the source-ownership caution below.

## AP recovery

The AP concentrated gap is recovered. The report contains 14 settings and four concrete installed-client records across `APWebView` and `APX5WebView`; those registrations match the expected 12 callback override facts. Source identity is correct: concrete `IAPWebPage` dispatch obtains the layout WebView, passes it as wrapper constructor parameter 1, the wrapper stores it in `mWebview`, and the same reached constructor invokes `InitWebView()` on itself. Settings and the two client fields are installed on that exact WebView.

The difference from v15a is limited to load-operation alias multiplicity. It does not reopen the 14-setting/12-callback AP gap.

## Editor Lazy remains missing

`QAEditorActivity` and `RichEditorActivity` remain at 51 facts each, exactly as in v16a. Their source-confirmed Lazy settings/client subset is not recovered.

The actual registration entry is `kotlin.j.ʼ(Function0): kotlin.i` with the exact `activity.d` initializer. The analyzer must preserve that initializer through the concrete Lazy instance returned as `kotlin.i`, the `BaseEditorActivity.ʻʼ` field, and `getValue()` on the same instance. `getValue()` must return the cached result of that initializer to the typed `REWebView` accessor. Merely analyzing the initializer body does not connect its returned object to the accessor consumer. Arbitrary constructed `Function0` objects must remain inactive.

## Remaining source regressions

The five hosts missing relative to successful v15a contain real source-confirmed subsets:

- Splash preview uses actual `View.post(Runnable)`; the posted `d.run()` constructs, attaches, and shows `SplashAdDynamicView`, whose conditional gesture path reaches the EasterEgg WebView.
- Vertical video reaches `tips.e.setData` and constructs the owned `CommonPendantFloatView`/pendant WebView. `VisitVerticalVideoActivity` inherits this path but is outside the current positive canonical.
- Rose live and FullPlay own their concrete player/controller and conditionally reach `YspMediaPlayer`, which attaches its BaseWebView.

Other retained-host losses from v16a remain: shown Login Dialog settings/clients, WebAdvert nested-wrapper initialization, and the two editor Lazy groups. Load/evaluate-only alias reductions are not automatically regressions or fixes; their receiver chains must be checked individually.

Pre-existing canonical gaps also remain. LongVideo requires exact lifecycle collection registration and dispatch. AdLanding requires its actually posted wrapper Runnable, listener factory return, and non-View page-to-wrapper chain. AdMosaic requires its actually registered DSDK `$12` callback or synchronous `getView` entry, exact custom provider return, and `mosaic.e` wrapper-to-native AdWebView relation. These precise entrances are documented in `v16-next-gap-chain` and `v17-adcore-entry-repair`.

The reviewed source does not currently prove `AdBonusPageActivity` owns an `EasterEggWebView`. It constructs `AdBonusPageView` and a video player/controller and reports manager events, but no construction, bind, attachment, or cross-Activity transfer of the originating EasterEgg WebView was found. Its canonical EasterEgg facts remain unresolved and should not drive a repair without a concrete ownership edge.

No canonical or production files were modified, and sealed holdouts were not read.
