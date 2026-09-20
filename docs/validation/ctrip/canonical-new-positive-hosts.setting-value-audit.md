# Setting value-kind audit for the 13-host expansion

All 90 added setting facts were rechecked against their normalized API argument descriptor and source expression. This audit updates only the pending expansion file; it does not edit canonical.

Classification rule:

- `enum` requires a WebSettings nested-enum parameter descriptor, such as `WebSettings$PluginState`, `$RenderPriority`, or `$LayoutAlgorithm`.
- `literal` covers primitive constants and source string literals. Capitalization is irrelevant.
- `dynamic` covers a runtime call/expression whose result is not statically equal to one literal.

The corrected distribution is 77 literal, 9 enum, and 4 dynamic facts. The only incorrect `value_kind` was Kwai `setDefaultTextEncodingName(Ljava/lang/String;)`: source line 70 passes the string literal `"UTF-8"`, so it is now `literal` with `source_expression="\"UTF-8\""`. Uppercase text does not make a Java string an enum.

The nine enum facts all have enum-typed descriptors: `PluginState` values `ON`, `ON_DEMAND`, or `OFF`; `RenderPriority.HIGH`; and `LayoutAlgorithm.NARROW_COLUMNS`. No `Ljava/lang/String;` argument remains classified as enum.

Four facts were already correctly dynamic, but lacked their source expressions. The pending file now records Sina `e.h()` for the user agent and the Tencent face-protocol `getDir(...).getPath()` expressions for app-cache, database, and geolocation database paths. These expression enrichments do not turn dynamic values into static expected constants.

`canonical-new-positive-hosts.setting-corrections.jsonl` is the machine-readable correction list: one classification correction and four dynamic-expression enrichments. The pre-correction state remains recoverable from `canonical-new-positive-hosts.pending-v1-report-assisted.jsonl` and repository history; canonical remains untouched by this audit.
