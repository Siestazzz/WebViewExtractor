# Tencent News v1 ownership audit

All 23 emitted Activities were reviewed against decompiled source. Verdicts: 21 valid, 0 proven wrong, and 2 uncertain. The two uncertain rows are the QQ/Qzone share Activities: they enter Tencent's SDK, while the emitted WebView facts come from shared SDK dialog/helper classes and the recovered call path does not prove those share flows instantiate the dialogs.

`global_setting` and `bridge_removal` were not treated as sufficient evidence by themselves. Every valid Activity has an independent owner path through a direct field, inheritance, controller/manager, or a concrete wrapper instance. QAEditorActivity and RichEditorActivity are valid owners but their output is highly incomplete: two shared bridge facts omit their editor WebView settings and client callbacks.

The highest-yield missing-capability improvements are:

1. Resolve the classic news-detail registry/manager chain (`NewsDetailActivity`/push detail → content manager → `viewmanager.u`) so its four registered interfaces, settings, and both clients are attributed once to each concrete owner.
2. Join router Activity and Fragment targets sharing the same route, especially `/visitor_mode/detail`, then propagate the Fragment's WebView, two bridges, settings, and clients to the Activity with explicit route evidence.
3. Follow layout/service-created page components (`QADetailPage`, `RelateWebViewContainer`, `AudioPlayerWebViewContainer`, `CaptureDetailPage`) back through XML inflation, services and shell Activities instead of requiring an Activity-local call.
4. Model wrapper registrations as reusable groups keyed by the concrete WebView instance, then deduplicate inherited X5/DT and OKWeb facts. This retains SDK bridges while preventing the repeated facts visible in Qzone and the dark-mode Activities.
5. Require a concrete constructor/field/return-value path before assigning shared SDK dialog facts to an Activity. Keep global settings and removal-only paths as supporting evidence, not Activity-positive evidence.

Holdout remains five distinct verified Activities. The requested ten-Activity holdout is still short by five; no candidate or uncertain Activity was promoted to fill it.
