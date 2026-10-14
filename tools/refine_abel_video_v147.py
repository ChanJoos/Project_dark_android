"""Reject object-colour terrain false positives using full diamond template matches."""
from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,cv2,json
root=Path(__file__).resolve().parents[1];folder=root/'app/src/main/assets/abel-video-v147';src=root/'master/source/abel_video_v147';d=json.loads((folder/'layout.json').read_text());names=['tile_white','tile_brick_rosette','tile_blue_water','tile_rock_paving','tile_sea_water','tile_grass'];templates=np.array([np.array(Image.open(folder/(name+'.png')))[:,:,:3] for name in names],dtype=np.float32);m=Image.new('L',(56,28));ImageDraw.Draw(m).polygon([(28,2),(52,14),(28,25),(3,14)],fill=255);mask=np.array(m)>0;features=templates[:,mask,:]
for key in ['main','east']:
 best=np.full((80,80),1e8);grid=np.zeros((80,80),int)
 for r in d['views']:
  if r['map']!=key:continue
  im=np.array(Image.open(src/'reference_frames'/f"{r['frame']:03d}.png"),dtype=np.float32);cx,cy=r['camera']
  for u in range(1,79):
   for v in range(1,79):
    x=round((u-v)*28-cx);y=round((u+v)*14-cy)
    if not(45<x<590 and 28<y<380):continue
    patch=im[y-14:y+14,x-28:x+28][mask];scores=((features-patch)**2).mean((1,2));k=int(scores.argmin());err=scores[k]
    if err<best[u,v] and err<2200:best[u,v]=err;grid[u,v]=k
 for kind in [2,4,5]:
  count,labels,stats,_=cv2.connectedComponentsWithStats(np.uint8(grid==kind),4)
  for j in range(1,count):
   if stats[j,cv2.CC_STAT_AREA]<6:grid[labels==j]=0
 if key=='east':grid[15:24,58:62]=2
 # Observed garden foundations: record the bounded source outline, not hidden full gardens.
 plots=[(5,[(25,282),(113,238),(201,282),(113,326)]),(2,[(242,153),(340,104),(438,153),(340,202)])] if key=='main' else [(28,[(18,281),(274,409),(274,470),(18,342)])]
 for n,poly in plots:
  cam=np.array(d['views'][n-1]['camera']);a=np.array(poly)+cam;uu=(a[:,0]/28+a[:,1]/14)/2;vv=(a[:,1]/14-a[:,0]/28)/2;u0,v0=max(1,int(np.ceil(uu.min()))),max(1,int(np.ceil(vv.min())));u1,v1=min(78,int(np.floor(uu.max()))),min(78,int(np.floor(vv.max())));grid[u0:u1+1,v0:v1+1]=5
 d['scenes'][key]['gridRows']=[''.join(map(str,row)) for row in grid]
for p in [folder/'layout.json',src/'layout.json']:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
print('Full source-diamond matching and observed boundaries refined')
# Repair observed rectangular basin footprints after objects occluded their tile centres.
for key in ['main','east']:
 grid=np.array([[int(v) for v in row] for row in d['scenes'][key]['gridRows']])
 if key=='east':grid[13:26,56:65][grid[13:26,56:65]==2]=0;grid[15:24,58:63]=2
 for kind in [2,4]:
  count,l,stats,_=cv2.connectedComponentsWithStats(np.uint8(grid==kind),4)
  for j in range(1,count):
   v,u,w,h,area=stats[j]
   if area>=8 and area/(w*h)>.65:grid[u:u+h,v:v+w]=kind
 d['scenes'][key]['gridRows']=[''.join(map(str,row)) for row in grid]
# One independently recorded second red facade (its upper/side edges are clipped in the source).
props=d['scenes']['main']['props'];name='building_two_storey'
if not any(p['asset']==name and np.linalg.norm(np.array(p['sourceWorld'])-[181.48,314.74])<20 for p in props):
 spec=next(a for a in d['assets'] if a['id']==name);props.append(dict(asset=name,scene='main',sourceWorld=[181.48,314.74],anchor=spec['anchor'],nativeSize=spec['size'],sourceFrame=5,evidence='SOURCE_VISIBLE_PARTIAL_REGISTERED_FACADE'))
