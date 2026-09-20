# Erlang v15a regression facts

`v15a-erlang-regression-facts.jsonl` contains four independently sourced facts: separate registration and endpoint-member facts for `mgTransfer` and `showMangPushShare`. The exact constrained receiver type is `com.hunantv.imgo.h5.ImgoWebView` in `com.mgtv.erlang.ErlangLiveActivity`.

The binding chain is `ErlangLiveActivity.fullWebView/detailWebView -> WebUIFragment.mWebView -> ImgoWebView.registerWebHandler`. `ImgoWebView.java:666-667` explicitly registers both literal names. `ImgoWebJavascriptImpl.registerHandler` installs its `e0` handler, whose audited fixed reflection rule uses `getDeclaredMethod(registrationName, String.class)`. The implementation declares the exact endpoints `mgTransfer(Ljava/lang/String;)V` and `showMangPushShare(Ljava/lang/String;)V` at lines 4643 and 5694.

The generator is idempotent and does not modify canonical. Scoring this four-row file gives v14c 4/4 and v15a 0/4. The saved evaluator outputs are `v15a-erlang-regression-v14c-score.json` and `v15a-erlang-regression-v15a-score.json`; this directly captures the two lost registrations and their two lost callable members.
