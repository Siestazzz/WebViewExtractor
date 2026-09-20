# Mango TV independent source validation

- APK: `test/apks/com.hunantv.imgo.activity.apk`, version 9.3.0, SHA-256 `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5` (matches `docs/validation/samples.json`).
- Decompiler: JADX 1.5.1, `--show-bad-code -j 4`; exit code 1 with 93 reported errors. Log: `test/decompiled/com.hunantv.imgo.activity.jadx.log`; exit record: `test/decompiled/com.hunantv.imgo.activity.jadx.exitcode`.
- Independence: this inventory was made from the APK, manifest, and JADX sources only. Extractor output was not inspected.
- Disclosed set: 30 positive Activity bindings across direct, inherited, fragment, wrapper and third-party layers. Framework manager/factory/message-bridge layers are separately mapped.
- Holdout: 10 reserved identifiers have no capability rows in `facts.jsonl`.
- Facts: 1480 JSONL rows. Each row records one Activity capability or its verified WebView binding, with source ownership chain, status, and APK hash.

Unknown means unknown. JADX failures, obfuscated constants, unresolved generated bindings, and incomplete inner-class bodies are recorded as `partial-decompilation` or reserved for holdout; none is counted as a negative.
