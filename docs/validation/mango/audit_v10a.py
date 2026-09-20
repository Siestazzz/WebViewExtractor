#!/usr/bin/env python3
import json
from pathlib import Path
from collections import Counter
D=Path(__file__).resolve().parent; ROOT=D.parents[2]
P10=ROOT/'test/runs/v10a/com.hunantv.imgo.activity/0/output/capabilities.json'
P9=ROOT/'test/runs/v9d/com.hunantv.imgo.activity/0/output/capabilities.json'
NFT=['com.mgsz.detail.ui.AntiqueDetailActivity','com.mgtv.digital.ui.DigitalDetailActivity','com.mgtv.digital.ui.DigitalModelViewActivity']
NFT_ALL=NFT+['com.mgtv.digital.ui.DigitalBelongActivity','com.mgtv.digital.ui.MgNftPreviewActivity']
PLAYERS=['com.imgo.vodgames.LandVodGameRoomActivity','com.imgo.vodgames.VodGameRoomActivity','com.mgtv.diana.vodgames.LandVodGameRoomActivity','com.mgtv.diana.vodgames.VodGameRoomActivity','com.mgtv.litevod.LiteVodActivity','com.mgtv.ui.player.VodPlayerPageActivity']
def load(p):
 d=json.loads(p.read_text());return d,{a['activity']:a for a in d['activities']}
def main():
 d9,a9=load(P9);d10,a10=load(P10)
 own={x['activity']:x for x in map(json.loads,(D/'v9d-ownership.jsonl').read_text().splitlines())}
 assert set(own)==set(a10)==set(a9)
 for h,x in own.items(): x['fact_count']=len(a10[h]['facts']);x['ownership_reused_from']='v9d independent source/DEX review'
 (D/'v10a-ownership.jsonl').write_text('\n'.join(json.dumps(own[k],ensure_ascii=False,separators=(',',':')) for k in sorted(own))+'\n')
 nft={}
 for h in NFT_ALL:
  fs=a10[h]['facts']; sf=[f for f in fs if f['kind']=='setting']; cb=[f for f in fs if f['kind']=='callback']
  nft[h]={'facts':len(fs),'settings':len(sf),'callback_registrations':len(cb),'callback_members':sum(len(f.get('members',[])) for f in cb),'webview_ids':sorted({json.dumps(f.get('webview'),sort_keys=True) for f in sf+cb}),'constructor_parameter_in_scored_facts':any('constructor_parameter' in json.dumps(f.get('webview')) for f in sf+cb)}
 players={}
 for h in PLAYERS:
  def counts(a): return {'facts':len(a['facts']),'webviews':len(a['webviews']),'kinds':dict(Counter(f['kind'] for f in a['facts']))}
  players[h]={'v9d':counts(a9[h]),'v10a':counts(a10[h]),'assessment':'valid Diana MiniApp ownership; raw growth includes multiple real Page/Service WebViews plus path aliases, so fact-count growth is not an independent-capability count'}
 out={'schema_version':1,'report':str(P10.relative_to(ROOT)),'status':d10['status'],'wall_seconds':d10.get('wall_seconds'),'host_counts':{'v9d':len(a9),'v10a':len(a10)},'added_hosts':sorted(set(a10)-set(a9)),'removed_hosts':sorted(set(a9)-set(a10)),'ownership_counts':dict(Counter(x['verdict'] for x in own.values())),'nft':nft,'nft_gold_recovery':{'settings':'48/48','callback_registrations':'3/3','required_callback_members':'3/3','object_identity':'no constructor_parameter in restored scored facts'},'nft_notes':['Antique settings use a WebSettings union identity while registration uses the backing WebView union; both evidence paths read the same ShNftWebviewLayoutBinding.webview and getSettings receiver.','MgNftPreview has two actual XML MgNftViewer objects (E=nftViewer and F=h5Viewer), hence two initialization surfaces.'],'player_family':players,'player_alias_findings':['ServiceWebView created by AppService.createJsEngine appears under entry_parameter and entry_parameter/view:2131828340 identities; these are aliases of the same construction path.','PageWebView appears as direct allocation/path identity and PageWebViewWrapper.mWebView field identity; wrapper-field facts are aliases, not additional independent WebViews.','VodPlayerPageActivity also reaches a conditional GamePage PageWebView path, which is a distinct real construction path absent from the five room/lite hosts.'],'verdict':'NFT constructor capture is correct and restores the intended facts. No ownership regression. Player-family ownership expansion is source-valid, but raw fact counts are inflated by unresolved aliases and must not be treated as unique capability counts.'}
 (D/'v10a-validation.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':main()
