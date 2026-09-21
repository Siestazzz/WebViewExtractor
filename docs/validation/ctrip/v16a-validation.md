# v16a Ctrip validation

v16a processed all 410 manifest Activities in 181.782 seconds and emitted 52 hosts, down from 55 in v15a.

## Delta and deletion assessment

Thirty-four Activity records differ. There are no semantic additions and 214 semantic removals: 129 WebView operations, 64 settings, 12 bridge removals, eight callback registrations, and one bridge. The three disappearing hosts are `QQSSOEntryActivity`, `QQEntryActivity`, and `CTTourSearchActivity2`.

The two QQ hosts are true conditional WebView hosts and their loss is a regression through an actually executed executor/Runnable/UI-thread chain. `CTTourSearchActivity2` is also a real conditional H5 Fragment host, although its four old reported tuples came from an invalid uninitialized-plugin receiver chain and are correctly deleted. Details and exact DEX entry signatures are in `v16a-source-review.md`.

The majority of the other operation deletions also have real scheduling or callback edges (`ThreadUtils.runOnUiThread`, `ThreadUtils.post`, registered SDK callbacks). They cannot be called precision improvements merely because construction-only seeding was removed. One known exception is `CtripCommonFeedBackActivity`, whose gallery WebView branch is source-infeasible for its empty URL.

`unattributed` changes from 131 to 143; index and manifest diagnostics are exactly unchanged. The report file SHA-256 is `5eb3656c817f1b5d5f7b90b89d5f20b543da9af79a78a71b28f7bc2a5112b9fa`.

## Ownership

The 52 emitted hosts retain 51 valid and one wrong source decision. The wrong host remains `CtripCommonFeedBackActivity`. The 52-row ownership JSONL records every emitted host; disappeared hosts are reviewed separately rather than silently omitted from the audit conclusion.

## Current canonical replay

Against canonical SHA-256 `83a206c80cd7d3ce23c386550c8c928b4123c2efa5382fb843a731a45ea4ebbf`, v16a still reports:

- settings 323/323;
- bridges and bridge members 3,682/3,682;
- callbacks 391/391;
- WebView operations 2/2;
- positive hosts 30/30;
- missing canonical facts 0.

This does not clear the regressions above because the canonical set does not enumerate the two QQ hosts, the real SearchH5Fragment2 host path, or most helper operations. Acceptance remains `unproven`, with no explicit allocation matches.

Artifacts: `v16a-delta.json`, `v16a-ownership.jsonl`, `v16a-current-canonical-scoring.json`, and `v16a-source-review.md`.
