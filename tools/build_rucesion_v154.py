"""Independent video objects with an expanded, explicitly ADAPTED inland district."""
from pathlib import Path
import json,numpy as np
r=Path(__file__).resolve().parents[1];p=r/'app/src/main/assets/rucesion-v154';d=json.loads((p/'assets.json').read_text());spec={a['id']:a for a in d['assets']};N=104;g=np.full((N,N),5,np.uint8);active=np.zeros((N,N),np.uint8);g[8:52,8:51]=0;active[8:52,8:51]=1;g[8:13,8:46]=4;g[13:51,20:23]=1;g[13:51,35:38]=1;g[24:27,8:50]=1;g[40:43,8:50]=1;g[28:40,27:29]=2;g[31:34,27:29]=1;g[50:96,8:79]=0;active[50:96,8:79]=1;g[50:96,8:10]=5;g[50:96,77:79]=5;g[94:96,8:79]=5
for v in [24,43,63]:g[48:94,v:v+4]=1;active[48:94,v:v+4]=1
for u in [57,76,89]:g[u:u+4,10:77]=1
g[62:94,35:37]=2;g[75:80,35:37]=1;g[88:93,35:37]=1
for u,v,w,h in [(63,49,10,10),(80,12,10,8),(80,68,10,7)]:g[u:u+w,v:v+h]=5
props=[]
def prop(a,sx,sy,evidence='ADAPTED_INLAND_PLACEMENT'):
 s=spec[a];props.append(dict(asset=a,sourceWorld=[sx,sy],anchor=s['anchor'],nativeSize=s['size'],evidence=evidence))
def at(a,u,v):prop(a,(u-v)*28,(u+v)*14)
for a,x,y in [('building_blue_hall',-19,1071),('building_yellow_shop',-587,740),('building_blue_house',586,777),('building_yellow_shop',84,714),('pergola_garden',126,1187),('rowing_boat',-344,383),('rowing_boat',-464,457)]:prop(a,x,y,'SOURCE_REGISTERED_GROUND')
for u in [64,69,83,88]:
 for v in [15,29,47,60,70]:
  if (u,v) not in [(69,47),(83,15),(88,70),(64,15),(69,15)]:at('building_yellow_shop' if (u,v)==(88,60) else 'building_blue_house',u,v)
at('building_blue_hall',70,20)
for uv in [(63,49),(72,49),(63,58),(72,58),(80,12),(89,12),(80,19),(89,19),(80,68),(89,68),(80,74),(89,74),(53,12),(53,70),(93,19),(93,53),(18,15),(19,30),(26,42),(34,16),(45,44),(33,44),(44,16),(17,42),(27,13)]:at('tree_broadleaf',*uv)
for v in [25,44,64]:
 for u in range(53,94,5):at('lamp_main',u,v)
for u in [57,77,90]:
 for v in range(14,76,8):at('lamp_main',u,v)
for u in [15,22,29,36,43,49]:
 for v in [20,36,47]:at('lamp_main',u,v)
for uv in [(63,51),(72,56),(81,20),(88,20),(81,67),(88,67),(18,26),(29,42),(43,45)]:at('bench_front_up',*uv)
for uv in [(66,52),(84,15),(85,71),(20,45),(35,43)]:at('flowerbed_orange',*uv)
for v in range(10,77,2):at('rail_up_clean',96,v)
for u in range(52,95,2):at('rail_down_clean',u,79)
for v in range(9,46,2):at('rail_up_clean',13,v)
D=dict(assets=d['assets'],gridRows=[''.join(map(str,x)) for x in g],activeRows=[''.join(map(str,x)) for x in active],props=props,sourceYOffset=168,runtimeOrigin=[3328,64],tile=[64,32],scope='Video-registered facade grounds; hidden inland topology ADAPTED');(p/'layout.json').write_text(json.dumps(D,indent=2)+'\n')
def f(x):return str(round(float(x),3))+'f'
lines=['package com.projectdark.mobile.world;','import java.util.*;','final class RucesionLayout {','static final String[] TILES={'+','.join(json.dumps(x) for x in D['gridRows'])+'};','static final String[] ACTIVE={'+','.join(json.dumps(x) for x in D['activeRows'])+'};','static List<RucesionTownGeometry.Prop> props(){return Collections.unmodifiableList(Arrays.asList('];lines.append(',\n'.join('new RucesionTownGeometry.Prop('+json.dumps(a['asset'])+','+','.join(f(x) for x in a['sourceWorld']+a['anchor']+a['nativeSize'])+')' for a in props));lines+=['));}','static float[] vertices(String name){switch(name){']
for a in d['assets']:
 if 'groundPolygon' in a:lines.append('case '+json.dumps(a['id'])+':return new float[]{'+','.join(f(x) for pt in a['groundPolygon'] for x in pt)+'};')
lines+=['default:return null;}}','}'];(r/'app/src/main/java/com/projectdark/mobile/world/RucesionLayout.java').write_text('\n'.join(lines)+'\n');print(len(props),'props')
