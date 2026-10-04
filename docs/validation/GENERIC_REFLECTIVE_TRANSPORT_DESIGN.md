# Installed Client reflective transport integration design

Status: design only, after the frozen v6 implementation. No production transport integration or new recall claim is made here. This review reads existing source-audit documents and analyzer code; it does not read application decompiled source.

## Evidence and distinctions

The reusable structures are documented in:

- `sohuvideo/ali-prompt-registry-source.{md,json}`: an installed inherited prompt callback reads a namespace→object map on its actual WebView, performs public inherited exact-parameter lookup, checks a runtime method annotation, and invokes the selected endpoint with a newly constructed context and a String payload. The annotation's value does not rename the method. Two registrations of separately constructed objects remain two namespaces; an empty registered object has no callable endpoints.
- `sohuvideo/sohu-ad-prompt-declared-method-source.{md,json}`: the installed prompt callback reaches a registered intercept object and an asynchronous parser while retaining a weak reference to the actual WebView. Annotation aliases index declared method names, followed by exact declared-method lookup and an accessibility override. The default target's annotated methods have incompatible parameters and are not callable. A separately source-confirmed replacement class has compatible parameters, but singleton construction snapshots its class choice; initialization order remains conditional.
- `news/qq-console-rule.{md,json}` and `news/v16-qq-console-router-identity.{md,json}`: an installed modern console callback traverses its captured Dialog into the same router used by registration. Dispatch receives that Dialog's actual WebView. Declared-method selection uses name and arity; invocation supplies decoded Strings and has a finite zero-to-six argument ladder. An uninstalled lookalike router or the logging-only legacy console callback cannot expose this registry.
- `news/adcore-prompt-rules.{md,json}`: a Method-valued map belongs to the reflection receiver itself. Enumeration is public/inherited, annotation filtering uses isAnnotationPresent, and a generated key includes method name and parameter-derived suffixes. Runtime request conversion and optional fallback determine compatibility. This is a different map shape from a namespace→handler registry and must not be interpreted as one.

The application names, private signatures, field names, marker strings, annotation names and endpoint lists in these documents are test evidence, never production matching constants. String guards and aliases may be extracted from an analyzed body as ordinary program values. Renaming those values must not change structural recognition.

## Current implementation limits

TransportProtocols currently supplies only structural registration candidates: a Map.put whose receiver is an identified field, retaining the owner/name/handler expressions and call offset. Its bind function performs pure parameter substitution; it does not read fields, construct instances, prove installation, select methods or emit a fact. clientEntry validates complete Android/Tencent console, prompt and selected URL callback contracts. A matching callback signature alone is insufficient to prove a transport.

Existing discoverMessageRegistries searches from broad transport-shaped methods, collects referenced fields and tries a particular annotation/parameter shape. Existing reflectEndpoints can enumerate all public members of a field receiver. Reusing either result as an endpoint certificate would miss declared-only/accessibility/annotation constraints and can overexpose unrelated methods. Existing map state also joins repeated writes and marks mutation order unknown; it does not implement precise last-writer replacement. The new design must state that uncertainty instead of asserting exact runtime contents.

## Runtime binding invariant

A transport fact requires this actual-object path:

`known WebView → actually installed standard Client → reached valid callback → actual helper/intercept/async receiver → actual registry map read → actual handler or Method receiver → compatible reflected endpoint`.

Use v6's installed-client execution context as the initial provenance. The context is specific to the installed client object, setter site and WebView; it already separates no-WebView-argument callbacks installed on different receivers. A console callback gets its WebView association from that installation, while prompt/URL callbacks carry a real WebView argument. If a dispatcher passes a different WebView, retain that argument and do not replace it with the installation receiver. A mismatch is observable evidence, not an excuse to join the two objects by type.

Every cross-object helper call must use normal evaluated call arguments and resolved virtual dispatch. Custom intercepts require their actual stored field/registration contract. Async work requires an actual recognized registration and its captured object/weak reference. Merely allocating an intercept or Runnable, or finding a similarly named callback, does not start the transport path. WeakReference.get retains its actual constructor argument as a conditional value; a cleared reference may yield null. Context objects are separately allocated and can capture the actual WebView; Activity Context arguments are not substituted for WebViews.

Do not attach transport context globally to every instance of a Client, router, singleton or handler class. Class literals alone install nothing. Runtime reflection-discovery metadata creates no global relevance root and no blanket callback/SDK traversal. The v5 framework-factory cost regression is a required regression lesson for this integration.

## Structural contract representation

A reusable discovery result should retain expressions rather than private names:

