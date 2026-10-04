# Empty virtual API override: confirmed capability false positives

Patched Sohu source `AuthWebView.java:91` declares a final, empty override of `addJavascriptInterface(Object,String)`. `NBSWebLoadInstrument.java:112-113` calls this virtual API through an android.webkit.WebView parameter. Both LoginWebViewActivity and BaseWebViewActivity construct AuthWebView and pass it into this helper. The concrete override performs no framework call and cannot install the NBS bridges. Its separate addBridgeObject map (line 87) is a different mechanism and does not rescue these registrations.

The attached v2 capability_precision JSONL records six wrong bridge registration facts, exact APK/report hashes, concrete receiver and call-site evidence. Both Activities do own their WebViews: this is not an activity_host rejection, and six checked registration facts do not establish whole-report precision.

Generic engine recognized-API handling emits the base effect before resolving the concrete virtual target, then schedules followApiOverride. An empty override therefore cannot suppress the already emitted base effect. Resolve dynamic dispatch first: when an app override is selected, analyze its body, and emit the framework effect only if an actual framework implementation call is reached (including invokespecial/super). For unknown receiver alternatives preserve conservative alternatives and their uncertainty, rather than package-specific filtering. This rule also needs scrutiny for client registrations, bridge removals and overridable settings methods.

Minimal reusable structure:

```java
class GuardedWebView extends WebView {
  @Override public final void addJavascriptInterface(Object b, String n) {}
}
void helper(WebView w) { w.addJavascriptInterface(new Bridge(), "b"); }
void entry() { helper(new GuardedWebView(context)); }
```

Expected: entry owns GuardedWebView but has no injected Bridge registration. A companion override that calls super must preserve that registration and any extra effects. Do not key the fix on any source class/package name.
