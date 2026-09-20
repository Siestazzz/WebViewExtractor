# v7a old-H5 alias first break

Scope is one public host, `ctrip.android.view.h5.view.H5Container`, and one
`Screen_a` registration row for each of the report IDs `constructor_parameter`
and `entry_parameter`. Both rows name the same site:

`Lctrip/base/init/m$c;->n(Ljava/lang/Object;Lctrip/android/view/h5/view/H5WebView;Ljava/util/List;)V@287`.

## Actual host path and constructor

`H5Container.initFragment(Bundle)` constructs
`Lctrip/android/view/h5/view/H5Fragment;-><init>(Landroid/os/Bundle;)V` and stores
that Fragment in `H5Container.h5Fragment`. Fragment lifecycle reaches
`H5Fragment.addWebView()`, whose source allocation is:

```text
A = new H5WebView(getContext())
H5Fragment.mWebView = A
```

The invoked overload is exactly
`Lctrip/android/view/h5/view/H5WebView;-><init>(Landroid/content/Context;)V`.
It delegates with a null `AttributeSet` to
`Lctrip/android/view/h5/view/H5WebView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V`.
Constructor chaining preserves the same receiver: the one-argument and
two-argument constructor `this` values are both `A`. Neither constructor
produces a second H5WebView.

## Parameter flow to the reported registration

`H5Fragment.initWebView()` reads `mWebView` and invokes:

```text
A.l0(fragment, loadURL, listener)
```

The complete receiver/parameter flow is:

```text
Lctrip/android/view/h5/view/H5WebView;->l0(
    Lctrip/android/view/h5/view/H5Fragment;,
    Ljava/lang/String;,
    Lctrip/android/view/h5/view/H5WebView$y;)V
  receiver = A
  parameter[0] = host Fragment

  -> A.o0(fragment, A, listener)
     L...H5WebView;->o0(Ljava/lang/Object;L...H5WebView;L...H5WebView$y;)V
     parameter[1] = A

  -> A.M(fragment, A)
     L...H5WebView;->M(Ljava/lang/Object;L...H5WebView;)V
     parameter[1] = A
     A.f45241h = A

  -> A.H0(fragment, A)
     parameter[1] = A

  -> service.n(fragment, A, A.j)
     Lctrip/base/init/m$c;->n(Ljava/lang/Object;L...H5WebView;Ljava/util/List;)V
     parameter[1] = A

  -> A.addJavascriptInterface(H5ScreenPlugin, "Screen_a")
```

Here `parameter[index]` is zero-based among explicit Java/DEX arguments and
excludes the instance receiver. Thus the WebView is parameter 1 in `o0`, `M`,
`H0`, and `m$c.n`. The object alias equation is:

```text
A
= H5Fragment.mWebView
= receiver(l0)
= o0.parameter[1]
= receiver(o0)
= M.parameter[1]
= receiver(M)
= A.f45241h
= H0.parameter[1]
= m$c.n.parameter[1]
= receiver(addJavascriptInterface)
```

## First break in the report identities

The first source-backed identity is v7a's allocation/path ID beginning
`Lctrip/android/view/h5/view/H5Fragment;->addWebView()V@27|...H5Container...`.
The two reviewed generic IDs have no independent allocation or field write:

- `constructor_parameter` is a context-free constructor/entry seed propagated
  as if it could be the later WebView argument. On this host path, the concrete
  one-argument constructor delegates to the two-argument constructor on the
  same newly allocated receiver `A`; constructor parameters are `Context` and
  null `AttributeSet`, not another WebView.
- `entry_parameter` is the context-free summary of the WebView argument to
  helper/service methods. The actual call chain supplies `A` at parameter 1 at
  every hop. Treating the callee parameter summary as a new root loses the
  caller substitution `parameter[1] := A`.

Both appear to have been retained as additional abstract entry roots after the
host-specific call path was already known. The explicit negative assertion is:

> For this host, neither `constructor_parameter` nor `entry_parameter` denotes
> a second WebView, and neither justifies another `Screen_a` registration. Both
> must canonicalize to `A` before object-bound facts are deduplicated.

This conclusion is limited to the normal packaged bodies; Robust PatchProxy may
replace a method body at runtime.

## Unknown collection elements

An unresolved collection element at a real `addJavascriptInterface` loop site
does not prove that one additional unknown object is registered alongside all
resolved elements. It represents incomplete points-to knowledge about the loop
element. Reports should express it as an uncertainty record attached to the
same call site and receiver, for example:

```json
{
  "kind": "bridge_candidate",
  "implementation": null,
  "binding_status": "unresolved_collection_element",
  "cardinality": "unknown; not additive",
  "possible_targets": ["...resolved alternatives when known..."]
}
```

It must not be counted as a concrete extra bridge or expanded into members
until a concrete element type and registration name are proved. Conversely, a
source loop can register multiple actual elements; those are separate facts
only when the collection construction proves separate objects. The selected
old-H5 `m$c.n` path is stronger: it constructs named plugin locals explicitly
and calls `addJavascriptInterface` for each, so its concrete `Screen_a` row is
not an unknown-element placeholder.

Source anchors: `H5Container.java:174-180`,
`H5Fragment.java:2733-2745,2935-2945`,
`H5WebView.java:843-845,1082-1090,1928-1952,2346-2365,2389-2397,2572-2603`,
and `base/init/m.java:710-765`.
