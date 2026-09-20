#!/usr/bin/env python3
"""Build the source/DEX-derived MGVideoPlayActivity Diana development facts."""
import hashlib, json, shutil
from pathlib import Path

HERE=Path(__file__).resolve().parent
CANON=HERE/'canonical-facts.jsonl'; SNAP=HERE/'canonical-facts-before-mgvideo-diana-v1.jsonl'
OUT=HERE/'mgvideo-diana-development-facts.jsonl'
SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
HOST='com.mgtv.ui.videoplay.MGVideoPlayActivity'
ROOT='test/decompiled/com.hunantv.imgo.activity/sources/'
BASE=ROOT+'com/mgtv/diana/sdk/api/core/BaseWebView.java'
CONT=ROOT+'com/mgtv/diana/sdk/api/webview/WebViewContainer.java'

HOST_CHAIN=[
 'MGVideoPlayActivity event case 9 -> y9().showMiniApp(String)',
 'NBFloatFragmentHelper.showMiniApp(String) -> AspectJ closure t.run(Object[])',
 't.run -> NBFloatFragmentHelper.H1(...) -> G1(...) -> F1(...)',
 'NBFloatFragmentHelper.n1(Fragment) stores DEX field h:Landroidx/fragment/app/Fragment;',
 'MiniAppFragment.onViewCreated -> DianaView -> renderMiniApp(FrameLayout)',
]

BASE_SETTINGS=[
 ('setAllowFileAccess','(Z)V',True,54),('setSupportMultipleWindows','(Z)V',False,55),
 ('setAppCacheEnabled','(Z)V',True,56),('setDomStorageEnabled','(Z)V',True,57),
 ('setDatabaseEnabled','(Z)V',True,58),('setJavaScriptEnabled','(Z)V',True,59),
 ('setGeolocationEnabled','(Z)V',True,60),('setUseWideViewPort','(Z)V',False,61),
 ('setBuiltInZoomControls','(Z)V',False,62),('setSupportZoom','(Z)V',False,63),
 ('setDisplayZoomControls','(Z)V',False,64),('setCacheMode','(I)V',2,65),
 ('setSavePassword','(Z)V',False,66),('setJavaScriptEnabled','(Z)V',False,119),
]
PAGE_EXTRA=[('setUseWideViewPort','(Z)V',True,707),('setLayoutAlgorithm','(Landroid/webkit/WebSettings$LayoutAlgorithm;)V','SINGLE_COLUMN',708),('setLoadWithOverviewMode','(Z)V',True,709),('setMediaPlaybackRequiresUserGesture','(Z)V',False,713),('setCacheMode','(I)V',-1,714),('setUserAgentString','(Ljava/lang/String;)V',None,717)]

def row(webview,kind,name,**kw):
 d={'schema_version':1,'dataset':'mango-mgvideo-diana-nonblind-development','app_version':'9.3.0',
    'validator':'GPT-5.6 Sol independent source+DEX validation','evidence_status':'confirmed-source-and-dex',
    'apk_sha256':SHA,'activity':HOST,'webview':webview,'webview_identity':webview,
    'kind':kind,'name':name,'binding_status':'confirmed','binding_chain':HOST_CHAIN,
    'webview_constraint':{'types':[webview]},
    'webview_constraint_semantics':'exact runtime WebView type constraint only; object identity remains source-audited separately',
    'nonblind_development_extension':True}
 d.update(kw); return d

def setting(w,n,desc,val,line,src=BASE):
 api=f'Landroid/webkit/WebSettings;->{n}{desc}'
 return row(w,'setting',n,normalized_signature=api,normalized_api=api,value=val,
   value_kind='dynamic' if val is None else ('enum' if n=='setLayoutAlgorithm' else 'literal'),
   source_file=src,source_location=f'{src}:{line}',implementation='android.webkit.WebSettings')

def cbreg(w,client,api,line,src):
 sig=f'Landroid/webkit/WebView;->{api}(Landroid/webkit/{api[3:]};)V'
 return row(w,'callback_registration',api,normalized_signature=sig,normalized_api=sig,
   value=client,value_kind='literal',implementation=client,callsite_owner=src.rsplit('/',1)[-1][:-5],source_file=src,source_location=f'{src}:{line}')

