"""V236 user Amnesia behavior and explicitly adapted role-specific rogue balance."""
from pathlib import Path
import json,csv
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';p=json.loads((R/'master/changes/V236_ROGUE_AMNESIA_DAMAGE.json').read_text());assert p['status']=='ACCEPTED'
f=A/'skills/abilities.json';rows=json.loads(f.read_text())
for r in rows:
 if r['id'] in p['damage']['coefficients']:
  r.update(coefficient=p['damage']['coefficients'][r['id']],evidence='PROJECT_ADAPTED_V236_NOT_ORIGINAL_COEFFICIENTS',source='master/changes/V236_ROGUE_AMNESIA_DAMAGE.json')
  r['description']='(무기 공격+STR×1.8+DEX×2.2+DAM) × '+str(r['coefficient'])+' · 프로젝트 조정값'
 if r['id']=='SK_도적_005':r.update(duration=10,description='전방 직선 1~4칸 최근접 몬스터의 인식을 초기화. 10초간 비선공, 받는 피해 2배. 피격되어도 유지.',evidence='USER_V236_DURATION_ADAPTED',source='User20261022; duration existing project10s')
f.write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
with (R/'docs/SKILL_ABILITY_FORMULAS.csv').open('w',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(rows[0]),lineterminator='\n');w.writeheader();w.writerows(rows)
keys=['id','name','job','circle','kind','formula','coefficient','status','mpCost','cooldown','duration','allMp','evidence','description','cooldownEvidence','cooldownSource'];lines=['package com.projectdark.mobile;','/** Generated per-ID abilities with accepted source overlays. */','final class SkillAbilityData {',' static final String[][] ROWS={']
for r in rows:lines.append('  {'+','.join(json.dumps(str(r[k]).lower() if isinstance(r[k],bool) else str(r[k]),ensure_ascii=False) for k in keys)+'},')
lines+=[' };','}'];(R/'app/src/main/java/com/projectdark/mobile/SkillAbilityData.java').write_text('\n'.join(lines)+'\n')
f=R/'docs/SKILL_ACTION_DECISIONS.csv'
with f.open() as src:reader=csv.DictReader(src);fields=reader.fieldnames;rules=list(reader)
for r in rules:
 if r['id']=='SK_도적_005':r.update(minReach='1',reach='4',spatialEvidence='USER_V236_SAME_RAY_AS_SHURIKEN',note='현재 바라보는 직선1~4칸 최근접 단일 몬스터. 접근/회전 없음.10초 비선공 및 받는피해2배(지속시간은프로젝트값).')
with f.open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');w.writeheader();w.writerows(rules)
keys=['id','name','mode','pattern','reach','minReach','mechanic','hits','contact','spatialEvidence','note','kind'];lines=['package com.projectdark.mobile;','','/** Generated reviewed action decisions. */','final class SkillActionData {','  static final String[][] ROWS={']
for r in rules:lines.append('    {'+', '.join(json.dumps(str(r[k]),ensure_ascii=False) for k in keys)+'},')
lines+=[' };','}'];(R/'app/src/main/java/com/projectdark/mobile/SkillActionData.java').write_text('\n'.join(lines)+'\n')
print('V236 Amnesia ray1..4,10s passive andx2; ten distinct adapted rogue coefficients')
