#!/usr/bin/env python3
import json,collections,hashlib
from pathlib import Path
D=Path(__file__).resolve().parent; ROOT=D.parents[2]
def load(v):
 d=json.load(open(ROOT/f'test/runs/{v}/com.hunantv.imgo.activity/0/output/capabilities.json'));return d,{a['activity']:a for a in d['activities']}
def kinds(a):return collections.Counter(x['kind'] for x in a['facts'])
def main():
 d0,a0=load('v12a'); d1,a1=load('v12h')
 old={x['activity']:x for x in map(json.loads,(D/'v12a-ownership.jsonl').read_text().splitlines())}
 additions={
 'com.mgtv.ui.channel.selected.ChannelDynamicSecondActivity':{'verdict':'valid','reason':'source-confirmed conditional fragment chain: Q2 constructs ChannelDynamicSecondFragment; its ChannelRefreshLayout can invoke ChannelSecondFloorHeader.showWebViewFragment and the verified AspectJ closure reaches WebViewFragment/ImgoWebView','source_evidence':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/channel/selected/ChannelDynamicSecondActivity.java:78','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/widget/refresh/ChannelSecondFloorHeader.java (showWebViewFragment closure)','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/browser/WebViewFragment.java (ImgoWebView field K)']},
 'com.mgtv.ui.cornucopia.CornucopiaActivity':{'verdict':'valid','reason':'source-confirmed conditional fragment chain: X2 assigns CornucopiaMainPagerFragment; pager creates CornucopiaMainFragment and CornucopiaBaseFragment can attach WebViewFragment/ImgoWebView through its verified AspectJ closure','source_evidence':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/cornucopia/CornucopiaActivity.java:192','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/cornucopia/feed/fragment/CornucopiaMainPagerFragment.java','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/cornucopia/CornucopiaBaseFragment.java','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/browser/WebViewFragment.java (ImgoWebView field K)']}}
 own={}
 for h in sorted(a1):
  if h in old: x=dict(old[h]);x['fact_count']=len(a1[h]['facts']);x['ownership_reused_from']='v12a independent source/DEX ownership review'
  else:x={'activity':h,**additions[h],'fact_count':len(a1[h]['facts']),'apk_sha256':d1['apk_sha256'],'binding_status':'candidate','ownership_review':'source-confirmed conditional reachability'}
  own[h]=x
 (D/'v12h-ownership.jsonl').write_text(''.join(json.dumps(own[h],ensure_ascii=False,separators=(',',':'))+'\n' for h in sorted(own)))
 changes=[]
 for h in sorted(set(a0)|set(a1)):
  c0=kinds(a0[h]) if h in a0 else collections.Counter();c1=kinds(a1[h]) if h in a1 else collections.Counter()
  if c0!=c1: changes.append({'activity':h,'v12a_facts':sum(c0.values()),'v12h_facts':sum(c1.values()),'delta':sum(c1.values())-sum(c0.values()),'added_by_kind':dict(c1-c0),'removed_by_kind':dict(c0-c1)})
 ev=json.load(open('/tmp/v12h-eval.json'))
 mg=a1['com.mgtv.ui.videoplay.MGVideoPlayActivity']; mgd=[f for f in mg['facts'] if 'com/mgtv/diana' in json.dumps(f) or 'com.mgtv.diana' in json.dumps(f)]
 oracle=[json.loads(x) for x in (D/'mgvideo-diana-development-facts.jsonl').read_text().splitlines()]
 mgmiss=[m for m in ev['missing'] if m['activity']=='com.mgtv.ui.videoplay.MGVideoPlayActivity']
 out={'schema_version':1,'report':'test/runs/v12h/com.hunantv.imgo.activity/0/output/capabilities.json','report_sha256':hashlib.sha256((ROOT/'test/runs/v12h/com.hunantv.imgo.activity/0/output/capabilities.json').read_bytes()).hexdigest(),'benchmark_status':next(x['status'] for x in json.load(open(ROOT/'docs/validation/v12h-benchmark.json')) if x['package']=='com.hunantv.imgo.activity'),'report_status':d1['status'],'wall_seconds':d1.get('wall_seconds'),'hosts':len(a1),'added_hosts':sorted(set(a1)-set(a0)),'removed_hosts':sorted(set(a0)-set(a1)),'ownership_counts':dict(collections.Counter(x['verdict'] for x in own.values())),'total_facts':{'v12a':sum(len(x['facts']) for x in a0.values()),'v12h':sum(len(x['facts']) for x in a1.values())},'changed_hosts_count':len(changes),'unchanged_hosts_count':len(set(a0)&set(a1))-sum(1 for x in changes if x['activity'] in a0 and x['activity'] in a1),'host_fact_deltas':changes,'evaluation':ev['metrics'],'missing_grouped':{f'{h}|{k}':n for (h,k),n in collections.Counter((m['activity'],m['kind']) for m in ev['missing']).items()},'mgvideo_diana':{'oracle_facts':len(oracle),'oracle_by_kind':dict(collections.Counter(x['kind'] for x in oracle)),'report_total_facts':len(mg['facts']),'report_diana_evidence_facts':len(mgd),'scorer_missing':len(mgmiss),'scorer_apparent_matched':len(oracle)-len(mgmiss),'actual_source_chain_recovered':0,'limitations':[x for x in mg.get('limitations',[]) if x=='host_context_budget' or x.startswith('aspectj_')],'verdict':'failed: no report fact has a Diana owner/evidence chain. The scorer apparent 30 matches are semantic collisions with other MG WebViews because object identity is not scored.'},'global_verdict':'v12h discovers two valid conditional hosts and greatly expands AspectJ-reachable facts, but MG/Diana recovery fails completely at host_context_budget. The large 20582->46669 expansion is not accepted as globally source-verified; candidate facts retain candidate status.'}
 (D/'v12h-validation.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':main()
