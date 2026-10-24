"""Decode retained Pote MPFs into the existing 12-pose presentation contract.
Original pixels, client two views and mirrored opposite views; no source overwrites.
These are three sampled poses, not a claim to full original animation playback.
"""
from pathlib import Path
import hashlib,json,zipfile
from PIL import Image,ImageDraw
from horror_original_formats import mpf,palette,render_frame
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'master/source/pote_v239'
OUT=ROOT/'assets/pote/production/pote_monsters_generated_v1/sprites/original_v239'
SPECS={'red_pamfet':259,'green_pamfet':260,'purple_pamfet':261,'silver_pamfet':262,
       'trant':264,'lycanthrope':265,'gnoll':274,'wolf_rider':275,'antlion':276,
       'ant_giant':277,'silver_wolf':278}
def main():
 ref=json.loads((SRC/'reference.json').read_text());records=[]
 with zipfile.ZipFile(SRC/'original_monsters.zip') as z:
  for n,digest in ref['sourceFiles'].items():assert hashlib.sha256(z.read(n)).hexdigest()==digest,n
  for name,ident in SPECS.items():
   m=mpf(z.read(f'mns{ident:03}.mpf'));pal=palette(z.read(f"mns{m['palette']:03}.pal"))
   for pose in ('idle','walk','attack'):
    for col,direction in enumerate(('nw','ne','sw','se')):
     view=0 if col<2 else 1
     if pose=='idle':index=m['stand']+view*m['standcount'] if m['standcount'] else m['walk']+view*m['walkcount']
     elif pose=='walk':index=m['walk']+view*m['walkcount']+min(1,m['walkcount']-1)
     else:index=m['attack']+view*m['attackcount']+min(1,m['attackcount']-1)
     f=m['frames'][index];im=render_frame(m,pal,index)
     x=48+f['left']-f['cx'];y=76+f['top']-f['cy']
     if col in (0,2):im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT);x=48-(f['right']-f['cx'])
     assert x>=0 and y>=0 and x+im.width<=96 and y+im.height<=96,(name,pose,direction,x,y,im.size)
     canvas=Image.new('RGBA',(96,96));canvas.alpha_composite(im,(x,y))
     # Two exact nearest pixels per original pixel, drawn at 96 logical pixels.
     canvas=canvas.resize((192,192),Image.Resampling.NEAREST)
     dest=OUT/name/f'{pose}_{direction}.png';dest.parent.mkdir(parents=True,exist_ok=True);canvas.save(dest)
     records.append(dict(family=name,mpf=ident,pose=pose,direction=direction,index=index,mirrored=col in (0,2),path=str(dest.relative_to(ROOT)),sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),anchor=[96,152],drawHeight=96))
 (SRC/'poses.json').write_text(json.dumps({'revision':'V239_ORIGINAL_POSE_SAMPLES','records':records},indent=2)+'\n')
 print('POTE_V239',len(records),'original sampled poses; full animation PENDING')
if __name__=='__main__':main()
