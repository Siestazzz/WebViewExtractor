# Callback implementation identity in scoring v7

A callback fact contains both an installed/delegated Client implementation and a full member signature. Inheritance can make two different concrete clients share a member declared on their parent. The previous member matcher ignored the source implementation field, so a sibling client could satisfy the expected fact.

The matcher now requires exact implementation equality whenever the independent source fact specifies it. The declaring owner is still checked by the full member signature. Source without concrete implementation remains signature-only and does not prove object binding or precision. This does not establish exact WebView instance identity.

The new regression fails against the old scorer (wrong sibling counted as matched) and passes with the fix. All14 evaluator tests pass, including registration, inherited member, missing concrete identity and legacy unconstrained cases. Logs and hashes: experiments/v19-callback-client-identity-scoring.json.

Scoring version is now7. Preserve v18 scores as historical output. The generic-v19-scoring-v7-baseline directory re-evaluates the same v18 reports and immutable reviewed v18 oracle; differences in that comparison are evaluator corrections, not analyzer improvements. Newly collected source implementation fields are being independently reviewed for concrete-type versus declaring-owner semantics; corrections must preserve the original facts and hash-bound evidence.

## Unsupported source semantics are independent of output

Native-name-free transport identities and runtime-expression settings are now identified as evaluator limitations even if no same-kind candidate exists in the report. Previously those cases could be called ordinary misses solely because the report lacked a candidate. Both remain in the expected denominator with zero matches; neither becomes an invented injection name or a wildcard setting match. Missing records now expose scoring_limitation. The empty/unrelated-output regression failed before the change and the full15-test evaluator suite passes. Evidence: experiments/v19-unscorable-independent-of-output.json. The preliminary scoring-v7 baseline retains its original scorer hash; final iteration replay must use the final scorer hash and reviewed receiver facts.

## Hash-bound source corrections

`scripts/apply_oracle_corrections.py` materializes a reviewed view from an immutable oracle and independently authored correction ledger. It verifies both row hashes, original-row membership, unique correction targets, APK identity, explicit verdict and independent reviewer/reason/counterevidence. It rejects duplicate/collapsing corrections and refuses to overwrite an existing view. Complete original rows and the ledger are copied alongside the view; history is not removed. Four tests cover tampering, missing targets, duplicate corrections, evidence requirements, untouched-row preservation and archival/refusal behavior.

Initial v19 source corrections cover57 Breaking News and18 FreeReels rows. Old historical subsets are still under source review. After these corrections, v18 callback matches are91/94 for Breaking and245/521 for FreeReels under the stricter scorer. These are diagnostic subset measurements, not new analyzer gains or completed source audits. Keep original and corrected oracle hashes with each score. FreeReels runtime-expression limitations remain nonmatches in the denominator.

The subsequent independent review of the original Breaking32-row subset corrected three additional inherited callback receivers. The chained reviewed view in generic-v19-receiver-corrections/breaking-news-history-reviewed retains182 rows and full predecessor/ledger copies. Frozen v18 then scores94/94 callbacks (70/70 bridge,16/16 settings) under scoring7. This restores agreement for the independently checked subset by fixing source schema semantics, not by changing analyzer output; it does not certify complete Breaking coverage or precision.

The next freeze also verifies every predecessor oracle against its manifest SHA256 before producing output. A tampered predecessor fails without creating a new freeze; all ten v18 reviewed files were checked against their manifests. This prevents accidental source edits from silently changing historical denominators. Five freeze tests and four correction tests pass.
