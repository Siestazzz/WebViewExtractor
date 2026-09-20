#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
V9B = ROOT / "test/runs/v9b/com.hunantv.imgo.activity/0/output/capabilities.json"
V9C = ROOT / "test/runs/v9c/com.hunantv.imgo.activity/0/output/capabilities.json"
BASE = OUT / "v8a-ownership.jsonl"
SHA = "65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5"

def activities(path):
    d = json.loads(path.read_text())
    return d, {a["activity"]: a for a in d["activities"]}

def main():
    bdoc, b = activities(V9B); cdoc, c = activities(V9C)
    added, removed = sorted(c.keys()-b.keys()), sorted(b.keys()-c.keys())
    qfact = next(f for f in c["com.mgsz.h5.WebContainerActivity"]["facts"] if "->q0:Lkq/d;" in json.dumps(f))
    members = qfact["members"]
    base = {x["activity"]: x for x in map(json.loads, BASE.read_text().splitlines())}
    for host in removed: base.pop(host, None)
    base["com.mgtv.miniplay.MiniPlayActivity"] = {
      "activity":"com.mgtv.miniplay.MiniPlayActivity","verdict":"valid","fact_count":len(c["com.mgtv.miniplay.MiniPlayActivity"]["facts"]),
      "reason":"Activity-owned MiniPlayFragment conditionally opens the comment dialog; its CommentInfoView creates/attaches WebViewFragment and the ImgoWebView.",
      "source_evidence":["MiniPlayActivity.V -> MiniPlayFragment.od", "MiniPlayerCommentDialogFragment.Ec -> CommentInfoView.X6", "CommentInfoView.X6 -> WebViewFragment.ue -> ImgoWebView"],"apk_sha256":SHA}
    base["com.bytedance.sdk.openadsdk.core.activity.base.TTDelegateActivity"] = {
      "activity":"com.bytedance.sdk.openadsdk.core.activity.base.TTDelegateActivity","verdict":"valid","fact_count":len(c["com.bytedance.sdk.openadsdk.core.activity.base.TTDelegateActivity"]["facts"]),
      "reason":"Intent-dispatched Activity constructs and stores Pangle dialog/widget instances whose instance fields own SSWebView objects.",
      "source_evidence":["TTDelegateActivity.onCreate -> z()/intent dispatch", "TTDelegateActivity fields kc/vr/xw/pb/s/p -> widget constructors", "widget SSWebView fields c/tn/zx/e"],"apk_sha256":SHA}
    assert set(base)==set(c), (set(c)-set(base),set(base)-set(c))
    (OUT/"v9c-ownership.jsonl").write_text("\n".join(json.dumps(base[k],ensure_ascii=False,separators=(",",":")) for k in sorted(base))+"\n")
    audit={
      "schema_version":1,"report":"test/runs/v9c/com.hunantv.imgo.activity/0/output/capabilities.json","apk_sha256":cdoc["apk_sha256"],
      "host_counts":{"v9b":len(b),"v9c":len(c)},"added":added,"removed":removed,
      "removed_valid_regressions":{
       "com.mgsz.detail.ui.AntiqueDetailActivity":"Activity.W -> AntiqueDetailFragment.E7 -> com.mgsz.hunantv.nft.MgNftViewer.setNftData -> NftWebviewLayout.setupData -> binding.webview.loadUrl",
       "com.mgtv.digital.ui.DigitalDetailActivity":"Activity.x3 -> com.hunantv.nft.MgNftViewer.setNftData -> NftWebviewLayout.setupData -> binding.webview.loadUrl",
       "com.mgtv.digital.ui.DigitalModelViewActivity":"Activity.U2 -> com.hunantv.nft.MgNftViewer.setNftData -> NftWebviewLayout.setupData -> binding.webview.loadUrl"},
      "earliest_common_break":"NftWebviewLayout is a FrameLayout carrier. The host reaches setupData and its Runnable; the missing edge is carrier/ViewBinding field mViewBinding.webview -> android.webkit.WebView before loadUrl, after platform-hierarchy/relevance filtering.",
      "q0":{"member_count":len(members),"observed":sum(m.get("dispatch_status")=="observed" for m in members),"unresolved":sum(m.get("dispatch_status")=="unresolved" for m in members),"members":[{"signature":m.get("signature"),"dispatch_status":m.get("dispatch_status")} for m in members]},
      "gold_unchanged_does_not_cover_nft":True
    }
    (OUT/"v9c-validation.json").write_text(json.dumps(audit,ensure_ascii=False,indent=2)+"\n")

if __name__ == "__main__": main()
