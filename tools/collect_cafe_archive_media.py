#!/usr/bin/env python3
"""Retain page-observed media bytes; verify them offline without revisiting Cafe."""
import argparse
import concurrent.futures
import hashlib
import io
import json
from pathlib import Path
import urllib.parse
import urllib.request
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / 'master/source/skill_fx/naver_cafe_archive_20261001'
INDEX = ARCHIVE / 'media.json'

def sha(data):
    return hashlib.sha256(data).hexdigest()

def source_url(observed):
    # The original URL is explicitly embedded in the rendered image's src.
    query = urllib.parse.parse_qs(urllib.parse.urlsplit(observed).query)
    nested = query.get('src', [None])[0]
    return nested.strip('"') if nested and nested.strip('"').startswith('https://') else observed

def verify():
    index = json.loads(INDEX.read_text())
    complete = 0
    for row in index['media']:
        if row['status'] != 'RETAINED':
            continue
        data = (ROOT / row['path']).read_bytes()
        assert sha(data) == row['sha256'], row['path']
        assert len(data) == row['bytes'], row['path']
        complete += 1
    print(f'OFFLINE_MEDIA_INTEGRITY_PASS {complete}/{len(index["media"])} media URLs')
    return complete == len(index['media'])

def fetch():
    cached = {r['sourceUrl']: r for r in json.loads(INDEX.read_text())['media']} if INDEX.exists() else {}
    references = {}
    for snapshot in sorted(ARCHIVE.glob('*.json')):
        if not snapshot.stem.isdigit():
            continue
        doc = json.loads(snapshot.read_text())
        for ordinal, image in enumerate(doc['images']):
            observed = image['src']
            url = source_url(observed)
            references.setdefault(url, []).append({'articleId': int(snapshot.stem), 'imageIndex': ordinal, 'observedUrl': observed})
    existing = {}
    for p in (ROOT / 'master/source/skill_fx').rglob('*'):
        if p.is_file() and p.suffix.lower() in {'.png', '.gif', '.jpg', '.jpeg', '.webp'}:
            existing.setdefault(sha(p.read_bytes()), str(p.relative_to(ROOT)))
    media_dir = ARCHIVE / 'media'
    media_dir.mkdir(exist_ok=True)

    def retain(item):
        url, refs = item
        old = cached.get(url)
        if old and old.get('status') == 'RETAINED':
            path = ROOT / old['path']
            if path.exists() and sha(path.read_bytes()) == old['sha256']:
                return dict(old, references=refs)
        row = {'sourceUrl': url, 'references': refs}
        try:
            with urllib.request.urlopen(url, timeout=30) as response:
                data = response.read()
                row.update(resolvedUrl=response.url, contentType=response.headers.get('Content-Type'))
            image = Image.open(io.BytesIO(data))
            digest = sha(data)
            suffix = {'JPEG': 'jpg', 'PNG': 'png', 'GIF': 'gif', 'WEBP': 'webp'}[image.format]
            path = existing.get(digest)
            if path is None:
                p = media_dir / (digest + '.' + suffix)
                p.write_bytes(data)
                path = str(p.relative_to(ROOT))
            durations = []
            for frame in range(getattr(image, 'n_frames', 1)):
                image.seek(frame)
                durations.append(image.info.get('duration', 0))
            row.update(status='RETAINED', path=path, sha256=digest, bytes=len(data), format=image.format,
                       width=image.width, height=image.height, frames=len(durations), durationsMs=durations,
                       bytePolicy='EXACT_HTTP_RESPONSE_NO_REENCODING',
                       sourceResolution='OBSERVED_ORIGINAL_URL' if url != refs[0]['observedUrl'] else 'PAGE_OBSERVED_CDN_VARIANT')
        except Exception as error:
            row.update(status='FETCH_FAILED', error=f'{type(error).__name__}: {error}')
        return row

    rows = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        for row in pool.map(retain, references.items()):
            rows.append(row)
            if len(rows) % 20 == 0:
                print(f'RETAINED {len(rows)}/{len(references)} source URLs', flush=True)
    INDEX.write_text(json.dumps({'revision': 'CAFE_MEDIA_BYTES_V1', 'media': rows}, ensure_ascii=False, indent=2) + '\n')
    print('RETAINED_URLS', sum(r['status'] == 'RETAINED' for r in rows), 'TOTAL_URLS', len(rows), flush=True)
    verify()

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--fetch', action='store_true', help='Download only image URLs already retained in the observed article snapshots.')
    args = parser.parse_args()
    if args.fetch:
        fetch()
    else:
        verify()
