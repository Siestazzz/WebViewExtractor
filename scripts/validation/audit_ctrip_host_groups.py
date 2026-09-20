#!/usr/bin/env python3
"""Audit every canonical Activity-to-group link at capability-family granularity."""
import argparse,json
from pathlib import Path

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--oracle-dir',type=Path,required=True); ap.add_argument('--output',type=Path,required=True); a=ap.parse_args()
 links=list(map(json.loads,a.oracle_dir.joinpath('activity-groups.jsonl').read_text().splitlines())); out=[]
 for link in links:
  excluded=link.get('exclude_capabilities',[])
  scopes={'settings':'confirmed','bridges':'confirmed','webview_client':'confirmed','webchrome_client':('incorrect' if 'webchrome_client' in excluded else 'confirmed')}
  if link['activity']=='ctrip.business.evaluation.EvaluateDialogActivity':
   rationale='Direct H5WebView.H() invokes plugin initialization and installs client e, but never constructs H5Fragment or installs g/a WebChromeClient.'
   chain=link['host_evidence']+[{'file':'sources/ctrip/business/evaluation/EvaluateDialogActivity.java','line':240,'text':'h5WebView2.H(this, this.webUrl, new b());'},{'file':'sources/ctrip/android/view/h5v2/view/H5WebView.java','line':244,'text':'E(activity, this);'},{'file':'sources/ctrip/android/view/h5v2/view/H5WebView.java','line':246,'text':'setWebViewClient(new e(this.x));'}]
  else:
   rationale='Concrete H5Container inheritance or concrete H5Fragment construction/attachment reaches the fragment lifecycle and its H5WebView initialization.'
   if link['group_id']=='ctrip-h5-v1':
    chain=link['host_evidence']+[{'file':'sources/ctrip/android/view/h5/view/H5Fragment.java','line':3002,'text':'initWebView();'},{'file':'sources/ctrip/android/view/h5/view/H5Fragment.java','line':2940,'text':'this.mWebView.l0(this, this.loadURL, new a());'},{'file':'sources/ctrip/android/view/h5/view/H5Fragment.java','line':2944,'text':'this.mWebView.setWebChromeClient(this.webViewClient);'}]
   else:
    chain=link['host_evidence']+[{'file':'sources/ctrip/android/view/h5v2/view/H5Fragment.java','line':1320,'text':'this.mWebView.I(this, this.loadURL, bVar);'},{'file':'sources/ctrip/android/view/h5v2/view/H5Fragment.java','line':1327,'text':'ctrip.android.view.h5v2.view.g.a aVar = new ctrip.android.view.h5v2.view.g.a(...);'},{'file':'sources/ctrip/android/view/h5v2/view/H5Fragment.java','line':1330,'text':'this.mWebView.setWebChromeClient(this.webViewClient);'}]
  out.append({**link,'verdict':'confirmed','capability_scope':scopes,'rationale':rationale,'chain':chain,'audit_note':'A confirmed group may still have explicitly incorrect/excluded capability families; conditional plugin groups are not Activity-linked required facts.'})
 linked={x['activity'] for x in links}
 for activity in map(json.loads,a.oracle_dir.joinpath('activities.jsonl').read_text().splitlines()):
  if activity['classification']=='positive' and activity['activity'] not in linked:
   rationale='Reviewed positive host has direct or non-H5-shared capability facts and is intentionally not assigned a shared H5/RN/Flutter group.'
   history=[]
   if activity['activity']=='ctrip.business.map.SimpleOverseaMapActivity':
    rationale='Plain CtripWebView host registers only local console/mapDAO bridges; it does not reach the H5 v1 registry.'
    history=['Removed erroneous ctrip-h5-v1 link after source review.']
   if activity['activity']=='ctrip.android.tour.search.view.CTTourSearchActivity2':
    rationale='SearchH5Fragment2 creates SearchWebView, whose custom M() registers a distinct subset plus SearchNavBarPlugin; it must not inherit the generic full core group by name alone.'
   out.append({'activity':activity['activity'],'group_id':None,'verdict':'confirmed','binding_mode':activity.get('binding_mode'),'host_evidence':activity.get('evidence',[]),'chain':activity.get('evidence',[]),'capability_scope':{'shared_group':'not_applicable'},'rationale':rationale,'correction_history':history,'apk_sha256':activity['apk_sha256']})
 a.output.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in out))
if __name__=='__main__': main()
