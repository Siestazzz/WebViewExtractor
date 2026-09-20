# WebView receiver constraint v2 audit

This version retains the v1 mapping and adds the verified parent chains for the labels affected by strict replay. The exact concrete types do not change.

`X5WrapperWebView` is a real X5 WebView subclass, not a composition wrapper: `X5WrapperWebView extends DtX5WebView`, and `DtX5WebView extends com.tencent.smtt.sdk.WebView`. Its constructor creates `new WebViewBridge(this)` and all inherited `getSettings`, client, and navigation APIs execute on that same object. `PrivacyAbsWebActivity` inflates `init_web_browser_layout`, whose WebView tag is exactly `X5WrapperWebView`, then stores that view. `SecurityTicketActivity` similarly casts its inflated view to `X5WrapperWebView`. Therefore `privacy_web` and `security_ticket` remain constrained to the concrete `X5WrapperWebView`; widening them to `com.tencent.smtt.sdk.WebView` to recover strict-score misses would weaken source identity.

`BaseWebView` is also the concrete receiver rather than a container. Its chain is `BaseWebView -> RecyclableWebView -> X5WrapperWebView -> DtX5WebView -> com.tencent.smtt.sdk.WebView`. `activity_support.xml` directly inflates `BaseWebView`, and `SupportActivity` stores that view in its declared X5 `WebView` field. Settings returned by the inherited X5 API belong to the same `BaseWebView` object. The constraint remains `BaseWebView`.

The novel path has two wrappers which are not API receivers. `NovelWebView` inflates and stores `NovelLoadingWebView`; `NovelLoadingWebView` finds and stores the XML `AdWebView`; its getter returns that object to `WebNovelActivity`. The receiver chain is `AdWebView -> BaseSysWebView -> DtWebView -> android.webkit.WebView`. The correct concrete constraint remains `com.tencent.news.tad.business.ui.landing.AdWebView`. `advert_web` uses `AdLoadingWebView` and reaches the same concrete `AdWebView` receiver.

The v2 strict replay loss of privacy settings/callbacks does not justify changing source truth: it shows the extractor emits a superclass or otherwise incompatible receiver identity under exact-type scoring. Canonical was not modified by this audit.
