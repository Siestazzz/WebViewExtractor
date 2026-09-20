#!/usr/bin/env python3
import json,collections
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];SHA='65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5'
report=json.load(open(R/'test/runs/v4e/com.hunantv.imgo.activity/0/output/capabilities.json'));assert report['apk_sha256']==SHA
prior={x['activity']:x for x in map(json.loads,open(H/'v4d-ownership.jsonl'))};assert len(report['activities'])==82
out=[]
for item in report['activities']:
 a=item['activity']; assert a in prior,a
 row=dict(prior[a]);row['fact_count']=len(item['facts']);row['reason']='stable source/DEX chain reused from independent v4d review: '+row['reason'];out.append(row)
assert len(out)==82 and len({x['activity'] for x in out})==82
with open(H/'v4e-ownership.jsonl','w') as f:
 for x in out:f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n')
c=collections.Counter(x['verdict'] for x in out);assert c=={'valid':77,'uncertain':5}
removed=sorted(set(prior)-{x['activity'] for x in out})
known_wrong={'com.mgtv.agent.ui.MgAgentActivity','com.mgtv.agent.ui.MgAgentFloatActivity','com.mgtv.libvip.activity.VipChannelPreviewActivity'};assert known_wrong<=set(removed)
lines=['# v4e Mango ownership delta','',f'v4e emits 82 Activities, all present in the independently reviewed v4d set. No new Activity or ownership chain appears. Stable source/DEX verdicts were reused for all 82 rows.','',f"Result: {c['valid']} valid, {c['uncertain']} uncertain, {c['wrong']} wrong.",'','All three known v4d wrong associations disappeared: `MgAgentActivity`, `MgAgentFloatActivity`, and `VipChannelPreviewActivity`. The five Pangle reward/middle Activities remain uncertain; v4e does not add source evidence resolving their base/factory chain.','',f'v4e removed {len(removed)} v4d Activities:','']+['- `'+x+'`' for x in removed]+['','Seven removed Activities previously had valid ownership chains (NFT, channel, or related view carriers). Their absence is an output recall change, not an ownership correction. No retained verdict changed.']
(H/'v4e-ownership.md').write_text('\n'.join(lines)+'\n');print(dict(c),removed)
