"""Fail closed if the pilot changes normal hardware rendering or misses its override."""
from pathlib import Path
import xml.etree.ElementTree as ET
ANDROID = '{http://schemas.android.com/apk/res/android}'
for variant, expected in [('captureSoftware', 'false'), ('debug', None)]:
    files = list(Path('app/build/intermediates/merged_manifests').glob(f'{variant}/**/AndroidManifest.xml'))
    if not files:
        files = list(Path('app/build/intermediates/merged_manifest').glob(f'{variant}/**/AndroidManifest.xml'))
    assert files, f'No merged manifest for {variant}'
    for file in files:
        root = ET.parse(file).getroot()
        app = root.find('application')
        activity = next(a for a in app.findall('activity') if a.get(ANDROID+'name').endswith('.MainActivity'))
        assert activity.get(ANDROID+'hardwareAccelerated') == expected, (variant, file)
        assert app.get(ANDROID+'hardwareAccelerated') is None, (variant, 'application override')
        if variant == 'captureSoftware':
            assert root.get('package') == 'com.projectdark.mobile.v266test.canvas'
            assert app.get(ANDROID+'label') == 'PROJECT DARK CANVAS PILOT'
        else:
            assert root.get('package') == 'com.projectdark.mobile.v266test'
            assert app.get(ANDROID+'label') == 'PROJECT DARK V266 TEST'
        print(f'{variant}: {file}: hardwareAccelerated={expected}; package={root.get("package")}')
