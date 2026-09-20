#!/usr/bin/env python3
"""Create a review candidate; deliberately never overwrites canonical-facts.jsonl."""
import json,hashlib
from pathlib import Path
D=Path(__file__).resolve().parent
SRC=D/'canonical-facts.jsonl'; MAP=D/'canonical-webview-type-map.json'
OUT=D/'canonical-facts-webview-constrained.candidate.jsonl'; AUDIT=D/'canonical-webview-constraint-migration.json'
def main():
 cfg=json.load(open(MAP)); rules={x['label']:x for x in cfg['rules']}; selectors=cfg.get('selectors',[]); rows=[json.loads(x) for x in SRC.read_text().splitlines() if x.strip()]
 labels={x.get('webview','') for x in rows}; missing=sorted(labels-rules.keys()); extra=sorted(rules.keys()-labels)
 out=[]; changed=0; retained=0
 for x in rows:
  y=dict(x)
  matched=[s for s in selectors if all(x.get(k)==v for k,v in s['match'].items())]
  if len(matched)>1: raise SystemExit(f'ambiguous selectors for {x}: {[s["id"] for s in matched]}')
  rule=matched[0] if matched else rules.get(x.get('webview',''))
  if rule:
   constraint={'types':rule['types']}
   if y.get('webview_constraint')==constraint: retained+=1
   else: y['webview_constraint']=constraint;changed+=1
   y['webview_constraint_semantics']='runtime WebView type only; object identity requires source audit'
   y['webview_constraint_evidence']=rule['basis']
  else:y['webview_constraint_status']='unresolved'
  out.append(y)
 text=''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in out);OUT.write_text(text)
 audit={'schema_version':3,'map_dataset':cfg['dataset'],'map_sha256':hashlib.sha256(MAP.read_bytes()).hexdigest(),'source':str(SRC.relative_to(D.parents[2])),'source_sha256':hashlib.sha256(SRC.read_bytes()).hexdigest(),'candidate':str(OUT.relative_to(D.parents[2])),'candidate_sha256':hashlib.sha256(OUT.read_bytes()).hexdigest(),'rows':len(rows),'labels':len(labels),'rules':len(rules),'selectors':len(selectors),'selector_matches':sum(1 for x in rows if any(all(x.get(k)==v for k,v in s['match'].items()) for s in selectors)),'newly_constrained_rows':changed,'already_constrained_rows':retained,'unresolved_rows':sum(1 for x in out if x.get('webview_constraint_status')=='unresolved'),'unresolved_labels':missing,'unused_rules':extra,'canonical_overwritten_by_this_script':False,'constraint_semantics':'type-only; no object-identity claim','failed_versions_preserved':['canonical-webview-type-map-v1.json','canonical-facts-webview-constrained-v1.failed.jsonl','canonical-webview-constraint-migration-v1.failed.json','canonical-webview-type-map-v2.json','canonical-facts-webview-constrained-v2.failed.jsonl','canonical-webview-constraint-migration-v2.failed.json']}
 AUDIT.write_text(json.dumps(audit,ensure_ascii=False,indent=2)+'\n');print(json.dumps(audit,sort_keys=True))
if __name__=='__main__':main()
