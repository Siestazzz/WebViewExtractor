# v4 unresolved H5 factory audit

The three remaining `unresolved_collection:union...` hosts all reach the same concrete H5 v2 factory. They do not require subtype enumeration. The missing summary edge is the replacement of an initialized receiver field by an interface-returned collection, followed immediately by a receiver method that reads that field.

## Shared object and factory chain

`H5Fragment.addWebView()` assigns the acquired `H5WebView` to `H5Fragment.mWebView`. `H5Fragment.initWebView()` invokes `mWebView.I(this, loadURL, client)`. `H5WebView.I(Fragment, String, client)` invokes `E(fragment, this)`, so the receiver of `E`, its second parameter, and the Fragment field all denote the same WebView object.

`H5WebView.E(Object, H5WebView)` performs these operations on that receiver:

1. The constructor previously initialized `this.y = new ArrayList()`.
2. `this.y.clear()`.
3. `b.a()` reads the globally installed `b.c` factory.
4. Interface dispatch calls `n$c.t(Object, H5WebView): List`.
5. The returned list replaces **receiver field** `this.y`.
6. The same receiver invokes `D(obj, h5WebView)`.
7. `D` iterates **receiver field** `this.y` and registers every non-null element on the second-parameter WebView, which is the same object.

The exact alias is therefore `H5Fragment.mWebView == H5WebView.I receiver == H5WebView.E receiver == E parameter h5WebView == H5WebView.D receiver == D parameter h5WebView`. The collection write is `E receiver.y`; the collection read is `D receiver.y`. Treating the returned `List` as an unconnected `union` loses this receiver-field def/use edge.

The factory is selected at application initialization by `n.a() -> b.d(new n$b(), new n$c(), new n$d())`; `b.d` writes its second argument to static `b.f45354b`; `b.a()` returns that exact field. Thus the interface target is `n$c.t`, not an arbitrary implementation of `b.c`.

## Host-specific prefixes

- `MktH5ContainerV2.initFragment(Bundle)` creates `MktH5FragmentV2`, assigns it to inherited `H5Container.h5Fragment`, and adds that same Fragment. `MktH5FragmentV2.initWebView()` calls `H5Fragment.initWebView()`, entering the shared chain.
- `CFHyWebActivityV2.initFragment(Bundle)` creates `CFHyFragmentV2`, assigns it to inherited `h5Fragment`, and adds it. `CFHyFragmentV2.initWebView()` calls `H5Fragment.initWebView()`, then adds its separate finance JS interface. The core factory still applies to the same `mWebView`.
- `H5PreRender.initFragment(Bundle)` creates plain `H5Fragment(bundle)`, assigns it to inherited `h5Fragment`, and adds it. Its normal Fragment view/load lifecycle enters `addWebView -> initWebView` on the same Fragment field.

## DEX descriptors verified in the APK symbol table

- `Lctrip/android/view/h5v2/view/H5Fragment;->addWebView()V`
- `Lctrip/android/view/h5v2/view/H5Fragment;->initWebView()V`
- `Lctrip/android/view/h5v2/view/H5WebView;->I(Landroidx/fragment/app/Fragment;Ljava/lang/String;Lctrip/android/view/h5v2/f/b;)V`
- `Lctrip/android/view/h5v2/view/H5WebView;->E(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)V`
- `Lctrip/android/view/h5v2/b;->a()Lctrip/android/view/h5v2/b$c;`
- `Lctrip/base/init/n$c;->t(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)Ljava/util/List;`
- `Lctrip/android/view/h5v2/view/H5WebView;->D(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)V`
- `Lctrip/base/init/n;->a()V`
- `Lctrip/android/view/h5v2/b;->d(Lctrip/android/view/h5v2/b$e;Lctrip/android/view/h5v2/b$c;Lctrip/android/view/h5v2/b$d;)V`

## Correct factory set and remaining false subtype facts

`n$c.t` constructs 35 unconditional core plugins. It then makes seven Bus lookups; their results are conditional and may be null. It adds `H5SamSungWalletPlugin` when the attached object is an `H5Fragment`, which is true for all three prefixes above. v4's fallback produces 48 candidate implementations for each host, but six have no creation/add path in this factory and are false positives:

- `H5BaseLocatePlugin`
- `H5BaseUtilPlugin`
- `H5BusinessPluginBase`
- `H5Plugin`
- `H5BaseImagePluginV2`
- `CTH5PayCookiePluginV2`

The first five are base/super types reached only through subtype enumeration. `CTH5PayCookiePluginV2` is a separate pay-cookie subtype and is not returned by any of the seven factory Bus keys. Conversely, `destination/H5GSPluginV2` is a real conditional Bus slot whose concrete result cannot be inferred merely by enumerating subclasses. The right model is the 35 explicit allocations, seven parameterized Bus-return slots, and the Fragment predicate for Samsung.

## v4 ownership replay

The v4 report contains the same 55 Activities as v3d. Replaying the source-reviewed ownership decisions with v4 fact counts yields **52 valid, 1 wrong, 2 uncertain**. The wrong Activity remains `CtripCommonFeedBackActivity`; the two uncertain entries remain cleanup-only `HotelDetailCharityProjectActivity` and `HotelVideoActivity`. The three hosts in this document are valid; only their six fallback plugin implementations are false capability facts. See `v4-ownership.jsonl`.
