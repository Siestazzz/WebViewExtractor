#!/usr/bin/env python3
"""Check exported counts and that every detailed host/receiver is preserved."""
import argparse,json,pathlib
p=argparse.ArgumentParser();p.add_argument('batch');a=p.parse_args();results=[]
paths=sorted(pathlib.Path(a.batch).glob('*/capabilities.compact.json'))
if not paths:raise SystemExit('No compact reports found')
for path in paths:
 c=json.loads(path.read_text());f=json.loads(path.with_name('capabilities.json').read_text())
 assert c['metadata']['status']==f['status'],path
 assert len(c['activities'])==len(f['activities']),path
 sums=dict(activities=len(c['activities']),webviews=0,bridges=0,settings=0,callbacks=0)
 full_hosts={'L'+x['activity'].replace('.','/')+';':x for x in f['activities']}
 signatures=[x['signature'] for x in c['activities']]
 assert len(full_hosts)==len(f['activities']),path
 assert len(signatures)==len(set(signatures)) and set(signatures)==set(full_hosts),path
 def rank(x):return (-x['counts']['bridges'],-x['counts']['callbacks'],-x['counts']['settings'],x['signature'])
 if c['metadata'].get('schema_version',1)>=2:assert c['activities']==sorted(c['activities'],key=rank),path
 for host in c['activities']:
  full=full_hosts[host['signature']]
  if c['metadata'].get('schema_version',1)>=2:assert host['webviews']==sorted(host['webviews'],key=rank),path
  ids={json.dumps(x['webview'],sort_keys=True) for x in full['facts']}
  assert len(host['webviews'])==len(ids),(path,host['signature'])
  totals=dict(activities=1,webviews=len(ids),bridges=0,settings=0,callbacks=0)
  for view in host['webviews']:
   for key in ('bridges','settings','callbacks'):
    n=len(view[key]);assert n==view['counts'][key],path
    assert len({json.dumps(x,sort_keys=True) for x in view[key]})==n,path
    totals[key]+=n
  assert totals==host['counts'],path
  for key in ('webviews','bridges','settings','callbacks'):sums[key]+=totals[key]
 assert sums==c['counts'],path
 if c['metadata'].get('schema_version',1)>=2:
  count_path=path.with_name('capabilities.counts.json')
  count=json.loads(count_path.read_text());expected=json.loads(json.dumps(c))
  expected['metadata']['projection']='counts_only'
  for host in expected['activities']:
   for view in host['webviews']:
    for key in ('bridges','settings','callbacks'):view.pop(key)
  assert count==expected,count_path
  assert '\n  ' in count_path.read_text(),count_path
 results.append(dict(package=c['metadata']['package'],checks='PASS',counts=sums))
print(json.dumps(results,indent=2))
