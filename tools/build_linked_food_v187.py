"""Source-table foods: preserve RGB, retain crop/alpha evidence, use only explicit effects."""
from pathlib import Path
import json,zipfile,io,hashlib
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/naver_bundle_20261019';rows=json.loads((SRC/'reviewed_foods.json').read_text());manifest=json.loads((ROOT/'app/src/main/assets/item-icons/manifest.json').read_text());q=lambda x:json.dumps(x,ensure_ascii=False)
with zipfile.ZipFile(SRC/'archives/293294.zip') as z:raw=z.read('293294/source_01.png')
source=Image.open(io.BytesIO(raw)).convert('RGB');source_sha=hashlib.sha256(raw).hexdigest();bg=np.array(source.crop((180,25,212,57))).astype(int)
lines=['package com.projectdark.mobile;','import java.util.*;','/** Historical article labels and explicit recovery only; unknown effects stay unavailable. */','final class LinkedFoodCatalogV187 {',' static void install(RpgProgressionState r){'];review=[]
with zipfile.ZipFile(SRC/'generated_foods.zip','w',zipfile.ZIP_DEFLATED) as out:
 for row in rows:
  ident=row['itemId'];x,y,w,h=row['crop'];im=source.crop((x,y,x+w,y+h)).convert('RGBA');a=np.array(im)[:,:,:3].astype(int);best=None
  for dy in range(32):
   for dx in range(32):
    sample=np.roll(np.roll(bg,dy,axis=0),dx,axis=1);mask=np.max(np.abs(a-sample),axis=2)>12;score=np.count_nonzero(~mask)
    if best is None or score>best[0]:best=(score,mask,dx,dy)
  mask=best[1];mask[0,:]=False;mask[-1,:]=False;mask[:,0]=False;mask[:,-1]=False
  visited=set()
  for yy in range(32):
   for xx in range(32):
    if (xx,yy) in visited or not mask[yy,xx]:continue
    queue=[(xx,yy)];visited.add((xx,yy));component=[]
    while queue:
     px,py=queue.pop();component.append((px,py))
     for dx,dy in [(-1,0),(1,0),(0,-1),(0,1),(-1,-1),(-1,1),(1,-1),(1,1)]:
      nx,ny=px+dx,py+dy
      if 0<=nx<32 and 0<=ny<32 and (nx,ny) not in visited and mask[ny,nx]:visited.add((nx,ny));queue.append((nx,ny))
    def brown(p):
     rr,gg,bb=a[p[1],p[0]];return 10<=rr<=160 and 5<=gg<=120 and 0<=bb<=85 and rr>gg>bb and .5<gg/max(1,rr)<.9 and .25<bb/max(1,gg)<.88
    strip=max(p[0] for p in component)-min(p[0] for p in component)<=1 and max(p[1] for p in component)-min(p[1] for p in component)>=27 and (min(p[0] for p in component)<=1 or max(p[0] for p in component)>=30)
    if all(brown(p) for p in component) or strip:
     for px,py in component:mask[py,px]=False
  alpha=Image.fromarray((mask*255).astype('uint8'));im.putalpha(alpha);buf=io.BytesIO();im.save(buf,format='PNG');b=buf.getvalue();path=f'item-icons/food_v187_{ident}.png';out.writestr(path,b);buf=io.BytesIO();alpha.save(buf,format='PNG');out.writestr(f'food-evidence-v187/{ident}_alpha.png',buf.getvalue());buf=io.BytesIO();source.crop((x,y,x+w,y+h)).save(buf,format='PNG');out.writestr(f'food-evidence-v187/{ident}_raw.png',buf.getvalue());kept=int(mask.sum());assert 5<kept<950,(ident,kept)
  # Non-equipment: recovery is routed through ItemEffects, never added to permanent/equipment HP.
  lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+q(ident)+','+q(row['name'])+',null,null,null,null,Collections.<String>emptySet(),false,null,null,Collections.<String,Integer>emptyMap(),RpgProgressionState.Evidence.FAN));')
  manifest['items'][ident]=dict(itemId=ident,assetPath=path,assetSha256=hashlib.sha256(b).hexdigest(),sourceArchive=str((SRC/'archives/293294.zip').relative_to(ROOT)),sourceMember='293294/source_01.png',sourceSha256=source_sha,crop=row['crop'],projection='SOURCE_RGB_WITH_REVIEWABLE_ALPHA',backgroundSample=[180,25,32,32],foregroundPixels=kept,identityMatch='EXACT_LABELLED_SOURCE_TABLE_ROW',evidence='HISTORICAL_FAN_TABLE');review.append(dict(row,sourceSha256=source_sha,assetSha256=hashlib.sha256(b).hexdigest(),foregroundPixels=kept,backgroundPhase=[best[2],best[3]],runtimeUse='HP_MP_RECOVERY_ONLY' if row['hp'] or row['mp'] else 'UNAVAILABLE_EFFECT_UNKNOWN'))
lines+=[' }',' static final String[] IDS={'+','.join(q(r['itemId']) for r in rows)+'};']
for key in ['hp','mp']:
 lines.append(' static int '+key+'(String id){if(id==null)return 0;switch(id){')
 for row in rows:
  if row[key]:lines.append('  case '+q(row['itemId'])+':return '+str(row[key])+';')
 lines+=['  default:return 0;',' }}']
lines+=['}'];(ROOT/'app/src/main/java/com/projectdark/mobile/LinkedFoodCatalogV187.java').write_text('\n'.join(lines)+'\n');manifest['registeredItems']=len(manifest['items']);(ROOT/'app/src/main/assets/item-icons/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(SRC/'food_runtime_catalog.json').write_text(json.dumps(dict(status='54_SOURCE_FOOD_ROWS_16_EXPLICIT_EFFECTS_NOT_ALL_LINKS',items=review),ensure_ascii=False,indent=2)+'\n');print('FOODS',len(rows),'EXPLICIT_EFFECTS',sum(bool(r['hp'] or r['mp']) for r in rows))
