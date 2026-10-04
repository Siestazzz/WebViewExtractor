#!/usr/bin/env python3
"""Strict evidence ledger; no absent, stale or sampled evidence can establish acceptance."""
import argparse, hashlib, json, pathlib

KINDS=('bridge','setting','callback')
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def read(p):return json.loads(p.read_text()) if p and p.is_file() else None
def assess(spec, reports, base):
 package=spec['package'];path=reports/package/'capabilities.json';report=read(path)
 result={'package':package,'requirements':{},'notes':[]};g=result['requirements']
 if report is None:
  g['report_identity']=False;result['notes'].append('Missing detailed report');result['passed']=False;return result
 rh=digest(path);ah=report.get('apk_sha256');g['report_identity']=ah==spec['sha256']
 def artifact(key):return read(base/spec[key]) if spec.get(key) else None
 score=artifact('score');oracle=base/spec['oracle'] if spec.get('oracle') else None
 fresh=bool(score and score.get('report_sha256')==rh and score.get('apk_sha256')==ah and oracle and oracle.is_file() and score.get('oracle_sha256')==digest(oracle))
 g['score_report_and_oracle_identity']=fresh
 recalls={k:score.get('metrics',{}).get(k,{}) if fresh else {} for k in KINDS}
 for k,v in recalls.items():g[k+'_recall_95']=bool(v.get('expected',0)>0 and v.get('matched',0)/v['expected']>=.95)
 result['recall']={k:({'matched':v.get('matched'),'expected':v.get('expected'),'candidate_inclusive':v.get('recall')} if v else None) for k,v in recalls.items()}
 hosts={a['activity'] for a in report.get('activities',[])};reviews={};rejected=[]
 if spec.get('ownership'):
  reviewpath=base/spec['ownership']
  if reviewpath.is_file():
   for line in reviewpath.read_text().splitlines():
    if not line.strip():continue
    r=json.loads(line);name=r.get('activity')
    if r.get('apk_sha256')!=ah or r.get('report_sha256')!=rh or r.get('scope')!='activity_host' or not r.get('evidence'):
     rejected.append(name);continue
    if name in reviews:raise ValueError('Duplicate applicable ownership review: '+str(name))
    reviews[name]=r.get('verdict')
 valid=sum(reviews.get(h)=='valid' for h in hosts);wrong=sum(reviews.get(h)=='wrong' for h in hosts);unknown=len(hosts)-valid-wrong
 upper=(wrong+unknown)/len(hosts) if hosts else None
 result['ownership']={'emitted':len(hosts),'valid':valid,'wrong':wrong,'unresolved_or_unreviewed':unknown,'rejected_rows':rejected,'conservative_error_upper_bound':upper}
 g['ownership_error_upper_bound_10']=upper is not None and upper<=.1
 precision=artifact('capability_precision')
 g['capability_precision_reported']=bool(precision and precision.get('apk_sha256')==ah and precision.get('report_sha256')==rh and precision.get('evidence') and all(k in precision.get('categories',{}) for k in KINDS))
 result['capability_precision']=precision if g['capability_precision_reported'] else None
 audit=artifact('source_audit');audit_ok=bool(audit and audit.get('apk_sha256')==ah and audit.get('evidence') and audit.get('reviewer'))
 positive=set(audit.get('deep_positive_activities',[])) if audit_ok else set()
 exhaustive=bool(audit_ok and audit.get('exhaustive_positive_inventory') is True and audit.get('exhaustive_inventory_evidence'))
 g['deep_positive_hosts_30_or_exhaustive']=len(positive)>=30 or exhaustive
 strata=audit.get('present_strata',{}) if audit_ok else {}
 g['two_per_present_stratum']=bool(strata) and all(len(set(v)&positive)>=2 for v in strata.values())
 hold=artifact('holdout');hold_ok=bool(hold and hold.get('apk_sha256')==ah and hold.get('report_sha256')==rh and hold.get('evidence') and hold.get('sealed_before_analysis') is True)
 holdhosts=set(hold.get('activities',[])) if hold_ok else set()
 g['ten_fresh_holdout_hosts']=len(holdhosts)>=10 and not bool(holdhosts&positive) and bool(hold.get('not_used_for_repairs')) if hold_ok else False
 g['holdout_quality']=bool(hold_ok and all(hold.get('recall',{}).get(k,0)>=.95 for k in KINDS) and hold.get('ownership_error_upper_bound',1)<=.1)
 perf=artifact('performance');runs=perf.get('runs',[]) if perf and perf.get('apk_sha256')==ah else []
 qualified=[r for r in runs if r.get('fresh_process') is True and r.get('analysis_results_reused') is False and r.get('isolated') is True and r.get('cpu_count')==8 and r.get('heap_gib')==16 and r.get('jar_sha256')==spec.get('jar_sha256') and r.get('valid_report') is True]
 g['three_isolated_runs']=len(qualified)>=3 and len({r.get('run_id') for r in qualified if r.get('run_id')})>=3
 g['hard_600_seconds']=g['three_isolated_runs'] and all(0<r.get('wall_seconds',float('inf'))<=600 for r in qualified)
 result['target_300_seconds']=bool(g['three_isolated_runs'] and all(0<r.get('wall_seconds',float('inf'))<=300 for r in qualified))
 result['passed']=all(g.values());return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--spec',required=True);p.add_argument('--reports',required=True);p.add_argument('--out',required=True);a=p.parse_args()
 specpath=pathlib.Path(a.spec);spec=json.loads(specpath.read_text());rows=[assess(s,pathlib.Path(a.reports),specpath.parent) for s in spec['apps']]
 result={'goal':'Ten APKs; no private App adapters; per-category recall >=95%; wrong+unknown ownership <=10%; hard600; independent audit and repeats','scope_complete':len(rows)==10 and len({r['package'] for r in rows})==10,'apps':rows,'note':'Machine checks verify evidence applicability and recorded gates, not the truth of a reviewer assertion. Source audit remains mandatory.'}
 result['passed']=result['scope_complete'] and all(r['passed'] for r in rows)
 pathlib.Path(a.out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n');print(json.dumps({'passed':result['passed'],'apps_passed':sum(r['passed'] for r in rows),'apps':len(rows)}));return 0 if result['passed'] else 2
if __name__=='__main__':raise SystemExit(main())
