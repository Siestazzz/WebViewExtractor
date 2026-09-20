# Ctrip source-to-WebView type map

This map constrains canonical facts by the concrete receiver proven by source. It does not use extractor output. `webview-group-types.jsonl` contains nine group rules and three direct SDK-host rules.

Confirmed groups are old H5 (`ctrip.android.view.h5.view.H5WebView`), new H5 (`ctrip.android.view.h5v2.view.H5WebView`), RN (`RNCWebViewManager$RNCWebView`), and Flutter (`WebViewHostApiImpl$WebViewPlatformView`). The three direct SDK activities genuinely allocate or declare `android.webkit.WebView`; this platform type is source evidence for those hosts, not a fallback.

The broad `pay-common`, `liveness`, `destination`, `finance-v2`, and `samsung-wallet` rows document why their Bus/combined registry definitions alone are unresolved. Capability-level selectors then resolve every current canonical implementation from the upstream calls: `m.n` passes its declared old-H5 parameter as Bus argument 0; `n.t` passes its declared new-H5 parameter at the corresponding V2 keys. Samsung registration is direct in those same typed methods. This is caller argument provenance, rather than a type inferred from plugin naming.

Replay without modifying canonical input:

```bash
python3 scripts/validation/apply_ctrip_webview_constraints.py \
  --input docs/validation/ctrip/canonical-facts.jsonl \
  --mapping docs/validation/ctrip/webview-group-types.jsonl \
  --output /tmp/ctrip-canonical-with-webview.jsonl
```

The script rejects in-place output, conflicting existing constraints, ambiguous confirmed rules, and malformed type lists. Unresolved rules never receive `android.webkit.WebView` as a convenience value.
