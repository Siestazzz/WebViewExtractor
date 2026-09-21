# Scheduler v2 development interruption

v2 retained phase-one-only facts as candidate/provisional entries through normal finalization. Receiver/key/argument refinement replaces unresolved placeholders only when recognized, and records superseded placeholders. Member shrinking does not erase prior exposed signatures. Added per-phase context counts, discarded queued-context counts, explicit deadline interruption, tracked deferred/XML counts, checkpoint time exclusion and retained-heap monitoring.

Original regression suite, resumable/isolation fixtures and initial PhaseRetentionFixture passed. Independent Sol review nevertheless reproduced two over-broad replacement cases: unchanged opaque unknown arguments allowed implementation concretization without provenance; message-registry receiver argument 0 was skipped. See scheduler-v2-review.md and regressions/SchedulerV2RefinementBoundaryProbe.java. The six-App development batch was deliberately interrupted when these failures were confirmed. It is NOT a complete performance measurement; negative exit codes are operator termination of task-owned processes, not spontaneous APK parser failures. Valid stage reports were retained. v3 fixes both boundaries and repeats all six Apps.

| App | Observed wall seconds at termination/end | Completed initial pass | Traversal finished | Pending | Output hosts |
|---|---:|---:|---:|---:|---:|
| com.cctv.yangshipin.app.androidp | 46.9 | 229/229 | 229 | 0 | 24 |
| com.freereels.app | 228.8 | 164/164 | 55 | 109 | 105 |
| com.hunantv.imgo.activity | 228.8 | 657/657 | 606 | 51 | 113 |
| com.ss.android.article.video | 228.8 | 628/628 | 283 | 345 | 36 |
| com.tencent.news | 176.1 | 2329/2329 | 2327 | 0 | 41 |
| ctrip.android.view | 228.8 | 410/410 | 386 | 24 | 55 |

Snapshots may predate interruption by a checkpoint interval. They must not be treated as final missing-capability measurements. Reproduction: use the v2 commit to build with `./gradlew capabilitySelfTest compactReportTest shadowJar --offline --console=plain`, then run scripts/run_parallel.py with compact-six-samples.json. Frozen v2 JAR and source archive remain in ignored test/runs.
