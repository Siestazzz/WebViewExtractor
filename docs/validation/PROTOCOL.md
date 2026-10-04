# Independent validation protocol

This is the original acceptance protocol, with the later development-run scope clarified below. Current implementation/results are indexed in [README.md](README.md). Quality acceptance remains unproven.

Scope: Activity/WebView ownership, bridge names/types/full exposed signatures,
all recognized settings and values, registered clients and full callback signatures.
No inference of callback or bridge business behavior.

APK identity is in samples.json. Source APKs and predecompiled inputs are read-only.
GPT-5.6 Sol agents own source verification; primary agent implements and consumes
reusable evidence records. Gold facts must be derived independently of extractor output.

Acceptance per APK: >=95% recall separately for bridge/settings/callback facts;
<=10% (wrong + unresolved) emitted Activity ownership; target 300 s, hard 600 s.
Candidate facts contribute to candidate-inclusive recall but are reported separately.
Also report incorrect capability assignments; do not inflate recall by attributing every
capability to every Activity. Inventory all manifest entries and all binding strata.
Deep verification: >=30 positive Activities, or document exhaustive search when fewer;
>=2 per present stratum; final fresh holdout >=10 Activities where available.
Decompiler failures are unknown, not negative evidence. Shared implementation evidence
may be reused, but each host association requires its own evidence chain.

Every iteration: meaningful synthetic tests, all three APKs serially, compare accumulated
facts, document omissions and runtime, then commit. No removal of failing oracle facts.
Final: three serial repeats per APK; fresh processes with no prior analysis cache.
8 logical CPUs, JVM heap 16 GiB. benchmark.py records affinity, commands and /usr/bin/time.
Decompilation and human/agent verification are offline evaluation, NOT charged as runtime
of the production extractor; runtime includes initialization, indexing and final output.

Baseline commit: 78afea4. Preserved executable: test/runs/original.jar (untracked).
Baseline run: python3 scripts/benchmark.py --label baseline --jar test/runs/original.jar --legacy
Full original run is capped at 600 seconds; failure/timeout is not a successful report.


## Later six-App development scope

The user subsequently requested six concurrent Apps, adding Xigua, FreeReels and Yangshipin.
`compact-six-samples.json` identifies those inputs; `scripts/run_parallel.py` gives each a separate
eight-CPU affinity and 16 GiB heap, with a 595-second supervisor inside a 600-second external limit.
These explicitly authorized parallel development runs supplement the original protocol; they do
not replace isolated final three-repeat measurements or establish accuracy on the added Apps.
FreeReels was analyzed as a base APK, without merging resource/native-library splits.

Scheduler v1 and v3 have complete six-App runs. The v2 run was deliberately interrupted after
independent review reproduced refinement defects; its retained snapshots are not completed
performance measurements. All versioned failures remain documented.

Initial source inventories were built independently; later development facts include
report-guided cases verified against source/DEX. Record that selection provenance and do not call
the accumulated canonical set a blind holdout. Final sealed holdouts remain unused/unpassed.
Current comparisons use scorer revision 5 and the same versioned canonical files; full semantic
per-fact regression checks use compare_oracle_matches.py, including settings values and types.

Activity initial-pass coverage, traversal completion, internal cap termination and deadline
interruption are separate quantities. Candidates/provisional facts count toward candidate-inclusive
recall and emitted-host ownership evaluation. Increased output counts alone prove neither recall
nor precision. Current all-output ownership/capability precision is still unverified; historical
ownership rates must not be copied onto the scheduler output.

## Generic-rule policy and four added patched samples (2026-10-04)

The user explicitly superseded the pressure to reach recall targets with a stronger implementation constraint: no App-private signature/name adapters, even if quality targets are missed. Public SDK contracts and generic structural/data-flow rules remain permitted. QNRouter was removed; historical evidence is retained. Four new samples (txws, sohuvideo, fanqiexiaoshuo, breaking-news) use the user-selected patched APKs, verified against predecompiled hashes. Earlier six regression hashes remain unchanged. Breaking News is base-only scope.

Source review for this extension uses GPT-6.1 Sol as requested. Current new-App facts cover small independently sourced groups plus explicitly labeled report-guided expansions; they do not satisfy the old 30-positive-host minimum, sealed holdouts, all-output precision, or three isolated repeats. Lower recall is allowed by the user, but uncertainty and unverified precision must still be reported. No denominator changes or deleted failures may be used to conceal regressions.

Final viewing artifacts are sorted `capabilities.compact.json` and `capabilities.counts.json`; detailed reports remain local for validation. Rows sort by Bridge, callback, then Settings counts descending. Counts include candidates and are not verified runtime object counts.