def cb(w,owner,name,desc,line,src,registered):
 sig=f'L{owner.replace(".","/")};->{name}{desc}'
 return row(w,'callback',name,normalized_signature=sig,normalized_api='WebChromeClient' if 'Chrome' in registered or name in {'onConsoleMessage','onJsPrompt','onProgressChanged','getDefaultVideoPoster','onHideCustomView','onShowCustomView'} else 'WebViewClient',
   value=None,value_kind='literal',implementation=owner,registered_implementation=registered,source_file=src,source_location=f'{src}:{line}',dispatch_status='observed')

def bridge(w,src,line):
 impl='com.mgtv.diana.sdk.api.core.JSInterface'; api='Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V'
 out=[row(w,'bridge','addJavascriptInterface',normalized_signature='',normalized_api=api,value='JSCore',value_kind='literal',registration_name='JSCore',implementation=impl,source_file=src,source_location=f'{src}:{line}')]
 for n,d,l in [('invokeHandler','(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V',70),('invokeHandlerSync','(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;',103),('log','(Ljava/lang/String;Ljava/lang/String;)V',121),('publishHandler','(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V',126)]:
  sig=f'Lcom/mgtv/diana/sdk/api/core/JSInterface;->{n}{d}'
  out.append(row(w,'bridge_method',n,normalized_signature=sig,normalized_api=api,value=None,value_kind='literal',registration_name='JSCore',implementation=impl,source_file=ROOT+'com/mgtv/diana/sdk/api/core/JSInterface.java',source_location=ROOT+f'com/mgtv/diana/sdk/api/core/JSInterface.java:{l}',annotation='Landroid/webkit/JavascriptInterface;'))
 return out

