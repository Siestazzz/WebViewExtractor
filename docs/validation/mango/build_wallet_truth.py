#!/usr/bin/env python3
"""Build and merge independently DEX-checked truth for the two wallet hosts."""
import json
import re
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
SRC = ROOT / "test/decompiled/com.hunantv.imgo.activity/sources"
DEX = ROOT / "test/runs/symbols/mango.jsonl"
FACTS = HERE / "facts.jsonl"
SHA = "65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5"
HOSTS = (
    "com.mgtb.money.web.ThirdWebActivity",
    "com.mgtb.money.web.ThirdFullWebActivity",
)
WEBVIEW = "com.mgtb.money.web.webview.ProgressWebView"
PREFIX = "test/decompiled/com.hunantv.imgo.activity/sources/"


def load_dex(types):
    result = {}
    with DEX.open(errors="replace") as stream:
        for line in stream:
            obj = json.loads(line)
            if obj.get("type") in types:
                result[obj["type"]] = obj
                if len(result) == len(types):
                    break
    missing = types - result.keys()
    if missing:
        raise SystemExit(f"missing DEX owners: {sorted(missing)}")
    return result


def line_for(relative, method):
    lines = (SRC / relative).read_text(errors="replace").splitlines()
    pattern = re.compile(r"\b" + re.escape(method) + r"\s*\(")
    candidates = [i for i, line in enumerate(lines, 1) if pattern.search(line)]
    if not candidates:
        raise SystemExit(f"source declaration not found: {relative}::{method}")
    return candidates[0]


def row(host, kind, name, value, implementation, signature, normalized, evidence,
        value_kind="", source_expression=""):
    result = {
        "activity": host,
        "webview": WEBVIEW,
        "kind": kind,
        "name": name,
        "value": value,
        "implementation": implementation,
        "signature": signature,
        "normalized_signature": normalized,
        "normalized_api": normalized if kind == "setting" else (
            "WebView message bridge registration" if kind == "bridge" else name
        ),
        "bridge_method": normalized if kind == "bridge" else "",
        "evidence": PREFIX + evidence,
        "binding_status": "confirmed",
        "apk_sha256": SHA,
        "value_kind": value_kind,
        "source_expression": source_expression,
        "symbol_status": "external_api" if normalized.startswith("Landroid/") else "confirmed",
    }
    if kind == "bridge":
        result["registration_name"] = name
    return result


owners = {
    "Lcom/mgtb/money/web/dsbridge/DWebView$InnerJavascriptInterface;",
    "Lcom/mgtb/money/web/dsbridge/DWebView$1;",
    "Lcom/mgtb/money/web/webview/DefaultWebBridgeAPI;",
    "Lcom/mgtb/money/web/webview/WebClientImpl$ProgressWebChromeClient;",
    "Lcom/mgtb/money/web/webview/WebClientImpl$a;",
    "Lcom/mgtb/money/web/WebFragment$b;",
    "Lcom/mgtb/money/web/WebFragment$c;",
}
dex = load_dex(owners)

# Each tuple is an observed invocation, including later overrides of an earlier value.
settings = (
    ("setDomStorageEnabled", True, "Z", 818, "DWebView.java"),
    ("setMixedContentMode", 0, "I", 820, "DWebView.java"),
    ("setAllowFileAccess", False, "Z", 821, "DWebView.java"),
    ("setAppCacheEnabled", False, "Z", 822, "DWebView.java"),
    ("setCacheMode", 2, "I", 823, "DWebView.java"),
    ("setJavaScriptEnabled", True, "Z", 824, "DWebView.java"),
    ("setLoadWithOverviewMode", True, "Z", 825, "DWebView.java"),
    ("setSavePassword", False, "Z", 826, "DWebView.java"),
    ("setAppCachePath", None, "Ljava/lang/String;", 827, "DWebView.java"),
    ("setUseWideViewPort", True, "Z", 828, "DWebView.java"),
    ("setJavaScriptEnabled", True, "Z", 262, "ProgressWebView.java"),
    ("setJavaScriptCanOpenWindowsAutomatically", True, "Z", 263, "ProgressWebView.java"),
    ("setAppCacheEnabled", True, "Z", 265, "ProgressWebView.java"),
    ("setSavePassword", False, "Z", 266, "ProgressWebView.java"),
    ("setDomStorageEnabled", True, "Z", 267, "ProgressWebView.java"),
    ("setDatabaseEnabled", True, "Z", 268, "ProgressWebView.java"),
    ("setTextZoom", 100, "I", 269, "ProgressWebView.java"),
    ("setMixedContentMode", 0, "I", 270, "ProgressWebView.java"),
)

