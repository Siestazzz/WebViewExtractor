# v9c Ctrip object audit

This is a bounded delta review of the same four public hosts used for v8c. It
checks the Android 36 hierarchy/carrier changes, listener propagation, and
setting values against the already verified source object chains. It does not
inspect sealed holdouts or establish whole-APK object precision.

## Host set

v9c reports 55 activities. Its activity-name set is exactly the v4e set:
55 shared, zero added, zero removed.

## Object-isolation delta

| Host | v8c rows / semantic keys / receiver IDs | v9c | Result |
|---|---:|---:|---|
| `ctrip.android.view.h5.view.H5Container` | 60 / 60 / 1 | 60 / 60 / 1 | Single-object result preserved. |
| `MyCtripOrderModalActivity` | 60 / 60 / 1 | 60 / 60 / 1 | Single Fragment WebView preserved. |
| `MktH5ContainerV2` | 453 / 72 / 12 | 453 / 72 / 12 | No alias change; the v8c defects remain. |
| `CMBApiEntryActivity` | 7 / 7 / 1 | 7 / 7 / 1 | Concrete Activity layout WebView preserved. |

No checked host regressed to `constructor_parameter` or `entry_parameter`.
Old H5 and CMB still have one receiver for every audited bridge, setting, and
client fact.

The platform View/carrier analysis changes old-H5 evidence without changing its
identity. v9c reaches the WebView through
`H5Fragment.historyPullLayout -> HistoryPullLayout.c:Landroid/view/View;`, where
v8c cited `H5Fragment.mWebView` directly. Source proves
`HistoryPullLayout.setContentView(this.mWebView)` immediately after allocating
and storing the same `mWebView`; the unchanged host allocation ID is therefore
a valid alias, not a second View. This is a useful positive carrier-getter case.

## Remaining new-H5 breaks

The source equation remains:

```text
A = acquireWebViewInternal(context)       // one cached or fresh branch
Fragment.mWebView = A
A.I(fragment, ...)
A.E(fragment, A)
A.x = A
A.D(fragment, A)
```

v9c still keeps the acquire branches as separate fact receivers after the one
`Fragment.mWebView` assignment. It also retains five IDs suffixed with the
`H5WebView.x` field. This is a confirmed duplicate: `I` passes `this` to `E` as
the WebView parameter, and `E` stores that argument into the receiver's `x`, so
`A.x = A`. Each field-derived ID repeats 37 bridge rows. Generic `settings` and
union receiver summaries also remain. Thus the v9c SDK hierarchy work did not
alter the known Mkt object-isolation defect.

## Values and listeners

The Android enum metadata improves setting values on the same receivers:

- old H5 `setTextSize` changes from unknown to `NORMAL`;
- old H5 `setRenderPriority` changes from unknown to `HIGH`;
- the corresponding new-H5 facts also resolve to `NORMAL` and `HIGH` on each
  still-duplicated branch receiver.

CMB's four setting values and its two client bindings are unchanged. The client
and custom-listener related receiver IDs for all four hosts are unchanged, so
the listener-store changes introduce no object mixing in this bounded set.

None of the audited four hosts contains a reported bitwise expression. This
sample therefore provides no evidence for or against bitwise evaluation beyond
confirming that the new logic did not perturb their existing setting values.

The JSONL contains four host summaries, two carrier-evidence records, the
remaining new-H5 self-field defect, and the bounded value/listener result.

Source anchors: `H5Container.java:174-180`, old
`H5Fragment.java:2733-2745,2935-2945`, old
`H5WebView.java:843-845,1082-1090,1928-1952,2346-2365,2389-2397`,
`MyCtripOrderModalActivity.java:117-135`, `MktH5ContainerV2.java:390-404`,
new `H5Fragment.java:1116-1125,1317-1334`,
`PreloadWebView.java:87-95,147-164`, new
`H5WebView.java:176-219,235-261`, and
`CMBApiEntryActivity.java:62-89,130-139`.
