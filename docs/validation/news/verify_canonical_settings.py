#!/usr/bin/env python3
"""Independently check typed setting semantics against source evidence."""
from pathlib import Path
import json
import re

BASE = Path("docs/validation/news")
FACTS = BASE / "canonical-facts.jsonl"
OUTPUT = BASE / "settings-value-verification.jsonl"
INTEGER = re.compile(r"[-+]?\d+")
ENUM = re.compile(r"(?:[A-Za-z_$][\w$]*\.)+[A-Z][A-Z0-9_]*")


def canonical_value(expression):
    """Independent implementation of the value contract under test."""
    expression = expression.strip()
    if expression in ("true", "false"):
        return "literal", expression == "true"
    if INTEGER.fullmatch(expression):
        return "literal", int(expression)
    if expression.startswith('"') and expression.endswith('"'):
        try:
            decoded = json.loads(expression)
        except json.JSONDecodeError:
            decoded = None
        if isinstance(decoded, str):
            return "literal", decoded
    if ENUM.fullmatch(expression):
        return "enum", expression
    return "dynamic", None


def source_line(evidence):
    location = evidence.split(";", 1)[0]
    path_text, line_text = location.rsplit(":", 1)
    path = Path(path_text)
    line_number = int(line_text)
    lines = path.read_text(errors="replace").splitlines()
    return lines[line_number - 1]


failures = []
with FACTS.open() as src, OUTPUT.open("w") as dst:
    for row_number, line in enumerate(src, 1):
        row = json.loads(line)
        if row.get("kind") != "setting":
            continue
        expression = row.get("source_expression")
        expected_kind, expected_value = canonical_value(expression)
        evidence_line = source_line(row["evidence"])
        # Entrypoint inventory records the complete argument expression. Require
        # both the API name and that exact expression on the cited source line.
        evidence_matches = (
            row["name"] in evidence_line and expression in evidence_line
        )
        semantic_matches = (
            row.get("value_kind") == expected_kind
            and row.get("value") == expected_value
            and type(row.get("value")) is type(expected_value)
        )
        dynamic_is_null = expected_kind != "dynamic" or row.get("value") is None
        result = {
            "canonical_row": row_number,
            "activity": row["activity"],
            "api": row["normalized_api"],
            "value_kind": row["value_kind"],
            "source_expression": expression,
            "evidence_expression_present": evidence_matches,
            "semantic_type_valid": semantic_matches,
            "dynamic_value_is_null": dynamic_is_null,
        }
        dst.write(json.dumps(result, ensure_ascii=False, separators=(",", ":")) + "\n")
        if not (evidence_matches and semantic_matches and dynamic_is_null):
            failures.append(result)

if failures:
    raise SystemExit(f"canonical setting verification failed for {len(failures)} rows")
