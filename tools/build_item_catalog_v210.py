"""One identity per original name/slot, explicit legacy aliases; never dedupe by pixels.
Retained named native previews + already admitted article sources. No invented stats/poses.
"""
from pathlib import Path
from PIL import Image
import csv,json,re,hashlib,zipfile,io,math,collections
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'master/source/items/catalog_v210';APP=ROOT/'app/src/main/assets';OUT.mkdir(exist_ok=True,parents=True)
q=lambda s:json.dumps(s,ensure_ascii=False);norm=lambda s:re.sub(r'\s+','',s or '')
master=json.loads((ROOT/'master/canonical/User_Item_Master_20261018.json').read_text())['records'];byname=collections.defaultdict(list)
for r in master:byname[norm(r['displayName'])].append(r)
manifest=json.loads((APP/'item-icons/manifest.json').read_text())['items']
# Current admitted names/slots, including constructor constants and post-construction identity corrections.
java=ROOT/'app/src/main/java/com/projectdark/mobile';defs={}
for f in java.glob('*.java'):
 if f.name in ['UnifiedItemCatalogV210.java','EquipmentIdentityV210.java']:continue
 s=f.read_text();const=dict(re.findall(r'(?:String|final String)\s+(\w+)\s*=\s*"([^"]+)"',s));const.update({'WEAPON_SLOT':'무기','ARMOR_SLOT':'갑옷','HEAD_SLOT':'모자','SHOES_SLOT':'신발','SHIELD_SLOT':'방패','STARTER_SHIRT_ITEM_ID':'IT_APPEARANCE_PEASANT_SHIRT','PLAYTEST_WEAPON_ITEM_ID':'IT_ADAPTED_PLAYTEST_MOKDO','STARTER_HAT_ITEM_ID':'IT_ADAPTED_STARTER_HAT','STARTER_SHIELD_ITEM_ID':'IT_ADAPTED_STARTER_SHIELD','REFERENCE_LEOPARD_ITEM_ID':'IT_REFERENCE_LEOPARD','REFERENCE_HELM_ITEM_ID':'IT_REFERENCE_HELM','CHUNGRYONG_ITEM_ID':'IT_WEAPON_CHUNGRYONG'})
 for m in re.finditer(r'new (?:RpgProgressionState\.)?ItemDefinition\(\s*("[^"]+"|\w+)\s*,\s*"([^"]+)"\s*,\s*([^,]+),\s*([^,]+),',s):
  ident=json.loads(m[1]) if m[1].startswith('"') else const.get(m[1]);slot=json.loads(m[3]) if m[3].startswith('"') else const.get(m[3].strip());a=json.loads(m[4]) if m[4].startswith('"') else None
  if ident and ident in manifest:defs[ident]=dict(id=ident,name=m[2],slot=slot,appearance=a)
identity=json.loads((ROOT/'master/source/equipment/identity_v128/catalog.json').read_text())
for ident,r in identity['existing'].items():
 if ident in manifest:defs[ident]=dict(id=ident,name=r['name']+(' · 수련' if r['adaptedVariant'] else ''),slot={'mw':'무기','mu':'갑옷','mh':'모자','ml':'신발','ms':'방패'}.get(r['appearanceId'][:2]),appearance=r['appearanceId'])
for job in ['WARRIOR','ROGUE','MAGE','CLERIC','MARTIAL_ARTIST']:
 for lv in [11,26,41,56,65]:
  ident=f'IT_B_CAMPAIGN_TOOL_{job}_{lv}'
  if ident in manifest and job=='MARTIAL_ARTIST':defs[ident]=dict(id=ident,name='가죽장갑 · 수련' if lv==11 else '금장갑 · 수련',slot='장갑',appearance=None)
# Fill named native source inventory identities. Same-name gender/source variants share identity, not stats.
native=collections.defaultdict(list);excluded=[]
for r in csv.DictReader((ROOT/'master/data/Asset_Master.csv').open(encoding='utf-8-sig')):
 if r['type'] not in ['weapon','armor','hair','shield','shoes','back','cape']:continue
 if r['full_audit_status']!='SOURCE_NAMED' or r['name'] in ['', 'UNNAMED']:
  excluded.append(dict(sourceAssetId=r['id'],name=r['name'],reason='ORIGINAL_NAME_UNRESOLVED'));continue
 slot={'weapon':'무기','armor':'갑옷','hair':'모자','shield':'방패','shoes':'신발','back':'장식','cape':'장식'}[r['type']]
 native[(norm(r['name']),slot)].append(r)
