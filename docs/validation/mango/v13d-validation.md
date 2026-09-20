# Mango v13d final audit

The fixed report has SHA-256 `af2256e370ecc09f07f3b09f210191cf1989f54b1283e973601c372036e7bdcf`, status `partial`, 87 output hosts, and internal wall time 356.50 seconds (the orchestration measurement was about 357.96 seconds). The semantic delta deliberately excludes `xml_binding_evidence` and `xml_binding_semantics`; those fields are reviewed as provenance metadata. Receiver ID/type, API, implementation, members, arguments, and values remain part of the comparison.

All five disappeared hosts are real regressions, not removal of old all-method XML false positives:

- `com.imgo.vodgames.VodGameRoomActivity`: `handleRoomData()` calls `initializeMiniApp()` (`VodGameRoomActivity.java:886-914`). With non-null room data, it builds the Diana bundle, creates `MiniAppFragment.newInstance`, and attaches it with `replace(R.id.game_parent, ...)` (`:503-553`).
- `com.imgo.vodgames.LandVodGameRoomActivity` inherits that implementation and was previously attributed through the same `handleRoomData` chain.
- `com.mgtv.diana.vodgames.VodGameRoomActivity`: `handleRoomData()` calls `initializeMiniApp()` (`:498-525`); the latter creates and attaches `MiniAppFragment` at `:529-579`.
- `com.mgtv.diana.vodgames.LandVodGameRoomActivity` inherits that implementation and chain.
- `com.mgtv.litevod.LiteVodActivity`: `onNewIntent` flow calls `showMiniApp(intent.getDataString())` (`:370-374`). After URL/app/provider guards, `showMiniApp` creates `MiniAppFragment` and attaches it to `R.id.flFloatContainer` (`:649-693`).

The WebViews are constructed later inside Diana's MiniAppFragment/DianaView render path, so limiting XML seeds to constructors and platform lifecycle must still follow the explicitly attached Fragment and its real `loadUrl`/initialization calls. Each removed host lost 223 facts, including PageWebView and ServiceWebView settings, client callbacks, bridge registrations, and removals. Conditional guards make these candidate capabilities, but do not make the host ownership false.

The same pruning causes a confirmed MGVideoPlayActivity oracle regression: all 81 independently sourced Diana facts are absent—34 settings, 29 callback methods, 8 callback registrations, 8 bridge methods, and 2 bridge registrations. Strict replay falls from v13c's 608/608 settings and 634/634 callbacks to 574/608 and 597/634; bridges fall from 1,147/1,163 to 1,137/1,163. The remaining non-MG misses are the established 14 Loading handlers and two dynamic registrations.

There is broad candidate churn across 52 hosts. Major losses include ChannelSecondIndex -791, MiniPlay -183, VideoClip -189, and multiple Pangle hosts; ChannelBackyard gains 162. Addition/removal counts in `v13d-delta.json` show that several nets conceal wholesale receiver-identity replacement. These changes require object-chain review and cannot be classified from counts alone.

The 87 emitted hosts reuse their v13c ownership evidence. The five absent hosts are retained in the validation document as source-proven conditional regressions; they are not silently relabelled invalid. The final v13d ownership JSONL intentionally contains only the 87 actual output hosts.
