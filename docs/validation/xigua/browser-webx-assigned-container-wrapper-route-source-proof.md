# Assigned view and installed delegate route

Independent source review, GPT-6.1 Sol; source APK and all file hashes are in the adjacent JSON. No tool reports or sealed holdout were read.

Existing Activity→retained factory Scene→LifecycleFragment/dispatcher→BrowserScene.onCreateView/attach route is in scene-installation-source-chain.json. The custom view adds a second cross-object alias boundary: its constructor assigns **this** to ContainerConfig, calls the manager with that config, and the manager reads that assigned original object, initializes it and installs newly constructed Client/Chrome wrappers directly through native super setters. The manager return value is not the sole ownership carrier.

Later BrowserScene sets its real Client/Chrome on the same view. With initialized environment, the setters update the delegate fields in those already installed wrapper instances. Callback members go through EventManager and then exact inner delegate calls. The JSON inventories49 own platform-compatible inner declarations and signature-specific forwards; this is not evidence that every delegate has49 overrides. The renderGone member forwards through an instrumentation helper and returns true. The empty onReachedMaxAppCacheSize declaration does **not** forward and is listed separately.

Reusable minimal structure:

```java
class View extends WebView {
  Wrapper installed;
  View(Context context) {
    super(context);
    Config config = new Config().assignContainer(this);
    manager.createContainer(context, config);
  }
  void installWrapper(Wrapper wrapper) {
    installed = wrapper;
    super.setWebViewClient(wrapper);
  }
  public void setWebViewClient(WebViewClient delegate) {
    installed.setDelegate(delegate);
  }
}
class Manager {
  View createContainer(Context context, Config config) {
    View sameView = (View) config.assignedContainer;
    sameView.installWrapper(new Wrapper());
    return sameView;
  }
}
class Wrapper extends WebViewClient {
  WebViewClient delegate;
  void setDelegate(WebViewClient client) { delegate = client; }
  public void onPageFinished(WebView view, String url) {
    if (delegate != null) delegate.onPageFinished(view, url);
  }
}
```

The pseudocode omits runtime extension event interception for clarity; source evidence retains that condition. PageCtrlExtension is proved bound to the installed wrappers and forwards its event hooks through super. Full global/container extensions and IXBridgeRegisterService implementation remain unresolved. Browser5 remains partial; no canonical facts were removed or changed by this review.
