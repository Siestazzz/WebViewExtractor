#!/usr/bin/env python3
import json
from pathlib import Path

D=Path(__file__).resolve().parent
ROOT=D.parents[2]
REPORT=ROOT/'test/runs/v9d/com.hunantv.imgo.activity/0/output/capabilities.json'
SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
NFT={
'com.mgsz.detail.ui.AntiqueDetailActivity':('valid','AntiqueDetailActivity.W -> AntiqueDetailFragment.S -> shaded MgNftViewer -> ShNftViewerLayoutBinding.nftWebview'),
'com.mgtv.digital.ui.DigitalDetailActivity':('valid','DigitalDetailActivity.d3 -> MgNftViewer.setNftData -> NftViewerLayoutBinding.nftWebview'),
'com.mgtv.digital.ui.DigitalModelViewActivity':('valid','DigitalModelViewActivity.U2 -> MgNftViewer.setNftData -> NftViewerLayoutBinding.nftWebview'),
'com.mgtv.digital.ui.DigitalBelongActivity':('valid','DigitalBelongActivity.T3 selects NftViewerType_Webview when applicable -> field G MgNftViewer.setNftData -> NftViewerLayoutBinding.nftWebview'),
'com.mgtv.digital.ui.MgNftPreviewActivity':('valid','MgNftPreviewActivity.S2 explicitly creates NftData(NftViewerType_Webview, buildH5Url(...)) -> field F MgNftViewer.setNftData -> NftViewerLayoutBinding.nftWebview')}
def main():
 report=json.loads(REPORT.read_text()); acts={a['activity']:a for a in report['activities']}
 old={x['activity']:x for x in map(json.loads,(D/'v9c-ownership.jsonl').read_text().splitlines())}
 for h,(v,reason) in NFT.items(): old[h]={'activity':h,'verdict':v,'fact_count':len(acts[h]['facts']),'reason':reason,'source_evidence':[reason],'apk_sha256':SHA}
 assert set(old)==set(acts),(set(acts)-set(old),set(old)-set(acts))
 (D/'v9d-ownership.jsonl').write_text('\n'.join(json.dumps(old[k],ensure_ascii=False,separators=(',',':')) for k in sorted(old))+'\n')
 audit={'schema_version':1,'app_version':'9.3.0','apk_sha256':SHA,'host_count':len(acts),'ownership_counts':{},'recovered_nft_hosts':sorted(NFT),'missing_development_facts':{'setting':48,'callback_registration':3,'callback':3,'total_scored':54},'operation_hits':5,'earliest_break':'Binding constructor parameters returned by bind(View) remain constructor_parameter and are not materialized into the returned Binding object fields; consequently NftWebviewLayout initialize/initWebview receiver identity does not reach host ownership.'}
 from collections import Counter
 audit['ownership_counts']=dict(Counter(x['verdict'] for x in old.values()))
 (D/'v9d-nft-binding-break.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':main()
