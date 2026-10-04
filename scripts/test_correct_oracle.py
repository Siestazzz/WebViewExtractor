import copy
import json
import tempfile
import unittest
from pathlib import Path
from correct_oracle import apply, digest, row_hash


class CorrectionsTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.old = dict(apk_sha256='apk', activity='A', kind='callback', positive_acceptance=True)
        self.new = dict(self.old, positive_acceptance=False, binding_status='source-declared-unforwarded')
        self.raw = (json.dumps(self.old) + '\n').encode()
        (self.root / 'snapshot.jsonl').write_bytes(self.raw)
        self.c = dict(correction_id='c1', reviewer='independent reviewer', reason='SDK does not dispatch',
                      source_counterevidence=[dict(file='SDK.java', line=1, sha256='source-hash')],
                      provenance='source-first', based_on_report=False, apk_sha256='apk',
                      old_row=self.old, new_row=self.new, old_canonical_json_sha256=row_hash(self.old),
                      new_canonical_json_sha256=row_hash(self.new), old_file='snapshot.jsonl',
                      old_file_sha256=digest(self.raw), old_line=1)

    def test_preserves_unrelated_failures_and_history(self):
        failure = dict(self.old, activity='B', name='still-missing')
        result = apply([self.old, failure, self.new], [self.c], self.root)
        self.assertEqual(len(result), 3)
        self.assertIn(failure, result)
        self.assertIn(self.new, result)
        self.assertEqual(result[0]['oracle_correction']['original_row_sha256'], row_hash(self.old))
        self.assertFalse(result[0]['positive_acceptance'])
        self.assertTrue(self.old['positive_acceptance'])
        self.assertEqual((self.root / 'snapshot.jsonl').read_bytes(), self.raw)

    def test_rejects_stale_or_unreviewed_evidence(self):
        for key, value in [('old_file_sha256', 'wrong'), ('new_canonical_json_sha256', 'wrong'),
                           ('old_line', 0), ('apk_sha256', 'other'), ('based_on_report', True),
                           ('source_counterevidence', [])]:
            c = copy.deepcopy(self.c)
            c[key] = value
            with self.subTest(key=key), self.assertRaises(ValueError):
                apply([self.old], [c], self.root)

    def test_no_fuzzy_replacement_or_conflicts(self):
        with self.assertRaises(ValueError):
            apply([dict(self.old, evidence='different')], [self.c], self.root)
        c = dict(self.c, correction_id='c2')
        with self.assertRaises(ValueError):
            apply([self.old], [self.c, c], self.root)

    def test_already_current_requires_exact_replacement(self):
        self.assertEqual(apply([self.new], [self.c], self.root), [self.new])
        with self.assertRaises(ValueError):
            apply([dict(self.new, evidence='different')], [self.c], self.root)


if __name__ == '__main__':
    unittest.main()
