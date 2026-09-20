# Ctrip H5 plugin registry audit

Result: `pass`. This audit reads only decompiled source and oracle groups; it does not read extractor output.

## ctrip-h5-v1

The unconditional registry contains 34 plugins and 222 annotated methods. Samsung Wallet is excluded here because registration is guarded by an H5Fragment type check.

| Type | TAG | Methods | Inherited |
|---|---:|---:|---:|
| `ctrip.android.view.h5.plugin.H5UtilPlugin` | `Util_a` | 48 | 17 |
| `ctrip.android.view.h5.plugin.H5BusinessPlugin` | `Business_a` | 28 | 0 |
| `ctrip.android.view.h5.plugin.H5LocatePlugin` | `Locate_a` | 12 | 5 |
| `ctrip.android.view.h5.plugin.H5HyBusinessPlugin` | `HyBusiness_a` | 2 | 0 |
| `ctrip.android.view.h5.plugin.H5HyToolPlugin` | `HyTool_a` | 2 | 0 |
| `ctrip.android.view.h5.plugin.H5HyAppPlugin` | `HyApp_a` | 4 | 0 |
| `ctrip.android.view.h5.plugin.H5HyGeoLocationPlugin` | `HyGeoLocation_a` | 1 | 0 |
| `ctrip.android.view.h5.plugin.H5NavBarPlugin` | `NavBar_a` | 10 | 0 |
| `ctrip.android.view.h5.plugin.H5LogPlugin` | `Log_a` | 6 | 0 |
| `ctrip.android.view.h5.plugin.H5UserPlugin` | `User_a` | 9 | 0 |
| `ctrip.android.view.h5.plugin.H5PipePlugin` | `Pipe_a` | 3 | 0 |
| `ctrip.android.view.h5.plugin.H5FilePlugin` | `File_a` | 8 | 0 |
| `ctrip.android.view.h5.plugin.H5DownloaderPlugin` | `Downloader_a` | 2 | 0 |
| `ctrip.android.view.h5.plugin.H5PagePlugin` | `Page_a` | 13 | 0 |
| `ctrip.android.view.h5.plugin.H5SharePlugin` | `Share_a` | 3 | 0 |
| `ctrip.android.view.h5.plugin.H5ImagePlugin` | `Image_a` | 5 | 4 |
| `ctrip.business.plugin.h5.H5AlbumPlugin` | `Photo_a` | 4 | 0 |
| `ctrip.business.videoupload.plugin.H5VideoUploadPlugin` | `VideoSplitUpload_a` | 4 | 0 |
| `ctrip.android.view.h5.plugin.H5StoragePlugin` | `Storage_a` | 3 | 0 |
| `ctrip.android.view.h5.plugin.H5EventPlugin` | `Event_a` | 3 | 0 |
| `ctrip.android.view.h5.plugin.H5UBTPlugin` | `UBT_a` | 1 | 0 |
| `ctrip.android.view.h5.plugin.H5HySharePlugin` | `HyShare_a` | 2 | 0 |
| `ctrip.android.view.h5.plugin.H5HyLogPlugin` | `HyLog_a` | 2 | 0 |
| `ctrip.android.view.h5.plugin.H5HyWebViewPlugin` | `HyWebView_a` | 5 | 0 |
| `ctrip.android.view.h5.plugin.H5HyNavigatorPlugin` | `HyNavigator_a` | 1 | 0 |
| `ctrip.android.view.h5.plugin.H5AdSdkPlugin` | `HybridAD_a` | 15 | 0 |
| `ctrip.android.view.h5.plugin.H5MapPlugin` | `Map_a` | 4 | 0 |
| `ctrip.android.view.h5.plugin.H5EncryptPlugin` | `Encrypt_a` | 3 | 0 |
| `ctrip.android.view.h5.plugin.H5PermissionPlugin` | `Permission_a` | 1 | 0 |
| `ctrip.business.plugin.h5.calendar.H5CalendarPlugin` | `Calendar_a` | 9 | 0 |
| `ctrip.android.view.h5.plugin.H5ScreenPlugin` | `Screen_a` | 2 | 0 |
| `ctrip.business.plugin.h5.H5VideoPlugin` | `VideoPlayer_a` | 4 | 0 |
| `ctrip.android.view.h5.plugin.H5CtripApplicationPlugin` | `Application_a` | 1 | 0 |
| `ctrip.android.view.h5.plugin.H5NetworkPlugin` | `Network_a` | 2 | 0 |

## ctrip-h5-v2

