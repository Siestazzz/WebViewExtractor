# Scoring v2 independent verification

Scope: source review of `scripts/evaluate.py` plus temporary synthetic report/oracle execution. No holdout data was read. The evaluator, production code, and canonical facts were not modified.

## Result

The four requested core cases now behave conservatively:

| case | synthetic expectation | observed v2 result |
|---|---|---|
| duplicate identical oracle setting row | one semantic expectation | `duplicate_oracle_rows=1`; setting expected 2 for three source rows comprising one duplicate Android fact plus one distinct X5 fact |
| unknown target, known registration candidate | registration presence may be shown, aggregate coverage must miss | aggregate bridge matched 0; `unscorable_oracle`; registration submetric matched 1 |
| unknown target, no Activity/candidate | stable conservative miss | aggregate bridge matched 0; `unscorable_oracle` even with an empty candidate list |
| same setting method on a different owner | must not match | Android report did not match X5 normalized API; setting recall was 1/2 |
| endpoint with wrong member signature | must not match | bridge-member recall 0/1 and `not_matched` |

The output also sets `scoring_version=2`, separates `bridge_registration` and `bridge_member`, lists unknown surfaces, and explicitly reports `binding_identity_verified=false`. These changes fix the main inflation paths identified in `scoring-audit.md`. Unknown known-name registrations remain visible in registration recall but cannot increase aggregate matched coverage, which is the intended distinction.

The synthetic fixture used one Activity with an Android setting, a `foo` registration whose member was deliberately wrong, and one absent Activity with a dynamic unknown registration. Its result was: setting expected 2/matched 1; bridge expected 3/matched 0/unscorable 2; bridge registration expected 2/matched 1; bridge member expected 1/matched 0.

## Remaining correctness bug

A normalized bridge endpoint can still be incorrectly treated as a registration-only success when its raw `signature` field is absent or blank.

`match()` stores `normalized_signature` in the local `signature`, but line 33 decides whether a `kind=bridge` row requires member matching using `g.get('signature')` rather than that normalized value:

```python
if k in ('bridge_method','message_handler') or g.get('signature'):
```

The granularity classifier correctly calls the same row `bridge_member` because it checks `normalized_signature`. This creates an internal contradiction: the member submetric can record a match without testing any member.

A second synthetic oracle contained:

```json
{"kind":"bridge","registration_name":"foo","normalized_signature":"Lpkg/T;->call()V","implementation":"pkg.T"}
```

with no raw `signature`. The report registered `foo` with the correct implementation but exposed only `Lpkg/T;->wrong()V`. V2 incorrectly returned aggregate bridge recall 1.0 and bridge-member recall 1.0.

The current Mango canonical file has zero rows in this exact shape, so this bug does not change the present Mango replay. It is a reusable evaluator bug and can affect another oracle or a future normalization pass. The condition should use the normalized local `signature` (or explicitly require member matching whenever `normalized_signature` is nonempty), matching the granularity rule.

## Limits that remain explicit rather than fixed

V2 still matches within an Activity and does not validate concrete WebView identity; the output now states this accurately. Activity bindings remain outside recall. Ownership remains optional and Activity-level. These are disclosed limitations, not silent claims in v2.

The evaluator also still creates the Activity map with a dictionary comprehension, so duplicate Activity records in a report overwrite earlier records. Rejecting duplicate Activity keys would make malformed reports fail closed.
