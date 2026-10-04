# Null-container constructor and guard evidence

The container field is **public, mutable, non-static and non-final** (`access_flags=0x1`). This requires exact-instance state propagation; final-field rules alone do not establish it. The two inspected Activities create fresh five-argument Info objects. All seven declared constructor variants and actual raw DEX instructions are recorded in null-container-dex-evidence.json from the patched APK classes9.dex, with APK/DEX hashes.

The five-argument constructor writes null to v6 and forwards it to the sixth argument. The six-argument constructor receives its container in v14 but discards it: it writes null to v6 and zero to v7, then calls the seven-argument constructor with v0..v7. The seven-argument constructor stores p6/v6 into the actual receiver's container field. Source-wide assignment search finds no second field writer. All helper overloads preserve the same actual Info object and the two Activity call paths do not supply a callback.

`forwardToSv` reads the field with `iget-object v1,v4` at code-unit offset71. `if-eqz v1,+21` at73 jumps to94. This skips new-instance75, WebView ctor77, addView87 and loadUrl90. The target's container is already known null on the two actual construction paths. The source's action selector is query parameter `action`, with action4 selecting the guarded branch; the companion `url` must be nonempty.

A reusable minimal fixture can preserve the following structure, with an Android layout-owned ViewGroup for the positive control:

```java
class Info {
  ViewGroup container;                 // deliberately mutable
  Info(String a, String b, String c, String d, int flags) {
    this(a, b, c, d, flags, null);
  }
  Info(String a, String b, String c, String d, int flags, ViewGroup ignored) {
    this(a, b, c, d, flags, null, 0);  // actual APK discards this argument
  }
  Info(String a, String b, String c, String d, int flags, ViewGroup value, int kind) {
    container = value;
  }
}
void forward(Context context, Info info) { guarded(context, info); }
void guarded(Context context, Info info) {
  if (info.container != null) {
    WebView view = new WebView(context);
    info.container.addView(view);
    view.addJavascriptInterface(new PublicAnnotatedBridge(), "test");
  }
}
```

Negative controls: construct Info with five arguments, or with six arguments including a nonnull group (the six-argument constructor still discards it). Positive control: directly use the seven-argument constructor with a real owned group. A second positive control can explicitly write `info.container=ownedGroup` before forward, proving that the generic engine does not assume every mutable container is permanently null. Unknown method-entry Info may remain a candidate but must not justify a concrete negative constructor receiver. No package/class-name rule is needed.

This proves the source/DEX false path. It does not prove where the engine loses the field/constructor state, nor that the entire Activity lacks any other WebView path.
