# AdCore entry repair audit

This audit reuses `adcore-wrapper-factory.json` and adds the missing entry half for three public development hosts. It preserves exact provider, callback, wrapper, and native-WebView identities. One prior host binding, `AdBonusPageActivity → EasterEggWebView`, is not supported by the reviewed source and remains pending rather than being forced into a repair.

## AdMosaicXiJingPageActivity

The Activity creates one `mosaic.a(Context,AdOrder)` provider, saves it in field `ʼˉ`, and passes the same provider as constructor parameter 3 of its `XJPageView` subclass. The DSDK WebView component obtains that provider from its own Mosaic engine.

The asynchronous entry is an actual registration. `DKMosaicWebViewComponentImpl.setJSEngine(JSEngine)` passes a new `DKMosaicWebViewComponentImpl$12` to `DynamicUtils.runOnUiThread`. The callback's `this$0` field is the same component, and `run()` calls `component.initWebView(component.mContext)`. There is also a synchronous reached entry: `getView()` calls `initWebView(mContext)` on the main thread when `mDKWebView` is null.

`initWebView` calls the exact engine provider's `getDKWebView(Context)`. Virtual dispatch reaches `mosaic.a.getDKWebView(Context)`, which returns `new mosaic.e(context)` only when the context resolves to `AdMosaicXiJingPageActivity`. The return is stored in the component's `mDKWebView` field.

`mosaic.e` is a non-View wrapper. Its constructor creates a concrete platform `AdWebView`, stores it in field `e.ˈˈ`, applies settings, and installs `mosaic.d` and `mosaic.f` on that same field. `e.getDKWebView(): View` returns the exact field, and the component returns that View to the mosaic UI. This is the required wrapper-to-native relation. It is a system WebView path; no X5 object should be inferred here.

## AdLandingPageActivity

The existing chain remains correct but its activation condition is now explicit. `AdLandingPageWrapper.onCreate(Bundle)` calls `Handler.post(Runnable)` with a new `AdLandingPageWrapper$2`. The posted object's `run()` calls `access$200(wrapper)`, then `delayLoad()`.

`delayLoad()` invokes the concrete Activity listener's `createAdPage(...)`, assigns that exact non-View-declared return to `mAdPage`, then synchronously calls `attachToCurrentActivity()` and `loadWebView(String)` on the same object. `loadWebView` reaches `AdWebViewWrapper.create(...)`. The locally proven system result is `AdSysWebViewWrapperImpl`, whose field `a:android.webkit.WebView` is the same object returned by `getWebview()` and used for settings/client installation.

The requested X5 implementation is loaded by the string `com.tencent.adwebview.appwebview.AppWebViewWrapperImpl`; it and `AppJsWebChromeClient` are absent from this APK's DEX. Runtime delivery is possible but locally unknown. The analyzer must not fabricate an X5 constructor, native field, settings, or client surface.

## AdBonusPageActivity

The reviewed source does not establish this Activity as an `EasterEggWebView` host. `BaseAdEasterEggActivity.onCreate` invokes virtual `createContentView(this)`, and the override returns a new `AdBonusPageView`. The concrete Activity initializes `AdBonusPagePlayer`, registers `AdBonusPagePlayerController` with `PlayerEventObserver`, and starts video playback. Neither it nor the reviewed base class constructs `InteractiveEastEggController`/`EasterEggWebView`, calls `bind`/`bindWebview`, or attaches such a WebView.

Calls to `AdDrawGestureManager.dispatchBonusPageEvent(...)` report page events. They do not prove that the originating gesture page's WebView is transferred into or displayed by the bonus Activity. The 17 inherited EasterEgg facts for this host therefore remain pending/unsupported. A correct repair must not seed them from common family names, manager reachability, or constructed callbacks.

## Reusable rule and fixtures

Carry an exact provider through engine storage, execute its factory only from a reached direct call or an actually installed and dispatched callback, store the exact factory return, and resolve a non-View wrapper to its native WebView only through a proven getter/field relation. Keep system and X5 branches as separate objects.

Positive fixtures should cover a provider registered in one engine and invoked by its installed callback, and a posted Runnable that stores a non-View page return before creating a wrapper. Negative fixtures must cover an unregistered callback, callback registered on a different engine instance, two wrapper implementations behind one interface, absent reflective X5 classes, and a manager event that never transfers or attaches its WebView.

All exact DEX identities are listed in the JSON companion and checked against `test/runs/symbols/news.jsonl`. No canonical facts or production code were changed, and sealed holdouts were not read.
