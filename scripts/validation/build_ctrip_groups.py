#!/usr/bin/env python3
"""Build Ctrip source-oracle capability groups without reading extractor output."""
import argparse, hashlib, json, re
from pathlib import Path

APK_SHA = "cb4c2a6715a012e53ecb1fc67aac16518f3602215ceed1202ebac185420b1e66"

def read_meta(path, root):
    text = path.read_text(errors="replace")
    pkg = re.search(r"^package\s+([\w.]+);", text, re.M)
    imports = {x.rsplit('.', 1)[-1]: x for x in re.findall(r"^import\s+([\w.]+);", text, re.M)}
    cls = re.search(r"public\s+(?:abstract\s+|final\s+)?class\s+(\w+)(?:\s+extends\s+([\w.]+))?", text)
    return text, (pkg.group(1) if pkg else ""), imports, (cls.group(1) if cls else path.stem), (cls.group(2) if cls and cls.group(2) else None)

def resolve_type(token, context, src):
    text, pkg, imports, _, _ = read_meta(context, src)
    if '.' in token:
        fq = token
    elif token in imports:
        fq = imports[token]
    else:
        fq = pkg + '.' + token
    p = src / (fq.replace('.', '/') + '.java')
    return fq, p if p.exists() else None

def source_evidence(path, line, root):
    lines = path.read_text(errors="replace").splitlines()
    return {"file": str(path.relative_to(root)), "line": line, "text": lines[line-1].strip()}

def type_desc(token, context, src):
    token=re.sub(r'<.*>','',token.strip()).replace('...','[]')
    dims=0
    while token.endswith('[]'): dims+=1; token=token[:-2]
    prim={'void':'V','boolean':'Z','byte':'B','char':'C','short':'S','int':'I','long':'J','float':'F','double':'D'}
    if token in prim: desc=prim[token]
    else:
        text,pkg,imports,_,_=read_meta(context,src)
        common={'String':'java.lang.String','Object':'java.lang.Object','Integer':'java.lang.Integer','Boolean':'java.lang.Boolean'}
        head=token.split('.')[0]
        if token in common: fq=common[token]
        elif head in imports: fq=imports[head]+token[len(head):]
        elif '.' in token and token[0].islower(): fq=token
        else: fq=pkg+'.'+token
        parts=fq.split('.'); cut=next((i for i,p in enumerate(parts) if p and p[0].isupper()),len(parts))
        fq='/'.join(parts[:cut])+('/' if cut else '')+'$'.join(parts[cut:])
        desc='L'+fq+';'
    return '['*dims+desc

def normalized(owner, ret, name, params, context, src):
    return 'L'+owner.replace('.','/')+';->'+name+'('+''.join(type_desc(x,context,src) for x in params)+')'+type_desc(ret,context,src)

def tag_and_methods(fqcn, path, src, root):
    methods, evidence, unknown = [], [], []
    tag = None
    seen = set()
    cur_fq, cur = fqcn, path
    while cur and cur_fq not in seen:
        seen.add(cur_fq)
        text, pkg, imports, cls, parent = read_meta(cur, src)
        if tag is None:
            m = re.search(r"\bTAG\s*=\s*\"([^\"]+)\"", text)
            if m:
                tag = m.group(1)
                line = text[:m.start()].count('\n') + 1
                evidence.append(source_evidence(cur, line, root))
        lines = text.splitlines()
        for i, line in enumerate(lines):
            if '@JavascriptInterface' not in line: continue
            decl = ''
            decl_line = i + 2
            for j in range(i+1, min(i+8, len(lines))):
                decl += ' ' + lines[j].strip()
                if re.search(r'\bpublic\b.*[;{]', decl):
                    decl_line = j + 1
                    break
            m = re.search(r"public\s+(?:final\s+)?([\w.$<>\[\]?]+)\s+(\w+)\s*\((.*?)\)\s*(?:throws\s+[^{]+)?\{", decl)
            if m:
                params=[]
                for p in m.group(3).split(','):
                    p=re.sub(r'@\w+(?:\([^)]*\))?\s*','',p.strip())
                    p=re.sub(r'\bfinal\s+','',p)
                    if p: params.append(' '.join(p.split()[:-1]) or p)
                sig=f"{m.group(1)} {m.group(2)}({', '.join(params)})"
                if sig not in {x['signature'] for x in methods}:
                    methods.append({'kind':'bridge_method','signature':sig,'normalized_signature':normalized(cur_fq,m.group(1),m.group(2),params,cur,src),'declaring_type':cur_fq,'evidence':source_evidence(cur,decl_line,root)})
        if not parent: break
        cur_fq, cur = resolve_type(parent, cur, src)
    if not path:
        unknown.append('plugin source missing')
    if tag is None:
        unknown.append('TAG value unresolved from class and available source ancestors')
    return tag, methods, evidence, unknown

