#!/usr/bin/env python3
"""Compare report facts without interpreting their source correctness."""
import argparse, collections, hashlib, json, pathlib
p = argparse.ArgumentParser()
p.add_argument('--before', required=True)
p.add_argument('--after', required=True)
p.add_argument('--out', required=True)
a = p.parse_args()
excluded = {'evidence', 'arguments', 'binding_status', 'conditional'}
def read(path):
    raw = pathlib.Path(path).read_bytes()
    report = json.loads(raw)
    facts = {host['activity']: collections.Counter(json.dumps({k: v for k, v in fact.items() if k not in excluded}, sort_keys=True) for fact in host['facts']) for host in report['activities']}
    if len(facts) != len(report['activities']):
        raise SystemExit('Duplicate Activity rows')
    return report, facts, hashlib.sha256(raw).hexdigest()
before, x, before_hash = read(a.before)
after, y, after_hash = read(a.after)
if before['apk_sha256'] != after['apk_sha256']:
    raise SystemExit('APK hashes differ')
changes = {}
for host in sorted(x.keys() & y.keys()):
    added, removed = y[host] - x[host], x[host] - y[host]
    if added or removed:
        changes[host] = dict(added=sum(added.values()), removed=sum(removed.values()))
result = dict(before=a.before, after=a.after, before_sha256=before_hash, after_sha256=after_hash, apk_sha256=before['apk_sha256'], excluded_fact_fields=sorted(excluded), added_hosts=sorted(y.keys()-x.keys()), removed_hosts=sorted(x.keys()-y.keys()), changed_hosts=changes, note='Report delta only, not independent source verification. Compared fields include receiver identities, values, implementation types and full member signatures. Excluded evidence/arguments/status fields require separate review.')
pathlib.Path(a.out).write_text(json.dumps(result, indent=2)+'\n')
print(json.dumps({k: result[k] for k in ['added_hosts', 'removed_hosts', 'changed_hosts']}))
