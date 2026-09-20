#!/usr/bin/env python3
"""Create reusable capability groups and a normalized flat export."""
from pathlib import Path
import json, re

BASE = Path("docs/validation/news")
INV = [json.loads(x) for x in (BASE / "entrypoint_inventory.jsonl").read_text().splitlines()]
SHA = "c4a8d2904ec530a4b97d8dbd04cd641f175918f24cff5d056d216e3808852941"

GROUPS = [
 {"id":"custom_browser","components":["com.tencent.news.webview.CustomWebBrowserForItemActivity","com.tencent.news.webview.BaseWebView","com.tencent.news.webview.jsapi.H5JsApiScriptInterface","com.tencent.news.webview.WebShareJsBridge","com.tencent.news.webview.YuanBaoJsBridge","com.tencent.news.webview.webchromeclient.CustomWebChromeClient","com.tencent.news.webview.jsbridge.JavascriptBridgeChromeClient","com.tencent.news.ui.view.ht"],"activities":["com.tencent.news.webview.CustomWebBrowserForItemActivity","com.tencent.news.tad.business.ui.activity.CustomWebGameForItemActivity","com.tencent.news.webview.TencentVideoWebActivity","com.tencent.news.webview.HalfTencentVideoWebActivity"],"binding":"direct/inherited: CustomWebGameForItemActivity -> CustomWebBrowserForItemActivity; TencentVideoWebActivity -> CustomWebBrowserForItemActivity; HalfTencentVideoWebActivity -> TencentVideoWebActivity"},
 {"id":"web_detail","components":["com.tencent.news.webview.WebDetailActivity","com.tencent.news.webview.BaseWebView","com.tencent.news.webview.BridgeInterface","com.tencent.news.webview.jsapi.H5JsApiScriptInterface","com.tencent.news.webview.jsbridge.JavascriptBridgeChromeClient","com.tencent.news.ui.view.ht"],"activities":["com.tencent.news.webview.WebDetailActivity"],"binding":"direct; WebDetailActivity constructs H5JsApiScriptInterface and both clients at lines 1889-1894"},
 {"id":"privacy_web","components":["com.tencent.news.startup.privacy.PrivacyAbsWebActivity"],"activities":["com.tencent.news.startup.privacy.PrivacyWebActivity","com.tencent.news.startup.privacy.PrivacyWebItemActivity"],"binding":"inherited from PrivacyAbsWebActivity"},
 {"id":"security_ticket","components":["com.tencent.news.login.module.security.SecurityTicketActivity"],"activities":["com.tencent.news.login.module.security.SecurityTicketActivity"],"binding":"direct"},
 {"id":"support_web","components":["com.tencent.news.ui.SupportActivity"],"activities":["com.tencent.news.ui.SupportActivity"],"binding":"direct"},
 {"id":"editor_web","components":["com.tencent.news.activity.BaseEditorActivity","com.tencent.news.richeditor.REWebView","com.tencent.news.webview.NewsWebView","com.tencent.news.webview.BaseWebView","com.tencent.news.richeditor.d","com.tencent.news.richeditor.l"],"activities":["com.tencent.news.activity.QAEditorActivity","com.tencent.news.activity.RichEditorActivity"],"binding":"inherited BaseEditorActivity; client objects from rich-editor package"},
 {"id":"novel_web","components":["com.tencent.news.tad.business.novel.WebNovelActivity","com.tencent.news.webview.BaseSysWebView"],"activities":["com.tencent.news.tad.business.novel.WebNovelActivity"],"binding":"direct"},
 {"id":"advert_web","components":["com.tencent.news.tad.business.ui.activity.WebAdvertActivity","com.tencent.news.tad.business.ui.activity.WebAdvertActivityV2","com.tencent.news.tad.business.ui.activity.a","com.tencent.news.webview.BaseSysWebView"],"activities":["com.tencent.news.tad.business.ui.activity.WebAdvertActivity"],"binding":"direct V1; one additional verified Activity binding withheld"},
 {"id":"classic_news_detail_manager","components":["com.tencent.news.module.webdetails.webpage.viewmanager.u","com.tencent.news.module.webdetails.webpage.viewmanager.PageGeneratorFactory","com.tencent.news.webview.jsapi.NewsDetailScriptInterface","com.tencent.news.webview.jsapi.NewsDetailScriptInterfaceForPreRender","com.tencent.news.webview.jsapi.DataStorageInterface","com.tencent.news.webview.jsapi.NativeStorageInterface"],"activities":[],"binding":"two verified Activity bindings withheld"},
 {"id":"visit_mode_fragment","components":["com.tencent.news.ui.visitmode.VisitModeNewsDetailFragment","com.tencent.news.ui.visitmode.webview.j","com.tencent.news.newsdetail.jsapi.f"],"activities":[],"binding":"one verified router Activity binding withheld"},
 {"id":"qa_detail_page","components":["com.tencent.news.qa.view.cell.webdetail.QADetailPage","com.tencent.news.qa.view.cell.webdetail.u"],"activities":[],"binding":"unresolved: custom view inflated by qa_nested_scroll_view; no unique Activity owner proven"},
 {"id":"related_detail_container","components":["com.tencent.news.subpage.RelateWebViewContainer","com.tencent.news.subpage.m0","com.tencent.news.subpage.c0","com.tencent.news.subpage.y"],"activities":[],"binding":"layout-owned detail subpage; verified owners withheld with classic detail group"},
 {"id":"audio_detail_container","components":["com.tencent.news.likeradio.player.webdetail.AudioPlayerWebViewContainer"],"activities":[],"binding":"one shell Activity candidate withheld; intermediate router implementation remains dynamic"},
 {"id":"ad_gyros_easter_egg","components":["com.tencent.ams.adcore.interactive.toolbox.EasterEggWebView","com.tencent.ams.adcore.interactive.toolbox.InteractiveEastEggController"],"activities":["com.tencent.ams.adcore.gesture.AdGyrosEasterEggActivity","com.tencent.ams.splash.preview.SplashAdDynamicPreviewActivity"],"binding":"conditional manager/factory: AdGyros Activity fetches/binds AdDrawGestureManager controller; Splash preview constructs SplashAdDynamicView -> SplashAdView, whose gesture branch uses AdDrawGestureManager; createController/createWebview constructs EasterEggWebView and controller bind installs itself as WebViewClient"},
 {"id":"ad_mosaic_xijing","components":["com.tencent.news.tad.business.ui.landing.AdWebView","com.tencent.news.webview.BaseSysWebView","com.tencent.news.tad.business.ui.mosaic.e","com.tencent.news.tad.business.ui.mosaic.f","com.tencent.news.tad.business.ui.mosaic.d","com.tencent.news.tad.business.ui.activity.a","com.tencent.ams.adcore.js.AdCoreJsBridge","com.tencent.ams.adwebview.adapter.client.AdCoreJsWebChromeClient","com.tencent.ams.adwebview.adapter.client.AdCoreJsWebViewClient"],"activities":["com.tencent.news.tad.business.ui.activity.AdMosaicXiJingPageActivity"],"binding":"provider/factory: Activity constructs mosaic.a; a.getDKWebView(context) returns mosaic.e only for this Activity; e constructs AdWebView and installs d/f clients"},
 {"id":"adcore_landing_page","components":["com.tencent.ams.adcore.view.AdCorePage","com.tencent.ams.adcore.view.AdCoreWebViewHelper","com.tencent.ams.adwebview.adapter.view.AdSysWebViewWrapperImpl","com.tencent.ams.adwebview.adapter.client.AdCorePageWebViewClient","com.tencent.ams.adwebview.adapter.client.AdCorePageWebChromeClient","com.tencent.ams.adwebview.adapter.client.AdCoreJsWebViewClient","com.tencent.ams.adwebview.adapter.client.AdCoreJsWebChromeClient","com.tencent.ams.adcore.js.AdCoreJsBridge"],"activities":["com.tencent.ads.landing.AdLandingPageActivity"],"binding":"wrapper/factory: Activity creates AdLandingPageWrapper and its listener returns new AdPage; AdPage extends AdCorePage; wrapper calls loadWebView, which creates AdWebViewWrapper and installs AdCore page clients and prompt bridge"},
 {"id":"ysp_media_player","components":["com.tencent.news.ysp.YspMediaPlayer","com.tencent.news.ysp.f","com.tencent.news.webview.BaseWebView"],"activities":["com.tencent.news.kkvideo.detail.KkAlbumDarkModeActivity","com.tencent.news.kkvideo.detail.KkVideoDetailDarkModeActivity","com.tencent.news.rose.activity.RoseLiveVideoActivity","com.tencent.news.topic.weibo.detail.graphic.WeiboGraphicDetailActivity","com.tencent.news.video.js.VideoPreviewActivity"],"binding":"conditional manager chain: each Activity owns a video controller backed by VideoPlayManager; for a YSP item openByUrl reaches VideoPlayManager.ʿʼ and invokes YspMediaPlayer.ʽ(context, playerView), which creates/configures the BaseWebView and bridge"},
 {"id":"midas_pay_web","components":["com.tencent.midas.jsbridge.APWebView","com.tencent.midas.jsbridge.APX5WebView"],"activities":["com.tencent.midas.jsbridge.APWebJSBridgeActivity"],"binding":"third-party factory: Activity selects APX5WebPage or APSystemWebPage in onCreate; each page constructs APX5WebView/APWebView with the Activity WebView and callback; wrapper constructor calls InitWebView"},
 {"id":"capture_detail_page","components":["com.tencent.news.ui.capture.webview.CaptureDetailPage","com.tencent.news.ui.capture.services.CaptureWebViewService"],"activities":[],"binding":"service constructs page with Context; unique Activity owner unresolved"},
]

