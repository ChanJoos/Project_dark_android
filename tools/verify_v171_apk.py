"""Verify the exact V171 combat and town integration APK, its source identity and retained assets."""
import datetime, hashlib, json, os, pathlib, subprocess, sys, zipfile
apk_path = pathlib.Path(sys.argv[1])
source = os.environ['PROJECT_DARK_SOURCE_SHA']
assert len(source) == 40 and all(c in '0123456789abcdef' for c in source)
assert subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip() == source
subprocess.run(['git', 'diff', '--exit-code', '--', 'app/src/main/java', 'app/build.gradle'], check=True)
pose_root = pathlib.Path('assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v3/giant_mantis')
poses = sorted(pose_root.glob('*.png'))
assert len(poses) == 12, len(poses)
assert {p.name for p in poses} == {f'{state}_{direction}.png' for state in ('idle','walk','attack') for direction in ('nw','ne','sw','se')}
with zipfile.ZipFile(apk_path) as apk:
    assert apk.read('assets/mobile-hud-v136/icons.png') == pathlib.Path('app/src/main/assets/mobile-hud-v136/icons.png').read_bytes()
    identity = json.loads(apk.read('assets/build_identity.json'))
    assert identity['sourceCommit'] == source, identity
    assert identity['runId'] == os.environ['GITHUB_RUN_ID'], identity
    assert identity['versionCode'] == 171, identity
    assert len(list(pathlib.Path('app/src/main/assets/rucesion-v154').glob('*'))) >= 26
    for path in pathlib.Path('app/src/main/assets/rucesion-v154').glob('*'):
        assert apk.read('assets/rucesion-v154/'+path.name)==path.read_bytes(),path
    for path in pathlib.Path('app/src/main/assets/abel-video-v147').glob('*'):
        assert apk.read('assets/abel-video-v147/'+path.name)==path.read_bytes(),path
    for path in pathlib.Path('app/src/main/assets/abel-town-v144').glob('*'):
        assert apk.read('assets/abel-town-v144/'+path.name)==path.read_bytes(),path
    for path in poses:
        packaged = 'assets/pote_monsters_generated_v1/sprites/campaign_v3/giant_mantis/' + path.name
        assert apk.read(packaged) == path.read_bytes(), path
    quality=list(pathlib.Path('assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v3').glob('*/*.png'))
    assert len(quality)==168
    for frame in quality:
        assert apk.read('assets/pote_monsters_generated_v1/sprites/campaign_v3/'+frame.parent.name+'/'+frame.name)==frame.read_bytes(),frame
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
    assert len(json.loads(apk.read('assets/equipment-icons/manifest.json'))['items']) == 172
    for path in pathlib.Path('app/src/main/assets/equipment-icons').glob('*'):
        assert apk.read('assets/equipment-icons/'+path.name)==path.read_bytes(),path
    for path in pathlib.Path('app/src/main/assets/classic-ui').glob('*'):
        assert apk.read('assets/classic-ui/'+path.name)==path.read_bytes(),path
    assert len(list(pathlib.Path('app/src/main/assets/classic-ui').glob('*.png')))==11
    for path in pathlib.Path('app/src/main/assets/item-icons').glob('*'):
        assert apk.read('assets/item-icons/'+path.name)==path.read_bytes(),path
    for path in pathlib.Path('app/src/main/assets/wearable-atlases').glob('*.webp'):
        assert apk.read('assets/wearable-atlases/'+path.name)==path.read_bytes(),path
    assert apk.read('assets/ui-fonts/GowunBatang-Heading.ttf')==pathlib.Path('app/src/main/assets/ui-fonts/GowunBatang-Heading.ttf').read_bytes()
    with zipfile.ZipFile('master/source/equipment/identity_v128/generated_assets.zip') as bundle:
        for name in bundle.namelist():
            assert apk.read('assets/'+name)==bundle.read(name), name
    assert 'assets/equipment-icons/it_ring_threelinegold.png' not in apk.namelist()
    assert json.loads(apk.read('assets/item-icons/manifest.json'))['items']['IT_RING_THREELINEGOLD']['assetPath'] == 'item-icons/ring_reference_v133.png'
    with zipfile.ZipFile("master/assets/abel_v139/generated_assets.zip") as coast:
        assert len(coast.namelist()) == 28
        for name in coast.namelist():
            if any(part in name for part in ("/red_octopus/","/crab/","/berserker/","/pong/")): continue
            assert apk.read("assets/"+name) == coast.read(name), name
    with zipfile.ZipFile("master/assets/abel_v140/generated_assets.zip") as coast:
        assert len(coast.namelist()) == 50
        for name in coast.namelist():
            if any(part in name for part in ("/red_octopus/","/crab/","/berserker/","/pong/")): continue
            assert apk.read("assets/"+name) == coast.read(name), name
    with zipfile.ZipFile("master/assets/abel_v141/generated_assets.zip") as coast:
        assert len(coast.namelist()) == 36
        for name in coast.namelist(): assert apk.read("assets/"+name) == coast.read(name), name
    with zipfile.ZipFile("master/assets/abel_v142/generated_assets.zip") as coast:
        assert len(coast.namelist()) == 96
        for name in coast.namelist(): assert apk.read("assets/"+name) == coast.read(name), name
    assert not any(any(part in n for part in ("/red_octopus/", "/crab/", "/berserker/", "abel_v140/pong/")) for n in apk.namelist())
    with zipfile.ZipFile('master/assets/rucesion_v158/generated_assets.zip') as ruc:
        assert len(ruc.namelist())==122
        for name in ruc.namelist():assert apk.read('assets/'+name)==ruc.read(name),name
    for path in pathlib.Path('app/src/main/assets/coast-v159').glob('*'):
        assert apk.read('assets/coast-v159/'+path.name)==path.read_bytes(),path
    with zipfile.ZipFile('app/build/generated/abel-v161-pack/generated_assets.zip') as revised:
        assert len(revised.namelist()) == 84
        for name in revised.namelist(): assert apk.read('assets/'+name)==revised.read(name),name
    assert apk.testzip() is None
