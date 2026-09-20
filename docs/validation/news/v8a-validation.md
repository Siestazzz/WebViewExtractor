# Tencent News v8a independent validation

## Ownership result

All 37 emitted Activities were checked. The result is 37 `valid`, 0 `wrong`, and 0 `uncertain`; the conservative ownership error upper bound is 0/37. Unchanged hosts reuse the source evidence in the corrected v7 ownership audit. The four newly emitted login hosts were independently traced through their concrete Activity objects and displayed SDK dialogs.

`HippyDetailActivity` is absent from the v8a activity list. Its former invalid chain—constructing a subclass of `H5JsApiScriptInterface` and then traversing an arbitrary inherited method without an invoke—does not appear in any v8a Activity evidence reviewed here. The targeted false-owner removal therefore succeeded.

## Login Dialog ownership

All four new hosts are valid conditional UI hosts under the established Dialog rule: the same Activity object reaches a concrete Dialog constructor, a reachable branch invokes `show()`, and the Dialog creates and attaches its WebView.

- `LoginActivity` initiates QQ login through its presenter and passes itself into `Tencent.login`. `AuthAgent` retains that Activity in a weak reference, recovers it on the UI thread, checks that it is not finishing, constructs `AuthDialog` or fallback `TDialog`, and calls `show()`.
- `LoginWithBackgroundActivity` extends `LoginActivity`; the inherited login path executes on the concrete subclass instance, so the same object reaches the displayed dialog.
- `LoginWithPhoneNumActivity` attaches `LoginWithPhoneNumView` from its own content view. The view receives the Activity as Context and creates the OAuth presenter from that Context. Its QQ-login/retry path passes the same Activity through Tencent login to the displayed SDK dialog.
- `LoginWithVerCodeActivity` directly initiates the same presenter/Tencent login flow with itself as Activity and reaches the displayed AuthDialog/TDialog branches.

For the secure-login branch, `AuthAgent` constructs `AuthDialog(activity, ...)` and invokes `show()` only after `!activity.isFinishing()`. `AuthDialog.b()` creates `com.tencent.open.d.d`, stores it in field `j`, adds it to a container, and calls `setContentView`. For secure-library failure or proxy/no-QQ branches, AuthAgent constructs `TDialog(activity, ...)`, checks the Activity state, and calls `show()`; `TDialog.onCreate/a()` creates field `i` and attaches its WebView container.

Relevant exact methods include:

- `Lcom/tencent/tauth/Tencent;->login(Landroid/app/Activity;Ljava/lang/String;Lcom/tencent/tauth/IUiListener;Z)I`
- `Lcom/tencent/connect/auth/AuthAgent;->doLogin(Landroid/app/Activity;Ljava/lang/String;Lcom/tencent/tauth/IUiListener;ZLandroidx/fragment/app/Fragment;ZLjava/util/Map;)I`
- `Lcom/tencent/connect/auth/AuthDialog;->b()V`
- `Lcom/tencent/connect/auth/AuthDialog;->j:Lcom/tencent/open/d/d;`
- `Lcom/tencent/open/TDialog;->a()V`
- `Lcom/tencent/open/TDialog;->i:Lcom/tencent/open/d/b;`

These are conditional capabilities. Passing an Activity as Context alone would not suffice; the verified `show()` and container attachment complete each ownership chain.

The public 28-host canonical metrics remain unchanged at bridge 1264/1365, settings 275/338, and callbacks 193/228. This review does not alter gold facts. Sealed holdout data was not opened.
