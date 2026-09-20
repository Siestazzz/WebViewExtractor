#!/usr/bin/env python3
"""Serial, isolated APK benchmark. Outputs are never used as validation truth."""
import argparse,json,os,pathlib,subprocess,time
p=argparse.ArgumentParser();p.add_argument('--label',required=True);p.add_argument('--jar',required=True);p.add_argument('--repeat',type=int,default=1);p.add_argument('--legacy',action='store_true');p.add_argument('--cpu-start',type=int,default=0);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[1];jar=pathlib.Path(a.jar).resolve()
cpu=sorted(os.sched_getaffinity(0))[a.cpu_start:a.cpu_start+8]; cpus=','.join(map(str,cpu)); results=[]
env=dict(os.environ,ANDROID_HOME='/home/d3008/phy/workspace/languages/Android/Sdk')
for sample in json.loads((root/'docs/validation/samples.json').read_text()):
 for run in range(a.repeat):
  directory=root/'test/runs'/a.label/sample['package']/str(run);directory.mkdir(parents=True,exist_ok=True)
  cmd=['/usr/bin/time','-v','-o',str(directory/'resources.txt'),'timeout','--signal=TERM','--kill-after=2','600','taskset','-c',cpus,'java','-Xmx16g','-XX:ActiveProcessorCount=8','-jar',str(jar),'--apkpath',str(root/sample['local'])]
  if not a.legacy:cmd+=['--out',str(directory/'output'),'--target-seconds','300','--hard-seconds','595']
  start=time.monotonic()
  with (directory/'console.log').open('w') as log:code=subprocess.call(cmd,cwd=directory,env=env,stdout=log,stderr=subprocess.STDOUT)
  row=dict(package=sample['package'],sha256=sample['sha256'],run=run,elapsed_seconds=time.monotonic()-start,exit_code=code,cpus=cpu,heap_gib=16,command=cmd,artifacts=str(directory.relative_to(root)))
  results.append(row);print(json.dumps(row),flush=True)
  (root/'docs/validation'/f'{a.label}-benchmark.json').write_text(json.dumps(results,indent=2)+'\n')
