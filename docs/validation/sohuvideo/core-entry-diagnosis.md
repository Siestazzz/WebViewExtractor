# Core entry diagnosis

Activity.onCreate -> initFragment -> actual Fragment object+input params+manager replace -> Fragment.onCreateView inflate -> onViewCreated -> initView writes concrete custom WebView field -> initViewByData -> initWebSetting -> virtual factories + capabilities

The crucial order is initView -> field write, then initViewByData -> initWebSetting. initWebSetting is called at line 2915 inside initViewByData, not initView. A scalar method-priority hint cannot help a concrete seed that has not yet been reached or was enqueued while its field was unknown.

CapabilityEngine.seed permits all relevant methods of a Fragment/component, including ordinary helpers, and gives non-View arguments entry_parameter placeholders. lifecycle-style onViewCreated receives fragment_view placeholder. This can introduce many helper contexts before the actual input/layout writes, but v2 pending metrics alone do not prove which job was lost.

Real source entry exists; unfinished v2 traversal is established. Pseudo-root flooding is a plausible generic mechanism, but requires job-origin trace to distinguish it from unresolved entry/layout binding. No proven propagation bug solely from this source audit.

See JSON for exact source evidence, minimal structural reproduction and the required generic job-origin diagnostics. Do not use package names or business-method names as production routing rules.
