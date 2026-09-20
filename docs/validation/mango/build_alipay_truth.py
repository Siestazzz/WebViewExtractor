#!/usr/bin/env python3
"""Build both conditional Alipay H5 carrier paths for two inherited hosts."""
import json
from pathlib import Path

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
DEX=ROOT/'test/runs/symbols/mango.jsonl'
FACTS=HERE/'facts.jsonl'
SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
HOSTS=('com.alipay.sdk.app.H5AuthActivity','com.alipay.sdk.app.H5OpenAuthActivity')
WEBVIEW='com.hunantv.imgo.h5.browser.MoWebView'
PREFIX='test/decompiled/com.hunantv.imgo.activity/sources/'

needed={'Lcom/alipay/sdk/app/b;','Lcom/alipay/sdk/widget/s;','Lcom/alipay/sdk/widget/t;'}
dex={}
with DEX.open(errors='replace') as stream:
    for line in stream:
        obj=json.loads(line)
        if obj.get('type') in needed:
            dex[obj['type']]=obj
            if len(dex)==len(needed):break
if dex.keys()!=needed: raise SystemExit(f'missing Alipay DEX owners: {sorted(needed-dex.keys())}')

def base(host,kind,name,value,impl,sig,norm,evidence,path):
    return dict(activity=host,webview=WEBVIEW,kind=kind,name=name,value=value,
        implementation=impl,signature=sig,normalized_signature=norm,
        normalized_api=norm if kind=='setting' else name,bridge_method='',
        evidence=PREFIX+evidence,binding_status='confirmed',apk_sha256=SHA,
        value_kind='',source_expression='',symbol_status='external_api' if norm.startswith('Landroid/') else 'confirmed',
        carrier_path=path,conditional=True)

# Observed calls, including the duplicated v2 JavaScript enable invocation.
v1_settings=(
 ('setUserAgentString','settings.getUserAgentString() + n.c(context)','Ljava/lang/String;',54),
 ('setRenderPriority','HIGH','Landroid/webkit/WebSettings$RenderPriority;',55),
 ('setSupportMultipleWindows','true','Z',56),('setJavaScriptEnabled','true','Z',57),
 ('setSavePassword','false','Z',58),('setJavaScriptCanOpenWindowsAutomatically','true','Z',59),
 ('setMinimumFontSize','settings.getMinimumFontSize() + 8','I',60),('setAllowFileAccess','false','Z',61),
 ('setTextSize','NORMAL','Landroid/webkit/WebSettings$TextSize;',62),('setDomStorageEnabled','true','Z',63),
 ('setCacheMode','1','I',64),
)
v2_settings=(
 ('setUserAgentString','webView.getSettings().getUserAgentString() + AlipaySDK(...)','Ljava/lang/String;',217),
 ('setUseWideViewPort','true','Z',186),('setAppCacheMaxSize','5242880','J',187),
 ('setAppCachePath','context.getCacheDir().getAbsolutePath()','Ljava/lang/String;',188),
 ('setAllowFileAccess','true','Z',189),('setAppCacheEnabled','true','Z',190),
 ('setJavaScriptEnabled','true','Z',191),('setCacheMode','-1','I',192),
 ('setSupportMultipleWindows','true','Z',193),('setJavaScriptEnabled','true','Z',194),
 ('setSavePassword','false','Z',195),('setJavaScriptCanOpenWindowsAutomatically','true','Z',196),
 ('setDomStorageEnabled','true','Z',197),
)
callback_names={
 'Lcom/alipay/sdk/app/b;':{'onPageFinished','onPageStarted','onReceivedError','onReceivedSslError','shouldOverrideUrlLoading'},
 'Lcom/alipay/sdk/widget/s;':{'onJsPrompt','onProgressChanged','onReceivedTitle'},
 'Lcom/alipay/sdk/widget/t;':{'onPageFinished','onReceivedError','onReceivedSslError','shouldOverrideUrlLoading'},
}

generated=[]
for host in HOSTS:
    for path,settings,relative in (
        ('alipay-v1-widget-h',v1_settings,'com/alipay/sdk/widget/h.java'),
        ('alipay-v2-widget-j-p',v2_settings,'com/alipay/sdk/widget/p.java')):
        for name,value,arg,line in settings:
            norm=f'Landroid/webkit/WebSettings;->{name}({arg})V'
            generated.append(base(host,'setting',name,value,
                'com.alipay.sdk.widget.h#a' if path.startswith('alipay-v1') else 'com.alipay.sdk.widget.p#c',
                norm,norm,f'{relative}:{line}',path))
    for owner,path,evidence,label in (
        ('Lcom/alipay/sdk/app/b;','alipay-v1-widget-h','com/alipay/sdk/widget/h.java:20-27 -> com/alipay/sdk/app/b.java:33-63','WebViewClient'),
        ('Lcom/alipay/sdk/widget/s;','alipay-v2-widget-j-p','com/alipay/sdk/widget/p.java:112-118 -> com/alipay/sdk/widget/s.java:19-50','WebChromeClient'),
        ('Lcom/alipay/sdk/widget/t;','alipay-v2-widget-j-p','com/alipay/sdk/widget/p.java:121-127 -> com/alipay/sdk/widget/t.java:19-56','WebViewClient'),
    ):
        impl=owner[1:-1].replace('/','.')
        for method in dex[owner]['methods']:
            if method['name'] not in callback_names[owner]:continue
            sig=method['signature'].replace('-\u003e','->')
            generated.append(base(host,'callback',f'{label}.{method["name"]}',impl,impl,sig,sig,evidence,path))

old=[json.loads(line) for line in FACTS.read_text().splitlines()]
old=[row for row in old if row.get('activity') not in HOSTS or row.get('kind')=='activity_binding']
with FACTS.open('w') as stream:
    for row in old+generated:stream.write(json.dumps(row,ensure_ascii=False,separators=(',',':'))+'\n')
from collections import Counter
print(json.dumps({'generated':len(generated),'per_host':len(generated)//2,'kinds':Counter(x['kind'] for x in generated)},default=dict,sort_keys=True))
