"""Reproduce the reviewed reference-reconstruction crops; no synthetic directions."""
from pathlib import Path
from PIL import Image
import hashlib,json,zipfile
root=Path(__file__).resolve().parents[1]
source=root/'master/source/abel_v141/monsters.png'
im=Image.open(source).convert('RGBA')
bands=[(16,239),(246,476),(484,690),(745,898),(947,1095),(1147,1265),(1285,1458),(1480,1658),(1688,1856)]
frames=[]
for family,start in [('arkpus',0),('pong',3),('gas',6)]:
 cells=[]
 for state,row in zip(('idle','walk','attack'),range(start,start+3)):
  for col,direction in enumerate(('nw','ne','sw','se')):
   y0,y1=bands[row];cell=im.crop((col*209,y0,(col+1)*209,y1))
   box=cell.getchannel('A').getbbox();assert box
   cells.append((state,direction,cell.crop(box)))
 ratio=min(176/max(c.width for _,_,c in cells),176/max(c.height for _,_,c in cells))
 for state,direction,cell in cells:
  cell=cell.resize((round(cell.width*ratio),round(cell.height*ratio)),Image.Resampling.NEAREST)
  out=Image.new('RGBA',(192,192));out.alpha_composite(cell,((192-cell.width)//2,184-cell.height))
  import io
  data=io.BytesIO();out.save(data,format='PNG');data=data.getvalue()
  name=f'pote_monsters_generated_v1/sprites/abel_v141/{family}/{state}_{direction}.png'
  frames.append((name,data))
dest=root/'master/assets/abel_v141';dest.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(dest/'generated_assets.zip','w',zipfile.ZIP_DEFLATED) as z:
 for name,data in sorted(frames):
  entry=zipfile.ZipInfo(name,(2026,10,11,0,0,0));entry.compress_type=zipfile.ZIP_DEFLATED;z.writestr(entry,data)
manifest={'evidence':'ADAPTED_GENERATED_REFERENCE_RECONSTRUCTION','sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),'frames':{name:hashlib.sha256(data).hexdigest() for name,data in frames},'canvas':[192,192],'groundY':184,'bands':bands,'status':'12 still poses per family; generated directions and original animation fidelity await visual acceptance'}
(dest/'provenance.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print('V141 sprites:',len(frames))
