#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent; R=H.parents[2]; F=H/'facts.jsonl'; SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
A='com.platform.oms.ui.LoadingWebActivity'; W='com.platform.oms.ui.FragmentWebLoadingBase'; SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/platform/oms/ui/FragmentWebLoadingBase.java:37-205 -> LoadingWebActivity.java:124,287-341'
owners={'Lcom/platform/oms/ui/FragmentWebLoadingBase$5;','Lcom/platform/usercenter/jsbridge/JsBridgeWebChromeClient;','Lcom/platform/oms/ui/LoadingWebActivity$CommonWebFragment$1;','Lcom/platform/oms/ui/js/JSCommondMethod;'}; dex={}
for line in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(line)
 if o.get('type') in owners:dex[o['type']]=o
 if len(dex)==len(owners):break
assert dex.keys()==owners
def base(k,n,v,impl,sig):
 return dict(activity=A,webview=W,kind=k,name=n,value=v,implementation=impl,signature=sig,normalized_signature=sig,normalized_api=sig if k=='setting' else n,bridge_method=sig if k in {'bridge_method','message_handler'} else '',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='FragmentWebLoadingBase/CommonWebFragment')
settings=[('setSupportZoom','false','Z'),('setJavaScriptEnabled','true','Z'),('setLoadsImagesAutomatically','true','Z'),('setUseWideViewPort','true','Z'),('setLoadWithOverviewMode','true','Z'),('setDefaultZoom','MEDIUM','Landroid/webkit/WebSettings$ZoomDensity;'),('setSupportZoom','false','Z'),('setUserAgentString','runtime base UA + UC UA','Ljava/lang/String;'),('setAppCacheEnabled','false','Z'),('setDomStorageEnabled','true','Z'),('setDatabaseEnabled','true','Z'),('setDatabasePath','application database path','Ljava/lang/String;'),('setCacheMode','2','I')]
out=[]
for n,v,t in settings:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V'; out.append(base('setting',n,v,'com.platform.oms.ui.FragmentWebLoadingBase#initView',s))
selected={
 'Lcom/platform/oms/ui/FragmentWebLoadingBase$5;':None,
 'Lcom/platform/usercenter/jsbridge/JsBridgeWebChromeClient;':{'onJsAlert','onJsConfirm'},
 'Lcom/platform/oms/ui/LoadingWebActivity$CommonWebFragment$1;':{'onJsPrompt'}}
for owner,names in selected.items():
 impl=owner[1:-1].replace('/','.')
 for m in dex[owner]['methods']:
  if m['name']=='<init>' or names is not None and m['name'] not in names:continue
  s=m['signature'].replace('-\\u003e','->');out.append(base('callback',m['name'],impl,impl,s))
for m in dex['Lcom/platform/oms/ui/js/JSCommondMethod;']['methods']:
 s=m['signature'].replace('-\\u003e','->')
 if '(Landroid/webkit/WebView;Lorg/json/JSONObject;Lcom/platform/usercenter/jsbridge/JsCallback;Landroid/os/Handler;)' not in s:continue
 x=base('message_handler',m['name'],'reflected','com.platform.oms.ui.js.JSCommondMethod',s);x.update(registration_name='JSCommondMethod',namespace='JSCommondMethod');out.append(x)
x=base('bridge','<dynamic-method-class-names>','registered',None,'');x.update(normalized_signature='',normalized_api='<dynamic-method-class-names>',bridge_method='',binding_status='registered-target-unknown',symbol_status='not_applicable',registration_name=None,registration_expression='Intent extra method_class_names split by &');out.append(x)
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
