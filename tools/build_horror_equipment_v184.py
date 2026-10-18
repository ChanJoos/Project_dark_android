"""Admit exact named, complete-source wearable pairs and U master rows. Keep every gap explicit."""
from pathlib import Path
import json,zipfile,io,re,hashlib,csv
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/equipment/horror_v184';ASSETS=ROOT/'app/src/main/assets'
receipt=json.loads((SRC/'receipt.json').read_text());rows=json.loads((ROOT/'master/canonical/User_Item_Master_20261018.json').read_text())['records'];manifest=json.loads((ASSETS/'item-icons/manifest.json').read_text());admitted=[];pending=[]
q=lambda s:json.dumps(s,ensure_ascii=False)
lines=['package com.projectdark.mobile;','import java.util.*;','/** U source rows; exact original wearable bytes. Unmapped/sparse sources remain explicit. */','final class HorrorEquipmentCatalog {',' static void install(RpgProgressionState r){']
with zipfile.ZipFile(SRC/'original_wearables.zip') as original,zipfile.ZipFile(SRC/'generated_assets.zip','w',zipfile.ZIP_DEFLATED) as out:
 for a in receipt['assets']:
  identity=a['appearanceId'];kind=a['type'];j=json.loads(original.read(f'data/type/{kind}/{identity}.json'));matches=[r for r in rows if r['uniqueName']==a['name']]
  complete=all({f['f'] for f in j['sprites'].get(g,[])}>=set(range(n)) for g,n in [('01',10),('02',4)])
  if not complete or not matches or identity.startswith('wu'):
   pending.append(dict(appearanceId=identity,name=a['name'],reason='SPARSE_ORIGINAL_FRAME_IDS' if not complete else 'USER_ROW_NAME_UNMAPPED' if not matches else 'FEMALE_BODY_NOT_IMPLEMENTED'));continue
  raw=original.read(f'atlas/{kind}/{identity}.webp');im=Image.open(io.BytesIO(raw));frame=next(f for f in j['sprites']['01'] if f['f']==0);icon=im.crop((frame['sx'],frame['sy'],frame['sx']+frame['w'],frame['sy']+frame['h']));buf=io.BytesIO();icon.save(buf,format='PNG');iconbytes=buf.getvalue();iconpath=f'item-icons/horror_v184_{identity}.png'
  out.writestr('wearable-atlases/'+identity+'.webp',raw);out.writestr('source-registration/'+identity+'.json',original.read(f'data/type/{kind}/{identity}.json'));out.writestr(iconpath,iconbytes)
  for row in matches:
   c=row['cells'];stats={k:int(c[i]) for k,i in [('AC',9),('HIT',10),('DAM',11),('HP',12),('MP',13),('STR',14),('DEX',15),('INT',16),('WIS',17),('CON',18)] if c[i] is not None}
   for col,lo,hi in [(24,'MinATK','MaxATK'),(25,'LMinATK','LMaxATK')]:
    m=re.fullmatch(r'(\d+)m(\d+)',str(c[col]));
    if m:stats.update({lo:int(m[1]),hi:int(m[2])})
   id=row['itemId'];slot='무기' if kind=='weapon' else '갑옷';level=c[21] if isinstance(c[21],int) else None
   args=[q(id),q(row['displayName']),q(slot),q(identity),'AnimationAction.SWING' if kind=='weapon' else 'null',str(level) if level is not None else 'null','Collections.<String>emptySet()','false','null','null','stats('+','.join(q(k)+','+str(v) for k,v in stats.items())+')','RpgProgressionState.Evidence.U']
   lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join(args)+'));')
   admitted.append(dict(itemId=id,name=row['displayName'],appearanceId=identity,sourceRow=row['sourceRow'],stats=stats,requiredLevel=level,type=kind,originalMonsterKitIdentity='UNRESOLVED'))
   manifest['items'][id]=dict(itemId=id,appearanceId=identity,assetPath=iconpath,assetSha256=hashlib.sha256(iconbytes).hexdigest(),sourcePath=str(SRC.relative_to(ROOT))+'/original_wearables.zip',sourceMember=f'atlas/{kind}/{identity}.webp',sourceSha256=hashlib.sha256(raw).hexdigest(),crop=[frame[k] for k in ['sx','sy','w','h']],projection='MASTER_EXACT_NATIVE_CROP',identityMatch='EXACT_WEARABLE_APPEARANCE',evidence='U')
lines+=[' }',' static final String[] IDS={'+','.join(q(a['itemId']) for a in admitted)+'};',' private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}','}']
(ROOT/'app/src/main/java/com/projectdark/mobile/HorrorEquipmentCatalog.java').write_text('\n'.join(lines)+'\n');manifest['registeredItems']=len(manifest['items']);(ASSETS/'item-icons/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
(SRC/'runtime_catalog.json').write_text(json.dumps(dict(status='PARTIAL_NOT_ALL_ARTICLE_OR_MONSTER_KITS',admitted=admitted,pending=pending,remaining=['Article variants without exact named appearance mapping','Ten original Horror monster weapon/armor/shield associations','Unidentified shields and their stats','Sparse source poses need correct frame-ID handling before admission']),ensure_ascii=False,indent=2)+'\n');print('ADMITTED',len(admitted),'GAPS',len(pending))
