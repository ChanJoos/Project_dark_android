"""Bounded V221 candidate gate from the last full-verified source.
Only reviewed exact runtime blobs qualify; unknown changes fail back to full CI.
Final integration uses manual full verification or the full-regression PR label.
"""
import hashlib
import json
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
# V218 closure: runtime 3ca35fe7, full common prefix39617101084 + guarded continuation39619025005.
BASE = '41938aa525fe8654b2b5632f6f7bba59adabf4f2'
CONFIG = {'AGENTS.md', '.github/workflows/android.yml', '.github/workflows/rucesion-town-v156.yml',
          'tools/ci_scope_v216.py', 'tools/ci_scope_v216.json', 'tools/test_ci_scope_v216.py'}

def classify(paths, hashes, expected, force_full=False):
    if force_full:
        return True, 'explicit final/full verification'
    for p in paths:
        if p in expected:
            if hashes.get(p) != expected[p]:
                return True, 'unreviewed blob: ' + p
        elif not (p.startswith('docs/') or p.startswith('verification/') or p in CONFIG):
            return True, 'unreviewed path: ' + p
    return False, 'reviewed V221 portal navigation, corrected pools and Sense HP/user EXP scope; focused workflow required'

def main():
    expected = json.loads((ROOT/'tools/ci_scope_v216.json').read_text())
    paths = subprocess.check_output(['git', 'diff', '--name-only', BASE, 'HEAD'], cwd=ROOT, text=True).splitlines()
    hashes = {p: hashlib.sha256((ROOT/p).read_bytes()).hexdigest() for p in paths if p in expected and (ROOT/p).is_file()}
    full, reason = classify(paths, hashes, expected, os.environ.get('FORCE_FULL') == 'true')
    print(('FULL: ' if full else 'FOCUSED: ') + reason)
    with open(os.environ['GITHUB_OUTPUT'], 'a') as out:
        out.write('full=' + str(full).lower() + '\n')

if __name__ == '__main__':
    main()
