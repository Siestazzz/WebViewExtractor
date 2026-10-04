# Generic v8: installed exact reflective transport

## Evidence and scope

This change follows `GENERIC_REFLECTIVE_TRANSPORT_DESIGN.md` and the source facts in `sohuvideo/ali-prompt-registry-source.md`, `sohuvideo/sohu-ad-prompt-declared-method-source.md`, and `news/v16-qq-console-router-identity.md`. These are development evidence, not private names used by production rules. No new App source was read for this implementation.

The first stage implements a closed route from an actually installed standard Client callback, through its actual object fields and a particular Map instance, to an actual registered object and exact reflective invocation. A structural discovery certificate alone emits nothing and creates no global capability relevance root. Shared Java names, class literals, unrelated map fields, and uninstalled Client objects cannot establish installation.

## Production rules

`ReflectionProtocols` recognizes only the exact Java descriptors of `Class.getMethod(String, Class[])`, `Class.getDeclaredMethod(String, Class[])`, `Method.getAnnotation(Class)`, `Method.isAnnotationPresent(Class)`, and `Method.invoke(Object, Object[])`. A certificate requires the annotation check to reference the same selected Method expression, its positive branch to reach invoke, its negative branch to be unable to reach invoke, and the gate to dominate invoke. CFG exception edges are retained. Method annotation values are not endpoint aliases.

Map field discovery reads the lookup/invoke expressions of certified plans. It does not collect every field in a forward callee closure. Actual registration writer summaries preserve receiver, key and handler expressions; argument order and concrete handler inheritance are not assumed. Such writers are executed only when their real calling context is reached. Virtual dispatch applies the same writer eligibility after resolving the actual receiver.

Inside the installed callback context, reads of those actual Map fields identify concrete map objects. A lookup result carries its own map identity, actual registration key and installed Client context. Reflective lookup receiver and invocation receiver must agree in object identity. The certificate is then checked against the actual Class[] descriptor vector, actual Object[] values, selected type, Java method name, visibility and runtime annotation. A dynamic method name may enumerate the exact compatible annotated shape within this bound handler only; it never expands into all implementations or map objects.

`getMethod` uses public hierarchy members. `getDeclaredMethod` uses only the declaring class and requires proven true `setAccessible` on that same Method, dominating invoke, before allowing nonpublic members. Selected versus actually dispatched member signatures remain distinct. Primitive reflective argument conversions are presently unsupported. Unknown, partial or wildcard parameter/argument vectors yield diagnostics rather than endpoints.

Facts are candidates and retain WebView, installed Client, registry and reflective target identities. An explicit WebView argument in the actual dispatch context retains its identity. Fact deduplication includes registry and target identities, preventing equal class/name/site from merging different handlers. Actual endpoint bodies are traversed only when otherwise capability relevant.

## Limits and cost

Protocol metadata introduces no global seeds or carrier propagation through SDK restore code. Reachability has depth 8 and 128 visited methods; expression field discovery has depth 12. Existing summary, array, context and host budgets remain unchanged. Exhaustion is diagnosed, not interpreted as proof that no transport exists. Discovery runs at most once after completion; repeated writer queries reuse its results. Diagnostic records are bounded to 128 reasons plus one overflow marker.

CFG construction, CFG reachability and global metadata scans cooperate with the existing analysis deadline. Expired or truncated summaries cannot become successful certificates. Interrupted discovery is not marked complete. No analysis budget or deadline was enlarged.

## Validation

`InstalledReflectionFixture` builds ordinary public prompt contracts without business class or field signatures. Two installed prompt Clients and one installed console Client each capture a distinct router, each router has a distinct map and same-name registration with a distinct handler. The console callback carries no WebView argument and reaches the router through its captured actual WebView field. Actual annotation-guarded invoke exposes exactly one compatible public endpoint per WebView.

The fixture rejects an uninstalled third Client/router, an installed incompatible handler, swapped endpoint parameter shapes, nonpublic public-lookup members, unannotated members and an annotation check that does not control invoke. It verifies an unused annotation value never renames the Java endpoint. Structural tests distinguish declared lookup from public hierarchy lookup, require same-selected-Method accessibility, test expired deadline diagnostics, and ensure repeated writer queries perform no additional discovery scan.

The frozen v7 jar fails this new fixture with zero endpoints (`test/runs/generic-v8-reflection-v7-reproduction.log`). The current implementation passes complete capabilitySelfTest and compactReportTest (`test/runs/generic-v8-reflection-build.log`). These synthetic results establish implementation behavior, not ten-App recall gains.

## Deferred protocols

Aliases established while iterating `getDeclaredMethods` and later applied to a different lookup Method, Map<String,Method> registration, name/arity enumeration, primitive coercion and arrays with unresolved contents remain outside this first stage. Cross-method annotation proof is not inferred merely because matching APIs coexist. The Sohu ad alias registry and QQ enumeration evidence therefore must not be claimed fully recovered by this version. Console callbacks can use the same exact protocol when reached through their actual installed Client context; the end-to-end fixture exercises both prompt and console.

The independent v8 conditional-init, FieldHeap and public SDK contract changes have separate documents and validation. Export timing corrections from v5 concern measurement and do not count as analysis speed improvements.
