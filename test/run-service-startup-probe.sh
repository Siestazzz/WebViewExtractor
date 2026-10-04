#!/usr/bin/env bash
set -euo pipefail
repo_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
probe_jar=${1:-"$repo_root/test/runs/generic-v11.jar"}
probe_out=${2:-"$repo_root/test/runs/news-startup-probe-v11"}
mkdir -p "$probe_out/classes"
javac -cp "$probe_jar" -d "$probe_out/classes" "$repo_root/src/test/java/org/example/ServiceConstructorArrayProbe.java" "$repo_root/src/test/java/org/example/ServiceStartupRegistrationProbe.java"
java -cp "$probe_out/classes:$probe_jar" org.example.ServiceStartupRegistrationProbe > "$probe_out/results.txt"
python3 - "$probe_jar" "$probe_out" <<'PY'
import pathlib,json,hashlib,sys
jar,out=map(pathlib.Path,sys.argv[1:]);rows={}
for line in (out/'results.txt').read_text().splitlines():
 name,body=line.split('\t',1)
 if name.endswith(' diagnostics'):rows[name[:-12]]['diagnostics']=body
 else:rows[name]={'facts_by_kind':{k:body.count('kind='+k+',') for k in ('bridge','setting','callback')},'raw_report':body}
for name,row in rows.items():row['harness_scope']='Activity explicitly invokes startup' if '/application-' not in name else 'Application attach exists but harness does not explicitly seed its lifecycle';row['all_categories_present']=all(row['facts_by_kind'].values())
for name,row in rows.items():
 row['expected_source_positive']=False if name.endswith('reflect-noinvoke') else (None if '/application-' in name else True)
 row['verdict']=('wrong' if row['all_categories_present'] else 'correct_negative') if row['expected_source_positive'] is False else ('unseeded_harness_unresolved' if row['expected_source_positive'] is None else ('matched' if row['all_categories_present'] else 'missing'))
(out/'results.json').write_text(json.dumps({'schema_version':1,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'cases':rows,'acceptance_claim':False},indent=2)+'\n')
for name,row in rows.items():print(name,row['facts_by_kind'])
PY
