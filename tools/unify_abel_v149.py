"""One continuous source-visible Abel town with explicitly adapted joins and bounded outskirts.
Preserve original main/east observations; only the unified scene is a runtime composition.
"""
from pathlib import Path
import json,copy,numpy as np
from PIL import Image,ImageDraw,ImageFilter
root=Path(__file__).resolve().parents[1];folder=root/'app/src/main/assets/abel-video-v147';d=json.loads((folder/'layout.json').read_text());N=112;DU,DV=28,4;dx,dy=(DU-DV)*28,(DU+DV)*14
masks={}
for key in ['main','east']:
 mask=np.zeros((N,N),np.uint8);offset=(0,0) if key=='main' else (DU,DV)
 for view in d['views']:
  if view['map']!=key:continue
  cx,cy=view['camera']
  for u in range(80):
   for v in range(80):
    sx=(u-v)*28-cx;sy=(u+v)*14-cy
    if 18<sx<620 and 5<sy<402:mask[u+offset[0],v+offset[1]]=1
 masks[key]=mask
points=sorted(map(tuple,np.argwhere(masks['main']|masks['east'])))
def cross(o,a,b):return (a[0]-o[0])*(b[1]-o[1])-(a[1]-o[1])*(b[0]-o[0])
low=[];high=[]
for a in points:
 while len(low)>=2 and cross(low[-2],low[-1],a)<=0:low.pop()
 low.append(a)
for a in reversed(points):
 while len(high)>=2 and cross(high[-2],high[-1],a)<=0:high.pop()
 high.append(a)
hull=low[:-1]+high[:-1];im=Image.new('L',(N,N));ImageDraw.Draw(im).polygon([(int(v),int(u)) for u,v in hull],fill=1);active=np.array(im.filter(ImageFilter.MaxFilter(5)));active[:2]=0;active[:,:2]=0;active[-2:]=0;active[:,-2:]=0
grid=np.full((N,N),5,dtype=np.uint8);grid[active>0]=0
for key in ['main','east']:
 off=(0,0) if key=='main' else (DU,DV)
 for u in range(80):
  for v in range(80):
   if masks[key][u+off[0],v+off[1]]:grid[u+off[0],v+off[1]]=int(d['scenes'][key]['gridRows'][u][v])
props=copy.deepcopy(d['scenes']['main']['props'])
for p in d['scenes']['east']['props']:
 a=copy.deepcopy(p);a['sourceWorld']=[a['sourceWorld'][0]+dx,a['sourceWorld'][1]+dy];a['sourceDistrict']='east';props.append(a)
# Finish the unobserved connector as a landscaped public street, using existing source families.
# Do not alter recorded water, facades or gardens. Tile corridors remain in the same 64x32 lattice.
for u in range(27,79):
 v=25+(u-27)//4
 for a in range(u-1,u+2):
  for b in range(v-1,v+2):
   if active[a,b] and grid[a,b]==0:grid[a,b]=1
# Garden pockets on bare gaps beside the spine; exclude observed material and facade footprints.
reserved=[]
for p in props:
 sx,sy=p['sourceWorld'];reserved.append(((sx/28+sy/14)/2,(sy/14-sx/28)/2))
template={a['asset']:a for a in props}
for u,v in [(u,v) for u in range(18,92,4) for v in range(18,78,4)]:
 if sum(p.get('evidence')=='ADAPTED_CONNECTOR_LANDSCAPING_REUSING_OBSERVED_ASSET' for p in props)>=32:break
 if masks['main'][u-2:u+3,v-2:v+3].sum()+masks['east'][u-2:u+3,v-2:v+3].sum()>8:continue
 if any((u-a)**2+(v-b)**2<28 for a,b in reserved):continue
 if not all(active[a,b] and grid[a,b]==0 for a in range(u-2,u+3) for b in range(v-2,v+3)):continue
 grid[u-2:u+3,v-2:v+3]=5
 reserved.append((u,v))
 for asset,du,dv in [('garden_tree_pair',0,0),('flowerbed_orange',1,1)]:
  a=copy.deepcopy(template[asset]);a['sourceWorld']=[(u+du-v-dv)*28,(u+du+v+dv)*14];a['evidence']='ADAPTED_CONNECTOR_LANDSCAPING_REUSING_OBSERVED_ASSET';props.append(a)
scene=dict(props=sorted(props,key=lambda p:p['sourceWorld'][1]),gridRows=[''.join(map(str,r)) for r in grid],activeRows=[''.join(map(str,r)) for r in active],eastOffsetTiles=[DU,DV],scope='SINGLE_RUNTIME_TOWN; observed districts retained, translation/landscaped joins/bounded grassy outskirts ADAPTED; original entire map extent unknown')
d['scenes']['unified']=scene
for p in [folder/'layout.json',root/'master/source/abel_video_v147/layout.json']:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
p=root/'app/src/main/java/com/projectdark/mobile/world/AbelUnifiedLayout.java';lines=['package com.projectdark.mobile.world;','import java.util.*;','/** V149 single town composition; original observations remain in AbelVideoLayout. */','public final class AbelUnifiedLayout {','public static final String[] TILES={'+','.join(json.dumps(r) for r in scene['gridRows'])+'};','public static final String[] ACTIVE={'+','.join(json.dumps(r) for r in scene['activeRows'])+'};','public static List<AbelTownGeometry.Prop> props(){List<AbelTownGeometry.Prop> p=new ArrayList<>();']
for a in scene['props']:lines.append('p.add(new AbelTownGeometry.Prop('+json.dumps(a['asset'])+','+','.join(f'{v:.3f}f' for v in a['sourceWorld']+a['anchor']+a['nativeSize'])+'));')
lines+=['return Collections.unmodifiableList(p);}','private AbelUnifiedLayout(){} }'];p.write_text('\n'.join(lines)+'\n');print('single scene',len(props),'props',int(active.sum()),'active cells; east tile offset',DU,DV)
