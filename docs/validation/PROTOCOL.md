# Independent validation protocol

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
