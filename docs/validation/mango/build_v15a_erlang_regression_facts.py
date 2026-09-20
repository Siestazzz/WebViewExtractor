#!/usr/bin/env python3
"""Build source-authored Erlang handler regression facts without touching canonical."""
import json
from pathlib import Path

HERE=Path(__file__).resolve().parent
OUT=HERE/'v15a-erlang-regression-facts.jsonl'
SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
HOST='com.mgtv.erlang.ErlangLiveActivity'
WV='com.hunantv.imgo.h5.ImgoWebView'
IMPL='com.mgtv.h5.ImgoWebJavascriptImpl'
ROOT='test/decompiled/com.hunantv.imgo.activity/sources/'
CHAIN=[
 'ErlangLiveActivity.fullWebView/detailWebView:Lcom/hunantv/webui/WebUIFragment;',
 'WebUIFragment.initWebView_aroundBody8 -> WebUIFragment.mWebView:Lcom/hunantv/imgo/h5/ImgoWebView;',
 'ImgoWebView.registerWebHandler -> mImgoJavaScriptInterface.registerHandler(name,this)',
 'ImgoWebJavascriptImpl.registerHandler -> BridgeWebView.registerHandler(name,new ImgoWebJavascriptImpl$e0(name,webview))',
 'ImgoWebJavascriptImpl$e0.handler -> implementation.getClass().getDeclaredMethod(name,String.class) -> invoke(implementation,json)'
]

def common(kind,name):
 return {'schema_version':1,'dataset':'mango-v15a-erlang-regression-nonblind-development',
  'app_version':'9.3.0','validator':'GPT-5.6 Sol independent source+DEX validation',
  'evidence_status':'confirmed-source-and-dex','nonblind_development_extension':True,
  'apk_sha256':SHA,'activity':HOST,'webview':WV,'webview_identity':WV,'kind':kind,
  'name':name,'registration_name':name,'registration_expression':json.dumps(name),
  'implementation':IMPL,'binding_status':'confirmed','symbol_status':'confirmed',
  'binding_chain':CHAIN,'carrier_path':'ErlangLiveActivity/WebUIFragment/ImgoWebView',
  'webview_constraint':{'types':[WV]},
  'webview_constraint_semantics':'exact runtime WebView type constraint only; object identity remains source-audited separately'}

def build():
 rows=[]
 for name,line in [('mgTransfer',666),('showMangPushShare',667)]:
  sig=f'Lcom/mgtv/h5/ImgoWebJavascriptImpl;->{name}(Ljava/lang/String;)V'
  r=common('bridge',name);r.update(value='registered',value_kind='literal',normalized_signature='',
   normalized_api='Lcom/hunantv/imgo/h5/jsbridge/BridgeWebView;->registerHandler(Ljava/lang/String;Lcom/hunantv/imgo/h5/jsbridge/BridgeHandler;)V',bridge_method='',
   source_file=ROOT+'com/hunantv/imgo/h5/ImgoWebView.java',source_location=ROOT+f'com/hunantv/imgo/h5/ImgoWebView.java:{line}',
   evidence=f'ImgoWebView.java:{line}; ImgoWebJavascriptImpl.java:5115-5116, handler reflection at audited e0.handler')
  rows.append(r)
  m=common('bridge_method',name);m.update(value=None,value_kind='literal',normalized_signature=sig,
   normalized_api='WebView message bridge member',bridge_method=sig,
   source_file=ROOT+'com/mgtv/h5/ImgoWebJavascriptImpl.java',
   source_location=ROOT+('com/mgtv/h5/ImgoWebJavascriptImpl.java:4643' if name=='mgTransfer' else 'com/mgtv/h5/ImgoWebJavascriptImpl.java:5694'),
   reflection_rule='getDeclaredMethod(registrationName, String.class); exact declared-only (Ljava/lang/String;)V')
  rows.append(m)
 return rows

def main():
 OUT.write_text(''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in build()))

if __name__=='__main__': main()
