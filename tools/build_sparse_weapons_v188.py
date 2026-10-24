"""Named source weapons with explicit missing-frame metadata. Never invent missing art."""
from pathlib import Path
from PIL import Image
import json,zipfile,io,hashlib,re
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/naver_bundle_20261019';H=ROOT/'master/source/equipment/horror_v184';user=json.loads((ROOT/'master/canonical/User_Item_Master_20261018.json').read_text())['records'];manifest=json.loads((ROOT/'app/src/main/assets/item-icons/manifest.json').read_text());data={};specs=[];q=lambda x:json.dumps(x,ensure_ascii=False)
for path in [H/'original_wearables.zip',SRC/'wearables.zip']:
 with zipfile.ZipFile(path) as z:
  for n in z.namelist():data[n]=z.read(n)
base=json.loads((H/'receipt.json').read_text())['assets'];pending={x['appearanceId'] for x in json.loads((H/'runtime_catalog.json').read_text())['pending'] if x['reason']=='SPARSE_ORIGINAL_FRAME_IDS'}
specs += [dict(id=x['appearanceId'],name=x['name'],originalArchive=str((H/'original_wearables.zip').relative_to(ROOT))) for x in base if x['appearanceId'] in pending]
specs += [dict(x,originalArchive=str((SRC/'wearables.zip').relative_to(ROOT))) for x in json.loads((SRC/'wearable_receipt.json').read_text()) if x['id'].startswith('mw') and not x.get('completeIdleWalkAction') and 'error' not in x]
lines=['package com.projectdark.mobile;','import java.util.*;','/** Exact source pixels; omitted source frames suppress only that gear layer. */','final class SparseWeaponCatalogV188 {',' static void install(RpgProgressionState r){'];admitted=[];seen=set();temp=SRC/'generated_sparse_weapons.zip.tmp'
with zipfile.ZipFile(temp,'w',zipfile.ZIP_DEFLATED) as out:
 for a in specs:
  ident=a['id'];meta=data[f'data/type/weapon/{ident}.json'];raw=data[f'atlas/weapon/{ident}.webp'];j=json.loads(meta);assert {x['f'] for x in j['sprites']['01']}>=set([0,1]);f=next(x for x in j['sprites']['01'] if x['f']==0);im=Image.open(io.BytesIO(raw));icon=im.crop((f['sx'],f['sy'],f['sx']+f['w'],f['sy']+f['h']));b=io.BytesIO();icon.save(b,format='PNG');png=b.getvalue();path=f'item-icons/sparse_v188_{ident}.png'
  for p,b in [(f'wearable-atlases/{ident}.webp',raw),(f'source-registration/{ident}.json',meta),(path,png)]:out.writestr(p,b)
  missing={g:sorted(set(range(n))-{x['f'] for x in j['sprites'].get(g,[])}) for g,n in [('01',10),('02',4)]}
  for row in user:
   if row['uniqueName']!=a['name'] or row['itemId'] in seen:continue
   seen.add(row['itemId']);c=row['cells'];stats={k:int(c[i]) for k,i in [('AC',9),('HIT',10),('DAM',11),('HP',12),('MP',13),('STR',14),('DEX',15),('INT',16),('WIS',17),('CON',18),('REGEN',22),('MAGIC_DEFENSE',23)] if isinstance(c[i],(int,float))}
   for col,lo,hi in [(24,'MinATK','MaxATK'),(25,'LMinATK','LMaxATK')]:
    m=re.fullmatch(r'(\d+)m(\d+)',str(c[col]))
    if m:stats.update({lo:int(m[1]),hi:int(m[2])})
   args=[q(row['itemId']),q(row['displayName']),q('무기'),q(ident),'AnimationAction.SWING',str(c[21]) if isinstance(c[21],int) else 'null','Collections.<String>emptySet()','false','null','null','stats('+','.join(q(k)+','+str(v) for k,v in stats.items())+')','RpgProgressionState.Evidence.U'];lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join(args)+'));')
   admitted.append(dict(itemId=row['itemId'],name=row['displayName'],appearanceId=ident,sourceRow=row['sourceRow'],stats=stats,missingSourceFrames=missing,sourceCoverage='PARTIAL_SOURCE_POSES_NO_COMPLETION_CLAIM',missingFramePolicy='EXPLICIT_SOURCE_GROUP_MISSING_FRAME_NO_GEAR_LAYER_BODY_RETAINED',originalArchive=a['originalArchive']))
   manifest['items'][row['itemId']]=dict(itemId=row['itemId'],assetPath=path,assetSha256=hashlib.sha256(png).hexdigest(),appearanceId=ident,sourceArchive=a['originalArchive'],sourceMember=f'atlas/weapon/{ident}.webp',sourceSha256=hashlib.sha256(raw).hexdigest(),projection='EXACT_NATIVE_IDLE0_CROP',evidence='EXACT_SOURCE_NAME_WITH_EXPLICIT_POSE_OMISSIONS')
with zipfile.ZipFile(temp) as z:assert z.testzip() is None
temp.replace(SRC/'generated_sparse_weapons.zip');lines+=[' }',' static final String[] IDS={'+','.join(q(x['itemId']) for x in admitted)+'};',' private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}','}'];(ROOT/'app/src/main/java/com/projectdark/mobile/SparseWeaponCatalogV188.java').write_text('\n'.join(lines)+'\n');manifest['registeredItems']=len(manifest['items']);(ROOT/'app/src/main/assets/item-icons/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(SRC/'sparse_weapon_runtime_catalog.json').write_text(json.dumps(dict(status='PARTIAL_SOURCE_FRAME_COVERAGE_NOT_ALL_LINK_ITEMS',admitted=admitted),ensure_ascii=False,indent=2)+'\n');print('ADMITTED_SPARSE_WEAPONS',len(admitted))
