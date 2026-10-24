"""V249 complete source effects. Never matte actors, mask effect pixels or crop to tiles.
EPF foot-table/order preserved; EFA header pitch/interval/blend preserved.
Venomi uses a complete original green particle sequence as ADAPTED visual assignment;
no independently identified Korean Venomi animation is claimed.
"""
from pathlib import Path
import argparse,json,zipfile,hashlib
import numpy as np
from PIL import Image
from horror_original_formats import Archive,palette
from build_mage_v241 import epf
from build_mage_v243 import decode
R=Path(__file__).resolve().parents[1];O=R/'app/src/main/assets/skill-presentation/status-v249';S=R/'master/source/status_v249'
def sha(x):return hashlib.sha256(x).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--roh',type=Path);a=p.parse_args();O.mkdir(parents=True,exist_ok=True);S.mkdir(parents=True,exist_ok=True)
 specs=[('heal',4,'epf',100),('confusion',208,'epf',60),('reinforce',278,'efa',None),('sleep',364,'efa',None),('poison',196,'epf',100)]
 if a.roh:
  ar=Archive(a.roh);names={'eff000.pal','effect.tbl'}
  for _,n,fmt,_ in specs:
   names.add(f'efct{n:03}.{fmt}')
   if fmt=='epf':names.add(f'efct{n:03}.tbl')
  with zipfile.ZipFile(S/'original_effects.zip','w',zipfile.ZIP_DEFLATED) as z:
   for n in sorted(names):z.writestr(n,ar[n])
 with zipfile.ZipFile(S/'original_effects.zip') as z:raw={n:z.read(n) for n in z.namelist()}
 video=R.parent/'upload/1000060796.mp4'
 video_hash=sha(video.read_bytes()) if video.is_file() else json.loads((S/'receipt.json').read_text())['referenceVideoSha256']
 assert len(video_hash)==64
 channels={};records=[]
 for name,n,fmt,project_ms in specs:
  key=f'efct{n:03}';frames=[]
  if fmt=='epf':
   _,_,fs=epf(raw[key+'.epf']);pal=palette(raw['eff000.pal']);piv=np.frombuffer(raw[key+'.tbl'],'<i2').reshape(-1,2);order=list(map(int,raw['effect.tbl'].decode('ascii').splitlines()[n].split()))
   for i in order:
    x,y,z=fs[i];frames.append(dict(rgba=pal[z],pivot=[int(piv[i,0])-x,int(piv[i,1])-y],size=[z.shape[1],z.shape[0]],index=i))
   ms=project_ms;blend='NORMAL';timing='ORIGINAL_ORDER_PROJECT_INTERVAL'
  else:
   ms,b,frames=decode(raw[key+'.efa']);blend='ADDITIVE' if b==1 else 'SCREEN';timing='ORIGINAL_EFA_HEADER'
  l=min(-f['pivot'][0] for f in frames);t=min(-f['pivot'][1] for f in frames);r=max(f['size'][0]-f['pivot'][0] for f in frames);b=max(f['size'][1]-f['pivot'][1] for f in frames)
  w=r-l+4;h=b-t+4;px=2-l;py=2-t;cols=8;atlas=Image.new('RGBA',(cols*w,((len(frames)+cols-1)//cols)*h));rec=[]
  for i,f in enumerate(frames):
   x=px-f['pivot'][0];y=py-f['pivot'][1];fw,fh=f['size'];atlas.paste(Image.fromarray(f['rgba']),(i%cols*w+x,i//cols*h+y));assert np.array_equal(np.array(atlas.crop((i%cols*w+x,i//cols*h+y,i%cols*w+x+fw,i//cols*h+y+fh))),f['rgba'])
   rec.append(dict(index=f.get('index',i),sourcePivot=f['pivot'],sourceSize=f['size'],placement=[x,y],rgbaSha256=sha(f['rgba'].tobytes()),rowPitch=f.get('pitch')))
  file=O/(name+'.png');atlas.save(file)
  q=dict(anchor='RECIPIENT',path='status-v249/'+file.name,width=w,height=h,columns=cols,durationsMs=[ms]*len(frames),pivotX=px,pivotY=py,scale=1,blend=blend,filterBitmap=False,footOffsetY=0,atlasSha256=sha(file.read_bytes()),sourceTiming=timing)
  channels[name]=q;records.append(dict(name=name,source=key+'.'+fmt,frames=rec,pixelRestoration='EXACT_RGBA_PASS',identity='ADAPTED_VENOMI_GREEN_PARTICLE_ASSIGNMENT' if name=='poison' else 'REFERENCE_SHAPE_MATCH_SOURCE_FRAMES',timing=timing))
 skills={}
 for id,name in [('SK_마법사_044','reinforce'),('SK_공통_010','heal'),('SK_무도가_032','heal'),('SK_성직자_058','heal'),('SK_마법사_056','heal'),('SK_마법사_006','confusion')]:skills[id]={'channels':{'RECIPIENT_CONTACT':channels[name]}}
 (O/'manifest.json').write_text(json.dumps(dict(revision='V249',skills=skills,persistent={'SLEEP':channels['sleep'],'POISON':channels['poison']}),ensure_ascii=False,indent=2)+'\n')
 (S/'receipt.json').write_text(json.dumps(dict(revision='V249',source='Retained741 roh.dat',installerSha256='dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093',sourceFiles={n:sha(b) for n,b in raw.items()},sequences=records,referenceVideo='1000060796.mp4',referenceVideoSha256=video_hash,venomiReference='https://lod.nexon.com/board/1493172227/7518',venomiEvidence='Nexon-hosted community text confirms timed poison, not an animation identification. Green original particle art is an explicit project-adapted assignment; existing poison balance retained.'),ensure_ascii=False,indent=2)+'\n')
 print('V249 complete source5 sequences PASS')
if __name__=='__main__':main()
