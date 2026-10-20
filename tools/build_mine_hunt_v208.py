"""Offline original MPF/HPF decoder; no screenshot-as-map or substitute monster art."""
from pathlib import Path
import hashlib,json,zipfile,numpy as np
from PIL import Image
from horror_original_formats import mpf,palette,render_frame,decompress,table
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'master/source/mine_hunt_v208'
OUT=ROOT/'app/build/generated/mine-hunt-v208/mine-hunt-v208'
OUT.mkdir(parents=True,exist_ok=True)
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
  wc,ac=m['walkcount'],m['attackcount'];rows=1+wc+ac
  # Source frame centers remain the common foot; canvas bounds are measured, not cropped.
  all_frames=[m['frames'][m['walk']+i] for i in range(2*wc)]+[m['frames'][m['attack']+i] for i in range(min(2*ac,len(m['frames'])-m['attack']))]
  extent=max(max(abs(f['left']-f['cx']),abs(f['right']-f['cx'])) for f in all_frames)
  above=max(f['cy']-f['top'] for f in all_frames);below=max(f['bottom']-f['cy'] for f in all_frames)
  width=max(96,((extent*2+15)//16)*16);height=max(96,((above+below+15)//16)*16)
  ax=width//2;ay=above;sheet=Image.new('RGBA',(width*2,height*rows))
  for row in range(rows):
   for col in range(2):
    view=col
    index=m['walk']+view*wc+(0 if row==0 else row-1) if row<=wc else m['attack']+view*ac+row-wc-1
    index=min(index,len(m['frames'])-1);f=m['frames'][index];im=render_frame(m,pal,index);left=ax+f['left']-f['cx'];top=ay+f['top']-f['cy']
    if False:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT);left=ax-(f['right']-f['cx'])
    assert 0<=left and 0<=top and left+im.width<=width and top+im.height<=height,(ident,row,col)
    sheet.alpha_composite(im,(col*width+left,row*height+top))
  sheet.save(OUT/f'monster_{typ}.png')
  monsters.append(dict(type=typ,mpf=ident,width=width,height=height,anchorX=ax,anchorY=ay,walk=wc,attack=ac,frontAttackFrames=min(ac,len(m['frames'])-m['attack']-ac),rows=rows,bytes=sheet.width*sheet.height*4,sourceSha256=hashlib.sha256(raw).hexdigest()))
 manifest=dict(sourceMap=2102,width=150,height=102,monsters=monsters,atlasBytes=sum(m['bytes'] for m in monsters),files={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.glob('*.png'))})
 assert manifest['atlasBytes']<=35389440
 (OUT/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print('ORIGINAL_MINE',len(manifest['files']),'textures',len(monsters),'appearances',manifest['atlasBytes'],'atlas bytes')

# The two admitted armor sources are retained in the same offline source package.
with zipfile.ZipFile(SRC/'armor_sources.zip') as z:
 for identity in ('mu0000037','mu0000039'):
  for source,destination in [(f'atlas/armor/{identity}.webp',f'wearable-atlases/{identity}.webp'),(f'data/type/armor/{identity}.json',f'source-registration/{identity}.json')]:
   path=OUT.parent/destination;path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(z.read(source))
