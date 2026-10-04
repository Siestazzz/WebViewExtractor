# Frozen v13 Ctrip single-host CPU sample

This is a diagnostic of HotelFlagShipLoginActivity, not a full-App performance measurement or a substitute for missing acceptance facts. Eight CPUs (48–55),16GiB heap, frozen generic-v13.jar, Java Flight Recorder profile settings. Total88.310s, inventory/index37.356s. The helper calls analyzeActivity, which automatically invokes Application bootstrap in this version.

The bootstrap report records19.716s, one context and zero retained static heap despite its cooperative1s budget. Of5602 execution samples,2750 contain index.read,890 contain ApplicationBootstrap.build,1288 contain Activity analysis without bootstrap and674 fall outside those selected stacks. All890 bootstrap samples contain ReflectionProtocols.writer/discoverFields;723 also contain DexFlow.decode. This independently confirms the News global discovery budget failure on Ctrip. Stack inclusions overlap and sample counts are not exact CPU seconds.

For the1288 Activity-only samples, the nearest analyzer frames include evalInner276, CapabilityIndex.shape161, subtype113, field90 and DexFlow.union67. These are optimization leads; they do not prove that removing any particular App call is sound, and pending-method queue counts are not used as CPU attribution.

The machine record includes frozen jar/JFR/report hashes, diagnostic limits, and inclusive versus nearest-analyzer counts. The full recording and selected-host report remain local and ignored. No real-App acceptance numerator was replaced by this diagnostic.

Reproduction after compiling test/diagnostics/SingleHostProbe.java against the frozen jar:

```sh
taskset -c 48-55 timeout --signal=TERM --kill-after=2 180 java -Xmx16g -XX:ActiveProcessorCount=8 -XX:StartFlightRecording=filename=test/runs/ctrip-v13-cpu-profile/profile.jfr,settings=profile,dumponexit=true -cp test/runs/ctrip-v13-cpu-profile/classes:test/runs/generic-v13.jar org.example.SingleHostProbe test/apks/ctrip.android.view.apk ctrip.android.hotel.order.view.flagship.HotelFlagShipLoginActivity test/runs/ctrip-v13-cpu-profile/capabilities.json
jfr print --json --events jdk.ExecutionSample --stack-depth 96 test/runs/ctrip-v13-cpu-profile/profile.jfr > test/runs/ctrip-v13-cpu-profile/samples.json
```

Classify each sample by stack membership, in order: ApplicationBootstrap.build, CapabilityEngine.analyzeActivity, CapabilityIndex.read, other. Deduplicate method names within each stack for inclusive counts; for nearest-analyzer counts use the first org.example frame. This avoids double counting recursive frames and separates startup work from Activity traversal.

Reusable aggregation: `python3 scripts/summarize_cpu_samples.py --samples test/runs/ctrip-v13-cpu-profile/samples.json --out test/runs/ctrip-v13-cpu-profile/summary.json`. Phase counts reproduce the recorded2750/890/1288/674. The helper reports truncated stacks because absent callers may affect classification. Focused tests verify phase precedence, recursive-frame deduplication and exclusion of non-execution events.
