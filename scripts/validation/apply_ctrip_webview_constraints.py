#!/usr/bin/env python3
"""Add source-audited WebView type constraints to a new canonical JSONL file.

This script never reads extractor output and refuses in-place writes. Unresolved
mapping entries deliberately leave facts Activity-only.
"""
import argparse
import json
from pathlib import Path


def load_jsonl(path):
    with path.open(encoding="utf-8") as stream:
        for number, line in enumerate(stream, 1):
            if line.strip():
                yield number, json.loads(line)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--mapping", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if args.input.resolve() == args.output.resolve():
        parser.error("refusing in-place canonical overwrite")

    rules = []
    for number, rule in load_jsonl(args.mapping):
        rule["_line"] = number
        if rule.get("status") == "confirmed":
            types = (rule.get("webview_constraint") or {}).get("types")
            if not types or any(not isinstance(t, str) or not t for t in types):
                raise SystemExit(f"invalid confirmed mapping line {number}")
        rules.append(rule)

    stats = {"rows": 0, "constrained": 0, "unresolved": 0, "existing_equal": 0}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8") as out:
        for number, fact in load_jsonl(args.input):
            stats["rows"] += 1
            matches = []
            for rule in rules:
                if rule.get("group_id") != fact.get("group_id"):
                    continue
                selector = rule.get("selector") or {}
                if all(fact.get(key) == value for key, value in selector.items()):
                    matches.append(rule)
            confirmed = [r for r in matches if r.get("status") == "confirmed"]
            if len(confirmed) > 1:
                raise SystemExit(f"ambiguous confirmed mappings for input line {number}")
            if confirmed:
                constraint = confirmed[0]["webview_constraint"]
                old = fact.get("webview_constraint")
                if old is not None and old != constraint:
                    raise SystemExit(f"constraint conflict at input line {number}: {old} != {constraint}")
                fact["webview_constraint"] = constraint
                stats["existing_equal" if old == constraint else "constrained"] += 1
            else:
                stats["unresolved"] += 1
            out.write(json.dumps(fact, ensure_ascii=False, separators=(",", ":")) + "\n")
    print(json.dumps(stats, sort_keys=True))


if __name__ == "__main__":
    main()
