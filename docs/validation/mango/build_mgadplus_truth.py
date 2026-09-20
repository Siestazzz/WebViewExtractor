#!/usr/bin/env python3
import json,re
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';A='com.mgadplus.brower.CustomWebActivity';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/mgadplus/brower/ImgoAdWebView.java:456-650 -> CustomWebActivity.java:176-185'
owners={'Lcom/mgadplus/brower/jsbridge/BridgeWebView$a;','Lcom/mgadplus/brower/c;','Lcom/mgadplus/brower/b;','Lcom/mgadplus/brower/CustomWebActivity$c;'};d={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==len(owners):break
assert d.keys()==owners
def row(k,n,v,i,s):return dict(activity=A,webview='com.mgadplus.brower.ImgoAdWebView',kind=k,name=n,value=v,implementation=i,signature=s,normalized_signature=s,normalized_api=s if k=='setting' else n,bridge_method=s if k=='message_handler' else '',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='BridgeWebView/ImgoAdWebView')
S=[('setDefaultTextEncodingName','UTF-8','Ljava/lang/String;'),('setSupportZoom','true','Z'),('setBuiltInZoomControls','true','Z'),('setDisplayZoomControls','false','Z'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setLayoutAlgorithm','NARROW_COLUMNS','Landroid/webkit/WebSettings$LayoutAlgorithm;'),('setSupportMultipleWindows','true','Z'),('setAllowContentAccess','true','Z'),('setDomStorageEnabled','true','Z'),('setSavePassword','false','Z'),('setPluginState','ON','Landroid/webkit/WebSettings$PluginState;'),('setAllowFileAccess','true','Z'),('setMediaPlaybackRequiresUserGesture','false','Z'),('setCacheMode','-1','I'),('setLoadsImagesAutomatically','true','Z'),('setMixedContentMode','0','I'),('setUserAgentString','runtime ad UA','Ljava/lang/String;'),('setLoadsImagesAutomatically','true','Z'),('setSavePassword','false','Z')];out=[]
for n,v,t in S:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'com.mgadplus.brower.ImgoAdWebView',s))
for owner in sorted(owners):
 impl=owner[1:-1].replace('/','.')
 for m in d[owner]['methods']:
  if m['name']=='<init>' or owner.endswith('/b;') and not (m['name'].startswith('on') or m['name'].startswith('getVideo')) or owner.endswith('BridgeWebView$a;') and not m['name'].startswith('on'):continue
  s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],impl,impl,s))
# The message registry uses concrete anonymous handler objects; emit their exact handler descriptors.
src=(R/'test/decompiled/com.hunantv.imgo.activity/sources/com/mgadplus/brower/ImgoAdWebView.java').read_text()
for name,cls in re.findall(r't\("([^"]+)", new ([a-z]\d?)\(\)\)',src):
 owner=f'Lcom/mgadplus/brower/ImgoAdWebView${cls};'; sig=f'{owner}->handler(Ljava/lang/String;Lcom/mgadplus/brower/jsbridge/d;)V'
 x=row('message_handler',name,'registered',owner[1:-1].replace('/','.'),sig);x.update(registration_name=name,namespace='');out.append(x)
# MgtvMethodChannel.L resolves to getUserInfo in the same APK.
sig='Lcom/mgadplus/brower/ImgoAdWebView$h;->handler(Ljava/lang/String;Lcom/mgadplus/brower/jsbridge/d;)V';x=row('message_handler','getUserInfo','registered','com.mgadplus.brower.ImgoAdWebView$h',sig);x.update(registration_name='getUserInfo',namespace='',registration_expression='MgtvMethodChannel.L');out.append(x)
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
