"""Reviewed source-table accessories. Exact socket crops; no unverified body art."""
from pathlib import Path
from PIL import Image
import json,zipfile,io,hashlib
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/naver_bundle_20261019';q=lambda x:json.dumps(x,ensure_ascii=False)
a=json.loads((SRC/'exact_name_candidates.json').read_text());rows={x['itemId']:x for x in json.loads((ROOT/'master/canonical/User_Item_Master_20261018.json').read_text())['records']};manifest=json.loads((ROOT/'app/src/main/assets/item-icons/manifest.json').read_text());indices=[24,25,27,28,29,30,31,32,33,34,43,44,53,55,56,58,64];review=[]
for i in indices:
 x=a[i];y=x['label']['top']+(-2 if i in [33,34,44] else -10);crop=[2,y,32,32] if i!=44 else [0,y,38,38]
 for match in x['items']:
  row=rows[match['itemId']];assert row['displayName']==x['label']['text'];review.append(dict(itemId=row['itemId'],name=row['displayName'],source=x['source'],crop=crop,slot='반지' if i<=34 else '목걸이' if i<=44 else '각반',identity='MANUALLY_REVIEWED_EXACT_TABLE_LABEL',visual='INVENTORY_ICON_ONLY_NO_BODY_ROUTE_CLAIM'))
lines=['package com.projectdark.mobile;','import java.util.*;','/** Reviewed accessory identities; no invented body layers or unresolved requirements. */','final class LinkedAccessoryCatalogV189 {',' static void install(RpgProgressionState r){'];pack=SRC/'generated_accessories_v189.zip.tmp'
with zipfile.ZipFile(pack,'w',zipfile.ZIP_DEFLATED) as out:
 for x in review:
  row=rows[x['itemId']];c=row['cells'];article=x['source'].split('/')[0]
  with zipfile.ZipFile(SRC/'archives'/f'{article}.zip') as z:raw=z.read(x['source'])
  im=Image.open(io.BytesIO(raw));cx,cy,w,h=x['crop'];icon=im.crop((cx,cy,cx+w,cy+h));b=io.BytesIO();icon.save(b,format='PNG');png=b.getvalue();path=f'item-icons/accessory_v189_{x["itemId"]}.png';out.writestr(path,png)
  stats={k:int(c[i]) for k,i in [('AC',9),('HIT',10),('DAM',11),('HP',12),('MP',13),('STR',14),('DEX',15),('INT',16),('WIS',17),('CON',18),('REGEN',22),('MAGIC_DEFENSE',23)] if isinstance(c[i],(int,float))}
  args=[q(row['itemId']),q(row['displayName']),q(x['slot']),'null','null',str(c[21]) if isinstance(c[21],int) else 'null','Collections.<String>emptySet()','false','null','null','stats('+','.join(q(k)+','+str(v) for k,v in stats.items())+')','RpgProgressionState.Evidence.U'];lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join(args)+'));');x.update(stats=stats,sourceRow=row['sourceRow'],requiredLevel=c[21],sourceSha256=hashlib.sha256(raw).hexdigest(),assetSha256=hashlib.sha256(png).hexdigest(),requirements='PENDING_NORMAL_EQUIP_BLOCKED')
  manifest['items'][row['itemId']]=dict(itemId=row['itemId'],assetPath=path,assetSha256=x['assetSha256'],sourceArchive=str((SRC/'archives'/f'{article}.zip').relative_to(ROOT)),sourceMember=x['source'],sourceSha256=x['sourceSha256'],crop=x['crop'],projection='EXACT_REVIEWED_SOCKET_CROP_BACKGROUND_PRESERVED',evidence=x['identity'])
with zipfile.ZipFile(pack) as z:assert z.testzip() is None;assert len(z.namelist())==len(review)
pack.replace(SRC/'generated_accessories_v189.zip');lines+=[' }',' static final String[] IDS={'+','.join(q(x['itemId']) for x in review)+'};',' private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}','}'];(ROOT/'app/src/main/java/com/projectdark/mobile/LinkedAccessoryCatalogV189.java').write_text('\n'.join(lines)+'\n');manifest['registeredItems']=len(manifest['items']);(ROOT/'app/src/main/assets/item-icons/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(SRC/'reviewed_accessories_v189.json').write_text(json.dumps(dict(status='PARTIAL_ARTICLE_COVERAGE',admitted=review,remaining=['Other source table rows and variants','Necklace element semantics','Normal job/promotion requirements','Legging/body layer source bindings']),ensure_ascii=False,indent=2)+'\n');print('REVIEWED',len(review))
