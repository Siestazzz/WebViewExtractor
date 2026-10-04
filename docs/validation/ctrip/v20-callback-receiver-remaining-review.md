# Ctrip remaining callback receiver review, v20

This is an independent source review of the 258 callback rows omitted from the earlier 154-row H5 composite-receiver correction. The earlier 154-row ledger and original v18/v19 inputs remain byte-for-byte unchanged. The v19-prefixed correction files are historical artifacts intended for the next v20 freeze; no existing v19 freeze is edited.

Actual sample identity remains ctrip-1 8.78.0, APK SHA-256 cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66. The source build.json and samples.json identify the original vuln APK. These facts do not concern a substituted patched APK.

All 258 remaining input callback rows are retained in the per-row audit with original canonical hash, concrete receiver, exact declaration descriptor, dispatch classification, conditional binding chain, source file SHA-256, source line ranges and actual source excerpts. There are 254 virtual-entry rows and four real explicit-super body rows. None was discarded or made negative. No additional independent branch delta was needed, because the original facts already separately represent the observed branches. Unknown receiver bindings within this submitted 258-row source scope: zero. This does not establish whole-APK coverage or runtime execution under hot patches, downloaded code, or all Activity configurations.

The four Sina parent onPageFinished/onPageStarted facts, input lines 4523–4526, are preserved as positive facts and corrected only by adding callback_dispatch=explicit_super and binding evidence. The a/d concrete override entry facts remain separate. The c receiver has no overrides and genuinely inherits its base declarations. WebActivity web_type 1/2/3 allocates d/a/c respectively, then installs the selected field. No class-family substitution was performed.

Other independently inspected cases include:

- QMP static IHelper singleton actually constructed as WebViewHelperSdk8; both factory returns allocate the installed ClientSdk8 and ViewClientSdk8 receivers. Activity interface delegates are not substituted for those clients.
- QQ host login delegates through Tencent and the actual AuthAgent to conditional web fallback. JNI success shows the secure auth dialog; JNI failure shows TDialog. Their concrete WebView clients and inherited anonymous Chrome field are separately traced to setters. Both genuine branches remain present.
- HotelFlagShipLoginActivity outer WebView installs its own Chrome/View clients. Its onCreateWindow callback constructs a distinct popup WebView and installs distinct nested receivers. Popup clients were not rewritten to the outer receiver.
- Ctrip v1 getWebClient constructs H5WebView$c, passed by p0 to setWebViewClient; fragment initialization reaches p0 through l0/o0. SearchWebView inherits that allocation path.
- Ctrip v2 H/I each directly installs new e; preloading constructs/caches H5WebView. Mkt/finance/flight fragment overrides and parent calls were inspected so that superclass declarations were not assumed to imply binding.
- Direct clients in CMB, UnionPay, ByteDance/Douyin, Kwai, Mqunar, Tencent face protocol, street map, Baidu/QQ authorization, pay, FAQ and Meizu retain their concrete installed receiver identities and complete descriptors.

No analyzer output, capability report, score, or holdout was consulted. Per-row hash checks and reviewed-view materialization use scripts/apply_oracle_corrections.py. The combined ledger contains the immutable earlier 154 corrections plus four dispatch annotations. The reviewed view retains all 4,944 original rows and a complete original-input copy; 158 rows are corrected. Production code is unchanged.

The supplemental ledger, its source audit and the combined reviewed view are ready for freezing under this explicit source-visible callback receiver scope. Exact hashes are recorded in v20-callback-receiver-remaining-summary.json and v20-callback-receiver-reviewed-view/manifest.json.
