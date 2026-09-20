#!/usr/bin/env python3
"""Verify normalized callback/bridge signatures against APK DEX method tables."""
from pathlib import Path
import json, re, subprocess, tempfile, xml.etree.ElementTree as ET

APK=Path("test/apks/com.tencent.news.apk")
FACTS=Path("docs/validation/news/facts_expanded.jsonl")
OUT=Path("docs/validation/news/dex-signature-verification.jsonl")
DEXDUMP="/home/d3008/phy/workspace/languages/Android/Sdk/build-tools/36.0.0/dexdump"

rows=[json.loads(x) for x in FACTS.read_text().splitlines()]
targets={x["normalized_signature"] for x in rows if x.get("kind") in ("callback","bridge_method","message_handler") and x.get("normalized_signature")}

def jdesc(t):
    prim={"void":"V","boolean":"Z","byte":"B","char":"C","short":"S","int":"I","long":"J","float":"F","double":"D"}
    n=0
    while t.endswith("[]"): n+=1;t=t[:-2]
    return "["*n+(prim[t] if t in prim else "L"+t.replace(".","/")+";")

found=set()
with tempfile.TemporaryDirectory(prefix="news-dex-") as td:
    listing=subprocess.run(["7z","l",str(APK)],check=True,text=True,capture_output=True).stdout
    names=sorted(set(re.findall(r"\bclasses\d*\.dex\b",listing)))
    subprocess.run(["7z","e","-y",f"-o{td}",str(APK),*names],check=True,stdout=subprocess.DEVNULL)
    for name in names:
        xml_path=Path(td)/(name+".xml")
        with xml_path.open("wb") as fh:
            subprocess.run([DEXDUMP,"-l","xml",str(Path(td)/name)],check=True,stdout=fh,stderr=subprocess.DEVNULL)
        package=""; cls=""
        for event,elem in ET.iterparse(xml_path,events=("start","end")):
            if event=="start" and elem.tag=="package": package=elem.attrib.get("name","")
            elif event=="start" and elem.tag=="class": cls=elem.attrib.get("name","")
            elif event=="end" and elem.tag=="method":
                owner="L"+(package+"." if package else "").replace(".","/")+cls.replace(".","/")+";"
                args="".join(jdesc(p.attrib["type"]) for p in elem.findall("parameter"))
                sig=owner+"->"+elem.attrib["name"]+"("+args+")"+jdesc(elem.attrib["return"])
                if sig in targets: found.add(sig)
                elem.clear()
        xml_path.unlink()

with OUT.open("w") as fh:
    for sig in sorted(targets):
        fh.write(json.dumps({"normalized_signature":sig,"dex_present":sig in found},separators=(",",":"))+"\n")
if found != targets:
    raise SystemExit(f"missing {len(targets-found)} of {len(targets)} signatures")
