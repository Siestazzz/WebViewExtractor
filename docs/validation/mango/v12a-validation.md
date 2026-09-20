# Mango v12a validation

v12a retains all 89 v11c hosts. Ownership remains 84 valid and 5 uncertain. NFT hosts and the six previously reviewed room/lite/player hosts are unchanged. Only `ErlangLiveActivity` and `MGVideoPlayActivity` change, and the two reductions have different correctness outcomes.

## Erlang: correct instance pruning

`ErlangLiveActivity` loses 20 `message_bridge` rows, one registration set attached to the `NestedImgoWebView` attributed through `ErlangSingClashH5Fragment`.

That fragment creates a `WebUIFragment` and sets URL, screen, lifecycle, transparency, and navigation arguments. It does not set `RouterConfig.INTENT_WEB_USE_NESTED`. `WebUIFragment.initWebView` reads that key with default `false`; it constructs `NestedImgoWebView` only when true and otherwise constructs `ImgoWebView`. Therefore the removed nested alternative is infeasible for this fragment. The same 20 `ImgoWebJavascriptImpl` registrations remain on the actual `ImgoWebView`. This is a valid benefit of instance-sensitive propagation.

## MGVideoPlayActivity: real regression

`MGVideoPlayActivity` loses all 64 facts attributed to Diana MiniApp WebViews: 35 settings, 8 operations, 8 callbacks, 10 bridge removals, and 3 bridge registrations. The source proves this capability is reachable:

1. The activity's player-event branch recognizes a `miniapp` jump and calls `y9().showMiniApp(url)` when `DianaEntrance.isOpen`.
2. `y9()` returns the activity's cached `NBFloatFragmentHelper` instance.
3. The AspectJ-wrapped `showMiniApp` reaches synthetic body `F1(helper,String,joinPoint)`.
4. `F1` creates `MiniAppFragment.newInstance(bundle)` and calls `helper.n1(newInstance)`.
5. `n1(Fragment)` writes that exact argument to `this.f88165h`.
6. `O0()` reads the same field, tests `instanceof MiniAppFragment`, and calls `MiniAppFragment.onBackPressed()`.
7. The MiniApp fragment lifecycle reaches `DianaView`, the page/game WebView and the service JavaScript-engine WebView.

The removed GamePage `PageWebView` is a real conditional object. The removed `ServiceWebView` still had a weak `constructor_parameter` suffix in v11c, but its construction in `AppService.createJsEngine` is real; weak identity presentation does not make the entire capability surface unreachable.

The earliest lost relationship is the write from `showMiniApp/F1 -> n1(Fragment) -> this.f88165h` to the read of that field on the same helper instance in `O0`. The synthetic AspectJ closure between public `showMiniApp` and `F1` is the likely summary boundary. The new setter analysis should retain this write because it occurs at an actual host-reachable callsite and does not require a global seed.

## Stable areas

All five NFT hosts retain their v11c counts and object identities, including the 48 development settings and six required callback registration/member facts. The four vod-game variants, `LiteVodActivity`, and `VodPlayerPageActivity` also retain their v11c counts and previously reviewed Diana object relationships.

The overall raw count falls from 20,666 to 20,582. Twenty removals are correct alias pruning; 64 are a source-confirmed attribution regression. `v12a-validation.json` contains the exact counts and binding chains. `audit_v12a.py` regenerates it and the complete ownership JSONL without reading sealed data.
