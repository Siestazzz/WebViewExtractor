# Mango v12h validation

The final report was audited only after `docs/validation/v12h-benchmark.json` recorded the completed Mango run. Benchmark status and report status are both `partial`; the run processed all 657 manifest activities, emitted 91 hosts, and took about 227 seconds externally (221 seconds reported internally).

## MGVideoPlayActivity Diana result

v12h does not recover the independently established Diana surface. The development oracle contains 81 facts: 34 settings, two bridge registrations, eight bridge members, eight callback registrations, and 29 callback members. The v12h MG host contains 2,478 total facts, but zero facts mention a `com.mgtv.diana` owner or carry the verified Diana evidence chain.

The evaluator reports 51 MG facts missing and therefore appears to match 30 of the 81. Those 30 are semantic collisions with other WebViews already present under MGVideoPlayActivity: 29 settings plus one callback share platform API/value or member keys. They are not evidence that PageWebView or ServiceWebView was reached. This is a concrete example of the evaluator's documented lack of WebView-object identity validation.

The MG limitations contain `host_context_budget`, `aspectj_unresolved_link`, and two unresolved join-point aliases. The verified path remains:

`showMiniApp → t.run → H1 → G1 → F1 → MiniAppFragment → n1 → DianaView`.

The run stops before emitting `F1`/Diana facts. This is an incomplete result, not evidence that the surface is absent.

## Hosts and ownership

v12h emits all 89 v12a hosts and adds two; none disappear. The previous 84 valid and five uncertain ownership verdicts are reused. Both additions have source-confirmed conditional ownership, producing 86 valid and five uncertain hosts total.

- `ChannelDynamicSecondActivity`: `Q2` constructs and commits `ChannelDynamicSecondFragment`. Its channel refresh/header path conditionally invokes `ChannelSecondFloorHeader.showWebViewFragment`; the verified closure reaches `WebViewFragment.K`, an `ImgoWebView`.
- `CornucopiaActivity`: `X2` assigns `CornucopiaMainPagerFragment`. The pager creates its main fragment, and `CornucopiaBaseFragment` conditionally attaches `WebViewFragment`; the verified closure reaches its `ImgoWebView`.

Both new-host capability sets remain `binding_status=candidate` because runtime content and branch conditions determine whether the WebView fragment is opened. Candidate status does not invalidate Activity ownership.

Complete ownership rows are in `v12h-ownership.jsonl`.

## Capability changes

Total emitted facts rise from 20,582 to 46,669. Thirty-six hosts change their per-kind counts and 55 retained hosts are count-stable. `v12h-validation.json` records every changed host with v12a/v12h totals and added/removed counts for each fact kind.

This 26,087-row expansion is primarily AspectJ-reachable shared WebView surfaces. It is too broad to treat as independently source-confirmed merely because the closure transport is now reachable. Facts retain their report `candidate` status, and the implementation audit's closure/state identity limitations still apply.

Four hosts lose at least one fact kind:

| Host | Removed facts | Added facts | Assessment |
|---|---:|---:|---|
| `XWebActivity` | 85 | 0 | Clear regression; a previously source-confirmed host loses 38 settings, six callbacks, one bridge, 11 bridge removals, three callback removals, and 26 operations. |
| `ErlangLiveActivity` | 136 settings | 354 other facts | Mixed object/path rewrite; setting loss is not validated as pruning. |
| `VideoClipActivity` | six bridge removals | 1,981 | Removal operations changed while the host greatly expanded; unresolved without ordering-sensitive proof. |
| `VodPlayerPageActivity` | 60 bridge/callback/removal facts | 1,889 | Large expansion does not justify the removed registrations/removals; preserve as unresolved regression. |

The two new hosts contribute 437 and 418 candidate facts respectively. The largest expansions occur in scene/live/player hosts and share generated closure paths. Their host ownership is valid, but this review does not certify every expanded object pairing because closure allocation currently omits complete captured-state identity and repeated attachment is unioned.

## Canonical evaluation

Against the 2,508-row canonical, v12h scores:

- Settings: 603/608
- Bridge/member domain: 1,137/1,163, with two unscorable registrations
- Callbacks: 598/634
- WebView operations: 3/3

The 67 residual entries group into Loading/Rainbow 14 members plus one dynamic registration, OPOS one dynamic registration, and MG/Diana 51. The high aggregate recall must not hide MG's zero source-chain recovery or the XWeb regression.

The verdict for v12h is **partial and failed for the new MG/Diana objective**. Preserve this result when comparing v12i; method rotation may address host starvation, but any recovery still needs Diana owner/evidence and correct PageWebView/ServiceWebView identity.
