"""Verify the exact V114 campaign APK, including its identity and Pote final-boss poses."""
import datetime, hashlib, json, os, pathlib, subprocess, sys, zipfile
apk_path = pathlib.Path(sys.argv[1])
source = os.environ['PROJECT_DARK_SOURCE_SHA']
assert len(source) == 40 and all(c in '0123456789abcdef' for c in source)
assert subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip() == source
subprocess.run(['git', 'diff', '--exit-code', '--', 'app/src/main/java', 'app/build.gradle'], check=True)
pose_root = pathlib.Path('assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v2/giant_mantis')
poses = sorted(pose_root.glob('*.png'))
assert len(poses) == 12, len(poses)
assert {p.name for p in poses} == {f'{state}_{direction}.png' for state in ('idle','walk','attack') for direction in ('nw','ne','sw','se')}
with zipfile.ZipFile(apk_path) as apk:
    identity = json.loads(apk.read('assets/build_identity.json'))
    assert identity['sourceCommit'] == source, identity
    assert identity['runId'] == os.environ['GITHUB_RUN_ID'], identity
    assert identity['versionCode'] == 114, identity
    for path in poses:
        packaged = 'assets/pote_monsters_generated_v1/sprites/campaign_v2/giant_mantis/' + path.name
        assert apk.read(packaged) == path.read_bytes(), path
    reference=list(pathlib.Path('assets/pote/production/reference_v113').glob('*'))
    assert len(reference)>=9
    for path in reference:
        assert apk.read('assets/reference_v113/'+path.name)==path.read_bytes(), path
    assert apk.read('assets/assets/world/portal/portal_reagent_shop.webp')==pathlib.Path('app/src/main/assets/assets/world/portal/portal_reagent_shop.webp').read_bytes()
    frames = list(pathlib.Path('assets/milles/production/interiors/v108').glob('field_mouse_*.png'))
    assert len(frames) == 16
    for path in frames:
        assert apk.read('assets/interiors/v108/' + path.name) == path.read_bytes(), path
    assert apk.read('assets/skill-presentation/catalog.json') == pathlib.Path('app/src/main/assets/skill-presentation/catalog.json').read_bytes()
    assert apk.testzip() is None
sdk = os.environ['ANDROID_HOME']
badging = subprocess.check_output([sdk+'/build-tools/35.0.0/aapt', 'dump', 'badging', str(apk_path)], text=True)
assert "versionCode='114'" in badging and "versionName='1.14-forest-performance-ui'" in badging, badging[:300]
expected_package = 'com.projectdark.mobile.v114test' if os.environ.get('PROJECT_DARK_SIDE_BY_SIDE') == '1' else 'com.projectdark.mobile'
assert "package: name='"+expected_package+"'" in badging
info = dict(identity, applicationId=expected_package, updateCompatibleWithV113=False if expected_package.endswith('.v114test') else 'PENDING', apkSha256=hashlib.sha256(apk_path.read_bytes()).hexdigest(), apkBytes=apk_path.stat().st_size,
            builtSeoul=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=9))).isoformat(),
            scope='Reference-derived forest trees/earth/current; per-actor depth and foreground canopy fade; map-wide quest-filtered AUTO; 72/96/108 population, 6-8s adapted respawn; preserved campaign/save/body',
            bossPoses=len(poses), phoneAcceptance='PENDING', userVisualAcceptance='PENDING')
(apk_path.parent/'BUILD_INFO.json').write_text(json.dumps(info, ensure_ascii=False, indent=2)+'\n')
print(json.dumps(info, ensure_ascii=False, indent=2))
