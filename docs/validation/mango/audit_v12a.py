#!/usr/bin/env python3
import json
from pathlib import Path
from collections import Counter
D=Path(__file__).resolve().parent;ROOT=D.parents[2]
def load(v):
 d=json.load(open(ROOT/f'test/runs/{v}/com.hunantv.imgo.activity/0/output/capabilities.json'));return d,{a['activity']:a for a in d['activities']}
def main():
 d0,a0=load('v11c');d1,a1=load('v12a');assert set(a0)==set(a1)
 own={x['activity']:x for x in map(json.loads,(D/'v11b-ownership.jsonl').read_text().splitlines())}
 for h,x in own.items():x['fact_count']=len(a1[h]['facts']);x['ownership_reused_from']='v11b/v10a independent source/DEX review'
 (D/'v12a-ownership.jsonl').write_text('\n'.join(json.dumps(own[k],ensure_ascii=False,separators=(',',':')) for k in sorted(own))+'\n')
 changed=[]
 for h in sorted(a0):
  if len(a0[h]['facts'])!=len(a1[h]['facts']):changed.append({'activity':h,'v11c':len(a0[h]['facts']),'v12a':len(a1[h]['facts']),'delta':len(a1[h]['facts'])-len(a0[h]['facts']),'removed_by_kind':dict(Counter(f['kind'] for f in a0[h]['facts'])-Counter(f['kind'] for f in a1[h]['facts']))})
 nft=['com.mgsz.detail.ui.AntiqueDetailActivity','com.mgtv.digital.ui.DigitalDetailActivity','com.mgtv.digital.ui.DigitalModelViewActivity','com.mgtv.digital.ui.DigitalBelongActivity','com.mgtv.digital.ui.MgNftPreviewActivity']
 players=['com.imgo.vodgames.LandVodGameRoomActivity','com.imgo.vodgames.VodGameRoomActivity','com.mgtv.diana.vodgames.LandVodGameRoomActivity','com.mgtv.diana.vodgames.VodGameRoomActivity','com.mgtv.litevod.LiteVodActivity','com.mgtv.ui.player.VodPlayerPageActivity']
 out={'schema_version':1,'report':'test/runs/v12a/com.hunantv.imgo.activity/0/output/capabilities.json','status':d1['status'],'wall_seconds':d1.get('wall_seconds'),'hosts':89,'added_hosts':[],'removed_hosts':[],'ownership_counts':dict(Counter(x['verdict'] for x in own.values())),'total_facts':{'v11c':sum(len(x['facts']) for x in a0.values()),'v12a':sum(len(x['facts']) for x in a1.values())},'changed_hosts':changed,'stable_families':{'nft':{h:[len(a0[h]['facts']),len(a1[h]['facts'])] for h in nft},'reviewed_player_family':{h:[len(a0[h]['facts']),len(a1[h]['facts'])] for h in players}},'erlang_assessment':{'removed':20,'verdict':'correct alias pruning','evidence':'ErlangSingClashH5Fragment does not set INTENT_WEB_USE_NESTED; WebUIFragment defaults it false and constructs ImgoWebView, so the removed NestedImgoWebView alternative was infeasible. The same 20 registrations remain on its actual ImgoWebView.'},'mg_video_assessment':{'removed':64,'removed_by_kind':{'setting':35,'webview_operation':8,'callback':8,'bridge_removal':10,'bridge':3},'verdict':'real attribution regression','valid_chain':['MGVideoPlayActivity event branch -> y9().showMiniApp(String)','NBFloatFragmentHelper.showMiniApp aspect closure -> F1(helper,String,joinPoint)','MiniAppFragment.newInstance(Bundle)','helper.n1(Fragment)','n1 writes this.f88165h=fragment','helper.O0 reads same f88165h and invokes MiniAppFragment.onBackPressed','MiniAppFragment.onViewCreated -> DianaView -> GamePage/ServiceWebView'],'earliest_lost_edge':'actual showMiniApp/F1 -> n1(field setter) write is no longer joined to the same helper instance read by O0; synthetic AspectJ closure is the likely boundary.','object_notes':['Removed GamePage PageWebView is a real conditional object.','Removed ServiceWebView identity still ended in constructor_parameter in v11c, but the underlying ServiceWebView is real; provenance weakness does not justify deleting the entire capability surface.']},'verdict':'Mixed: instance propagation correctly prunes one impossible Erlang nested-WebView alias, but regresses the valid MGVideoPlayActivity MiniApp/Diana capability surface. NFT and previously reviewed six-player family are stable.'}
 (D/'v12a-validation.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':main()
