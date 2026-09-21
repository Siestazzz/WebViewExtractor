#!/usr/bin/env python3
import json
from pathlib import Path
OUT=Path('docs/validation/ctrip/v16a-missing-hosts-source-facts.pending.jsonl')
APK='cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66'
hosts=[('ctrip.android.login.view.thirdlogin.QQSSOEntryActivity','sources/ctrip/android/login/view/thirdlogin/QQSSOEntryActivity.java',207),('ctrip.android.view.login.v.third.QQEntryActivity','sources/ctrip/android/view/login/v/third/QQEntryActivity.java',176)]
base={'apk_sha256':APK,'app_version':'8.78.0','status':'confirmed_conditional','provenance':'nonblind public host; source independently enumerated after v16a deletion identified the host gap','validator':'oracle_ctrip_source_enum','validator_version':'v16a-missing-hosts-1','validator_model':'gpt-5.6-sol'}
settings=[('setSaveFormData','Landroid/webkit/WebSettings;->setSaveFormData(Z)V','false','literal'),('setCacheMode','Landroid/webkit/WebSettings;->setCacheMode(I)V','-1','literal'),('setNeedInitialFocus','Landroid/webkit/WebSettings;->setNeedInitialFocus(Z)V','false','literal'),('setBuiltInZoomControls','Landroid/webkit/WebSettings;->setBuiltInZoomControls(Z)V','true','literal'),('setSupportZoom','Landroid/webkit/WebSettings;->setSupportZoom(Z)V','true','literal'),('setRenderPriority','Landroid/webkit/WebSettings;->setRenderPriority(Landroid/webkit/WebSettings$RenderPriority;)V','HIGH','enum'),('setJavaScriptEnabled','Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V','true','literal'),('setDatabaseEnabled','Landroid/webkit/WebSettings;->setDatabaseEnabled(Z)V','true','literal'),('setDatabasePath','Landroid/webkit/WebSettings;->setDatabasePath(Ljava/lang/String;)V',None,'dynamic'),('setDomStorageEnabled','Landroid/webkit/WebSettings;->setDomStorageEnabled(Z)V','true','literal'),('setSavePassword','Landroid/webkit/WebSettings;->setSavePassword(Z)V','false','literal'),('setAllowFileAccess','Landroid/webkit/WebSettings;->setAllowFileAccess(Z)V','false','literal'),('setAllowFileAccessFromFileURLs','Landroid/webkit/WebSettings;->setAllowFileAccessFromFileURLs(Z)V','false','literal')]
branches=[
 {'id':'AuthDialog::j','type':'com.tencent.open.c.c','init':'Lcom/tencent/connect/auth/a;->d()V','file':'sources/com/tencent/connect/auth/a.java','line':520,'client':('com.tencent.connect.auth.a$a',['Lcom/tencent/connect/auth/a$a;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V','Lcom/tencent/connect/auth/a$a;->onPageStarted(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V','Lcom/tencent/connect/auth/a$a;->onReceivedError(Landroid/webkit/WebView;ILjava/lang/String;Ljava/lang/String;)V','Lcom/tencent/connect/auth/a$a;->onReceivedSslError(Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;)V','Lcom/tencent/connect/auth/a$a;->shouldOverrideUrlLoading(Landroid/webkit/WebView;Ljava/lang/String;)Z']), 'chrome':('android.webkit.WebChromeClient',[]),'message':('SecureJsInterface','com.tencent.open.web.security.SecureJsInterface',['Lcom/tencent/open/web/security/SecureJsInterface;->clearAllEdit()V','Lcom/tencent/open/web/security/SecureJsInterface;->curPosFromJS(Ljava/lang/String;)V','Lcom/tencent/open/web/security/SecureJsInterface;->customCallback()Z','Lcom/tencent/open/web/security/SecureJsInterface;->getMD5FromNative()Ljava/lang/String;','Lcom/tencent/open/web/security/SecureJsInterface;->isPasswordEdit(Ljava/lang/String;)V'],561)},
 {'id':'TDialog::k','type':'com.tencent.open.c.b','init':'Lcom/tencent/open/TDialog;->b()V','file':'sources/com/tencent/open/TDialog.java','line':416,'client':('com.tencent.open.TDialog$FbWebViewClient',['Lcom/tencent/open/TDialog$FbWebViewClient;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V','Lcom/tencent/open/TDialog$FbWebViewClient;->onPageStarted(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V','Lcom/tencent/open/TDialog$FbWebViewClient;->onReceivedError(Landroid/webkit/WebView;ILjava/lang/String;Ljava/lang/String;)V','Lcom/tencent/open/TDialog$FbWebViewClient;->shouldOverrideUrlLoading(Landroid/webkit/WebView;Ljava/lang/String;)Z']), 'chrome':('com.tencent.open.b$1',['Lcom/tencent/open/b$1;->onConsoleMessage(Ljava/lang/String;ILjava/lang/String;)V','Lcom/tencent/open/b$1;->onConsoleMessage(Landroid/webkit/ConsoleMessage;)Z']),'message':('sdk_js_if','com.tencent.open.TDialog$JsListener',['Lcom/tencent/open/TDialog$JsListener;->onAddShare(Ljava/lang/String;)V','Lcom/tencent/open/TDialog$JsListener;->onCancel(Ljava/lang/String;)V','Lcom/tencent/open/TDialog$JsListener;->onCancelAddShare(Ljava/lang/String;)V','Lcom/tencent/open/TDialog$JsListener;->onCancelInvite()V','Lcom/tencent/open/TDialog$JsListener;->onCancelLogin()V','Lcom/tencent/open/TDialog$JsListener;->onComplete(Ljava/lang/String;)V','Lcom/tencent/open/TDialog$JsListener;->onInvite(Ljava/lang/String;)V','Lcom/tencent/open/TDialog$JsListener;->onLoad(Ljava/lang/String;)V','Lcom/tencent/open/TDialog$JsListener;->showMsg(Ljava/lang/String;)V'],440)}]
