# Txws v14: real installation behind198 frozen regressions

All six hosts have explicit source-positive installation paths. The frozen oracle remains unchanged. Patched APK SHA199a194170a2fcb7e3d9261b77deaaa1d803289fc627c9f75bc68af8652a70d5 matches source build metadata. Exact source quotes/hashes and actual DEX invokes are in the sibling JSON.

| Host | Actual installation |
| --- | --- |
| WebviewBaseActivity | onCreate → create/store mWebViewFragment → showFragment → replace3arg/commit |
| MainProcessWebviewActivity | inherits the complete base lifecycle/transaction |
| NotifyDialogWebViewActivity | explicit super.onCreate → base lifecycle/transaction |
| HalfWebViewActivity | actual support-manager controller → HalfWebFragment add3arg/commit; separate dialog path installs child with replace3arg/commitAllowingStateLoss |
| TenvideoPayWebViewActivityLandscape | super → PayActivityBase → static helper with actual Activity/manager → TencentVideoPayWebFragment add3arg/commit |
| HorizontalVideoActivity | initVipPayGuideModule → manager-held PayGuideManager/payButton → add3arg/show/commitAllowingStateLoss |

Installed HalfWebFragment and TencentVideoPayWebFragment inherit WebViewBaseFragment. Its onCreateView initializes the UI and actual pooled ScrollObservableWebView, retains mWebView, and configures settings/clients. These are actual transaction chains; no claim that allocation alone installs a Fragment is needed. Runtime URI/bundle preconditions remain conditional.

Actual WebviewBaseActivity.showFragment DEX calls:

```text
@12 FragmentActivity.getSupportFragmentManager()FragmentManager
@16 FragmentManager.beginTransaction()FragmentTransaction
@29 FragmentTransaction.replace(int,Fragment,String)FragmentTransaction
@32 FragmentTransaction.commit()int
```

Every owner is the standard AndroidX class, not an App transaction subclass. Half transparent and Pay helper use standard add3arg/commit. PayGuideManager additionally uses standard show(Fragment) returning the same transaction before commitAllowingStateLoss. Source includes packaged SDK implementations: FragmentActivity's manager getter, FragmentManager's new BackStackRecord, concrete FragmentTransaction operation bodies and BackStackRecord commit.

`PackagedFragmentProtocolProbe` isolates the packaged-body distinction on frozenv14:

| Case | Bridge/settings/callback |
| --- | --- |
| absent standard SDK bodies | 1/1/1 |
| packaged standard SDK bodies | 0/0/0 |
| App getter override returns null | 0/0/0 |
| uncommitted transaction | 0/0/0 |
| allocation only | 0/0/0 |
| App getter override forwards super | 0/0/0; separate forwarding gap |

The analyzer's generic view-access guard rejects implemented methods unless the declaring owner is android.* or exact AndroidX Fragment. Therefore the real standard packaged FragmentActivity/Manager/Transaction implementations are rejected as though they were App overrides. The engine agent has received actual source/DEX evidence and this generic regression. A correction should admit exact standard public Fragment contracts while preserving concrete App override and installation negatives. It must not admit arbitrary AndroidX code or new-as-install.

Reproduce diagnostic mode:

```sh
javac -cp test/runs/generic-v14.jar -d test/runs/txws-v14-fragment-source/probe-classes src/test/java/org/example/ServiceConstructorArrayProbe.java src/test/java/org/example/PackagedFragmentProtocolProbe.java
java -cp test/runs/txws-v14-fragment-source/probe-classes:test/runs/generic-v14.jar org.example.PackagedFragmentProtocolProbe
```

Public run() and main verify enforce the two installation positives and three negatives for the corrected engine. The explicit-super positive is recorded separately and does not weaken the null-override negative. No production or frozen facts were edited here. Synthetic restoration is not actual six-host recovery or overall acceptance.
