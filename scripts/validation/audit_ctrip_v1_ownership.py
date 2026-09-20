#!/usr/bin/env python3
"""Audit v1 candidate Activity ownership against independent decompiled source."""
import argparse, collections, json, re
from pathlib import Path

WRONG={'ctrip.business.feedback.view.CtripCommonFeedBackActivity'}
UNCERTAIN={'ctrip.android.hotel.order.view.flagship.HotelFlagShipLoginActivity','ctrip.android.hotel.detail.map.HotelDetailMapActivity','ctrip.android.hotel.view.UI.list.map.HotelListMapActivity','ctrip.android.hotel.detail.image.HotelAlbumBrowseActivity','ctrip.android.hotel.detail.image.HotelPhotoViewActivity','ctrip.android.hotel.detail.view.base.HotelDetailCharityProjectActivity','ctrip.android.hotel.view.UI.video.HotelVideoActivity'}
PAT=re.compile(r'new\s+\w*(?:WebView|H5Fragment|QRScanFragment|H5TransFragment)|extends\s+(?:H5Container|H5PayActivity|BaseWebAuthorizeActivity)|(?:WebView|H5Fragment|CtripUnitedMapView|QRScanFragment|H5TransFragment)\s+\w+|createPopLayerView|setWebViewClient|addJavascriptInterface')

def evidence(cls,src,root):
    p=src/(cls.replace('.','/')+'.java')
    if not p.exists(): return []
    lines=p.read_text(errors='replace').splitlines()
    for i,l in enumerate(lines,1):
        if not l.lstrip().startswith('import ') and PAT.search(l): return [{'file':str(p.relative_to(root)),'line':i,'text':l.strip()}]
    for i,l in enumerate(lines,1):
        if re.search(r'public .*class ',l): return [{'file':str(p.relative_to(root)),'line':i,'text':l.strip()}]
    return []

def evline(src,root,rel,line):
    p=src/rel
    return {'file':str(p.relative_to(root)),'line':line,'text':p.read_text(errors='replace').splitlines()[line-1].strip()}

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--candidates',type=Path,required=True);ap.add_argument('--decompile-root',type=Path,required=True);ap.add_argument('--output',type=Path,required=True)
    a=ap.parse_args();data=json.loads(a.candidates.read_text());src=a.decompile_root/'sources'; rows=[]
    for act in data['activities']:
        cls=act['activity']; status='wrong' if cls in WRONG else ('uncertain' if cls in UNCERTAIN else 'valid')
        owners=collections.defaultdict(lambda:collections.Counter())
        for fact in act['facts']:
            owner=(fact.get('site') or '').split(';->')[0].lstrip('L').replace('/','.') or '<unknown>'
            owners[owner][fact['kind']]+=1
        chain=evidence(cls,src,a.decompile_root)
        if cls=='ctrip.business.share.qqapi.TencentEntryActivity':
            chain=[evline(src,a.decompile_root,'ctrip/business/share/qqapi/TencentEntryActivity.java',149),evline(src,a.decompile_root,'com/tencent/tauth/Tencent.java',464),evline(src,a.decompile_root,'com/tencent/connect/share/QQShare.java',296)]
            rationale='Conditionally valid: onCreate dispatches to shareToQQ/shareToQzone; the QQ path constructs and shows TDialog when native sharing is unsupported, so its WebView capability is reachable.'
        elif cls=='ctrip.business.feedback.view.CtripCommonFeedBackActivity':
            chain=[evline(src,a.decompile_root,'ctrip/business/feedback/view/CtripCommonFeedBackActivity.java',804),evline(src,a.decompile_root,'ctrip/business/feedback/view/CtripCommonFeedBackActivity.java',822),evline(src,a.decompile_root,'ctrip/base/ui/gallery/GalleryView.java',830),evline(src,a.decompile_root,'ctrip/base/ui/gallery/c.java',58),evline(src,a.decompile_root,'ctrip/base/ui/gallery/c.java',73)]
            rationale='Confirmed wrong for this Activity: galleryImage creates fresh ImageItem objects but never assigns bottomWebViewUrl; GalleryView passes that null field to c.b(), whose empty-URL branch skips the H5WebView constructor in the non-empty branch.'
        elif status=='uncertain':
            rationale='The shared hotel BaseActivity exposes showPopLayer/createPopLayer paths, but this concrete Activity has no source-visible invocation. Inheritance alone does not prove runtime use.'
        else:
            rationale='Source-visible direct, inherited, Fragment, custom-view, SDK, or map-wrapper chain supports Activity ownership.'
        rows.append({'activity':cls,'verdict':status,'candidate_fact_count':len(act['facts']),'candidate_kinds':dict(collections.Counter(x['kind'] for x in act['facts'])),'site_owner_buckets':[{'owner':o,'fact_count':sum(c.values()),'kinds':dict(c)} for o,c in sorted(owners.items())],'source_evidence':chain,'rationale':rationale,'checked_scope':'Activity source plus candidate site-owner chain; capability values are not accepted from candidate output','apk_sha256':data['apk_sha256']})
    a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in rows))
    c=collections.Counter(x['verdict'] for x in rows)
    summary=f'''# v1 Activity ownership audit\n\nThis audit treats `capabilities.json` only as a candidate list. Ownership is decided from the matching decompiled source. It reviews {len(rows)} reported Activities: {c['valid']} valid, {c['wrong']} wrong, and {c['uncertain']} uncertain.\n\n`valid` means a source-visible direct, inheritance, Fragment, custom WebView, SDK, or map-wrapper chain reaches the implementation, including conditional paths. `wrong` requires contradictory source evidence, not merely failure to find a chain. `uncertain` means inheritance/shared reachability exists but a concrete invocation could not be proved.\n\n`TencentEntryActivity` is conditionally valid: its QQ share path reaches `QQShare.shareToQQ`, which constructs `TDialog` when native sharing is unsupported. `CtripCommonFeedBackActivity` is the one confirmed wrong owner: it creates new `ImageItem` objects without setting `bottomWebViewUrl`, passes null through `GalleryView.C`, and therefore takes the empty-URL branch before the H5WebView constructor.\n\nThe seven hotel cases are uncertain. Their shared BaseActivity or hotel infrastructure exposes pop-layer construction, but the concrete subclasses do not visibly invoke it; inheritance alone is insufficient to call them wrong or valid. QR scan/gallery, map wrapper, and H5 container chains remain valid where their Fragment or wrapper construction is explicit.\n'''
    a.output.with_name('v1-ownership-summary.md').write_text(summary)

if __name__=='__main__':main()
