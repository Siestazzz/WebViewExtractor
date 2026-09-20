# AspectJ runtime closure contract in Mango 9.3.0

This is a reusable runtime contract derived from the APK's decompiled `org.aspectj.runtime.internal.a`, concrete join-point class `p61.h`, factory `p61.e`, and their independent DEX descriptors. It does not depend on an Activity name.

## Closure construction and linking

`org.aspectj.runtime.internal.a.<init>(Object[])` stores the supplied array reference directly in `state`; it does not copy it. Both `linkClosureAndJoinPoint()` overloads read the existing object in `state[state.length - 1]`, cast it to `org.aspectj.lang.d`, and invoke `d.g(this)`. The concrete `p61.h.g(a)` stores that exact closure in field `p61.h.r`.

Linking therefore returns the same join-point object held in the last state slot. It does not allocate a join point. The integer overload additionally assigns its argument to the closure's `bitflags` field before returning the join point.

```text
closure.state ──last slot──> p61.h join point
      │                         │
      └──── g(closure) ─────────┘ stores closure in p61.h.r
```

## Proceed

`p61.h.proceed()` reads attached closure field `r`. With no attached closure it returns `null`. Otherwise it evaluates `r.run(r.getState())`: the input to `run` is the same captured state array object.

`p61.h.a(Object[])`, the `proceed(Object[])` implementation under the obfuscated interface name, first mutates that captured state array according to five flag bits, then invokes `run(state)`. The exact index algorithm is recorded in the JSON companion. The bytecode establishes the transformation but does not retain the source-level meanings of bits `0x10000`, `0x1000`, `0x100`, `0x10`, and `0x1`; those semantic flag names remain unproven. A framework adapter should reproduce the observed index algorithm rather than attach guessed meanings to the bits.

## `showMiniApp` instance

`NBFloatFragmentHelper.showMiniApp(String)` creates `t` with one array: `[helper instance, scheme string, join point]`. Class `t` extends `org.aspectj.runtime.internal.a`; its constructor delegates the same array to the parent. `linkClosureAndJoinPoint(69648)` (`0x11010`) takes index 2 as the join point, attaches the concrete `t`, and returns that same object to `LibTryCatchRuntimeAspect.excuteWithTryCatch`.

`LibTryCatchRuntimeAspect.excuteWithTryCatch(d)` directly calls `d.proceed()` in both its debug/inter_google branch and its guarded production branch. `p61.h` then calls `t.run(t.state)`. Raw DEX, which takes precedence over the misleading JADX body of `t.java`, shows that `t.run` reads captured `this.state` indices 0/1/2 and calls `NBFloatFragmentHelper.H1(helper, scheme, joinPoint)`.

`H1` is the next woven advice bridge. It obtains `MainAppAspect.aspectOf()`, casts the same `org.aspectj.lang.c` join point to `org.aspectj.lang.d`, and calls `G1(helper, scheme, joinPoint, aspect, proceedingJoinPoint)`. It does not allocate another closure or join point. `G1` is the around-advice body: each normal channel branch calls `F1` directly; its guarded production branch catches and reports an exception. `F1` constructs `MiniAppFragment` and passes it to `n1(Fragment)`.

The required generic analysis edge is therefore allocation-sensitive:

1. Preserve the `Object[]` instance stored by the closure constructor.
2. Recognize its final element as the existing join point.
3. Model `g(closure)` as an attachment on that join-point instance.
4. Connect `proceed()` to `attachedClosure.run(attachedClosure.state)`.
5. Propagate the captured elements through `t.run → H1`; preserve the same join-point identity through the cast in `H1`, then traverse `H1 → G1 → F1`.

Expanding an unbound `Fragment` parameter to every subtype cannot substitute for this path. In this case it missed `F1` entirely and risks attributing unrelated fragments.

The machine-readable contract, exact descriptors, bit tests, and source locations are in [aspectj-runtime-contract.json](/home/d3008/phy/workspace/WebViewGPT/webview-extractor/docs/validation/mango/aspectj-runtime-contract.json).
