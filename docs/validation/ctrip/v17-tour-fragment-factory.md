# Tour search Fragment factory binding (Ctrip 8.78.0)

This is an independent source/DEX audit of the single `CTTourSearchActivity2 -> SearchH5Fragment2` path. It does not infer the binding from an extractor report and does not generalize the result to arbitrary `Fragment` subclasses.

## Result

The binding is **confirmed, conditional, and object-specific**. For an included search tab whose URL is not a CRN URL, `CTTourSearchActivity2.createPages()` creates one page descriptor containing the exact class name of `SearchH5Fragment2` and that tab's `Bundle`. `configTabs()` passes the same `FragmentPagerItems` object to a newly constructed `SearchPageAdapter` and installs that exact adapter on the activity's `ViewPager2`. The framework calls the adapter's `createFragment(position)` callback; it reads the descriptor at that position and calls `Fragment.instantiate(context, storedClassName, storedBundle)`. The resulting `SearchH5Fragment2` instance then enters the ordinary Fragment lifecycle and its inherited H5 initialization creates a `SearchWebView`.

The necessary conditions are: the tab survives the `searchTabs` membership filter, its URL is classified non-CRN, `configTabs()` runs after the pages have been created, and ViewPager2 requests that position. Merely observing the `SearchH5Fragment2.class` literal is insufficient; the adapter installation and callback chain below establish the live factory path.

## Exact chain

1. `Lctrip/android/tour/search/view/CTTourSearchActivity2;->createPages()V` allocates `FragmentPagerItems`, writes it to `pages`, and for the non-CRN branch calls `a.f(name, SearchH5Fragment2.class, bundle, customTab)` before adding the returned descriptor to that same collection. Source: `CTTourSearchActivity2.java:2288-2290,2295-2301,2344-2373`.
2. `Lctrip/android/tour/search/view/widget/AdvancedTabView/utils/v4/a;->f(Ljava/lang/CharSequence;Ljava/lang/Class;Landroid/os/Bundle;Lctrip/android/tour/search/model/response/CustomTab;)Lctrip/android/tour/search/view/widget/AdvancedTabView/utils/v4/a;` delegates to `e(..., 1.0f, ...)`. `e` executes `Class.getName()` and its constructor stores the result in field `e:Ljava/lang/String;` and the same bundle in `f:Landroid/os/Bundle;`. Source: `a.java:16-46`.
3. The activity's data callback calls `access$createPages`, then schedules/enters `CTTourSearchActivity2$e.run()`, whose exact method `Lctrip/android/tour/search/view/CTTourSearchActivity2$e;->run()V` calls `access$configTabs`. Source: `CTTourSearchActivity2.java:669-670,682-695`.
4. `Lctrip/android/tour/search/view/CTTourSearchActivity2;->configTabs()Z` constructs `SearchPageAdapter(this, this.pages)`, stores it in `fragmentPagerItemAdapter:Lctrip/android/tour/search/adapter/SearchPageAdapter;`, and calls `ViewPager2.setAdapter` on `viewPager:Landroidx/viewpager2/widget/ViewPager2;`. Source: `CTTourSearchActivity2.java:2074-2078`.
5. The installed adapter is a `FragmentStateAdapter`. Its framework callback `Lctrip/android/tour/search/adapter/SearchPageAdapter;->createFragment(I)Landroidx/fragment/app/Fragment;` reads `pages.get(position)` and invokes descriptor method `d(activity.getBaseContext(), position)`. Source: `SearchPageAdapter.java:27-32,58-73`.
6. `Lctrip/android/tour/search/view/widget/AdvancedTabView/utils/v4/a;->d(Landroid/content/Context;I)Landroidx/fragment/app/Fragment;` first adds `FragmentPagerItem:Position` to the stored bundle, then calls `Landroidx/fragment/app/Fragment;->instantiate(Landroid/content/Context;Ljava/lang/String;Landroid/os/Bundle;)Landroidx/fragment/app/Fragment;` with fields `e` and `f`. This is a class-name/reflection construction step; it invokes the no-argument constructor of the exact stored class and attaches the supplied arguments. Source: `a.java:50-68`.
7. The created type is fixed by the stored name: `Lctrip/android/tour/search/view/v2/SearchH5Fragment2;`, whose DEX parent is `Lctrip/android/view/h5/view/H5Fragment;`. Its `onCreate(Bundle)` calls the parent, and `onCreateView(LayoutInflater,ViewGroup,Bundle)` calls the parent method. Source: `SearchH5Fragment2.java:319-338`.
8. Base `H5Fragment.onCreateView` calls `initLayoutView`; that method invokes virtual `addWebView()`. Dispatch therefore reaches `SearchH5Fragment2.addWebView()`, which executes `mWebView = new SearchWebView(getContext())` and adds that same object to `mWebViewContainer`. Source: `H5Fragment.java:2181-2193,3263-3273`; `SearchH5Fragment2.java:221-232`.
9. On the visible lifecycle path, base `H5Fragment.onResume()` calls virtual `loadWebview()`. The override appends search parameters and explicitly invokes `H5Fragment.loadWebview()`, which calls `initWebView()` unless disabled; `initWebView()` configures the same inherited `mWebView` object. Source: `H5Fragment.java:2935-2953,2996-3014,3506-3527`; `SearchH5Fragment2.java:278-294`.

Object equations established by the writes/reads are:

`activity.pages === adapter.pages`; `descriptor.e == SearchH5Fragment2.class.getName()`; `descriptor.f === tabBundle`; `createFragment(p) result === Fragment.instantiate(context, descriptor.e, descriptor.f)`; and inside that result, `H5Fragment.mWebView === new SearchWebView(fragmentContext)`.

## Precision boundaries

- The CRN branch stores `SearchCRNFragment2`, so this proof does not bind H5 abilities to those positions.
- A tab with nested tabs may also add a `SearchFragment2` descriptor. That is a separate descriptor/position and is not an alias for the `SearchH5Fragment2` descriptor.
- `SearchPageAdapter.getPage(position)` only reads a weak reference written after `createFragment`; it is not a creation edge.
- `PatchProxy` can replace method bodies at runtime. The static APK path is confirmed when proxy interception is not active; this audit makes no claim about an externally supplied hot patch.

All listed application methods and fields were checked against `test/runs/symbols/ctrip.jsonl`. APK SHA-256: `cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66`.
