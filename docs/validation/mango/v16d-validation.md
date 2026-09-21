# Mango v16d validation

The final report SHA-256 is `bc2b1d57df81055745e65a2d16480e188ca32e481aead5405fe6c7e1fe5b7b48`. It is `partial`, contains 90 hosts and 41,663 facts, and reports 364.569 seconds (benchmark 366.147 seconds). The host set is identical to v16a, so all 90 ownership verdicts are reused. The two DTF FaceLoading hosts remain absent.

Against current canonical SHA-256 `0a39821962101aacaac95e0b54557a1594dba39898377354e24cd8b8978eff47`, v16d scores settings 608/608, bridges 1153/1259 with two unscorable, callbacks 634/662, and operations 3/3. There are 134 missing facts.

Only 6 of the 120 cumulative v16 regression facts recover: registration and endpoint pairs for Erlang `openSystemSetting`, `sendLocalPixelBarrage`, and `showInteractiveMagicCube`. Pangle remains 0/18, mgadplus 0/72, DTF 0/20, and the two constant-backed Erlang names remain 0/4. The earlier Erlang `mgTransfer` and `showMangPushShare` four facts also remain missing. Thus actual interface-call relevance improves one Erlang subset but does not cross the independently documented mgadplus registry callback or DTF network callback entries.

There are 14 v16a→v16d changed hosts and no ownership changes. Large LiveRoom growth is retained as a candidate expansion; other row changes are receiver/path churn or removals and do not alter the cumulative source verdict above. Full receiver-sensitive differences are in `v16d-delta.json`.
