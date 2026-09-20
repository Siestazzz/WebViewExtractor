# AdCore WebView wrapper factory binding

## Factory result

`AdWebViewWrapper.create(Context, boolean)` delegates directly to `create(Context, boolean, 0)`.

The three-argument factory selects a class as follows:

- `priorChoice == 2` forces the system wrapper.
- `priorChoice == 1` requests X5.
- `priorChoice == 0` uses `AdCoreConfig.getInstance().useX5()`.
- The requested X5 class name is the literal `com.tencent.adwebview.appwebview.AppWebViewWrapperImpl` and is loaded with `Class.forName`.
- The system/fallback class is the concrete `com.tencent.ams.adwebview.adapter.view.AdSysWebViewWrapperImpl.class`.
- The selected class is reflectively constructed with `(Context, boolean)`.

The X5 implementation class is absent from this APK's decompiled source and DEX symbol table. Therefore this APK's locally provable native-WebView path is the system fallback. The reflective X5 branch remains conditional on that class being supplied at runtime. The decompiler has incorrect local types and inverted-looking branches in this method; the class literals, reflection string, constructor descriptor, catch/fallback behavior, and concrete system implementation are independently visible and are the reliable facts.

## Parameter 0 to native WebView alias

For the system result, the shortest exact object chain is:

```text
AdWebViewWrapper.create(context, z[, choice])
 -> reflective AdSysWebViewWrapperImpl.<init>(context, z)
 -> receiver.f142972a = new AdWebView(context, z)

AdCoreWebViewHelper.setJsWebChromeClient(parameter0, bridge, client)
 -> parameter0 == the returned AdSysWebViewWrapperImpl receiver
 -> receiver.setWebChromeClient(selectedClient)
 -> receiver.f142972a.setWebChromeClient((WebChromeClient) selectedClient)

parameter0.getWebview()
 -> AdSysWebViewWrapperImpl.getWebview()
 -> return the same receiver.f142972a
```

Thus the native `android.webkit.WebView` receiving the WebChromeClient and the `View` returned by `getWebview()` are the same field object: `Lcom/tencent/ams/adwebview/adapter/view/AdSysWebViewWrapperImpl;->a:Landroid/webkit/WebView;` (JADX source name `f142972a`). The allocated runtime subtype is `AdWebView`, which extends the platform WebView.

`setJsWebChromeClient` first configures JavaScript, DOM storage, and database settings through parameter 0. If its third argument is non-null, that object is passed to `parameter0.setWebChromeClient`. If the third argument is null and the bridge is non-null, it creates a client: system wrappers use `AdCoreJsWebChromeClient(AdCoreJsBridge)`; X5 wrappers attempt the literal reflective class `com.tencent.adwebview.appwebview.AppJsWebChromeClient`. A null bridge plus null third argument installs no WebChromeClient.

For the system path, `AdSysWebViewWrapperImpl.setWebChromeClient(Object)` additionally requires that the native field is non-null and the supplied object is an `android.webkit.WebChromeClient`.

## Exact DEX signatures and fields

- `Lcom/tencent/ams/adcore/webview/AdWebViewWrapper;->create(Landroid/content/Context;Z)Lcom/tencent/ams/adcore/webview/AdWebViewWrapper;`
- `Lcom/tencent/ams/adcore/webview/AdWebViewWrapper;->create(Landroid/content/Context;ZI)Lcom/tencent/ams/adcore/webview/AdWebViewWrapper;`
- `Lcom/tencent/ams/adwebview/adapter/view/AdSysWebViewWrapperImpl;-><init>(Landroid/content/Context;Z)V`
- `Lcom/tencent/ams/adwebview/adapter/view/AdSysWebViewWrapperImpl;->a:Landroid/webkit/WebView;`
- `Lcom/tencent/ams/adwebview/adapter/view/AdSysWebViewWrapperImpl;->getWebview()Landroid/view/View;`
- `Lcom/tencent/ams/adwebview/adapter/view/AdSysWebViewWrapperImpl;->setWebChromeClient(Ljava/lang/Object;)V`
- `Lcom/tencent/ams/adcore/view/AdCoreWebViewHelper;->setJsWebChromeClient(Lcom/tencent/ams/adcore/webview/AdWebViewWrapper;Lcom/tencent/ams/adcore/js/AdCoreJsBridge;Ljava/lang/Object;)V`
- `Lcom/tencent/ams/adcore/view/AdCoreWebViewHelper;->createAdCoreJsWebChromeClient(Lcom/tencent/ams/adcore/js/AdCoreJsBridge;)Ljava/lang/Object;`

