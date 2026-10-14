from pathlib import Path
import json,numpy as np
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[1];assets=root/'app/src/main/assets/abel-video-v147';data=json.loads((assets/'layout.json').read_text());out=root/'build/reports/abel-v147-source-projection';out.mkdir(parents=True,exist_ok=True)
images={a['id']:Image.open(assets/(a['id']+'.png')).convert('RGBA') for a in data['assets']};names=['tile_white','tile_brick_rosette','tile_blue_water','tile_rock_paving','tile_sea_water','tile_grass']
for n in [2,5,14,19,21,28,37,43,46,54]:
 r=data['views'][n-1];s=data['scenes'][r['map']];cam=np.array(r['camera']);grid=s['gridRows'];im=Image.new('RGBA',(640,408),'#c6c7ce')
 for u in range(80):
  for v in range(80):
   x,y=np.array([(u-v)*28,(u+v)*14])-cam
   if -35<x<675 and -25<y<435:im.alpha_composite(images[names[int(grid[u][v])]],(round(x)-28,round(y)-14))
 for u in range(1,79):
  for v in range(1,79):
   k=int(grid[u][v]);x,y=np.array([(u-v)*28,(u+v)*14])-cam
   if not(-35<x<675 and -25<y<435 and k in [2,4,5]):continue
   a,b=('garden_curb_down_edge','garden_curb_up_edge') if k==5 else ('coping_up_edge','coping_down_edge')
   for du,dv,name,ax,ay in [(-1,0,b,x-28,y),(0,-1,a,x,y-14),(1,0,b,x,y+14),(0,1,a,x-28,y)]:
    if int(grid[u+du][v+dv])!=k:
     spec=next(z for z in data['assets'] if z['id']==name);im.alpha_composite(images[name],(round(ax-spec['anchor'][0]),round(ay-spec['anchor'][1])))
 for p in s['props']:
  t=images[p['asset']];x,y=np.array(p['sourceWorld'])-np.array(p['anchor'])-cam
  if x<640 and x+t.width>0 and y<408 and y+t.height>0:im.alpha_composite(t,(round(x),round(y)))
 im.convert('RGB').save(out/f'{n:03d}.png')
print('10 projection views, not Android native captures')
