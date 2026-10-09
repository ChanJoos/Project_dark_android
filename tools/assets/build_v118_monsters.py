"""Slice generated multi-family sheets; preserve canvas/ground scale, never mirror poses."""
from pathlib import Path
from PIL import Image
import hashlib, json, shutil, argparse
import numpy as np
from scipy import ndimage

p=argparse.ArgumentParser();p.add_argument('generated_dir',type=Path);a=p.parse_args()
review=Path('assets/pote/review/monster_v118');review.mkdir(parents=True,exist_ok=True)
out=Path('assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v3')
specs=[
 ('pamfet.png','exec-bfefd230-3285-434f-8f2f-500c8d88d4fe.png',8,6,[('red_pamfet',0,0,.625),('green_pamfet',4,0,.625),('purple_pamfet',0,3,.625),('silver_pamfet',4,3,.625)]),
 ('wolves.png','exec-cb1e00c8-f35a-43d7-9791-80601aec1cfb.png',8,6,[('lycanthrope',0,0,2/3),('wolf_rider',4,0,.917),('gnoll',0,3,.917),('silver_wolf',4,3,.917)]),
 ('insects.png','exec-433e73f1-2de9-4843-8ce9-7dd9f6567b19.png',8,6,[('antlion',4,0,.917),('ant_giant',0,3,.917),('giant_mantis',4,3,.917)]),
 ('treant.png','exec-39cb7f20-de90-4b2c-9c02-dc88e3549cbc.png',4,3,[('trant',0,0,.917)]),
 ('spirits.png','exec-cb71bcbd-5b53-4971-97ac-33398d8912aa.png',4,6,[('brown_pote_spirit',0,0,.917),('black_pote_spirit',0,3,.917)])]
rows=[]
for sheet,source,columns,lines,families in specs:
 target=review/sheet
 if (a.generated_dir/source).exists():shutil.copyfile(a.generated_dir/source,target)
 im=Image.open(target).convert('RGBA');im.load()
 for family,col,row,fraction in families:
  cells=[]
  for j,state in enumerate(('idle','walk','attack')):
   for i,direction in enumerate(('nw','ne','sw','se')):
    box=(round((col+i)*im.width/columns),round((row+j)*im.height/lines),round((col+i+1)*im.width/columns),round((row+j+1)*im.height/lines))
    cell=im.crop(box)
    # Crop the principal connected actor, excluding detached shadows/neighbour-cell tips.
    labels,count=ndimage.label(np.array(cell.getchannel('A'))>32)
    assert count,(family,state,direction)
    sizes=np.bincount(labels.ravel());sizes[0]=0
    principal=labels==sizes.argmax()
    yy,xx=np.where(principal);bounds=(int(xx.min()),int(yy.min()),int(xx.max()+1),int(yy.max()+1))
    # A bounding rectangle alone retains unrelated neighbour fragments inside it.
    # Retain the principal actor plus its antialiased edge pixels only.
    rgba=np.array(cell)
    keep=ndimage.binary_dilation(principal,iterations=1)
    rgba[~keep,3]=0
    cell=Image.fromarray(rgba)
    cells.append((state,direction,box,cell.crop(bounds)))
  # One shared species scale; different poses keep their relative proportions.
  scale=min(192*fraction/max(c.height for *_,c in cells),180/max(c.width for *_,c in cells))
  for state,direction,box,cell in cells:
   cell=cell.resize((max(1,round(cell.width*scale)),max(1,round(cell.height*scale))),Image.Resampling.NEAREST)
   frame=Image.new('RGBA',(192,192));frame.alpha_composite(cell,((192-cell.width)//2,184-cell.height))
   destination=out/family/f'{state}_{direction}.png';destination.parent.mkdir(parents=True,exist_ok=True);frame.save(destination,optimize=True)
   rows.append(dict(family=family,pose=state,direction=direction,source=sheet,crop=box,size=[192,192],groundBaseline=184,sha256=hashlib.sha256(destination.read_bytes()).hexdigest()))
manifest=dict(version=118,evidence='ADAPTED_GENERATED_USER_REFERENCE; original animation not verified',poseContract='idle/walk/attack x NW/NE/SW/SE; one still per pose, no mirroring',sources={sheet:hashlib.sha256((review/sheet).read_bytes()).hexdigest() for sheet,*_ in specs},frames=rows)
(out/'provenance.json').write_text(json.dumps(manifest,indent=2)+'\n')
assert len(rows)==168
print('V118_FRAMES',len(rows),'FAMILIES',len({r['family'] for r in rows}))
