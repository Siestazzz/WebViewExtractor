# Tencent News v15a source validation

Scope: Tencent News 7.9.50, APK SHA-256 `c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941`. The frozen report is `test/runs/v15a/com.tencent.news/0/output/capabilities.json`. This is a development, non-blind comparison; sealed holdouts were not read.

## Result

v15a emits 42 Activities and 5,386 facts, exactly the same host set and normalized semantic facts as v14c. The delta has zero additions and zero removals. The normalized comparison excludes evidence and binding presentation fields but retains capability kind, API/member, implementation, registration name, values, and WebView identity. `v15a-delta.json` records the complete empty delta.

Ownership therefore remains 41 valid and one uncertain (`ShellActivity`), with a conservative non-valid upper bound of 1/42 (2.38%). `v15a-ownership.jsonl` contains all 42 rows and explicitly cites the zero delta before reusing each v14c source chain.

## Focused source checks

No new News host appears. The existing advertising SDK Activities and their fact sets are unchanged, so the bounded callback-argument rule introduced no new advertising ownership or capability association in this APK.

`LongVideoDetailActivity` and `TvLongVideoDetailActivity` remain absent. The new callback-argument relevance does not bridge the previously documented Activity/page/service/player event chain, and this report supplies no new object identity evidence for either host.

The six v14c-changed hosts are also unchanged in v15a. Privacy's two conditional DtX5 bridge registrations remain source-confirmed on the same XML `X5WrapperWebView`; Support's late XML consumer facts remain bound to its `BaseWebView`; NewsDetail and PushDetail retain the already documented collection/PatchRedirector receiver aliases. No canonical source conclusion changes in this version.

## Known negative failure

The synthetic near-callback negative still emits two bridge objects when only one callback is registered. `docs/validation/v15-near-callback-known-failure.json` shows the same failure in v14c and v15a. This makes it a pre-existing constructor-seeding defect rather than a regression caused by the new callback-argument rule. It remains open: construction near an API is still sufficient to spuriously execute an unregistered second callback in that fixture. Accordingly, this validation does not claim that v15a excludes every unregistered callback.

The report status is `partial`, consistent with prior frozen News runs; the output itself is final. No canonical files or production code were changed.
