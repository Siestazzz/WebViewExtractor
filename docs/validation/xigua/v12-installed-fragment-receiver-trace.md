# Installed Fragment receiver trace

The full installed factory chain does **not** enqueue `LifecycleFragment.onActivityCreated(Bundle)`. No competing lifecycle receiver ID was found. Actual allocation, the queued `setDelegate` receiver and the delegate heap key all share:

```text
Lcontainer/Host;->onCreate(Landroid/os/Bundle;)V@34|activity:container.Host
```

The same receiver's `delegate` field contains the actual Dispatcher allocated at Host.onCreate offset29. Thus assignment itself survives. `componentEntry` is true, but the lifecycle method is absent from static relevance and `relevantOnReceiver(actualFragment)` returns false. The inspected experimental Engine relevance walk accepts receiver `param0` calls, while this callback dispatches through `this.delegate`; this explains the missing lifecycle job without inventing an identity mismatch.

A diagnostic intervention waits until that actual delegate heap entry exists, then enqueues the callback with the same actual receiver ID. With the independently experimental constructor-capture Engine, it recovers one bridge, one settings registration and one callback, with no diagnostics. Normal frozen and experimental full-chain runs produce zero facts and no lifecycle job. This is an intervention proof of the isolated boundary, not a production fix or actual APK recovery.

Compile and reproduce:

```sh
javac -cp test/runs/generic-v12.jar -d test/runs/xigua-fragment-trace-v12/classes src/test/java/org/example/ServiceConstructorArrayProbe.java src/test/java/org/example/LifecycleContainerFactoryProbe.java test/diagnostics/FragmentReceiverTrace.java
java -cp test/runs/xigua-fragment-trace-v12/classes:test/runs/lifecycle-root-diagnostic/experimental:test/runs/generic-v12.jar org.example.FragmentReceiverTrace installed
java -cp test/runs/xigua-fragment-trace-v12/classes:test/runs/lifecycle-root-diagnostic/experimental:test/runs/generic-v12.jar org.example.FragmentReceiverTrace installed inject
```

A generic correction should connect actual installation provenance to lifecycle relevance through real field-held delegate receivers. The separate `fragment-uninstalled` direct-content negative already emits three candidate facts; simply seeding every allocated Fragment would preserve or enlarge that precision error. The original fourteen-case matrix, source oracle and production code remain unchanged. Raw traces and all artifact hashes are in the sibling JSON. Actual App budget and further gaps remain unproven.
