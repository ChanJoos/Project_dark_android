#!/usr/bin/env python3
"""Reproducible lossless user-reference window skin crops; item identities stay in RPG."""
from pathlib import Path
import json,hashlib
from PIL import Image
R=Path(__file__).resolve().parents[1];out=R/'app/src/main/assets/item-window';out.mkdir(exist_ok=True)
specs={'equipment':{'paper':(400,1330,600,1380),'header':(28,30,320,126),'edge':(2,205,10,1470),'bottom':(20,1505,940,1518),'slot':(201,464,314,579)},'inventory':{'detail':(225,756,500,787),'top':(602,148,930,213),'left':(75,610,84,1120),'bottom-detail':(90,1136,725,1148),'cell':(954,289,1039,369),'all':(640,220,691,268),'gear':(723,220,776,268),'use':(807,219,860,268)}}
manifest={'sources':{},'crops':{},'limitations':['User-selected window pixels only; screenshot equipment/items/stats are not inserted as owned game items.','Mobile panel geometry is adapted to landscape. Item capacity/expansion, original durability and character grade are not fabricated.']}
for kind,parts in specs.items():
 source=R/f'master/source/item_windows/{kind}_reference_20261001.jpg';im=Image.open(source).convert('RGB');manifest['sources'][kind]={'path':str(source.relative_to(R)),'sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'size':list(im.size)}
 for key,box in parts.items():
  crop=im.crop(box)
  if key=='slot':
   crop=crop.convert('RGBA')
   for y in range(crop.height):
    for x in range(crop.width):
     if 7<=x<crop.width-7 and 7<=y<crop.height-7:crop.putpixel((x,y),(0,0,0,0))
  path=out/f'{key}.png';crop.save(path);manifest['crops'][key]={'path':'item-window/'+path.name,'box':list(box),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print('Extracted',len(manifest['crops']),'item-window source crops')
