#!/usr/bin/env python3
"""Build source-only WebView entrypoint inventory. Never reads extractor output."""
from pathlib import Path
import json, re

SRC = Path("test/decompiled/com.tencent.news/sources")
OUT = Path("docs/validation/news/entrypoint_inventory.jsonl")

call_rx = re.compile(r"\.(addJavascriptInterface|setWebViewClient|setWebChromeClient|setJavaScriptEnabled|setDomStorageEnabled|setDatabaseEnabled|setMixedContentMode|setAllowFileAccess|setAllowFileAccessFromFileURLs|setAllowUniversalAccessFromFileURLs|setJavaScriptCanOpenWindowsAutomatically|setSupportMultipleWindows|setGeolocationEnabled|setCacheMode|setSafeBrowsingEnabled)\s*\((.*)")
settings_rx = re.compile(r"(?:getSettings\(\)|\bsettings)\.(set\w+)\s*\((.*)")
class_rx = re.compile(r"\bclass\s+(\w+)\s+extends\s+([\w.]+)")
method_rx = re.compile(r"\b(public|protected)\s+(?:(?:final|static|synchronized)\s+)*(?:[\w.$<>?\[\], ]+)\s+(\w+)\s*\(([^)]*)\)")

rows = []
def clean_args(value):
    value=value.strip()
    return value[:-2] if value.endswith(");") else value
for path in SRC.rglob("*.java"):
    text = path.read_text(errors="replace")
    rel = str(path)
    pkg_m = re.search(r"^package\s+([\w.]+);", text, re.M)
    pkg = pkg_m.group(1) if pkg_m else ""
    lines = text.splitlines()
    top_m = re.search(r"^(?:public\s+)?(?:abstract\s+|final\s+)?class\s+(\w+)", text, re.M)
    owner = (pkg + "." + top_m.group(1)) if top_m else pkg
    for lineno, line in enumerate(lines, 1):
        m = call_rx.search(line)
        if m:
            rows.append({"record":"call","owner":owner,"operation":m.group(1),"arguments":clean_args(m.group(2)),"evidence":f"{rel}:{lineno}"})
        sm = settings_rx.search(line)
        if sm and (not m or sm.group(1) != m.group(1)):
            rows.append({"record":"call","owner":owner,"operation":sm.group(1),"arguments":clean_args(sm.group(2)),"evidence":f"{rel}:{lineno}"})
    # Annotation surfaces, requiring the next declaration to be a public method.
    for i, line in enumerate(lines):
        if "@JavascriptInterface" not in line and "@AdJavascriptInterface" not in line:
            continue
        for j in range(i + 1, min(i + 8, len(lines))):
            stripped = lines[j].strip()
            if not stripped or stripped.startswith("@") or stripped.startswith("/*") or stripped.startswith("*"):
                continue
            mm = method_rx.search(stripped)
            if mm:
                rows.append({"record":"javascript_method","owner":owner,"signature":stripped.split("{")[0].strip(),"evidence":f"{rel}:{j+1}"})
            break
    # Client subclasses plus every @Override public/protected declaration in their brace range.
    for cm in class_rx.finditer(text):
        base = cm.group(2)
        if not (base.endswith("WebViewClient") or "ChromeClient" in base or base.endswith("CustomWebViewClient") or base == "ht" or base == "com.tencent.news.tad.business.ui.activity.a"):
            continue
        brace = text.find("{", cm.end())
        if brace < 0:
            continue
        depth, end = 0, brace
        for pos in range(brace, len(text)):
            if text[pos] == "{": depth += 1
            elif text[pos] == "}":
                depth -= 1
                if depth == 0:
                    end = pos + 1
                    break
        body = text[brace:end]
        class_line = text.count("\n", 0, cm.start()) + 1
        methods, local_depth, pending_override = [], 1, False
        for raw in body[1:].splitlines():
            stripped = raw.strip()
            if local_depth == 1 and stripped.startswith("@Override"):
                pending_override = True
            elif local_depth == 1 and pending_override:
                mm = method_rx.search(stripped)
                if mm:
                    sig = stripped.split("{")[0].strip()
                    methods.append(re.sub(r"\s+", " ", sig))
                    pending_override = False
                elif stripped and not stripped.startswith("@") and not stripped.startswith("//") and not stripped.startswith("/*") and not stripped.startswith("*"):
                    pending_override = False
            local_depth += raw.count("{") - raw.count("}")
        rows.append({"record":"client_class","owner":owner,"class":cm.group(1),"base":base,"callbacks":methods,"evidence":f"{rel}:{class_line}"})

with OUT.open("w") as fh:
    for row in rows:
        fh.write(json.dumps(row, ensure_ascii=False, separators=(",", ":")) + "\n")
