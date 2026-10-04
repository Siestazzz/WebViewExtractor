# Generic rules v1

2026-10-04. Production analysis must not depend on private App package names, class names, signatures, registration keys, or business-method names. Lower recall is acceptable when a binding lacks generic evidence.

## Removed

`FrameworkServices` and its QNRouter registration/lookup adapter were deleted. `CapabilityIndex` no longer indexes private service metadata; `CapabilityEngine` no longer resolves those lookup signatures, synthesizes registered-service receivers, or marks jobs by their private synthetic IDs. The fixture that expected those signatures to resolve was replaced with standard-client contract checks. No package-free or short-name replacement was introduced.

Service acquisition can still be followed where ordinary code provides concrete allocation, return values, field writes, provider installation, interface dispatch, or Class-keyed Map structure. These rules inspect DEX structure and data flow rather than private signatures. A registry whose implementation cannot be derived from those observations remains unresolved.

## Standard client callbacks

Standard Android and public Tencent SDK client callbacks now require the installed client family and a complete DEX shape (name, parameter types, and return type). Actual SDK declarations are used when present, and explicit public contract shapes provide the fallback when SDK classes are absent. An `onPageFinished()` overload, a wrong return type, a Chrome callback on a WebViewClient, an unrelated class, and a mismatched SDK WebView parameter are rejected. Tests cover valid Android/Tencent callbacks and the emitted member list.

Inherited implementations and explicit super calls remain supported. Hidden `openFileChooser` overloads require an actual SDK declaration; the fallback does not guess them by name.

## Audit and boundaries

Production-source audit found no remaining App-specific package/signature adapters after the removal. Retained namespaces describe public platform/SDK or language/runtime contracts: Android/AndroidX, Tencent WebView, UC/OEM WebViews, Kotlin, and AspectJ. Registry, reflection, factory, field, callback-carrier and closure propagation remain structural.

Custom listener discovery still uses a familiar callback name plus a WebView parameter as an initial candidate filter. Acceptance additionally requires a setter storing that listener and an observed invocation on the same stored field with a matching declared contract signature. This is a generic structural heuristic, not proof of runtime execution. XML view callback entry selection and several platform helper recognizers still have name-oriented heuristics; this change does not claim all framework contracts are exhaustively modeled.

The client fallback is conservative for SDK-specific overloads not represented in public contract shapes. Runtime registration order, dynamic replacement, reflection strings, asynchronous scheduling, and unresolved factories can still limit recall or certainty. Historical validation artifacts and tutorials were not rewritten.

## Validation

The coordinating agent confirmed the final shared-source build passed `capabilitySelfTest`, `compactReportTest`, and `shadowJar`, then froze `test/runs/generic-v1.jar` for the ten-App run.

Existing generic registry tests already cover the replacement scope without private signatures:

- `keyedRegistryFixture`: two independent registry instances backed by a HashMap field, inherited constructors, Class-keyed registration, and interface lookup dispatch. Each instance binds only its own plugin; registration methods must not become global capability seeds.
- `keyedMapFixture`: concrete Map factory return and Class lookup preserve the exact registered instance. A different Class key is excluded; same-name String keys do not match Class keys. Unknown keys expose alternatives and uncertainty; opaque Map receivers stay unknown; repeated writes do not pretend to establish runtime replacement order.
- `installedProviderFixture`: an installed implementation reaches its host and an uninstalled implementation cannot leak into the result.

These fixtures provide both positive data-flow evidence and negative isolation checks. No replacement App-specific service fixture is needed.

An independent follow-up review identified one remaining standard-client limitation: the predicate rejects static methods but currently does not reject private methods with the same complete shape. A private method cannot participate in virtual framework callback dispatch. This is recorded as a known access-flag gap pending the coordinating agent's decision about the frozen validation build.


## Generic v2 follow-up

The follow-up source now excludes private and static methods from standard-client matching, and excludes private/static SDK declarations when deriving a virtual callback contract. Added checks cover private/static same-signature client methods, absence of private methods from the member report, and private versus public SDK declarations for a shape that requires actual SDK evidence. Existing public Android/Tencent positive checks remain. These changes do not alter the frozen v1 jar; v2 build/test validation is pending coordination.
