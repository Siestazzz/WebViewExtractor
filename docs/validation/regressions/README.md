# Known failing regression fixtures

These fixtures record unresolved defects. They are not counted as passing validation.

`LifecycleFixture.java` reproduces the v11c long-video binding defect without app-specific names:
a base Activity creates a concrete page, calls an interface-typed field setter/getter, and forwards
that object to a subclass callback. The setter and getter are excluded from relevance. A direct
one-method page callback would be independently seeded by the existing near-capability callback
heuristic and hide this defect, so the fixture uses three ordinary wrapper calls before the bridge
registration. Constructor settings are still found; the callback's bridge is missing.

Reproduction from repository root after building capabilitySelfTest and shadowJar:

```sh
javac -cp build/libs/webview_extractor-1.0-SNAPSHOT-all.jar:build/classes/java/test -d test/runs/debug docs/validation/regressions/LifecycleFixture.java
java -cp build/libs/webview_extractor-1.0-SNAPSHOT-all.jar:build/classes/java/test:test/runs/debug org.example.LifecycleFixture
```

Observed with frozen v11c: `setterRelevant=false, getterRelevant=false`, exit1 with
`Base lifecycle setter/getter lost concrete page argument to subclass override`.
This must become a passing regression after actual binding repair; do not delete it to improve
validation status. Source evidence: news/v11b-validation.md. Debug heap/index evidence separately
confirms news setPage/getPage are excluded while its inherited onCreate is visited.
