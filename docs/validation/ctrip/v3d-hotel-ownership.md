# v3d hotel ownership re-audit

This audit re-reads the seven previously uncertain v3 Activities from the v3d evidence and independently checks each path in decompiled source. It does not use an emitted capability as ground truth.

The result is five source-supported hosts and two cleanup-only Activities. `HotelFlagShipLoginActivity`, both gallery Activities, and both map Activities are valid ownerships. `HotelDetailCharityProjectActivity` and `HotelVideoActivity` remain `uncertain` as WebView hosts: their one emitted `callback_removal` is a real inherited destruction path and remains a scored fact, but neither Activity has a source-visible WebView or pop-layer registration path.

## Distinct chains

- **Flagship login (13 emitted facts):** `onCreate` constructs `android.webkit.WebView`, adds it to the layout, then calls `configWebView` and `registerListener`. The three direct settings and two direct clients are unconditional after construction. `onCreateWindow` conditionally creates a second WebView and installs five settings and two clients. The inherited pop-layer removal is cleanup-only and conditional on `mPopLayerClient != null`; it is not the basis for the valid host decision.
- **Hotel detail map (14):** `HotelDetailMapActivity` constructs `CtripUnitedMapView`; its initialization selects `CGoogleMapView` on the Google-map branch; `CGoogleMapView` calls `initWebView`, which creates the WebView, configures nine settings/global settings, installs both clients, and registers `GoogleMapUtils`. The 13 map facts are conditionally valid for that runtime map branch. The remaining pop-layer removal is an inherited cleanup fact.
- **Hotel list map (14):** `onCreate -> initMapPresenter -> HotelListMapCorePresenter` and the presenter constructor calls `A`, which constructs `HotelListUnitedMapView`. That subclass calls `CtripUnitedMapView`, reaching the same conditional Google-map branch and the same 13 map facts. The remaining removal is inherited cleanup.
- **Album/photo gallery (329 each):** each Activity builds `GalleryView.p` from its real `imageList` and invokes `GalleryView.L`; `GalleryView.M` reads the selected `ImageItem.bottomWebViewUrl`, then `C -> c.b`. `c.b` creates and initializes `H5WebView` only when the URL is nonempty. The H5 ownership is therefore source-supported but conditional on input data. This host audit does not turn every polymorphic plugin candidate in the v3d expansion into an unconditional registration; plugin membership remains governed by the separate registry audit.
- **Charity/video (1 each):** the concrete class extends hotel `BaseActivity`. Lifecycle dispatch reaches `BaseActivity.onDestroy -> destroyPopLayer`; when `mPopLayerClient` exists, its teardown reaches the emitted `n.j` callback removal. No Activity-specific creation, `showPopLayer`, WebView, or registration chain was found. The removal is valid as a nullable inherited cleanup path; the Activity remains cleanup-only/uncertain as a positive WebView host.

After updating the reusable v3 ownership file, its conservative totals are **52 valid, 1 wrong, 2 uncertain**. The wrong-or-uncertain host upper bound is **3/55 (5.5%)**. This does not discard the two cleanup facts from capability scoring.

Machine-readable chains and per-path verdicts are in `v3d-hotel-ownership.jsonl`.
