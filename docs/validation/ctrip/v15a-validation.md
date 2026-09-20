# v15a Ctrip validation

v15a processed all 410 manifest Activities in 188.285 seconds and emitted the same 55-host set as v14c. The APK hash is unchanged.

## Full delta

Twenty-seven Activity records differ. Eleven have semantic additions and sixteen differ only in representation or ordering. There are 85 additions: 84 `webview_operation` rows and one bridge row. There are zero semantic removals. `unattributed`, index diagnostics, and manifest diagnostics are exactly equal.

The additions split into the five independently reviewed Evaluate facts and eight inherited load operations for each of ten source-confirmed H5v2 hosts. Their source and object-identity assessment is recorded in `v15a-source-review.md`. No deletion requires a regression exception because the semantic removal set is empty.

## Ownership

All 55 output hosts were replayed through the prior source ownership audit. The set remains 54 valid and one wrong. The known wrong host is `CtripCommonFeedBackActivity`: its gallery path supplies an empty `bottomWebViewUrl`, so the constructor branch is not reached. This gives a conservative output ownership precision of 54/55 and a wrong-host upper bound of one in this audited set.

Evaluate retains the same XML object identity, `activity:ctrip.business.evaluation.EvaluateDialogActivity/view:2131313182`, for its original H5 operations, the recovered superclass operations, `_VideoEnabledWebView`, and the embedded `notifyVideoEnd()` member. The callback captures `H5WebView.this`; no receiver substitution occurs across inherited dispatch.

The four established object samples remain stable: old H5 `H5Container`, `MyCtripOrderModalActivity`, and CMB have no semantic delta. `MktH5ContainerV2` gains eight inherited operations on the existing cached/fresh acquisition alternatives; this does not resolve its exact-allocation alias ambiguity.

## Current canonical replay

Against canonical SHA-256 `83a206c80cd7d3ce23c386550c8c928b4123c2efa5382fb843a731a45ea4ebbf`:

- settings: 323/323;
- bridges and bridge members: 3,682/3,682;
- callbacks: 391/391;
- WebView operations: 2/2;
- positive hosts: 30/30;
- missing facts: 0.

Acceptance remains `unproven`: all matched rows have `explicit_matched=0`, so this replay measures activity/type-constrained recall rather than exact allocation identity. The known unregistered near-callback negative fixture remains failing; v15a must not be described as eliminating all listener-neighborhood false positives.

Artifacts:

- `v15a-delta.json`
- `v15a-ownership.jsonl`
- `v15a-current-canonical-scoring.json`
- `v15a-source-review.md`
- `v15a-evaluate-callback-audit.md`
