#!/usr/bin/env python3
import json,re
from pathlib import Path
H=Path(__file__).resolve().parent; R=H.parents[2]
names=['getSMSNumber','getSMSCode','payWithWeChat','share','showCustomShareMenus','invokeH5Callback','webviewBecomeActive','webviewEnterBackground']
src=(R/'test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/imgo/h5/ImgoWebView.java').read_text()
impl=(R/'test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/h5/ImgoWebJavascriptImpl.java').read_text()
for n in names: assert f'registerHandler("{n}", this)' in src
assert 'ImgoWebJavascriptImpl.class.getDeclaredMethod(this.f56347a, String.class).invoke(ImgoWebJavascriptImpl.this, str)' in impl
owner=None
for line in open(R/'test/runs/symbols/mango.jsonl',errors='replace'):
 o=json.loads(line)
 if o.get('type')=='Lcom/mgtv/h5/ImgoWebJavascriptImpl;':owner=o;break
assert owner
methods={n:[m['signature'].replace('-\\u003e','->') for m in owner['methods'] if m['name']==n] for n in names}
compatible={n:[s for s in ss if '(Ljava/lang/String;)' in s] for n,ss in methods.items()}
assert not any(compatible.values()),compatible
facts=[json.loads(x) for x in (H/'canonical-facts.jsonl').read_text().splitlines()]
rows=[x for x in facts if x.get('binding_status') in {'registered-target-unknown','registered-no-compatible-endpoint'} and x.get('registration_name') in names]
assert len(rows)==56 and len({x['activity'] for x in rows})==7
print(json.dumps({'registrations':len(rows),'hosts':7,'names':names,'exact_declared_string_endpoints':0,'same_name_other_descriptors':methods},ensure_ascii=False))
