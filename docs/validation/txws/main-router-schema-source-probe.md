# MainActivity source-positive omission probe

The patched APK contains a concrete application bootstrap service binding and a legitimate configured schema-tab path to an actual child WebViewBaseFragment under MainActivity. The host is absent in v7 output. Source hashes and exact source lines, including application Router installation, typed factory registration and actual child add/commit, are recorded in main-router-schema-source-probe.json. This is development source evidence, not an engine root-cause claim or a completed whole-host deep audit.

Reusable pattern: an application-installed typed lambda factory returns an implementation; a pager/tab callback obtains a Fragment through the service map; the returned Fragment is attached to an Activity-owned child FragmentManager. A general engine must preserve both registry key-to-factory association and returned Fragment ownership.
