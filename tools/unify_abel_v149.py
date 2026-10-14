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
# V150: live objects require complete silhouettes; source archive scenes remain unmodified.
# Generated missing building/ship portions are explicitly ADAPTED. Full source tree/boat
# families replace contaminated scene rectangles, retaining the observed ground locations.
specs={a['id']:a for a in d['assets']}
replacements={'building_two_storey':'building_two_storey_complete','building_awning_corner':'building_awning_corner_complete','building_awning_long':'building_two_storey_complete','sailing_ship':'sailing_ship_complete','rowing_boat':'rowing_boat_complete','tree_slender':'tree_broadleaf','flower_red_clump':'flowerbed_orange','lamp_pool':'lamp_main','rail_up':'rail_up_clean','rail_down':'rail_down_clean','pool_rail_down':'rail_down_clean','fountain':'fountain_complete','signpost':'signpost_complete','banner':'lamp_banner_complete'}
def replace_prop(p,name):
 a=specs[name];p.update(asset=name,anchor=a['anchor'],nativeSize=a['size'],evidence=a['status'],sourceParent=p.get('sourceParent',p['asset']))
for p in props:
 if p['asset'] in replacements:
  parent=p['asset'];replace_prop(p,replacements[parent])
  if parent=='fountain':p['sourceWorld'][1]+=24
  if parent=='signpost':p['sourceWorld'][0]-=5
  if parent=='rowing_boat':
   a=specs['rowing_boat_complete'];view=next(r for r in d['views'] if r['frame']==41);box=a['box'];p['sourceWorld']=[view['camera'][0]+box[0]+a['anchor'][0]-4+dx,view['camera'][1]+box[1]+a['anchor'][1]-4+dy];p['evidence']='SOURCE_VISIBLE_COMPLETE_ROWING_BOAT_ORIGIN_FROM_FRAME41'
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
# Garden screenshots contain adjacent walls and cut trunks. Never render those fragments.
clean=[]
for p in props:
 if p['asset'] in {'garden_tree_pair','garden_tree_mass','garden_foreground_grove'}:
  offsets=[(-14,-7),(14,7)] if p['asset']=='garden_tree_pair' else [(-42,-7),(-14,-21),(14,-7),(42,7),(0,21)]
  for dx1,dy1 in offsets:
   q=copy.deepcopy(p);replace_prop(q,'tree_broadleaf');q['sourceParent']=p['asset'];q['evidence']='ADAPTED_GARDEN_PLACEMENT_OF_COMPLETE_SOURCE_TREE';q['sourceWorld']=[p['sourceWorld'][0]+dx1,p['sourceWorld'][1]+dy1];clean.append(q)
 else:clean.append(p)
props=clean
# Original camera floor votes cannot classify water hidden by a boat. Keep afloat
# geometry coherent under its hull, never turning an occluded boat into grass/land.
# Hull footprint inference is ADAPTED; the visible surrounding source water is retained.
def inside(poly,x,y):
 result=False
 for i in range(len(poly)):
  j=i-1
  if (poly[i][1]>y)!=(poly[j][1]>y) and x<(poly[j][0]-poly[i][0])*(y-poly[i][1])/(poly[j][1]-poly[i][1])+poly[i][0]:result=not result
 return result
hulls={'sailing_ship_complete':([(24,143),(72,235),(188,300),(307,329),(377,310),(397,268),(340,218),(173,105),(62,95)],4),'rowing_boat_complete':([(8,83),(36,116),(96,124),(155,105),(176,73),(174,34),(130,7),(92,10),(45,45)],2)}
for p in props:
 if p['asset'] not in hulls:continue
 poly,kind=hulls[p['asset']];sx,sy=p['sourceWorld'];ax,ay=p['anchor']
 for u in range(N):
  for v in range(N):
   if active[u,v] and any(inside(poly,(u-v)*28-sx+ax+ox,(u+v)*14-sy+ay+oy) for ox in [-28,0,28] for oy in [-28,0,28]):grid[u,v]=kind
 p['underHullFloorEvidence']='ADAPTED_WATER_FOOTPRINT_BENEATH_SOURCE_VISIBLE_FLOATING_BOAT'
# A source railing interval is 28x14; incomplete template matches skipped modules.
# Complete each observed collinear fence run between its recorded endpoints.
for asset,sign in [('rail_down_clean',1),('rail_up_clean',-1)]:
 groups=[]
 for p in [p for p in props if p['asset']==asset]:
  sx,sy=p['sourceWorld'];line=sy-sign*sx/2;group=next((g for g in groups if abs(g[0]-line)<1),None)
  if group is None:group=[line,[]];groups.append(group)
  group[1].append(p)
 for line,segments in groups:
  ordered=sorted(segments,key=lambda p:p['sourceWorld'][0])
  for first,last in zip(ordered,ordered[1:]):
   sx,sy=first['sourceWorld'];steps=round((last['sourceWorld'][0]-sx)/28)
   # Large collinear gaps may separate two piers. Never bridge an open waterway.
   if steps>3:continue
   for i in range(1,steps):
    point=[sx+i*28,sy+sign*i*14];u=int(np.floor((point[0]/28+point[1]/14)/2+.5));v=int(np.floor((point[1]/14-point[0]/28)/2+.5))
    if u<0 or v<0 or u>=N or v>=N or not active[u,v] or grid[u,v] in [2,4]:continue
    if any(abs(p['sourceWorld'][0]-point[0])<3 and abs(p['sourceWorld'][1]-point[1])<3 for p in segments):continue
    p=copy.deepcopy(first);p['sourceWorld']=point;p['evidence']='ADAPTED_CONTINUOUS_OBSERVED_RAIL_RUN_SOURCE_INTERVAL_REUSE';props.append(p)
scene=dict(props=sorted(props,key=lambda p:p['sourceWorld'][1]),gridRows=[''.join(map(str,r)) for r in grid],activeRows=[''.join(map(str,r)) for r in active],eastOffsetTiles=[DU,DV],scope='SINGLE_RUNTIME_TOWN; observed districts retained, translation/landscaped joins/bounded grassy outskirts ADAPTED; original entire map extent unknown')
d['scenes']['unified']=scene
for p in [folder/'layout.json',root/'master/source/abel_video_v147/layout.json']:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
p=root/'app/src/main/java/com/projectdark/mobile/world/AbelUnifiedLayout.java';lines=['package com.projectdark.mobile.world;','import java.util.*;','/** V150 complete objects in single town composition; original observations remain in AbelVideoLayout. */','public final class AbelUnifiedLayout {','public static final String[] TILES={'+','.join(json.dumps(r) for r in scene['gridRows'])+'};','public static final String[] ACTIVE={'+','.join(json.dumps(r) for r in scene['activeRows'])+'};','public static List<AbelTownGeometry.Prop> props(){List<AbelTownGeometry.Prop> p=new ArrayList<>();']
for a in scene['props']:lines.append('p.add(new AbelTownGeometry.Prop('+json.dumps(a['asset'])+','+','.join(f'{v:.3f}f' for v in a['sourceWorld']+a['anchor']+a['nativeSize'])+'));')
lines+=['return Collections.unmodifiableList(p);}','private AbelUnifiedLayout(){} }'];p.write_text('\n'.join(lines)+'\n');print('single scene',len(props),'props',int(active.sum()),'active cells; east tile offset',DU,DV)
