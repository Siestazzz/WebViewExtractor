# Mango v13e final audit

The fixed v13e report has SHA-256 `70203f4f0c6266c44ba36857532ecbf3692218cbe93f873b5f4236c21a9cc6d4`, status `partial`, 87 hosts, and internal wall time 346.49 seconds (orchestration measured about 348.16 seconds). Its host set is identical to v13d. The delta excludes `xml_binding_evidence` and `xml_binding_semantics` while retaining receiver ID/type, API, implementation, members, arguments, and values.

Following actual WebView API overrides recovers 1,045 net facts across many existing hosts, including XWeb +14, WebContainer +33, MGVideoPlay +62, and LiveRoom +267. This does **not** restore the five disappeared MiniApp hosts, and it restores none of the independently sourced Diana81 for MGVideoPlay. Strict replay is unchanged from v13d: 574/608 settings, 597/634 callbacks, 1,137/1,163 bridges, and 3/3 operations. The five absent hosts therefore remain real regressions.

The earliest missing generic edge is an application callback contract rather than a WebView override:

1. A host creates and attaches `MiniAppFragment`; its view path constructs `DianaView`.
2. `DianaView.renderMiniApp(FrameLayout)` constructs `new AppService(this)`, stores it in `mAppService`, adds it to the frame, and constructs `mPageManager` (`DianaView.java:763-768`).
3. `AppService(DianaView)` stores the exact same object in final fields `dianaView` and `mEventListener` typed `com.mgtv.diana.g0.g`; it initializes the JS engine, calls `addInterceptor(this)`, and registers itself as the Diana facade event sender (`AppService.java:59-69`).
4. `AppService.publish(String,String,String)` dispatches across its `List<com.mgtv.diana.s0.a> mInterceptors` and invokes `onInterceptServiceEvent` (`AppService.java:275-308`; JADX rendered one null branch incorrectly, so the list iteration is the stable semantic evidence).
5. `AppService.onInterceptServiceEvent(String,String,int)` matches the exact event constant `DianaEventDefine.Service2PageEvent.ON_SERVICE_READY == "onServiceReady"`, then calls private `onEventServiceReady(String)` (`AppService.java:261-268`).
6. `onEventServiceReady` initializes configuration and calls `mEventListener.onServiceReady()` (`AppService.java:140-146`). Because the constructor fixed that field to its `DianaView` argument, the interface dispatch target is the same `DianaView` instance.
7. `DianaView.onServiceReady()` checks `mPageManager`/`mAppConfig`, derives the entry path, then calls `mPageManager.a(entryPagePath, this, new h())` (`DianaView.java:1490-1517`). That call creates the Page/PageWebView chain documented in the source oracle.

A generic repair should model exact callback registration and field identity: constructor parameter → final interface field, `addInterceptor(this)` into the particular list instance, iteration → interface method, exact constant predicate, field read → interface callback. It should enqueue only the concrete stored target (`DianaView`) and only the invoked contract member (`onServiceReady`), rather than all `on*` methods or every interface implementor. The path is conditional on the JS event and configuration, so resulting host facts remain candidates.

All 87 emitted-host ownership decisions reuse v13d evidence. The five source-proven conditional hosts remain absent and are recorded as regressions rather than invalid hosts. The report still has large receiver/candidate churn across 53 hosts, so its net growth is not a count of new verified APIs.
