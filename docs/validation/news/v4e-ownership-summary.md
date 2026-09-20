# Tencent News v4e ownership review

The 30 emitted Activities contain **27 valid, 3 wrong, 0 uncertain** ownership candidates. Counting wrong plus uncertain candidates, the conservative ownership-error upper bound is **3/30 = 10%**. Verdicts were rechecked against the v4e evidence and retain the v4d source rationale. The wrong candidates remain `MobileQQActivity`, `QzoneShareActivity`, and `HippyDetailActivity`: SDK-dialog or business-call reachability still does not bind their emitted WebViews to the Activity.

Compared with v4d, six source-valid Activities disappeared from output: Ad landing, VisitVerticalVideo, RoseLiveVideo, WeiboGraphicDetail, FullPlayVideo, and VideoPreview. Their removal is an ownership recall regression rather than a precision improvement. No new Activity was added.

Total emitted facts fell from 18,030 to 12,116. Several noisy hosts improved numerically (`WebNovelActivity` 1,027→482, Custom browser 1,356→937, WebDetail 1,253→620), while the two detail Activities grew from roughly 1,420 to 2,048 facts each. Large sibling/union surfaces therefore remain. `SecurityTicketActivity`, privacy, and Support remain narrowly scoped.

The former medal-dialog constructor mismatch is fixed. The surviving Custom-browser Tencent-video receiver is source-valid on the `isTencentVideoDomain()` layout branch, as detailed in `v4e-crossbinding-conclusion.md`. Fifteen unknown-union duplicates still require per-alternative filtering so the subclass initializer is not applied to the normal BaseWebView branch.
