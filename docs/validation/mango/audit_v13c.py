#!/usr/bin/env python3
import json,collections,hashlib
from pathlib import Path
D=Path(__file__).parent; R=D.parents[2]/'test/runs'
def load(v): return json.load(open(R/v/'com.hunantv.imgo.activity/0/output/capabilities.json'))
def key(f):
 return tuple(json.dumps(f.get(k),sort_keys=True,ensure_ascii=False) for k in ('kind','name','site','api','webview','registration_name','implementation','members','arguments'))
def counts(a): return collections.Counter(f.get('kind','unknown') for f in a['facts'])
i,j=load('v12j'),load('v13c'); ai={a['activity']:a for a in i['activities']}; aj={a['activity']:a for a in j['activities']}
delta={}
for h in sorted(set(ai)|set(aj)):
 old=ai.get(h,{'facts':[]}); new=aj.get(h,{'facts':[]}); ko=collections.Counter(map(key,old['facts']));kn=collections.Counter(map(key,new['facts']))
 if ko!=kn: delta[h]={'v12j':len(old['facts']),'v13c':len(new['facts']),'net':len(new['facts'])-len(old['facts']),'v13c_kinds':dict(counts(old)),'v13c_kinds':dict(counts(new)),'added_semantic_facts':sum((kn-ko).values()),'removed_semantic_facts':sum((ko-kn).values())}
out={'schema_version':1,'apk_sha256':j['apk_sha256'],'reports':{'v13c_sha256':hashlib.sha256((R/'v13c/com.hunantv.imgo.activity/0/output/capabilities.json').read_bytes()).hexdigest(),'v12j_sha256':hashlib.sha256((R/'v12j/com.hunantv.imgo.activity/0/output/capabilities.json').read_bytes()).hexdigest()},'status':j['status'],'wall_seconds':j['wall_seconds'],'hosts':{'v12j':len(ai),'v13c':len(aj),'added':sorted(set(aj)-set(ai)),'removed':sorted(set(ai)-set(aj))},'changed_hosts':delta}
(D/'v13c-delta.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
# ownership
prior={x['activity']:x for x in map(json.loads,open(D/'v12j-ownership.jsonl'))}
rows=[]
for h,a in sorted(aj.items()):
 if h in prior:
  x=dict(prior[h]);x['fact_count']=len(a['facts']);x['ownership_reused_from']='v12j independent source/DEX ownership review'
 else:x={'activity':h,'verdict':'valid_conditional','fact_count':len(a['facts']),'reason':'Activity constructs VideoSquareFragment; its uc() attaches VideoSquareHeaderFragment, whose inherited channel header/content path conditionally reaches the audited WebViewFragment/ImgoWebView carrier. Capability ownership depends on selected channel content.','source_evidence':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/videosquare/VideoSquareActivity.java:42-53','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/videosquare/fragment/VideoSquareFragment.java:834-835','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/videosquare/fragment/VideoSquareHeaderFragment.java:32,155-156'],'apk_sha256':j['apk_sha256']}
 rows.append(x)
(D/'v13c-ownership.jsonl').write_text(''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in rows))
print(json.dumps({'changed':len(delta),'added':out['hosts']['added'],'removed':out['hosts']['removed']},indent=2))
