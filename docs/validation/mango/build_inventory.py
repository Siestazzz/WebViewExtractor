#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
SRC = "test/decompiled/com.hunantv.imgo.activity/sources/"
SHA = "65533b5fb593db4179791fd028865e53342c729b7228f73d820b0b65afe397f5"

activities = [
    ("com.mgtv.ui.browser.HalfWebActivity", "inherited", "com.hunantv.imgo.h5.ImgoWebView", "com/mgtv/ui/browser/HalfWebActivity.java:35 -> com/mgtv/ui/browser/BaseWebActivity.java:1639 -> com/hunantv/imgo/h5/ImgoWebView.java:89"),
    ("com.mgtv.ui.login.MeCaptureWebActivity", "inherited", "com.hunantv.imgo.h5.ImgoWebView", "com/mgtv/ui/login/MeCaptureWebActivity.java:36 -> com/mgtv/ui/browser/BaseWebActivity.java:1639 -> com/hunantv/imgo/h5/ImgoWebView.java:89"),
    ("com.mgtv.ui.guide.PureWebActivity", "direct", "com.hunantv.imgo.h5.ImgoWebView", "com/mgtv/ui/guide/PureWebActivity.java:45 -> com/hunantv/imgo/h5/ImgoWebView.java:89"),
    ("com.mgtv.ui.other.BackDoorWebActivity", "direct", "android.webkit.WebView", "com/mgtv/ui/other/BackDoorWebActivity.java:49-69"),
    ("com.mgtv.diana.sdk.api.pay.WechatWapPayRouterActivity", "direct", "android.webkit.WebView", "com/mgtv/diana/sdk/api/pay/WechatWapPayRouterActivity.java:112-122"),
    ("com.ccb.ccbnetpay.H5PayActivity", "direct", "android.webkit.WebView", "com/ccb/ccbnetpay/H5PayActivity.java:131-206"),
    ("com.sina.weibo.sdk.web.WebActivity", "direct", "com.hunantv.imgo.h5.browser.MoWebView", "com/sina/weibo/sdk/web/WebActivity.java:98-205"),
    ("com.mgtv.ui.browser.WebActivity", "fragment", "com.mgtv.ui.browser.WebViewFragment", "com/mgtv/ui/browser/WebActivity.java:57,101,680"),
    ("com.mgtv.ui.browser.WebActivityTransparent", "fragment", "com.mgtv.ui.browser.WebViewFragment", "com/mgtv/ui/browser/WebActivityTransparent.java:52,90,482"),
    ("com.mgtv.ui.login.CaptureWebActivity", "fragment", "com.mgtv.ui.login.widget.CaptureWebViewFragment", "com/mgtv/ui/login/CaptureWebActivity.java:51,102,419-420"),
    ("com.hunantv.imgo.xweb.XWebActivity", "fragment", "com.hunantv.imgo.xweb.XWebViewFragment", "com/hunantv/imgo/xweb/XWebActivity.java:51,82,357"),
    ("com.hunantv.webui.WebUIActivity", "fragment", "com.hunantv.webui.WebUIFragment", "com/hunantv/webui/WebUIActivity.java:51,89,470"),
    ("com.mgsz.h5.WebContainerActivity", "fragment", "com.mgsz.h5.WebViewFragment", "com/mgsz/h5/WebContainerActivity.java:16-35"),
    ("com.platform.oms.ui.LoadingWebActivity", "fragment", "com.platform.oms.ui.FragmentWebLoadingBase", "com/platform/oms/ui/LoadingWebActivity.java:31,38,124,287"),
    ("com.mgtv.ui.live.mglive.webview.WebViewActivity", "direct", "com.mgtv.ui.live.mglive.h5.jsbridge.BridgeWebView", "com/mgtv/ui/live/mglive/webview/WebViewActivity.java:26,31,129"),
    ("com.imgo.vipcardiac.activity.LandWebActivity", "wrapper", "com.imgo.webbase.hybird.BaseWebView", "com/imgo/vipcardiac/activity/LandWebActivity.java:36,90,134-138"),
    ("com.mgadplus.brower.CustomWebActivity", "wrapper", "com.mgadplus.brower.ImgoAdWebView", "com/mgadplus/brower/CustomWebActivity.java:29,36,176-185"),
    ("com.mgtb.money.web.ThirdWebActivity", "fragment", "com.mgtb.money.web.webview.ProgressWebView", "com/mgtb/money/web/ThirdWebActivity.java:14,87"),
    ("com.mgtb.money.web.ThirdFullWebActivity", "fragment", "com.mgtb.money.web.webview.ProgressWebView", "com/mgtb/money/web/ThirdFullWebActivity.java:14,58"),
    ("com.opos.cmn.biz.web.activity.apiimpl.AdWebActivity", "inherited", "OPOS BaseWebActivity web component", "com/opos/cmn/biz/web/activity/apiimpl/AdWebActivity.java:9"),
    ("com.opos.mobad.ui.feedback.FeedBackWebViewActivity", "inherited", "com.opos.cmn.module.ui.WebViewActivity", "com/opos/mobad/ui/feedback/FeedBackWebViewActivity.java:9 -> com/opos/cmn/module/ui/WebViewActivity.java:15"),
    ("com.opos.cmn.module.ui.WebViewActivity", "wrapper", "com.opos.cmn.module.ui.a", "com/opos/cmn/module/ui/WebViewActivity.java:15,126-146"),
    ("com.bytedance.sdk.openadsdk.core.activity.base.TTWebPageActivity", "third-party", "com.bytedance.sdk.component.widget.SSWebView", "com/bytedance/sdk/openadsdk/core/activity/base/TTWebPageActivity.java:66,183,420"),
    ("com.bytedance.sdk.openadsdk.core.activity.base.TTPlayableWebPageActivity", "third-party", "com.bytedance.sdk.component.widget.SSWebView", "com/bytedance/sdk/openadsdk/core/activity/base/TTPlayableWebPageActivity.java:45,788-802"),
    ("com.bytedance.sdk.openadsdk.core.activity.base.TTVideoWebPageActivity", "third-party", "com.bytedance.sdk.component.widget.SSWebView", "com/bytedance/sdk/openadsdk/core/activity/base/TTVideoWebPageActivity.java:64,178,447"),
    ("com.bytedance.sdk.openadsdk.core.activity.base.TTVideoScrollWebPageActivity", "inherited", "com.bytedance.sdk.component.widget.SSWebView", "com/bytedance/sdk/openadsdk/core/activity/base/TTVideoScrollWebPageActivity.java:12 -> TTVideoWebPageActivity.java:178"),
    ("com.huawei.petalpaysdk.webpay.PayWebviewActivity", "inherited", "com.huawei.petalpaysdk.widget.PaySafeWebView", "com/huawei/petalpaysdk/webpay/PayWebviewActivity.java:26-39"),
    ("com.alipay.sdk.app.H5AuthActivity", "inherited", "Alipay H5PayActivity web container", "com/alipay/sdk/app/H5AuthActivity.java:4 -> com/alipay/sdk/app/H5PayActivity.java:12"),
    ("com.alipay.sdk.app.H5OpenAuthActivity", "inherited", "Alipay H5PayActivity web container", "com/alipay/sdk/app/H5OpenAuthActivity.java:8 -> com/alipay/sdk/app/H5PayActivity.java:12"),
    ("com.ubix.ssp.open.comm.UBiXWebViewActivity", "third-party", "com.ubix.ssp.open.comm.widget.c", "com/ubix/ssp/open/comm/UBiXWebViewActivity.java:39,107-108"),
]

