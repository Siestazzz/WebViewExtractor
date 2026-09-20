#!/usr/bin/env python3
"""Fail closed on registration/endpoint conflation in Mango bridge truth."""
import json
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
rows = [json.loads(line) for line in (HERE / "canonical-facts.jsonl").read_text().splitlines()]
bridges = [row for row in rows if row.get("kind") == "bridge"]

targets = {row.get("bridge_method") for row in bridges if row.get("bridge_method")}
found = set()
with (ROOT / "test/runs/symbols/mango.jsonl").open(errors="replace") as stream:
    for line in stream:
        obj = json.loads(line)
        for method in obj.get("methods", ()):
            signature = method.get("signature", "").replace("-\u003e", "->")
            if signature in targets:
                found.add(signature)
        if found == targets:
            break
assert found == targets, sorted(targets - found)

fake = "Lcom/hunantv/imgo/h5/callback/ImgoWebJavascriptInterface;->registerHandler(Ljava/lang/String;Lcom/hunantv/imgo/h5/ImgoWebView;)V"
assert all(row.get("normalized_signature") != fake for row in bridges)
assert all("registration_name" in row for row in bridges)
for row in bridges:
    endpoint = row.get("bridge_method", "")
    if endpoint:
        assert row.get("normalized_signature") == endpoint, row
        assert row.get("implementation"), row
        assert row.get("symbol_status") == "confirmed", row
    if row.get("binding_status") in {"registered-target-unknown", "registered-no-compatible-endpoint"}:
        assert not row.get("bridge_method"), row
        assert not row.get("normalized_signature"), row
        assert not row.get("signature"), row
        assert row.get("implementation") is None, row

imgo_hosts = {
    "com.mgtv.ui.browser.HalfWebActivity", "com.mgtv.ui.login.MeCaptureWebActivity",
    "com.mgtv.ui.guide.PureWebActivity", "com.mgtv.ui.browser.WebActivity",
    "com.mgtv.ui.browser.WebActivityTransparent", "com.mgtv.ui.login.CaptureWebActivity",
    "com.hunantv.webui.WebUIActivity",
}
imgo = [row for row in bridges if row["activity"] in imgo_hosts and row["value"] == "registered"]
assert len(imgo) == 966, len(imgo)
assert Counter(row["binding_status"] for row in imgo) == {"confirmed": 910, "registered-no-compatible-endpoint": 56}

ccb = [row for row in bridges if row["activity"] == "com.ccb.ccbnetpay.H5PayActivity"]
assert len(ccb) == 3
assert {row["registration_name"] for row in ccb} == {"javaObj"}
assert {row["value"] for row in ccb} == {"object_registered", "sdkCallBack", "showFinish"}

print(json.dumps({
    "bridge_rows": len(bridges), "dex_endpoints": len(targets),
    "imgo_registrations": len(imgo), "imgo_confirmed": 910,
    "imgo_no_compatible_endpoint_retained": 56, "ccb_rows": len(ccb),
}, sort_keys=True))
