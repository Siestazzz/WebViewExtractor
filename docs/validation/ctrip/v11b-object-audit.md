# v11b Ctrip object and ownership delta

The v11b file was read only after `processed_activities=410`, equal to its
`manifest_activities=410`; it contains the expected 55 reported hosts. The file
retains the extractor status `partial`, so this audit describes the completed
manifest pass without reinterpreting that status.

## Host and semantic-fact result

The v11b 55-host name set is exactly equal to both v10a and the previously
source-audited v4e set. No ownership row needs updating.

Ignoring evidence/order and receiver representation, every host has the same
capability semantic keys in v10a and v11b: zero additions and zero removals.
The new exact Map key/value and structured Class propagation therefore produced
no new Ctrip capability or ownership claim in this run.

Nineteen host records differ byte-for-byte. Eighteen changes are limited to
nested argument-union IDs; fact counts, top-level WebView IDs, semantic keys,
and ownership are unchanged:

- `ctrip.android.chat.helper.url.ChatFloatingWebView`
- `ctrip.android.destination.view.hybrid.GSHybridPageActivity`
- `ctrip.android.finance.cfhy.CFHyWebActivityV2`
- `ctrip.android.finance.home.CTFinanceHomeActivity`
- `ctrip.android.flight.component.hybrid.h5.container.FlightHalfMaskH5Container`
- `ctrip.android.flight.component.hybrid.h5.container.FlightMaskH5Container`
- `ctrip.android.flight.component.hybrid.h5.container.FlightPageH5Container`
- `ctrip.android.hotel.detail.image.HotelAlbumBrowseActivity`
- `ctrip.android.hotel.detail.image.HotelPhotoViewActivity`
- `ctrip.android.train.pages.triporder.TrainContainerActivity`
- `ctrip.android.view.h5.view.H5Container`
- `ctrip.android.view.h5v2.view.H5Container`
- `ctrip.android.view.h5v2.view.H5PreRender`
- `ctrip.android.view.myctrip.orderbiz.MyCtripOrderModalActivity`
- `ctrip.base.ui.gallery.PhotoViewDetailActivity`
- `ctrip.business.accessible.AgingHomeActivity`
- `ctrip.business.evaluation.EvaluateDialogActivity`
- `ctrip.business.feedback.view.CtripCommonFeedBackActivity`

The remaining changed host is `MktH5ContainerV2`, where six setting facts now
use the existing underlying WebView union instead of the synthetic receiver ID
`settings`. Its fact count remains 552; bridge/setting/callback rows remain 453;
semantic capability keys remain 72. Capability receiver IDs decrease from 12
to 11. This is a valid normalization: source obtains each `WebSettings` from
the selected `H5WebView`.

## Four public object samples

- Old `H5Container` remains 60 capability rows, 60 semantic keys, one receiver.
  Its only report delta is the nested String argument union on two WebView
  operations. The source identity remains the `H5Fragment.addWebView`
  allocation propagated through `mWebView`.
- `MyCtripOrderModalActivity` has the same 60/60/1 result and the same two
  nested argument-union changes. Its Fragment wrapper remains correctly bound.
- CMB is exactly unchanged: ten total facts, with its bridge, four settings and
  two clients on the single Activity layout WebView.
- `MktH5ContainerV2` benefits from removal of the generic `settings` receiver,
  but its known alias defects remain.

For Mkt, source still proves:

```text
A = acquireWebViewInternal(context)       // one runtime branch
Fragment.mWebView = A
A.I(fragment, ...)
A.E(fragment, A)
A.x = A
A.D(fragment, A)
```

v11b continues to emit separate acquire-branch receiver identities and five
field-derived `base::H5WebView.x` identities. The latter each repeat 37 bridge
facts even though `A.x=A`. Settings union normalization removes one wrapper
identity but does not fix this earlier WebView-object alias break.

## Interpretation of new propagation logic

The 18 argument-only deltas are consistent with rebuilding internal union IDs
after more precise Map/Class/String propagation. They do not establish new
WebViews or capabilities and should not trigger ownership changes. The only
top-level receiver delta is the six Mkt setting rows described above, and it is
source-supported. No new structured registry result appears in the selected
four hosts.

The JSONL contains a run summary, all 19 changed hosts, the unchanged CMB sample,
the surviving Mkt defect, and the v4e host-set assertion.

Source anchors reused from the public audits: old `H5Container.java:174-180`,
old `H5Fragment.java:2733-2745,2935-2945`, old
`H5WebView.java:1082-1090,1928-1952,2346-2365,2389-2397`,
`MyCtripOrderModalActivity.java:117-135`, `MktH5ContainerV2.java:390-404`,
new `H5Fragment.java:1116-1125,1317-1334`,
`PreloadWebView.java:147-164`, new `H5WebView.java:176-219,235-261`, and
`CMBApiEntryActivity.java:62-89,130-139`.
