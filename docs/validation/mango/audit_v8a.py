#!/usr/bin/env python3
import collections,json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2]
v7=json.load(open(R/'test/runs/v7a/com.hunantv.imgo.activity/0/output/capabilities.json'))
v8=json.load(open(R/'test/runs/v8a/com.hunantv.imgo.activity/0/output/capabilities.json'))
s7={x['activity'] for x in v7['activities']}; cur={x['activity']:x for x in v8['activities']}
added=sorted(set(cur)-s7); removed=sorted(s7-set(cur))
assert added==['com.mgsz.detail.ui.AntiqueDetailActivity','com.mgtv.digital.ui.DigitalDetailActivity','com.mgtv.digital.ui.DigitalModelViewActivity'] and not removed
prior={x['activity']:x for x in map(json.loads,(H/'v6-ownership.jsonl').open())}
v4d={x['activity']:x for x in map(json.loads,(H/'v4d-ownership.jsonl').open())}
out=[]
for a,item in cur.items():
 row=dict(prior.get(a) or v4d[a]);row['fact_count']=len(item['facts'])
 row['reason']='stable source/DEX chain reused from independent v6/v4d review: '+row['reason'];out.append(row)
out.sort(key=lambda x:x['activity']);counts=collections.Counter(x['verdict'] for x in out)
assert counts=={'valid':80,'uncertain':5}
(H/'v8a-ownership.jsonl').write_text(''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in out))

host=cur['com.mgsz.h5.WebContainerActivity']; names={'setGeolocationEnabled','setDatabaseEnabled','setGeolocationDatabasePath','setDatabasePath'}
assert not [x for x in host['facts'] if x.get('name') in names]
assert not [x for x in host['facts'] if 'registerHandler' in str(x) or 'registerWebHandler' in str(x)]
summary={'activity_count':85,'ownership':{'counts':dict(counts),'added':added,'removed':removed},
 'webcontainer_setting_regression':{'missing':sorted(names),'reachable':True,
  'xml_constructor':'Lcom/mgsz/h5/ImgoWebView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V',
  'call_chain':['Lcom/mgsz/h5/ImgoWebView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V -> super',
   'Lcom/mgsz/h5/browser/RootWebView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V',
   'Lcom/mgsz/h5/browser/RootWebView;->init(Landroid/content/Context;)V',
   'Lcom/mgsz/h5/browser/RootWebView;->getSettings()Landroid/webkit/WebSettings;',
   'Lcom/mgsz/h5/browser/RootWebView;->supportHtml5(Landroid/webkit/WebSettings;)V'],
  'first_lost_edge':'the WebSettings value returned by RootWebView.getSettings() in RootWebView.init is passed as parameter 0 to supportHtml5; v8a no longer follows that parameterized helper after removing context-free WebView/Settings helper entry seeds',
  'calls':[{'signature':'Landroid/webkit/WebSettings;->setGeolocationEnabled(Z)V','value':True,'site_offset':1},
   {'signature':'Landroid/webkit/WebSettings;->setDatabaseEnabled(Z)V','value':True,'site_offset':4},
   {'signature':'Landroid/webkit/WebSettings;->setGeolocationDatabasePath(Ljava/lang/String;)V','value':'getContext().getDir("geolocation",0).getPath()','site_offset':52},
   {'signature':'Landroid/webkit/WebSettings;->setDatabasePath(Ljava/lang/String;)V','value':'getContext().getDir("databases",0).getPath()','site_offset':55}],
  'bridge_guard':'No Imgo registerHandler fact is emitted for WebContainer; K0 remains null on the XML path. Restoring the context-bound supportHtml5 edge must not seed the separate interface/programmatic registration route.'}}
(H/'v8a-validation.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(summary,ensure_ascii=False))
