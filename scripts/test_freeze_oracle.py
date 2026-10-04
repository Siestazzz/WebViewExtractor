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
 def test_malformed_input_is_not_silently_skipped(self):
  with tempfile.TemporaryDirectory() as folder:
   p=pathlib.Path(folder)/'bad.jsonl';p.write_text('{broken\n')
   with self.assertRaises(json.JSONDecodeError):union_sources([p])
if __name__=='__main__':unittest.main()
