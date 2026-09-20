# YSP message registry pattern

Tencent News 7.9.50 uses a small synchronous map router behind the annotated `YspJSBridge.invoke(String)` method.

`com.tencent.news.ysp.c` is a DEX interface, not an abstract class. Its class flags are `PUBLIC INTERFACE ABSTRACT`, and its single endpoint is `c.ʻ(String, JSONObject): Object`. The four concrete `YspMediaPlayer$c/$d/$e/$f` classes implement this interface.

The router `com.tencent.news.ysp.f` owns field `ʼ: java.util.Map`, initialized with `new LinkedHashMap`. Registration method `f.ˋ(String,c)` has logical arguments 0=selector and 1=handler. In raw DEX they are `v3/p1` and `v4/p2`; `v2/p0` is the router receiver. The normal path executes exactly:

```text
iget-object v0, v2, Lcom/tencent/news/ysp/f;->ʼ:Ljava/util/Map;
invoke-interface {v0, v3, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
```

There is no cast, wrapper, lambda, delegate, or coroutine around either argument. The ignored `Map.put` result gives replacement semantics. A QFix redirect branch may return before the write, while the ordinary fallthrough performs it.

`YspMediaPlayer.ٴ(f)` directly allocates each handler and calls the register method with a literal selector: `videoStatus`, `videoProcess`, `resume`, and `getScreenSize`.

Inbound dispatch is also synchronous. `f.invoke(String)` parses the JSON, reads `method` and `params`, loads field `f.ʼ`, calls `Map.get(selector)`, casts the result to `c`, and directly invokes `c.ʻ(selector, params)`. It then wraps the returned object as a success JSON string. Missing handlers and exceptions produce error JSON.

There is no async, coroutine, or delegate node in this inbound chain. The separate `f.ʾ(String,String)` method posts a Runnable and later uses `evaluateJavascript`; that is the outbound native-to-JavaScript event path and must not be mixed into handler discovery.

Subsequent report inspection established that registry discovery did **not** miss these handlers: all four concrete implementations and complete signatures were already emitted. The score mismatch was caused by two distinct naming levels. `YspJSBridge` is the native `addJavascriptInterface` registration name for the injected `f` object. `videoStatus`, `videoProcess`, `resume`, and `getScreenSize` are selectors dispatched inside that same object; they are not native registration names.

The report therefore needs a transport binding for the injected object containing `YspJSBridge`, its WebView identity, and registration site. A message handler can match only when its selector matches the gold method name and its associated transport binding matches gold registration name `YspJSBridge`, with the same injected object and WebView identity.

The following remain useful generic scanner hypotheses, but they are disproved as the explanation for this sample's score mismatch:

1. accept `Map.put` interface calls on stable fields, including fields initialized with a concrete map elsewhere;
2. record key/value parameter expressions from the register method;
3. substitute caller literals and concrete allocations through argument positions;
4. inspect normal fallthrough after redirector guards;
5. preserve the `f` router receiver identity.

Abstract-class endpoint support is unrelated here because `c` is already an interface.

All 16 APK method descriptors and both APK field descriptors used by this rule were checked against `test/runs/symbols/news.jsonl` and found. The two `java.util.Map` method descriptors are platform declarations, so they are absent from the APK declaration export; their `invoke-interface` references were checked in the raw APK DEX disassembly.
