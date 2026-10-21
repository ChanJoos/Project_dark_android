"""Offline original MPF/HPF decoder; no screenshot-as-map or substitute monster art."""
from pathlib import Path
import hashlib,json,zipfile,numpy as np
from PIL import Image
from horror_original_formats import mpf,palette,render_frame,decompress,table
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'master/source/death_v217'
OUT=ROOT/'app/build/generated/death-v217/death-v217'
OUT.mkdir(parents=True,exist_ok=True)
# Generated output only: remove stale sheets if the admitted roster shrinks.
for stale in OUT.glob("*.png"):stale.unlink()
ref=json.loads((SRC/'reference.json').read_text())
class Entries:
 def __init__(self,values):self.entries=values
 def __getitem__(self,k):return self.entries[k]
with zipfile.ZipFile(SRC/'original_sources.zip') as z:
 entries={n:z.read(n) for n in z.namelist()}
 for n,digest in ref['files'].items():assert hashlib.sha256(entries[n]).hexdigest()==digest,n
 ia=Entries({n[3:]:b for n,b in entries.items() if n.startswith('ia/')})
 seo=Entries({n[4:]:b for n,b in entries.items() if n.startswith('seo/')})
 ft=table(ia,'stc');bt=table(seo,'mpt')
 for n,b in ia.entries.items():
  if not n.endswith('.hpf'):continue
  ident=int(n[3:8]);data=decompress(b)[8:];pal=palette(ia[f'stc{ft.get(ident+1,0)%1000:04}.pal'])
  Image.fromarray(pal[np.frombuffer(data,np.uint8).reshape(-1,28)]).save(OUT/(n[:-4]+'.png'))
 for n,b in seo.entries.items():
  if not n.startswith('bg'):continue
  ident=int(n[2:-4]);pal=palette(seo[f'mpt{bt.get(ident+1,0)%1000:04}.pal'])
  rgba=pal[np.frombuffer(b,np.uint8).reshape(27,56)].copy();rgba[:,:,3]=255
  for y in range(27):
   margin=abs(13-y)*2;rgba[y,:margin,3]=0
   if margin:rgba[y,56-margin:,3]=0
  Image.fromarray(rgba).save(OUT/(n[:-4]+'.png'))
 monsters=[]
 for typ,ident in enumerate(ref['sourceMPFs']):
  raw=entries[f'hades/mns{ident:03}.mpf'];m=mpf(raw);pal=palette(entries[f"hades/mns{m['palette']:03}.pal"])
  wc,ac=m['walkcount'],m['attackcount'];sc=max(1,m['standcount']);rows=sc+wc+ac
  # Source frame centers remain the common foot; canvas bounds are measured, not cropped.
  idle_frames=[m['frames'][m['stand']+i] for i in range(2*m['standcount'])] if m['standcount'] else [m['frames'][m['walk']+view*wc] for view in range(2)]
  all_frames=idle_frames+[m['frames'][m['walk']+i] for i in range(2*wc)]+[m['frames'][m['attack']+i] for i in range(min(2*ac,len(m['frames'])-m['attack']))]
  extent=max(max(abs(f['left']-f['cx']),abs(f['right']-f['cx'])) for f in all_frames)
  above=max(f['cy']-f['top'] for f in all_frames);below=max(f['bottom']-f['cy'] for f in all_frames)
  width=max(96,((extent*2+15)//16)*16);height=max(96,((above+below+15)//16)*16)
  ax=width//2;ay=above;sheet=Image.new('RGBA',(width*2,height*rows))
  for row in range(rows):
   for col in range(2):
    view=col
    if row<sc:index=m['stand']+view*sc+row if m['standcount'] else m['walk']+view*wc
    elif row<sc+wc:index=m['walk']+view*wc+row-sc
    else:index=m['attack']+view*ac+row-sc-wc
    index=min(index,len(m['frames'])-1);f=m['frames'][index];im=render_frame(m,pal,index);left=ax+f['left']-f['cx'];top=ay+f['top']-f['cy']
    assert 0<=left and 0<=top and left+im.width<=width and top+im.height<=height,(ident,row,col)
    sheet.alpha_composite(im,(col*width+left,row*height+top))
  sheet.save(OUT/f'monster_{typ}.png')
  monsters.append(dict(type=typ,mpf=ident,width=width,height=height,anchorX=ax,anchorY=ay,idle=sc,walk=wc,attack=ac,frontAttackFrames=min(ac,len(m['frames'])-m['attack']-ac),rows=rows,bytes=sheet.width*sheet.height*4,sourceSha256=hashlib.sha256(raw).hexdigest()))
 manifest=dict(monsters=monsters,atlasBytes=sum(m['bytes'] for m in monsters),files={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.glob('*.png'))})
 (OUT/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print('DEATH_ORIGINAL',len(entries),len(monsters))
# Explicit mobile stairs use original adjacent stone floor pixels; no unseen original stair claim.
from PIL import ImageDraw
base=Image.open(OUT/'bg5253.png').convert('RGBA');stair=Image.new('RGBA',(64,64));draw=ImageDraw.Draw(stair)
for step in range(6):
 x=4+step*4;y=46-step*7;top=[(x,y),(x+28,y+14),(x+36,y+10),(x+8,y-4)]
 draw.polygon([(x,y),(x+28,y+14),(x+28,y+18),(x,y+4)],fill=(55,47,33,255))
 mask=Image.new('L',(64,64));ImageDraw.Draw(mask).polygon(top,fill=255)
 texture=Image.new('RGBA',(64,64));texture.alpha_composite(base,(x-2,y-13))
 texture.putalpha(Image.composite(texture.getchannel('A'),Image.new('L',(64,64)),mask));stair.alpha_composite(texture)
 draw=ImageDraw.Draw(stair);draw.line([(x,y),(x+28,y+14)],fill=(133,118,82,255),width=1)
stair.save(OUT/'stairs.png')

manifest['files']={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.glob('*.png'))}
(OUT/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
