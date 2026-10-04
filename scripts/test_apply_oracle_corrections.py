import copy
import json
import subprocess
import tempfile
import unittest
from pathlib import Path
from apply_oracle_corrections import correct,digest


class CorrectionsTest(unittest.TestCase):
 def fixture(self):
  old=dict(apk_sha256='apk',activity='Host',kind='callback',implementation='Parent',normalized_signature='LParent;->callback()V',positive_acceptance=True)
  new=dict(old,implementation='Child',declaring_implementation='Parent')
  change=dict(old_row=old,new_row=new,old_canonical_json_sha256=digest(old),new_canonical_json_sha256=digest(new),apk_sha256='apk',reviewer='source auditor',reason='installed Child inherits Parent callback',source_counterevidence=[dict(file='source',sha256='evidence')],based_on_report=False)
  return old,new,change
 def test_preserves_other_rows_and_input(self):
  old,new,change=self.fixture();other=dict(old,activity='Other');rows=[old,other];before=copy.deepcopy(rows)
  self.assertEqual(correct(rows,[change]),[new,other]);self.assertEqual(rows,before)
 def test_rejects_tamper_unknown_and_duplicate(self):
  old,new,change=self.fixture()
  for ledger in [[dict(change,new_canonical_json_sha256='wrong')],[change,change]]:
   with self.assertRaises(ValueError):correct([old],ledger)
  with self.assertRaisesRegex(ValueError,'does not belong'):correct([], [change])
 def test_requires_independent_evidence_and_apk_identity(self):
  old,new,change=self.fixture()
  for update in [dict(based_on_report=True),dict(reason=''),dict(source_counterevidence=[]),dict(apk_sha256='other')]:
   with self.assertRaises(ValueError):correct([old],[dict(change,**update)])
 def test_cli_retains_input_ledger_and_refuses_overwrite(self):
  old,new,change=self.fixture()
  with tempfile.TemporaryDirectory() as folder:
   root=Path(folder);source=root/'source.jsonl';ledger=root/'ledger.jsonl';out=root/'view'
   source.write_text(json.dumps(old)+'\n');ledger.write_text(json.dumps(change)+'\n')
   command=['python3',str(Path(__file__).with_name('apply_oracle_corrections.py')),'--oracle',str(source),'--ledger',str(ledger),'--out',str(out)]
   subprocess.run(command,check=True,capture_output=True)
   self.assertEqual(source.read_bytes(),(out/'original.jsonl').read_bytes())
   self.assertEqual(ledger.read_bytes(),(out/'corrections.jsonl').read_bytes())
   self.assertEqual(json.loads((out/'reviewed.jsonl').read_text()),new)
   before={f.name:f.read_bytes() for f in out.iterdir()}
   self.assertNotEqual(subprocess.run(command,capture_output=True).returncode,0)
   self.assertEqual({f.name:f.read_bytes() for f in out.iterdir()},before)


if __name__=='__main__':unittest.main()
