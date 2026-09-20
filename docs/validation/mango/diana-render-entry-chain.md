# Diana `renderMiniApp` entry chain

The v13e compact probe places the break before `DianaView.renderMiniApp`: that method, the `AppService` constructor, and `DianaView.onServiceReady` are relevant but unvisited. The probe filter printed `MiniAppFragment.onViewCreated`, but it did not request `onActivityCreated`; absence of an `onActivityCreated` line is therefore not negative evidence. In fact, its `CONTEXTS` section reports two contexts for the exact three-argument `Diana.startDianaApp(Activity, Fragment, DianaAppStartupParam)` overload, proving that this entry was reached. `onViewCreated` resolves `mini_box_view`, tags it, stores it in `WeakReference<DianaView> dianaRef`, and installs `MiniAppFragment$a` as the app-event listener (`MiniAppFragment.java:261-268`).

The real entry starts in a different Fragment lifecycle callback:

1. `MiniAppFragment.onActivityCreated(Bundle)` builds `DianaAppStartupParam`, checks `getActivity() != null` and the Diana enable flag, then calls `Diana.startDianaApp(getActivity(), this, build)` (`MiniAppFragment.java:147-168`). This is a normal Fragment lifecycle callback, not an arbitrary `on*` method.
2. Exact overload `Diana.startDianaApp(Activity, Fragment, DianaAppStartupParam)` checks initialization, non-null/non-detached Fragment, and a non-null `ViewGroup` root. It reads `((ViewGroup) fragment.getView()).getChildAt(0)`, tests `instanceof DianaView`, stores the parent Fragment, initializes that exact view, then calls `dianaView.startLoad()` (`Diana.java:341-355`). This `getChildAt(0)`/cast is the binding from Fragment root to the XML `DianaView`; it is separate from `dianaRef.get()`.
3. `DianaView.startLoad()` registers the view with the SDK manager, shows loading UI, and calls `requestMiniAppInfo(new DianaView$c())` (`DianaView.java:1720-1733`).
4. `requestMiniAppInfo` passes the exact callback object into `com.mgtv.diana.h0.c...a(interfaceC0712c)...` (`DianaView.java:779-783`). `DianaView$c.onSuccess(MiniAppInfo)` invokes `DianaView.onSuccess`, initializes the facade, and binds the remote service; its failure overload also binds when not already connected (`DianaView.java:247-271`). This is an asynchronous SDK callback contract.
5. `DianaView.onSuccess` creates `mMiniAppLoader = new com.mgtv.diana.h0.d(context, mLoadMiniAppCallback)` (`DianaView.java:1573-1586`). `mLoadMiniAppCallback` is initialized once in every DianaView constructor as `new DianaView$a()` (`DianaView.java:1835`).
6. `DianaView$a`, implementing `com.mgtv.diana.h0.d.c`, calls `goNextStepByResult(...)` from its package-load completion members (`DianaView.java:184-204`). Separately, `DianaView$c` initiates `bindService`; Android invokes the registered `ServiceConnection.onServiceConnected` on the same DianaView instance.
7. The two async halves synchronize through fields. `goNextStepByResult(true)` calls `renderMiniApp(this)` immediately if `mIpcBridgeService != null`, then sets `packageReady=true` (`DianaView.java:454-468`). `onServiceConnected` stores `mIpcBridgeService` and calls `renderMiniApp(this)` when `packageReady` is already true (`DianaView.java:1400-1428`). Thus either completion order reaches the same private renderer once both conditions hold.
8. `renderMiniApp(FrameLayout)` then creates `AppService`/ServiceWebView and the page manager. The separately documented exact `AppService` event listener chain eventually invokes `DianaView.onServiceReady()`, which asks the page manager to create the Page/PageWebView.

The source chain begins at `MiniAppFragment.onActivityCreated` and the probe proves that the exact three-argument `Diana.startDianaApp` overload is reached. The earliest probe-supported break is therefore **after entry to that overload and before `renderMiniApp`**. The existing filtered log cannot distinguish failure to materialize `fragment.getView()/getChildAt(0)` as the `DianaView`, failure to reach or propagate `startLoad`, or loss in either asynchronous completion chain. A complete visited/heap/XML probe is required to choose among those edges. The source evidence still proves that `WeakReference.get()` is not the renderer entry.

A reusable model needs four bounded rules:

- retain actual Android Fragment lifecycle contexts already reached, including `onActivityCreated`, without inferring absence from a filtered trace;
- preserve `fragment.getView()` root identity through `ViewGroup.getChildAt(constant index)` and `instanceof`/cast;
- preserve callback object identity through builder/setter registration, dispatch only interface members actually invoked by the async API;
- join the package-ready and service-connected states on the same DianaView receiver. Either callback may be first; do not globally seed `renderMiniApp` or all callback methods.

All branches are conditional on Diana being enabled, valid Fragment attachment/root shape, package lookup/load, and service connection. The resulting capabilities remain host candidates with a proven construction/registration path.

## Enhanced v14 pre-fix probe

`test/runs/v14-miniapp-before.json.contexts.json` resolves the earlier uncertainty without copying its large heap. It records two `MiniAppFragment.onActivityCreated` contexts and two contexts for the exact three-argument `Diana.startDianaApp` overload. `DianaView.startLoad` has one context, but its receiver is `unknown`, identified only as the return of `ViewGroup.getChildAt`. `DianaView$c.<init>(DianaView)` is reached with that same unknown receiver. `DianaView$c.onSuccess`, `DianaView.onServiceConnected`, and `renderMiniApp` have zero contexts. Separately, `DianaView$a.<init>` captures an XML lookup object identified as `entry_parameter/view:2131828340`, rather than the `getChildAt` receiver.

This proves two independent missing links. First, the Fragment root/child object is not unified with the concrete XML `DianaView` instance, even though `startLoad` is entered. Second, the registered network and service callbacks are not scheduled from their actual registrations. The source chain and the earlier lower/upper bound remain valid, but `onActivityCreated` scheduling is no longer a candidate failure.
