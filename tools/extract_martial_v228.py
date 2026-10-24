from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,json,hashlib,zipfile,io,math
from scipy.ndimage import binary_closing,binary_fill_holes,label
from scipy.spatial import ConvexHull
import argparse,tempfile,subprocess
parser=argparse.ArgumentParser();parser.add_argument('video',type=Path);args=parser.parse_args();video=args.video.resolve();work=Path(tempfile.mkdtemp(prefix='martial-v228-'));(work/'dense').mkdir();subprocess.run(['ffmpeg','-v','error','-i',str(video),'-t','21','-vf','fps=15',str(work/'dense/f-%04d.png')],check=True)
root=Path(__file__).resolve().parents[1];source=root/'master/source/martial_v228';source.mkdir(parents=True,exist_ok=True);out=root/'app/src/main/assets/skill-presentation/user-v228';out.mkdir(parents=True,exist_ok=True)
names={'035':'허공답보','027':'트리플어택','024':'연천단각','025':'붕신선각','026':'파천각','028':'무영신공','029':'자기보호'}
icon_spec={'035':(0,(78,531,166,619)),'027':(0,(84,1085,174,1175)),'024':(0,(76,1650,166,1740)),'025':(6,(76,1825,166,1915)),'026':(15,(66,446,156,536)),'028':(15,(72,1069,162,1161)),'029':(15,(76,1663,166,1755))}
clips={'035':(2.8,4.6,(348,401,958,778)),'027':(2.8,4.6,(375,955,985,1332)),'024':(9.1,11.9,(358,886,969,1263)),'025':(9.1,11.9,(358,1440,969,1817)),'026':(16.6,19.9,(350,305,961,682)),'028':(16.6,19.9,(364,915,974,1292)),'029':(16.6,19.9,(367,1528,977,1905))}
manifest={'skills':{}};icons={};records=[];reviews=[]
with zipfile.ZipFile(source/'observed_clips.zip','w',zipfile.ZIP_DEFLATED) as z:
 def put(name,data):
  info=zipfile.ZipInfo(name,date_time=(2026,10,22,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,data)
 for n,name in names.items():
  t,box=icon_spec[n];icon=Image.open(work/'dense'/f'f-{t*15+1:04}.png').crop(box).convert('RGB');buf=io.BytesIO();icon.save(buf,format='PNG');put(f'{n}/icon.png',buf.getvalue());icon.resize((40,40),Image.Resampling.LANCZOS).save(out/f'icon-{n}.png');icons['SK_무도가_'+n]=f'skill-presentation/user-v228/icon-{n}.png'
  start,end,box=clips[n];frames=[]
  for idx in range(round(start*15),round(end*15)):
   im=Image.open(work/'dense'/f'f-{idx+1:04}.png').crop(box).resize((240,148),Image.Resampling.NEAREST).convert('RGB');frames.append(np.array(im,dtype=float));buf=io.BytesIO();im.save(buf,format='PNG');pass # full original video retained in the attached durable source; selected observation is retained below
  base=np.percentile(frames,25,axis=0);tiles=[];counts=[]
  yy,xx=np.mgrid[:148,:240]
  for c in frames:
   r,g,b=c[:,:,0],c[:,:,1],c[:,:,2];diff=c-base;delta=diff.max(2)>30
   cyan=(g>r+20)&(b>r+18)&(g>120);white=(c.min(2)>215)&(diff.min(2)>35)
   if n=='026':mask=(xx>35)&(xx<180)&delta&(((b>r+15)&(b>g+5)&(b>130))|((g>r+20)&(b>r+10))|white)
   elif n=='029':mask=delta&(((b>130)&(r>g*1.25)&(b>g*1.3))|white);mask&=~((xx>98)&(xx<124)&(yy>57)&(yy<133))
   elif n=='028':mask=binary_fill_holes(binary_closing((r>65)&(r>g*1.6)&(g>8)&(b<r*.6)&(xx>125)&(xx<164)&(yy>67)&(yy<116),iterations=1))
   elif n=='025':mask=delta&(((g>r+25)&(g>b+10)&(g>130))|cyan|white)&(xx>112)&(xx<177)&(yy>45)&(yy<135)
   elif n=='035':mask=delta&(r>180)&(g>155)&(b<155)&(xx>115)&(xx<160)&(yy>25)&(yy<100)
   else:mask=delta&(cyan|white)&(xx>121)&(xx<172)&(yy>40)&(yy<138)
   emission=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1);alpha=np.max(emission,axis=2);alpha[~mask]=0;fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255);rgba=np.dstack((fg,np.round(alpha*255))).astype('uint8');rgba[~mask]=0
   if n=='028':
    labels,nn=label(mask);sizes=np.bincount(labels.ravel());sizes[0]=0
    if sizes.max()>4:
     mask=labels==sizes.argmax();points=np.argwhere(mask);hull=ConvexHull(points);poly=Image.new('1',(240,148));ImageDraw.Draw(poly).polygon([(int(points[k,1]),int(points[k,0])) for k in hull.vertices],fill=1);mask=np.array(poly,dtype=bool)
    rgba=np.dstack((c,np.where(mask,255,0))).astype('uint8');rgba[~mask]=0
   counts.append(int(np.count_nonzero(rgba[:,:,3])));tiles.append(rgba)
  # Keep one observed contiguous effect burst; avoid baking several looping demonstrations into one cast.
  active=[i for i,c in enumerate(counts) if c>=3];groups=[]
  for i in active:
   if not groups or i>groups[-1][-1]+3:groups.append([i])
   else:groups[-1].append(i)
  assert groups,(n,counts)
  group=max(groups,key=lambda ids:sum(counts[i] for i in ids));first,last=group[0],group[-1]+1
  if last-first>25:last=first+25
  chosen=tiles[first:last];atlas=Image.new('RGBA',(240*4,148*math.ceil(len(chosen)/4)))
  for i,tile in enumerate(chosen):atlas.paste(Image.fromarray(tile),(i%4*240,i//4*148))
  atlas.save(out/f'fx-{n}.png');caster=n in ('026','029');pivot=[110,110] if caster else [140,122]
  channel=dict(path=f'user-v228/fx-{n}.png',width=240,height=148,columns=4,durationsMs=[67]*len(chosen),pivotX=pivot[0],pivotY=pivot[1],scale=1.0,blend='NORMAL' if n=='028' else 'SCREEN',anchor='CASTER' if caster else 'RECIPIENT',registration='VISUAL_CENTER',footOffsetY=0,atlasSha256=hashlib.sha256((out/f'fx-{n}.png').read_bytes()).hexdigest(),source='libfile_2a630cf24fac81919793ba88d449ed0e',frameIndices=list(range(round(start*15)+first,round(start*15)+last)),opaquePixelCounts=counts[first:last])
  manifest['skills']['SK_무도가_'+n]={'name':name,'channels':{'RECIPIENT_CONTACT':channel}}
  records.append(dict(id='SK_무도가_'+n,name=name,iconAt=t,iconCrop=icon_spec[n][1],clipSeconds=[start,end],clipCrop=box,nonzeroPixels=max(counts),frames=len(chosen)))
  sample=Image.fromarray(frames[int(np.argmax(counts))].astype('uint8'));buf=io.BytesIO();sample.save(buf,format='PNG');put(f'{n}/source-observation.png',buf.getvalue())
  best=Image.fromarray(tiles[int(np.argmax(counts))]);bg=Image.new('RGBA',best.size,'#26302a');bg.alpha_composite(best);reviews.append((n,bg.convert('RGB').resize((480,296))))
(out/'icons.json').write_text(json.dumps(icons,ensure_ascii=False,indent=2)+'\n');(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
record=dict(revision='V228_USER_LABELLED_MARTIAL_VIDEO',date='2026-10-22',classification='U labelled order + V community demonstration',sourceVideo='Screen_Recording_20261022_140905_NAVER.mp4',libraryFileId='libfile_2a630cf24fac81919793ba88d449ed0e',sourceSha256=hashlib.sha256(video.read_bytes()).hexdigest(),article='https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/415458',clipsArchiveSha256=hashlib.sha256((source/'observed_clips.zip').read_bytes()).hexdigest(),rows=records,limitations=['Video samples at15fps; original GIF timing/alpha unavailable.','Approximate inverse SCREEN matting and colour masks. Captured people/floor/HP bars excluded; occluded pixels remain absent.','One recorded facing only; runtime retains approved body and selected directional rig.','Raw video attachment remains unchanged and is the full regeneration input; ZIP retains seven original icon crops and representative observations; no generated artwork.'])
(source/'reference.json').write_text(json.dumps(record,ensure_ascii=False,indent=2)+'\n')
review=Image.new('RGB',(480*4,320*2),'#333')
for i,(n,im) in enumerate(reviews):review.paste(im,(i%4*480,i//4*320+20));ImageDraw.Draw(review).text((i%4*480+5,i//4*320+4),n,fill='white')
review.save(work/'matted-review.png');print('Review:',work/'matted-review.png');print(json.dumps(records,ensure_ascii=False))
