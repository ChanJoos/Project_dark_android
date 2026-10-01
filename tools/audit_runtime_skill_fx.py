#!/usr/bin/env python3
"""Per-ID runtime precedence, source bytes, projection losses and unresolved adaptation audit."""
from pathlib import Path
import json,csv,hashlib,collections
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';B=A/'skill-presentation'
cat=json.loads((A/'skills/catalog.json').read_text());presentation={r['id']:r for r in json.loads((B/'catalog.json').read_text())['skills']};rules={r['id']:r for r in csv.DictReader((R/'docs/SKILL_ACTION_DECISIONS.csv').open())}
order=['rogue','classic','warrior','shared','captured'];manifests={k:json.loads((B/k/'manifest.json').read_text())['skills'] for k in order};rows=[]
for e in cat:
 sid=e['id'];candidates=[k for k in order if sid in manifests[k]];selected=candidates[0] if candidates else 'NO_PARTICLE' if presentation[sid]['caster']=='NONE' and presentation[sid]['target']=='NONE' else 'ADAPTED';channels=[];warnings=[];reviewedSource=None
 if selected not in ['ADAPTED','NO_PARTICLE']:
  entry=manifests[selected][sid];ch=entry.get('channels',{'RECIPIENT_CONTACT':entry})
  if not ch:
   source=R/entry['sourceGif'];sourcehash=hashlib.sha256(source.read_bytes()).hexdigest();assert sourcehash==entry['sourceSha256'];reviewedSource={'source':entry['sourceGif'],'sourceSha256':sourcehash,'reason':entry.get('review',{}).get('reason','No separately recoverable particle')} 
  for phase,v in ch.items():
   source=R/v['sourceGif'];atlas=B/v['path'];sourcehash=hashlib.sha256(source.read_bytes()).hexdigest();atlashash=hashlib.sha256(atlas.read_bytes()).hexdigest();assert sourcehash==v['sourceSha256'];assert atlashash==v['atlasSha256']
   for item in v.get('sourceFrames',[]):
    assert hashlib.sha256((R/item['path']).read_bytes()).hexdigest()==item['sha256']
   plate=v.get('review',{}).get('mattingPlate')
   if plate:assert hashlib.sha256((R/plate['path']).read_bytes()).hexdigest()==plate['sha256']
   peak=max(v.get('opaquePixelCounts',[0]));flags=[]
   if peak<30:flags.append('LOW_PIXEL_CHANNEL_REVIEW')
   if v.get('sourceTiming')=='PROJECT_STATIC_HOLD_FADE':flags.append('STATIC_SOURCE_PROJECT_TIMING')
   channels.append({'phase':phase,'anchor':v.get('anchor','CASTER_AT_CONTACT'),'path':v['path'],'source':v['sourceGif'],'sourceSha256':sourcehash,'atlasSha256':atlashash,'frames':len(v['durationsMs']),'durationMs':sum(v['durationsMs']),'peakPixels':peak,'blend':v['blend'],'directional':v.get('directional',False),'flags':flags})
  warnings+=['SOURCE_MATTING_OCCLUSION_APPROXIMATE'] if channels else ['REVIEWED_NO_PARTICLE_CHANNEL']
 else:warnings.append('NO_DECLARED_PARTICLE' if selected=='NO_PARTICLE' else 'PROJECT_DRAWN_FALLBACK_NOT_SUPPLIED_ORIGINAL')
 if len(candidates)>1:warnings.append('SHADOWED_SOURCE_VARIANT_REVIEW')
 rule=rules[sid];allowed=rule['mode'] not in ['LINKED','UTILITY'] and rule['pattern'] not in ['PASSIVE','UTILITY','UNRESOLVED']
 if not allowed:warnings.append('SERVICE_PASSIVE_LINKED_NOT_STANDALONE_CAST')
 rows.append({'id':sid,'name':e['name'],'job':e['job'],'standalonePresentationAllowed':allowed,'runtimeBranch':selected,'sourceCandidates':candidates,'channels':channels,'reviewedSourceWithoutChannel':reviewedSource,'warnings':warnings,'originalFourFacingPixels':'NOT_PROVEN','device':'USER_REPORTED_FAILURE' if e['name'] in ['크래셔','완전방어','매드소울','단각','붕각'] else 'PENDING','visualAcceptance':'SHAPE_USER_ACCEPTED_QUALITY_PENDING' if e['name']=='완전방어' else 'PENDING'})
counts=dict(collections.Counter(r['runtimeBranch'] for r in rows));out={'revision':'V77_FULL_RUNTIME_FX_AUDIT','catalogCount':len(rows),'branchCounts':counts,'sourceProjectedSkillCount':sum(bool(r['channels']) for r in rows),'reviewedWithoutChannelCount':sum(r['reviewedSourceWithoutChannel'] is not None for r in rows),'sourceChannelCount':sum(len(r['channels']) for r in rows),'lowPixelChannels':sum('LOW_PIXEL_CHANNEL_REVIEW' in c['flags'] for r in rows for c in r['channels']),'rows':rows,'boundary':'File/route checks are not visual/device acceptance. Captured pixels remain approximate; generic damage impact is a separate project reuse channel. No missing original effect is certified.'}
(R/'docs/verification/SKILL_FX_V77_AUDIT.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
with (R/'docs/SKILL_FX_RUNTIME_AUDIT.csv').open('w',newline='') as f:
 cols=['id','name','job','standalonePresentationAllowed','runtimeBranch','sourceCandidates','channels','reviewedSourceWithoutChannel','warnings','originalFourFacingPixels','device','visualAcceptance'];w=csv.DictWriter(f,fieldnames=cols,lineterminator='\n');w.writeheader()
 for r in rows:w.writerow({k:json.dumps(r[k],ensure_ascii=False) if isinstance(r[k],(list,dict)) else r[k] for k in cols})
print('Full catalog',len(rows),'branches',counts,'source channels',out['sourceChannelCount'],'low-pixel review channels',out['lowPixelChannels'])
