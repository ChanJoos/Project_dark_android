"""Field receipts for exact identities only. No original values inferred from appearances."""
from pathlib import Path
import json,re,hashlib
from bs4 import BeautifulSoup
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/equipment/functions_v129'
def article(p):
 s=BeautifulSoup(p.read_bytes(),'html.parser');t=s.get_text('\n',strip=True);a=t.find('현자의 마을');b=t.find('페이스북',a);return t[a:b] if a>=0 else t
sources=[]
for p in sorted(SRC.glob('*.html')):
 if p.stem.split('_')[1] not in {'244','268','270','276','3747','3578','1878','1340','1414','4933'}:continue
 sources.append(dict(path=str(p.relative_to(ROOT)),sha256=hashlib.sha256(p.read_bytes()).hexdigest(),url=('https://lod.nexon.com/News/update/4933' if p.stem=='nexon_4933' else 'https://lod.nexon.com/community/game/'+p.stem.split('_')[1]+'?SearchBoard=1'),authority=('NEXON_OFFICIAL_UPDATE_O' if p.stem=='nexon_4933' else 'NEXON_HOSTED_COMMUNITY_V_HISTORICAL'),retrieved='2026-10-10'))
(SRC/'provenance.json').write_text(json.dumps(sources,ensure_ascii=False,indent=2)+'\n')
cat=json.loads((ROOT/'master/source/equipment/identity_v128/catalog.json').read_text());names={r['itemId']:r['name'] for r in cat['additions']};names.update({i:r['name'] for i,r in cat['existing'].items()})
receipts={}
for i in [244,268,269,270]:
 p=SRC/f'nexon_{i}.html' if i!=269 else ROOT/'master/source/equipment/nexon_269.html';t=article(p);t=t[t.find('일반 아이템(Set 1)'):t.find('특수 아이템(Set 2)')]
 for m in re.finditer(r'([^\n:]+?)\s*:\s*Ac\s*(-\d+),\s*레벨\s*(\d+)\s*이상',t,re.I):
  for name in m[1].split('/'):
   name=name.strip();matches=[id for id,n in names.items() if n==name and id.startswith('IT_WARDROBE_')]
   for id in matches:receipts[id]=dict(name=name,stats={'AC':int(m[2])},level=int(m[3]),source=str(p.relative_to(ROOT)),excerpt=m[0].strip(),status='PARTIAL_NUMERIC_VERIFIED')
# Exact promotion hats from retained labelled male table; original promotion requirement remains pending.
hats={'헬름':{'AC':-12,'HP':400,'MP':500,'MAGIC_DEFENSE':1},'루크':{'AC':-8,'HP':800,'MP':200,'MAGIC_DEFENSE':1},'마르두크':{'AC':-7,'HP':300,'MP':600,'MAGIC_DEFENSE':1},'아가트':{'AC':-7,'HP':300,'MP':-500,'MAGIC_DEFENSE':1}}
for id,name in names.items():
 if id.startswith('IT_WARDROBE_') and name in hats:receipts[id]=dict(name=name,stats=hats[name],level=None,source='master/source/equipment/nexon_986.html',status='PROMOTION_RESTRICTION_AND_MDEF_UNIT_PENDING')
# Preserve/regenerate accessory originals separately; add reviewed missing special fields with uncertainty kept.
accessories=json.loads((ROOT/'master/canonical/Accessory_Catalog.json').read_text());extra={};notes={}
for r in accessories:
 comment=r.get('unprojectedComment','');m=re.search(r'再生力\s*\+\s*(\d+)(?![\d?])',comment)
 if m and '?' not in comment:
  extra[r['itemId']]={'REGEN':int(m[1])};notes[r['itemId']]='재생력은 HP 추가 회복에 적용됩니다. 원작 공식 충돌로 시험식을 사용합니다.'
 if '経験値' in comment:notes[r['itemId']]='경험치 증가·시간별 내구 소모는 원작 규칙 미확정으로 적용되지 않습니다.'
 # Preserve all remaining original comments in audit, including durability/trade/skill materials.
lines=['package com.projectdark.mobile;','import java.util.*;','/** Generated from retained per-field source receipts. Unknown original fields stay pending. */','final class SourceItemFunctions {',' static void install(RpgProgressionState r){']
for id,v in receipts.items():
 kv=','.join(json.dumps(k)+','+str(n) for k,n in v['stats'].items());lines.append(f'  r.fillSourceOptions("{id}",stats({kv}),'+('null' if v['level'] is None else str(v['level']))+');')
staff_levels={name:(41 if name=='아리펠스탭' else 11) for name in ['매직파나','매직루나','매직마르시아','매직새티아','매직스태프','매직쥬피티아','매직가이아','매직솔라','아리펠스탭','홀리머큐리아']}
level_receipts={}
for id,name in names.items():
 if name in staff_levels:
  level_receipts[id]=dict(name=name,level=staff_levels[name],source='master/source/equipment/functions_v129/nexon_3578.html',status='LEVEL_ONLY_OTHER_REQUIREMENTS_PENDING')
  lines.append(f'  r.fillSourceLevel("{id}",{staff_levels[name]});')
for id,v in extra.items():lines.append(f'  r.addSourceOptions("{id}",stats("REGEN",{v["REGEN"]}));')
lines+=[' }',' static String unresolved(String id){switch(id){']
for id,n in notes.items():lines.append(' case '+json.dumps(id)+':return '+json.dumps(n,ensure_ascii=False)+';')
lines+=[' default:return "";}}',' private static Map<String,Integer> stats(Object... kv){Map<String,Integer> m=new LinkedHashMap<>();for(int i=0;i<kv.length;i+=2)m.put((String)kv[i],(Integer)kv[i+1]);return m;}','}']
(ROOT/'app/src/main/java/com/projectdark/mobile/SourceItemFunctions.java').write_text('\n'.join(lines)+'\n')
(SRC/'numeric_receipts.json').write_text(json.dumps(receipts,ensure_ascii=False,indent=2)+'\n')
(SRC/'special_receipts.json').write_text(json.dumps({'staffLevelReceipts':level_receipts,'accessoryRegeneration':extra,'warnings':notes,'regenerationFormula':'PROJECT_ADAPTED: additional HP every25s = floor(baseMaxHP * REGEN /1000); no MP; source1878 internally inconsistent, not original-certification','staffTimingSource':'nexon_3578.html','panaMpSource':'nexon_276.html','consumableSource':'nexon_3747.html'},ensure_ascii=False,indent=2)+'\n')
print('EXACT_NUMERIC_OPTIONS',len(receipts),'REGEN_OPTIONS',len(extra))
