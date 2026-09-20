#!/usr/bin/env python3
"""Replace QMP delegate methods with the actual installed client overrides."""
import argparse,json
from pathlib import Path

APK='cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66'
METHODS=[
('ClientSdk8','onJsAlert','Z','Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsResult;',53),
('ClientSdk8','onJsConfirm','Z','Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsResult;',78),
('ClientSdk8','onProgressChanged','V','Landroid/webkit/WebView;I',110),
('ClientSdk8','onReceivedTitle','V','Landroid/webkit/WebView;Ljava/lang/String;',115),
('ViewClientSdk8','onPageFinished','V','Landroid/webkit/WebView;Ljava/lang/String;',128),
('ViewClientSdk8','onPageStarted','V','Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;',133),
('ViewClientSdk8','onReceivedError','V','Landroid/webkit/WebView;ILjava/lang/String;Ljava/lang/String;',138),
('ViewClientSdk8','onReceivedSslError','V','Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;',143),
('ViewClientSdk8','shouldOverrideUrlLoading','Z','Landroid/webkit/WebView;Ljava/lang/String;',149),
]
def main():
 ap=argparse.ArgumentParser(); ap.add_argument('facts',type=Path); ap.add_argument('--decompile-root',type=Path,required=True); a=ap.parse_args(); rows=list(map(json.loads,a.facts.read_text().splitlines()))
 rows=[x for x in rows if not (x['activity']=='com.qmp.sdk.ui.activity.AuthActivity' and x['kind']=='callback')]
 src=(a.decompile_root/'sources/com/qmp/sdk/utils/WebViewHelper.java').read_text(errors='replace').splitlines()
 for cls,name,ret,params,line in METHODS:
  owner=f'com.qmp.sdk.utils.WebViewHelper$WebViewHelperSdk8${cls}'
  rows.append({'activity':'com.qmp.sdk.ui.activity.AuthActivity','webview':'android.webkit.WebView','kind':'callback','name':name,'value':None,'implementation':owner,'signature':None,'normalized_signature':f"L{owner.replace('.','/')};->{name}({params}){ret}",'normalized_api':None,'evidence':[{'file':'sources/com/qmp/sdk/utils/WebViewHelper.java','line':line,'text':src[line-1].strip()}],'evidence_strength':'high','binding_status':'third-party_factory','unknown':[],'apk_sha256':APK})
 a.facts.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows))
if __name__=='__main__': main()
