#!/usr/bin/env python3
"""Reproducible presentation choices; never changes combat or the source Master."""
import csv, json, shutil
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/src/main/assets/skill-presentation'
OUT.mkdir(parents=True,exist_ok=True)
catalog=json.loads((ROOT/'app/src/main/assets/skills/catalog.json').read_text())
profiles={
 'CAST':{'group':'f','back':[1,1,0],'front':[3,3,2]},
 'PUNCH':{'group':'e','back':[0,1,0],'front':[2,3,2]},
 'THRUST':{'group':'e','back':[8,10,8],'front':[12,14,12]},
 'THROW':{'group':'d','back':[0,1,2],'front':[3,4,5]},
 'FRONT_KICK':{'group':'c','back':[14,16,18],'front':[19,21,23]},
 'SIDE_KICK':{'group':'c','back':[14,17,18],'front':[19,22,23]},
 'SPIN_KICK':{'group':'c','back':[15,16,17,18],'front':[20,21,22,23]},
 'JUMP_KICK':{'group':'c','back':[24,25,26],'front':[27,28,29]},
 'SWING':{'group':'02','back':[0],'front':[2]},
 'RAISE':{'group':'f','back':[0,1,0],'front':[2,3,2]},
 'IDLE':{'group':'01','back':[0],'front':[1]},
 'EQUIPPED_BASIC':{'group':'02','back':[0],'front':[2]},
}
decisions=list(csv.DictReader((ROOT/'docs/SKILL_ACTION_DECISIONS.csv').open()))
decisions_by_id={r['id']:r for r in decisions}
assert len(decisions_by_id)==len(catalog)==219
assert set(decisions_by_id)=={r['id'] for r in catalog}
entries=[]
for r in catalog:
 decision=decisions_by_id[r['id']]
 motion,caster,target,sheet,row=decision['motion'],decision['caster'],decision['targetFx'],decision['targetSheet'],int(decision['targetRow'])
 anchor='RECIPIENT' if decision['pattern'] in ['SELF','ALLY','GROUP'] else 'TARGET'
 # SWING reuses the already accepted four-direction weapon composite. Others use source groups.
 entries.append(dict(id=r['id'],name=r['name'],job=r['job'],kind=r['kind'],motion=motion,
  caster=caster,target=target,targetSheet=sheet,targetRow=row,targetAnchor=anchor,
  poseEvidence=decision['motionEvidence'],effectEvidence=decision['effectEvidence'],
  originalEffectVerified=False,combatStatus='EXISTING_RUNTIME_ONLY',
  note=decision['note']))
frames=list(csv.DictReader((ROOT/'master/data/Asset_Animation_Frame_Master.csv').open(encoding='utf-8-sig')))
manifest={}
for sex in ['mm001','wm001']:
 for r in frames:
  if r['id']!=sex:continue
  path=Path(r['webp']);dest=OUT/'body'/sex/r['group']/path.name
  dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/path,dest)
  key=f"{sex}/{r['group']}/{int(r['frame_index'])}"
  manifest[key]={'path':'body/'+sex+'/'+r['group']+'/'+path.name,'w':int(r['w']),'h':int(r['h']),'pivotX':float(r['px'])*int(r['w']),'offsetY':-float(r['py'])*int(r['h'])}
data={'revision':'SOURCE_REVIEWED_SKILL_CONTRACT_V65','profiles':profiles,'frames':manifest,'skills':entries}
(OUT/'catalog.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
with (ROOT/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',encoding='utf-8',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(entries[0]),lineterminator="\n");w.writeheader();w.writerows(entries)
data_rows=[]
for r in decisions:
 data_rows.append('    '+json.dumps([r[k] for k in ['id','name','mode','pattern','reach','minReach','mechanic','hits','contact','spatialEvidence','note','kind']],ensure_ascii=False).replace('[','{',1).rsplit(']',1)[0]+'},')
java='package com.projectdark.mobile;\n\n/** Generated from the reviewed per-ID decisions; do not infer rules from skill names. */\nfinal class SkillActionData {\n  static final String[][] ROWS={\n'+'\n'.join(data_rows)+'\n  };\n}\n'
(ROOT/'app/src/main/java/com/projectdark/mobile/SkillActionData.java').write_text(java)
print(f'{len(entries)} reviewed skill mappings; {len(manifest)} unchanged original body frames')
