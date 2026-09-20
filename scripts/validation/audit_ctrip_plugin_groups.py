#!/usr/bin/env python3
"""Check the source-derived Ctrip plugin groups; never reads extractor output."""
import argparse, json
from pathlib import Path

CORE={'ctrip-h5-v1':34,'ctrip-h5-v2':35}

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--decompile-root',type=Path,required=True)
    ap.add_argument('--oracle-dir',type=Path,required=True)
    a=ap.parse_args(); root=a.decompile_root
    groups={x['group_id']:x for x in map(json.loads,a.oracle_dir.joinpath('groups.jsonl').read_text().splitlines())}
    links=list(map(json.loads,a.oracle_dir.joinpath('activity-groups.jsonl').read_text().splitlines()))
    report={'scope':'source oracle only; no extractor result read','core':{},'conditional_host_links':[],'errors':[]}
    for gid,expected in CORE.items():
        plugins=[]; group=groups[gid]
        for bridge in group['capabilities']['bridges']:
            methods=bridge['methods']; signatures=[m['normalized_signature'] for m in methods]
            inherited=sum(m.get('declaring_type')!=bridge['type'] for m in methods)
            checks=[]
            for method in methods:
                ev=method['evidence']; lines=(root/ev['file']).read_text(errors='replace').splitlines()
                lo=max(0,ev['line']-8); annotation=any('@JavascriptInterface' in x for x in lines[lo:ev['line']])
                checks.append(annotation)
                if not annotation: report['errors'].append(f"{gid}: annotation not found before {method['normalized_signature']}")
            reg=bridge['registry_evidence']; reg_text=(root/reg['file']).read_text(errors='replace').splitlines()[reg['line']-1].strip()
            plugins.append({'type':bridge['type'],'registration_name':bridge.get('name'),'method_count':len(methods),'inherited_method_count':inherited,'unique_signatures':len(set(signatures)),'registry_evidence':reg,'registry_text':reg_text,'all_method_evidence_annotated':all(checks)})
            if len(signatures)!=len(set(signatures)): report['errors'].append(f"{gid}: duplicate method signature in {bridge['type']}")
        report['core'][gid]={'expected_unconditional_plugin_count':expected,'actual_unconditional_plugin_count':len(plugins),'method_count':sum(x['method_count'] for x in plugins),'plugins':plugins}
        if len(plugins)!=expected: report['errors'].append(f"{gid}: expected {expected}, got {len(plugins)}")
    report['conditional_host_links']=[x for x in links if groups[x['group_id']]['evaluation_tier']=='conditional_required']
    if report['conditional_host_links']: report['errors'].append('conditional groups are attached to Activity hosts as required facts')
    report['conditional_groups']={gid:{'plugin_count':len(g['capabilities'].get('bridges',[])),'unknown':g.get('unknown',[])} for gid,g in groups.items() if g['evaluation_tier']=='conditional_required'}
    report['ok']=not report['errors']
    a.oracle_dir.joinpath('plugin-registry-audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
    lines=['# Ctrip H5 plugin registry audit','',f"Result: `{'pass' if report['ok'] else 'fail'}`. This audit reads only decompiled source and oracle groups; it does not read extractor output.",'']
    for gid,data in report['core'].items():
        lines += [f"## {gid}",'',f"The unconditional registry contains {data['actual_unconditional_plugin_count']} plugins and {data['method_count']} annotated methods. Samsung Wallet is excluded here because registration is guarded by an H5Fragment type check.",'','| Type | TAG | Methods | Inherited |','|---|---:|---:|---:|']
        for p in data['plugins']: lines.append(f"| `{p['type']}` | `{p['registration_name']}` | {p['method_count']} | {p['inherited_method_count']} |")
        lines.append('')
    lines += ['## Conditional applicability','',f"Conditional groups have {len(report['conditional_host_links'])} Activity links. A zero count is required: Bus and type-guarded plugins remain possible runtime registrations, not unconditional per-Activity recall facts.",'','Settings use the method name from `normalized_api`. `value_kind=literal` and `value_kind=enum` are exact comparison values; `value_kind=dynamic` preserves the Java expression in `source_expression` and leaves `value` null.','']
    a.oracle_dir.joinpath('plugin-registry-audit.md').write_text('\n'.join(lines))

if __name__=='__main__': main()
