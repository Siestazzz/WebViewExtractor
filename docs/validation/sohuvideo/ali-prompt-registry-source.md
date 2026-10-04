# Ali prompt registry source audit

LoginWebViewActivity inherits the actual AuthWebView creation/layout chain and installs its b ChromeClient, inheriting final BridgeWebChromeClient.onJsPrompt. This callback selects the registry only when defaultValue is hv_hybrid: and the callback WebView is AuthWebView. The alternative wv_hybrid: branch reports failure without invoking registry endpoints.

The prompt message is parsed as a URI. Its host selects the map key, its last path segment selects the method name, its port is copied into a fresh context object, and the raw suffix after ? is the payload String (empty becomes {}). The exact reflective lookup is getMethod(name, com.ali.auth.third.ui.context.a.class, String.class): public inherited methods may be selected, but the selected Method must carry runtime BridgeMethod. The annotation value is not read. Invocation passes that fresh context and payload string.

accountBridge and loginBridge each point to a distinct new LoginBridge. Each exposes six public annotated void methods with exactly (context,String): auth, bindByUsername, loginByQrCode, loginByUsername, qrLoginConfirm, unbindByUsername. LoginBridge has no application superclass. The two names produce twelve independently named exposure endpoints. ivBridge points to an empty class, with zero matching annotated endpoints.

This registry is distinct from JavascriptInterface injection. AuthWebView's final addJavascriptInterface override is empty. Complete per-site source hashes, host inheritance, endpoint signatures, annotation rules and dispatch evidence are in ali-prompt-registry-source.json. Dynamic runtime messages are unresolved; this records the available conditional interface, not observed execution. Holdout source was not used.
