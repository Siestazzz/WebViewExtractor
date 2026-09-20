# Pangle shared-family expansion

`TTWebPageActivity` owns the landing-page `SSWebView`. `TTPlayableWebPageActivity` receives a distinct renderer-owned `SSWebView` from `core.playable.bp$c`; the two identities remain separate.

Both paths construct `SSWebView`, apply `setSavePassword(false)`, then use `core.widget.c.tn` with zoom disabled. The helper contributes nine exact settings calls; each path then sets a dynamic user agent and mixed-content mode 0. Thus each host has 12 observed Settings calls.

The landing path installs `TTWebPageActivity$1/$6` over shared clients `core.widget.c.z/zx`: 23 effective override bodies. The playable path installs `bp$c$2/$3` over the same shared clients: 26 effective override bodies. Subclass and superclass bodies are both retained where the subclass invokes `super`.

`core.i.z` registers `z$c` under `JS_LANDING_PAGE_LOG_OBJ`. Independent DEX annotations confirm `getUrl()`, `readHtml(String,String)`, and `readPercent(String)` for both paths. The three standard unsafe-interface removals are not exposures.

Canonical totals are 39 rows for `TTWebPageActivity` and 42 for `TTPlayableWebPageActivity`, including their existing bindings. All 55 callback/bridge rows are exact DEX matches; all 24 Settings rows are Android external APIs.

The same builder also closes `TTVideoWebPageActivity` and its inherited `TTVideoScrollWebPageActivity`: each has 39 rows (binding 1, settings 12, effective callbacks 23, bridge endpoints 3). The scroll subclass adds no separate WebView construction or configuration.

Rebuild with `build_pangle_truth.py` after the inventory, wallet, and Alipay builders and before `finalize_canonical.py`.
