"""V243 approved circle3 preview -> complete original EFA recipient sequences.
Source decoding follows retained DALib EfaFrame/EfaFile format. Blending1 is additive;
blending2 is per-channel SCREEN. Keep faint RGB565 pixels, row pitch, header interval
and per-frame foot. No source masks, invented frames or tile-sized clipping.
"""
from pathlib import Path
import struct,zlib,json,hashlib,zipfile,argparse
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];S=R/'master/source/mage_v243';O=R/'app/src/main/assets/skill-presentation/mage-v243'
SPECS=[(13,235,'water'),(14,251,'earth'),(15,241,'wind'),(16,238,'fire')]
def sha(b):return hashlib.sha256(b).hexdigest()
def decode(raw):
 _,count,interval,blend=struct.unpack_from('<iiiB',raw);assert 0<count<100 and interval>0 and blend in (1,2);frames=[]
 for j in range(count):
  v=struct.unpack_from('<10i2hi6hi',raw,64+64*j);off,cs,ds,pitch,bc=v[1],v[2],v[3],v[6],v[8];cx,cy=v[10:12];iw,ih,left,top,fw,fh=v[13:19]
  assert left==top==0 and iw==fw and ih==fh and bc==pitch*fh and pitch>=fw*2
  data=zlib.decompress(raw[64+64*count+off:64+64*count+off+cs]);assert len(data)==ds
  words=np.frombuffer(data[:bc],'<u2').reshape(fh,pitch//2)[:,:fw].astype(np.uint32)
  rgb=np.stack(((words>>11)*255//31,((words>>5)&63)*255//63,(words&31)*255//31),axis=-1).astype(np.uint8)
  rgba=np.dstack((rgb,np.where(words!=0,255,0).astype(np.uint8)))
  frames.append(dict(rgba=rgba,pivot=[cx,cy],size=[fw,fh],pitch=pitch))
 return interval,blend,frames

def main():
 p=argparse.ArgumentParser();p.add_argument('--roh',type=Path);args=p.parse_args();S.mkdir(exist_ok=True);O.mkdir(exist_ok=True)
 if args.roh:
  from horror_original_formats import Archive
  a=Archive(args.roh)
  with zipfile.ZipFile(S/'original_effects.zip','w',zipfile.ZIP_DEFLATED) as z:
   for _,n,_ in SPECS:z.writestr(f'efct{n:03}.efa',a[f'efct{n:03}.efa'])
 entries={};records=[];total=0
 with zipfile.ZipFile(S/'original_effects.zip') as z:
  for sid,n,element in SPECS:
   key=f'efct{n:03}.efa';raw=z.read(key);interval,blend,frames=decode(raw)
   l=min(-f['pivot'][0] for f in frames);t=min(-f['pivot'][1] for f in frames);r=max(f['size'][0]-f['pivot'][0] for f in frames);b=max(f['size'][1]-f['pivot'][1] for f in frames)
   w=r-l+4;h=b-t+4;px=2-l;py=2-t;cols=8;atlas=Image.new('RGBA',(w*cols,h*((len(frames)+cols-1)//cols)));rec=[]
   for i,f in enumerate(frames):
    x=px-f['pivot'][0];y=py-f['pivot'][1];fw,fh=f['size'];atlas.paste(Image.fromarray(f['rgba']),(i%cols*w+x,i//cols*h+y))
    assert np.array_equal(np.array(atlas.crop((i%cols*w+x,i//cols*h+y,i%cols*w+x+fw,i//cols*h+y+fh))),f['rgba'])
    rec.append(dict(index=i,sourcePivot=f['pivot'],sourceSize=f['size'],placement=[x,y],rowPitch=f['pitch'],rgbaSha256=sha(f['rgba'].tobytes())))
   path=O/(element+'.png');atlas.save(path);total+=atlas.width*atlas.height*4
   q=dict(anchor='RECIPIENT',path='mage-v243/'+path.name,width=w,height=h,columns=cols,durationsMs=[interval]*len(frames),pivotX=px,pivotY=py,scale=1,blend='ADDITIVE' if blend==1 else 'SCREEN',filterBitmap=False,footOffsetY=0,sourceTiming='ORIGINAL_EFA_HEADER_INTERVAL',atlasSha256=sha(path.read_bytes()))
   entries[f'SK_마법사_{sid:03}']={'channels':{'RECIPIENT_CONTACT':q}}
   records.append(dict(id=f'SK_마법사_{sid:03}',element=element,source=key,sourceSha256=sha(raw),blendCode=blend,intervalMs=interval,frames=rec))
 (O/'manifest.json').write_text(json.dumps(dict(revision='ORIGINAL_CIRCLE3_V243',skills=entries),ensure_ascii=False,indent=2)+'\n')
 (S/'receipt.json').write_text(json.dumps(dict(revision='V243',status='USER_APPROVED_PREVIEW',source='Retained Original741 roh.dat',installerSha256='dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093',preview='CIRCLE3_MOTION_PREVIEW.gif;CIRCLE3_FRAME_PREVIEW.png',screenshotEvidence=['1000060725.jpg','1000060723.jpg','1000060721.jpg','1000060719.jpg'],identity='User approved this exact four-sequence preview2026-10-23; third independent source PENDING_REFERENCE',timing='Original EFA interval, one full sequence; Korean gameplay repetition not independently verified',sequences=records,decodedBytes=total),ensure_ascii=False,indent=2)+'\n')
 print('V243 four original circle3 atlases, source pixels intact, decoded bytes',total)
if __name__=='__main__':main()
