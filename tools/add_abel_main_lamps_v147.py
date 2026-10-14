from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,cv2,json,hashlib
root=Path(__file__).resolve().parents[1];folder=root/'app/src/main/assets/abel-video-v147';src=root/'master/source/abel_video_v147';d=json.loads((folder/'layout.json').read_text());name='lamp_main';box=(353,46,380,176);anchor=[13,129];a=np.array(Image.open(src/'reference_frames/005.png').crop(box).convert('RGBA'));m=Image.new('L',(27,130));poly=[(11,0),(14,0),(14,8),(20,13),(24,22),(19,29),(16,35),(16,114),(25,123),(26,126),(20,129),(7,129),(0,126),(2,122),(10,114),(10,35),(5,29),(2,22),(6,13),(11,8)];ImageDraw.Draw(m).polygon(poly,fill=255);a[:,:,3]=np.array(m);rgb=a[:,:,:3].astype(float);a[(rgb[:,:,1]>rgb[:,:,0]*1.1)&(rgb[:,:,1]>rgb[:,:,2]*1.1),3]=0;Image.fromarray(a).save(folder/(name+'.png'));spec=dict(id=name,sourceFrame=5,box=list(box),polygon=poly,anchor=anchor,size=[27,130],status='SOURCE_VISIBLE',sha256=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest());d['assets']=[x for x in d['assets'] if x['id']!=name]+[spec]
found=[];t=a[:,:,:3];mask=np.uint8(a[:,:,3]>0)*255
for r in d['views']:
 if r['map']!='main':continue
 im=np.array(Image.open(src/'reference_frames'/f"{r['frame']:03d}.png").convert('RGB'))[:408];res=cv2.matchTemplate(im,t,cv2.TM_SQDIFF_NORMED,mask=mask);res=np.nan_to_num(res,nan=1,posinf=1,neginf=1)
 for _ in range(20):
  y,x=np.unravel_index(np.argmin(res),res.shape);err=float(res[y,x])
  if err>.055:break
  res[max(0,y-14):y+15,max(0,x-14):x+15]=1
  if x<17 or x+27>619:continue
  p=np.array(r['camera'])+[x,y]+anchor;old=next((q for q in found if np.linalg.norm(p-q['sourceWorld'])<18),None);row=dict(asset=name,scene='main',sourceWorld=p.round(2).tolist(),anchor=anchor,nativeSize=[27,130],sourceFrame=r['frame'],matchError=err,evidence='SOURCE_VISIBLE')
  if old is None:found.append(row)
  elif err<old['matchError']:old.update(row)
ps=d['scenes']['main']['props'];ps[:]=[p for p in ps if p['asset']!=name and not(p['asset']=='lamp' and any(np.linalg.norm(np.array(p['sourceWorld'])-q['sourceWorld'])<18 for q in found))];ps.extend(found);ps.sort(key=lambda p:p['sourceWorld'][1])
for p in [folder/'layout.json',src/'layout.json']:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
print('Source main lamps',len(found))
