"""Decode actual Horror MPFs. Never composite substitute player wardrobes.
Original indexed pixels, two source views and client-mirrored opposite directions.
"""
from pathlib import Path
import json,zipfile,hashlib,sys
from PIL import Image
from horror_original_formats import mpf,palette,render_frame
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/build/generated/horror-v180-pack/horror-v180';OUT.mkdir(parents=True,exist_ok=True)
SRC=ROOT/'master/source/horror_v180'
# Master type order: male/female Warrior, Rogue, Mage, Cleric, Monk.
IDS=[363,368,365,370,364,369,367,372,366,371]
receipt=[]
with zipfile.ZipFile(SRC/'original_monsters.zip') as z:
 for typ,ident in enumerate(IDS):
  raw=z.read(f'mns{ident}.mpf');m=mpf(raw);pal=palette(z.read(f"mns{m['palette']:03}.pal"));sheet=Image.new('RGBA',(640,1280))
  assert m['walkcount']==5 and m['attackcount'] in [2,3,4]
  for row in range(10):
   for col in range(4):
    view=0 if col<2 else 1
    index=m['walk']+view*5+(0 if row==0 else min(row-1,4)) if row<6 else m['attack']+view*m['attackcount']+min(row-6,m['attackcount']-1)
    f=m['frames'][index];im=render_frame(m,pal,index)
    left=80+f['left']-f['cx'];top=88+f['top']-f['cy']
    if col in [0,2]:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT);left=80-(f['right']-f['cx'])
    assert left>=0 and top>=0 and left+im.width<=160 and top+im.height<=128,(typ,row,col)
    sheet.alpha_composite(im,(col*160+left,row*128+top))
  sheet.save(OUT/f'monster_{typ}.png')
  receipt.append(dict(type=typ,mpf=ident,sha256=hashlib.sha256(raw).hexdigest(),sourceFrames=len(m['frames']),walkFrames=m['walkcount'],attackFrames=m['attackcount'],anchor=[80,88],directions=['NW','NE','SW','SE']))
manifest=dict(revision='V180_ORIGINAL_MPF',source='Official DarkAges 7.41 hades.dat; visually matched to Korean user recording',installer_sha256='dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093',installer_url='https://s3.amazonaws.com/kru-downloads/da/DarkAges741single.exe',monsters=receipt,files={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.glob('monster_*.png'))})
(OUT/'monsters.json').write_text(json.dumps(manifest,indent=2)+'\n')
print('ORIGINAL_HORROR_MPF',len(receipt),'all native frames, no wardrobe substitutions')
# Map tiles are decoded from retained raw HPF/palette/indexed-floor bytes.
import numpy as np,re,base64
from horror_original_formats import decompress,table
class Entries:
 def __init__(self,entries):self.entries=entries
 def __getitem__(self,key):return self.entries[key]
source=json.loads((SRC/'map_sources.json').read_text())
with zipfile.ZipFile(SRC/'original_maps.zip') as z:
 entries={n:z.read(n) for n in z.namelist()}
 for n,digest in source['files'].items():assert hashlib.sha256(entries[n]).hexdigest()==digest,n
 ia=Entries({n[3:]:b for n,b in entries.items() if n.startswith('ia/')});seo=Entries({n[4:]:b for n,b in entries.items() if n.startswith('seo/')});ft=table(ia,'stc');bt=table(seo,'mpt')
 for n,b in ia.entries.items():
  if not n.endswith('.hpf'):continue
  ident=int(n[3:8]);raw=decompress(b)[8:];pal=palette(ia[f'stc{ft.get(ident+1,0)%1000:04}.pal']);Image.fromarray(pal[np.frombuffer(raw,np.uint8).reshape(-1,28)]).save(OUT/(n[:8]+'.png'))
 for n,b in seo.entries.items():
  if not n.startswith('bg'):continue
  ident=int(n[2:-4]);idx=max(0,ident-1);pal=palette(seo[f'mpt{bt.get(idx+2,0)%1000:04}.pal']);rgba=pal[np.frombuffer(b,np.uint8).reshape(27,56)].copy();rgba[:,:,3]=255
  for y in range(27):
   margin=abs(13-y)*2;rgba[y,:margin,3]=0
   if margin:rgba[y,56-margin:,3]=0
  Image.fromarray(rgba).save(OUT/(n[:-4]+'.png'))
 # Cross-check compiled grid and navigation against raw .map cells, not the generator's pictures.
 compiled=(ROOT/'app/src/main/java/com/projectdark/mobile/world/HorrorCastleOriginalData.java').read_text()
 specs=re.findall(r'MAPS.put\("([^"]+)",new Grid\((\d+),(\d+),(\d+),"([^"]+)","([^"]+)"\)\)',compiled)
 assert len(specs)==14
 for name,i,w,h,c,bits in specs:
  raw=entries[f'maps/lod{i}.map'];assert base64.b64decode(c)==raw;assert len(raw)==int(w)*int(h)*6
  arr=np.frombuffer(raw,'<u2').reshape(int(h),int(w),3);nav=base64.b64decode(bits);count=0
  for v in range(int(h)):
   for u in range(int(w)):
    k=v*int(w)+u
    if nav[k//8]&(1<<(k%8)):
     count+=1;assert arr[v,u,0]>0
     for f in map(int,arr[v,u,1:]):assert not (f>0 and f<=len(ia['sotp.dat']) and ia['sotp.dat'][f-1]&15)
  assert count==source['maps'][name]['walkable']
(OUT/'maps.json').write_text(json.dumps(source,indent=2)+'\n')
print('ORIGINAL_HORROR_MAPS',len(source['maps']),'SOURCE_PENDING',source['unavailable'])
