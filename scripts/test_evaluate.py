#!/usr/bin/env python3
"""Black-box regressions for independently identified scoring failures."""
import json,pathlib,subprocess,tempfile,unittest

class EvaluationTest(unittest.TestCase):
 def replay(self, gold, facts):
  with tempfile.TemporaryDirectory() as directory:
   root=pathlib.Path(directory)
   (root/'report.json').write_text(json.dumps(dict(apk_sha256='fixture',status='partial',activities=[dict(activity='A',facts=facts)])))
   (root/'oracle.jsonl').write_text('\n'.join(json.dumps(dict(apk_sha256='fixture',activity='A',**g)) for g in gold))
   subprocess.run(['python3',str(pathlib.Path(__file__).with_name('evaluate.py')),'--report',str(root/'report.json'),'--oracle',str(root/'oracle.jsonl'),'--out',str(root/'result.json')],check=True,capture_output=True)
   return json.loads((root/'result.json').read_text())
 def test_normalized_member_is_required(self):
  g=dict(kind='bridge',name='api',registration_name='api',normalized_signature='LBridge;->right()V')
  f=dict(kind='bridge',registration_name='api',members=[dict(signature='LBridge;->wrong()V')])
  r=self.replay([g],[f]);self.assertEqual(r['metrics']['bridge']['matched'],0)
 def test_duplicate_evidence_does_not_inflate_denominator(self):
  g=dict(kind='bridge',name='api',registration_name='api')
  r=self.replay([g,dict(g,evidence='another file')],[dict(kind='bridge',registration_name='api',members=[])])
  self.assertEqual(r['metrics']['bridge']['expected'],1);self.assertEqual(r['duplicate_oracle_rows'],1)
 def test_unknown_target_is_not_complete_surface(self):
  g=dict(kind='bridge',name='api',registration_name='api',binding_status='registered-target-unknown')
  for facts in [[],[dict(kind='bridge',registration_name='api',members=[])]]:
   r=self.replay([g],facts);self.assertEqual(r['metrics']['bridge']['matched'],0);self.assertEqual(r['metrics']['bridge']['unscorable'],1)
 def test_proven_empty_surface_rejects_fabricated_member(self):
  g=dict(kind='bridge',name='api',registration_name='api',binding_status='registered-no-compatible-endpoint')
  f=dict(kind='message_bridge',registration_name='api',members=[dict(signature='LHandler;->dispatch()V')])
  self.assertEqual(self.replay([g],[f])['metrics']['bridge']['matched'],0)
  f['members']=[]
  self.assertEqual(self.replay([g],[f])['metrics']['bridge']['matched'],1)
 def test_handler_requires_selector_transport_and_same_view(self):
  g=dict(kind='message_handler',name='event',registration_name='Native',implementation='Handler',normalized_signature='LHandler;->handle(Ljava/lang/String;)V')
  binding=dict(registration_name='Native',webview=dict(id='view'),bridge_object_id='injected-object')
  f=dict(kind='message_bridge',registration_name='event',implementation='Handler',webview=dict(id='view'),members=[dict(signature=g['normalized_signature'])],transport_bindings=[binding])
  self.assertEqual(self.replay([g],[f])['metrics']['bridge']['matched'],1)
  for bad in [dict(f,registration_name='other-event'),dict(f,transport_bindings=[]),dict(f,transport_bindings=[dict(binding,registration_name='OtherNative')]),dict(f,webview=dict(id='other-view')),dict(f,transport_bindings=[dict(binding,bridge_object_id='')])]:
   self.assertEqual(self.replay([g],[bad])['metrics']['bridge']['matched'],0)
 def test_setting_owner_is_not_discarded(self):
  g=dict(kind='setting',name='setJavaScriptEnabled',normalized_api='Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V',value='true',value_kind='literal')
  f=dict(kind='setting',api='Lcom/tencent/smtt/sdk/WebSettings;->setJavaScriptEnabled(Z)V',values=['true'])
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],0)

if __name__=='__main__':unittest.main()
