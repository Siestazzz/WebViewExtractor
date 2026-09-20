#!/usr/bin/env python3
"""Build the non-blind v12h disappeared-capability development oracle.

Input is the frozen v12a report only for exact sites/members; ownership and status
are fixed below from the independent source audit documented alongside the output.
This file intentionally does not read or update canonical-facts.jsonl.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
REPORT = ROOT / "test/runs/v12a/com.tencent.news/0/output/capabilities.json"
OUT = Path(__file__).with_name("v12h-disappeared-development-facts.jsonl")
CONSTRAINTS = json.loads(Path(__file__).with_name("webview-type-constraints.json").read_text())["development_labels"]
SHA = "c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941"

d = json.loads(REPORT.read_text())
by_activity = {a["activity"]: a["facts"] for a in d["activities"]}
rows = []

def base(activity, webview, group, status, evidence, chain):
    return {"activity": activity, "webview": webview, "registration_name": None,
            "value": None, "signature": None, "normalized_signature": None,
            "normalized_api": None, "transport_signature": None,
            "router_signature": None, "resolver_signature": None,
            "selector": None, "dispatch": None, "binding_status": "pending_host_binding",
            "apk_sha256": SHA, "app_version": "7.9.50", "validator": "GPT-5.6 Sol",
            "development_sample": True, "blind_status": "non_blind_v12a_report_selected",
            "evidence_status": "component_source_confirmed_host_binding_pending",
            "component_evidence_status": status,
            "host_binding_status": "pending_branch_reachability",
            "positive_acceptance": False, "capability_group": group,
            "evidence": evidence, "binding_chain": chain}

capture_chain = [
 "ShellActivity.getVideoPlayerViewContainer returns its root-fragment container or creates VideoPlayerViewContainer(this)",
 "the container can attach KkVideoDetailDarkModeFragment; its mixed controller preserves fragment.getContext() into relatedvideo.i and TLVideoCompleteView",
 "TLVideoCompleteView constructs sharedialog.n(Context), whose k2 superclass stores that Context, but its proven click listeners pass only 3/4",
 "UNPROVEN: no Shell execution/selector edge reaches Lcom/tencent/news/share/k2;->ˆʾ(Lcom/tencent/news/model/pojo/Item;IZ)V case 107",
 "Lcom/tencent/news/share/utils/CaptureFullContentShareDialogHelperKt;->ʾʾ(Landroid/content/Context;Lcom/tencent/news/share/model/ShareData;I)V resolves service b",
 "Lcom/tencent/news/ui/capture/services/CaptureWebViewService;->ʻ(Landroid/content/Context;Lcom/tencent/news/model/pojo/Item;Ljava/lang/String;Lcom/tencent/news/core/detail/model/IDetailModel;ILkotlin/jvm/functions/p;)V creates CaptureDetailPage with that Context and calls setData",
 "CaptureDetailPage.setData calls initWebView; NewsWebViewFactory.acquire returns the stored NewsWebView",
 "CaptureFullContentShareDialogHelperKt.ــ callback adds CaptureDetailPage as main content and later displays the capture image UI"
]
kk_capture_chain = [
 "KkAlbumDarkModeActivity inherits onCreate that installs VideoPlayerViewContainer(this) and its KkVideoDetailDarkModeFragment",
 "fragment -> mixed.p -> relatedvideo.i -> attached TLVideoCompleteView preserves fragment.getContext()",
 "TLVideoCompleteView constructs sharedialog.n(Context), whose k2 superclass stores that Context; proven listeners pass 3/4",
 "UNPROVEN: no source edge reaches k2 case 107",
 *capture_chain[4:]
]
capture_ev = "CaptureDetailPage.java:214-229,350-395; CaptureWebViewService.java:28-37; CaptureFullContentShareDialogHelperKt.java:359-397,611-640; k2.java:2740-2744; NewsDetailFloatViewJavaScriptServiceImpl.java:31-40"

# Representative of the five/seven-host common CaptureDetailPage loss: all 15
# settings and every installed client override from the concrete install sites.
A = "com.tencent.news.basebiz.ShellActivity"
feedback_bridge_done = False
for f in by_activity[A]:
    is_setting = f["kind"] == "setting" and any(x in f["site"] for x in
        ("CaptureDetailPage", "NewsWebView;->initView", "WebFontScaleKt", "WebView4File", "bugfix/i"))
    is_client = f["kind"] == "callback" and "CaptureDetailPage;->initWebView" in f["site"]
    if not (is_setting or is_client): continue
    common = base(A, "capture_detail_page.webView", "capture_page_settings_clients",
                  "source_confirmed_dex_verified", capture_ev, capture_chain)
    if is_setting:
        val = f.get("value")
        dynamic = val in ("unknown", None)
        common.update(kind="setting", name=f["name"], value=None if dynamic else val,
                      implementation=f["site"].split(";->",1)[0][1:].replace("/","."),
                      normalized_api=f.get("api"), site=f["site"],
                      value_kind="dynamic" if dynamic else ("enum" if val == "HIGH" else "literal"),
                      source_expression="DEX expression at " + f["site"] if dynamic else str(val).lower())
        rows.append(common)
    else:
        for m in f.get("members", []):
            r = dict(common)
            r.update(kind="callback", name=m["name"], implementation=m["signature"].split(";->",1)[0][1:].replace("/","."),
                     signature=m.get("display"), normalized_signature=m["signature"],
                     normalized_api=f.get("api"), site=f["site"])
            rows.append(r)

# Representative of the KK-only CaptureDetailPage native bridge.  The source
# runtime object is j; discard the report's parallel interface-typed unknown.
A = "com.tencent.news.kkvideo.detail.KkAlbumDarkModeActivity"
for f in by_activity[A]:
    if f["kind"] == "bridge" and "CaptureDetailPage;->loadUrl" in f["site"] and f.get("implementation") == "com.tencent.news.ui.visitmode.webview.j":
        common = base(A, "capture_detail_page.webView", "capture_page_native_bridge",
                      "source_confirmed_dex_verified", capture_ev + "; j.java:24-77,222-307", kk_capture_chain)
        common.update(kind="bridge", name="TencentNews", registration_name="TencentNews",
                      implementation=f["implementation"], normalized_api=f["api"], site=f["site"],
                      value="this.floatCardInterface")
        rows.append(common)
        for m in f.get("members", []):
            r=dict(common); sig=m["signature"]
            r.update(kind="bridge_method", name=sig.split(";->",1)[1].split("(",1)[0],
                     normalized_signature=sig, signature=m.get("display"), value=None)
            rows.append(r)
        break

# Representative of the KK-only feedback Dialog group. Keep the direct client
# plus the native transport registration/method; reflective endpoint expansion
# is deliberately not copied from v12a because it mixed declared helper methods
# with callable message selectors.
feedback_chain = [
 "KkAlbumDarkModeActivity -> installed fragment -> mixed.p -> relatedvideo.i -> attached TLVideoCompleteView preserves Activity Context into sharedialog.n/k2",
 "the proven TLVideoCompleteView listeners pass 3/4; UNPROVEN: no source edge dispatches option 1021",
 "Lcom/tencent/news/video/t;->ʻ(Landroid/content/Context;ZLcom/tencent/news/model/pojo/Item;I)V receives the same Activity Context",
 "VideoErrorFeedbackDialog.Companion creates the dialog and PopManager enqueues/displays it",
 "VideoErrorFeedbackDialog.initViews finds field webView from its attached root layout",
 "the same field is passed to new WebViewBridge, assigned VideoErrorFeedbackDialog$c, and loadUrl is invoked"
]
feedback_ev = "k2.java:2760-2770 (case 1021); VideoFeedbackService.java:24-31; VideoErrorFeedbackDialog.java:69-98,291-315"
for f in by_activity[A]:
    if f["kind"] == "callback" and "VideoErrorFeedbackDialog;->initViews" in f["site"]:
        common=base(A,"video_error_feedback_dialog.webView","feedback_dialog_client_bridge",
                    "source_confirmed_dex_verified",feedback_ev,feedback_chain)
        for m in f.get("members",[]):
            r=dict(common); sig=m["signature"]
            r.update(kind="callback",name=m.get("name") or sig.split(";->",1)[1].split("(",1)[0],
                     implementation=sig.split(";->",1)[0][1:].replace("/","."),
                     signature=m.get("display"),normalized_signature=sig,
                     normalized_api=f.get("api"),site=f["site"])
            rows.append(r)
    if not feedback_bridge_done and f["kind"] == "bridge" and "WebViewBridge;->addJavascriptInterface" in f["site"] and f.get("implementation") == "com.tencent.news.webview.BridgeInterface" and f.get("webview",{}).get("type") == "com.tencent.smtt.sdk.WebView":
        common=base(A,"video_error_feedback_dialog.webView","feedback_dialog_client_bridge",
                    "source_confirmed_dex_verified",feedback_ev + "; WebViewBridge.java; BridgeInterface.java",feedback_chain)
        common.update(kind="bridge",name=f["registration_name"],registration_name=f["registration_name"],
                      implementation=f["implementation"],normalized_api=f["api"],site=f["site"],value="BridgeInterface bound to H5DialogScriptInterface")
        rows.append(common)
        for m in f.get("members",[]):
            r=dict(common); sig=m["signature"]
            r.update(kind="bridge_method",name=sig.split(";->",1)[1].split("(",1)[0],
                     normalized_signature=sig,signature=m.get("display"),value=None)
            rows.append(r)
        feedback_bridge_done = True

# Stable order and exact duplicate suppression.
seen=set(); final=[]
for r in rows:
    k=(r["activity"],r["webview"],r["kind"],r["name"],r.get("normalized_signature"),r.get("normalized_api"),r.get("site"))
    if k not in seen:
        seen.add(k)
        c = CONSTRAINTS.get(r["webview"])
        if c and c.get("status", "").startswith("source_confirmed"):
            r["webview_constraint"] = {"types": c["types"]}
        final.append(r)
OUT.write_text("".join(json.dumps(r,ensure_ascii=False,separators=(",",":"))+"\n" for r in final))
print(json.dumps({"rows":len(final),"by_group":{g:sum(x["capability_group"]==g for x in final) for g in sorted({x["capability_group"] for x in final})}},ensure_ascii=False))