# Source pool bench/lamp/railing, bounded independently and retaining original RGB.
im=Image.open(src/'reference_frames/028.png').convert('RGBA');cam=np.array(d['views'][27]['camera'])
for name,box,poly,anchor,mode in [
 ('bench_pool',(334,318,388,372),[(0,18),(37,0),(53,5),(53,31),(14,53),(0,39)],[27,51],'wood'),
 ('lamp_pool',(272,289,298,408),[(11,0),(16,1),(16,7),(22,12),(25,20),(20,30),(16,35),(16,112),(23,118),(5,118),(10,109),(10,35),(4,29),(0,19),(4,11),(10,7)],[13,119],'polygon'),
 ('pool_rail_down',(49,267,80,313),[(0,3),(6,0),(12,7),(12,17),(24,22),(30,17),(30,45),(0,29)],[0,29],'rail')]:
 a=np.array(im.crop(box));rgb=a[:,:,:3].astype(float);m=Image.new('L',(a.shape[1],a.shape[0]));ImageDraw.Draw(m).polygon(poly,fill=255);mask=np.array(m)
 if mode=='wood':mask[np.logical_not((rgb[:,:,0]>rgb[:,:,1]*1.18)&(rgb[:,:,0]>rgb[:,:,2]*1.4)|(rgb.mean(2)<95))]=0
 if mode=='rail':mask[(rgb[:,:,2]>rgb[:,:,0]*1.5)&(rgb[:,:,1]>rgb[:,:,0]*1.25)]=0
 a[:,:,3]=mask;Image.fromarray(a).save(folder/(name+'.png'));spec=dict(id=name,sourceFrame=28,box=list(box),polygon=poly,anchor=anchor,size=[a.shape[1],a.shape[0]],status='SOURCE_VISIBLE_PARTIAL' if name=='lamp_pool' else 'SOURCE_VISIBLE');d['assets']=[x for x in d['assets'] if x['id']!=name]+[spec];ps=d['scenes']['east']['props'];ps[:]=[p for p in ps if p['asset']!=name]
 points=[(49+28*i,296+14*i) for i in range(6)] if mode=='rail' else [tuple(np.array(box[:2])+anchor)]
 for xy in points:ps.append(dict(asset=name,scene='east',sourceWorld=(cam+xy).tolist(),anchor=anchor,nativeSize=spec['size'],sourceFrame=28,evidence=spec['status']))
# A clean observed inn checker patch excludes table supports.
a=np.array(Image.open(src/'reference_frames/026.png').crop((232,200,288,228)).convert('RGBA'));m=Image.new('L',(56,28));ImageDraw.Draw(m).polygon([(28,0),(55,14),(28,27),(0,14)],fill=255);a[:,:,3]=np.array(m);Image.fromarray(a).save(folder/'tile_inn_checker.png')
for s in d['scenes'].values():s['props'].sort(key=lambda p:p['sourceWorld'][1])
for p in [folder/'layout.json',src/'layout.json']:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
print('Observed basins/facade/pool details refined, clean inn checker separated')

# Complete source bench family and ground contact. The pool fragment had an occluded support.
import shutil,hashlib
shutil.copyfile(folder/'bench_front_up.png',folder/'bench_pool.png')
specs={a['id']:a for a in d['assets']}
for name in ['bench_front_down','bench_front_up','bench_back_down','bench_back_up','bench_pool']:
 a=np.array(Image.open(folder/(name+'.png')).convert('RGBA'));rgb=a[:,:,:3].astype(float);a[(rgb[:,:,1]>rgb[:,:,0]*1.07)&(rgb[:,:,1]>rgb[:,:,2]*1.1),3]=0
 yy,xx=np.where(a[:,:,3]>0);sole=int(yy.max())+1;out=np.zeros((sole+4,a.shape[1],4),np.uint8);out[:sole]=a[:sole];Image.fromarray(out).save(folder/(name+'.png'));sp=specs[name];sp['size']=[a.shape[1],sole+4];sp['anchor']=[30 if name=='bench_pool' else sp['anchor'][0],sole];sp['anchorContract']='FRONTMOST_SOURCE_SUPPORT_CONTACT_PLUS_4PX_TRANSPARENT_GUTTER';sp['sha256']=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest()
 if name=='bench_pool':sp['sourceParent']='bench_front_up';sp['status']='SOURCE_CLEAR_SAME_FACING_REUSE'
for scene in d['scenes'].values():
 for prop in scene['props']:
  if prop['asset'].startswith('bench_'):sp=specs[prop['asset']];prop['anchor']=sp['anchor'];prop['nativeSize']=sp['size']
for p in [folder/'layout.json',src/'layout.json']:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')

# The observed inn counter is partial: never turn the on-video advertisement into a room prop.
a=np.array(Image.open(folder/'inn_counter_segment.png').convert('RGBA'));a[:16,:,3]=0;Image.fromarray(a).save(folder/'inn_counter_segment.png')
