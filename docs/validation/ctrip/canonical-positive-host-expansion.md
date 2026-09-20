# Ctrip public positive-host expansion

The existing canonical file contains 17 distinct non-null Activities. This pending expansion adds 13 source-confirmed Activities that are absent from that set, bringing the public development oracle to exactly 30 positive hosts if merged. It does not touch the sealed ten-host holdout and does not modify `canonical-facts.jsonl`.

## Method and provenance

Candidates were selected from the already public ownership inventory, so this set is explicitly **not blind**. For each candidate, the decompiled source was inspected first for the host-to-WebView allocation/layout field and for the same receiver's settings, bridge, and client installation. After fixing those bindings and concrete receiver types, callback and bridge members were independently enumerated through the DEX symbol parent graph and annotations. v12j is used only for a later non-blind comparison, not as the expected-member boundary. Every exported member signature is present in `test/runs/symbols/ctrip.jsonl`.

`canonical-new-positive-hosts.pending.jsonl` contains 196 deduplicated facts across the 13 hosts. It includes settings, `callback_registration` installation facts and every independently enumerated override signature, plus bridge registration and every exposed method for `SimpleOverseaMapActivity`. Dynamic setting expressions remain `value_kind=dynamic`; an unknown value is not converted into a literal. Each row carries both host binding evidence and a source location for the capability/method.

## Added hosts and receiver types

- Ten direct SDK or application hosts use a source-declared or directly allocated `android.webkit.WebView`: Douyin authorization through `BaseWebAuthorizeActivity`, Kwai login, Mqunar face verification, Sina Weibo, Tencent face protocol, Street Scenic, Baidu authorization, QQ authorization, FAQ, and Meizu authorization.
- `HotelFlagShipLoginActivity` owns a main platform WebView and conditionally creates a second platform WebView for `onCreateWindow`; the shared exact type constraint is valid, while the two allocations remain distinct objects.
- `OtherPayActivity` binds `ctrip.android.pay.view.otherpay.PayWebView` from its layout and installs its settings and clients on that field.
- `SimpleOverseaMapActivity` binds `ctrip.android.view.h5.view.CtripWebView` and registers `console` and `mapDAO` on that receiver.

RN and Flutter remain framework-level canonical groups without a proven manifest Activity host in this APK. They are not converted into positive Activities merely to increase the count. Existing old/new H5 Fragment wrappers remain among the original 17 positive hosts, so the combined 30-host set covers direct, inherited, Fragment, payment, third-party SDK, and custom WebView paths.

## Files

- `canonical-new-positive-hosts.pending.jsonl`: facts ready for review/merge
- `canonical-new-positive-hosts.inventory.jsonl`: one row per added host, binding chain, receiver type, counts, and non-blind status

The expansion is conservative about host identity, but it is not a claim that all 410 manifest Activities have been exhaustively classified or that exact allocation aliases in extractor reports are correct.
