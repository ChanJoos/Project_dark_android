"""Check every admitted name/icon/wearable identity and retain source gaps explicitly."""
from pathlib import Path
import json,hashlib,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/equipment/identity_v128';j=json.loads((SRC/'catalog.json').read_text());m=json.loads((ROOT/'app/src/main/assets/item-icons/manifest.json').read_text())['items']
with zipfile.ZipFile(SRC/'generated_assets.zip') as bundle:
 for name in bundle.namelist():
  dest=ROOT/'app/src/main/assets'/name
  if not dest.exists():
   dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(bundle.read(name))
for id,r in j['existing'].items():
 if id not in m:continue
 assert m[id]['appearanceId']==r['appearanceId'],id
for r in j['additions']:
 assert m[r['itemId']]['appearanceId']==r['appearanceId']
 assert r['name']==j['icons'][r['appearanceId']]['name']
 assert r['requiredLevel'] is None and not r['statModifiers']
retrieval=json.loads((SRC/'retrieval.json').read_text());assert not retrieval['failures']
with zipfile.ZipFile(SRC/'original_wearables.zip') as z:
 assert z.testzip() is None
 for r in retrieval['sources']:
  raw=z.read(r['archiveMember']);assert hashlib.sha256(raw).hexdigest()==r['sha256'];assert (ROOT/'app/src/main/assets'/r['assetPath']).read_bytes()==raw
for a,r in j['icons'].items():
 meta=json.loads((ROOT/'app/src/main/assets/source-registration'/f'{a}.json').read_text());atlas=Image.open(ROOT/'app/src/main/assets/wearable-atlases'/f'{a}.webp')
 if a in j['sourceFrameGaps']:continue
 assert len(meta['sprites']['01'])==10 and len(meta['sprites']['02'])>=4,(a,meta['sprites'].keys())
 for group in ['01','02']:
  for f in meta['sprites'][group]:assert 0<=f['sx']<f['sx']+f['w']<=atlas.width and 0<=f['sy']<f['sy']+f['h']<=atlas.height,(a,f)
assert '2.5f' not in (ROOT/'app/src/main/java/com/projectdark/mobile/SourceItemIconRegistry.java').read_text()
print('IDENTITY_V128_PASS',len(m),'items',len(j['icons']),'exact wearable identities',len(j['additions']),'added; source gaps explicit')
