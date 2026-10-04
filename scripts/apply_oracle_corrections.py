#!/usr/bin/env python3
"""Materialize a reviewed oracle view while retaining hash-bound source history."""
import argparse
import hashlib
import json
from pathlib import Path
from freeze_oracle import canonical, validate_positive_shape


def digest(row):
 return hashlib.sha256(canonical(row).encode()).hexdigest()


def correct(rows, ledger):
 originals={digest(row):row for row in rows}
 if len(originals)!=len(rows):raise ValueError('Duplicate input rows')
 replacements={}
 for change in ledger:
  old,new=change['old_row'],change['new_row']
  key=digest(old)
  if key!=change['old_canonical_json_sha256'] or digest(new)!=change['new_canonical_json_sha256']:raise ValueError('Correction row hash mismatch')
  if key not in originals:raise ValueError('Correction does not belong to input oracle')
  if key in replacements:raise ValueError('Conflicting or duplicate correction')
  if not change.get('reviewer') or not change.get('reason') or not change.get('source_counterevidence') or change.get('based_on_report') is not False:raise ValueError('Independent correction evidence required')
  if old.get('apk_sha256')!=new.get('apk_sha256') or old.get('apk_sha256')!=change.get('apk_sha256'):raise ValueError('Correction APK mismatch')
  if type(new.get('positive_acceptance')) is not bool:raise ValueError('Explicit corrected verdict required')
  if new['positive_acceptance']:validate_positive_shape(new,'correction',change.get('correction_id'))
  replacements[key]=new
 result=[replacements.get(digest(row),row) for row in rows]
 if len({digest(row) for row in result})!=len(result):raise ValueError('Correction collapses distinct source rows')
 return result


def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('--oracle',required=True);p.add_argument('--ledger',required=True);p.add_argument('--out',required=True)
 a=p.parse_args();source=Path(a.oracle);ledger_path=Path(a.ledger);dest=Path(a.out)
 if dest.exists():p.error('Refusing to replace a reviewed view')
 raw=source.read_bytes();ledger_raw=ledger_path.read_bytes()
 rows=[json.loads(x) for x in raw.splitlines() if x.strip()];ledger=[json.loads(x) for x in ledger_raw.splitlines() if x.strip()]
 result=correct(rows,ledger)
 # Retain the complete input and ledger beside the active view. Supersession is
 # inspectable, not deletion of a failed case or mutation of an earlier freeze.
 dest.mkdir(parents=True)
 (dest/'original.jsonl').write_bytes(raw);(dest/'corrections.jsonl').write_bytes(ledger_raw)
 output=('\n'.join(canonical(row) for row in result)+'\n').encode()
 (dest/'reviewed.jsonl').write_bytes(output)
 manifest=dict(source=str(source),source_sha256=hashlib.sha256(raw).hexdigest(),ledger=str(ledger_path),ledger_sha256=hashlib.sha256(ledger_raw).hexdigest(),reviewed_sha256=hashlib.sha256(output).hexdigest(),rows=len(rows),corrected_rows=len(ledger),note='Reviewed view only; original evidence and full supersession ledger retained. No analyzer improvement inferred.')
 (dest/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print(json.dumps(manifest))


if __name__=='__main__':main()
