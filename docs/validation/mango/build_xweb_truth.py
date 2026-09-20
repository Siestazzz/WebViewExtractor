#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';A='com.hunantv.imgo.xweb.XWebActivity';W='com.hunantv.imgo.xweb.web.XWebView';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/xweb/browser/RootWebView.java:87-130 -> jsbridge/BridgeWebView.java:163-180 -> web/XWebView.java:197-407'
owners={'Lcom/hunantv/imgo/xweb/web/XWebView$2;','Lcom/hunantv/imgo/xweb/web/XWebView$3;','Lcom/hunantv/imgo/xweb/browser/RootWebChromeClient;','Lcom/hunantv/imgo/xweb/jsbridge/JSInterface;'};dex={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:dex[o['type']]=o
 if len(dex)==len(owners):break
assert dex.keys()==owners
def row(k,n,v,impl,sig):return dict(activity=A,webview=W,kind=k,name=n,value=v,implementation=impl,signature=sig,normalized_signature=sig,normalized_api=sig if k=='setting' else n,bridge_method=sig if k=='bridge_method' else '',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='RootWebView/BridgeWebView/XWebView')
S=[('setGeolocationEnabled','true','Z'),('setDatabaseEnabled','true','Z'),('setDomStorageEnabled','true','Z'),('setAppCacheEnabled','true','Z'),('setGeolocationDatabasePath','runtime geolocation path','Ljava/lang/String;'),('setDatabasePath','runtime database path','Ljava/lang/String;'),('setAppCachePath','runtime appcache path','Ljava/lang/String;'),('setSaveFormData','true','Z'),('setLightTouchEnabled','false','Z'),('setNeedInitialFocus','false','Z'),('setJavaScriptEnabled','true','Z'),('setSupportZoom','true','Z'),('setLoadWithOverviewMode','true','Z'),('setUseWideViewPort','true','Z'),('setSupportMultipleWindows','false','Z'),('setPluginState','ON_DEMAND','Lcom/tencent/smtt/sdk/WebSettings$PluginState;'),('setUserAgentString','runtime default UA','Ljava/lang/String;'),('setJavaScriptEnabled','true','Z'),('setTextZoom','100','I'),('setSavePassword','false','Z'),('setDefaultTextEncodingName','UTF-8','Ljava/lang/String;'),('setSupportZoom','true','Z'),('setBuiltInZoomControls','true','Z'),('setDisplayZoomControls','false','Z'),('setLoadWithOverviewMode','true','Z'),('setGeolocationEnabled','true','Z'),('setUseWideViewPort','true','Z'),('setSupportMultipleWindows','true','Z'),('setJavaScriptEnabled','true','Z'),('setAppCacheEnabled','true','Z'),('setDomStorageEnabled','true','Z'),('setDatabaseEnabled','true','Z'),('setCacheMode','-1','I'),('setPluginState','ON','Lcom/tencent/smtt/sdk/WebSettings$PluginState;'),('setAllowFileAccess','true','Z'),('setMediaPlaybackRequiresUserGesture','false','Z'),('setLoadsImagesAutomatically','true','Z'),('setMixedContentMode','0','I'),('setUserAgentString','runtime application UA','Ljava/lang/String;'),('setLoadsImagesAutomatically','true','Z')]
out=[]
for n,v,t in S:
 s=f'Lcom/tencent/smtt/sdk/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'XWebView constructor chain',s))
root_names={'getDefaultVideoPoster','getVideoLoadingProgressView','onCloseWindow','onGeolocationPermissionsHidePrompt','onGeolocationPermissionsShowPrompt','onHideCustomView','onJsAlert','onJsBeforeUnload','onJsConfirm','onJsPrompt','onProgressChanged','onReceivedIcon','onShowCustomView','onShowFileChooser','openFileChooser'}
for owner,names in [('Lcom/hunantv/imgo/xweb/web/XWebView$2;',None),('Lcom/hunantv/imgo/xweb/web/XWebView$3;',None),('Lcom/hunantv/imgo/xweb/browser/RootWebChromeClient;',root_names)]:
 impl=owner[1:-1].replace('/','.')
 for m in dex[owner]['methods']:
  if m['name']=='<init>' or names is not None and m['name'] not in names:continue
  s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],impl,impl,s))
s='Lcom/hunantv/imgo/xweb/jsbridge/JSInterface;->callNative(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V';x=row('bridge_method','callNative','exposed','com.hunantv.imgo.xweb.jsbridge.JSInterface',s);x.update(registration_name='jsobj',namespace='jsobj');out.append(x)
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
