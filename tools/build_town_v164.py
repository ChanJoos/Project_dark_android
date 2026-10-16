"""Deterministic production sizing/registration of built-in image_gen repairs.
Original inputs are retained; no source screenshot or original-design claim is made.
"""
from pathlib import Path
import json, hashlib, re
import numpy as np
from PIL import Image,ImageOps
R=Path(__file__).resolve().parents[1]
S=R/'master/source/town_v164'
receipt=[]
for folder in ['abel-video-v147','rucesion-v154']:
 root=R/'app/src/main/assets'/folder
 layout=json.loads((root/'layout.json').read_text());spec={a['id']:a for a in layout['assets']}
 names=['tree_broadleaf','bench_front_up','bench_front_down','bench_back_up','bench_back_down','bench_pool']
 if folder=='rucesion-v154':names=['tree_broadleaf','pergola_garden','bench_front_up','bench_front_down']
 for name in names:
  dest=root/(name+'.png');before=S/'before'/folder/(name+'.png');before.parent.mkdir(parents=True,exist_ok=True)
  if not before.exists():before.write_bytes(dest.read_bytes())
  old=Image.open(before).convert('RGBA');box=old.getbbox();kind='tree' if name.startswith('tree') else 'pergola' if name=='pergola_garden' else 'bench_back' if name.startswith('bench_back') else 'bench_front'
  src=S/'generated'/(kind+'.png');im=Image.open(src).convert('RGBA');a=np.array(im);ys,xs=np.where(a[:,:,3]>=128);im=im.crop((int(xs.min()),int(ys.min()),int(xs.max()+1),int(ys.max()+1)))
  if name.endswith('_down'):im=ImageOps.mirror(im)
  # Keep native canvas, contact pivot and original visible envelope. Binary native
  # alpha avoids resampling halos and retains the existing opaque-foot assertions.
  maxw,maxh=box[2]-box[0],box[3]-box[1]
  if name.startswith('bench'):maxh=min(maxh,int(spec[name]['anchor'][1])-box[1])
  im.thumbnail((maxw,maxh),Image.Resampling.LANCZOS);a=np.array(im);a[:,:,3]=np.where(a[:,:,3]>=128,255,0).astype(np.uint8);im=Image.fromarray(a)
  out=Image.new('RGBA',old.size);x=box[0]+(maxw-im.width)//2;y=box[3]-im.height
  if name.startswith('bench'):y=int(spec[name]['anchor'][1])-im.height
  out.alpha_composite(im,(x,y));out.save(dest)
  spec[name]['runtimeRepairV164']=dict(status='ADAPTED_GENERATED_REPAIR',source=str(src.relative_to(R)),sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),nativeCanvas=list(out.size),anchorPreserved=True,method='alpha128 crop;Lanczos native sizing;binary alpha;mirror only down-facing bench')
  receipt.append(dict(file=str(dest.relative_to(R)),beforeSha256=hashlib.sha256(before.read_bytes()).hexdigest(),afterSha256=hashlib.sha256(dest.read_bytes()).hexdigest()))
 # Pool bench has always been an alias of front_up, including identical bytes.
 if folder=='abel-video-v147':(root/'bench_pool.png').write_bytes((root/'bench_front_up.png').read_bytes())
 (root/'layout.json').write_text(json.dumps(layout,ensure_ascii=False,indent=2)+'\n')
# Only clear the overcrowded ADAPTED garden reconstruction, preserving actual
# source registrations, services, tile grids and collision footprint semantics.
root=R/'app/src/main/assets/abel-video-v147';path=root/'layout.json';d=json.loads(path.read_text());scene=d['scenes']['unified'];kept=[];removed=[]
for p in scene['props']:
 if p['asset']=='tree_broadleaf' and p.get('sourceParent') in {'garden_tree_pair','garden_tree_mass','garden_foreground_grove'} and any(q['asset']=='tree_broadleaf' and sum((p['sourceWorld'][i]-q['sourceWorld'][i])**2 for i in [0,1])<48**2 for q in kept):removed.append(p);continue
 kept.append(p)
scene['props']=kept
path.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
# Regenerate the live placement list without re-running historical reconstruction.
p=R/'app/src/main/java/com/projectdark/mobile/world/AbelUnifiedLayout.java';s=p.read_text();a=s.index('public static List<AbelTownGeometry.Prop> props()');s=s[:a]+'public static List<AbelTownGeometry.Prop> props(){List<AbelTownGeometry.Prop> p=new ArrayList<>();\n'
for prop in kept:s+='p.add(new AbelTownGeometry.Prop('+json.dumps(prop['asset'])+','+','.join(f'{v:.3f}f' for v in prop['sourceWorld']+prop['anchor']+prop['nativeSize'])+'));\n'
s+='return Collections.unmodifiableList(p);}\nprivate AbelUnifiedLayout(){} }\n';p.write_text(s)
out=S/'projection.json';prior=json.loads(out.read_text()) if out.exists() else {}
removed=prior.get('removedCrowdedAdaptedTrees',[])+removed
out.write_text(json.dumps(dict(status='ADAPTED_GENERATED_REPAIR',assets=receipt,removedCrowdedAdaptedTrees=removed,sourceScenesUnchanged=True,remainingAbelProps=len(kept)),ensure_ascii=False,indent=2)+'\n')
print('V164',len(receipt),'registered assets;',len(removed),'crowded adapted trees removed;',len(kept),'Abel props')
