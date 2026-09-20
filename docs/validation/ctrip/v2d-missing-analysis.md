# Ctrip v2d missing capability audit

The stale evaluation lists 322 misses. Source ownership review separates oracle errors from extractor gaps:

## extractor_gap_v1_chrome_argument_flow

Count: 11; kinds: `{'callback': 11}`.

H5Fragment.initWebView creates H5Fragment$b, stores it, then calls VideoEnabledWebView.setWebChromeClient; the override forwards the same parameter to WebView.setWebChromeClient.

## oracle_error_non_h5_host

Count: 290; kinds: `{'setting': 13, 'bridge': 34, 'bridge_method': 222, 'callback': 21}`.

SimpleOverseaMapActivity owns CtripWebView and registers only console/mapDAO; it never constructs H5WebView/H5Fragment.

## oracle_error_direct_h5webview_no_chrome

Count: 11; kinds: `{'callback': 11}`.

EvaluateDialogActivity calls H5WebView.H directly. H() installs a WebViewClient but no WebChromeClient; no H5Fragment or g/a instance is created.

## extractor_gap_field_object_bridge

Count: 7; kinds: `{'bridge': 1, 'bridge_method': 6}`.

CMBApiEntryActivity.<init> writes null to mCmbSdkExecutor. onCreate calls initJsInterface(), which constructs cmbapi.h and writes the field; the following initWebView() reads that field for CMBSDK registration.

## oracle_error_delegate_not_override

Count: 3; kinds: `{'callback': 3}`.

AuthActivity implements IWebCallback. Actual installed overrides are ClientSdk8 and ViewClientSdk8 returned through WebViewHelper.IMPL.

## Corrected callback gap

Of the reported 46 callback misses, 21 came from the incorrect SimpleOverseaMap H5 group link, 3 were QMP delegate methods mislabeled as installed overrides, and 11 v2 Chrome callbacks were incorrectly assigned to EvaluateDialogActivity. That Activity directly calls H5WebView.H(), which installs only its WebViewClient. The remaining reported gap is the 11-method v1 WebChromeClient chain in MyCtripOrderModalActivity. Correcting QMP adds nine actual installed overrides under `ClientSdk8` and `ViewClientSdk8`; these require a new evaluation run.

The callback fix needs argument identity through an override/trampoline, followed by superclass-member closure. The concrete object passed to `setWebChromeClient` must remain concrete after `VideoEnabledWebView.setWebChromeClient(WebChromeClient)` calls `super.setWebChromeClient(webChromeClient)`. Member collection then includes the concrete class and inherited Ctrip video client class, deduplicated by full DEX signature.

## Corrected bridge gap

Of the reported 263 bridge/bridge-method misses, 256 came from the incorrect SimpleOverseaMap H5 group link. The seven real reported gaps are the `CMBSDK` registration and six annotated methods on `Lcmbapi/h;`. The constructor initializes `mCmbSdkExecutor` to null. `onCreate` then calls `initJsInterface()`, whose `new cmbapi.h` result is written to the field, before `initWebView()` reads it for registration. v3 emits the CMBSDK registration name but resolves the implementation as `number` with no members, so the helper field-writer dependency gap remains.
