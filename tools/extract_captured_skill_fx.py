#!/usr/bin/env python3
"""Deterministic capture matting. Retains source GIF bytes; no generated/repainted art.
Floor/actor occlusion cannot recover hidden effect pixels: capture-derived, not DAT art.
"""
import json, hashlib, math
from pathlib import Path
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'master/source/skill_fx/naver_401229'
OUT=ROOT/'app/src/main/assets/skill-presentation/captured'
OUT.mkdir(parents=True,exist_ok=True)
SPECS={
 'crasher':('SK_전사_015',1,9,'cyan'),
 'mad_soul':('SK_전사_013',1,15,'warm'),
 'devil_crasher':('SK_전사_023',1,12,'red'),
 'mad_soul_jin':('SK_전사_022',1,17,'warm'),
 'assassination':('SK_도적_020',0,31,'warm'),
 'assassination_jin':('SK_도적_027',0,19,'warm'),
 'dara':('SK_무도가_020',1,6,'cyan'),
 'guyang':('SK_무도가_021',1,6,'purple'),
 'dalma':('SK_무도가_023',1,4,'gold'),
}
result={'revision':'FAN_CAPTURE_MATTED_V67','nativeArchivePixels':False,'limitations':['Occluded pixels are not invented.','Colour/alpha inverse matting is approximate because GIF has a composited floor and actor.','Clipped frame edges remain clipped.','Per-skill capture actor pose does not authenticate the selected male/female BODY mapping.'],'skills':{}}
for slug,(sid,start,end,kind) in SPECS.items():
 source=SOURCE/(slug+'.gif');im=Image.open(source);frames=[];durations=[]
 for i in range(im.n_frames):
  im.seek(i);frames.append(np.asarray(im.convert('RGB'),dtype=np.float32));durations.append(im.info.get('duration',100))
 h,w=frames[0].shape[:2];base=frames[-1] if start==0 else frames[0]
 atlas=Image.new('RGBA',(w*4,h*math.ceil((end-start)/4)))
 counts=[]
 for out_index,i in enumerate(range(start,end)):
  c=frames[i];diff=c-base;r,g,b=c[:,:,0],c[:,:,1],c[:,:,2]
  bright=(np.min(c,axis=2)>170)&(np.min(diff,axis=2)>24)
  if kind=='cyan':colour=(b-r>15)&(g-r>12)
  elif kind=='red':colour=(r-g>18)&(r-b>20)
  elif kind=='purple':colour=(r-g>12)&(b-g>12)
  else:colour=(r-b>14)&(r>140)&(g>50)
  mask=(colour|bright)&(np.max(np.abs(diff),axis=2)>22)
  # Source actor occlusion stays transparent; do not bake a foreign character.
  from PIL import ImageDraw
  body=Image.new('1',(w,h));draw=ImageDraw.Draw(body)
  if slug in ('devil_crasher','mad_soul_jin','assassination_jin'):
   draw.polygon([(77,69),(112,69),(116,94),(132,110),(122,149),(134,172),(124,195),(85,195),(75,151),(67,115)],fill=1)
   draw.polygon([(107,135),(175,146),(178,164),(125,167)],fill=1)
  else:
   draw.polygon([(64,49),(100,49),(103,75),(114,88),(116,122),(120,155),(111,182),(65,182),(58,139),(57,96)],fill=1)
  mask[np.asarray(body,dtype=bool)]=False
  if slug in ('devil_crasher','mad_soul_jin','assassination_jin'):mask[:46,:205]=False
  if slug in ('dara','guyang'):mask[:35,:150]=False
  # Capture source has luminous SCREEN blending. Invert it per channel, preserving
  # bright cores without baking the floor colours into a normal-alpha sprite.
  emission=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1)
  alpha=np.max(emission,axis=2);alpha[~mask]=0
  foreground=np.clip(emission/np.maximum(alpha[:,:,None],.0001)*255,0,255)
  rgba=np.dstack([foreground,np.round(alpha*255)]).astype(np.uint8);rgba[~mask,:]=0
  tile=Image.fromarray(rgba,'RGBA');atlas.paste(tile,((out_index%4)*w,(out_index//4)*h));counts.append(int(np.count_nonzero(rgba[:,:,3])))
 target=OUT/(slug+'.png');atlas.save(target)
 result['skills'][sid]={'slug':slug,'path':'captured/'+slug+'.png','width':w,'height':h,'columns':4,'frameIndices':list(range(start,end)),'durationsMs':durations[start:end],'pivotX':100 if slug in ('devil_crasher','mad_soul_jin','assassination_jin') else 85,'pivotY':185 if slug in ('devil_crasher','mad_soul_jin','assassination_jin') else 165,'scale':.32,'directional':slug in ('mad_soul','mad_soul_jin'),'directionPivotLift':55,'sourceForwardDistance':125,'registrationEvidence':'PROJECT_REGISTRATION:116px source actor vs37px runtime actor; source forward burst on positive X; directional projection uses contact target vector.','channel':'CASTER_AT_CONTACT','blend':'SCREEN','sourceGif':'master/source/skill_fx/naver_401229/'+slug+'.gif','sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'atlasSha256':hashlib.sha256(target.read_bytes()).hexdigest(),'opaquePixelCounts':counts,'evidence':'FAN_CAPTURE_DERIVED_ALPHA_APPROXIMATE'}
(OUT/'manifest.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
print('capture sequences',len(result['skills']),'frames',sum(len(v['durationsMs']) for v in result['skills'].values()))
