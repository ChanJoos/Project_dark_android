#!/usr/bin/env python3
"""Project-selected masks of labelled retained 2015 sources; never synthesizes occluded pixels."""
import hashlib,json,math
from pathlib import Path
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'master/source/skill_fx/naver_cafe_archive_20261001'
OUT=ROOT/'app/src/main/assets/skill-presentation/rogue';OUT.mkdir(exist_ok=True)
(OUT/'icons').mkdir(exist_ok=True)
media={(r['articleId'],r['imageIndex']):m for m in json.loads((SOURCE/'media.json').read_text())['media'] for r in m['references']}
def source(ref):
 m=media[(ref['articleId'],ref['imageIndex'])]
 assert hashlib.sha256((ROOT/m['path']).read_bytes()).hexdigest()==m['sha256']
 return m
bindings=json.loads((SOURCE/'rogue_bindings.json').read_text())
manifest={'revision':bindings['revision'],'nativeArchivePixels':False,'limitations':['Temporal-minimum SCREEN extraction and manually selected masks are approximate.','Occluded/dark pixels are absent, source facing is not a four-way visual acceptance.','BODY registration and event timing are project choices; no original mechanic is inferred.'],'skills':{}}
references=[]
for binding in bindings['rows']:
 sid=binding['id'];slug=hashlib.sha256(sid.encode()).hexdigest()[:16]
 if 'icon' in binding:
  ref=binding['icon'];m=source(ref);im=Image.open(ROOT/m['path']);crop=ref['crop'];assert crop[2]<=im.width and crop[3]<=im.height
  path='icons/'+slug+'.png';im.crop(crop).save(OUT/path)
  references.append(dict(id=sid,iconAssetPath=path,sourcePath=m['path'],sourceSha256=m['sha256'],sourceOrdinal=ref,crop=crop))
 if 'noEffect' in binding:
  m=source(binding['noEffect']);manifest['skills'][sid]={'channels':{},'review':binding['noEffect'],'sourceGif':m['path'],'sourceSha256':m['sha256']}
 if 'effect' not in binding:continue
 ref=binding['effect'];m=source(ref);im=Image.open(ROOT/m['path']);rgb=[];times=[]
 for i in range(im.n_frames):
  im.seek(i);rgb.append(np.array(im.convert('RGB'),dtype=np.float32));times.append(max(10,im.info.get('duration',100)))
 base=np.min(rgb,axis=0);h,w=base.shape[:2];valid=np.zeros((h,w),dtype=bool);x0,y0,x1,y1=ref['region'];valid[y0:y1,x0:x1]=True
 for a,b,c,d in ref.get('blocked',[]):valid[b:d,a:c]=False
 tiles=[];counts=[]
 for frame in rgb:
  diff=np.maximum(frame-base,0);emission=np.clip(diff/np.maximum(255-base,1),0,1);alpha=np.max(emission,axis=2)
  mask=valid&(np.max(diff,axis=2)>23)&((np.max(frame,axis=2)-np.min(frame,axis=2)>35)|(np.min(frame,axis=2)>150))
  if ref.get('rejectWarmActorPixels'):mask&=~((frame[:,:,0]>frame[:,:,1]*1.4)&(frame[:,:,2]<frame[:,:,1]))
  if ref.get('yellowOnly'):mask&=(frame[:,:,0]>frame[:,:,2]*1.2)
  alpha[~mask]=0;fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255);tile=np.dstack((fg,np.round(alpha*255))).astype(np.uint8);tile[~mask]=0;tiles.append(tile);counts.append(int(np.count_nonzero(tile[:,:,3])))
 assert max(counts)>=3,(sid,'No recoverable source pixels')
 atlas=Image.new('RGBA',(w*4,h*math.ceil(len(tiles)/4)))
 for i,t in enumerate(tiles):atlas.paste(Image.fromarray(t),((i%4)*w,(i//4)*h))
 path=slug+'.png';atlas.save(OUT/path)
 channel=dict(path='rogue/'+path,width=w,height=h,columns=4,durationsMs=times,frameIndices=list(range(len(times))),pivotX=ref['pivot'][0],pivotY=ref['pivot'][1],scale=1.0,blend='SCREEN',anchor=ref['anchor'],sourceGif=m['path'],sourceSha256=m['sha256'],atlasSha256=hashlib.sha256((OUT/path).read_bytes()).hexdigest(),opaquePixelCounts=counts,review=ref)
 manifest['skills'][sid]={'channels':{ref['channel']:channel}}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
(OUT/'references.json').write_text(json.dumps({'revision':bindings['revision'],'rows':references},ensure_ascii=False,indent=2)+'\n')
print('Rogue icons',len(references),'reviewed effects/no-particle IDs',len(manifest['skills']),'channels',sum(len(v['channels']) for v in manifest['skills'].values()))
