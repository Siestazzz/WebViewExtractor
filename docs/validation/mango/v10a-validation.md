# Mango v10a validation

v10a retains exactly the 89 v9d activities. No host was added or removed. The complete ownership file therefore reuses the independently reviewed v9d verdicts and updates each host's fact count: 84 valid and 5 uncertain.

## NFT constructor capture

The intended constructor capture works. For the three development-set hosts, v9d's single operation fact becomes the expected 16 settings plus one `setWebViewClient` fact whose member list contains the required concrete `shouldOverrideUrlLoading` implementation. Strict replay reports 48/48 settings, 3/3 registrations, and 3/3 required members. None of these restored setting or callback facts has a `constructor_parameter` WebView identity.

The object paths match the generated ViewBinding code reviewed for v9d:

`Activity view -> MgNftViewer -> NftViewerLayoutBinding.nftWebview -> NftWebviewLayout constructor -> NftWebviewLayoutBinding.webview -> initialize/initWebview`.

The main implementation registers `com.hunantv.nft.threed.NftWebviewLayout$initialize$1`; the shaded implementation registers `com.mgsz.hunantv.nft.threed.NftWebviewLayout$a`. Their member lists contain the exact DEX `shouldOverrideUrlLoading(WebView,WebResourceRequest):boolean`. The tool also lists inherited `MoWebViewClient.onRenderProcessGone` and `shouldInterceptRequest`; those are real inherited overrides, not setter-derived callback names.

For `AntiqueDetailActivity`, settings use a `WebSettings` union identity while the callback uses the backing `WebView` union identity. Source and report evidence show both read the same `ShNftWebviewLayoutBinding.webview`; `getSettings()` accounts for the type/identity change. This is an identity presentation limitation, not an incorrect host combination.

`MgNftPreviewActivity` has two distinct XML viewer fields. `setupViews` assigns `E` from `R.id.nftViewer` and `F` from `R.id.h5Viewer`. Each `MgNftViewer` inflates its own nested `NftWebviewLayout`, so its two 16-setting/client groups are real separate initialization surfaces. Only `F` receives the preview Webview data, but construction-time settings and client installation occur for both objects.

## Player-family expansion

Six existing hosts changed substantially:

| Family | v9d facts | v10a facts |
|---|---:|---:|
| Four `LandVodGameRoomActivity` / `VodGameRoomActivity` variants | 74 each | 221 or 222 |
| `LiteVodActivity` | 74 | 222 |
| `VodPlayerPageActivity` | 658 | 838 |

The common ownership is valid. Each room/lite host initializes `MiniAppFragment`; `onViewCreated` reaches `DianaView`, which creates the `AppService` JavaScript engine and page WebViews. `VodPlayerPageActivity.pk` reaches the same MiniApp path and can additionally render a distinct `GamePage` WebView.

The raw increase is not a count of unique capabilities. v10a retains multiple identities for the same objects:

- The `ServiceWebView` from `AppService.createJsEngine(Context)` appears once as `entry_parameter` and again as `entry_parameter/view:2131828340` under the same allocation prefix.
- A `PageWebView` appears under its allocation/render path and through `PageWebViewWrapper.mWebView`; these field paths alias the allocated page WebView.
- Similar `Page.b` and `WebViewContainer.webViewWrapper` paths expose the same receiver through different holders.

The new major groups still correspond to real Diana objects: one service WebView, normal page WebViews, the cover/container page WebView, and for `VodPlayerPageActivity` a conditional game-page WebView. I found no cross-host ownership error. The unresolved aliases explain the 221/222/838 raw counts and should be semantically deduplicated before interpreting coverage or surface size. The 14 facts removed from each smaller host and 26 removed from `VodPlayerPageActivity` are replaced object identities rather than lost source capabilities.

`v10a-validation.json` contains per-host kind counts, identities, and the alias assessment. `audit_v10a.py` regenerates it and `v10a-ownership.jsonl` without reading sealed data.
