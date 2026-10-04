#!/usr/bin/env python3
"""Integration check: a supervisor deadline must leave a parseable atomic report."""
import argparse,json,pathlib,subprocess,tempfile,time
p=argparse.ArgumentParser();p.add_argument('--jar',required=True);p.add_argument('--apk',required=True);a=p.parse_args()
with tempfile.TemporaryDirectory(prefix='wv-deadline-') as folder:
 output=pathlib.Path(folder); start=time.monotonic(); snapshots=0
 cmd=['java','-Xmx16g','-XX:ActiveProcessorCount=8','-jar',str(pathlib.Path(a.jar).resolve()),'--apkpath',str(pathlib.Path(a.apk).resolve()),'--out',str(output),'--target-seconds','1','--hard-seconds','1']
 with (output/'console.log').open('w') as log:
  worker=subprocess.Popen(cmd,stdout=log,stderr=subprocess.STDOUT)
  try:
   while worker.poll() is None:
    for name in ('capabilities.json','capabilities.compact.json','capabilities.counts.json'):
     report=output/name
     if report.exists():json.loads(report.read_text());snapshots+=1
    if time.monotonic()-start>5:raise AssertionError('Hard deadline did not stop process within 5 seconds')
    time.sleep(.02)
  finally:
   if worker.poll() is None:worker.kill();worker.wait()
 report=json.loads((output/'capabilities.json').read_text())
 assert worker.returncode==2,(worker.returncode,report)
 assert report['status'] in ('timeout','failed'),report
 assert report.get('diagnostics'),report
 assert isinstance(report['activities'],list)
 compact=json.loads((output/'capabilities.compact.json').read_text())
 counts=json.loads((output/'capabilities.counts.json').read_text())
 assert compact['metadata']['status']==counts['metadata']['status']==report['status']
 assert compact['counts']==counts['counts']
 for host in counts['activities']:
  for view in host['webviews']:assert not any(k in view for k in ('bridges','settings','callbacks'))
 print(json.dumps(dict(check='deadline_three_atomic_reports',elapsed_seconds=time.monotonic()-start,snapshots=snapshots,status=report['status'],diagnostics=report['diagnostics'])))
