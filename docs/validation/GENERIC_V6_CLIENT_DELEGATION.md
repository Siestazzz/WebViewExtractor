# Generic v6 installed-client delegation

v4 correctly stopped emitting native API effects before known application overrides, but this exposed a legitimate callback composition gap. Mango's ProgressWebView installs an internal client, while its setter stores the supplied user client in that internal client's captured holder. The installed client's actual standard callbacks then invoke the stored delegate. Source paths, lines and hashes are recorded in `mango/generic-v4-callback-delegation-source-review.md`. Fanqie's ReadingWebView uses the same general structure; the independently checked proof is `fanqiexiaoshuo/installed-client-delegate-source-proof.json`, including the modern onReceivedError overload as well as its deprecated counterpart.

## Actual callback path

A native recognized client installation creates a context containing that exact installed client object, setter site and WebView. Only a complete standard callback entry on this actual object may begin the delegation traversal. The existing signature contracts continue to exclude static/private methods and wrong overloads. A bounded call search (eight levels, 128 methods) determines whether an installed callback can reach a standard delegate call through ordinary helpers or inherited superclass calls. Budget exhaustion records `client_delegation_search_budget`; it does not activate all methods.

This search is metadata, not a global relevance root. Helpers become eligible only while processing the actual installed callback context. The context travels as an internal extra job argument, so queue de-duplication distinguishes the same no-WebView-argument callback object installed on different WebViews. It does not change DEX parameter positions or invent a runtime argument. Context is cleared after every processed job.

At an actual virtual standard callback call, normal expression evaluation resolves the field receiver and exact implementation. The resulting callback fact includes only that called standard method, plus the same-signature application superclass methods explicitly invoked by super. It does not expose all members of the delegate merely because one callback was invoked. A callback WebView argument preserves its actual call identity; callbacks lacking one retain their installed-path association. Facts record the delegate and installed-client object IDs and `client_delegate` evidence, with candidate binding because framework invocation and branch feasibility remain conditional. Unresolved receivers record `client_delegate_unresolved`; unrelated instances of the same class are never merged.

Actual standard Client constructors now preserve their captures, and objects holding standard Client fields can preserve actual constructor writes. Unresolved field owners still do not trigger speculative constructors. The original overridden setter API is not restored; empty overrides remain suppressing behavior.

## Precision fixtures

ClientDelegationFixture fails against frozen v5 with `Installed wrapper actual field delegate has no callback capability` (`test/runs/generic-v6-delegation-repro.log`). Its positive path uses an installed inherited wrapper, a separate holder field, an explicit superclass callback, a private helper and a callback without a WebView parameter. Two independent holders/clients and three WebViews are kept separate, including one wrapper installed on two distinct WebViews.

An uninstalled third wrapper does not activate its delegate. A wrong-signature overload containing a delegate call is not a framework entry. An empty standard wrapper callback and the delegate's unused standard callback do not create delegation facts. A same-class receiver union on one WebView preserves two distinct delegate object IDs in fact de-duplication. Existing private/static standard-client negatives and empty API override tests remain enabled.

## v5 framework-factory relevance regression

The v5 ten-App run exposed a large cost regression: framework Fragment restore code invoking instantiate became a reverse relevance root, and factory-field/carrier metadata reinforced the same SDK state-machine expansion. The v6 change excludes public framework implementations in `android.*`, `androidx.fragment.*`, `androidx.viewpager2.*` and `android.support.v4.app.*` from protocol-only global roots and factory-field/carrier additions. Ordinary real capability relevance is retained; this is not a blanket traversal exclusion for those libraries. Exact APIs still resolve at an actual reached call, and the prior modeled terminal API boundaries remain.

FrameworkFactoryBoundaryFixture reproduces the v5 error: a synthetic SDK restore helper's instantiate call made the manager state machine and its caller globally relevant. Its v6 assertions reject both the root closure and the factory String-field/carrier metadata. Log: `test/runs/generic-v6-framework-root-repro.log`. FragmentFactoryFixture still preserves the application List<Descriptor> → actually installed adapter → instantiate positive and all uninstalled/dynamic/class-literal negatives.

## Validation

Full `capabilitySelfTest` and `compactReportTest` passed after the final changes, including the coordinating agent's public annotated static bridge fixture (eight seconds, `test/runs/generic-v6-final-core-build.log`). Static Java bridge exposure is independent of virtual Client callback contracts; static Client methods remain excluded. No time, queue, summary or dispatch budget was increased. Real Mango/Fanqie recovery and v5 cost-regression recovery require the new frozen ten-App run; source and fixture confirmation do not imply complete golden recovery.