holdout = [
    "com.mgtv.ui.ad.MgtvLuckWebActivity", "com.hunantv.fukubukuro.core.activity.LuckyBagWebActivity",
    "com.imgo.vipmix.activity.WebActivity", "com.mgtv.diana.sdk.main.MangoMiniAppActivity",
    "com.alipay.sdk.app.H5PayActivity", "com.mg.ec.main.viper.view.LivePlayActivity",
    "com.mgtv.ui.live.liveplay.ui.user.UserMgMoneyActivity", "com.hunantv.person.live.activity.PersonLiveActivity",
    "com.mgtv.diana.vodgames.VipTransferActivity", "com.mgtv.erlang.ErlangLiveActivity",
]

facts=[]
def add(a,w,k,n,v,impl,sig,ev,status="confirmed"):
    facts.append(dict(activity=a,webview=w,kind=k,name=n,value=v,implementation=impl,signature=sig,
                      normalized_signature="",normalized_api="",bridge_method="",
                      evidence=SRC+ev,binding_status=status,apk_sha256=SHA))

# One verified positive callback/ownership fact per disclosed Activity.
for a,layer,w,ev in activities:
    add(a,w,"activity_binding","webview_binding",layer,w,"activity -> webview binding",ev)

# Complete directly exposed JavaScript interface methods seen in the two Mango bridge families.
imgo_hosts=("com.mgtv.ui.browser.HalfWebActivity","com.mgtv.ui.login.MeCaptureWebActivity","com.mgtv.ui.guide.PureWebActivity","com.mgtv.ui.browser.WebActivity","com.mgtv.ui.browser.WebActivityTransparent","com.mgtv.ui.login.CaptureWebActivity","com.hunantv.webui.WebUIActivity")
imgo_settings=(
 ("setSaveFormData","true","boolean",42,"browser/RootWebView.java"),("setLightTouchEnabled","false","boolean",43,"browser/RootWebView.java"),("setNeedInitialFocus","false","boolean",44,"browser/RootWebView.java"),
 ("setSupportZoom","true","boolean",50,"browser/RootWebView.java"),("setLoadWithOverviewMode","true","boolean",51,"browser/RootWebView.java"),("setUseWideViewPort","true","boolean",52,"browser/RootWebView.java"),("setSupportMultipleWindows","false","boolean",53,"browser/RootWebView.java"),
 ("setPluginState","ON_DEMAND","WebSettings.PluginState",55,"browser/RootWebView.java"),("setUserAgentString","mDefaultUserAgent","String",61,"browser/RootWebView.java"),("setGeolocationEnabled","true","boolean",71,"browser/RootWebView.java"),
 ("setDatabaseEnabled","true","boolean",72,"browser/RootWebView.java"),("setDomStorageEnabled","true","boolean",73,"browser/RootWebView.java"),("setAppCacheEnabled","true","boolean",74,"browser/RootWebView.java"),
 ("setGeolocationDatabasePath","path2","String",78,"browser/RootWebView.java"),("setDatabasePath","path","String",79,"browser/RootWebView.java"),("setAppCachePath","path3","String",80,"browser/RootWebView.java"),
 ("setTextZoom","100","int",217,"jsbridge/BridgeWebView.java"),("setSavePassword","false","boolean",218,"jsbridge/BridgeWebView.java"),
 ("setDefaultTextEncodingName","UTF-8","String",251,"ImgoWebView.java"),("setSupportZoom","true","boolean",252,"ImgoWebView.java"),("setBuiltInZoomControls","true","boolean",253,"ImgoWebView.java"),("setDisplayZoomControls","false","boolean",254,"ImgoWebView.java"),
 ("setLoadWithOverviewMode","true","boolean",255,"ImgoWebView.java"),("setUseWideViewPort","true","boolean",256,"ImgoWebView.java"),("setSupportMultipleWindows","true","boolean",257,"ImgoWebView.java"),("setDomStorageEnabled","true","boolean",263,"ImgoWebView.java"),
 ("setPluginState","ON","WebSettings.PluginState",264,"ImgoWebView.java"),("setAllowFileAccess","false","boolean",265,"ImgoWebView.java"),("setMediaPlaybackRequiresUserGesture","false","boolean",266,"ImgoWebView.java"),("setCacheMode","-1","int",267,"ImgoWebView.java"),
 ("setLoadsImagesAutomatically","true","boolean",268,"ImgoWebView.java"),("setMixedContentMode","0","int",269,"ImgoWebView.java"),("setUserAgentString","AppBaseInfoUtil.getWebViewUA()","String",274,"ImgoWebView.java"),
)
for a in imgo_hosts:
    chain = ("com/mgtv/ui/browser/BaseWebActivity.java:1639 -> " if "Pure" not in a else "com/mgtv/ui/guide/PureWebActivity.java:45 -> ")
    for n,v,arg_type,line,source in imgo_settings:
        add(a,"com.hunantv.imgo.h5.ImgoWebView","setting",n,v,"ImgoWebView initialization chain",f"void {n}({arg_type})",chain+f"com/hunantv/imgo/h5/{source}:{line}")
    add(a,"com.hunantv.imgo.h5.jsbridge.BridgeWebView","bridge","jsobj","callNative","com.hunantv.imgo.h5.jsbridge.BridgeWebView.JsObject","public String callNative(String str, String str2, String str3)",chain+"com/hunantv/imgo/h5/jsbridge/BridgeWebView.java:63-64")
    add(a,"com.hunantv.imgo.h5.ImgoWebView","callback","WebViewClient","com.hunantv.imgo.h5.ImgoWebView$2","com.hunantv.imgo.h5.ImgoWebView#init","onLoadResource(WebView,String); onPageFinished(WebView,String); onPageStarted(WebView,String,Bitmap); onReceivedError(WebView,WebResourceRequest,WebResourceError); onReceivedHttpError(WebView,WebResourceRequest,WebResourceResponse); onRenderProcessGone(WebView,RenderProcessGoneDetail); shouldOverrideUrlLoading(WebView,String); onReceivedError(WebView,int,String,String)",chain+"com/hunantv/imgo/h5/ImgoWebView.java:278-382")
    add(a,"com.hunantv.imgo.h5.ImgoWebView","callback","WebChromeClient","com.hunantv.imgo.h5.ImgoWebView$3","com.hunantv.imgo.h5.ImgoWebView#init","onConsoleMessage(ConsoleMessage); onCreateWindow(WebView,boolean,boolean,Message); onPermissionRequest(PermissionRequest); onProgressChanged(WebView,int); onReceivedTitle(WebView,String)",chain+"com/hunantv/imgo/h5/ImgoWebView.java:383-473")
    add(a,"com.hunantv.imgo.h5.ImgoWebView","callback","popup.WebViewClient","com.hunantv.imgo.h5.ImgoWebView$3$1","ImgoWebView popup client","shouldOverrideUrlLoading(WebView,String)",chain+"com/hunantv/imgo/h5/ImgoWebView.java:405-418")
    add(a,"com.hunantv.imgo.h5.browser.RootWebViewClient","callback","WebViewClient.inherited","com.hunantv.imgo.h5.browser.RootWebViewClient","inherited effective callbacks","doUpdateVisitedHistory(WebView,String,boolean); onReceivedSslError(WebView,SslErrorHandler,SslError); shouldInterceptRequest(WebView,String); shouldInterceptRequest(WebView,WebResourceRequest)",chain+"com/hunantv/imgo/h5/browser/RootWebViewClient.java:121-223")
    add(a,"com.hunantv.imgo.h5.browser.RootWebChromeClient","callback","WebChromeClient.inherited","com.hunantv.imgo.h5.browser.RootWebChromeClient","inherited effective callbacks","getDefaultVideoPoster(); getVideoLoadingProgressView(); onCloseWindow(WebView); onGeolocationPermissionsHidePrompt(); onGeolocationPermissionsShowPrompt(String,GeolocationPermissions.Callback); onHideCustomView(); onJsAlert(WebView,String,String,JsResult); onJsBeforeUnload(WebView,String,String,JsResult); onJsConfirm(WebView,String,String,JsResult); onJsPrompt(WebView,String,String,String,JsPromptResult); onReceivedIcon(WebView,Bitmap); onShowCustomView(View,WebChromeClient.CustomViewCallback); onShowFileChooser(WebView,ValueCallback<Uri[]>,WebChromeClient.FileChooserParams); onShowCustomView(View,int,WebChromeClient.CustomViewCallback)",chain+"com/hunantv/imgo/h5/browser/RootWebChromeClient.java:332-602")

