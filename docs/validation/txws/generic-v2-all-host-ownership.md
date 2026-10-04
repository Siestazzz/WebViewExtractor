# generic-v2 full emitted-host ownership review

All 15 emitted Activities have a source-supported direct, inherited, Fragment or QQ auth dialog WebView association. All 15 rows use scope=activity_host and verdict=valid with the exact generic-v2 report hash. This establishes source host association for this report only; capability precision is a separate, uncompleted audit. QQScanLogin follows the actual caller Activity through Tencent login WeakReference and on-UI-thread auth Dialog construction. HorizontalVideo follows the pay-button Fragment rather than its navigation to another Activity. Neither is declared valid just because it can launch a web Activity.

This review does not prove there are fewer than 30 source-positive Activities in the APK. The full Manifest remains partly unknown and non-emitted mini-program/Hippy paths require further review.
