# Source consolidation for the compact-six baseline

The pending v16/v17 extractor sources are now committed as a prerequisite to the resumable scheduler. Their exact contents were recovered from `test/runs/compact-six-source.tar.gz`, the source archive for the already measured frozen compact-six.jar, without replacing the current working tree.

This includes actual asynchronous registration adapters, contract relevance, lazy initializer invocation, registered message handler bodies, Activity-capturing arguments and expression work budgets. Implementation details and limits are recorded in v16-registration-design.md and the per-App v16 review documents; the final six-App baseline configuration, counts, timings and limitations are in COMPACT_REPORT.md / compact-six-results.json. It is not an acceptance claim. The earlier three-App source oracles are replayed unchanged in scheduler-baseline-{news,mango,ctrip}.json for comparison with the upcoming scheduler.

The next commit will contain scheduling changes separately, so the pre-scheduler implementation remains inspectable in Git. Existing failed cases and source facts have not been removed to improve metrics.