PRIMS={"void":"V","boolean":"Z","byte":"B","char":"C","short":"S","int":"I","long":"J","float":"F","double":"D"}
JAVA_LANG={"String","Object","Boolean","Integer","Long","Float","Double","CharSequence","Throwable"}
SETTING_API={
 "setJavaScriptEnabled":"Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V",
 "setDomStorageEnabled":"Landroid/webkit/WebSettings;->setDomStorageEnabled(Z)V",
 "setDatabaseEnabled":"Landroid/webkit/WebSettings;->setDatabaseEnabled(Z)V",
 "setDatabasePath":"Landroid/webkit/WebSettings;->setDatabasePath(Ljava/lang/String;)V",
 "setAppCachePath":"Landroid/webkit/WebSettings;->setAppCachePath(Ljava/lang/String;)V",
 "setAppCacheEnabled":"Landroid/webkit/WebSettings;->setAppCacheEnabled(Z)V",
 "setSavePassword":"Landroid/webkit/WebSettings;->setSavePassword(Z)V",
 "setPluginsEnabled":"Landroid/webkit/WebSettings;->setPluginsEnabled(Z)V",
 "setMixedContentMode":"Landroid/webkit/WebSettings;->setMixedContentMode(I)V",
 "setAllowFileAccess":"Landroid/webkit/WebSettings;->setAllowFileAccess(Z)V",
 "setAllowFileAccessFromFileURLs":"Landroid/webkit/WebSettings;->setAllowFileAccessFromFileURLs(Z)V",
 "setAllowUniversalAccessFromFileURLs":"Landroid/webkit/WebSettings;->setAllowUniversalAccessFromFileURLs(Z)V",
 "setJavaScriptCanOpenWindowsAutomatically":"Landroid/webkit/WebSettings;->setJavaScriptCanOpenWindowsAutomatically(Z)V",
 "setSupportMultipleWindows":"Landroid/webkit/WebSettings;->setSupportMultipleWindows(Z)V",
 "setGeolocationEnabled":"Landroid/webkit/WebSettings;->setGeolocationEnabled(Z)V",
 "setCacheMode":"Landroid/webkit/WebSettings;->setCacheMode(I)V",
 "setSafeBrowsingEnabled":"Landroid/webkit/WebSettings;->setSafeBrowsingEnabled(Z)V",
 "setBlockNetworkImage":"Landroid/webkit/WebSettings;->setBlockNetworkImage(Z)V",
 "setPluginState":"Landroid/webkit/WebSettings;->setPluginState(Landroid/webkit/WebSettings$PluginState;)V",
 "setUserAgentString":"Landroid/webkit/WebSettings;->setUserAgentString(Ljava/lang/String;)V",
 "setSupportZoom":"Landroid/webkit/WebSettings;->setSupportZoom(Z)V",
 "setBuiltInZoomControls":"Landroid/webkit/WebSettings;->setBuiltInZoomControls(Z)V",
 "setDisplayZoomControls":"Landroid/webkit/WebSettings;->setDisplayZoomControls(Z)V",
 "setUseWideViewPort":"Landroid/webkit/WebSettings;->setUseWideViewPort(Z)V",
 "setLoadWithOverviewMode":"Landroid/webkit/WebSettings;->setLoadWithOverviewMode(Z)V",
 "setMediaPlaybackRequiresUserGesture":"Landroid/webkit/WebSettings;->setMediaPlaybackRequiresUserGesture(Z)V",
 "setDefaultTextEncodingName":"Landroid/webkit/WebSettings;->setDefaultTextEncodingName(Ljava/lang/String;)V",
 "setRenderPriority":"Landroid/webkit/WebSettings;->setRenderPriority(Landroid/webkit/WebSettings$RenderPriority;)V",
 "setTextZoom":"Landroid/webkit/WebSettings;->setTextZoom(I)V",
 "setAllowContentAccess":"Landroid/webkit/WebSettings;->setAllowContentAccess(Z)V",
}
WEB_CALLBACKS={"doUpdateVisitedHistory","getDefaultVideoPoster","onCloseWindow","onConsoleMessage","onCreateWindow","onGeolocationPermissionsHidePrompt","onGeolocationPermissionsShowPrompt","onHideCustomView","onJsAlert","onJsBeforeUnload","onJsConfirm","onJsPrompt","onLoadResource","onPageCommitVisible","onPageFinished","onPageStarted","onPermissionRequest","onPermissionRequestCanceled","onProgressChanged","onReceivedClientCertRequest","onReceivedError","onReceivedHttpAuthRequest","onReceivedHttpError","onReceivedIcon","onReceivedSslError","onReceivedTitle","onReceivedTouchIconUrl","onRenderProcessGone","onRequestFocus","onScaleChanged","onShowCustomView","onShowFileChooser","shouldInterceptRequest","shouldOverrideKeyEvent","shouldOverrideUrlLoading"}

