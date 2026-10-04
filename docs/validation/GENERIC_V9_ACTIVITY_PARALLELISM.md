# Generic v9: bounded Activity analysis workers

## Problem and implementation

The v8 worker process reserved up to eight CPUs but advanced Activity contexts on one analysis thread. This version adds `--analysis-workers`, defaults to `min(8, availableProcessors)`, accepts 1–8, and forwards the actual setting through the supervisor command into its worker process. `--analysis-workers 1` provides a direct control. CPU and memory JVM limits, host/context/summary budgets and deadline/watchdog reserves are unchanged.

`ParallelActivityScheduler` assigns the existing complete, deterministically ordered root list to fixed lanes. Each lane owns one `CapabilityEngine`, its constants, dispatch metadata, evaluation counters and Activity hosts. A Host's heap, contextual queue, installed Client state and phase facts are never shared between active lanes. Lane tasks may use different executor threads between epochs, but only one task owns a lane at a time.

Initial batches give one root per lane its existing first slice (at most eight contexts and 50 ms, retaining the remaining target share calculation). No deep epoch starts until the whole root first pass completes or the unchanged deadline expires. Within a deep epoch each lane rotates its pending hosts, keeping the existing 100-context/50-ms slice limits. Jobs remain atomic with the existing cooperative checks. A ten-second epoch bounds the normal checkpoint interval; an individual job may still delay a barrier until its cooperative deadline, and the process supervisor remains the hard backstop.

## Shared summaries and index

All engines share one `DexFlow`, so immutable base summaries, guard probes and contextual refinement variants occupy one cache rather than eight. The two summary APIs are synchronized; decoding, checking the context resolver and publishing a refinement occur on the calling worker thread under the same reentrant monitor. A resolver never executes in another worker's Host. The existing 16-variant limit remains global per method, and decoded/refined metrics count unique cache publications, not a sum over copies.

The shared monitor also prevents double publication and inconsistent base/probe pairs. It may limit scaling for workloads dominated by summary decoding or guard resolution; this version does not claim an eightfold speedup. Refinement selection under a saturated 16-variant budget can depend on which actual context reaches the shared cache first, as it already depends on traversal order in serial analysis. Exhaustion still falls back to the conservative all-branch base, with the existing diagnostic. No additional variants or budget are authorized by worker count.

The populated index and APK inventory structures remain read-only during analysis. Runtime subtype, possible-type, delegation-reachability and SDK member-shape caches use concurrent maps/sets. The index diagnostic list uses synchronized publication. Each report copies and sorts index diagnostics only at a quiescent barrier.

## Checkpoint and reporting guarantees

The coordinator waits for every submitted lane task before reading any Host. Even if a task fails, or the coordinator is interrupted, remaining submitted tasks are drained first; only then is the interruption/failure reported. Workers do not invoke report callbacks or write files. At each quiescent barrier the coordinator merges states in root order, unions all workers' `boundSites` and collects their Activity diagnostics. The shared base cache plus this complete union drive unattributed-site calculation. No per-worker unattributed lists are concatenated.

Detailed, compact and counts reports are written by the existing single-threaded atomic export. Coverage includes the entire actual root list, including empty, pending and never-started hosts. Metrics retain per-Activity measured analysis seconds; these can overlap across workers and their sum is not wall time. New metadata records `analysis_workers`, `shared_summary_cache`, `checkpoint_consistency` and final `quiescent_barriers`. Host counts and queue/heap sums are taken once from the merged states, and summary counters once from the shared flow.

## Validation

`ParallelActivityFixture` uses 18 capability hosts and one empty host. Each capability host allocates two distinct WebViews and bridges and reaches a shared helper through four actual helper levels; first slices cannot complete the graph, so resumable deep work is exercised.

Complete facts, owner identities and unattributed records equal the original unsliced serial engine for 1, 2 and 8 workers, including a repeated 8-worker run. All roots finish, empty roots remain in coverage, two WebViews do not merge within a host, and facts do not cross Activities. Base decode counts equal the serial engine. A dedicated eight-thread barrier verifies the configured pool permits eight simultaneous tasks.

A shared boolean-guard summary resolves two actual contexts independently on eight workers and publishes only one base plus two variants. Further contexts stop at exactly 16 variants globally. Quiescent assertions check every lane's `currentHost` is cleared before snapshots. Tests cover an interrupted coordinator, one failing task while another completes, and an already-expired scheduler that starts no roots. A separate partial-deadline test retains a real capability, pending queues and two interrupted hosts without consuming more jobs after expiry.

Atomic exports are parsed after barriers; all three files are present without temporary files, and retained timeout facts/root coverage survive. CLI tests check the serial override, bounded default, rejection above eight and supervisor propagation with its original five-second reserve.

`capabilitySelfTest` and `compactReportTest` pass together in `test/runs/generic-v9-parallel-build.log` (8 seconds). These tests establish isolation, publication and synthetic semantic equivalence. Actual ten-App quality, wall time, RSS and deadline behavior must be measured using the frozen next jar; no App recall or speed claim is made here.

## Known scheduling tradeoffs

Fixed lanes can become imbalanced: a lane with expensive hosts can outlive idle lanes. The shared summary monitor can also serialize guard-heavy work. This deliberately avoids transferring live Host/Engine ownership or enlarging budgets in the first implementation. Checkpoint serialization time is outside worker execution epochs and does not overlap mutations. A non-cooperative library call can still hold a worker until the unchanged supervisor kills the process, leaving the last fully quiescent checkpoint.
