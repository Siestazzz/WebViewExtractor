# NFT nonblind development facts

This development-only set adds three independently verified host facts absent from the current canonical set. It is intentionally separate from sealed data and does not remove or rewrite any failed case.

| Host | Implementation family | Scored capability facts | Separate operation facts |
|---|---|---:|---|
| `AntiqueDetailActivity` | `com.mgsz.hunantv.nft` | 18 | 1 `loadUrl` |
| `DigitalDetailActivity` | `com.hunantv.nft` | 18 | 1 `loadUrl` |
| `DigitalModelViewActivity` | `com.hunantv.nft` | 18 | 1 `loadUrl` |

All rows use schema version 1, app version `9.3.0`, validator `GPT-5.6 Sol independent source+DEX validation`, APK SHA-256 `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5`, normalized DEX signatures, explicit `value_kind`, precise source locations, WebView field identity, and host-specific binding chains.

The generated schema follows the canonical distinction between `callback_registration` and `callback`: `setWebViewClient` is only the registration row, with the platform setter in `normalized_api`, the instantiated client in `implementation`, and the layout class in `callsite_owner`; `shouldOverrideUrlLoading` is the callable callback member. Settings use `literal` or `enum`, while the operation URL is `dynamic`. `raw_value_kind` preserves the pre-migration classification. `nft-development-schema-migration.json` records the complete 57-to-57 migration.

The stricter constructor audit disproved the earlier claim that the complete surface was only `loadUrl`. Each `MgNftViewer` inflates a viewer layout containing its `NftWebviewLayout` even before the Webview enum variant is selected. Android layout inflation invokes the three-argument view constructor, which unconditionally calls `initialize` and `initWebview`. Therefore each host has 16 settings, one `setWebViewClient` registration, and the registered client's `shouldOverrideUrlLoading(WebView, WebResourceRequest)` member. There is no `addJavascriptInterface` or other bridge registration in either implementation.

The 16 settings are: default encoding `UTF-8`; support zoom, built-in zoom, overview mode, wide viewport, multiple windows, JavaScript, DOM storage, and automatic image loading enabled; display zoom controls, file access, save password, and media playback without gesture disabled; plugin state `ON`; cache mode `-1`; mixed-content mode `0`. JavaScript is invoked through `com.mgtv.aop.bb.U.a(WebSettings, true)` and remains a real attempted setting even though the caller catches exceptions.

The three original dynamic `loadUrl(String)` rows are retained with expanded metadata and classified as `host-operation-recall`, outside the bridge/setting/callback capability scores. Shared implementation evidence is reused only after the distinct activity-to-viewer chain was checked for each host.
