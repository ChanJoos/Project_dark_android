"""V83 project combat policy; source formulas stay separate from authored coefficients."""
import json,csv
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
cat=json.loads((ROOT/'app/src/main/assets/skills/catalog.json').read_text())
learn=json.loads((ROOT/'app/src/main/assets/skills/mobile_learning.json').read_text())
rules={r['id']:r for r in csv.DictReader((ROOT/'docs/SKILL_ACTION_DECISIONS.csv').open())}
# Semantic IDs, not fuzzy name checks, are exported to Java. Values without evidence are ADAPTED.
statuses={
 'BASIC':['기본공격'], 'DOOR':['문열기','자물쇠열기'], 'ROOT':['바투','포효'], 'CLEAR_ROOT':['디바투'],
 'DRAGON':['드래곤모드'], 'PHOENIX':['피닉스모드'], 'PHYSICAL_GUARD':['완전방어','포트리스'], 'TECH_MAGIC_GUARD':['델리스펠라스'],
 'FOCUS':['집중'], 'DISARM':['적무기쳐내기'], 'TAUNT':['레스큐'],
 'RESET_AGGRO':['아무네지아','콘푸지오'], 'BLIND':['연막탄터뜨리기','딜루메니','일음지'],
 'STEALTH':['하이드','하이더'], 'EVASION':['라이트닝무브','미종보법'], 'ARMOR_BREAK':['적갑옷해체'],
 'POISON':['베노미'], 'DEATH_POISON':['데스'], 'SLEEP':['나르콜리'], 'FREEZE':['소루마'],
 'CURSE20':['렌토','렌티아'], 'CURSE35':['바르도','바르데아'], 'CURSE50':['데프레코','데프레타'], 'CURSE65':['프라보','프라베라'], 'CURSE70':['어둠의각인'],
 'ELEMENT_BOOST':['속성강화'], 'ARMOR20':['매직프로텍션'], 'HIT10':['벨라르모'], 'HIT20':['수페라벨라르모'], 'HIT30':['엑스벨라룸'],
 'DAM4':['에나르마'], 'DAM8':['수페라에나르마'], 'REGEN':['쿠랄툼'], 'SHIELD':['실드'], 'ARMOR10':['콜라마','철포삼'],
 'PROTECT':['호르라마','자기보호'], 'INVINCIBLE':['이모탈','금강불괴'], 'RESCUE':['코마디아'], 'REFLECT_MAGIC':['리플렉토'],
 'MAGIC_GUARD':['칸의축복'], 'BLESS_ALL':['신의축복'], 'CLEAR_ALL':['리베라토'],
 'CLEAR_CURSE20':['디렌토','디렌타'], 'CLEAR_CURSE35':['디바르도','디바르데아'], 'CLEAR_CURSE50':['디데프레카','디데프레타'],
 'CLEAR_CURSE65':['디프라바','디프라베라'], 'CLEAR_CURSE70':['홀리큐어','홀리큐레스'], 'CLEAR_BLIND':['일루메나','일루메룸'],
 'CLEAR_POISON':['디베노모','디베노메라'], 'CLEAR_SLEEP':['디나르콜리','디나르콜룸'], 'CLEAR_FREEZE':['디소루마','디소루메라'],
 'SPEED':['경신공','경신공법'], 'BASIC_POWER':['소수신공'], 'REFLECT_TECH':['반탄신공'],
 'DRAIN':['흡정신공'], 'PUSH':['밀기'], 'LEAP':['이형환위'], 'CHARGE':['돌진'], 'POISON_HIT':['통배권'], 'FREEZE_HIT':['발경'],
 'TRANSFER':['수혈'], 'CHANGE_ELEMENT':['디내추라'], 'REST':['휴식'],
 'INSPECT':['센스몬스터','센스','탐색','아들레스투','품뒤져보기','센서스'],
 'TRAP':['함정파기','설치형트랩'], 'FIND_TRAP':['함정찾기1','함정찾기2'], 'CLEAR_TRAP':['함정해체','상자트랩해체'],
 'TRAVEL':['아지토'], 'STAFF':['유즈스태프'],
}
byname={n:s for s,names in statuses.items() for n in names}
anchors={'크래셔':('CRASH',3.38),'데빌크래셔':('CRASH',6.76),'매드소울':('SOUL',1.5),'매드소울진':('SOUL',1.65),
 '암살격':('ASSASSIN',.6336),'암살격진':('ASSASSIN_PLUS',.95),'세멜리아':('SEMELIA',1.815),'메테오':('METEOR',1.815),
 '라그나로크':('RAGNAROK',.454),'다라밀공':('DARA',3.38),'달마신공':('DALMA',.3),'구양신공':('HP_BURST',.6336),'무영신공':('MP_BURST',4),
 '아마게돈':('MP_BURST',1.05),'숨마스텔라':('MP_BURST',.8),'홀리드래곤':('MP_BURST',.65)}
