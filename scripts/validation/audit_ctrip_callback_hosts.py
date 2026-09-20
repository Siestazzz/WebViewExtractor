#!/usr/bin/env python3
"""Verify concrete callback/bridge host chains without reading holdouts."""
import argparse,json
from pathlib import Path

def ev(root,rel,line):
 p=root/'sources'/rel; return {'file':f'sources/{rel}','line':line,'text':p.read_text(errors='replace').splitlines()[line-1].strip()}

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--decompile-root',type=Path,required=True); ap.add_argument('--v3',type=Path); ap.add_argument('--output',type=Path,required=True); a=ap.parse_args(); root=a.decompile_root
 rows=[
 {'activity':'ctrip.android.view.myctrip.orderbiz.MyCtripOrderModalActivity','capability':'v1_webchrome_client','verdict':'bound','chain':[ev(root,'ctrip/android/view/myctrip/orderbiz/MyCtripOrderModalActivity.java',113),ev(root,'ctrip/android/view/myctrip/orderbiz/MyCtripOrderModalActivity.java',126),ev(root,'ctrip/android/view/myctrip/orderbiz/MyCtripOrderModalActivity.java',135),ev(root,'ctrip/android/view/h5/view/H5Fragment.java',3514),ev(root,'ctrip/android/view/h5/view/H5Fragment.java',3002),ev(root,'ctrip/android/view/h5/view/H5Fragment.java',2941),ev(root,'ctrip/android/view/h5/view/H5Fragment.java',2944)],'implementation':'ctrip.android.view.h5.view.H5Fragment$b','inherited_from':'ctrip.android.view.h5.view.f','callback_count':11},
 {'activity':'ctrip.business.evaluation.EvaluateDialogActivity','capability':'v2_webchrome_client','verdict':'not_bound','chain':[ev(root,'ctrip/business/evaluation/EvaluateDialogActivity.java',202),ev(root,'ctrip/business/evaluation/EvaluateDialogActivity.java',240),ev(root,'ctrip/android/view/h5v2/view/H5WebView.java',235),ev(root,'ctrip/android/view/h5v2/view/H5WebView.java',246)],'implementation':None,'callback_count':0,'reason':'Direct H5WebView.H path installs WebViewClient e only; no Fragment or setWebChromeClient call.'},
 {'activity':'cmbapi.CMBApiEntryActivity','capability':'bridge:CMBSDK','verdict':'bound','chain':[ev(root,'cmbapi/CMBApiEntryActivity.java',137),ev(root,'cmbapi/CMBApiEntryActivity.java',63),ev(root,'cmbapi/CMBApiEntryActivity.java',138),ev(root,'cmbapi/CMBApiEntryActivity.java',89)],'dex_chain':['Lcmbapi/CMBApiEntryActivity;-><init>()V writes null to ->mCmbSdkExecutor:Lcmbapi/h;','Lcmbapi/CMBApiEntryActivity;->initJsInterface()V new-instance Lcmbapi/h; then iput-object ->mCmbSdkExecutor:Lcmbapi/h;','Lcmbapi/CMBApiEntryActivity;->initWebView()V iget-object ->mCmbSdkExecutor:Lcmbapi/h; then addJavascriptInterface(Object,String)'],'implementation':'cmbapi.h','bridge_method_count':6},
 ]
 if a.v3:
  data=json.loads(a.v3.read_text())
  for row in rows:
   act=next((x for x in data['activities'] if x['activity']==row['activity']),None)
   if row['capability']=='bridge:CMBSDK' and act:
    facts=[x for x in act['facts'] if x['kind']=='bridge' and x.get('registration_name')=='CMBSDK']
    row['v3_observation']=[{'implementation':x.get('implementation'),'member_count':len(x.get('members',[])),'site':x.get('site')} for x in facts]
 a.output.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows))
if __name__=='__main__': main()
