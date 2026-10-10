"""Independent packaged coverage and source RGB audit; no drawable placeholders allowed."""
from pathlib import Path
import json,hashlib,zipfile
from PIL import Image
from build_full_item_catalog import runtime_items
ROOT=Path(__file__).resolve().parents[1];APP=ROOT/'app/src/main/assets';m=json.loads((APP/'item-icons/manifest.json').read_text());assert set(m['items'])==set(runtime_items());seen=set();equivalent=0
with zipfile.ZipFile(ROOT/'master/source/items/full_20261010/original_sources.zip')as z:
 for ident,r in m['items'].items():
  if r.get('identityMatch')=='PENDING_REPLACEMENT_SOURCE':
   assert ident=='IT_RING_THREELINEGOLD' and r['assetPath'] is None
   assert not (APP/'equipment-icons/it_ring_threelinegold.png').exists()
   continue
  p=APP/r['assetPath'];assert p.exists(),ident;assert hashlib.sha256(p.read_bytes()).hexdigest()==r['assetSha256'],ident
  if 'EQUIVALENT'in r['identityMatch']:equivalent+=1;assert r.get('limitation'),ident
  if r['assetPath']in seen:continue
  seen.add(r['assetPath']);im=Image.open(p).convert('RGBA');assert im.getbbox(),ident
  if r.get('identityMatch')=='EXACT_WEARABLE_APPEARANCE':
   source=ROOT/r['sourcePath'];assert hashlib.sha256(source.read_bytes()).hexdigest()==r['sourceSha256']
   raw=Image.open(source).convert('RGBA');x,y,w,h=r['crop'];raw=raw.crop((x,y,x+w,y+h));assert im.size==raw.size and im.tobytes()==raw.tobytes(),ident
  if r.get('sourceArchive','').endswith('full_20261010/original_sources.zip'):
   import io
   original=z.read(r['sourceMember']);assert hashlib.sha256(original).hexdigest()==r['sourceSha256'];raw=Image.open(io.BytesIO(original)).convert('RGBA');assert im.size==raw.size
   for y in range(im.height):
    for x in range(im.width):assert im.getpixel((x,y))[:3]==raw.getpixel((x,y))[:3],(ident,x,y)
   assert im.getpixel((0,0))[3]==0;assert im.getpixel((31,31))[3]==0
assert len(m['items'])==422
for p in ('GameView.java','TownShopWindow.java'):
 t=(ROOT/'app/src/main/java/com/projectdark/mobile'/p).read_text();assert 'ItemIconCatalog'not in t
print('VERIFIED_FULL_ORIGINAL_ART',len(m['items']),'items',len(seen),'unique originals',equivalent,'explicit visual equivalents (not exact historical identity claims)')
