#!/usr/bin/env python3
"""Save immutable detailed-report snapshots near a target time without re-analysis."""
import argparse,json,pathlib,time,hashlib
p=argparse.ArgumentParser();p.add_argument('--batch',required=True);p.add_argument('--samples',required=True);p.add_argument('--out',required=True);p.add_argument('--seconds',type=float,default=295);a=p.parse_args()
batch=pathlib.Path(a.batch);out=pathlib.Path(a.out);out.mkdir(parents=True,exist_ok=True)
pending={s['package'] for s in json.loads(pathlib.Path(a.samples).read_text())};rows=[];stop=time.monotonic()+650
while pending and time.monotonic()<stop:
 finished={r['package'] for r in json.loads((batch/'summary.json').read_text())} if (batch/'summary.json').exists() else set()
 for package in sorted(pending):
  folder=batch/package;compact=folder/'capabilities.compact.json'
  if not compact.exists():continue
  meta=json.loads(compact.read_text())['metadata']
  if package not in finished and meta.get('elapsed_seconds',0)<a.seconds:continue
  report=folder/'capabilities.json'
  if not report.exists():continue
  raw=report.read_bytes();data=json.loads(raw);elapsed=data.get('metrics',{}).get('elapsed_seconds',0)
  dest=out/package;dest.mkdir(exist_ok=True);(dest/'capabilities.json').write_bytes(raw)
  rows.append(dict(package=package,elapsed_seconds=elapsed,finished_at_capture=package in finished,report_sha256=hashlib.sha256(raw).hexdigest(),apk_sha256=data.get('apk_sha256'),note='Detailed snapshot only; elapsed time is observed, not guaranteed exactly at target.'))
  pending.remove(package)
 (out/'index.json').write_text(json.dumps(rows,indent=2)+'\n')
 if pending:time.sleep(2)
if pending:raise SystemExit('Uncaptured packages: '+', '.join(sorted(pending)))
