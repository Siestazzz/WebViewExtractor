# v4d Mango Activity ownership

Reviewed all 92 emitted Activities against the same APK source/DEX. Existing independent v1 verdicts were reused for 68 Activities; 24 newly emitted Activities received source-chain review. Holdout data was not read.

Result: 84 valid, 3 wrong, 5 uncertain.

The three new wrong associations are `MgAgentActivity`, `MgAgentFloatActivity`, and `VipChannelPreviewActivity`. Their reported path crosses a generic coroutine continuation into `DebugDialog...injectConsole` and then `PageWebView`; the host Activity/Fragment source does not call that debug continuation or construct the reported WebView. Shared coroutine machinery is insufficient ownership evidence.

New valid families include LuckyBag renderer inheritance, Diana mini-app/GamePage carriers, explicit commerce share-to-`TDialog` paths, mgadplus reward rendering, NFT viewer layouts, WebContainer ViewBinding, and channel WebView helpers. Conditional share dialog paths remain valid positive ownership paths.

| verdict | count |
|---|---:|
| valid | 84 |
| wrong | 3 |
| uncertain | 5 |
