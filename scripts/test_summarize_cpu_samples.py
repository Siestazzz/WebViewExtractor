import unittest
from summarize_cpu_samples import summarize


def event(*methods, truncated=False, kind='jdk.ExecutionSample'):
    return {'type': kind, 'values': {'stackTrace': {
        'truncated': truncated,
        'frames': [{'method': {'type': {'name': name.rsplit('.', 1)[0].replace('.', '/')},
                               'name': name.rsplit('.', 1)[1]}} for name in methods]}}}


class SamplesTest(unittest.TestCase):
    def test_bootstrap_precedence_and_recursive_deduplication(self):
        method = 'org.example.DexFlow.decode'
        result = summarize([event(method, method, 'org.example.ApplicationBootstrap.build',
                                  'org.example.CapabilityEngine.analyzeActivity')])
        self.assertEqual(result['phase_samples'], {'bootstrap': 1})
        counts = result['by_phase']['bootstrap']
        self.assertEqual(counts['inclusive_analyzer_methods'][method], 1)
        self.assertEqual(counts['nearest_analyzer_frame'], {method: 1})

    def test_missing_frames_truncation_and_non_execution_events(self):
        result = summarize([event(truncated=True), event(kind='jdk.GarbageCollection'),
                            event('org.example.CapabilityIndex.read')])
        self.assertEqual(result['total_samples'], 2)
        self.assertEqual(result['phase_samples'], {'other': 1, 'index': 1})
        self.assertEqual(result['truncated_stack_samples']['other'], 1)


if __name__ == '__main__':
    unittest.main()
