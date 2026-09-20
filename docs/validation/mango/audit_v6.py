#!/usr/bin/env python3
import collections, json
from pathlib import Path

H=Path(__file__).resolve().parent; R=H.parents[2]
rp=R/'test/runs/v6/com.hunantv.imgo.activity/0/output/capabilities.json'
report=json.load(rp.open()); current={x['activity']:x for x in report['activities']}; assert len(current)==82
v4={x['activity']:x for x in map(json.loads,(H/'v4e-ownership.jsonl').open())}
v5={x['activity']:x for x in map(json.loads,(H/'v5-ownership.jsonl').open())}
v4d={x['activity']:x for x in map(json.loads,(H/'v4d-ownership.jsonl').open())}
added=sorted(set(current)-set(v5)); removed=sorted(set(v5)-set(current))
assert added==['com.mgtv.ui.channel.selected.ChannelBackyardActivity','com.mgtv.ui.channel.selected.ChannelSecondIndexActivity']
assert removed==[]
ownership=[]
for a,item in current.items():
    prior=v5.get(a) or v4d[a]
    row=dict(prior); row['fact_count']=len(item['facts'])
    row['reason']='stable source/DEX chain reused from independent v5/v4d review: '+row['reason']
    ownership.append(row)
ownership.sort(key=lambda x:x['activity']); oc=collections.Counter(x['verdict'] for x in ownership)
assert oc=={'valid':77,'uncertain':5}
(H/'v6-ownership.jsonl').write_text(''.join(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n' for x in ownership))

e5=json.load(open(R/'docs/validation/v5-mango-evaluation.json')); e6=json.load(open(R/'docs/validation/v6-mango-evaluation.json'))
def key(x): return (x['activity'],x['kind'],x['name'],x.get('signature',''))
m5={key(x):x for x in e5['missing']}; m6={key(x):x for x in e6['missing']}
gained=[x for k,x in m5.items() if k not in m6]
assert len(gained)==44
assert collections.Counter(x['activity'] for x in gained)=={
 'com.mgadplus.brower.CustomWebActivity':25,
 'com.mgtv.ui.live.mglive.webview.WebViewActivity':19}

# Validate every gained registration against the emitted concrete implementation/member.
emitted={}
for host in ['com.mgadplus.brower.CustomWebActivity','com.mgtv.ui.live.mglive.webview.WebViewActivity']:
 for f in current[host]['facts']:
  if f.get('kind')=='message_bridge' and f.get('members'):
   emitted.setdefault((host,f.get('registration_name')),set()).update(m['signature'] for m in f['members'])
for x in gained:
 assert x['signature'] in emitted[(x['activity'],x['name'])],x

# Independent DEX confirmation: owner class and exact method descriptor exist.
wanted={x['signature'] for x in gained}; found=set(); user_info_constant=False
with open(R/'test/runs/symbols/mango.jsonl') as f:
 for line in f:
  obj=json.loads(line)
  if obj.get('type')=='Lcom/mgtv/flutter/channel/MgtvMethodChannel;':
   user_info_constant=any(x.get('name')=='L' and x.get('value')=='getUserInfo' for x in obj.get('fields',[]))
  for m in obj.get('methods',[]):
   sig=m['signature'].replace('->',';->') if ';->' not in m['signature'] else m['signature']
   # DexSymbols serializes owner separator as -> already; canonical uses ;->.
   if m['signature'] in wanted: found.add(m['signature'])
assert found==wanted and user_info_constant,(len(found),len(wanted),sorted(wanted-found)[:2],user_info_constant)

src_mg=(R/'test/decompiled/com.hunantv.imgo.activity/sources/com/mgadplus/brower/ImgoAdWebView.java').read_text()
src_live=(R/'test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/live/mglive/webview/a.java').read_text()
for x in gained:
 src=src_mg if 'mgadplus' in x['activity'] else src_live
 # getUserInfo is represented by the independently resolved MgtvMethodChannel.L constant.
 assert '"'+x['name']+'"' in src or (x['name']=='getUserInfo' and 'MgtvMethodChannel.L' in src)
assert src_mg.count('void handler(String str, com.mgadplus.brower.jsbridge.d dVar)')>=25
assert src_live.count('void a(String str, cn0.d dVar)')>=19

groups=collections.defaultdict(list)
for x in e6['missing']:
 a=x['activity']
 fw=('Loading' if a=='com.platform.oms.ui.LoadingWebActivity' else 'mgadplus' if 'mgadplus' in a else
     'mglive' if 'mglive' in a else 'Pangle' if 'bytedance' in a else 'OPOS' if 'opos' in a else
     'WebContainer' if 'WebContainer' in a else a)
 groups[fw].append(x)
assert len(e6['missing'])==31 and len(groups['Loading'])==15

summary={'report':str(rp.relative_to(R)),'activities':82,
 'ownership':{'counts':dict(oc),'added_since_v5':added,'removed_since_v5':removed},
 'new_message_handler_matches':{'total':44,'mgadplus':25,'mglive':19,
  'validation':['registration literal in decompiled source','exact concrete interface member in independent DEX symbols','registration helper/map dispatch and host reachability from framework audits','exact emitted member signature equals oracle signature']},
 'remaining_missing':{k:{'count':len(v),'by_kind':dict(collections.Counter(x['kind'] for x in v)),'items':v} for k,v in sorted(groups.items())}}
(H/'v6-validation.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
(H/'v6-ownership.md').write_text('# v6 Mango ownership delta\n\nv6 emits 82 Activities: 77 valid and five uncertain. It retains every v5 Activity and restores two independently valid v4d channel hosts: `ChannelBackyardActivity` and `ChannelSecondIndexActivity`. There are no removals and no retained verdict changes. Per-host rows are in `v6-ownership.jsonl`.\n')
(H/'v6-validation.md').write_text('''# v6 Mango focused validation

The 44 newly matched message-handler members are supported by source and independent DEX evidence, rather than by the aggregate score. `CustomWebActivity` owns field `E: ImgoAdWebView`; its initialization reaches `ImgoAdWebView.J()`, which passes 25 concrete anonymous handlers to `BridgeWebView.t(String, jsbridge.a)`. Each emitted member is the concrete class implementation of `jsbridge.a.handler(String, d)`, and each exact owner/name/descriptor exists in `mango.jsonl`. The one source constant, `MgtvMethodChannel.L`, has DEX value `getUserInfo`.

`mglive.webview.WebViewActivity` directly owns the mglive `BridgeWebView` and calls helper `webview.a.p(...)`. That helper passes 19 concrete handlers to `BridgeWebView.n(String, cn0.a)`. Every newly matched member is the corresponding implementation of `cn0.a.a(String, cn0.d)`, confirmed by exact DEX descriptors. The twentieth registration, `checkUpdate`, remains missing because its target is still unresolved in v6; it was not counted among the 19 verified gains.

The report still contains 31 residual rows. Loading is the largest systematic gap: 14 concrete `JSCommondMethod` message-handler methods plus one dynamic method-class registration fact. The other groups are seven mgadplus callbacks, four WebContainer settings, three Pangle settings, one mglive `checkUpdate` handler, one OPOS dynamic bridge registration, and the Loading dynamic registration. The 98.53% aggregate bridge recall therefore does not establish Loading framework coverage.

Ownership has 82 outputs: 77 valid and five uncertain. Relative to v5, `ChannelBackyardActivity` and `ChannelSecondIndexActivity` return with their independently verified v4d ownership chains; no Activity is removed and no retained verdict changes. Detailed residual rows and machine-checkable validation results are in `v6-validation.json`; ownership rows are in `v6-ownership.jsonl`.
''')
print(json.dumps(summary,ensure_ascii=False))
