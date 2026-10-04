# Development bootstrap deadline comparison

Frozen v13 is compared with the stable three-class development override recorded by source hashes and patch. This is a bootstrap-only diagnostic, not a v14 release or ten-App acceptance run. Each run reads the APK afresh, has eight disjoint logical CPUs and16GiB heap. Runs may overlap; these measurements do not replace final isolated repetitions.

| APK | Baseline bootstrap wall | Override wall | Baseline CPU | Override CPU |
|---|---:|---:|---:|---:|
| News | 11.918s | 1.015s | 11.500s | 1.010s |
| Ctrip | 20.630s | 1.038s | 20.178s | 1.030s |

All four runs report one bootstrap context and zero retained static heap. The cooperative1s boundary is substantially improved, but no successful initialization or recall gain is established. Indexing is measured separately: News53.190/52.980s; Ctrip37.570/38.120s (baseline/override). Local budgets remain cooperative and a single unchecked operation could still overshoot.

The override adds a local-deadline supplier to ReflectionProtocols, checks discovery loops and prefilters reflection plans by the three calls that their existing matching logic already requires. The tested snapshot also includes allocation capture changes, so it is identified as a three-class snapshot rather than a separately measured one-line fix. Later production changes must be retested.

Compile the three source files in the hash-bound snapshot plus test/diagnostics/BootstrapTimingProbe.java against generic-v13.jar into a separate directory. Place that directory first on the classpath for the override; for the baseline compile only the unchanged probe. Run org.example.BootstrapTimingProbe with the APK path. Retain full logs and compare the three labelled time lines plus diagnostics. The machine record specifies CPUs, hashes and local artifact paths. The adjacent patch preserves the exact source override used for reproduction.

## Necessary-condition writer prefilter experiment

The next isolated change asks whether the queried method directly contains a Map.put call matching the existing registration-discovery condition before starting global discovery. Methods failing that necessary condition cannot be recorded as writers by the existing implementation. The filter does not reject a writer based on App identity.

News now visits9 startup contexts in0.042s (CPU0.040s), without the previous startup timeout. Ctrip visits10 in1.016s (CPU1.007s), still reaching the local budget. Both retain zero static heap, so this is increased traversal and reduced wasted work, not demonstrated capability recovery. All frozen-v13 CapabilitySelfTest cases pass against the isolated override, including reflective transport positives and unrelated-map negatives. New production integration and later-version tests remain separate work.

Exact added patch and snapshot hashes are in v14-writer-prefilter.patch and v14-writer-prefilter-comparison.json. Apply this patch after the prior three-class snapshot; compile and run the same BootstrapTimingProbe with the same eight-CPU/16GiB settings. The experiment contains no application-specific condition.
