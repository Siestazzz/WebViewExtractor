# v9d NFT binding break audit

v9d contains 89 activities: all 84 v9c activities, the three previously removed and independently confirmed NFT activities, plus `DigitalBelongActivity` and `MgNftPreviewActivity`. All five NFT activities are valid owners. v9d recovers only their dynamic `loadUrl` operation. It still misses all 48 settings, three `setWebViewClient` registrations, and three registered `shouldOverrideUrlLoading` members in `nft-development-facts.jsonl`.

## New hosts

`MgNftPreviewActivity.S2` explicitly constructs `NftData(NftViewerType_Webview, buildH5Url(...), ...)` and calls its `F:MgNftViewer.setNftData`. This is an unconditional Webview-variant ownership chain after the method executes.

`DigitalBelongActivity.T3` computes the NFT viewer type; its source includes the `NftViewerType_Webview` arm and then calls `G:MgNftViewer.setNftData`. The WebView capability is conditional on that type arm, but the object is owned by this Activity.

## Exact ViewBinding chains

Both outer binding classes explicitly implement `androidx.viewbinding.ViewBinding` in the decompiled class declaration. The DEX symbol table independently confirms their private constructors, static `bind`/`inflate`, `getRoot`, and typed final fields.

Main NFT implementation:

1. `Lcom/hunantv/nft/databinding/NftViewerLayoutBinding;->inflate(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;)Lcom/hunantv/nft/databinding/NftViewerLayoutBinding;`
2. It inflates `nft_viewer_layout`, then calls `bind(viewGroup)`.
3. `Lcom/hunantv/nft/databinding/NftViewerLayoutBinding;->bind(Landroid/view/View;)Lcom/hunantv/nft/databinding/NftViewerLayoutBinding;` calls `View.findViewById(R.id.nft_webview)`, casts the result to `Lcom/hunantv/nft/threed/NftWebviewLayout;`, and passes it as constructor parameter 5 (including the root parameter).
4. `Lcom/hunantv/nft/databinding/NftViewerLayoutBinding;-><init>(Landroid/view/View;Lcom/hunantv/nft/twod/NftImageLayout;Lcom/hunantv/nft/twod/NftMusicLayout;Lcom/hunantv/nft/twod/NftGifLayout;Lcom/hunantv/nft/threed/NftWebviewLayout;Lcom/hunantv/nft/twod/NftVideoLayout;)V` stores that parameter in `nftWebview:Lcom/hunantv/nft/threed/NftWebviewLayout;`.
5. The nested layout constructor calls `initialize`, which invokes `Lcom/hunantv/nft/databinding/NftWebviewLayoutBinding;->inflate(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;)Lcom/hunantv/nft/databinding/NftWebviewLayoutBinding;` and then `bind`.
6. Nested `bind(View)` finds `R.id.webview`, casts it to `Landroid/webkit/WebView;`, and calls private `NftWebviewLayoutBinding.<init>(View,WebView)`, which stores the value in final field `webview:Landroid/webkit/WebView;`.

Shaded `mgsz` implementation:

1. `Lcom/google/android/filament/utils/databinding/ShNftViewerLayoutBinding;->inflate(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;)Lcom/google/android/filament/utils/databinding/ShNftViewerLayoutBinding;` inflates `sh_nft_viewer_layout` and calls `bind(viewGroup)`.
2. `bind(View)` calls `findViewById(R.id.nft_webview)`, casts to `Lcom/mgsz/hunantv/nft/threed/NftWebviewLayout;`, and passes it as constructor parameter 4 after the root.
3. `Lcom/google/android/filament/utils/databinding/ShNftViewerLayoutBinding;-><init>(Landroid/view/View;Lcom/mgsz/hunantv/nft/twod/NftImageLayout;Lcom/mgsz/hunantv/nft/twod/NftMusicLayout;Lcom/mgsz/hunantv/nft/threed/NftWebviewLayout;Lcom/mgsz/hunantv/nft/threed/NftModelLayout;Lcom/mgsz/hunantv/nft/twod/NftVideoLayout;)V` stores it in final field `nftWebview:Lcom/mgsz/hunantv/nft/threed/NftWebviewLayout;`.
4. The nested layout repeats the same pattern through `ShNftWebviewLayoutBinding.inflate/bind`, whose private constructor is exactly `Lcom/google/android/filament/utils/databinding/ShNftWebviewLayoutBinding;-><init>(Landroid/view/View;Landroid/webkit/WebView;)V` and whose final WebView field is `webview:Landroid/webkit/WebView;`.

## Earliest missing materialization

The report labels the eventual receiver `constructor_parameter`. This is accurate provenance but incomplete object modeling. The earliest shared break is after each generated `bind(View)` constructs and returns a new binding: the analyzer does not materialize constructor arguments into the returned binding object's final fields. The needed relationship is an allocation-local constructor store, not a general `field_object` guess:

`findViewById/cast value -> new Binding(... value ...) -> Binding.<init> iput final field -> returned Binding -> caller field -> initWebview receiver`.

Until that exact returned-object relation is represented, the outer `NftViewerLayoutBinding.nftWebview` and nested `NftWebviewLayoutBinding.webview` chains cannot carry `initialize/initWebview` back to these hosts. The existing `loadUrl` reaches the same receiver through the later `setupData` path, explaining why operation recovery does not imply recovery of constructor-time settings or client installation.

`v9d-nft-binding-break.json` preserves the 48+6 missing count. `audit_v9d_nft.py` regenerates it and the 89-row ownership file without reading sealed data.