# All source names remain in coverage; only exactly matched user-master identities with stats are new gameplay records.
canon={};alias={};records=[];pending=[]
for key,rs in sorted(native.items()):
 name,slot=key;existing=[d for d in defs.values() if norm(d['name'])==name and d['slot']==slot and '·수련' not in norm(d['name'])];matches=byname.get(name,[])
 if len(matches)>1:pending.append(dict(name=name,reason='AMBIGUOUS_USER_ROWS'));continue
 row=matches[0] if matches else None
 target=row['itemId'] if row else (sorted(existing,key=lambda d:d['id'])[0]['id'] if existing else 'IT_NATIVE_V210_'+hashlib.sha256((slot+'|'+name).encode()).hexdigest()[:16].upper())
 chosen=sorted(rs,key=lambda r:(not r['id'].lower().startswith('m'),r['id'].lower()))[0]
 # Prefer already-reviewed dedicated inventory art / active wearable identity over a generic preview.
 art=manifest.get(target) or next((manifest[d['id']] for d in existing if d['id'] in manifest),None)
 appearance=next((d['appearance'] for d in existing if d['appearance']),None)
 cells=row['cells'] if row else None
 stats={k:int(cells[i]) for k,i in [('AC',9),('HIT',10),('DAM',11),('HP',12),('MP',13),('STR',14),('DEX',15),('INT',16),('WIS',17),('CON',18),('REGEN',22),('MAGIC_DEFENSE',23)] if isinstance(cells[i],(int,float))} if cells else {}
 if cells:
  for col,lo,hi in [(24,'MinATK','MaxATK'),(25,'LMinATK','LMaxATK')]:
   mm=re.fullmatch(r'(\d+)m(\d+)',str(cells[col]))
   if mm:stats.update({lo:int(mm[1]),hi:int(mm[2])})
 rec=dict(itemId=target,name=row['displayName'] if row else chosen['name'],slot=slot,appearanceId=appearance,stats=stats,level=cells[21] if cells and isinstance(cells[21],int) else None,sourceRow=row['sourceRow'] if row else None,sources=[dict(id=r['id'],path=r['webp'],sha256=hashlib.sha256((ROOT/r['webp']).read_bytes()).hexdigest(),nameEvidence=r['name_evidence_source']) for r in rs],art=art,previewPath=chosen['webp'],inventoryOnly=not existing and target not in defs,evidence='U' if row else 'PENDING')
 canon[key]=rec;records.append(rec)
 for d in existing:
  if d['id']!=target:alias[d['id']]=target
# Original accessory/article identities not in wearable previews: exact original name + same equipment slot only.
groups=collections.defaultdict(list)
for d in defs.values():
 if d['slot'] and not d['name'].endswith(' · 수련') and not d['name'].startswith('이름 미확인'):groups[(norm(d['name']),d['slot'])].append(d)
for key,ds in groups.items():
 if key in canon:continue
 users=[d for d in ds if d['id'].startswith('USER_ITEM_')];target=sorted(users or ds,key=lambda d:d['id'])[0]['id']
 # Source author identity must agree; no grouping by asset hash/shape/color or unrelated deity names.
 for d in ds:
  if d['id']!=target and (d['appearance']==next(x['appearance']for x in ds if x['id']==target) or not d['appearance']):alias[d['id']]=target
# Retire our synthetic training/campaign variants by explicit name/appearance, never by icon equality.
for d in defs.values():
 if '·수련' not in norm(d['name']):continue
 key=(norm(d['name'].replace(' · 수련','')),d['slot']);target=canon.get(key,{}).get('itemId')
 if not target:
  candidates=groups.get(key,[]);target=sorted(candidates,key=lambda x:(not x['id'].startswith('USER_ITEM_'),x['id']))[0]['id'] if candidates else None
 if target:alias[d['id']]=target
alias['IT_B_POTE_SHOES']='IT_TEST_SHOES_ML230'
# Chains always resolve directly to the authoritative identity.
for ident in list(alias):
 target=alias[ident];seen={ident}
 while target in alias:
  assert target not in seen;seen.add(target);target=alias[target]
 alias[ident]=target
