"""Verify the actual candidate APK, including its source identity and all mouse frames."""
import datetime, hashlib, json, os, pathlib, subprocess, sys, zipfile
apk_path = pathlib.Path(sys.argv[1])
source = os.environ['PROJECT_DARK_SOURCE_SHA']
assert len(source) == 40 and all(c in '0123456789abcdef' for c in source)
assert subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip() == source
subprocess.run(['git', 'diff', '--exit-code', '--', 'app/src/main/java', 'app/build.gradle'], check=True)
with zipfile.ZipFile(apk_path) as apk:
    identity = json.loads(apk.read('assets/build_identity.json'))
    assert identity['sourceCommit'] == source, identity
    assert identity['runId'] == os.environ['GITHUB_RUN_ID'], identity
    assert identity['versionCode'] == 109, identity
    frames = list(pathlib.Path('assets/milles/production/interiors/v108').glob('field_mouse_*.png'))
    assert len(frames) == 16
    for path in frames:
        assert apk.read('assets/interiors/v108/' + path.name) == path.read_bytes(), path
    assert apk.read('assets/skill-presentation/catalog.json') == pathlib.Path('app/src/main/assets/skill-presentation/catalog.json').read_bytes()
    assert apk.testzip() is None
sdk = os.environ['ANDROID_HOME']
badging = subprocess.check_output([sdk+'/build-tools/35.0.0/aapt', 'dump', 'badging', str(apk_path)], text=True)
assert "versionCode='109'" in badging and "versionName='1.09-device-quest-rescue-fix'" in badging, badging[:300]
info = dict(identity, apkSha256=hashlib.sha256(apk_path.read_bytes()).hexdigest(), apkBytes=apk_path.stat().st_size,
            builtSeoul=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=9))).isoformat(),
            scope='MainActivity default preview ON: opening and growth quickquest AUTO; saved Shortblade 3-use restart; near non-damaging Rescue and live taunt AI; smaller 16-pose mice; native/UI/campaign regressions.',
            phoneAcceptance='PENDING', userVisualAcceptance='PENDING')
(apk_path.parent/'BUILD_INFO.json').write_text(json.dumps(info, ensure_ascii=False, indent=2)+'\n')
print(json.dumps(info, ensure_ascii=False, indent=2))
