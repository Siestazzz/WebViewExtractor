# v7a Ctrip WebView object audit

This is a bounded source review of four previously public hosts. It does not
claim object precision for all 55 reported activities or re-audit every plugin
method. The selected paths cover the old H5 stack, the new/preloaded H5 stack,
an Activity-to-Fragment wrapper, and a third-party payment WebView.

## Result

| Host | Source runtime identity | v7a result |
|---|---|---|
| `ctrip.android.view.h5.view.H5Container` | the `H5WebView` allocated once by its `H5Fragment.addWebView` and stored in `H5Fragment.mWebView` | Bridge registrations, settings and both client setters reach that same object. v7a splits it into 9 IDs and repeats the same capability families across helper parameters/unions. |
| `ctrip.android.view.myctrip.orderbiz.MyCtripOrderModalActivity` | the committed old-H5 Fragment's `mWebView` | Same-object binding is source-confirmed. The 9 reported IDs do not represent 9 WebViews; generic constructor/entry IDs and unions duplicate the host allocation facts. |
| `ctrip.android.ad.webview.MktH5ContainerV2` | the single value returned by `PreloadWebView.acquireWebViewInternal`, stored in the `MktH5FragmentV2` inherited `mWebView` field | At runtime the value is either a cached `Stack.pop()` object or a fresh `H5WebView`, then all registration/settings/client calls use that selected object. v7a exposes 8 IDs for settings and 7 for bridges/clients. These are alternatives and helper aliases, not simultaneous objects. |
| `cmbapi.CMBApiEntryActivity` | the layout WebView stored in `Activity.mWebView` | `CMBSDK`, four settings, `WebChromeClient`, and `cmbapi.b` all use the same field. The reported union's literal-zero member is not a second WebView. |

The Activity-level capability set therefore masks substantial object
duplication in both H5 generations. Old-H5 hosts each have 286 bridge rows for
one registration API, 91 setting rows for 17 setting names, and 16 callback
rows for two setter names. Source shows one host Fragment WebView, with helper
methods receiving that same object. These duplicated rows are incorrect as
additional object-bound capabilities even where the underlying unique
capability is valid.

The new-H5 host similarly has 231 bridge rows distributed over seven IDs. Its
preload factory has real control-flow alternatives: it returns a cached object
or constructs a fresh one. The alternatives join at the single assignment to
`H5Fragment.mWebView`; they must remain a may-alias set for one selected runtime
receiver. `entry_parameter`, PatchProxy summaries, and the `Stack.pop` return
must not each create another copy of every capability. In addition, v7a emits
an `implementation: "unknown"` bridge candidate for the container element
union. `H5WebView.D` registers only non-null concrete `H5Plugin` loop elements,
so the summary value itself is not an extra JavaScript interface. This is a
specific false extra capability candidate independent of Activity-level recall.

For CMB, `initView` writes the result of `findViewById` to `mWebView`, and
`initWebView` dereferences it before each audited operation. If it is null, the
first client setter throws and the outer `onCreate` catch prevents subsequent
settings and bridge registration. The literal zero inside v7a's receiver union
is thus a nullability condition, not a WebView identity.

## Source chains

The old H5 container creates `H5Fragment(bundle)` and installs it. The Fragment
allocates `new H5WebView(getContext())`, stores it in `mWebView`, initializes the
same field, passes it into UI-watch setup, and installs its Chrome client on the
field. H5 plugin setup stores its WebView parameter and registers interfaces on
the receiver. `MyCtripOrderModalActivity.renderUI` installs the same Fragment
type, so the identical object relation applies through a different host edge.

`MktH5ContainerV2.initFragment` installs `MktH5FragmentV2`. Its inherited
`addWebView` assigns the exact `acquireWebViewInternal` result to `mWebView`.
That factory returns one of the cached or fresh branches. `H5WebView.I` then
calls `E(fragment, this)`; `E` obtains the plugin list for that same argument,
and `D` invokes `h5WebView.addJavascriptInterface(...)`. The Fragment installs
the Chrome client on the same `mWebView` field.

The machine-readable audit is in `v7a-object-audit.jsonl`. Each host has a
positive mapping assertion and a negative assertion describing the report IDs
that must not be interpreted as additional objects.

Source anchors:

- `H5Container.java:174-180`
- old `H5Fragment.java:2733-2745,2935-2945`
- old `H5WebView.java:1928-1950`
- `MyCtripOrderModalActivity.java:117-135`
- `MktH5ContainerV2.java:390-404`
- new `H5Fragment.java:1116-1125,1317-1334`
- `PreloadWebView.java:87-95,147-164`
- new `H5WebView.java:176-219,235-261`
- `CMBApiEntryActivity.java:62-89,130-139`
