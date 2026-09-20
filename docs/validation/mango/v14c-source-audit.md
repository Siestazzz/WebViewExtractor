# Mango v14c source audit

The Fragment root repair is real but stops before Diana's asynchronous request callback. The focused probe tied `MiniAppFragment.onActivityCreated`, the three-argument `Diana.startDianaApp`, and both `DianaView.startLoad` contexts to the same inflated child (`view:2131828340`). In the full report this replaces `ViewGroup.getChildAt`-only receiver identities in `MangoMiniAppActivity` with paths through `createAndAttachDianaView -> DianaView.renderMiniApp -> Page.m -> PageWebViewWrapper.mWebView`.

That repair does not reach the network callback. The independently verified continuation is `requestMiniAppInfo(new DianaView$c)`; the callback is copied through `com.mgtv.diana.h0.c$d$a.h` and `c$d.h`, captured by `c$a`, registered at `okhttp3.e.H(okhttp3.f)`, and invoked from `c$a.onResponse` through a posted `ny.a`. Only then can `DianaView$c.onSuccess`, `renderMiniApp`, AppService, and `onServiceReady` reach PageWebView/ServiceWebView. v14c still emits none of the 81 constrained MGVideoPlay Diana facts.

The five conditional Diana hosts remain absent: both `com.imgo.vodgames` activities, both `com.mgtv.diana.vodgames` activities, and `com.mgtv.litevod.LiteVodActivity`. Their absence is a regression relative to v13c, not evidence that they lack WebView capability.

`MangoMiniAppActivity` adds 39 and removes 13 semantic rows, net +26. The direct identities anchored at `createAndAttachDianaView` are source-consistent. The `unresolved_collection` alternatives and paired `:0`/non-`:0` identities are not proven separate objects, so they remain candidate aliases. This audit found no new host and no newly added API member that source proves false.

See `diana-render-entry-chain.md`, `diana-request-callback-chain.md`, and `v14c-miniapp-validation.md` for the underlying source/DEX contracts.
