#!/usr/bin/env python3
"""Project capture matting; immutable Naver GIFs are never rewritten."""
import hashlib,json
from pathlib import Path
import numpy as np
from PIL import Image,ImageSequence
from scipy.ndimage import label
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'master/source/weapons/chungryong_490398'
OUT=ROOT/'app/src/main/assets/weapons/chungryong'
OUT.mkdir(parents=True,exist_ok=True)
names=['basic','twohand','horizontal','vertical']
# Source capture is west-facing. Reflect only the extracted weapon into the existing east-facing rig.
grips=[[(200,113),(238,75),(226,142),(190,110),(234,76),(202,118)],
       [(237,53),(190,96),(173,78),(214,120),(234,69),(186,97),(225,95),(191,134)],
       [(199,128),(228,78),(251,89),(210,125),(233,78),(208,125)],
       [(204,127),(240,58),(204,135),(205,126),(243,56),(196,137)]]
frames={};atlas=Image.new('RGBA',(8*180,4*180))
for row,name in enumerate(names):
 im=Image.open(SRC/(name+'.gif'));n=im.n_frames//2;records=[]
 for i,frame in enumerate(ImageSequence.Iterator(im)):
  rgb=np.array(frame.convert('RGB')).astype(int);r,g,b=rgb.transpose(2,0,1)
  mask=(b-r>=20)&(g-r>=5)&(b>=90)
  # All source weapon pixels lie within the actor capture region; labels/floor/UI are excluded.
  region=np.zeros(mask.shape,bool);region[0:180,100:330]=True;mask &= region
  labels,count=label(mask,np.ones((3,3)));sizes=np.bincount(labels.ravel());sizes[0]=0
  mask=labels==sizes.argmax();ys,xs=np.where(mask);box=[int(xs.min()),int(ys.min()),int(xs.max()+1),int(ys.max()+1)]
  rgba=np.zeros((*mask.shape,4),dtype=np.uint8);rgba[:,:,:3]=rgb;rgba[:,:,3]=mask*255;rgba[~mask,:3]=0
  sprite=Image.fromarray(rgba).crop(box).transpose(Image.Transpose.FLIP_LEFT_RIGHT)
  x=i*180;y=row*180;atlas.paste(sprite,(x,y))
  gx,gy=grips[row][i]
  records.append(dict(sx=x,sy=y,w=sprite.width,h=sprite.height,gripX=box[2]-gx,gripY=gy-box[1],sourceFrame=i,sourceDurationMs=frame.info.get('duration',300),sourceBox=box))
 frames[name]=dict(front=records[:n],back=records[n:])
atlas.save(OUT/'atlas.png')
# The labelled item window contains an actual inventory icon, including the red cord.
im=Image.open(SRC/'item.png').convert('RGB').crop((19,16,77,76));a=np.array(im).astype(int);r,g,b=a.transpose(2,0,1)
mask=((b-r>15)&(g-r>5))|((r>80)&(r-g>35))|((r>145)&(g>170)&(b>175))
rgba=np.zeros((*mask.shape,4),dtype=np.uint8);rgba[:,:,:3]=a;rgba[:,:,3]=mask*255;rgba[~mask,:3]=0
Image.fromarray(rgba).save(OUT/'icon.png')
data=dict(revision='CHUNGRYONG_V94',article='https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/490398?fromList=true&menuId=131&tc=cafe_article_list',title='『청룡의숨결』 각 직업 모션',author='아머드코어',published='2025-07-08 16:33',sourceScale=.60,frames=frames,evidence='V_COMMUNITY_CAPTURE / ADAPTED_COLOR_MATTING_AND_HAND_REGISTRATION',limitations=['cyan capture silhouette only; occluded grip/cord pixels cannot be recovered','source has front/back views; west/east reflection follows existing rig','source GIF holds are 300ms; combat duration/contact remains existing resolver','original event distribution, durability and 1~2 S/L damage are not invented as runtime rules'],sourceHashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in SRC.iterdir() if p.suffix in ['.gif','.png']})
(OUT/'manifest.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print('26 source weapon frames, actual source icon; immutable input hashes recorded')
