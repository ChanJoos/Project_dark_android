"""Fail APK delivery when recovered source frames are absent or differ from reviewed bytes."""
import hashlib
import json
import sys
import zipfile
from pathlib import Path

proof = json.loads(Path('verification/V106_RESTORED_EQUIPMENT.json').read_text())
with zipfile.ZipFile(sys.argv[1]) as apk:
    for item in proof['restored']:
        entry = item['apkEntry']
        assert entry in apk.namelist(), f'Missing packaged equipment: {entry}'
        assert hashlib.sha256(apk.read(entry)).hexdigest() == item['sha256'], entry
    assert apk.testzip() is None
print(f"PASS: {len(proof['restored'])} restored exact source files in actual APK")
