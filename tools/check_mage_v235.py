"""Review gates for the exact user request, independent of UI rendering assertions."""
from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';S=R/'master/source/mage_v235'
def read(p):return json.loads(p.read_text())
def sha(d):return hashlib.sha256(json.dumps(d,ensure_ascii=False,sort_keys=True,separators=(',',':')).encode()).hexdigest()
c=read(R/'master/changes/V235_MAGE_SOURCES.json');guide=read(S/'guide.json');refs=read(A/'skill-presentation/mage-v235/references.json')['rows'];catalog=read(A/'skills/catalog.json');policy=read(A/'skills/mobile_learning.json')
assert len(guide['rows'])==len(refs)==54
assert sha({k:{f:v for f,v in row.items() if f!='summary'} for k,row in policy.items()})==c['learningPolicyCoreSha256'],'Acquisition gold/items/stats or other policy fields changed'
assert sha([{k:v for k,v in row.items() if k!='effect'} for row in catalog])==c['catalogCoreSha256'],'Catalog outside descriptions changed'
byid={r['id']:r for r in refs}
for row in guide['rows']:
 assert not any(k in row for k in ['gold','items','requirements','prerequisites','stats'])
 target=byid[row['id']];original=R/row['iconPath'];projected=A/'skill-presentation/mage-v235'/target['iconAssetPath']
 assert hashlib.sha256(original.read_bytes()).hexdigest()==row['iconSha256'];assert original.read_bytes()==projected.read_bytes()
 assert policy[row['id']]['summary']==target['description']
 assert next(r for r in catalog if r['id']==row['id'])['effect']==target['description']
assert not set(c['unlistedGuideSkillsRetained']) & set(byid)
classic=read(A/'skill-presentation/classic/manifest.json')['skills']
for ids,indices in [(['001','013','026'],[3,4,5]),(['003','014','027'],[9,10,11]),(['004','015','028'],[15,16,17]),(['005','016','029'],[21,22,23])]:
 for sid,index in zip(ids,indices):
  ch=classic['SK_마법사_'+sid]['channels']['RECIPIENT_CONTACT'];assert [f['index'] for f in ch['sourceFrames']]==[index]
for n in range(39,53):
 ch=classic['SK_마법사_'+str(n).zfill(3)]['channels']['RECIPIENT_CONTACT'];assert ch['sourceFrames']
 assert all((R/f['path']).exists() and hashlib.sha256((R/f['path']).read_bytes()).hexdigest()==f['sha256'] for f in ch['sourceFrames'])
print('PASS:54 byte-exact icons/descriptions; no acquisition imports/numeric edits;12 lower-row source routes;14 retained promotion sources')
