from pathlib import Path
from PIL import Image
import numpy as np,json,hashlib
p=Path('mine203/assets');im=Image.open('mine203/frames/native068.png').convert('RGB');a=np.array(im).astype(float);best=None
for y in range(3,82,3):
 for x in range(245,555,3):
  b=a[y:y+28,x:x+56];blue=(b[:,:,2]>b[:,:,0]+45)&(b[:,:,2]>b[:,:,1]+15)
  if blue.mean()<.997:continue
  score=b.std(axis=(0,1)).mean()
  if best is None or score<best[0]:best=(score,x,y)
_,x,y=best
im.crop((x,y,x+56,y+28)).save(p/'water_material.png');Image.open('mine203/frames/native050.png').crop((500,10,580,90)).convert('RGB').save(p/'cliff_material.png')
f=Path('mine203/assets.json');d=json.loads(f.read_text());d['continuousMaterials']={'water':'world-anchored mirrored pixel material, no tile-sized rock motifs','cliff':'continuous jagged stone wall mesh from source material; source fragment no longer repeated'}
for name,frame,box in [('water_material',68,[x,y,x+56,y+28]),('cliff_material',50,[500,10,580,90])]:
 raw=p/(name+'.png');d['assets'].append({'id':name,'frame':frame,'box':box,'polygon':None,'greenMask':False,'size':list(Image.open(raw).size),'sha256':hashlib.sha256(raw.read_bytes()).hexdigest()})
f.write_text(json.dumps(d,ensure_ascii=False,indent=2));print('WATER',best)
