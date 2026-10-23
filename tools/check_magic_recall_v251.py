"""Exact screenshot crops and preserved preview-only original sleep candidates."""
from pathlib import Path
from PIL import Image
import json,hashlib,zipfile
import numpy as np
R=Path(__file__).resolve().parents[1];S=R/'master/source/recalls_v251';O=R/'app/src/main/assets/recalls-v251';j=json.loads((O/'manifest.json').read_text());raw=Image.open(S/'1000060685.jpg').convert('RGBA');assert np.array_equal(np.array(raw),np.array(Image.open(S/'reference-rgba.png')))
assert len(j['recalls'])==10 and sum(r['currentTown'] for r in j['recalls'])==4
for r in j['recalls']:
 p=R/'app/src/main/assets'/r['path'];assert hashlib.sha256(p.read_bytes()).hexdigest()==r['sha256'];assert np.array_equal(np.array(Image.open(p)),np.array(raw.crop(r['crop'])))
from horror_original_formats import palette
from build_mage_v241 import epf
N=R/'master/source/narcoli_review_v251';review=json.loads((N/'receipt.json').read_text());assert review['status']=='PREVIEW_ONLY_USER_REVIEW_REQUIRED'
with zipfile.ZipFile(N/'original_sources.zip') as z:
 pal=palette(z.read('eff000.pal'))
 for row in review['candidates']:
  _,_,fs=epf(z.read(row['source']+'.epf'))
  for f in row['playback']:assert np.array_equal(np.array(Image.open(N/(row['source']+f'-frame{f}.png'))),pal[fs[f][2]])
print('V251:10 screenshot crops/4 runtime destinations and8 full original preview-only sleep frames PASS')
