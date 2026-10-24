"""Deterministic120 independent sprite registration, padded alpha, common foot anchor."""
from pathlib import Path
from PIL import Image
import io,json,hashlib,zipfile
root=Path(__file__).resolve().parents[1];src=root/'master/source/rucesion_v157';dest=root/'master/assets/rucesion_v158';dest.mkdir(parents=True,exist_ok=True)
families=('blue_hacon','red_hacon','arkpus','plant_arkpus','kakapo','poison_kakapo','koatl','elect_koatl','tyrant','ariel');frames=[];sources={}
for family in families:
 source=(root/'master/source/rucesion_v158'/(family+'.png')) if family in ('blue_hacon','elect_koatl') else src/(family+'.png');sheet=Image.open(source).convert('RGBA');sheet.putalpha(sheet.getchannel('A').point(lambda a:a if a>=16 else 0));alpha=sheet.getchannel('A')
 def seam(n,total,horizontal):
  target=round(n*total/(3 if horizontal else 4));runs=[]
  for p in range(max(1,target-90),min(total-1,target+91)):
   stripe=alpha.crop((0,p,sheet.width,p+1) if horizontal else (p,0,p+1,sheet.height))
   if stripe.getbbox() is None:
    if not runs or p>runs[-1][-1]+1:runs.append([p])
    else:runs[-1].append(p)
  mids=[a[len(a)//2] for a in runs if len(a)>2];assert mids,(family,'no padded seam',n,horizontal);return min(mids,key=lambda p:abs(p-target))
 cells=[]
 if family=='ariel':
  # Extended attack arms cross a straight column seam but do not touch.
  # Separate the twelve isolated alpha components, never slice off those arms.
  import numpy as np
  from scipy.ndimage import label,find_objects
  labels,n=label(np.array(alpha)>=16);counts=np.bincount(labels.ravel());parts=[]
  for ident,sl in enumerate(find_objects(labels),1):
   if counts[ident]>3000:parts.append((ident,(sl[1].start,sl[0].start,sl[1].stop,sl[0].stop)))
  assert len(parts)==12,(family,'twelve intact components',len(parts))
  parts.sort(key=lambda p:(p[1][1]+p[1][3])/2)
  for row,pose in enumerate(('idle','walk','attack')):
   group=sorted(parts[row*4:row*4+4],key=lambda p:p[1][0])
   for col,(ident,box) in enumerate(group):
    assert box[0]>0 and box[1]>0 and box[2]<sheet.width and box[3]<sheet.height,(family,'source clipping',box)
    cell=sheet.crop(box);mask=np.array(cell.getchannel('A'));keep=labels[box[1]:box[3],box[0]:box[2]]==ident
    cell.putalpha(Image.fromarray(np.where(keep,mask,0).astype('uint8')))
    cells.append((pose,('nw','ne','sw','se')[col],cell))
 else:
  xs=[0]+[seam(n,sheet.width,False) for n in range(1,4)]+[sheet.width];ys=[0]+[seam(n,sheet.height,True) for n in range(1,3)]+[sheet.height]
  for row,pose in enumerate(('idle','walk','attack')):
   for col,facing in enumerate(('nw','ne','sw','se')):
    cell=sheet.crop((xs[col],ys[row],xs[col+1],ys[row+1]));box=cell.getchannel('A').getbbox();assert box and box[0]>0 and box[1]>0 and box[2]<cell.width and box[3]<cell.height,(family,pose,facing,box,cell.size);cells.append((pose,facing,cell.crop(box)))
 scale=min(176/max(c.width for _,_,c in cells),176/max(c.height for _,_,c in cells));hashes=[]
 for pose,facing,cell in cells:
  cell=cell.resize((round(cell.width*scale),round(cell.height*scale)),Image.Resampling.NEAREST);frame=Image.new('RGBA',(192,192));frame.alpha_composite(cell,((192-cell.width)//2,184-cell.height));stream=io.BytesIO();frame.save(stream,format='PNG');data=stream.getvalue();hashes.append(hashlib.sha256(data).hexdigest());frames.append((f'pote_monsters_generated_v1/sprites/rucesion_v157/{family}/{pose}_{facing}.png',data))
 assert len(set(hashes))==12,(family,'duplicate poses');sources[family]=hashlib.sha256(source.read_bytes()).hexdigest()
for name in ('sand','water'):frames.append((f'rucesion_v157/{name}.png',(src/(name+'.png')).read_bytes()))
with zipfile.ZipFile(dest/'generated_assets.zip','w',zipfile.ZIP_DEFLATED) as z:
 for name,data in sorted(frames):
  entry=zipfile.ZipInfo(name,(2026,10,14,0,0,0));entry.compress_type=zipfile.ZIP_DEFLATED;z.writestr(entry,data)
(dest/'provenance.json').write_text(json.dumps({'evidence':'USER_LABELLED_VIDEO/ADAPTED_GENERATED_DIRECTIONAL_RECONSTRUCTION','sources':sources,'frames':{name:hashlib.sha256(data).hexdigest() for name,data in frames},'canvas':[192,192],'groundY':184,'originalAnimationVerified':False,'phoneAcceptance':'PENDING'},indent=2)+'\n')
print('Rucesion120 poses +2 source textures =',len(frames))
