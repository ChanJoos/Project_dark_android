#!/usr/bin/env python3
"""Prove every packaged Master icon is an exact original-pixel crop, never redrawn."""
from pathlib import Path
import hashlib,json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
DIR=ROOT/'app/src/main/assets/equipment-icons'
j=json.loads((DIR/'manifest.json').read_text())
assert len(j['appearances'])==30
for r in j['appearances']:
    source=ROOT/r['sourcePath'];asset=DIR/r['asset']
    if not source.exists() and 'sourceArchive' in r:
        import zipfile
        with zipfile.ZipFile(ROOT/r['sourceArchive']) as z:
            source.parent.mkdir(parents=True,exist_ok=True);source.write_bytes(z.read(r['sourceMember']))
    assert hashlib.sha256(source.read_bytes()).hexdigest()==r['sourceSha256']
    assert hashlib.sha256(asset.read_bytes()).hexdigest()==r['assetSha256']
    x,y,w,h=r['crop'];original=Image.open(source).convert('RGBA').crop((x,y,x+w,y+h));out=Image.open(asset).convert('RGBA')
    assert original.size==out.size and original.tobytes()==out.tobytes(),r['appearanceId']
print('EXACT_ORIGINAL_MASTER_ICONS_PASS',len(j['appearances']))

for ident,r in j['items'].items():
    assert ident==r['itemId']
    source=ROOT/r['sourcePath'];asset=DIR/r['asset']
    if not source.exists() and 'sourceArchive' in r:
        import zipfile
        with zipfile.ZipFile(ROOT/r['sourceArchive']) as z:
            source.parent.mkdir(parents=True,exist_ok=True);source.write_bytes(z.read(r['sourceMember']))
    assert hashlib.sha256(source.read_bytes()).hexdigest()==r['sourceSha256']
    assert hashlib.sha256(asset.read_bytes()).hexdigest()==r['assetSha256']
    x,y,w,h=r['crop'];crop=Image.open(source).convert('RGBA').crop((x,y,x+w,y+h))
    if r.get('projection')!='DECODED_FIRST_FRAME_EXACT_RGBA':
        bg=tuple(r['transparentBackgroundRGB'])
        crop.putdata([(*p[:3],0) if p[:3]==bg else p for p in crop.getdata()])
    out=Image.open(asset).convert('RGBA')
    assert crop.size==out.size and crop.tobytes()==out.tobytes(),ident
print('LABELLED_ORIGINAL_ACCESSORY_ICONS_PASS',len(j['items']))
