# Mango v15a final validation

The final report has SHA-256 `d0aaac0c902f945bbb5ed816aa3d26675c0df5117ba27894bd43bd915465d89e`, status `partial`, 657/657 processed activities, 87 output hosts, and 41,622 facts. Its internal report time is 375.342 seconds. The output host set is identical to v14c, so the 87 ownership verdicts are reused with updated fact counts.

Using the same semantic comparison fields as v14c and excluding XML evidence metadata, four hosts change: ErlangLive 2215→2021, InteractVod 1202→1201, MGVideoPlay 1732→1724, and VideoSquare 1556→1548. ErlangLive contains 306 added and 500 removed receiver-specific rows caused chiefly by union identity consolidation; the remaining three hosts only lose rows. No source review supports calling this a newly recovered capability.

Current constrained canonical replay is unchanged: settings 574/608, callbacks 597/634, bridges 1137/1163 with two unscorable dynamic registrations, and operations 3/3. The same 97 rows remain missing, including all 81 MGVideoPlay Diana rows and the same five absent conditional hosts.

The synthetic near-callback negative still fails: v15a observes two bridge objects instead of the sole registered object. This is an open precision failure and prevents a general claim that unregistered callbacks are excluded.

Artifacts are `v15a-delta.json`, `v15a-ownership.jsonl`, `v15a-source-audit.{md,json}`, and `audit_v15a.py`.
