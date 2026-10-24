"""Lossless original EPF promotion frames; video identifies each spell.
No screen matting, actor holes, recolouring, reflection or tile-sized crop.
Format: eriscorp/dalib EpfFile.cs. efctNNN.tbl stores source foot pivots,
effect.tbl stores playback frame indices (NOT durations). Cadence calibrated
from the user recording remains project-adapted, not a claimed client tick rate.
"""
from pathlib import Path
import argparse,struct,json,hashlib,zipfile
import numpy as np
from PIL import Image,ImageDraw
from horror_original_formats import Archive,palette
R=Path(__file__).resolve().parents[1];SRC=R/'master/source/mage_v241';OUT=R/'app/src/main/assets/skill-presentation/mage-v241'
# Only exact visual correspondences, including the seal's visible skull/flash phases.
SPECS=[(39,101,'ice',50),(40,103,'quake',50),(41,97,'storm',100),(42,102,'flare',50),(45,87,'protection',65),(46,104,'dark-mark',78),(51,157,'delis',80),(52,91,'fortress',85)]
def sha(b):return hashlib.sha256(b).hexdigest()
def epf(b):
 n,w,h,_,toc=struct.unpack_from('<hhh2si',b);frames=[]
 for i in range(n):
  top,left,bottom,right,start,end=struct.unpack_from('<4h2i',b,12+toc+16*i);size=(bottom-top)*(right-left)
  assert size>=0 and 12+start+size<=12+toc
  z=np.frombuffer(b[12+start:12+start+size],np.uint8).reshape(bottom-top,right-left).copy()
  frames.append((left,top,z))
 return w,h,frames

def main():
 p=argparse.ArgumentParser();p.add_argument('--roh',type=Path);p.add_argument('--qa',type=Path);a=p.parse_args();SRC.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
 if a.roh:
  ar=Archive(a.roh);names=['eff000.pal','effpal.tbl','effect.tbl']
  for _,n,_,_ in SPECS:names += [f'efct{n:03}.epf',f'efct{n:03}.tbl']
  with zipfile.ZipFile(SRC/'original_effects.zip','w',zipfile.ZIP_DEFLATED) as z:
   for name in names:z.writestr(name,ar[name])
 with zipfile.ZipFile(SRC/'original_effects.zip') as z:raw={n:z.read(n) for n in z.namelist()}
 pal=palette(raw['eff000.pal']);orders=raw['effect.tbl'].decode('ascii').splitlines();skills={};receipt=[];decoded=0
 for sid,n,name,ms in SPECS:
  key=f'efct{n:03}';w,h,frames=epf(raw[key+'.epf']);order=list(map(int,orders[n].split()))
  pivots=np.frombuffer(raw[key+'.tbl'],'<i2').reshape(-1,2);assert max(order)<len(frames)
  # Restore each source frame relative to its own foot, including content that extends outside header bounds.
  left=min(frames[i][0]-int(pivots[i,0]) for i in order);top=min(frames[i][1]-int(pivots[i,1]) for i in order)
  right=max(frames[i][0]+frames[i][2].shape[1]-int(pivots[i,0]) for i in order);bottom=max(frames[i][1]+frames[i][2].shape[0]-int(pivots[i,1]) for i in order)
  pad=2;cw=right-left+2*pad;ch=bottom-top+2*pad;px=pad-left;py=pad-top;cols=8;atlas=Image.new('RGBA',(cols*cw,((len(order)+cols-1)//cols)*ch));records=[]
  for j,i in enumerate(order):
   x,y,indices=frames[i];assert min(pivots[i])>=0
   rgba=pal[indices];ox=x-int(pivots[i,0])+px;oy=y-int(pivots[i,1])+py
   assert ox>=pad and oy>=pad and ox+indices.shape[1]<=cw-pad and oy+indices.shape[0]<=ch-pad
   atlas.paste(Image.fromarray(rgba),(j%cols*cw+ox,j//cols*ch+oy))
   # Exact palette RGBA restoration, including intentionally transparent source pixels.
   restored=np.array(atlas.crop((j%cols*cw+ox,j//cols*ch+oy,j%cols*cw+ox+indices.shape[1],j//cols*ch+oy+indices.shape[0])))
   assert np.array_equal(restored,rgba)
   records.append(dict(frame=i,sourceBounds=[x,y,x+indices.shape[1],y+indices.shape[0]],sourcePivot=pivots[i].astype(int).tolist(),placement=[ox,oy],rgbaSha256=sha(rgba.tobytes())))
  dest=OUT/(name+'.png');atlas.save(dest);decoded+=atlas.width*atlas.height*4
  q=dict(anchor='RECIPIENT',path='mage-v241/'+dest.name,width=cw,height=ch,columns=cols,durationsMs=[ms]*len(order),pivotX=px,pivotY=py,scale=1,blend='NORMAL',filterBitmap=False,footOffsetY=0,sourceTiming='ORIGINAL_FRAME_ORDER_VIDEO_CALIBRATED_PROJECT_TICK',atlasSha256=sha(dest.read_bytes()))
  skills[f'SK_마법사_{sid:03}']={'channels':{'RECIPIENT_CONTACT':q}}
  receipt.append(dict(id=f'SK_마법사_{sid:03}',source=key+'.epf',playbackOrder=order,sourcePivotTable=key+'.tbl',frames=records,originalCanvas=[w,h],decodedBytes=atlas.width*atlas.height*4,pixelRestoration='EXACT_RGBA_PASS',sourcePixelCount=sum(int(np.count_nonzero(frames[i][2])) for i in order),videoEvidence={'dark-mark':[38.7,40.65],'ice':[62.1,62.95],'quake':[85,88],'storm':[70.3,70.85],'flare':[78.4,79],'protection':[46,47.5],'delis':[51.35,52.5],'fortress':[56.9,58.05]}[name]))
  if a.qa:
   a.qa.mkdir(parents=True,exist_ok=True);sheet=Image.new('RGB',(8*170,210),'#54585b');d=ImageDraw.Draw(sheet)
   for k,j in enumerate(np.linspace(0,len(order)-1,8).astype(int)):
    im=atlas.crop((j%cols*cw,j//cols*ch,(j%cols+1)*cw,(j//cols+1)*ch));im.thumbnail((165,185));sheet.paste(im,(k*170,25),im);d.text((k*170,0),f'{name} frame{order[j]}',fill='white')
   sheet.save(a.qa/(name+'.jpg'))
 manifest=dict(revision='ORIGINAL_PROMOTION_V241',skills=skills);(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
 ref=dict(revision='V241',source='https://s3.amazonaws.com/kru-downloads/da/DarkAges741single.exe',installerSha256='dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093',archive='roh.dat',sourceFiles={n:sha(b) for n,b in raw.items()},video='Screen_Recording_20261022_235706_YouTube.mp4',videoSha256='b90ff1c951bab421f470a5ee6206bff7c40468f4a2dcb3f3e2dbf6fc2befae94',mapping='Visual correspondence to user recording and labelled retained254766; original US client bytes, Korean spell identity supported by footage; third independent reference PENDING_REFERENCE.',decodedBytes=decoded,sequences=receipt,unchanged='Element reinforcement044 remains prior source: no certain original animation identity. Other spells, gameplay rules, Pote basic4 and hunting maps unchanged.')
 (SRC/'receipt.json').write_text(json.dumps(ref,ensure_ascii=False,indent=2)+'\n');print('V241',len(skills),'lossless original sequences',decoded,'bytes')
if __name__=='__main__':main()
