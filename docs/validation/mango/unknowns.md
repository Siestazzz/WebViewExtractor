# Unknown and partial-decompilation ledger

- JADX exited 1 and reported 93 errors. The complete diagnostic stream is retained in `test/decompiled/com.hunantv.imgo.activity.jadx.log`; the numeric exit record is `test/decompiled/com.hunantv.imgo.activity.jadx.exitcode`.
- Constant-backed `registerHandler` names are preserved as source expressions with `binding_status=unknown-constant`. They are not converted into negative or guessed literal names.
- A literal handler whose matching callback-interface declaration cannot be recovered is marked `unknown-signature`.
- Generated data-binding names and obfuscated wrapper types are accepted only when an Activity-to-WebView ownership statement is visible. Their undiscovered settings or callbacks are unknown.
- Ten holdout candidates are intentionally unadjudicated. No missing row for them is a negative assertion.

