#!/usr/bin/env python3
import json,re
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';A='com.mgtv.ui.live.mglive.webview.WebViewActivity';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/live/mglive/h5/jsbridge/BridgeWebView.java:136-152 -> webview/a.java:962-1001'
owners={'Lcom/mgtv/ui/live/mglive/webview/a$a;','Lcom/mgtv/ui/live/mglive/webview/a$t;'};d={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==2:break
assert d.keys()==owners
def row(k,n,v,i,s):return dict(activity=A,webview='com.mgtv.ui.live.mglive.h5.jsbridge.BridgeWebView',kind=k,name=n,value=v,implementation=i,signature=s,normalized_signature=s,normalized_api=s if k=='setting' else n,bridge_method=s if k=='message_handler' else '',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='mglive BridgeWebView/webview.a')
S=[('setTextZoom','100','I'),('setJavaScriptEnabled','true','Z'),('setSavePassword','false','Z'),('setUserAgentString','runtime mglive UA suffix','Ljava/lang/String;'),('setCacheMode','2','I'),('setDefaultTextEncodingName','utf-8','Ljava/lang/String;'),('setSavePassword','false','Z'),('setMixedContentMode','0','I')];out=[]
for n,v,t in S:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'BridgeWebView + webview.a#o',s))
for owner in sorted(owners):
 impl=owner[1:-1].replace('/','.')
 for m in d[owner]['methods']:
  if m['name']!='<init>':
   s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],impl,impl,s))
src=(R/'test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/live/mglive/webview/a.java').read_text()
for name,cls in re.findall(r'bridgeWebView\.n\("([^"]+)", new ([a-z])\(',src):
 sig=f'Lcom/mgtv/ui/live/mglive/webview/a${cls};->a(Ljava/lang/String;Lcn0/d;)V';x=row('message_handler',name,'registered',f'com.mgtv.ui.live.mglive.webview.a${cls}',sig);x.update(registration_name=name,namespace='');out.append(x)
for name,cls,expr in [('getUserInfo','v','MgtvMethodChannel.L'),('copy','d','zd.a.f160787v'),('checkUpdate','j','DianaEventDefine.SDK2HostProcessEvent.CHECK_UPDATE')]:
 sig=f'Lcom/mgtv/ui/live/mglive/webview/a${cls};->a(Ljava/lang/String;Lcn0/d;)V';x=row('message_handler',name,'registered',f'com.mgtv.ui.live.mglive.webview.a${cls}',sig);x.update(registration_name=name,namespace='',registration_expression=expr);out.append(x)
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