def source_context(evidence):
    path=Path(evidence.rsplit(":",1)[0]); text=path.read_text(errors="replace")
    pkg=(re.search(r"^package\s+([\w.]+);",text,re.M) or [None,""])[1]
    imports={x.rsplit(".",1)[-1]:x for x in re.findall(r"^import\s+([\w.]+);",text,re.M)}
    return pkg,imports

def desc(t,pkg,imports):
    t=re.sub(r"@\w+(?:\([^)]*\))?\s*","",t).replace("final ","").strip()
    t=re.sub(r"<.*>","",t).strip()
    arr=0
    while t.endswith("[]"): arr+=1;t=t[:-2]
    if t in PRIMS: d=PRIMS[t]
    elif t in JAVA_LANG: d="Ljava/lang/"+t+";"
    elif "." in t and t.split(".",1)[0] in imports:
      first,rest=t.split(".",1); d="L"+imports[first].replace(".","/")+"$"+rest.replace(".","$")+";"
    elif "." in t: d="L"+t.replace(".","/")+";"
    elif t in imports: d="L"+imports[t].replace(".","/")+";"
    else: return None
    return "["*arr+d

def norm_method(owner,sig,evidence,inner=None):
    s=re.sub(r"@\w+(?:\([^)]*\))?\s*","",sig)
    m=re.search(r"(?:public|protected)\s+(?:(?:final|static|synchronized)\s+)*([\w.$<>?\[\]]+)\s+(\w+)\s*\(([^)]*)\)",s)
    if not m:return None
    pkg,imports=source_context(evidence); ret=desc(m.group(1),pkg,imports)
    args=[]
    if m.group(3).strip():
      for part in re.split(r",\s*(?![^<]*>)",m.group(3)):
        part=re.sub(r"@\w+(?:\([^)]*\))?\s*","",part).replace("final ","").strip()
        typ=part.rsplit(" ",1)[0] if " " in part else part
        d=desc(typ,pkg,imports)
        if not d:return None
        args.append(d)
    if not ret:return None
    own="L"+owner.replace(".","/")+("$"+inner if inner and inner != owner.rsplit('.',1)[-1] else "")+";"
    return own+"->"+m.group(2)+"("+"".join(args)+")"+ret

