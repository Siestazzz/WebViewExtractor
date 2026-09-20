#!/usr/bin/env python3
import json
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];F=H/'facts.jsonl';A='com.huawei.petalpaysdk.webpay.PayWebviewActivity';SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5';SRC='test/decompiled/com.hunantv.imgo.activity/sources/com/huawei/petalpaysdk/webpay/BaseWebViewActivity.java:52-87 -> PayWebviewActivity.java:26-101'
owners={'Lcom/huawei/petalpaysdk/webpay/BaseWebViewActivity$1;','Lcom/huawei/petalpaysdk/webpay/PayWebviewActivity$MyWebViewClient;'};d={}
for l in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(l)
 if o.get('type') in owners:d[o['type']]=o
 if len(d)==2:break
assert d.keys()==owners
def row(k,n,v,i,s):return dict(activity=A,webview='com.huawei.petalpaysdk.widget.PaySafeWebView',kind=k,name=n,value=v,implementation=i,signature=s,normalized_signature=s,normalized_api=s if k=='setting' else n,bridge_method='',evidence=SRC,binding_status='confirmed',apk_sha256=SHA,value_kind='',source_expression='',symbol_status='external_api' if k=='setting' else 'confirmed',carrier_path='BaseWebViewActivity/PaySafeWebView')
S=[('setAllowContentAccess','false','Z'),('setJavaScriptEnabled','true','Z'),('setSavePassword','false','Z'),('setDomStorageEnabled','true','Z'),('setSavePassword','false','Z')];out=[]
for n,v,t in S:
 s=f'Landroid/webkit/WebSettings;->{n}({t})V';out.append(row('setting',n,v,'PaySafeWebView configuration',s))
for owner in sorted(owners):
 impl=owner[1:-1].replace('/','.')
 for m in d[owner]['methods']:
  if m['name']!='<init>':
   s=m['signature'].replace('-\\u003e','->');out.append(row('callback',m['name'],impl,impl,s))
old=[json.loads(x) for x in F.read_text().splitlines()];old=[x for x in old if x.get('activity')!=A or x.get('kind')=='activity_binding']
with F.open('w') as f:
 for x in old+out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
print(len(out))
