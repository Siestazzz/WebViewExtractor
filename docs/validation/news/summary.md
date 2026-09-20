# Tencent News validation summary

- Package/version: `com.tencent.news` 7.9.50.
- APK SHA-256: `c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`, matching `docs/validation/samples.json`.
- JADX: requested command used with `--show-bad-code -j 4`; completed at 100%, exit 1, 164 reported errors. See `test/decompiled/com.tencent.news.jadx.log` and `.jadx.exit`.
- Independence: capability construction was completed from decompiled source and manifest before extractor output was inspected; the later v1 ownership audit is separately documented.
- Published ground truth: the original `facts.jsonl` is preserved. `groups.jsonl` contains reusable shared capability groups and separate Activity bindings; `facts_expanded.jsonl` is the normalized flat export. Bridge registrations and exposed methods are separate facts, settings have `normalized_api`, and callbacks have DEX `normalized_signature`.
- Holdout: ten independently verified Activity bindings are undisclosed; the final five came from a separate manifest/base-class probe and were not used to guide repairs.
- `TencentNewsJsBridge` exposes only `BridgeInterface.bridgeCall(String)` directly. H5 API endpoints are recorded as `message_handler`: `bridgeCall` forwards a JSON payload to `JavascriptBridge.call`, selects the public endpoint from JSON `method` plus `types`, resolves it with `interfaceObj.getClass().getMethod`, and invokes it on `interfaceObj`. Each routed fact records the transport, router, resolver, selector and endpoint descriptors.
- The concrete object-binding chain and the distinction between reflected receiver methods and adapter/manager methods are documented in `message-routing.md`.
- Shared `BaseWebView`, `BaseSysWebView`, and `NewsWebView` settings have been added for all applicable published Activities; see `shared-settings-audit.md`.
- Published development positive-Activity count: 23 after the YSP manager, AdCore/Splash, and Midas branches were independently expanded. Ten further verified Activities remain reserved as holdout, giving at least 33 source-confirmed positives without disclosing holdout identities. The remaining manifest and unowned-entrypoint candidates were exhaustively audited; exclusions and unresolved ownership are documented in `development-expansion-ad-sdk.md` and `unknowns.jsonl`.
