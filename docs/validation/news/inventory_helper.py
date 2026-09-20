#!/usr/bin/env python3
"""Independent source inventory helper; never reads extractor output."""
from pathlib import Path
import re

ROOT = Path("test/decompiled/com.tencent.news")
SRC = ROOT / "sources"
MANIFEST = ROOT / "resources/AndroidManifest.xml"

classes = {}
simple = {}
for path in SRC.rglob("*.java"):
    text = path.read_text(errors="replace")
    pkg = re.search(r"^package\s+([\w.]+);", text, re.M)
    decl = re.search(r"^(?:public\s+)?(?:abstract\s+|final\s+)?class\s+(\w+)(?:\s+extends\s+([\w.]+))?", text, re.M)
    if not pkg or not decl:
        continue
    fqcn = pkg.group(1) + "." + decl.group(1)
    classes[fqcn] = {"path": str(path), "parent": decl.group(2), "text": text}
    simple.setdefault(decl.group(1), []).append(fqcn)

def resolve_parent(fqcn):
    parent = classes[fqcn]["parent"]
    if not parent:
        return None
    if "." in parent and parent in classes:
        return parent
    own_pkg = fqcn.rsplit(".", 1)[0] + "." + parent
    if own_pkg in classes:
        return own_pkg
    imports = re.findall(r"^import\s+([\w.]+);", classes[fqcn]["text"], re.M)
    for item in imports:
        if item.endswith("." + parent) and item in classes:
            return item
    matches = simple.get(parent, [])
    return matches[0] if len(matches) == 1 else None

for fqcn in classes:
    classes[fqcn]["resolved_parent"] = resolve_parent(fqcn)

manifest = MANIFEST.read_text(errors="replace")
activities = re.findall(r"<activity\b[^>]*?android:name=\"([^\"]+)\"", manifest, re.S)
signals = re.compile(r"addJavascriptInterface|setJavaScriptEnabled|setDomStorageEnabled|setMixedContentMode|setAllowFileAccess|setWebViewClient|setWebChromeClient")
for activity in activities:
    if activity.startswith("."):
        activity = "com.tencent.news" + activity
    cur, chain, hits = activity, [], []
    while cur and cur not in chain:
        chain.append(cur)
        info = classes.get(cur)
        if not info:
            break
        for lineno, line in enumerate(info["text"].splitlines(), 1):
            if signals.search(line):
                hits.append(f"{info['path']}:{lineno}:{line.strip()}")
        cur = info["resolved_parent"]
    if hits:
        print("ACTIVITY\t" + activity)
        print("CHAIN\t" + " -> ".join(chain))
        for hit in hits:
            print("HIT\t" + hit)

print("ALL_SOURCE_ACTIVITIES")
for activity in sorted(x for x in classes if x.endswith("Activity")):
    cur, chain, hits = activity, [], []
    while cur and cur not in chain:
        chain.append(cur)
        info = classes.get(cur)
        if not info:
            break
        for lineno, line in enumerate(info["text"].splitlines(), 1):
            if signals.search(line):
                hits.append(f"{info['path']}:{lineno}:{line.strip()}")
        cur = info["resolved_parent"]
    if hits:
        print("SOURCE_ACTIVITY\t" + activity)
        print("CHAIN\t" + " -> ".join(chain))
        for hit in hits:
            print("HIT\t" + hit)
