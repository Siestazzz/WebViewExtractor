#!/usr/bin/env python3
"""Build source/DEX-authored v16a regression facts; never mutates canonical."""
import json,re
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];OUT=H/'v16a-regression-development-facts.jsonl'
SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';ROOT='test/decompiled/com.hunantv.imgo.activity/sources/'
syms={}
want={'Lcom/bytedance/sdk/component/adexpress/bp/z;','Lcom/dtf/face/ui/widget/ToygerWebView$DTFJSBridge;','Lcom/dtf/face/ui/widget/ToygerWebView$ToygerWebChromeClient;','Lcom/dtf/face/ui/widget/ToygerWebView$ToygerWebViewClient;'}
for l in open(R/'test/runs/symbols/mango.jsonl'):
 x=json.loads(l);t=x.get('type')
 if t in want:syms[t]=x
 if t and t.startswith('Lam/a$'):syms[t]=x
assert want<=syms.keys()
def base(a,w,k,n,impl,loc,chain):return {'schema_version':1,'dataset':'mango-v16a-regression-nonblind-development','app_version':'9.3.0','validator':'GPT-5.6 Sol independent source+DEX validation','evidence_status':'confirmed-source-and-dex','nonblind_development_extension':True,'apk_sha256':SHA,'activity':a,'webview':w,'webview_identity':w,'webview_constraint':{'types':[w]},'webview_constraint_semantics':'exact runtime WebView type only; identity source-audited','kind':k,'name':n,'implementation':impl,'binding_status':'confirmed','symbol_status':'confirmed','source_location':ROOT+loc,'binding_chain':chain}
def reg(a,w,n,impl,api,loc,chain):
 x=base(a,w,'bridge',n,impl,loc,chain);x.update(value='registered',value_kind='literal',registration_name=n,registration_expression=json.dumps(n),normalized_signature='',normalized_api=api,bridge_method='');return x
def member(a,w,n,impl,sig,regname,loc,chain,kind='bridge_method'):
 x=base(a,w,kind,n,impl,loc,chain);x.update(value=None,value_kind='literal',registration_name=regname,normalized_signature=sig,normalized_api='WebView message bridge member',bridge_method=sig);return x
out=[]
# Pangle: independently audited host families reach bp.c(WebView,r,String); source constant interface name and @JS member.
pangle=['com.bytedance.sdk.openadsdk.core.activity.base.TTMiddlePageActivity','com.bytedance.sdk.openadsdk.core.activity.base.TTPlayableWebPageActivity','com.bytedance.sdk.openadsdk.core.activity.base.TTVideoScrollWebPageActivity','com.bytedance.sdk.openadsdk.core.activity.base.TTVideoWebPageActivity','com.bytedance.sdk.openadsdk.core.activity.base.TTWebPageActivity','com.bytedance.sdk.openadsdk.core.component.reward.activity.TTFullScreenVideoActivity','com.bytedance.sdk.openadsdk.core.component.reward.activity.TTFullScreenVideoLandscapeActivity','com.bytedance.sdk.openadsdk.core.component.reward.activity.TTRewardVideoActivity','com.bytedance.sdk.openadsdk.core.component.reward.activity.TTRewardVideoLandscapeActivity']
api='Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V';sig='Lcom/bytedance/sdk/component/adexpress/bp/z;->invokeMethod(Ljava/lang/String;)V';chain=['host Pangle renderer -> adexpress bridge manager','bp.c(WebView,r,String) -> new/reused z(r) -> WebView.addJavascriptInterface(z,name)','component.c.j.f12300zx literal IESJSBridge','z.invokeMethod(String) @JavascriptInterface']
for a in pangle:
 out += [reg(a,'android.webkit.WebView','IESJSBridge','com.bytedance.sdk.component.adexpress.bp.z',api,'com/bytedance/sdk/component/adexpress/bp/bp.java:221-234',chain),member(a,'android.webkit.WebView','invokeMethod','com.bytedance.sdk.component.adexpress.bp.z',sig,'IESJSBridge','com/bytedance/sdk/component/adexpress/bp/z.java:21-29',chain)]
