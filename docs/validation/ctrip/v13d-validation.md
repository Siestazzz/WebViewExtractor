# v13d Ctrip source delta and public-30 replay

v13d completed 410/410 manifest Activities in 185.61 seconds and emitted the same 55 hosts as v13c. The APK hash is unchanged. This audit treats report differences as observations and source as the ownership/capability authority.

## Strict delta

Thirty-two Activity records differ; 22 have semantic tuple removals and ten only order/representation differences. There are no additions and 3,235 removed tuple instances: 2,599 bridge, 279 bridge-removal, 200 WebView-operation, 134 setting, and 23 callback instances. Unattributed and both index/manifest diagnostics are exactly equal.

Most removals are duplicate receiver/site paths. After reducing each host to capability identity (bridge registration/type/members, setting API/value, callback API/type/members, or operation API/name), 21 of the 22 affected hosts retain the same capability set. `EvaluateDialogActivity` is the sole host with reduced capability identities removed.

## Source assessment of the XML-seed change

`EvaluateDialogActivity.onCreate` binds the layout `H5WebView`, calls `H(activity,url,new SelfViewClient)` and then directly calls `H5WebView.loadUrl(String)` (source lines 202, 240, and 248). `H` calls `E`, which obtains the concrete plugin list, calls `D`, and installs its WebViewClient (H5WebView lines 235–247; E/D lines 177–220). These core registrations remain represented sufficiently for all canonical bridge and callback facts to score, although many report paths disappeared.

The direct `loadUrl(String)` has an actual override chain that v13d no longer fully reports:

1. `H5WebView.loadUrl(String)` calls `loadUrl(str,null)` (lines 458–465).
2. Dispatch reaches inherited `VideoEnabledWebView.loadUrl(String,Map)`, which calls private `c()` and then `super.loadUrl` (lines 151–159).
3. `c()` registers `_VideoEnabledWebView` before the platform call (lines 77–85).

Therefore the lost H5 override operation, inherited VideoEnabled operation, and `_VideoEnabledWebView` bridge are true v13d false negatives. The removed `loadData*`, helper `k/v`, and generic `setWebChromeClient` entry-parameter facts are methods made reachable only by the old “seed every XML view method” behavior; this Activity does not call those paths. The large union bridge record is also not an additional runtime registration beyond the concrete plugin registrations.

## Four public object samples

`H5Container`, `MyCtripOrderModalActivity`, and `CMBApiEntryActivity` are record-for-record unchanged. `MktH5ContainerV2` falls from 556 to 346 facts, but its reduced bridge/setting/callback capability set remains intact. Its source object conclusion is unchanged: cached and fresh acquisition are runtime alternatives and `A.x=A` is a self-alias. v13d does not resolve exact allocation identity.

## Public 30-host replay

The current 4,394-row canonical oracle, including the 196 public-host expansion and corrected Kwai string literal, was replayed against v13d:

- settings: 323/323
- bridges and bridge members: 3,680/3,680
- callbacks: 391/391
- positive hosts: 30/30
- source type constraints: 4,394/4,394

The scorer still marks acceptance `unproven` because type constraints do not prove exact allocation identity. The perfect three-category recall does not erase the source-confirmed operation/auxiliary bridge regression above; those facts are outside the current scored three-category denominator or remain covered by another equivalent report record.

`v13d-delta.json` contains every changed tuple. `v13d-ownership.jsonl` replays source-reviewed ownership for all 55 unchanged hosts. `v13d-public30-scoring.json` is the machine replay output.