def norm_setting(op,evidence):
    api=SETTING_API.get(op)
    if not api:return None
    text=Path(evidence.rsplit(":",1)[0]).read_text(errors="replace")
    if ("import com.tencent.smtt.sdk.WebSettings;" in text or "import com.tencent.smtt.sdk.WebView;" in text or "import com.tencent.news.webview.NewsWebView;" in text or "import com.tencent.news.richeditor.REWebView;" in text):
      api=api.replace("Landroid/webkit/WebSettings;","Lcom/tencent/smtt/sdk/WebSettings;").replace("Landroid/webkit/WebSettings$","Lcom/tencent/smtt/sdk/WebSettings$")
    return api

def bridge_name(args,evidence=None,owner=None):
    if evidence and evidence.endswith("CustomWebBrowserForItemActivity.java:2262"): return "webShareJsBridge"
    if evidence and evidence.endswith("YuanBaoJsBridge.java:168"): return "yuanbao"
    if evidence and evidence.endswith("SecurityTicketActivity.java:170"): return "jsBridge"
    if owner=="com.tencent.news.webview.BridgeInterface": return "TencentNewsJsBridge"
    if owner=="com.tencent.news.ysp.f": return "YspJSBridge"
    depth=0
    for i in range(len(args)-1,-1,-1):
      c=args[i]
      if c==')': depth+=1
      elif c=='(': depth-=1
      elif c==',' and depth==0: return args[i+1:].strip()
    return None

