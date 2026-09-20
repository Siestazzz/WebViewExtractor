# Tencent News v12j validation

The frozen v12j report is `partial`, takes 190.71 seconds, and emits the same 44 Activities as v12i. It has 6,333 raw facts versus 5,677 in v12i. Using a semantic key that retains kind, site, API, value, registration, implementation, member signatures, and receiver type, v12j adds 227 tuples across 15 hosts and removes none. The complete delta, including every added tuple's site and receiver type, is in `v12j-delta.json`.

The full ownership audit remains 43 valid hosts and one uncertain host, `ShellActivity`. No Activity was added or removed. The verdict means the Activity owns at least one real WebView path; it does not certify every emitted fact. Existing concrete ownership evidence is reused in `v12j-ownership.jsonl`, with current fact counts and an explicit fact-level audit reference.

Removing the path-length limit did not restore the previously over-attributed capture or feedback components. No v12j semantic addition has a `CaptureDetailPage` or `VideoErrorFeedbackDialog` site. The source-confirmed break remains: the concrete `TLVideoCompleteView` listeners pass selector 3 or 4 to `k2`, while capture and feedback require cases 107 and 1021. Those 288 development facts remain a separate pending set.

The new Shell rows are YSP/DtX5 inherited operations and bridge removals. They do not resolve Shell ownership: the longer evidence path still does not prove the relevant Shell lifecycle executes with the concrete player object. They remain pending.

The KK detail hosts, `VisitVerticalVideoActivity`, Weibo detail, and Read24Hours additions are consistent with their already source-owned YSP or pendant receivers. Most added rows expose inherited X5/DtX5 methods or constructor removals. Receiver labels such as `BaseWebView` and `DtX5WebView` are superclass aliases of the same concrete receiver and should not be counted as separate WebViews.

The login additions are source-valid conditional dialog capabilities. The Activity reaches Tencent/QQAuth with itself; `AuthDialog.d()` creates and configures its `com.tencent.open.d.d` WebView, installs both clients, and loads the same field. `LoginWithBackgroundActivity` regains the full group of 17 settings, two clients, six removals, and seven operations. The smaller sibling deltas are methods on the same dialog receiver.

`NewsDetailActivity` and `PushDetailActivity` are mixed. Their WebViewForCell and detail page operations have real source entry paths, but nine new tuples retain an unknown receiver and three YSP superclass operations do not gain a new source binding merely because the report path is longer. These rows remain unresolved. UserHome/UserSearch similarly contain valid inherited YSP operations alongside duplicate superclass aliases and mutually exclusive Android/X5 helper branches; host ownership is valid, but every emitted alias is not an independent capability.

Neither long-video Activity is emitted. v12j therefore leaves that known recall gap unchanged.
