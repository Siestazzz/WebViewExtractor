# Correction log

- The initial inventory encoded Activity-to-WebView ownership as `kind=callback`, `name=webview_binding`. That classification was wrong because it is not a callback override.
- All such rows are now `kind=activity_binding`. They must not participate in callback precision, recall, or denominator calculations.
- Callback rows now represent one concrete override per row and carry a normalized DEX-style method signature.
- Every fact now has `normalized_signature`, `normalized_api`, and `bridge_method`. Fields that do not apply are the empty string rather than an inferred value.
- Settings initially used a short `android.webkit.WebSettings#method` value in `normalized_api`. The comparison key is now the complete DEX API signature, identical to `normalized_signature`.
- Callback normalization initially used an initialization/container owner from `implementation` for several rows. It now uses the concrete client class from the allocation/declaration (`ImgoWebView$2`, `$3`, `$3$1`, concrete named clients, or verified Activity anonymous classes), and `implementation` records that same concrete owner.
- `jsobj` bridge rows initially placed the transport signature only in `bridge_method`. They now copy the verified DEX signature to `normalized_signature` and use `$` for the real registered inner-class owner.
- CCB `javaObj` members were initially vulnerable to the generic Mango registration normalizer because their value was also `registered`. They now use the concrete `H5PayActivity$d` owner and their own annotated method descriptors.
- Canonical Settings now carry `value_kind` and `source_expression`: decoded booleans/numbers/plain strings are literals, PluginState values are enums, and runtime expressions have `value=null`.
- APK-owned normalized methods are checked by exact equality against `test/runs/symbols/mango.jsonl`. Android framework Settings APIs are marked `external_api`, since they are not defined in the APK DEX.
- Message registrations remain registration facts. If the reflected target method is absent from the DEX symbol table, `bridge_method` is cleared and status becomes `registered-target-unknown`; `registerHandler` is not substituted as an exposed member.