# Build small byte-unique atlas cells from immutable sources. Full original sources remain in their archives.
assetBytes={}
for folder in ['master/source/equipment/identity_v128','master/source/equipment/horror_v184','master/source/items/naver_bundle_20261019','master/source/items/rapid_v198']:
 for zf in sorted((ROOT/folder).glob('generated*.zip')):
  with zipfile.ZipFile(zf) as z:
   for n in z.namelist():
    if n.endswith(('.png','.jpg','.webp','.json')):assetBytes.setdefault(n,z.read(n))
def readasset(path):
 p=APP/path
 if p.exists():return p.read_bytes()
 if path in assetBytes:return assetBytes[path]
 raise FileNotFoundError(path)
def image(path):return Image.open(io.BytesIO(readasset(path))).convert('RGBA')
# Existing Java source color masks, reused for socket captures only.
s=(java/'InventoryArtworkV199.java').read_text();pal=[]
for tag in ['WOOD','FRAME']:
 pal.append([tuple(int(h[i:i+2],16)for i in [0,2,4])for h in re.findall(r'0x([a-f0-9]{6})',s.split('int[] '+tag+'=')[1].split(';')[0])])
def clean(im):
 im=im.copy();w,h=im.size
 for y in range(h):
  for x in range(w):
   c=im.getpixel((x,y));edge=x<5 or x>=w-5 or y<5 or y>=h-5
   if any(max(abs(c[i]-p[i])for i in range(3))<=10 for p in pal[0]+(pal[1] if edge else [])):im.putpixel((x,y),(0,0,0,0))
 return im
allart=dict(manifest)
for rec in records:
 if rec['itemId'] not in allart:allart[rec['itemId']]=rec['art'] or dict(nativePreview=rec['previewPath'],sourcePath=rec['previewPath'],projection='EXACT_SOURCE_NAMED_PREVIEW')
for ident,target in alias.items():
 if target in allart:allart[ident]=allart[target]
