#!/usr/bin/env python3
"""Freeze an append-only semantic union without multiplying identical evidence rows."""
import argparse,hashlib,json,pathlib

def canonical(row):
 return json.dumps(row,ensure_ascii=False,sort_keys=True,separators=(',',':'))

def union_sources(sources):
 unique={};evidence=[];input_rows=0
 for source in sources:
  source=pathlib.Path(source);raw=source.read_bytes();rows=[json.loads(line) for line in raw.splitlines() if line.strip()]
  if any(not isinstance(row,dict) for row in rows):raise ValueError('Expected JSON objects: '+str(source))
  evidence.append(dict(path=str(source),sha256=hashlib.sha256(raw).hexdigest(),rows=len(rows)))
  input_rows+=len(rows)
  for row in rows:unique.setdefault(canonical(row),row)
 # Preserve first occurrence order, including failed, unknown and rejected facts.
 output=('\n'.join(unique)+'\n').encode('utf-8') if unique else b''
 return output,dict(sources=evidence,input_rows=input_rows,unique_rows=len(unique),identical_rows_deduplicated=input_rows-len(unique),unique_set_sha256=hashlib.sha256('\n'.join(sorted(unique)).encode('utf-8')).hexdigest())

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--previous',required=True);p.add_argument('--append',action='append',default=[],metavar='APP=JSONL');p.add_argument('--out',required=True);a=p.parse_args()
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
  raw,meta=union_sources([row['oracle'],*extra[row['app']]])
  target=dest/(row['app']+'.jsonl');new=dict(package=row['package'],app=row['app'],oracle=str(target),sha256=hashlib.sha256(raw).hexdigest(),**meta,note='All distinct full evidence rows retained; exact semantic duplicates only collapsed. No filtering by verdict, kind, host, failure or acceptance. Development set, not holdout.')
  prepared.append((target,raw,new))
 dest.mkdir(parents=True)
 for target,raw,row in prepared:target.write_bytes(raw)
 (dest/'manifest.json').write_text(json.dumps([row for _,_,row in prepared],ensure_ascii=False,indent=2)+'\n')
 print(json.dumps(dict(apps=len(prepared),input_rows=sum(r['input_rows'] for _,_,r in prepared),unique_rows=sum(r['unique_rows'] for _,_,r in prepared))))
if __name__=='__main__':main()
