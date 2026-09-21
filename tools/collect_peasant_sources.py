"""Collect exact public wearable sources. Never substitute item thumbnails or another item."""
import hashlib
import json
from pathlib import Path
from urllib.request import urlopen

ROOT = 'https://lod-dressup-2.web.app/'
out = Path('qa/peasant-sources')
out.mkdir(parents=True, exist_ok=True)
report = []
for kind, identity in [('body', 'mm001'), ('armor', 'mu0000001'), ('armor', 'mu0000058')]:
    for path in [f'data/type/{kind}/{identity}.json', f'atlas/{kind}/{identity}.webp']:
        row = {'url': ROOT + path, 'identity': identity}
        try:
            with urlopen(row['url'], timeout=30) as response:
                data = response.read()
                row['content_type'] = response.headers.get('Content-Type')
            if path.endswith('.json'):
                value = json.loads(data)
                assert isinstance(value.get('sprites'), dict), 'Not a sprite manifest'
            else:
                assert data[:4] == b'RIFF' and data[8:12] == b'WEBP', 'Not a WebP atlas'
            dest = out / path
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
            row.update(status='FETCHED', bytes=len(data), sha256=hashlib.sha256(data).hexdigest())
        except Exception as error:
            row.update(status='UNAVAILABLE', reason=str(error))
        report.append(row)
        print(json.dumps(row, ensure_ascii=False))
(out / 'retrieval.json').write_text(json.dumps(report, indent=2, ensure_ascii=False))
