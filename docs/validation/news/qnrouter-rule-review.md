# QNRouter service-binding rule review

## Exact lookup key

QNRouter's service map is a two-level map. The outer key is the exact binary
name returned by the **first `Class` argument to `ServiceMap.autoRegister`**;
the inner key is the registration's `implName` string. `APIMeta` holds the
implementation class (its second constructor argument), singleton flag, creator,
and optional cached instance.

For a statically generated registration such as:

```text
ServiceMap.autoRegister(API.class, qualifier,
    new APIMeta(API.class, Impl.class, singleton))
```

an exact rule may bind a lookup only when its constant API class and normalized
qualifier select that same pair of map keys. The implementation is
`APIMeta.getImplClazz()`, not an arbitrary subtype of the API. QNRouter itself
does not validate that the first `APIMeta` class equals the `autoRegister` class,
or that `Impl` is assignable to the API, so the registration call's first class
must remain authoritative for lookup.

## Default and explicit qualifiers

- `Services.call(Class)` and `Services.get(Class)` explicitly query
  `"_default_impl_"`.
- `Services.get(String)` also explicitly queries `"_default_impl_"`.
- In the common resolver, a `null` **or empty** qualifier falls back to
  `"_default_impl_"`.
- A nonempty explicit qualifier is exact. It does not fall back to the default
  entry when absent.
- `safeCall` and `safeGet` normalize `null` to `"_default_impl_"`; their Kotlin
  default-argument bridges also supply that literal. Empty still reaches the
  resolver and becomes default.
- `Services.register(...)` normalizes a null/empty registration qualifier to the
  empty string. At lookup time an empty qualifier means default, so an entry
  registered under `""` is not selected by an empty lookup. This API behavior is
  internally asymmetric and such a registration must not be treated as a
  default binding.

Missing behavior differs by API. `get(...)` returns null after notifying the
optional service listener. `call(...)` invokes the same nullable resolver and
then enforces non-null, so a missing registration/qualifier or failed creator
throws at the call boundary. `safeCall` suppresses the failure; `safeGet` returns
its supplied default value. A module-only mapping triggers the page-fault
listener and the current lookup still returns null; it does not synchronously
retry after module loading.

## Registration replacement and instance creation

`autoRegister` passes the router debug flag to `putWithCheck`:

- with `Services.init(false)`, duplicate `(API, qualifier)` registrations use
  `ConcurrentHashMap.put`; the registration executed last replaces the earlier
  metadata;
- with debug enabled, a duplicate existing key throws `IllegalStateException`;
- explicit `Services.register` always uses duplicate checking disabled and
  replaces the same inner key, while also replacing the API's module mapping;
- `unregister(serviceName)` removes the complete outer entry and therefore all
  qualifiers for that service name.

There is no annotation scan at lookup time. By default, `APIMeta` reflects the
first declared constructor of the exact implementation class, makes it
accessible, and invokes it with zero arguments. A lookup-provided `APICreator`
overrides that creator for the selected metadata. `singleton=true` caches the
created object in that `APIMeta`; false creates on each lookup. Constructor or
creator exceptions are caught and yield the same missing-service behavior.

## Initialization reachability and confidence

The packaged application calls `Services.init(false)` from
`BaseApplication.attachBaseContext`. That reflectively invokes
`Lcom/tencent/news/qnrouter/Loader;->init()V`; Loader directly invokes each
packaged `RouterEntry.init`, and each entry invokes its generated
`ServiceMapGen*.init`, which performs `autoRegister` calls. For example Loader
directly reaches `L4qnplayer.RouterEntry`, then `ServiceMapGenL4qnplayer`, so the
YSP default registration is reachable before the subsequent service lookup in
the normal packaged path.

Classification for a static analysis rule should be:

- **confirmed binding** when the lookup API class and qualifier are constants,
  the matching generated registration is exact, and its RouterEntry is reached
  from packaged Loader initialization;
- **conditional binding** when a QFix redirector can replace a relevant init or
  factory body, or when replacement registration ordering is path-dependent;
- **candidate only** when initialization is not proven, the class or qualifier
  is dynamic, only a module mapping exists, or lookup supplies a custom creator;
- **no binding** for a nonempty constant qualifier with no exact registered key.

The packaged `BaseApplication` and generated router entry methods contain QFix
redirect checks. Thus the normal source path is confirmed for the shipped
fallback bodies, while runtime patch replacement remains an explicit condition.

## Rule gaps that can cause wrong host or capability ownership

1. Treating null, empty, and `"_default_impl_"` as the same **registration** key
   invents a binding for entries registered under `""`.
2. Ignoring explicit qualifiers binds the default implementation or every
   implementation to a call that selects one named entry.
3. Enumerating API subtypes instead of using `APIMeta.getImplClazz()` creates
   false implementations and can transfer their WebView abilities to unrelated
   hosts.
4. Treating all generated maps as initialized ignores RouterEntry reachability,
   module page faults, QFix diversion, and initialization failures.
5. Merging duplicate registrations ignores last-writer behavior in this app's
   `init(false)` path. It may attach capabilities from an implementation that
   has already been replaced.
6. Treating module loading as a synchronous return edge is incorrect: the first
   lookup returns null after requesting the module.
7. Ignoring `APICreator` at a lookup can claim the metadata implementation even
   though a custom creator supplies another object. This case is a candidate
   until creator behavior is followed.
8. Propagating an implementation from a service lookup to every caller of the
   same API, without proving the selected qualifier and registration state at
   that call site, produces wrong host ownership.

## Verified DEX methods

- `Lcom/tencent/news/qnrouter/service/ServiceMap;->autoRegister(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APIMeta;)V`
- `Lcom/tencent/news/qnrouter/service/ServiceMap;->register(Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APIMeta;)V`
- `Lcom/tencent/news/qnrouter/service/Services;->call(Ljava/lang/Class;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/Services;->call(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/Services;->get(Ljava/lang/Class;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/Services;->get(Ljava/lang/Class;Ljava/lang/String;Lcom/tencent/news/qnrouter/service/APICreator;)Ljava/lang/Object;`
- `Lcom/tencent/news/qnrouter/service/Services;->init(Z)V`
- `Lcom/tencent/news/qnrouter/Loader;->init()V`
- `Lcom/tencent/news/qnrouter/utils/CheckDuplicateConcurrentHashMap;->putWithCheck(Ljava/lang/Object;Ljava/lang/Object;Z)Ljava/lang/Object;`

Source anchors: `Services.java:33-57,76-79,88-154,185-246,284-373`,
`ServiceMap.java:20-97`, `APIMeta.java:8-62`,
`CheckDuplicateConcurrentHashMap.java:7-17`, `g.java:9-39`,
`BaseApplication.java:284-305`, `Loader.java:1-309`,
`L4qnplayer/RouterEntry.java:18-24`, and
`ServiceMapGenL4qnplayer.java:21-30`.
