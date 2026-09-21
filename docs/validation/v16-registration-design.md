# v16 actual asynchronous entry model (development)

The v15 near-callback negative proves constructor-only callback execution unsound. The repair
retains constructor capture/relevance, but no longer calls seed() merely because the constructed
type is Runnable/Callable or has callbackEntries. Actual synchronous interface invocations still
use ordinary forward dispatch. Existing WebViewClient/WebChromeClient registration remains intact.

Framework adapters enqueue only the concrete registered argument and named interface members:
Handler/View post variants, Activity.runOnUiThread, Executor.execute, Context.bindService, and
structurally recognized okhttp3 Call/Callback pairs. Obfuscated network contracts require one
callback argument, exactly two callback members with the same Call first argument, IOException
failure and Closeable response. API names alone cannot qualify. Application scheduling overrides
are followed through their implementation; platform semantics apply at the eventual framework call.

All modeled asynchronous successors are candidates with a conditional_async_registration evidence
step. Network success, posting acceptance, service connection, order and cancellation are not proven.
The adapters do not establish the package-ready/service-ready join needed by Diana; nor do they
provide complete external framework callback coverage. Unknown callback receivers are diagnosed.

Tests preserve the previously failing near-API callback construction case and posted-vs-unposted
Runnable receiver isolation. The old callbackEntryIsolation fixture asserted that mere allocation
executes onReady; its original input is retained but its assertion is corrected to require no facts.
Actual synchronous callbacks remain covered by CallbackArgumentFixture. Additional negatives reject
an unrelated post name, a concrete application override returning false, and a structurally invalid
network response contract.

v16a frozen development APK runs omit only the subsequent app-override adapter guard. v16b adds
that guard and contract fixture assertions; final iteration results must identify the exact jar.
No final acceptance is claimed.

Further development variants: v16c makes bounded actual-argument lookahead recognize a known
registration inside an otherwise irrelevant forwarding wrapper; a posted-vs-unposted wrapper
fixture passes. v16d restores relevance of callers of near-capability application contracts without
seeding concrete implementors. Runtime object/field dispatch is still required. A synthetic page
interface -> concrete page -> wrapper constructor -> initializer fixture verifies this distinction.
Lazy.getValue now schedules the concrete stored initializer body as well as evaluating its return;
a separate fixture proves one consumed Lazy emits its bridge while the other unused Lazy does not.
The previous near-callback reproducer's extra assertion that its forwarding helper must remain
globally irrelevant is removed from the integrated test: the new contract relevance intentionally
changes that property. Its core two-WebView isolation and one-bridge assertions remain intact.

Runtime failures are retained in v16d-runtime-failures.json. News v16d failed after 2233/2329
Activities because a nullable Long in the dynamic attachToRoot ternary was implicitly unboxed.
v16e boxes both fallback operands and adds a real dynamic-attachment regression case. Ctrip v16d
reached the internal deadline after 396/410 Activities; its valid partial report is not a performance
or quality pass. A subsequent v17 expression-work budget is a separate prototype, not part of v16e.
