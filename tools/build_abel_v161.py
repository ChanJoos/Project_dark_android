"""Pack reviewed V161 sheets; preserve source bytes and record every extraction."""
from pathlib import Path
import hashlib, io, json, zipfile
import numpy as np
from PIL import Image
from scipy.ndimage import label, find_objects

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'master/source/abel_v161'
DEST = ROOT / 'app/build/generated/abel-v161-pack'
DEST.mkdir(parents=True, exist_ok=True)
frames, families = {}, {}
for path in sorted(SOURCE.glob('*.png')):
    sheet = Image.open(path).convert('RGBA')
    pixels = np.array(sheet)
    labels, _ = label(pixels[:, :, 3] >= 32)
    components = []
    for ident, region in enumerate(find_objects(labels), 1):
        area = int((labels[region] == ident).sum())
        if area > 300:
            components.append((ident, region, area))
    assert len(components) == 12, (path, len(components))
    components.sort(key=lambda c: (c[1][0].start+c[1][0].stop)/2)
    cells = []
    for row in range(3):
        group = sorted(components[row*4:row*4+4], key=lambda c: c[1][1].start)
        for col, (ident, region, area) in enumerate(group):
            y, x = region
            assert x.start > 0 and y.start > 0 and x.stop < sheet.width and y.stop < sheet.height
            # Only this connected sprite; excludes transparent RGB and tiny generation speckles.
            data = pixels[y, x].copy()
            data[:, :, 3][labels[y, x] != ident] = 0
            cell = Image.fromarray(data, 'RGBA')
            cells.append((row, col, cell, [x.start,y.start,x.stop,y.stop]))
    # One family scale across all actions/directions. Never normalize individual poses.
    ratio = min(176/max(c.width for _,_,c,_ in cells), 176/max(c.height for _,_,c,_ in cells))
    records = []
    for row, col, cell, crop in cells:
        state = ('idle','walk','attack')[row]
        facing = ('nw','ne','sw','se')[col]
        cell = cell.resize((round(cell.width*ratio), round(cell.height*ratio)), Image.Resampling.NEAREST)
        frame = Image.new('RGBA', (192,192))
        frame.alpha_composite(cell, ((192-cell.width)//2,184-cell.height))
        stream = io.BytesIO(); frame.save(stream,format='PNG')
        name = f'pote_monsters_generated_v1/sprites/abel_v161/{path.stem}/{state}_{facing}.png'
        frames[name] = stream.getvalue()
        records.append(dict(path=name,crop=crop,bounds=list(frame.getbbox()),sha256=hashlib.sha256(frames[name]).hexdigest()))
    assert len({r['sha256'] for r in records}) == 12, path
    families[path.stem] = dict(sourceSha256=hashlib.sha256(path.read_bytes()).hexdigest(),sourceSize=list(sheet.size),familyScale=ratio,frames=records)
lock = SOURCE/'provenance.json'
if lock.exists():
    expected = json.loads(lock.read_text())['families']
    assert set(expected) == set(families), 'source family set differs from reviewed lock'
    for key, family in families.items():
        assert family == expected[key], ('reviewed sprite bytes changed', key)
with zipfile.ZipFile(DEST/'generated_assets.zip','w',zipfile.ZIP_DEFLATED) as bundle:
    for name, data in sorted(frames.items()):
        entry=zipfile.ZipInfo(name,(2026,10,15,0,0,0));entry.compress_type=zipfile.ZIP_DEFLATED
        bundle.writestr(entry,data)
(DEST/'provenance.json').write_text(json.dumps(dict(evidence='ADAPTED_GENERATED_REFERENCE_RECONSTRUCTION',canvas=[192,192],groundY=184,alphaCutoff=32,families=families,originalAnimationVerified=False,userVisualAcceptance='PENDING'),indent=2)+'\n')
print('V161 packed',len(frames),'frames',len(families),'families')
