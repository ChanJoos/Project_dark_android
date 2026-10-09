"""Reproduce capture-derived Pote assets from the three user-retained videos; never publish raw videos.
Usage: python tools/extract_pote_v113_reference.py FOREST_VIDEO HUNT_VIDEO WATER_VIDEO
Requires ffmpeg and Pillow. Crop/alpha recipe and accepted output hashes live in provenance.json.
"""
import sys,subprocess,tempfile,json,hashlib
from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter
root=Path(__file__).resolve().parents[1];dest=root/'assets/pote/production/reference_v113';manifest=json.loads((dest/'provenance.json').read_text());videos=[Path(x).resolve() for x in sys.argv[1:]]
assert len(videos)==3,'need the forest, hunt and earlier Milles water video'
expected=list(manifest['sourceHashes'].values());assert [hashlib.sha256(v.read_bytes()).hexdigest() for v in videos]==expected,'input bytes differ from the recorded source videos'
frames={}
with tempfile.TemporaryDirectory() as tmp:
 for vi,t in [(0,9),(0,15),(0,27),(1,25),(2,20)]:
  p=Path(tmp)/f'{vi}_{t}.png';subprocess.run(['ffmpeg','-v','error','-ss',str(t),'-i',str(videos[vi]),'-frames:v','1',str(p)],check=True);frames[vi,t]=Image.open(p).resize((2032,938),Image.Resampling.NEAREST).convert('RGBA')
 for r in manifest['records']:
  name=r['file']
  if name=='forest_floor.png':continue
  frame=frames[(1,25) if name=='tree_oak.png' else (0,9) if name in ('tree_willow.png','trail_earth.png','fern.png') else (0,15) if name=='flowers.png' else (2,20)]
  im=frame.crop(r['box'])
  if name.startswith('tree_'):
   m=Image.new('L',im.size);ImageDraw.Draw(m).polygon(r['exterior'],fill=255);bark=Image.new('L',im.size);bd=ImageDraw.Draw(bark)
   for poly in r['bark']:bd.polygon(poly,fill=255)
   px=im.load();mp=m.load();bp=bark.load()
   for y in range(im.height):
    for x in range(im.width):
     red,g,b,a=px[x,y];keep=mp[x,y]>0 and (g>=red*.87 and g>b*1.12 or (12<red<55 and 14<g<55 and 8<b<45) or bp[x,y]>0);px[x,y]=(red,g,b,255 if keep else 0)
   im=im.resize((im.width//2,im.height//2),Image.Resampling.NEAREST)
  elif name in ('fern.png','flowers.png'):
   px=im.load()
   for y in range(im.height):
    for x in range(im.width):
     red,g,b,a=px[x,y];keep=(g>red*.91 and g>b*1.13) or (name=='flowers.png' and b>120 and red>140);px[x,y]=(red,g,b,255 if keep else 0)
   im=im.resize((im.width//2,im.height//2),Image.Resampling.NEAREST)
  elif name=='shore_rock.png':
   mask=Image.new('L',im.size);ImageDraw.Draw(mask).polygon([(9,9),(47,0),(83,10),(95,28),(78,45),(42,51),(4,39),(0,20)],fill=255);im.putalpha(mask);im=im.resize((48,27),Image.Resampling.NEAREST);px=im.load()
   for y in range(im.height):
    for x in range(im.width):
     red,g,b,a=px[x,y]
     if b>red*1.2 and g>red*1.1:px[x,y]=(red,g,b,0)
  else:
   sizes={'trail_earth.png':(64,32),'water_current.png':(114,31),'shore_material.png':(28,16)};im=im.convert('RGB').resize(sizes[name],Image.Resampling.NEAREST)
  im.save(dest/name)
 r=next(r for r in manifest['records'] if r['file']=='forest_floor.png');src=frames[0,27].convert('RGB');patches=[src.crop(b).resize((96,80),Image.Resampling.NEAREST) for b in r['boxes']];out=Image.new('RGB',(384,320));out.paste(patches[0].resize(out.size,Image.Resampling.NEAREST));mask=Image.new('L',(96,80));ImageDraw.Draw(mask).rectangle((12,12,83,67),fill=255);mask=mask.filter(ImageFilter.GaussianBlur(7))
 for j in range(-1,6):
  for i in range(-1,7):out.paste(patches[(i*7+j*3)%4],(i*64,j*56),mask)
 px=out.load();w,h=out.size
 for x in range(20):
  f=(20-x)/40
  for y in range(h):
   a,b=px[x,y],px[w-1-x,y];px[x,y]=tuple(round(a[k]*(1-f)+b[k]*f) for k in range(3));px[w-1-x,y]=tuple(round(b[k]*(1-f)+a[k]*f) for k in range(3))
 for y in range(20):
  f=(20-y)/40
  for x in range(w):
   a,b=px[x,y],px[x,h-1-y];px[x,y]=tuple(round(a[k]*(1-f)+b[k]*f) for k in range(3));px[x,h-1-y]=tuple(round(b[k]*(1-f)+a[k]*f) for k in range(3))
 out.save(dest/'forest_floor.png')
 for r in manifest['records']:assert hashlib.sha256((dest/r['file']).read_bytes()).hexdigest()==r['sha256'],r['file']
print('ALL9 CAPTURE-DERIVED OUTPUT HASHES MATCH')
