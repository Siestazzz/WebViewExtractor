#!/usr/bin/env bash
set -euo pipefail
repo_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
probe_jar=${1:-"$repo_root/test/runs/generic-v10.jar"}
probe_out=${2:-"$repo_root/test/runs/news-constructor-probe-v10"}
mkdir -p "$probe_out/classes"
javac -cp "$probe_jar" -d "$probe_out/classes" "$repo_root/src/test/java/org/example/ServiceConstructorArrayProbe.java"
java -cp "$probe_out/classes:$probe_jar" org.example.ServiceConstructorArrayProbe > "$probe_out/results.txt"
python3 - "$probe_jar" "$probe_out" "$repo_root/src/test/java/org/example/ServiceConstructorArrayProbe.java" <<'PY'
import sys,pathlib,json,hashlib
jar,out,source=map(pathlib.Path,sys.argv[1:]);rows={}
for line in (out/'results.txt').read_text().splitlines():
 name,body=line.split('\t',1)
 if name.endswith(' diagnostics'):rows[name[:-12]]['diagnostics']=body
 elif name.endswith(' relevance'):rows[name[:-10]]['creator_relevant']=body=='true'
 elif name.endswith(' creatorSummary'):rows[name[:-15]]['creator_summary']=body
 else:rows[name]={'facts_by_kind':{k:body.count('kind='+k+',') for k in ('bridge','callback','setting')},'raw_report':body}
for name,row in rows.items():
 row['expected_source_positive']=not(name.startswith('map-') and name.endswith('missingkey'))
 row['three_capabilities_present']=all(row['facts_by_kind'].values())
 row['verdict']=('matched' if row['three_capabilities_present'] else 'missing') if row['expected_source_positive'] else ('wrong' if row['three_capabilities_present'] else 'correct_negative')
assert rows['nomap-nocache-direct']['three_capabilities_present'],'Direct construction baseline must bind three capabilities.'
result={'schema_version':1,'jar':str(jar.resolve()),'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'run_is_acceptance':False,'cases':rows}
(out/'results.json').write_text(json.dumps(result,indent=2)+'\n')
for name,row in rows.items():print(name,row['facts_by_kind'],row['verdict'])
PY
