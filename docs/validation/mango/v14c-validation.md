# Mango v14c final validation

The fixed report has SHA-256 `1525015ec8d0ead4f278f8a39e814faf15f97e396eead689013e740460c808d5`, status `partial`, 87 output activities, 41,833 facts, and internal wall time 359.941 seconds (benchmark 361.476 seconds). Its activity set is identical to v13e. The 87-row ownership file therefore reuses the prior independent source/DEX verdicts; there are no new ownership claims.

Compared with v13e, five hosts change semantically after excluding `xml_binding_evidence` and `xml_binding_semantics`: `MangoMiniAppActivity` +26 (39 additions, 13 removals), `ErlangLiveActivity` -22, `VideoClipActivity` -1, `VodPlayerPageActivity` -67, and `MGVideoPlayActivity` -10. MangoMiniApp's growth is chiefly PageWebView identity refinement and alias expansion. The other four have no additions. The VodPlayer removals are predominantly an ImgoWebView candidate path, while MGVideoPlay loses ten Imgo `registerHandler` rows; neither loss restores or explains the Diana surface.

Strict replay of the current constrained canonical set yields settings 574/608, callbacks 597/634, bridges 1137/1163 with two unscorable registrations, and operations 3/3. The 97 missing rows are exactly 81 MGVideoPlay Diana facts, 14 Loading/Rainbow members, and the two retained dynamic registrations (Loading and OPOS). The Diana 81 split is 34 settings, 29 callback members, eight callback registrations, eight bridge members, and two bridge registrations.

The Fragment root/object repair is confirmed, but the full capability chain is not. The remaining earliest verified boundary is the actual OkHttp callback registration and posted success runnable described in `v14c-source-audit.md`. Consequently the five v13c-valid conditional Diana hosts remain absent and must not be treated as negative source evidence.

Artifacts: `v14c-delta.json`, `v14c-ownership.jsonl`, `v14c-source-audit.{md,json}`, and reproducible `audit_v14c.py`.