def settings(path, root):
    out=[]
    boolean_names={'allowContentAccess','allowFileAccess','allowFileAccessFromFileURLs','allowUniversalAccessFromFileURLs','builtInZoomControls','databaseEnabled','displayZoomControls','domStorageEnabled','geolocationEnabled','javaScriptCanOpenWindowsAutomatically','javaScriptEnabled','loadWithOverviewMode','mediaPlaybackRequiresUserGesture','saveFormData','savePassword','supportMultipleWindows','useWideViewPort'}
    int_names={'cacheMode','defaultFontSize','defaultFixedFontSize','minimumFontSize','minimumLogicalFontSize','mixedContentMode','textZoom'}
    string_names={'cursiveFontFamily','databasePath','defaultTextEncodingName','fantasyFontFamily','fixedFontFamily','sansSerifFontFamily','serifFontFamily','standardFontFamily','userAgentString'}
    enum_names={'textSize':'Landroid/webkit/WebSettings$TextSize;','renderPriority':'Landroid/webkit/WebSettings$RenderPriority;','layoutAlgorithm':'Landroid/webkit/WebSettings$LayoutAlgorithm;','pluginState':'Landroid/webkit/WebSettings$PluginState;'}
    for i,line in enumerate(path.read_text(errors='replace').splitlines(),1):
        m=re.search(r"(?:settings|getSettings\(\))\.set(\w+)\(([^;]+)\);",line)
        if m:
            expr=m.group(2).strip(); name=m.group(1)[0].lower()+m.group(1)[1:]
            arg='Z' if name in boolean_names else ('I' if name in int_names else ('Ljava/lang/String;' if name in string_names else enum_names.get(name,'UNKNOWN')))
            out.append({'kind':'setting','name':name,'value_expression':expr,'normalized_api':f'Landroid/webkit/WebSettings;->set{m.group(1)}({arg})V','evidence':source_evidence(path,i,root),'unknown':(['normalized parameter type unresolved'] if arg=='UNKNOWN' else [])})
    return out

def overrides(path, root, src, owner=None, client_kind='webview'):
    text=path.read_text(errors='replace'); lines=text.splitlines(); out=[]
    allowed = ({'onLoadResource','onPageCommitVisible','onPageFinished','onPageStarted','onReceivedError','onReceivedHttpError','onReceivedSslError','shouldInterceptRequest','shouldOverrideUrlLoading','onRenderProcessGone','onSafeBrowsingHit'} if client_kind=='webview' else {'getVideoLoadingProgressView','onConsoleMessage','onGeolocationPermissionsShowPrompt','onHideCustomView','onJsAlert','onJsBeforeUnload','onJsConfirm','onJsPrompt','onPermissionRequest','onProgressChanged','onReceivedIcon','onReceivedTitle','onReceivedTouchIconUrl','onShowCustomView','onShowFileChooser'})
    for i,line in enumerate(lines):
        if '@Override' not in line: continue
        decl=''; decl_line=i+2
        for j in range(i+1,min(i+8,len(lines))):
            decl += ' '+lines[j].strip()
            if re.search(r'\bpublic\b.*\{',decl): decl_line=j+1; break
        m=re.search(r"public\s+(?:final\s+)?([\w.$<>\[\]?]+)\s+((?:on|should|getVideoLoading)\w+)\s*\((.*?)\)\s*\{",decl)
        if m and m.group(2) in allowed:
            params=[]
            for p in m.group(3).split(','):
                p=p.strip()
                if p: params.append(' '.join(p.split()[:-1]) or p)
            own=owner or read_meta(path,src)[1]+'.'+read_meta(path,src)[3]
            out.append({'kind':'callback','signature':f"{m.group(1)} {m.group(2)}({', '.join(params)})",'normalized_signature':normalized(own,m.group(1),m.group(2),params,path,src),'evidence':source_evidence(path,decl_line,root)})
    return out

