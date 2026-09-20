# Final five partial-host expansion

| host | canonical rows | settings | callbacks | exposed/message endpoints | retained unknown registrations |
|---|---:|---:|---:|---:|---:|
| `com.mgtv.ui.live.mglive.webview.WebViewActivity` | 35 | 8 | 6 | 20 | 0 |
| `com.imgo.vipcardiac.activity.LandWebActivity` | 48 | 34 | 7 | 6 | 0 |
| `com.mgadplus.brower.CustomWebActivity` | 65 | 20 | 19 | 25 | 0 |
| `com.huawei.petalpaysdk.webpay.PayWebviewActivity` | 12 | 5 | 6 | 0 | 0 |
| `com.ubix.ssp.open.comm.UBiXWebViewActivity` | 38 | 24 | 13 | 0 | 0 |

Every callback and endpoint descriptor is exact-matched against the independent APK DEX symbol export. Repeated setting calls and SDK-conditional values are preserved. Mglive and mgadplus use URL/message-queue bridge registries rather than Android JavaScript-interface transports; LandWeb additionally exposes the annotated `jsobj.callNative` transport.

Constant-field registration names are resolved from their definitions in the same APK: `MgtvMethodChannel.L` is `getUserInfo`, `zd.a.f160787v` is `copy`, and `CHECK_UPDATE` is `checkUpdate`. Their concrete anonymous handler descriptors are DEX-confirmed. No handler-registration API is substituted as an endpoint. Huawei and UBiX have no positive bridge exposure in their proven construction paths.

Reproduction uses `build_huawei_truth.py`, `build_ubix_truth.py`, `build_mgadplus_truth.py`, `build_mglive_truth.py`, and `build_landweb_truth.py` after the earlier family generators and before `finalize_canonical.py`.
