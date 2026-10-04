#!/usr/bin/env python3
"""Replay terminal App reports against fixed and shared expanded oracle snapshots."""
import argparse
import json
from pathlib import Path
import re
import shutil
import subprocess
import time


def run(command):
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode:
        raise RuntimeError(str(command) + '\n' + result.stdout + result.stderr)
    return result.stdout


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--version', required=True)
    p.add_argument('--previous', required=True)
    p.add_argument('--oracle-manifest', required=True)
    p.add_argument('--fixed-manifest', default='docs/validation/generic-v3-facts/manifest.json')
    p.add_argument('--runs', default='test/runs')
    p.add_argument('--out', default='docs/validation')
    p.add_argument('--wait-seconds', type=float, default=0)
    a = p.parse_args()
    if any(not re.fullmatch(r'v[0-9]+', v) for v in [a.version, a.previous]):
        p.error('Versions must be v followed by digits')
    if a.version == a.previous or a.wait_seconds < 0:
        p.error('Versions must differ and wait-seconds must be nonnegative')
    fixed = json.loads(Path(a.fixed_manifest).read_text())
    expanded = json.loads(Path(a.oracle_manifest).read_text())
    by_package = {r['package']: r for r in expanded}
    if len(by_package) != len(expanded) or {r['package'] for r in fixed} != set(by_package):
        p.error('Fixed and expanded oracle package sets differ or contain duplicates')
    dest = Path(a.out)
    dest.mkdir(parents=True, exist_ok=True)
    batch = Path(a.runs) / ('generic-' + a.version)
    previous = Path(a.runs) / ('generic-' + a.previous)
    pending = {r['package']: r for r in fixed}
    end = time.monotonic() + a.wait_seconds
    while pending:
        summary = batch / 'summary.json'
        terminal = {r['package'] for r in json.loads(summary.read_text())} if summary.exists() else set()
        for package in sorted(set(pending) & terminal):
            row = pending[package]
            app = row['app']
            if by_package[package]['app'] != app:
                raise ValueError('Oracle App identity mismatch: ' + package)
            report = batch / package / 'capabilities.json'
            old = previous / package / 'capabilities.json'
            run(['python3', 'scripts/evaluate.py', '--report', str(report), '--oracle', row['oracle'],
                 '--out', str(dest / f'generic-{a.version}-{app}-frozen-score.json')])
            run(['python3', 'scripts/compare_oracle_matches.py', '--before', str(old), '--after', str(report),
                 '--oracle', row['oracle'], '--out', str(dest / f'generic-{a.version}-{app}-vs-{a.previous}-frozen-delta.json')])
            for version, path in [(a.previous, old), (a.version, report)]:
                run(['python3', 'scripts/evaluate.py', '--report', str(path), '--oracle', by_package[package]['oracle'],
                     '--out', str(dest / f'generic-{version}-{app}-expanded-{a.version}-score.json')])
            print('SCORED ' + app, flush=True)
            del pending[package]
        if pending:
            if time.monotonic() >= end:
                raise RuntimeError('Reports are not terminal: ' + ', '.join(sorted(pending)))
            time.sleep(min(5, max(0, end - time.monotonic())))
    for name in ['summary', 'environment']:
        shutil.copy2(batch / (name + '.json'), dest / f'generic-{a.version}-{name}.json')
    structure = run(['python3', 'scripts/check_compact.py', str(batch)])
    (dest / f'generic-{a.version}-structure.json').write_text(structure)
    print('DONE', flush=True)


if __name__ == '__main__':
    main()
