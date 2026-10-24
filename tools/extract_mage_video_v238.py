"""Recover observed additive animation pixels; no invented geometry or actor texture.
Input is the user-supplied 20261022_225347 recording, not committed raw footage.
"""
from pathlib import Path
import argparse,hashlib,json,subprocess
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def decode(video,start,duration,box,fps=20):
 x,y,w,h=box
 raw=subprocess.check_output(['ffmpeg','-v','error','-ss',str(start),'-i',str(video),'-t',str(duration),'-vf',f'fps={fps},crop={w}:{h}:{x}:{y}','-f','rawvideo','-pix_fmt','rgb24','-threads','1','-'])
 return np.frombuffer(raw,np.uint8).reshape(-1,h,w,3).copy()
def recover(frames,plate,dark=False):
 obs=frames.astype(np.float32);bg=plate.astype(np.float32)
 # Inverse SCREEN: O = B + F*(1-B). Transparent below quantisation noise.
 signal=np.maximum(0,(obs-bg[None]-5)/(255-bg[None]).clip(15)*255)
 diff=np.max(obs-bg[None],axis=-1)
 keep=(diff>18)&(signal.max(axis=-1)>25)
 if dark:
  r,g,b=obs[:,:,:,0],obs[:,:,:,1],obs[:,:,:,2]
  keep &= (r>g*1.12)&(r>b*1.08)&~((g>r*.45)&(b<g*.85))
 else:
  # Health bar / digits are above the chosen recipient; no captured yellow UI.
  keep[:,:12,:]=False
  r,g,b=obs[:,:,:,0],obs[:,:,:,1],obs[:,:,:,2]
  keep &= ~((r>150)&(g>75)&(g<r*.86)&(b<r*.22))
 # Remove isolated codec noise, retaining actual connected bursts.
 n=np.zeros_like(keep,dtype=np.uint8)
 for dy in (-1,0,1):
  for dx in (-1,0,1):n+=np.roll(np.roll(keep,dy,1),dx,2)
 keep &= n>=3
 rgb=np.clip(signal,0,255).astype(np.uint8)
 rgba=np.zeros((*rgb.shape[:3],4),np.uint8);rgba[:,:,:,:3]=rgb;rgba[:,:,:,3]=keep.astype(np.uint8)*255
 rgba[:,:,:,:3]*=keep[:,:,:,None]
 return rgba

