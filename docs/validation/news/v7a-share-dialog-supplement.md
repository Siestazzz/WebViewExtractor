# v7a share-dialog ownership supplement

This supplement revisits the two Tencent share Activities under the project definition that an Activity owns capability exposed by a Dialog it actually invokes and displays. It preserves the earlier ownership files as history rather than overwriting them.

## Revised result

`MobileQQActivity` and `QzoneShareActivity` should be `valid` conditional hosts, not `wrong`. The previous decision required the WebView to return to or be stored by the Activity, which is stricter than the accepted Dialog-component model.

For Mobile QQ, `MobileQQActivity` passes `this` into `news.share.channel.b`; the channel stores the same instance and passes it to `Tencent.shareToQQ`. Tencent constructs `QQShare` and forwards that Activity. When native QQ sharing is unsupported, `QQShare.shareToQQ` executes `new TDialog(activity, ...).show()`. `TDialog.onCreate` calls its initializer, creates/stores `com.tencent.open.d.b` in field `i`, and that WebView constructor calls `a()` to remove the three JavaScript interfaces. The first v7a fact is therefore valid on this conditional UI path.

For Qzone, `QzoneShareActivity` likewise passes `this` through channel `c` and `Tencent.shareToQzone`. After parameter validation, Qzone SDK code has two relevant displayed-dialog paths: an unsupported-version branch directly calls `new TDialog(activity, ...).show()`, while the 4.2–4.6 compatibility branch constructs `QQShare` with the same Activity and calls `shareToQQ`, which can show the same download dialog. The v7a evidence uses the second path and is valid conditionally.

The correct general rule is broader than Activity field ownership: a dialog capability may bind to an Activity when the same Activity object flows into the Dialog constructor and a reachable branch invokes `show()`. A mere Context argument without construction/display remains insufficient. The model should require all of:

1. identity-preserving Activity argument flow;
2. concrete Dialog construction with that argument;
3. a reachable `show()` call;
4. Dialog lifecycle/initializer flow to the WebView receiver.

`HippyDetailActivity` remains wrong. Its chain still jumps from a subclass constructor to an arbitrary inherited method without an invocation or registered dispatch.

Applied to v7a, the corrected ownership count is 33 valid, 1 wrong, 0 uncertain; conservative ownership error upper bound is 1/34 = 2.94%. This is a supplemental correction and does not rewrite `v7a-ownership.jsonl`.

## Ownership definition clarification

A Dialog WebView is part of an Activity's capability surface when source proves that the same Activity instance reaches the Dialog constructor, a reachable execution branch calls `show()`, and the displayed Dialog creates or attaches that WebView. The WebView does not need to be written back into an Activity field.

Passing an Activity or Context to a method is not sufficient by itself. Without a concrete displayed Dialog, attached container/view tree, returned owned wrapper, or equivalent UI ownership evidence, callee-internal WebViews must not be attributed to the caller.

The corrected current ownership export is `v7-ownership.jsonl`. Historical `v7a-ownership.jsonl` remains unchanged to preserve the original decision and subsequent correction trail.
