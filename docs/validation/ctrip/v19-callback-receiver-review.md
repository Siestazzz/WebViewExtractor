# Ctrip v19 callback receiver semantic review

Independent GPT-6.1 Sol source audit; based_on_report=false. No tool capability report, score, or holdout was used. Production code and prior facts/freezes were not modified.

The supplied v18 input has 4,944 rows, including 412 callback rows. This ledger fully checks the receiver/declaration distinction for 154 H5 fragment Chrome callback rows: 55 v1 and 99 v2. All 154 are corrections of composite `concrete + inherited parent` implementation strings to the concrete constructed receiver. Among them, 56 callbacks retain the superclass declaring owner in their complete normalized_signature. The other 98 retain their concrete declaration owner. No descriptor was mechanically changed to the receiver type.

Construction, field assignment, actual setter, overridden setter forwarding, and inheritance are separately represented with exact source excerpts, line ranges, and SHA-256 in each correction. VideoEnabledWebView forwards the same argument to the framework setter; its instanceof test retains that receiver and does not substitute a different client. These are conditional source fallback paths when PatchProxy does not intercept. Existing Activity host binding evidence is retained; this review does not independently close all Activity/fragment lifecycle bindings.

The 258 other callback rows are unchanged and outside this ledger's complete chain review. Ancillary inspections show concrete setters/factories for direct clients and three real Sina branches, but they are not counted as fully reviewed host facts. There are zero unknown receiver types within the 154 completed rows; 258 rows remain outside complete receiver-chain review. This is not whole-APK coverage. Unknown or unproved bindings must not be promoted to positive based on this document.

Sina WebActivity.java:216–229 has distinct web_type 1/2/3 receivers d/a/c. Their overrides differ: a/d override onPageFinished/onPageStarted and both URL overloads, c inherits the base declarations. Existing a/d parent onPageFinished/onPageStarted facts also have explicit super calls in those concrete override bodies. This audit leaves those rows untouched rather than mechanically replacing their owner. No extra conditional receiver delta was established; the delta file is empty.

Sample identity is a material limitation: samples.json and the source build.json identify ctrip-1, 8.78.0, SHA-256 cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66, whose source APK is named 携程旅行_8.78.0_vuln.apk. Therefore these artifacts cannot be represented as independent proof for a separately named patched Ctrip APK.

Files:

- v19-callback-receiver-corrections.jsonl: complete hash-bound supersession ledger.
- v19-callback-receiver-audit.jsonl: all 412 input callback row identities and review scope.
- v19-callback-receiver-delta.jsonl: empty; no added independent branch facts.
- v19-callback-receiver-summary.json: counts and exact source hashes.
- v19-callback-receiver-reviewed-view/: validated materialization preserving the complete original input and ledger.

scripts/apply_oracle_corrections.py accepted all 154 corrections. Original input SHA-256: 65e8bcb1cd11c21ec50858c5d38064240d692ed3cc02d050395409f4e320d7f8. Ledger SHA-256: 9c3b5de8a831450d08e2a45bb485b0b76bb49ddb00b377f62ea619840fa03513. Reviewed view SHA-256: 837d6cf15db5730d127b068288e80ca667128a569d2586ed0d5dbf2a65dff8a8. These hashes are ready to freeze for this explicitly limited source-semantic correction scope.
