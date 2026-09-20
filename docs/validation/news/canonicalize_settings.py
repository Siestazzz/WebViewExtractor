#!/usr/bin/env python3
"""Create typed setting values without changing the preserved expanded facts."""
from pathlib import Path
import json
import re

BASE = Path("docs/validation/news")
SOURCE = BASE / "facts_expanded.jsonl"
OUTPUT = BASE / "canonical-facts.jsonl"

# Source audit: the installed derived Chrome clients override this method and
# do not invoke super, so these broad component-group facts are not executable
# callback implementations for the bound Activities.
SHADOWED_CALLBACKS = {
    (activity, "Lcom/tencent/news/webview/jsbridge/JavascriptBridgeChromeClient;->onProgressChanged(Lcom/tencent/smtt/sdk/WebView;I)V")
    for activity in (
        "com.tencent.news.webview.CustomWebBrowserForItemActivity",
        "com.tencent.news.tad.business.ui.activity.CustomWebGameForItemActivity",
        "com.tencent.news.webview.TencentVideoWebActivity",
        "com.tencent.news.webview.HalfTencentVideoWebActivity",
        "com.tencent.news.webview.WebDetailActivity",
    )
}

INTEGER = re.compile(r"[-+]?\d+")
ENUM = re.compile(r"(?:[A-Za-z_$][\w$]*\.)+[A-Z][A-Z0-9_]*")


def canonical_value(expression):
    expression = expression.strip()
    if expression == "true":
        return "literal", True
    if expression == "false":
        return "literal", False
    if INTEGER.fullmatch(expression):
        return "literal", int(expression)
    if expression.startswith('"') and expression.endswith('"'):
        try:
            value = json.loads(expression)
        except json.JSONDecodeError:
            pass
        else:
            if isinstance(value, str):
                return "literal", value
    if ENUM.fullmatch(expression):
        return "enum", expression
    return "dynamic", None


with SOURCE.open() as src, OUTPUT.open("w") as dst:
    for line in src:
        row = json.loads(line)
        if (row.get("activity"), row.get("normalized_signature")) in SHADOWED_CALLBACKS:
            continue
        if row.get("kind") == "setting":
            expression = row.get("value")
            if not isinstance(expression, str):
                raise ValueError(f"setting lacks source expression: {row}")
            row["source_expression"] = expression
            row["value_kind"], row["value"] = canonical_value(expression)
        dst.write(json.dumps(row, ensure_ascii=False, separators=(",", ":")) + "\n")
