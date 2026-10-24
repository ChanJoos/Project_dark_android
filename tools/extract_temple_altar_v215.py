from PIL import Image,ImageDraw
from pathlib import Path
import json,hashlib
out=Path('master/source/temple_v215');src=Image.open(out/'video21_native.png').convert('RGBA')
# Observed object silhouette. A small UI-occluded right roof corner is an explicit source-pixel reconstruction.
box=(410,59,538,148);mask=Image.new('L',src.size);ImageDraw.Draw(mask).polygon([(411,109),(441,92),(447,90),(447,81),(440,77),(440,73),(468,59),(530,89),(530,94),(519,98),(519,111),(534,113),(534,120),(515,131),(477,147),(461,146),(429,132),(414,121)],fill=255)
a=src.copy()
# Recover the small occluded corner as three stone surfaces using this object's visible texture.
# Source-derived adapted geometry: roof plane, dark support and base; no UI pixels retained.
regions=[([(499,75),(530,90),(499,104)],'roof'), ([(499,99),(519,95),(519,113),(499,124)],'support'), ([(499,123),(519,112),(535,113),(535,120),(499,139)],'base')]
for polygon,kind in regions:
 m=Image.new('L',src.size);ImageDraw.Draw(m).polygon(polygon,fill=255)
 for yy in range(59,148):
  for xx in range(499,538):
   if not m.getpixel((xx,yy)):continue
   if kind=='roof': sx,sy=465+(xx-499)%15,82+(yy-75)%5
   elif kind=='support':sx,sy=487+(xx-499)%8,109+(yy-99)%7
   else:sx,sy=449+(xx-499)%12,118+(yy-112)%8
   import random
   rng=random.Random(xx*977+yy*31)
   if kind=='roof':sx,sy=480+rng.randrange(16),80+rng.randrange(9)
   elif kind=='support':sx,sy=488+rng.randrange(7),110+rng.randrange(7)
   else:sx,sy=431+rng.randrange(13),112+rng.randrange(6)
   color=src.getpixel((sx,sy));gray=int(sum(color[:3])/3)
   if kind=='roof':gray=max(85,min(110,gray))
   elif kind=='support':gray=max(26,min(42,gray))
   else:gray=max(91,min(110,gray))
   a.putpixel((xx,yy),(gray,gray,max(0,gray-5),255))
# All remaining UI-covered pixels lie outside the source object's silhouette.
for yy in range(59,148):
 for xx in range(499,538):
  if mask.getpixel((xx,yy)) and a.getpixel((xx,yy))==src.getpixel((xx,yy)): mask.putpixel((xx,yy),0)
for yy in range(59,148):
 for xx in range(410,538):
  r,g,b,_=a.getpixel((xx,yy))
  if g>r*1.12 and g>b*1.18:mask.putpixel((xx,yy),0)
a.putalpha(mask);im=a.crop(box)
assets=Path('app/src/main/assets/temple-v215');assets.mkdir(exist_ok=True);im.save(assets/'altar.png')
(out/'reference.json').write_text(json.dumps({'status':'ACCEPTED_ADAPTED','video':'Screen_Recording_20261021_092403_YouTube.mp4','timeSeconds':21,'nativeCrop':[210,0,2130,1080],'nativeSize':[640,360],'sourceSha256':hashlib.sha256((out/'video21_native.png').read_bytes()).hexdigest(),'objectBox':box,'observedRoofAndBase':'original video source pixels; alpha silhouette cut','occlusion':'Minimap covers right roof/base corner. Small occluded area completed with source-sampled stone colors and textures; adapted roof/support/base corner reconstruction, ADAPTED; exact hidden geometry unresolved. No generated illustration.','rightRoofPatch':[469,61,499,86,499,76],'rightBasePatch':[470,123,490,139,514,110]},ensure_ascii=False,indent=2)+'\n')