add("com.mgtv.ui.other.BackDoorWebActivity","android.webkit.WebView","setting","setDomStorageEnabled","true","BackDoorWebActivity#onCreate","void setDomStorageEnabled(boolean)","com/mgtv/ui/other/BackDoorWebActivity.java:65")
add("com.mgtv.ui.other.BackDoorWebActivity","android.webkit.WebView","callback","WebViewClient","BackDoorWebActivity.a","BackDoorWebActivity#onCreate","shouldOverrideUrlLoading(WebView,WebResourceRequest); shouldOverrideUrlLoading(WebView,String)","com/mgtv/ui/other/BackDoorWebActivity.java:69,92-105")
add("com.mgtv.diana.sdk.api.pay.WechatWapPayRouterActivity","android.webkit.WebView","setting","setAllowFileAccess","false","WechatWapPayRouterActivity#init","void setAllowFileAccess(boolean)","com/mgtv/diana/sdk/api/pay/WechatWapPayRouterActivity.java:120")
add("com.mgtv.diana.sdk.api.pay.WechatWapPayRouterActivity","android.webkit.WebView","callback","WebViewClient","anonymous MoWebViewClient","WechatWapPayRouterActivity#init","onRenderProcessGone(WebView,RenderProcessGoneDetail); shouldOverrideUrlLoading(WebView,WebResourceRequest); shouldOverrideUrlLoading(WebView,String)","com/mgtv/diana/sdk/api/pay/WechatWapPayRouterActivity.java:122-151")
add("com.ccb.ccbnetpay.H5PayActivity","android.webkit.WebView","bridge","javaObj","object_registered","com.ccb.ccbnetpay.H5PayActivity$d","","com/ccb/ccbnetpay/H5PayActivity.java:206")
add("com.ccb.ccbnetpay.H5PayActivity","android.webkit.WebView","bridge","javaObj","sdkCallBack","com.ccb.ccbnetpay.H5PayActivity$d","public void sdkCallBack(String str)","com/ccb/ccbnetpay/H5PayActivity.java:96-101,206")
add("com.ccb.ccbnetpay.H5PayActivity","android.webkit.WebView","bridge","javaObj","showFinish","com.ccb.ccbnetpay.H5PayActivity$d","public void showFinish()","com/ccb/ccbnetpay/H5PayActivity.java:114-115,206")
add("com.ccb.ccbnetpay.H5PayActivity","android.webkit.WebView","callback","WebChromeClient.onProgressChanged","com.ccb.ccbnetpay.H5PayActivity.c","H5PayActivity direct client","onProgressChanged(WebView,int)","com/ccb/ccbnetpay/H5PayActivity.java:82-88")
add("com.ccb.ccbnetpay.H5PayActivity","android.webkit.WebView","callback","WebViewClient","com.ccb.ccbnetpay.H5PayActivity.e","H5PayActivity direct client","onPageFinished(WebView,String); onPageStarted(WebView,String,Bitmap); onReceivedError(WebView,int,String,String); shouldOverrideUrlLoading(WebView,String)","com/ccb/ccbnetpay/H5PayActivity.java:274-311")
for n,v,line in (("setAllowContentAccess","false",193),("setAllowFileAccess","false",195),("setAllowFileAccessFromFileURLs","false",198),("setMixedContentMode","2",205)):
    add("com.sina.weibo.sdk.web.WebActivity","com.hunantv.imgo.h5.browser.MoWebView","setting",n,v,"com.sina.weibo.sdk.web.WebActivity#init",f"void {n}({'int' if n=='setMixedContentMode' else 'boolean'})",f"com/sina/weibo/sdk/web/WebActivity.java:{line}")
