"""Final accepted circle3/caster-cross overlay. Preserve all unrelated rows and source guides."""
from pathlib import Path
import json,csv,re
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';p=json.loads((R/'master/changes/V243_CIRCLE3_CROSS.json').read_text());assert p['status']=='ACCEPTED'
policy={r['id']:r for r in p['rows']}
def save(path,value):path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n')
f=A/'skills/abilities.json';abilities=json.loads(f.read_text());byid={a['id']:a for a in abilities}
for sid,row in policy.items():
 a=byid[sid];single=byid[row['single']];assert a['circle']==row['circle'] and a['formula']==single['formula']=='MAGIC'
 a.update(coefficient=single['coefficient'],description=single['description']+' · 시전자 사방 한 칸의 각 적에게 1회',evidence='USER_V243_SAME_CIRCLE_SINGLE_POWER_PROJECT_FORMULA',source='master/changes/V243_CIRCLE3_CROSS.json')
save(f,abilities)
with (R/'docs/SKILL_ABILITY_FORMULAS.csv').open('w',newline='') as out:
 w=csv.DictWriter(out,fieldnames=list(abilities[0]),lineterminator='\n');w.writeheader();w.writerows(abilities)
# Rewrite only affected generated Java rows; retained generated data are authoritative.
f=R/'app/src/main/java/com/projectdark/mobile/SkillAbilityData.java';text=f.read_text();keys=['id','name','job','circle','kind','formula','coefficient','status','mpCost','cooldown','duration','allMp','evidence','description','cooldownEvidence','cooldownSource']
for sid in policy:
 a=byid[sid];line='  {'+','.join(json.dumps(str(a[k]).lower() if isinstance(a[k],bool) else str(a[k]),ensure_ascii=False) for k in keys)+'},'
 text,count=re.subn(r'^  \{"'+re.escape(sid)+r'".*$',lambda _:line,text,flags=re.M);assert count==1
f.write_text(text)
f=R/'docs/SKILL_ACTION_DECISIONS.csv'
with f.open() as src:reader=csv.DictReader(src);fields=reader.fieldnames;rules=list(reader)
for r in rules:
 if r['id'] in policy:r.update(pattern='CROSS',reach='1',minReach='0',spatialEvidence='USER_V243_CASTER_FOUR_ADJACENT_TILES',spatialSource='master/changes/V243_CIRCLE3_CROSS.json',note='시전자 기준 인접 사방 한 칸. 대각선·중앙·두 칸 밖 제외. 같은 써클 단일마법의 속성/이펙트/기본공격력으로 각 적 1회; MP 시전당 1회.')
with f.open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');w.writeheader();w.writerows(rules)
f=R/'app/src/main/java/com/projectdark/mobile/SkillActionData.java';text=f.read_text();keys=['id','name','mode','pattern','reach','minReach','mechanic','hits','contact','spatialEvidence','note','kind']
for r in rules:
 if r['id'] not in policy:continue
 line='    {'+', '.join(json.dumps(r[k],ensure_ascii=False) for k in keys)+'},';text,count=re.subn(r'^    \{"'+re.escape(r['id'])+r'".*$',lambda _:line,text,flags=re.M);assert count==1
f.write_text(text)
f=A/'skill-presentation/mage-v235/references.json';refs=json.loads(f.read_text());summaries={}
for r in refs['rows']:
 if r['id'] not in policy:continue
 q=policy[r['id']];element={'WATER':'수','EARTH':'토','WIND':'풍','FIRE':'화'}[q['element']]
 r['description']=f"시전자 주변 사방 한 칸의 적에게 {element} 속성 마법 피해를 줍니다. {byid[q['single']]['name']}와 같은 이펙트·기본공격력이며 대각선은 제외합니다.";summaries[r['id']]=r['description']
save(f,refs)
f=A/'skills/catalog.json';rows=json.loads(f.read_text())
for r in rows:
 if r['id'] in summaries:r['effect']=summaries[r['id']]
save(f,rows)
f=A/'skills/mobile_learning.json';rows=json.loads(f.read_text())
for sid,summary in summaries.items():rows[sid]['summary']=summary
save(f,rows)
print('V243 eight caster crosses/paired single coefficients/live descriptions; MP/cadence/acquisition unchanged')
