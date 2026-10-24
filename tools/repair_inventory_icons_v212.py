"""Repair only source socket alpha; preserve visible original RGB and immutable source archives."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,zipfile,io,hashlib
import numpy as np
from scipy.ndimage import label,binary_dilation,binary_fill_holes
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/items/naver_bundle_20261019';OUT=ROOT/'app/src/main/assets/item-repairs-v212';OUT.mkdir(parents=True,exist_ok=True)
rows=json.loads((SRC/'reviewed_foods.json').read_text());manifest={};icons=[];audit=[]
with zipfile.ZipFile(SRC/'archives/293294.zip') as z:raw=z.read('293294/source_01.png')
source=Image.open(io.BytesIO(raw)).convert('RGB');crops=[np.array(source.crop((x,y,x+w,y+h))) for r in rows for x,y,w,h in [r['crop']]]
# Repeated cells share the source socket texture; learn its per-position RGB from the table itself.
# Match each crop against BOTH source-table region medians rather than remove every brown pixel.
sections={section:np.median([crops[i]for i,r in enumerate(rows)if r['section']==section],axis=0).astype('uint8') for section in {r['section']for r in rows}}
for i,(row,a) in enumerate(zip(rows,crops)):
 # Foreground seed is the source item's lit material, well above the dark socket texture.
 # Restore enclosed dark material and one pixel of the original outline; never recolor the object.
 rr,gg,bb=[a[:,:,c].astype(int)for c in range(3)]
 mask=(a.max(axis=2)>90)|((gg>35)&(gg>=rr*.95))|((bb>45)&(bb>=rr*.85))
 mask[:2,:]=False;mask[-2:,:]=False;mask[:,:2]=False;mask[:,-2:]=False
 labs,n=label(mask,np.ones((3,3)));sizes=np.bincount(labs.ravel());sizes[0]=0
 if not n:raise ValueError(row['itemId'])
 main=labs==sizes.argmax();near=binary_dilation(main,iterations=3);seeds=main.copy()
 for k in range(1,n+1):
  if sizes[k]>=2 and np.any((labs==k)&near):seeds|=labs==k
 keep=binary_fill_holes(binary_dilation(seeds,iterations=1))
 keep[:2,:]=False;keep[-2:,:]=False;keep[:,:2]=False;keep[:,-2:]=False
 im=Image.fromarray(a).convert('RGBA');im.putalpha(Image.fromarray((keep*255).astype('uint8')));icons.append((row['itemId'],im))
 audit.append(dict(itemId=row['itemId'],sourceArchive='archives/293294.zip',sourceMember='293294/source_01.png',sourceSha256=hashlib.sha256(raw).hexdigest(),crop=row['crop'],mask='REVIEWED_LIT_FOREGROUND_LOCAL_MATTE_ENCLOSED_DARK_MATERIAL_SOURCE_RGB',foregroundPixels=int(keep.sum())))
# Rapid icons contain compressed frame/wood: restrict a source-local interior matte to silver/gold wing pixels.
full=json.loads((ROOT/'app/src/main/assets/item-icons/manifest.json').read_text())['items']
with zipfile.ZipFile(SRC/'archives/259752.zip')as z:
 for ident,row in full.items():
  if not row['assetPath'].startswith('item-icons/rapid_v185_'):continue
  raw=z.read(row['sourceMember']);a=np.array(Image.open(io.BytesIO(raw)).convert('RGB'));rr,gg,bb=[a[:,:,c].astype(int)for c in range(3)]
  yy,xx=np.mgrid[:a.shape[0],:a.shape[1]];inside=(xx>=5)&(xx<31)&(yy>=8)&(yy<30)
  bright=(np.maximum(gg,bb)>=100)&(np.maximum(gg,bb)>=rr*.62)&inside
  labs,n=label(bright,np.ones((3,3)));sizes=np.bincount(labs.ravel());sizes[0]=0
  keep=np.zeros_like(bright)
  for k in range(1,n+1):
   if sizes[k]>=3:keep|=labs==k
  # One-pixel source outline; pixels remain source RGB, never painted or recolored.
  keep=binary_dilation(keep,iterations=1)&inside&((rr+gg+bb)>75)
  im=Image.fromarray(a).convert('RGBA');im.putalpha(Image.fromarray((keep*255).astype('uint8')));icons.append((ident,im));audit.append(dict(itemId=ident,sourceArchive=row['sourceArchive'],sourceMember=row['sourceMember'],sourceSha256=hashlib.sha256(raw).hexdigest(),mask='REVIEWED_INTERIOR_WING_MATTE_SOURCE_RGB',foregroundPixels=int(keep.sum())))
sheet=Image.new('RGBA',(512,((len(icons)+7)//8)*48));review=Image.new('RGB',(640,((len(icons)+7)//8)*82),(65,80,90));draw=ImageDraw.Draw(review)
for i,(ident,im)in enumerate(icons):
 x=i%8*64;y=i//8*48;sheet.paste(im,(x,y));manifest[ident]=dict(x=x,y=y,w=im.width,h=im.height)
 b=im.resize((64,64),Image.Resampling.NEAREST);review.paste(b,(i%8*80,i//8*82),b);draw.text((i%8*80,i//8*82+64),ident[-6:],fill='white')
sheet.save(OUT/'icons.png');(OUT/'manifest.json').write_text(json.dumps(manifest,separators=(',',':'))+'\n');dest=ROOT/'master/source/items/icon_repairs_v212.json';dest.write_text(json.dumps(dict(status='SOURCE_ALPHA_REPAIR_CANDIDATE',items=audit,atlasSha256=hashlib.sha256((OUT/'icons.png').read_bytes()).hexdigest()),ensure_ascii=False,indent=2)+'\n');review.save('/workspace/scratch/ada883d05015/delivery/icons_after.png');print('REPAIRED',len(icons))