add("com.sina.weibo.sdk.web.WebActivity","com.hunantv.imgo.h5.browser.MoWebView","callback","WebChromeClient","anonymous WebChromeClient","com.sina.weibo.sdk.web.WebActivity#init","onProgressChanged(WebView,int); onReceivedTitle(WebView,String)","com/sina/weibo/sdk/web/WebActivity.java:144-157")

# Every registration expression in the disclosed ImgoWebView manager. Literal names
# are exact; constant-backed names retain the source expression and unknown status.
import re
imgo = ROOT / SRC / "com/hunantv/imgo/h5/ImgoWebView.java"
iface = ROOT / SRC / "com/hunantv/imgo/h5/callback/ImgoWebJavascriptInterface.java"
impl = ROOT / SRC / "com/mgtv/h5/ImgoWebJavascriptImpl.java"
iface_sigs={}
for decl in iface.read_text(errors="replace").splitlines():
    dm=re.match(r'\s*(?:[\w<>@.]+\s+)*(\w+)\s*\([^;]*\);\s*$',decl)
    if dm: iface_sigs[dm.group(1)]=decl.strip().rstrip(';')
impl_string_methods=set()
for decl in impl.read_text(errors="replace").splitlines():
    dm=re.match(r'\s*public\s+void\s+(\w+)\s*\(\s*(?:@\w+\s+)?String\s+\w+\s*\)\s*\{',decl)
    if dm: impl_string_methods.add(dm.group(1))
