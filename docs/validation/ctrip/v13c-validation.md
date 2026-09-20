# v13c Ctrip intermediate validation

v13c completed 410/410 manifest Activities in 203.86 seconds and emitted the same 55-host set as v12j. This is an intermediate/failed candidate, not final acceptance.

Compared with v12j, 46 Activity records differ, but only three hosts have semantic changes: 14 additions and no removals. `unattributed`, index diagnostics, and manifest diagnostics are exactly equal. The bridge, setting, and bridge-method sets are unchanged.

The additions are source-real conditional paths:

- `QRScanActivity` constructs `QRScanFragment`; its external-URL path constructs `ctrip.android.qrcode.fragment.QRScanDialogFragment`, whose `readTitle()` installs WebChromeClient and WebViewClient and loads the scanned URL (source lines 198–200). v13c emits each site twice due to distinct propagated identities; source proves the sites, not duplicate runtime registration.
- `QRScanHistoryActivity` constructs `QRScanHistoryFragment`; its URL handling reaches the same QR dialog family and adds one instance of each client installation and `loadUrl` site.
- `GalleryDetailActivity.showJumpDialog()` constructs `ctrip.base.ui.gallery.util.QRScanDialogFragment`; its `readTitle()` installs both clients and calls `loadUrl` (lines 130–132). Its share-dialog path also reaches `CTShareDefaultPromoView`, which allocates/configures a WebView and calls `loadDataWithBaseURL` at line 169. The reported multiplicity two is an analysis identity count, not proof of two allocations.

No v12i Hotel map regression is repaired: the two source-real `CGoogleMapView.executeJSAction -> mGoogleWebView.loadUrl` facts remain absent. The four public object samples are semantically unchanged, so their prior allocation/alias conclusions continue to apply.

`v13c-ownership.jsonl` has one source-provenance row for each of the unchanged 55 hosts. `v13c-delta.json` records all strict and semantic differences.