def main():
 p=argparse.ArgumentParser();p.add_argument('video',type=Path);a=p.parse_args()
 dest=R/'app/src/main/assets/skill-presentation/mage-v238';dest.mkdir(parents=True,exist_ok=True)
 # Recipient moves during the recording: follow its leftmost yellow HP bar,
 # then inverse-matte in recipient coordinates. Never subtract a world-space plate.
 from scipy import ndimage
 wide=(180,900,680,470);rawall=decode(a.video,13.42,2.90,wide)
 aligned=[];positions=[]
 for f in rawall:
  z=f.astype(float);r,g,b=z[:,:,0],z[:,:,1],z[:,:,2]
  mask=(r>155)&(g>100)&(g<r*.95)&(b<70)
  labels,count=ndimage.label(mask);candidates=[]
  for sl in ndimage.find_objects(labels):
   if sl is None:continue
   yy,xx=sl;w=xx.stop-xx.start;h=yy.stop-yy.start
   if w>=22 and h<=9 and xx.start<300 and yy.start<290:candidates.append((xx.start,yy.start,w))
  if not candidates:raise RuntimeError('No unambiguous source recipient HP bar')
  x,y,w=min(candidates);left=x-64;top=y+10
  assert left>=0 and top+180<=f.shape[0]
  aligned.append(f[top:top+180,left:left+180]);positions.append([wide[0]+left,wide[1]+top,180,180])
 Image.fromarray(aligned[16]).resize((540,540)).save('/workspace/scratch/2f4d45a68334/video-review/aligned.png')
 aligned=np.stack(aligned);plate=np.percentile(aligned,20,axis=0)
 # Explicit original-recipient silhouette mask. Occluded FX is unknown, not filled.
 r,g,b=plate[:,:,0],plate[:,:,1],plate[:,:,2]
 actor=(b>r*.83)&(g>r*.88)&(r<200)
 actor[:5]=False
 labels,n=ndimage.label(actor);sizes=np.bincount(labels.ravel());sizes[0]=0
 actor=labels==sizes.argmax();actor=ndimage.binary_dilation(actor,iterations=1)
 spec=[('water',26,13.80,.50),('earth',27,15.05,.45),('wind',28,13.42,.24),('fire',29,14.30,.60)]
 channels={};evidence=[];qa=[]
 for name,num,start,duration in spec:
  first=round((start-13.42)*20);count=round(duration*20);raw=aligned[first:first+count]
  rgba=recover(raw,plate)
  # Source effects are offset from the recipient in the original renderer.
  # Keep their own observed lobe, not the statue-centred bounding rectangle.
  yy,xx=np.mgrid[:180,:180];rgb=raw.astype(float);rr,gg,bb=rgb[:,:,:,0],rgb[:,:,:,1],rgb[:,:,:,2]
  if name=='water':
   support=((xx-111)/49)**2+((yy-75)/55)**2<1
   color=(bb>gg*.98)&(bb>rr*.94)&(bb>160)&(rr>120)
   px,py=111,128
  elif name=='fire':
   support=((xx-133)/51)**2+((yy-75)/58)**2<1
   color=(rr>170)&(gg>120)&(rr>bb*1.03)
   px,py=133,128
  elif name=='wind':
   support=((xx-72)/51)**2+((yy-102)/57)**2<1
   color=(bb>155)&(gg>145)&(bb>rr*.95)
   px,py=72,155
  else:
   support=((xx-71)/62)**2+((yy-98)/59)**2<1
   color=(rr>120)&(abs(rr-gg)<28)&(abs(gg-bb)<28)
   px,py=71,151
  keep=support[None]&color
  keep[:,:22]=False
  # Softly suppress the filmed actor core instead of rectangular holes through FX.
  if name=='earth':
   dist=((xx-72)/26)**2+((yy-83)/55)**2
   opacity=np.clip((dist-.35)/.8,0,1)
   rgba[:,:,:,3]=(rgba[:,:,:,3]*opacity[None]).astype(np.uint8)
  if name=='water':rgba[raw.max(axis=-1)<175]=0
  if name=='wind':rgba[raw.max(axis=-1)<170]=0
  if name=='fire':rgba[raw.max(axis=-1)<185]=0
  rgba[~keep]=0
  # Reconstruct occluded bilateral lobes from the visible same-frame counterpart.
  # This is explicitly adapted capture reconstruction, not recovered hidden pixels.
  if name=='fire':
   # The later source frame exposes the whole flare. Reconstruct its filmed
   # growth phase from that real texture, avoiding a reflected actor-shaped hole.
   peak=rgba[min(8,len(rgba)-1)].copy()
   for k in range(min(7,len(rgba))):
    scale=.28+.72*k/6;im=Image.fromarray(peak);nw=round(180*scale);nh=round(180*scale)
    im=im.resize((nw,nh),Image.Resampling.BILINEAR);frame=Image.new('RGBA',(180,180));frame.paste(im,(round(133-133*scale),round(75-75*scale)));rgba[k]=np.array(frame)
  # Water retains the asymmetric visible source lobes, with no mirrored body.

  if name=='earth':
   rgba[:,:,72:134]=rgba[:,:,10:72][:,:,::-1]
  # Recorded combat labels are red; reject their residual antialiased edge.
  if name=='wind':rgba[(rr>gg*1.12)&(rr>bb*.95)]=0
  qa.append((name,rgba));channels[f'SK_마법사_{num:03d}']=(name,rgba,px,py,.42,start,positions[first:first+count])
 # The dark-seal close camera holds still for the second cast. Capture actual red skull/lines.
 boxdark=(235,950,390,480)
 darkbase=decode(a.video,3.80,.28,boxdark)
 dark=decode(a.video,4.20,1.30,boxdark)
 # Dark tiles/actors are stable; color gate rejects purple bodies and yellow names.
 plate=np.median(darkbase,axis=0)
 rgba=recover(dark,plate,True)
 yy,xx=np.mgrid[:480,:390];ellipse=((xx-190)/181)**2+((yy-260)/186)**2<1
 rgba[:,~ellipse,:]=0
 color=dark.astype(float);good=(color[:,:,:,0]>130)&(color[:,:,:,1]>55)&(color[:,:,:,2]>55);rgba[~good]=0
 qa.append(('dark-mark',rgba));channels['SK_마법사_046']=('dark-mark',rgba,190,380,.22,4.20,boxdark)
 for sid,(name,frames,px,py,scale,start,box) in channels.items():
  count,h,w,_=frames.shape;cols=min(8,count);rows=(count+cols-1)//cols;atlas=Image.new('RGBA',(cols*w,rows*h))
  for i,f in enumerate(frames):atlas.paste(Image.fromarray(f),(i%cols*w,i//cols*h))
  path=dest/(name+'.png');atlas.save(path)
  channel=dict(anchor='RECIPIENT',path='mage-v238/'+path.name,atlasSha256=sha(path),width=w,height=h,columns=cols,durationsMs=[50]*count,pivotX=px,pivotY=py,scale=scale,blend='SCREEN',footOffsetY=0,sourceTiming='OBSERVED_CAPTURE_20FPS',sourceFrames=[dict(videoSha256=sha(a.video),timestampSeconds=round(start+i*.05,3),crop=box[i] if isinstance(box[0],list) else list(box)) for i in range(count)])
  evidence.append(dict(id=sid,sourceFrameCount=count,sourceInterval=[start,round(start+count*.05,3)],crop=box,atlasSha256=sha(path),limitation='Video-derived pixels and observed timing. Filmed recipient silhouettes/UI removed; source particle lobe registered to the recipient. Bilateral lobes occluded by filmed actors are reflected from a visible same-frame lobe (ADAPTED reconstruction). This reconstruction is ADAPTED; occluded originals remain unknown.'))
  channels[sid]=dict(channels={'RECIPIENT_CONTACT':channel})
 manifest=dict(revision='USER_VIDEO_V238',sourceVideoSha256=sha(a.video),sourceVideoName=a.video.name,skills=channels,screenAliases={f'SK_마법사_{30+i:03d}':f'SK_마법사_{26+i:03d}' for i in range(4)})
 (dest/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
 source=R/'master/source/mage_video_v238';source.mkdir(parents=True,exist_ok=True)
 (source/'receipt.json').write_text(json.dumps(dict(videoSha256=sha(a.video),videoName=a.video.name,videoDuration=19.896833,rawVideoCommitted=False,extraction='inverse SCREEN from static temporal/background plate; source-color gates reject bodies and labels; no generative imagery',effects=evidence,mapping='Names matched against retained labelled article221564 lower-third captures. Video has no spell-name captions; mapping is visual correspondence, not OCR.',scope='Single dark mark;4circle four single-target spells;existing screen4 aliases reuse same sequences per live recipient. No new advancement/whole-dark-mark ability.'),ensure_ascii=False,indent=2)+'\n')
 # Disposable QA strips over mid grey for source/occlusion review.
 out=Path('/workspace/scratch/2f4d45a68334/video-review');out.mkdir(exist_ok=True)
 for name,frames in qa:
  count,h,w,_=frames.shape;thumb=140 if name!='dark-mark' else 170
  samples=list(range(0,count,max(1,count//8)))[:8];grid=Image.new('RGB',(thumb*len(samples),thumb+22),'#54585b');d=ImageDraw.Draw(grid)
  for j,i in enumerate(samples):
   im=Image.fromarray(frames[i]);im.thumbnail((thumb,thumb));grid.paste(im,(j*thumb,22),im);d.text((j*thumb,0),str(i*.05),fill='white')
  grid.save(out/(name+'-strip.png'))
 print('V238',[(r['id'],r['sourceFrameCount']) for r in evidence])
if __name__=='__main__':main()