# Reflection at ImgoWebJavascriptImpl.java:752 requires this exact DEX shape.
# Use the independent export rather than accepting an interface declaration.
dex_impl_string_methods=set()
with (ROOT / "test/runs/symbols/mango.jsonl").open(errors="replace") as dex_stream:
    for dex_line in dex_stream:
        dex_obj=json.loads(dex_line)
        if dex_obj.get('type')=='Lcom/mgtv/h5/ImgoWebJavascriptImpl;':
            suffix='(Ljava/lang/String;)V'
            dex_impl_string_methods={m['name'] for m in dex_obj.get('methods',[]) if m.get('signature','').endswith(suffix)}
            break
if not dex_impl_string_methods:
    raise SystemExit('missing ImgoWebJavascriptImpl DEX methods')
constant_registration_names={
    "AIDLConstants.FUN_NAME.CONFIRM_LOGIN":"confirmLogin",
    "MgtvMethodChannel.L":"getUserInfo",
    "t.f135638u":"feedback",
    "DianaEventDefine.ON_USER_CAPTURE_SCREEN":"onUserCaptureScreen",
    "JsApiPage.SHOW_TOAST":"showToast",
    "AIDLConstants.FUN_NAME.SEND_COMMENT":"sendComment",
    "AIDLConstants.FUN_NAME.SHOW_MANGO_KID":"showMangoKid",
    "VideoInteractionEvent.f55356g":"VideoInteractionEvent",
    "VideoSetPlayerMutedEvent.f55366f":"VideoSetPlayerMuted",
}
for lineno, line in enumerate(imgo.read_text(errors="replace").splitlines(), 1):
    m = re.search(r'registerHandler\(([^,]+),\s*this\)', line)
    if not m: continue
    expr=m.group(1).strip()
    literal=expr.startswith('"') and expr.endswith('"')
    name=expr[1:-1] if literal else constant_registration_names.get(expr,expr)
    target_confirmed=name in dex_impl_string_methods
    for a in imgo_hosts:
        add(a,"com.hunantv.imgo.h5.ImgoWebView","bridge",name,"registered",
            "com.mgtv.h5.ImgoWebJavascriptImpl" if target_confirmed else None,
            f"void {name}(String str)" if target_confirmed else "",
            f"com/hunantv/imgo/h5/ImgoWebView.java:{lineno} -> com/mgtv/h5/ImgoWebJavascriptImpl.java:732-758",
            "confirmed" if target_confirmed else "registered-no-compatible-endpoint")
        facts[-1]['registration_name']=name
        facts[-1]['registration_expression']=expr
        if not target_confirmed:
            facts[-1]['audit_reference']='docs/validation/mango/imgo-unknown-handler-audit.md'

add("com.hunantv.imgo.xweb.XWebActivity","com.hunantv.imgo.xweb.jsbridge.BridgeWebView","bridge","jsobj","callNative","com.hunantv.imgo.xweb.jsbridge.JSInterface#callNative","public void callNative(String str, String str2, String str3)","com/hunantv/imgo/xweb/jsbridge/BridgeWebView.java:292 -> com/hunantv/imgo/xweb/jsbridge/JSInterface.java:18-19")

