# Ten-App acceptance ledger

The active goal now requires all ten Apps to meet the original numerical gates while retaining the ban on private App adapters. Previous permission to tolerate lower quality must not be interpreted as acceptance of the current outputs. Latest baseline: generic v2, with zero Apps proven to satisfy all gates.

Run from the repository root:

```sh
python3 scripts/acceptance_status.py --spec docs/validation/acceptance/spec.json --reports test/runs/generic-v2 --out docs/validation/acceptance/generic-v2-status.json
python3 scripts/test_acceptance_status.py
```

Exit 2 means not accepted, not an unexpected script failure. The ledger preserves each requirement separately. It checks report/APK/oracle identity before using recall, and never treats an empty capability category as 100% recall. Missing source inventories, ownership reviews, holdouts or repeat measurements remain unproven.

## Evidence contracts

Paths in spec.json are relative to this directory. Each App provides:

- `score` and `oracle`: unchanged evaluator output and the matching versioned expected facts. Both report hash and oracle hash must match.
- `ownership`: JSONL with `scope: activity_host`, `activity`, `apk_sha256`, exact `report_sha256`, `verdict: valid|wrong|unresolved`, and nonempty `evidence`. A valid host has a source-supported WebView association. A wrong sampled Bridge on a host is NOT a verdict that the whole Activity lacks a valid WebView; sampled-capability reviews are inapplicable to this gate. Unreviewed or stale rows count as unresolved. Duplicate applicable reviews are rejected.
- `capability_precision`: report/APK hash, evidence, and per-category `categories` for bridge/setting/callback documenting wrong and unresolved capability attributions. This is reported separately from the <=10% host gate; no invented numerical capability-precision threshold is imposed.
- `source_audit`: APK hash, reviewer, evidence, `deep_positive_activities`, and `present_strata` mapping each actual binding pattern to verified Activity names. At least 30 positives, or a documented exhaustive inventory with evidence if fewer exist; at least two deeply checked hosts per present stratum.
- `holdout`: report/APK hash, evidence, at least 10 separate Activity names, recorded sealing before analysis and no use for repairs, category recalls and ownership upper bound. Holdout hosts must not overlap development deep-positive hosts. The boolean metadata alone is not proof: sealing records and source evidence require review.
- `performance`: APK hash and at least three distinct run IDs with exact current JAR hash, fresh process, no reused results, isolation, 8 CPUs, 16 GiB heap, valid report and measured wall time. All must be <=600 seconds; <=300 is separately recorded as the preferred target.

Reviews cannot be carried forward merely because the APK hash is unchanged. For a new report, either re-review the changed output or explicitly establish applicability with new evidence. Do not delete unmatched oracle rows or relabel candidates out of the denominator.

These checks validate evidence identity and recorded claims, not the truth of source assertions. Full completion still needs a requirement-by-requirement audit. The final candidate must also retain sorted compact/counts exports, deadline reports, no private adapters, and documented residual issues.
