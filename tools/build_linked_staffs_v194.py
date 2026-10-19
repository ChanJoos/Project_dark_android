"""Project manually reviewed staff weapons, never infer female/source aliases."""
from pathlib import Path
import json,zipfile,io,hashlib,re
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/naver_bundle_20261019';rows={r['itemId']:r for r in json.loads((ROOT/'master/canonical/User_Item_Master_20261018.json').read_text())['records']};review=json.loads((SRC/'reviewed_staffs_v194.json').read_text());manifest=json.loads((ROOT/'app/src/main/assets/item-icons/manifest.json').read_text());q=lambda s:json.dumps(s,ensure_ascii=False);admitted=[]
lines=['package com.projectdark.mobile;','import java.util.*;','/**3 exact named staff weapons. Historical article conflicts retain current U values. */','final class LinkedStaffCatalogV194 {',' static void install(RpgProgressionState r){']
with zipfile.ZipFile(SRC/'staff_originals_v194.zip') as source,zipfile.ZipFile(SRC/'generated_staffs_v194.zip.tmp','w',zipfile.ZIP_DEFLATED) as out:
 for item in review:
  assert item.get('completeIdleWalkAction') and item['appearanceId'].startswith('mw'),item['name'];row=rows[item['itemId']];assert row['displayName']==item['name'];ident=item['appearanceId'];meta=source.read(f'data/type/weapon/{ident}.json');raw=source.read(f'atlas/weapon/{ident}.webp');j=json.loads(meta)
  for group,count in [('01',10),('02',4)]:assert {x['f'] for x in j['sprites'][group]}>=set(range(count))
  im=Image.open(io.BytesIO(raw));f=next(x for x in j['sprites']['01'] if x['f']==0);icon=im.crop((f['sx'],f['sy'],f['sx']+f['w'],f['sy']+f['h']));buf=io.BytesIO();icon.save(buf,format='PNG');b=buf.getvalue();path=f'item-icons/staff_v194_{ident}.png'
  for p,content in [(f'wearable-atlases/{ident}.webp',raw),(f'source-registration/{ident}.json',meta),(path,b)]:out.writestr(p,content)
  c=row['cells'];stats={k:int(c[i]) for k,i in [('AC',9),('HIT',10),('DAM',11),('HP',12),('MP',13),('STR',14),('DEX',15),('INT',16),('WIS',17),('CON',18),('REGEN',22),('MAGIC_DEFENSE',23)] if isinstance(c[i],(int,float))}
  for col,lo,hi in [(24,'MinATK','MaxATK'),(25,'LMinATK','LMaxATK')]:
   match=re.fullmatch(r'(\d+)m(\d+)',str(c[col]))
   if match:stats.update({lo:int(match[1]),hi:int(match[2])})
  args=[q(row['itemId']),q(row['displayName']),q('무기'),q(ident),'AnimationAction.SWING',str(c[21]) if isinstance(c[21],int) else 'null','Collections.<String>emptySet()','false','null','null','stats('+','.join(q(k)+','+str(v) for k,v in stats.items())+')','RpgProgressionState.Evidence.U'];lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join(args)+'));')
  manifest['items'][row['itemId']]=dict(itemId=row['itemId'],assetPath=path,assetSha256=hashlib.sha256(b).hexdigest(),appearanceId=ident,sourceArchive=str((SRC/'staff_originals_v194.zip').relative_to(ROOT)),sourceMember=f'atlas/weapon/{ident}.webp',sourceSha256=hashlib.sha256(raw).hexdigest(),projection='EXACT_NATIVE_IDLE0_CROP',evidence='EXACT_LABEL_AND_CURRENT_USER_MASTER');admitted.append(dict(item,itemId=row['itemId'],stats=stats,requiredLevel=c[21],currentStatSourceRow=row['sourceRow'],normalJobRequirements='PENDING_SANDBOX_REVIEW',status='IMPLEMENTED_PENDING_NATIVE_VERIFICATION'))
with zipfile.ZipFile(SRC/'generated_staffs_v194.zip.tmp') as z:assert z.testzip() is None
(SRC/'generated_staffs_v194.zip.tmp').replace(SRC/'generated_staffs_v194.zip')
lines+=[' }',' static final String[] IDS={'+','.join(q(r['itemId']) for r in admitted)+'};',' private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}','}'];(ROOT/'app/src/main/java/com/projectdark/mobile/LinkedStaffCatalogV194.java').write_text('\n'.join(lines)+'\n');manifest['registeredItems']=len(manifest['items']);(ROOT/'app/src/main/assets/item-icons/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(SRC/'staff_runtime_v194.json').write_text(json.dumps(dict(status='3_STAFF_WEAPONS_NOT_ALL_LINK_ITEMS',admitted=admitted),ensure_ascii=False,indent=2)+'\n');print('ADMITTED_CLOTHES',len(admitted))
