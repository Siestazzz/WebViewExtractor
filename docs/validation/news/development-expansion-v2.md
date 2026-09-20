# Public development-positive scope audit v2

This audit keeps the final holdout set sealed. No holdout identity or capability record was opened or copied into the public corpus.

## Counts

The previous canonical corpus contained 1,838 capability facts for 23 Activities. All 23 were deeply expanded: registrations/methods or message handlers, settings, and installed client callbacks were materialized rather than represented only by an ownership verdict.

The v4 ownership audits contained additional source-valid Activities, but ownership review alone is not a capability inventory. Those rows were therefore not counted as deeply expanded. Most of that difference belongs to the sealed set and was not reused here.

Development v2 adds five independently bound, non-holdout Activities and expands their shared implementations into the flat canonical facts. The resulting public corpus contains 1,931 facts for 28 Activities. The versioned snapshots are `canonical-facts-development-v2.jsonl` and `groups-development-v2.jsonl`; the live canonical files contain the same generated data.

## Added bindings

- `VerticalVideoVideoActivity`: the concrete short-video card/controller owns an `n2` controller; the player-manager YSP branch creates `YspMediaPlayer`, whose `mo157615(Context, ViewGroup)` creates/configures `BaseWebView`, installs `com.tencent.news.ysp.f`, and registers `YspJSBridge`.
- `FullPlayVideoActivity`: `FullVideoPlayer.m117080()` supplies the concrete `n2` used by the Activity's player path; the same conditional `VideoPlayManager` YSP branch reaches `YspMediaPlayer`.
- `LongVideoDetailActivity` and `TvLongVideoDetailActivity`: both are manifest Activities extending `AbsLongVideoDetailActivity`; the common detail-player composition supplies the concrete video controller and manager used by the conditional YSP player path.
- `AdBonusPageActivity`: this manifest Activity extends `BaseAdEasterEggActivity`, the same concrete gesture/easter-egg host family. Its controller factory creates `EasterEggWebView`; binding the `InteractiveEastEggController` installs the WebView client and `_interactBridge`.

Each shared capability is expanded per Activity by `build_groups.py`; the Activity binding appears in every flat fact's evidence chain.

## Current acceptance status

The non-holdout manifest/source inventory was rechecked across direct WebView calls, known WebView Activity bases, custom-view layouts, player/manager factories, and the unowned entrypoint inventory. The public development set currently has 28 deeply expanded positive Activities. This does **not** establish the plan's requirement of at least 30 positive Activities, and it does not establish that the APK has fewer than 30 positives. Router-only launchers, SDK plugin components without a matching DEX implementation, and context-only views without a unique Activity owner remain excluded from the public count.

Acceptance remains pending until the final holdout is opened and merged into the audit. That final audit must confirm at least 30 deeply verified positive Activities or add further source-proven positives. The sealed set is unchanged and absent from these artifacts.

`QADetailPage` remains an explicit unresolved capability carrier. Its settings, bridge registrations, and client callbacks are retained in `groups.jsonl`, while `unknowns.jsonl` records `activity: null` because no unique Activity owner has yet been proven. It is not counted as an Activity positive.

These five additions are development/non-blind samples: their identities had appeared in prior extractor output or development investigation. They must not be scored as final unseen samples.
