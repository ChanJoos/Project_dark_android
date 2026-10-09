"""Bind every registered item to original inventory art, with explicit visual-equivalence receipts.
Never infer historical item identity/statistics from a visually similar picture.
"""
from pathlib import Path
import json,re,hashlib,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/full_20261010';APP=ROOT/'app/src/main/assets/item-icons'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def runtime_items():
 text=(ROOT/'app/src/main/java/com/projectdark/mobile/RpgProgressionState.java').read_text();constants=dict(re.findall(r'String\s+(\w+)\s*=\s*"([^"]+)"',text));constants['AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID']='IT_B_TRAINING_TOKEN';rows={}
 for m in re.finditer(r'new ItemDefinition\(\s*("[^"]+"|[\w.]+)\s*,\s*"([^"]+)"',text):
  ident=json.loads(m[1]) if m[1].startswith('"')else constants.get(m[1])
  if ident:rows[ident]=m[2]
 for job in ('WARRIOR','ROGUE','MAGE','CLERIC','MARTIAL_ARTIST'):
  for lv in (11,26):
   rows[f'IT_B_CAMPAIGN_{job}_{lv}']=job+' armor';rows[f'IT_B_CAMPAIGN_TOOL_{job}_{lv}']=job+' tool'
 for e in json.loads((ROOT/'master/canonical/Accessory_Catalog.json').read_text()):rows.setdefault(e['itemId'],e['name'])
 wardrobe=ROOT/'master/source/equipment/identity_v128/catalog.json'
 if wardrobe.exists():
  j=json.loads(wardrobe.read_text())
  for id,e in j['existing'].items():
   if id in rows:rows[id]=e['name']+(' · 수련' if e['adaptedVariant'] else '')
  for e in j['additions']:rows[e['itemId']]=e['name']
 return rows
