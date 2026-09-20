# Two-host expansion: wallet WebViews

## Closed host paths

- `ThirdWebActivity`: `ThirdWebActivity.java:14,87` reaches `WebFragment`, then the layout-owned `ProgressWebView` at `WebFragment.java:213`.
- `ThirdFullWebActivity`: `ThirdFullWebActivity.java:14,58` returns the same `WebFragment` from `g3()` and reaches the same carrier.
- Both hosts execute `DWebView.d()` followed by `ProgressWebView.initProgress()`. `WebFragment.java:222-223` installs its delegates through `ProgressWebView`; the system-installed callback owners remain `WebClientImpl$ProgressWebChromeClient` and `WebClientImpl$a`.

## Canonical evidence

`build_wallet_truth.py` streams the independent 266 MB DEX symbol export and refuses to emit if any required owner is absent. It retains the existing activity bindings, replaces only the two hosts' prior non-binding facts, and does not touch any failed or unrelated row. `finalize_canonical.py` then performs exact descriptor membership checks.

Each host has 110 canonical rows:

| kind | rows per host | evidence split |
|---|---:|---|
| activity binding | 1 | existing confirmed binding |
| setting invocation | 18 | 10 in `DWebView.d()`, 8 in `ProgressWebView.initProgress()` |
| callback override | 43 | 29 `ProgressWebChromeClient`, 11 `WebClientImpl$a`, 1 `WebFragment$b`, 2 `WebFragment$c` |
| bridge endpoint | 48 | 1 `_dsbridge`, 5 `_dsb`, 42 default-namespace API methods |

The settings count intentionally preserves successive calls. For example, `setAppCacheEnabled(false)` in `DWebView.d()` is followed by `setAppCacheEnabled(true)` in `ProgressWebView.initProgress()`; both are observed calls and the latter is the final configured value.

Bridge rows contain the real reflected or annotated endpoint descriptor. `_dsbridge.call` is the only member exposed through Android `addJavascriptInterface`. `_dsb` and the default API are DSBridge namespace objects reached through that transport. The default object is unconditionally registered by `ProgressWebView.java:271-273`; its 42 emitted methods all carry `Landroid/webkit/JavascriptInterface;` in the independent DEX export. Registration helpers are not substituted for endpoints.

## Validation result

Across these two hosts, all 182 DEX-owned callback/bridge rows are `symbol_status=confirmed`, all 36 Android settings rows are `external_api`, and the two activity bindings are `not_applicable`. There are no unresolved symbols or bindings in either host. Unknown Imgo registrations are handled separately in `bridge-normalization-audit.md`.

Reproduction:

```sh
python3 docs/validation/mango/build_wallet_truth.py
python3 docs/validation/mango/build_alipay_truth.py
python3 docs/validation/mango/build_pangle_truth.py
python3 docs/validation/mango/build_opos_truth.py
python3 docs/validation/mango/build_loadingweb_truth.py
python3 docs/validation/mango/build_xweb_truth.py
python3 docs/validation/mango/build_webcontainer_truth.py
python3 docs/validation/mango/build_huawei_truth.py
python3 docs/validation/mango/build_ubix_truth.py
python3 docs/validation/mango/build_mgadplus_truth.py
python3 docs/validation/mango/build_mglive_truth.py
python3 docs/validation/mango/build_landweb_truth.py
python3 docs/validation/mango/finalize_canonical.py
```