Source anchors: `AdWebViewWrapper.java:31-32,109-169`, `AdSysWebViewWrapperImpl.java:18-22,87-90,283-306`, and `AdCoreWebViewHelper.java:14-24,85-105`.

## AdLandingPageActivity host-to-page factory edge

The shipped, non-QFix path in `AdLandingPageActivity.onCreate(Bundle)` constructs
`AdLandingPageWrapper` with a concrete anonymous listener of DEX type
`Lcom/tencent/ads/landing/AdLandingPageActivity$1;`. The wrapper constructor stores
that same object in
`Lcom/tencent/ads/landing/AdLandingPageWrapper;->mAdLandingPageListener:Lcom/tencent/ads/landing/AdLandingPageWrapper$AdLandingPageListener;`.
The exact constructor is:

- `Lcom/tencent/ads/landing/AdLandingPageWrapper;-><init>(Landroid/app/Activity;Lcom/tencent/ads/landing/AdLandingPageWrapper$AdLandingPageListener;)V`

The Activity then calls
`Lcom/tencent/ads/landing/AdLandingPageWrapper;->onCreate(Landroid/os/Bundle;)V`.
That method posts `Lcom/tencent/ads/landing/AdLandingPageWrapper$2;` to a newly
created `android.os.Handler`. Its `run()V` calls the synthetic accessor
`Lcom/tencent/ads/landing/AdLandingPageWrapper;->access$200(Lcom/tencent/ads/landing/AdLandingPageWrapper;)V`,
which calls `Lcom/tencent/ads/landing/AdLandingPageWrapper;->delayLoad()V`.

`delayLoad()` reads the stored listener and synchronously invokes:

```text
Lcom/tencent/ads/landing/AdLandingPageWrapper$AdLandingPageListener;
  ->createAdPage(
       Landroid/content/Context;
       Lcom/tencent/ams/adcore/view/AdCorePageListener;
       ZZ
       Lcom/tencent/ads/view/AdServiceHandler;
     )Lcom/tencent/ams/adcore/view/AdCorePage;
```

Virtual dispatch reaches the concrete override with the identical parameter and
return descriptors:

`Lcom/tencent/ads/landing/AdLandingPageActivity$1;->createAdPage(Landroid/content/Context;Lcom/tencent/ams/adcore/view/AdCorePageListener;ZZLcom/tencent/ads/view/AdServiceHandler;)Lcom/tencent/ams/adcore/view/AdCorePage;`.

On the normal branch the override returns `new com.tencent.ads.view.AdPage(...)`.
Its exact constructor is
`Lcom/tencent/ads/view/AdPage;-><init>(Landroid/content/Context;Lcom/tencent/ams/adcore/view/AdCorePageListener;ZZLcom/tencent/ams/adcore/view/AdCoreServiceHandler;)V`;
`AdPage` extends `AdCorePage`. The wrapper assigns the callback return directly to
`Lcom/tencent/ads/landing/AdLandingPageWrapper;->mAdPage:Lcom/tencent/ams/adcore/view/AdCorePage;`,
then invokes, on that same field object and in sequence:

- `Lcom/tencent/ams/adcore/view/AdCorePage;->attachToCurrentActivity()V`
- `Lcom/tencent/ams/adcore/view/AdCorePage;->loadWebView(Ljava/lang/String;)V`

`attachToCurrentActivity()` adds the `AdCorePage` view to the Activity.
`loadWebView(String)` creates the `AdWebViewWrapper`, places its native view in
the page content, and installs the WebView clients. It is therefore the page
initialization call that reaches the wrapper factory documented above.

This factory execution is **asynchronous relative to the Activity's call to
`AdLandingPageWrapper.onCreate`**, because it crosses `Handler.post(Runnable)`.
Inside `AdLandingPageWrapper$2.run`, however, listener invocation, return-value
assignment, `attachToCurrentActivity`, and `loadWebView` are synchronous and
ordered. Since the Handler is created during Activity `onCreate` without an
explicit Looper, the normal Android execution uses the creating thread's Looper.

The required interprocedural edge is the callback object stored in
`mAdLandingPageListener`, together with the callback return assignment
`mAdLandingPageListener.createAdPage(...) -> mAdPage`. Dropping this assignment
loses the concrete `AdPage` even though the callback returns a non-`View`
declared type (`AdCorePage`); the later receiver calls on `mAdPage` are what lead
to WebView creation. QFix redirectors may replace these bodies at runtime, so
the concrete `AdPage` conclusion applies to the packaged fallback branch.

Source anchors: `AdLandingPageActivity.java:85-103,129-132`,
`AdLandingPageWrapper.java:51-53,85-92,146-147,264-281,588-617`,
`AdPage.java:14-21`, and `AdCorePage.java:494-512,772-809`.
