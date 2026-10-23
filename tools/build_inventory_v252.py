"""Reproduce reviewed complete native inventory frames; no palette masks, rotation or recoloring.
Matching decisions and immutable source bytes remain independently auditable.
"""
from pathlib import Path
import json,sys,zipfile,hashlib,math,collections
sys.path.insert(0,'tools');from horror_original_formats import Archive,palette
from build_mage_v241 import epf
from PIL import Image,ImageDraw
import numpy as np
R=Path(__file__).resolve().parents[1];
import os,argparse
os.chdir(R)
p=argparse.ArgumentParser();p.add_argument('--client',type=Path);args=p.parse_args()
S=R/'master/source/items/inventory_v252';S.mkdir(exist_ok=True);O=R/'app/src/main/assets/inventory-v252';O.mkdir(exist_ok=True)
ar=Archive(args.client) if args.client else None;
if ar is None:
 z=zipfile.ZipFile(S/'original_inventory.zip');ar={n:z.read(n) for n in z.namelist()}
aliases=json.load(open('master/source/items/catalog_v210/catalog.json'))['aliases'];canonical=lambda i:aliases.get(i,i)
rows={};matches={x['file']:x for x in json.load(open('master/source/items/inventory_v252/native-approved.json'))};collection={x['file']:x for x in json.load(open('master/source/items/full_20261010/collection.json'))}
for ident,file in json.load(open('master/source/items/inventory_v252/collection-match.json')).items():
 if file in matches:
  r=matches[file];rows[canonical(ident)]=dict(itemId=canonical(ident),native=r['source'],evidence='EXACT_RETAINED_NAMED_INVENTORY_IMAGE_AND_CLIENT_PIXELS',referenceArchive='master/source/items/full_20261010/original_sources.zip',referenceMember='raw/'+file,referenceSha256=collection[file]['sha256'],referenceRows=collection[file]['rows'],comparisonMeanRgbError=r['fullError'],comparisonP95RgbError=r['p95'])
for r in json.load(open('master/source/items/inventory_v252/table-matches.json')):
 if r['error']>3:continue
 for ident in r['ids']:
  key=canonical(ident)
  source=r['source'];raw=zipfile.ZipFile('master/source/items/naver_bundle_20261019/archives/'+source.split('/')[0]+'.zip').read(source)
  rows[key]=dict(itemId=key,native=r['native'],name=r['name'],evidence='EXACT_NAMED_TABLE_INVENTORY_ICON_AND_CLIENT_PIXELS',referenceArchive='master/source/items/naver_bundle_20261019/archives/'+source.split('/')[0]+'.zip',referenceMember=source,referenceSha256=hashlib.sha256(raw).hexdigest(),referenceCrop=r['crop'],comparisonMeanRgbError=r['error'])
for r in json.load(open('master/source/items/inventory_v252/manifest-matches.json')):
 key=canonical(r['itemId'])
 if key in rows or r['itemId']=='IT_REAGENT_CURUM':continue
 original=json.load(open('app/src/main/assets/item-icons/manifest.json'))['items'][r['itemId']]
 rows[key]=dict(itemId=key,native=r['native'],evidence='EXISTING_EXACT_NAMED_INVENTORY_RECEIPT_AND_CLIENT_PIXELS',referenceAssetPath=r['assetPath'],referenceReceipt=original,comparisonMeanRgbError=r['error'],referenceAlignmentOffset=r['offset'])
# Same identity aliases resolve at runtime; no nearest-name or appearance-ID guesses.
images={};rawnames={'item000.pal','itempal.tbl'}
for ident,r in rows.items():
 name,index,paln,_,_=r['native'];rawnames|={name,f'item{paln:03}.pal'};w,h,fs=epf(ar[name]);x,y,indices=fs[index];im=Image.fromarray(palette(ar[f'item{paln:03}.pal'])[indices]);images[ident]=im;r.update(nativeEpfSha256=hashlib.sha256(ar[name]).hexdigest(),nativePaletteSha256=hashlib.sha256(ar[f'item{paln:03}.pal']).hexdigest(),nativeFrame=index,nativeOrigin=[x,y],sourceWidth=im.width,sourceHeight=im.height,rgbaSha256=hashlib.sha256(im.tobytes()).hexdigest(),projection='COMPLETE_ORIGINAL_INVENTORY_FRAME_NATIVE_ALPHA_NO_ROTATION_NO_RECOLOR')
with zipfile.ZipFile(S/'original_inventory.zip','w',zipfile.ZIP_DEFLATED)as z:
 for name in sorted(rawnames):z.writestr(name,ar[name])
keys=list(rows);sheet=Image.new('RGBA',(512,math.ceil(len(keys)/16)*32));manifest={};review=Image.new('RGB',(960,math.ceil(len(keys)/12)*85),(40,43,45));draw=ImageDraw.Draw(review)
for i,k in enumerate(keys):
 im=images[k];x=i%16*32;y=i//16*32;assert im.width<=32 and im.height<=32,(k,im.size);sheet.paste(im,(x,y));manifest[k]=dict(x=x,y=y,w=im.width,h=im.height,projection=rows[k]['projection']);a=im.resize((im.width*2,im.height*2),Image.Resampling.NEAREST);review.paste(a,(i%12*80,i//12*85),a);draw.text((i%12*80,i//12*85+65),str(i)+' '+k[-5:],fill='white')
sheet.save(O/'icons.png');(O/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,separators=(',',':'))+'\n');(S/'receipt.json').write_text(json.dumps(dict(status='SOURCE_RECOVERED_SCOPED_NOT_ALL_ITEMS_ORIGINAL_ACCEPTED',items=list(rows.values()),bindingCount=len(rows),policy='Native complete inventory sprites recovered by retained named inventory reference. Unresolved wearable previews are not certified inventory icons.'),ensure_ascii=False,indent=2)+'\n');reviewpath=R/'review252/inventory-restored.png';reviewpath.parent.mkdir(exist_ok=True);review.save(reviewpath)
print('restored',len(rows),'unique',len(set(tuple(r['native'])for r in rows.values())))
