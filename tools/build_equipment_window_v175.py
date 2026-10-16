"""Extract the user-requested reference skin; no AI repaint, items or baked stat values."""
from PIL import Image
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parents[1]
src=root/'master/source/equipment_window_v175/reference.jpg'
im=Image.open(src).convert('RGB').crop((94,478,616,1034))
original=im.copy()
out=root/'app/src/main/assets/equipment-window-v175';out.mkdir(exist_ok=True)
# Preserve the screenshot header, rivets, frame, stat labels and number-box bevels.
# Replace the equipment/actor area with its own exposed woven background.
texture=im.crop((212,145,244,177))
def paper(box):
 x0,y0,x1,y1=box
 for y in range(y0,y1):
  for x in range(x0,x1):im.putpixel((x,y),texture.getpixel(((x-212)%32,(y-145)%32)))
paper((24,45,503,449))
# Empty source bevel from the already-preserved original equipment source.
slot=Image.open(root/'app/src/main/assets/item-window/slot.png').convert('RGB').resize((70,70),Image.Resampling.LANCZOS)
slot.save(out/'slot.png')
# Remove screenshot numeric values, leaving the original six stat label/box pixels.
for x,y in [(110,480),(110,516),(258,480),(258,516),(412,480),(412,516)]:
 for yy in range(y+3,y+27):
  for xx in range(x+4,x+39):im.putpixel((xx,yy),(27,21,15))
im.paste(original.crop((430,54,493,107)),(430,54))
im.save(out/'panel.png')
manifest={'source':'master/source/equipment_window_v175/reference.jpg','sourceSha256':hashlib.sha256(src.read_bytes()).hexdigest(),'crop':[94,478,616,1034],'size':[522,556],'policy':'USER supplied 2026-10-16: exact screenshot skin, only 15 functional slots; live actor/items/FinalStats. Exposed texture patch replaces omitted source cells and baked actor/items. No invented ornament items.','files':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.glob('*.png'))}}
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
