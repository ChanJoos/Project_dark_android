"""Reproduce the V142 reviewed 4-column/3-row generated sheet crops."""
from pathlib import Path
from PIL import Image
import hashlib, io, json, zipfile

root = Path(__file__).resolve().parents[1]
source = root / 'master/source/abel_v142'
frames = []
sources = {}
for family in ('grab', 'armored', 'octopus', 'purple_egg', 'orange_float', 'purple_maw', 'blue_boss', 'kingark'):
    path = source / (family + '.png')
    sheet = Image.open(path).convert('RGBA')
    # Generated alpha contains invisible <=15/255 speckles outside each sprite.
    # Preserve source bytes; remove only those extraction artefacts in the pack.
    sheet.putalpha(sheet.getchannel('A').point(lambda a: a if a >= 16 else 0))
    alpha = sheet.getchannel('A')
    def seam(n, total, horizontal):
        target = round(n*total/(3 if horizontal else 4))
        candidates = []
        for position in range(max(1,target-64), min(total-1,target+65)):
            stripe = alpha.crop((0,position,sheet.width,position+1) if horizontal else (position,0,position+1,sheet.height))
            if stripe.getbbox() is None: candidates.append(position)
        assert candidates, (family, 'no transparent sheet seam', n, horizontal)
        runs = []
        for p in candidates:
            if not runs or p > runs[-1][-1]+1: runs.append([p])
            else: runs[-1].append(p)
        midpoints = [r[len(r)//2] for r in runs if len(r)>2]
        assert midpoints, (family, 'no padded seam', n, horizontal)
        return min(midpoints, key=lambda p: abs(p-target))
    xs = [0]+[seam(n,sheet.width,False) for n in range(1,4)]+[sheet.width]
    ys = [0]+[seam(n,sheet.height,True) for n in range(1,3)]+[sheet.height]
    cells = []
    for row, state in enumerate(('idle', 'walk', 'attack')):
        for col, facing in enumerate(('nw', 'ne', 'sw', 'se')):
            box = (xs[col],ys[row],xs[col+1],ys[row+1])
            cell = sheet.crop(box)
            occupied = cell.getchannel('A').getbbox()
            assert occupied, (family, state, facing)
            # A cell boundary must not cut through an authored silhouette.
            assert occupied[0] > 0 and occupied[1] > 0 and occupied[2] < cell.width and occupied[3] < cell.height, (family, state, facing, occupied)
            cells.append((state, facing, cell.crop(occupied)))
    scale = min(176/max(c.width for _, _, c in cells), 176/max(c.height for _, _, c in cells))
    for state, facing, cell in cells:
        cell = cell.resize((round(cell.width*scale), round(cell.height*scale)), Image.Resampling.NEAREST)
        frame = Image.new('RGBA', (192, 192))
        frame.alpha_composite(cell, ((192-cell.width)//2, 184-cell.height))
        stream = io.BytesIO(); frame.save(stream, format='PNG')
        frames.append((f'pote_monsters_generated_v1/sprites/abel_v142/{family}/{state}_{facing}.png', stream.getvalue()))
    sources[family] = hashlib.sha256(path.read_bytes()).hexdigest()
dest = root / 'master/assets/abel_v142'; dest.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(dest/'generated_assets.zip', 'w', zipfile.ZIP_DEFLATED) as bundle:
    for name, data in sorted(frames):
        entry = zipfile.ZipInfo(name, (2026, 10, 12, 0, 0, 0)); entry.compress_type = zipfile.ZIP_DEFLATED
        bundle.writestr(entry, data)
manifest = dict(evidence='ADAPTED_GENERATED_REFERENCE_RECONSTRUCTION', sourceSha256=sources,
                frames={name: hashlib.sha256(data).hexdigest() for name, data in frames}, canvas=[192,192], groundY=184,
                sourceLayout=[4,3], extractionAlphaCutoff=16, originalAnimationVerified=False, userVisualAcceptance='PENDING')
(dest/'provenance.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2)+'\n')
print('V142 sprites:', len(frames))