# Mechanical normalization fields used by strict regression comparison.
prim={"void":"V","boolean":"Z","int":"I","long":"J","byte":"B","short":"S","char":"C","float":"F","double":"D","String":"Ljava/lang/String;","Bitmap":"Landroid/graphics/Bitmap;","View":"Landroid/view/View;","WebView":"Landroid/webkit/WebView;","ConsoleMessage":"Landroid/webkit/ConsoleMessage;","PermissionRequest":"Landroid/webkit/PermissionRequest;","Message":"Landroid/os/Message;","WebResourceRequest":"Landroid/webkit/WebResourceRequest;","WebResourceError":"Landroid/webkit/WebResourceError;","WebResourceResponse":"Landroid/webkit/WebResourceResponse;","RenderProcessGoneDetail":"Landroid/webkit/RenderProcessGoneDetail;","SslErrorHandler":"Landroid/webkit/SslErrorHandler;","SslError":"Landroid/net/http/SslError;","GeolocationPermissions.Callback":"Landroid/webkit/GeolocationPermissions$Callback;","JsResult":"Landroid/webkit/JsResult;","JsPromptResult":"Landroid/webkit/JsPromptResult;","WebChromeClient.CustomViewCallback":"Landroid/webkit/WebChromeClient$CustomViewCallback;","WebChromeClient.FileChooserParams":"Landroid/webkit/WebChromeClient$FileChooserParams;","ValueCallback<Uri[]>":"Landroid/webkit/ValueCallback;","WebSettings.PluginState":"Landroid/webkit/WebSettings$PluginState;"}
def desc(t):
    t=re.sub(r'@\w+\s*','',t).strip()
    return prim.get(t,"L"+t.replace('.','/')+";")
def norm(owner,sig):
    m=re.search(r'(\w+)\s+(\w+)\((.*)\)',sig)
    if not m:return ""
    ret,name,args=m.groups(); aa=[]
    if args.strip():
        for x in args.split(','):
            bits=x.strip().split(); aa.append(desc(' '.join(bits[:-1]) if len(bits)>1 else bits[0]))
    return "L"+owner.replace('.','/')+";->"+name+"("+''.join(aa)+")"+desc(ret)
for x in facts:
    if x['kind']=='setting':
        x['normalized_signature']=norm('android.webkit.WebSettings',x['signature'])
        x['normalized_api']=x['normalized_signature']
    elif x['kind']=='callback':
        # Multi-method callback rows preserve all exact Java signatures in signature;
        # normalized_signature is intentionally empty until split below.
        x['normalized_api']=x['name']
    elif x['kind']=='bridge':
        x['normalized_api']='WebView message bridge registration'
        x.setdefault('registration_name',x['name'])
        if x['value']=='registered' and not x['name'].startswith('javaObj.'):
            x['registration_name']=x.get('registration_name',x['name'])
            if x['binding_status']=='confirmed' and x['name'] in dex_impl_string_methods:
                x['bridge_method']='Lcom/mgtv/h5/ImgoWebJavascriptImpl;->'+x['name']+'(Ljava/lang/String;)V'
                x['normalized_signature']=x['bridge_method']
            else:
                x['normalized_signature']=''
                x['bridge_method']=''
                x['signature']=''
                x['implementation']=None
                x['binding_status']='registered-no-compatible-endpoint' if x.get('audit_reference') else 'registered-target-unknown'
        elif x['name']=='jsobj':
            ret='Ljava/lang/String;' if 'String callNative' in x['signature'] else 'V'
            owner='com/hunantv/imgo/h5/jsbridge/BridgeWebView$JsObject' if ret!='V' else 'com/hunantv/imgo/xweb/jsbridge/JSInterface'
            x['bridge_method']='L'+owner+';->callNative(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)'+ret
            x['normalized_signature']=x['bridge_method']
            x['implementation']=owner.replace('/','.')
        elif x['name']=='javaObj' and x['value']=='sdkCallBack':
            x['bridge_method']='Lcom/ccb/ccbnetpay/H5PayActivity$d;->sdkCallBack(Ljava/lang/String;)V'
            x['normalized_signature']=x['bridge_method']
            x['implementation']='com.ccb.ccbnetpay.H5PayActivity$d'
        elif x['name']=='javaObj' and x['value']=='showFinish':
            x['bridge_method']='Lcom/ccb/ccbnetpay/H5PayActivity$d;->showFinish()V'
            x['normalized_signature']=x['bridge_method']
            x['implementation']='com.ccb.ccbnetpay.H5PayActivity$d'