def registry_plugins(registry, generation, src, root):
    text,pkg,imports,_,_=read_meta(registry,src)
    tokens=[]
    if generation=='v1':
        body=text[text.find('public void n(Object obj'):text.find('public void o(',text.find('public void n(Object obj'))]
        tokens=re.findall(r"\b(H5\w+Plugin)\s+\w+\s*=\s*new\s+\1",body)
    else:
        body=text[text.find('public List<H5Plugin> t('):text.find('public void u(',text.find('public List<H5Plugin> t('))]
        tokens=re.findall(r"new\s+([\w.]*H5\w*Plugin(?:V2)?)\s*\(",body)
    result=[]
    for token in dict.fromkeys(tokens):
        # Samsung Wallet is guarded by `obj instanceof H5Fragment`; model it
        # separately instead of inflating the unconditional core registry.
        if token.endswith('H5SamSungWalletPlugin'):
            continue
        fq,path=resolve_type(token,registry,src)
        if path:
            tag,methods,tag_ev,unknown=tag_and_methods(fq,path,src,root)
        else:
            tag,methods,tag_ev,unknown=None,[],[],['plugin source missing']
        line=text[:text.find('new '+token)].count('\n')+1 if 'new '+token in text else 1
        result.append({'kind':'bridge','type':fq,'name':tag,'tag':tag,'methods':methods,'registry_evidence':source_evidence(registry,line,root),'tag_evidence':tag_ev,'unknown':unknown})
    return result

def concrete_bridge(fq, registry, line, src, root, name_field='TAG'):
    path=src/(fq.replace('.','/')+'.java')
    if not path.exists(): return {'kind':'bridge','type':fq,'name':None,'methods':[],'registry_evidence':source_evidence(registry,line,root),'unknown':['plugin source missing']}
    tag,methods,tag_ev,unknown=tag_and_methods(fq,path,src,root)
    if tag is None and name_field!='TAG':
        text=path.read_text(errors='replace'); m=re.search(r'\b'+re.escape(name_field)+r'\s*=\s*"([^"]+)"',text)
        if m:
            tag=m.group(1); ln=text[:m.start()].count('\n')+1; tag_ev=[source_evidence(path,ln,root)]; unknown=[x for x in unknown if not x.startswith('TAG value')]
    return {'kind':'bridge','type':fq,'name':tag,'tag':tag,'methods':methods,'registry_evidence':source_evidence(registry,line,root),'tag_evidence':tag_ev,'unknown':unknown}

