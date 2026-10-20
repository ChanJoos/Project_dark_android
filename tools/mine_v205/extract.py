"""Source-only crop/mattes. Run from workspace after frame extraction/alignment.
No synthesized art; native source pixels. Raw phone footage remains private.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,json,hashlib
root=Path('project_dark');out=root/'app/src/main/assets/mine-v205';out.mkdir(exist_ok=True);spec=[]
def crop(name,im,box,points,clean=False):
 a=np.array(im.crop(box).convert('RGBA'));m=Image.new('L',(a.shape[1],a.shape[0]));ImageDraw.Draw(m).polygon([(x-box[0],y-box[1]) for x,y in points],fill=255);alpha=np.array(m)
 if clean:
  r,g,b=a[:,:,:3].astype(float).transpose(2,0,1);alpha[(g>r*1.15)&(g>b*1.18)&(g>50)]=0
 if name.startswith('bridge_') and name!='bridge_deck':
  r,g,b=a[:,:,:3].astype(float).transpose(2,0,1);alpha[(b>r*1.3)&(b>g*1.08)]=0
 a[:,:,3]=alpha;a[alpha==0,:3]=0;p=out/(name+'.png');Image.fromarray(a).save(p);spec.append({'id':name,'box':box,'polygon':points,'greenMatte':clean,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
def f(n):return Image.open(f'mine205/frames/{n:03}.png')
b=Image.open('mine205/bridge-scene.png')
crop('bridge_deck',b,(373,108,923,418),[(390,331),(734,169),(884,245),(548,412)])
crop('bridge_back',b,(373,108,923,418),[(376,298),(387,294),(390,308),(749,130),(750,114),(764,108),(768,130),(760,150),(755,153),(755,176),(746,180),(746,154),(394,332),(380,338)],True)
crop('bridge_front',b,(373,108,923,418),[(544,366),(555,362),(555,376),(901,203),(900,187),(913,184),(921,196),(916,223),(910,229),(907,254),(896,257),(896,231),(562,406),(555,418),(544,418)],True)
# Entire original cave mouth and surrounding rock, no UI or character. Upper edge is a source gap.
crop('cave',Image.open('mine205/cave-scene.png'),(0,0,398,317),[(0,29),(56,8),(133,8),(143,0),(252,0),(309,40),(333,63),(349,83),(386,104),(397,120),(360,139),(333,151),(329,166),(295,182),(280,183),(257,180),(232,203),(211,219),(193,229),(178,245),(150,252),(130,261),(107,271),(90,282),(70,286),(41,303),(0,317)])
crop('paving0',f(50),(103,185,172,225),[(103,192),(121,185),(146,187),(166,201),(172,212),(155,225),(133,224),(113,212)],True)
crop('paving1',f(65),(42,32,113,78),[(43,47),(66,33),(94,38),(112,57),(92,76),(63,69)],True)
crop('paving2',f(65),(359,210,411,244),[(359,220),(380,210),(401,214),(410,230),(391,242),(371,237)],True)
# Foam attached to shore stones, with source-facing contours retained.
for n,(box,poly) in enumerate([
 ((151,208,200,260),[(155,225),(161,215),(173,210),(183,215),(190,227),(196,239),(199,254),(184,258),(162,245),(151,237)]),
 ((197,223,247,276),[(198,244),(207,230),(221,223),(235,231),(242,244),(239,257),(246,269),(231,275),(209,263),(198,255)]),
 ((239,246,286,299),[(240,265),(249,251),(261,247),(273,255),(281,270),(282,284),(285,293),(268,298),(249,284),(239,275)])]):
 crop('shore'+str(n),f(65),box,poly,True)
 p=out/('shore'+str(n)+'.png');a=np.array(Image.open(p));r,g,b=a[:,:,:3].astype(float).transpose(2,0,1);a[(b>r*1.2)&(b>g*1.05),3]=0;a[a[:,:,3]==0,:3]=0;Image.fromarray(a).save(p);spec[-1]['sha256']=hashlib.sha256(p.read_bytes()).hexdigest()
for n,box in enumerate([(128,299,177,337),(60,278,98,309),(230,365,271,395)]):
 x,y,X,Y=box;crop('riverrock'+str(n),f(65),box,[(x+3,y+13),(x+13,y+3),(X-13,y+3),(X-3,y+15),(X-1,Y-7),(X-15,Y-1),(x+3,Y-7)])
 p=out/('riverrock'+str(n)+'.png');a=np.array(Image.open(p));r,g,b=a[:,:,:3].astype(float).transpose(2,0,1);a[(b>r*1.2)&(b>g*1.05),3]=0;a[a[:,:,3]==0,:3]=0;Image.fromarray(a).save(p);spec[-1]['sha256']=hashlib.sha256(p.read_bytes()).hexdigest()
crop('tree',f(38),(177,29,239,160),[(178,49),(188,32),(205,29),(226,35),(237,58),(229,77),(231,92),(217,105),(213,142),(218,157),(205,160),(203,105),(187,98),(180,79)])
# Small foliage at original cave foot; no new mining machinery invented.
reg={'source':'Screen_Recording_20261020_160455_YouTube.mp4','sourceSha256':hashlib.sha256(Path('upload/Screen_Recording_20261020_160455_YouTube.mp4').read_bytes()).hexdigest(),'frameConvention':'1fps, firstframe=001; crop1920x1080at210,0 then640x480','bridge':'5fps frames135..164 registered with source-feature translations; separate deck/back/front rail, source coordinates preserved','evidence':'USER_VIDEO_SOURCE_PIXELS; compact map placement and unseen cliff cap ADAPTED','assets':spec}
p=root/'master/source/mine_v205';p.mkdir(parents=True,exist_ok=True);(p/'assets.json').write_text(json.dumps(reg,ensure_ascii=False,indent=2))