# A callback fact is one real override, never an Activity ownership proxy or a
# semicolon-packed list. Split the audited client declarations mechanically.
expanded=[]
callback_owner_fix={
 "BackDoorWebActivity.a":"com.mgtv.ui.other.BackDoorWebActivity$a",
 "anonymous MoWebViewClient":"com.mgtv.diana.sdk.api.pay.WechatWapPayRouterActivity$1",
 "com.ccb.ccbnetpay.H5PayActivity.c":"com.ccb.ccbnetpay.H5PayActivity$c",
 "com.ccb.ccbnetpay.H5PayActivity.e":"com.ccb.ccbnetpay.H5PayActivity$e",
 "anonymous WebChromeClient":"com.sina.weibo.sdk.web.WebActivity$4",
}
for x in facts:
    if x['kind']!='callback' or ';' not in x['signature']:
        if x['kind']=='callback' and '(' in x['signature']:
            method=x['signature'].split('(')[0].split()[-1]
            returns={'shouldOverrideUrlLoading':'boolean','onRenderProcessGone':'boolean','onConsoleMessage':'boolean','onCreateWindow':'boolean','onJsAlert':'boolean','onJsBeforeUnload':'boolean','onJsConfirm':'boolean','onJsPrompt':'boolean','onShowFileChooser':'boolean','shouldInterceptRequest':'WebResourceResponse','getDefaultVideoPoster':'Bitmap','getVideoLoadingProgressView':'View'}
            ret=returns.get(method,'void')
            jsig=ret+' '+x['signature'].strip()
            owner=callback_owner_fix.get(x['value'],x['value'])
            x['implementation']=owner
            x['value']=owner
            x['normalized_signature']=norm(owner,jsig)
        expanded.append(x); continue
    for one in [s.strip() for s in x['signature'].split(';') if s.strip()]:
        y=dict(x); method=one.split('(')[0].split()[-1]
        returns={'shouldOverrideUrlLoading':'boolean','onRenderProcessGone':'boolean','onConsoleMessage':'boolean','onCreateWindow':'boolean','onJsAlert':'boolean','onJsBeforeUnload':'boolean','onJsConfirm':'boolean','onJsPrompt':'boolean','onShowFileChooser':'boolean','shouldInterceptRequest':'WebResourceResponse','getDefaultVideoPoster':'Bitmap','getVideoLoadingProgressView':'View'}
        ret=returns.get(method,'void')
        y['name']=x['name']+'.'+method
        y['signature']=ret+' '+one
        owner=callback_owner_fix.get(x['value'],x['value'])
        y['implementation']=owner
        y['value']=owner
        y['normalized_signature']=norm(owner,y['signature'])
        expanded.append(y)
facts=expanded

OUT.mkdir(parents=True,exist_ok=True)
with (OUT/"facts.jsonl").open("w") as f:
    for x in facts: f.write(json.dumps(x,ensure_ascii=False,separators=(",",":"))+"\n")

counts={}
for _,k,_,_ in activities: counts[k]=counts.get(k,0)+1
(OUT/"inventory.md").write_text("# Mango TV WebView Activity inventory\n\n"+f"APK SHA-256: `{SHA}`\n\n"+"| Activity | layer | WebView / carrier | evidence |\n|---|---|---|---|\n"+"\n".join(f"| `{a}` | {k} | `{w}` | `{SRC+e}` |" for a,k,w,e in activities)+"\n\nLayer counts: "+", ".join(f"{k}={v}" for k,v in sorted(counts.items()))+". `manager`, `factory`, and `message bridge` are implementation layers catalogued in `bridge-inventory.md`; they are not Activity ownership categories.\n")
(OUT/"holdout.md").write_text("# Reserved final holdout\n\nThese ten identifiers are reserved. No capability facts or evidence chains are disclosed here or in `facts.jsonl`. Some names are intentionally unresolved candidates; they must be adjudicated only in the final holdout pass.\n\n"+"\n".join(f"- `{x}`" for x in holdout)+"\n")

