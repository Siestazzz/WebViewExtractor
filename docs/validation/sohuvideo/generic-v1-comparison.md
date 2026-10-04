# generic-v1 comparison (running snapshot)

The initial evaluation read a running snapshot at roughly 266 seconds. The core WebViewActivity was pending deep analysis (1,408 contexts processed, 2,330 pending) and contained three emitted facts. The snapshot therefore cannot be used to conclude omission or accuracy. Final scoring must replace this snapshot result after the runner completes.

Independent source patterns for later diagnosis: fragment virtual initJsBridge/initChromeWebViewClient/initWebViewClient factories; custom WebView.setWebChromeClient adding JavaScript settings; lazy bridge registration during loadUrl/loadData; inherited ChromeClient/ng callbacks; resource-inflated custom WebView ownership. ExerciseWebViewFragment and WebViewPgcFragment override factories, so base implementations must not be copied without virtual dispatch and per-host evidence.

## Reusable pending-path diagnosis

At 463.5 seconds the core host still had 2,135 processed contexts and 2,799 pending, phase 1, despite source-visible direct initWebSetting capability calls. This is evidence of unfinished traversal, not proof of semantic resolution failure. Production `CapabilityEngine.seed` (597–636) schedules relevant component methods broadly; `enqueue` (657–661) appends FIFO jobs; `Main` (83–86) gives each host 100 contexts per round. A generic capability-distance priority within each host could surface capability-bearing initialization before secondary UI helpers without dropping pending work. It requires a synthetic regression with many relevant helper contexts and a short lifecycle-to-configuration chain.

A separate semantic regression pattern should use a base fragment with virtual `initBridge` / `initClient`, a derived fragment replacing both returned implementations, and a custom WebView override of `setWebChromeClient` that calls `getSettings().setJavaScriptEnabled(true)`. Host-specific binding must follow actual virtual targets; client and bridge inherited exposed signatures must use declaration owners. A second test should load a custom WebView that lazily registers an otherwise empty bridge: registration is still a fact even when exposed member count is zero.
