#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';A='com.imgo.vipcardiac.activity.LandWebActivity';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/imgo/webbase/jsbridge/BridgeWebView.java:140-259 -> hybird/BaseWebView.java:84-135 -> com/imgo/vipcardiac/activity/LandWebActivity.java:134-230'
owners={'Ltb/b;','Lcom/imgo/vipcardiac/activity/LandWebActivity$a;'};d={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==2:break
assert d.keys()==owners
def row(k,n,v,i,s):return dict(activity=A,webview='com.imgo.webbase.hybird.BaseWebView',kind=k,name=n,value=v,implementation=i,signature=s,normalized_signature=s,normalized_api=s if k=='setting' else n,bridge_method=s if k in {'bridge_method','message_handler'} else '',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='BridgeWebView/BaseWebView/LandWebActivity')
S=[('setSavePassword','false','Z'),('setJavaScriptEnabled','true','Z'),('setUserAgentString','runtime webbase UA','Ljava/lang/String;'),('setSaveFormData','true','Z'),('setLightTouchEnabled','false','Z'),('setNeedInitialFocus','false','Z'),('setDefaultTextEncodingName','UTF-8','Ljava/lang/String;'),('setSupportZoom','true','Z'),('setBuiltInZoomControls','true','Z'),('setDisplayZoomControls','false','Z'),('setAllowContentAccess','true','Z'),('setTextZoom','100','I'),('setSavePassword','false','Z'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setSupportMultipleWindows','true','Z'),('setPluginState','ON','Landroid/webkit/WebSettings$PluginState;'),('setAllowFileAccess','false','Z'),('setMediaPlaybackRequiresUserGesture','false','Z'),('setCacheMode','-1','I'),('setLoadsImagesAutomatically','true','Z'),('setMixedContentMode','0','I'),('setSupportZoom','true','Z'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setSupportMultipleWindows','false','Z'),('setPluginState','ON_DEMAND','Landroid/webkit/WebSettings$PluginState;'),('setGeolocationEnabled','true','Z'),('setGeolocationDatabasePath','runtime geolocation path','Ljava/lang/String;'),('setDomStorageEnabled','true','Z'),('setDatabaseEnabled','true','Z'),('setDatabasePath','runtime database path','Ljava/lang/String;'),('setAppCacheEnabled','true','Z'),('setAppCachePath','runtime appcache path','Ljava/lang/String;')];out=[]
for n,v,t in S:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'BridgeWebView/BaseWebView initialization',s))
override={'onRenderProcessGone','shouldOverrideUrlLoading'}
for m in d['Ltb/b;']['methods']:
 if m['name'].startswith(('on','should')) and m['name'] not in override:
  s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],'tb.b','tb.b',s))
for m in d['Lcom/imgo/vipcardiac/activity/LandWebActivity$a;']['methods']:
 if m['name']!='<init>':
  s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],'LandWebActivity$a','com.imgo.vipcardiac.activity.LandWebActivity$a',s))
s='Lcom/imgo/webbase/jsbridge/BridgeWebView$c;->callNative(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;';x=row('bridge_method','callNative','exposed','com.imgo.webbase.jsbridge.BridgeWebView$c',s);x.update(registration_name='jsobj',namespace='jsobj');out.append(x)
for name,num in [('getDeviceInfo',2),('getUserInfo',3),('login',4),('jumpPage',5),('closeWebView',6)]:
 s=f'Lcom/imgo/vipcardiac/activity/LandWebActivity${num};->handler(Ljava/lang/String;Lcom/imgo/webbase/jsbridge/d;)V';x=row('message_handler',name,'registered',f'com.imgo.vipcardiac.activity.LandWebActivity${num}',s);x.update(registration_name=name,namespace='');out.append(x)
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
