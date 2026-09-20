# v2b capability ownership audit

The candidate file contributes 2640 bridge facts at the generic H5Plugin iterator site. Source factory evidence validates 1925 core occurrences, supports 385 occurrences only as conditional candidates, and rejects 330 subtype-expansion occurrences.

The required proof is value-sensitive: a concrete type must be constructed in `n.t()` and placed in the returned list, or be returned by the exact Bus call added to that list. Assignability to `H5Plugin` alone is insufficient. `H5WebView.D()` iterates that returned list and reflectively reads the runtime instance TAG. The v2b rows inspected here carry `conditional=true`; that flag correctly prevents optional Bus/plugin facts from claiming unconditional execution, but it does not rescue types that no factory or provider can produce.

## False subtype expansions

- `ctrip.android.pay.common.plugin.CTH5PayCookiePluginV2`
- `ctrip.android.view.h5v2.plugin.H5BaseLocatePlugin`
- `ctrip.android.view.h5v2.plugin.H5BaseUtilPlugin`
- `ctrip.android.view.h5v2.plugin.H5BusinessPluginBase`
- `ctrip.android.view.h5v2.plugin.H5Plugin`
- `ctrip.business.plugin.h5.H5BaseImagePluginV2`

## Conditional-only types

- `ctrip.android.destination.view.h5.H5GSPluginV2Compat`
- `ctrip.android.finance.plugin.CustomCameraPluginV2`
- `ctrip.android.finance.plugin.H5FinanceHomePluginV2`
- `ctrip.android.finance.plugin.H5FinanceUtilPluginV2`
- `ctrip.android.pay.common.hybird.H5PayPluginV2`
- `ctrip.android.pay.facekitwrap.H5LivenessPluginV2`
- `ctrip.android.view.h5v2.plugin.H5SamSungWalletPlugin`

`v2-capability-ownership.jsonl` retains every candidate occurrence, including failures; no gold failure case is deleted.
