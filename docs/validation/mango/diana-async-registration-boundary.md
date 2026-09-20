# Diana asynchronous registration boundary

Three explicit registration adapters are enough to schedule Diana's real callback entry methods. They are not enough by themselves to prove `renderMiniApp`, because the callbacks must preserve one `DianaView` identity and its two-event state join.

The network edge is exactly `Lokhttp3/e;->H(Lokhttp3/f;)V`. Its concrete callback argument is `Lcom/mgtv/diana/h0/c$a;`; only `onResponse(e,b0)` and `onFailure(e,IOException)` become asynchronous entries. The adapter field `a:Lcom/mgtv/diana/h0/c$d;` retains the request, and request field `h:Lcom/mgtv/diana/h0/c$c;` retains the original `DianaView$c`. Constructing any of these objects without observing `H(f)` is insufficient.

The main-thread edge is exactly `Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z`. It schedules only the concrete posted `ny.a` or `ny.b` instance. Its captured callback must remain the same request `h` object; `ny.a.run` then reaches `DianaView$c.onSuccess` through the interface dispatch.

The Android service edge is exactly `Landroid/content/Context;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z`, invoked on the `Activity` held in `DianaView.mAct`. Both observed Diana sites pass an explicit Intent targeting `MiniAppIpcService`, pass `this` as argument 1, and pass flag value `1`: private `DianaView.bindService(Activity)` and `DianaView.lambda$new$4()`. Therefore the registered `ServiceConnection` is the same `DianaView`, and the valid framework entries are its exact `onServiceConnected(ComponentName,IBinder)` and `onServiceDisconnected(ComponentName)` methods.

The analysis must retain `DianaView$c.this$0 == bindService arg1 == onServiceConnected receiver`. The bind return writes that receiver's `mConnected`; `onServiceConnected` writes its `mIpcBridgeService`. Rendering additionally requires the package-load callback to set `packageReady` on the same receiver. A possible service callback must not be interpreted as successful binding, fixed callback order, or satisfaction of this state join.