generated = []
for host in HOSTS:
    for name, value, arg, source_line, source_file in settings:
        expression = "this.APP_CACHE_DIRNAME" if name == "setAppCachePath" else str(value).lower()
        # facts.jsonl stores source expressions; finalize_canonical.py converts
        # literal strings to typed JSON values.
        emitted_value = expression
        generated.append(row(
            host, "setting", name, emitted_value, "com.mgtb.money.web.dsbridge.DWebView#d" if source_file == "DWebView.java" else "com.mgtb.money.web.webview.ProgressWebView#initProgress",
            f"Landroid/webkit/WebSettings;->{name}({arg})V",
            f"Landroid/webkit/WebSettings;->{name}({arg})V",
            f"com/mgtb/money/web/webview/{source_file}:{source_line}" if source_file == "ProgressWebView.java" else f"com/mgtb/money/web/dsbridge/{source_file}:{source_line}",
            "dynamic" if value is None else "literal", expression,
        ))

    callback_specs = (
        ("Lcom/mgtb/money/web/webview/WebClientImpl$ProgressWebChromeClient;", "com/mgtb/money/web/webview/WebClientImpl.java:80-419", "WebChromeClient"),
        ("Lcom/mgtb/money/web/webview/WebClientImpl$a;", "com/mgtb/money/web/webview/WebClientImpl.java:421-654", "WebViewClient"),
        ("Lcom/mgtb/money/web/WebFragment$b;", "com/mgtb/money/web/WebFragment.java:103-123,222", "delegated WebChromeClient"),
        ("Lcom/mgtb/money/web/WebFragment$c;", "com/mgtb/money/web/WebFragment.java:125-152,223", "delegated WebViewClient"),
    )
    for owner, evidence, client_name in callback_specs:
        implementation = owner[1:-1].replace("/", ".")
        for method in dex[owner]["methods"]:
            signature = method["signature"].replace("-\u003e", "->")
            if method["name"] == "<init>" or method["name"] in {"b", "d"}:
                continue
            generated.append(row(host, "callback", f"{client_name}.{method['name']}", implementation,
                                 implementation, signature, signature, evidence))

    bridge_specs = (
        ("Lcom/mgtb/money/web/dsbridge/DWebView$InnerJavascriptInterface;", "_dsbridge", {"call"}, "com/mgtb/money/web/dsbridge/DWebView.java:112,831"),
        ("Lcom/mgtb/money/web/dsbridge/DWebView$1;", "_dsb", None, "com/mgtb/money/web/dsbridge/DWebView.java:625-742"),
        ("Lcom/mgtb/money/web/webview/DefaultWebBridgeAPI;", "<default>", None, "com/mgtb/money/web/webview/ProgressWebView.java:271-273 -> com/mgtb/money/web/webview/DefaultWebBridgeAPI.java"),
    )
    for owner, namespace, names, evidence in bridge_specs:
        implementation = owner[1:-1].replace("/", ".")
        for method in dex[owner]["methods"]:
            annotations = set(method.get("annotations", ()))
            if "Landroid/webkit/JavascriptInterface;" not in annotations:
                continue
            if names is not None and method["name"] not in names:
                continue
            signature = method["signature"].replace("-\u003e", "->")
            item=row(host, "bridge", namespace, method["name"], implementation,
                     signature, signature, evidence)
            if namespace == "_dsbridge":
                item["transport_registration_name"]="_dsbridge"
                item["registration_name"]="_dsbridge"
                item["namespace"]=None
            else:
                item["registration_name"]="" if namespace == "<default>" else namespace
                item["namespace"]=item["registration_name"]
            generated.append(item)

old = [json.loads(line) for line in FACTS.read_text().splitlines()]
# Preserve the already-audited activity bindings and every unrelated or failed row.
old = [r for r in old if r.get("activity") not in HOSTS or r.get("kind") == "activity_binding"]
with FACTS.open("w") as stream:
    for item in old + generated:
        stream.write(json.dumps(item, ensure_ascii=False, separators=(",", ":")) + "\n")

from collections import Counter
print(json.dumps({"generated": len(generated), "per_host": len(generated) // 2,
                  "kinds": Counter(r["kind"] for r in generated)}, default=dict, sort_keys=True))
