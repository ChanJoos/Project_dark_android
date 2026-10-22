"""Observed continuous promotion FX from the second supplied recording.
Colour/region masks exclude filmed actors and UI; occluded pixels stay unknown.
"""
from pathlib import Path
import argparse,subprocess,json,hashlib
import numpy as np
from PIL import Image
from scipy import ndimage
ROOT=Path(__file__).resolve().parents[1]
def decode(video,t,d,box):
 x,y,w,h=box
 raw=subprocess.check_output(['ffmpeg','-v','error','-ss',str(t),'-i',str(video),'-t',str(d),'-vf',f'fps=20,crop={w}:{h}:{x}:{y}','-f','rawvideo','-pix_fmt','rgb24','-threads','1','-'])
 return np.frombuffer(raw,np.uint8).reshape(-1,h,w,3).copy()
def main():
 p=argparse.ArgumentParser();p.add_argument('video',type=Path);a=p.parse_args()
 out=ROOT/'app/src/main/assets/skill-presentation/mage-v239';out.mkdir(exist_ok=True)
 source=hashlib.sha256(a.video.read_bytes()).hexdigest();skills={};rows=[]
 specs=[(51,'delis',51.35,1.15,(780,180,400,400),165,190,.35),
        (52,'fortress',56.9,1.15,(780,180,400,400),165,190,.35),
        (39,'ice',62.25,.60,(900,80,260,350),115,270,.35),
        (41,'storm',70.30,.55,(800,130,360,330),170,220,.30),
        (42,'flare',78.40,.55,(820,80,230,350),110,270,.35)]
 for ident,name,t,d,box,px,py,scale in specs:
  raw=decode(a.video,t,d,box);z=raw.astype(float);r,g,b=z[:,:,:,0],z[:,:,:,1],z[:,:,:,2]
  h,w=raw.shape[1:3];yy,xx=np.mgrid[:h,:w]
  plate=np.percentile(raw,15,axis=0).astype(float)
  if ident in (51,52):
   plate=np.median(decode(a.video,53,.35,box),axis=0)
   diff=z-plate[None];keep=(diff.max(axis=-1)>12)&(b>r*.65)&(b>g*.65)&(b>80)
   # Stable filmed actor/weapon core, explicit occlusion rather than invented fill.
   core=((xx-161)/22)**2+((yy-191)/56)**2<1
   keep[:,core]=False;keep[:,:76]=False
   support=((xx-161)/100)**2+((yy-178)/135)**2<1;keep &=support[None]
  elif ident==39:
   keep=(b>155)&(b>r*1.10)&(b>g*1.02)&(g>95)
   keep &= (((xx-115)/108)**2+((yy-142)/140)**2<1)[None]
   keep[:,220:]=False
  elif ident==41:
   keep=(g>205)&(g>r*.95)&(b>115)&(g>b*.90)
   keep &= (((xx-170)/154)**2+((yy-147)/144)**2<1)[None]
  elif ident==42:
   keep=(r>225)&(g>155)&(b>65)&(r>g*.96)&(g>b*1.1)
   keep &= (((xx-110)/88)**2+((yy-150)/137)**2<1)[None]
  else:
   keep=(r>115)&(abs(r-g)<26)&(abs(g-b)<27)&((z-plate[None]).max(axis=-1)>26)
   keep &= (((xx-130)/110)**2+((yy-132)/104)**2<1)[None]
  # No health bars, digits or disconnected compression speckle.
  keep[:,:12]=False
  for i in range(len(keep)):
   lab,n=ndimage.label(keep[i]);sizes=np.bincount(lab.ravel());sizes[0]=0
   keep[i]&=sizes[lab]>=(100 if ident==41 else 8)
  rgba=np.zeros((*raw.shape[:3],4),np.uint8)
  signal=np.clip((z-plate[None]-3)/(255-plate[None]).clip(20)*255,0,255).astype(np.uint8)
  rgba[:,:,:,:3]=signal;rgba[:,:,:,3]=keep.astype(np.uint8)*255;rgba[~keep]=0
  count=len(rgba);cols=8;atlas=Image.new('RGBA',(cols*w,((count+cols-1)//cols)*h))
  for i,f in enumerate(rgba):atlas.paste(Image.fromarray(f),(i%cols*w,i//cols*h))
  dest=out/(name+'.png');atlas.save(dest)
  assert np.count_nonzero(rgba[:,:,:,3])>100,(name,'empty sequence')
  channel=dict(anchor='RECIPIENT',path='mage-v239/'+dest.name,width=w,height=h,columns=cols,durationsMs=[50]*count,pivotX=px,pivotY=py,scale=scale,blend='SCREEN',footOffsetY=0,sourceTiming='OBSERVED_CAPTURE_20FPS',atlasSha256=hashlib.sha256(dest.read_bytes()).hexdigest())
  skills[f'SK_마법사_{ident:03}']={'channels':{'RECIPIENT_CONTACT':channel}}
  rows.append(dict(id=f'SK_마법사_{ident:03}',seconds=t,duration=d,crop=box,sourceSha256=source,visiblePixels=int(np.count_nonzero(rgba[:,:,:,3])),evidence='ADAPTED_CAPTURE_MATTE; occluded pixels not reconstructed'))
 (out/'manifest.json').write_text(json.dumps(dict(revision='USER_VIDEO_V239',skills=skills),ensure_ascii=False,indent=2)+'\n')
 src=ROOT/'master/source/mage_v239';src.mkdir(exist_ok=True)
 (src/'receipt.json').write_text(json.dumps(dict(source=a.video.name,sha256=source,sequences=rows,remaining='Other named debuffs and the first video lower-circle casts remain evidence inventory; no unsupported mechanical grants.'),ensure_ascii=False,indent=2)+'\n')
 print('MAGE_V239',[(r['id'],r['visiblePixels']) for r in rows])
if __name__=='__main__':main()