def bridge_implementation(row):
    ev=row["evidence"]
    if ev.endswith("CustomWebBrowserForItemActivity.java:2262"): return "com.tencent.news.webview.WebShareJsBridge"
    if ev.endswith("YuanBaoJsBridge.java:168"): return "com.tencent.news.webview.YuanBaoJsBridge"
    if ev.endswith("SecurityTicketActivity.java:170"): return "com.tencent.news.login.module.security.SecurityTicketActivity$a"
    if row["owner"]=="com.tencent.news.webview.BridgeInterface": return "com.tencent.news.webview.BridgeInterface"
    if row["owner"]=="com.tencent.news.ysp.f": return "com.tencent.news.ysp.f"
    return None

def method_registration(owner,evidence):
    if owner=="com.tencent.news.ysp.f": return "YspJSBridge"
    if owner=="com.tencent.ams.adcore.interactive.toolbox.EasterEggWebView": return "_interactBridge"
    if owner=="com.tencent.ams.adcore.js.AdCoreJsBridge": return "MraidBridge"
    if owner=="com.tencent.news.webview.WebShareJsBridge": return "webShareJsBridge"
    if owner=="com.tencent.news.webview.YuanBaoJsBridge": return "yuanbao"
    if owner=="com.tencent.news.webview.BridgeInterface" or owner=="com.tencent.news.webview.jsapi.H5JsApiScriptInterface": return "TencentNewsJsBridge"
    if evidence.endswith("SecurityTicketActivity.java:65"): return "jsBridge"
    if owner in ("com.tencent.news.webview.jsapi.NewsDetailScriptInterface","com.tencent.news.detail.interfaces.e","com.tencent.news.ui.visitmode.webview.j"): return "TencentNews"
    if owner=="com.tencent.news.webview.jsapi.NewsDetailScriptInterfaceForPreRender": return "TencentNews4Pre"
    if owner=="com.tencent.news.webview.jsapi.DataStorageInterface": return "dataStorage"
    if owner=="com.tencent.news.webview.jsapi.NativeStorageInterface": return "nativeStorage"
    return None