def group(group_id, family, capabilities, evidence, unknown=None):
    tier = 'core_required' if family in ('h5-v1','h5-v2') else ('conditional_required' if family=='conditional-plugin' else 'bypass_required')
    return {'group_id':group_id,'family':family,'evaluation_tier':tier,'capabilities':capabilities,'evidence':evidence,'unknown':unknown or [],'apk_sha256':APK_SHA}

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--decompile-root',type=Path,required=True); ap.add_argument('--out',type=Path,required=True)
    a=ap.parse_args(); root=a.decompile_root; src=root/'sources'; out=a.out; out.mkdir(parents=True,exist_ok=True)
    v1wv=src/'ctrip/android/view/h5/view/H5WebView.java'; v2wv=src/'ctrip/android/view/h5v2/view/H5WebView.java'
    v1reg=src/'ctrip/base/init/m.java'; v2reg=src/'ctrip/base/init/n.java'
    groups=[]
    v1plugins=registry_plugins(v1reg,'v1',src,root); v2plugins=registry_plugins(v2reg,'v2',src,root)
    groups.append(group('ctrip-h5-v1','h5-v1',{'settings':settings(v1wv,root),'bridges':v1plugins,'webview_client':{'type':'ctrip.android.view.h5.view.H5WebView$c','overrides':overrides(v1wv,root,src,'ctrip.android.view.h5.view.H5WebView$c')},'webchrome_client':{'type':'ctrip.android.view.h5.view.H5Fragment$b + inherited ctrip.android.view.h5.view.f','overrides':overrides(src/'ctrip/android/view/h5/view/H5Fragment.java',root,src,'ctrip.android.view.h5.view.H5Fragment$b','webchrome')+overrides(src/'ctrip/android/view/h5/view/f.java',root,src,client_kind='webchrome')}},[source_evidence(v1reg,711,root),source_evidence(v1wv,1494,root),source_evidence(src/'ctrip/android/view/h5/view/H5Fragment.java',2944,root)],['Bus-provided payment/liveness/destination plugins are separate conditional groups']))
    groups.append(group('ctrip-h5-v2','h5-v2',{'settings':settings(v2wv,root),'bridges':v2plugins,'webview_client':{'type':'ctrip.android.view.h5v2.view.e','overrides':overrides(src/'ctrip/android/view/h5v2/view/e.java',root,src)},'webchrome_client':{'type':'ctrip.android.view.h5v2.view.g.a + inherited ctrip.android.view.h5v2.view.f','overrides':overrides(src/'ctrip/android/view/h5v2/view/g/a.java',root,src,client_kind='webchrome')+overrides(src/'ctrip/android/view/h5v2/view/f.java',root,src,client_kind='webchrome')}},[source_evidence(v2reg,513,root),source_evidence(v2wv,186,root),source_evidence(src/'ctrip/android/view/h5v2/view/H5Fragment.java',1330,root)],['Reflective TAG read failures are swallowed; group is conditional on provider t() and URL gate']))
    # Payment/liveness bridge registrations are conditional Bus extensions to both H5 generations.
    pay=src/'ctrip/android/pay/common/bus/PayCommonBusObject.java'
    paybridges=[concrete_bridge('ctrip.android.pay.common.hybird.H5PayPlugin',pay,232,src,root),concrete_bridge('ctrip.android.pay.common.hybird.H5PayPluginV2',pay,249,src,root),concrete_bridge('ctrip.android.finance.plugin.CustomCameraPlugin',pay,266,src,root,'PLUGIN_TAG'),concrete_bridge('ctrip.android.finance.plugin.CustomCameraPluginV2',pay,281,src,root,'PLUGIN_TAG')]
    groups.append(group('pay-common','conditional-plugin',{'bridges':paybridges},[source_evidence(pay,232,root)],['Registration is conditional on Bus route and non-null parameters']))
    live=src/'ctrip/android/pay/facekitwrap/LivenessWrapBusObject.java'
    livebridges=[concrete_bridge('ctrip.android.pay.facekitwrap.H5LivenessPlugin',live,60,src,root),concrete_bridge('ctrip.android.pay.facekitwrap.H5LivenessPluginV2',live,79,src,root)]
    groups.append(group('liveness','conditional-plugin',{'bridges':livebridges},[source_evidence(live,60,root)],['Registration is conditional on Bus route and valid parameters']))
    dest=src/'ctrip/android/destination/view/DestinationBusObject.java'
    destbridges=[concrete_bridge('ctrip.android.destination.view.h5.H5GSPlugin',dest,170,src,root),concrete_bridge('ctrip.android.destination.view.h5.H5GSPluginV2',dest,178,src,root),concrete_bridge('ctrip.android.destination.view.h5.H5GSPluginV2Compat',dest,186,src,root)]
    groups.append(group('destination','conditional-plugin',{'bridges':destbridges},[source_evidence(dest,170,root)],['Registration is conditional on Bus route and valid parameters']))
    finance=src/'ctrip/android/finance/bus/FinanceBusObject.java'
    financebridges=[concrete_bridge('ctrip.android.finance.plugin.H5FinanceHomePluginV2',finance,63,src,root),concrete_bridge('ctrip.android.finance.plugin.H5FinanceUtilPluginV2',finance,85,src,root)]
    groups.append(group('finance-v2','conditional-plugin',{'bridges':financebridges},[source_evidence(finance,63,root)],['Registration is conditional on Finance Bus route and valid WebView']))
    samsungbridges=[concrete_bridge('ctrip.android.view.h5.plugin.H5SamSungWalletPlugin',v1reg,835,src,root),concrete_bridge('ctrip.android.view.h5v2.plugin.H5SamSungWalletPlugin',v2reg,530,src,root)]
    groups.append(group('samsung-wallet','conditional-plugin',{'bridges':samsungbridges},[source_evidence(v1reg,833,root),source_evidence(v2reg,529,root)],['Registration requires obj instanceof the matching H5Fragment generation']))
    rn=src/'ctrip/business/crnwebview/RNCWebViewManager.java'
    rnmethod={'kind':'bridge_method','signature':'void postMessage(String)','normalized_signature':'Lctrip/business/crnwebview/RNCWebViewManager$RNCWebView$d;->postMessage(Ljava/lang/String;)V','declaring_type':'ctrip.business.crnwebview.RNCWebViewManager$RNCWebView$d','evidence':source_evidence(rn,259,root)}
    groups.append(group('rn-webview','rn',{'bridges':[{'kind':'bridge','name':'ReactNativeWebView','type':'ctrip.business.crnwebview.RNCWebViewManager$RNCWebView$d','methods':[rnmethod],'registry_evidence':source_evidence(rn,543,root),'unknown':[]},{'kind':'bridge','name':'__REACT_WEB_VIEW_BRIDGE','type':'ctrip.business.crnwebview.RNCWebViewManager$RNCWebView$d','methods':[rnmethod],'registry_evidence':source_evidence(rn,544,root),'unknown':[]}],'settings':settings(rn,root)},[source_evidence(rn,543,root)],['Activity reachability is through React Native manager/package, not a direct Activity WebView field']))
    fl=src/'io/flutter/plugins/webviewflutter/WebViewHostApiImpl.java'; ctf=src/'io/flutter/plugins/webviewflutter/CTJavaScriptInterface.java'; jsch=src/'io/flutter/plugins/webviewflutter/JavaScriptChannel.java'
    flutterbridges=[{'kind':'bridge','name':None,'name_expression':'javaScriptChannel.javaScriptChannelName','type':'io.flutter.plugins.webviewflutter.JavaScriptChannel','methods':tag_and_methods('io.flutter.plugins.webviewflutter.JavaScriptChannel',jsch,src,root)[1],'registry_evidence':source_evidence(fl,183,root),'unknown':['bridge name is supplied at runtime']},{'kind':'bridge','name':'Business','type':'io.flutter.plugins.webviewflutter.CTJavaScriptInterface','methods':tag_and_methods('io.flutter.plugins.webviewflutter.CTJavaScriptInterface',ctf,src,root)[1],'registry_evidence':source_evidence(ctf,19,root),'unknown':[]}]
    groups.append(group('flutter-webview','flutter',{'bridges':flutterbridges,'settings':settings(fl,root)+settings(ctf,root)},[source_evidence(fl,183,root),source_evidence(ctf,19,root)],['JavaScript channel names are runtime values']))
    with (out/'groups.jsonl').open('w') as f:
        for x in groups: f.write(json.dumps(x,ensure_ascii=False)+'\n')
    acts=[json.loads(x) for x in (out/'activities.jsonl').read_text().splitlines()]
    links=[]
    for x in acts:
        if x['classification']!='positive': continue
        cls=x['activity']; p=src/(cls.replace('.','/')+'.java'); body=p.read_text(errors='replace') if p.exists() else ''
        gids=[]
        binding_lines=[(i,l.strip()) for i,l in enumerate(body.splitlines(),1) if not l.lstrip().startswith('import ') and re.search(r'(?:new\s+(?:[\w.]*H5Fragment|[\w.]*H5WebView)|extends\s+(?:[\w.]*H5Container)|\b(?:[\w.]*H5Fragment|[\w.]*H5WebView)\s+\w+\b|\((?:[\w.]*H5Fragment|[\w.]*H5WebView)\)\s*\w+)',l)]
        if binding_lines and ('ctrip.android.view.h5v2' in body or cls in ('ctrip.android.view.h5v2.view.H5Container','ctrip.android.view.h5v2.view.H5PreRender')):
            gids.append('ctrip-h5-v2')
        if binding_lines and ('ctrip.android.view.h5.view' in body or cls=='ctrip.android.view.h5.view.H5Container') and 'h5v2' not in body:
            gids.append('ctrip-h5-v1')
        if 'ctrip.business.crnwebview.RNCWebViewManager' in body: gids.append('rn-webview')
        if 'io.flutter.plugins.webviewflutter' in body: gids.append('flutter-webview')
        host_ev=([source_evidence(p,binding_lines[0][0],root)] if binding_lines and p.exists() else x['evidence'])
        for gid in dict.fromkeys(gids):
            link={'activity':cls,'group_id':gid,'host_evidence':host_ev,'binding_mode':x['binding_mode'],'apk_sha256':APK_SHA}
            if cls=='ctrip.business.evaluation.EvaluateDialogActivity':
                link['exclude_capabilities']=['webchrome_client']
            links.append(link)
    with (out/'activity-groups.jsonl').open('w') as f:
        for x in links: f.write(json.dumps(x,ensure_ascii=False)+'\n')
    # Enrich the original hand-reviewed facts with strict comparison keys.
    facts_path=out/'facts.jsonl'
    if facts_path.exists():
        enriched=[]
        for fact in map(json.loads,facts_path.read_text().splitlines()):
            if fact.get('kind')=='setting':
                val=fact.get('value'); arg='Z' if isinstance(val,bool) else ('I' if isinstance(val,int) else ('Ljava/lang/String;' if isinstance(val,str) else 'UNKNOWN'))
                fact['normalized_api']=f"Landroid/webkit/WebSettings;->set{fact['name'][0].upper()+fact['name'][1:]}({arg})V"
            if fact.get('kind') in ('bridge_method','callback') and fact.get('signature') and fact.get('implementation'):
                m=re.match(r'([\w.$<>\[\]?]+)\s+(\w+)\((.*)\)',fact['signature'])
                if m:
                    def raw_desc(t):
                        t=re.sub(r'<.*>','',t.strip()); dims=0
                        while t.endswith('[]'): dims+=1; t=t[:-2]
                        prim={'void':'V','boolean':'Z','byte':'B','char':'C','short':'S','int':'I','long':'J','float':'F','double':'D'}
                        d=prim.get(t,'L'+t.replace('.','/')+';')
                        return '['*dims+d
                    params=[] if not m.group(3).strip() else [x.strip() for x in m.group(3).split(',')]
                    fact['normalized_signature']='L'+fact['implementation'].replace('.','/')+';->'+m.group(2)+'('+''.join(raw_desc(x) for x in params)+')'+raw_desc(m.group(1))
            enriched.append(fact)
        facts_path.write_text(''.join(json.dumps(x,ensure_ascii=False)+'\n' for x in enriched))
    counts_path=out/'inventory-counts.json'
    if counts_path.exists():
        counts=json.loads(counts_path.read_text()); counts['scope_complete']=False
        counts['scope_complete_reason']='Shared H5 capability groups are expanded; per-Activity exclusive capabilities and all uncertain JADX-missing entries are not closed.'
        counts_path.write_text(json.dumps(counts,ensure_ascii=False,indent=2)+'\n')

if __name__=='__main__': main()
