from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,json,hashlib
out=Path('mine203/assets');out.mkdir(exist_ok=True);spec=[]
def save(name,image,box,poly=None,anchor=None,clean=False,frame=None):
 im=image.crop(box).convert('RGBA');w,h=im.size;mask=Image.new('L',im.size,0);ImageDraw.Draw(mask).polygon(poly or [(0,0),(w,0),(w,h),(0,h)],fill=255);a=np.array(im);m=np.array(mask)
 if clean:
  rgb=a[:,:,:3].astype(int);green=(rgb[:,:,1]>rgb[:,:,0]*1.16)&(rgb[:,:,1]>rgb[:,:,2]*1.16)&(rgb[:,:,1]-np.minimum(rgb[:,:,0],rgb[:,:,2])>24);m[green]=0
 a[:,:,3]=m;im=Image.fromarray(a);im.save(out/(name+'.png'));spec.append(dict(id=name,size=[w,h],anchor=anchor or [w/2,h-2],frame=frame,box=box,polygon=poly,greenMask=clean,sha256=hashlib.sha256((out/(name+'.png')).read_bytes()).hexdigest()))
def f(n):return Image.open(f'mine203/frames/native{n:03d}.png')
save('house',Image.open('mine203/stitched.png'),[824,174,1146,442],[(0,135),(39,87),(223,0),(283,27),(322,119),(312,138),(311,208),(132,268),(5,217)],anchor=[76,242],clean=True,frame='SIFT aligned69..82; original compression retained; foreground post occlusion small at base')
save('lamp',f(68),[404,190,465,308],[(12,0),(20,0),(19,25),(58,13),(60,19),(44,28),(48,48),(48,57),(39,59),(35,53),(38,35),(19,33),(19,97),(28,114),(20,118),(13,110),(6,118),(0,114),(7,96),(10,37)],anchor=[13,116],frame=68)
save('bench',Image.open('mine203/stitched.png'),[755,386,806,430],[(0,23),(42,1),(51,7),(51,23),(10,44),(5,40),(5,30),(0,29)],clean=True,frame='69..82')
save('mine_hatch',f(37),[330,143,445,206],[(0,15),(33,0),(115,42),(80,61),(8,39)],anchor=[50,50],clean=True,frame=37)
save('cliff',f(50),[230,10,405,151],[(0,97),(175,10),(175,140),(141,139),(131,129),(130,106),(107,101),(101,87),(70,84),(70,79),(40,82),(20,86)],clean=True,anchor=[90,130],frame=50)
# Independent boulders and rock paving, not a screenshot used as a map.
for i,(box,poly) in enumerate([([196,217,240,262],[(0,16),(9,3),(23,0),(42,13),(44,32),(30,44),(11,38),(1,29)]),([242,222,287,266],[(0,11),(12,0),(28,4),(44,16),(44,29),(24,43),(5,35)]),([352,299,397,348],[(0,16),(8,5),(24,0),(44,17),(44,36),(25,48),(5,37)])]):save('rock'+str(i),f(68),box,poly,frame=68)
save('cobble',f(64),[380,174,505,247],[(0,40),(38,0),(125,30),(124,48),(57,73),(0,57)],clean=True,frame=64)
for name,n,box in [('grass',68,[178,121,234,149]),('grass2',64,[332,125,388,153]),('grass3',68,[152,140,208,168]),('water',68,[306,30,362,58]),('planks',32,[166,207,222,235])]:save(name,f(n),box,[(0,14),(28,0),(56,14),(28,28)],anchor=[28,14],frame=n)
# A source-visible bridge railing segment. No source character or UI admitted.
save('rail',f(32),[100,274,157,323],[(0,23),(56,0),(57,5),(48,10),(48,36),(43,38),(43,13),(25,23),(25,46),(21,49),(20,25),(4,34),(4,48),(0,49)],anchor=[28,42],frame=32)
Path('mine203/assets.json').write_text(json.dumps({'source':'Screen_Recording_20261020_160455_YouTube.mp4','sha256':hashlib.sha256(Path('upload/Screen_Recording_20261020_160455_YouTube.mp4').read_bytes()).hexdigest(),'canonicalFrame':'crop1920x1080at210,0 ->640x480; runtime scale8/7','evidence':'USER_VIDEO_SOURCE_PIXELS; layout/hidden joins ADAPTED','assets':spec},ensure_ascii=False,indent=2))
preview=Image.new('RGB',(1100,600),'#777');d=ImageDraw.Draw(preview)
x=y=0
for a in spec:
 im=Image.open(out/(a['id']+'.png'));im.thumbnail((330,270));
 if x+im.width>1100:x=0;y+=290
 preview.paste(im,(x,y+20),im);d.text((x+3,y+2),a['id'],fill='white');x+=max(im.width+15,90)
preview.save('mine203/asset-review.png')
