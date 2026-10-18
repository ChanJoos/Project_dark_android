"""Admit only reviewed exact identities; preserve source bytes and all unresolved rows."""
from pathlib import Path
import json,zipfile,hashlib,io,re
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/naver_bundle_20261019';ASSETS=ROOT/'app/src/main/assets'
rows=json.loads((ROOT/'master/canonical/User_Item_Master_20261018.json').read_text())['records'];candidates=json.loads((SRC/'exact_name_candidates.json').read_text());accessories=json.loads((ROOT/'master/canonical/Accessory_Catalog.json').read_text());manifest=json.loads((ASSETS/'item-icons/manifest.json').read_text());wear=json.loads((SRC/'wearable_receipt.json').read_text());norm=lambda s:re.sub(r'\s+','',s)
for article in [259752]:
 with zipfile.ZipFile(SRC/'archives'/f'{article}.zip') as z:
  for name in z.namelist():
   p=SRC/name
   if not p.exists():p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(name))
by_name={}
for r in rows:by_name.setdefault(norm(r['displayName']),[]).append(r)
access_by={norm(a['name']):a for a in accessories};ready={r['name']:r for r in wear if r.get('completeIdleWalkAction') and r['id'].startswith('mw')};admitted=[];pending=[];seen=set();q=lambda x:json.dumps(x,ensure_ascii=False)
lines=['package com.projectdark.mobile;','import java.util.*;','/** Exact source-row links; this is NOT the whole article catalog. */','final class LinkedItemCatalogV185 {',' static void install(RpgProgressionState r){']
def admit(row,slot,appearance,icon,proof):
 if row['itemId'] in seen:return
 seen.add(row['itemId']);c=row['cells'];stats={k:int(c[i]) for k,i in [('AC',9),('HIT',10),('DAM',11),('HP',12),('MP',13),('STR',14),('DEX',15),('INT',16),('WIS',17),('CON',18),('REGEN',22),('MAGIC_DEFENSE',23)] if isinstance(c[i],(int,float))}
 for col,lo,hi in [(24,'MinATK','MaxATK'),(25,'LMinATK','LMaxATK')]:
  m=re.fullmatch(r'(\d+)m(\d+)',str(c[col]))
  if m:stats.update({lo:int(m[1]),hi:int(m[2])})
 args=[q(row['itemId']),q(row['displayName']),q(slot),q(appearance),'AnimationAction.SWING' if appearance else 'null',str(c[21]) if isinstance(c[21],int) else 'null','Collections.<String>emptySet()','false','null','null','stats('+','.join(q(k)+','+str(v) for k,v in stats.items())+')','RpgProgressionState.Evidence.U']
 lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join(args)+'));')
 entry=dict(itemId=row['itemId'],name=row['displayName'],slot=slot,appearanceId=appearance,requiredLevel=c[21],sourceRow=row['sourceRow'],stats=stats,proof=proof,sourceWeight=c[2],sourceDurability=c[8],jobAndPromotionRequirements='PENDING_NORMAL_EQUIP_BLOCKED',characterVisual='ORIGINAL_REGISTERED_WEAPON' if appearance else 'NO_BODY_ROUTE_CLAIM')
 admitted.append(entry);manifest['items'][row['itemId']]=dict(icon,itemId=row['itemId'])
