#!/usr/bin/env python3
"""Freeze an append-only semantic union without multiplying identical evidence rows."""
import argparse,hashlib,json,pathlib

def canonical(row):
 return json.dumps(row,ensure_ascii=False,sort_keys=True,separators=(',',':'))

def validate_positive_shape(row, source, line):
 constraint=row.get('webview_constraint')
 if constraint is not None and (not isinstance(constraint,dict) or set(constraint)!={'types'} or not isinstance(constraint['types'],list) or not constraint['types'] or not all(isinstance(t,str) and t for t in constraint['types'])):
  raise ValueError(f'Unsupported source WebView constraint; preserve extra binding evidence outside matcher constraint: {source}:{line}')
 if row.get('kind')=='callback_registration':
  api=row.get('normalized_api') or row.get('normalized_signature') or ''
  owner=api.split(';->',1)[0]
  if owner in {'Landroid/webkit/WebViewClient','Landroid/webkit/WebChromeClient','Landroid/webkit/WebViewRenderProcessClient','Lcom/tencent/smtt/sdk/WebViewClient','Lcom/tencent/smtt/sdk/WebChromeClient'}:
   raise ValueError(f'Callback member contract used as registration API: {source}:{line}: {api}')

def union_sources(sources, require_append_verdicts=False):
 unique={};evidence=[];input_rows=0
 for source_index,source in enumerate(sources):
  source=pathlib.Path(source);raw=source.read_bytes();rows=[json.loads(line) for line in raw.splitlines() if line.strip()]
  if any(not isinstance(row,dict) for row in rows):raise ValueError('Expected JSON objects: '+str(source))
  evidence.append(dict(path=str(source),sha256=hashlib.sha256(raw).hexdigest(),rows=len(rows)))
  input_rows+=len(rows)
  for line,row in enumerate(rows,1):
   key=canonical(row)
   if require_append_verdicts and source_index>0 and key not in unique and type(row.get('positive_acceptance')) is not bool:
    raise ValueError(f'New source fact requires explicit boolean positive_acceptance: {source}:{line}')
   if require_append_verdicts and source_index>0 and key not in unique and row.get('positive_acceptance') is True and row.get('kind') not in {'activity_binding','bridge','bridge_method','message_handler','setting','callback','callback_registration','webview_operation'}:
    raise ValueError(f'Unsupported positive fact kind: {source}:{line}: {row.get("kind")}')
   if require_append_verdicts and source_index>0 and key not in unique and row.get("positive_acceptance") is True:validate_positive_shape(row,source,line)
   unique.setdefault(key,row)
 # Preserve first occurrence order, including failed, unknown and rejected facts.
 output=('\n'.join(unique)+'\n').encode('utf-8') if unique else b''
 return output,dict(sources=evidence,input_rows=input_rows,unique_rows=len(unique),identical_rows_deduplicated=input_rows-len(unique),unique_set_sha256=hashlib.sha256('\n'.join(sorted(unique)).encode('utf-8')).hexdigest())

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--previous',required=True);p.add_argument('--append',action='append',default=[],metavar='APP=JSONL');p.add_argument('--out',required=True);p.add_argument('--require-append-verdicts',action='store_true',help='Require an explicit source verdict on each new appended row; preserve legacy rows unchanged');a=p.parse_args()
 previous=json.loads(pathlib.Path(a.previous).read_text());names={r['app'] for r in previous};extra={name:[] for name in names}
 for item in a.append:
  name,sep,path=item.partition('=')
  if not sep or name not in names:p.error('Unknown app or malformed --append: '+item)
  extra[name].append(pathlib.Path(path))
 dest=pathlib.Path(a.out)
 if dest.exists():p.error('Refusing to replace an existing frozen directory')
 # Validate/read all sources before creating the new freeze.
 prepared=[]
 for row in previous:
  if hashlib.sha256(pathlib.Path(row['oracle']).read_bytes()).hexdigest()!=row.get('sha256'):
   raise ValueError('Previous oracle no longer matches frozen manifest: '+row['oracle'])
  raw,meta=union_sources([row['oracle'],*extra[row['app']]],a.require_append_verdicts)
  target=dest/(row['app']+'.jsonl');new=dict(package=row['package'],app=row['app'],oracle=str(target),sha256=hashlib.sha256(raw).hexdigest(),**meta,note='All distinct full evidence rows retained; exact semantic duplicates only collapsed. No filtering by verdict, kind, host, failure or acceptance. Development set, not holdout.')
  prepared.append((target,raw,new))
 dest.mkdir(parents=True)
 for target,raw,row in prepared:target.write_bytes(raw)
 (dest/'manifest.json').write_text(json.dumps([row for _,_,row in prepared],ensure_ascii=False,indent=2)+'\n')
 print(json.dumps(dict(apps=len(prepared),input_rows=sum(r['input_rows'] for _,_,r in prepared),unique_rows=sum(r['unique_rows'] for _,_,r in prepared))))
if __name__=='__main__':main()
