# Generic v16: explicit superclass Fragment protocol dispatch

v15's application-null-override negative remains valid. An application override that actually invokes its SDK superclass is a different reachable path: the superclass invocation must resolve its declared SDK implementation, rather than being rejected by a second virtual dispatch to that same application override.

The Fragment-only protocol guard now accepts an explicit-super flag. Its access check resolves the call's declared SDK reference for a super invocation; ordinary virtual calls still check the actual receiver implementation. The existing exact public acquisition and transaction contract family remains unchanged. The native Activity getter's implemented SDK declaration is also recognized. No arbitrary application override or AndroidX method is bypassed.

DexFlow preserves both beginTransaction call-site offset and super invocation kind in its result expression. This prevents the precise superclass check from losing its dispatch mode, while retaining v14's transaction allocation identity isolation.

FragmentSuperProtocolFixture executes the real application getter body for native Android, AndroidX and support-v4 families. Each family's direct super-return and super-return passed through an actual static identity helper retain all three capability categories. A null-returning application override retains none. Full capabilitySelfTest passed in 19 seconds, test/runs/generic-v16-super-tests.log. These are synthetic contracts, not additional application recall claims.

Custom transaction implementations and their runtime manager state remain outside this narrow getter repair. The current model does not generally treat an arbitrary new transaction subclass as an installed framework transaction.
