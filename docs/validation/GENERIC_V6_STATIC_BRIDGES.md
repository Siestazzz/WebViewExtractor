# Public static Java bridge endpoints

Independent Sohu source audit identified156 missing expectations across26 verified hosts: public static annotated methods in actually injected objects. Static members are not virtual Client callbacks, but Java bridge reflection has a different contract.

The coordinating agent verified the Chromium implementation at [fixed revision2f38a9ff](https://chromium.googlesource.com/chromium/src/+/2f38a9ff1e78023cc3fa341a30448d1b9ca5a891/content/browser/android/java/gin_java_method_invocation_helper.cc). Lines130–155 select the class reference for a static method; lines275–280 invoke the static JNI void-method entry, and other return categories have analogous static calls. This is platform evidence, not an App-specific exception. The downloaded source SHA256 is recorded in generic-v6-static-bridge-tests.json.

bridgeMembers now retains public static annotated endpoints alongside public instance endpoints. Existing annotation/target-SDK checks remain; private/protected methods remain excluded. Inherited members retain full defining signatures, and a hidden same-shape parent member is not duplicated. Standard Client callback matching still rejects static methods. Registration names, object identities and WebView ownership are unchanged.

StaticBridgeFixture performs actual DEX registration, tests inherited public static plus instance members, static hiding, and private/protected/unannotated negatives. Frozen v5 fails with only the instance endpoint; current code passes. This is a focused regression result, not proof of full ten-App recall. Real-source facts are retained in sohuvideo/canonical-shared-sdk-surface-report-guided.jsonl; actual restored matches require the next frozen APK run.