# mgadplus Q registrations in two independently owned hosts.
mgsrc=(R/(ROOT+'am/a.java')).read_text(); pairs=re.findall(r'\.t\("([^"]+)", new ([A-Za-z0-9]+)\(\)\)',mgsrc[mgsrc.index('public void Q()'):mgsrc.index('public void R(')])
assert len(pairs)==15,pairs
for a in ['com.mgmi.ads.api.render.RewardAdFreeActivity','com.mgtv.ui.videoplay.MGVideoPlayActivity']:
 chain=['host ad render path -> am.a with field e:ImgoAdWebView','am.a.Q() checks e != null','same e.t(literalName,new handler()) -> BridgeWebView handler map']
 for name,cls in pairs:
  owner=f'Lam/a${cls};'; meth=[m for m in syms[owner]['methods'] if m['name'] in ('handler','a') and 'Ljava/lang/String;' in m['signature']]
  assert meth,(name,owner)
  sig=meth[0]['signature'];impl=owner[1:-1].replace('/','.')
  out += [reg(a,'com.mgadplus.brower.ImgoAdWebView',name,impl,'Lcom/mgadplus/brower/jsbridge/BridgeWebView;->t(Ljava/lang/String;Lcom/mgadplus/brower/jsbridge/a;)V','am/a.java:1333-1354',chain),member(a,'com.mgadplus.brower.ImgoAdWebView',meth[0]['name'],impl,sig,name,'am/a.java:1333-1354',chain,'message_handler')]
 # U() installs the exact lifecycle object; enumerate only its DEX-declared framework overrides.
 impl='am.a$w'; api_cb='Lcom/mgadplus/brower/ImgoAdWebView;->setWebViewLifeCycleCallback(Lcom/mgadplus/brower/g;)V'
 x=base(a,'com.mgadplus.brower.ImgoAdWebView','callback_registration','setWebViewLifeCycleCallback',impl,'am/a.java:1392-1396',chain);x.update(value=impl,value_kind='literal',normalized_signature=api_cb,normalized_api=api_cb);out.append(x)
 for m in syms['Lam/a$w;']['methods']:
  if m['name'].startswith('on'):
   x=base(a,'com.mgadplus.brower.ImgoAdWebView','callback',m['name'],impl,'am/a.java:570-650',chain);x.update(value=None,value_kind='literal',normalized_signature=m['signature'],normalized_api='com.mgadplus.brower.g',registered_implementation=impl,dispatch_status='framework');out.append(x)
# DTF concrete constructor surface for both subclasses.
for a in ['com.dtf.face.ui.LandFaceLoadingActivity','com.dtf.face.ui.PortFaceLoadingActivity']:
 w='com.dtf.face.ui.widget.ToygerWebView';chain=['host extends FaceLoadingActivity','FaceLoadingActivity creates new ToygerWebView and stores field M','ToygerWebView(Context,AttributeSet) -> init()']
 out += [reg(a,w,'AndroidInterface','com.dtf.face.ui.widget.ToygerWebView$DTFJSBridge',api,'com/dtf/face/ui/widget/ToygerWebView.java:187-211',chain),member(a,w,'getStyleOptions','com.dtf.face.ui.widget.ToygerWebView$DTFJSBridge','Lcom/dtf/face/ui/widget/ToygerWebView$DTFJSBridge;->getStyleOptions()Ljava/lang/String;','AndroidInterface','com/dtf/face/ui/widget/ToygerWebView.java:37-42',chain)]
 for typ,setter in [('ToygerWebChromeClient','setWebChromeClient'),('ToygerWebViewClient','setWebViewClient')]:
  owner=f'Lcom/dtf/face/ui/widget/ToygerWebView${typ};';impl=owner[1:-1].replace('/','.');desc='Landroid/webkit/WebChromeClient;' if 'Chrome' in typ else 'Landroid/webkit/WebViewClient;';s=f'Landroid/webkit/WebView;->{setter}({desc})V'
  x=base(a,w,'callback_registration',setter,impl,'com/dtf/face/ui/widget/ToygerWebView.java:187-211',chain);x.update(value=impl,value_kind='literal',normalized_signature=s,normalized_api=s);out.append(x)
  for m in syms[owner]['methods']:
   if m['name'].startswith(('on','should')):
    x=base(a,w,'callback',m['name'],impl,'com/dtf/face/ui/widget/ToygerWebView.java:45-185',chain);x.update(value=None,value_kind='literal',normalized_signature=m['signature'],normalized_api='WebChromeClient' if 'Chrome' in typ else 'WebViewClient',registered_implementation=impl,dispatch_status='framework');out.append(x)
# Five additional Erlang literal registrations and exact declared-only endpoints.
for name,line,ml in [('openSystemSetting',661,4980),('sendLocalPixelBarrage',662,5373),('showInteractiveMagicCube',663,5667),('VideoInteractionEvent',664,3470),('VideoSetPlayerMuted',665,3476)]:
 w='com.hunantv.imgo.h5.ImgoWebView';a='com.mgtv.erlang.ErlangLiveActivity';impl='com.mgtv.h5.ImgoWebJavascriptImpl';chain=['ErlangLiveActivity fullWebView/detailWebView -> WebUIFragment','WebUIFragment.mWebView:ImgoWebView -> registerWebHandler','literal/constant-resolved name -> ImgoWebJavascriptImpl.registerHandler','e0.handler getDeclaredMethod(name,String.class)']
 out += [reg(a,w,name,impl,'Lcom/hunantv/imgo/h5/jsbridge/BridgeWebView;->registerHandler(Ljava/lang/String;Lcom/hunantv/imgo/h5/jsbridge/BridgeHandler;)V',f'com/hunantv/imgo/h5/ImgoWebView.java:{line}',chain),member(a,w,name,impl,f'Lcom/mgtv/h5/ImgoWebJavascriptImpl;->{name}(Ljava/lang/String;)V',name,f'com/mgtv/h5/ImgoWebJavascriptImpl.java:{ml}',chain)]
OUT.write_text(''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in out))
print(len(out))
