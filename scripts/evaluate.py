#!/usr/bin/env python3
"""Strict replay of independent oracle facts; missing normalization is NOT a pass."""
import argparse,collections,json,pathlib,re,hashlib
p=argparse.ArgumentParser();p.add_argument('--report',required=True);p.add_argument('--oracle',required=True);p.add_argument('--out',required=True);p.add_argument('--ownership');a=p.parse_args()
r=json.loads(pathlib.Path(a.report).read_text());truth=[json.loads(l) for l in pathlib.Path(a.oracle).read_text().splitlines() if l.strip()]
actual={x['activity']:x['facts'] for x in r['activities']}
if len(actual)!=len(r['activities']):raise SystemExit('Duplicate Activity entries in report')
stats=collections.defaultdict(lambda:dict(expected=0,matched=0,explicit_matched=0,unscorable=0));failures=[]
def match(g,f):
 k=g['kind']; signature=g.get('normalized_signature'); api=g.get('normalized_api') or g.get('normalized_signature');name=g['name']
 if k=='setting':
  if f['kind']!='setting':return False
  if not api:return None
  if f['api']!=api:return False
  val=str(g.get('value')).strip(); vk=g.get('value_kind')
  if vk=='dynamic':return bool(f.get('arguments'))
  if val.startswith('"') and val.endswith('"'):val=val[1:-1]
  if vk=='enum' or re.fullmatch(r'(?:[A-Za-z_$][\w$]*\.)+[A-Z_]+',val):
   member=val.split('.')[-1]
   return any('->'+member+':' in arg.get('id','') for arg in f.get('arguments',[]))
  if val.lower() in ('true','false') or re.fullmatch(r'-?\d+',val) or str(g.get('value','')).startswith('"'):
   return val.lower() in [str(v).lower() for v in f.get('values',[])]
  if vk=='literal':return val.lower() in [str(v).lower() for v in f.get('values',[])]
  return None
 if k=='callback':
  if f['kind']!='callback':return False
  if not signature:return None
  return any(m['signature']==signature for m in f.get('members',[]))
 if k in ('bridge','bridge_method','message_handler'):
  if f['kind'] not in ('bridge','message_bridge'):return False
  registration=g.get('registration_name',name if k=='bridge' else None)
  if registration is None:return None
  if f.get('registration_name')!=registration:return False
  if g.get('implementation') and f.get('implementation')!=g['implementation']:return False
  if g.get('binding_status')=='registered-no-compatible-endpoint':return not f.get('members')
  if k in ('bridge_method','message_handler') or signature:
   if not signature:return None
   return any(m['signature']==signature for m in f.get('members',[]))
  return True
 return None
# Deduplicate semantic expectations, keeping every source row in the versioned oracle.
unique={};duplicate_rows=0
for g in truth:
 key=tuple(json.dumps(g.get(k),sort_keys=True) for k in ('activity','webview','kind','registration_name','name','normalized_signature','normalized_api','implementation','value','value_kind','binding_status'))
 if key in unique:duplicate_rows+=1
 else:unique[key]=g
submetrics=collections.defaultdict(lambda:dict(expected=0,matched=0))
unknown_surfaces=[]
for g in unique.values():
 if g['kind']=='activity_binding' or g.get('activity') is None:continue
 if g.get('apk_sha256')!=r.get('apk_sha256'):raise SystemExit('APK hash mismatch; cannot compare')
 category='bridge' if g['kind'].startswith('bridge') or g['kind']=='message_handler' else g['kind'];s=stats[category];s['expected']+=1
 unknown='unknown' in g.get('binding_status','')
 if unknown:unknown_surfaces.append(dict(activity=g['activity'],name=g['name'],status=g['binding_status']))
 grain=('bridge_member' if g['kind'] in ('bridge_method','message_handler') or g.get('normalized_signature') and category=='bridge' else 'bridge_registration') if category=='bridge' else category
 submetrics[grain]['expected']+=1
 candidates=actual.get(g['activity'],[]);outcomes=[match(g,f) for f in candidates];matches=[f for f,m in zip(candidates,outcomes) if m is True]
 submetrics[grain]['matched']+=int(bool(matches))
 if unknown:matches=[]
 if matches:
  s['matched']+=1;s['explicit_matched']+=int(any(f.get('binding_status')=='explicit' for f in matches))
 else:
  unscorable=unknown or any(x is None for x in outcomes) or (g['kind'] in ('setting','callback','bridge_method') and not g.get('normalized_signature') and not g.get('normalized_api'))
  s['unscorable']+=int(unscorable);failures.append(dict(activity=g['activity'],kind=g['kind'],name=g['name'],signature=g.get('normalized_signature',g.get('signature')),reason='unscorable_oracle' if unscorable else 'not_matched'))
for s in submetrics.values():s['recall']=s['matched']/s['expected'] if s['expected'] else None
for s in stats.values():s['recall']=s['matched']/s['expected'] if s['expected'] else None;s['explicit_recall']=s['explicit_matched']/s['expected'] if s['expected'] else None
out=dict(oracle_sha256=hashlib.sha256(pathlib.Path(a.oracle).read_bytes()).hexdigest(),report_sha256=hashlib.sha256(pathlib.Path(a.report).read_bytes()).hexdigest(),unassigned_oracle_facts=sum(g.get('activity') is None for g in truth),oracle_file=a.oracle,apk_sha256=r.get('apk_sha256'),report_status=r['status'],metrics=stats,missing=failures,emitted_activities=len(actual),acceptance='unproven',note='Candidate-inclusive fact recall is measured; independent output ownership review and full oracle scope are still required.')
out['scoring_version']=2
out['duplicate_oracle_rows']=duplicate_rows
out['capability_granularity']=submetrics
out['unknown_target_surfaces']=unknown_surfaces
out['binding_identity_verified']=False
out['note']='Deduplicated candidate-inclusive Activity-level recall; unknown targets count as unresolved misses in aggregate coverage. Registration-only matches are shown separately and do not prove endpoint coverage. WebView object identity requires independent review.'
reviews={}
if a.ownership:
 for line in pathlib.Path(a.ownership).read_text().splitlines():
  if not line.strip():continue
  row=json.loads(line)
  if row.get('apk_sha256')!=r.get('apk_sha256'):raise SystemExit('Ownership APK hash mismatch')
  if row['activity'] in reviews:raise SystemExit('Duplicate ownership verdict for '+row['activity'])
  reviews[row['activity']]=row['verdict']
ownership=collections.Counter(reviews.get(activity,'unreviewed') for activity in actual)
valid=ownership['valid'];wrong=ownership['wrong'];uncertain=len(actual)-valid-wrong
out['activity_ownership']=dict(counts=ownership,emitted=len(actual),wrong=wrong,uncertain_or_unreviewed=uncertain,conservative_error_upper_bound=(wrong+uncertain)/len(actual) if actual else None)
out['capability_precision']='Requires independent bound-object review; positive fact recall alone does not measure false capability associations.'
pathlib.Path(a.out).write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n');print(json.dumps(dict(metrics=stats,missing=len(failures)),ensure_ascii=False))
