#!/usr/bin/env python3
import json,pathlib,tempfile,unittest
from freeze_oracle import union_sources

class FreezeOracleTest(unittest.TestCase):
 def test_only_identical_full_rows_collapse(self):
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);a=root/'a.jsonl';b=root/'b.jsonl'
   missing=dict(activity='A',kind='bridge',name='missing',verdict='unmatched',evidence='source1')
   second=dict(missing,evidence='source2');rejected=dict(missing,positive_acceptance=False)
   a.write_text(json.dumps(missing)+'\n'+json.dumps(second)+'\n')
   b.write_text(json.dumps(dict(reversed(list(missing.items()))))+'\n'+json.dumps(rejected)+'\n')
   before=(a.read_bytes(),b.read_bytes());raw,meta=union_sources([a,b])
   rows=[json.loads(x) for x in raw.splitlines()]
   self.assertEqual(rows,[missing,second,rejected]);self.assertEqual(meta['identical_rows_deduplicated'],1)
   self.assertEqual(before,(a.read_bytes(),b.read_bytes()))
   c=root/'c.jsonl';c.write_bytes(raw);again,info=union_sources([c,a,b])
   self.assertEqual(raw,again);self.assertEqual(meta['unique_set_sha256'],info['unique_set_sha256'])
 def test_strict_append_rejects_missing_or_non_boolean_verdict(self):
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);old=root/'old.jsonl';new=root/'new.jsonl'
   legacy=dict(activity='A',kind='bridge',name='existing');old.write_text(json.dumps(legacy)+'\n')
   new.write_text(json.dumps(legacy)+'\n');union_sources([old,new],True)
   for verdict in [None,'false',0]:
    row=dict(activity='B',kind='bridge',name='new',positive_acceptance=verdict)
    new.write_text(json.dumps(row)+'\n')
    with self.assertRaisesRegex(ValueError,'explicit boolean'):union_sources([old,new],True)
   new.write_text(json.dumps(dict(row,kind='callback_binding',positive_acceptance=True))+'\n')
   with self.assertRaisesRegex(ValueError,'Unsupported positive fact kind'):union_sources([old,new],True)
   rejected=dict(row,positive_acceptance=False);new.write_text(json.dumps(rejected)+'\n')
   raw,_=union_sources([old,new],True);self.assertEqual([json.loads(x) for x in raw.splitlines()],[legacy,rejected])
 def test_strict_positive_shapes_do_not_silently_become_misses(self):
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);old=root/'old.jsonl';new=root/'new.jsonl';old.write_text('')
   row=dict(activity='A',kind='callback_registration',positive_acceptance=True,normalized_api='Landroid/webkit/WebViewClient;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V')
   new.write_text(json.dumps(row)+'\n')
   with self.assertRaisesRegex(ValueError,'member contract'):union_sources([old,new],True)
   row.update(kind='callback',webview_constraint={'types':['android.webkit.WebView'],'field':'A.view'})
   new.write_text(json.dumps(row)+'\n')
   with self.assertRaisesRegex(ValueError,'Unsupported source WebView constraint'):union_sources([old,new],True)
   row.update(webview_constraint={'types':['android.webkit.WebView']},source_webview_field='A.view')
   new.write_text(json.dumps(row)+'\n');raw,_=union_sources([old,new],True);self.assertEqual(json.loads(raw),row)
 def test_malformed_input_is_not_silently_skipped(self):
  with tempfile.TemporaryDirectory() as folder:
   p=pathlib.Path(folder)/'bad.jsonl';p.write_text('{broken\n')
   with self.assertRaises(json.JSONDecodeError):union_sources([p])
if __name__=='__main__':unittest.main()
