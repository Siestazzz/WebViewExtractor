# Diana mini-app-info request callback chain

`DianaView.requestMiniAppInfo(new DianaView$c())` has a fully explicit registration-to-dispatch path. The callback must be followed through the builder, OkHttp callback, and main-thread Runnable; allocating `DianaView$c` alone does not justify seeding all its members.

1. `DianaView.startLoad()` creates `new DianaView$c(this)` and passes it to private `requestMiniAppInfo(com.mgtv.diana.h0.c$InterfaceC0712c)` (`DianaView.java:1720-1733`). The inner object captures the exact outer `DianaView` in its synthetic outer field.
2. `requestMiniAppInfo` constructs `com.mgtv.diana.h0.c$d$a`, calls its overload `a(InterfaceC0712c)`, then `a()` to build request object `d`, and passes that object to singleton `com.mgtv.diana.h0.c.b().a(d)` (`DianaView.java:779-783`).
3. Builder method `c$d$a.a(InterfaceC0712c)` stores the callback in builder field `f48475h`. `c$d$a.a()` creates `d` and copies that exact reference into request field `d.f48467h` (`h0/c.java:180-257`). This is the registration edge.
4. Manager `c.a(d)` builds the mini-info HTTP request and creates `new c$a(d)`. The adapter captures the whole request object in final synthetic field `c$a.f48456a`, then registers itself through `com.mgtv.diana.o0.e.a().a(Request, okhttp3.f)` (`h0/c.java:294-321`).
5. Network manager overload `o0.e.a(Request, okhttp3.f)` creates an `okhttp3.e` Call and invokes `Call.H(fVar)` (`o0/e.java:452-459`). This is the reusable OkHttp asynchronous registration entry: callback argument 1 becomes the receiver of `okhttp3.f.onResponse(Call,Response)` or `onFailure(Call,IOException)`.
6. `c$a.onResponse` parses `MiniAppInfoResponse`. A valid response constructs `MiniAppInfo` and calls manager helper `c.a(d.f48467h, miniAppInfo)`; all error branches call the analogous failure helper with the same field (`h0/c.java:35-91`). `c$a.onFailure` also reads `d.f48467h` and invokes the failure helper.
7. Success helper `c.a(InterfaceC0712c,MiniAppInfo)` creates `ny.a`, which captures the exact callback and result, and posts it through `o0.e.a(Runnable)`. That method calls its main-looper `Handler f48732c.post(runnable)` (`h0/c.java:332-340`; `o0/e.java:408-410`).
8. `ny.a.run()` calls static `c.b(callback,miniAppInfo)`, which null-checks and invokes only `InterfaceC0712c.onSuccess(MiniAppInfo)` (`h0/c.java:322-328,332-340`). For this registered instance, virtual dispatch reaches `DianaView$c.onSuccess`, which calls the captured outer `DianaView.onSuccess`, initializes its facade, and calls `bindService` (`DianaView.java:247-271`).
9. Failure uses `ny.b.run()` → static `c.b(callback,int,int,String)` → `onFail(int,int,String)`. The synthetic two-argument `onFail(int,String)` exists on `DianaView$c`, but this request path does not invoke it directly; it must not be seeded merely because the object implements the interface.

Reusable framework contracts:

- **Builder capture:** fluent setter `a(InterfaceC0712c)` writes a field; terminal `a()` copies it into `d.f48467h`. Preserve the exact object.
- **OkHttp:** `okhttp3.e.H(okhttp3.f)` registers its callback. Schedule only the standard `onResponse`/`onFailure` members on that callback instance.
- **Handler:** `android.os.Handler.post(Runnable)` registers the exact Runnable; dispatch only `run()` on it.
- **Interface callback:** after `ny.a.run`, invoke only the concrete target for `onSuccess`; after `ny.b.run`, invoke only the matching failure descriptor.

These rules apply beyond Mango when the same OkHttp and Handler signatures occur. They must remain instance-sensitive: do not connect unrelated callbacks merely because their classes implement `okhttp3.f`, `Runnable`, or the same application interface.

## DEX identity audit

The names above beginning with `f484...` are JADX aliases and must not be used by a structural matcher. The independent DEX symbol table gives these real fields and descriptors:

- Builder `Lcom/mgtv/diana/h0/c$d$a;`: callback field `h:Lcom/mgtv/diana/h0/c$c;`. Registration setter is `a(Lcom/mgtv/diana/h0/c$c;)Lcom/mgtv/diana/h0/c$d$a;`; terminal builder is `a()Lcom/mgtv/diana/h0/c$d;`.
- Request `Lcom/mgtv/diana/h0/c$d;`: callback field `h:Lcom/mgtv/diana/h0/c$c;`. The builder copies its field `h` to request field `h`.
- OkHttp adapter `Lcom/mgtv/diana/h0/c$a;`: captured request field `a:Lcom/mgtv/diana/h0/c$d;` and captured manager/outer field `b:Lcom/mgtv/diana/h0/c;`. Constructor is `<init>(Lcom/mgtv/diana/h0/c;Lcom/mgtv/diana/h0/c$d;)V`.
- Registration sink is exactly `Lokhttp3/e;->H(Lokhttp3/f;)V`.
- Callback interface members are exactly `Lokhttp3/f;->onFailure(Lokhttp3/e;Ljava/io/IOException;)V` and `Lokhttp3/f;->onResponse(Lokhttp3/e;Lokhttp3/b0;)V`.
- Response `Lokhttp3/b0;` is the OkHttp response class. Its DEX methods include `a()Lokhttp3/c0;` for the nullable body, `l()I` for status code, and `close()V`. Decompiled class declaration confirms `implements java.io.Closeable`; the symbols exporter omitted that interface array for this class, so the Closeable relationship is corroborating library/source metadata rather than inferred from `close()` alone.

The stable structural rule should match field types and data flow (`callback-interface field → request callback-interface field → adapter request field`) plus the exact registration descriptor. It should not match `f48475h`, `f48467h`, or `f48456a`, which are decompiler-generated names.
