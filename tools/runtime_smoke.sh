#!/usr/bin/env bash
set -euo pipefail
mkdir -p runtime-evidence
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.projectdark.mobile.test/com.projectdark.mobile.RuntimeSmokeInstrumentation | tee runtime-evidence/instrumentation.txt
adb logcat -d -v threadtime > runtime-evidence/logcat.txt
adb pull /sdcard/Android/data/com.projectdark.mobile/files/runtime-qa runtime-evidence/
python3 - <<'PY'
import json,pathlib
root=pathlib.Path('runtime-evidence')
assert 'PROJECT_DARK_RUNTIME_PASS' in (root/'instrumentation.txt').read_text()
report=json.loads((root/'runtime-qa/report.json').read_text())
assert report['status']=='PASS' and report['checks']>=20
assert len(list((root/'runtime-qa').glob('*.png')))==3
print('Real Android runtime evidence verified:',report)
PY