texture={};bindings={};raws=[];unavailable=[]
for ident,row in sorted(allart.items()):
 if row.get('identityMatch')=='PENDING_REPLACEMENT_SOURCE':unavailable.append(ident);continue
 try:
  if row.get('nativePreview'):im=Image.open(ROOT/row['nativePreview']).convert('RGBA')
  else:
   im=image(row['assetPath']);a=row.get('appearanceId','').lower()
   if a.startswith(('mu','wu')) and 'IDLE' in row.get('projection',''):
    try:
     j=json.loads(readasset('source-registration/'+a+'.json'));f=next(x for x in j['sprites']['01'] if x['f']==1);im=image('wearable-atlases/'+a+'.webp').crop((f['sx'],f['sy'],f['sx']+f['w'],f['sy']+f['h']))
    except (FileNotFoundError,StopIteration):pass
   if 'BACKGROUND_PRESERVED' in row.get('projection','') or row.get('projection')=='ORIGINAL_ICON_SOCKET_BYTES':im=clean(im)
  box=im.getchannel('A').getbbox();assert box,ident;im=im.crop(box)
  if max(im.size)>60:im.thumbnail((60,60),Image.Resampling.NEAREST)
  # Transparent RGB is not visible artwork and must not prevent texture sharing.
  px=list(im.getdata());im.putdata([p if p[3] else (0,0,0,0)for p in px]);key=hashlib.sha256(str(im.size).encode()+im.tobytes()).hexdigest()
  if key not in texture:texture[key]=len(raws);raws.append(im)
  n=texture[key];bindings[ident]=dict(texture=key,page=n//256,x=n%16*64,y=n%256//16*64,w=im.width,h=im.height,source=row.get('sourcePath') or row.get('assetPath'),sourceAssetSha256=row.get('assetSha256'))
 except (FileNotFoundError,AssertionError) as e:unavailable.append(dict(id=ident,error=str(e)))
# Cell dimensions retain native art ≤60px; original source RGB not painted. Rotation remains shared runtime weapon display.
pack=APP/'item-atlas-v210';pack.mkdir(exist_ok=True)
for p in pack.glob('page-*.png'):p.unlink()
for page in range(math.ceil(len(raws)/256)):
 sheet=Image.new('RGBA',(1024,1024))
 for n,im in enumerate(raws[page*256:(page+1)*256]):sheet.paste(im,(n%16*64,n//16*64))
 sheet.save(pack/f'page-{page:02}.png')
(pack/'manifest.json').write_text(json.dumps(dict(revision='V210_IDENTITY_NOT_IMAGE_DEDUP',items=bindings,textureCount=len(raws),pages=math.ceil(len(raws)/256),cell=64,unavailable=unavailable),ensure_ascii=False,separators=(',',':'))+'\n')
(pack/'new-sources.json').write_text(json.dumps({r['itemId']:dict(itemId=r['itemId'],sourcePath=r['previewPath'],assetPath='item-atlas-v210/page-'+format(bindings[r['itemId']]['page'],'02')+'.png',projection='V210_PACKED_NAMED_NATIVE_PREVIEW',inventoryOnly=r['inventoryOnly'],evidence=r['evidence'])for r in records if r['itemId'] not in manifest},ensure_ascii=False,separators=(',',':'))+'\n')
# Generated domain catalog uses small methods to stay below JVM method bytecode limits.
lines=['package com.projectdark.mobile;','import java.util.*;','/** Source-name identities. Unknown numbers and wearable motions remain unresolved. */','final class UnifiedItemCatalogV210 {',' static void install(RpgProgressionState r){']
for i in range(math.ceil(len(records)/70)):lines.append(f'  part{i}(r);')
lines+=[' }']
for i in range(math.ceil(len(records)/70)):
 lines.append(f' private static void part{i}(RpgProgressionState r){{')
 for rec in records[i*70:(i+1)*70]:
  stats='stats('+','.join(q(k)+','+str(v)for k,v in rec['stats'].items())+')';lines.append('  r.registerUnifiedItem('+','.join([q(rec['itemId']),q(rec['name']),q(rec['slot']),q(rec['appearanceId']),str(rec['level']) if rec['level'] is not None else 'null',stats,'RpgProgressionState.Evidence.'+rec['evidence'],str(rec['inventoryOnly']).lower()])+');')
 lines.append(' }')
lines+=[' static final String[] IDS={'+','.join(q(r['itemId'])for r in records)+'};',' private static Map<String,Integer> stats(Object... p){Map<String,Integer> m=new LinkedHashMap<>();for(int i=0;i<p.length;i+=2)m.put((String)p[i],(Integer)p[i+1]);return m;}','}'];(java/'UnifiedItemCatalogV210.java').write_text('\n'.join(lines)+'\n')
lines=['package com.projectdark.mobile;','import java.util.*;','/** Accepted exact source identities and explicit retirement of our invented training variants. */','final class EquipmentIdentityV210 {',' static final Map<String,String> ALIASES=new LinkedHashMap<>();',' static {']
for ident,target in sorted(alias.items()):lines.append('  ALIASES.put('+q(ident)+','+q(target)+');')
lines+=[' }',' private static final Map<String,String> NAMES=new HashMap<>();',' static {']
for ident in sorted(set(alias.values())):
 rec=next((r for r in records if r['itemId']==ident),None);name=rec['name'] if rec else defs[ident]['name'];lines.append('  NAMES.put('+q(ident)+','+q(name)+');')
lines+=[' }',' static String name(String id,String fallback){return NAMES.getOrDefault(canonical(id),fallback);}',' static String canonical(String id){return ALIASES.getOrDefault(id,id);}',' static boolean alias(String id){return ALIASES.containsKey(id);}','}'];(java/'EquipmentIdentityV210.java').write_text('\n'.join(lines)+'\n')
(OUT/'catalog.json').write_text(json.dumps(dict(revision='V210',sourceNamedRows=1618,originalIdentities=len(records),aliases=alias,records=records,unresolvedSources=excluded,ambiguousNames=pending,atlasTextures=len(raws),atlasBindings=len(bindings),unavailable=unavailable,policy='Never merge distinct original names or stats by image; preserve source and save quantities. Native inventory-only additions do not claim wearable motions.'),ensure_ascii=False,indent=2)+'\n')
print('V210',len(records),'source identities;',sum(r['inventoryOnly']for r in records),'new;',len(alias),'aliases;',len(raws),'textures;',len(bindings),'bindings; unavailable',len(unavailable));print('missing',unavailable[:8]);print('unresolved',len(excluded),'ambiguous',len(pending))
