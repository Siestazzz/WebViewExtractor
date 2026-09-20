# Mango TV WebView Activity inventory

APK SHA-256: `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5`

| Activity | layer | WebView / carrier | evidence |
|---|---|---|---|
| `com.mgtv.ui.browser.HalfWebActivity` | inherited | `com.hunantv.imgo.h5.ImgoWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/browser/HalfWebActivity.java:35 -> com/mgtv/ui/browser/BaseWebActivity.java:1639 -> com/hunantv/imgo/h5/ImgoWebView.java:89` |
| `com.mgtv.ui.login.MeCaptureWebActivity` | inherited | `com.hunantv.imgo.h5.ImgoWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/login/MeCaptureWebActivity.java:36 -> com/mgtv/ui/browser/BaseWebActivity.java:1639 -> com/hunantv/imgo/h5/ImgoWebView.java:89` |
| `com.mgtv.ui.guide.PureWebActivity` | direct | `com.hunantv.imgo.h5.ImgoWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/guide/PureWebActivity.java:45 -> com/hunantv/imgo/h5/ImgoWebView.java:89` |
| `com.mgtv.ui.other.BackDoorWebActivity` | direct | `android.webkit.WebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/other/BackDoorWebActivity.java:49-69` |
| `com.mgtv.diana.sdk.api.pay.WechatWapPayRouterActivity` | direct | `android.webkit.WebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/diana/sdk/api/pay/WechatWapPayRouterActivity.java:112-122` |
| `com.ccb.ccbnetpay.H5PayActivity` | direct | `android.webkit.WebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/ccb/ccbnetpay/H5PayActivity.java:131-206` |
| `com.sina.weibo.sdk.web.WebActivity` | direct | `com.hunantv.imgo.h5.browser.MoWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/sina/weibo/sdk/web/WebActivity.java:98-205` |
| `com.mgtv.ui.browser.WebActivity` | fragment | `com.mgtv.ui.browser.WebViewFragment` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/browser/WebActivity.java:57,101,680` |
| `com.mgtv.ui.browser.WebActivityTransparent` | fragment | `com.mgtv.ui.browser.WebViewFragment` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/browser/WebActivityTransparent.java:52,90,482` |
| `com.mgtv.ui.login.CaptureWebActivity` | fragment | `com.mgtv.ui.login.widget.CaptureWebViewFragment` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/login/CaptureWebActivity.java:51,102,419-420` |
| `com.hunantv.imgo.xweb.XWebActivity` | fragment | `com.hunantv.imgo.xweb.XWebViewFragment` | `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/xweb/XWebActivity.java:51,82,357` |
| `com.hunantv.webui.WebUIActivity` | fragment | `com.hunantv.webui.WebUIFragment` | `test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/webui/WebUIActivity.java:51,89,470` |
| `com.mgsz.h5.WebContainerActivity` | fragment | `com.mgsz.h5.WebViewFragment` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgsz/h5/WebContainerActivity.java:16-35` |
| `com.platform.oms.ui.LoadingWebActivity` | fragment | `com.platform.oms.ui.FragmentWebLoadingBase` | `test/decompiled/com.hunantv.imgo.activity/sources/com/platform/oms/ui/LoadingWebActivity.java:31,38,124,287` |
| `com.mgtv.ui.live.mglive.webview.WebViewActivity` | direct | `com.mgtv.ui.live.mglive.h5.jsbridge.BridgeWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/live/mglive/webview/WebViewActivity.java:26,31,129` |
| `com.imgo.vipcardiac.activity.LandWebActivity` | wrapper | `com.imgo.webbase.hybird.BaseWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/imgo/vipcardiac/activity/LandWebActivity.java:36,90,134-138` |
| `com.mgadplus.brower.CustomWebActivity` | wrapper | `com.mgadplus.brower.ImgoAdWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgadplus/brower/CustomWebActivity.java:29,36,176-185` |
| `com.mgtb.money.web.ThirdWebActivity` | fragment | `com.mgtb.money.web.webview.ProgressWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtb/money/web/ThirdWebActivity.java:14,87` |
| `com.mgtb.money.web.ThirdFullWebActivity` | fragment | `com.mgtb.money.web.webview.ProgressWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/mgtb/money/web/ThirdFullWebActivity.java:14,58` |
| `com.opos.cmn.biz.web.activity.apiimpl.AdWebActivity` | inherited | `OPOS BaseWebActivity web component` | `test/decompiled/com.hunantv.imgo.activity/sources/com/opos/cmn/biz/web/activity/apiimpl/AdWebActivity.java:9` |
| `com.opos.mobad.ui.feedback.FeedBackWebViewActivity` | inherited | `com.opos.cmn.module.ui.WebViewActivity` | `test/decompiled/com.hunantv.imgo.activity/sources/com/opos/mobad/ui/feedback/FeedBackWebViewActivity.java:9 -> com/opos/cmn/module/ui/WebViewActivity.java:15` |
| `com.opos.cmn.module.ui.WebViewActivity` | wrapper | `com.opos.cmn.module.ui.a` | `test/decompiled/com.hunantv.imgo.activity/sources/com/opos/cmn/module/ui/WebViewActivity.java:15,126-146` |
| `com.bytedance.sdk.openadsdk.core.activity.base.TTWebPageActivity` | third-party | `com.bytedance.sdk.component.widget.SSWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity.java:66,183,420` |
| `com.bytedance.sdk.openadsdk.core.activity.base.TTPlayableWebPageActivity` | third-party | `com.bytedance.sdk.component.widget.SSWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/bytedance/sdk/openadsdk/core/activity/base/TTPlayableWebPageActivity.java:45,788-802` |
| `com.bytedance.sdk.openadsdk.core.activity.base.TTVideoWebPageActivity` | third-party | `com.bytedance.sdk.component.widget.SSWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity.java:64,178,447` |
| `com.bytedance.sdk.openadsdk.core.activity.base.TTVideoScrollWebPageActivity` | inherited | `com.bytedance.sdk.component.widget.SSWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/bytedance/sdk/openadsdk/core/activity/base/TTVideoScrollWebPageActivity.java:12 -> TTVideoWebPageActivity.java:178` |
| `com.huawei.petalpaysdk.webpay.PayWebviewActivity` | inherited | `com.huawei.petalpaysdk.widget.PaySafeWebView` | `test/decompiled/com.hunantv.imgo.activity/sources/com/huawei/petalpaysdk/webpay/PayWebviewActivity.java:26-39` |
| `com.alipay.sdk.app.H5AuthActivity` | inherited | `Alipay H5PayActivity web container` | `test/decompiled/com.hunantv.imgo.activity/sources/com/alipay/sdk/app/H5AuthActivity.java:4 -> com/alipay/sdk/app/H5PayActivity.java:12` |
| `com.alipay.sdk.app.H5OpenAuthActivity` | inherited | `Alipay H5PayActivity web container` | `test/decompiled/com.hunantv.imgo.activity/sources/com/alipay/sdk/app/H5OpenAuthActivity.java:8 -> com/alipay/sdk/app/H5PayActivity.java:12` |
| `com.ubix.ssp.open.comm.UBiXWebViewActivity` | third-party | `com.ubix.ssp.open.comm.widget.c` | `test/decompiled/com.hunantv.imgo.activity/sources/com/ubix/ssp/open/comm/UBiXWebViewActivity.java:39,107-108` |

Layer counts: direct=6, fragment=9, inherited=8, third-party=4, wrapper=3. `manager`, `factory`, and `message bridge` are implementation layers catalogued in `bridge-inventory.md`; they are not Activity ownership categories.
