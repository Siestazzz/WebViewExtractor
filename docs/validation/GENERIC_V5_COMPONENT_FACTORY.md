# Generic v5 component factory model

The source-confirmed motivating chain is documented in `ctrip/v17-tour-fragment-factory.md`: Class.getName → a constructed descriptor's class-name/Bundle fields → the same pages collection passed into a FragmentStateAdapter → actual ViewPager2.setAdapter → createFragment(position) → Fragment.instantiate. No application name participates in these rules.

## Protocols and actual objects

Only `java.lang.Class.getName()Ljava/lang/String;` evaluated on an existing class value yields its binary class-name constant. This operation creates no component or lifecycle root. Unknown class values remain unknown.

The exact native, AndroidX and support-v4 Fragment.instantiate Context/String overloads construct only a known, public, nonabstract compatible Fragment class with its own public no-argument constructor. Unknown or incompatible names do not expand into arbitrary subclasses; gaps include `fragment_factory_dynamic_name`, `fragment_factory_type_unresolved` and `fragment_factory_constructor_unresolved`. A real supplied Bundle is stored on that factory object and is available through the modeled framework getArguments call.

Factory identity contains its invocation offset, the existing bounded receiver allocation context and resolved type. The actual constructor is materialized using existing budgets. As in the prior allocation model, lifecycle traversal after construction is conditional candidate evidence, not proof that construction caused attachment. An instantiated but unattached Fragment is therefore not asserted to have completed every lifecycle callback.

Only the exact ViewPager2.setAdapter(RecyclerView.Adapter) API installs the actual argument object in this model. A known FragmentStateAdapter receiver supplies its own resolved createFragment(int) callback. Its position remains unknown and the callback path is conditional on a framework request; the model does not assume every collection position is requested. Its returned concrete Fragment is entered through the existing exact lifecycle signature rules, with `installed_fragment_adapter` and `adapter_fragment_result` evidence paths. Uninstalled adapter allocations do not run createFragment. Other adapter implementations do not trigger a guessed Fragment factory.

Both recognized SDK APIs are terminal modeled contracts in processJob; their library implementations are not traversed again for the same call. This is an API-specific boundary, not a blanket AndroidX exclusion.

## Dependency propagation

Protocol callers join ordinary reverse relevance, but are not direct capability-priority seeds. Actual FragmentStateAdapter constructors are binding carriers; relevant adapter collection fields preserve their actual writers. Fields referenced by a method directly invoking the exact Fragment factory preserve their actual writers and constructor capture. This lets real descriptor class-name values survive field storage without restoring arbitrary constructor inference on unresolved field owners. These dependencies alone do not execute an adapter callback.

## Reproduction and tests

FragmentFactoryFixture creates distinct adapters with separately constructed List<Descriptor> objects. A descriptor stores Class.getName and creates the Fragment using its actual Context. Two installed adapters produce two different WebViews through official lifecycle callbacks. A third adapter is constructed but never installed; a fourth uses an unresolved dynamic name; and another Fragment appears only as a class literal. Only the two installed constant-name paths emit their bridges. The dynamic path records its unresolved-name gap rather than guessing a class.

The same fixture fails against frozen v4 with `Actual adapter installation never reached Fragment lifecycle`, log `test/runs/generic-v5-factory-repro.log`. The strengthened fixture and full capabilitySelfTest/compactReportTest passed in `test/runs/generic-v5-factory-build.log`. Final integrated validation will include the coordinating agent's independently added SDK DownloadListener contract tests.

No new context, time, dispatch or summary budget is introduced. Actual APK golden recovery still requires a frozen run; this document makes no recall claim from the synthetic tests. FragmentTransaction attachment, restored state, arbitrary FragmentFactory APIs, scalar getters outside this captured dependency pattern and adapter item-count/position feasibility remain boundaries requiring their own protocol evidence. The coordinating agent separately corrected duplicate nested export timing in Main; that accounting change is not an analyzer speedup and historical frozen timing values are not rewritten.
