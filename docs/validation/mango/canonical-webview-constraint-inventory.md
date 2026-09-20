# Mango canonical WebView type-constraint candidate

This migration is source-authored and does not read extractor reports to infer allowed types. It translates all 30 existing canonical `webview` labels into concrete runtime WebView types using the independent host/framework audits, decompiled construction/XML chains, and DEX class hierarchy. It deliberately writes `canonical-facts-webview-constrained.candidate.jsonl`; the current canonical is not overwritten.

The candidate contains 2,508 rows. The existing 81 MG/Diana rows retain their constraints, and 2,427 rows receive a new constraint. All 30 labels have a source-supported rule; no label is silently treated as its own type merely because of its text. Two consecutive runs produced candidate SHA-256 `90a30e3d26c3d17642ab192b7a65c07365dd3665172071f219c86ab7be6b908f` from canonical SHA-256 `b22404aed295f50a3e14251da61e6e55b5068281833b13e289e375e35e39f8b2`.

The important label translations are:

| Non-WebView label | Constrained runtime type | Source basis |
|---|---|---|
| `RootWebChromeClient`, `RootWebViewClient` | `com.hunantv.imgo.h5.ImgoWebView` | These clients are installed on the audited ImgoWebView instance. |
| `com.hunantv.imgo.h5.jsbridge.BridgeWebView` | `com.hunantv.imgo.h5.ImgoWebView` | Canonical hosts instantiate the concrete ImgoWebView subclass. |
| Main-app WebView/Capture/WebUI fragments | `com.hunantv.imgo.h5.ImgoWebView` | Fragment fields and constructors; WebUI's nested flag is false/default. |
| `XWebViewFragment` | `com.hunantv.imgo.xweb.web.XWebView` | Fragment-owned concrete XWebView. |
| `com.mgsz.h5.WebViewFragment` | `com.mgsz.h5.ImgoWebView` | XML/field chain to the concrete mgsz view. |
| NFT ViewBinding field labels | `android.webkit.WebView` | `findChildViewById`, cast, Binding constructor, and stored field. |
| `FragmentWebLoadingBase` | `com.platform.oms.ui.widget.TimeoutCheckWebView` | Field declaration and `new TimeoutCheckWebView` at source line 174. |
| Alipay H5Pay container | `com.hunantv.imgo.h5.browser.MoWebView` | `com.alipay.sdk.widget.h` line 22 constructs MoWebView. |
| OPOS activity/component/wrapper labels | `com.hunantv.imgo.h5.browser.MoWebView` | Audited OPOS component/factory implementation constructs MoWebView. |
| UBiX wrapper label | `com.ubix.ssp.ad.e.c0.c` | Activity field type and `new c(Activity)`; `c` directly extends WebView. |

Concrete labels such as `SSWebView`, `ProgressWebView`, `PaySafeWebView`, PageWebView, ServiceWebView, mglive BridgeWebView, ImgoAdWebView, and the Land BaseWebView map to their exact class. Platform `android.webkit.WebView` remains exact rather than permitting arbitrary subclasses because its three audited hosts use the platform class at the relevant construction/XML site.

`webview_constraint` is a type filter. It does not state that two facts share one runtime object, establish allocation identity, or validate an extractor's object id. Those remain source/data-flow questions. The migration keeps the original `webview` label and adds `webview_constraint_evidence` so reviewers can distinguish presentation labels from runtime type constraints.

Files:

- `canonical-webview-type-map.json`: reusable label rules and evidence basis.
- `build_canonical_webview_constraints.py`: idempotent candidate generator; it refuses to overwrite canonical by design.
- `canonical-webview-constraint-migration.json`: counts, hashes, unresolved labels, and overwrite status.
- `canonical-facts-webview-constrained.candidate.jsonl`: review candidate.

## v2 correction: Pangle SSWebView

The v1 mapping incorrectly treated `com.bytedance.sdk.component.widget.SSWebView` as the API receiver. Source proves it is a `SkinnableFrameLayout`, not a WebView. It declares `vw:android.webkit.WebView`, constructs `new com.hunantv.imgo.h5.browser.MoWebView(...)` in its constructor, adds that child to the FrameLayout, returns it from `getWebView()`, and delegates settings/client/bridge calls to `vw`.

The v2 rule therefore constrains all 159 SSWebView-labeled canonical facts to `com.hunantv.imgo.h5.browser.MoWebView`. The failed v1 map, 2,508-row candidate, and migration audit are retained under `*-v1.failed` names. The current v2 candidate SHA-256 is `c44085000cacc9a9f07dfc4ecce2a7b5d21f3bec674d62328fc575ce39cd9c23`.

The other wrapper mappings were rechecked by allocation rather than label inheritance:

- Alipay widget `h` constructs `new MoWebView(activity)` at source line 22.
- OPOS BaseWebActivity component implementation constructs `new MoWebView(context)` at `com/opos/cmn/biz/web/b/b/b/b.java:316`; the module UI factory delegates to that component.
- Loading fragment constructs `new TimeoutCheckWebView`, whose class extends `MoWebView`; the exact receiver remains TimeoutCheckWebView.
- UBiX Activity declares field `c webView`, executes `new c(Activity)`, and class `com.ubix.ssp.ad.e.c0.c` directly extends platform WebView.
- NFT Binding labels store an XML child cast to platform `android.webkit.WebView`; they are not constrained to their Layout/Binding owner.

No additional type was admitted based on a tool report. The v2 generator operates on whatever canonical snapshot is present but still writes only the candidate path; shared-workspace canonical changes made by another process are recorded through the source hash and are not attributed to this script.

## v3 host/fact selectors (v12j audit)

The label-only v2 map still conflated declared field/host labels with the runtime receiver in two cases. The v2 map, candidate, and migration audit are preserved with `.v2.failed` names.

- Seven `popup.WebViewClient` callback facts labelled `ImgoWebView` execute on a popup child allocated by `ImgoWebView$3.onCreateWindow`: `new MoWebView(...)`, followed by `moWebView.setWebViewClient(new ImgoWebView$3$1)`. v3 constrains these callback facts to `MoWebView`.
- Eight CCB facts labelled `android.webkit.WebView` use field `F: WebView`, but `H5PayActivity.f()` assigns `new MoWebView(this)` to `F`; `g()` calls settings, clients, and `addJavascriptInterface` on that field. v3 constrains this host/label combination to `MoWebView`.
- The other ten `android.webkit.WebView` rows are genuine platform receivers: four BackDoor rows come from the `<WebView>` tag in `activity_backdoor_web.xml`, and six Wechat WAP rows come from `new WebView(this)` in `WechatWapPayRouterActivity.onInitializeUI()`.

The v3 candidate has 2,508 rows, SHA-256 `8dc251199b3790f265d6b8a1ea7f897cfaa5bc6d54e14d46d110fbd1f5f9776d`. Two selectors match 16 rows. A double replay produced the same hash. The script does not overwrite canonical facts.
