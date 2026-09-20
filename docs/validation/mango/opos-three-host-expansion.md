# OPOS three-host expansion

The three declared hosts converge on `com.opos.cmn.biz.web.b.b.b.b`, which constructs the `MoWebView`, configures its settings, installs callback owners `$c` and `$d`, and consumes the optional JavaScript-interface map. `build_opos_truth.py` expands this shared carrier once and emits the same proven calls separately for each host.

| host | binding | settings | callbacks | bridge registration |
|---|---:|---:|---:|---:|
| `com.opos.cmn.biz.web.activity.apiimpl.AdWebActivity` | 1 | 15 | 12 | 1 dynamic target |
| `com.opos.mobad.ui.feedback.FeedBackWebViewActivity` | 1 | 15 | 12 | 0 |
| `com.opos.cmn.module.ui.WebViewActivity` | 1 | 15 | 12 | 0 |

The callback descriptors are exact members of the independent APK DEX export. Android setting APIs are external symbols. The Ad host's `WebViewInitParams.jsInterfaceMap` is runtime data: its registration is retained, while its name, target type, and endpoint signature remain unknown. It is not scored as an exposed endpoint.

Evidence: `test/decompiled/com.hunantv.imgo.activity/sources/com/opos/cmn/biz/web/b/b/b/b.java:316-427`. Reproduce with:

```sh
python3 docs/validation/mango/build_inventory.py
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