def build():
 page='com.mgtv.diana.sdk.page.view.PageWebView'; service='com.mgtv.diana.sdk.service.view.ServiceWebView'; rows=[]
 rows += [setting(page,*x) for x in BASE_SETTINGS]
 rows += [setting(page,*x,src=CONT) for x in PAGE_EXTRA]
 rows += [setting(service,*x) for x in BASE_SETTINGS]
 rows += bridge(page,CONT,912)+bridge(service,ROOT+'com/mgtv/diana/sdk/service/AppService.java',89)
 # Registrations are retained separately from their callback members.
 regs=[
  (page,'com.mgtv.diana.sdk.api.core.BaseWebView$a','setWebChromeClient',69,BASE),
  (page,'com.mgtv.diana.sdk.api.webview.WebViewContainer$c','setWebChromeClient',551,CONT),
  (page,'com.mgtv.diana.sdk.api.webview.WebViewContainer$a','setWebViewClient',552,CONT),
  (page,'com.mgtv.diana.sdk.api.webview.WebViewContainer$f','setWebChromeClient',830,CONT),
  (page,'com.mgtv.diana.sdk.page.view.PageWebView$a','setWebViewClient',83,ROOT+'com/mgtv/diana/sdk/page/view/PageWebView.java'),
  (service,'com.mgtv.diana.sdk.api.core.BaseWebView$a','setWebChromeClient',69,BASE),
  (service,'com.mgtv.diana.sdk.service.AppService$a','setWebViewClient',93,ROOT+'com/mgtv/diana/sdk/service/AppService.java'),
  (service,'com.mgtv.diana.sdk.service.view.ServiceWebView$a','setWebChromeClient',116,ROOT+'com/mgtv/diana/sdk/service/view/ServiceWebView.java')]
 rows += [cbreg(*x) for x in regs]
 C=CONT; P=ROOT+'com/mgtv/diana/sdk/page/view/PageWebView.java'; A=ROOT+'com/mgtv/diana/sdk/service/AppService.java'; S=ROOT+'com/mgtv/diana/sdk/service/view/ServiceWebView.java'
 members={
 'com.mgtv.diana.sdk.api.core.BaseWebView$a':[('onConsoleMessage','(Landroid/webkit/ConsoleMessage;)Z',36,BASE)],
 'com.mgtv.diana.sdk.api.webview.WebViewContainer$c':[('getDefaultVideoPoster','()Landroid/graphics/Bitmap;',134,C),('onConsoleMessage','(Landroid/webkit/ConsoleMessage;)Z',150,C),('onJsPrompt','(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsPromptResult;)Z',191,C),('onProgressChanged','(Landroid/webkit/WebView;I)V',254,C)],
 'com.mgtv.diana.sdk.api.webview.WebViewContainer$f':[('onHideCustomView','()V',418,C),('onShowCustomView','(Landroid/view/View;Landroid/webkit/WebChromeClient$CustomViewCallback;)V',436,C)],
 'com.mgtv.diana.sdk.api.webview.WebViewContainer$a':[('onPageFinished','(Landroid/webkit/WebView;Ljava/lang/String;)V',955,C),('onPageStarted','(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V',998,C),('onReceivedError','(Landroid/webkit/WebView;ILjava/lang/String;Ljava/lang/String;)V',1021,C),('onReceivedSslError','(Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;)V',1033,C),('onRenderProcessGone','(Landroid/webkit/WebView;Landroid/webkit/RenderProcessGoneDetail;)Z',1042,C),('shouldInterceptRequest','(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;',1057,C),('shouldOverrideUrlLoading','(Landroid/webkit/WebView;Ljava/lang/String;)Z',1063,C),('shouldInterceptRequest','(Landroid/webkit/WebView;Ljava/lang/String;)Landroid/webkit/WebResourceResponse;',1085,C),('onReceivedError','(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;Landroid/webkit/WebResourceError;)V',1098,C)],
 'com.mgtv.diana.sdk.page.view.PageWebView$a':[('onPageFinished','(Landroid/webkit/WebView;Ljava/lang/String;)V',217,P),('onPageStarted','(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V',226,P),('onRenderProcessGone','(Landroid/webkit/WebView;Landroid/webkit/RenderProcessGoneDetail;)Z',236,P),('shouldInterceptRequest','(Landroid/webkit/WebView;Ljava/lang/String;)Landroid/webkit/WebResourceResponse;',243,P),('shouldInterceptRequest','(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;',254,P)],
 'com.mgtv.diana.sdk.service.AppService$a':[('onPageFinished','(Landroid/webkit/WebView;Ljava/lang/String;)V',350,A),('onPageStarted','(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V',362,A),('onReceivedError','(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;Landroid/webkit/WebResourceError;)V',374,A),('onRenderProcessGone','(Landroid/webkit/WebView;Landroid/webkit/RenderProcessGoneDetail;)Z',382,A),('onReceivedError','(Landroid/webkit/WebView;ILjava/lang/String;Ljava/lang/String;)V',389,A)],
 'com.mgtv.diana.sdk.service.view.ServiceWebView$a':[('onJsPrompt','(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsPromptResult;)Z',46,S)]}
 for w,client,_,_,_ in regs:
  for n,d,l,src in members.get(client,[]): rows.append(cb(w,client,n,d,l,src,client))
  if client.endswith('WebViewContainer$f'):
   for n,d,l,src in members['com.mgtv.diana.sdk.api.webview.WebViewContainer$c']: rows.append(cb(w,'com.mgtv.diana.sdk.api.webview.WebViewContainer$c',n,d,l,src,client))
  if client.endswith('AppService$a'):
   src=ROOT+'com/hunantv/imgo/h5/browser/MoWebViewClient.java'
   rows.append(cb(w,'com.hunantv.imgo.h5.browser.MoWebViewClient','shouldInterceptRequest','(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;',77,src,client))
 # Exact-signature dedupe only; distinct WebViews and distinct declaring owners remain distinct.
 seen=set(); out=[]
 for r in rows:
  k=(r['activity'],r['webview'],r['kind'],r.get('registration_name'),r.get('normalized_signature'),json.dumps(r.get('value'),sort_keys=True))
  if k not in seen: seen.add(k); out.append(r)
 return out

def main():
 rows=build(); text=''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in rows)
 OUT.write_text(text)
 if not SNAP.exists(): shutil.copyfile(CANON,SNAP)
 old=[json.loads(x) for x in SNAP.read_text().splitlines() if x.strip()]
 keys={(x.get('activity'),x.get('webview'),x.get('kind'),x.get('registration_name'),x.get('normalized_signature'),json.dumps(x.get('value'),sort_keys=True)) for x in rows}
 kept=[x for x in old if (x.get('activity'),x.get('webview'),x.get('kind'),x.get('registration_name'),x.get('normalized_signature'),json.dumps(x.get('value'),sort_keys=True)) not in keys]
 CANON.write_text(''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in kept+rows))
 print(json.dumps({'facts':len(rows),'canonical_before':len(old),'canonical_after':len(kept)+len(rows),'snapshot_sha256':hashlib.sha256(SNAP.read_bytes()).hexdigest()},sort_keys=True))
if __name__=='__main__': main()
