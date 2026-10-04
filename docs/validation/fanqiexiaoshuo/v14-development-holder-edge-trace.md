# Actual holder edge classification and host CPU sampling

This is an independent development overlay diagnostic, not frozen v13. Index completed127.388s; bootstrap1.011s; the selected host processed66 contexts with116 pending. A15-second scheduling target overshot to60.003s because a single atomic operation ran to the separate global analysis deadline.

| Actual source edge | Visited | Pending |
| --- | --- | --- |
| Swipe constructor | yes | other context |
| Placeholder constructor | yes | other context |
| Placeholder initialization c | no | yes |
| Placeholder getter | no | yes |
| Field-writing b/e | no | not yet generated |
| Swipe Q registration | no | yes |

The actual Holder receiver on queued c is exactly the allocation identity embedded in M's unknown field_object. There is no evidence here of a different Holder ID, nor of a completed b/e field write losing its value. The confirmed local boundary is initialization writer and registration **not yet executed**. Do not relabel it callback member recognition or completed propagation failure.

The host sampler collected5771 samples. Retained top20 distinct groups contain2729 samples; all2729 include ReflectionProtocols.discoverFields,2547 also include DexFlow.decode. The live dump shows field/map observation → observeTransportMap → mapField → whole-index discoverFields → summary/decode. This is the concrete CPU operation consuming the atomic context, not a conclusion based on queue size. Writer-prefilter bootstrap improvement does not eliminate mapField-triggered global discovery during Activity analysis.

Class-key service dispatch remains a separate independently proved semantic gap. Actual target NsCommon factory has not been reached by this run; a pending unrelated ServiceManager lookup uses NsBaseLocalCacheDepend. The generic ClassKeyFactoryProbe now also includes398 branches/1992 instructions matching the real dispatcher shape, still unknown on frozen v13. A small Class equality correction is insufficient if the large contextual gate remains.

Raw heap/visited/pending records, stack samples, live dump, development class hashes and source oracle references are bound in the sibling JSON. No production source or gold was changed. The actual six callback recovery remains unproven; further diagnosis should first bound/avoid unrelated whole-index transport discovery, then continue the same actual initialization receiver rather than infer its future field state.
