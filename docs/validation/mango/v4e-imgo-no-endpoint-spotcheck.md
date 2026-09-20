# v4e Imgo no-compatible-endpoint spot check

The v4e extractor still models the generic dispatch adapter as the exposed JavaScript member for the eight registrations proven to have no compatible reflected endpoint.

For every matching emitted fact inspected, v4e reports:

- kind `message_bridge`;
- implementation `com.mgtv.h5.ImgoWebJavascriptImpl$e0`;
- member `Lcom/mgtv/h5/ImgoWebJavascriptImpl$e0;->handler(Ljava/lang/String;Lcom/hunantv/imgo/h5/jsbridge/CallBackFunction;)V`.

This is incorrect endpoint attribution. JavaScript selects the captured registration name; `e0.handler` then executes `ImgoWebJavascriptImpl.class.getDeclaredMethod(name, String.class)`. The callable endpoint, if present, must therefore be the same-name `(String)` member on `ImgoWebJavascriptImpl`. Independent DEX validation proves that none exists for `getSMSNumber`, `getSMSCode`, `payWithWeChat`, `share`, `showCustomShareMenus`, `invokeH5Callback`, `webviewBecomeActive`, or `webviewEnterBackground`. The adapter's `handler` method is transport/dispatch machinery and is not registered under the JavaScript route as the semantic endpoint.

Among the seven canonical Imgo hosts, all seven Activities are emitted. Six contain 96 matching facts; all 96 attach `e0.handler`, and three hosts emit the eight routes three times. `WebUIActivity` emits no matching facts for these names. Across the broader v4e output, the same incorrect attribution also appears on non-canonical hosts reached through the shared Imgo framework.

Expected representation: retain a registration fact with `registered-no-compatible-endpoint`, blank callable-member list, and no endpoint implementation. `e0.handler` may be recorded only as the registry dispatcher in framework semantics.
