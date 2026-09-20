#!/usr/bin/env python3
"""Audit v2 bridge candidates against Ctrip's source factory sets."""
import argparse, json
from pathlib import Path

SITE='Lctrip/android/view/h5v2/view/H5WebView;->D(Ljava/lang/Object;Lctrip/android/view/h5v2/view/H5WebView;)V@84'

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--candidates',type=Path,required=True); ap.add_argument('--oracle-dir',type=Path,required=True); ap.add_argument('--output',type=Path,required=True)
    a=ap.parse_args(); data=json.loads(a.candidates.read_text())
    groups={x['group_id']:x for x in map(json.loads,a.oracle_dir.joinpath('groups.jsonl').read_text().splitlines())}
    core={b['type'] for b in groups['ctrip-h5-v2']['capabilities']['bridges']}
    conditional={b['type']:gid for gid,g in groups.items() if g['evaluation_tier']=='conditional_required' for b in g['capabilities'].get('bridges',[])}
    rows=[]
    for activity in data['activities']:
        for fact in activity['facts']:
            if fact.get('kind')!='bridge' or fact.get('site')!=SITE: continue
            impl=fact.get('implementation')
            if impl in core: verdict='valid_core'; reason='Concrete type is an element of the unconditional list returned by ctrip.base.init.n.t().'
            elif impl in conditional: verdict='conditional_only'; reason=f'Concrete type is supplied by conditional group {conditional[impl]}. The candidate is acceptable only with conditional=true and must not enter the unconditional core denominator.'
            else: verdict='false_subclass_expansion'; reason='Type is absent from the n.t() factory list and conditional Bus providers; iterator element type H5Plugin cannot be expanded to every subtype.'
            rows.append({'activity':activity['activity'],'candidate_site':SITE,'implementation':impl,'registration_name':fact.get('registration_name'),'candidate_conditional':fact.get('conditional'),'verdict':verdict,'reason':reason,'required_relation':'factory element or conditional provider return tied to the same iterator/list value','factory_evidence':[{'file':'sources/ctrip/base/init/n.java','line':520,'text':'arrayList.addAll(Arrays.asList(... concrete core constructors ...));'},{'file':'sources/ctrip/android/view/h5v2/view/H5WebView.java','line':218,'text':'this.y = ctrip.android.view.h5v2.b.a().t(obj, h5WebView);'},{'file':'sources/ctrip/android/view/h5v2/view/H5WebView.java','line':182,'text':'for (H5Plugin h5Plugin : this.y) {'},{'file':'sources/ctrip/android/view/h5v2/view/H5WebView.java','line':186,'text':'h5WebView.addJavascriptInterface(h5Plugin, h5Plugin.getClass().getField("TAG").get(h5Plugin).toString());'}],'apk_sha256':data['apk_sha256']})
    a.output.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows))
    from collections import Counter
    counts=Counter(x['verdict'] for x in rows); impls={v:sorted({x['implementation'] for x in rows if x['verdict']==v}) for v in counts}
    md=['# v2b capability ownership audit','',f"The candidate file contributes {len(rows)} bridge facts at the generic H5Plugin iterator site. Source factory evidence validates {counts['valid_core']} core occurrences, supports {counts['conditional_only']} occurrences only as conditional candidates, and rejects {counts['false_subclass_expansion']} subtype-expansion occurrences.",'','The required proof is value-sensitive: a concrete type must be constructed in `n.t()` and placed in the returned list, or be returned by the exact Bus call added to that list. Assignability to `H5Plugin` alone is insufficient. `H5WebView.D()` iterates that returned list and reflectively reads the runtime instance TAG. The v2b rows inspected here carry `conditional=true`; that flag correctly prevents optional Bus/plugin facts from claiming unconditional execution, but it does not rescue types that no factory or provider can produce.','', '## False subtype expansions','']
    md += [f"- `{x}`" for x in impls.get('false_subclass_expansion',[])]
    md += ['','## Conditional-only types','']+[f"- `{x}`" for x in impls.get('conditional_only',[])]
    md += ['','`v2-capability-ownership.jsonl` retains every candidate occurrence, including failures; no gold failure case is deleted.','']
    a.output.with_suffix('.md').write_text('\n'.join(md))

if __name__=='__main__': main()
