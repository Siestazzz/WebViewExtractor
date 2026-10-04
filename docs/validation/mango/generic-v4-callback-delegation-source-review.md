# Generic v4 callback delegation regression

This review uses the previously authorized Mango development source, APK SHA-256 `65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5`. It does not reverse v4's empty API override fix or claim that every golden callback was source-confirmed independently.

Source root: `test/decompiled/com.hunantv.imgo.activity/sources/`.

`com/mgtb/money/web/webview/ProgressWebView.java:244-261` constructs an actual WebClientImpl holder, then installs its inner clients through superclass setters. The overriding setters at 462-468 do not call a superclass setter: they pass the supplied user client to that same holder. `WebClientImpl.java:807-812` stores the client in mWebChromeClient/mWebViewClient without installing it directly on a WebView.

The installed ProgressWebChromeClient retains the actual outer holder. Its methods at 86-156 delegate standard callback calls to the stored user client, guarded by a non-null check, and fall back to superclass behavior when absent. Examples include getDefaultVideoPoster, getVisitedHistory, onCloseWindow, onConsoleMessage and onCreateWindow. Consequently the supplied client has real callback behavior through a wrapper, although it is not the object directly installed by the framework setter. Emitting the original overridden setter as a native installation would conflate those two relationships and would reintroduce the confirmed empty-override bug.

Source SHA-256: ProgressWebView `4f9a3b806a326a6ab5495d46326739a389e1ac6170c28d4ffbeca9cf4a9c31b1`; WebClientImpl `d5580f58ceae24fc12ab3a7fbcf5a52cfc6e39185092f8a8ac6ac1697ae5c5bb`.

## Next-version fixture and model plan

Use a synthetic installed SDK client wrapper capturing a real holder object. A custom WebView override stores a supplied delegate in that holder; an official wrapper callback invokes the same signature on the stored delegate. Only this actually invoked callback relationship should propagate the installed WebView association. The production model must validate complete standard callback contracts and actual receiver objects; no private field, holder, application or callback name is needed.

Positive cases should include callbacks both with a WebView argument and without one, adjacent field setter/getter capture, inherited callback implementation and actual virtual dispatch. Two different holders, delegates and WebViews must remain isolated. Negative cases should include an uninstalled wrapper, an unused stored delegate, an empty callback override, an unrelated same-name method and a delegate placed in another holder. A nullable delegate keeps a conditional path, and unknown receiver origins retain diagnostics rather than joining every instance of the same type.

v5 deliberately freezes the factory and SDK DownloadListener changes first. This delegation model is not implemented in v5 and no callback recovery is claimed from this source review alone. The frozen v4 delta records the actual ThirdWebActivity/ThirdFullWebActivity callback losses and remains the regression evidence for the subsequent fixture.