def emit(rows,d): rows.append({**base,**d})
rows=[]
for host,hfile,hline in hosts:
 bind=[{'file':hfile,'line':hline,'text':'mTencent.login(this, "all", loginListener)'},{'file':'sources/com/tencent/connect/auth/AuthAgent.java','line':307,'text':'k.b(new AuthAgent$1 Runnable)'},{'file':'sources/com/tencent/connect/auth/AuthAgent.java','line':316,'text':'activity.runOnUiThread(new AuthAgent$1$1)' }]
 for br in branches:
  common={'activity':host,'webview_constraint':{'types':[br['type']]},'source_webview_identity':br['id'],'source_binding_evidence':bind+[{'file':'sources/com/tencent/connect/auth/AuthAgent.java','line':322 if br['id'].startswith('Auth') else 333,'text':'conditional web fallback constructs this dialog'}]}
  for name,api,val,vk in settings:
   expr='context.getDir("databases",0).getPath()' if name=='setDatabasePath' else val
   emit(rows,{**common,'kind':'setting','name':name,'normalized_api':api,'value':val,'value_kind':vk,'source_expression':expr,'capability_site':br['init'],'capability_evidence':[{'file':br['file'],'line':br['line'],'text':'dialog WebSettings configuration; helper j.a supplies security settings where applicable'}]})
  for setter,impl,members in [('setWebViewClient',*br['client']),('setWebChromeClient',*br['chrome'])]:
   api=f'Landroid/webkit/WebView;->{setter}(Landroid/webkit/{"WebViewClient" if setter.endswith("ViewClient") else "WebChromeClient"};)V'
   emit(rows,{**common,'kind':'callback_registration','name':setter,'implementation':impl,'normalized_api':api,'capability_site':br['init'],'capability_evidence':[{'file':br['file'],'line':br['line'],'text':setter+' installs concrete client'}]})
   for sig in members: emit(rows,{**common,'kind':'callback','name':sig.split('->')[1].split('(')[0],'implementation':impl,'normalized_signature':sig,'capability_site':br['init'],'capability_evidence':[{'file':br['file'],'line':br['line'],'text':'installed client override; DEX signature verified'}]})
  reg,impl,members,line=br['message']
  emit(rows,{**common,'kind':'message_bridge','name':reg,'registration_name':reg,'implementation':impl,'normalized_api':'Lcom/tencent/open/a;->a(Ljava/lang/Object;Ljava/lang/String;)V','capability_site':br['init'],'capability_evidence':[{'file':br['file'],'line':line,'text':'console-message bridge registry registration'}]})
  for sig in members: emit(rows,{**common,'kind':'message_bridge_method','name':sig.split('->')[1].split('(')[0],'registration_name':reg,'implementation':impl,'normalized_signature':sig,'capability_site':br['init'],'capability_evidence':[{'file':br['file'],'line':line,'text':'registered message bridge public method; DEX signature verified'}]})
  emit(rows,{**common,'kind':'webview_operation','name':'loadUrl','normalized_api':'Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V','capability_site':br['init'],'capability_evidence':[{'file':br['file'],'line':559 if br['id'].startswith('Auth') else 441,'text':'dialog loads authorization URL'}]})
