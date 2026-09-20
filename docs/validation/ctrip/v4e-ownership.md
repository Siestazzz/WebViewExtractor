# v4e ownership and bridge regression note

v4e emits 55 Activities, but its set differs from v4 by three additions and three removals. The 52 shared Activities retain their source-reviewed v4 verdicts. The three additions are source-confirmed positive hosts: `UserAgreementActivity` directly loads a layout WebView; `GSAllMapActivity` constructs `CtripUnitedMapView`; and `TouristMapActivity` inherits `TouristMapParentActivity.initMap`, which constructs and stores `CtripUnitedMapView`. The removed Activities are `HotelDetailCharityProjectActivity`, `HotelInquireActivity`, and `HotelVideoActivity`.

The resulting emitted-set audit is **54 valid, 1 wrong, 0 uncertain**. The sole wrong ownership remains `CtripCommonFeedBackActivity`. This count change reflects the changed emitted set: the two cleanup-only uncertain hotel Activities are absent.

The bridge regression is sharply localized. All **1,024 missing bridge rows** belong to four v1 H5 hosts, 256 rows each:

- `ctrip.android.view.h5.view.H5Container`
- `ctrip.android.flight.component.hybrid.h5.container.FlightMaskH5Container`
- `ctrip.android.flight.component.hybrid.h5.container.FlightHalfMaskH5Container`
- `ctrip.android.view.myctrip.orderbiz.MyCtripOrderModalActivity`

These are the same confirmed v1 H5Fragment hosts already established by the source oracle. The shortest required binding edge is:

`host -> attached ctrip.android.view.h5.view.H5Fragment -> fragment.mWebView -> H5WebView.H0(Object,H5WebView) -> ctrip.android.view.h5.b.a().n(obj, webView, receiver.j) -> ctrip.base.init.m$c.n(...) -> direct addJavascriptInterface calls`.

`m$c.n` constructs/registers the v1 core plugins and updates the same list passed as `H5WebView.j`; the loss is therefore at the installed v1 provider/interface-dispatch edge or its propagation back to these four Fragment hosts. It is not evidence that individual plugin classes disappeared, and subtype enumeration is unnecessary. The exact source anchors are `H5WebView.java:1082-1089` and `ctrip/base/init/m.java:711-839`; concrete host-to-Fragment evidence remains in `canonical-host-group-audit.jsonl` and `callback-binding-chains.md`.
