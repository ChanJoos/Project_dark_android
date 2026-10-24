"""User V234: labelled Seupgyeok source, distinct source colours, source-actor cleanup, straight throw."""
from pathlib import Path
from PIL import Image,ImageDraw
from scipy import ndimage
import json,hashlib,math,csv
import numpy as np
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation';S=R/'master/source/skill_fx/naver_cafe_archive_20261001'
def save(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
media={(r['articleId'],r['imageIndex']):m for m in json.loads((S/'media.json').read_text())['media'] for r in m['references']}
source=media[245450,23];im=Image.open(R/source['path']);frames=[];times=[]
for i in range(im.n_frames):im.seek(i);frames.append(np.array(im.convert('RGB'),float));times.append(im.info.get('duration',150))
base=np.min(frames,axis=0);h,w=base.shape[:2];atlas=Image.new('RGBA',(w*4,h*math.ceil(len(frames)/4)));counts=[]
for i,c in enumerate(frames):
 diff=np.maximum(c-base,0);em=np.clip(diff/np.maximum(255-base,1),0,1);alpha=em.max(2);r,g,b=c[:,:,0],c[:,:,1],c[:,:,2];mask=(b>r+18)&(g>r+12)&(b>130)&(diff.max(2)>23);valid=np.zeros((h,w),bool);valid[35:106,35:93]=True;mask&=valid;alpha[~mask]=0;fg=np.clip(em/np.maximum(alpha[:,:,None],.001)*255,0,255);tile=np.dstack((fg,np.round(alpha*255))).astype('uint8');tile[~mask]=0;atlas.paste(Image.fromarray(tile),(i%4*w,i//4*h));counts.append(int(mask.sum()))
f=A/'rogue/manifest.json';m=json.loads(f.read_text());path='rogue/'+hashlib.sha256('SK_도적_015'.encode()).hexdigest()[:16]+'.png';atlas.save(A/path);m['skills']['SK_도적_015']={'channels':{'RECIPIENT_CONTACT':{'path':path,'width':w,'height':h,'columns':4,'durationsMs':times,'frameIndices':list(range(len(times))),'pivotX':61,'pivotY':97,'scale':1.0,'blend':'SCREEN','anchor':'RECIPIENT','sourceGif':source['path'],'sourceSha256':source['sha256'],'atlasSha256':sha(A/path),'opaquePixelCounts':counts,'review':{'articleId':245450,'imageIndex':23,'region':[35,35,93,106],'evidence':'LABELLED_SEUPGYEOK_BLUE_WHITE_ORIGINAL_CONTACT_NOT_ADAPTED_AMBUSH'}}}}
save(f,m)
# Source capture actors must not be emitted onto a live recipient. Copy nearby visible FX
# samples into tiny outline gaps; transparent exterior stays transparent. This is explicitly
# source-derived cleanup (ADAPTED), not a claim that occluded original DAT pixels were recovered.
f=A/'captured/manifest.json';m=json.loads(f.read_text())
for sid in ['SK_도적_020','SK_도적_027']:
 ch=m['skills'][sid];im=Image.open(R/ch['sourceGif']);frames=[]
 for i in range(im.n_frames):im.seek(i);frames.append(np.array(im.convert('RGB'),float))
 base=frames[-1];h,w=base.shape[:2];atlas=Image.new('RGBA',(w*4,h*math.ceil(len(ch['frameIndices'])/4)));counts=[];cleaned=[]
 for out,i in enumerate(ch['frameIndices']):
  c=frames[i];diff=c-base;r,g,b=c[:,:,0],c[:,:,1],c[:,:,2];bright=(c.min(2)>170)&(diff.min(2)>24);warm=(r-b>14)&(r>140)&(g>50);cold=(b>r+20)&(g>r+10)&(b>140);purple=(r>g+20)&(b>g+20)&(b>140);mask=(warm|cold|purple|bright)&(np.abs(diff).max(2)>22)
  em=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1);alpha=em.max(2);alpha[~mask]=0;fg=np.clip(em/np.maximum(alpha[:,:,None],.0001)*255,0,255);tile=np.dstack((fg,np.round(alpha*255))).astype('uint8');tile[~mask]=0
  actor=Image.new('1',(w,h));d=ImageDraw.Draw(actor)
  if sid.endswith('020'):
   d.polygon([(61,43),(104,43),(109,77),(119,91),(123,151),(119,186),(59,186),(54,135),(55,87)],fill=1);cx,cy=134,126
  else:
   d.polygon([(72,50),(116,50),(122,88),(139,107),(131,139),(180,142),(183,170),(137,174),(140,199),(72,199),(63,150),(61,108)],fill=1);cx,cy=160,110
  stencil=np.array(actor,dtype=bool)
  # The unobscured opposite side supplies actual same-frame effect samples.
  # This explicit adapted reconstruction removes the capture actor without a cutout.
  yy,xx=np.indices((h,w));sx=np.clip(2*cx-xx,0,w-1);sy=yy.copy();conflict=stencil[sy,sx];sy[conflict]=np.clip(2*cy-yy[conflict],0,h-1);donor=tile[sy,sx].copy();donor[stencil[sy,sx]]=0;tile[stencil]=donor[stencil]
  labels,num=ndimage.label(tile[:,:,3]>0)
  for label in range(1,num+1):
   component=labels==label
   if component.sum()<25:tile[component]=0
  atlas.paste(Image.fromarray(tile),(out%4*w,out//4*h));counts.append(int((tile[:,:,3]>0).sum()));cleaned.append(int(stencil.sum()))
 atlas.save(A/ch['path']);ch.update(atlasSha256=sha(A/ch['path']),opaquePixelCounts=counts,displayRepair={'revision':'V234_NO_SOURCE_ACTOR_OR_GHOST_OUTLINE','colours':'Full original warm/pink/blue/white phases retained','cleanup':'ADAPTED same-frame opposite-side source FX sample copy in actor silhouette; no source actor emitted','actorMaskPixels':cleaned})
save(f,m)
# Explicit user spatial correction, independent from source animation timings/damage coefficients.
f=R/'docs/SKILL_ACTION_DECISIONS.csv'
with f.open() as src:reader=csv.DictReader(src);fields=reader.fieldnames;rules=list(reader)
for r in rules:
 if r['id']=='SK_도적_003':r.update(minReach='1',reach='4',spatialEvidence='U_V234_FACING_RAY_NEAREST_1_TO_4',note='현재 바라보는 직선 1~4칸의 가장 가까운 몬스터 1마리. 자동 접근/회전/관통 없음.')
with f.open('w',newline='') as out:writer=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');writer.writeheader();writer.writerows(rules)
keys=['id','name','mode','pattern','reach','minReach','mechanic','hits','contact','spatialEvidence','note','kind'];lines=['package com.projectdark.mobile;','','/** Generated from the reviewed per-ID decisions; do not infer rules from skill names. */','final class SkillActionData {','  static final String[][] ROWS={']
for r in rules:lines.append('    {'+', '.join(json.dumps(str(r[k]),ensure_ascii=False) for k in keys)+'},')
lines+=[' };','}'];(R/'app/src/main/java/com/projectdark/mobile/SkillActionData.java').write_text('\n'.join(lines)+'\n')
f=A/'catalog.json';m=json.loads(f.read_text())
for r in m['skills']:
 if r['id']=='SK_도적_003':r['note']='현재 바라보는 직선 1~4칸, 가장 가까운 단일 몬스터'
 if r['id']=='SK_도적_015':r['effectEvidence']='FAN_LABELLED_ARTICLE245450_IMAGE23_BLUE_WHITE_SOURCE_V234'
save(f,m)
with (R/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',newline='') as out:writer=csv.DictWriter(out,fieldnames=list(m['skills'][0]),lineterminator='\n');writer.writeheader();writer.writerows(m['skills'])
print('V234 Seupgyeok blue source,full original colours,actor cleanup,straight nearest throw1..4')