# source page + first-cell image filename; different pages may use identical filenames.
CHOICES={
 'IT_ADAPTED_PLAYTEST_MOKDO':(81,'bokutou.gif','EXACT_SOURCE_NAME'),
 'IT_TEST_WEAPON_MW002':(81,'epe.gif','EXACT_SOURCE_NAME'),
 'IT_TEST_WEAPON_MW003':(81,'katorasu.gif','EXACT_SOURCE_NAME'),
 'IT_SHOP_WEAPON_MW004':(81,'sabel.gif','HISTORICAL_LOCALIZATION_EQUIVALENT'),
 'IT_SHOP_WEAPON_MW005':(81,'gradius.gif','EXACT_SOURCE_NAME'),
 'IT_B_JOB_WARRIOR_WEAPON':(81,'longsword.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_ROGUE_WEAPON':(81,'tanken1.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_MAGE_WAND':(87,'magic_satia.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_CLERIC_WAND':(250,'seitue01.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_ADAPTED_STARTER_SHIELD':(196,'tate1.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_TEST_SHIELD_MS002':(196,'tate11.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_TEST_SHIELD_MS003':(196,'tate26.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_TEST_ARMOR_MU0000002':(164,'L1_male.gif','EXACT_SOURCE_NAME'),
 'IT_APPEARANCE_PEASANT_SHIRT':(163,'japanuniform01.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_SHOP_ARMOR_JIPON':(164,'O41_male.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_TEST_ARMOR_MU0000003':(168,'sougetu.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_REFERENCE_LEOPARD':(165,'L1_male.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_REFERENCE_HELM':(131,'baikuherumeto.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_ADAPTED_STARTER_HAT':(131,'m_goggle.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_TEST_HAT_MH173':(131,'f_goggle.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_TEST_HAT_MH174':(131,'f_goggle.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_B_JOB_WARRIOR_TUNIC':(164,'L1_male.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_ROGUE_GARMENT':(165,'L1_male.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_MAGE_ROBE':(166,'L1_male.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_CLERIC_ROBE':(167,'N1_male.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_JOB_MONK_GI':(168,'sougetu.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_SMALL_POTION':(84,'kuranumu.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_MP_POTION':(84,'maradhiumu.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_REAGENT_KOMADIUM':(84,'komadhiumu.gif','EXACT_SOURCE_NAME'),
 'IT_REAGENT_DIBENOMUM':(84,'dhibenonumu.gif','EXACT_SOURCE_NAME'),
 'IT_REAGENT_CURANUM':(84,'kuranumu.gif','EXACT_SOURCE_NAME'),
 'IT_REAGENT_EXCURANUM':(84,'axkuranumu.gif','EXACT_SOURCE_NAME'),
 'IT_REAGENT_CURUM':(84,'kuranumu.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_REAGENT_HOLYWATER':(82,'seimeinomizud.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_RECALL_MILLES':(84,'redrikoru.gif','UNRESOLVED_IDENTITY_VISUAL_EQUIVALENT'),
 'IT_B_TRAINING_TOKEN':(82,'ounomonsyou.gif','ADAPTED_VISUAL_EQUIVALENT'),
 'IT_B_PURIFIED_ESSENCE':(365,'1.gif','ADAPTED_VISUAL_EQUIVALENT'),
}
REUSE={'IT_SHOES':'IT_SOURCE_150_KUTU1_6FDB18DA','IT_TEST_SHOES_ML229':'IT_SOURCE_150_KUTU56_83C01E8B','IT_TEST_SHOES_ML230':'IT_SOURCE_150_KUTU41_24CE5A29','IT_B_POTE_SHOES':'IT_SOURCE_150_KUTU41_24CE5A29','IT_SOURCE_MINIMOB_2':'IT_SOURCE_197_ZEMSTONE_UMI_62F2D328'}
for job,page,filename,tool in [('WARRIOR',164,'L86_male.gif','IT_B_JOB_WARRIOR_WEAPON'),('ROGUE',165,'L1_male.gif','IT_B_JOB_ROGUE_WEAPON'),('MAGE',166,'L99_male.gif','IT_B_JOB_MAGE_WAND'),('CLERIC',167,'L99_male.gif','IT_B_JOB_CLERIC_WAND'),('MARTIAL_ARTIST',168,'orurosu.gif',None)]:
 for lv in (11,26):
  CHOICES[f'IT_B_CAMPAIGN_{job}_{lv}']=(page,filename,'ADAPTED_VISUAL_EQUIVALENT')
  if tool:CHOICES[f'IT_B_CAMPAIGN_TOOL_{job}_{lv}']=CHOICES[tool]
  else:REUSE[f'IT_B_CAMPAIGN_TOOL_{job}_{lv}']='IT_GLOVE_LEATHER' if lv==11 else 'IT_SOURCE_170_KINTEBUKURO_7E1109C9'
def main():
 APP.mkdir(parents=True,exist_ok=True)
 if not (SRC/'raw').exists():
  with zipfile.ZipFile(SRC/'original_sources.zip')as z:
   for name in z.namelist():
    if name.startswith('raw/'):(SRC/name).parent.mkdir(parents=True,exist_ok=True);(SRC/name).write_bytes(z.read(name))
 collection=json.loads((SRC/'collection.json').read_text());old=json.loads((ROOT/'app/src/main/assets/equipment-icons/manifest.json').read_text());items={}
 for ident,e in old['items'].items():items[ident]=dict(e,assetPath='equipment-icons/'+e['asset'],identityMatch='EXISTING_LABELLED_SOURCE')
 entries={}
 for e in collection:
  if 'file'not in e:continue
  for row in e['rows']:entries[(int(row['page'][5:]),e['file'].split('_',1)[1])]=e
 bg=Image.open(ROOT/'master/assets/equipment/background_masks_v126/source_socket_consensus.png').convert('RGB');audit=[]
 for ident,(page,name,identity)in CHOICES.items():
  e=entries[(page,name)];src=SRC/'raw'/e['file'];assert sha(src)==e['sha256'];im=Image.open(src).convert('RGBA');assert im.size==(32,32),(ident,im.size)
  original_alpha=im.getchannel('A');native_alpha=original_alpha.getextrema()[0]==0
  mask=Image.new('L',im.size,255)
  for y in range(32):
   for x in range(32):
    rgb=im.getpixel((x,y))[:3];sample=bg.getpixel((x,y))
    if x in (0,31) or y in (0,31) or max(abs(rgb[i]-sample[i])for i in range(3))<=12:mask.putpixel((x,y),0)
  # Same source capture texture; transparent detached brown specks only.
  visited=set()
  for y in range(32):
   for x in range(32):
    if (x,y)in visited or not mask.getpixel((x,y)):continue
    todo=[(x,y)];visited.add((x,y));component=[]
    while todo:
     q=todo.pop();component.append(q)
     for dx,dy in ((-1,0),(1,0),(0,-1),(0,1),(-1,-1),(-1,1),(1,-1),(1,1)):
      n=(q[0]+dx,q[1]+dy)
      if 0<=n[0]<32 and 0<=n[1]<32 and n not in visited and mask.getpixel(n):visited.add(n);todo.append(n)
    def brown(q):
     r,g,b=im.getpixel(q)[:3];return 45<=r<=135 and 25<=g<=100 and 10<=b<=65 and r>g>b and .52<g/max(1,r)<.86 and .30<b/max(1,g)<.84
    if all(brown(q)for q in component):
     for q in component:mask.putpixel(q,0)
  if native_alpha:mask=original_alpha # Already transparent originals need no capture-frame removal.
  kept=sum(bool(x)for x in mask.getdata());assert 5<kept<870,(ident,kept)
  im.putalpha(mask);file=e['file'].rsplit('.',1)[0]+'.png';im.save(APP/file)
  record=dict(itemId=ident,assetPath='item-icons/'+file,sourceUrl=e['url'],sourceRows=e['rows'],sourceArchive='master/source/items/full_20261010/original_sources.zip',sourceMember='raw/'+e['file'],sourceSha256=e['sha256'],assetSha256=sha(APP/file),projection='SOURCE_RGB_WITH_SEPARATE_ALPHA',backgroundRemoval='ORIGINAL_SOURCE_ALPHA' if native_alpha else 'SOURCE_SOCKET_SPATIAL_MATCH_PLUS_DETACHED_GRAIN',identityMatch=identity,foregroundPixels=kept,limitation=None if identity=='EXACT_SOURCE_NAME' else 'Original inventory artwork used as a visual equivalent; exact historical identity is not asserted. Game names/stats/wearable layers unchanged.')
  items[ident]=record;audit.append(record)
 for ident,target in REUSE.items():items[ident]=dict(items[target],itemId=ident,reusesItemArt=target,identityMatch='ADAPTED_VISUAL_EQUIVALENT',limitation='Original inventory artwork visual equivalent; exact historical identity not asserted.')
 # Retain the already labelled original Chungryong inventory illustration.
 p=ROOT/'app/src/main/assets/weapons/chungryong/icon.png';items['IT_WEAPON_CHUNGRYONG']=dict(itemId='IT_WEAPON_CHUNGRYONG',assetPath='weapons/chungryong/icon.png',assetSha256=sha(p),sourcePath='master/source/chungryong',identityMatch='EXISTING_LABELLED_SOURCE',projection='EXISTING_SOURCE_ALPHA')
 wardrobe=ROOT/'master/source/equipment/identity_v128/catalog.json'
 if wardrobe.exists():
  j=json.loads(wardrobe.read_text())
  for ident,e in j['existing'].items():
   if ident in items:items[ident]=dict(j['icons'][e['appearanceId']],itemId=ident)
  for e in j['additions']:items[e['itemId']]=dict(j['icons'][e['appearanceId']],itemId=e['itemId'])
 runtime=runtime_items();assert set(items)==set(runtime),(set(runtime)-set(items),set(items)-set(runtime));assert len(items)==408
 manifest=dict(revision='IDENTITY_FIRST_NATIVE_SIZE_V128',registeredItems=len(items),policy='Every current item has an explicit original inventory-art binding. Source RGB retained, native-size presentation. Exact wearable identities replace visual equivalents; missing dedicated inventory illustrations and unknown names/stats remain explicitly recorded.',items=items)
 (APP/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
 (SRC/'bindings.json').write_text(json.dumps(dict(currentItems=len(items),newSourceBindings=len(audit),reusedSourceBindings=len(REUSE),items={k:dict(name=runtime[k],assetPath=v['assetPath'],identityMatch=v['identityMatch'])for k,v in items.items()}),ensure_ascii=False,indent=2)+'\n')
 print('FULL_ITEM_ART',len(items),'runtime bindings;',len(set(v['assetPath']for v in items.values())),'unique originals')
if __name__=='__main__':main()
