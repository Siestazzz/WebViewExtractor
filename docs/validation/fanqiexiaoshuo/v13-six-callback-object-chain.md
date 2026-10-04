# Fanqie v13 six callback misses: bound-object diagnosis

All six frozen positives remain. Fresh fallback source proves Activity.D2 → lazy layout → new SwipeRefreshWebView → actual Placeholder → inherited base.a WebView field → factory-created ReadingWebView → Swipe final M → Q with nonnull new Swipe Client. ReadingWebView wraps it in m, stores the actual delegate in inherited r64.j.f364446a, and forwards through its WebView superclass API chain. Existing independent installation-versus-cleanup proof was reused after source SHA validation; the sibling JSON adds exact source lines, six complete callback signatures and current report evidence.

Four misses are **not callback member recognition failures**. V13's Q registration already lists Swipe Client.onPageFinished, onPageStarted, onReceivedSslError and modern onReceivedError with their exact gold signatures. Its receiver is instead a `field_object` of generic `android.webkit.WebView`, keyed by the Placeholder's inherited base.a field. It fails the source-backed ReadingWebView/e/d type constraint and cannot establish the subclass override wrapping. The two m callbacks have no corresponding installation on the proven fresh receiver.

Wrapper inheritance is m → Reading.o → r64.j → WebViewClient. Actual delegate inheritance is Swipe.c → r64.g → WebViewClient. m.onPageFinished invokes super; o invokes super; j forwards the same WebView/String to its captured delegate. m's modern request intercept likewise delegates through super on the appropriate paths. WebView inheritance is Reading → webx.core.webview.c → WebViewContainer → WebViewContainerInner → platform WebView. Container source contains both native-super and internal-listener branches; source evidence does not assume a single unconditional native route.

A new generic DEX diagnostic, `InheritedWebViewWrapperProbe`, preserves inherited holder storage, super getter, nonnull override wrapping, field delegate forwarding and null reset. Both direct and inherited-holder positives recover wrapper/delegate on frozen v13; cleanup(null) produces zero callbacks. Therefore simple inherited field storage alone does not isolate the real failure. Holder output retains `previous_phase_not_rederived:1` and duplicates; it is not a precision acceptance fixture.

Compile and run:

```sh
javac -cp test/runs/generic-v13.jar -d test/runs/fanqie-holder-probe-v13/classes src/test/java/org/example/ServiceConstructorArrayProbe.java src/test/java/org/example/InheritedWebViewWrapperProbe.java
java -cp test/runs/fanqie-holder-probe-v13/classes:test/runs/generic-v13.jar org.example.InheritedWebViewWrapperProbe
```

Preserve the old cleanup counterexample: cleanup mode3 passes null; Reading's null branch calls super(null) and returns before constructing m. Earlier v7 matches through that branch were false evidence and must not be restored to satisfy recall.

Actual host traversal is incomplete: v13 processes2620 contexts and retains5571, with zero priority contexts. This does not prove the remaining misses are wholly budget-caused or wholly semantic. The precise actual field write/return identity boundary is pending a bounded single-host heap trace; no production edits or frozen oracle changes were made.


A further generic source shape precedes the holder: NsCommonDepend.IMPL is initialized by ServiceManager.getService of the literal service Class. Its cache/creator miss calls a large generated factory containing `if (cls == literalClass)` branches; the target branch directly constructs NsCommonDependImpl at zp0.a.java842–843. A formatted-name reflective proxy is a separate fallback. Source lines and hashes are added in JSON.

`ClassKeyFactoryProbe` independently passes an actual known Product0 Class into a generic Class-reference factory. Frozen v13 returns Product0, Product1 and null for two branches, and unknown for eighty branches. DexFlow's equality evaluator only handles numeric literals; abstract union overflow then loses concrete return identity. This proves a reusable semantic gap, but does not yet prove its exact causal share in the actual App's unfinished traversal. No App signatures appear in this fixture.

Both real single-host trace attempts yielded zero Activity jobs: the first exhausted the combined deadline during indexing; the second completed indexing119.550s but exhausted its fresh60s engine budget before Activity work through bootstrap initialization. They are retained as invalid holder diagnostics and provide no actual-heap negative evidence. Actual field-return trace remains outstanding. Do not claim the Class-key correction alone recovers all six facts.

Reproduce the Class-key diagnostic separately:

```sh
javac -cp test/runs/generic-v13.jar -d test/runs/fanqie-class-key-probe-v13/classes src/test/java/org/example/ServiceConstructorArrayProbe.java src/test/java/org/example/ClassKeyFactoryProbe.java
java -cp test/runs/fanqie-class-key-probe-v13/classes:test/runs/generic-v13.jar org.example.ClassKeyFactoryProbe
```

The minimal correction boundary is precise identity comparison for known Class constants, while preserving unresolved loader/dynamic Class cases. If contextual decoding is skipped for a large dispatcher, that independent size boundary must remain visible rather than claiming all generated factories are resolved. The old null-reset counterexample and holder's provisional/duplicate output require preservation during any correction.

Actual patched DEX directly confirms the generated factory is public static with1992 instructions,398 Class constants and398 binary equality/inequality branches. This exceeds the contextual500-instruction gate; Class identity support alone in a small fixture is insufficient to prove actual factory recovery. DEX entry hash and metadata output are bound in JSON.