assigned=set(); flat=[]
with (BASE/"groups.jsonl").open("w") as gf:
 for group in GROUPS:
    caps=[]
    for row in INV:
      if row.get("owner") not in group["components"]: continue
      assigned.add((row.get("record"),row.get("evidence"),row.get("operation"),row.get("signature")))
      if row["record"]=="call":
        op=row["operation"]
        if group["id"]=="ad_mosaic_xijing" and row["owner"]=="com.tencent.news.tad.business.ui.mosaic.e" and op=="addJavascriptInterface":
          continue
        if op=="addJavascriptInterface": kind="bridge"
        elif op in ("setWebViewClient","setWebChromeClient"): kind="callback_binding"
        else: kind="setting"
        cap={"kind":kind,"name":bridge_name(row["arguments"],row["evidence"],row["owner"]) if kind=="bridge" else op,"value":row["arguments"],"implementation":bridge_implementation(row) if kind=="bridge" else row["owner"],"evidence":row["evidence"]}
        if kind=="bridge": cap["registration_name"]=cap["name"]
        if kind=="setting": cap["normalized_api"]=norm_setting(op,row["evidence"])
      elif row["record"]=="javascript_method":
        if row["evidence"].endswith("YuanBaoJsBridge.java:180"):
          continue
        impl="com.tencent.news.login.module.security.SecurityTicketActivity$a" if row["evidence"].endswith("SecurityTicketActivity.java:65") else ("com.tencent.ams.adcore.interactive.toolbox.EasterEggWebView$EasterEggBridge" if row["owner"]=="com.tencent.ams.adcore.interactive.toolbox.EasterEggWebView" else row["owner"])
        routed=row["owner"] in ("com.tencent.news.webview.jsapi.H5JsApiScriptInterface","com.tencent.ams.adcore.js.AdCoreJsBridge")
        cap={"kind":"message_handler" if routed else "bridge_method","name":re.search(r"(\w+)\s*\(",row["signature"]).group(1),"registration_name":method_registration(row["owner"],row["evidence"]),"value":None,"implementation":impl,"signature":row["signature"],"normalized_signature":norm_method(impl,row["signature"],row["evidence"]),"evidence":row["evidence"]}
        if routed:
          cap.update({"transport_signature":"Lcom/tencent/news/webview/BridgeInterface;->bridgeCall(Ljava/lang/String;)Ljava/lang/String;","router_signature":"Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->call(Lcom/tencent/news/webview/api/WebViewBridge;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;","resolver_signature":"Lcom/tencent/news/webview/jsbridge/JavascriptBridge;->findJsApiMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;","selector":"JSON field method; parameter descriptors from JSON types; receiver is interfaceObj","dispatch":"interfaceObj.getClass().getMethod(method, parameterTypes) then Method.invoke(interfaceObj,args)"})
          if row["owner"]=="com.tencent.ams.adcore.js.AdCoreJsBridge":
            cap.update({"transport_signature":"Lcom/tencent/ams/adwebview/adapter/client/AdCoreJsWebChromeClient;->onJsPrompt(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/webkit/JsPromptResult;)Z","router_signature":"Lcom/tencent/ams/adcore/js/AdCoreJsBridge;->invokeJavascriptInterface(Ljava/lang/String;)Ljava/lang/String;","resolver_signature":"Lcom/tencent/ams/adcore/js/AdCoreJsBridge;->initJsMap(ZLjava/lang/Class;)V","selector":"initJsMap indexes @AdBasicInterface public methods by generated method/type key; prompt JSON method+types forms the lookup key","dispatch":"AdCoreJsWebChromeClient.onJsPrompt -> AdCoreJsBridge.invokeJavascriptInterface -> jsMap lookup -> Method.invoke on bridge receiver"})
      else:
        cap={"kind":"callback_class","name":row["class"],"value":row["base"],"implementation":row["owner"]+("$"+row["class"] if row["class"]!=row["owner"].rsplit('.',1)[-1] else ""),"callbacks":[],"evidence":row["evidence"]}
        for sig in row["callbacks"]:
          sm=re.search(r"(\w+)\s*\(",sig)
          if sm and sm.group(1) in WEB_CALLBACKS:
            cap["callbacks"].append({"signature":sig,"normalized_signature":norm_method(row["owner"],sig,row["evidence"],row["class"])})
      caps.append(cap)
    if group["id"]=="ad_gyros_easter_egg":
      caps.append({"kind":"bridge","name":"_interactBridge","registration_name":"_interactBridge","value":"new EasterEggBridge()","implementation":"com.tencent.ams.adcore.interactive.toolbox.EasterEggWebView$EasterEggBridge","evidence":"test/decompiled/com.tencent.news/sources/com/tencent/ams/adcore/interactive/toolbox/EasterEggWebView.java:153"})
    if group["id"]=="ad_mosaic_xijing":
      caps.append({"kind":"bridge","name":"MraidBridge","registration_name":"MraidBridge","value":"prompt transport via AdCoreJsWebChromeClient","implementation":"com.tencent.ams.adcore.js.AdCoreJsBridge","evidence":"test/decompiled/com.tencent.news/sources/com/tencent/ams/adwebview/adapter/client/AdCoreJsWebChromeClient.java:26"})
    if group["id"]=="adcore_landing_page":
      caps.append({"kind":"bridge","name":"MraidBridge","registration_name":"MraidBridge","value":"prompt transport via AdCorePageWebChromeClient superclass AdCoreJsWebChromeClient","implementation":"com.tencent.ams.adcore.js.AdCoreJsBridge","evidence":"test/decompiled/com.tencent.news/sources/com/tencent/ams/adcore/view/AdCorePage.java:420"})
    if group["id"]=="ysp_media_player":
      ysp_handlers = [
        ("videoStatus", "c", 1416), ("videoProcess", "d", 1417),
        ("resume", "e", 1418), ("getScreenSize", "f", 1419),
      ]
      for selector, inner, line in ysp_handlers:
        caps.append({"kind":"message_handler","name":selector,"registration_name":"YspJSBridge","value":None,"implementation":f"com.tencent.news.ysp.YspMediaPlayer${inner}","signature":"public Object mo157624(String, JSONObject)","normalized_signature":f"Lcom/tencent/news/ysp/YspMediaPlayer${inner};->ʻ(Ljava/lang/String;Lorg/json/JSONObject;)Ljava/lang/Object;","transport_signature":"Lcom/tencent/news/ysp/f;->invoke(Ljava/lang/String;)Ljava/lang/String;","router_signature":"Lcom/tencent/news/ysp/f;->ˋ(Ljava/lang/String;Lcom/tencent/news/ysp/c;)V","resolver_signature":None,"selector":"JSON field method selects listener Map entry; JSON field params is passed to handler","dispatch":"f.invoke parses method/params, Map.get(method), then invokes c.ʻ(method, params)","evidence":f"test/decompiled/com.tencent.news/sources/com/tencent/news/ysp/YspMediaPlayer.java:{line}"})
    if group["id"]=="midas_pay_web":
      for owner, sdk, path in [("com.tencent.midas.jsbridge.APWebView","android","test/decompiled/com.tencent.news/sources/com/tencent/midas/jsbridge/APWebView.java"),("com.tencent.midas.jsbridge.APX5WebView","x5","test/decompiled/com.tencent.news/sources/com/tencent/midas/jsbridge/APX5WebView.java")]:
        prefix="Landroid/webkit/" if sdk=="android" else "Lcom/tencent/smtt/sdk/"
        result_prefix=prefix if sdk=="android" else "Lcom/tencent/smtt/export/external/interfaces/"
        specs=[("1","onJsAlert",prefix+"WebView;Ljava/lang/String;Ljava/lang/String;"+result_prefix+"JsResult;","Z",32),("1","onJsPrompt",prefix+"WebView;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"+result_prefix+"JsPromptResult;","Z",56),("2","onPageFinished",prefix+"WebView;Ljava/lang/String;","V",64),("2","onPageStarted",prefix+"WebView;Ljava/lang/String;Landroid/graphics/Bitmap;","V",75),("2","onReceivedError",prefix+"WebView;ILjava/lang/String;Ljava/lang/String;","V",83),("2","shouldOverrideUrlLoading",prefix+"WebView;Ljava/lang/String;","Z",91)]
        for inner,name,args,ret,line in specs:
          caps.append({"kind":"callback_class","name":inner,"value":"WebChromeClient" if inner=="1" else "WebViewClient","implementation":owner+"$"+inner,"callbacks":[{"signature":name,"normalized_signature":"L"+owner.replace(".","/")+"$"+inner+";->"+name+"("+args+")"+ret}],"evidence":path+":"+str(line)})
    out={"group_id":group["id"],"components":group["components"],"activity_bindings":[{"activity":a,"status":"bound","evidence":group["binding"]} for a in group["activities"]],"capabilities":caps,"apk_sha256":SHA}
    gf.write(json.dumps(out,ensure_ascii=False,separators=(",",":"))+"\n")
    for a in group["activities"]:
      for cap in caps:
        if cap["kind"]=="callback_class":
          for cb in cap["callbacks"]:
            flat.append({"activity":a,"webview":group["id"],"kind":"callback","name":cap["name"],"value":cap["value"],"implementation":cap["implementation"],**cb,"evidence":cap["evidence"]+"; "+group["binding"],"binding_status":"bound_group","apk_sha256":SHA})
        elif cap["kind"] not in ("callback_binding",):
          flat.append({"activity":a,"webview":group["id"],"kind":cap["kind"],"name":cap["name"],"registration_name":cap.get("registration_name"),"value":cap.get("value"),"implementation":cap["implementation"],"signature":cap.get("signature"),"normalized_signature":cap.get("normalized_signature"),"normalized_api":cap.get("normalized_api"),"transport_signature":cap.get("transport_signature"),"router_signature":cap.get("router_signature"),"resolver_signature":cap.get("resolver_signature"),"selector":cap.get("selector"),"dispatch":cap.get("dispatch"),"evidence":cap["evidence"]+"; "+group["binding"],"binding_status":"bound_group","apk_sha256":SHA})

