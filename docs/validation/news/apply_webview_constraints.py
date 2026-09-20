#!/usr/bin/env python3
"""Replay source-authored WebView constraints onto a JSONL fact file.

Writes a separate output and never overwrites canonical input. Labels absent from
the mapping remain unchanged and are reported as unresolved.
"""
import argparse, json
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
ap = argparse.ArgumentParser()
ap.add_argument("input", type=Path)
ap.add_argument("output", type=Path)
ap.add_argument("--mapping", type=Path, default=HERE / "webview-type-constraints.json")
args = ap.parse_args()
if args.input.resolve() == args.output.resolve():
    raise SystemExit("refusing in-place overwrite")
m = json.loads(args.mapping.read_text())
labels = dict(m["canonical_labels"])
labels.update(m.get("development_labels", {}))
counts = Counter(); unresolved = Counter()
out = []
for line in args.input.read_text().splitlines():
    if not line.strip(): continue
    row = json.loads(line)
    label = row.get("webview")
    entry = labels.get(label)
    if entry and entry.get("status", "").startswith("source_confirmed"):
        row["webview_constraint"] = {"types": entry["types"]}
        counts[label] += 1
    else:
        unresolved[str(label)] += 1
    out.append(row)
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text("".join(json.dumps(x, ensure_ascii=False, separators=(",", ":")) + "\n" for x in out))
print(json.dumps({"input_rows":len(out), "constrained_rows":sum(counts.values()),
                  "constrained_by_label":dict(counts), "unresolved_by_label":dict(unresolved)},
                 ensure_ascii=False, indent=2))
