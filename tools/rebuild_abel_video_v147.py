"""Rebuild source-visible Abel tiles and independent alpha-masked sprites.
Inputs are local user footage. Does not download or publish media. Partial silhouettes
remain partial. Ground outside observed camera footprints is an adapted extension.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,cv2,json,hashlib,argparse
parser=argparse.ArgumentParser();parser.add_argument('--video',type=Path,required=True);args=parser.parse_args()
root=Path(__file__).resolve().parents[1];out=root/'app/src/main/assets/abel-video-v147';src=root/'master/source/abel_video_v147';frames=src/'reference_frames'
out.mkdir(parents=True,exist_ok=True);frames.mkdir(parents=True,exist_ok=True)
import subprocess
subprocess.run(['ffmpeg','-y','-v','error','-i',str(args.video),'-vf','fps=1,crop=1920:1080:210:0,scale=640:480:flags=area',str(frames/'%03d.png')],check=True)
images={i:Image.open(frames/f'{i:03d}.png').convert('RGB') for i in range(1,55)}
assets=[]
def asset(name,n,box,mode='polygon',poly=None,anchor=None,partial=False):
 a=np.array(images[n].crop(box).convert('RGBA'));h,w=a.shape[:2];m=Image.new('L',(w,h));poly=poly or [(0,0),(w-1,0),(w-1,h-1),(0,h-1)];ImageDraw.Draw(m).polygon(poly,fill=255);mask=np.array(m);rgb=a[:,:,:3].astype(float);hsv=cv2.cvtColor(a[:,:,:3],cv2.COLOR_RGB2HSV)
 if mode=='tree':mask=np.uint8(((hsv[:,:,0]>32)&(hsv[:,:,0]<75)&(hsv[:,:,1]>55)|((rgb[:,:,0]>rgb[:,:,2]*1.4)&(rgb[:,:,1]<rgb[:,:,0]*.92)&(rgb[:,:,0]<180)))&(mask>0))*255
 if mode=='wood':mask=np.uint8(((rgb[:,:,0]>rgb[:,:,1]*1.18)&(rgb[:,:,0]>rgb[:,:,2]*1.4)|(rgb.mean(2)<95))&(mask>0))*255
 if mode=='ship':mask[((hsv[:,:,1]<50)&(rgb.mean(2)>115))|((hsv[:,:,0]>93)&(hsv[:,:,0]<135)&(hsv[:,:,1]>40))|((hsv[:,:,0]>36)&(hsv[:,:,0]<90)&(hsv[:,:,1]>50))]=0
 if name.startswith('building_'):mask[(hsv[:,:,0]>38)&(hsv[:,:,0]<94)&(hsv[:,:,1]>60)]=0
 a[:,:,3]=mask;Image.fromarray(a).save(out/(name+'.png'));d=dict(id=name,sourceFrame=n,box=list(box),polygon=poly,mode=mode,anchor=anchor or [w/2,h-1],size=[w,h],status='SOURCE_VISIBLE_PARTIAL' if partial else 'SOURCE_VISIBLE',sha256=hashlib.sha256((out/(name+'.png')).read_bytes()).hexdigest());assets.append(d);return d
def tile(name,n,x,y):return asset(name,n,(x-28,y-14,x+28,y+14),poly=[(28,0),(55,14),(28,27),(0,14)],anchor=[28,14])
for name,n,x,y in [('tile_white',28,257,83),('tile_white_aged',28,285,97),('tile_brick_rosette',28,341,41),('tile_blue_water',28,117,237),('tile_rock_paving',54,341,320),('tile_sea_water',19,553,389),('tile_inn_checker',26,470,260)]:tile(name,n,x,y)
asset('tree_broadleaf',43,(22,46,86,157),'tree',[(0,17),(18,0),(57,1),(63,42),(43,111),(28,111),(21,84),(0,62)],[35,108])
asset('tree_slender',28,(396,267,455,377),'tree',anchor=[27,104])
asset('garden_tree_pair',5,(84,177,178,326),'tree',anchor=[44,147])
asset('garden_tree_mass',2,(243,39,438,199),'tree',anchor=[110,158])
asset('garden_foreground_grove',28,(18,287,274,408),'tree',anchor=[128,118],partial=True)
asset('flowerbed_orange',28,(396,23,578,132),poly=[(18,51),(82,0),(156,27),(181,67),(103,108),(0,64)],anchor=[92,98])
asset('flower_red_clump',28,(174,0,309,39),partial=True)
asset('fountain',28,(142,185,254,312),poly=[(52,0),(61,0),(65,38),(81,65),(102,78),(111,93),(109,108),(93,122),(59,126),(23,122),(1,110),(0,92),(13,76),(31,64),(39,40),(52,35)],anchor=[56,119])
asset('lamp',46,(224,207,247,327),poly=[(10,0),(13,4),(13,9),(20,14),(22,22),(19,31),(15,37),(15,104),(21,111),(22,116),(15,119),(6,119),(0,116),(0,111),(9,104),(9,37),(4,31),(1,22),(3,14),(9,9)],anchor=[13,119])
asset('bench_front_down',46,(190,76,247,134),'wood',[(4,0),(50,20),(56,32),(48,57),(0,39)],[29,54])
asset('bench_front_up',54,(127,190,189,251),'wood',[(0,28),(50,0),(61,8),(61,30),(48,55),(17,60),(3,48)],[30,57])
asset('bench_back_up',46,(173,304,235,367),'wood',[(4,34),(43,0),(58,0),(61,31),(47,50),(16,62),(0,49)],[31,60])
asset('bench_back_down',45,(534,39,594,92),'wood',[(0,0),(15,0),(56,27),(59,40),(53,52),(8,27)],[31,48])
asset('signpost',28,(546,337,585,380),'wood',[(0,10),(17,8),(18,0),(24,1),(26,9),(38,11),(38,20),(25,23),(26,42),(18,42),(17,25),(0,25)],[22,42])
asset('banner',14,(299,12,333,113),'wood',[(2,0),(30,0),(28,83),(16,102),(13,46),(0,34)],[8,102])
asset('rowing_boat',39,(298,266,518,406),'ship',[(0,60),(45,28),(76,0),(109,0),(151,17),(196,24),(217,56),(219,86),(168,119),(80,139),(30,119)],[109,134])
asset('sailing_ship',19,(17,62,407,408),'ship',[(0,173),(104,80),(119,7),(142,0),(165,122),(223,63),(238,5),(254,0),(277,116),(333,216),(369,224),(389,275),(386,319),(318,345),(176,345),(46,309),(0,265)],[195,334],True)
asset('building_two_storey',2,(410,5,620,281),poly=[(2,123),(12,93),(37,77),(48,42),(63,42),(67,25),(179,0),(197,12),(209,13),(209,224),(83,275),(2,232)],anchor=[48,235],partial=True)
asset('building_awning_corner',21,(16,0,313,214),poly=[(0,0),(228,0),(271,25),(287,31),(296,88),(289,184),(243,213),(182,184),(140,208),(0,146)],anchor=[250,194],partial=True)
asset('building_awning_long',14,(413,150,620,408),poly=[(0,146),(18,74),(37,28),(58,0),(206,53),(206,257),(54,235)],anchor=[178,242],partial=True)
for name,n,box,poly,anchor in [('coping_up',28,(331,236,389,287),[(0,0),(57,28),(57,50),(0,22)],[0,0]),('coping_down',28,(87,169,145,220),[(0,28),(57,0),(57,22),(0,50)],[0,28]),('rail_up',37,(418,159,453,212),[(0,20),(29,0),(34,5),(34,33),(5,52),(0,44)],[1,49]),('rail_down',19,(369,212,408,266),[(0,0),(38,19),(38,53),(0,34)],[0,34]),('quay_wall_down',19,(439,271,496,329),[(0,0),(56,28),(56,57),(0,29)],[0,0]),('quay_wall_up',37,(421,205,478,263),[(0,28),(56,0),(56,28),(0,57)],[0,28]),('garden_curb_down',5,(50,297,108,337),[(0,0),(57,28),(57,39),(0,11)],[0,0]),('garden_curb_up',5,(139,285,197,325),[(0,28),(56,0),(56,11),(0,39)],[0,28])]:asset(name,n,box,poly=poly,anchor=anchor)
asset('garden_grass',21,(91,243,127,260),anchor=[0,0])
asset('inn_table_bottles',26,(331,189,442,276),'wood',[(16,0),(93,29),(110,61),(90,80),(79,77),(69,86),(21,65),(6,64),(0,49),(14,29)],[58,80])
asset('inn_table_mugs',26,(439,145,553,224),'wood',[(18,0),(43,0),(95,24),(113,59),(93,76),(21,78),(0,58),(7,31)],[56,73])
asset('inn_table_back',26,(174,94,295,157),'wood',[(11,0),(79,0),(119,31),(107,55),(81,62),(10,47),(0,29)],[61,58])
asset('inn_stool',26,(356,115,380,150),'wood',anchor=[12,32]);asset('inn_stool_alt',26,(411,119,438,152),'wood',anchor=[13,31])
asset('inn_barrel',26,(498,0,541,30),'wood',partial=True)
asset('inn_counter_segment',26,(337,79,456,117),'wood',[(0,0),(118,0),(118,21),(62,37),(0,10)],[60,35],True)
asset('inn_plant_pot',26,(534,11,556,40),'tree',anchor=[11,27])
# A repeated visible grass material is an adaptation, rather than a falsely recovered original tile.
g=np.array(Image.open(out/'garden_grass.png'));yy,xx=np.indices((28,56));a=g[yy%g.shape[0],xx%g.shape[1]].copy();m=Image.new('L',(56,28));ImageDraw.Draw(m).polygon([(28,0),(55,14),(28,27),(0,14)],fill=255);a[:,:,3]=np.array(m);Image.fromarray(a).save(out/'tile_grass.png');assets.append(dict(id='tile_grass',size=[56,28],anchor=[28,14],sourceParent='garden_grass',status='ADAPTED_VISIBLE_MATERIAL'))
for name,box,anchor in [('coping_up',(0,0,29,37),[0,0]),('coping_down',(0,14,29,51),[0,14]),('garden_curb_down',(0,0,29,26),[0,0]),('garden_curb_up',(0,14,29,40),[0,14])]:
 a=Image.open(out/(name+'.png')).crop(box);a.save(out/(name+'_edge.png'));assets.append(dict(id=name+'_edge',sourceParent=name,size=list(a.size),anchor=anchor,status='SOURCE_EDGE_MODULE'))
# Register static world offsets from feature matches, excluding the screen-fixed HUD/player centre.
sift=cv2.SIFT_create(nfeatures=2500);features={}
for n,im in images.items():
 gray=cv2.cvtColor(np.array(im),cv2.COLOR_RGB2GRAY);m=np.zeros_like(gray);m[12:398,24:608]=255;m[170:260,275:345]=0;features[n]=sift.detectAndCompute(gray,m)
segments=[(range(1,21),2,(3.5,214.75),'main'),(range(21,26),21,(1087,813),'main'),(range(26,27),26,(0,0),'inn'),(range(27,36),27,(-1349,855),'east'),(range(36,42),36,(495.5,859.25),'east'),(range(42,55),42,(-935,508),'east')]
views=[];matcher=cv2.BFMatcher()
for ns,ref,origin,scene in segments:
 offsets={ref:np.zeros(2)}
 def offset(a,b):
  ka,da=features[a];kb,db=features[b];matches=matcher.knnMatch(da,db,k=2);good=[m for m,n in matches if m.distance<n.distance*.7];assert len(good)>4,(a,b)
  pa=np.float32([ka[m.queryIdx].pt for m in good]);pb=np.float32([kb[m.trainIdx].pt for m in good]);delta=pa-pb
  # Translation only: consensus rejects repetitive-tile aliases and screen overlays.
  best=max(delta,key=lambda d:np.sum(np.linalg.norm(delta-d,axis=1)<2.5));inliers=delta[np.linalg.norm(delta-best,axis=1)<2.5];return np.median(inliers,axis=0),len(inliers)
 for n in ns:
  if n==ref:continue
  parent=n-1 if n>ref else n+1
  d,num=offset(parent,n);offsets[n]=offsets[parent]+d
 for n in ns:views.append(dict(frame=n,map=scene,camera=(np.array(origin)+offsets[n]).round(2).tolist()))
views.sort(key=lambda x:x['frame']);scenes={k:dict(props=[],gridRows=['0'*80 for _ in range(80)]) for k in ['main','east','inn']}
# Recover multiple occurrences by masked original-RGB matching across registered source cameras.
skip=lambda n:n.startswith('tile_') or n.startswith(('coping','garden_curb','quay_wall')) or n=='garden_grass'
for spec in assets:
 name=spec['id']
 if skip(name):continue
 rgba=np.array(Image.open(out/(name+'.png')));t=rgba[:,:,:3];mask=np.uint8(rgba[:,:,3]>0)*255;h,w=mask.shape;found=[]
 for r in views:
  if (name.startswith('inn_'))!=(r['map']=='inn'):continue
  im=np.array(images[r['frame']])[:408];res=cv2.matchTemplate(im,t,cv2.TM_SQDIFF_NORMED,mask=mask);res=np.nan_to_num(res,nan=1,posinf=1,neginf=1)
  for _ in range(30):
   y,x=np.unravel_index(np.argmin(res),res.shape);err=float(res[y,x]);threshold=.035 if name.startswith('building') else .055
   if err>threshold:break
   res[max(0,y-16):y+17,max(0,x-16):x+17]=1
   if x<16 or x+w>620:continue
   p=np.array(r['camera'])+[x,y]+spec['anchor'];old=next((a for a in found if a['scene']==r['map'] and np.linalg.norm(p-a['sourceWorld'])<18),None)
   obj=dict(asset=name,scene=r['map'],sourceWorld=p.round(2).tolist(),anchor=spec['anchor'],nativeSize=spec['size'],sourceFrame=r['frame'],matchError=round(err,6),evidence=spec['status'])
   if old is None:found.append(obj)
   elif err<old['matchError']:old.update(obj)
 for p in found:scenes[p['scene']]['props'].append(p)
# Classify only observed, mostly clear diamond centres. Buildings/trees are separate objects.
for key in ['main','east']:
 votes=np.zeros((80,80,6),np.int32)
 for r in views:
  if r['map']!=key:continue
  im=np.array(images[r['frame']]);cx,cy=r['camera']
  for u in range(1,79):
   for v in range(1,79):
    x=round((u-v)*28-cx);y=round((u+v)*14-cy)
    if not(30<x<605 and 25<y<385):continue
    sample=im[y-3:y+4,x-5:x+6];rgb=np.median(sample.reshape(-1,3),axis=0);hsv=cv2.cvtColor(sample,cv2.COLOR_RGB2HSV);sat=np.median(hsv[:,:,1]);rr,gg,bb=rgb
    if bb>rr*1.3 and gg>rr*1.06:k=4 if bb>gg*1.55 and gg<90 else 2
    elif gg>rr*1.12 and gg>bb*1.15:k=5
    elif rr>bb*1.12 and rr>gg*1.08 and sat<105:k=1
    elif sat<65:k=0 if rgb.mean()>155 else 3
    else:continue
    votes[u,v,k]+=1
 grid=votes.argmax(2)
 # Remove isolated object-colour samples; only continuous observed terrain survives.
 for kind in [2,4,5]:
  count,labels,stats,_=cv2.connectedComponentsWithStats(np.uint8(grid==kind),4)
  for j in range(1,count):
   if stats[j,cv2.CC_STAT_AREA]<8:grid[labels==j]=0
 if key=='east':grid[15:24,58:62]=2
 scenes[key]['gridRows']=[''.join(map(str,row)) for row in grid]
 scenes[key]['props'].sort(key=lambda p:p['sourceWorld'][1])
receipt=dict(sourceVideo=args.video.name,libraryFileId='libfile_b12b5199c538819198ba49f39380eba0',sourceSha256=hashlib.sha256(args.video.read_bytes()).hexdigest(),sourceTile=[56,28],runtimeTile=[64,32],runtimeScale=8/7,assets=assets,views=views,scenes=scenes,scope='Source-visible only. Missing pixels, building identities and full topology remain unresolved. Ground extension and service joins are ADAPTED.')
for p in [out/'layout.json',src/'layout.json']:p.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n')
print('EXTRACTED',len(assets),'PROPS',{k:len(v['props']) for k,v in scenes.items()})