The unconditional registry contains 35 plugins and 228 annotated methods. Samsung Wallet is excluded here because registration is guarded by an H5Fragment type check.

| Type | TAG | Methods | Inherited |
|---|---:|---:|---:|
| `ctrip.android.view.h5v2.plugin.H5UtilPlugin` | `Util_a` | 47 | 15 |
| `ctrip.android.view.h5v2.plugin.H5BusinessPlugin` | `Business_a` | 27 | 0 |
| `ctrip.android.view.h5v2.plugin.H5LocatePlugin` | `Locate_a` | 12 | 5 |
| `ctrip.android.view.h5v2.plugin.H5HyBusinessPlugin` | `HyBusiness_a` | 2 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HyToolPlugin` | `HyTool_a` | 2 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HyAppPlugin` | `HyApp_a` | 4 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HyGeoLocationPlugin` | `HyGeoLocation_a` | 1 | 0 |
| `ctrip.android.view.h5v2.plugin.H5NavBarPlugin` | `NavBar_a` | 12 | 0 |
| `ctrip.android.view.h5v2.plugin.H5LogPlugin` | `Log_a` | 6 | 0 |
| `ctrip.android.view.h5v2.plugin.H5UserPlugin` | `User_a` | 9 | 0 |
| `ctrip.android.view.h5v2.plugin.H5PipePlugin` | `Pipe_a` | 3 | 0 |
| `ctrip.android.view.h5v2.plugin.H5FilePlugin` | `File_a` | 8 | 0 |
| `ctrip.android.view.h5v2.plugin.H5DownloaderPlugin` | `Downloader_a` | 2 | 0 |
| `ctrip.android.view.h5v2.plugin.H5PagePlugin` | `Page_a` | 14 | 0 |
| `ctrip.android.view.h5v2.plugin.H5SharePlugin` | `Share_a` | 3 | 0 |
| `ctrip.android.view.h5v2.plugin.H5ImagePlugin` | `Image_a` | 5 | 4 |
| `ctrip.business.plugin.h5.H5AlbumPluginV2` | `Photo_a` | 4 | 0 |
| `ctrip.business.videoupload.plugin.H5VideoUploadPluginV2` | `VideoSplitUpload_a` | 4 | 0 |
| `ctrip.android.view.h5v2.plugin.H5StoragePlugin` | `Storage_a` | 3 | 0 |
| `ctrip.android.view.h5v2.plugin.H5EventPlugin` | `Event_a` | 3 | 0 |
| `ctrip.android.view.h5v2.plugin.H5UBTPlugin` | `UBT_a` | 1 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HySharePlugin` | `HyShare_a` | 2 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HyLogPlugin` | `HyLog_a` | 2 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HyWebViewPlugin` | `HyWebView_a` | 6 | 0 |
| `ctrip.android.view.h5v2.plugin.H5HyNavigatorPlugin` | `HyNavigator_a` | 1 | 0 |
| `ctrip.android.view.h5v2.plugin.H5AdSdkPlugin` | `HybridAD_a` | 16 | 0 |
| `ctrip.android.view.h5v2.plugin.H5MapPlugin` | `Map_a` | 4 | 0 |
| `ctrip.android.view.h5v2.plugin.H5EncryptPlugin` | `Encrypt_a` | 3 | 0 |
| `ctrip.android.view.h5v2.plugin.H5PermissionPlugin` | `Permission_a` | 2 | 0 |
| `ctrip.business.plugin.h5.calendar.H5CalendarPluginV2` | `Calendar_a` | 9 | 0 |
| `ctrip.android.view.h5v2.plugin.H5ScreenPlugin` | `Screen_a` | 2 | 0 |
| `ctrip.android.view.h5v2.plugin.H5NetworkPlugin` | `Network_a` | 2 | 0 |
| `ctrip.business.plugin.h5.H5VideoPluginV2` | `VideoPlayer_a` | 4 | 0 |
| `ctrip.android.view.h5v2.plugin.H5CtripApplicationPlugin` | `Application_a` | 1 | 0 |
| `ctrip.business.evaluation.H5InvitePlugin` | `InstantSurvey_a` | 2 | 0 |

## Conditional applicability

Conditional groups have 0 Activity links. A zero count is required: Bus and type-guarded plugins remain possible runtime registrations, not unconditional per-Activity recall facts.

Settings use the method name from `normalized_api`. `value_kind=literal` and `value_kind=enum` are exact comparison values; `value_kind=dynamic` preserves the Java expression in `source_expression` and leaves `value` null.
