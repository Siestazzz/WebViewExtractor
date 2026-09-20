# v7a Mango focused validation

The only bridge change from v6 is the recovery of the known mglive `checkUpdate` handler. The v6 missing row

`Lcom/mgtv/ui/live/mglive/webview/a$j;->a(Ljava/lang/String;Lcn0/d;)V`

is absent from the v7a missing set, and no new missing row appears. v7a emits it at `Lcom/mgtv/ui/live/mglive/webview/a;->p(Context,BridgeWebView,ShareProxy,a$a0)V@137`, where registration name `checkUpdate` and a newly constructed `a$j` are passed to `BridgeWebView.n(String,cn0.a)`. The receiver is the Activity-bound mglive `BridgeWebView` with id `activity:com.mgtv.ui.live.mglive.webview.WebViewActivity/view:2131834272`.

The recovered member is the correct registry interface endpoint. Decompiled source declares `a$j implements cn0.a` and overrides `a(String,cn0.d)`. Independent DEX symbols contain that exact public descriptor. v7a reports `implementation=com.mgtv.ui.live.mglive.webview.a$j`, resolution `registered_handler_interface_dispatch`, and only that member.

The method's body later calls `Class.forName("om.mgtv.live.play.app.utils.CheckUpdateHelper").getMethod(checkUpdate, Context.class).invoke(...)`. That is downstream business dispatch after the JS handler has been selected. v7a does not emit `getMethod`, `CheckUpdateHelper`, or a reflected `checkUpdate(Context)` method as a bridge member anywhere in the report. The extra bridge match is therefore the real `cn0.a` endpoint, not the internal reflection target.

The output Activity set is identical to v6: 82 Activities, with no addition or removal. The v6 ownership review can be reused unchanged: 77 valid and five uncertain. v7a leaves the other metric counts unchanged and reduces the residual missing list from 31 to 30.

No holdout material was read, and no production, scoring, or canonical fact file was modified.