with zipfile.ZipFile(SRC/'wearables.zip') as z,zipfile.ZipFile(SRC/'generated_assets.zip','w',zipfile.ZIP_DEFLATED) as out:
 for i,c in enumerate(candidates):
  name=c['label']['text']
  if i in [37,38]:pending.append(dict(source=c['source'],name=name,reason='ENHANCEMENT_PREFIX_ON_PREVIOUS_LINE_NOT_BASE_ITEM'));continue
  matches=by_name.get(norm(name),[])
  if name in ready:
   w=ready[name];ident=w['id'];meta=json.loads(z.read(f'data/type/weapon/{ident}.json'));raw=z.read(f'atlas/weapon/{ident}.webp');im=Image.open(io.BytesIO(raw));f=next(x for x in meta['sprites']['01'] if x['f']==0);icon=im.crop((f['sx'],f['sy'],f['sx']+f['w'],f['sy']+f['h']));buf=io.BytesIO();icon.save(buf,format='PNG');image=buf.getvalue();path=f'item-icons/linked_v185_{ident}.png'
   for p,b in [(f'wearable-atlases/{ident}.webp',raw),(f'source-registration/{ident}.json',z.read(f'data/type/weapon/{ident}.json')),(path,image)]:
    if p not in out.namelist():out.writestr(p,b)
   iconrow=dict(assetPath=path,assetSha256=hashlib.sha256(image).hexdigest(),appearanceId=ident,sourceArchive=str((SRC/'wearables.zip').relative_to(ROOT)),sourceMember=f'atlas/weapon/{ident}.webp',sourceSha256=hashlib.sha256(raw).hexdigest(),projection='EXACT_NATIVE_IDLE0_CROP',evidence='EXACT_NAMED_SOURCE')
   for row in matches:admit(row,'무기',ident,iconrow,dict(article=c['source'].split('/')[0],source=c['source'],sourceLabelTop=c['label']['top']))
  elif norm(name) in access_by and access_by[norm(name)]['itemId'] in manifest['items']:
   a=access_by[norm(name)];iconrow=manifest['items'][a['itemId']]
   for row in matches:admit(row,a['slot'],None,iconrow,dict(article=c['source'].split('/')[0],source=c['source'],existingExactNamedIcon=a['itemId']))
  else:pending.append(dict(source=c['source'],name=name,reason='EXACT_ICON_OR_COMPLETE_REGISTERED_WEARABLE_NOT_READY'))
 rapid_names=['라피드헤어클립-I','라피드헤어클립FX-II','라피드헤어클립FX-III','라피드헤어클립FX-IV','라피드헤어클립FX-V','흑룡의라피드헤어클립FX-IV']
 for n,name in enumerate(rapid_names):
  index=n if n<5 else 3;raw=(SRC/'259752'/f'source_{index:02}.jpg').read_bytes();path=f'item-icons/rapid_v185_{index}.jpg'
  if path not in out.namelist():out.writestr(path,raw)
  iconrow=dict(assetPath=path,assetSha256=hashlib.sha256(raw).hexdigest(),sourceArchive=str((SRC/'archives/259752.zip').relative_to(ROOT)),sourceMember=f'259752/source_{index:02}.jpg',sourceSha256=hashlib.sha256(raw).hexdigest(),projection='ORIGINAL_ICON_SOCKET_BYTES',evidence='SOURCE_TEXT_ORDER_AND_MASTER_STATS')
  for row in by_name.get(name,[]):admit(row,'장식',None,iconrow,dict(article='259752',source='rapid_article.txt',levelIconOrder=[35,74,85,99,'PROMOTED'],blackDragonSameAs99='EXPLICIT_SOURCE_TEXT' if n==5 else None))
lines+=[' }',' static final String[] IDS={'+','.join(q(x['itemId']) for x in admitted)+'};',' private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}','}']
(ROOT/'app/src/main/java/com/projectdark/mobile/LinkedItemCatalogV185.java').write_text('\n'.join(lines)+'\n');manifest['registeredItems']=len(manifest['items']);(ASSETS/'item-icons/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(SRC/'runtime_catalog.json').write_text(json.dumps(dict(status='PARTIAL_NOT_ALL_ARTICLE_ITEMS',admitted=admitted,pending=pending,remaining=['Complete all other table rows including clothes and food','Exact variant and attribute appearance mappings','Rapid body/effect registration','Missing source action poses','Normal job/promotion requirements','Source weight/durability mechanics and regeneration effect are not implemented by stat mapping']),ensure_ascii=False,indent=2)+'\n');print('ADMITTED',len(admitted),'PENDING_REVIEW',len(pending))
