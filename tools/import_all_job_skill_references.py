#!/usr/bin/env python3
"""Retained warrior shapes and explicitly named cross-job shared forms; no generated art."""
import json,hashlib,math
from pathlib import Path
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SOURCE=ROOT/'master/source/skill_fx/naver_cafe_archive_20261001';BASE=ROOT/'app/src/main/assets/skill-presentation';OUT=BASE/'warrior';OUT.mkdir(exist_ok=True);(OUT/'icons').mkdir(exist_ok=True)
for stale in OUT.glob('*.png'):stale.unlink()
lookup={(r['articleId'],r['imageIndex']):m for m in json.loads((SOURCE/'media.json').read_text())['media'] for r in m['references']}
cat={r['id']:r for r in json.loads((ROOT/'app/src/main/assets/skills/catalog.json').read_text())}
def source(ref):
 m=lookup[ref['articleId'],ref['imageIndex']];assert hashlib.sha256((ROOT/m['path']).read_bytes()).hexdigest()==m['sha256'];return m
bindings=json.loads((SOURCE/'warrior_bindings.json').read_text());manifest=dict(revision=bindings['revision'],nativeArchivePixels=False,limitations=['Historical fan capture; approximate background removal.', 'Static source frames use project-authored hold/fade timing, not original animation cadence.', 'Source facing does not authenticate all four facing VFX. BODY uses retained front/back source pixels and horizontal mirroring.'],skills={});references=[]
def channel(sid,ref):
 m=source(ref);im=Image.open(ROOT/m['path']).convert('RGB');rgb=np.array(im,dtype=np.float32);h,w=rgb.shape[:2];valid=np.zeros((h,w),dtype=bool)
 x0,y0,x1,y1=ref.get('region',[0,0,w,h]);valid[y0:y1,x0:x1]=True
 for a,b,c,d in ref.get('blocked',[]):valid[b:d,a:c]=False
 r,g,b=rgb[:,:,0],rgb[:,:,1],rgb[:,:,2]
 if ref.get('backdrop')=='FLAT_GRAY':
  edges=np.concatenate((rgb[:3].reshape(-1,3),rgb[-3:].reshape(-1,3)));bg=np.median(edges,axis=0);emission=np.maximum(rgb-bg,0)/np.maximum(255-bg,1);mask=valid&(np.max(np.abs(rgb-bg),axis=2)>20)
 else:
  emission=rgb/255;mask=valid
  if ref.get('greenOnly'):mask&=(g>130)&(g>r*1.3)&(g>b*1.05)
  elif ref.get('orangeOnly'):mask&=(r>150)&(r>g*1.15)&(g>40)&(g>b*1.1)
  elif ref.get('purpleOnly'):mask&=(b>g*1.15)&(r>g*1.1)&(b>70)
  elif ref.get('redOnly'):mask&=(r>150)&(r>g*1.35)&(r>b*1.2)
  else:mask&=(np.min(rgb,axis=2)>175)&(np.max(rgb,axis=2)-np.min(rgb,axis=2)<60)
 if ref.get('blueOnly'):mask&=(b>r*1.25)&(b>g*1.05)
 alpha=np.max(emission,axis=2);alpha[~mask]=0;fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255)
 assert np.count_nonzero(alpha)>3,(sid,'Empty mask')
 atlas=Image.new('RGBA',(w*4,h));counts=[]
 for i,fade in enumerate([.35,1,.75,.25]):
  tile=np.dstack((fg,np.round(alpha*255*fade))).astype(np.uint8);tile[~mask]=0;atlas.paste(Image.fromarray(tile),(i*w,0));counts.append(int(np.count_nonzero(tile[:,:,3])))
 slug=hashlib.sha256(sid.encode()).hexdigest()[:16];path=slug+'.png';atlas.save(OUT/path)
 return dict(path='warrior/'+path,width=w,height=h,columns=4,durationsMs=[70,110,100,80],frameIndices=[0,0,0,0],sourceTiming='PROJECT_STATIC_HOLD_FADE',pivotX=ref['pivot'][0],pivotY=ref['pivot'][1],scale=ref['scale'],anchor=ref['anchor'],blend='SCREEN',sourceGif=m['path'],sourceSha256=m['sha256'],atlasSha256=hashlib.sha256((OUT/path).read_bytes()).hexdigest(),opaquePixelCounts=counts,review=ref)
for row in bindings['rows']:
 sid=row['id'];assert cat[sid]['name']==row['name'];m=source(row['icon']);im=Image.open(ROOT/m['path']);path='icons/'+hashlib.sha256(sid.encode()).hexdigest()[:16]+'.png';im.save(OUT/path)
 references.append(dict(id=sid,name=row['name'],iconAssetPath=path,sourcePath=m['path'],sourceSha256=m['sha256'],sourceOrdinal=row['icon']))
 if row.get('effect'):
  ref=row['effect']
  if ref.get('sharedRogueId'):
   assert '도적 : 아무네지아' in json.loads((SOURCE/'191105.json').read_text())['text']
   borrowed=json.loads((BASE/'rogue/manifest.json').read_text())['skills'][ref['sharedRogueId']]
   manifest['skills'][sid]=dict(channels=borrowed['channels'],mapping='SOURCE_EXPLICIT_SHARED_EFFECT_REUSE',donorId=ref['sharedRogueId'])
  else:manifest['skills'][sid]={'channels':{ref['channel']:channel(sid,ref)}}
manifest['damageImpact']=channel('PROJECT_RECIPIENT_DAMAGE_SOURCE_REUSE',bindings['damageImpact'])
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(OUT/'references.json').write_text(json.dumps(dict(revision=bindings['revision'],rows=references),ensure_ascii=False,indent=2)+'\n')
# Exact same spell names may reuse a retained form only with an explicit project-reuse record.
SHARED=BASE/'shared';SHARED.mkdir(exist_ok=True);(SHARED/'icons').mkdir(exist_ok=True)
classic=json.loads((BASE/'classic/manifest.json').read_text());refs=json.loads((BASE/'classic/references.json').read_text())['rows'];shared=dict(revision='EXACT_NAME_SHARED_FORM_V70',nativeArchivePixels=False,limitations=['Exact name cross-job form reuse is a project choice, not per-job original animation proof.'],skills={});srefs=[]
for sid,e in cat.items():
 if sid in classic['skills']:continue
 candidates=[r for r in refs if r.get('id') and r['name']==e['name'] and r['id']!=sid]
 if not candidates:continue
 donor=candidates[0];donorId=donor['id'];filename=hashlib.sha256(sid.encode()).hexdigest()[:16]+'.png';path='icons/'+filename;(SHARED/path).write_bytes((BASE/'classic'/donor['iconAssetPath']).read_bytes())
 srefs.append(dict(id=sid,name=e['name'],donorId=donorId,iconAssetPath=path,mapping='EXACT_NAME_PROJECT_SOURCE_FORM_REUSE',sourceIcon=donor['icon']))
 if donorId in classic['skills']:
  shared['skills'][sid]=dict(channels=classic['skills'][donorId]['channels'],name=e['name'],donorId=donorId,mapping='EXACT_NAME_PROJECT_SOURCE_FORM_REUSE')
(SHARED/'manifest.json').write_text(json.dumps(shared,ensure_ascii=False,indent=2)+'\n');(SHARED/'references.json').write_text(json.dumps(dict(revision=shared['revision'],rows=srefs),ensure_ascii=False,indent=2)+'\n')
print('Warrior:',len(references),'icons,',len(manifest['skills']),'source shapes; shared:',len(srefs),'icons,',len(shared['skills']),'source forms')
