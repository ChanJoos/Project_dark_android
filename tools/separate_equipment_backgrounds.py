"""Keep source RGB unchanged; remove captured sockets using reviewable alpha masks."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,hashlib
from collections import Counter
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/'app/src/main/assets/equipment-icons'
RAW=ROOT/'master/assets/equipment/accessories_20261009'
MASK=ROOT/'master/assets/equipment/background_masks_v126'
# Hand-traced source silhouettes for JPEG screenshots with unrelated frames.
# Points refer to original pixels, not resized/prettified renderings.
POLYGONS={
 'minimob_0.png':[(6,9),(13,9),(17,11),(23,11),(26,15),(24,18),(20,19),(18,24),(14,29),(10,28),(10,23),(6,23),(6,17)],
 'minimob_2.png':[(19,3),(27,3),(29,6),(28,11),(26,14),(30,18),(29,23),(27,26),(26,30),(21,32),(17,29),(13,31),(9,29),(6,26),(7,21),(12,17),(18,17),(20,12),(23,8),(23,6),(19,6)],
 'minimob_5.png':[(3,14),(4,10),(13,8),(25,7),(29,11),(31,15),(30,21),(26,24),(22,24),(18,26),(13,25),(10,28),(5,27),(3,24)],
 'minimob_7.png':[(2,14),(5,10),(9,6),(16,3),(20,2),(22,4),(26,4),(29,7),(30,10),(34,11),(33,18),(33,23),(30,27),(26,29),(24,31),(19,30),(16,33),(12,31),(10,30),(7,26),(6,22),(3,20)],
 'minimob_9.png':[(7,13),(9,10),(12,8),(15,8),(19,9),(22,9),(25,11),(26,14),(26,17),(29,20),(30,22),(28,23),(31,26),(31,28),(27,30),(23,30),(19,27),(15,27),(13,30),(10,30),(7,33),(4,32),(5,28),(8,25),(11,24),(15,23),(17,22),(21,23),(23,24),(24,21),(23,18),(20,17),(17,17),(14,19),(9,18),(7,16)],
}
HOLES={
 'minimob_2.png':[[(21,7),(25,6),(26,9),(23,14),(21,17),(19,17),(21,12)]],
 'minimob_7.png':[[(10,16),(14,12),(19,8),(23,7),(28,9),(26,15),(24,18),(20,21),(16,22),(13,20)]],
 'minimob_9.png':[[(11,13),(15,12),(21,12),(23,14),(20,15),(16,15),(12,16)]],
}

def main():
 MASK.mkdir(parents=True,exist_ok=True)
 m=json.loads((APP/'manifest.json').read_text())
 names=sorted({r['asset'] for r in m['items'].values() if (RAW/r['asset']).exists()})
 small=[Image.open(RAW/n).convert('RGBA') for n in names if Image.open(RAW/n).size==(32,32)]
 # Background samples at the same pixel position across independent originals.
 # This keeps thin rings/chains, black outlines and brown boot body colours.
 modes=[]
 for y in range(32):
  for x in range(32):
   modes.append(Counter(im.getpixel((x,y))[:3] for im in small).most_common(1)[0][0])
 bg=Image.new('RGB',(32,32));bg.putdata(modes);bg.save(MASK/'source_socket_consensus.png')
 audit=[]
 for name in names:
  src=RAW/name;im=Image.open(src).convert('RGBA');mask=Image.new('L',im.size,255)
  if name in POLYGONS:
   mask=Image.new('L',im.size,0);ImageDraw.Draw(mask).polygon(POLYGONS[name],fill=255)
   for hole in HOLES.get(name,[]):ImageDraw.Draw(mask).polygon(hole,fill=0)
   method='REVIEWED_SOURCE_POLYGON'
  else:
   for y in range(im.height):
    for x in range(im.width):
     rgb=im.getpixel((x,y))[:3]
     if im.size==(32,32):
      sample=modes[y*32+x]
      is_bg=max(abs(rgb[i]-sample[i]) for i in range(3))<=12
     else:
      # Three differently cropped historical sockets use the same brown palette.
      is_bg=any(max(abs(rgb[i]-b[i]) for i in range(3))<=10 for b in set(modes))
     if is_bg:mask.putpixel((x,y),0)
   method='SOURCE_SOCKET_SPATIAL_MATCH' if im.size==(32,32) else 'SOURCE_SOCKET_PALETTE_MATCH'
  # Source capture frame must never follow the item into another UI socket.
  for x in range(im.width):mask.putpixel((x,0),0);mask.putpixel((x,im.height-1),0)
  for y in range(im.height):mask.putpixel((0,y),0);mask.putpixel((im.width-1,y),0)
  # Quantized captures leave detached brown grain; it is socket texture, not art.
  visited=set()
  for yy in range(im.height):
   for xx in range(im.width):
    if (xx,yy) in visited or not mask.getpixel((xx,yy)):continue
    todo=[(xx,yy)];visited.add((xx,yy));component=[]
    while todo:
     x,y=todo.pop();component.append((x,y))
     for dx,dy in [(-1,0),(1,0),(0,-1),(0,1),(-1,-1),(-1,1),(1,-1),(1,1)]:
      q=(x+dx,y+dy)
      if 0<=q[0]<im.width and 0<=q[1]<im.height and q not in visited and mask.getpixel(q):visited.add(q);todo.append(q)
    def socket_colour(q):
     r,g,b=im.getpixel(q)[:3]
     return 45<=r<=135 and 25<=g<=100 and 10<=b<=65 and r>g>b and .52<g/max(1,r)<.86 and .30<b/max(1,g)<.84
    if all(socket_colour(q) for q in component):
     for q in component:mask.putpixel(q,0)
  mask_path=MASK/name;mask.save(mask_path)
  im.putalpha(mask);im.save(APP/name)
  kept=sum(v!=0 for v in mask.getdata());assert 5<kept<im.width*im.height*.85,(name,kept)
  audit.append({'asset':name,'mask':str(mask_path.relative_to(ROOT)),'method':method,'foregroundPixels':kept,'totalPixels':im.width*im.height})
  for r in m['items'].values():
   if r['asset']!=name:continue
   r.update(projection='SOURCE_RGB_WITH_SEPARATE_ALPHA_MASK',rawPixelAsset=str(src.relative_to(ROOT)),alphaMask=str(mask_path.relative_to(ROOT)),alphaMaskSha256=hashlib.sha256(mask_path.read_bytes()).hexdigest(),assetSha256=hashlib.sha256((APP/name).read_bytes()).hexdigest(),backgroundRemoval=method)
 m['revision']='SOURCE_EQUIPMENT_SEPARATE_BACKGROUND_V126'
 (APP/'manifest.json').write_text(json.dumps(m,ensure_ascii=False,indent=2)+'\n')
 (MASK/'manifest.json').write_text(json.dumps({'policy':'Original RGB preserved at every pixel; only alpha changed. Source pixels, masks and methods retained.','items':audit},ensure_ascii=False,indent=2)+'\n')
 print('SEPARATE_ICON_BACKGROUNDS',len(audit))
if __name__=='__main__':main()
