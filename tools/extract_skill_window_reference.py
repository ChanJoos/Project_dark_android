#!/usr/bin/env python3
"""Lossless source crops for the user-requested window skin; no generated art."""
import hashlib,json
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
source=ROOT/'master/source/skill_window/user_reference_20261001.jpg'
out=ROOT/'app/src/main/assets/skill-window';out.mkdir(exist_ok=True)
im=Image.open(source).convert('RGB')
for stale in out.glob('SK_*.png'):stale.unlink()
crops={
 'paper':[535,375,971,485], 'detail-paper':[1040,312,1440,387],
 'list-header':[57,80,1001,134], 'detail-header':[1011,79,1482,133],
 'left-edge':[55,220,62,895], 'right-edge':[996,220,1004,895],
 'bottom-edge':[65,928,995,937], 'detail-left':[1008,134,1016,554],
 'detail-right':[1478,134,1486,554], 'detail-bottom':[1016,554,1478,563],
 'icon-frame':[79,437,158,537], 'selected-frame':[254,598,336,699],
 'scroll-track':[980,202,1000,914], 'scroll-thumb':[980,487,1000,884],
 'scroll-up':[980,184,1000,203], 'scroll-down':[980,904,1000,926],
 'command-box':[1034,393,1465,445],
 'SK_도적_014':[86,280,151,345], 'SK_도적_015':[174,280,239,345],
 'SK_도적_016':[262,280,327,345], 'SK_도적_017':[350,280,415,345],
 'SK_도적_018':[438,280,503,345], 'SK_도적_019':[86,444,151,509],
 'SK_도적_020':[174,444,239,509], 'SK_도적_025':[263,606,328,671],
}
# Frames exclude their sample icon, counter and scrollbar knob. Keep only genuine border strips.
manifest={'source':'master/source/skill_window/user_reference_20261001.jpg','sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'size':list(im.size),'crops':{},'iconBindings':{},'limitations':['Only the selected trap is labelled directly in this screenshot. Circle-four/five order is cross-checked against the preserved catalog; keep this inference explicit.','Second-job rank counts are not an implemented progression system.','Window skin is extracted from this single user-selected reference, not certified as shipping licensed art.']}
for name,box in crops.items():
 image=im.crop(box)
 if name in ('icon-frame','selected-frame'):
  # Preserve border only; transparent interior is painted with the genuine paper texture at runtime.
  image=image.convert('RGBA')
  for y in range(image.height):
   for x in range(image.width):
    if 4<=x<image.width-4 and 5<=y<image.height-5:image.putpixel((x,y),(0,0,0,0))
 if name=='scroll-track':image=im.crop((982,214,998,472))
 filename='rogue_'+name.rsplit('_',1)[1] if name.startswith('SK_') else name
 path=out/(filename+'.png');image.save(path)
 manifest['crops'][name]={'box':box,'path':'skill-window/'+path.name,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
 if name.startswith('SK_'):manifest['iconBindings'][name]='skill-window/'+path.name
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print('Extracted',len(crops),'source crops')
