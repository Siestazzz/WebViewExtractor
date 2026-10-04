# Generic v13 actual Application bootstrap and static reflection

This stage adds a real Manifest Application entry model and a bounded ordinary static reflection protocol. It uses no application/module/service names as rules. Source-guided structural evidence is `news/v12-application-proxy-attach-source.json` and the independent `ServiceStartupRegistrationProbe`: Application startup can initialize a retained static registry before an Activity later requests a service. The real News path additionally uses a hidden/proxy delegate attachment that this stage does not implement. No claim of real News recovery is made.

## Actual roots and ordered startup

ApkInventory retains the Android Manifest application's resolved relative/full class name. Only that exact APK class can become an Application root. It must be public, concrete, an Application subtype, and declare its own public implemented no-argument constructor. Constructors are not inherited. Missing, inaccessible, abstract/interface or unresolved roots produce explicit diagnostics instead of falling back to another Application subclass.

The independent bootstrap Host executes the actual noarg constructor, then the closest real inherited/overridden `attachBaseContext(Context):void`, then `onCreate():void`. Each root drains its pending work before the next starts. The Context parameter is the actual Application abstraction of this APK. Startup entry alone does not seed arbitrary Application helpers; helpers are reached through actual calls using existing relevance or the bounded static-reflection protocol graph. An actual proxy override therefore prevents direct seeding of an otherwise convenient superclass implementation.

Bootstrap is run once before the worker pool starts. Its independent heap contains actual static registration writes and retained objects. A static-root object traversal retains field edges, Map entries, array entries/lengths and collection contents. Read-only snapshot maps/sets are published to all engines. Every Activity receives **eager independent mutable copies** of these containers and fields; this is not lazy copy-on-write. Object values themselves remain immutable abstract values. Coverage exposes `application_bootstrap_import_entries` and `application_bootstrap_import_seconds` per Host. Independent-copy tests verify mutating one Host's registry cannot change another Host or the snapshot.

A startup-created WebView is not assigned to all Activities. Its actual capabilities are retained in detailed report `unattributed` rows with `application_bootstrap_no_activity_owner`, without a fabricated Activity owner. **Retained shared WebView surfaces remain unsupported:** if bootstrap stores such a WebView in a static graph, the heap copy preserves the object but does not yet reattribute its pre-existing Bridge/settings/Client facts to a later Activity that actually attaches it. `application_bootstrap_shared_webview_surface_unmodeled` records that gap. This implementation covers module/registry startup followed by later actual Activity capability calls; it does not claim general shared-WebView ownership completeness.

## Exact ordinary static reflection

StaticReflection recognizes the exact public Class.getMethod(String,Class[]) and Method.invoke(Object,Object[]) signatures at real call sites. The lookup requires an actual known Class object, literal method name and known empty parameter array. It resolves a unique public static zero-parameter method whose declaring APK class is public. Invocation requires that resolved Method identity and a known empty argument array. Static invocation ignores its target object as Java does. setAccessible does not expand access to private methods or inaccessible classes. Actual invocation enqueues that exact method and its registration side effects; a Class literal, name or lookup without invoke does not execute initialization.

Nonempty lookup/invoke signatures, private/inaccessible lookup, instance methods, unknown arrays/names, getDeclaredMethod/hidden access, proxy delegate attachment and static reflective return-value resolution remain conservative unknowns with diagnostics. The existing installed-client reflective transport protocol is preserved separately; ordinary static invocation does not consume its instance Method.invoke calls.

Protocol reachability is used only on an actual call's target and never added to the index's global capability seeds/relevant closure. Its immutable graph query is bounded to128 method states. YES/NO and BUDGET are distinct cached outcomes. A cached BUDGET result leaves an unresolved diagnostic in every querying Host and avoids repeated graph traversal; deadline interruption is not cached as a graph conclusion. This graph cache contains no receiver/map/client binding decisions.

## Cost and incomplete state

Startup has a cooperative one-second envelope inside the unchanged APK hard deadline and original12000 context budget. Evaluation/refresh, graph traversal and snapshot traversal honor the local deadline. Static closure also caps12000 visited values/objects and memoizes abstract-value identity to avoid repeated shared DAG expansion. A once-built receiver field index avoids scanning the full heap for each object. These bounds do not enlarge Activity budgets or sixteen-variant summary refinement.

Time/context/deadline/closure exhaustion is explicitly diagnosed. Static state may be partial when startup consumes the envelope; partial state is never reported as proof that registration is absent. Empty valid startup, missing/unmanifest root and interrupted startup are distinguishable. Root diagnostics include processed contexts and retained static heap size, while coverage records the actual per-Activity import cost. Dynamic provider Contexts, hidden delegate construction and unmodeled proxy attach are not guessed.

## Verification

`StaticStartupReflectionFixture`: actual static registry initialization by direct call versus Class.getMethod + wrapper Method.invoke both reach Bridge/setting/callback. Wrong name, lookup-only, private method, instance method, inaccessible declaring class, wrong populated invocation arity and incomplete parameter array emit none. Frozen v12 independently fails the reflected positive; log `test/runs/generic-v13-static-v12-proof/result.log`.

`StaticReflectionBudgetFixture`: repeated over-budget graph root traverses once, stays BUDGET, diagnoses two Hosts, retains an independent short true path, and does not cache deadline interruption.

`ApplicationBootstrapFixture`: real AXML relative Application name, direct/reflected startup, inherited lifecycle, two Activity heaps and their capability uses, independent registry copies, serial/two-worker fact equality, unmanifest/unused helper, abstract/nonpublic/missing-own-constructor negatives, expired-startup diagnosis, and startup-owned WebView unattributed/shared-surface-gap checks.

The complete capability self-test retains prior Class-carrier global relevance regression, twelve complete service constructor paths and existing Map missing-key reflection negatives, client/transport/conditional/parallel/refresh boundaries. The older direct-new missing-key error remains separate and unresolved. Final run evidence is `test/runs/generic-v13-bootstrap-all-tests.log`; real ten-APK validation follows freezing.
