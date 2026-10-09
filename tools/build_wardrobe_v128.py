"""Identity-first source catalog. Extract native previews; never invent names/stats from pictures."""
from pathlib import Path
import csv,json,re,hashlib,zipfile,io
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'master/source/equipment/identity_v128';SOURCE.mkdir(parents=True,exist_ok=True)
rs=list(csv.DictReader((ROOT/'master/data/Asset_Master.csv').open(encoding='utf-8-sig')))
byid={r['id'].lower():r for r in rs}
known=json.loads((ROOT/'app/src/main/assets/equipment-icons/manifest.json').read_text())
# Reconstruct existing runtime appearance links from the actual constructor and campaign templates.
t=(ROOT/'app/src/main/java/com/projectdark/mobile/RpgProgressionState.java').read_text()
constants=dict(re.findall(r'String\s+(\w+)\s*=\s*"([^"]+)"',t))
links={}
for m in re.finditer(r'new ItemDefinition\(\s*("[^"]+"|\w+)\s*,\s*"[^"]+"\s*,\s*[^,]+,\s*("(?:m[wuhlst]|w[wuhlst])[^"\s]*"|\w+_APPEARANCE_ID)',t):
 id=json.loads(m[1]) if m[1].startswith('"') else constants.get(m[1]);a=json.loads(m[2]) if m[2].startswith('"') else constants.get(m[2]);
 if id and a:links[id]=a
for i,job in enumerate(['WARRIOR','ROGUE','MAGE','CLERIC','MARTIAL_ARTIST']):
 for lv in [11,26]:
  links[f'IT_B_CAMPAIGN_{job}_{lv}']=['mu0000059','mu0000055','mu0000117','mu0000210','mu0000057'][i]
  if job!='MARTIAL_ARTIST':links[f'IT_B_CAMPAIGN_TOOL_{job}_{lv}']=links[{'WARRIOR':'IT_B_JOB_WARRIOR_WEAPON','ROGUE':'IT_B_JOB_ROGUE_WEAPON','MAGE':'IT_B_JOB_MAGE_WAND','CLERIC':'IT_B_JOB_CLERIC_WAND'}[job]]
# Correct semantic mismatches, not just their icon. Preserve existing adapted numerical modifiers.
rebind={'IT_B_JOB_ROGUE_WEAPON':'mw002','IT_B_JOB_CLERIC_WAND':'mw008','IT_B_JOB_ROGUE_GARMENT':'mu0000004','IT_B_JOB_MONK_GI':'mu0000003'}
for job,a in [('WARRIOR','mu0000007'),('ROGUE','mu0000004'),('MAGE','mu0000006'),('CLERIC','mu0000015'),('MARTIAL_ARTIST','mu0000003')]:
 for lv in [11,26]:rebind[f'IT_B_CAMPAIGN_{job}_{lv}']=a
for job,a in [('ROGUE','mw002'),('CLERIC','mw008')]:
 for lv in [11,26]:rebind[f'IT_B_CAMPAIGN_TOOL_{job}_{lv}']=a
