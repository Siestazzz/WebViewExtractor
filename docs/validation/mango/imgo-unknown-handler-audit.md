# Imgo registered-target audit

This audit resolves the 56 existing `registered-target-unknown` rows formed by seven Imgo hosts and eight registration names. It reads the disclosed JADX source and the independent APK DEX symbol export. It does not read holdout data and does not modify canonical facts.

## Dispatch rule

`ImgoWebView.registerWebHandler()` passes each route name and the WebView to `ImgoWebJavascriptImpl.registerHandler(String, ImgoWebView)`. That method installs an `ImgoWebJavascriptImpl$e0` handler on the WebView. Its `handler(String, CallBackFunction)` implementation performs exactly:

```java
ImgoWebJavascriptImpl.class
    .getDeclaredMethod(capturedRegistrationName, String.class)
    .invoke(ImgoWebJavascriptImpl.this, data);
```

The lookup class and invocation receiver are fixed. `getDeclaredMethod` searches only methods declared directly on `ImgoWebJavascriptImpl`; it does not search inherited methods. The code does not use `getClass()`, the registered `ImgoWebView`, an interface implementation, a runtime target map, or a subclass as the reflection owner. The only compatible endpoint shape is therefore an exact method name with descriptor `(Ljava/lang/String;)...` declared on `Lcom/mgtv/h5/ImgoWebJavascriptImpl;`.

## Per-name result

| registration name | exact declared `(String)` member in independent DEX | classification |
|---|---|---|
| `getSMSNumber` | none | proven no compatible endpoint |
| `getSMSCode` | none | proven no compatible endpoint |
| `payWithWeChat` | none | proven no compatible endpoint |
| `share` | none | proven no compatible endpoint |
| `showCustomShareMenus` | none | proven no compatible endpoint |
| `invokeH5Callback` | none; only `invokeH5Callback()V` exists | proven no compatible endpoint |
| `webviewBecomeActive` | none; `handleWebViewBecomeActive()V` is a different name and shape | proven no compatible endpoint |
| `webviewEnterBackground` | none; `handleWebViewEnterBackground()V` is a different name and shape | proven no compatible endpoint |

Related methods such as `showShare(String)`, `showShareMenus(String)`, and `shareTo(String)` do not satisfy exact-name reflection. Permission checks and `JSSDKMananger` gating occur before reflection but cannot create or redirect a missing Java method. If the gate permits dispatch, lookup reaches the exception path and removes the callback entry; if it denies dispatch, no endpoint is invoked.

All eight names have the same result for all seven hosts because they share the same concrete registration helper and fixed reflection owner. There is no remaining dynamic target choice for these 56 registrations.

## Recommended representation

Keep the registration facts and their source evidence. Classify them uniformly as `registered-no-compatible-endpoint`, with blank endpoint signature and no endpoint implementation. Count them in registration coverage if the name is emitted, exclude them from callable-member recall, and report their empty exposed-member surface separately. They should not remain endpoint “unknowns,” but they also must not be deleted merely to improve a metric.

Reproduce the check with `python3 docs/validation/mango/audit_imgo_unknown_handlers.py`.
