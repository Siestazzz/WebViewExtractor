# Bridge normalization audit

## Imgo message-handler registrations

`ImgoWebView.registerWebHandler()` makes 138 registrations at `ImgoWebView.java:536-673`. Seven disclosed hosts use that same initialization chain, so all 966 registration facts remain in `facts.jsonl` and `canonical-facts.jsonl`.

The endpoint rule comes from `ImgoWebJavascriptImpl.java:732-758`: `ImgoWebJavascriptImpl$e0.handler()` calls `ImgoWebJavascriptImpl.class.getDeclaredMethod(registrationName, String.class)` and invokes the resulting method. Therefore:

- `registration_name` records the JavaScript message name independently.
- `registration_expression` preserves the literal or source constant used at the registration site.
- `bridge_method` and `normalized_signature` contain only the exact reflected `ImgoWebJavascriptImpl;(Ljava/lang/String;)V` target found in `test/runs/symbols/mango.jsonl`.
- `implementation` is `com.mgtv.h5.ImgoWebJavascriptImpl` only when that target exists. It is `null` for an unknown target.
- A registration without an exact target remains a `kind=bridge` row with `binding_status=registered-target-unknown`, blank endpoint signature fields, and `symbol_status=not_applicable`.

The prior form used `ImgoWebJavascriptInterface.registerHandler(String, ImgoWebView)` as `normalized_signature` on registration rows. That descriptor identifies the setup API, not a JavaScript endpoint, and is no longer emitted as an endpoint.

## Constant registrations

Nine non-literal expressions resolve from their source declarations:

| expression | registration name | source |
|---|---|---|
| `AIDLConstants.FUN_NAME.CONFIRM_LOGIN` | `confirmLogin` | `AIDLConstants.java:19` |
| `MgtvMethodChannel.L` | `getUserInfo` | `MgtvMethodChannel.java:53` |
| `t.f135638u` | `feedback` | `eo/t.java:72` |
| `DianaEventDefine.ON_USER_CAPTURE_SCREEN` | `onUserCaptureScreen` | `DianaEventDefine.java:9` |
| `JsApiPage.SHOW_TOAST` | `showToast` | `JsApiPage.java:33` |
| `AIDLConstants.FUN_NAME.SEND_COMMENT` | `sendComment` | `AIDLConstants.java:57` |
| `AIDLConstants.FUN_NAME.SHOW_MANGO_KID` | `showMangoKid` | `AIDLConstants.java:64` |
| `VideoInteractionEvent.f55356g` | `VideoInteractionEvent` | `VideoInteractionEvent.java:39` |
| `VideoSetPlayerMutedEvent.f55366f` | `VideoSetPlayerMuted` | `VideoSetPlayerMutedEvent.java:35` |

All nine have exact reflected DEX targets.

## Counts and retained unknowns

The 966 Imgo rows now split into 910 confirmed endpoints and 56 registration-only facts. The latter are eight names repeated across seven hosts: `getSMSNumber`, `getSMSCode`, `payWithWeChat`, `share`, `showCustomShareMenus`, `invokeH5Callback`, `webviewBecomeActive`, and `webviewEnterBackground`.

These rows were not deleted. For example, `invokeH5Callback()` exists as a zero-argument method, but the dispatcher requests `(String)`, so it cannot be claimed as the reflected endpoint.

## CCB object bridge

`H5PayActivity.java:206` registers inner object `H5PayActivity$d` under `javaObj`. The registration row and both DEX-confirmed annotated members are retained as three separate facts. The two member rows use `registration_name=javaObj`, while `value` distinguishes `sdkCallBack` from `showFinish`; neither concatenates the object and method into a fabricated registration name.

## Reproduction

```sh
python3 docs/validation/mango/build_inventory.py
python3 docs/validation/mango/build_wallet_truth.py
python3 docs/validation/mango/build_alipay_truth.py
python3 docs/validation/mango/build_pangle_truth.py
python3 docs/validation/mango/finalize_canonical.py
```

After the four-host Pangle expansion, the full canonical file has 1,925 rows: 1,469 symbol-confirmed, 369 Android external APIs, and 87 not-applicable registration/binding rows. The bridge subset has 1,085 rows: 1,029 confirmed bindings and 56 retained unknown-target registrations.
