# Ctrip oracle summary

`scope_complete=false`. Shared H5 capability groups have been expanded deeply, but all per-Activity exclusive capabilities and the 151 JADX-missing entries are not closed. The oracle must not yet be used to claim 95% whole-app recall.

## Sample identity

- Package: `ctrip.android.view`
- Version: `8.78.0`
- APK SHA-256: `cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66`
- Decompiled source: `/HDD/d3008/WebViewBench/WebViewBench-predecompiled/ctrip-1`
- Manifest Activity entries: 413

## Inventory result

The current inventory classifies 33 entries positive, 211 source-scoped negative, 159 uncertain, and 10 withheld positives. Eight original holdout identities appeared in development ownership audits and were retired to uncertain; eight source-reviewed replacements were selected without publishing their facts. The 33 public positives cover all requested binding modes: direct (6), inherited (6), fragment (8), wrapper (4), factory (2), manager (2), third-party (3), and message bridge (2). These counts are derived from `activities.jsonl`; the exact machine-readable totals are in `inventory-counts.json`.

The strongest shared chains are the two Ctrip H5 containers through their H5 fragments and custom H5WebView classes; inherited container subclasses; Flight/MyCtrip/calendar/finance Activity-to-fragment bindings; direct Activity-owned WebViews; wrapped search/chat/evaluation WebViews; SDK WebViews; and UnionPay/CMB message bridges. The public set exceeds the requested 30 positive Activities without consuming the ten holdouts.

## Detailed reliable facts

`facts.jsonl` records the public Activity bindings plus exact settings for Ctrip H5WebView v1/v2, CMB, QMP Auth, and UnionPay. It records three complete reviewed JavaScript interfaces:

- `CMBSDK` implemented by `cmbapi.h`, with all six `@JavascriptInterface` methods found in that class.
- `handle` implemented by `com.qmp.sdk.ui.activity.AuthActivity`, with both annotated methods.
- `_WebViewJavascriptBridge` implemented by `com.unionpay.WebViewJavascriptBridge`, with its annotated five-string message entry point.

The file also records complete callback signatures for the reviewed CMB WebViewClient, QMP callback implementation, UnionPay WebViewClient/WebChromeClient, and the Ctrip H5 v2 WebViewClient. Overloaded `onReceivedError` methods remain distinct facts.

`groups.jsonl` now resolves the main dynamic registries. The unconditional H5 v1 registry contains 34 bridges with 222 exposed annotated methods; H5 v2 contains 35 with 228 methods. The additional Samsung Wallet bridge in each generation is modeled separately because registration requires an H5Fragment instance. All 69 unconditional core plugin TAG values resolve from the plugin class or an available source ancestor. The two WebViewClient implementations each have 10 visible overrides. The v1 and v2 WebChromeClient chains each have 11 visible overrides after inherited Ctrip video-client methods are included. Conditional payment, liveness, destination, finance, and Samsung Wallet groups retain their concrete types, TAG values, exposed methods, and registration evidence without being fanned out as required facts for every H5 Activity. RN resolves both names to the same `postMessage(String)` bridge object; Flutter records its runtime JavaScript channels and the fixed `Business` bridge.

Machine comparison uses DEX-style `normalized_signature` for bridge methods and callbacks and `normalized_api` for settings. `activity_binding` remains host evidence and is excluded from capability recall.

The corrected canonical export contains 4,330 shared-group and direct reviewed rows. All 3,532 method-bearing rows were independently checked against the APK DEX method table; every owner/name/parameter/return tuple exists. The reduction removes an erroneous full-H5 group link from a plain CtripWebView map Activity and excludes fragment-only Chrome callbacks from a direct H5WebView host; QMP now records the nine installed helper-client overrides instead of three Activity delegate methods. Settings use the actual `setXXX` method extracted from `normalized_api`. Literal and enum values remain strict comparison values; dynamic Java expressions have a null comparison value and preserve their text in `source_expression`. This guards against treating runtime expressions as constants.

The v1 ownership audit reviewed all 52 reported Activities from `test/runs/v1/ctrip.android.view/0/output/capabilities.json` as candidates. Source evidence supports 44, rejects 1, and leaves 7 hotel cases uncertain. `TencentEntryActivity` is conditionally valid because its QQ share path can construct `TDialog`. `CtripCommonFeedBackActivity` is the one confirmed wrong owner: its newly created gallery items leave `bottomWebViewUrl` null, so the gallery helper skips H5WebView construction. The seven hotel subclasses inherit shared pop-layer infrastructure, but no concrete invocation is source-visible; they remain uncertain rather than being rejected from absence alone.

The v3 ownership audit covers all 55 emitted Activities without using holdouts: 47 valid, 1 wrong, and 7 uncertain, for a conservative wrong-or-uncertain upper bound of 8/55 (14.5%). Four newly emitted Activities have fresh source chains; the other decisions reuse the earlier source audit rather than accepting v3 capabilities as truth.

Notable independently verified settings include JavaScript enabled in all reviewed WebViews; DOM storage enabled in Ctrip v1/v2, QMP, and UnionPay; Ctrip v1/v2 allowing file access and file-URL cross-origin access; and Ctrip v1/v2 mixed-content mode 0. These are source facts, not safety judgments.

## Evidence and confidence

High strength means a concrete source statement such as construction, client installation, bridge registration, setting call, fragment field/creation, or explicit inheritance from a reviewed WebView container. Medium strength means the chain is visible but one implementation edge is missing or JADX did not emit the target source. Every public positive carries its source path, line, and text. Holdouts carry no evidence or capability details by design.

## Missing and unresolved

- JADX did not emit matching Java for 151 manifest Activity entries. They remain uncertain rather than being guessed negative.
- The core v1/v2 registries, TAG values, annotated methods, and visible client overrides are expanded. Runtime plugin membership remains conditional on provider initialization, URL gating, Bus availability, and successful reflective TAG reads.
- Payment, liveness, destination, and finance Bus plugins are modeled as conditional groups. Their registration may return null or be skipped when parameters or optional modules are absent.
- Some source methods contain JADX failure stubs. Facts are included only when the relevant statement is present outside a failed body.
- Source-scoped negatives have not been proven against reflection, native code, dynamically loaded bundles, or server-delivered mini-app code.
- One public wrapper (`ScanPayH5Container`) has a manifest entry but no emitted Java source; its medium-strength status should be revisited before treating it as a strict regression oracle.
- Runtime Flutter JavaScript channel names remain unknown because application code supplies them dynamically. No value is inferred from a variable or class name.
- Activity-to-group links cover source-visible H5/RN/Flutter host chains among the current public positives. Per-Activity local settings, clients, and bridges outside those shared groups still require closure before `scope_complete` can become true.

## Holdout protocol

Ten positive candidates are marked only as `holdout` in `activities.jsonl`. Their mode, facts, and evidence are intentionally absent from all other files. They should be decoded only after extractor fixes are frozen, then compared once as a final check. This prevents iterative implementation from fitting to those samples.

The holdout set was rotated after eight original identities appeared in the v1 ownership audit. Two identities that never appeared outside the withheld inventory were retained, and eight new source-reviewed candidates replaced the leaked entries. `scripts/validation/rotate_ctrip_holdouts.py` makes this selection deterministic without publishing the withheld capability facts.