# Tour: source-valid host/group binding only; expansion must come from independently audited v1 group.
emit(rows,{'activity':'ctrip.android.tour.search.view.CTTourSearchActivity2','kind':'activity_group_binding','name':'ctrip-h5-v1','group_id':'ctrip-h5-v1','webview_constraint':{'types':['ctrip.android.view.h5.view.H5WebView']},'source_webview_identity':'SearchH5Fragment2::H5Fragment.mWebView','source_binding_evidence':[{'file':'sources/ctrip/android/tour/search/view/CTTourSearchActivity2.java','line':2372,'text':'pager factory receives SearchH5Fragment2.class and Bundle'},{'file':'sources/ctrip/android/tour/search/view/v2/SearchH5Fragment2.java','line':38,'text':'SearchH5Fragment2 extends H5Fragment'}],'unknown':['conditional on a custom tab selecting SearchH5Fragment2; exact pager factory instantiation timing is lifecycle-dependent'],'note':'Expand only from independently source-audited ctrip-h5-v1 group. Do not use initData SearchNavBarPlugin.mWebView.'})
OUT.write_text(''.join(json.dumps(x,separators=(',',':'),ensure_ascii=False)+'\n' for x in rows))
print(f'wrote {len(rows)} rows to {OUT}')
# Produce scorer-compatible cumulative rows. Unsupported binding/message kinds are normalized or expanded.
score=[]
for row in rows:
    if row['activity']=='ctrip.android.tour.search.view.CTTourSearchActivity2':
        continue
    r=dict(row)
    if r['kind']=='message_bridge':
        r['kind']='bridge'; r['transport_kind']='console_jsbridge'
    elif r['kind']=='message_bridge_method':
        r['kind']='bridge_method'; r['transport_kind']='console_jsbridge'
    if r['kind']=='webview_operation':
        r['value']=None; r['value_kind']='dynamic'; r['source_expression']='authorization URL assembled by AuthAgent and captured by the selected Dialog'
    score.append(r)
# Expand only the independently audited v1 capabilities applicable to the real SearchWebView instance.
import sys
sys.path.insert(0,str(Path('scripts/validation').resolve()))
from export_oracle import setting_name,setting_value
G={x['group_id']:x for x in map(json.loads,Path('docs/validation/ctrip/groups.jsonl').read_text().splitlines())}['ctrip-h5-v1']
tour_common={**base,'activity':'ctrip.android.tour.search.view.CTTourSearchActivity2','group_id':'ctrip-h5-v1','family':'h5-v1','evaluation_tier':'core_required','webview_constraint':{'types':['ctrip.android.tour.search.view.widget.SearchWebView']},'source_webview_identity':'SearchH5Fragment2::mWebView(SearchWebView)','source_binding_evidence':[{'file':'sources/ctrip/android/tour/search/view/CTTourSearchActivity2.java','line':2372,'text':'pager factory receives SearchH5Fragment2.class and Bundle'},{'file':'sources/ctrip/android/tour/search/view/v2/SearchH5Fragment2.java','line':38,'text':'SearchH5Fragment2 extends H5Fragment'},{'file':'sources/ctrip/android/tour/search/view/v2/SearchH5Fragment2.java','line':227,'text':'addWebView assigns new SearchWebView(getContext()) to inherited mWebView'},{'file':'sources/ctrip/android/tour/search/view/v2/SearchH5Fragment2.java','line':338,'text':'onCreateView invokes super.onCreateView, executing H5Fragment initialization'},{'file':'sources/ctrip/android/tour/search/view/v2/SearchH5Fragment2.java','line':293,'text':'loadWebview invokes super.loadWebview'}]}
for s in G['capabilities'].get('settings',[]):
    val,vk,expr=setting_value(s['value_expression'])
    score.append({**tour_common,'kind':'setting','name':setting_name(s['normalized_api']),'normalized_api':s['normalized_api'],'value':val,'value_kind':vk,'source_expression':expr,'capability_evidence':[s['evidence']]})
for br in G['capabilities'].get('bridges',[]):
    reg=br.get('name') or br.get('name_expression');ev=br.get('registry_evidence')
    score.append({**tour_common,'kind':'bridge','name':br.get('name'),'registration_name':reg,'implementation':br.get('type'),'normalized_api':'Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V','value':br.get('name_expression',br.get('name')),'capability_evidence':([ev] if ev else [])})
    for m in br.get('methods',[]):
        score.append({**tour_common,'kind':'bridge_method','name':m['normalized_signature'].split('->')[1].split('(')[0],'registration_name':reg,'implementation':br.get('type'),'normalized_signature':m['normalized_signature'],'capability_evidence':[m['evidence']]})
for key,setter,api in [('webview_client','setWebViewClient','Landroid/webkit/WebView;->setWebViewClient(Landroid/webkit/WebViewClient;)V'),('webchrome_client','setWebChromeClient','Landroid/webkit/WebView;->setWebChromeClient(Landroid/webkit/WebChromeClient;)V')]:
    client=G['capabilities'].get(key,{})
    if client:
        score.append({**tour_common,'kind':'callback_registration','name':setter,'implementation':client.get('type'),'normalized_api':api,'capability_evidence':[client.get('registration_evidence') or {'file':'sources/ctrip/android/view/h5/view/H5Fragment.java','line':0,'text':'inherited H5Fragment client registration'}]})
    for cb in client.get('overrides',[]):
        score.append({**tour_common,'kind':'callback','name':cb['normalized_signature'].split('->')[1].split('(')[0],'implementation':client.get('type'),'normalized_signature':cb['normalized_signature'],'capability_evidence':[cb['evidence']]})
SCORE=Path('docs/validation/ctrip/v16a-missing-hosts-source-facts.scoreable.jsonl')
SCORE.write_text(''.join(json.dumps(x,separators=(',',':'),ensure_ascii=False)+'\n' for x in score))
print(f'wrote {len(score)} scoreable rows to {SCORE}')
