#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';A='com.ubix.ssp.open.comm.UBiXWebViewActivity';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/ubix/ssp/ad/e/c0/c.java:45-105 -> open/comm/UBiXWebViewActivity.java:103-109'
owners={'Lcom/ubix/ssp/ad/e/c0/b;','Lcom/ubix/ssp/ad/e/c0/d;'};d={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==2:break
assert d.keys()==owners
def row(k,n,v,i,s):return dict(activity=A,webview='com.ubix.ssp.ad.e.c0.c',kind=k,name=n,value=v,implementation=i,signature=s,normalized_signature=s,normalized_api=s if k=='setting' else n,bridge_method='',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='UBiXWebViewActivity/c')
S=[('setJavaScriptEnabled','true','Z'),('setDefaultTextEncodingName','utf-8','Ljava/lang/String;'),('setJavaScriptCanOpenWindowsAutomatically','true','Z'),('setGeolocationEnabled','true','Z'),('setAllowContentAccess','false','Z'),('setSavePassword','false','Z'),('setSaveFormData','true','Z'),('setCacheMode','-1','I'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setAllowFileAccessFromFileURLs','false','Z'),('setAllowUniversalAccessFromFileURLs','false','Z'),('setMediaPlaybackRequiresUserGesture','false','Z'),('setLoadsImagesAutomatically','true','Z'),('setBlockNetworkImage','false','Z'),('setBlockNetworkLoads','false','Z'),('setSupportZoom','true','Z'),('setBuiltInZoomControls','runtime SDK conditional boolean','Z'),('setDatabaseEnabled','true','Z'),('setDomStorageEnabled','true','Z'),('setSaveFormData','true','Z'),('setMixedContentMode','0','I'),('setLayoutAlgorithm','SINGLE_COLUMN','Landroid/webkit/WebSettings$LayoutAlgorithm;'),('setRenderPriority','HIGH','Landroid/webkit/WebSettings$RenderPriority;')];out=[]
for n,v,t in S:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'com.ubix.ssp.ad.e.c0.c#a',s))
for owner in sorted(owners):
 impl=owner[1:-1].replace('/','.')
 for m in d[owner]['methods']:
  if m['name']=='<init>' or not (m['name'].startswith('on') or m['name'].startswith('should')):continue
  s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],impl,impl,s))
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
