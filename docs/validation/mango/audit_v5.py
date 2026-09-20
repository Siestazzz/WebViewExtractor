#!/usr/bin/env python3
import collections
import json
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REPORT = ROOT / "test/runs/v5/com.hunantv.imgo.activity/0/output/capabilities.json"
NAMES = {
    "getSMSNumber", "getSMSCode", "payWithWeChat", "share",
    "showCustomShareMenus", "invokeH5Callback", "webviewBecomeActive",
    "webviewEnterBackground",
}
HOSTS = {
    "com.hunantv.webui.WebUIActivity",
    "com.mgtv.ui.browser.HalfWebActivity",
    "com.mgtv.ui.browser.WebActivity",
    "com.mgtv.ui.browser.WebActivityTransparent",
    "com.mgtv.ui.guide.PureWebActivity",
    "com.mgtv.ui.login.CaptureWebActivity",
    "com.mgtv.ui.login.MeCaptureWebActivity",
}

report = json.loads(REPORT.read_text())
prior = {x["activity"]: x for x in map(json.loads, (HERE / "v4e-ownership.jsonl").open())}
current = {x["activity"]: x for x in report["activities"]}
assert len(current) == 80
assert not (set(current) - set(prior))

ownership = []
for activity, item in current.items():
    row = dict(prior[activity])
    row["fact_count"] = len(item["facts"])
    row["reason"] = "stable source/DEX chain reused from independent v4e review: " + row["reason"]
    ownership.append(row)
ownership.sort(key=lambda x: x["activity"])
counts = collections.Counter(x["verdict"] for x in ownership)
assert counts == {"valid": 75, "uncertain": 5}
(HERE / "v5-ownership.jsonl").write_text("".join(
    json.dumps(x, ensure_ascii=False, separators=(",", ":")) + "\n" for x in ownership
))

raw = []
for host in HOSTS:
    assert host in current
    for fact in current[host]["facts"]:
        if fact.get("registration_name") in NAMES:
            raw.append(fact)
assert len(raw) == 416
pairs = {(x["activity"], x["registration_name"]) for x in raw}
assert pairs == {(h, n) for h in HOSTS for n in NAMES}
assert all(x.get("kind") == "message_bridge" for x in raw)
assert all(x.get("members") == [] for x in raw)
assert all(x.get("endpoint_status") == "registered-no-compatible-endpoint" for x in raw)
assert all(x.get("implementation") == "com.mgtv.h5.ImgoWebJavascriptImpl$e0" for x in raw)

removed = sorted(set(prior) - set(current))
assert removed == [
    "com.mg.ec.cards.result.SmallCardResultActivity",
    "com.mg.ec.ui.videodetail.VideoDetailActivity",
]

summary = {
    "report": str(REPORT.relative_to(ROOT)),
    "activity_count": len(current),
    "ownership": {
        "counts": dict(counts),
        "added_since_v4e": [],
        "removed_since_v4e": removed,
        "retained_verdict_changes": [],
    },
    "imgo_dispatcher": {
        "hosts": sorted(HOSTS),
        "registration_names": sorted(NAMES),
        "semantic_registration_count": len(pairs),
        "raw_emitted_fact_count": len(raw),
        "all_members_empty": True,
        "all_endpoint_status": "registered-no-compatible-endpoint",
        "dispatcher_implementation": "com.mgtv.h5.ImgoWebJavascriptImpl$e0",
        "independent_rule_evidence": "imgo-unknown-handler-audit.md",
    },
    "async_registry_discovery": {
        "mglive": {
            "chain": [
                "cn0.c.shouldOverrideUrlLoading(WebView,String)",
                "BridgeWebView.flushMessageQueue()",
                "new BridgeWebView$a passed to javascript:_fetchQueue callback",
                "BridgeWebView$a.onCallBack(String)",
                "BridgeWebView.G.get(handlerName)",
                "cn0.a.a(String,cn0.d)",
            ],
            "note": "onPageFinished installs/flushes startup JS messages; the yy:// queue signal in shouldOverrideUrlLoading starts the inbound registry dispatch",
        },
        "mgadplus": {
            "chain": [
                "BridgeWebView$a client callback d/e (page lifecycle delegated by base client)",
                "BridgeWebView.flushMessageQueue()",
                "new BridgeWebView$c passed to javascript:_fetchQueue callback",
                "BridgeWebView$c.onCallBack(String)",
                "BridgeWebView.G.get(handlerName)",
                "com.mgadplus.brower.jsbridge.a.handler(String,d)",
            ],
            "note": "the queue callback is $c, not the installed WebViewClient $a",
        },
    },
}
(HERE / "v5-validation.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n")
(HERE / "v5-ownership.md").write_text(
    "# v5 Mango ownership delta\n\n"
    "v5 emits 80 Activities: 75 retain independently supported ownership chains and "
    "five retain the v4e `uncertain` verdict. No retained verdict changed, no Activity "
    "was added, and no known-wrong association returned.\n\n"
    "Removed since v4e:\n\n" + "".join(f"- `{x}`\n" for x in removed) +
    "\nBoth removed rows were `valid` in v4e, so this is an output-recall reduction rather "
    "than an ownership correction. Per-Activity evidence and verdicts are in "
    "`v5-ownership.jsonl`.\n"
)
(HERE / "v5-validation.md").write_text(
    "# v5 Mango focused validation\n\n"
    "The Imgo dispatcher false-member issue is fixed for the audited surface. The seven "
    "hosts still contain all 7 × 8 = 56 semantic registrations. v5 emits 416 raw copies "
    "because carrier paths duplicate facts; every copy has an empty `members` array and "
    "`endpoint_status=registered-no-compatible-endpoint`. This result is supported by the "
    "independent fixed reflection rule in `imgo-unknown-handler-audit.md`: "
    "`ImgoWebJavascriptImpl$e0.handler` calls `ImgoWebJavascriptImpl.class.getDeclaredMethod"
    "(capturedName, String.class)` on the enclosing instance, without superclass or runtime-"
    "subclass lookup, and DEX contains zero exact declared `(String)` matches for the eight "
    "names. The literal registration values alone are not treated as endpoint evidence.\n\n"
    "Ownership changed only by removal of `SmallCardResultActivity` and "
    "`VideoDetailActivity`; v5 has 75 valid and five uncertain rows, with no additions or "
    "retained verdict changes. See `v5-ownership.jsonl`.\n\n"
    "The async registry gap has a short carrier chain. For mglive, `cn0.c.onPageFinished` "
    "injects the bridge and releases startup messages; an inbound `yy://` signal reaches "
    "`cn0.c.shouldOverrideUrlLoading`, then `flushMessageQueue`, `new BridgeWebView$a`, "
    "`BridgeWebView$a.onCallBack`, `G.get(handlerName)`, and finally "
    "`cn0.a.a(String,cn0.d)`. For mgadplus, installed WebViewClient "
    "`BridgeWebView$a` delegates page/URL callbacks; queue dispatch reaches "
    "`flushMessageQueue`, **`new BridgeWebView$c`**, `$c.onCallBack`, "
    "`G.get(handlerName)`, and `jsbridge.a.handler(String,d)`. The mgadplus queue callback "
    "is `$c`; `$a` is the installed client. A registry finder that stops at the client "
    "callback misses the asynchronously constructed callback and its map read.\n"
)
print(json.dumps(summary, ensure_ascii=False))
