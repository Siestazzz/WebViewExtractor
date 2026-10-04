# Generic v14: actual constructor captures and installed Fragment entries

This version adds no application names, private signatures, or global Class-holder relevance roots. It preserves existing context, summary (500-instruction eligibility and 16 variants), time, and allocation budgets.

## Allocation capture

Previously, an allocated ordinary factory could lose its constructor-held argument before a later getter/interface call. `ConstructorCaptures` records fields at the actual allocation, with argument values captured at that point. It does not delay constructor execution until a future field read. Unknown arguments stay unknown rather than refreshing from later heap writes.

The bounded model captures only writes to the new object, with a 12,000-object ceiling, 500-instruction body boundary, and bounded superclass traversal. Unsupported constructor calls and ambiguous read/write ordering produce unknown values and diagnostics. Prior indirect calls are checked for field mutation, including inherited declaring methods and possible overriding methods; uncertainty or proof-budget exhaustion does not yield a later concrete value. No constructor side effects on unrelated objects or static fields are invented.

`AllocationCaptureFixture` checks two objects, overwritten owner fields, frozen unknown input, the object cap, unchanged global relevance and ten directly reached factory routes. `ConstructorCaptureOrderingFixture` checks plain argument capture plus constructor-internal overwrite, superclass overwrite, indirect helper, inherited helper and overriding helper; the latter ambiguous cases must retain old values or diagnostic unknown, never later values.

## Fragment activation

Constructing a Fragment or resolving `Fragment.instantiate` no longer establishes a lifecycle entry. An actual installed ViewPager2 adapter result or a supported public FragmentTransaction add/replace followed by commit activates the actual Fragment object. add without commit does not activate; remove removes the pending object. Native, AndroidX and support-v4 transaction families use full API signatures. Unknown receivers do not expand all Fragment subclasses.

Transactions retain manager and receiver context, declaring analysis method, and beginTransaction instruction offset. Two begin calls in one method remain separate. Repeated calls at the same method/offset and receiver context still share the existing abstract allocation identity; this is a precision limit, not full runtime transaction identity.

Installed lifecycle relevance also follows actual instance fields to concrete delegate receivers, under bounded traversal. These heap-sensitive results are not stored in the old type-only relevance cache. A same-class second object does not receive the first object's delegate. A changed field on an installed Fragment requests bounded callback replay; construction alone cannot trigger replay. Direct and super dispatch retain their call semantics.

`FragmentActivationFixture` checks installed onCreate/onActivityCreated versus the same uninstalled direct-content callback, and unknown-to-known delegate updates plus same-class receiver isolation. `FragmentTransactionIsolationProbe` checks a committed single transaction, an uncommitted transaction, and two same-manager transactions with only the second committed. The fourteen-route factory matrix now checks installed and late-setter positives, uninstalled and wrong-selector negatives. Exact known String.equals guards fold only two actual non-null String literals; unresolved values retain conservative branches.

Existing ComponentEntry, ClientGetterLifecycle and FragmentLayout fixtures formerly constructed Fragments without an installation. Their positive lifecycle cases now contain real AndroidX FragmentActivity/FragmentManager/FragmentTransaction contracts. The deep evidence fixture still checks its real 72-hop direct capability; its uninstalled Fragment is now explicitly a negative. No assertion was removed to mask an installed lifecycle loss.

XML fragment creation, navigation controllers and restored-state attach are not fully modeled here. Suppressed unattached lifecycle entries carry `fragment_lifecycle_not_installed`; this version must be measured for regressions on these paths. Framework transaction implementation overrides are respected by the existing actual-receiver override check. Complex transaction operations, asynchronous completion and runtime repeated allocations remain conservative boundaries.

## Reflection discovery cost

Transport plan eligibility now requires the lookup, annotation gate and actual Method.invoke calls already required by the certificate. Global field discovery checks the active local deadline in both passes. A writer query first checks its own indexed calls for an exact Map.put contract; an unrelated constructor cannot initiate discovery. The InstalledReflectionFixture asserts zero discovery visits for that constructor and retained discovery for its actual writer, in addition to installed prompt/console endpoint and isolation tests.

Frozen-v13 versus isolated deadline-only News/Ctrip measurements are recorded in `experiments/V14_BOOTSTRAP_DEADLINE_COMPARISON.md`; writer-prefilter measurements have separate experimental snapshots. Reduced discovery time is not evidence that static bootstrap registration recovered: the measured static heap remains empty.

## Validation

`./gradlew capabilitySelfTest compactReportTest shadowJar --console=plain` passed in 18 seconds, recorded in `test/runs/generic-v14-freeze-tests.log`. This is synthetic and report validation only. Ten-APK quality/performance acceptance remains the next independent measurement; no recall or runtime improvement is claimed from fixture success alone.
