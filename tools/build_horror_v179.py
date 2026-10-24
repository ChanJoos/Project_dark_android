"""Deterministic source-pixel assets. Original frames retained; no generated replacement monster art."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageOps
import json,hashlib
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/horror_v179';OUT=ROOT/'app/src/main/assets/horror-v179';OUT.mkdir(exist_ok=True,parents=True)
import zipfile
if not (SRC/'wm001.json').exists():
 with zipfile.ZipFile(SRC/'original_sources.zip') as z:z.extractall(SRC)
receipt=[]
for t in range(10):
 body='wm001' if t%2 else 'mm001';gear=('wu' if t%2 else 'mu')+['0000017','0000025','0000020','0000057','0000013'][t//2]
 sheet=Image.new('RGBA',(256,384));sources=[(json.loads((SRC/(i+'.json')).read_text()),Image.open(SRC/(i+'.webp')).convert('RGBA')) for i in [body,gear,('wh106' if t%2 else 'mh106')]]
 for row in range(6):
  for col,direction in enumerate(['NW','NE','SW','SE']):
   group='02' if row==5 else '01';index=col if row==5 else ([0,2,3,4,5][row] if col<2 else [1,6,7,8,9][row])
   cell=Image.new('RGBA',(64,64))
   base=sources[0][0]['sprites'][group][index];base_by=-base['py']*base['h']
   for meta,atlas in sources:
    frame=meta['sprites'][group][index];w,h=frame['w'],frame['h'];patch=atlas.crop((frame['sx'],frame['sy'],frame['sx']+w,frame['sy']+h));cell.alpha_composite(patch,(round(32-frame['px']*w),round(60+base_by-h+frame['py']*h)))
   # Source standing/walk groups store right-facing frames and their original mirrored directions.
   if row!=5 and col in [0,2]:cell=ImageOps.mirror(cell)
   assert cell.getbbox() and cell.getbbox()[0]>0 and cell.getbbox()[2]<64
   sheet.alpha_composite(cell,(col*64,row*64))
 sheet.save(OUT/f'human_{t}.png');receipt.append({'type':t,'body':body,'wardrobe':gear,'projection':'SOURCE_PIVOT_COMPOSITE / ADAPTED_MONSTER_WARDROBE','states':['idle','walk0','walk1','walk2','walk3','attack'],'directionOrder':['NW','NE','SW','SE']})
lobby=Image.open(SRC/'video_005.png').convert('RGBA');room=Image.open(SRC/'video_026.png').convert('RGBA')
for name,im,box in [('carpet',lobby,(850,350,946,398)),('wood',lobby,(620,47,684,59)),('wall',room,(823,98,847,144))]:im.crop(box).save(OUT/(name+'.png'))
# Isolated original furniture pixels, with explicit source-space polygon masks.
polys={
 'tiger':[(734,245),(750,246),(755,258),(783,265),(795,282),(807,289),(836,300),(838,308),(831,312),(815,306),(794,311),(783,324),(800,333),(795,341),(776,337),(769,328),(754,328),(749,334),(736,341),(718,341),(710,335),(704,326),(706,314),(714,304),(716,296),(704,286),(693,281),(689,277),(694,270),(707,273),(716,280),(729,285),(736,278),(745,272),(738,263),(730,260),(729,255)],
 'chair':[(595,401),(601,392),(614,397),(622,418),(636,430),(632,450),(623,458),(612,452),(606,455),(594,442)],
 'sofa':[(394,285),(402,270),(416,264),(430,269),(445,279),(467,280),(489,295),(489,325),(477,335),(451,326),(427,318),(401,307)],
 'table':[(310,326),(323,317),(347,312),(375,313),(400,322),(417,335),(418,347),(408,359),(382,369),(354,369),(330,362),(313,352),(307,339)],
 'door':[(650,70),(663,61),(677,66),(693,84),(705,114),(716,154),(700,166),(678,151),(657,129),(648,102)]}
for name,points in polys.items():
 mask=Image.new('L',room.size);ImageDraw.Draw(mask).polygon(points,fill=255);asset=room.copy();asset.putalpha(mask);asset=asset.crop(mask.getbbox());asset.save(OUT/(name+'.png'))
clean=Image.open(SRC/'video_116.png').convert('RGBA');mask=Image.new('L',clean.size);ImageDraw.Draw(mask).polygon([(298,234),(303,225),(315,225),(324,220),(331,215),(342,217),(350,212),(360,216),(365,225),(375,232),(375,255),(366,265),(344,270),(321,277),(302,270)],fill=255);clean.putalpha(mask);clean.crop(mask.getbbox()).save(OUT/'sofa.png')
manifest={'revision':'V179_HORROR_FIRST_PLAYABLE','video':{'library_file_id':'libfile_4b5c620ef9488191a7a6560282e1d363','timestamps':[5,26],'viewScale':'1170x540','unseenRooms':'ADAPTED_LAYOUTS_FROM_OBSERVED_THEMES'},'humans':receipt,'files':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.glob('*.png'))}}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');print('HORROR_ASSETS',len(manifest['files']))
