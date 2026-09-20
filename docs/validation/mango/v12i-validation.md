# Mango v12i incremental validation

The report was read only after `docs/validation/v12i-benchmark.json` recorded Mango completion. It remains `partial`, processed all 657 manifest activities, emitted the same 91 hosts as v12h, and took about 237 seconds externally.

## MGVideoPlayActivity Diana recovery

v12i recovers the independently verified Diana chain. Report evidence explicitly contains:

`MGVideoPlayActivity → showMiniApp → LibTryCatchRuntimeAspect.excuteWithTryCatch → proceed → t.run → H1 → G1 → F1 → MiniAppFragment → DianaView`.

The report has 130 facts whose concrete WebView type is one of the two source-proven types: 75 on `PageWebView` and 55 on `ServiceWebView`. Of these, 124 carry the full `H1 → G1 → F1` evidence sequence. The remaining facts are reached through later Diana lifecycle paths; their concrete types and owners remain Diana-specific.

After adding `webview_constraint.types` to all 81 development facts, evaluation reports no missing MG fact:

- 34/34 settings
- 2/2 `JSCore` registrations
- 8/8 annotated JSInterface members
- 8/8 callback registrations
- 29/29 callback members

This is a real recovery rather than the v12h semantic collision: v12i facts identify `com.mgtv.diana.sdk.page.view.PageWebView` or `com.mgtv.diana.sdk.service.view.ServiceWebView` and carry the verified host chain. The constraints prove type compatibility only. Runtime object identity was assessed from the source chain and report evidence; the evaluator still does not establish identity by itself.

The report emits extra Diana alternatives and duplicates, including several ServiceWebView provenance forms (`constructor_parameter`, `entry_parameter`, and a view-derived id), unknown registration alternatives, LocalStorage, removal operations, and WebView operations outside the 81-fact oracle. They remain candidate output and were not added to source truth.

`host_context_budget` is still present for MGVideoPlayActivity. The known Diana surface is recovered, but the entire 1,694-fact host must not be described as exhaustively analyzed.

## Hosts and ownership

The v12h and v12i host sets are identical: 91 hosts, with 86 independently valid and five uncertain ownership verdicts. No host was added or removed. `v12i-ownership.jsonl` reuses those source/DEX verdicts and updates fact counts.

## Other capability changes

Total facts fall from 46,669 to 38,800. Sixteen hosts change count and 75 remain count-stable. `v12i-validation.json` lists every changed host and all added/removed fact-kind counts.

Most large reductions are `message_bridge` facts from the broad v12h expansion: ErlangLive −2,859, VideoClip −1,545, VodPlayerPage −1,553, MGVideoPlay −1,216, VideoHallGallery −941, MeetSceneLive −801, and InteractVod −771. These are not canonical regressions in v12i's scored result, but their removal is not independently proven endpoint-by-endpoint here. They should be treated as v12h/v12i candidate-set instability rather than confirmed pruning.

Smaller removals remain in ChannelBackyard, ChannelDynamicSecond, ChannelSecondIndex, VodPlayerPage operations, and several callback/settings groups. The earlier XWebActivity regression persists unchanged at 237 facts; v12i does not restore the 85 facts lost between v12a and v12h.

## Canonical evaluation

With type-constrained MG facts, v12i scores:

- Settings: 608/608
- Callbacks: 634/634
- Bridge/member domain: 1,147/1,163, with two unscorable registrations
- WebView operations: 3/3

The remaining 16 items are the known Loading/Rainbow 14 message handlers plus one dynamic Loading registration and one dynamic OPOS registration. Full setting/callback recall does not remove the report's partial status, ownership uncertainty for five hosts, candidate alternative risk, or host budget diagnostics.

v12i therefore passes the specific MG/Diana recovery objective and preserves the full host set. It does not establish global completeness for every emitted candidate fact.
