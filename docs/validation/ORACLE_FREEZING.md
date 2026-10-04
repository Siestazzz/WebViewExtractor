# Reproducible cumulative oracle snapshots

`scripts/freeze_oracle.py` creates a new immutable snapshot from an earlier manifest plus explicitly named additional source JSONL files. Every distinct full JSON evidence row is retained, including failures, unknowns and source-rejected facts. Only identical full rows (ignoring object-key order and whitespace) collapse. Distinct evidence, versions, conditions, verdicts or normalized values remain distinct.

Each manifest records source paths and byte SHA256, input/unique/duplicate counts, output SHA256 and a canonical sorted unique-row-set SHA256. The script refuses an existing output directory and validates all inputs before writing it. Tests verify source immutability, retention of unmatched and rejected rows, distinct evidence retention, stable repeated union and malformed-input failure.

This deduplication does not change the evaluator's semantic fact denominator; it prevents repeated historical unions from multiplying identical evidence rows. Source corrections require separate hash-bound review records. The raw cumulative regression oracle still retains historical positive expectations until an explicit documented correction policy is applied; unreviewed exclusions must never silently increase recall.

```sh
python3 scripts/test_freeze_oracle.py
python3 scripts/freeze_oracle.py --previous docs/validation/generic-v7-expanded-facts/manifest.json --append yangshipin=docs/validation/yangshipin/canonical-facts.jsonl --out /tmp/example-new-oracle
```