sdk = os.environ['ANDROID_HOME']
badging = subprocess.check_output([sdk+'/build-tools/35.0.0/aapt', 'dump', 'badging', str(apk_path)], text=True)
assert "versionCode='171'" in badging and "versionName='1.71-test-twohand'" in badging, badging[:300]
expected_package = 'com.projectdark.mobile.v171test' if os.environ.get('PROJECT_DARK_SIDE_BY_SIDE') == '1' else 'com.projectdark.mobile'
assert "package: name='"+expected_package+"'" in badging
if expected_package.endswith(".v171test"): assert "application-label:'PROJECT DARK V171 TEST'" in badging
resource_table = subprocess.check_output([sdk+'/build-tools/35.0.0/aapt', 'dump', 'resources', str(apk_path)], text=True)
assert 'name='+expected_package in resource_table, resource_table[:400]
for drawable in ('player_peasant_idle_walk','player_body_mm001_action02_0','player_shirt_mu0000001_source'):
    assert ':drawable/'+drawable in resource_table, drawable
info = dict(identity, applicationId=expected_package, updateCompatibleWithV167=False if expected_package.endswith('.v171test') else 'PENDING', apkSha256=hashlib.sha256(apk_path.read_bytes()).hexdigest(), apkBytes=apk_path.stat().st_size,
            builtSeoul=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=9))).isoformat(),
            scope="V171 test-mode learned eligibility and startup restore ordering for twohand equip; retained independent linked basic hits, learned twohand equip gate, video-adapted 0.80s twohand cycle; retained source c twohand blades, consistent four-facing basic/skill renderer, visible joystick, cached actor visual projection and weapon rigs, lean resource projection; retained persisted random CON/WIS growth and actual level-up receipt; asymmetric two-hand/shield equip; Chungryong two-hand BODY; reagent-shop town managers/arrival; shared orange Curum and original Maradium HUD artwork; retained V167 equipment/job mapping and explicit weapon ranges; STR/INT/DEX combat and CON/WIS future level growth integration; one/two-hand four-facing motion and weapon-hand alignment; recipient-centered non-Crasher finishers; Dara resource prerequisite and damage; town-local hunting destinations; collision-free Abel travel arrival; Abel six flowerbeds, overlapping-tree removal and pavement cleanup. Source artwork preserved; original equipment gaps and phone/user visual acceptance PENDING.",
            bossPoses=len(poses), phoneAcceptance='PENDING', userVisualAcceptance={'RucesionMonsters':'USER_ACCEPTED_20261015','AbelCalibrationAndMaps':'PENDING'})
(apk_path.parent/'BUILD_INFO.json').write_text(json.dumps(info, ensure_ascii=False, indent=2)+'\n')
print(json.dumps(info, ensure_ascii=False, indent=2))

