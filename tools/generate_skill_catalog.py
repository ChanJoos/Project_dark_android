"""Deterministic read-only UI projection; no combat formulas or acquisition defaults."""
import csv, json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
def rows(name):
    with (root/'master/data'/f'{name}.csv').open(encoding='utf-8-sig',newline='') as f: return list(csv.DictReader(f))
def pairs(row, keys): return ' · '.join(k+' '+row[k] for k in keys if row.get(k))
requirements={r['Skill_ID']:r for r in rows('Skill_Requirements')}
legacy={(r['Job'],r['Skill_Name']):r for r in rows('Skill_Legacy_Requirements')}
entries=[]
for r in rows('Skill_Master'):
    if r['Runtime_Inclusion']=='EXCLUDE': continue
    q=requirements.get(r['Skill_ID'],{})
    old=legacy.get((r['직업'],r['스킬명']),{})
    entries.append(dict(id=r['Skill_ID'],name=r['스킬명'],job=r['직업'],stage=r['단계'],kind=r['종류'],circle=r['서클'],effect=r['핵심효과'] or '효과 미확정',target=r['대상'] or '미확정',range=r['사거리/범위'] or '미확정',resource=(r['소모자원']+' '+r['소모량']).strip() or '미확정',limit=r['발동조건/제한'] or '미확정',requirements=pairs(q,['요구Lv','STR','INT','WIS','CON','DEX','선행스킬','필요아이템','Gold','숙련조건']) or '습득 조건 미확정',requirementValues={k:q.get(k,'') for k in ['요구Lv','STR','INT','WIS','CON','DEX']},requirementStatus=q.get('상태','확인 필요'),legacy=pairs(old,['Legacy_Circle','STR','INT','WIS','CON','DEX','Prerequisite_Skill','Required_Prerequisite_Level']),evidence=r['Effect_Evidence'] or r['Evidence'],source=r['Detail_Source_URL'] or r['Source_URL']))
p=root/'app/src/main/assets/skills/catalog.json';p.parent.mkdir(parents=True,exist_ok=True)
p.write_text(json.dumps(entries,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'{len(entries)} skill catalog entries')
