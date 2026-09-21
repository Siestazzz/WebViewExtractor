#!/usr/bin/env python3
"""Check exported counts and that every detailed host/receiver is preserved."""
import argparse,json,pathlib
p=argparse.ArgumentParser();p.add_argument('batch');a=p.parse_args();results=[]
for path in sorted(pathlib.Path(a.batch).glob('*/capabilities.compact.json')):
 c=json.loads(path.read_text());f=json.loads(path.with_name('capabilities.json').read_text())
 assert c['metadata']['status']==f['status'],path
 assert len(c['activities'])==len(f['activities']),path
 sums=dict(activities=len(c['activities']),webviews=0,bridges=0,settings=0,callbacks=0)
 for host,full in zip(c['activities'],f['activities']):
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
 results.append(dict(package=c['metadata']['package'],checks='PASS',counts=sums))
print(json.dumps(results,indent=2))
