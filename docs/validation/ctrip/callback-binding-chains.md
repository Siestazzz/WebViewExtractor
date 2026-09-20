# Callback binding chains behind the v2d gaps

## H5 v1 WebChromeClient

`MyCtripOrderModalActivity.onCreate()` calls `renderUI(String)` at line 113. `renderUI` either retrieves the tagged v1 `H5Fragment` or constructs `new H5Fragment()` at line 126, supplies a nonempty load URL, and commits it into the Activity at line 135. Fragment lifecycle reaches `H5Fragment.onResume()`; once visible it calls `loadWebview()` at lines 3512-3515. `loadWebview()` calls `initWebView()` unless explicitly disabled. `initWebView()` constructs `Lctrip/android/view/h5/view/H5Fragment$b;` at line 2941, stores it as the fragment client, and invokes `Lctrip/android/view/h5/view/VideoEnabledWebView;->setWebChromeClient(Landroid/webkit/WebChromeClient;)V` at line 2944. That override retains the instance when it is an `f` and forwards the same parameter to the framework setter. The concrete class contributes seven overrides and inherits four from `Lctrip/android/view/h5/view/f;`, for 11 missing signatures. The exact signatures are recorded under `extractor_gap_v1_chrome_argument_flow` in `v2d-missing-analysis.jsonl`.

The common failure is loss of parameter identity at the overridden setter, followed by failure to close members over the concrete superclass. Deduplication must use the full owner/name/parameters/return descriptor; it must not collapse overloads by method name.

## EvaluateDialogActivity correction

`EvaluateDialogActivity` does not instantiate `H5Fragment` and does not install `Lctrip/android/view/h5v2/view/g/a;`. Its layout supplies an `H5WebView`; `onCreate` calls `H5WebView.H(Activity,String,b)` at source line 240. `H()` initializes plugins and installs only `new e(this.x)` as the WebViewClient (`H5WebView.java:235-247`). No source-visible call installs the fragment WebChromeClient. The former 11 v2 Chrome expectations were shared-group over-attribution and are excluded for this host.

## QMP helper factory

The old three `AuthActivity` callback expectations were delegate interface methods and have been removed. The actual installed callback implementations follow:

- `Lcom/qmp/sdk/utils/WebViewHelper;->getChromeClient(Lcom/qmp/sdk/utils/WebViewHelper$IWebCallback;)Landroid/webkit/WebChromeClient;`
- `Lcom/qmp/sdk/utils/WebViewHelper;->IMPL:Lcom/qmp/sdk/utils/WebViewHelper$IHelper;`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8;->getChromeClient(Lcom/qmp/sdk/utils/WebViewHelper$IWebCallback;)Landroid/webkit/WebChromeClient;` returns `ClientSdk8`
- `Lcom/qmp/sdk/utils/WebViewHelper;->getViewClient(Lcom/qmp/sdk/utils/WebViewHelper$IWebCallback;)Landroid/webkit/WebViewClient;`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8;->getViewClient(Lcom/qmp/sdk/utils/WebViewHelper$IWebCallback;)Landroid/webkit/WebViewClient;` returns `ViewClientSdk8`

The nine actual override signatures are:

- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ClientSdk8;->onJsAlert(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsResult;)Z`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ClientSdk8;->onJsConfirm(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsResult;)Z`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ClientSdk8;->onProgressChanged(Landroid/webkit/WebView;I)V`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ClientSdk8;->onReceivedTitle(Landroid/webkit/WebView;Ljava/lang/String;)V`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ViewClientSdk8;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ViewClientSdk8;->onPageStarted(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ViewClientSdk8;->onReceivedError(Landroid/webkit/WebView;ILjava/lang/String;Ljava/lang/String;)V`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ViewClientSdk8;->onReceivedSslError(Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;)V`
- `Lcom/qmp/sdk/utils/WebViewHelper$WebViewHelperSdk8$ViewClientSdk8;->shouldOverrideUrlLoading(Landroid/webkit/WebView;Ljava/lang/String;)Z`

This case requires receiver-sensitive dispatch through a static interface-typed singleton and return-object propagation across the helper call.

## CMB bridge

`Lcmbapi/CMBApiEntryActivity;-><init>()V` writes null to `mCmbSdkExecutor`; it does not construct the bridge. `onCreate` calls `Lcmbapi/CMBApiEntryActivity;->initJsInterface()V`, which executes `new-instance Lcmbapi/h;` and `iput-object` to `->mCmbSdkExecutor:Lcmbapi/h;`, then calls `initWebView()`. `Lcmbapi/CMBApiEntryActivity;->initWebView()V` uses `iget-object` on that field and passes the result to `addJavascriptInterface` with literal registration name `CMBSDK`. The missing group contains the registration plus six exact `Lcmbapi/h;` method descriptors in `v2d-missing-analysis.jsonl`. This requires a dependency from the registration method to a separate field-writer helper invoked earlier in the same lifecycle method.
