# Tencent News independent WebView inventory

Scope: JADX source under `test/decompiled/com.tencent.news`, generated directly from the APK. Extractor output was not read. The helper scans all 57,927 emitted Java source files and resolves manifest Activity inheritance where unambiguous. JADX's progress denominator was 45,695 processing units.

## Layers

- Direct Activity: `CustomWebBrowserForItemActivity`, `WebDetailActivity`, `SecurityTicketActivity`, `SupportActivity`, `PrivacyAbsWebActivity`, and `BaseEditorActivity` contain explicit settings, client, or interface calls.
- Inherited Activity: `PrivacyWebActivity`/`PrivacyWebItemActivity`, `QAEditorActivity`/`RichEditorActivity`, and `TencentVideoWebActivity`/`HalfTencentVideoWebActivity` inherit bindings. The latter has a two-hop chain.
- Fragment/page: `VisitModeNewsDetailFragment`, `QADetailPage`, `CaptureDetailPage`, and `AudioPlayerWebViewContainer` are WebView-bearing page components. Their Activity ownership needs a separate construction-chain proof; an unresolved path is not counted negative.
- Wrapper: `WebViewBridge` dispatches between Android and X5 WebViews; `BaseWebView`, `BaseSysWebView`, `X5WrapperWebView`, and `RecyclableWebView` centralize configuration or lifecycle.
- Factory: `PageGeneratorFactory` and web-detail page generators construct page implementations. Activity binding is retained as unknown until constructor use is followed.
- Manager/controller: web-detail `viewmanager` classes and ad `WebViewController` classes connect Activities to page/WebView objects. Obfuscated manager edges are marked unknown when the recovered type is insufficient.
- Third-party: `com.tencent.open.yyb.AppbarActivity` has a direct WebView signal; Tencent Midas proxy/plugin Activity code mentions WebView but plugin resolution is incomplete. Neither is promoted to an app Activity fact without a manifest/ownership proof.
- Message bridge: `BridgeInterface` registers `TencentNewsJsBridge` and exposes `public String bridgeCall(String str)`; `WebShareJsBridge` exposes `public final void webShareContentCallBack(@Nullable String description)`. `YuanBaoJsBridge`, `WxOfficialAccountArticleJsApi`, and `YspJSBridge` provide additional annotated surfaces.

## Exhaustiveness and shortfall

The first full manifest-to-inheritance scan found 13 manifest Activities with explicit direct or inherited WebView configuration signals. Reverse tracing of registrations, subclasses, fragments, factories, and managers verified five additional Activity owners. This is below 30, so the corpus does not fabricate 30 positives. The original 13 were already disclosed in the first batch; the five newly verified Activity bindings are retained as holdout. The requested ten-Activity holdout is therefore short by five Activities.

## Unknowns

JADX completed with 164 errors and exit code 1. Any failed or ambiguous class, unresolved obfuscated parent, reflective factory edge, or plugin-loaded implementation is unknown rather than negative. `activity_candidates.tsv` is an audit trail, not ground truth.

`entrypoint_inventory.jsonl` contains the source-wide call, annotated-method, and client-class inventory. `unowned_entrypoints.jsonl` contains every entry not assigned to a published capability group, including SDK/framework entries and app entries whose Activity owner is unresolved.
