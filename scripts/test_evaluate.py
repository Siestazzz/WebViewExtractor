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
   r=self.replay([g],facts);self.assertEqual(r['metrics']['bridge']['matched'],0);self.assertEqual(r['metrics']['bridge']['unscorable'],1);self.assertEqual(r['capability_granularity']['bridge_registration']['matched'],0)
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
 def test_callback_registration_requires_api_and_concrete_type(self):
  g=dict(kind='callback_registration',name='setWebViewClient',normalized_api='Landroid/webkit/WebView;->setWebViewClient(Landroid/webkit/WebViewClient;)V',implementation='Client')
  f=dict(kind='callback',api=g['normalized_api'],implementation='Client',members=[])
  r=self.replay([g],[f]);self.assertEqual(r['metrics']['callback']['matched'],1);self.assertEqual(r['capability_granularity']['callback_registration']['matched'],1)
  for bad in [dict(f,implementation='OtherClient'),dict(f,api='LUnrelated;->setWebViewClient(Landroid/webkit/WebViewClient;)V')]:
   self.assertEqual(self.replay([g],[bad])['metrics']['callback']['matched'],0)
  member=dict(kind='callback',name='onPageFinished',normalized_signature='LClient;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V')
  self.assertEqual(self.replay([g,member],[f])['metrics']['callback']['matched'],1)
 def test_unsupported_oracle_is_unscorable_even_without_candidate(self):
  gold=[dict(kind='bridge_method',name='promptTarget',registration_name=None,normalized_signature='LTarget;->go()V'),dict(kind='setting',name='setJavaScriptEnabled',normalized_api='Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V',value_kind='runtime_expression',value='remote.flag')]
  for g in gold:
   category='bridge' if g['kind']=='bridge_method' else 'setting'
   for facts in [[],[dict(kind='callback',members=[])]]:
    r=self.replay([g],facts)
    self.assertEqual(r['metrics'][category]['matched'],0)
    self.assertEqual(r['metrics'][category]['unscorable'],1)
    self.assertEqual(r['missing'][0]['reason'],'unscorable_oracle')
    self.assertTrue(r['missing'][0]['scoring_limitation'])
 def test_inherited_callback_requires_source_concrete_client(self):
  # Sibling clients inherit the same body, but are distinct installed objects.
  g=dict(kind='callback',name='onPageFinished',implementation='InstalledChild',normalized_signature='LParent;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V')
  f=dict(kind='callback',implementation='OtherChild',members=[dict(signature=g['normalized_signature'])])
  self.assertEqual(self.replay([g],[f])['metrics']['callback']['matched'],0)
  f['implementation']='InstalledChild'
  self.assertEqual(self.replay([g],[f])['metrics']['callback']['matched'],1)
  del f['implementation']
  self.assertEqual(self.replay([g],[f])['metrics']['callback']['matched'],0)
  # Legacy source without a concrete identity remains signature-only, explicitly
  # covered by the evaluator's existing unproven binding/precision status.
  del g['implementation']
  self.assertEqual(self.replay([g],[f])['metrics']['callback']['matched'],1)
 def test_operation_is_separate_from_three_capability_categories(self):
  g=dict(kind='webview_operation',name='loadUrl',normalized_signature='Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V',value_kind='dynamic_string')
  f=dict(kind='webview_operation',api=g['normalized_signature'],arguments=[dict(id='view'),dict(kind='unknown')])
  r=self.replay([g],[f]);self.assertEqual(r['metrics']['webview_operation']['matched'],1);self.assertNotIn('bridge',r['metrics']);self.assertEqual(r['positive_host_recall']['matched'],1)
  self.assertEqual(self.replay([g],[dict(f,api='LWrong;->loadUrl(Ljava/lang/String;)V')])['metrics']['webview_operation']['matched'],0)
 def test_setting_owner_is_not_discarded(self):
  g=dict(kind='setting',name='setJavaScriptEnabled',normalized_api='Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V',value='true',value_kind='literal')
  f=dict(kind='setting',api='Lcom/tencent/smtt/sdk/WebSettings;->setJavaScriptEnabled(Z)V',values=['true'])
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],0)

 def test_string_setting_values_are_case_sensitive(self):
  g=dict(kind='setting',name='setUserAgentString',normalized_api='Landroid/webkit/WebSettings;->setUserAgentString(Ljava/lang/String;)V',value='"Agent/ABC"',value_kind='literal')
  f=dict(kind='setting',api=g['normalized_api'],values=['agent/abc'])
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],0)
  f['values']=['Agent/ABC']
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],1)
  # An unquoted source literal and its significant whitespace are also exact.
  g['value']=' Agent/ABC ';f['values']=['Agent/ABC']
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],0)
  f['values']=[' Agent/ABC ']
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],1)
  # Boolean-shaped strings are not boolean settings.
  g['value']='"TRUE"';f['values']=['true']
  self.assertEqual(self.replay([g],[f])['metrics']['setting']['matched'],0)

 def test_same_activity_wrong_webview_is_not_a_match(self):
  pairs=[
   (dict(kind='setting',name='setJavaScriptEnabled',normalized_api='Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V',value='true',value_kind='literal'),dict(kind='setting',api='Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V',values=['true']),'setting'),
   (dict(kind='bridge',name='native',registration_name='native'),dict(kind='bridge',registration_name='native',members=[]),'bridge'),
   (dict(kind='callback',name='onPageFinished',normalized_signature='LClient;->onPageFinished()V'),dict(kind='callback',members=[dict(signature='LClient;->onPageFinished()V')]),'callback')]
  for g,f,category in pairs:
   g['webview_constraint']=dict(types=['PageWebView']);f['webview']=dict(id='other',type='ServiceWebView')
   r=self.replay([g],[f]);self.assertEqual(r['metrics'][category]['matched'],0);self.assertEqual(r['webview_constraint_coverage']['activity_only'],0)
   f['webview']=dict(id='page',type='PageWebView')
   self.assertEqual(self.replay([g],[f])['metrics'][category]['matched'],1)
   f['webview']=dict(id='union',type='unknown');f['webview_alternatives']=[dict(id='page',type='PageWebView'),dict(id='other',type='ServiceWebView')]
   r=self.replay([g],[f]);self.assertEqual(r['metrics'][category]['matched'],1);self.assertFalse(r['webview_constraint_coverage']['exact_instance_identity_verified'])
 def test_unconstrained_gold_is_explicitly_activity_only(self):
  g=dict(kind='bridge',name='native');f=dict(kind='bridge',registration_name='native',members=[])
  r=self.replay([g],[f]);self.assertEqual(r['webview_constraint_coverage']['activity_only'],1);self.assertEqual(r['acceptance'],'unproven')
 def test_malformed_webview_constraint_does_not_pass(self):
  for constraint in [{},dict(types=[]),dict(types='PageWebView'),dict(unsupported='x')]:
   g=dict(kind='bridge',name='native',webview_constraint=constraint);f=dict(kind='bridge',registration_name='native',members=[])
   r=self.replay([g],[f]);self.assertEqual(r['metrics']['bridge']['matched'],0);self.assertEqual(r['metrics']['bridge']['unscorable'],1)

 def test_unconfirmed_source_host_is_retained_without_inflating_positives(self):
  g=dict(kind='bridge',name='native',positive_acceptance=False,binding_status='pending_host_binding')
  f=dict(kind='bridge',registration_name='native',members=[])
  r=self.replay([g],[f]);self.assertNotIn('bridge',r['metrics']);self.assertEqual(len(r['unconfirmed_source_facts']),1);self.assertEqual(r['positive_host_recall']['expected'],0)

if __name__=='__main__':unittest.main()
