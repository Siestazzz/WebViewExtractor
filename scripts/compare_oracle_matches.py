#!/usr/bin/env python3
"""Compare per-fact outcomes using the existing CLI scorer's exact match function."""
import argparse,ast,hashlib,json,pathlib,re,collections
p=argparse.ArgumentParser();p.add_argument('--before',required=True);p.add_argument('--after',required=True);p.add_argument('--oracle',required=True);p.add_argument('--out',required=True);a=p.parse_args()
source=pathlib.Path(__file__).with_name('evaluate.py').read_text();tree=ast.parse(source);function=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='match');scope={'re':re};exec(compile(ast.Module(body=[function],type_ignores=[]),'evaluate.py:match','exec'),scope);match=scope['match']
before=json.loads(pathlib.Path(a.before).read_text());after=json.loads(pathlib.Path(a.after).read_text());assert before['apk_sha256']==after['apk_sha256']
b={x['activity']:x['facts'] for x in before['activities']};n={x['activity']:x['facts'] for x in after['activities']};seen=set();rows=[];counts=collections.Counter()
keys=('activity','webview','kind','registration_name','name','normalized_signature','normalized_api','implementation','value','value_kind','binding_status','webview_constraint','positive_acceptance')
for line in pathlib.Path(a.oracle).read_text().splitlines():
 if not line.strip():continue
 g=json.loads(line);key=tuple(json.dumps(g.get(k),sort_keys=True) for k in keys)
 if key in seen:continue
 seen.add(key)
 if g['kind']=='activity_binding' or g.get('activity') is None or g.get('positive_acceptance') is False:continue
 assert g['apk_sha256']==before['apk_sha256']
 def matched(facts):return 'unknown' not in g.get('binding_status','') and any(match(g,f) is True for f in facts.get(g['activity'],[]))
 old,new=matched(b),matched(n);counts['before_matched']+=old;counts['after_matched']+=new
 if old!=new:
  change='regression' if old else 'gain';counts[change]+=1;rows.append(dict(change=change,fact={k:g.get(k) for k in keys},fact_sha256=hashlib.sha256(json.dumps(g,sort_keys=True).encode()).hexdigest()))
out=dict(apk_sha256=before['apk_sha256'],scorer_sha256=hashlib.sha256(source.encode()).hexdigest(),counts=dict(counts),changes=rows,note='Full semantic oracle identity retained, including setting values and implementation types; candidate-inclusive matcher unchanged.')
pathlib.Path(a.out).write_text(json.dumps(out,indent=2)+'\n');print(json.dumps(out['counts']))
