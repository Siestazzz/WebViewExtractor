#!/usr/bin/env python3
"""Apply exact, source-reviewed corrections to a new oracle; preserve raw history."""
import argparse
import hashlib
import json
from pathlib import Path
from freeze_oracle import canonical


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def row_hash(row):
    return digest(canonical(row).encode('utf-8'))


def apply(rows, corrections, root=Path('.')):
    replacements = {}
    ids = set()
    for c in corrections:
        cid = c['correction_id']
        if cid in ids:
            raise ValueError('Duplicate correction ID: ' + cid)
        ids.add(cid)
        if not (c.get('reviewer') and c.get('reason') and c.get('source_counterevidence')
                and c.get('provenance') == 'source-first' and c.get('based_on_report') is False):
            raise ValueError('Missing independent source review: ' + cid)
        old, new = c['old_row'], c['new_row']
        oh = row_hash(old)
        if oh != c['old_canonical_json_sha256'] or row_hash(new) != c['new_canonical_json_sha256']:
            raise ValueError('Row hash mismatch: ' + cid)
        if not c.get('apk_sha256') or any(r.get('apk_sha256') != c['apk_sha256'] for r in (old, new)):
            raise ValueError('APK identity mismatch: ' + cid)
        raw = (root / c['old_file']).read_bytes()
        if digest(raw) != c['old_file_sha256']:
            raise ValueError('Source snapshot hash mismatch: ' + cid)
        line = c['old_line']
        if not isinstance(line, int) or line < 1 or line > len(raw.splitlines()):
            raise ValueError('Invalid source snapshot line: ' + cid)
        if row_hash(json.loads(raw.splitlines()[line - 1])) != oh:
            raise ValueError('Source snapshot row mismatch: ' + cid)
        if oh in replacements:
            raise ValueError('Conflicting corrections for old row: ' + cid)
        replacements[oh] = c
    available = {row_hash(r) for r in rows}
    absent = {h for h, c in replacements.items()
              if h not in available and row_hash(c['new_row']) not in available}
    if absent:
        raise ValueError('Exact old rows absent; no fuzzy correction allowed: ' + str(sorted(absent)))
    result = []
    seen = set()
    for row in rows:
        c = replacements.get(row_hash(row))
        if c:
            old = dict(row)
            old.update(positive_acceptance=False, binding_status='source-superseded',
                       oracle_correction=dict(id=c['correction_id'], original_row_sha256=row_hash(row),
                                              replacement_row_sha256=row_hash(c['new_row']),
                                              reason=c['reason']))
            candidates = [old, c['new_row']]
        else:
            candidates = [row]
        for candidate in candidates:
            key = canonical(candidate)
            if key not in seen:
                result.append(candidate)
                seen.add(key)
    return result


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--oracle', required=True)
    p.add_argument('--ledger', action='append', required=True)
    p.add_argument('--out', required=True)
    a = p.parse_args()
    dest = Path(a.out)
    if dest.exists():
        p.error('Refusing to replace an existing oracle directory')
    source = Path(a.oracle)
    raw = source.read_bytes()
    rows = [json.loads(line) for line in raw.splitlines() if line.strip()]
    corrections = []
    ledgers = []
    for name in a.ledger:
        data = Path(name).read_bytes()
        corrections.extend(json.loads(line) for line in data.splitlines() if line.strip())
        ledgers.append(dict(path=name, sha256=digest(data)))
    result = apply(rows, corrections)
    output = ('\n'.join(canonical(row) for row in result) + '\n').encode('utf-8')
    hashes = {row_hash(r) for r in rows}
    statuses = [dict(id=c['correction_id'], status='applied' if row_hash(c['old_row']) in hashes
                    else 'already_current_exact_replacement_present',
                    old_row_sha256=row_hash(c['old_row']), new_row_sha256=row_hash(c['new_row']))
                for c in corrections]
    metadata = dict(raw_oracle=str(source), raw_oracle_sha256=digest(raw), ledgers=ledgers,
                    correction_statuses=statuses,
                    corrections_applied=sum(s['status'] == 'applied' for s in statuses),
                    raw_rows=len(rows), corrected_rows=len(result),
                    corrected_oracle_sha256=digest(output),
                    note='Source correction, not extractor improvement. Raw oracle and scores must remain available. '
                         'Only exact hash-identified rows superseded; original rows remain in raw history and ledgers. '
                         'Machine validation checks identity, not the truth of the independent source review.')
    dest.mkdir(parents=True)
    (dest / 'facts.jsonl').write_bytes(output)
    (dest / 'manifest.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(metadata, ensure_ascii=False))


if __name__ == '__main__':
    main()
