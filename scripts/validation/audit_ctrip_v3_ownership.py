#!/usr/bin/env python3
"""Build conservative v3 Activity ownership decisions from source-reviewed evidence."""
import argparse,json
from pathlib import Path

NEW={
'ctrip.android.login.view.thirdlogin.QQSSOEntryActivity':('valid','conditional SDK login dialog path',181,207),
'ctrip.android.view.login.v.third.QQEntryActivity':('valid','conditional SDK login dialog path',150,178),
'ctrip.android.tour.search.view.CTTourSearchActivity2':('valid','pager factory receives SearchH5Fragment2.class and arguments',2372,2372),
'ctrip.base.ui.gallery.PhotoViewDetailActivity':('valid','conditional gallery bottomWebViewUrl path from caller-supplied parcelable ImageItem list',510,227),
}
def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--v3',type=Path,required=True); ap.add_argument('--v1-audit',type=Path,required=True); ap.add_argument('--decompile-root',type=Path,required=True); ap.add_argument('--output',type=Path,required=True); a=ap.parse_args()
 data=json.loads(a.v3.read_text()); old={x['activity']:x for x in map(json.loads,a.v1_audit.read_text().splitlines())}; rows=[]
 for act in data['activities']:
  cls=act['activity']
  if cls in old:
   base=old[cls]; rows.append({'activity':cls,'verdict':base['verdict'],'source_evidence':base['source_evidence'],'rationale':'Reused prior source-reviewed ownership decision; v3 facts were not accepted as truth. '+base['rationale'],'candidate_fact_count':len(act['facts']),'apk_sha256':data['apk_sha256']}); continue
  verdict,why,l1,l2=NEW[cls]; rel=cls.replace('.','/')+'.java'; p=a.decompile_root/'sources'/rel; lines=p.read_text(errors='replace').splitlines()
  rows.append({'activity':cls,'verdict':verdict,'source_evidence':[{'file':'sources/'+rel,'line':n,'text':lines[n-1].strip()} for n in dict.fromkeys((l1,l2))],'rationale':why,'candidate_fact_count':len(act['facts']),'apk_sha256':data['apk_sha256']})
 a.output.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows))
 from collections import Counter
 c=Counter(x['verdict'] for x in rows); upper=c['wrong']+c['uncertain']
 a.output.with_suffix('.md').write_text(f"# v3 Activity ownership audit\n\nReviewed {len(rows)} emitted Activities: {c['valid']} valid, {c['wrong']} wrong, {c['uncertain']} uncertain. The conservative wrong-or-uncertain upper bound is {upper}/{len(rows)} ({upper/len(rows):.1%}). Prior v1 decisions are reused only where the Activity is identical; four new Activities have fresh source evidence. Candidate capability values do not define ownership.\n")
if __name__=='__main__': main()
