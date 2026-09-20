#!/usr/bin/env python3
"""Strictly verify published method facts against the independent DEX symbol export."""
from pathlib import Path
import json

FACTS=Path("docs/validation/news/facts_expanded.jsonl")
SYMBOLS=Path("test/runs/symbols/news.jsonl")
OUT=Path("docs/validation/news/symbol-verification.jsonl")

facts=[json.loads(x) for x in FACTS.read_text().splitlines()]
targets={x["normalized_signature"] for x in facts if x.get("kind") in ("callback","bridge_method","message_handler") and x.get("normalized_signature")}
owners={x.split("->",1)[0] for x in targets}
found=set(); found_owners=set()
with SYMBOLS.open() as fh:
    for line in fh:
        row=json.loads(line)
        if row.get("type") not in owners:
            continue
        found_owners.add(row["type"])
        for method in row.get("methods",[]):
            sig=method.get("signature")
            if sig in targets: found.add(sig)

with OUT.open("w") as fh:
    for sig in sorted(targets):
        owner=sig.split("->",1)[0]
        fh.write(json.dumps({"normalized_signature":sig,"owner_present":owner in found_owners,"method_present":sig in found},separators=(",",":"))+"\n")
missing=targets-found
if missing:
    raise SystemExit(f"missing {len(missing)} of {len(targets)} signatures")
