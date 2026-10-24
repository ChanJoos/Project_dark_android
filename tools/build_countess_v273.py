"""Decode retained original Countess bytes deterministically. Never replace missing source art."""
from pathlib import Path
import json,zipfile,hashlib,math
import numpy as np
from PIL import Image
from horror_original_formats import mpf,palette,decompress,table
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/countess_v273';OUT=ROOT/'app/build/generated/countess-v273/countess-v273';OUT.mkdir(parents=True,exist_ok=True)
class Entries:
 def __init__(self,e):self.entries=e
 def __getitem__(self,k):return self.entries[k]
meta=json.loads((SRC/'sources.json').read_text());records=[]
with zipfile.ZipFile(SRC/'original_sources.zip') as z:
 e={n:z.read(n) for n in z.namelist()}
 for n,digest in meta['sourceFiles'].items():assert hashlib.sha256(e[n]).hexdigest()==digest,n
 ia=Entries({n[3:]:b for n,b in e.items() if n.startswith('ia/')});seo=Entries({n[4:]:b for n,b in e.items() if n.startswith('seo/')});ft=table(ia,'stc');bt=table(seo,'mpt')
 for n,b in ia.entries.items():
  if not n.endswith('.hpf'):continue
  ident=int(n[3:8]);raw=decompress(b)[8:];pal=palette(ia[f'stc{ft.get(ident+1,0)%1000:04}.pal']);Image.fromarray(pal[np.frombuffer(raw,np.uint8).reshape(-1,28)]).save(OUT/(n[:-4]+'.png'))
 for n,b in seo.entries.items():
  if not n.endswith('.raw'):continue
  ident=int(n[2:-4]);pal=palette(seo[f'mpt{bt.get(ident+1,0)%1000:04}.pal']);rgba=pal[np.frombuffer(b,np.uint8).reshape(27,56)].copy();rgba[:,:,3]=255
  for y in range(27):
   margin=abs(13-y)*2;rgba[y,:margin,3]=0
   if margin:rgba[y,56-margin:,3]=0
  Image.fromarray(rgba).save(OUT/(n[:-4]+'.png'))
 for typ,ident in enumerate(meta['mpfs']):
  m=mpf(e[f'hades/mns{ident:03}.mpf']);pal=palette(e[f"hades/mns{m['palette']:03}.pal"]);fs=m['frames'];lx=min(f['left']-f['cx'] for f in fs)-2;rx=max(f['right']-f['cx'] for f in fs)+2;ty=min(f['top']-f['cy'] for f in fs)-2;by=max(f['bottom']-f['cy'] for f in fs)+2;w=rx-lx;h=by-ty;ax=-lx;ay=-ty
  sheet=Image.new('RGBA',(w*4,h*math.ceil(len(fs)/4)))
  for i,f in enumerate(fs):
   raw=np.frombuffer(f['data'],np.uint8).reshape(f['bottom']-f['top'],f['right']-f['left']);im=Image.fromarray(pal[raw]);sheet.alpha_composite(im,((i%4)*w+f['left']-f['cx']+ax,(i//4)*h+f['top']-f['cy']+ay))
  sheet.save(OUT/f'monster_{typ}.png');records.append(dict(type=typ,mpf=ident,width=w,height=h,anchorX=ax,anchorY=ay,frames=len(fs),idle=m['stand'],idleCount=m['standcount'],walk=m['walk'],walkCount=m['walkcount'],attack=m['attack'],attackCount=m['attackcount'],sourceSha256=hashlib.sha256(e[f'hades/mns{ident:03}.mpf']).hexdigest()))
manifest=dict(revision='V273',monsters=records,files={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.glob('*.png'))},evidence=meta['evidence'])
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print('COUNTESS_SOURCE_DECODE',len(meta['maps']),'maps',len(records),'complete native-frame species',len(manifest['files']),'PNGs')
