"""User-selected Mage guide presentation only; preserve all acquisition/runtime numbers."""
from pathlib import Path
import json,hashlib,shutil
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';O=A/'skill-presentation/mage-v235';O.mkdir(exist_ok=True);(O/'icons').mkdir(exist_ok=True)
def save(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
source=json.loads((R/'master/source/mage_v235/guide.json').read_text());refs=[]
contract=json.loads((R/'master/changes/V235_MAGE_SOURCES.json').read_text());aliases=contract['screenFxAliases']
names={r['id']:r['name'] for r in json.loads((A/'skills/catalog.json').read_text())}
for row in source['rows']:
 p=R/row['iconPath'];assert hashlib.sha256(p.read_bytes()).hexdigest()==row['iconSha256']
 path='icons/'+row['id'].rsplit('_',1)[-1]+'.png';shutil.copyfile(p,O/path)
 description=' '.join(row['description'].split())
 if row['id'] in aliases:
  element={'030':'수','031':'토','032':'풍','033':'화'}[row['id'].rsplit('_',1)[-1]]
  description=f"{element} 속성 화면 전체 공격 마법. 화면 안의 모든 몬스터에게 {names[aliases[row['id']]]} 이펙트와 피해를 가합니다."
 refs.append(dict(id=row['id'],name=row['name'],description=description,sourceDescription=row['description'],iconAssetPath=path,sourceIconPath=row['iconPath'],iconSha256=row['iconSha256'],sourceTable=row['sourceTable'],sourceIconIndex=row['sourceIconIndex']))
save(O/'references.json',dict(revision='V235_MAGE_54_SOURCE_ICONS_DESCRIPTIONS_ONLY',article=source['article'],rows=refs))
byid={r['id']:r for r in refs}
p=A/'skills/catalog.json';catalog=json.loads(p.read_text())
for row in catalog:
 if row['id'] in byid:row['effect']=byid[row['id']]['description']
save(p,catalog)
p=A/'skills/mobile_learning.json';policy=json.loads(p.read_text())
for sid,row in byid.items():policy[sid]['summary']=row['description']
save(p,policy)
classic=json.loads((A/'skill-presentation/classic/manifest.json').read_text())['skills'];coverage=[]
for sid in list(aliases.values())+['SK_마법사_'+str(n).zfill(3) for n in range(39,53)]:
 ch=classic[sid]['channels']['RECIPIENT_CONTACT'];assert ch['sourceFrames']
 coverage.append(dict(id=sid,sourceFrames=ch['sourceFrames'],atlasSha256=ch['atlasSha256'],timing=ch.get('sourceTiming','PROJECT_AUTHORED')))
save(O/'coverage.json',dict(screenFxAliases=aliases,preservedSourceRoutes=coverage,lowerRowOnly={'수':[3,4,5],'토':[9,10,11],'풍':[15,16,17],'화':[21,22,23]},guideUnlistedPreserved=contract['unlistedGuideSkillsRetained']))
def sha(d):return hashlib.sha256(json.dumps(d,ensure_ascii=False,sort_keys=True,separators=(',',':')).encode()).hexdigest()
assert sha({k:{f:v for f,v in row.items() if f!='summary'} for k,row in policy.items()})==contract['learningPolicyCoreSha256'],'Acquisition fields changed'
assert sha([{k:v for k,v in row.items() if k!='effect'} for row in catalog])==contract['catalogCoreSha256'],'Catalog outside description changed'
print('V235:54 source icons/descriptions;4 screen source aliases;14 promotion routes audited;acquisition numbers unchanged')