rows=[]
for e in cat:
 r=rules[e['id']];n=e['name'];c=int(e['circle']) if e['circle'].isdigit() else (7 if '순수' in e['circle'] else 6)
 status=byname.get(n,'');mechanic=r['mechanic'];formula='NONE';coef=0
 kind='DAMAGE' if mechanic=='DAMAGE_ADAPTED_BALANCE' else 'HEAL' if mechanic=='HEAL_ADAPTED_BALANCE' else 'STATUS' if status else 'PASSIVE' if r['mode'] in ('LINKED','PASSIVE') else 'SERVICE'
 if status in ('POISON_HIT','FREEZE_HIT','DRAIN','CHARGE'):kind='DAMAGE'
 if n=='파천각':kind='DAMAGE'
 evidence='PROJECT_ADAPTED_V83';source='User 2026-10-02: author unprovided formulas by learning circle/stat requirements'
 # Physical multipliers grow with circle and acquisition burden; martial balance uses STR/CON geometric mean.
 required=learn[e['id']]['stats'];burden=sum(required.values())-15
 if kind=='DAMAGE':
  formula='MAGIC' if e['kind']=='마법' else 'MARTIAL' if e['job']=='무도가' else 'ROGUE' if e['job']=='도적' else 'PHYSICAL'
  coef=round(.8+c*.27+max(0,burden)*.003,3)
  if r['pattern'] in ('CROSS','TARGET_CROSS','SCREEN','AROUND'):coef=round(coef*.88,3)
  if n=='표창날리기':formula='BASIC';coef=1
  if n in anchors:
   formula,coef=anchors[n]
   if n not in ('매드소울','매드소울진','암살격진','아마게돈','숨마스텔라','홀리드래곤','데빌크래셔'):
    evidence='MASTER_FORMULA_V';source='master/data/Skill_Formula_Research.csv + Skill_Damage_Model.csv'
   elif n in ('매드소울','매드소울진','암살격진','데빌크래셔'):
    evidence='MASTER_ANCHOR_PLUS_USER_PDF';source='User skills PDF: upgrade multiplier/HP condition; classic base anchors retained'
 elif kind=='HEAL':formula='HEAL';coef=round(.55+c*.6+max(0,burden)*.004,3)
 if n=='쿠로토':coef=.55
 if n=='쿠라노토':coef=2.9
 if n=='쿠로':coef=.9
 if n=='쿠라노':coef=1.7
 if n=='쿠라노소':coef=2.4
 cost=0 if e['kind']!='마법' else max(6,round(4+c*c*3+(max(0,burden)*.1)))
 if n=='쿠로토':cost=6
 if n=='금강불괴':cost=200
 allmp=formula in ('SEMELIA','METEOR','RAGNAROK') or n in ('아마게돈','숨마스텔라','홀리드래곤','데스')
 if allmp:cost=0
 cd=round(.8+c*.45,2)
 if n=='쿠로토':cd=2
 if formula=='DARA':cd=12;cost=0
 if formula in ('CRASH','SOUL','ASSASSIN','ASSASSIN_PLUS','DALMA','HP_BURST'):cd=12
 duration=9 if n=='금강불괴' else 18 if n=='이모탈' else 10 if status=='FREEZE_HIT' else 6+c*2
 if kind=='HEAL':desc=f'회복 = round((WIS×5 + INT×1.5 + Lv×2)×{coef}); 최대 HP까지'
 elif kind=='DAMAGE':desc={'PHYSICAL':'(8+STR×3+DAM)','ROGUE':'(8+STR×1.8+DEX×2.2+DAM)','MARTIAL':'(8+sqrt(STR×CON)×4+DAM)','MAGIC':'(8+INT×4+WIS+Lv×1.5)','BASIC':'(8+STR×3+DAM)'}.get(formula,formula)+f' × {coef}'
 else:desc=(e['effect']+f' · {duration}초') if status else ('기본공격/장착 연동' if kind=='PASSIVE' else '대상 서비스가 존재할 때만 사용; 없는 콘텐츠 생성/보상 없음')
 exact={'CRASH':f'최대HP × {coef}; 현재HP ≤ 최대HP의2%', 'SOUL':f'현재HP × {coef}; HP90% 소모', 'ASSASSIN':'현재HP × 0.6336 / 강타25%: ×1.69', 'ASSASSIN_PLUS':'현재HP ×0.95 / 강타25%: ×3.5', 'SEMELIA':'max(0, MP-3240) ×1.815', 'METEOR':'max(0, MP-12960) ×1.815', 'RAGNAROK':'MP ×0.454', 'DARA':'max(0, HP+MP-1440) ×3.38', 'DALMA':'현재HP ×0.30; 방어/속성/무적 무시', 'HP_BURST':f'현재HP ×{coef}', 'MP_BURST':f'MP ×{coef}'}
 if formula in exact:desc=exact[formula]
 rows.append(dict(id=e['id'],name=n,job=e['job'],circle=c,kind=kind,formula=formula,coefficient=coef,status=status,mpCost=cost,cooldown=cd,duration=duration,allMp=allmp,evidence=evidence,source=source,description=desc))
(ROOT/'app/src/main/assets/skills/abilities.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
fields=list(rows[0]);p=ROOT/'docs/SKILL_ABILITY_FORMULAS.csv'
with p.open('w',newline='') as f:
 w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)
keys=['id','name','job','circle','kind','formula','coefficient','status','mpCost','cooldown','duration','allMp','evidence','description']
lines=['package com.projectdark.mobile;','/** Generated by tools/generate_skill_abilities.py; per-ID source and adapted policies. */','final class SkillAbilityData {',' static final String[][] ROWS={']
for r in rows:lines.append('  {'+','.join(json.dumps(str(r[k]).lower() if isinstance(r[k],bool) else str(r[k]),ensure_ascii=False) for k in keys)+'},')
lines+=[' };','}'];(ROOT/'app/src/main/java/com/projectdark/mobile/SkillAbilityData.java').write_text('\n'.join(lines)+'\n')
from collections import Counter
print(len(rows),Counter(r['kind'] for r in rows))
