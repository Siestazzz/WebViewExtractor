# v9a custom callback audit

v9a emits eight registrations with `callback_field`. Seven store a `com.mgadplus.brower.g` listener in `ImgoAdWebView.W`; one stores a `kq.d` listener in `com.mgsz.h5.ImgoWebView.q0`. The output Activity set contains the same 85 hosts as v8a/v8c, with no addition or removal.

## mgadplus registrations

All seven mgadplus registrations have the correct WebView and listener instance:

- `CustomWebActivity` field/view `E` receives a newly constructed `CustomWebActivity$c`.
- `AdLandingPageActivity` installs its new `$h` listener on its bound `ImgoAdWebView`.
- `MgExtendBizActivity` reaches the same view through field `F`/layout alternatives and passes a new `jm.c$a` from the wrapper constructor.
- `RewardAdFreeActivity` has two concrete embedded-ad views/listeners: `am.a$w` at `am.a.U()` and `MgAdSessionView$c` at `MgAdSessionView.j3()`.
- `MGVideoPlayActivity` reaches those same two embedded-ad constructions through its verified fragment/session chain; each setter receiver is the corresponding session-owned `ImgoAdWebView`, rather than the Activity's unrelated primary WebView.

The exact setter `ImgoAdWebView.setWebViewLifeCycleCallback(g)` stores parameter 0 in `this.W`. Every reported member signature exists in independent DEX symbols. The ten-member effective `g` contract is complete for each concrete listener: concrete overrides are used where present and inherited implementations are retained otherwise.

Calls through `W` are observed for `loadCustomScheme`, page finish/start, legacy `onReceivedError(WebView,int,String,String)`, `onReceivedHttpError`, `onTitle`, and `shouldOverrideUrlLoading`. The same-field scan did not observe calls for `onProgressChanged`, modern `onReceivedError(WebView,WebResourceRequest,WebResourceError)`, or `shouldInterceptRequest`. Their false values should be read as unresolved rather than proof that no runtime dispatch exists. Thus the seven callbacks that restore the canonical callback score are real registrations, while forwarding evidence remains a separate per-member property.

## WebContainer registration

The additional WebContainer registration is real: `WebViewFragment.nc()` passes a new `WebViewFragment$b` to `ImgoWebView.setH5LifeCycleCallback(kq.d)`, which stores parameter 0 in `q0`. The previously reviewed Activity-to-fragment/XML-WebView chain supports the receiver even though v9a displays its local id as `constructor_parameter`.

Its member metadata is incomplete. `kq.d` extends `kq.f`, but v9a enumerates only the 18 methods declared directly by `kq.d`. It omits five effective parent-contract implementations:

- `kq.e.onPageFinished(WebView,String)`
- `WebViewFragment$b.onPageStarted(WebView,String,Bitmap)`
- `WebViewFragment$b.onReceivedError(WebView,int,String,String)`
- `kq.e.onReceivedHttpError(WebView,WebResourceRequest,WebResourceResponse)`
- `kq.e.shouldOverrideUrlLoading(WebView,String)`

All five descriptors exist in independent DEX symbols. Two reported flags are also false negatives: `WebViewFragment$b.onProgressChanged(WebView,int)` and `kq.e.onRenderProcessGone(RenderProcessGoneDetail)` are both invoked directly through `q0` in `ImgoWebView`, but v9a marks them `dispatch_observed=false`. Other reported true flags correspond to actual `q0` calls.

The two missed calls use the same precise DEX carrier. `ImgoWebView$3.onProgressChanged(WebView,int)` executes `iget-object` from its synthetic `this$0` field to the outer `ImgoWebView`, then a second `iget-object` of `ImgoWebView.q0:kq.d` into local `v0`, a null check, and `invoke-interface {v0,webView,progress}, kq.d.onProgressChanged(WebView,int)`. There is no cast, getter, returned carrier, or longer-lived field alias. Contract resolution maps that interface signature to the concrete `WebViewFragment$b.onProgressChanged` override.

`ImgoWebView$2.onRenderProcessGone(WebView,RenderProcessGoneDetail)` likewise reads `$2.this$0`, reads outer `q0` into local `v0`, null-checks it, and invokes `kq.d.onRenderProcessGone(RenderProcessGoneDetail)`. Its effective implementation is inherited from `kq.e`. The callback invocation intentionally passes only the detail argument; the Android WebView parameter belongs to the enclosing client callback, not the `kq.d` contract signature.

The reusable dispatch rule must normalize `inner.this$0 -> outer.callbackField` as a read of the registered field. An `invoke-interface` owned by the contract counts as observed for its exact contract signature, after which that signature is resolved to the concrete override or inherited superclass implementation. Requiring the field read to occur directly in a method declared on the outer WebView misses both cases.

The structural rule is sound when it requires a WebView-subclass instance method with one callback parameter, a direct `this.field = parameter` store, and at least one call through that same field to a contract method. Member enumeration must traverse the full parent-interface contract and resolve each signature to the effective concrete/superclass implementation. `dispatch_observed` must then be computed per signature from calls through that field; one observed contract method must not mark sibling methods observed.

The JSONL companion contains one audit row per registration, including receiver ids, listener allocation ids, all reported members, missing members, flag corrections, and the exact two-`iget` instruction chains. No holdout material was read.
