#!/usr/bin/env python3
"""Group v2d misses using source-verified ownership; does not read holdouts."""
import argparse,json,collections
from pathlib import Path

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('evaluation',type=Path); ap.add_argument('--output',type=Path,required=True); a=ap.parse_args()
 missing=json.loads(a.evaluation.read_text())['missing']; groups=collections.defaultdict(list)
 for x in missing:
  act=x['activity']; kind=x['kind']
  if act=='ctrip.business.map.SimpleOverseaMapActivity': key='oracle_error_non_h5_host'
  elif act=='com.qmp.sdk.ui.activity.AuthActivity': key='oracle_error_delegate_not_override'
  elif act=='cmbapi.CMBApiEntryActivity': key='extractor_gap_field_object_bridge'
  elif kind=='callback' and act=='ctrip.android.view.myctrip.orderbiz.MyCtripOrderModalActivity': key='extractor_gap_v1_chrome_argument_flow'
  elif kind=='callback' and act=='ctrip.business.evaluation.EvaluateDialogActivity': key='oracle_error_direct_h5webview_no_chrome'
  else: key='other'
  groups[key].append(x)
 evidence={
 'oracle_error_non_h5_host':['SimpleOverseaMapActivity owns CtripWebView and registers only console/mapDAO; it never constructs H5WebView/H5Fragment.'],
 'oracle_error_delegate_not_override':['AuthActivity implements IWebCallback. Actual installed overrides are ClientSdk8 and ViewClientSdk8 returned through WebViewHelper.IMPL.'],
 'extractor_gap_field_object_bridge':['CMBApiEntryActivity.<init> writes null to mCmbSdkExecutor. onCreate calls initJsInterface(), which constructs cmbapi.h and writes the field; the following initWebView() reads that field for CMBSDK registration.'],
 'extractor_gap_v1_chrome_argument_flow':['H5Fragment.initWebView creates H5Fragment$b, stores it, then calls VideoEnabledWebView.setWebChromeClient; the override forwards the same parameter to WebView.setWebChromeClient.'],
 'oracle_error_direct_h5webview_no_chrome':['EvaluateDialogActivity calls H5WebView.H directly. H() installs a WebViewClient but no WebChromeClient; no H5Fragment or g/a instance is created.'],
 'other':['Unclassified.']}
 out=[]
 for key,xs in groups.items():
  out.append({'group':key,'count':len(xs),'activities':sorted({x['activity'] for x in xs}),'kinds':dict(collections.Counter(x['kind'] for x in xs)),'normalized_signatures':sorted(x['signature'] for x in xs if x.get('signature')),'names':sorted({x['name'] for x in xs}),'diagnosis':evidence[key]})
 a.output.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in out))
 md=['# Ctrip v2d missing capability audit','',f"The stale evaluation lists {len(missing)} misses. Source ownership review separates oracle errors from extractor gaps:",'']
 for x in out: md += [f"## {x['group']}",'',f"Count: {x['count']}; kinds: `{x['kinds']}`.",'',x['diagnosis'][0],'']
 md += ['## Corrected callback gap','', 'Of the reported 46 callback misses, 21 came from the incorrect SimpleOverseaMap H5 group link, 3 were QMP delegate methods mislabeled as installed overrides, and 11 v2 Chrome callbacks were incorrectly assigned to EvaluateDialogActivity. That Activity directly calls H5WebView.H(), which installs only its WebViewClient. The remaining reported gap is the 11-method v1 WebChromeClient chain in MyCtripOrderModalActivity. Correcting QMP adds nine actual installed overrides under `ClientSdk8` and `ViewClientSdk8`; these require a new evaluation run.','', 'The callback fix needs argument identity through an override/trampoline, followed by superclass-member closure. The concrete object passed to `setWebChromeClient` must remain concrete after `VideoEnabledWebView.setWebChromeClient(WebChromeClient)` calls `super.setWebChromeClient(webChromeClient)`. Member collection then includes the concrete class and inherited Ctrip video client class, deduplicated by full DEX signature.','', '## Corrected bridge gap','', 'Of the reported 263 bridge/bridge-method misses, 256 came from the incorrect SimpleOverseaMap H5 group link. The seven real reported gaps are the `CMBSDK` registration and six annotated methods on `Lcmbapi/h;`. The constructor initializes `mCmbSdkExecutor` to null. `onCreate` then calls `initJsInterface()`, whose `new cmbapi.h` result is written to the field, before `initWebView()` reads it for registration. v3 emits the CMBSDK registration name but resolves the implementation as `number` with no members, so the helper field-writer dependency gap remains.','']
 a.output.with_suffix('.md').write_text('\n'.join(md))
if __name__=='__main__': main()
