"""Original4circle EFA animations matched to user080624 recording.
Format primary reference: eriscorp/dalib EfaFile.cs / Graphics.RenderImage(EfaFrame).
Decode original RGB565 using row pitch, preserve full source frame and pivot.
Only self-alpha blend2 is admitted here; SCREEN retains every faint edge channel.
No captured actor/UI matting, threshold, repaint, symmetry or tile cropping.
"""
from pathlib import Path
import argparse,struct,zlib,json,hashlib,zipfile
import numpy as np
from PIL import Image,ImageDraw
from horror_original_formats import Archive
R=Path(__file__).resolve().parents[1];SRC=R/'master/source/mage_v242';OUT=R/'app/src/main/assets/skill-presentation/mage-v242'
SPECS=[(26,236,'water',[18.0,18.6]),(27,252,'earth',[20.1,20.7]),(28,242,'wind',[18.55,18.95]),(29,239,'fire',[19.5,20.1])]
def sha(b):return hashlib.sha256(b).hexdigest()
def efa(b):
 _,n,ms,blend=struct.unpack_from('<iiiB',b);assert 0<n<100 and ms>0 and blend==2
 frames=[]
 for i in range(n):
  v=struct.unpack_from('<10i2hi6hi',b,64+64*i);off,cs,ds,pitch,count=v[1],v[2],v[3],v[6],v[8];cx,cy=v[10:12];iw,ih,left,top,fw,fh=v[13:19]
  assert left==top==0 and iw==fw and ih==fh and count==pitch*fh and pitch>=fw*2
  stream=zlib.decompressobj();data=stream.decompress(b[64+64*n+off:64+64*n+off+cs]);assert stream.eof and len(data)==ds
  pixels=np.frombuffer(data[:count],'<u2').reshape(fh,pitch//2)[:,:fw]
  rgb=np.stack(((pixels>>11)*255//31,((pixels>>5)&63)*255//63,(pixels&31)*255//31),axis=-1).astype(np.uint8)
  alpha=np.where(rgb.max(axis=-1)>0,255,0).astype(np.uint8)
  frames.append(dict(rgba=np.dstack((rgb,alpha)),pivot=[cx,cy],sourceShape=[fw,fh],pitch=pitch,rgbSha256=sha(rgb.tobytes())))
 return ms,frames

def main():
 p=argparse.ArgumentParser();p.add_argument('--roh',type=Path);p.add_argument('--video',type=Path);p.add_argument('--qa',type=Path);a=p.parse_args();SRC.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
 if a.roh:
  ar=Archive(a.roh)
  with zipfile.ZipFile(SRC/'original_effects.zip','w',zipfile.ZIP_DEFLATED) as z:
   for _,n,_,_ in SPECS:z.writestr(f'efct{n:03}.efa',ar[f'efct{n:03}.efa'])
 skills={};receipts=[];source={};decoded=0
 with zipfile.ZipFile(SRC/'original_effects.zip') as z:
  for sid,n,name,interval in SPECS:
   key=f'efct{n:03}.efa';raw=z.read(key);source[key]=sha(raw);ms,frames=efa(raw)
   l=min(-f['pivot'][0] for f in frames);t=min(-f['pivot'][1] for f in frames);r=max(f['sourceShape'][0]-f['pivot'][0] for f in frames);b=max(f['sourceShape'][1]-f['pivot'][1] for f in frames)
   px=2-l;py=2-t;w=r-l+4;h=b-t+4;cols=8;atlas=Image.new('RGBA',(cols*w,((len(frames)+cols-1)//cols)*h));records=[]
   for i,f in enumerate(frames):
    x=px-f['pivot'][0];y=py-f['pivot'][1];fw,fh=f['sourceShape'];atlas.paste(Image.fromarray(f['rgba']),(i%cols*w+x,i//cols*h+y))
    assert np.array_equal(np.array(atlas.crop((i%cols*w+x,i//cols*h+y,i%cols*w+x+fw,i//cols*h+y+fh))),f['rgba'])
    records.append(dict(frame=i,sourceShape=f['sourceShape'],sourcePivot=f['pivot'],placement=[x,y],rowPitch=f['pitch'],rgbSha256=f['rgbSha256'],rgbaSha256=sha(f['rgba'].tobytes())))
   dest=OUT/(name+'.png');atlas.save(dest);decoded+=atlas.width*atlas.height*4
   q=dict(anchor='RECIPIENT',path='mage-v242/'+dest.name,width=w,height=h,columns=cols,durationsMs=[ms]*len(frames),pivotX=px,pivotY=py,scale=1,blend='SCREEN',filterBitmap=False,footOffsetY=0,sourceTiming='ORIGINAL_EFA_HEADER_INTERVAL',atlasSha256=sha(dest.read_bytes()))
   skills[f'SK_마법사_{sid:03}']={'channels':{'RECIPIENT_CONTACT':q}}
   receipts.append(dict(id=f'SK_마법사_{sid:03}',screenAlias=f'SK_마법사_{sid+4:03}',element=name,source=key,frameIntervalMs=ms,frames=records,videoEvidence=interval,decodedBytes=atlas.width*atlas.height*4,pixelRestoration='EXACT_RGBA_PASS'))
   if a.qa:
    a.qa.mkdir(parents=True,exist_ok=True);sheet=Image.new('RGB',(8*200,220),'#54585b');d=ImageDraw.Draw(sheet)
    for j,i in enumerate(np.linspace(0,len(frames)-1,8).astype(int)):
     im=atlas.crop((i%cols*w,i//cols*h,(i%cols+1)*w,(i//cols+1)*h));im.thumbnail((195,190))
     # SCREEN preview against neutral grey rather than opaque source black outlines.
     f=np.array(im).astype(float);bg=np.full(f.shape[:2]+(3,),84.);mix=255-(255-bg)*(255-f[...,:3])/255;sheet.paste(Image.fromarray(mix.astype(np.uint8)),(j*200,25));d.text((j*200,0),f'{name} {i} / {i*ms}ms',fill='white')
    sheet.save(a.qa/(name+'.jpg'))
 (OUT/'manifest.json').write_text(json.dumps(dict(revision='ORIGINAL_CIRCLE4_V242',skills=skills),ensure_ascii=False,indent=2)+'\n')
 ref=dict(revision='V242',source='https://s3.amazonaws.com/kru-downloads/da/DarkAges741single.exe',installerSha256='dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093',archive='roh.dat',sourceFiles=source,video='Screen_Recording_20261023_080624_YouTube.mp4',videoSha256=sha(a.video.read_bytes()) if a.video else None,rawVideoCommitted=False,mapping='User assigns these four effects to 4circle SCREEN and SINGLE. Original236/252/242/239 visually match star/dust/lightning/flare, supported by retained labelled221564 thirdtier. Third independent reference PENDING_REFERENCE.',sequences=receipts,decodedBytes=decoded,scope='Only singles026..029 and existing screenaliases030..033; screen onepulse per livevisible recipient, SINGLE one target. Basic4/V241 promotions/circle3/cost/damage/learning/world/save unchanged.')
 (SRC/'receipt.json').write_text(json.dumps(ref,ensure_ascii=False,indent=2)+'\n');print('V2424 native EFA sequences originaltiming',decoded,'decoded bytes')
if __name__=='__main__':main()
