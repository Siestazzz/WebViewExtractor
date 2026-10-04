#!/usr/bin/env python3
import importlib.util,json,pathlib,tempfile,unittest
spec=importlib.util.spec_from_file_location('gate',pathlib.Path(__file__).with_name('acceptance_status.py'));gate=importlib.util.module_from_spec(spec);spec.loader.exec_module(gate)
class Gates(unittest.TestCase):
 def test_stale_scopes_and_empty_categories_cannot_pass(self):
  with tempfile.TemporaryDirectory() as tmp:
   root=pathlib.Path(tmp);folder=root/'app';folder.mkdir();report=folder/'capabilities.json';report.write_text(json.dumps({'apk_sha256':'apk','activities':[{'activity':'A','facts':[]}]}));rh=gate.digest(report)
   oracle=root/'oracle.jsonl';oracle.write_text('{}\n')
   score={'apk_sha256':'apk','report_sha256':rh,'oracle_sha256':gate.digest(oracle),'metrics':{k:{'expected':100,'matched':95,'recall':.95} for k in gate.KINDS}}
   (root/'score.json').write_text(json.dumps(score));s={'package':'app','sha256':'apk','score':'score.json','oracle':'oracle.jsonl','ownership':'ownership.jsonl','jar_sha256':'jar'}
   review={'activity':'A','apk_sha256':'apk','report_sha256':rh,'verdict':'valid','scope':'sampled_bridge_only','evidence':['source'],'reviewed_categories':list(gate.KINDS)}
   (root/'ownership.jsonl').write_text(json.dumps(review)+'\n');r=gate.assess(s,root,root)
   self.assertFalse(r['passed']);self.assertEqual(r['ownership']['conservative_error_upper_bound'],1)
   review['scope']='activity_host';(root/'ownership.jsonl').write_text(json.dumps(review)+'\n');r=gate.assess(s,root,root);self.assertTrue(r['requirements']['ownership_error_upper_bound_10'])
   review['report_sha256']='old';(root/'ownership.jsonl').write_text(json.dumps(review)+'\n');self.assertFalse(gate.assess(s,root,root)['requirements']['ownership_error_upper_bound_10'])
   score['metrics']['bridge']={'expected':0,'matched':0,'recall':1};(root/'score.json').write_text(json.dumps(score));self.assertFalse(gate.assess(s,root,root)['requirements']['bridge_recall_95'])
   oracle.write_text('{"new":true}\n');self.assertFalse(gate.assess(s,root,root)['requirements']['score_report_and_oracle_identity'])
 def test_parallel_or_reused_runs_are_not_isolated_repeats(self):
  with tempfile.TemporaryDirectory() as tmp:
   root=pathlib.Path(tmp);folder=root/'app';folder.mkdir();(folder/'capabilities.json').write_text(json.dumps({'apk_sha256':'apk','activities':[]}))
   run={'fresh_process':True,'analysis_results_reused':False,'isolated':False,'cpu_count':8,'heap_gib':16,'jar_sha256':'jar','valid_report':True,'wall_seconds':100}
   p=root/'perf.json';p.write_text(json.dumps({'apk_sha256':'apk','runs':[dict(run,run_id=str(i)) for i in range(3)]}));s={'package':'app','sha256':'apk','jar_sha256':'jar','performance':'perf.json'}
   self.assertFalse(gate.assess(s,root,root)['requirements']['three_isolated_runs'])
   run['isolated']=True;p.write_text(json.dumps({'apk_sha256':'apk','runs':[dict(run,run_id=str(i)) for i in range(3)]}));self.assertTrue(gate.assess(s,root,root)['requirements']['hard_600_seconds'])
if __name__=='__main__':unittest.main()
