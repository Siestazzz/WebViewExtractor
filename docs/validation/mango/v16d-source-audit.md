# Mango v16d source audit

v16d preserves the source-verified Diana recovery from v16a. It does not restore the major construction-seed regressions.

The three recovered Erlang names are exact `ImgoWebView.registerWebHandler` registrations with declared `(String)V` methods, so their return is valid. `VideoInteractionEvent` and `VideoSetPlayerMuted` remain missing despite the same source structure; `mgTransfer` and `showMangPushShare` remain missing as well.

The unresolved high-impact boundaries remain precise:

- mgadplus: stored `showMedia -> am.a$q0` registry value, exact `q0.handler`, same outer `am.a`, then `Y -> Q` and `I -> D -> U`.
- DTF: `onResume -> I`, callback stored by `baseverify.d.a`, actual `y5.b.e(Map,APICallback)` registration, exact `FaceLoadingActivity$h.onSuccess`, then protocol branch `z -> J`.
- Pangle: synchronous `j(WebView)/r.c(j)` and `adexpress.bp.bp.c(WebView,r,String)` factory paths. No pre-registration asynchronous callback is source-proven or required.

These rules and exact descriptors are in `v16-callback-entry-repair.{md,json}`. Their continued absence is a reachability limitation, not negative source evidence.
