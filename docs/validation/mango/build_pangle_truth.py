#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent; R=H.parents[2]; F=H/'facts.jsonl'
SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'; W='com.bytedance.sdk.component.widget.SSWebView'
HOSTS={'com.bytedance.sdk.openadsdk.core.activity.base.TTWebPageActivity':'landing-page','com.bytedance.sdk.openadsdk.core.activity.base.TTPlayableWebPageActivity':'playable-renderer','com.bytedance.sdk.openadsdk.core.activity.base.TTVideoWebPageActivity':'video-page','com.bytedance.sdk.openadsdk.core.activity.base.TTVideoScrollWebPageActivity':'video-page'}
owners={'Lcom/bytedance/sdk/openadsdk/core/widget/c/z;','Lcom/bytedance/sdk/openadsdk/core/widget/c/zx;','Lcom/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity$1;','Lcom/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity$6;','Lcom/bytedance/sdk/openadsdk/core/playable/bp$c$2;','Lcom/bytedance/sdk/openadsdk/core/playable/bp$c$3;','Lcom/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity$1;','Lcom/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity$9;','Lcom/bytedance/sdk/openadsdk/core/i/z$c;'}
d={}
for line in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(line)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==len(owners):break
assert d.keys()==owners
def row(a,k,n,v,impl,sig,norm,ev,path):
 x=dict(activity=a,webview=W,kind=k,name=n,value=v,implementation=impl,signature=sig,normalized_signature=norm,normalized_api=norm if k=='setting' else ('WebView message bridge registration' if k=='bridge' else n),bridge_method=norm if k=='bridge' else '',evidence='test/decompiled/com.hunantv.imgo.activity/sources/'+ev,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if norm.startswith('Landroid/') else 'confirmed',carrier_path=path,conditional=True)
 if k=='bridge':x['registration_name']='JS_LANDING_PAGE_LOG_OBJ'
 return x
settings=[('setSavePassword','false','Z','com/bytedance/sdk/component/widget/SSWebView.java:152-158'),('setMediaPlaybackRequiresUserGesture','false','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:116-121'),('setJavaScriptEnabled','true','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:80-84'),('setSupportZoom','false','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:85-91'),('setLoadWithOverviewMode','true','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:95'),('setUseWideViewPort','true','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:96'),('setDomStorageEnabled','true','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:97'),('setAllowFileAccess','false','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:98'),('setBlockNetworkImage','false','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:99'),('setDisplayZoomControls','false','Z','com/bytedance/sdk/openadsdk/core/widget/c/tn.java:100'),('setUserAgentString','md.c(...)','Ljava/lang/String;',''),('setMixedContentMode','0','I','')]
base=['Lcom/bytedance/sdk/openadsdk/core/widget/c/z;','Lcom/bytedance/sdk/openadsdk/core/widget/c/zx;']
specific={'landing-page':['Lcom/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity$1;','Lcom/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity$6;'],'playable-renderer':['Lcom/bytedance/sdk/openadsdk/core/playable/bp$c$2;','Lcom/bytedance/sdk/openadsdk/core/playable/bp$c$3;'],'video-page':['Lcom/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity$1;','Lcom/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity$9;']}
out=[]
for a,path in HOSTS.items():
 for n,v,t,e in settings:
  ev=e or ('com/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity.java:421-426' if path=='landing-page' else ('com/bytedance/sdk/openadsdk/core/playable/bp.java:226-247' if path=='playable-renderer' else 'com/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity.java:430-483'))
  norm=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row(a,'setting',n,v,'Pangle SSWebView configuration',norm,norm,ev,path))
 for owner in base+specific[path]:
  impl=owner[1:-1].replace('/','.')
  for m in d[owner]['methods']:
   if not m['name'].startswith(('on','should','getDefaultVideoPoster')):continue
   sig=m['signature'].replace('-\u003e','->');out.append(row(a,'callback',m['name'],impl,impl,sig,sig,'com/bytedance/sdk/openadsdk/core/widget/c/z.java | zx.java',path))
 owner='Lcom/bytedance/sdk/openadsdk/core/i/z$c;';impl=owner[1:-1].replace('/','.')
 for m in d[owner]['methods']:
  if 'Landroid/webkit/JavascriptInterface;' not in m.get('annotations',[]):continue
  sig=m['signature'].replace('-\u003e','->');out.append(row(a,'bridge','JS_LANDING_PAGE_LOG_OBJ',m['name'],impl,sig,sig,'com/bytedance/sdk/openadsdk/core/i/z.java:149-154',path))
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity') not in HOSTS or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
from collections import Counter
print(len(out),Counter(x['activity'] for x in out),Counter(x['kind'] for x in out))
