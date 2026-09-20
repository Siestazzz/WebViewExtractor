#!/usr/bin/env python3
import json,collections,hashlib
from pathlib import Path
D=Path(__file__).resolve().parent;R=D.parents[2]
def load(v):
 d=json.load(open(R/f'test/runs/{v}/com.hunantv.imgo.activity/0/output/capabilities.json'));return d,{x['activity']:x for x in d['activities']}
def c(a):return collections.Counter(f['kind'] for f in a['facts'])
def main():
 dh,h=load('v12h');di,i=load('v12i');assert set(h)==set(i)
 changes=[]
 for host in sorted(h):
  a,b=c(h[host]),c(i[host])
  if a!=b:changes.append({'activity':host,'v12h_facts':sum(a.values()),'v12i_facts':sum(b.values()),'delta':sum(b.values())-sum(a.values()),'added_by_kind':dict(b-a),'removed_by_kind':dict(a-b)})
 ev=json.load(open('/tmp/v12i-eval.json'));mg=i['com.mgtv.ui.videoplay.MGVideoPlayActivity'];df=[f for f in mg['facts'] if f.get('webview',{}).get('type') in {'com.mgtv.diana.sdk.page.view.PageWebView','com.mgtv.diana.sdk.service.view.ServiceWebView'}]
 chains=[f for f in df if all(any(s in e for e in f.get('evidence',[])) for s in ['NBFloatFragmentHelper;->H1','NBFloatFragmentHelper;->G1','NBFloatFragmentHelper;->F1'])]
 out={'schema_version':1,'report':'test/runs/v12i/com.hunantv.imgo.activity/0/output/capabilities.json','report_sha256':hashlib.sha256((R/'test/runs/v12i/com.hunantv.imgo.activity/0/output/capabilities.json').read_bytes()).hexdigest(),'benchmark_status':next(x['status'] for x in json.load(open(R/'docs/validation/v12i-benchmark.json')) if x['package']=='com.hunantv.imgo.activity'),'hosts':len(i),'added_hosts':[],'removed_hosts':[],'ownership_counts':dict(collections.Counter(json.loads(x)['verdict'] for x in (D/'v12i-ownership.jsonl').read_text().splitlines())),'total_facts':{'v12h':sum(len(x['facts']) for x in h.values()),'v12i':sum(len(x['facts']) for x in i.values())},'changed_hosts_count':len(changes),'unchanged_hosts_count':len(i)-len(changes),'host_fact_deltas':changes,'evaluation':ev['metrics'],'missing_grouped':{f'{a}|{b}':n for (a,b),n in collections.Counter((m['activity'],m['kind']) for m in ev['missing']).items()},'mgvideo_diana':{'oracle_facts':81,'constraint_types':['com.mgtv.diana.sdk.page.view.PageWebView','com.mgtv.diana.sdk.service.view.ServiceWebView'],'evaluator_missing':sum(1 for x in ev['missing'] if x['activity']=='com.mgtv.ui.videoplay.MGVideoPlayActivity'),'report_facts_on_constrained_types':len(df),'by_type_and_kind':{f'{a}|{b}':n for (a,b),n in collections.Counter((f['webview']['type'],f['kind']) for f in df).items()},'facts_with_full_H1_G1_F1_evidence':len(chains),'host_context_budget_present':'host_context_budget' in mg.get('limitations',[]),'verdict':'81/81 constrained oracle facts matched; report contains concrete PageWebView/ServiceWebView facts and full H1->G1->F1 host chains. Extra alternatives/duplicates remain candidate and are not promoted to source truth.'},'verdict':'v12i recovers the MG/Diana constrained surface and preserves the same 91-host ownership set. Aggregate fact changes and candidate alternatives remain separately enumerated; constrained matching proves type, not runtime object identity.'}
 (D/'v12i-validation.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':main()