| Part | Required evidence |
| --- | --- |
| Entry | Complete standard callback contract and actual reached implementation |
| Request | Actual callback argument or derived console/URL/JSON value; guard expressions retained |
| Registry | Map object expression, field provenance if present, key expression and lookup site |
| Target | Actual Map.get result, self receiver, or reflected class/new-instance expression |
| Selection | Exact public/declared lookup or enumerated public/declared methods; name/parameter/arity expressions |
| Annotation | Runtime annotation Class expression, selected Method identity, actual gate and any consumed annotation element |
| Invocation | Selected Method expression, invocation receiver, Object[] element expressions and known length |
| Accessibility | Actual relevant setAccessible or equivalent path, rather than a global private-method allowance |
| Response | Actual result sink and guard, separately from endpoint availability |

Suggested implementation records are TransportEntry, RegistryAccess, ReflectionSelection, InvocationContract and BoundTransport. Discovery records may have unresolved expressions. BoundTransport must additionally carry the actual Client, WebView, map and target identities, evidence path, and uncertainty flags. These records are proposed interfaces, not implemented classes.

Reflection method values need provenance tying enumeration/lookup to the class and selected method. This distinguishes Map<String,Method> from Map<String,Object>. A Method map's invocation receiver may be its actual owner object, another supplied object, or null for a static method; the map key cannot determine that relationship.

## Parameter and reflection constraints

Exact lookup is based on the actual Class[] argument, preserving its length, order, array descriptors and primitive types. A partial array is not an exact vector. Zero parameters are a valid resolved vector, not an unresolved vector. getMethod and getDeclaredMethod match parameter types exactly at lookup; invocation compatibility is a second, separate check. A method carrying the right annotation or alias but a different parameter vector has no callable endpoint under that contract.

Public lookup/enumeration includes inherited public methods with normal overriding/hiding resolution. Declared lookup/enumeration excludes inherited methods. A selected nonpublic declared method requires the actual accessibility path or a proved permitted caller relationship; otherwise record access uncertainty/failure rather than label it callable. A setAccessible call on one Method value must not enable another Method or the entire target class.

Annotation identity comes from the actual Class expression passed to getAnnotation/isAnnotationPresent or from a concretely modeled annotation iteration. Do not match a suffix such as BridgeMethod or H5CallNa. Only a gate on the selected Method can constrain exposure. An annotation value becomes the JavaScript alias only if a consumed annotation-element result reaches the registry key/name. Presence without value consumption leaves the Java name unchanged. Runtime annotation retention/visibility must be respected. An annotation on an unrelated method, or on a replaced superclass declaration, does not certify the selected method.

Invocation checks every Object[] element against the selected parameter vector. Null can satisfy a reference parameter but not a primitive one. Concrete boxed values can use Java reflection's permitted unboxing/widening when modeled; decoded String values do not magically become primitive/JSON/context objects. Reference compatibility uses the available hierarchy; missing hierarchy stays unknown. A reflected varargs declaration does not justify automatically packing arbitrary supplied strings into a trailing array.

For name/arity enumeration, retain the actual invocation ladder limit and supplied element types. In the documented decoded-String structure, a compatible String/supertype reference can be called, an incompatible primitive or unrelated reference cannot, and a selected arity beyond the actual supplied array length fails. Same-name/same-arity overloads remain alternatives when enumeration order is unspecified; do not select a deterministic DEX order as Java's runtime order. An incompatible first match need not fall through to another overload unless the body actually does so.

For Method-valued registries, parameter-derived key construction and request conversion must come from actual control/data flow. Do not hardcode the documented suffix strings or one SDK's JSON type table. If the key builder or conversion cannot be resolved generically, retain an unresolved key/conversion contract and avoid claiming all annotated methods are callable. A source-derived key fallback applies only when that actual branch is reached.

Static reflected endpoints can be valid, independently of Client callback rules. Their invocation receiver is ignored by Java reflection, but the Class/Method selected from an actually bound transport remains necessary. Public static annotated JavaScriptInterface endpoints already have a separate v6 rule and must not become a substitute for this transport proof.

## Map receiver and context substitution

For each registration summary, bind expressions against the actual callee arguments using correct DEX numbering: instance receiver at index zero, static arguments beginning at zero, wide parameters represented by the existing abstract argument layout. Then evaluate bound owner/name/handler expressions in that call's context. A field expression is not a map instance: read that exact field on that exact owner object to obtain the actual map identity. Nested fields and returned map aliases use normal evaluation and constructor captures. No owner constructor is invented to repair an unknown field.

Dispatch performs the analogous substitution and evaluation on the actual reached call. Join registration and dispatch only when their evaluated map objects alias. Field descriptor plus owner identity is provenance, while actual map identity is the state key; two different maps in one owner remain distinct, and one map stored in two fields can still be the same registry. An unresolved map never acts as a wildcard joining every same-type registry.

