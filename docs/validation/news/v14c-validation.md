# Tencent News v14c source validation

Scope: `com.tencent.news.apk` 7.9.50, SHA-256 `c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`. This is a development-selected, non-blind review. Sealed holdout material was not opened.

## Ownership

v14c emits the same 42 Activities as v13e. Source ownership remains 41 valid and one uncertain (`ShellActivity`), so the conservative non-valid upper bound is 1/42 (2.38%). There are no newly added or removed hosts. The full row-level result is in `v14c-ownership.jsonl`; unchanged rows reuse their previously cited source chains, while all six changed rows cite the v14c delta.

The report contains 5,386 facts. Relative to v13e, the normalized semantic comparison finds 84 additions and no removals across six Activities: SecurityTicket +3, PrivacyWeb +5, PrivacyWebItem +6, NewsDetail +33, PushDetail +33, and Support +4. Evidence-only fields are excluded from this comparison. The complete facts are retained in `v14c-delta.json`.

## Source regression findings

The Privacy recovery is real on its known path. Both Activities obtain resource `2131303140` from their own layout as the same concrete `X5WrapperWebView` allocation. Their parent-declared `smtt.WebView.loadUrl(String)` calls dispatch to `X5WrapperWebView.loadUrl(String)`, whose `invoke-super` reaches the closest implementation `DtX5WebView.loadUrl(String)`. That implementation calls `injectBridge()`. Conditional injection registers:

- `DTJsBridgeInterface` → `BridgeInterface`, exposing only `bridgeCall(String): String`.
- `dtBridge` → `JsBridgeInterfaceV2`, exposing only `postMessage(String): String`.

The registration requires `JsBinderHelper.allowInjectOnLoad()` and the corresponding per-instance injection flag to be false. Public `dispatchVisibilityChange(boolean)` has no JavaScript annotation and is excluded. The four scoring registrations and four exposed-method facts for the two Activities are in `privacy-v14c-bridge-supplement-facts.jsonl`. Registration rows use `normalized_signature=null` and put the add-interface API in `normalized_api`; method rows use the exact exposed DEX member signature. Every row carries the concrete source WebView type, SHA, version, reviewer, receiver identity, and conditional binding chain. The older override-edge evidence remains separate and is not placed in a scoring capability kind.

SecurityTicket's three additions follow the same conditional inherited `X5WrapperWebView → DtX5WebView.loadUrl → injectBridge` behavior on its Activity field. Its direct source `initView()` loads the URL on that concrete field, so the host and base capability are valid; PatchRedirector alternatives remain unknown.

Support's four additions are source-confirmed late-consumer replay. `SupportActivity.initView()` calls `setJavaScriptEnabled(true)`, `setDomStorageEnabled(true)`, and dynamic `setUserAgentString(...)` through field `f109620`; later methods call `loadUrl(...)` through that same field. v14c binds them to resource `2131303125`, concrete `BaseWebView`. These are one real XML object, not four new objects.

NewsDetail and PushDetail remain real WebView hosts, but their 33 additions per host are not 33 independently established capabilities. Each group consists of 11 v1 bridge registrations, 11 v2 registrations, and 11 `loadData` operations. The receiver IDs are return sites such as `LruCache.remove`, `LinkedList.removeFirst`, and several `IPatchRedirector.redirect` overloads, split again across `BaseWebView` and `NewsWebView`. Source confirms the underlying DtWebView instrumentation can inject those bridges when the corresponding real receiver loads data. It does not establish those collection/redirect return signatures as distinct WebView allocations. These rows therefore remain a mixed alias expansion and should be deduplicated by concrete receiver identity before capability counting.

The Fragment root/child binding change creates no new News host in this report. No ownership decision relies only on a Fragment type or a shared resource ID.

## Remaining limits

The known parent-typed/unknown replay candidates and PatchRedirector branches remain. A concrete XML alternative is enough to validate the conditional Privacy path, but it does not make every union alternative valid. `ShellActivity` remains uncertain for the reasons recorded in earlier source audits. This review validates News v14c only and does not generalize probe results to the other APKs.
