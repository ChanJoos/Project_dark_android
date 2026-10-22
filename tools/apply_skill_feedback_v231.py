"""User V231 display corrections; raw article bytes remain immutable."""
from pathlib import Path
import json,csv,hashlib
from PIL import Image
import numpy as np
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation'
def save(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
p=json.loads((R/'master/changes/V231_SKILL_FEEDBACK.json').read_text());assert p['status']=='ACCEPTED'
d=json.loads((A/'catalog.json').read_text());d['profiles']['ROGUE_STAB']=p['stabProfile'];save(A/'catalog.json',d)
# Always derive palettes from immutable source crops, never from a previously recolored image.
f=A/'rogue/references.json';refs=json.loads(f.read_text());by={r['id']:r for r in refs['rows']}
reference=by['SK_도적_014'];raw=Image.open(R/reference['sourcePath']).convert('RGBA').crop(reference['crop']);palette=np.asarray(raw)[:,:,:3].reshape(-1,3);palette=palette[np.argsort(palette@np.array([.299,.587,.114]))]
for id in p['brownIcons']:
 row=by[id];im=Image.open(R/row['sourcePath']).convert('RGBA').crop(row['crop']);rgba=np.array(im);rgb=rgba[:,:,:3].astype(float);lum=rgb@np.array([.299,.587,.114]);lumref=palette@np.array([.299,.587,.114]);# original luminance structure/glyph retained; use the same warm brown ramp as double stab
 mapped=np.stack([np.interp(lum,lumref,palette[:,c]) for c in range(3)],axis=2);rgba[:,:,:3]=np.round(mapped).astype('uint8');Image.fromarray(rgba).save(A/'rogue'/row['iconAssetPath']);row['displayTransform']='V231_DOUBLE_STAB_BROWN_PALETTE';row['displaySourceSha256']=hashlib.sha256((R/row['sourcePath']).read_bytes()).hexdigest()
save(f,refs)
f=A/'user-v228/manifest.json';m=json.loads(f.read_text());m['skills']['SK_무도가_028']['channels']['RECIPIENT_CONTACT']['scale']=1.9;save(f,m)
f=A/'rogue/manifest.json';m=json.loads(f.read_text());
m['skills']['SK_도적_003']['channels']={};m['skills']['SK_도적_003']['displayPolicy']='USER_V231_THROW_BODY_AND_DAMAGE_ONLY';save(f,m)
print('V231 rogue knee-forward source pose, brown icon palette, sphere/ring sizes, no shuriken particles')

f=A/'captured/manifest.json';m=json.loads(f.read_text())
for id in ['SK_도적_020','SK_도적_027']:m['skills'][id]['scale']=.416
save(f,m)
