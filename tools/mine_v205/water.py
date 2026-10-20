import cv2,numpy as np
from PIL import Image
from pathlib import Path
best=None
for n in range(1,84):
 a=np.array(Image.open(f'mine205/frames/{n:03}.png')).astype(float);r,g,b=a.transpose(2,0,1);water=(b>r+45)&(b>g+12)&(g>65)
 integ=cv2.integral(water.astype(np.uint8))
 for y in range(8,300,8):
  for x in range(24,480,8):
   frac=(integ[y+80,x+128]-integ[y,x+128]-integ[y+80,x]+integ[y,x])/(128*80)
   if best is None or frac>best[0]:best=(frac,n,x,y)
print(best)
_,n,x,y=best;a=np.array(Image.open(f'mine205/frames/{n:03}.png').crop((x,y,x+128,y+80))).astype(float)
# Periodic-plus-smooth decomposition: source detail preserved, only edge mismatch removed.
h,w=a.shape[:2];v=np.zeros_like(a);v[0]+=a[-1]-a[0];v[-1]+=a[0]-a[-1];v[:,0]+=a[:,-1]-a[:,0];v[:,-1]+=a[:,0]-a[:,-1]
den=2*np.cos(2*np.pi*np.arange(w)/w)[None,:]+2*np.cos(2*np.pi*np.arange(h)/h)[:,None]-4;den[0,0]=1
f=np.fft.fft2(v,axes=(0,1))/den[:,:,None];f[0,0]=0;smooth=np.fft.ifft2(f,axes=(0,1)).real
Image.fromarray(np.clip(a-smooth,0,255).astype('uint8')).save('mine205/water-periodic.png')
Image.fromarray(a.astype('uint8')).save('mine205/water-crop.png')

import shutil,hashlib,json
p=Path("project_dark/app/src/main/assets/mine-v205/water_flow.png")
shutil.copyfile("mine205/water-periodic.png",p)
r=Path("project_dark/master/source/mine_v205/assets.json");d=json.loads(r.read_text());d["assets"]=[a for a in d["assets"] if a["id"]!="water_flow"];d["assets"].append({"id":"water_flow","frame":n,"box":[x,y,x+128,y+80],"adaptation":"Periodic-plus-smooth edge correction; source ripple pixels; static material, not original animation.","sha256":hashlib.sha256(p.read_bytes()).hexdigest()});r.write_text(json.dumps(d,ensure_ascii=False,indent=2))
