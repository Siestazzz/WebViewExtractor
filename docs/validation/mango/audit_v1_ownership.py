#!/usr/bin/env python3
import json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
SRC=ROOT/'test/decompiled/com.hunantv.imgo.activity/sources'
IN=ROOT/'test/runs/v1/com.hunantv.imgo.activity/0/output/capabilities.json'
OUT=Path(__file__).resolve().parent
data=json.loads(IN.read_text())
known={}
for line in (OUT/'inventory.md').read_text().splitlines():
    m=re.match(r'\| `([^`]+)` \| ([^|]+) \|',line)
    if m: known[m.group(1)]=m.group(2).strip()

def source_for(cls):
    p=SRC/(cls.replace('.','/')+'.java')
    return p if p.exists() else None

rows=[]
deep_valid={
 'com.mgtv.dalang.DalangLiveActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/dalang/fragment/DalangFragment.java:152','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/dalang/fragment/DalangFragment.java:2076'],
 'com.mgtv.sanlang.SanlangLiveActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/sanlang/fragment/SanlangVoteFragment.java:39','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/sanlang/fragment/SanlangVoteFragment.java:79'],
 'com.mgtv.ui.audioroom.main.AudioLiveRoomActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/audioroom/main/AudioSceneLiveFragment.java:213','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/audioroom/main/AudioSceneLiveFragment.java:875'],
 'com.mgtv.ui.cornucopia.CornucopiaActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/cornucopia/CornucopiaBaseFragment.java:1082'],
 'com.mgtv.ui.fantuan.square.activity.FantuanBestFeedsActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/fantuan/square/activity/a.java:26','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/fantuan/square/activity/a.java:103'],
 'com.mgtv.ui.fantuan.userhomepage.FantuanStarHomepageActivity':['test/decompiled/com.hunantv.imgo.activity/sources/yk0/b.java (ImgoWebView field/constructor; obfuscated holder reached by homepage fragment)'],
 'com.mgtv.ui.player.chatroom.ChatRoomActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/player/chatroom/detail/fragment/ChatRoomDetailFragment.java:102','test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/ui/player/chatroom/detail/fragment/ChatRoomDetailFragment.java:326'],
 'com.dtf.face.ui.LandFaceLoadingActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/dtf/face/ui/FaceLoadingActivity.java:59','test/decompiled/com.hunantv.imgo.activity/sources/com/dtf/face/ui/FaceLoadingActivity.java:619'],
 'com.dtf.face.ui.PortFaceLoadingActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/dtf/face/ui/FaceLoadingActivity.java:59','test/decompiled/com.hunantv.imgo.activity/sources/com/dtf/face/ui/FaceLoadingActivity.java:619'],
 'com.opos.mobad.activity.VideoActivity':['test/decompiled/com.hunantv.imgo.activity/sources/com/opos/mobad/video/player/BaseShowActivity.java','test/decompiled/com.hunantv.imgo.activity/sources/com/opos/mobad/video/player/b/c.java'],
}
for rec in data['activities']:
    a=rec['activity']; p=source_for(a); text=p.read_text(errors='replace') if p else ''
    hits=[]
    for i,line in enumerate(text.splitlines(),1):
        if re.search(r'WebView|WebFragment|WebUIFragment|XWebView|H5|Browser|browser|webview',line):
            hits.append(f"{p.relative_to(ROOT)}:{i}: {line.strip()[:180]}")
            if len(hits)==3: break
    if a in deep_valid:
        status='valid'; reason='source-confirmed nested Fragment/holder chain'; hits.extend(deep_valid[a])
    elif a in known:
        status='valid'; reason='independent oracle host chain ('+known[a]+')'
    elif hits:
        status='valid'; reason='Activity source directly declares/constructs/references a WebView or web Fragment'
    elif len(rec.get('facts',[]))<=1:
        status='wrong'; reason='no Activity-source web ownership edge; lone setting is consistent with global/helper leakage'
    else:
        status='uncertain'; reason='no direct Activity-source web token; possible XML/base/fragment indirection requires DEX edge resolution'
    rows.append({'activity':a,'status':status,'v1_fact_count':len(rec.get('facts',[])),'reason':reason,'source_evidence':hits,'apk_sha256':data['apk_sha256']})

with (OUT/'v1-ownership.jsonl').open('w') as f:
    for r in rows:f.write(json.dumps(r,ensure_ascii=False,separators=(',',':'))+'\n')
from collections import Counter
c=Counter(r['status'] for r in rows)
largest=sorted(data['activities'],key=lambda x:len(x.get('facts',[])),reverse=True)[:10]
(OUT/'v1-ownership-summary.md').write_text(f'''# v1 Activity ownership audit

Audited all {len(rows)} Activity records in `test/runs/v1/com.hunantv.imgo.activity/0/output/capabilities.json` against independently decompiled source. Counts: valid={c['valid']}, wrong={c['wrong']}, uncertain={c['uncertain']}.

`valid` means at least one Activity-to-WebView/Fragment ownership path is supported. It does not validate every fact attributed to that Activity. Very large repeated fact sets remain a separate over-attribution defect.

## Highest-risk fanout

'''+"\n".join(f"- `{x['activity']}`: {len(x.get('facts',[]))} facts" for x in largest)+'''\n
## Main error types

- Shared Fragment/helper fanout multiplies the same settings, clients and bridges into many live/player Activities. A legitimate nested Web Fragment does not prove every candidate fact is reachable in every Activity configuration.
- Global settings appear beside Activity facts and must never establish ownership.
- Base-class and SDK wrapper propagation is useful only when the concrete Activity-to-base/factory edge and the returned WebView identity both resolve.
- A class name containing Web/H5 is supporting context, not proof; the JSONL evidence records actual source references.
''')
print(len(rows),dict(c))
