# Generic lifecycle-container factory: first frozen-v12 boundary

Run `test/run-lifecycle-container-probe.sh`; optional arguments select a jar and isolated output directory. Only the two test files are compiled, against the frozen v12 jar. The fixture uses generic container/content/factory names and ordinary object fields plus Android Fragment APIs. It contains no Scene or App signature rule. The companion JSON binds the original independent installation-chain document, revalidated source hashes/quotes, fixture, jar and complete results.

The earliest reproducible difference is a field-backed factory result. Directly reading the Activity's stored Content and invoking its create-view/activity-created methods yields one setting, bridge and callback. Directly invoking a factory that creates a new concrete Content also yields all three. Changing only its result to the Content already held through its captured Activity owner returns zero, with unresolved ContentContract lifecycle dispatch.

Removing the selector guard does not fix this. A direct concrete Factory call also fails. Reducing the capture to a constructor-written Content field, including passing that exact selected Content as the constructor argument, still fails. The result is therefore not specific to selector matching, custom Scene names, Fragment installation or the extra owner-to-root hop. The first required generic boundary is preserving actual captured object identity through a field-backed factory return and subsequent ordinary virtual/interface calls. The exact internal implementation cause remains for an engine trace.

Minimal structure:

```java
host.root = new Content();
RootFactory factory = new Capture(host);
ContentContract selected = factory.create(); // returns this.owner.root
selected.onCreateView(host, ownedContainer);
selected.onActivityCreated(bundle); // configures the same retained WebView
```

The full installed case creates a real android.app.Fragment, assigns its dispatcher delegate, adds/commits it, forwards onActivityCreated into the dispatcher, invokes the registered root factory and calls the returned Content lifecycle. The source-order variant adds the Fragment before assigning the delegate, then commits. Both produce zero in frozen v12. A Navigation-holder path with a fresh factory result also fails and remains a separate candidate object-graph boundary; the current findings do not yet identify that internal cause.

All fourteen cases finished in at most 0.301 seconds against a thirty-second per-case budget, with no budget/deadline diagnostic. This demonstrates a semantic gap independently of long work queues. It does not prove that all 480 real missing Browser facts arise from that gap: the real five Activities also had unfinished queues. Their budget and semantic contributions remain separate.

Uninstalled and wrong-selector cases produce zero and remain required negatives. Their current zero may be masked by the earlier positive failure, so they must be rerun when the positive chain is repaired. Never seed all allocated components or enumerate every compatible implementation to recover this group. Settings, bridge and registered callback must stay bound to the actual factory-returned object's WebView and the installed Activity container.

The actual Browser source factory returns its already selected Activity.t instance when the supplied class selector matches; NavigationScene registers and invokes that actual factory, and a real LifeCycleFragment transaction installs the lifecycle dispatcher. Source hashes and exact lines were checked against the existing independent document. Its apparent duplicate fallback push in decompiled Java remains a branch-dominance caution; this probe does not resolve that bytecode question.

No production code, frozen facts or holdout was changed.

The current fixture uses standard Activity.onCreate(Bundle) and assigns ID 1 to its actual FrameLayout before setContentView; transaction.add uses that same ID. These source-validity corrections leave the frozen-v12 category outcomes unchanged. The JSON retains the correction reasons and refreshed hashes/timings.
