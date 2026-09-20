#!/usr/bin/env python3
import json
from pathlib import Path

OUT=Path(__file__).with_name("nft-development-facts.jsonl")
MIGRATION=Path(__file__).with_name("nft-development-schema-migration.json")
SHA="65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5"
SETTINGS=[
 ("setDefaultTextEncodingName","(Ljava/lang/String;)V","UTF-8","string"),
 ("setSupportZoom","(Z)V",True,"boolean"),("setBuiltInZoomControls","(Z)V",True,"boolean"),
 ("setDisplayZoomControls","(Z)V",False,"boolean"),("setLoadWithOverviewMode","(Z)V",True,"boolean"),
 ("setUseWideViewPort","(Z)V",True,"boolean"),("setSupportMultipleWindows","(Z)V",True,"boolean"),
 ("setSavePassword","(Z)V",False,"boolean"),("setJavaScriptEnabled","(Z)V",True,"boolean"),
 ("setDomStorageEnabled","(Z)V",True,"boolean"),("setPluginState","(Landroid/webkit/WebSettings$PluginState;)V","ON","enum"),
 ("setAllowFileAccess","(Z)V",False,"boolean"),("setMediaPlaybackRequiresUserGesture","(Z)V",False,"boolean"),
 ("setCacheMode","(I)V",-1,"integer"),("setLoadsImagesAutomatically","(Z)V",True,"boolean"),
 ("setMixedContentMode","(I)V",0,"integer")]
HOSTS=[
 ("com.mgsz.detail.ui.AntiqueDetailActivity","com.mgsz.hunantv.nft.threed.NftWebviewLayout","com.google.android.filament.utils.databinding.ShNftWebviewLayoutBinding","test/decompiled/com.hunantv.imgo.activity/sources/com/mgsz/hunantv/nft/threed/NftWebviewLayout.java",113,143,["AntiqueDetailActivity.W","AntiqueDetailFragment.E7","com.mgsz.hunantv.nft.MgNftViewer.setNftData"]),
 ("com.mgtv.digital.ui.DigitalDetailActivity","com.hunantv.nft.threed.NftWebviewLayout","com.hunantv.nft.databinding.NftWebviewLayoutBinding","test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/nft/threed/NftWebviewLayout.java",118,50,["DigitalDetailActivity.x3","com.hunantv.nft.MgNftViewer.setNftData"]),
 ("com.mgtv.digital.ui.DigitalModelViewActivity","com.hunantv.nft.threed.NftWebviewLayout","com.hunantv.nft.databinding.NftWebviewLayoutBinding","test/decompiled/com.hunantv.imgo.activity/sources/com/hunantv/nft/threed/NftWebviewLayout.java",118,50,["DigitalModelViewActivity.U2","com.hunantv.nft.MgNftViewer.setNftData"])]

def base(host,impl,binding,src,chain):
 viewer_binding="ShNftViewerLayoutBinding.nftWebview" if "mgsz" in impl else "NftViewerLayoutBinding.nftWebview"
 identity=f"{binding}.webview:Landroid/webkit/WebView;"
 return {"schema_version":1,"dataset":"mango-nft-nonblind-development","app_version":"9.3.0","validator":"GPT-5.6 Sol independent source+DEX validation","evidence_status":"confirmed-source-and-dex","apk_sha256":SHA,"activity":host,"webview":identity,"webview_identity":identity,"binding_chain":chain+["MgNftViewer constructor -> viewer layout inflate",viewer_binding,f"{impl}.<init>(Context,AttributeSet,int)",f"{impl}.initialize(Context,AttributeSet,int)",f"{binding}.webview"] ,"implementation":impl,"source_file":src}
def main():
 rows=[]
 for host,impl,binding,src,initline,initializeline,chain in HOSTS:
  b=base(host,impl,binding,src,chain)
  # Preserve the original operation fact, now with the required metadata. It is outside the three capability score families.
  rows.append(b|{"kind":"webview_operation","name":"loadUrl","normalized_signature":"Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V","normalized_api":"Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V","value_kind":"dynamic","raw_value_kind":"dynamic_string","value":None,"conditional":host.endswith("AntiqueDetailActivity"),"scoring_domain":"host-operation-recall","source_location":f"{src}:{86 if 'mgsz' in impl else 157}"})
  for name,desc,value,vk in SETTINGS:
   if name=="setJavaScriptEnabled":
    location="test/decompiled/com.hunantv.imgo.activity/sources/com/mgtv/aop/bb/U.java:10"
   else:
    location=f"{src}:"+str(next(i for i,x in enumerate(Path(src).read_text().splitlines(),1) if f"settings.{name}(" in x))
   api=f"Landroid/webkit/WebSettings;->{name}{desc}"
   rows.append(b|{"kind":"setting","name":name,"normalized_signature":api,"normalized_api":api,"value_kind":"enum" if vk=="enum" else "literal","raw_value_kind":vk,"value":value,"conditional":False,"source_location":location,"call_chain":[f"{impl}.<init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V",f"{impl}.initialize(Landroid/content/Context;Landroid/util/AttributeSet;I)V",f"{impl}.initWebview()V"]})
  cbline=167 if 'mgsz' in impl else 75
  cbimpl=f"{impl}$a" if 'mgsz' in impl else f"{impl}$initialize$1"
  setter="Landroid/webkit/WebView;->setWebViewClient(Landroid/webkit/WebViewClient;)V"
  rows.append(b|{"kind":"callback_registration","name":"setWebViewClient","normalized_signature":setter,"normalized_api":setter,"implementation":cbimpl,"callsite_owner":impl,"value_kind":"literal","raw_value_kind":"implementation_type","value":cbimpl,"source_location":f"{src}:{cbline}"})
  member=f"L{cbimpl.replace('.','/')};->shouldOverrideUrlLoading(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Z"
  rows.append(b|{"kind":"callback","name":"WebViewClient.shouldOverrideUrlLoading","normalized_signature":member,"normalized_api":"WebViewClient","implementation":cbimpl,"callsite_owner":impl,"value_kind":"literal","raw_value_kind":"method","value":None,"source_location":f"{src}:{46 if 'mgsz' in impl else 77}","dispatch_status":"observed"})
 OUT.write_text("\n".join(json.dumps(r,ensure_ascii=False,separators=(",",":")) for r in rows)+"\n")
 MIGRATION.write_text(json.dumps({"schema_version":1,"migration":"nft-development-v1-to-canonical-compatible-v2","semantic_rows_before":57,"semantic_rows_after":len(rows),"changes":["copied webview_identity to webview while retaining webview_identity","added normalized_api","mapped string/boolean/integer to literal and dynamic_string to dynamic; retained raw_value_kind","renamed callback setter fact to callback_registration and assigned instantiated client implementation","renamed callback_method to callback and retained its actual implementation class","retained all three webview_operation rows outside the three scored capability families"]},ensure_ascii=False,indent=2)+"\n")
if __name__=="__main__": main()
