#!/usr/bin/env python3
"""Summarize JFR execution samples without equating samples with CPU seconds."""
import argparse
from collections import Counter, defaultdict
import hashlib
import json
from pathlib import Path


def summarize(events):
    phases = Counter()
    inclusive, nearest = defaultdict(Counter), defaultdict(Counter)
    truncated = Counter()
    for event in events:
        if event['type'] != 'jdk.ExecutionSample':
            continue
        stack = event['values'].get('stackTrace') or {}
        frames = [frame['method']['type']['name'].replace('/', '.') + '.' + frame['method']['name']
                  for frame in stack.get('frames', [])]
        phase = next((name for name, method in [
            ('bootstrap', 'org.example.ApplicationBootstrap.build'),
            ('activity', 'org.example.CapabilityEngine.analyzeActivity'),
            ('index', 'org.example.CapabilityIndex.read')]
                      if method in frames), 'other')
        phases[phase] += 1
        truncated[phase] += bool(stack.get('truncated'))
        analyzer = [frame for frame in frames if frame.startswith('org.example.')]
        inclusive[phase].update(set(analyzer))
        if analyzer:
            nearest[phase][analyzer[0]] += 1
    return {
        'scope': 'Execution sample counts, not CPU seconds. Inclusive method counts overlap. '
                 'Phase classification requires the corresponding frame to be retained; '
                 'truncated stacks can conceal callers. Not an APK acceptance measurement.',
        'total_samples': sum(phases.values()),
        'phase_samples': dict(phases),
        'truncated_stack_samples': dict(truncated),
        'by_phase': {phase: {
            'inclusive_analyzer_methods': dict(inclusive[phase].most_common(30)),
            'nearest_analyzer_frame': dict(nearest[phase].most_common(30))}
            for phase in phases}}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--samples', type=Path, required=True)
    parser.add_argument('--out', type=Path, required=True)
    args = parser.parse_args()
    content = args.samples.read_bytes()
    result = summarize(json.loads(content)['recording']['events'])
    result['samples_sha256'] = hashlib.sha256(content).hexdigest()
    args.out.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result['phase_samples']))


if __name__ == '__main__':
    main()
