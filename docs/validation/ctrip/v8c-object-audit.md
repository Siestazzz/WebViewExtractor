# v8c Ctrip object-isolation audit

This bounded review reuses the published source chains for four hosts and
compares v7a, v8a, and v8c. It does not inspect sealed holdouts or claim object
precision for every output activity.

## Host-set check

v8c contains 55 activities. Its activity-name set is exactly equal to v4e's
55-name set: no additions and no removals. This confirms set continuity only;
it does not replay the source ownership verdict for every fact.

## Comparison

| Host | v7a capability rows / semantic keys / receiver IDs | v8a | v8c | Verdict |
|---|---:|---:|---:|---|
| old `H5Container` | 393 / 60 / 9 | 209 / 60 / 4 | 60 / 60 / 1 | Fixed for the checked capability kinds. No `constructor_parameter`, `entry_parameter`, or field-derived duplicate remains. |
| `MyCtripOrderModalActivity` | 393 / 60 / 9 | 209 / 60 / 4 | 60 / 60 / 1 | Fixed. Its committed Fragment's single `mWebView` allocation is now the sole receiver ID. |
| `MktH5ContainerV2` | 398 / 72 / 8 | 695 / 72 / 13 | 453 / 72 / 12 | Improved from v8a, but still duplicates 60 semantic facts across receiver alternatives; 5 additional IDs are reads through the self-alias field `x`. |
| `CMBApiEntryActivity` | 7 / 7 / 1 | 7 / 7 / 1 | 7 / 7 / 1 | Object identity improved from a null-containing union in v7a to the concrete Activity layout-view ID in v8a/v8c. No duplicate remains. |

“Semantic key” here is `(kind, site, api, registration_name,
implementation, name)` with the report WebView ID removed. The comparison is
designed to expose one source operation repeated under multiple object IDs.

## Confirmed fixes

For both old-H5 hosts, v8c substitutes the caller object through the complete
helper chain. The surviving ID starts at `H5Fragment.addWebView`; source proves
that allocation is stored in `mWebView`, passed as parameter 1 through
`o0/M/H0/m$c.n`, and used as the registration/settings/client receiver. The
v7a/v8a `constructor_parameter`, `entry_parameter`, and
`constructor_parameter::...H5WebView->h` roots are absent. Every one of the 60
checked semantic facts now has one receiver ID.

CMB likewise has one concrete ID,
`activity:cmbapi.CMBApiEntryActivity/view:2131313185`, for the bridge, four
settings, and two client setters. The earlier literal-null alternative no
longer contaminates the object identity.

## Remaining MktH5ContainerV2 defects

The source equation is:

```text
A = acquireWebViewInternal(context)       // cached pop OR fresh object
Fragment.mWebView = A
A.I(fragment, url, listener)
A.E(fragment, A)
A.x = A
A.D(fragment, A)
```

The first remaining break is the return-to-field join. v8c retains the cached,
fresh, PatchProxy, and union provenance as separate receivers after the single
assignment to `Fragment.mWebView`. These are possible values of one runtime
slot, rather than simultaneous WebViews. If branch-sensitive identities are
retained, repeated facts need explicit mutually exclusive/may-alternative
semantics and cannot be counted additively.

The next break is stronger and is a confirmed alias error. `I` invokes
`E(fragment, this)`, so in `E` the receiver and WebView argument are both `A`.
The write `this.x = h5WebView` establishes `A.x = A`. v8c nevertheless emits
five base IDs again with the suffix
`::Lctrip/android/view/h5v2/view/H5WebView;->x:...`, duplicating 37 bridge
facts per field-derived ID. A read of `x` on this path must canonicalize to its
base object.

Two generic IDs also remain: `settings` and
`union:692fbe9a-ca08-33f1-a89f-831e449a7bef`. Settings are obtained from
`A.getSettings()`, and the UI-watch/VideoEnabled helpers receive or execute on
`A`; these summaries are not additional WebViews. Any unresolved collection
element represented through a union remains a non-additive candidate, not an
extra concrete bridge registration.

The machine-readable file contains one summary per host plus three reusable
Mkt defect records, each with its earliest break, binding chain, negative
assertion, and source evidence.

Source anchors: old `H5Container.java:174-180`, old
`H5Fragment.java:2733-2745,2935-2945`, old
`H5WebView.java:843-845,1082-1090,1928-1952,2346-2365,2389-2397`,
`MyCtripOrderModalActivity.java:117-135`, `MktH5ContainerV2.java:390-404`,
new `H5Fragment.java:1116-1125,1317-1334`,
`PreloadWebView.java:87-95,147-164`, new
`H5WebView.java:176-219,235-261`, and
`CMBApiEntryActivity.java:62-89,130-139`.
