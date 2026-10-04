# Allocation-local constructor provenance: isolated experiment

A frozen-v12 heap trace for the unconditional captured-owner factory contains the actual Host.root object but no CapturedFactory.owner field. The factory type is absent from bindingObjects and its constructor is not globally relevant. This is a specific lost capture, not evidence that a Scene class-name adapter is needed.

The experimental patch records the actual allocation's matching constructor expressions and caller Job without adding any global relevant class/method. When a field read on that allocated object has no heap value, it evaluates those captured actual arguments and invokes existing constructor materialization. It never scans arbitrary constructors for an unknown declared receiver. Existing materialization budgets and recursion guards remain; provenance records additionally cap objects at12000 and variants at8 with diagnostics.

Ten direct routes then yield Bridge/setting/callback, including the captured-owner/root path, direct value capture, nested holder and dispatcher cases. Installed and late-setter Fragment paths still yield none; the absent installation and wrong-selector negatives remain empty. A second Fragment dispatch/relevance boundary is therefore still open. No real-App output is changed by this isolated test.

The first matrix predates an explicit FrameLayout.setId(1) correction to the source-derived fixture, so it is not used to establish valid installation behavior. The corrected fixture must be rerun and hashed. Field overwrite/order, two separate allocation instances, inherited constructors, unresolved receivers, memory and real-App cost still need dedicated checks before integrating this idea.

The patch applies to the frozen v12 CapabilityEngine, not the current development worktree. Compile the patched class into a separate directory against generic-v12.jar, place that directory first on the classpath, and run LifecycleContainerFactoryProbe. Do not label this experimental class override as an APK acceptance build.

## Corrected fixture and construction-time argument experiment

The fourteen-case matrix was rerun after using standard Activity.onCreate(Bundle) and a FrameLayout ID matching the transaction container. Results retain the same ten direct positives and four empty Fragment/negative paths, now with the corrected fixture hash recorded in allocation-constructor-capture-v12-eager-results.json.

A second probe demonstrates why storing argument expressions for later reevaluation is unsound: construct a factory from an old outer-field object, overwrite that outer field, then read the factory's captured value. The original lazy-expression patch returns the new object and fails. The eager-arguments patch saves argument values when the actual allocation is evaluated and returns the old object, passing the regression. Constructor body materialization remains demand-driven; argument capture is not delayed. Both patches and their distinct results are retained.

`test/diagnostics/ConstructorCaptureTimeProbe.java` reproduces this heap-state boundary against the isolated class override. Unresolved/deferred argument values, repeated variants and full control-flow write ordering still need further investigation; this one focused check does not establish full temporal precision.

## Constructor-body time counterexample

The `nodefield` variant of `ConstructorCaptureTimeProbe` passes the Host itself to a constructor that reads Host.root. It then overwrites Host.root before reading the factory capture. Even with allocation-time argument values, demand-driven constructor body execution reads the newer root and fails. The observed failure is retained in `test/runs/lifecycle-root-diagnostic/constructor-nodefield-time-result.txt`. Thus eager argument capture alone is insufficient; this patch must not be integrated as temporally sound. A production implementation must capture supported constructor writes at the actual allocation boundary or explicitly retain unknowns for unsupported mutable reads.

The installed Fragment diagnosis is now separately evidenced in `../xigua/v12-installed-fragment-receiver-trace.md`: the actual Fragment allocation and setter receiver have the same ID, and its delegate field contains the actual Dispatcher. The lifecycle job is absent because receiver relevance does not follow that field. Diagnostic insertion of that lifecycle on the same receiver restores all three abilities. This identifies a separate scheduling failure, not an object-ID mismatch.

The independent layer probe also emits three abilities for an uninstalled Fragment. Consequently the empty uninstalled case in the factory matrix was masked by the constructor-capture failure and must not be used to claim installation precision.
