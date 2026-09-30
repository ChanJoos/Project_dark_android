#!/usr/bin/env python3
"""Deterministic read-only projection of retained 2020 cafe tables and demonstrations."""
import csv,json,re,hashlib,math
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];SOURCE=ROOT/'master/source/skill_fx/naver_classic_2020';OUT=ROOT/'app/src/main/assets/skill-presentation';OUT.mkdir(exist_ok=True)
CLASSIC=OUT/'classic';ICONS=CLASSIC/'icons';CLASSIC.mkdir(exist_ok=True);ICONS.mkdir(exist_ok=True)
# Android/Robolectric asset lookup does not reliably resolve Hangul paths.
# Generated paths are stable ASCII hashes; source IDs/names remain in JSON.
for old in CLASSIC.glob('*.png'):old.unlink()
for old in ICONS.glob('*.png'):old.unlink()
rows=json.loads((SOURCE/'definitions.json').read_text());manifest={'evidence':'FAN_CLASSIC_SEO_CAPTURE_2020','nativeArchivePixels':False,'limitations':['Capture-derived inverse matting is approximate.','Captured actors, HP bars and state icons are masked; hidden effect pixels remain absent.','Black/dark effect pixels cannot be recovered by SCREEN matting.','Sources show one facing; other facings use the retained BODY rig with project-selected registration.'],'skills':{}}
# Every capture is bound by its labelled table, never by a guessed GIF filename.
for row in rows:
 sid=row['id'];demo=row['demonstration'];
 if not sid or not demo or row['name'] in ['이형환위','로카메아','소모니아']:continue
 im=Image.open(ROOT/demo['path']);rgb=[];times=[]
 for i in range(im.n_frames):im.seek(i);rgb.append(np.asarray(im.convert('RGB'),dtype=np.float32));times.append(max(10,im.info.get('duration',100)))
 h,w=rgb[0].shape[:2];base=np.median(rgb,axis=0)
 # Initial frame is an idle floor/actor reference in these demonstrations.
 base=rgb[0]
 martial=row['job']=='무도가';selfEffect=martial and row['name'] not in ['단각','통배권','붕각','백보신권','일음지','장풍','흡정신공','발경'] or row['name'] in ['실드','이모탈','리플렉토']
 caster=(118,113) if martial and not selfEffect else (108,116) if martial else (56,94)
 recipient=caster if selfEffect else (145,122) if martial else (141,107)
 body=Image.new('1',(w,h));d=ImageDraw.Draw(body)
 if martial:
  if selfEffect:d.polygon([(99,56),(117,56),(124,78),(127,107),(119,119),(99,119),(94,94),(97,77)],fill=1)
  else:
   d.polygon([(109,56),(126,56),(134,80),(137,98),(128,116),(108,116),(103,83)],fill=1)
   d.polygon([(134,62),(156,62),(161,85),(153,103),(155,123),(134,123),(136,88)],fill=1)
 else:
  d.polygon([(48,38),(65,38),(71,64),(81,77),(65,84),(66,95),(48,95),(45,63)],fill=1)
  d.polygon([(133,76),(152,76),(158,95),(153,109),(129,109),(129,93)],fill=1)
  if row['name'] in ['쿠러스','쿠라누스','쿠라네라','엑스쿠라네라']:d.rectangle((174,89,196,125),fill=1)
 blocked=np.array(body,dtype=bool);blocked[:36,:]=True
 # UI health/status area is excluded, including non-default capture sizes.
 if martial:
  blocked[25:45,90:175]=True
 else:
  blocked[35:55,120:min(w,174)]=True
 emitted=[]
 for c in rgb:
  diff=c-base;emission=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1)
  alpha=np.max(emission,axis=2);changed=np.max(np.abs(diff),axis=2)>23
  saturation=np.max(c,axis=2)-np.min(c,axis=2);bright=np.min(c,axis=2)>150
  mask=changed&((saturation>35)|bright)&~blocked
  alpha[~mask]=0;fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255)
  tile=np.dstack((fg,np.round(alpha*255))).astype(np.uint8);tile[~mask]=0;emitted.append(tile)
 # Two independent channels: captured caster sparks and recipient effect.
 channels={}
 for channel,anchor in [('CASTER_START',caster),('RECIPIENT_CONTACT',recipient)]:
  if selfEffect and channel=='CASTER_START':continue
  selection=[];counts=[]
  for tile in emitted:
   t=tile.copy()
   if not selfEffect:
    boundary=92 if not martial else 133
    if channel=='CASTER_START':t[:,boundary:]=0
    else:t[:,:boundary]=0
   counts.append(int(np.count_nonzero(t[:,:,3])));selection.append(t)
  nonzero=[i for i,c in enumerate(counts) if c>=3]
  if not nonzero:continue
  start,end=nonzero[0],nonzero[-1]+1;frames=selection[start:end];durations=times[start:end];atlas=Image.new('RGBA',(w*4,h*math.ceil(len(frames)/4)))
  for i,t in enumerate(frames):atlas.paste(Image.fromarray(t),((i%4)*w,(i//4)*h))
  slug=hashlib.sha256(sid.encode('utf-8')).hexdigest()[:16]+'_'+channel.lower();path='classic/'+slug+'.png';target=OUT/path;target.parent.mkdir(exist_ok=True);atlas.save(target)
  channels[channel]={'path':path,'width':w,'height':h,'columns':4,'durationsMs':durations,'frameIndices':list(range(start,end)),'pivotX':anchor[0],'pivotY':anchor[1],'scale':1.0,'blend':'SCREEN','anchor':'CASTER' if selfEffect or channel=='CASTER_START' else 'RECIPIENT','sourceGif':demo['path'],'sourceSha256':demo['sha256'],'atlasSha256':hashlib.sha256(target.read_bytes()).hexdigest(),'opaquePixelCounts':counts[start:end]}
 if channels:manifest['skills'][sid]={'channels':channels,'name':row['name'],'article':row['article']}
(OUT/'classic/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print('reference rows',len(rows),'runtime captured IDs',len(manifest['skills']),'channels',sum(len(v['channels']) for v in manifest['skills'].values()))

for row in rows:
 if row['id']:
  filename=hashlib.sha256(row['id'].encode('utf-8')).hexdigest()[:16]+'.png';row['iconAssetPath']='icons/'+filename
  (ICONS/filename).write_bytes((ROOT/row['icon']['path']).read_bytes())
# Preserve full 61-source table rows, including explicitly excluded and missing-ID references.
(OUT/'classic/references.json').write_text(json.dumps({'revision':'CLASSIC_TABLES_V68','rows':rows},ensure_ascii=False,indent=2)+'\n')
