"""Identity and source-byte gates; shared texture is never a reason to merge identities."""
from pathlib import Path
from PIL import Image
import json,hashlib
ROOT=Path(__file__).resolve().parents[1]
def main():
 j=json.loads((ROOT/'master/source/items/catalog_v210/catalog.json').read_text());m=json.loads((ROOT/'app/src/main/assets/item-atlas-v210/manifest.json').read_text());aliases=j['aliases'];names={r['itemId']:r['name']for r in j['records']}
 assert len(j['records'])==1445 and len(aliases)>=290 and sum(x.startswith('IT_B_')for x in aliases)==60
 assert not j['ambiguousNames'] and not j['unavailable'] and not m['unavailable']
 for ident,target in aliases.items():assert target not in aliases and ident!=target
 for r in j['records']:
  assert r['itemId'] in m['items']
  for source in r['sources']:assert hashlib.sha256((ROOT/source['path']).read_bytes()).hexdigest()==source['sha256']
 for a,b in [('USER_ITEM_20261018_R05424','USER_ITEM_20261018_R03440'),('USER_ITEM_20261018_R03419','USER_ITEM_20261018_R03409')]:assert aliases.get(a,a)!=aliases.get(b,b)
 pages={};textures={}
 for ident,e in m['items'].items():
  if e['page'] not in pages:pages[e['page']]=Image.open(ROOT/'app/src/main/assets/item-atlas-v210'/f'page-{e["page"]:02}.png').convert('RGBA')
  im=pages[e['page']].crop((e['x'],e['y'],e['x']+e['w'],e['y']+e['h']));digest=hashlib.sha256(str(im.size).encode()+im.tobytes()).hexdigest();assert digest==e['texture'],ident;textures[digest]=(e['page'],e['x'],e['y'])
 assert len(textures)==m['textureCount'];assert len(pages)==m['pages'];assert len(m['items'])>=2065
 print('V210_SOURCE_IDENTITY_ATLAS_PASS',len(j['records']),'identities',len(aliases),'aliases',len(textures),'unique textures',len(m['items']),'bindings')
if __name__=='__main__':main()
