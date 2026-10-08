"""Preserve adapted source bytes and split the V120 style sheet into existing runtime keys."""
from pathlib import Path
from PIL import Image
from scipy import ndimage
import numpy as np
import hashlib,json,sys,shutil
review=Path('assets/pote/review/monster_v120');review.mkdir(parents=True,exist_ok=True)
source=review/'wolves_style.png'
if len(sys.argv)>1:
 for p in Path(sys.argv[1]).glob('*.png'): shutil.copyfile(p,review/p.name)
root=Path('assets/pote/production/pote_monsters_generated_v1/sprites/campaign_v3')
manifest=json.loads((root/'provenance.json').read_text())
rows=[]
for family,col,row,fraction in [('lycanthrope',0,0,2/3),('wolf_rider',0,0,.917),('gnoll',0,0,.917),('silver_wolf',0,0,.917)]:
 source=review/(family+'.png'); im=Image.open(source).convert('RGBA')
 cells=[]
 rgba=np.array(im);labels,count=ndimage.label(rgba[:,:,3]>32);sizes=np.bincount(labels.ravel());sizes[0]=0
 actors=[n for n in range(1,count+1) if sizes[n]>=max(800,sizes.max()*.1)]
 assert len(actors)==12,(family,'expected 12 separated actors',len(actors))
 bounds={}
 for n in actors:
  yy,xx=np.where(labels==n);bounds[n]=(int(xx.min()),int(yy.min()),int(xx.max()+1),int(yy.max()+1))
 actors.sort(key=lambda n:(bounds[n][1]+bounds[n][3])/2)
 for j,state in enumerate(('idle','walk','attack')):
  group=sorted(actors[j*4:j*4+4],key=lambda n:(bounds[n][0]+bounds[n][2])/2)
  for n,direction in zip(group,('nw','ne','sw','se')):
   keep=ndimage.binary_dilation(labels==n,iterations=1);part=rgba.copy();part[~keep,3]=0
   isolated=Image.fromarray(part);box=isolated.getbbox();assert box
   strong=np.where(keep,rgba[:,:,3],0)>64
   assert not (strong[0,:].any() or strong[-1,:].any() or strong[:,0].any() or strong[:,-1].any()),(family,state,direction,'opaque source clipping',box)
   cells.append((state,direction,box,isolated.crop(box)))
 scale=min(192*fraction/max(c.height for *_,c in cells),180/max(c.width for *_,c in cells))
 for state,direction,box,cell in cells:
  cell=cell.resize((max(1,round(cell.width*scale)),max(1,round(cell.height*scale))),Image.Resampling.NEAREST)
  frame=Image.new('RGBA',(192,192));frame.alpha_composite(cell,((192-cell.width)//2,184-cell.height))
  dest=root/family/f'{state}_{direction}.png';frame.save(dest,optimize=True)
  rows.append(dict(family=family,pose=state,direction=direction,source=str(source),crop=box,size=[192,192],groundBaseline=184,sha256=hashlib.sha256(dest.read_bytes()).hexdigest()))
changed={r['family'] for r in rows}
manifest['frames']=[r for r in manifest['frames'] if r['family'] not in changed]+rows
manifest.update(version=120,evidence='ADAPTED_GENERATED_USER_REFERENCE; style matched to retained Pote spirits; original animation not verified')
for family in changed:
 p=review/(family+'.png');manifest['sources'][str(p)]=hashlib.sha256(p.read_bytes()).hexdigest()
(root/'provenance.json').write_text(json.dumps(manifest,indent=2)+'\n')
assert len(manifest['frames'])==168 and len(rows)==48
# Exact runtime-size review with unchanged spirits for direct style/size inspection.
canvas=Image.new('RGBA',(960,360),(52,62,39,255))
for j,family in enumerate(('brown_pote_spirit','lycanthrope','gnoll','wolf_rider','silver_wolf')):
 height=72 if family in ('lycanthrope','gnoll','wolf_rider') else 48
 for i,pose in enumerate(('idle','walk','attack')):
  for d,direction in enumerate(('nw','ne','sw','se')):
   frame=Image.open(root/family/f'{pose}_{direction}.png').convert('RGBA').resize((height,height),Image.Resampling.NEAREST)
   canvas.alpha_composite(frame,(j*192+10+d*44,i*120+30))
canvas.save(review/'runtime_scale_review.png')
print('V120: 48 replacement poses; 168 total; source/crop/hash and ground baseline recorded')
