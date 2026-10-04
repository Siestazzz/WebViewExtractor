# Frozen v8: two ReadingWebView callback regressions

This investigation reads the frozen v7/v8 reports, generic engine code and a synthetic fixture. No App decompiled source was read and no production Java was changed. A subsequent independent source-agent proof supplied by the parent is incorporated below. Machine-readable evidence, exact report SHA-256 values and complete matching v7 facts are in `generic-v8-two-callback-diagnosis.json`.

The two lost gold matches are `ReadingWebView$m.onPageFinished(android.webkit.WebView,String)` and the modern `shouldInterceptRequest(android.webkit.WebView,android.webkit.WebResourceRequest)` on WebViewActivity.

## What the reports establish

The target host has two whole wrapper-install facts in v7, at the native setters inside WebViewContainerInner's twin method (offsets 6 and 14). Both bind the same factory-created ReadingWebView object and wrapper allocated at ReadingWebView.setWebViewClient offset 7. Their complete evidence starts at `WebViewActivity.onDestroy → ReadingWebView.E → ReadingWebView.t → helper t.e → setWebViewClient`, then traverses actual API overrides. These are the facts supplying the two gold matches; the report does not show an original setter argument before wrapping.

V8 has no such wrapper fact for this host. It also loses the Chrome wrapper install on that same factory WebView; it retains the factory object's constructor-chain DownloadListener installation and different callback installation on the separate preloaded `d` WebView. This is an installation-context difference, not simply removal of two members from an existing fact.

The exact two method signatures remain in v8 reports for five other hosts: AdLandingActivity, AdSubmitDialogActivity, NewAdLandingActivity, NewAdSubmitDialogActivity and ExternalWebActivity. The same concrete implementation still passes the same setter contract, so a global SDK signature-family or contract-filtering regression is ruled out for these methods.

Both target traversals remain unfinished in phase 1. V7 processes 1,511 contexts with 593 pending; v8 processes 1,280 with 549 pending. Neither discards contexts due to the host cap. Lower v8 coverage makes not-yet-reached installation contexts a real possibility. The bounded pending samples do not identify the missing setter context, so they cannot prove it remains queued.

## A second mechanism reproduced without private signatures

`NullClientOverrideDiagnosticFixture` contains a generic WebView subclass whose setter checks its Client argument: null forwards a reset to the framework; non-null allocates and installs a wrapper. That wrapper declares exactly the public Android page-finished and modern request-intercept contracts. Two different WebViews exercise null and non-null paths.

An independent javac compilation and execution against the frozen jars produce:

| Behavior | Frozen v7 | Frozen v8 |
| --- | --- | --- |
| Null-reset WebView falsely acquires wrapper callbacks | Yes | No |
| Non-null WebView retains wrapper callbacks | Yes | Yes |
| Refined summaries in fixture | 0 | 1 |

Logs are `test/runs/generic-v8-fanqie-nullable-v7.log` and `test/runs/generic-v8-fanqie-nullable-v8.log`. V8's ordinary-IF refinement can therefore correctly remove this pair of callbacks when older matching evidence came from a null reset. The target v7 path's destroy entry makes this mechanism plausible, but its actual original argument is absent from the report. Neither the helper name nor allocation offset proves that argument was null, and this investigation does not make that claim.

## Subsequent independent source proof

`webviewactivity-wrapper-install-vs-cleanup-source-proof.json` supplied by the source agent confirms the real positive path: WebViewActivity.D2 calls the lazy layout factory; the constructed SwipeRefreshWebView obtains its actual WebView from the placeholder and Q installs a non-null Client; ReadingWebView.setWebViewClient constructs `m`, captures the delegate and forwards to the native installation. The fresh placeholder fallback creates ReadingWebView through NsCommonDependImpl.

The same proof confirms cleanup mode 3 passes null and ReadingWebView's null branch forwards `super(null)` and returns without constructing `m`. Therefore the specific v7 report matches shown above arose through an infeasible cleanup-wrapper branch. V8's removal of that reported chain is correct precision behavior, directly reproduced by the generic fixture. It does **not** establish that the real positive installation was recovered: v8 still fails to report the wrapper members reached through non-null initialization on the target host.

## Decision

Keep both canonical gold positives. Their real initialization path is independently established; the old match merely coincided with the wrong cleanup path. Global SDK contract rejection is disproven, and restoring the old unrefined null-reset branch would restore false evidence rather than fix the true initial installation.

The true non-null initialization loss remains unresolved from the retained reports: both traversals stop in phase 1, the v8 target processes fewer contexts, and bounded pending samples cannot show whether the actual factory-field/Q/setter context remains queued. Do not equate the v7 matched count with a correctly traced initialization or remove the gold because its old matching fact was false.

The next measurement is frozen v9: compare target progress and actual non-null installation evidence after parallel analysis. If the real installation is still missing with a completed target traversal, collect tool-level actual placeholder-return/field/setter arguments before selecting a generic fix. The diagnostic fixture is standalone and does not modify the frozen v9 SelfTest or production files.
