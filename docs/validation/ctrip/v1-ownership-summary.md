# v1 Activity ownership audit

This audit treats `capabilities.json` only as a candidate list. Ownership is decided from the matching decompiled source. It reviews 52 reported Activities: 44 valid, 1 wrong, and 7 uncertain.

`valid` means a source-visible direct, inheritance, Fragment, custom WebView, SDK, or map-wrapper chain reaches the implementation, including conditional paths. `wrong` requires contradictory source evidence, not merely failure to find a chain. `uncertain` means inheritance/shared reachability exists but a concrete invocation could not be proved.

`TencentEntryActivity` is conditionally valid: its QQ share path reaches `QQShare.shareToQQ`, which constructs `TDialog` when native sharing is unsupported. `CtripCommonFeedBackActivity` is the one confirmed wrong owner: it creates new `ImageItem` objects without setting `bottomWebViewUrl`, passes null through `GalleryView.C`, and therefore takes the empty-URL branch before the H5WebView constructor.

The seven hotel cases are uncertain. Their shared BaseActivity or hotel infrastructure exposes pop-layer construction, but the concrete subclasses do not visibly invoke it; inheritance alone is insufficient to call them wrong or valid. QR scan/gallery, map wrapper, and H5 container chains remain valid where their Fragment or wrapper construction is explicit.
