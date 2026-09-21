#!/usr/bin/env python3
"""Fresh parallel APK runs; each child has its own CPU set, heap and watchdog."""
import argparse,concurrent.futures,hashlib,json,os,pathlib,platform,re,subprocess,time

def main():
    p=argparse.ArgumentParser();p.add_argument('--samples',required=True);p.add_argument('--jar',required=True);p.add_argument('--out',required=True);a=p.parse_args()
    root=pathlib.Path(__file__).resolve().parents[1];jar=pathlib.Path(a.jar).resolve();out=pathlib.Path(a.out).resolve();out.mkdir(parents=True,exist_ok=True)
    samples=json.loads(pathlib.Path(a.samples).read_text());cpus=sorted(os.sched_getaffinity(0))
    if len(cpus)<8*len(samples):p.error('Need eight separate logical CPUs per APK for this benchmark')
    env=dict(os.environ,ANDROID_HOME=os.environ.get('ANDROID_HOME','/home/d3008/phy/workspace/languages/Android/Sdk'))
    environment=dict(mode='parallel; not comparable to isolated serial measurements',platform=platform.platform(),jar=str(jar),jar_sha256=hashlib.file_digest(jar.open('rb'),'sha256').hexdigest(),java=subprocess.run(['java','-version'],capture_output=True,text=True).stderr)
    (out/'environment.json').write_text(json.dumps(environment,indent=2)+'\n')
    def run(item):
        i,sample=item;directory=out/sample['package'];directory.mkdir(exist_ok=True);assigned=cpus[i*8:i*8+8]
        cmd=['/usr/bin/time','-v','-o',str(directory/'resources.txt'),'timeout','--signal=TERM','--kill-after=2','600','taskset','-c',','.join(map(str,assigned)),'java','-Xmx16g','-XX:ActiveProcessorCount=8','-jar',str(jar),'--apkpath',str(root/sample['local']),'--out',str(directory),'--target-seconds','300','--hard-seconds','595']
        start=time.monotonic()
        with (directory/'run.log').open('w') as log:code=subprocess.call(cmd,cwd=root,env=env,stdout=log,stderr=subprocess.STDOUT)
        row=dict(package=sample['package'],seconds=time.monotonic()-start,exit_code=code,cpus=assigned,heap_gib=16,command=cmd)
        report=directory/'capabilities.compact.json'
        if report.exists():
            data=json.loads(report.read_text());row.update(status=data['metadata'].get('status'),counts=data['counts'],metadata=data['metadata'],compact_bytes=report.stat().st_size,full_bytes=(directory/'capabilities.json').stat().st_size)
        resource=(directory/'resources.txt').read_text();rss=re.search(r'Maximum resident set size \(kbytes\): (\d+)',resource);row['peak_rss_kib']=int(rss[1]) if rss else None
        return row
    results=[]
    with concurrent.futures.ThreadPoolExecutor(max_workers=len(samples)) as pool:
        for future in concurrent.futures.as_completed([pool.submit(run,item) for item in enumerate(samples)]):
            row=future.result();results.append(row);print(json.dumps(row),flush=True)
            tmp=out/'summary.json.tmp';tmp.write_text(json.dumps(sorted(results,key=lambda x:x['package']),indent=2)+'\n');tmp.replace(out/'summary.json')
if __name__=='__main__':main()
