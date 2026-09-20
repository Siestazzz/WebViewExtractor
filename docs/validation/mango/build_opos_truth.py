#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
hosts=['com.opos.cmn.biz.web.activity.apiimpl.AdWebActivity','com.opos.mobad.ui.feedback.FeedBackWebViewActivity','com.opos.cmn.module.ui.WebViewActivity'];W='com.hunantv.imgo.h5.browser.MoWebView'
owners={'Lcom/opos/cmn/biz/web/b/b/b/b$c;','Lcom/opos/cmn/biz/web/b/b/b/b$d;'};d={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==2:break
assert d.keys()==owners
settings=[('setJavaScriptEnabled','true','Z',404),('setDefaultZoom','MEDIUM','Landroid/webkit/WebSettings$ZoomDensity;',405),('setRenderPriority','HIGH','Landroid/webkit/WebSettings$RenderPriority;',406),('setDomStorageEnabled','true','Z',407),('setDatabaseEnabled','true','Z',408),('setDatabasePath','application database path','Ljava/lang/String;',409),('setCacheMode','-1','I',410),('setLayoutAlgorithm','NARROW_COLUMNS','Landroid/webkit/WebSettings$LayoutAlgorithm;',411),('setUseWideViewPort','true','Z',412),('setSavePassword','false','Z',413),('setAllowFileAccess','false','Z',414),('setAllowFileAccessFromFileURLs','false','Z',415),('setAllowUniversalAccessFromFileURLs','false','Z',416),('setMixedContentMode','0','I',418),('setAllowContentAccess','false','Z',419)]
def row(a,k,n,v,impl,sig,norm):return dict(activity=a,webview=W,kind=k,name=n,value=v,implementation=impl,signature=sig,normalized_signature=norm,normalized_api=norm if k=='setting' else n,bridge_method='',evidence='test/decompiled/com.hunantv.imgo.activity/sources/com/opos/cmn/biz/web/b/b/b/b.java:316-427',binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='opos-web-widget')
out=[]
for a in hosts:
 for n,v,t,_ in settings:
  q=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row(a,'setting',n,v,'com.opos.cmn.biz.web.b.b.b.b#m',q,q))
 for owner in sorted(owners):
  impl=owner[1:-1].replace('/','.')
  for m in d[owner]['methods']:
   if m['name']=='<init>':continue
   q=m['signature'].replace('-\u003e','->');out.append(row(a,'callback',m['name'],impl,impl,q,q))
 if a.endswith('AdWebActivity'):
  x=row(a,'bridge','<dynamic-jsInterfaceMap>','registered',None,'','');x.update(binding_status='registered-target-unknown',symbol_status='not_applicable',registration_name=None,registration_expression='WebViewInitParams.jsInterfaceMap');out.append(x)
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity') not in hosts or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