Within one known map, a concrete lookup key selects its matching entry. A dynamic key may select the conditional union of registrations on that same map, but must preserve each possible registration name and handler identity. It does not make every map in the Activity compatible. Dynamic registration names remain unknown entries on their own map; they are not absent registrations or aliases for a convenient known string.

Map replacement/removal must follow available execution evidence. Existing weak updates preserve alternatives and report unresolved update order; the new transport should inherit that candidate status. A known unconditional sequential put can eventually support a local strong update, but phase-joined/union state cannot. clear/remove/putAll/compute semantics require their own modeled mutations; absence of a model must not produce explicit live-entry claims. Singleton fields similarly preserve construction-time snapshots and known ordering alternatives rather than retroactively replacing a previously created target when a configuration setter changes.

## Emission and conservative unknowns

Emit message_bridge candidates only after installation, actual registry binding and a compatible reflection contract. Preserve separate registration-site and transport/dispatch-site evidence. The endpoint/member identity includes its real descriptor; object provenance includes map, handler/reflection receiver, installed Client and actual WebView IDs. The endpoint fact should not be confused with native addJavascriptInterface injection, especially when an actual empty override suppresses that API.

Retain explicit states for bound registry with no compatible endpoint, bound registry with unresolved target/selection/conversion, and an unbound transport. A default target with incompatible parameters has zero callable members. A concrete replacement with compatible methods is a separate conditional alternative. An unknown replacement class is not every known application class. Dynamic runtime messages mean available conditional interface, never proof that every endpoint executes on launch.

Proposed gap categories are transport_client_uninstalled, transport_registry_receiver_unresolved, transport_request_key_dynamic, transport_reflection_parameter_vector_partial, transport_reflection_access_unresolved, transport_reflection_argument_type_unknown, transport_alias_order_unresolved, transport_target_snapshot_order_unresolved and transport_search_budget. These are proposed diagnostics; names can be aligned with existing conventions during implementation.

Response transport is separate: invoking a nonvoid endpoint does not prove its return reaches JavaScript. Actual prompt confirmation, callback guards, result conversion and WebView result sinks must be reached on the same path. A logging-only callback or a failure-only scheme branch exposes no endpoints merely because another branch in the class is a transport.

## Required synthetic fixtures

| Fixture family | Positive | Negative/boundary |
| --- | --- | --- |
| Installed console router | Modern installed callback → captured owner → same map/router → actual WebView; swapped/static registration helpers | Legacy logger, uninstalled Client/router, two same-name Dialog instances, mismatched dispatcher WebView |
| Prompt namespace map | Actual callback WebView map; public inherited exact (context,String) method; consumed runtime annotation gate | Different owner map, unused annotation value, unannotated/mismatched/private method, failure-only branch, empty registered handler |
| Declared annotation alias | Declared method alias → exact declared parameters → actual accessibility override | Inherited method, incompatible default method, accessibility on different Method, duplicate alias order unknown |
| Method-valued map | Actual enumerated Method provenance, same invocation receiver, exact key/conversion path | Unrelated Method map, alias collision, unknown suffix builder, unrelated annotation gate, no invocation |
| Invocation compatibility | Zero args, compatible references, known boxed conversions, static method where actually selected | Partial Class[], arity above ladder, String→primitive mismatch, missing varargs packing, wrong array descriptor |
| Object/context preservation | Adjacent constructor field capture, actual intercept, registered async parser, WeakReference to same WebView | Context argument mistaken for WebView, alternate holder, unregistered Runnable, cleared/null weak reference |
| Map/snapshot state | Same map aliases through two fields; known class name snapshot; preserved replacement alternatives | Same field name on two owners, same alias in two maps, unknown map wildcard join, singleton retroactive target replacement |
| Cost controls | Reachable installed protocol retains positive under existing budgets | Reflection-shaped SDK restore/helper alone creates no global relevant root; exhausted search has a diagnostic |

Each positive should have a corresponding body/receiver mutation that removes its proof and therefore its endpoint facts. Rename all application classes, fields, aliases, annotations and marker values in at least one equivalent fixture. Existing TransportProtocolsFixture remains the discovery-only test; it cannot stand in for these installed runtime tests.

## Integration order

First add bounded structural ReflectionSelection/InvocationContract metadata and pure unit fixtures, with no capability roots. Next join actual installed console/prompt contexts to namespace/object maps and exact-parameter reflection; preserve empty/incompatible-target outcomes. Then add declared enumeration/name-arity selection, annotation aliases and registered async context. Finally consider Method-valued maps with generated type keys only when their generic expression/conversion model is proven.

At each stage compare a frozen build with the same development positives and negatives, check pending-method diagnostics for library-state-machine expansion, and run the full existing selfTest/compact suite. No larger queue/context/summary/time budget is proposed. No private-signature fallback should be used to make the documented samples pass when a generic structural step remains unsupported.
