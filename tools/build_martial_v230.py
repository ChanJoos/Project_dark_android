"""Project exact article415458 pixels, not enlarged screen-recording/page margins."""
from pathlib import Path
from PIL import Image,ImageSequence
import numpy as np,json,hashlib,math
from scipy.ndimage import binary_fill_holes,label
R=Path(__file__).resolve().parents[1];S=R/'master/source/martial_v230';O=R/'app/src/main/assets/skill-presentation/user-v228'
m=json.loads((O/'manifest.json').read_text());records=[]
for n in ['035','027','024','025','026','028','029']:
 src=S/(n+'.png');Image.open(src).convert('RGBA').save(O/('icon-'+n+'.png'))
 records.append({'id':'SK_무도가_'+n,'iconSha256':hashlib.sha256(src.read_bytes()).hexdigest(),'gifSha256':hashlib.sha256((S/(n+'.gif')).read_bytes()).hexdigest()})
 if n not in ['026','028','029']:continue
 im=Image.open(S/(n+'.gif'));fs=[np.array(f.convert('RGB'),dtype=float) for f in ImageSequence.Iterator(im)];ds=[f.info['duration'] for f in ImageSequence.Iterator(im)];base=fs[0];tiles=[];durations=[];yy,xx=np.mgrid[:134,:217]
 inds={'026':list(range(9,16)),'028':[1,2,3,4,5],'029':list(range(1,10))}[n]
 for i in inds:
  c=fs[i];diff=c-base
  if n=='028':
   # First recipient only: preserve the observed black/orange impact disk, never its body/UI.
   if i in (1,2):
    cx,cy,rx,ry=(129,70,7,8) if i==1 else (128,74,14.5,14.5)
    mask=((xx-cx)/rx)**2+((yy-cy)/ry)**2<=1
   else:mask=np.zeros(xx.shape,bool)
   rgba=np.dstack([c,np.where(mask,255,0)]).astype('uint8');pivot=[128,74]
  else:
   emission=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1)
   if n=='026':mask=(c[:,:,1]>c[:,:,0]+15)&(c[:,:,1]>c[:,:,2])&(diff[:,:,1]>12)&(xx>35)&(xx<145)&(yy>58)&(yy<128);pivot=[103,111]
   else:mask=(diff.max(2)>10)&(((c[:,:,2]>c[:,:,1]+15)&(c[:,:,0]>c[:,:,1]+5))|((c.min(2)>220)&(diff.min(2)>10)))&(xx>40)&(xx<150)&(yy>35)&(yy<129);pivot=[103,92]
   a=emission.max(2);a[~mask]=0;fg=np.clip(emission/np.maximum(a[:,:,None],.001)*255,0,255);rgba=np.dstack([fg,np.round(a*255)]).astype('uint8')
  rgba[rgba[:,:,3]==0]=0;tiles.append(Image.fromarray(rgba));durations.append(ds[i])
 tiles.append(Image.new('RGBA',(217,134)));durations.append(100)
 atlas=Image.new('RGBA',(217*4,134*math.ceil(len(tiles)/4)))
 for i,t in enumerate(tiles):atlas.paste(t,(i%4*217,i//4*134))
 atlas.save(O/f'fx-{n}.png');ch=m['skills']['SK_무도가_'+n]['channels']['RECIPIENT_CONTACT'];ch.update(width=217,height=134,durationsMs=durations,pivotX=pivot[0],pivotY=pivot[1],blend='NORMAL' if n=='028' else 'SCREEN',frameIndices=inds+[-1],atlasSha256=hashlib.sha256((O/f'fx-{n}.png').read_bytes()).hexdigest(),source='master/source/martial_v230/'+n+'.gif',opaquePixelCounts=[np.count_nonzero(np.array(t)[:,:,3]).item() for t in tiles]);ch['footOffsetY']=0 if n!='028' else -25
(O/'manifest.json').write_text(json.dumps(m,ensure_ascii=False,indent=2)+'\n')
(S/'reference.json').write_text(json.dumps({'article':'https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/415458','acquired':'2026-10-22','method':'Live article img downloadMedia; raw files retained','rows':records,'limitations':['Original GIF frames/timing recovered. Inverse SCREEN alpha remains approximate. Protection retains all visible purple pixels without a caster-shaped exclusion.','Pacheon source blue pillars are monsters, excluded; only green four-side motion from frames9..15. Muyeong first-recipient disk only; purple humanoids and HP bars excluded.']},ensure_ascii=False,indent=2)+'\n')
