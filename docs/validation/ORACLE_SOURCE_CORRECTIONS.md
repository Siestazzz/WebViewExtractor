# Explicit source corrections without rewriting regression history

`scripts/correct_oracle.py` creates a separate derived oracle. Raw frozen facts and their scores are unchanged. A source reviewer must supply the old and replacement full rows, their canonical JSON hashes, the immutable original snapshot hash and line, APK hash, source counterevidence and review provenance. No report-driven adjustment or fuzzy matching is accepted.

Each exact old row is retained in the derived file as `source-superseded`, with a correction ID and original/replacement hashes, and excluded from the current positive denominator. The reviewed replacement is inserted. The original unmodified row remains in the raw oracle and correction ledger. Unrelated missing facts are unchanged. If the old row never entered this raw freeze, the exact replacement must already be present; that correction is recorded as already current and performs no mutation. Any other missing or mismatched evidence aborts the operation.

Machine checks validate record identity, not the correctness of the reviewer's source interpretation. Main-agent source review remains delegated. Four tests cover unchanged failures/history, stale hashes, APK mismatches, invalid snapshot lines, non-independent evidence, conflicting corrections, prohibition of approximate matching, and exact already-current no-ops.

## Yangshipin example

The independent SDK audit found sixteen historical one/two-argument file chooser declarations that the installed SDK adapter never dispatches. Separately, eleven source enum settings had been represented using an unsuitable dynamic-value schema. Runtime user-agent and file path expressions are not changed by this correction.

The 27 ledger entries apply 24 corrections to the v9 raw development oracle; three later native-host enum rows are already present in their correct form. All statuses and hashes are recorded in `generic-v9-yangshipin-source-corrected/manifest.json`. Compare v8 and v9 on the same corrected oracle using `generic-v8/9-yangshipin-source-corrected-v9-score.json`; continue to publish their raw-oracle scores separately. A denominator change caused by a source correction is not an extractor improvement.

```sh
python3 scripts/test_correct_oracle.py
python3 scripts/correct_oracle.py \
  --oracle docs/validation/generic-v9-expanded-facts/yangshipin.jsonl \
  --ledger docs/validation/yangshipin/corrected-facts-ledger.jsonl \
  --ledger docs/validation/yangshipin/enum-normalization-corrections-ledger.jsonl \
  --out test/runs/yangshipin-corrected-oracle
```

The scorer currently groups nonpositive source rows under a general unconfirmed-source section. For corrected rows, the correction manifest and `source-superseded` status give the precise reason; they are not unresolved host associations.
