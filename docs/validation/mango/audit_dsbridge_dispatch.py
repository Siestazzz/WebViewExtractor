#!/usr/bin/env python3
"""Validate the generic DSBridge dispatch rule against the independent DEX export."""
import json
from pathlib import Path

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
rule=json.loads((HERE/'dsbridge-dispatch.json').read_text())
types={
 'Lcom/mgtb/money/web/dsbridge/DWebView;',
 'Lcom/mgtb/money/web/dsbridge/DWebView$InnerJavascriptInterface;',
 'Lcom/mgtb/money/web/dsbridge/DWebView$1;',
 'Lcom/mgtb/money/web/webview/DefaultWebBridgeAPI;',
}
dex={}
with (ROOT/'test/runs/symbols/mango.jsonl').open(errors='replace') as stream:
    for line in stream:
        obj=json.loads(line)
        if obj.get('type') in types:
            dex[obj['type']]=obj
            if len(dex)==len(types):break
assert dex.keys()==types
methods={m['signature'].replace('-\u003e','->'):m for obj in dex.values() for m in obj.get('methods',())}
assert rule['transport']['entrypoint'] in methods
assert 'Landroid/webkit/JavascriptInterface;' in methods[rule['transport']['entrypoint']]['annotations']
assert rule['registry']['registration'] in methods

def endpoints(owner):
    return [m for m in dex[owner]['methods'] if 'Landroid/webkit/JavascriptInterface;' in m.get('annotations',())]

internal=endpoints('Lcom/mgtb/money/web/dsbridge/DWebView$1;')
default=endpoints('Lcom/mgtb/money/web/webview/DefaultWebBridgeAPI;')
assert len(internal)==5,len(internal)
assert len(default)==42,len(default)
allowed=('(Ljava/lang/Object;)','(Ljava/lang/Object;Lcom/mgtb/money/web/dsbridge/a;)')
for method in internal+default:
    descriptor=method['signature'].split('->',1)[1]
    assert any(shape in descriptor for shape in allowed),method
print(json.dumps({'transport':1,'internal_endpoints':len(internal),'default_endpoints':len(default),'reflection_endpoints':len(internal)+len(default)},sort_keys=True))
