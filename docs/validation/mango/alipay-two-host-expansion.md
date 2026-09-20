# Alipay two-host expansion

`H5AuthActivity` and `H5OpenAuthActivity` inherit `H5PayActivity`. `H5PayActivity.onCreate()` selects one of two WebView carriers from the `version` extra (`H5PayActivity.java:107-119`), so the paths remain separate conditional identities in canonical facts.

## v1 carrier

`com.alipay.sdk.widget.h` constructs `MoWebView`, applies 11 WebSettings calls, and installs `com.alipay.sdk.app.b` (`h.java:20-27,52-83`). The DEX-confirmed client overrides are `onPageFinished`, `onPageStarted`, legacy `onReceivedError`, `onReceivedSslError`, and `shouldOverrideUrlLoading`.

## v2 carrier

`com.alipay.sdk.widget.j` constructs `com.alipay.sdk.widget.p` and supplies its proxy interfaces (`j.java:116-135`). `p` constructs `MoWebView`, applies 13 settings calls plus its user-agent call, and installs concrete clients `com.alipay.sdk.widget.s` and `t` (`p.java:112-127,180-204,216-218`). DEX confirms three Chrome callbacks and four WebView callbacks.

The second `setJavaScriptEnabled(true)` call at `p.java:194` is retained separately because truth records observed calls. Both carriers remove `searchBoxJavaBridge_`, `accessibility`, and `accessibilityTraversal`; these are hardening removals and do not expose bridge members. The v2 `alipayjsbridge://` URL/prompt protocol is implemented through the confirmed Client callbacks and does not use `addJavascriptInterface`.

## Counts

Each host adds 36 capability rows: 24 setting invocations and 12 callback overrides across the two conditional carriers, plus the existing activity binding for 37 canonical rows per host. All 24 callback descriptors across both hosts must exist in the independent DEX export. The two hosts have no exposed Java-object bridge rows.

Reproduction:

```sh
python3 docs/validation/mango/build_inventory.py
python3 docs/validation/mango/build_wallet_truth.py
python3 docs/validation/mango/build_alipay_truth.py
python3 docs/validation/mango/build_pangle_truth.py
python3 docs/validation/mango/finalize_canonical.py
```
