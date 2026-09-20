#!/usr/bin/env python3
import json
from pathlib import Path

HERE=Path(__file__).resolve().parent
rows=[json.loads(x) for x in (HERE/'facts.jsonl').read_text().splitlines()]

for x in rows:
    x['value_kind']=''
    x['source_expression']=''
    if x['kind']=='setting':
        raw=x.get('value')
        x['source_expression']=raw
        if raw in {'true','false'} or (isinstance(raw,str) and raw.lstrip('-').isdigit()) or raw=='UTF-8':
            x['value_kind']='literal'
            if raw in {'true','false'}: x['value']=(raw=='true')
            elif raw.lstrip('-').isdigit(): x['value']=int(raw)
        elif raw in {'ON','ON_DEMAND'}:
            x['value_kind']='enum'; x['value']='android.webkit.WebSettings.PluginState.'+raw
        elif x.get('name')=='setRenderPriority' and raw=='HIGH':
            x['value_kind']='enum'; x['value']='android.webkit.WebSettings.RenderPriority.HIGH'
        elif x.get('name')=='setDefaultZoom' and raw=='MEDIUM':
            x['value_kind']='enum'; x['value']='android.webkit.WebSettings.ZoomDensity.MEDIUM'
        elif x.get('name')=='setLayoutAlgorithm' and raw=='NARROW_COLUMNS':
            x['value_kind']='enum'; x['value']='android.webkit.WebSettings.LayoutAlgorithm.NARROW_COLUMNS'
        elif x.get('name')=='setTextSize' and raw=='NORMAL':
            x['value_kind']='enum'; x['value']='android.webkit.WebSettings.TextSize.NORMAL'
        else:
            x['value_kind']='dynamic'; x['value']=None

# Symbol validation is exact string equality against the independent DEX export.
targets={x.get('normalized_signature') for x in rows if x.get('normalized_signature')}
targets|={x.get('bridge_method') for x in rows if x.get('bridge_method')}
targets.discard('')
found=set()
with open(HERE.parents[2]/'test/runs/symbols/mango.jsonl',errors='replace') as f:
    for line in f:
        if not targets-found: break
        obj=json.loads(line)
        for m in obj.get('methods',[]):
            s=m.get('signature','').replace('-\u003e','->')
            if s in targets: found.add(s)

for x in rows:
    if x['kind']=='bridge':
        x.setdefault('registration_name',x.get('name',''))
    ns=x.get('normalized_signature',''); bm=x.get('bridge_method','')
    x['symbol_status']=('external_api' if ns.startswith(('Landroid/','Lcom/tencent/smtt/')) else ('confirmed' if ns and ns in found else ('not_applicable' if not ns else 'unresolved')))
    if x['kind']=='bridge' and x.get('value')=='registered':
        # Registration remains true, but only a symbol-confirmed reflected target
        # is an exposed member. registerHandler itself is never that member.
        if not bm or bm not in found:
            x['normalized_signature']=''
            x['bridge_method']=''
            x['signature']=''
            x['implementation']=None
            x['binding_status']='registered-target-unknown'

with (HERE/'canonical-facts.jsonl').open('w') as f:
    for x in rows:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')

from collections import Counter
print('rows',len(rows),'kinds',dict(Counter(x['kind'] for x in rows)),'symbols',dict(Counter(x['symbol_status'] for x in rows)),'settings_values',dict(Counter(x['value_kind'] for x in rows if x['kind']=='setting')),'unknown_bridge_targets',sum(x['binding_status']=='registered-target-unknown' for x in rows))
