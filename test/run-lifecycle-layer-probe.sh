#!/usr/bin/env bash
set -euo pipefail
repo_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
probe_jar=${1:-"$repo_root/test/runs/generic-v12.jar"}
probe_out=${2:-"$repo_root/test/runs/xigua-layer-probe-v12"}
mkdir -p "$probe_out/classes"
javac -cp "$probe_jar" -d "$probe_out/classes" "$repo_root/src/test/java/org/example/ServiceConstructorArrayProbe.java" "$repo_root/src/test/java/org/example/LifecycleContainerFactoryProbe.java" "$repo_root/src/test/java/org/example/LifecycleLayerBoundaryProbe.java"
java -cp "$probe_out/classes:$probe_jar" org.example.LifecycleLayerBoundaryProbe > "$probe_out/results.txt"
python3 - "$probe_jar" "$probe_out" <<'PY'
import json,pathlib,hashlib,sys
jar,out=map(pathlib.Path,sys.argv[1:]);rows={}
for line in (out/'results.txt').read_text().splitlines():
 name,body=line.split('\t',1)
 if name.endswith(' diagnostics'):rows[name[:-12]]['diagnostics']=body
 elif name.endswith(' seconds'):rows[name[:-8]]['analysis_seconds']=float(body)
 elif name.endswith(' factorySummary'):rows[name[:-15]]['factory_summary']=body
 else:rows[name]={'facts_by_kind':{k:body.count('kind='+k+',') for k in ('bridge','setting','callback')},'raw_report':body}
for name,row in rows.items():
 row['expected_source_positive']=name != 'fragment-uninstalled'
 row['three_categories_present']=all(row['facts_by_kind'].values())
 row['verdict']=('matched' if row['three_categories_present'] else 'missing') if row['expected_source_positive'] else ('wrong' if row['three_categories_present'] else 'correct_negative')
assert rows['fragment-create']['three_categories_present'],'Installed Fragment creation control must bind three categories.'
assert rows['fragment-activity-created']['three_categories_present'],'Installed Fragment callback control must bind three categories.'
(out/'results.json').write_text(json.dumps({'schema_version':1,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'cases':rows,'acceptance_claim':False,'analysis_budget_seconds_per_case':30},indent=2)+'\n')
for name,row in rows.items():print(name,row['facts_by_kind'],row['verdict'],row['analysis_seconds'])
PY
