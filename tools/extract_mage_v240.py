"""User-confirmed Pote basic4; recorded timing/pixels, occlusions stay unknown."""
from pathlib import Path
import json,hashlib,subprocess,argparse
import numpy as np
from PIL import Image,ImageDraw
from scipy import ndimage
R=Path(__file__).resolve().parents[1]
def decode(v,t,d,box):
 x,y,w,h=box
 b=subprocess.check_output(['ffmpeg','-v','error','-ss',str(t),'-i',str(v),'-t',str(d),'-vf',f'fps=20,crop={w}:{h}:{x}:{y}','-f','rawvideo','-pix_fmt','rgb24','-threads','1','-'])
 return np.frombuffer(b,np.uint8).reshape(-1,h,w,3)
def main():
 p=argparse.ArgumentParser();p.add_argument('video',type=Path);a=p.parse_args();digest=hashlib.sha256(a.video.read_bytes()).hexdigest()
 out=R/'app/src/main/assets/skill-presentation/mage-v240';out.mkdir(exist_ok=True);skills={};receipt=[];preview=Image.new('RGB',(1120,680),'#454b48');draw=ImageDraw.Draw(preview)
 specs=[(1,'mareno',53.45,.70,53.10,(1390,615,150,150),70,135),(3,'teramiko',54.30,.65,54.05,(1100,440,160,140),70,110),(4,'adyuro',57.25,.65,56.95,(1390,615,150,150),70,135),(5,'plamo',60.30,.75,60.05,(1290,560,160,150),60,120)]
 for row,(n,name,t,d,bt,box,px,py) in enumerate(specs):
  raw=decode(a.video,t,d,box);crops=[list(box)]*len(raw)
  if n==5:
   # Follow the only nearby yellow recipient bar when the camera pans at60.8s.
   wide=(1200,470,500,360);frames=decode(a.video,t,d,wide);aligned=[];crops=[]
   for f in frames:
    rr,gg,bb=[f[:,:,k].astype(float) for k in range(3)];mask=(rr>140)&(gg>105)&(bb<180)&(abs(rr-gg)<100)
    labels,_=ndimage.label(mask);bars=[]
    for sl in ndimage.find_objects(labels):
     if sl is None:continue
     yy,xx=sl
     if xx.stop-xx.start>40 and yy.stop-yy.start<=10 and yy.start<110:bars.append((xx.start,yy.start))
    assert bars,'missing fire recipient bar'
    bx,by=min(bars,key=lambda p:abs(p[0]-160)+abs(p[1]-55));left=bx-60;top=by+28
    assert left>=0 and top+150<=360
    aligned.append(f[top:top+150,left:left+160]);crops.append([wide[0]+left,wide[1]+top,160,150])
   raw=np.stack(aligned);px,py=75,124
  z=raw.astype(float);bg=np.median(decode(a.video,bt,.15,tuple(crops[0])),axis=0);r,g,b=z[:,:,:,0],z[:,:,:,1],z[:,:,:,2]
  diff=z-bg[None];keep=diff.max(-1)>18
  if n==1:keep&=(b>130)&(g>115)&(b>r*1.12)&(g>r*1.10)
  elif n==3:
   keep&=(r>125)&(g>110)&(abs(r-g)<40)&(abs(g-b)<45);keep[:,:25]=False
  elif n==4:keep&=(b>170)&(g>170)&(r>145)&(abs(g-b)<45)
  else:keep&=(r>210)&(g>190)&(g>b*1.2)
  # Crop lies below all labels/HP/digits. Reject isolated codec noise.
  for i in range(len(keep)):
   labels,_=ndimage.label(keep[i]);sizes=np.bincount(labels.ravel());sizes[0]=0;keep[i]&=sizes[labels]>=4
  rgba=np.zeros((*raw.shape[:3],4),np.uint8);signal=np.clip((diff-3)/(255-bg[None]).clip(20)*255,0,255).astype(np.uint8)
  rgba[:,:,:,:3]=signal;rgba[:,:,:,3]=keep*255;rgba[~keep]=0
  yy,xx=np.where(keep.any(0));l,top,right,bottom=int(xx.min()),int(yy.min()),int(xx.max()+1),int(yy.max()+1);full=rgba;rgba=rgba[:,top:bottom,l:right];restored=np.zeros_like(full);restored[:,top:bottom,l:right]=rgba;assert np.array_equal(full,restored)
  count,h,w,_=rgba.shape;cols=8;atlas=Image.new('RGBA',(cols*w,((count+7)//8)*h))
  for i,f in enumerate(rgba):atlas.paste(Image.fromarray(f),(i%cols*w,i//cols*h))
  dest=out/(name+'.png');atlas.save(dest);q=dict(anchor='RECIPIENT',path='mage-v240/'+dest.name,width=w,height=h,columns=cols,durationsMs=[50]*count,pivotX=px-l,pivotY=py-top,scale=.42,blend='SCREEN',footOffsetY=0,sourceTiming='OBSERVED_CAPTURE_20FPS',atlasSha256=hashlib.sha256(dest.read_bytes()).hexdigest(),sourceFrames=[dict(videoSha256=digest,timestampSeconds=round(t+i*.05,3),crop=crops[i]) for i in range(count)])
  skills[f'SK_마법사_{n:03}']={'channels':{'RECIPIENT_CONTACT':q}};receipt.append(dict(id=f'SK_마법사_{n:03}',name=name,sourceInterval=[t,t+d],plateInterval=[bt,bt+.15],sourceCrop=box,trim=[l,top,right,bottom],trimRestoration='EXACT_RGBA_PASS',visiblePixels=int(keep.sum()),decodedBytes=atlas.width*atlas.height*4,limitation='ADAPTED inverse-screen matte; filmed actor, scenery and occluded effect pixels excluded. User-confirmed basic4 correspondence; original asset/cadence fidelity not user accepted.'))
  draw.text((0,row*170),name,fill='white')
  for j,i in enumerate(np.linspace(0,count-1,8).astype(int)):
   im=Image.fromarray(rgba[i]);im.thumbnail((140,140));preview.paste(im,(j*140,row*170+25),im)
 (out/'manifest.json').write_text(json.dumps(dict(revision='USER_CORRECTION_V240',sourceVideo=a.video.name,sourceSha256=digest,skills=skills),ensure_ascii=False,indent=2)+'\n')
 source=R/'master/source/mage_v240';source.mkdir(exist_ok=True);(source/'receipt.json').write_text(json.dumps(dict(videoName=a.video.name,videoSha256=digest,mappingAuthority='User explicitly requests only Pote basic4: Mareno/Teramiko/Plamo/Adyuro. Other-circle corrections deferred.',effects=receipt),ensure_ascii=False,indent=2)+'\n')
 review=R.parent/'review/v240';review.mkdir(parents=True,exist_ok=True);preview.save(review/'basic4-source.png');print(receipt)
if __name__=='__main__':main()
