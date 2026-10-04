# v18 work in progress: use the necessary Method.invoke candidate index

The reflective dispatch certificate already requires an exact java.lang.reflect.Method.invoke call, a compatible Class method lookup, an annotation check and a controlling CFG gate. However, transport field discovery originally enumerated every indexed APK method before applying that certificate filter. Each worker could repeat this enumeration.

The first discovery pass now starts from the existing reverse-call index for the exact Method.invoke descriptor. It retains the existing candidate check and complete certificate validation, and observes the same deadline. The second pass retains its necessary referenced-field and Map.put filters. No registration is emitted merely because its method is an indexed candidate. No App names, new reflection signatures, SDK prefixes or broad namespace rules are introduced.

ReflectionDiscoveryPrefilterFixture keeps a real certified registry and65 unrelated Map methods. Against frozen v17, the new visitation assertion fails with68 method visits. With the change it requires exactly2 visits (certified consumer and writer), while retaining the actual writer and excluding unrelated methods from summaries. The complete capabilitySelfTest, compactReportTest and shadowJar suite passes in24 seconds. Evidence is in experiments/v18-reflection-invoke-prefilter.json and test/runs/generic-v18-reflection-candidates-tests.log.

This change is not in frozen v17. It does not repair absent startup writes, expand the reflection certificate, or establish real-App performance/recall gains. Index errors and overall deadlines remain diagnostics. The reverse index may contain extra candidates due to resolved aliases; the existing full certificate filters them before acceptance.