links.update(rebind)
# Classic five-circle wardrobe, excluding wigs/cash costumes and higher advancements.
# This defined source slice is NOT a claim that the entire original game's item set is complete.
weapon_ids={f'mw{i:03d}' for i in range(1,63)}
armor_ids={f'mu{i:07d}' for i in list(range(1,30))+list(range(43,103))+list(range(111,141))+[180]}
hat_ids={f'mh{i:03d}' for i in list(range(106,114))+list(range(121,126))+list(range(131,139))+list(range(143,148))+[152,153,154,155,156,157,158,162,163,165,166,167,168,171,172,173,174]}
selected={a:r for a,r in byid.items() if a in weapon_ids|armor_ids|hat_ids|set(links.values()) and r['name'] not in ('','UNNAMED') and r['full_audit_status']=='SOURCE_NAMED'}
selected.update({a:byid[a] for a in links.values() if a in byid})
items=[];used=set(links.values());icons={};frame_gaps={'mw030','mw031','mw038','mw042','mw046','mw052','mw054'}
for a,r in sorted(selected.items()):
 p=ROOT/r['webp'];assert p.exists(),p
 im=Image.open(p).convert('RGBA');crop=[int(float(r[k])) for k in ['thumb_x','thumb_y','thumb_w','thumb_h']]
 # Master item files are already source thumbnails, not full atlases. Crop only when source dimensions require it.
 if im.size==(crop[2],crop[3]):out=im;projection='MASTER_NATIVE_THUMBNAIL';region=[0,0,*im.size]
 else:region=crop;out=im.crop((crop[0],crop[1],crop[0]+crop[2],crop[1]+crop[3]));projection='MASTER_EXACT_NATIVE_CROP'
 assert out.getbbox(),a
 target=ROOT/'app/src/main/assets/item-icons'/('identity_'+a+'.png');out.save(target)
 icons[a]=dict(appearanceId=a,assetPath='item-icons/'+target.name,assetSha256=hashlib.sha256(target.read_bytes()).hexdigest(),sourcePath=r['webp'],sourceSha256=hashlib.sha256(p.read_bytes()).hexdigest(),crop=region,projection=projection,identityMatch='EXACT_WEARABLE_APPEARANCE',nativeSize=list(out.size),name=r['name'] or '이름 미확인 장비',nameEvidence=r['name_evidence_source'] or 'UNRESOLVED',limitation='Original wearable preview of this exact appearance; dedicated inventory illustration not yet recovered. No enlargement or painted pixels.')
 if a in frame_gaps or a in used or not r['name'] or r['full_audit_status']!='SOURCE_NAMED':continue
 slot={'weapon':'무기','armor':'갑옷','hair':'모자'}[r['type']]
 items.append(dict(itemId='IT_WARDROBE_'+a.upper(),appearanceId=a,name=r['name'],slot=slot,requiredLevel=None,jobRestrictionResolved=False,statModifiers={},evidence='PENDING',sourceAssetId=r['id'],nameEvidence=r['name_evidence_source'],limitation='Source-named appearance, original requirements/statistics unresolved; available in explicit equipment sandbox.'))
renames={id:dict(appearanceId=a,name=byid[a]['name'] if byid[a]['name'] else ('이름 미확인 '+{'shoes':'신발','shield':'방패','armor':'갑옷','hair':'모자','weapon':'무기'}.get(byid[a]['type'],'장비')),adaptedVariant=id.startswith('IT_B_')) for id,a in links.items() if a in byid}
data=dict(sourceFrameGaps=sorted(frame_gaps),revision='IDENTITY_FIRST_V128',scope='Classic source-named wardrobe slice; all registered items audited, original whole-game completeness not claimed.',existing=renames,additions=items,icons=icons)
(SOURCE/'catalog.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
# Record all Master identities, including exclusions and unresolved names. Never silently omit source gaps.
audit=[]
for r in rs:
 if r['type'] not in ('weapon','armor','hair','shoes','shield'):continue
 a=r['id'].lower();audit.append(dict(sourceAssetId=r['id'],name=r['name'],type=r['type'],gender=r['gender'],status='SOURCE_FRAME_GAPS_NOT_ADMITTED' if a in frame_gaps else 'CONNECTED_V128' if a in selected else 'NAME_UNRESOLVED' if not r['name'] or r['name']=='UNNAMED' else 'OUTSIDE_CLASSIC_SOURCE_SLICE',reason=None if a in selected else 'Exact canonical name unresolved' if not r['name'] or r['name']=='UNNAMED' else 'Other gender / costume / later advancement / no admitted classic inventory identity',sourcePath=r['webp']))
(SOURCE/'master_inventory_audit.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2)+'\n')
lines=['package com.projectdark.mobile;','import java.util.*;','/** Generated identity catalog; missing source stats/requirements remain pending. */','final class SourceWardrobeCatalog {',' static void install(RpgProgressionState r){']
for id,v in renames.items():
 name=v['name']+(' · 수련' if v['adaptedVariant'] else '')
 lines.append('  r.rebindSourceIdentity('+json.dumps(id)+','+json.dumps(name,ensure_ascii=False)+','+json.dumps(v['appearanceId'])+');')
for v in items:
 action='AnimationAction.SWING' if v['slot']=='무기' else 'null'
 lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join([json.dumps(v['itemId']),json.dumps(v['name'],ensure_ascii=False),json.dumps(v['slot'],ensure_ascii=False),json.dumps(v['appearanceId']),action,'null','Collections.<String>emptySet()','false','null','null','Collections.<String,Integer>emptyMap()','RpgProgressionState.Evidence.PENDING'])+'));')
lines+=[' }','}'];(ROOT/'app/src/main/java/com/projectdark/mobile/SourceWardrobeCatalog.java').write_text('\n'.join(lines)+'\n')
print('EXACT_WARDROBE',len(selected),'ADDITIONS',len(items),'EXISTING_LINKS',len(links),'MASTER_AUDIT',len(audit))
