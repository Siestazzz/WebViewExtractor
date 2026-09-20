# Shared capability groups

## ImgoWebView group

Hosts: `HalfWebActivity`, `MeCaptureWebActivity`, `PureWebActivity`, browser `WebActivity`, `WebActivityTransparent`, `CaptureWebActivity`, and `WebUIActivity`.

The host chains are recorded in `inventory.md`. Each reaches `ImgoWebView`, whose complete initialization chain in this APK is `RootWebView -> BridgeWebView -> ImgoWebView`. `facts.jsonl` expands every Settings call in those three initializers for every host: 16 calls from `RootWebView`, two from `BridgeWebView`, and 15 from `ImgoWebView`. Repeated setters are retained because later calls override earlier values. Client facts are split into one row for every override in `ImgoWebView$2`, `ImgoWebView$3`, and its popup `MoWebViewClient`.

## Direct and SDK groups

`BackDoorWebActivity`, `WechatWapPayRouterActivity`, CCB `H5PayActivity`, and Sina Weibo `WebActivity` have direct settings/client evidence in `facts.jsonl`. The remaining SDK Activities bind wrappers or fragments whose configuration is performed behind obfuscated factories. Their binding is positive, while unavailable settings and client implementations remain unknown under `unknowns.md`; no empty capability set is interpreted as negative.

This inventory treats only calls reached by the recorded host ownership chain as Activity capabilities. Global WebView helper calls with no host connection are excluded.