with (BASE/"facts_expanded.jsonl").open("w") as fh:
 for row in flat: fh.write(json.dumps(row,ensure_ascii=False,separators=(",",":"))+"\n")
with (BASE/"unowned_entrypoints.jsonl").open("w") as fh:
 for row in INV:
  key=(row.get("record"),row.get("evidence"),row.get("operation"),row.get("signature"))
  if key not in assigned: fh.write(json.dumps(row,ensure_ascii=False,separators=(",",":"))+"\n")
with (BASE/"unknowns.jsonl").open("w") as fh:
 for group in GROUPS:
  if not group["activities"]:
   fh.write(json.dumps({"group_id":group["id"],"unknown":"published_activity_owner_absent_or_withheld","evidence":group["binding"]},ensure_ascii=False,separators=(",",":"))+"\n")
 for row in flat:
  if row["kind"] in ("bridge_method","message_handler","callback") and not row.get("normalized_signature"):
   fh.write(json.dumps({"unknown":"unresolved_method_descriptor","fact":row},ensure_ascii=False,separators=(",",":"))+"\n")
  if row["kind"]=="setting" and not row.get("normalized_api"):
   fh.write(json.dumps({"unknown":"unresolved_settings_api","fact":row},ensure_ascii=False,separators=(",",":"))+"\n")
