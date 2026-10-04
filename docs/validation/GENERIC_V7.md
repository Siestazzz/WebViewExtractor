# Generic v7: bounded-time field binding recovery

v7 repairs repeated shared-graph expansion and lets Ctrip continue to a normal partial-report shutdown. It does not satisfy ten-App acceptance. Six Apps still approach600s; News and Mango become slower. Next-version field indexing and numeric IF precision work are not part of this frozen implementation.

## Fixed regression oracle

Same generic-v3 facts, evaluated with scoring version6. String values now retain exact case/whitespace; see GENERIC_V7_SCORING.md. Old score artifacts are not overwritten.

| App | Seconds | Exit | Bridge | Settings | Callback |
|---|---:|---:|---:|---:|---:|
| news | 259.7 | 0 | 1260/1393 | 228/366 | 185/240 |
| mango | 245.2 | 0 | 1215/1259 | 565/608 | 587/662 |
| ctrip | 592.1 | 0 | 3680/3970 | 343/391 | 397/444 |
| xigua | 591.5 | 0 | Not audited | 0/35 | 0/20 |
| freereels | 592.3 | 0 | 7/7 | 15/15 | 4/4 |
| yangshipin | 37.4 | 0 | 10/10 | 40/40 | Not audited |
| txws | 51.6 | 0 | 8/8 | 175/175 | 160/160 |
| sohuvideo | 593.0 | 0 | 7/115 | 88/88 | 17/99 |
| fanqiexiaoshuo | 592.1 | 0 | 12/12 | 21/21 | 12/16 |
| breaking-news | 591.0 | 0 | 10/10 | 14/14 | 8/8 |

## Expanded development oracle

Append-only union, not a holdout set. Both v6 and v7 reports have been replayed against this same oracle and scorer in generic-v6/7-*-expanded-v7-score.json. Source audit continues after this freeze; declared-but-unforwarded or wrongly shared facts are retained with separate source corrections, not silently erased.

| App | Bridge | Settings | Callback |
|---|---:|---:|---:|
| news | 1260/1393 | 228/366 | 185/240 |
| mango | 1215/1259 | 565/608 | 587/662 |
| ctrip | 3680/3970 | 343/391 | 397/444 |
| xigua | 30/65 | 193/278 | 7/150 |
| freereels | 7/15 | 70/112 | 16/19 |
| yangshipin | 17/17 | 161/201 | 296/312 |
| txws | 8/8 | 175/175 | 170/170 |
| sohuvideo | 411/663 | 152/488 | 540/1514 |
| fanqiexiaoshuo | 12/12 | 21/21 | 12/16 |
| breaking-news | 10/10 | 14/14 | 8/8 |

## Mechanism and tests

See GENERIC_V7_REFRESH_MEMO.md: invocation-local memo includes object identity, remaining depth and relevant cycle history; cached results preserve seen-set effects. Budget expiry retains unresolved input with a diagnostic. Analysis budgets remain unchanged. Diamond regression reduces199288 recursive calls to56 state expansions, and80 deterministic cyclic graphs match the prior result and seen effects. Full capabilitySelfTest, compactReportTest and shadowJar passed in10s. Scorer13 tests pass; three-report atomic deadline smoke check and ten-App compact/counts consistency checks pass.

## Findings and open work

- Ctrip fixed-gold Bridge rises2123→3680, settings295→343, callbacks334→397 compared with v6. It remains below95% in every category and at592.1s; normal exit does not mean traversal/quality completion.
- News124.1→259.7s and Mango204.6→245.2s expose additional memo-dependency cost. News index time and decoded/refined/context counts stay unchanged; extra analysis time concentrates in NewsDetail/PushDetail. Lazy per-host field indexes are being prepared for the next version, with no speedup claim yet.
- Expanded Sohu facts reveal a major regression hidden by the old small set: v7 settings152/488, bridge411/663, callbacks540/1514. Host ownership passing does not imply retained capability coverage. Per-closure replay and profiling are required; added runtime cost is a hypothesis, not yet a proved sole cause.
- Source-confirmed false constructor-flag Bridge binding is reproduced generically; its numeric IF refinement repair is next-version work.
- Reflection/prompt/console registration, scene/framework routing, X5 extension callbacks, source deep-audit scope, all-output precision and fresh holdouts remain open.
- Independently rebound v7 ownership: Txws15/15 valid; Sohu273 valid/26 unresolved out of299 (8.70% conservative upper bound). Six previously emitted Sohu hosts disappear, including five source-positive hosts; disappearance is not a negative source conclusion. Capability precision and other audit gates remain unproven.
- No three isolated final performance repetitions have been qualified. Current parallel development timings are not final acceptance measurements. Strict combined acceptance remains0/10.

## Reproduction

Frozen jar SHA256: `79b474e4e1934b24c5129b637ede6b1c3f3365e05eb2b7159066d9ce660b7661`. Eight disjoint logical CPUs and16GiB per App.

```sh
./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain
python3 scripts/test_evaluate.py
taskset -c 32-111 python3 scripts/run_parallel.py --samples docs/validation/generic-ten-samples.json --jar build/libs/webview_extractor-1.0-SNAPSHOT-all.jar --out test/runs/generic-v7-reproduce
python3 scripts/check_compact.py test/runs/generic-v7
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/generic-v7-spec.json --reports test/runs/generic-v7 --out docs/validation/acceptance/generic-v7-status.json
```
