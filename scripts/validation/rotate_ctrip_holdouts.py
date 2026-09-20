#!/usr/bin/env python3
"""Replace Ctrip holdouts whose identities appeared in development audits."""
import argparse, json
from pathlib import Path

KEEP={'com.mqunar.atom.meglive.facekit.activity.web.WebActivity','com.megvii.lv5.sdk.detect.guide.UserAgreementActivity'}
NEW={'ctrip.android.view.h5v2.debug.H5Setting','ctrip.android.view.h5v2.debug.H5TestActivity','ctrip.android.view.h5.debug.FileBrowser','ctrip.android.view.h5v2.debug.FileBrowser','ctrip.android.publicproduct.home.view.CtripHomeActivity','ctrip.android.view.view.CtripBootActivity','ctrip.android.reactnative.CRNBaseActivity','ctrip.android.reactnative.preloadv2.CRNBaseActivityV2'}

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('activities',type=Path); a=ap.parse_args()
    rows=list(map(json.loads,a.activities.read_text().splitlines()))
    for row in rows:
        if row['classification']=='holdout' and row['activity'] not in KEEP:
            row.update(classification='uncertain',binding_mode=None,evidence_strength='none',evidence=[],basis='retired_holdout_identity_leaked')
        if row['activity'] in NEW:
            row.update(classification='holdout',binding_mode=None,evidence_strength='withheld',evidence=[],basis='rotated_holdout')
    selected=[x['activity'] for x in rows if x['classification']=='holdout']
    if len(selected)!=10 or set(selected)!=(KEEP|NEW): raise SystemExit(f'unexpected holdouts: {selected}')
    a.activities.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows))

if __name__=='__main__': main()
