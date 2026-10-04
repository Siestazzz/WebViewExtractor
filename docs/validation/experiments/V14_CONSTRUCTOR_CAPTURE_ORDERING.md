# Constructor capture ordering regressions

The new generic DEX fixture models six cases. At the actual allocation boundary it supplies the heap after any preceding helper mutation, while the caller's argument register was loaded earlier. The correct captured value is the earlier object; diagnosed unknown is accepted when ordering is not proved. A concrete later object is never accepted. The plain unmodified capture must retain the old object exactly, preventing an always-unknown implementation from passing all tests.

| Case | Before mutation fix | After |
|---|---|---|
| Unmodified direct field argument | exact old value | exact old value |
| Constructor reads field, then overwrites it | diagnosed unknown | diagnosed unknown |
| Super constructor initializes field; child reads then overwrites | diagnosed unknown | diagnosed unknown |
| Caller saves old field register; indirect static helper overwrites field | diagnosed unknown | diagnosed unknown |
| Same old register, inherited virtual helper overwrites field | **wrong later value, no diagnostic** | diagnosed unknown |
| Declared base helper is empty, actual override overwrites field | **wrong later value, no diagnostic** | diagnosed unknown |

The inherited failure came from checking field-writer membership only under the invocation reference rather than the resolved declaring-method identity. The override failure came from treating the declared base body as proof that the actual virtual call could not mutate. The fix checks the resolved method too and conservatively refuses unresolved overridable calls. These are generic temporal/dispatch rules, not App adaptations.

Both failed outputs remain in v14-constructor-order-before-fix.json, and all six post-fix outputs are in v14-constructor-order-after-fix.json, with implementation/fixture hashes. This is an isolated allocation-capture test against the frozen-v13 engine plus development overrides; full v14 integration and ten-App results remain separately required.

Reproduction: compile src/test/java/org/example/ConstructorCaptureOrderingFixture.java and the versioned ConstructorCaptures.java against the recorded override classes, frozen probe test helpers and generic-v13.jar, then run org.example.ConstructorCaptureOrderingFixture. Its optional argument selects plain, body, super, indirect, inherited or override. The fixture is intended for inclusion in the full capabilitySelfTest.
