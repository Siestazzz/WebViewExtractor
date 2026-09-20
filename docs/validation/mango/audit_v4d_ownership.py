#!/usr/bin/env python3
import json,collections
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
report=json.load(open(R/'test/runs/v4d/com.hunantv.imgo.activity/0/output/capabilities.json'))
assert report['apk_sha256']==SHA and len(report['activities'])==92
old={x['activity']:x for x in map(json.loads,open(H/'v1-ownership.jsonl'))}
wrong={'com.mgtv.agent.ui.MgAgentActivity','com.mgtv.agent.ui.MgAgentFloatActivity','com.mgtv.libvip.activity.VipChannelPreviewActivity'}
# New source-reviewed chains. DebugDialog coroutine continuations in the three wrong hosts
# are shared callback reachability, not calls made by their Activity/Fragment paths.
family={
 'LuckyBag':'Activity superclass onInitializeData -> renderer createView -> WebView renderer',
 'VodGame':'Activity/MiniAppFragment -> DianaView -> GamePage/PageWebView',
 'ShareDialog':'Activity share action -> MGShareActivity -> Tencent QQShare -> TDialog WebView',
 'RewardAd':'onCreate -> MgAdSessionView -> mgadplus BridgeWebView',
 'NFT':'Activity/Fragment -> MgNftViewer -> NftWebviewLayout',
 'WebContainer':'initData -> WebViewFragment -> generated ViewBinding.webview:ImgoWebView',
 'Diana':'Activity -> DianaView/MiniAppFragment -> PageWebView',
 'Channel':'Activity -> ChannelIndexFragment -> ChannelWebViewHelper -> ImgoWebView',
}
def reason(a):
 if a in wrong:return 'wrong: shared coroutine/DebugDialog continuation has no source call from this host to PageWebView construction'
 if 'Lucky' in a or 'Luck' in a:return family['LuckyBag']
 if 'VodGame' in a or 'LiteVod' in a:return family['VodGame']
 if a.endswith('SmallCardResultActivity') or a.endswith('VideoDetailActivity'):return family['ShareDialog']
 if a.endswith('RewardAdFreeActivity'):return family['RewardAd']
 if 'Digital' in a or 'Nft' in a or 'Antique' in a:return family['NFT']
 if a.endswith('WebContainerActivity'):return family['WebContainer']
 if a.endswith('MangoMiniAppActivity'):return family['Diana']
 if 'ChannelBackyard' in a or 'ChannelSecondIndex' in a:return family['Channel']
 return 'source-reviewed direct/inherited/fragment carrier chain'
out=[]
for item in report['activities']:
 a=item['activity']; src=R/'test/decompiled/com.hunantv.imgo.activity/sources'/Path(*a.split('.')).with_suffix('.java'); assert src.exists(),a
 if a in old:
  verdict=old[a]['status']; why='reused independent v1 source verdict: '+old[a]['reason']; evidence=old[a]['source_evidence']
 else:
  verdict='wrong' if a in wrong else 'valid'; why=reason(a); evidence=[str(src.relative_to(R)),*item['facts'][0].get('evidence',[])[:5]]
 out.append({'activity':a,'verdict':verdict,'fact_count':len(item['facts']),'reason':why,'source_evidence':evidence,'apk_sha256':SHA})
assert len(out)==92 and len({x['activity'] for x in out})==92
with open(H/'v4d-ownership.jsonl','w') as f:
 for x in out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
c=collections.Counter(x['verdict'] for x in out)
lines=['# v4d Mango Activity ownership','',f"Reviewed all {len(out)} emitted Activities against the same APK source/DEX. Existing independent v1 verdicts were reused for {sum(x['activity'] in old for x in out)} Activities; 24 newly emitted Activities received source-chain review. Holdout data was not read.",'',f"Result: {c['valid']} valid, {c['wrong']} wrong, {c['uncertain']} uncertain.",'','The three new wrong associations are `MgAgentActivity`, `MgAgentFloatActivity`, and `VipChannelPreviewActivity`. Their reported path crosses a generic coroutine continuation into `DebugDialog...injectConsole` and then `PageWebView`; the host Activity/Fragment source does not call that debug continuation or construct the reported WebView. Shared coroutine machinery is insufficient ownership evidence.','', 'New valid families include LuckyBag renderer inheritance, Diana mini-app/GamePage carriers, explicit commerce share-to-`TDialog` paths, mgadplus reward rendering, NFT viewer layouts, WebContainer ViewBinding, and channel WebView helpers. Conditional share dialog paths remain valid positive ownership paths.','', '| verdict | count |','|---|---:|']+[f'| {k} | {c[k]} |' for k in ('valid','wrong','uncertain')]
(H/'v4d-ownership.md').write_text('\n'.join(lines)+'\n')
print(dict(c))