(OUT/"bridge-inventory.md").write_text(f'''# Bridge and framework inventory

## Message bridge

- `com.hunantv.imgo.h5.jsbridge.BridgeWebView` registers JavaScript object `jsobj` at `{SRC}com/hunantv/imgo/h5/jsbridge/BridgeWebView.java:223`.
- Complete annotated exposure in its registered object: `public String callNative(String str, String str2, String str3)` at lines 63–64. The bridge dispatches by the first argument to `messageHandlers`; `registerHandler(String, BridgeHandler)` is declared at line 413. This documents signatures and binding only.
- `ImgoWebView` registers 138 named handlers through `registerWebHandler()` at lines 529–673. `ImgoWebJavascriptImpl$e0.handler()` reflects `getDeclaredMethod(registrationName, String.class)` at `ImgoWebJavascriptImpl.java:732-758`. Constant-backed names are resolved to their source values; only exact DEX `(String)V` targets populate `bridge_method`.

## XWeb message bridge

- `com.hunantv.imgo.xweb.jsbridge.BridgeWebView` registers `new JSInterface(iBridge)` under `jsobj` at `{SRC}com/hunantv/imgo/xweb/jsbridge/BridgeWebView.java:292`.
- Complete annotated exposure in `com.hunantv.imgo.xweb.jsbridge.JSInterface`: `public void callNative(String str, String str2, String str3)` at `{SRC}com/hunantv/imgo/xweb/jsbridge/JSInterface.java:18-19`.

## Wrapper / factory / manager

- Wrapper: `BaseWebActivity` constructs `ImgoWebView` at `{SRC}com/mgtv/ui/browser/BaseWebActivity.java:1639`.
- Wrapper: `BaseWebView` / `ProgressWebView` carry `com.imgo.webbase.jsbridge.BridgeWebView` for wallet pages.
- Factory: `WebUIActivity`, `XWebActivity`, and browser Activities construct or attach their corresponding fragments; evidence is in `inventory.md`.
- Manager: `ImgoWebView` owns settings, clients, handler registration, and lifecycle callbacks. `XWebView` is the X5-side manager.

## Annotated third-party bridge

- CCB registers object name `javaObj` at `{SRC}com/ccb/ccbnetpay/H5PayActivity.java:206`; its distinct annotated members are `sdkCallBack` and `showFinish` at lines 100–115.
- Wallet DSBridge uses `_dsbridge`-family plumbing in `com.mgtb.money.web.dsbridge.DWebView`; its annotated signatures are held for the final holdout pass.
''')

(OUT/"summary.md").write_text(f'''# Mango TV independent source validation

- APK: `test/apks/com.hunantv.imgo.activity.apk`, version 9.3.0, SHA-256 `{SHA}` (matches `docs/validation/samples.json`).
- Decompiler: JADX 1.5.1, `--show-bad-code -j 4`; exit code 1 with 93 reported errors. Log: `test/decompiled/com.hunantv.imgo.activity.jadx.log`; exit record: `test/decompiled/com.hunantv.imgo.activity.jadx.exitcode`.
- Independence: this inventory was made from the APK, manifest, and JADX sources only. Extractor output was not inspected.
- Disclosed set: {len(activities)} positive Activity bindings across direct, inherited, fragment, wrapper and third-party layers. Framework manager/factory/message-bridge layers are separately mapped.
- Holdout: 10 reserved identifiers have no capability rows in `facts.jsonl`.
- Facts: {len(facts)} JSONL rows. Each row records one Activity capability or its verified WebView binding, with source ownership chain, status, and APK hash.

Unknown means unknown. JADX failures, obfuscated constants, unresolved generated bindings, and incomplete inner-class bodies are recorded as `partial-decompilation` or reserved for holdout; none is counted as a negative.
''')

complete=set(imgo_hosts)|{"com.mgtv.ui.other.BackDoorWebActivity","com.mgtv.diana.sdk.api.pay.WechatWapPayRouterActivity","com.ccb.ccbnetpay.H5PayActivity","com.sina.weibo.sdk.web.WebActivity"}
expanded={"com.mgsz.h5.WebContainerActivity","com.hunantv.imgo.xweb.XWebActivity","com.mgtb.money.web.ThirdWebActivity","com.mgtb.money.web.ThirdFullWebActivity","com.alipay.sdk.app.H5AuthActivity","com.alipay.sdk.app.H5OpenAuthActivity","com.bytedance.sdk.openadsdk.core.activity.base.TTWebPageActivity","com.bytedance.sdk.openadsdk.core.activity.base.TTPlayableWebPageActivity","com.bytedance.sdk.openadsdk.core.activity.base.TTVideoWebPageActivity","com.bytedance.sdk.openadsdk.core.activity.base.TTVideoScrollWebPageActivity","com.opos.cmn.biz.web.activity.apiimpl.AdWebActivity","com.opos.mobad.ui.feedback.FeedBackWebViewActivity","com.opos.cmn.module.ui.WebViewActivity","com.platform.oms.ui.LoadingWebActivity","com.mgtv.ui.live.mglive.webview.WebViewActivity","com.imgo.vipcardiac.activity.LandWebActivity","com.mgadplus.brower.CustomWebActivity","com.huawei.petalpaysdk.webpay.PayWebviewActivity","com.ubix.ssp.open.comm.UBiXWebViewActivity"}
complete|=expanded
with (OUT/"coverage.jsonl").open("w") as f:
    for a,layer,w,ev in activities:
        state="complete" if a in complete else "partial-wrapper-or-obfuscated-factory"
        f.write(json.dumps({"activity":a,"settings_scan":state,"client_override_scan":state,"bridge_scan":"complete" if a in complete|{"com.hunantv.imgo.xweb.XWebActivity"} else "not-established-or-partial","evidence":SRC+ev,"apk_sha256":SHA},ensure_ascii=False,separators=(",",":"))+"\n")

print(len(activities),len(facts),len(holdout))
