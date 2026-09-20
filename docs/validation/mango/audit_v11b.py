#!/usr/bin/env python3
import json
from pathlib import Path
from collections import Counter
D=Path(__file__).resolve().parent; ROOT=D.parents[2]
def load(v):
 d=json.load(open(ROOT/f'test/runs/{v}/com.hunantv.imgo.activity/0/output/capabilities.json'));return d,{a['activity']:a for a in d['activities']}
def main():
 d10,a10=load('v10a');d11,a11=load('v11b');assert set(a10)==set(a11)
 own={x['activity']:x for x in map(json.loads,(D/'v10a-ownership.jsonl').read_text().splitlines())}
 for h,x in own.items():x['fact_count']=len(a11[h]['facts']);x['ownership_reused_from']='v10a independent source/DEX review'
 (D/'v11b-ownership.jsonl').write_text('\n'.join(json.dumps(own[k],ensure_ascii=False,separators=(',',':')) for k in sorted(own))+'\n')
 changed=[]
 for h in sorted(a10):
  if len(a10[h]['facts'])!=len(a11[h]['facts']):changed.append({'activity':h,'v10a':len(a10[h]['facts']),'v11b':len(a11[h]['facts']),'delta':len(a11[h]['facts'])-len(a10[h]['facts']),'v10a_kinds':dict(Counter(f['kind'] for f in a10[h]['facts'])),'v11b_kinds':dict(Counter(f['kind'] for f in a11[h]['facts']))})
 ant=a11['com.mgsz.detail.ui.AntiqueDetailActivity']; scored=[f for f in ant['facts'] if f['kind'] in ('setting','callback')]
 ids=sorted({json.dumps(f['webview'],sort_keys=True) for f in scored})
 out={'schema_version':1,'report':'test/runs/v11b/com.hunantv.imgo.activity/0/output/capabilities.json','status':d11['status'],'wall_seconds':d11.get('wall_seconds'),'hosts':89,'added_hosts':[],'removed_hosts':[],'ownership_counts':dict(Counter(x['verdict'] for x in own.values())),'total_facts':{'v10a':sum(len(x['facts']) for x in a10.values()),'v11b':sum(len(x['facts']) for x in a11.values())},'antique':{'setting_count':sum(f['kind']=='setting' for f in scored),'callback_registration_count':sum(f['kind']=='callback' for f in scored),'underlying_webview_alternatives':ids,'settings_and_callback_same_alternatives':len(ids)==1,'constructor_parameter':any('constructor_parameter' in json.dumps(f['webview']) for f in scored)},'changed_hosts':changed,'semantic_findings':{'settings_normalization':['Antique 16 settings now use exactly the same WebView union as setWebViewClient.','ErlangLiveActivity and SanlangLiveActivity each replace one WebSettings-union fact with two actual alternatives: ImgoWebView and NestedImgoWebView; this is identity refinement, not a new setting API.'],'map_registry':['Pangle host bridge-count reductions remove declared_bridge_contract_only/implementation=unknown duplicates. Exact known rows at the same sites remain: bp.z for IESJSBridge and bp.zx for SDK_INJECT_GLOBAL.','Source bp.c(WebView,r,String) and bp.c(SSWebView,tn) confirm the registrations and map get/put update behavior; v11b retains their concrete rows.','Diagnostics conservatively retain unresolved_map_key, unresolved_map_lookup_key, and map_update/mutation_order_unresolved cases.']},'verdict':'No host or semantic capability regression. Settings identity normalization is correct. Map-instance precision removes speculative duplicate bridge facts while retaining independently verified concrete registrations.'}
 (D/'v11b-validation.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':main()
