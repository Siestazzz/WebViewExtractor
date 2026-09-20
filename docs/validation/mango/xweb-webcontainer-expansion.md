# XWeb and WebContainer expansion

`XWebActivity` reaches the X5 chain `RootWebView -> BridgeWebView -> XWebView`. The truth preserves 40 ordered setting calls, 27 effective callback overrides after subclass replacement, and the DEX-confirmed `jsobj.callNative` endpoint.

`WebContainerActivity` reaches the Android chain `RootWebView -> BridgeWebView -> ImgoWebView`. It preserves 38 ordered setting calls, 28 effective callback overrides after subclass replacement, and its distinct DEX-confirmed `jsobj.callNative` endpoint. The layout constructor leaves `K0` null, so `registerWebHandler()` returns before its handler list; those source strings are not reported as runtime registrations for this host.

Both generators stream the independent DEX export and assert every callback/bridge owner exists. Reproduce using `build_xweb_truth.py` and `build_webcontainer_truth.py` before `finalize_canonical.py`.
