#!/usr/bin/env python3
"""Expand shared capability groups through Activity links into flat JSONL facts."""
import argparse, json, re, subprocess
from pathlib import Path

def setting_name(normalized_api):
    match=re.search(r';->([^\(]+)\(',normalized_api or '')
    return match.group(1) if match else None

def setting_value(expression):
    """Return comparison value, kind, and lossless source expression."""
    if isinstance(expression,(bool,int,float)) or expression is None:
        return expression,'literal',json.dumps(expression,ensure_ascii=False)
    source=str(expression).strip()
    if source=='true': return True,'literal',source
    if source=='false': return False,'literal',source
    if source=='null': return None,'literal',source
    if re.fullmatch(r'-?\d+',source): return int(source),'literal',source
    if re.fullmatch(r'-?(?:\d+\.\d*|\d*\.\d+)(?:[eE][+-]?\d+)?[fFdD]?',source): return float(source.rstrip('fFdD')),'literal',source
    if re.fullmatch(r'"(?:\\.|[^"\\])*"',source):
        try: return json.loads(source),'literal',source
        except json.JSONDecodeError: pass
    if re.fullmatch(r'(?:[A-Za-z_$][\w$]*\.)+[A-Z][A-Z0-9_]*',source): return source,'enum',source
    return None,'dynamic',source

