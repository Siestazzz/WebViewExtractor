# Generic v11: actual reflection factories and class-name registry carriers

Source-guided News service probes isolated three separate omissions. A local Class constant remained a generic Object after Class.newInstance; capturing that Class in an ordinary metadata object lost the constructor field; and Class.getName-based two-level registries did not qualify for the existing exact Class-key registry handling. Each boundary was reproduced independently before extending the next.

## Construction semantics

`ReflectiveFactories` handles exact public API identities at actual evaluated call sites. Class.forName accepts an already resolved literal naming an APK class. Class.newInstance requires a concrete public class and a public no-argument constructor. The created object retains actual class, call site and allocation context, and the ordinary constructor materialization binds its fields. No class declaration becomes an Activity entry merely because it exists.

Class.getDeclaredConstructors is modeled only when the class declares exactly one constructor, so index zero has an unambiguous identity. Constructor.newInstance currently requires that exact constructor, a known empty argument array, public access and a concrete public class. Multiple constructor array ordering, private access through setAccessible, dynamic names and nonempty argument binding remain diagnostic unknowns. The implementation does not guess reflection array order or claim general reflection completeness.

## Preserving metadata and registry objects

Actually allocated objects with instance Class fields now retain their constructor captures, using the same object-local mechanism as existing WebView/Client holders. This fixes the no-Map metadata cases without globally creating metadata objects.

Registry recognition accepts either the actual Class parameter or exact Class.getName applied to that parameter as the outer key. Besides directly storing the value parameter, it recognizes a fresh inner Map populated with a literal key and that same value parameter, then stored into the actual outer Map field. Reader and writer must share that field. Keys and actual object identities are still evaluated by normal propagation; neither a default-key spelling nor an App/service name is encoded.

## Verification and limits

`ReflectiveFactoryFixture` tests exact Class/name construction, two allocation sites, public/private/abstract access boundaries, singleton constructor identity, arity rejection and refusal to guess multiple-constructor ordering.

`ServiceConstructorArrayProbe.runRegression()` checks twelve complete service paths: Class versus Constructor-array construction, with/without a nested registry, with/without singleton caching, and missing-key reflection negatives. Positive paths must reach Bridge, setting and callback on the created product WebView; negative paths must emit none of those three. The isolated v11 snapshot passes. Frozen v10 could not follow even a local literal Class.newInstance; intermediate v11 snapshots demonstrated the separate carrier and registry failures before the final changes. Logs are under test/runs/generic-v11-service-*.log.

The standalone diagnostic probe also preserves the older **direct-new missing-key false positive**: an unresolved metadata receiver can reach a static default creator that unconditionally constructs an object. This is not fixed by reflection modeling, and it is not reclassified as a passing negative. The expanded diagnostic now has32 cases; its earlier24-case v10 evidence remains separately recorded. New real-App recall and precision must be measured before claiming News service omissions are recovered.

## Real-App checkpoint

The first terminal v11 News run retains Bridge1260/1393 and Settings228/366; its four additional callback matches come from the separate delegation change. None of the nine source-reviewed Ysp hosts emits Ysp capabilities. Report-only diagnostics are preserved in `news/generic-v11-ysp-runtime-diagnostics.json`.

Independent source review then identified an earlier boundary absent from the synthetic service test: registration occurs during Application.attachBaseContext through a no-argument static Method.invoke wrapper, and writes a static registry with a custom inner Map. The fixture registers directly from the Activity in an instance Map. Passing that fixture therefore proves only the modeled factory segment, not recovery of the real initialization chain. Follow-up source evidence and a closer reproducer are in progress; missing facts remain in the denominator.
