# v14c MiniApp binding validation

The v14c focused probe fixes the Fragment-root binding that was missing in the v14 pre-fix probe. It does not restore the asynchronous request/service path or the final WebView capabilities.

For the host-derived `MiniAppFragment` instance, both `onViewCreated` and `onActivityCreated` retain the same Fragment allocation lineage from `VodGameRoomActivity.initializeMiniApp`. The exact three-argument `Diana.startDianaApp` overload receives that Fragment. Its root `ViewGroup.getChildAt(0)` result is now refined to a concrete `com.mgtv.diana.sdk.main.DianaView` with XML ID `2131828340`, rather than the earlier unknown child.

The host-specific concrete identity is:

`inflate:MiniAppFragment.onCreateView@12 | MiniAppFragment.newInstance@64 | VodGameRoomActivity.initializeMiniApp@71 | 924fc372... / view:2131828340`

That same identity appears as:

- the receiver of `DianaView.startLoad()`;
- the captured outer receiver of `DianaView$c.<init>(DianaView)` created inside `startLoad`;
- the receiver of `setOnDianaAppEvtListener` from `MiniAppFragment.onViewCreated`;
- the captured outer receiver of constructor-created `DianaView$a` for the corresponding `onCreateView@12` branch.

A second entry-parameter Fragment context is likewise internally consistent with identity suffix `22d3a999.../view:2131828340`. The probe also carries extra alternative/super inflation identities (`onCreateView@5`, base `Fragment.onCreateView`, and `0/view`); these are candidates and do not negate the per-context equality above. They should not be globally merged across different Fragment allocations.

The narrow recovery is therefore confirmed: Fragment root, child 0, XML lookup, `startLoad` receiver, and the two inner callback outer captures can refer to the same concrete DianaView within a given Fragment context.

The rest of the chain remains absent. The probe has two concrete `startLoad` contexts and reaches `DianaView$c` construction, but has zero contexts for `DianaView$c.onSuccess`, `DianaView.onServiceConnected`, and `DianaView.renderMiniApp`. No `AppService`, ServiceWebView, PageWebView, settings, clients, or bridges can be claimed recovered from this probe. The next missing edges are the separately documented actual OkHttp callback dispatch, main-thread Handler dispatch, Android ServiceConnection dispatch, and their same-receiver state join.