def walk(value, path=()):
    if isinstance(value, dict):
        if ('evidence' in value or 'registry_evidence' in value) and any(k in value for k in ('signature','name','name_expression','registration_expression','value_expression','type','tag')):
            item=dict(value)
            if 'evidence' not in item and 'registry_evidence' in item: item['evidence']=item['registry_evidence']
            yield path, item
        for k,v in value.items():
            if k not in ('evidence','registry_evidence','tag_evidence'): yield from walk(v,path+(k,))
    elif isinstance(value,list):
        for i,v in enumerate(value): yield from walk(v,path+(str(i),))

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('oracle_dir',type=Path); ap.add_argument('--output',type=Path); ap.add_argument('--canonical',type=Path); ap.add_argument('--apk',type=Path); ap.add_argument('--apkanalyzer',type=Path); ap.add_argument('--symbols',type=Path)
    a=ap.parse_args(); groups={x['group_id']:x for x in map(json.loads,a.oracle_dir.joinpath('groups.jsonl').read_text().splitlines())}; links=list(map(json.loads,a.oracle_dir.joinpath('activity-groups.jsonl').read_text().splitlines()))
    out=a.output or a.oracle_dir/'facts-expanded.jsonl'
    with out.open('w') as f:
        for link in links:
            g=groups[link['group_id']]
            for path,cap in walk(g['capabilities']):
                if path and path[0] in link.get('exclude_capabilities',[]): continue
                row={'activity':link['activity'],'group_id':g['group_id'],'family':g['family'],'evaluation_tier':g['evaluation_tier'],'capability_path':'.'.join(path),'capability':cap,'host_evidence':link['host_evidence'],'binding_mode':link['binding_mode'],'group_unknown':g.get('unknown',[]),'apk_sha256':g['apk_sha256']}
                f.write(json.dumps(row,ensure_ascii=False)+'\n')
    canonical=[]
    linked_groups={x['group_id'] for x in links}
    canonical_links=links+[{'activity':None,'group_id':gid,'host_evidence':[],'binding_mode':'unattributed'} for gid in groups if gid not in linked_groups]
    for link in canonical_links:
        g=groups[link['group_id']]
        common={'activity':link['activity'],'apk_sha256':g['apk_sha256'],'host_evidence':link['host_evidence'],'group_id':g['group_id'],'evaluation_tier':g['evaluation_tier']}
        for setting in g['capabilities'].get('settings',[]):
            value,value_kind,source_expression=setting_value(setting['value_expression'])
            canonical.append({**common,'kind':'setting','name':setting_name(setting['normalized_api']),'registration_name':None,'implementation':'android.webkit.WebSettings','normalized_signature':None,'normalized_api':setting['normalized_api'],'value':value,'value_kind':value_kind,'source_expression':source_expression,'evidence':[setting['evidence']]})
        for bridge in g['capabilities'].get('bridges',[]):
            registration=bridge.get('name') or bridge.get('name_expression')
            bev=bridge.get('registry_evidence'); canonical.append({**common,'kind':'bridge','name':bridge.get('name'),'registration_name':registration,'implementation':bridge.get('type'),'normalized_signature':None,'normalized_api':'Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V','value':bridge.get('name_expression',bridge.get('name')),'evidence':([bev] if bev else [])})
            for method in bridge.get('methods',[]):
                canonical.append({**common,'kind':'bridge_method','name':re.search(r';->([^\(]+)',method['normalized_signature']).group(1),'registration_name':registration,'implementation':bridge.get('type'),'normalized_signature':method['normalized_signature'],'normalized_api':None,'value':None,'evidence':[method['evidence']]})
        for client_name in ('webview_client','webchrome_client'):
            if client_name in link.get('exclude_capabilities',[]): continue
            client=g['capabilities'].get(client_name,{})
            for cb in client.get('overrides',[]):
                canonical.append({**common,'kind':'callback','name':re.search(r';->([^\(]+)',cb['normalized_signature']).group(1),'registration_name':None,'implementation':client.get('type'),'normalized_signature':cb['normalized_signature'],'normalized_api':None,'value':None,'evidence':[cb['evidence']]})
    # Include independently reviewed direct facts that do not come through a shared group.
    reviewed=a.oracle_dir/'facts.jsonl'
    if reviewed.exists():
        linked={(x['activity'],x['kind'],x.get('normalized_signature'),x.get('normalized_api'),x.get('name')) for x in canonical}
        for fact in map(json.loads,reviewed.read_text().splitlines()):
            if fact['kind']=='activity_binding': continue
            name=fact['name']
            if fact['kind'] in ('bridge_method','callback') and fact.get('normalized_signature'): name=re.search(r';->([^\(]+)',fact['normalized_signature']).group(1)
            value=fact.get('value'); value_kind=None; source_expression=None
            if fact['kind']=='setting':
                name=setting_name(fact.get('normalized_api')) or name
                value,value_kind,source_expression=setting_value(value)
            row={'activity':fact['activity'],'kind':fact['kind'],'name':name,'registration_name':(fact['name'] if fact['kind'] in ('bridge','bridge_method') else None),'implementation':fact.get('implementation'),'normalized_signature':fact.get('normalized_signature'),'normalized_api':fact.get('normalized_api'),'value':fact.get('value'),'apk_sha256':fact['apk_sha256'],'evidence':fact['evidence'],'host_evidence':[],'group_id':None,'evaluation_tier':'direct_required'}
            row['value']=value
            if fact['kind']=='setting': row.update(value_kind=value_kind,source_expression=source_expression)
            key=(row['activity'],row['kind'],row.get('normalized_signature'),row.get('normalized_api'),row.get('name'))
            if key not in linked: canonical.append(row); linked.add(key)
    # Optional exact APK method-table validation. No extractor output is read.
    validation={'checked':False,'sources':[],'method_fact_count':0,'unique_signature_count':0,'present':0,'missing':[]}
    if a.apk and a.apkanalyzer:
        text=subprocess.run([str(a.apkanalyzer),'dex','packages','--defined-only',str(a.apk)],check=True,text=True,stdout=subprocess.PIPE).stdout
        table=set()
        for line in text.splitlines():
            m=re.match(r'M\s+\w\s+(?:\d+\s+){3}(\S+)\s+(\S+)\s+([^\s(]+)\((.*)\)$',line)
            if m: table.add((m.group(1),m.group(3),tuple(x for x in m.group(4).split(',') if x),m.group(2)))
        def parse_desc(sig):
            m=re.match(r'L([^;]+);->([^\(]+)\((.*)\)(.+)',sig); owner=m.group(1).replace('/','.')
            def one(s,pos):
                dims=0
                while s[pos]=='[': dims+=1; pos+=1
                prim={'V':'void','Z':'boolean','B':'byte','C':'char','S':'short','I':'int','J':'long','F':'float','D':'double'}
                if s[pos]=='L': end=s.index(';',pos); typ=s[pos+1:end].replace('/','.'); pos=end+1
                else: typ=prim[s[pos]]; pos+=1
                return typ+'[]'*dims,pos
            ps=[]; raw=m.group(3); pos=0
            while pos<len(raw): typ,pos=one(raw,pos); ps.append(typ)
            ret,_=one(m.group(4),0); return owner,m.group(2),tuple(ps),ret
        validation['checked']=True
        validation['sources'].append('apkanalyzer dex packages --defined-only')
        for row in canonical:
            sig=row.get('normalized_signature')
            if not sig: continue
            validation['method_fact_count']+=1
            key=parse_desc(sig); row['signature_in_apk']=key in table
            if row['signature_in_apk']: validation['present']+=1
            else: validation['missing'].append({'activity':row['activity'],'kind':row['kind'],'normalized_signature':sig,'parsed_key':key})
    if a.symbols:
        symbol_methods=set()
        with a.symbols.open() as symbol_file:
            for line in symbol_file:
                symbol_methods.update(m['signature'] for m in json.loads(line).get('methods',[]))
        sigs={row['normalized_signature'] for row in canonical if row.get('normalized_signature')}
        missing=sorted(sigs-symbol_methods)
        validation['checked']=True; validation['sources'].append(str(a.symbols)); validation['unique_signature_count']=len(sigs)
        validation['dex_symbols_present']=len(sigs)-len(missing); validation['dex_symbols_missing']=missing
    canonical_path=a.canonical or a.oracle_dir/'canonical-facts.jsonl'
    with canonical_path.open('w') as f:
        for row in canonical: f.write(json.dumps(row,ensure_ascii=False)+'\n')
    (a.oracle_dir/'normalized-signature-validation.json').write_text(json.dumps(validation,ensure_ascii=False,indent=2)+'\n')

if __name__=='__main__': main()
