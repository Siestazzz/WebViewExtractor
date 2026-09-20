# v12h Ctrip validation

The report was read after `docs/validation/v12h-benchmark.json` contained the
completed Ctrip row. The run processed all 410 manifest activities, emitted 55
activities, and used APK SHA-256
`cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66`.
The extractor status remains `partial`; completion here means the manifest
queue reached 410/410, not that every emitted capability is proven correct.

## Strict v12a to v12h comparison

- Activity-name sets are identical: 55 versus 55.
- `unattributed`, `index_diagnostics`, and `manifest_diagnostics` are exactly
  equal.
- Activity arrays are not byte-for-byte or canonical-JSON equal.
- Thirty-two activity records differ: 31 differ only in fact ordering; one,
  `ctrip.android.train.pages.triporder.TrainContainerActivity`, changes a nested
  argument-union representation.
- Across all 55 activities there are zero semantic capability additions and
  zero semantic capability removals using the tuple `(kind, site, api, name,
  registration_name, implementation, signature, value)`.
- Fact counts for every activity are unchanged.

The exact file and canonical component hashes for v11c, v12a, and v12h, plus
the complete list of 32 changed records, are stored in `v12h-delta.json`.
This distinguishes deterministic semantic equality from strict serialization
equality.

## Four public object samples

`H5Container`, `MktH5ContainerV2`, and `CMBApiEntryActivity` are exactly equal
to their v12a activity records. `MyCtripOrderModalActivity` has the same 84
facts and the same semantic facts; only ordering changes. Therefore:

- old H5 and the modal Fragment wrapper retain the previously proven single
  `H5Fragment.mWebView` receiver;
- CMB retains its single Activity layout WebView;
- Mkt retains the previously documented unresolved acquire-branch identities
  and `A.x=A` field-alias duplicates. Equality does not cure that known defect.

No v12h change in these samples requires a new source capability or ownership
decision. Their object conclusions reuse `v9c-object-audit.md`,
`v8c-object-audit.md`, and `v7a-alias-first-break.md`.

## Ownership coverage

`v12h-ownership.jsonl` contains one row for every emitted activity. Each row
copies the prior source-reviewed verdict and source anchor from
`v4e-ownership.jsonl`, records the v12h fact count and APK hash, and explicitly
states that revalidation is based on unchanged host membership plus zero
semantic deltas. These verdicts are not derived from extractor predictions.

No ownership update is required. This preserves the earlier audit's limits:
reusing a host verdict confirms that the Activity has a source-supported
WebView path; it does not endorse every capability currently attributed to
that host.

## v11c continuity

v11c and v12h use the same APK hash, have the same 55-host set, and have exactly
equal `unattributed` rows. The machine delta retains hashes for both runs.
Because v12a was already independently accepted as the semantic baseline, the
v12a-to-v12h zero-semantic-delta result is the controlling comparison here.

Files produced:

- `docs/validation/ctrip/v12h-delta.json`
- `docs/validation/ctrip/v12h-ownership.jsonl`
- `docs/validation/ctrip/v12h-validation.md`
