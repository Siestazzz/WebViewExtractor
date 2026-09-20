#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';A='com.mgsz.h5.WebContainerActivity';W='com.mgsz.h5.ImgoWebView';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/mgsz/h5/browser/RootWebView.java:37-81 -> jsbridge/BridgeWebView.java:163-172 -> ImgoWebView.java:379-950'
owners={'Lcom/mgsz/h5/ImgoWebView$2;','Lcom/mgsz/h5/ImgoWebView$3;','Lcom/mgsz/h5/browser/RootWebChromeClient;','Lcom/mgsz/h5/jsbridge/BridgeWebView$b;'};dex={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:dex[o['type']]=o
 if len(dex)==len(owners):break
assert dex.keys()==owners
def row(k,n,v,impl,sig):return dict(activity=A,webview=W,kind=k,name=n,value=v,implementation=impl,signature=sig,normalized_signature=sig,normalized_api=sig if k=='setting' else n,bridge_method=sig if k=='bridge_method' else '',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='RootWebView/BridgeWebView/ImgoWebView')
S=[('setGeolocationEnabled','true','Z'),('setDatabaseEnabled','true','Z'),('setDomStorageEnabled','true','Z'),('setGeolocationDatabasePath','runtime geolocation path','Ljava/lang/String;'),('setDatabasePath','runtime database path','Ljava/lang/String;'),('setSaveFormData','true','Z'),('setAllowFileAccess','false','Z'),('setLightTouchEnabled','false','Z'),('setNeedInitialFocus','false','Z'),('setSavePassword','false','Z'),('setJavaScriptEnabled','true','Z'),('setSupportZoom','true','Z'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setSupportMultipleWindows','false','Z'),('setPluginState','ON_DEMAND','Landroid/webkit/WebSettings$PluginState;'),('setUserAgentString','runtime default UA','Ljava/lang/String;'),('setAllowFileAccess','false','Z'),('setJavaScriptEnabled','true','Z'),('setTextZoom','100','I'),('setSavePassword','false','Z'),('setDefaultTextEncodingName','UTF-8','Ljava/lang/String;'),('setSupportZoom','true','Z'),('setBuiltInZoomControls','true','Z'),('setDisplayZoomControls','false','Z'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setSupportMultipleWindows','false','Z'),('setJavaScriptEnabled','true','Z'),('setDomStorageEnabled','true','Z'),('setPluginState','ON','Landroid/webkit/WebSettings$PluginState;'),('setAllowFileAccess','false','Z'),('setMediaPlaybackRequiresUserGesture','false','Z'),('setCacheMode','-1','I'),('setLoadsImagesAutomatically','true','Z'),('setMixedContentMode','0','I'),('setUserAgentString','runtime application UA','Ljava/lang/String;'),('setLoadsImagesAutomatically','true','Z')]
out=[]
for n,v,t in S:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'ImgoWebView constructor chain',s))
root={'onCloseWindow','onGeolocationPermissionsHidePrompt','onGeolocationPermissionsShowPrompt','onHideCustomView','onJsAlert','onJsBeforeUnload','onJsConfirm','onJsPrompt','onReceivedIcon','onShowCustomView','onShowFileChooser','openFileChooser'}
for owner,names in [('Lcom/mgsz/h5/ImgoWebView$2;',None),('Lcom/mgsz/h5/ImgoWebView$3;',None),('Lcom/mgsz/h5/browser/RootWebChromeClient;',root)]:
 impl=owner[1:-1].replace('/','.')
 for m in dex[owner]['methods']:
  if m['name']=='<init>' or names is not None and m['name'] not in names:continue
  s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],impl,impl,s))
s='Lcom/mgsz/h5/jsbridge/BridgeWebView$b;->callNative(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;';x=row('bridge_method','callNative','exposed','com.mgsz.h5.jsbridge.BridgeWebView$b',s);x.update(registration_name='jsobj',namespace='jsobj');out.append(x)
# XML construction leaves K0 null; registerWebHandler returns before all listed registrations.
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
