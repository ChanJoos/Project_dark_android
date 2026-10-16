"""Extract the user-requested reference skin; no AI repaint, items or baked stat values."""
from PIL import Image
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parents[1]
src=root/'master/source/equipment_window_v175/reference.jpg'
im=Image.open(src).convert('RGB').crop((94,478,616,1034))
original=im.copy()
out=root/'app/src/main/assets/equipment-window-v176';out.mkdir(exist_ok=True)
# Preserve the screenshot header, rivets, frame, stat labels and number-box bevels.
# Replace the equipment/actor area with its own exposed woven background.
texture=im.crop((212,145,244,177))
def paper(box):
 x0,y0,x1,y1=box
 for y in range(y0,y1):
  for x in range(x0,x1):im.putpixel((x,y),texture.getpixel(((x-212)%32,(y-145)%32)))
paper((24,45,503,449))
# Exact bevel pixels from this reference's weapon cell; remove only its baked icon.
slot=original.crop((109,216,179,286))
for y in range(5,65):
 for x in range(5,65):
  rgb=texture.getpixel((x%32,y%32));slot.putpixel((x,y),tuple(round(v*.57) for v in rgb))
slot.save(out/'slot.png')
# Remove screenshot numeric values, leaving the original six stat label/box pixels.
for x,y in [(110,480),(110,516),(258,480),(258,516),(412,480),(412,516)]:
 for yy in range(y+3,y+27):
  for xx in range(x+4,x+39):im.putpixel((xx,yy),(27,21,15))
im.paste(original.crop((430,54,493,107)),(430,54))
# Replace the lower-right hinge with clean source background.
im.paste(original.crop((480,434,522,472)),(480,510))
im.save(out/'panel.png')
manifest={'source':'master/source/equipment_window_v175/reference.jpg','sourceSha256':hashlib.sha256(src.read_bytes()).hexdigest(),'crop':[94,478,616,1034],'size':[522,556],'policy':'USER V176: shield below ornaments;regular15slots;fitted visible icons;empty sockets;source bronze close;lower-right hinge removed. Exposed texture patch replaces omitted source cells and baked actor/items. No invented ornament items.','files':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.glob('*.png'))}}
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
