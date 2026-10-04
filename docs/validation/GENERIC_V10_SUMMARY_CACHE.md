# Generic v10: published base-summary reads bypass refinement locks

A v9 Fanqie thread snapshot at 262 seconds shows multiple Activity workers blocked in DexFlow.summary while another worker resolves contextual guards. Even a read of an already-published base summary acquired the same global monitor.

The base cache is now a ConcurrentHashMap. A cache hit returns its published immutable summary without taking that monitor. A miss still enters the original monitor, rechecks the cache, decodes once, stores guard probes, increments the decode count and then publishes the base result. Contextual variants retain the existing synchronization and global 16-variant limit. No resolver result or host-local binding is shared across Activities by this change.

SummaryCacheConcurrencyFixture holds an actual contextual guard resolver behind a latch. A separate cached base read must finish before that resolver is released. Frozen v9 fails with `Published base read blocked behind unrelated contextual resolver`; the new implementation passes. Eight concurrent cold reads also return one identical published summary and increment decoded exactly once. This establishes the removed contention, not an App-level speedup; the next ten-App measurements are still required.

Local reproduction logs: test/runs/generic-v9-fanqie-thread-255s.txt and test/runs/generic-v10-cache-v9-repro.log. The fixture is included in capabilitySelfTest for the next version. Concurrent cache misses and contextual refinements can still contend on the monitor; this change does not claim fully parallel method decoding.
