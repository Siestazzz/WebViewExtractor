# v16d Ctrip validation

v16d is a **budget-truncated diagnostic run**, not a quality acceptance run. The report status is `partial`; it consumed 590.009 of its 590-second worker budget and processed 396 of 410 manifest Activities. It emitted 51 hosts. The missing 14 manifest entries are not identified in the report, so absence of an emitted host cannot be interpreted as a semantic deletion.

## Delta from v16a

Among hosts that v16d did emit, only two have semantic tuple changes under the same normalization used for this audit:

- `GSAllMapActivity` gains 13 map-WebView tuples.
- `TouristMapActivity` gains 29 tuples and loses one old `loadUrl` identity tuple.

These changes expose an already source-valid Google map path. `GSAllMapActivity` lazily constructs `CtripUnitedMapView` (`GSAllMapActivity.java:1778-1843`) and returns that same object from `getMapView()` (`:2199-2205`). `TouristMapParentActivity.initMap()` constructs and stores its `CtripUnitedMapView` (`TouristMapParentActivity.java:2144,2238,2278`; field at `:468`). `CtripUnitedMapView.initMapViewWithMapType` selects/initializes `CGoogleMapView`, whose `initWebView()` configures its internal native WebView. Thus the settings, bridge, clients, and load are real capabilities of the map object. The multiple `entry_parameter` and full-field-path IDs in v16d are alternative abstract identities for this path; they do not prove multiple runtime WebView allocations. The one TouristMap removal is an identity rewrite from an Activity-prefixed field path to entry/full-path aliases, not a source capability removal.

`TencentEntryActivity` is the only previously emitted host absent in v16d. It remains a source-valid conditional host via `TencentEntryActivity -> Tencent.shareToQQ -> QQShare.shareToQQ -> new TDialog`. Because v16d stopped after 396/410 Activities and does not publish the processed-name set, its disappearance is classified `budget_truncated_unassessable`, not a regression or precision gain.

The machine delta reports 42 additions and 56 removals when abstract WebView identity is included in the key. Fifty-five of the removals belong to the unassessable Tencent host. The processed-host delta is therefore 42 additions and one identity replacement removal. This distinction is recorded in `v16d-delta.json`.

## Ownership

All 51 emitted hosts were mapped to the prior independent source decisions: 50 valid and one wrong (`CtripCommonFeedBackActivity`, whose empty gallery URL makes the WebView branch infeasible). `v16d-ownership.jsonl` contains one row for every emitted host. This establishes Activity ownership only; the map alias duplication above remains an object-precision limitation.

## Current canonical replay

Canonical SHA-256 `a4e3f06b04b7fd9f9afac229ef3bda47341d5c853e2842f8de80bfaf11f355a9` contains 4,944 raw rows. v16d matches:

- settings: 323/391;
- bridges and bridge members: 3,682/3,970;
- callbacks: 391/444;
- WebView operations: 2/6;
- positive hosts: 30/33.

All 413 missing facts are concentrated in the three source-confirmed hosts already absent in v16a: `QQSSOEntryActivity` (59), `QQEntryActivity` (59), and `CTTourSearchActivity2` (295). The v16d budget cutoff adds no newly measurable canonical miss, but incomplete manifest coverage prevents acceptance. The report also contains extensive per-method `flow_budget`, `receiver_relevance_budget`, and `transport_discovery_budget` diagnostics.

Artifacts: `v16d-delta.json`, `v16d-ownership.jsonl`, and `v16d-current-canonical-scoring.json`. No extractor output was used to establish source truth; it was used only to enumerate the run delta being audited.
