# Application proxy attach: exact source and platform boundary

The APK manifest names TNApplication. Its constructor supplies a literal packaged delegate class to the proxy. Proxy attachBaseContext allocates that delegate through Class.forName(name,true,classLoader).newInstance, then calls its reflection helper with the actual delegate object, method name attach, exact Context parameter array and actual Context argument array.

The helper is source-confirmed: it starts at obj.getClass(), searches each superclass with getDeclaredMethod(name,types), marks the selected Method accessible and returns it; the caller invokes that Method on the same delegate. Direct DEX class-data examination verifies the complete packaged hierarchy Application -> BaseApplication -> RePluginApplication -> android.app.Application. None of the three packaged classes declares attach. The delegate declares no attachBaseContext override, while BaseApplication declares public attachBaseContext(Context). All hashes, flags, method IDs and source locations are in the JSON.

Official AOSP Android 16 framework source independently defines package-private final Application.attach(Context) and calls attachBaseContext(context) on the same receiver. This closes the framework dispatch contract from the selected attach target to the delegate's BaseApplication override; it is not an inference from the method name. The source is pinned to [AOSP revision 99b01a65cc4c104933788b3143285ab6bae65827](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/app/Application.java), with full fetched-file SHA and exact lines 345–346 recorded.

Successful runtime reflective access remains a condition. This audit does not certify every Android release, benchmark device runtime or hidden-API policy. The packaged helper explicitly requests accessibility; the target is hidden framework code, so a generic implementation should record platform-contract support and retain unresolved access where necessary instead of treating a name as an unconditional executable target.

Once this delegate attach path executes, BaseApplication.attachBaseContext calls Services.init(false), which reflects Loader.init through the ordinary Method.invoke wrapper and reaches the generated static registry. Those later side-effect and global-state boundaries are separately reproduced in the v11 startup probe. The prior constructor-only positive cannot replace this full chain.

No production code, oracle or holdout was changed.
