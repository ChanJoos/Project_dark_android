#!/usr/bin/env python3
"""Reproducible presentation choices; never changes combat or the source Master."""
import csv, json, shutil
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/src/main/assets/skill-presentation'
OUT.mkdir(parents=True,exist_ok=True)
catalog=json.loads((ROOT/'app/src/main/assets/skills/catalog.json').read_text())
profiles={
 'CAST':{'group':'b','back':[0,1,2],'front':[3,4,5]},
 'PUNCH':{'group':'e','back':[0,1,0],'front':[2,3,2]},
 'THRUST':{'group':'e','back':[8,12,8],'front':[9,15,9]},
 'FRONT_KICK':{'group':'c','back':[0,1,3],'front':[4,5,7]},
 'SIDE_KICK':{'group':'c','back':[14,16,17],'front':[19,21,23]},
 'JUMP_KICK':{'group':'c','back':[24,25,26],'front':[27,28,29]},
 'SWING':{'group':'02','back':[0],'front':[2]},
 'RAISE':{'group':'f','back':[0,1,0],'front':[2,3,2]},
 'IDLE':{'group':'01','back':[0],'front':[1]},
 'EQUIPPED_BASIC':{'group':'02','back':[0],'front':[2]},
}
# These are explicit project pose decisions from visually inspected source frames, not source-code semantics.
def choose(r):
 n=r['name'];effect=r.get('effect','');kind=r['kind']
 if n=='기본공격':motion='EQUIPPED_BASIC'
 elif kind=='마법' or n.startswith(('쿠로','쿠라','쿠랄')):motion='CAST'
 elif any(k in n for k in ['붕각','선풍각','붕신선각']):motion='SIDE_KICK'
 elif '파천각' in n:motion='JUMP_KICK'
 elif '단각' in n:motion='FRONT_KICK'
 elif any(k in n for k in ['찌르','찔러','백스탭','암살']):motion='THRUST'
 elif any(k in n for k in ['블레이드','어택','슬래쉬','크래셔','매드소울','쳐내기']):motion='SWING'
 elif r['job']=='무도가' and any(k in n for k in ['권','펀치','장풍','발경','신공','일음지']):motion='PUNCH'
 elif any(k in n for k in ['방어','보호','모드','집중','포효','하이드','센스']):motion='RAISE'
 else:motion='IDLE'
 # Separate channels, with NO_TARGET an intentional choice for non-target interactions.
 caster='ARCANE';target='NONE';sheet='target';row=-1;anchor='TARGET'
 if motion in ['SWING','THRUST','EQUIPPED_BASIC']:caster='SLASH';target='CUT';row=0
 elif motion in ['PUNCH','FRONT_KICK','SIDE_KICK','JUMP_KICK']:caster='MARTIAL';target='BLUNT';row=1
 if any(k in n for k in ['마레','마네나','아이스']):target='WATER';row=2
 elif any(k in n for k in ['테라미','퀘이크']):target='EARTH';row=3
 elif any(k in n for k in ['아듀','플레쉬스톰']):target='WIND';row=4
 elif any(k in n for k in ['플라','플레어','메테오']):target='FIRE';row=5
 elif any(k in n for k in ['홀리볼트','홀리블로우','홀리쇼크','홀리드래곤']):target='HOLY';row=6
 elif n.startswith(('쿠로','쿠라','쿠랄','수페라쿠','엑스쿠','홀리쿠')) or n in ['수혈','클리멘스']:
  caster='HEAL';target='HEAL';row=7;anchor='RECIPIENT'
 elif n.startswith('디') or n in ['일루메나','일루메룸','리베라토','델리스펠라스','헬푸라']:
  target='DISPEL';sheet='status';row=5
 elif any(k in n for k in ['베노','통배']):target='POISON';sheet='status';row=1
 elif any(k in n for k in ['나르콜','소루마']):target='SLEEP';sheet='status';row=2
 elif any(k in n for k in ['딜루메','일음지']):target='BLIND';sheet='status';row=3
 elif any(k in n for k in ['렌토','렌티아','바르도','바르데아','데프레','프라보','프라베','콘푸','어둠의각인','데스','라그나','아마게돈','숨마']):
  target='CURSE';sheet='status';row=0
 elif any(k in n for k in ['방어','보호','실드','벨라','에나르','이모탈','리플렉','금강','철포삼','반탄','블레스','축복','포트리스','강화','모드']):
  target='PROTECT';sheet='status';row=4;anchor='RECIPIENT'
 elif any(k in n for k in ['로카','아들레스','이형환위','경신','라이트닝','돌진','습격','기습','미종','하이드','하이더','무영']):
  target='TELEPORT';sheet='status';row=6;anchor='RECIPIENT'
 elif n=='소모니아':target='SUMMON';sheet='status';row=7
 if motion=='IDLE' and kind=='기술':caster='NONE';target='NONE';row=-1
 return motion,caster,target,sheet,row,anchor

entries=[]
for r in catalog:
 motion,caster,target,sheet,row,anchor=choose(r)
 # SWING reuses the already accepted four-direction weapon composite. Others use source groups.
 entries.append(dict(id=r['id'],name=r['name'],job=r['job'],kind=r['kind'],motion=motion,
  caster=caster,target=target,targetSheet=sheet,targetRow=row,targetAnchor=anchor,
  poseEvidence='SOURCE_PIXELS + PROJECT_SELECTED_POSE',effectEvidence='ADAPTED_NEW_ART',
  originalEffectVerified=False,combatStatus='EXISTING_RUNTIME_ONLY',
  note='Non-combat skills intentionally retain idle/no target effect.' if motion=='IDLE' else 'Pose selection is a project adaptation; original per-skill animation was not independently verified.'))
frames=list(csv.DictReader((ROOT/'master/data/Asset_Animation_Frame_Master.csv').open(encoding='utf-8-sig')))
manifest={}
for sex in ['mm001','wm001']:
 for r in frames:
  if r['id']!=sex:continue
  path=Path(r['webp']);dest=OUT/'body'/sex/r['group']/path.name
  dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/path,dest)
  key=f"{sex}/{r['group']}/{int(r['frame_index'])}"
  manifest[key]={'path':'body/'+sex+'/'+r['group']+'/'+path.name,'w':int(r['w']),'h':int(r['h']),'pivotX':float(r['px'])*int(r['w']),'offsetY':-float(r['py'])*int(r['h'])}
data={'revision':'PROJECT_ADAPTED_SKILL_PRESENTATION_V1','profiles':profiles,'frames':manifest,'skills':entries}
(OUT/'catalog.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
with (ROOT/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',encoding='utf-8',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(entries[0]),lineterminator="\n");w.writeheader();w.writerows(entries)
print(f'{len(entries)} skill mappings; {len(manifest)} original body frames; no combat unlock changes')
