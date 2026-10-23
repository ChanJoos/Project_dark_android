"""Check retained original Z bytes -> complete unmasked runtime sequence."""
import json,zipfile,hashlib
from pathlib import Path
import numpy as np
from PIL import Image
from horror_original_formats import palette
from build_mage_v241 import epf
R=Path(__file__).resolve().parents[1];S=R/'master/source/narcoli_v253';O=R/'app/src/main/assets/skill-presentation/narcoli-v253'
r=json.loads((S/'receipt.json').read_text());q=json.loads((O/'manifest.json').read_text());atlas=np.array(Image.open(O/'z.png').convert('RGBA'))
with zipfile.ZipFile(S/'original_z.zip') as z:
 raw={n:z.read(n) for n in z.namelist()}
for n,b in raw.items():assert hashlib.sha256(b).hexdigest()==r['sourceFiles'][n]
p=palette(raw['eff000.pal']);fs=epf(raw['efct032.epf'])[2];piv=np.frombuffer(raw['efct032.tbl'],'<i2').reshape(-1,2);order=list(map(int,raw['effect.tbl'].decode().splitlines()[32].split()))
assert q['durationsMs']==[100]*len(order) and q['blend']=='NORMAL'
for i,k in enumerate(order):
 x,y,z=fs[k];im=p[z];dx=q['pivotX']-(int(piv[k,0])-x);dy=q['pivotY']-(int(piv[k,1])-y);h,w=im.shape[:2];sx=i*q['width']+dx
 assert np.array_equal(atlas[dy:dy+h,sx:sx+w],im)
 assert np.count_nonzero(atlas[:,i*q['width']:(i+1)*q['width'],3])==np.count_nonzero(im[...,3])
assert 'ADAPTED_VIDEO_TRACE' in r['aura']
print('V253 original Z complete source pixels/order/pivot PASS; aura explicitly ADAPTED_VIDEO_TRACE, not original-client verification')
